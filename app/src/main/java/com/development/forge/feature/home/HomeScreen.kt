package com.development.forge.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.development.forge.core.components.Amber

/**
 * DESIGN SPECIFICATION — Home / Focus screen
 * Source: Forge Design System, AppScreens.jsx → HomeView
 *
 * ═══════════════════════════════════════════════════════════════
 * SCREEN CHROME
 * ═══════════════════════════════════════════════════════════════
 * This screen does NOT use ForgeTopBar. Instead, the wordmark sits inside the
 * screen's own greeting row, on the right-hand side.
 *
 * The bottom navigation bar IS shown, because this is the "Focus" tab.
 *
 * The entire screen scrolls vertically. Padding is 24dp on the left and right,
 * 24dp at the top, and 28dp at the bottom.
 *
 * ═══════════════════════════════════════════════════════════════
 * DATA — everything on this screen is derived from the task list
 * ═══════════════════════════════════════════════════════════════
 * The screen receives a list of tasks and computes four things from it:
 *
 *   1. "open"      — every task that is not yet done.
 *   2. "focus"     — the FIRST open task. This is "the only task", the hero of
 *                    the screen. If there are no open tasks at all, this is null.
 *   3. "upNext"    — the next three open tasks after the focus task. Never more
 *                    than three.
 *   4. "doneToday" — a count of how many tasks are marked done.
 *
 * Use derivedStateOf for all four, since they all recompute whenever the task
 * list changes.
 *
 * If "focus" is null (meaning nothing is left to do), the screen renders the
 * empty state described in section four INSTEAD of the focus card and the
 * up-next list. The greeting row still shows.
 *
 * ═══════════════════════════════════════════════════════════════
 * SECTION 1 — THE GREETING ROW
 * ═══════════════════════════════════════════════════════════════
 * A Row with its children pushed to opposite ends, aligned to the TOP of the
 * row rather than centred vertically.
 *
 * On the LEFT, a Column containing two pieces of text:
 *
 *   The greeting itself, which changes with the time of day. Before noon it
 *   reads "Good morning.", before 6pm "Good afternoon.", otherwise
 *   "Good evening." Note that each one ends with a full stop — that punctuation
 *   is part of the design's voice, not an accident.
 *   Style: Space Grotesk, 26sp, weight 600, letter spacing -0.025em,
 *   line height 1.1em, colour #F5EDDE.
 *
 *   Then a 6dp gap, followed by a metadata line combining today's date and the
 *   completed count, separated by a middle dot. For example: "WED 15 JUL · 3 DONE".
 *   Style: JetBrains Mono, 12sp, letter spacing 0.1em, all uppercase,
 *   colour #7A7060.
 *
 * On the RIGHT, the small ForgeWordmark component you have already built.
 *
 * ═══════════════════════════════════════════════════════════════
 * SECTION 2 — THE FOCUS CARD (only rendered when a focus task exists)
 * ═══════════════════════════════════════════════════════════════
 * This is the heart of the screen — the single task the user should be working
 * on, given full visual weight.
 *
 * Start with a 28dp gap after the greeting row.
 *
 * Then a section label reading "The only task". This label has no action button
 * on its right side.
 *
 * Then a 10dp gap, followed by the card itself: a ForgeCard with its accent
 * variant enabled (the amber-tinted version), padded 22dp at the top, 20dp on
 * the sides, and 20dp at the bottom. The whole card is tappable, and tapping it
 * opens the task's detail screen.
 *
 * Inside the card, arranged in a Column:
 *
 *   First, a Row with a 10dp gap between its children, vertically centred:
 *   the task's priority chip (the normal size, not the dense variant), and if
 *   the task belongs to a project, the project's name in Space Grotesk at
 *   12.5sp, colour #B5A990.
 *
 *   Then a 16dp gap.
 *
 *   Then the task's title, which is the largest text on the card:
 *   Space Grotesk, 26sp, weight 600, letter spacing -0.02em, line height 1.15em,
 *   colour #F5EDDE.
 *
 *   Then, only if the task has a due date, a 12dp gap followed by the due chip.
 *
 *   Then a 20dp gap.
 *
 *   Finally, a primary button reading "Mark it done" with a check icon, which
 *   marks the focus task as complete.
 *
 *   One thing to verify on a real device: tapping this button must NOT also
 *   trigger the card's own tap action and open the detail screen. In Compose a
 *   button normally consumes the tap itself, so this should work without any
 *   special handling — but confirm it, because nested clickables are a common
 *   source of surprises.
 *
 * ═══════════════════════════════════════════════════════════════
 * SECTION 3 — THE UP NEXT LIST (only when there are further tasks)
 * ═══════════════════════════════════════════════════════════════
 * Start with a 34dp gap after the focus card.
 *
 * Then a section label reading "Up next", this time WITH an action button on the
 * right reading "All tasks", which switches the user to the Tasks tab.
 *
 * Then a 4dp gap, followed by up to three task rows, each in its compact form.
 *
 * An important detail: this is a fixed list of at most three items sitting
 * inside a Column that already scrolls. Do NOT reach for LazyColumn here —
 * nesting a scrolling list inside a scrolling parent causes real problems. A
 * plain forEach is correct. LazyColumn belongs on the Tasks tab, where the list
 * is genuinely long and unbounded.
 *
 * ═══════════════════════════════════════════════════════════════
 * SECTION 4 — THE EMPTY STATE (when nothing is left to do)
 * ═══════════════════════════════════════════════════════════════
 * A centred Column with its text centred, padded 64dp vertically and 24dp
 * horizontally.
 *
 *   A circular badge, 88dp across, filled with amber at 10% opacity and outlined
 *   with a 1.5dp border of amber at 30% opacity. Inside it, a filled
 *   local_fire_department icon at 40dp in amber. This is exactly the badge
 *   pattern from your Success screen — reuse it rather than rebuilding it.
 *
 *   Then a 24dp gap.
 *
 *   Then the title "Nothing left to forge." in Space Grotesk, 24sp, weight 600,
 *   letter spacing -0.02em, colour #F5EDDE.
 *
 *   Then a 12dp gap.
 *
 *   Then the body text "You've cleared today. Rest, or line up what's next." in
 *   Space Grotesk, 15sp, line height 1.5em, colour #B5A990, constrained to a
 *   maximum width of 280dp so it wraps nicely rather than stretching edge to edge.
 *
 *   Finally a call-to-action button reading "Add a task", which opens the Add
 *   sheet.
 *
 *
 * ═══════════════════════════════════════════════════════════════
 * ═══════════════════════════════════════════════════════════════
 * NEW COMPONENTS REQUIRED — build these before assembling the screen
 * ═══════════════════════════════════════════════════════════════
 * ═══════════════════════════════════════════════════════════════
 *
 * ───────────────────────────────────────────────────────────────
 * ForgeCard(accent: Boolean = false, onClick: (() -> Unit)? = null)
 * ───────────────────────────────────────────────────────────────
 * A rounded container with a 16dp corner radius and a background of #1C180F
 * (the raised surface colour).
 *
 * When accent is true, an additional diagonal gradient wash is layered ON TOP of
 * that background: a linear gradient running at roughly 160 degrees, starting at
 * amber with 10% opacity, reaching amber at 2% opacity by the 60% mark, then
 * transparent. In Compose, a top-left to bottom-right diagonal gradient is close
 * enough to the design's 160-degree angle.
 *
 * The border is 1dp. When accent is true it is amber at 28% opacity. Otherwise
 * it is the default border colour, which is cream at 10% opacity.
 *
 * The shadow differs by variant. The accent card gets a large, soft, dark
 * shadow: black at 38% opacity, offset 12dp downward, with a blur reach of about
 * 32dp. The plain card gets something much subtler: black at 28% opacity, offset
 * 2dp downward, blur reach about 14dp. (The design also layers a 1dp amber
 * hairline via a CSS shadow trick on the accent card — skip that, it does not
 * translate meaningfully to Compose.)
 *
 * When the card is tappable, pressing it scales it down to 0.99 over 120ms on
 * the ease-out curve. There is no ripple — set indication to null. The scale
 * itself is the press feedback.
 *
 * ───────────────────────────────────────────────────────────────
 * ForgeSectionLabel(text: String, action: String? = null, onAction: (() -> Unit)?)
 * ───────────────────────────────────────────────────────────────
 * A Row with its children pushed to opposite ends, aligned by their text
 * BASELINE rather than centred, with a 4dp gap beneath it.
 *
 * The label itself: JetBrains Mono, 11sp, weight 500, letter spacing 0.14em, all
 * uppercase, colour #7A7060.
 *
 * The optional action on the right is simply your existing ForgeTextButton at a
 * slightly smaller size: Space Grotesk, 13sp, weight 600, amber.
 *
 * ───────────────────────────────────────────────────────────────
 * ForgeCheckbox(checked: Boolean, onChange: (Boolean) -> Unit, size: Dp = 26.dp)
 * ───────────────────────────────────────────────────────────────
 * A rounded square, sized by the size parameter, with a 9dp corner radius and a
 * 2dp border.
 *
 * When unchecked, the border is cream at 18% opacity (border-strong) and the
 * background is transparent.
 *
 * When checked, both the border and the background become amber, and a glow is
 * added: amber at 40% opacity reaching about 12dp outward.
 *
 * The background and border colours animate over 160ms on the ease-out curve.
 *
 * Inside sits the check mark itself, sized 8dp smaller than the box, coloured
 * #1A1207 (the dark on-amber colour), at weight 700.
 *
 * When unchecked, the check mark is scaled to zero and fully transparent. When
 * checked, it scales to full size and becomes fully opaque.
 *
 * The critical detail: the check mark's SCALE animates on the ease-spring curve
 * — CubicBezierEasing(0.34, 1.56, 0.64, 1) — over 180ms. That curve overshoots
 * past its target before settling, and that overshoot is precisely what makes
 * the check feel satisfying rather than mechanical. Its opacity animates
 * separately, over 120ms on the ordinary ease-out curve.
 *
 * ───────────────────────────────────────────────────────────────
 * ForgePriorityChip(level: Priority, dense: Boolean = false)
 * ───────────────────────────────────────────────────────────────
 * A small pill with an 8dp corner radius. Padding is 2dp vertical and 8dp
 * horizontal in the dense variant, 4dp and 10dp otherwise.
 *
 * It contains a Row: a 5dp dot, a 5dp gap, then the label text in JetBrains
 * Mono, 10.5sp, weight 600, letter spacing 0.08em, all uppercase.
 *
 * The three levels differ in colour:
 *
 *   HIGH — background is solid amber #F58A00; both the text and the dot are
 *          #1A1207 (the dark on-amber colour). No border.
 *
 *   MED  — background is amber at 16% opacity; text and dot are #FFAA2E
 *          (amber-300). No border.
 *
 *   LOW  — background is #251F14 (the elevated surface); text and dot are
 *          #7A7060 (tertiary). This is the ONLY level with a border: 1dp of
 *          cream at 10% opacity.
 *
 * ───────────────────────────────────────────────────────────────
 * ForgeDueChip(label: String?, overdue: Boolean = false)
 * ───────────────────────────────────────────────────────────────
 * If the label is null, this renders nothing at all.
 *
 * Otherwise it is a Row with a 4dp gap: a "schedule" clock icon at 13dp in the
 * outline style, followed by the label text in JetBrains Mono, 11sp, weight 500,
 * letter spacing 0.04em.
 *
 * The colour depends on state: when overdue it is #E8624D (the danger colour),
 * otherwise #7A7060 (tertiary).
 *
 * ───────────────────────────────────────────────────────────────
 * ForgeTaskRow(task, onToggle, onOpen, compact: Boolean = false)
 * ───────────────────────────────────────────────────────────────
 * A Row, vertically centred, with a 14dp gap between children, tappable to open
 * the task's detail screen.
 *
 * Padding is 10dp vertical and 4dp horizontal when compact, 14dp and 4dp
 * otherwise.
 *
 * The entire row's opacity drops to 0.5 when the task is done, and returns to
 * 1.0 when it is not — animated over 200ms on the ease-out curve.
 *
 * On the LEFT sits the checkbox, sized 24dp when compact and 26dp otherwise,
 * which toggles the task's done state.
 *
 * The MIDDLE takes the remaining width (weight 1f), which matters: it means a
 * long title truncates rather than pushing the priority chip off the screen. It
 * contains:
 *
 *   The task's title in Space Grotesk — 14.5sp when compact, 15.5sp otherwise —
 *   weight 500, letter spacing -0.01em, colour #F5EDDE, limited to a single line
 *   with an ellipsis on overflow. When the task is done, the title gets a
 *   strikethrough.
 *
 *   Then, only if the task has a due date or a project, a 3dp gap followed by a
 *   Row with a 10dp gap containing the due chip and then the project name in
 *   Space Grotesk at 12sp, colour #7A7060.
 *
 * On the RIGHT, the task's priority chip in its dense variant — but ONLY when
 * the task is not done. Once a task is complete, its priority no longer matters,
 * so the chip disappears.
 *
 * ───────────────────────────────────────────────────────────────
 * ForgeEmptyState(icon, title, body, cta, onCta)
 * ───────────────────────────────────────────────────────────────
 * Specified in section four above.
 *
 *
 * ═══════════════════════════════════════════════════════════════
 * MODEL CHANGES — domain/model/Task.kt needs extending
 * ═══════════════════════════════════════════════════════════════
 * The design's task shape is wider than the stub you currently have. It needs:
 * an id, a title, a done flag, a priority (LOW, MED or HIGH), an optional due
 * value, an overdue flag, an optional project name, optional notes, and a list
 * of subtasks.
 *
 * One decision to make deliberately before you build: in the prototype, "due" is
 * a plain display string such as "Today · 6:00 PM", and "overdue" is a separate
 * boolean flag sitting next to it. That is a prototype shortcut. In a real app
 * you would store a proper Instant or LocalDateTime and DERIVE both the display
 * label and the overdue flag from it, comparing against the current time.
 *
 * You can either match the prototype now (faster today, but you will refactor it
 * when business logic arrives) or model it properly straight away. Given you are
 * only two weeks from wiring real data, I would model it properly now and avoid
 * the rework.
 *
 * ═══════════════════════════════════════════════════════════════
 * PARAMETERS
 * ═══════════════════════════════════════════════════════════════
 * @param tasks    the list of tasks to render.
 * @param onToggle called to mark a task done or undone.
 * @param onOpen   called to navigate to a task's detail screen.
 * @param onSeeAll called to switch to the Tasks tab.
 * @param onAdd    called to open the Add sheet.
 */
@Preview
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "HomeScreen", color = Amber)
    }
}
