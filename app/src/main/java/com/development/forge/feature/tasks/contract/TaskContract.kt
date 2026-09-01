package com.development.forge.feature.tasks.contract

import com.development.forge.domain.model.Priority
import com.development.forge.domain.model.Subtask
import com.development.forge.domain.model.Task
import kotlin.time.Instant

data class TaskState(
    val editingTaskId: String? = null,
    val title: String = "",
    val priority: Priority = Priority.MED,
    val dueAt: Instant? = null,
    val project: String? = null,
    val notes: String? = null,
    val subtasks: List<Subtask> = emptyList(),
    val existingProjects: List<String> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val isSaving: Boolean = false
)

sealed interface TaskIntent {
    data class StartEdit(val taskId: String) : TaskIntent
    data object ClearForm : TaskIntent
    data class UpdateTitle(val title: String) : TaskIntent
    data class UpdatePriority(val priority: Priority) : TaskIntent
    data class UpdateDueAt(val dueAt: Instant?) : TaskIntent
    data class UpdateProject(val project: String?) : TaskIntent
    data class UpdateNotes(val notes: String?) : TaskIntent
    data class ToggleDone(val taskId: String) : TaskIntent
    data object StartCreate : TaskIntent
    data class AddSubtask(val title: String) : TaskIntent
    data class RemoveSubtask(val index: Int) : TaskIntent
    data class SaveTask(val subtasks: List<Subtask>) : TaskIntent
}

sealed interface TaskEffect {
    data object NavigateBack : TaskEffect
}