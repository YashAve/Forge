package com.development.forge.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.development.forge.data.local.entity.SubTaskEntity
import com.development.forge.data.local.entity.SyncState
import com.development.forge.data.local.entity.TaskEntity
import com.development.forge.data.local.entity.TaskWithSubTask
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

/** One row per day of week that had completions. See [TaskDao.getCompletionsByDayOfWeek]. */
data class DayCompletionCount(
    @ColumnInfo(name = "day_of_week") val dayOfWeek: Int,
    @ColumnInfo(name = "completions") val completions: Int
)

@Dao
interface TaskDao {
    @Upsert
    suspend fun upsertTask(vararg task: TaskEntity)

    @Delete
    suspend fun deleteTask(vararg task: TaskEntity)

    @Transaction
    @Query("SELECT * FROM tasks")
    fun getTaskWithSubtask(): Flow<List<TaskWithSubTask>>

    @Delete
    fun deleteTasks(vararg task: TaskEntity)

    @Query("DELETE FROM tasks WHERE task_id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Query("SELECT * FROM tasks WHERE task_id = :taskId")
    fun getTaskById(taskId: String): Flow<TaskWithSubTask>

    @Transaction
    @Query("SELECT * FROM tasks WHERE sync_state != 'SYNCED'")
    suspend fun getPendingTasks(): List<TaskWithSubTask>

    @Query("DELETE FROM tasks WHERE task_id = :taskId AND sync_state = 'PENDING_DELETE'")
    suspend fun deletePending(taskId: String)

    @Query("UPDATE tasks SET done = :isDone, completed_at = :completedAt, sync_state = 'PENDING_UPDATE' WHERE task_id = :taskId")
    suspend fun toggleTaskDone(taskId: String, isDone: Boolean, completedAt: Instant?)

    @Query("""
    SELECT project FROM tasks
    WHERE project IS NOT NULL
    GROUP BY project
    ORDER BY COUNT(*) DESC
    LIMIT 5
""")
    suspend fun getTopProjects(): List<String>

    /**
     * Completions since [weekStartMillis] — the caller supplies the boundary.
     *
     * The boundary is computed in Kotlin (`TaskRepository.startOfCurrentWeekMillis`)
     * rather than in SQL. The previous version used
     * `strftime('%s','now','weekday 0','-7 days')`, which was wrong twice over:
     * `weekday 0` does not advance when today is already Sunday, so on Sundays the
     * window silently reported the *previous* week; and with no `start of day` the
     * week boundary drifted with the current clock time. Passing an explicit
     * millisecond boundary in makes the week a value this query is told about
     * rather than one it has to re-derive.
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE completed_at >= :weekStartMillis")
    fun getDoneThisWeek(weekStartMillis: Long): Flow<Int>

    /**
     * Completions since [weekStartMillis], bucketed by local day of week.
     *
     * `%w` is SQLite's day-of-week: **0 = Sunday** through 6 = Saturday. The chart
     * is Monday-first, so the repository rotates this into a Monday-based index.
     * `'localtime'` matters: `completed_at` is a UTC instant, and bucketing it by
     * UTC day would put a task completed at 02:00 IST on the previous day.
     *
     * Days with no completions are simply absent from the result — there is no row
     * to return for them — so the caller fills a seven-slot list rather than
     * assuming seven rows.
     */
    @Query("""
    SELECT CAST(strftime('%w', completed_at / 1000, 'unixepoch', 'localtime') AS INTEGER) AS day_of_week,
           COUNT(*) AS completions
    FROM tasks
    WHERE completed_at >= :weekStartMillis
    GROUP BY day_of_week
""")
    fun getCompletionsByDayOfWeek(weekStartMillis: Long): Flow<List<DayCompletionCount>>

    /**
     * Every distinct local date that had at least one completion, newest first,
     * as `yyyy-MM-dd`. Feeds streak calculation — see `TaskRepository.computeStreaks`.
     *
     * Deliberately unbounded: a streak can be arbitrarily long, so there is no
     * sensible cut-off, and the row count here is one per *day*, not per task.
     */
    @Query("""
    SELECT DISTINCT date(completed_at / 1000, 'unixepoch', 'localtime') AS day
    FROM tasks
    WHERE completed_at IS NOT NULL
    ORDER BY day DESC
""")
    fun getCompletionDays(): Flow<List<String>>

    @Query("UPDATE tasks SET sync_state = 'PENDING_DELETE' WHERE task_id = :taskId")
    suspend fun softDeleteTask(taskId: String)

    @Query("UPDATE tasks SET sync_state = 'SYNCED' WHERE task_id = :taskId")
    suspend fun markSynced(taskId: String)

    /** Null when there is no such row. Used to tell a never-uploaded task from a stored one. */
    @Query("SELECT sync_state FROM tasks WHERE task_id = :taskId")
    suspend fun syncStateOf(taskId: String): SyncState?

    @Transaction
    @Query("SELECT * FROM tasks WHERE sync_state != 'PENDING_DELETE'")
    fun getVisibleTasks(): Flow<List<TaskWithSubTask>>
}

@Dao
interface SubtaskDao {
    @Upsert
    suspend fun upsertSubTask(vararg subTask: SubTaskEntity)
    @Query("UPDATE subtasks SET is_done = 1 WHERE task_id = :taskId")
    suspend fun completeAllForTask(taskId: String)
    @Query("UPDATE subtasks SET is_done = :isDone WHERE subtask_id = :subtaskId")
    suspend fun toggleSubtaskDone(subtaskId: String, isDone: Boolean)
    @Delete
    suspend fun deleteSubTask(vararg subTask: SubTaskEntity)
    @Query("DELETE FROM subtasks WHERE subtask_id = :subtaskId")
    suspend fun deleteSubTaskById(subtaskId: String)
}