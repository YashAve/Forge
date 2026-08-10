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
}

@Dao
interface SubtaskDao {
    @Upsert
    suspend fun upsertSubTask(vararg subTask: SubTaskEntity)
    @Delete
    suspend fun deleteSubTask(vararg subTask: SubTaskEntity)
    @Query("DELETE FROM subtasks WHERE subtask_id = :subtaskId")
    suspend fun deleteSubTaskById(subtaskId: String)
}