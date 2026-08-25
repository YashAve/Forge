package com.development.forge.shared.api

import kotlinx.serialization.Serializable

/**
 * ═══════════════════════════════════════════════════════════════
 * SHARED API CONTRACT — auth payloads
 * ═══════════════════════════════════════════════════════════════
 * See the header in [TaskDtos.kt] for the rules governing this package.
 * Note what is absent: no password hash, no reset token, ever leaves the server
 * through these types.
 */

/** `POST /auth/register` — creates the account and returns a token immediately. */
@Serializable
data class RegisterRequest(
    val email: String,
    val password: String
)

/** `POST /auth/login`. */
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * `POST /auth/forgot` — starts a password reset.
 *
 * Always answers 200 even when the email is unknown, so the endpoint can't be
 * used to enumerate which addresses have accounts.
 */
@Serializable
data class ForgotPasswordRequest(
    val email: String
)

/** `POST /auth/reset` — consumes the reset token and sets a new password. */
@Serializable
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)

/**
 * `POST /auth/google` — signs in (or registers) with a Google ID token.
 *
 * The token is minted by Google on the device; the server verifies it against
 * Google's certs before trusting the email inside it. No password is involved in
 * either direction.
 */
@Serializable
data class GoogleAuthRequest(
    val idToken: String
)

/** The authenticated user. Deliberately excludes `passwordHash`. */
@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val createdAt: Long
)

/** Returned by register and login: the JWT plus who it belongs to. */
@Serializable
data class AuthResponse(
    val token: String,
    val user: UserDto
)

/** Generic “it worked” body for endpoints with nothing else to say. */
@Serializable
data class MessageResponse(
    val message: String
)
