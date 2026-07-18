package com.development.forge.feature.tasks

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/**
 * DESIGN SPECIFICATION — Task List screen
 * Source: Forge Design System, AppScreens.jsx → TaskListView
 *
 * The full task list, grouped into sections. Reached from the Home screen's
 * "All tasks" action. Unlike Home (which shows one focus task + three), this
 * shows EVERY task, grouped by when it's due, with completed tasks at the bottom.
 *
 * ═══════════════════════════════════════════════════════════════
 * CHROME
 * ═══════════════════════════════════════════════════════════════
 * - A top bar titled "All tasks" with a back button (returns to Home) and an
 *   add action on the right (opens the Add sheet).
 *   NOTE: this is a NEW top bar variant — it has a centred/left title AND a
 *   trailing add icon. Your ForgeTopBar currently handles wordmark + optional
 *   back + optional trailing. You'll either extend it to accept a title string,
 *   or build a small AppTopBar variant. The title is the main new piece.
 * - The bottom nav is NOT shown here in the design (this is a pushed screen from
 *   Home, reached via "All tasks", not a bottom-nav destination).
 *   DECISION: in your architecture you made Tasks a bottom-nav sibling earlier,
 *   but the design reaches this via Home's "All tasks" as a push. Decide which:
 *   either it's a bottom-nav tab (bottom bar shows) OR a pushed detail-style
 *   screen (back button, no bottom bar). Given your bottom nav is Focus/Add/
 *   Stats/You — there is NO Tasks tab — so this is correctly a PUSHED screen
 *   from Home with a back button and NO bottom bar. Good, that's consistent.
 *
 * ═══════════════════════════════════════════════════════════════
 * DATA — grouping logic
 * ═══════════════════════════════════════════════════════════════
 * Split tasks into active (not done) and done:
 *   val active = tasks.filter { !it.done }
 *   val done   = tasks.filter { it.done }
 *
 * The active tasks are grouped into THREE buckets by their due value:
 *   TODAY    — due starts with "Today", OR the task is overdue
 *   UPCOMING — has a due, doesn't start with "Today", and is not overdue
 *   SOMEDAY  — has no due at all (due == null)
 *
 * (This grouping works off the prototype's String due. When you move to real
 * Instant-based dates, this becomes date-comparison logic instead of string
 * matching — flag it for the refactor.)
 *
 * Each group only renders if it has at least one task. Empty groups are skipped
 * entirely — no empty headers.
 *
 * Done tasks form their own final group at the bottom, always last.
 *
 * ═══════════════════════════════════════════════════════════════
 * LAYOUT — this is a LazyColumn (your first one)
 * ═══════════════════════════════════════════════════════════════
 * Unlike Home's fixed 3-item list (plain forEach in a scrolling Column), THIS
 * list is unbounded — a user could have 50 tasks. That's exactly what LazyColumn
 * is for: it only composes the items currently visible on screen, recycling as
 * you scroll, instead of composing all 50 at once.
 *
 * Content padding: 8dp top, 24dp horizontal, 28dp bottom.
 *
 * Structure inside the LazyColumn:
 *   - For each non-empty group (Today, Upcoming, Someday, Done):
 *       - a section header: ForgeSectionLabel with text "<Label> · <count>"
 *         e.g. "TODAY · 3", "DONE · 2". Give it 18dp top spacing (24dp before
 *         the Done group).
 *       - then each task as a ForgeTaskRow (NORMAL size, not compact — this is
 *         the roomy variant).
 *   - If there are no tasks at all (active and done both empty): show
 *     ForgeEmptyState with icon = checklist, title "No tasks yet.", body "Add
 *     your first one and let Forge keep the fire lit.", cta "Add a task".
 *
 * ═══════════════════════════════════════════════════════════════
 * LAZYCOLUMN MECHANICS — the new concepts
 * ═══════════════════════════════════════════════════════════════
 * LazyColumn takes a content lambda with a special DSL (LazyListScope), NOT
 * regular composables directly. Inside it you use:
 *
 *   item { ... }            — adds ONE item (use for section headers)
 *   items(list) { t -> ... } — adds one item PER element in a list (use for the
 *                              task rows in each group)
 *
 * So a group looks like:
 *   item { ForgeSectionLabel(label = "Today · ${todayTasks.size}") }
 *   items(todayTasks, key = { it.id }) { task ->
 *       ForgeTaskRow(task = task, onToggle = onToggle, onOpen = onOpen)
 *   }
 *
 * IMPORTANT — the `key` parameter: always pass key = { it.id } to items(). This
 * tells LazyColumn how to identify each row across recompositions. Without it,
 * when a task moves groups (e.g. you complete it and it jumps to Done), Compose
 * can't track which row is which, and animations/state get confused. With a
 * stable key, Compose knows "this specific task moved" and can animate it
 * correctly.
 *
 * DO NOT wrap the LazyColumn in a verticalScroll — LazyColumn scrolls itself.
 * Nesting a scroll inside a scroll crashes. (This is the opposite of Home, where
 * you used a plain Column WITH verticalScroll.)
 *
 * ═══════════════════════════════════════════════════════════════
 * OPTIONAL POLISH (later, not now)
 * ═══════════════════════════════════════════════════════════════
 * When a task is completed and jumps from an active group to Done, LazyColumn can
 * animate that movement with Modifier.animateItem() on the row. That's a nice
 * touch but adds complexity — get the static list working first, add the item
 * animation as a second pass.
 *
 * ═══════════════════════════════════════════════════════════════
 * PARAMETERS
 * ═══════════════════════════════════════════════════════════════
 * @param tasks    the full task list.
 * @param onToggle called with a task id when a checkbox is tapped.
 * @param onOpen   called with a task id when a row is tapped (→ TaskDetail).
 * @param onBack   called when the back button is tapped (→ Home).
 * @param onAdd    called when the add action is tapped (→ Add sheet).
 * @param modifier applied to the root.
 */
@Preview
@Composable
fun TaskListScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Text(text = "TaskListScreen")
    }
}
