package com.development.forge.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverter
import com.development.forge.domain.model.Priority
import java.util.UUID
import kotlin.time.Instant

class TypeConverters {
    @TypeConverter
    fun fromInstantToMilliseconds(input: Instant?): Long? = input?.toEpochMilliseconds()

    @TypeConverter
    fun fromMillisecondsToInstant(input: Long?): Instant? =
        input?.let { Instant.fromEpochMilliseconds(it) }
}

enum class SyncState {
    SYNCED,
    PENDING_CREATE,
    PENDING_UPDATE,
    PENDING_DELETE
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    @ColumnInfo("task_id") val taskId: String = UUID.randomUUID().toString(),
    @ColumnInfo("title") val title: String,
    @ColumnInfo("done") val done: Boolean = false,
    @ColumnInfo("priority") val priority: Priority = Priority.MED,
    @ColumnInfo("due") val due: String? = null,
    @ColumnInfo("overdue") val overdue: Boolean = false,
    @ColumnInfo("project") val project: String? = null,
    @ColumnInfo("notes") val notes: String? = null,
    @ColumnInfo("due_at") val dueAt: Instant? = null,
    @ColumnInfo("sync_state") val syncState: SyncState = SyncState.PENDING_CREATE
)

@Entity(
    tableName = "subtasks", foreignKeys = [
        ForeignKey(
            TaskEntity::class,
            childColumns = ["task_id"],
            parentColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("task_id")]
)
data class SubTaskEntity(
    @PrimaryKey
    @ColumnInfo("subtask_id") val subTaskId: String = UUID.randomUUID().toString(),
    @ColumnInfo("task_id") val taskId: String,
    @ColumnInfo("title") val title: String,
    @ColumnInfo("is_done") val isDone: Boolean = false
)

data class TaskWithSubTask(
    @Embedded val task: TaskEntity,
    @Relation(
        entity = SubTaskEntity::class,
        parentColumn = "task_id",
        entityColumn = "task_id"
    )
    val subtasks: List<SubTaskEntity>
)
