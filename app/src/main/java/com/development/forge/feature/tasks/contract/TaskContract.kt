package com.development.forge.feature.tasks.contract

import com.development.forge.domain.model.Priority
import kotlin.time.Instant

data class TaskState(
    val taskId: String = "",
    val title: String = "",
    val priority: Priority = Priority.MED,
    val dueAt: Instant? = null,
    val project: String? = null,
    val notes: String? = null,
    val subTask: List<String> = emptyList(),
    val isSaving: Boolean = false
)

sealed interface TaskIntent {

}

sealed interface TaskEffect {
    data object NavigateBack : TaskEffect
}