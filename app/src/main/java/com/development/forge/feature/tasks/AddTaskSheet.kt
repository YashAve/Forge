package com.development.forge.feature.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.components.ForgeSectionLabel
import com.development.forge.core.components.ForgeTextField
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme
import com.development.forge.domain.model.Task
import com.development.forge.feature.tasks.components.ForgeSelectionChip

/**
 * DESIGN SPECIFICATION — Add / Edit Task bottom sheet (v2, redesigned)
 * Source: Forge Design System v2, AppScreens.jsx → AddTaskSheet
 *
 * The last UI piece. A ModalBottomSheet for creating or editing a task. The v2
 * redesign fixes the "cramped form" feel with generous group spacing, a hero
 * title field, quick-date chips, a collapsing reminder section, and a segmented
 * priority control. All colours reference ForgeTheme.colors.
 *
 * ═══════════════════════════════════════════════════════════════
 * THE SHEET CONTAINER
 * ═══════════════════════════════════════════════════════════════
 * Use Material3 ModalBottomSheet (this is the ONE new Compose API here).
 *   - Background: ForgeTheme.colors.bgElev
 *   - Top corners rounded 24dp (bottom corners square — it's anchored to the
 *     screen bottom). Pass a shape with only top corners rounded.
 *   - A 1dp top border in colors.borderDefault (subtle separation from the scrim).
 *   - Scrim behind: a dark wash (~55% black). ModalBottomSheet provides this.
 *   - Content padding: 22dp horizontal, ~10dp top (above the handle), 24dp bottom.
 *   - The sheet SCROLLS internally (verticalScroll) — it's tall, up to ~94% of
 *     screen height. Content can exceed the visible area, especially with the
 *     reminder section expanded + keyboard up.
 *   - A drag handle at the top: a 40dp × 4dp rounded bar in colors.borderStrong,
 *     centred. ModalBottomSheet has a default dragHandle; either use it or supply
 *     this custom one.
 *   - AMBIENT EMBER: a faint radial amber glow behind the content, top-left —
 *     amber @11% alpha fading to transparent by ~60%, anchored around 22% from
 *     the left, 0% from top. Same motif as everywhere else; draw via a Box with
 *     a radial-gradient background or a drawBehind, BEHIND the content (zIndex 0).
 *
 * ═══════════════════════════════════════════════════════════════
 * STATE (all hoisted — this sheet is stateless, driven by a ViewModel later)
 * ═══════════════════════════════════════════════════════════════
 *   title: String, notes: String, dueDate: String?, dueTime: String?,
 *   priority: Priority, remind: Boolean, remindStart: String, remindEnd: String
 * canSave = title.trim().isNotEmpty()
 * editing = (the task being edited != null) → title is "Edit task" vs "New task"
 *
 * ═══════════════════════════════════════════════════════════════
 * 1. HEADER (paddingTop 12dp above it)
 * ═══════════════════════════════════════════════════════════════
 * A Row, SpaceBetween, top-aligned:
 *   LEFT — a Column:
 *     - title: "New task" (or "Edit task" when editing)
 *       → Space Grotesk, 26sp, W600, letterSpacing -0.025em, lineHeight 1.1,
 *         colors.fgPrimary
 *     - 6dp gap, then subtitle "Capture it before it slips."
 *       → Space Grotesk, 14sp, colors.fgSecondary
 *   RIGHT — a close (X) button: 36dp tap target, `close` icon 22dp,
 *     colors.fgTertiary. Dismisses the sheet.
 *
 * ═══════════════════════════════════════════════════════════════
 * 2. TITLE + NOTES GROUP (26dp top margin, 10dp gap between the two fields)
 * ═══════════════════════════════════════════════════════════════
 * Two ForgeTextFields. The design uses a `big` variant for the title so it reads
 * as the hero:
 *   - TITLE: your ForgeTextField with a `big` flag — minHeight 72dp (vs 64dp
 *     normal), leadingIcon `bolt`, label "Title", autofocus. Everything else
 *     matches your existing field (floating label, focus ring, amber border on
 *     focus). Add a `big: Boolean = false` param to ForgeTextField if it doesn't
 *     have one — it only changes minHeight 64→72 and can bump the input text size
 *     slightly (e.g. 16→18sp).
 *   - NOTES: normal ForgeTextField, leadingIcon `notes`, label "Notes".
 *
 * ═══════════════════════════════════════════════════════════════
 * 3. DUE GROUP (28dp top margin)
 * ═══════════════════════════════════════════════════════════════
 * A ForgeSectionLabel "Due", 12dp gap, then:
 *   a) QUICK-DATE CHIPS — a wrapping Row (FlowRow), 8dp gap, four chips:
 *      "Today", "Tomorrow", "This weekend", "Next week".
 *      Each chip: padding 8dp vertical / 14dp horizontal, fully rounded (999),
 *      Space Grotesk 13sp W600.
 *        - SELECTED: background colors.amber, text colors.fgOnAmber, no border
 *        - UNSELECTED: background colors.bgRaised, text colors.fgSecondary,
 *          1.5dp border colors.borderDefault
 *      Tapping a chip sets dueDate to that preset; tapping the ACTIVE chip again
 *      clears it (toggle). Only one active at a time. Transition 140ms ease-out.
 *      (Use androidx.compose.foundation.layout.FlowRow — the wrapping Row.)
 *   b) 10dp gap, then a Row of two PICKER FIELDS (gap 10dp):
 *      - date picker (weight 1.4f): ForgePickerField, icon `calendar_month`,
 *        shows the formatted date or "Pick a date"
 *      - time picker (weight 1f): ForgePickerField, icon `schedule`, shows the
 *        time or "Time"
 *
 * ── ForgePickerField(icon, value: String?, placeholder, onClick) ── NEW small component
 *   A clickable pill: height 56dp, corner 16dp, padding 0/16dp, background
 *   colors.bgRaised, 1.5dp border colors.borderDefault. A Row (gap 10dp):
 *     - the icon: 20dp, colors.fgTertiary
 *     - the value text (weight 1f): JetBrains Mono, 14sp, W500, single line,
 *       ellipsis. Colour colors.fgPrimary when value present, else colors.fgTertiary
 *       (showing the placeholder). No ripple; it opens a picker dialog on tap.
 *
 * ═══════════════════════════════════════════════════════════════
 * 4. REMINDER GROUP (28dp top margin) — collapses cleanly when off
 * ═══════════════════════════════════════════════════════════════
 * A Row, SpaceBetween: a ForgeSectionLabel "Remind me" + a ForgeToggle (bound to
 * `remind`).
 * BELOW it, a section that EXPANDS/COLLAPSES with the toggle:
 *   - When remind = true: visible. When false: collapsed to zero height, cleanly
 *     (no leftover gap).
 *   - The web uses a grid-rows 0fr→1fr trick with 280ms ease-out. In Compose, use
 *     AnimatedVisibility with expandVertically() + shrinkVertically() (tween 280,
 *     ease-out) wrapping the content. That's the idiomatic equivalent.
 *   - Inside (when expanded):
 *       - 12dp top margin, a Row (gap 10dp, vertically centred):
 *           ForgePickerField (icon `notifications`, value remindStart, "From")
 *           a "TO" label → JetBrains Mono, 12sp, letterSpacing 0.1em, UPPERCASE,
 *             colors.fgTertiary
 *           ForgePickerField (icon `notifications_active`, value remindEnd, "To")
 *       - 10dp gap, then explanatory microcopy → Space Grotesk, 13sp,
 *         lineHeight 1.45, colors.fgTertiary:
 *         "Forge only nudges you inside this window. No pings before <start> or
 *          after <end>."  (interpolate the two times)
 *
 * ═══════════════════════════════════════════════════════════════
 * 5. PRIORITY GROUP (28dp top margin) — segmented control (NEW component)
 * ═══════════════════════════════════════════════════════════════
 * A ForgeSectionLabel "Priority", 10dp gap, then ForgePrioritySegmented.
 *
 * ── ForgePrioritySegmented(value: Priority, onChange: (Priority) -> Unit) ── NEW
 *   ONE cohesive track with a sliding amber indicator behind the selected option.
 *   Three segments: Low, Medium, High.
 *   THE TRACK: a Row, corner 14dp, background colors.bgRaised, 1.5dp border
 *   colors.borderDefault, 4dp inner padding. Height ~50dp (segments are 42dp).
 *   THE SLIDING INDICATOR (the interesting part): an amber rounded rect (corner
 *   10dp) sitting BEHIND the segment text, that slides to the selected segment.
 *     - It occupies 1/3 of the track width, and its horizontal position animates
 *       to the selected index.
 *     - In Compose: this is exactly your BiasAlignment trick from ForgeToggle,
 *       scaled to 3 positions. Put the indicator in a Box that fills the track,
 *       aligned with BiasAlignment(horizontalBias = bias), where bias animates
 *       between -1f (Low), 0f (Medium), 1f (High) via animateFloatAsState on the
 *       EASE-SPRING curve (CubicBezier 0.34,1.56,0.64,1) — the overshoot is what
 *       makes it feel springy, matching the web's 240ms ease-spring.
 *     - The indicator width = 1/3 of the track. With BiasAlignment and a child
 *       that's fillMaxWidth(1f/3f) of the inner area, bias -1/0/1 lands it in the
 *       three thirds. Give it a glow (amber @35%, dropShadow before its clip).
 *   THE THREE SEGMENTS (on top of the indicator, zIndex above it): each a Row,
 *   weight 1f, 42dp tall, centred, gap 7dp:
 *     - a 7dp dot: colour = currentColor(fgOnAmber) when active, else colors.fgTertiary
 *     - the label (Low/Medium/High) → Space Grotesk, 14sp, W600
 *       colour animates: active → colors.fgOnAmber, inactive → colors.fgSecondary
 *       (200ms ease-out)
 *   Tapping a segment calls onChange with that Priority.
 *   NOTE: Priority enum is LOW/MED/HIGH; map to labels Low/Medium/High.
 *
 * ═══════════════════════════════════════════════════════════════
 * 6. SAVE BUTTON (32dp top margin)
 * ═══════════════════════════════════════════════════════════════
 * Your ForgeButton (primary, full-width), label "Save task", check icon.
 * DISABLED (dimmed) until canSave (title non-empty). onClick = save → assembles
 * the task and calls onSave, then dismisses.
 *
 * ═══════════════════════════════════════════════════════════════
 * THE PICKER DIALOGS (date + time)
 * ═══════════════════════════════════════════════════════════════
 * Tapping a ForgePickerField opens a dialog. The design has custom DatePicker/
 * TimePicker dialogs, but for the real app you can use Material3's DatePicker /
 * TimePicker in a Dialog, or build custom ones later. For NOW: wire the fields to
 * open Material3 pickers (DatePickerDialog / a TimePicker in an AlertDialog) and
 * style them minimally — matching the custom picker aesthetic is a polish pass,
 * not required for the sheet to function. Flag custom pickers as later work.
 *
 * ═══════════════════════════════════════════════════════════════
 * DERIVED / HELPER LOGIC (prototype does this; keep for parity)
 * ═══════════════════════════════════════════════════════════════
 * - composeDue(date, time): builds the display string "Today · 6:00 PM" etc.
 *   For the real app this is where you'd derive from a real timestamp instead.
 * - presetToDate(preset): maps "Today"/"Tomorrow"/"This weekend"/"Next week" to
 *   an actual date. Prototype-level; real app uses LocalDate math.
 * - The quick-date chip is "active" if its preset resolves to the current dueDate.
 *
 * ═══════════════════════════════════════════════════════════════
 * NEW COMPONENTS THIS SHEET INTRODUCES
 * ═══════════════════════════════════════════════════════════════
 *   1. ForgePickerField      — the tappable pill (date/time/reminder fields)
 *   2. ForgePrioritySegmented — the sliding-indicator segmented control
 *   3. ForgeTextField `big`   — a minHeight variant of your existing field
 *   + ModalBottomSheet wiring — the one new Material3 API
 *
 * ═══════════════════════════════════════════════════════════════
 * PARAMETERS
 * ═══════════════════════════════════════════════════════════════
 * @param editingTask  the task being edited, or null for a new task.
 * @param onSave       called with the assembled task data on Save.
 * @param onDismiss    called when the sheet is dismissed (X, scrim, drag-down).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AddTaskSheet(
    modifier: Modifier = Modifier,
    editingTask: Task? = null,
    onSave: () -> Unit = { },
    onDismiss: () -> Unit = { }
) {
    /**
     * ═══════════════════════════════════════════════════════════════
     * THE SHEET CONTAINER
     * ═══════════════════════════════════════════════════════════════
     * Use Material3 ModalBottomSheet (this is the ONE new Compose API here).
     *   - Background: ForgeTheme.colors.bgElev
     *   - Top corners rounded 24dp (bottom corners square — it's anchored to the
     *     screen bottom). Pass a shape with only top corners rounded.
     *   - A 1dp top border in colors.borderDefault (subtle separation from the scrim).
     *   - Scrim behind: a dark wash (~55% black). ModalBottomSheet provides this.
     *   - Content padding: 22dp horizontal, ~10dp top (above the handle), 24dp bottom.
     *   - The sheet SCROLLS internally (verticalScroll) — it's tall, up to ~94% of
     *     screen height. Content can exceed the visible area, especially with the
     *     reminder section expanded + keyboard up.
     *   - A drag handle at the top: a 40dp × 4dp rounded bar in colors.borderStrong,
     *     centred. ModalBottomSheet has a default dragHandle; either use it or supply
     *     this custom one.
     *   - AMBIENT EMBER: a faint radial amber glow behind the content, top-left —
     *     amber @11% alpha fading to transparent by ~60%, anchored around 22% from
     *     the left, 0% from top. Same motif as everywhere else; draw via a Box with
     *     a radial-gradient background or a drawBehind, BEHIND the content (zIndex 0).*/
    val bottomSheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        containerColor = ForgeTheme.colors.bgElev,
        shape = RoundedCornerShape(topStart = 14.dp, topEnd = 24.dp),
        scrimColor = Color.Black.copy(0.55f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(percent = 100))
                    .background(ForgeTheme.colors.borderStrong)
            )
        },
    ) {
        AddTaskContent()
    }
}

