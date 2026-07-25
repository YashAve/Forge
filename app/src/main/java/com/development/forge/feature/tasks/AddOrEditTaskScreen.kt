package com.development.forge.feature.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.components.ForgeSectionLabel
import com.development.forge.core.components.ForgeTaskTopBar
import com.development.forge.core.components.OnForgeOnBack
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme
import com.development.forge.domain.model.Priority
import com.development.forge.domain.model.Task
import com.development.forge.domain.model.tasks
import com.development.forge.feature.tasks.components.ForgeDatePickerDialog
import com.development.forge.feature.tasks.components.ForgePickerField
import com.development.forge.feature.tasks.components.ForgePrioritySegmented
import com.development.forge.feature.tasks.components.ForgeSelectionChip
import com.development.forge.feature.tasks.components.ForgeTimePickerDialog
import com.development.forge.feature.tasks.components.NakedValidationText

/**
 * DESCRIPTION — AddOrEditTaskScreen
 *
 * This one screen handles BOTH creating a new task AND editing an existing one.
 * How it knows which mode it's in: it takes a parameter `editingTask: Task?`.
 * If that is null, the user is creating a new task. If it holds a task, the user
 * is editing that task. The word "editing" below always means "editingTask is
 * not null".
 *
 * ═══════════════════════════════════════════════════════════════
 * THE STATE THIS SCREEN HOLDS
 * ═══════════════════════════════════════════════════════════════
 * "State" here means: values the screen remembers and updates as the user types
 * and taps. Each one uses `remember { mutableStateOf(...) }`. When editing, each
 * starts from the existing task's value; when creating, each starts empty/default.
 *
 *   title        : String  — starts as editingTask?.title ?: ""
 *   notes        : String  — starts as editingTask?.notes ?: ""
 *   dueDate      : String  — the picked date, e.g. "2026-07-25". Empty = none.
 *   dueTime      : String  — the picked time, e.g. "6:00 PM". Empty = none.
 *   priority     : Priority — starts as editingTask?.priority ?: Priority.MED
 *   project      : String  — the project name. Empty = none.
 *   subtasks     : a list of small editable subtask objects (explained below)
 *   remind       : Boolean — is the reminder window turned on? default false
 *   remindStart  : String  — e.g. "9:00 AM"
 *   remindEnd    : String  — e.g. "6:00 PM"
 *   openPicker   : which picker dialog is currently open. One of:
 *                  null (none), "date", "dueTime", "remindStart", "remindEnd".
 *                  This single value decides which dialog shows. Only one dialog
 *                  can be open at a time, so one variable is enough.
 *   expanded     : Boolean — is the "More details" section open?
 *                  IMPORTANT: it starts as `editing`. So when editing an existing
 *                  task, the details are open immediately (you want to see
 *                  everything). When creating new, it starts closed (fast capture).
 *
 * canSave : a derived boolean = title.trim().isNotEmpty()
 *   "derived" means it's computed from title, not stored separately. Use
 *   `derivedStateOf { title.trim().isNotEmpty() }` so things reading canSave only
 *   recompose when it flips from false to true, not on every keystroke.
 *
 * ═══════════════════════════════════════════════════════════════
 * THE SUBTASK LIST — what each item is
 * ═══════════════════════════════════════════════════════════════
 * `subtasks` is a list where each item needs three things:
 *   - title: String (editable text)
 *   - isDone: Boolean (whether it's checked — but NOT editable on this screen)
 *   - isNew: Boolean — a flag meaning "this row was just added, so autofocus it"
 * Since your real Subtask model only has (id, title, isDone), for THIS screen you
 * can use a small screen-local class OR add a transient `isNew` you strip out on
 * save. Simplest: a local `data class EditableSubtask(val title, val isDone, val
 * isNew)` used only inside this screen. On save you convert them to real Subtasks
 * and drop the empty ones.
 *
 * Three operations on this list (all just produce a NEW list — never mutate in
 * place, because Compose only recomposes when the list reference changes):
 *   - setSubtaskTitle(index, newText): replace the item at `index` with a copy
 *     that has the new title.
 *   - deleteSubtask(index): remove the item at `index`.
 *   - addSubtask(): append a new empty EditableSubtask(title="", isDone=false,
 *     isNew=true) to the end.
 *
 * ═══════════════════════════════════════════════════════════════
 * OVERALL LAYOUT — a Column filling the whole screen height
 * ═══════════════════════════════════════════════════════════════
 * Top to bottom:
 *   1. A fixed top bar (does NOT scroll)
 *   2. A scrolling area below it (takes all remaining height, via weight(1f) +
 *      verticalScroll) — everything else lives in here.
 *
 * ═══════════════════════════════════════════════════════════════
 * 1. THE TOP BAR (fixed at the top, padding: 14dp top, 24dp sides, 6dp bottom)
 * ═══════════════════════════════════════════════════════════════
 * A Row, items vertically centered:
 *   - Back button on the far left. A 44dp tappable circle holding a 24dp
 *     `arrow_back` icon in colors.fgPrimary. Tapping it calls onBack (leaves the
 *     screen without saving). Shift it left by about 12dp so the icon lines up
 *     with the 24dp screen edge padding rather than sitting inset.
 *   - The title text, taking the remaining width (weight 1f): shows "Edit task"
 *     when editing, else "New task". Space Grotesk, 22sp, weight 600,
 *     letterSpacing -0.02em, colors.fgPrimary.
 *   - A "Save" text button on the far right. Space Grotesk, 15sp, weight 700.
 *     Its color is colors.amber when canSave is true, else colors.fgDisabled,
 *     and that color animates over 160ms ease-out. It's only tappable when
 *     canSave is true. Tapping it runs the save logic (below).
 *
 * ═══════════════════════════════════════════════════════════════
 * THE SCROLL AREA (padding inside: 18dp top, 24dp sides, 36dp bottom)
 * ═══════════════════════════════════════════════════════════════
 * Everything below is stacked in a Column inside the scroll area.
 *
 * ── 2. HERO TITLE ──
 * A NakedInput (your borderless text field), big variant, autofocus on, with the
 * placeholder "What needs forging?". Bound to the `title` state. This is the only
 * required field.
 *
 * ── 3. DUE SECTION (36dp gap above it) ──
 * A ForgeSectionLabel with the text "Due". Then:
 *   a) A FlowRow (a Row that wraps to a second line if it runs out of width),
 *      with 8dp horizontal AND 8dp vertical gaps, holding four ForgeSelectionChips:
 *      "Today", "Tomorrow", "This weekend", "Next week".
 *      A chip is "selected" when its preset date equals the current dueDate.
 *      Tapping a chip: if it was already the selected one, clear dueDate (set it
 *      to empty); otherwise set dueDate to that chip's date. So tapping the active
 *      chip again turns it off. Only one can be active at a time (because dueDate
 *      is a single value).
 *      "preset date" just means: "Today" maps to today's date, "Tomorrow" to
 *      tomorrow's, etc. — a small helper function does that mapping. For now that
 *      helper can be simple/placeholder; real date math comes in the data phase.
 *   b) A 12dp gap, then a Row with an 8dp gap holding two ForgePickerFields (the
 *      pill that opens a dialog when tapped):
 *        - date pill: icon calendar_month, shows the formatted dueDate or the
 *          placeholder "Pick a date". Tapping sets openPicker = "date".
 *        - time pill: icon schedule, shows dueTime or placeholder "Time".
 *          Tapping sets openPicker = "dueTime".
 *
 * ── 4. PRIORITY SECTION (30dp gap above it) ──
 * A ForgeSectionLabel "Priority", then your ForgePrioritySegmented, bound to the
 * `priority` state, slim variant. (You already built this.)
 *
 * ── 5. "MORE DETAILS" TOGGLE ROW (34dp gap above it) ──
 * This is a full-width tappable Row that opens/closes the extra sections. It has
 * a 1dp line along its TOP edge (colors.borderSubtle) so it reads like a section
 * divider, and 16dp of vertical padding. Inside:
 *   - The text "MORE DETAILS" on the left (taking remaining width): JetBrains
 *     Mono, 11sp, weight 500, letterSpacing 0.16em, all uppercase, colors.fgTertiary.
 *   - A chevron icon on the right (keyboard_arrow_down, 20dp) that ROTATES 180°
 *     when `expanded` is true, animated over 260ms ease-out. To rotate it, put
 *     `Modifier.graphicsLayer { rotationZ = if (expanded) 180f else 0f }` — but
 *     animate it: `val angle by animateFloatAsState(if (expanded) 180f else 0f,
 *     tween(260, EaseOut))` and use `rotationZ = angle`.
 * Tapping this whole row flips `expanded`.
 *
 * ── 6. THE EXPANDED BLOCK (only visible when expanded == true) ──
 * Wrap everything below in AnimatedVisibility with enter = expandVertically() and
 * exit = shrinkVertically(), both using tween(320, EaseOut). AnimatedVisibility is
 * the Compose tool that smoothly grows/shrinks content in and out instead of it
 * popping. (This replaces the web's grid-rows 0fr→1fr trick — same effect, done
 * the Compose way.) Inside the AnimatedVisibility is a Column holding four
 * sub-sections:
 *
 *   6a. PROJECT (8dp gap above)
 *     A ForgeSectionLabel "Project". Then:
 *       - A FlowRow (8dp gaps) of ForgeSelectionChips, ONE per project name in
 *         `existingProjects` (the list of project names the user has used before,
 *         passed into the screen). A chip is selected when `project` equals that
 *         name. Tapping toggles it: if already selected, clear project; else set
 *         project to that name.
 *       - A 14dp gap, then a NakedInput (normal size, not big) with placeholder
 *         "Or type a new project", bound to `project`, with 8dp bottom padding and
 *         a 1dp bottom line (colors.borderDefault) — a single underline, the only
 *         visual hint that it's a field. Typing here and tapping a chip both write
 *         to the same `project` value, so they stay in sync: type a name matching
 *         a chip and that chip highlights; tap a chip and the text field fills.
 *
 *   6b. SUBTASKS (30dp gap above)
 *     A ForgeSectionLabel. Its text is "Subtasks" when the list is empty, or
 *     "Subtasks · N" where N is the count when there are some.
 *     Then a Column holding:
 *       - One row per subtask. Each row (Row, 12dp gap, 9dp vertical padding,
 *         vertically centered):
 *           · a small 7dp filled circle (dot) in colors.fgTertiary
 *           · a NakedInput taking the remaining width (weight 1f), placeholder
 *             "Subtask", bound to that subtask's title. If the subtask's isNew
 *             flag is true, autofocus it (so a freshly-added row is ready to type
 *             in). When the user presses Enter/Done on the keyboard here, call
 *             addSubtask() — this instantly adds ANOTHER empty row so the user can
 *             keep adding several quickly without reaching for the button. (In
 *             Compose: set keyboardOptions imeAction = ImeAction.Done and
 *             keyboardActions onDone = { addSubtask() }.)
 *           · a delete button on the right: a 32dp tappable circle with a 17dp
 *             `close` icon in colors.fgTertiary. Tapping calls deleteSubtask(index).
 *       - After all the subtask rows, an "Add subtask" row (Row, 12dp gap, 9dp
 *         vertical padding, tappable): a 19dp `add` icon + the text "Add subtask",
 *         both in colors.amber, Space Grotesk 14sp weight 600. Tapping calls
 *         addSubtask().
 *     NOTE: there are NO checkboxes here. On this screen subtasks are just a dot
 *     plus editable text — you're writing what the subtasks ARE, not checking them
 *     off. Checking them off happens later on the Task Detail screen.
 *
 *   6c. REMINDER (30dp gap above)
 *     A Row with the two ends pushed apart (SpaceBetween): a ForgeSectionLabel
 *     "Remind me" on the left, and a ForgeToggle on the right bound to `remind`.
 *     BELOW that, wrapped in its OWN AnimatedVisibility (visible only when
 *     remind == true, tween 280 EaseOut, expand/shrink vertically), a Column:
 *       - A 12dp gap, then a Row (8dp gap, vertically centered):
 *           · a ForgePickerField: icon notifications, value remindStart,
 *             placeholder "From". Tapping sets openPicker = "remindStart".
 *           · a small "TO" label: JetBrains Mono, 11sp, letterSpacing 0.1em,
 *             uppercase, colors.fgTertiary.
 *           · a ForgePickerField: icon notifications_active, value remindEnd,
 *             placeholder "To". Tapping sets openPicker = "remindEnd".
 *       - A 12dp gap, then a line of explaining text: Space Grotesk 13sp,
 *         lineHeight 1.45, colors.fgTertiary:
 *         "Forge only nudges you inside this window. No pings before <remindStart>
 *          or after <remindEnd>." — inserting the two time values into the string.
 *
 *   6d. NOTES (30dp gap above)
 *     A ForgeSectionLabel "Notes". Then a NakedInput (normal size), placeholder
 *     "Anything else worth noting", bound to `notes`, with 8dp bottom padding and
 *     a 1dp bottom line (colors.borderDefault) — same single-underline look as the
 *     project text field.
 *
 * ═══════════════════════════════════════════════════════════════
 * 7. THE PICKER DIALOGS (shown on top of everything, based on openPicker)
 * ═══════════════════════════════════════════════════════════════
 * At the very end of the screen's composable body, check openPicker and show the
 * matching dialog. Only one shows at a time because openPicker holds one value:
 *   - openPicker == "date"        → your ForgeDatePickerDialog. On confirm, set
 *                                    dueDate and set openPicker = null.
 *   - openPicker == "dueTime"     → ForgeTimePickerDialog. On confirm, set dueTime,
 *                                    openPicker = null.
 *   - openPicker == "remindStart" → ForgeTimePickerDialog → sets remindStart.
 *   - openPicker == "remindEnd"   → ForgeTimePickerDialog → sets remindEnd.
 * Each dialog's dismiss (Cancel or tapping outside) just sets openPicker = null.
 *
 * ═══════════════════════════════════════════════════════════════
 * 8. THE SAVE LOGIC (what the Save button does)
 * ═══════════════════════════════════════════════════════════════
 * When tapped (and canSave is true), build a Task from the state and hand it to
 * onSave:
 *   - title   = title.trimmed
 *   - notes   = notes (or null if blank, your choice)
 *   - priority= priority
 *   - id      = editingTask?.id (keep the same id when editing; when new, the id
 *               is assigned later by whatever creates it — for now you can pass a
 *               placeholder or generate one)
 *   - project = project.trim(), but if that's empty, use null
 *   - subtasks= the subtasks list, but FILTER OUT any whose title is blank, and
 *               convert each EditableSubtask into a real Subtask (drop the isNew
 *               flag)
 *   - due     = a display string composed from dueDate + dueTime (e.g.
 *               "Today · 6:00 PM"). A small helper builds this. If both are empty,
 *               due is null.
 *   - the reminder values only if remind is true, else null.
 * Then call onSave(theTask). The parent (the navigation host) takes it from there.
 *
 * ═══════════════════════════════════════════════════════════════
 * PARAMETERS (what the screen receives from outside)
 * ═══════════════════════════════════════════════════════════════
 * @param editingTask       the task being edited, or null when creating new.
 * @param existingProjects  a List<String> of project names the user has used
 *                          before, for the suggestion chips. The nav host builds
 *                          this with: tasks.mapNotNull { it.project }.distinct().
 * @param onSave            called with the finished Task when Save is tapped.
 * @param onBack            called when the back button is tapped (leaves without
 *                          saving).
 * @param modifier          applied to the root Column.
 */
