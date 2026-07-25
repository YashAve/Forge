package com.development.forge.server.plugins

import com.development.forge.server.error.ApiException
import com.development.forge.shared.api.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException as KtorBadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.SerializationException
import org.slf4j.LoggerFactory

/**
 * Turns every failure into a clean JSON [ErrorResponse] — no stack traces, no HTML
 * error pages, one shape for the client to parse.
 *
 * Handlers are ordered most-specific first; Ktor picks the closest match.
 */
fun Application.configureStatusPages() {
    val log = LoggerFactory.getLogger("StatusPages")

    install(StatusPages) {

        // Our own typed errors already carry the right status + error code.
        exception<ApiException> { call, cause ->
            call.respond(cause.status, ErrorResponse(cause.error, cause.message))
        }

        // Malformed JSON, or a body missing a required field, surfaces as one of
        // these. Both are the caller's fault → 400, not 500.
        exception<SerializationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("malformed_body", cause.message ?: "Request body could not be parsed.")
            )
        }
        exception<KtorBadRequestException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("bad_request", cause.message ?: "Malformed request.")
            )
        }

        // Anything unanticipated: log the full detail server-side, return a generic
        // message. The client never receives an exception message or stack trace,
        // which could leak internals (e.g. a Mongo connection string).
        exception<Throwable> { call, cause ->
            log.error("Unhandled exception on ${call.request.local.uri}", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("internal_error", "Something went wrong.")
            )
        }

        // Unmatched route → JSON 404 rather than Ktor's default text body.
        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(
                status,
                ErrorResponse("not_found", "No such endpoint: ${call.request.local.uri}")
            )
        }
    }
}
