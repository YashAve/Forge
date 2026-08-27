package com.development.forge.server.task

import com.development.forge.server.db.MongoDatabase
import com.development.forge.server.db.SubtaskDocument
import com.development.forge.server.db.TaskDocument
import com.development.forge.server.error.ConflictException
import com.development.forge.shared.api.TaskUpsertRequest
import com.mongodb.MongoWriteException
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
     * Creates a task owned by [userId] under the caller-chosen [taskId], or reports
     * that the caller already has a task with that id.
     *
     * `userId` and both timestamps are still server-assigned, so a client cannot
     * plant a task in another user's account or re-date one. [taskId] is the single
     * thing the client now chooses, and `TaskRoutes` has already checked it is a
     * canonical UUID before it gets here.
     *
     * ## Why a duplicate is not simply a conflict
     * For an offline-first client a re-sent create is *ordinary*: the network drops
     * after the write lands but before the 201 does, and the sync queue retries. So
     * the caller's own id coming back is success — [TaskCreateResult.AlreadyExisted]
     * — not an error the client has to special-case.
     *
     * ## Why the ownership check is load-bearing
     * `_id` is unique across the **whole collection**, not per user: the owner
     * filter that scopes every other method here does not scope the uniqueness
     * namespace. So a duplicate-key error can come from a document belonging to
     * somebody else, and "catch duplicate, return the existing task" — the obvious
     * implementation — would hand one user another user's task. That is why the
     * only read on this path goes through [findForUser], and why the fallback when
     * it finds nothing is a deliberately vague 409 that does not say whose id it is.
     * Same posture as the 404 that conflates "no such task" with "not yours".
     *
     * The re-read inside the catch closes the race where two concurrent creates from
     * the same client both miss the first lookup: the loser would otherwise be told
     * its own task conflicts.
     */
    suspend fun create(
        userId: String,
        taskId: String,
        request: TaskUpsertRequest,
        title: String
    ): TaskCreateResult {
        findForUser(userId, taskId)?.let { return TaskCreateResult.AlreadyExisted(it) }

        val now = System.currentTimeMillis()
        val task = TaskDocument(
            id = taskId,
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
        try {
            mongo.tasks.insertOne(task)
        } catch (e: MongoWriteException) {
            if (e.error.category != com.mongodb.ErrorCategory.DUPLICATE_KEY) throw e
            // Either we lost a race with the caller's own concurrent create, or the
            // id belongs to another account. Re-reading under the owner filter tells
            // the two apart without ever reading a document the caller does not own.
            val alreadyTheirs = findForUser(userId, taskId)
                ?: throw ConflictException("task_id_taken", "That task id is already in use.")
            return TaskCreateResult.AlreadyExisted(alreadyTheirs)
        }
        return TaskCreateResult.Created(task)
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

/**
 * What [TaskRepository.create] actually did.
 *
 * Both outcomes are successes and both carry the same [task]; they differ only in
 * the status code the route should answer with. A sealed type rather than a
 * boolean so the `when` in `TaskRoutes` is checked by the compiler — and so the
 * two cases are named at the call site instead of being a `true` whose meaning has
 * to be remembered.
 */
sealed interface TaskCreateResult {

    val task: TaskDocument

    /** Inserted by this request → **201**. */
    data class Created(override val task: TaskDocument) : TaskCreateResult

    /**
     * The caller already had this id and nothing was written → **200**.
     *
     * The stored task is returned as-is: the request body is deliberately *not*
     * applied. A retry normally carries an identical body, and where it doesn't —
     * the client edited the task between the failed create and the retry — silently
     * replacing the stored version here would duplicate `PUT /tasks/{id}` while
     * bypassing its explicit full-replace contract. A client that edits after a
     * failed create should follow the 200 with a PUT.
     */
    data class AlreadyExisted(override val task: TaskDocument) : TaskCreateResult
}
