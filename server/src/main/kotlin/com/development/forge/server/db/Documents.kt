package com.development.forge.server.db

import com.development.forge.shared.api.Priority
import org.bson.codecs.pojo.annotations.BsonId

/**
 * ═══════════════════════════════════════════════════════════════
 * MONGO DOCUMENTS — the server's storage shapes
 * ═══════════════════════════════════════════════════════════════
 * Kept separate from the DTOs in `com.development.forge.shared.api` on purpose:
 * documents hold secrets ([UserDocument.passwordHash], the reset token) that
 * must never be serialized to a client. Mapping is explicit
 * (`toDto()`), so adding a field here can't accidentally leak it over the wire.
 *
 * These are plain data classes read by the Kotlin driver's data-class codec —
 * NOT `@Serializable`, which is what keeps them off the JSON layer entirely.
 * `@BsonId` maps `id` onto Mongo's `_id`.
 */

/**
 * A registered user.
 *
 * Ids are app-generated UUID strings rather than `ObjectId` so the same id shape
 * works for tasks, JWT subjects and the Android client without conversion.
 *
 * @param passwordHash BCrypt hash — never plaintext, never returned to a client.
 * @param resetToken   set by `/auth/forgot`, cleared by `/auth/reset`. Single-use.
 */
data class UserDocument(
    @BsonId val id: String,
    val email: String,
    val passwordHash: String,
    val createdAt: Long,
    val resetToken: String? = null,
    val resetTokenExpiresAt: Long? = null

    // TODO(FCM): Firebase is out of scope for now. When push notifications land,
    //  the device token belongs here as `val fcmTokens: List<String> = emptyList()`
    //  (a list, since one account can be signed in on several devices), written by
    //  a future `POST /auth/fcm-token` endpoint — see the matching TODO in
    //  `auth/AuthRoutes.kt`. Do NOT add Firebase dependencies until that work
    //  is actually scoped.
)

/** An embedded subtask. Lives inside [TaskDocument.subtasks] — there is no subtask collection. */
data class SubtaskDocument(
    val id: String,
    val title: String,
    val isDone: Boolean = false
)

/**
 * A task, owned by exactly one user.
 *
 * [userId] is the ownership key every query filters on (see
 * `task/TaskRepository.kt`) and is indexed at startup by [MongoDatabase].
 */
data class TaskDocument(
    @BsonId val id: String,
    val userId: String,
    val title: String,
    val done: Boolean = false,
    val priority: Priority = Priority.MED,
    val due: String? = null,
    val overdue: Boolean = false,
    val project: String? = null,
    val notes: String? = null,
    val subtasks: List<SubtaskDocument> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long
)
