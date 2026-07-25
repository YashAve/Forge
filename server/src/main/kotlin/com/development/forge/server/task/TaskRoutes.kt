package com.development.forge.server.task

import com.development.forge.server.db.toDto
import com.development.forge.server.error.NotFoundException
import com.development.forge.server.error.UnauthorizedException
import com.development.forge.server.plugins.JWT_AUTH
import com.development.forge.server.validation.Validation
import com.development.forge.shared.api.TaskUpsertRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

/**
 * `/tasks/…` — every route JWT-authenticated and scoped to the caller.
 *
 * The whole block sits inside `authenticate(JWT_AUTH)`, so an absent, malformed,
 * expired or wrongly-signed token is rejected by Ktor with 401 before any handler
 * runs. Handlers therefore only ever execute for a verified principal.
 */
fun Route.taskRoutes(taskRepository: TaskRepository) {

    authenticate(JWT_AUTH) {
        route("/tasks") {

            /** GET /tasks → 200 + the caller's tasks (newest first). Never anyone else's. */
            get {
                val userId = call.userId()
                val tasks = taskRepository.findAllForUser(userId).map { it.toDto() }
                call.respond(HttpStatusCode.OK, tasks)
            }

            /** POST /tasks → 201 + the created task. 400 if the title is blank. */
            post {
                val userId = call.userId()
                val body = call.receive<TaskUpsertRequest>()
                val title = Validation.taskTitle(body.title)

                val created = taskRepository.create(userId, body, title)
                call.respond(HttpStatusCode.Created, created.toDto())
            }

            /**
             * PUT /tasks/{id} → 200 + the updated task. Full replace, not a merge.
             *
             * 404 when the id is unknown **or owned by someone else** — the repository
             * filters on owner, so the two cases are indistinguishable by design.
             */
            put("/{id}") {
                val userId = call.userId()
                val taskId = call.taskId()
                val body = call.receive<TaskUpsertRequest>()
                val title = Validation.taskTitle(body.title)

                val updated = taskRepository.replace(userId, taskId, body, title)
                    ?: throw NotFoundException("task_not_found", "No task with id '$taskId'.")

                call.respond(HttpStatusCode.OK, updated.toDto())
            }

            /** DELETE /tasks/{id} → 200. 404 when unknown or not the caller's. */
            delete("/{id}") {
                val userId = call.userId()
                val taskId = call.taskId()

                val deleted = taskRepository.delete(userId, taskId)
                if (!deleted) {
                    throw NotFoundException("task_not_found", "No task with id '$taskId'.")
                }
                call.respond(
                    HttpStatusCode.OK,
                    com.development.forge.shared.api.MessageResponse("Task deleted.")
                )
            }
        }
    }
}

/**
 * The authenticated user's id, read from the JWT `sub` claim.
 *
 * This is the *only* source of the owner id used by task queries — it is never
 * taken from the request body, a query parameter or a header, so a client cannot
 * ask for another user's data. Ktor has already verified the signature, issuer,
 * audience and expiry by the time this runs; the 401 here is the belt-and-braces
 * case of a token that verified but carries no subject.
 */
private fun io.ktor.server.application.ApplicationCall.userId(): String =
    principal<JWTPrincipal>()?.subject
        ?: throw UnauthorizedException("invalid_token", "Token is missing a subject claim.")

/** The `{id}` path segment, validated non-blank. */
private fun io.ktor.server.application.ApplicationCall.taskId(): String =
    Validation.requireNotBlank(
        parameters["id"].orEmpty(), "task_id_required", "Task id is required."
    )
