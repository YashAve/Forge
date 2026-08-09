package com.development.forge.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.development.forge.domain.model.Priority
import java.util.UUID

private val id: String
    get() = "${System.currentTimeMillis()}-${UUID.randomUUID()}"

@Entity(
    tableName = "tasks", foreignKeys = [
        ForeignKey(
            SubTaskEntity::class,
            childColumns = ["subtask_id"],
            parentColumns = ["subtask_id"],
            onDelete = ForeignKey.CASCADE
        )]
)
data class TaskEntity(
    @PrimaryKey
    @ColumnInfo(name = "task_id") val taskId: String = id,
    @ColumnInfo(name = "subtask_id") val subtaskId: String? = null,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo("done") val done: Boolean = false,
    @ColumnInfo(name = "priority") val priority: Priority = Priority.MED,
    @ColumnInfo("due") val due: String? = null,
    @ColumnInfo(name = "overdue") val overdue: Boolean = false,
    @ColumnInfo("project") val project: String? = null,
    @ColumnInfo("notes") val notes: String? = null,
)

@Entity
data class SubTaskEntity(
    @PrimaryKey
    @ColumnInfo(name = "subtask_id") val subTaskId: String = id,
    @ColumnInfo("title") val title: String,
    @ColumnInfo("is_done") val isDone: Boolean = false
)
