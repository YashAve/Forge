package com.development.forge.data.repository

import com.development.forge.data.local.dao.SubtaskDao
import com.development.forge.data.local.dao.TaskDao
import com.development.forge.data.local.entity.SubTaskEntity
import com.development.forge.data.local.entity.TaskEntity
import com.development.forge.domain.model.Priority
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Instant

@Singleton
class TaskRepository @Inject constructor(
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

        val taskId = UUID.randomUUID().toString()

        val task = TaskEntity(
            taskId = taskId,
            title = title,
            priority = priority,
            dueAt = dueAt,
            project = project,
            notes = notes
        )

        val subtaskEntities = subtasks.map { subtaskTitle ->
            SubTaskEntity(
                taskId = taskId,
                title = subtaskTitle
            )
        }

        taskDao.createTaskWithSubtasks(subtaskDao, task, subtaskEntities)
    }
}