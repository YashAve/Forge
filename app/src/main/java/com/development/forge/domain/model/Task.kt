package com.development.forge.domain.model

enum class Priority(val label: String) {
    LOW("LOW"),
    MED("MED"),
    HIGH("HIGH")
}

data class Subtask(
    val id: String,
    val title: String,
    val isDone: Boolean = false
)

data class Task(
    val id: String,
    val title: String,
    val done: Boolean = false,
    val priority: Priority = Priority.MED,
    val due: String? = null,
    val overdue: Boolean = false,
    val project: String? = null,
    val notes: String? = null,
    val subtasks: List<Subtask> = emptyList()
)

// ── Sample tasks — varied states, priorities, and content ──────────────────

val task1 = Task(
    id = "1",
    title = "Draft the Q3 proposal",
    done = false,
    priority = Priority.HIGH,
    due = "Today · 6:00 PM",
    overdue = false,
    project = "Work",
    notes = "Include revised budget breakdown and team headcount projections.",
    subtasks = listOf(
        Subtask("1a", "Outline the executive summary"),
        Subtask("1b", "Add the revenue forecast table", isDone = true),
        Subtask("1c", "Review with Mohammed before sending")
    )
)

val task2 = Task(
    id = "2",
    title = "Reply to the design team",
    done = true,
    priority = Priority.MED,
    due = "Today · 10:00 AM",
    overdue = false,
    project = "Work",
    notes = null,
    subtasks = emptyList()
)

val task3 = Task(
    id = "3",
    title = "Submit the expense report before the deadline passes completely",
    done = false,
    priority = Priority.LOW,
    due = "Yesterday",
    overdue = true,
    project = "Admin",
    notes = null,
    subtasks = emptyList()
)

val task4 = Task(
    id = "4",
    title = "Review the MDM enrollment flow",
    done = false,
    priority = Priority.HIGH,
    due = "Today · 3:00 PM",
    overdue = false,
    project = "CosmiqGuard",
    notes = "Check the zero-touch enrollment steps against the new AMAPI docs.",
    subtasks = listOf(
        Subtask("4a", "Test on the Pixel 7 emulator"),
        Subtask("4b", "Verify DPC extras are passing correctly")
    )
)

val task5 = Task(
    id = "5",
    title = "Update the Forge component library README",
    done = false,
    priority = Priority.MED,
    due = "Tomorrow",
    overdue = false,
    project = "Forge",
    notes = null,
    subtasks = emptyList()
)

val task6 = Task(
    id = "6",
    title = "Book the October flight to Dubai",
    done = false,
    priority = Priority.HIGH,
    due = "This week",
    overdue = false,
    project = null,
    notes = "Check Emirates and Air Arabia. Aim for the first week of October.",
    subtasks = listOf(
        Subtask("6a", "Compare flight prices"),
        Subtask("6b", "Confirm dates with Mum"),
        Subtask("6c", "Book the ticket", isDone = false)
    )
)

val task7 = Task(
    id = "7",
    title = "Read the Kotlin coroutines documentation — structured concurrency chapter",
    done = false,
    priority = Priority.MED,
    due = "Tonight",
    overdue = false,
    project = "Learning",
    notes = null,
    subtasks = emptyList()
)

val task8 = Task(
    id = "8",
    title = "Call the bank about the UAE account",
    done = true,
    priority = Priority.LOW,
    due = null,
    overdue = false,
    project = null,
    notes = null,
    subtasks = emptyList()
)

val task9 = Task(
    id = "9",
    title = "Finish the Stats screen Canvas ring animation",
    done = false,
    priority = Priority.HIGH,
    due = "This week",
    overdue = false,
    project = "Forge",
    notes = "The arc sweep needs to animate from 0 on entry — use animateFloatAsState.",
    subtasks = listOf(
        Subtask("9a", "Draw the track arc"),
        Subtask("9b", "Draw the filled sweep arc"),
        Subtask("9c", "Add the entry animation"),
        Subtask("9d", "Add the ember glow behind the ring")
    )
)

val task10 = Task(
    id = "10",
    title = "Prepare questions for the Dubai interview",
    done = false,
    priority = Priority.MED,
    due = "Next week",
    overdue = false,
    project = "Dubai Job Hunt",
    notes = "Cover Kotlin coroutines, Compose state, MVI, and Canvas custom drawing.",
    subtasks = emptyList()
)

val tasks = listOf(
    task1, task2, task3, task4, task5,
    task6, task7, task8, task9, task10
)