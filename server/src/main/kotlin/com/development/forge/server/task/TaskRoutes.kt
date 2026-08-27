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
import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * `/tasks/…` — every route JWT-authenticated and scoped to the caller.
 *
 * The whole block sits inside `authenticate(JWT_AUTH)`, so an absent, malformed,
 * expired or wrongly-signed token is rejected by Ktor with 401 before any handler
 * runs. Handlers therefore only ever execute for a verified principal.
 */
fun Route.taskRoutes(taskRepository: TaskRepository) {

    val log = LoggerFactory.getLogger("TaskRoutes")

    authenticate(JWT_AUTH) {
        route("/tasks") {

            /** GET /tasks → 200 + the caller's tasks (newest first). Never anyone else's. */
            get {
                val userId = call.userId()
                val tasks = taskRepository.findAllForUser(userId).map { it.toDto() }
                call.respond(HttpStatusCode.OK, tasks)
            }

            /**
             * POST /tasks → **201** + the created task.
             *
             * 400 blank title · 400 `invalid_task_id` · 409 `task_id_taken` ·
             * **200** + the stored task when the caller already has this id.
             *
             * The client chooses the id (see [TaskUpsertRequest.id]) so an
             * offline-first task keeps one identity from the moment it is written to
             * the local database. That makes a re-sent create the *normal* path
             * rather than an error — the network can drop after the write lands but
             * before the 201 does — so the caller's own id coming back answers 200
             * with the stored task instead of a conflict the client would have to
             * decode. 201 means "written now", 200 means "already there"; both are
             * success, and the status code is the only difference.
             *
             * An id that is taken but **not** the caller's is a 409 that does not say
             * whose it is. See [TaskRepository.create] for why that distinction has
             * to exist at all.
             */
            post {
                val userId = call.userId()
                val body = call.receive<TaskUpsertRequest>()
                val title = Validation.taskTitle(body.title)

                // A blank id is a client bug, not an absent id: `null` means "did not
                // supply one", `""` means "tried and got it wrong", and Validation
                // rejects the second rather than quietly minting a replacement.
                val taskId = body.id?.let { Validation.taskId(it) }
                    ?: UUID.randomUUID().toString().also {
                        log.info(
                            "POST /tasks carried no client id — generated {}. An " +
                                "offline-first client should send its own.", it
                        )
                    }

                when (val result = taskRepository.create(userId, taskId, body, title)) {
                    is TaskCreateResult.Created ->
                        call.respond(HttpStatusCode.Created, result.task.toDto())

                    is TaskCreateResult.AlreadyExisted ->
                        call.respond(HttpStatusCode.OK, result.task.toDto())
                }
            }

            /**
             * PUT /tasks/{id} → 200 + the updated task. Full replace, not a merge.
             *
             * 404 when the id is unknown **or owned by someone else** — the repository
             * filters on owner, so the two cases are indistinguishable by design.
             *
             * **The body's [TaskUpsertRequest.id] is ignored here — read the path.**
             * That field exists for `POST`, and this route shares the request type
             * with it. Honouring it would let a caller name one task in the path and
             * another in the body, which is a re-homing primitive, not a convenience.
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
