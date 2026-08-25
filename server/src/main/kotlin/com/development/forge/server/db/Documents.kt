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

/** How an account proves who it is. Defaults to [PASSWORD] for every pre-existing row. */
enum class AuthProvider { PASSWORD, GOOGLE }

/**
 * A registered user.
 *
 * Ids are app-generated UUID strings rather than `ObjectId` so the same id shape
 * works for tasks, JWT subjects and the Android client without conversion.
 *
 * @param passwordHash BCrypt hash — never plaintext, never returned to a client.
 *   **Empty string for [AuthProvider.GOOGLE] accounts**, which have no password.
 *
 *   `String?` is the type-honest model for "this account has no password", and it
 *   was the first choice — but it would change the type at
 *   `PasswordHasher.verify(body.password, user.passwordHash)` in `/auth/login`,
 *   whose second parameter is non-null `String`. Making it nullable therefore
 *   forces an edit to a route that is deliberately frozen.
 *
 *   `""` is safe rather than merely convenient: no BCrypt hash can match it, and
 *   `PasswordHasher.verify` already returns `false` for a malformed stored hash
 *   instead of throwing. So a Google account attempting password login fails as an
 *   ordinary `invalid_credentials` 401, indistinguishable from a wrong password —
 *   the anti-enumeration posture holds without `/auth/login` knowing this field
 *   exists. Branch on [authProvider], never on `passwordHash.isEmpty()`.
 * @param authProvider how this account signs in. Absent on documents written
 *   before Google sign-in existed, which the constructor default reads as
 *   [AuthProvider.PASSWORD] — correct, since those all have a real hash.
 * @param resetToken   set by `/auth/forgot`, cleared by `/auth/reset`. Single-use.
 *   Never issued for a [AuthProvider.GOOGLE] account.
 */
data class UserDocument(
    @BsonId val id: String,
    val email: String,
    val passwordHash: String,
    val createdAt: Long,
    val authProvider: AuthProvider = AuthProvider.PASSWORD,
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
