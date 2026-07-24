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

// sample data unchanged below — all 20 tasks
val task1 = Task(
    id = "1",
    title = "Draft the Q3 proposal",
    done = false,
    priority = Priority.HIGH,
    due = "Today",
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
    title = "Book the November flight to Dubai",
    done = false,
    priority = Priority.HIGH,
    due = "This week",
    overdue = false,
    project = "Dubai Move",
    notes = "Check Emirates and Air Arabia. Aim for the first week of November.",
    subtasks = listOf(
        Subtask("6a", "Compare flight prices on Emirates and Air Arabia"),
        Subtask("6b", "Confirm exact dates with Mum"),
        Subtask("6c", "Book the ticket"),
        Subtask("6d", "Email CosmiqGuard HR about last working day")
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

val task11 = Task(
    id = "11",
    title = "Submit EPFO withdrawal — Form 19 and Form 10C together",
    done = false,
    priority = Priority.HIGH,
    due = "This month",
    overdue = false,
    project = "Dubai Move",
    notes = "Both forms must be filed together at full settlement. PF + EPS.",
    subtasks = listOf(
        Subtask("11a", "Confirm last working day with HR"),
        Subtask("11b", "Download Form 19 from EPFO portal"),
        Subtask("11c", "Download Form 10C"),
        Subtask("11d", "Submit both together online")
    )
)

val task12 = Task(
    id = "12",
    title = "Pack and ship winter clothes to Dubai before November",
    done = false,
    priority = Priority.LOW,
    due = "October",
    overdue = false,
    project = "Dubai Move",
    notes = "Dubai winters are mild — pack light layers. Avoid shipping gym equipment, sell locally.",
    subtasks = emptyList()
)

val task13 = Task(
    id = "13",
    title = "Build Forge backend — Ktor server with Postgres",
    done = false,
    priority = Priority.HIGH,
    due = "This month",
    overdue = false,
    project = "Forge",
    notes = "Claude Code will scaffold it. Review every endpoint — must be defensible in an interview.",
    subtasks = listOf(
        Subtask("13a", "Auth endpoints: register, login, forgot password, reset"),
        Subtask("13b", "Task CRUD: create, read all, update, delete"),
        Subtask("13c", "JWT implementation — understand the flow"),
        Subtask("13d", "Docker Compose for local Postgres"),
        Subtask("13e", "Deploy to a VPS or Railway for the portfolio")
    )
)

val task14 = Task(
    id = "14",
    title = "Complete Forge MVI ViewModels for all screens",
    done = false,
    priority = Priority.HIGH,
    due = "This month",
    overdue = false,
    project = "Forge",
    notes = "HomeViewModel, TaskListViewModel, TaskDetailViewModel, StatsViewModel.",
    subtasks = listOf(
        Subtask("14a", "HomeViewModel — derive focus + upNext from StateFlow"),
        Subtask("14b", "TaskListViewModel — grouping logic, toggle, open"),
        Subtask("14c", "TaskDetailViewModel — subtask toggle, complete, reopen"),
        Subtask("14d", "StatsViewModel — compute streak, completion rate, week bars")
    )
)

val task15 = Task(
    id = "15",
    title = "Integrate Firebase FCM for task reminders",
    done = false,
    priority = Priority.MED,
    due = "This month",
    overdue = false,
    project = "Forge",
    notes = "Local scheduled notifications for known reminder times. FCM for server-initiated pushes.",
    subtasks = listOf(
        Subtask("15a", "Add Firebase to the project"),
        Subtask("15b", "Set up FCM token registration"),
        Subtask("15c", "Build the local notification scheduler"),
        Subtask("15d", "Test reminder delivery on S24 Ultra")
    )
)

val task16 = Task(
    id = "16",
    title = "Update resume with Forge and CosmiqGuard AMAPI work",
    done = false,
    priority = Priority.HIGH,
    due = "October",
    overdue = false,
    project = "Dubai Job Hunt",
    notes = "Lead with Forge — custom Canvas, MVI, Nav3, single Activity. Mohammed Ali as reference.",
    subtasks = listOf(
        Subtask("16a", "Write the Forge project bullet points"),
        Subtask("16b", "Add MDM/AMAPI/WebRTC bullet points from CosmiqGuard"),
        Subtask("16c", "Update the skills section"),
        Subtask("16d", "Send to Mohammed for review")
    )
)

val task17 = Task(
    id = "17",
    title = "Apply to Android developer roles in Dubai — first 20 applications",
    done = false,
    priority = Priority.HIGH,
    due = "October",
    overdue = false,
    project = "Dubai Job Hunt",
    notes = "LinkedIn, Bayt, GulfTalent, Naukrigulf. Target product companies and fintech first.",
    subtasks = listOf(
        Subtask("17a", "Set up LinkedIn profile with Dubai location preference"),
        Subtask("17b", "Identify top 20 target companies"),
        Subtask("17c", "Tailor cover note per application"),
        Subtask("17d", "Track applications in a spreadsheet")
    )
)

val task18 = Task(
    id = "18",
    title = "Learn basic Arabic greetings before November",
    done = false,
    priority = Priority.LOW,
    due = "October",
    overdue = false,
    project = "Dubai Move",
    notes = "Marhaba, Shukran, Inshallah, Yalla — the essentials. Mum will help.",
    subtasks = emptyList()
)

val task19 = Task(
    id = "19",
    title = "Deep dive into StateFlow vs SharedFlow for the interview",
    done = false,
    priority = Priority.MED,
    due = "This week",
    overdue = false,
    project = "Learning",
    notes = "StateFlow: always has a value, replays last to new collectors, for UI state. SharedFlow: configurable replay, for events/effects.",
    subtasks = emptyList()
)

val task20 = Task(
    id = "20",
    title = "Set up the Madar Development flat for the long stay",
    done = false,
    priority = Priority.MED,
    due = "November",
    overdue = false,
    project = "Dubai Move",
    notes = "Mum's flat. Confirm the room, get a SIM card, set up the workspace.",
    subtasks = listOf(
        Subtask("20a", "Confirm arrival with Mum"),
        Subtask("20b", "Buy a du or Etisalat SIM on arrival"),
        Subtask("20c", "Set up desk and development environment"),
        Subtask("20d", "Take Joey for a walk on the first day")
    )
)

val tasks = listOf(
    task1, task2, task3, task4, task5,
    task6, task7, task8, task9, task10,
    task11, task12, task13, task14, task15,
    task16, task17, task18, task19, task20
)