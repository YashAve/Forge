package com.development.forge.server

import com.development.forge.server.auth.AuthRepository
import com.development.forge.server.auth.GoogleTokenVerifier
import com.development.forge.server.auth.JwtService
import com.development.forge.server.auth.authRoutes
import com.development.forge.server.security.AttemptLimiter
import com.development.forge.server.task.TaskRepository
import com.development.forge.server.task.taskRoutes
import com.development.forge.shared.api.MessageResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

/**
 * The complete route table. Deliberately small — four auth routes, four task
 * routes, and a health check.
 *
 * There is **no** `/stats` endpoint: statistics are derived from the task list on
 * the client, so the server doesn't duplicate that logic.
 */
fun Application.configureRouting(
    authRepository: AuthRepository,
    taskRepository: TaskRepository,
    jwtService: JwtService,
    googleTokenVerifier: GoogleTokenVerifier,
    resetAttemptLimiter: AttemptLimiter
) {
    routing {
        /** Unauthenticated liveness probe — handy for checking the server is up. */
        get("/health") {
            call.respond(HttpStatusCode.OK, MessageResponse("Forge server is running."))
        }

        // /auth/* — no token required
        authRoutes(authRepository, jwtService, googleTokenVerifier, resetAttemptLimiter)
        taskRoutes(taskRepository)               // /tasks/* — JWT required, user-scoped
    }
}