@Preview
@Composable
fun AddOrEditTaskScreen(
    modifier: Modifier = Modifier,
    editingTask: Task? = tasks[0],
    existingProjects: List<String> = listOf(
        *tasks.mapNotNull { it.project }.distinct().toTypedArray()
    ),
    onSave: () -> Unit = { },
    onBack: () -> Unit = { }
) {
    var title by rememberSaveable { mutableStateOf(editingTask?.title ?: "") }
    // PROJECT — one single String is the source of truth.
    // Chips read from it (selected = project == chipName).
    // Chips write to it (tap → set or clear project).
    // The text field reads AND writes to it too.
    // No separate boolean list needed.
    var project by rememberSaveable { mutableStateOf(editingTask?.project ?: "") }
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val canSave by remember {
        derivedStateOf {
            title.trim().isNotEmpty()
        }
    }
    val animateSaveColor by animateColorAsState(
        targetValue = if (canSave) ForgeTheme.colors.amber else ForgeTheme.colors.fgDisabled,
        animationSpec = tween(160, easing = EaseOut),
        label = "save animate color"
    )

    // due date chips — same pattern: one value (dueLabel), chip is selected when
    // dueLabel == that chip's label. No boolean list needed here either, but
    // keeping your existing selections list for now since the due section works.
    val labels = listOf("Today", "Tomorrow", "This weekend", "Next week")
    val selections = remember {
        mutableStateListOf(
            *(editingTask?.due?.let { due -> labels.map { it == due } }
                ?: List(labels.size) { false }).toTypedArray()
        )
    }

    var date: String? by rememberSaveable { mutableStateOf(null) }
    var time: String? by rememberSaveable { mutableStateOf(null) }
    var isShowDate by rememberSaveable { mutableStateOf(false) }
    var isShowTime by rememberSaveable { mutableStateOf(false) }

    var priority by rememberSaveable { mutableStateOf(Priority.HIGH) }

    var expanded by rememberSaveable { mutableStateOf(true) }
    val angle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(200, easing = EaseOut)
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(modifier = modifier) {
        ForgeTaskTopBar(
            label = editingTask?.let { "Edit Task" } ?: "Add Task",
            onStart = {
                OnForgeOnBack {
                    onBack()
                }
            },
            onEnd = {
                Text(
                    text = "Save", style = TextStyle(
                        fontFamily = ForgeFonts.spaceGrotesk,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.W700,
                        color = animateSaveColor
                    ),
                    modifier = Modifier.clickable(
                        indication = null, interactionSource = interactionSource,
                        enabled = canSave,
                        onClick = {
                            onSave()
                        }
                    )
                )
            })
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 18.dp, bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val style = TextStyle(
                fontFamily = ForgeFonts.spaceGrotesk,
                fontSize = 28.sp,
                fontWeight = FontWeight.W600,
                letterSpacing = (-0.02).em,
                lineHeight = (1.3).em,
                color = ForgeTheme.colors.fgPrimary
            )
            NakedValidationText(
                input = title,
                placeholder = "What needs forging?",
                maxLines = 3,
                isTakeFocus = true,
                inputLength = 120
            ) { value ->
                title = value.take(120)
            }
            Spacer(modifier = Modifier.height(36.dp))
            ForgeSectionLabel(
                label = "DUE"
            )
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                labels.forEachIndexed { index, value ->
                    ForgeSelectionChip(
                        label = value,
                        selected = selections[index]
                    ) {
                        val wasSelected = selections[index]
                        selections.indices.forEach { i -> selections[i] = false }
                        if (!wasSelected) selections[index] = true
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ForgePickerField(
                    icon = Icons.Outlined.CalendarMonth,
                    value = date,
                    placeholder = "Pick a date"
                ) {
                    isShowDate = true
                }
                ForgePickerField(
                    icon = Icons.Outlined.Schedule,
                    value = time,
                    placeholder = "Time"
                ) {
                    isShowTime = true
                }
            }
            if (isShowDate) ForgeDatePickerDialog(
                onDismiss = {
                    isShowDate = false
                }
            )
            if (isShowTime) ForgeTimePickerDialog(
                onDismiss = {
                    isShowTime = false
                }
            )
            Spacer(modifier = Modifier.height(30.dp))
            ForgeSectionLabel(label = "Priority")
            Spacer(modifier = Modifier.height(10.dp))
            ForgePrioritySegmented(
                value = priority
            ) { value ->
                priority = value
            }
            Spacer(modifier = Modifier.height(34.dp))
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ForgeTheme.colors.borderSubtle)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = true,
                        onClick = {
                            expanded = !expanded
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ForgeSectionLabel(label = "MORE DETAILS", modifier = Modifier.weight(1f))
                Icon(
                    Icons.Outlined.KeyboardArrowDown,
                    null,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            rotationZ = angle
                        },
                    ForgeTheme.colors.fgTertiary
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(320, easing = EaseOut)),
                exit = shrinkVertically(tween(320, easing = EaseOut))
            ) {
                /**
                 *   6a. PROJECT (8dp gap above)
                 *     A ForgeSectionLabel "Project". Then:
                 *       - A FlowRow (8dp gaps) of ForgeSelectionChips, ONE per project name in
                 *         `existingProjects` (the list of project names the user has used before,
                 *         passed into the screen). A chip is selected when `project` equals that
                 *         name. Tapping toggles it: if already selected, clear project; else set
                 *         project to that name.
                 *       - A 14dp gap, then a NakedInput (normal size, not big) with placeholder
                 *         "Or type a new project", bound to `project`, with 8dp bottom padding and
                 *         a 1dp bottom line (colors.borderDefault) — a single underline, the only
                 *         visual hint that it's a field. Typing here and tapping a chip both write
                 *         to the same `project` value, so they stay in sync: type a name matching
                 *         a chip and that chip highlights; tap a chip and the text field fills.*/
                Column {
                    Spacer(modifier = Modifier.height(24.dp))
                    ForgeSectionLabel(label = "Project")
                    Spacer(modifier = Modifier.height(16.dp))
                    // PROJECT CHIPS — selected when project == chipName.
                    // No separate pSelections boolean list needed.
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        existingProjects.forEach { projectName ->
                            ForgeSelectionChip(
                                label = projectName,
                                // chip is "selected" when the single project
                                // variable matches this chip's name
                                selected = project == projectName
                            ) {
                                // tap: if already selected, clear it;
                                // otherwise set project to this chip's name
                                project = if (project == projectName) "" else projectName
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    // PROJECT TEXT FIELD — bound to the SAME project variable.
                    // Typing "Work" highlights the "Work" chip automatically,
                    // because the chip checks project == "Work".
                    // Tapping a chip fills this field automatically,
                    // because it sets project = chipName.
                    NakedValidationText(
                        input = project,
                        placeholder = "Or type a new project",
                        inputSize = 18.sp,
                        maxLines = 1,
                        isTakeFocus = false,
                        inputLength = 30
                    ) { value ->
                        project = value.take(30)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}