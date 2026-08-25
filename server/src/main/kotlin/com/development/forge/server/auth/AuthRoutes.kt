package com.development.forge.server.auth

import com.development.forge.server.db.AuthProvider
import com.development.forge.server.db.provider
import com.development.forge.server.db.toDto
import com.development.forge.server.error.BadRequestException
import com.development.forge.server.error.ConflictException
import com.development.forge.server.error.UnauthorizedException
import com.development.forge.server.validation.Validation
import com.development.forge.shared.api.AuthResponse
import com.development.forge.shared.api.ForgotPasswordRequest
import com.development.forge.shared.api.GoogleAuthRequest
import com.development.forge.shared.api.LoginRequest
import com.development.forge.shared.api.MessageResponse
import com.development.forge.shared.api.RegisterRequest
import com.development.forge.shared.api.ResetPasswordRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.slf4j.LoggerFactory

/**
 * `/auth/…` — all unauthenticated. These are the only routes that don't require a
 * JWT, because they're how you get one.
 */
fun Route.authRoutes(
    authRepository: AuthRepository,
    jwtService: JwtService,
    googleTokenVerifier: GoogleTokenVerifier
) {
    val log = LoggerFactory.getLogger("AuthRoutes")

    route("/auth") {

        /**
         * POST /auth/register → 201 + [AuthResponse]
         *
         * 400 invalid email / weak password · 409 email already registered.
         * Returns a token straight away so the client doesn't have to log in after
         * registering.
         */
        post("/register") {
            val body = call.receive<RegisterRequest>()
            val email = Validation.email(body.email)
            val password = Validation.password(body.password)

            // Throws ConflictException (409) if the unique index rejects the insert.
            val user = authRepository.createUser(email, password)
            log.info("Registered user {}", user.id)

            call.respond(
                HttpStatusCode.Created,
                AuthResponse(token = jwtService.issue(user), user = user.toDto())
            )
        }

        /**
         * POST /auth/login → 200 + [AuthResponse]
         *
         * 401 on both "no such user" and "wrong password", with the same body: a
         * distinct message would let someone probe which emails have accounts.
         */
        post("/login") {
            val body = call.receive<LoginRequest>()
            val email = Validation.email(body.email)

            val user = authRepository.findByEmail(email)
            val credentialsOk = user != null &&
                PasswordHasher.verify(body.password, user.passwordHash)

            if (user == null || !credentialsOk) {
                throw UnauthorizedException("invalid_credentials", "Incorrect email or password.")
            }

            call.respond(
                HttpStatusCode.OK,
                AuthResponse(token = jwtService.issue(user), user = user.toDto())
            )
        }

        /**
         * POST /auth/forgot → 200 + [MessageResponse]
         *
         * ⚠️ STUB, by scope: sending email is out of scope, so the reset token is
         * **logged to the server console** and never delivered anywhere. Grab it
         * from the server log to exercise `/auth/reset`.
         *
         * Always 200, even for an unknown address, so the endpoint can't be used to
         * enumerate accounts. (The response is identical either way; only the log
         * differs.)
         */
        post("/forgot") {
            val body = call.receive<ForgotPasswordRequest>()
            val email = Validation.email(body.email)

            val user = authRepository.findByEmail(email)
            if (user == null) {
                log.info("Password reset requested for unknown email (no-op)")
            } else if (user.provider == AuthProvider.GOOGLE) {
                // A Google account has no password to reset. Issuing a token anyway
                // would let /auth/reset write a real passwordHash onto it, silently
                // turning it into a password account the owner never created — a way
                // in that bypasses Google entirely. Same 200 as every other branch,
                // so the caller still cannot tell these cases apart.
                log.info("Password reset requested for a Google account (no-op)")
            } else {
                val token = authRepository.createResetToken(user)
                // TODO(email): deliver this out-of-band instead of logging it. Email
                //  sending is not in scope — the console is the delivery channel today.
                log.warn(
                    "\n" +
                        "════════════════════════════════════════════════════════\n" +
                        " PASSWORD RESET TOKEN (dev only — email is not wired up)\n" +
                        " email : {}\n" +
                        " token : {}\n" +
                        " expires in {} minutes\n" +
                        "════════════════════════════════════════════════════════",
                    user.email,
                    token,
                    com.development.forge.server.config.ServerConfig.resetTokenValidityMillis / 60_000
                )
            }

            call.respond(
                HttpStatusCode.OK,
                MessageResponse("If that account exists, a reset token has been generated.")
            )
        }

        /**
         * POST /auth/reset → 200 + [MessageResponse]
         *
         * 400 on a blank/unknown/expired token or a weak new password. The token is
         * single-use: `updatePassword` clears it in the same write.
         */
        post("/reset") {
            val body = call.receive<ResetPasswordRequest>()
            val token = Validation.requireNotBlank(
                body.token, "token_required", "Reset token is required."
            )
            val newPassword = Validation.password(body.newPassword)

            val user = authRepository.findByResetToken(token)
                ?: throw BadRequestException("invalid_token", "That reset token is not valid.")

            val expiresAt = user.resetTokenExpiresAt
            if (expiresAt == null || expiresAt < System.currentTimeMillis()) {
                throw BadRequestException("expired_token", "That reset token has expired.")
            }

            authRepository.updatePassword(user.id, newPassword)
            log.info("Password reset for user {}", user.id)

            call.respond(HttpStatusCode.OK, MessageResponse("Password updated."))
        }

        /**
         * POST /auth/google → 200 + [AuthResponse]
         *
         * 401 invalid_google_token · 409 invalid_auth_provider.
         *
         * Find-or-create: an unknown email becomes a new Google account, a known
         * Google account signs straight in. Responds with the same [AuthResponse]
         * shape as `/auth/login`, carrying **our** JWT — the Google token is proof
         * of identity for this one request and is never stored or re-used.
         *
         * The 409 does not leak account existence the way it would on `/auth/login`:
         * verification happens first, so the only caller who can reach that branch
         * already holds a Google-signed token for that exact address, and is being
         * told about their own account.
         */
        post("/google") {
            val body = call.receive<GoogleAuthRequest>()
            val idToken = Validation.requireNotBlank(
                body.idToken, "id_token_required", "Google ID token is required."
            )

            // Normalised the same way register/login do it, so the lookup below
            // matches an account created through either path.
            val email = Validation.email(googleTokenVerifier.verifiedEmail(idToken))

            val existing = authRepository.findByEmail(email)
            val user = when {
                existing == null ->
                    authRepository.createGoogleUser(email).also {
                        log.info("Registered Google user {}", it.id)
                    }

                existing.provider == AuthProvider.GOOGLE -> existing

                // The address is spoken for by a password account. Signing in here
                // would hand control of it to whoever holds the matching Google
                // account, which is not the same person by default.
                else -> throw ConflictException(
                    "invalid_auth_provider",
                    "That email is already registered with a password. Sign in with your password instead."
                )
            }

            call.respond(
                HttpStatusCode.OK,
                AuthResponse(token = jwtService.issue(user), user = user.toDto())
            )
        }

        // TODO(FCM): Firebase is out of scope. When push lands, the device-token
        //  endpoint goes here:
        //      authenticate(JWT_AUTH) { post("/fcm-token") { … } }
        //  It must be JWT-authenticated (the token belongs to the *signed-in* user),
        //  read the user id from the JWT subject exactly like TaskRoutes does, and
        //  append to `UserDocument.fcmTokens` (see the matching TODO in
        //  `db/Documents.kt`). Do NOT add Firebase dependencies until then.
    }
}
