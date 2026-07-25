package com.development.forge.server.task

import com.development.forge.server.db.MongoDatabase
import com.development.forge.server.db.SubtaskDocument
import com.development.forge.server.db.TaskDocument
import com.development.forge.shared.api.TaskUpsertRequest
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.conversions.Bson
import java.util.UUID

/**
 * ═══════════════════════════════════════════════════════════════
 * TASK STORAGE — and the single chokepoint for user scoping
 * ═══════════════════════════════════════════════════════════════
 * **Every** method here takes `userId` as its first parameter and folds it into
 * the Mongo filter via [ownedBy]. There is deliberately no `findById(id)` or
 * `all()` overload that omits the owner, so a route physically cannot ask for a
 * task without saying whose account it belongs to — the scoping can't be
 * forgotten at a call site because the type system won't let you.
 *
 * `userId` always comes from the verified JWT subject, never from the request
 * body or a query parameter (see `TaskRoutes.kt`).
 */
class TaskRepository(private val mongo: MongoDatabase) {

    /**
     * The ownership filter: `_id == taskId AND userId == userId`.
     *
     * Because ownership is part of the *query* rather than a check performed after
     * loading, a task belonging to someone else simply doesn't match — update and
     * delete report "not found" instead of touching another user's data.
     */
    private fun ownedBy(userId: String, taskId: String): Bson =
        Filters.and(Filters.eq("_id", taskId), Filters.eq(TaskDocument::userId.name, userId))

    /** All tasks for one user, newest first. */
    suspend fun findAllForUser(userId: String): List<TaskDocument> =
        mongo.tasks
            .find(Filters.eq(TaskDocument::userId.name, userId))
            .sort(Sorts.descending(TaskDocument::createdAt.name))
            .toList()

    /** One task, but only if [userId] owns it. Returns null otherwise — caller maps to 404. */
    suspend fun findForUser(userId: String, taskId: String): TaskDocument? =
        mongo.tasks.find(ownedBy(userId, taskId)).firstOrNull()

    /**
     * Creates a task owned by [userId].
     *
     * Note what is NOT taken from the request: id, userId, and both timestamps are
     * server-assigned, so a client can't choose its own id or plant a task in
     * another user's account.
     */
    suspend fun create(userId: String, request: TaskUpsertRequest, title: String): TaskDocument {
        val now = System.currentTimeMillis()
        val task = TaskDocument(
            id = UUID.randomUUID().toString(),
            userId = userId,
            title = title,
            done = request.done,
            priority = request.priority,
            due = request.due,
            overdue = request.overdue,
            project = request.project,
            notes = request.notes,
            subtasks = request.subtasks.toDocuments(),
            createdAt = now,
            updatedAt = now
        )
        mongo.tasks.insertOne(task)
        return task
    }

    /**
     * Full replace of an existing task. Returns the updated document, or null if
     * the id doesn't exist **or belongs to another user**.
     *
     * `id`, `userId` and `createdAt` are carried over from the stored document
     * rather than the request, so a PUT can never re-home or re-date a task.
     */
    suspend fun replace(
        userId: String,
        taskId: String,
        request: TaskUpsertRequest,
        title: String
    ): TaskDocument? {
        val existing = findForUser(userId, taskId) ?: return null
        val updated = existing.copy(
            title = title,
            done = request.done,
            priority = request.priority,
            due = request.due,
            overdue = request.overdue,
            project = request.project,
            notes = request.notes,
            subtasks = request.subtasks.toDocuments(),
            updatedAt = System.currentTimeMillis()
        )
        // Filter (not just the id) repeats the ownership condition, so the write is
        // scoped even if the document changed hands between read and write.
        mongo.tasks.replaceOne(ownedBy(userId, taskId), updated)
        return updated
    }

    /** Deletes the task if [userId] owns it. False → 404 (missing or not yours). */
    suspend fun delete(userId: String, taskId: String): Boolean =
        mongo.tasks.deleteOne(ownedBy(userId, taskId)).deletedCount > 0

    /** Client-supplied subtasks keep their id when they have one, otherwise get a fresh one. */
    private fun List<com.development.forge.shared.api.SubtaskInput>.toDocuments() =
        map { input ->
            SubtaskDocument(
                id = input.id?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
                title = input.title.trim(),
                isDone = input.isDone
            )
        }
}
