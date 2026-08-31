package com.development.forge.data.repository

import androidx.room.withTransaction
import com.development.forge.data.local.dao.SubtaskDao
import com.development.forge.data.local.dao.TaskDao
import com.development.forge.data.local.database.AppDatabase
import com.development.forge.data.local.entity.SubTaskEntity
import com.development.forge.data.local.entity.SyncState
import com.development.forge.data.local.entity.TaskEntity
import com.development.forge.data.local.entity.TaskWithSubTask
import com.development.forge.domain.model.Priority
import com.development.forge.domain.model.Subtask
import com.development.forge.domain.model.Task
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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

    suspend fun getTopProjects() = taskDao.getTopProjects()

    suspend fun deleteTask(taskId: String) =
        taskDao.softDeleteTask(taskId)

    suspend fun toggleTaskDone(taskId: String, isDone: Boolean) =
        taskDao.toggleTaskDone(taskId, isDone)
    suspend fun completeAllSubtasks(taskId: String) = subtaskDao.completeAllForTask(taskId)

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
