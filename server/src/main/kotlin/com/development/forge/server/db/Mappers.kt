package com.development.forge.server.db

import com.development.forge.shared.api.SubtaskDto
import com.development.forge.shared.api.TaskDto
import com.development.forge.shared.api.UserDto

/**
 * Document → DTO mapping. The boundary where storage shapes become wire shapes.
 *
 * These conversions are written out by hand on purpose: it means a new field on a
 * document (say, a password reset token or an FCM token) is invisible to clients
 * until someone explicitly adds it here. There is intentionally **no**
 * `UserDocument.toDto()` that includes `passwordHash`.
 */

fun UserDocument.toDto(): UserDto = UserDto(
    id = id,
    email = email,
    createdAt = createdAt
    // passwordHash / resetToken deliberately omitted — never leaves the server.
)

fun SubtaskDocument.toDto(): SubtaskDto = SubtaskDto(
    id = id,
    title = title,
    isDone = isDone
)

fun TaskDocument.toDto(): TaskDto = TaskDto(
    id = id,
    title = title,
    done = done,
    priority = priority,
    due = due,
    overdue = overdue,
    project = project,
    notes = notes,
    subtasks = subtasks.map { it.toDto() },
    userId = userId,
    createdAt = createdAt,
    updatedAt = updatedAt
)
