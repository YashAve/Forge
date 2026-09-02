package com.development.forge.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.components.DueChip
import com.development.forge.core.components.ForgeButton
import com.development.forge.core.components.ForgeCard
import com.development.forge.core.components.ForgeEmptyState
import com.development.forge.core.components.ForgeSectionLabel
import com.development.forge.core.components.ForgeTaskRow
import com.development.forge.core.components.ForgeWordmark
import com.development.forge.core.components.PriorityChip
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme
import com.development.forge.domain.model.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
fun HomeScreen(
    modifier: Modifier = Modifier,
    tasks: List<Task> = emptyList(),
    onToggle: (String) -> Unit = { },
    onOpen: (String) -> Unit = { },
    onSeeAll: () -> Unit = { },
    onAdd: () -> Unit = { }
) {
    val open = tasks.filter { !it.done }
    val focus = open.firstOrNull()
    val upNext = open.drop(1).take(3)
    val doneToday = tasks.count { it.done }

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning."
        hour < 18 -> "Good afternoon."
        else      -> "Good evening."
    }
    val today = SimpleDateFormat("EEE dd MMM", Locale.ENGLISH)
        .format(Date()).uppercase()
    val metaLine = "$today · $doneToday DONE"

    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 28.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Greeting row ──────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = greeting,
                    style = TextStyle(
                        fontFamily = ForgeFonts.spaceGrotesk,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.W600,
                        letterSpacing = (-0.025).em,
                        lineHeight = (1.1).em,
                        color = ForgeTheme.colors.fgPrimary
                    )
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = metaLine,
                    style = TextStyle(
                        fontFamily = ForgeFonts.jetbrainsMono,
                        fontSize = 12.sp,
                        letterSpacing = (0.1).em,
                        color = ForgeTheme.colors.fgTertiary
                    )
                )
            }
            ForgeWordmark()
        }

        Spacer(Modifier.height(28.dp))

        if (focus != null) {
            // ── Focus card ────────────────────────────────────
            ForgeSectionLabel(label = "The only task")
            Spacer(Modifier.height(10.dp))
            ForgeCard(accent = true, onClick = { onOpen(focus.id) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp, bottom = 20.dp)
                        .padding(horizontal = 20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PriorityChip(level = focus.priority)   // not dense
                        focus.project?.let {
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = it,
                                style = TextStyle(
                                    fontFamily = ForgeFonts.spaceGrotesk,
                                    fontSize = 12.5.sp,
                                    color = ForgeTheme.colors.fgSecondary
                                )
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = focus.title,
                        style = TextStyle(
                            fontFamily = ForgeFonts.spaceGrotesk,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.W600,
                            letterSpacing = (-0.02).em,
                            lineHeight = (1.15).em,
                            color = ForgeTheme.colors.fgPrimary
                        )
                    )
                    focus.due?.let {
                        Spacer(Modifier.height(12.dp))
                        DueChip(label = it, overdue = focus.overdue)
                    }
                    Spacer(Modifier.height(20.dp))
                    ForgeButton(
                        label = "Mark it done",
                        isEnabled = true,
                        isLoading = false,
                        icon = Icons.Filled.Check,
                        onClick = { onToggle(focus.id) }
                    )
                }
            }

            // ── Up next ───────────────────────────────────────
            if (upNext.isNotEmpty()) {
                Spacer(Modifier.height(34.dp))
                ForgeSectionLabel(
                    label = "Up next",
                    action = "All tasks",
                    onAction = onSeeAll
                )
                Spacer(Modifier.height(4.dp))
                upNext.forEach { task ->
                    ForgeTaskRow(
                        task = task,
                        onToggle = onToggle,
                        onOpen = { onOpen(task.id) },
                        compact = true
                    )
                }
            }
        } else {
            // ── Empty state ───────────────────────────────────
            ForgeEmptyState(
                title = "Nothing left to forge.",
                body = "You've cleared today. Rest, or line up what's next.",
                cta = "Add a task",
                onCta = onAdd
            )
        }
    }
}