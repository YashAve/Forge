package com.development.forge.server.auth

import at.favre.lib.crypto.bcrypt.BCrypt

/**
 * BCrypt password hashing — the only place in the server that touches a plaintext
 * password, and it never returns one.
 *
 * BCrypt salts each hash internally, so the salt travels inside the stored string
 * and there is nothing extra to persist.
 */
object PasswordHasher {

    /**
     * Cost factor 12 (~250ms/hash on a modern CPU). Deliberately slow: that cost
     * is paid once per login but multiplied across every guess in an offline
     * attack on a stolen database.
     */
    private const val COST = 12

    /** Returns the BCrypt hash (salt included) to store in `UserDocument.passwordHash`. */
    fun hash(plaintext: String): String =
        BCrypt.withDefaults().hashToString(COST, plaintext.toCharArray())

    /**
     * Constant-time-ish verification via BCrypt's own comparison.
     *
     * Returns `false` rather than throwing on a malformed/legacy stored hash, so a
     * corrupt row is a failed login, not a 500.
     */
    fun verify(plaintext: String, storedHash: String): Boolean = runCatching {
        BCrypt.verifyer().verify(plaintext.toCharArray(), storedHash).verified
    }.getOrDefault(false)
}
