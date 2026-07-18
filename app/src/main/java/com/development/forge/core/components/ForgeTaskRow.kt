package com.development.forge.core.components

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.domain.model.Priority
import com.development.forge.domain.model.Task

/**
 * DESIGN SPECIFICATION — ForgeTaskRow
 * Source: Forge Design System, AppComponents.jsx → TaskRow
 *
 * A single task in a list. Composes three components you've already built —
 * ForgeCheckbox, DueChip, PriorityChip — arranged in a horizontal row. Used on
 * the Home screen's "up next" list (compact) and on the Task List screen (normal).
 *
 * ═══════════════════════════════════════════════════════════════
 * THE ROW
 * ═══════════════════════════════════════════════════════════════
 * A Row, vertically centred, with a 14dp gap between its children. The whole row
 * is tappable and opens the task's detail screen.
 *
 * Padding depends on the compact flag:
 *   compact → 10dp vertical, 4dp horizontal
 *   normal  → 14dp vertical, 4dp horizontal
 *
 * WHOLE-ROW OPACITY (animated):
 *   A done task fades to 0.5 opacity; an open task is at 1.0. Animated over
 *   200ms on the ease-out curve. Apply via graphicsLayer { alpha = ... } on the
 *   Row, driven by animateFloatAsState(if (done) 0.5f else 1f).
 *   This dims the ENTIRE row — checkbox, title, chips — all at once, so a
 *   completed task recedes as a single unit.
 *
 * ═══════════════════════════════════════════════════════════════
 * LEFT — the checkbox
 * ═══════════════════════════════════════════════════════════════
 * ForgeCheckbox, sized 24dp when compact, 26dp otherwise.
 *   checked  = task.done
 *   onChange = { onToggle(task.id) }
 *
 * ═══════════════════════════════════════════════════════════════
 * MIDDLE — the title and metadata (takes remaining width via weight(1f))
 * ═══════════════════════════════════════════════════════════════
 * This must have Modifier.weight(1f) so it takes all the space left after the
 * checkbox and the priority chip. That weight is what makes a long title
 * TRUNCATE with an ellipsis rather than pushing the priority chip off screen.
 *
 * A Column containing:
 *
 *   THE TITLE:
 *     Space Grotesk, weight 500, letter spacing -0.01em, colour #F5EDDE.
 *     Font size: 14.5sp compact, 15.5sp normal.
 *     maxLines = 1, overflow = TextOverflow.Ellipsis (truncate, don't wrap).
 *     When the task is done, add textDecoration = TextDecoration.LineThrough.
 *
 *   THE METADATA LINE (only if task.due != null OR task.project != null):
 *     A 3dp gap above it, then a Row with a 10dp gap containing:
 *       - DueChip(label = task.due, overdue = task.overdue)
 *       - if task.project != null: the project name in Space Grotesk, 12sp,
 *         colour #7A7060 (tertiary)
 *     If both due and project are null, this whole line is absent — no empty gap.
 *
 * ═══════════════════════════════════════════════════════════════
 * RIGHT — the priority chip
 * ═══════════════════════════════════════════════════════════════
 * PriorityChip(level = task.priority, dense = true) — but ONLY when the task is
 * NOT done. Once a task is complete its priority no longer matters, so the chip
 * disappears entirely. Use a simple if (!task.done) { ... } guard.
 *
 * ═══════════════════════════════════════════════════════════════
 * INTERACTION NOTE
 * ═══════════════════════════════════════════════════════════════
 * The row is clickable (opens detail) AND contains a clickable checkbox (toggles
 * done). Tapping the checkbox must NOT also open the detail. In Compose the
 * checkbox's own clickable normally consumes the tap, so this should work — but
 * verify on device that tapping the checkbox only toggles and does not navigate.
 *
 * ═══════════════════════════════════════════════════════════════
 * PARAMETERS
 * ═══════════════════════════════════════════════════════════════
 * @param task     the task to render (needs id, title, done, priority, due,
 *                 overdue, project).
 * @param onToggle called with the task id when the checkbox is tapped.
 * @param onOpen   called with the task id when the row is tapped.
 * @param compact  true for the tighter Home-screen variant, false for the
 *                 roomier Task List variant. Defaults to false.
 * @param modifier applied to the root Row.
 */

private val task = Task(
    id = "1",
    title = "Draft the Q3 proposal",
    done = false,
    priority = Priority.HIGH,
    due = "Today · 6:00 PM",
    overdue = false,
    project = "Work",
    notes = null,
    subtasks = emptyList()
)

@Preview
@Composable
fun ForgeTaskRow(
    modifier: Modifier = Modifier,
    task: Task = com.development.forge.core.components.task,
    onToggle: (String) -> Unit = { },
    onOpen: (String) -> Unit = { },
    compact: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val taskDetailOpacity by animateFloatAsState(
        targetValue = if (task.done) 0.5f else 1f,
        animationSpec = tween(200, easing = EaseOut)
    )
    Row(
        modifier
            .graphicsLayer {
                alpha = taskDetailOpacity
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = true,
                onClick = { onOpen(task.id) }
            )
            .padding(
                horizontal = 4.dp,
                vertical = if (compact) 10.dp else 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically) {
        ForgeCheckbox(
            size = if (compact) 24.dp else 26.dp,
            checked = task.done,
            onChange = {
                onToggle(task.id)
            })
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title, style = TextStyle(
                    fontFamily = ForgeFonts.spaceGrotesk,
                    fontWeight = FontWeight.W500,
                    letterSpacing = (-0.01).em,
                    fontSize = if (compact) 14.5.sp else 15.5.sp,
                    color = Color(0xFFF5EDDE),
                ), maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (task.done) TextDecoration.LineThrough else null
            )
            if (task.due != null || task.project != null) {
                Spacer(modifier = Modifier.height(3.dp))
                Row {
                    DueChip(label = task.due, overdue = task.overdue)
                    task.project?.let {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = task.project, style = TextStyle(
                                fontFamily = ForgeFonts.spaceGrotesk,
                                fontSize = 12.sp,
                                color = FgTertiary
                            )
                        )
                    }
                }
            }
        }
        if (!task.done) {
            Spacer(modifier = Modifier.width(14.dp))
            PriorityChip(level = task.priority, dense = true)
        }
    }
}