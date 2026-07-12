package com.development.forge.domain.model

enum class Priority { LOW, MED, HIGH }

data class Subtask(
    val id: String,
    val title: String,
    val isDone: Boolean = false
)

data class Task(
    val id: String,
    val title: String,
    val priority: Priority = Priority.MED,
    val subtasks: List<Subtask> = emptyList()
)

val sampleTasks: List<Task> = emptyList()
