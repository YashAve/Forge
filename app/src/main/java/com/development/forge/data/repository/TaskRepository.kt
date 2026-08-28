package com.development.forge.data.repository

import androidx.room.withTransaction
import com.development.forge.data.local.dao.SubtaskDao
import com.development.forge.data.local.dao.TaskDao
import com.development.forge.data.local.database.AppDatabase
import com.development.forge.data.local.entity.SubTaskEntity
import com.development.forge.data.local.entity.SyncState
import com.development.forge.data.local.entity.TaskEntity
import com.development.forge.domain.model.Priority
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Instant

@Singleton
class TaskRepository @Inject constructor(
    private val database: AppDatabase,
    private val taskDao: TaskDao,
    private val subtaskDao: SubtaskDao
) {

    suspend fun createTask(
        title: String,
        priority: Priority = Priority.MED,
        dueAt: Instant? = null,
        project: String? = null,
        notes: String? = null,
        subtasks: List<String> = emptyList()
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

    suspend fun updateTask(
        taskId: String,
        title: String,
        priority: Priority = Priority.MED,
        dueAt: Instant? = null,
        project: String? = null,
        notes: String? = null,
        subtasks: List<String> = emptyList()
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

    suspend fun deleteTask(taskId: String) =
        taskDao.softDeleteTask(taskId)

    suspend fun toggleTaskDone(taskId: String, isDone: Boolean) =
        taskDao.toggleTaskDone(taskId, isDone)

    private suspend fun saveTask(
        taskId: String,
        title: String,
        priority: Priority,
        dueAt: Instant?,
        project: String?,
        notes: String?,
        subtasks: List<String>,
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
        val subtaskEntities = subtasks.map { subtaskTitle ->
            SubTaskEntity(taskId = taskId, title = subtaskTitle)
        }
        database.withTransaction {
            taskDao.upsertTask(task)
            subtaskDao.upsertSubTask(*subtaskEntities.toTypedArray())
        }
    }
}
