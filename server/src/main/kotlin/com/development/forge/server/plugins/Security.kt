package com.development.forge.server.plugins

import com.development.forge.server.auth.JwtService
import com.development.forge.server.config.ServerConfig
import com.development.forge.shared.api.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond

/** Name of the JWT auth provider, referenced by `authenticate(JWT_AUTH)` in `TaskRoutes`. */
const val JWT_AUTH = "auth-jwt"

/**
 * Installs JWT bearer authentication.
 *
 * Validation chain for a request to a protected route:
 *  1. Ktor reads the `Authorization: Bearer <token>` header.
 *  2. [JwtService.verifier] checks the HMAC256 signature, the issuer, the audience
 *     and the expiry. Any failure → `challenge` → 401.
 *  3. [validate] additionally requires a non-blank `sub` claim, so a structurally
 *     valid token with no subject can't reach a handler that needs an owner id.
 *  4. The resulting [JWTPrincipal] is what `TaskRoutes` reads the user id from.
 */
fun Application.configureSecurity(jwtService: JwtService) {
    install(Authentication) {
        jwt(JWT_AUTH) {
            realm = ServerConfig.jwtRealm
            verifier(jwtService.verifier)

            validate { credential ->
                val subject = credential.payload.subject
                if (subject.isNullOrBlank()) null else JWTPrincipal(credential.payload)
            }

            // Reached when the token is absent, malformed, expired or fails
            // verification. Returns the same JSON error shape as every other failure
            // so the client never has to parse a second error format.
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(
                        error = "unauthorized",
                        message = "A valid bearer token is required."
                    )
                )
            }
        }
    }
}