@Preview
@Composable
fun AddTaskContent(
    modifier: Modifier = Modifier,
    editingTask: Task? = null,
    onDismiss: () -> Unit = { }
) {
    var title by rememberSaveable { mutableStateOf(editingTask?.title ?: "") }
    var notes by rememberSaveable { mutableStateOf(editingTask?.notes ?: "") }
    val selections = rememberSaveable { mutableStateListOf(false, false, false, false) }
    val labels = listOf("Today", "Tomorrow", "This weekend", "Next week")

    val glowColor = ForgeTheme.colors.amber

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        0f to glowColor.copy(alpha = 0.11f),
                        0.60f to Color.Transparent,
                        center = Offset(size.width * 0.22f, 0f),
                        radius = 360.dp.toPx()
                    )
                )
            }
            .padding(horizontal = 22.dp)
            .padding(top = 10.dp, bottom = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = editingTask?.let { "Edit task" } ?: "New task", style = TextStyle(
                        fontFamily = ForgeFonts.spaceGrotesk,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.W600,
                        letterSpacing = (-0.025).em,
                        lineHeight = (1.1).em,
                        color = ForgeTheme.colors.fgPrimary
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Capture it before it slips.", style = TextStyle(
                        fontFamily = ForgeFonts.spaceGrotesk,
                        fontSize = 14.sp,
                        color = ForgeTheme.colors.fgSecondary
                    )
                )
            }
            IconButton(
                enabled = true,
                onClick = onDismiss,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Filled.Close,
                    null,
                    Modifier.size(22.dp),
                    ForgeTheme.colors.fgTertiary
                )
            }
        }
        Spacer(modifier = Modifier.height(26.dp))
        ForgeTextField(
            label = "Title",
            leadingIcon = Icons.Filled.Bolt,
            value = title,
            onValueChange = { value ->
                title = value
            },
            big = true,
            autoFocus = true
        )
        Spacer(modifier = Modifier.height(10.dp))
        ForgeTextField(
            label = "Notes",
            leadingIcon = Icons.AutoMirrored.Filled.Notes,
            value = notes,
            onValueChange = { value ->
                notes = value
            },
            big = true,
            autoFocus = false
        )
        Spacer(modifier = Modifier.height(28.dp))
        ForgeSectionLabel(label = "Due")
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            labels.forEachIndexed { index, value ->
                ForgeSelectionChip(
                    label = value,
                    selected = selections[index],
                    onClick = {
                        val wasSelected = selections[index]
                        for (i in selections.indices) selections[i] = false
                        if (!wasSelected) selections[index] = true
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

        }
    }
}
