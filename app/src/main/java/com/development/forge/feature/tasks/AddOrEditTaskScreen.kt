package com.development.forge.feature.tasks

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
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
import com.development.forge.domain.model.Task
import com.development.forge.domain.model.tasks
import com.development.forge.feature.tasks.components.ForgeDatePickerDialog
import com.development.forge.feature.tasks.components.ForgePickerField
import com.development.forge.feature.tasks.components.ForgeSelectionChip
import com.development.forge.feature.tasks.components.ForgeTimePickerDialog

/**
 * DESIGN SPECIFICATION — Add / Edit Task screen (v3, FULL SCREEN)
 * Source: Forge Design System v3, AppScreens.jsx → AddTaskScreen
 *
 * The flagship capture screen — a full pushed screen (NOT a sheet). Two-stage:
 * a fast default view (title + due + priority + Save, no scroll) that expands
 * into calm, Task-Detail-style sections. The defining move is "naked inputs" —
 * borderless, transparent text fields directly on the canvas, so the screen
 * reads editorial, not form-like.
 *
 * Receives: the task being edited (or null), and existingProjects: List<String>
 * (the deduplicated project names from all tasks, for the suggestion chips).
 *
 * ═══════════════════════════════════════════════════════════════
 * STRUCTURE — Column filling the screen height
 * ═══════════════════════════════════════════════════════════════
 *   1. Top bar (fixed): back · title · Save
 *   2. Scroll area (weight 1f + verticalScroll): all the content
 * No bottom nav. editing = (task != null) → title "Edit task" vs "New task",
 * AND the expanded section starts OPEN when editing (you're editing an existing
 * task, so show everything), CLOSED when creating (fast capture first).
 *
 * ═══════════════════════════════════════════════════════════════
 * 1. TOP BAR (padding 14dp top / 24dp sides / 6dp bottom)
 * ═══════════════════════════════════════════════════════════════
 * A Row, vertically centred:
 *   - back button (44dp tap target, arrow_back 24dp, colors.fgPrimary) → onBack
 *   - title (weight 1f): "New task" / "Edit task" → Space Grotesk, 22sp, W600,
 *     letterSpacing -0.02em, colors.fgPrimary
 *   - "Save" text button → Space Grotesk, 15sp, W700; colour colors.amber when
 *     canSave (title non-blank), else colors.fgDisabled. Disabled until canSave.
 *     Animate the colour 160ms ease-out.
 *
 * ═══════════════════════════════════════════════════════════════
 * SCROLL AREA (padding 18dp top / 24dp sides / 36dp bottom)
 * ═══════════════════════════════════════════════════════════════
 *
 * ── NEW COMPONENT: NakedInput(value, onValueChange, placeholder, big, onKeyDone) ──
 * A BasicTextField with NO box, NO border, NO background — transparent, sitting
 * directly on the canvas. caretColor = colors.amber. Two sizes:
 *   - big = true (title): Space Grotesk, 28sp, W600, letterSpacing -0.02em,
 *     lineHeight 1.3, colour colors.fgPrimary. Placeholder in colors.fgTertiary.
 *   - big = false (project/notes/subtask): Space Grotesk, 15sp, W400,
 *     letterSpacing -0.005em.
 * This is the workhorse of the screen. Build it once from BasicTextField with a
 * placeholder overlay (BasicTextField has no placeholder param — show the
 * placeholder Text when value is empty, in fgTertiary).
 *
 * ── 2a. HERO TITLE ──
 * A NakedInput(big = true), autofocus, placeholder "What needs forging?".
 * No icon, no box — just big type with an amber cursor. This alone is the
 * whole "hero" — the naked big type does the work a boxed field couldn't.
 *
 * ── 2b. DUE (36dp top margin) ──
 * SectionLabel "Due", then:
 *   - 12dp gap, quick-date chips (FlowRow, 8dp gap): Today / Tomorrow /
 *     This weekend / Next week. Use the shared `chip` style (below). Tapping the
 *     active one again clears it.
 *   - 12dp gap, a Row (8dp gap) of two PickerChips:
 *       PickerChip(icon calendar_month, value = formatted date, "Pick a date")
 *       PickerChip(icon schedule, value = dueTime, "Time")
 *
 * ── NEW COMPONENT: PickerChip(icon, value, placeholder, onClick) ──
 * A compact pill (NOT a form box — reads like a DueChip): height 38dp, fully
 * rounded (999), padding 0/14dp, a Row (gap 7dp): icon 17dp + label (JetBrains
 * Mono, 12.5sp, W500). Two states:
 *   - SET (value present): background amber @12%, border amber @35%, icon+text
 *     colors.amberA300.
 *   - EMPTY: background colors.bgRaised, border colors.borderDefault, icon+text
 *     colors.fgTertiary (shows placeholder).
 * Tapping opens a picker dialog.
 *
 * ── shared `chip` style (quick-date + project chips) ──
 * A pill: padding 8dp/14dp, fully rounded, Space Grotesk 13sp W600. Selected →
 * background colors.amber, text colors.fgOnAmber, no border, a soft amber glow
 * (dropShadow amber @28%). Unselected → background colors.bgRaised, text
 * colors.fgSecondary, 1dp border colors.borderDefault. 140ms ease-out.
 * (This is the ForgeSelectionChip you already started building — reuse it.)
 *
 * ── 2c. PRIORITY (30dp top margin) ──
 * SectionLabel "Priority", then ForgePrioritySegmented(slim = true).
 * The SLIM variant: track 1dp border colors.borderSubtle (vs 1.5dp default),
 * corner 12dp, padding 3dp, segment height 38dp (vs 42dp). Sliding amber
 * indicator on ease-spring (your BiasAlignment trick, 3 positions, overshoot).
 * Default value MED.
 *
 * ── 2d. "MORE DETAILS" TOGGLE ROW (34dp top margin) ──
 * A full-width button, 16dp vertical padding, with a 1dp TOP border in
 * colors.borderSubtle (reads like a Task-Detail section break). A Row:
 *   - "MORE DETAILS" (weight 1f, left) → JetBrains Mono, 11sp, W500,
 *     letterSpacing 0.16em, UPPERCASE, colors.fgTertiary
 *   - a chevron (keyboard_arrow_down, 20dp) that ROTATES 180° when expanded,
 *     animated 260ms ease-out.
 * Tapping toggles `expanded`.
 *
 * ═══════════════════════════════════════════════════════════════
 * EXPANDED SECTION — animate via AnimatedVisibility(expandVertically +
 * shrinkVertically, ~320ms ease-out). Starts open when editing, closed when new.
 * ═══════════════════════════════════════════════════════════════
 *
 * ── PROJECT (8dp top margin) ──
 * SectionLabel "Project", then:
 *   - 12dp gap, a FlowRow (8dp gap) of `chip`s — one per existingProjects entry.
 *     Selected when project == that name. Tapping toggles (tap active to clear).
 *   - 14dp gap, a NakedInput (not big), placeholder "Or type a new project",
 *     with an 8dp bottom padding and a 1dp bottom border colors.borderDefault
 *     (a single underline, the only "field" affordance — otherwise naked).
 *   Typing here sets project directly; the chips and the input stay in sync
 *   (typing a name that matches a chip highlights it; tapping a chip fills the
 *   input). This is the "suggestion chips + free text, no separate create step"
 *   pattern you designed.
 *
 * ── SUBTASKS (30dp top margin) ──
 * SectionLabel "Subtasks · N" (N = count, omit the · N when empty). Then a
 * Column of subtask rows + an add row:
 *   Each subtask row (Row, 12dp gap, 9dp vertical padding):
 *     - a 7dp dot, colors.fgTertiary
 *     - a NakedInput (not big, flex/weight 1f), placeholder "Subtask". autofocus
 *       when it's a newly-added row. onKeyDone (Enter/IME Done) → ADD ANOTHER
 *       subtask row immediately (rapid entry — the key UX detail).
 *     - a delete button (32dp tap target, close icon 17dp, colors.fgTertiary)
 *   The "Add subtask" row (Row, 12dp gap, 9dp vertical padding, clickable):
 *     - add icon 19dp + "Add subtask" → Space Grotesk 14sp W600, colors.amber.
 *   NOTE: no boxes, no checkboxes here — subtasks are just dot + naked text,
 *   Task-Detail-style. (Completion toggling happens in Task Detail, not here.)
 *
 * ── REMINDER (30dp top margin) ──
 * A Row SpaceBetween: SectionLabel "Remind me" + ForgeToggle (bound to remind).
 * Below, AnimatedVisibility (remind, 280ms) reveals:
 *   - 12dp gap, a Row (8dp gap): PickerChip(notifications, remindStart, "From"),
 *     a "TO" label (JetBrains Mono 11sp, 0.1em, UPPERCASE, fgTertiary),
 *     PickerChip(notifications_active, remindEnd, "To").
 *   - 12dp gap, microcopy → Space Grotesk 13sp, lineHeight 1.45, fgTertiary:
 *     "Forge only nudges you inside this window. No pings before <start> or
 *      after <end>."
 *
 * ── NOTES (30dp top margin) ──
 * SectionLabel "Notes", then a NakedInput (not big), placeholder "Anything else
 * worth noting", with 8dp bottom padding + 1dp bottom border colors.borderDefault
 * (same single-underline affordance as project).
 *
 * ═══════════════════════════════════════════════════════════════
 * STATE (all local to the screen for now; hoisted to a ViewModel later)
 * ═══════════════════════════════════════════════════════════════
 *   title, notes, dueDate, dueTime, priority (default MED), project,
 *   subtasks: List (each {title, isDone, isNew}), remind (default false),
 *   remindStart ("9:00 AM"), remindEnd ("6:00 PM"),
 *   picker (which dialog is open: date/dueTime/remindStart/remindEnd/null),
 *   expanded (default = editing).
 * canSave = title.trim().isNotEmpty().
 * On Save: assemble the Task — project = project.trim().ifBlank { null },
 *   subtasks = subtasks.filter { it.title.isNotBlank() }, due = composeDue(...).
 *
 * ═══════════════════════════════════════════════════════════════
 * PICKER DIALOGS
 * ═══════════════════════════════════════════════════════════════
 * Tapping a PickerChip opens a date or time dialog. For now use Material3
 * DatePicker / TimePicker in a Dialog styled minimally, OR defer custom pickers
 * as a later polish pass. The screen functions without pretty pickers.
 *
 * ═══════════════════════════════════════════════════════════════
 * NEW COMPONENTS THIS SCREEN NEEDS
 * ═══════════════════════════════════════════════════════════════
 *   1. NakedInput           — borderless transparent BasicTextField, big/normal
 *   2. PickerChip           — compact pill that opens a dialog
 *   3. ForgeSelectionChip   — the shared date/project chip (you started this)
 *   4. ForgePrioritySegmented (+ slim variant) — sliding-indicator control
 *   You already have: ForgeToggle, ForgeSectionLabel, ForgeButton.
 *
 * ═══════════════════════════════════════════════════════════════
 * NAVIGATION WIRING
 * ═══════════════════════════════════════════════════════════════
 * Add a ForgeNavKey.AddTask (data object, or data class AddTask(taskId: String?)
 * for edit mode). The bottom nav's Add button now PUSHES this key instead of
 * opening a sheet: onAddClick = { backStack.add(ForgeNavKey.AddTask) }.
 * TaskDetail's edit action pushes AddTask(task.id). onBack/onSave pop it.
 * existingProjects is derived: tasks.mapNotNull { it.project }.distinct().
 *
 * ═══════════════════════════════════════════════════════════════
 * PARAMETERS
 * ═══════════════════════════════════════════════════════════════
 * @param editingTask       the task to edit, or null for new.
 * @param existingProjects  distinct project names, for the suggestion chips.
 * @param onSave            called with the assembled task.
 * @param onBack            pops the screen without saving.
 */
