package com.development.forge.data.repository

import androidx.room.withTransaction
import com.development.forge.BuildConfig
import com.development.forge.data.local.dao.SubtaskDao
import com.development.forge.data.local.dao.TaskDao
import com.development.forge.data.local.database.AppDatabase
import com.development.forge.data.local.entity.SubTaskEntity
import com.development.forge.data.local.entity.SyncState
import com.development.forge.data.local.entity.TaskEntity
import com.development.forge.core.datastore.ForgePreferences
import com.development.forge.data.local.entity.TaskWithSubTask
import com.development.forge.data.remote.TaskApi
import com.development.forge.domain.model.Priority
import com.development.forge.domain.model.Subtask
import com.development.forge.domain.model.Task
import com.development.forge.shared.api.SubtaskInput
import com.development.forge.shared.api.TaskUpsertRequest
import com.development.forge.shared.api.Priority as SharedPriority
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Instant

@Singleton
class TaskRepository @Inject constructor(
    private val database: AppDatabase,
    private val taskApi: TaskApi,
    private val preferences: ForgePreferences,
    private val taskDao: TaskDao,
    private val subtaskDao: SubtaskDao
) {

    suspend fun createTask(
        title: String,
        priority: Priority = Priority.MED,
        dueAt: Instant? = null,
        project: String? = null,
        notes: String? = null,
        subtasks: List<Subtask> = emptyList()
    ) {
        saveTask(
            taskId = UUID.randomUUID().toString(),
            title = title,
            priority = priority,
            dueAt = dueAt,
            project = project,
            notes = notes,
            subtasks = subtasks,
            syncState = SyncState.PENDING_CREATE
        )
    }

    suspend fun getTaskById(taskId: String): Task? =
        taskDao.getTaskById(taskId).first().toTask()

    fun getVisibleTasks() = taskDao.getVisibleTasks()
        .map { list -> list.map { it.toTask() } }

    private fun TaskWithSubTask.toTask() = Task(
        id = task.taskId,
        title = task.title,
        done = task.done,
        priority = task.priority,
        dueAt = task.dueAt,
        project = task.project,
        notes = task.notes,
        subtasks = subtasks.map { Subtask(it.subTaskId, it.title, it.isDone) }
    )

    suspend fun updateTask(
        taskId: String,
        title: String,
        priority: Priority = Priority.MED,
        dueAt: Instant? = null,
        project: String? = null,
        notes: String? = null,
        subtasks: List<Subtask> = emptyList()
    ) {
        saveTask(
            taskId = taskId,
            title = title,
            priority = priority,
            dueAt = dueAt,
            project = project,
            notes = notes,
            subtasks = subtasks,
            syncState = SyncState.PENDING_UPDATE
        )
    }

    suspend fun getTopProjects() = taskDao.getTopProjects()

    /** Tasks completed since the start of the current (Monday-based) week. */
    fun doneThisWeek(): Flow<Int> = taskDao.getDoneThisWeek(startOfCurrentWeekMillis())

    /**
     * Seven completion counts for the current week, **Monday first**, to match the
     * chart's existing `M T W T F S S` labels.
     *
     * SQLite's `%w` is Sunday-based (0 = Sunday), so `(dayOfWeek + 6) % 7` rotates
     * it: Monday's 1 becomes index 0, Sunday's 0 becomes index 6. Days with no
     * completions never come back from the GROUP BY, so the list starts at seven
     * zeroes and is filled in.
     */
    fun weekCompletions(): Flow<List<Int>> =
        taskDao.getCompletionsByDayOfWeek(startOfCurrentWeekMillis()).map { rows ->
            MutableList(DAYS_IN_WEEK) { 0 }.apply {
                rows.forEach { row -> this[(row.dayOfWeek + 6) % DAYS_IN_WEEK] = row.completions }
            }
        }

    /** Current and longest run of consecutive days with at least one completion. */
    fun streaks(): Flow<StreakInfo> =
        taskDao.getCompletionDays().map { days -> computeStreaks(days) }

    /**
     * Midnight on the most recent Monday, in the device's time zone.
     *
     * `Calendar.DAY_OF_WEEK` is Sunday-based (SUNDAY = 1 … SATURDAY = 7), so
     * `(dayOfWeek + 5) % 7` gives days elapsed since Monday: Monday → 0,
     * Sunday → 6. Computed here rather than in SQL because SQLite's `weekday`
     * modifier cannot express "the current week" without the off-by-a-week bug it
     * had before — see [TaskDao.getDoneThisWeek].
     */
    /**
     * Pushes everything pending to the server, then marks it synced.
     *
     * **Silence is the contract here, not a shortcut.** The server lives on a LAN
     * machine that is usually off; the app is fully usable without it. So an
     * unreachable host is an ordinary outcome, not an error state — nothing is
     * surfaced, nothing is retried in a loop, and every unsent row simply stays
     * pending for the next attempt. This is the one place in the codebase where a
     * swallowed network failure is correct rather than a bug, which is why it is
     * spelled out rather than left as a bare `catch`.
     *
     * A transport failure aborts the whole run: if the host did not answer for one
     * task it will not answer for the next eighteen, and marching through them all
     * only buys a stack of timeouts. A *protocol* failure — some non-2xx for one
     * specific row — skips only that row, since it says nothing about the others.
     */
    suspend fun syncPendingTasks() {
        val token = preferences.authToken().first()
        if (token.isBlank()) return

        for (pending in taskDao.getPendingTasks()) {
            try {
                push(token, pending)
            } catch (_: IOException) {
                return
            } catch (_: Exception) {
                // Client-side failure for this row (serialization, a malformed
                // field). Leave it pending rather than blocking the queue behind it.
                continue
            }
        }
    }

    /** Sends one pending row and, on success, records that locally. */
    private suspend fun push(token: String, pending: TaskWithSubTask) {
        val taskId = pending.task.taskId
        when (pending.task.syncState) {
            SyncState.PENDING_CREATE -> {
                val response = taskApi.createTask(token, pending.toUpsertRequest())
                // 201 = created, 200 = that id was already there from an earlier
                // attempt whose response never made it back. Both mean the server
                // holds this task now, which is all SYNCED claims.
                if (response.status.isSuccess()) taskDao.markSynced(taskId)
            }

            SyncState.PENDING_UPDATE -> {
                val response = taskApi.updateTask(token, taskId, pending.toUpsertRequest())
                if (response.status.isSuccess()) taskDao.markSynced(taskId)
            }

            SyncState.PENDING_DELETE -> {
                val response = taskApi.deleteTask(token, taskId)
                // 404 is success in the only sense that matters: the server does not
                // have this task, which is exactly what we asked it to arrange.
                if (response.status.isSuccess() || response.status == HttpStatusCode.NotFound) {
                    taskDao.deletePending(taskId)
                }
            }

            // getPendingTasks filters these out; the branch exists so adding a
            // SyncState later is a compile error here rather than a silent no-op.
            SyncState.SYNCED -> Unit
        }
    }

    /**
     * Room row to wire request.
     *
     * [TaskUpsertRequest.id] carries the locally-generated UUID — the whole point of
     * the phone owning the id is that a task keeps one identity on both sides.
     *
     * The two `Priority` enums are different types on purpose: the domain one here,
     * [SharedPriority] on the wire, so the wire contract can change without dragging
     * the domain model along. They share case names, so mapping by `name` stays
     * honest — if either ever gains a case the other lacks it throws rather than
     * silently choosing a default.
     */
    private fun TaskWithSubTask.toUpsertRequest() = TaskUpsertRequest(
        id = task.taskId,
        title = task.title,
        done = task.done,
        priority = SharedPriority.valueOf(task.priority.name),
        due = task.due,
        overdue = task.overdue,
        project = task.project,
        notes = task.notes,
        subtasks = subtasks.map { SubtaskInput(it.subTaskId, it.title, it.isDone) }
    )

    /**
     * Fills the local database with ~18 tasks so Stats has something to compute.
     *
     * **Debug only, twice over**: the row that calls this is only rendered when
     * `BuildConfig.DEBUG`, and this guard means that even a stray call site in
     * release code does nothing. R8 sees `BuildConfig.DEBUG` as a constant `false`
     * in release and strips the body outright.
     *
     * Rows are written [SyncState.SYNCED], not `PENDING_CREATE` — the sync loop
     * drains anything pending, and seeded fixtures have no business being uploaded
     * to a real account.
     *
     * The completion dates are chosen, not random. Completions land on days
     * 0, 1, 3, 4, 5, 6, 7 and 9 ago, so:
     *  - **the current streak is 2** (today and yesterday), broken by the gap at
     *    day 2;
     *  - **the longest streak is 5** (days 3–7);
     *  - current < longest, so the "personal best" suffix stays hidden — the case
     *    the old hardcoded card could never show.
     *
     * Calling it twice seeds a second batch; it does not clear what is already
     * there, because silently deleting a database is a worse debug affordance than
     * duplicate fixtures.
     */
    suspend fun seedDebugData() {
        if (!BuildConfig.DEBUG) return

        data class Seed(
            val title: String,
            val priority: Priority,
            val project: String?,
            val notes: String? = null,
            val daysAgoCompleted: Int? = null,
            val subtasks: List<Pair<String, Boolean>> = emptyList()
        )

        val seeds = listOf(
            // --- day 0: today (2 completions) ---
            Seed("Ship the stats screen", Priority.HIGH, "Forge", daysAgoCompleted = 0,
                subtasks = listOf("Wire the DAO" to true, "Replace the literals" to true)),
            Seed("Reply to the design review", Priority.MED, "Work", daysAgoCompleted = 0),
            // --- day 1: yesterday (1) — current streak = 2 ---
            Seed("Fix the subtask duplication bug", Priority.HIGH, "Forge", daysAgoCompleted = 1,
                notes = "Upsert was inserting because ids were re-derived."),
            // --- day 2: GAP, deliberately empty — this is what breaks the streak ---
            // --- days 3..7: five consecutive days, the longest run ---
            Seed("Draft the Q3 proposal", Priority.HIGH, "Work", daysAgoCompleted = 3,
                subtasks = listOf("Outline" to true, "Budget table" to true, "Review" to false)),
            Seed("Book the November flight", Priority.HIGH, "Dubai Move", daysAgoCompleted = 4),
            Seed("Read the coroutines chapter", Priority.LOW, "Learning", daysAgoCompleted = 5),
            Seed("Submit the expense report", Priority.LOW, "Admin", daysAgoCompleted = 5),
            Seed("Update the component README", Priority.MED, "Forge", daysAgoCompleted = 6),
            Seed("Call the bank", Priority.LOW, null, daysAgoCompleted = 7),
            // --- day 8: GAP ---
            // --- day 9: one more, isolated ---
            Seed("Pack the winter clothes", Priority.MED, "Dubai Move", daysAgoCompleted = 9,
                subtasks = listOf("Sort layers" to true)),

            // --- open tasks: no completed_at, so invisible to every stats query ---
            Seed("Wire the sync layer", Priority.HIGH, "Forge",
                notes = "POST/PUT/DELETE plus the pending drain.",
                subtasks = listOf("TaskApi methods" to true, "Drain pendingTasks" to false)),
            Seed("Prepare interview questions", Priority.HIGH, "Dubai Job Hunt"),
            Seed("Learn basic Arabic greetings", Priority.LOW, "Dubai Move"),
            Seed("Deep dive StateFlow vs SharedFlow", Priority.MED, "Learning",
                subtasks = listOf("Replay semantics" to false, "Write the notes" to false)),
            Seed("Renew the domain", Priority.MED, "Admin"),
            Seed("Set up the desk", Priority.LOW, null),
            Seed("Tidy the Room migrations", Priority.MED, "Forge"),
            Seed("Buy a SIM on arrival", Priority.LOW, null)
        )

        database.withTransaction {
            seeds.forEach { seed ->
                val taskId = UUID.randomUUID().toString()
                taskDao.upsertTask(
                    TaskEntity(
                        taskId = taskId,
                        title = seed.title,
                        done = seed.daysAgoCompleted != null,
                        priority = seed.priority,
                        project = seed.project,
                        notes = seed.notes,
                        completedAt = seed.daysAgoCompleted?.let { middayDaysAgo(it) },
                        syncState = SyncState.SYNCED
                    )
                )
                seed.subtasks.forEach { (title, isDone) ->
                    subtaskDao.upsertSubTask(
                        SubTaskEntity(
                            subTaskId = UUID.randomUUID().toString(),
                            taskId = taskId,
                            title = title,
                            isDone = isDone
                        )
                    )
                }
            }
        }
    }

    /**
     * Midday, [daysAgo] days back, in the device's time zone.
     *
     * Midday rather than "now minus N × 24h" so a seed can never drift across a
     * local midnight and land on the day either side of the one intended — which
     * would quietly change the streak the fixtures are built to produce.
     */
    private fun middayDaysAgo(daysAgo: Int): Instant =
        Instant.fromEpochMilliseconds(
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_YEAR, -daysAgo)
            }.timeInMillis
        )

    private fun startOfCurrentWeekMillis(): Long =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -((get(Calendar.DAY_OF_WEEK) + 5) % DAYS_IN_WEEK))
        }.timeInMillis

    /**
     * Walks [daysDesc] — distinct `yyyy-MM-dd` local dates, newest first — into a
     * current and a longest streak.
     *
     * **Today never breaks the current streak.** A day with nothing completed yet is
     * still in progress, so if today is absent the walk starts at yesterday instead
     * of returning zero. Yesterday's absence does break it.
     */
    private fun computeStreaks(daysDesc: List<String>): StreakInfo {
        if (daysDesc.isEmpty()) return StreakInfo(current = 0, longest = 0)

        val completedDays = daysDesc.toSet()
        val formatter = SimpleDateFormat(DAY_PATTERN, Locale.US)

        val cursor = Calendar.getInstance()
        if (formatter.format(cursor.time) !in completedDays) {
            cursor.add(Calendar.DAY_OF_YEAR, -1)
        }
        var current = 0
        while (formatter.format(cursor.time) in completedDays) {
            current++
            cursor.add(Calendar.DAY_OF_YEAR, -1)
        }

        // The list is distinct and descending, so a run continues wherever the next
        // entry is exactly the day before the previous one.
        var longest = 1
        var run = 1
        for (i in 1 until daysDesc.size) {
            val dayBefore = Calendar.getInstance().apply {
                time = formatter.parse(daysDesc[i - 1])!!
                add(Calendar.DAY_OF_YEAR, -1)
            }
            run = if (formatter.format(dayBefore.time) == daysDesc[i]) run + 1 else 1
            if (run > longest) longest = run
        }

        return StreakInfo(current = current, longest = longest)
    }

    /**
     * Deletes a task, tombstoning it for sync **only if the server might know about it**.
     *
     * A task still in [SyncState.PENDING_CREATE] has never been uploaded, so there is
     * nothing to tell the server about and no reason to keep a tombstone the sync
     * would send a doomed `DELETE` for. It is simply removed.
     *
     * Doing the check here rather than in [syncPendingTasks] is what keeps
     * `PENDING_DELETE` meaning exactly one thing — "the server has this row, remove
     * it" — instead of a state the sync has to disambiguate after the fact, by which
     * point the information needed to do so is already gone.
     */
    suspend fun deleteTask(taskId: String) = database.withTransaction {
        if (taskDao.syncStateOf(taskId) == SyncState.PENDING_CREATE) {
            taskDao.deleteTaskById(taskId)
        } else {
            taskDao.softDeleteTask(taskId)
        }
    }

    suspend fun toggleTaskDone(taskId: String, isDone: Boolean) =
        taskDao.toggleTaskDone(taskId, isDone, if (isDone) kotlin.time.Clock.System.now() else null)

    suspend fun toggleSubtaskDone(subtaskId: String, isDone: Boolean) =
        subtaskDao.toggleSubtaskDone(subtaskId, isDone)

    suspend fun completeAllSubtasks(taskId: String) {
        database.withTransaction {
            subtaskDao.completeAllForTask(taskId)
            toggleTaskDone(taskId, true)
        }
    }

    private suspend fun saveTask(
        taskId: String,
        title: String,
        priority: Priority,
        dueAt: Instant?,
        project: String?,
        notes: String?,
        subtasks: List<Subtask>,
        syncState: SyncState
    ) {
        val task = TaskEntity(
            taskId = taskId,
            title = title,
            priority = priority,
            dueAt = dueAt,
            project = project,
            notes = notes,
            syncState = syncState
        )
        val subtaskEntities = subtasks.map { subtask ->
            SubTaskEntity(
                subTaskId = subtask.id,
                taskId = taskId,
                title = subtask.title,
                isDone = subtask.isDone
            )
        }
        database.withTransaction {
            taskDao.upsertTask(task)
            subtaskDao.upsertSubTask(*subtaskEntities.toTypedArray())
        }
    }

    private companion object {
        const val DAYS_IN_WEEK = 7
        const val DAY_PATTERN = "yyyy-MM-dd"
    }
}

/** Consecutive-day completion runs. [current] counts back from today; [longest] is all-time. */
data class StreakInfo(
    val current: Int,
    val longest: Int
)
