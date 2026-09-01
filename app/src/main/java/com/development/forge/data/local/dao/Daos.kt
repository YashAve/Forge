package com.development.forge.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.development.forge.data.local.entity.SubTaskEntity
import com.development.forge.data.local.entity.TaskEntity
import com.development.forge.data.local.entity.TaskWithSubTask
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

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
    @Query("UPDATE tasks SET sync_state = 'PENDING_DELETE' WHERE task_id = :taskId")
    suspend fun softDeleteTask(taskId: String)
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