@Preview
@Composable
fun AddOrEditTaskScreen(
    modifier: Modifier = Modifier,
    editingTask: Task? = tasks[0],
    onSave: () -> Unit = { },
    onBack: () -> Unit = { }
) {
    var title by rememberSaveable { mutableStateOf(editingTask?.title ?: "") }
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val saved by remember {
        derivedStateOf {
            true // I will change it as I design
        }
    }
    val animateSaveColor by animateColorAsState(
        targetValue = if (saved) ForgeTheme.colors.amber else ForgeTheme.colors.fgDisabled,
        animationSpec = tween(160, easing = EaseOut),
        label = "save animate color"
    )

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
                        enabled = saved,
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
            BasicTextField(
                value = title, onValueChange = { value ->
                    title = value.take(120)
                },
                textStyle = style,
                cursorBrush = SolidColor(ForgeTheme.colors.amber),
                decorationBox = { innerTextField ->
                    if (title.isEmpty()) {
                        Text(
                            text = "What needs forging?",
                            style = style.copy(color = ForgeTheme.colors.fgTertiary)
                        )
                    }
                    innerTextField()
                },
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
            if (title.length > 96) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${title.length}/120",
                    style = TextStyle(
                        fontFamily = ForgeFonts.jetbrainsMono,
                        fontSize = 11.sp,
                        color = if (title.length >= 120) ForgeTheme.colors.danger
                        else ForgeTheme.colors.fgTertiary
                    ),
                    modifier = Modifier.align(Alignment.End)
                )
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
                        selections[index] = !selections[index]
                        if (selections[index]) selections.indices.forEach { i ->
                            selections[i] = (i == index)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
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
        }
    }
}