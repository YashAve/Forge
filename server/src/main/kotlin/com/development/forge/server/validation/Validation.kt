package com.development.forge.server.validation

import com.development.forge.server.config.ServerConfig
import com.development.forge.server.error.BadRequestException

/**
 * Request validation. Each function either returns the cleaned-up value or throws
 * a [BadRequestException] that StatusPages renders as a 400 with a specific error
 * code — so the client can tell "bad email" from "short password".
 */
object Validation {

    /**
     * Deliberately permissive: one `@`, a non-empty local part, and a dotted
     * domain. Over-strict regexes reject valid addresses; the real proof an
     * address works is a delivered email, which is out of scope here.
     */
    private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)+$")

    /** Normalises email to lowercase/trimmed so `A@b.com` and `a@b.com` are one account. */
    fun email(raw: String): String {
        val email = raw.trim().lowercase()
        if (email.isEmpty()) {
            throw BadRequestException("email_required", "Email is required.")
        }
        if (!EMAIL_REGEX.matches(email)) {
            throw BadRequestException("invalid_email", "'$raw' is not a valid email address.")
        }
        return email
    }

    /** Not trimmed — leading/trailing spaces are legitimate password characters. */
    fun password(raw: String): String {
        if (raw.isEmpty()) {
            throw BadRequestException("password_required", "Password is required.")
        }
        if (raw.length < ServerConfig.minPasswordLength) {
            throw BadRequestException(
                "weak_password",
                "Password must be at least ${ServerConfig.minPasswordLength} characters."
            )
        }
        return raw
    }

    /** Task titles must carry content; whitespace-only is blank. */
    fun taskTitle(raw: String): String {
        val title = raw.trim()
        if (title.isEmpty()) {
            throw BadRequestException("title_required", "Task title cannot be blank.")
        }
        return title
    }

    /** Non-blank free text used for tokens and path ids. */
    fun requireNotBlank(raw: String, error: String, message: String): String {
        val value = raw.trim()
        if (value.isEmpty()) throw BadRequestException(error, message)
        return value
    }
}
