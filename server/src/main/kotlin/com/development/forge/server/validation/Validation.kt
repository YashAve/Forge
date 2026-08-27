package com.development.forge.server.validation

import com.development.forge.server.config.ServerConfig
import com.development.forge.server.error.BadRequestException
import java.util.UUID

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

    /**
     * A client-supplied task id: a **canonical** UUID string, returned unchanged.
     *
     * `UUID.fromString` on its own is not a sufficient check — it is lenient, and
     * silently repairs input that is nowhere near a UUID. Verified on JBR 21.0.10
     * and OpenJDK 21.0.12:
     *
     * ```
     * "1-1-1-1-1"       -> 00000001-0001-0001-0001-000000000001   (accepted!)
     * "A1B2…-…-4C5D"    -> a1b2…-…-4c5d                           (case-folded)
     * ```
     *
     * Both would defeat the point of validating at all. The first hands a client a
     * short, guessable primary key it could deliberately squat on; the second is
     * worse than it looks, because `Filters.eq("_id", …)` is an exact match, so
     * `A1B2…` and `a1b2…` would be **two documents for one UUID** — the aliasing
     * that client-chosen ids exist to eliminate.
     *
     * Comparing `UUID.fromString(id).toString()` back against the input closes both
     * in one expression: only the canonical lowercase form survives. Nothing is
     * lost by being strict, since `UUID.toString()` emits exactly that.
     */
    fun taskId(raw: String): String {
        val id = raw.trim()
        val parsed = runCatching { UUID.fromString(id) }.getOrNull()
        if (parsed == null || parsed.toString() != id) {
            throw BadRequestException("invalid_task_id", "'$raw' is not a valid task id.")
        }
        return id
    }

    /** Non-blank free text used for tokens and path ids. */
    fun requireNotBlank(raw: String, error: String, message: String): String {
        val value = raw.trim()
        if (value.isEmpty()) throw BadRequestException(error, message)
        return value
    }
}
