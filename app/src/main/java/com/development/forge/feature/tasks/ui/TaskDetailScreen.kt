package com.development.forge.feature.tasks.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.components.DueChip
import com.development.forge.core.components.ForgeButton
import com.development.forge.core.components.ForgeCheckbox
import com.development.forge.core.components.ForgeSecondaryButton
import com.development.forge.core.components.ForgeSectionLabel
import com.development.forge.core.components.PriorityChip
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme
import com.development.forge.domain.model.Priority
import com.development.forge.domain.model.Subtask
import com.development.forge.domain.model.Task

@Composable
fun TaskDetailScreen(
    modifier: Modifier = Modifier,
    task: Task,
    onToggle: (String) -> Unit = { },
    onComplete: (String) -> Unit = { },
    onToggleSub: (String) -> Unit = { },
    onEdit: () -> Unit = { }
) {
    Column(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 28.dp)
    ) {
        // ── Scrollable content ────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // meta row — priority chip + project name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PriorityChip(level = task.priority, dense = false)
                task.project?.let {
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = it,
                        style = TextStyle(
                            fontFamily = ForgeFonts.spaceGrotesk,
                            fontSize = 13.sp,
                            color = ForgeTheme.colors.fgSecondary
                        )
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // title — strikethrough when done
            Text(
                text = task.title,
                style = TextStyle(
                    fontFamily = ForgeFonts.spaceGrotesk,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.W600,
                    letterSpacing = (-0.02).em,
                    lineHeight = (1.15).em,
                    color = ForgeTheme.colors.fgPrimary
                ),
                textDecoration = if (task.done) TextDecoration.LineThrough else null
            )

            // due chip — only if due exists
            task.due?.let {
                Spacer(Modifier.height(14.dp))
                DueChip(label = it, overdue = task.overdue)
                Spacer(Modifier.height(18.dp))
            }

            // notes — only if notes exist, no extra top spacer
            task.notes?.let {
                Text(
                    text = it,
                    style = TextStyle(
                        fontFamily = ForgeFonts.spaceGrotesk,
                        fontSize = 15.sp,
                        lineHeight = (1.55).em,
                        color = ForgeTheme.colors.fgSecondary
                    )
                )
            }

            // subtasks — only if the list is non-empty
            if (task.subtasks.isNotEmpty()) {
                Spacer(Modifier.height(26.dp))
                ForgeSectionLabel(
                    label = "Subtasks · ${task.subtasks.count { it.isDone }}/${task.subtasks.size}"
                )
                task.subtasks.forEach { subtask ->
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .alpha(if (subtask.isDone) 0.55f else 1f),   // dim only when done
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ForgeCheckbox(
                            checked = subtask.isDone,
                            size = 22.dp,
                            onChange = { onToggleSub(subtask.id) }   // pass subtask id, not task id
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = subtask.title,
                            style = TextStyle(
                                fontFamily = ForgeFonts.spaceGrotesk,
                                fontSize = 14.5.sp,
                                color = ForgeTheme.colors.fgPrimary
                            ),
                            textDecoration = if (subtask.isDone) TextDecoration.LineThrough else null
                        )
                    }
                }
            }

            // metadata footer
            Spacer(Modifier.height(28.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ForgeTheme.colors.borderSubtle)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "CREATED 2D AGO · EDITED TODAY",
                style = TextStyle(
                    fontFamily = ForgeFonts.jetbrainsMono,
                    fontSize = 11.sp,
                    letterSpacing = (0.1).em,
                    color = ForgeTheme.colors.fgTertiary
                )
            )
        }

        // ── Fixed bottom action ───────────────────────────────
        Spacer(Modifier.height(12.dp))
        if (task.done) {
            ForgeSecondaryButton(
                label = "Reopen task",
                leadingIcon = {
                    Icon(
                        Icons.AutoMirrored.Filled.Undo,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = ForgeTheme.colors.fgDisabled
                    )
                },
                onClick = { onToggle(task.id) }
            )
        } else {
            ForgeButton(
                label = "Mark it done",
                isEnabled = true,
                isLoading = false,
                icon = Icons.Filled.Check,
                onClick = { onComplete(task.id) }
            )
        }
    }
}

@Preview
@Composable
private fun TaskDetailScreenPreview() {
    ForgeTheme {
        TaskDetailScreen(
            task = Task(
                id = "preview",
                title = "Draft the Q3 proposal",
                priority = Priority.HIGH,
                project = "Work",
                notes = "Include the revised budget breakdown.",
                subtasks = listOf(
                    Subtask("preview-a", "Outline the executive summary"),
                    Subtask("preview-b", "Add the revenue forecast", isDone = true)
                )
            )
        )
    }
}
