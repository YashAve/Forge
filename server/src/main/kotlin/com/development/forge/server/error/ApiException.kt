package com.development.forge.server.error

import io.ktor.http.HttpStatusCode

/**
 * The one exception type routes throw to produce an error response.
 *
 * Routes never call `call.respond(HttpStatusCode.BadRequest)` by hand — they
 * throw one of the subclasses below and the StatusPages plugin
 * (`plugins/StatusPages.kt`) turns it into a clean JSON [com.development.forge.shared.api.ErrorResponse].
 * That keeps status codes and error codes consistent across every endpoint.
 *
 * @param status HTTP status to return.
 * @param error  stable machine-readable code the client can switch on.
 */
sealed class ApiException(
    val status: HttpStatusCode,
    val error: String,
    override val message: String
) : RuntimeException(message)

/** 400 — malformed or semantically invalid input (bad email, short password, blank title). */
class BadRequestException(
    error: String = "bad_request",
    message: String
) : ApiException(HttpStatusCode.BadRequest, error, message)

/** 401 — missing/invalid credentials or token. */
class UnauthorizedException(
    error: String = "unauthorized",
    message: String
) : ApiException(HttpStatusCode.Unauthorized, error, message)

/**
 * 404 — the resource doesn't exist **or isn't yours**.
 *
 * Deliberately conflates "no such task" with "someone else's task": a 403 would
 * confirm that an id exists in another account, which leaks information.
 */
class NotFoundException(
    error: String = "not_found",
    message: String
) : ApiException(HttpStatusCode.NotFound, error, message)

/** 409 — conflicts with existing state; here, a duplicate email on register. */
class ConflictException(
    error: String = "conflict",
    message: String
) : ApiException(HttpStatusCode.Conflict, error, message)
