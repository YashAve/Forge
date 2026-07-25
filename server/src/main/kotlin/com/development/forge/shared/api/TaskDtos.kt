package com.development.forge.shared.api

import kotlinx.serialization.Serializable

/**
 * ═══════════════════════════════════════════════════════════════
 * SHARED API CONTRACT — task payloads
 * ═══════════════════════════════════════════════════════════════
 * This package is the wire contract between the Forge server and the Forge
 * Android client. It deliberately lives under `com.development.forge.shared.api`
 * rather than `…forge.server.…` so it can be lifted verbatim into a real shared
 * module later without changing a single import on either side.
 *
 * RULES for anything in this package:
 *   - `@Serializable` only, no server-only types (no BSON, no Ktor).
 *   - Nothing secret ever appears here — no password hashes, no reset tokens.
 *   - Defaults on every optional field so old clients keep deserializing.
 *
 * Mirrors the Android app's `com.development.forge.domain.model` shapes.
 */

/** Task urgency. Matches the Android app's `Priority` enum exactly (LOW / MED / HIGH). */
@Serializable
enum class Priority { LOW, MED, HIGH }

/**
 * A subtask as returned by the server. Subtasks are **embedded** in the task
 * document, so they have no endpoints of their own — they travel with the task.
 */
@Serializable
data class SubtaskDto(
    val id: String,
    val title: String,
    val isDone: Boolean = false
)

/** A subtask as *sent* by the client. `id` is optional — the server mints one when absent. */
@Serializable
data class SubtaskInput(
    val id: String? = null,
    val title: String,
    val isDone: Boolean = false
)

/**
 * A task as returned by the server — the full server-side truth.
 *
 * `userId` is the owner. It is server-assigned from the JWT and **ignored** if a
 * client tries to send one, so a task can never be created or moved into another
 * user's account. `createdAt` / `updatedAt` are epoch milliseconds (UTC).
 */
@Serializable
data class TaskDto(
    val id: String,
    val title: String,
    val done: Boolean = false,
    val priority: Priority = Priority.MED,
    val due: String? = null,
    val overdue: Boolean = false,
    val project: String? = null,
    val notes: String? = null,
    val subtasks: List<SubtaskDto> = emptyList(),
    val userId: String,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * The body for `POST /tasks` (create) and `PUT /tasks/{id}` (full replace).
 *
 * Intentionally has no `id`, `userId`, `createdAt` or `updatedAt`: identity,
 * ownership and timestamps are the server's business. `PUT` is a full replace —
 * every omitted field falls back to the default below, it is **not** a merge.
 */
@Serializable
data class TaskUpsertRequest(
    val title: String,
    val done: Boolean = false,
    val priority: Priority = Priority.MED,
    val due: String? = null,
    val overdue: Boolean = false,
    val project: String? = null,
    val notes: String? = null,
    val subtasks: List<SubtaskInput> = emptyList()
)
