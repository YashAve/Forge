package com.development.forge.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
enum class Priority(val label: String) {
    LOW("LOW"),
    MED("MED"),
    HIGH("HIGH")
}

@Serializable
data class Subtask(
    val id: String,
    val title: String,
    val isDone: Boolean = false
)

@Serializable
data class Task(
    val id: String,
    val title: String,
    val done: Boolean = false,
    val priority: Priority = Priority.MED,
    val due: String? = null,
    val overdue: Boolean = false,
    val project: String? = null,
    val notes: String? = null,
    val subtasks: List<Subtask> = emptyList(),
    val dueAt: Instant? = null
)
