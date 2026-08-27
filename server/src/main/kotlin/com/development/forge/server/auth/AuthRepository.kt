package com.development.forge.server.auth

import com.development.forge.server.config.ServerConfig
import com.development.forge.server.db.AuthProvider
import com.development.forge.server.db.MongoDatabase
import com.development.forge.server.db.UserDocument
import com.development.forge.server.error.ConflictException
import com.mongodb.MongoWriteException
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

/**
 * All user reads/writes. The only component that knows how users are stored.
 */
class AuthRepository(private val mongo: MongoDatabase) {

    /** Emails are stored already-normalised by `Validation.email`, so this is an exact match. */
    suspend fun findByEmail(email: String): UserDocument? =
        mongo.users.find(Filters.eq(UserDocument::email.name, email)).firstOrNull()

    suspend fun findById(id: String): UserDocument? =
        mongo.users.find(Filters.eq("_id", id)).firstOrNull()

    /**
     * Creates a user, or throws [ConflictException] (→ 409) if the email is taken.
     *
     * The duplicate check is the **unique index** on `users.email`, not a
     * read-then-write: catching Mongo's duplicate-key error closes the race where
     * two simultaneous registrations for the same address both see "not taken".
     */
    suspend fun createUser(email: String, plaintextPassword: String): UserDocument {
        val user = UserDocument(
            id = UUID.randomUUID().toString(),
            email = email,
            passwordHash = PasswordHasher.hash(plaintextPassword),
            createdAt = System.currentTimeMillis(),
            // Written explicitly rather than left to the default: the field is
            // nullable (see UserDocument), and `null` is reserved to mean "row
            // predates Google sign-in". New rows should always say what they are.
            authProvider = AuthProvider.PASSWORD
        )
        try {
            mongo.users.insertOne(user)
        } catch (e: MongoWriteException) {
            if (e.error.category == com.mongodb.ErrorCategory.DUPLICATE_KEY) {
                throw ConflictException("email_taken", "An account with that email already exists.")
            }
            throw e
        }
        return user
    }

    /**
     * Creates a Google-authenticated user, or throws [ConflictException] (→ 409).
     *
     * Same id/`createdAt` conventions as [createUser], and the same reliance on the
     * unique index rather than a read-then-write: `/auth/google` looks the email up
     * first, so reaching a duplicate-key error here means a second sign-in for the
     * same new address landed in between. Surfacing that as a 409 keeps the race
     * closed instead of silently creating a second account.
     *
     * `passwordHash` is `""` — see [com.development.forge.server.db.UserDocument]
     * for why that is the safe value rather than a nullable column.
     */
    suspend fun createGoogleUser(email: String): UserDocument {
        val user = UserDocument(
            id = UUID.randomUUID().toString(),
            email = email,
            passwordHash = "",
            createdAt = System.currentTimeMillis(),
            authProvider = AuthProvider.GOOGLE
        )
        try {
            mongo.users.insertOne(user)
        } catch (e: MongoWriteException) {
            if (e.error.category == com.mongodb.ErrorCategory.DUPLICATE_KEY) {
                throw ConflictException("email_taken", "An account with that email already exists.")
            }
            throw e
        }
        return user
    }

    /**
     * Generates a single-use reset code, stores it with an expiry, and returns it.
     *
     * The code is 8 characters from [ResetTokens] — short because a human reads it
     * off the server console and types it into the app; email delivery is out of
     * scope. See [ResetTokens] for the alphabet and the 40-bit figure, and
     * [com.development.forge.server.security.AttemptLimiter] for the rate limiting
     * that makes a code that short safe to accept.
     *
     * **The retry loop is the price of a short code.** At 256 bits a collision was
     * unthinkable and a plain write was fine. At 40 bits two live codes could
     * coincide, and because [findByResetToken] matches on the code alone that would
     * let one account's code reset a different account's password. The unique
     * partial index on `resetToken` (see
     * [com.development.forge.server.db.MongoDatabase.ensureIndexes]) turns that into
     * a duplicate-key error instead of a silent cross-account reset — so here, a
     * duplicate key means "that code is already out there" and we simply draw
     * another.
     *
     * Exhausting [RESET_TOKEN_ATTEMPTS] needs the keyspace to be saturated, which
     * would take on the order of a trillion outstanding codes. It surfaces as a 500
     * rather than a wrong answer, which is the correct failure for a state that
     * cannot occur.
     */
    suspend fun createResetToken(user: UserDocument): String {
        repeat(RESET_TOKEN_ATTEMPTS) {
            val token = ResetTokens.generate()
            try {
                mongo.users.updateOne(
                    Filters.eq("_id", user.id),
                    Updates.combine(
                        Updates.set(UserDocument::resetToken.name, token),
                        Updates.set(
                            UserDocument::resetTokenExpiresAt.name,
                            System.currentTimeMillis() + ServerConfig.resetTokenValidityMillis
                        )
                    )
                )
                return token
            } catch (e: MongoWriteException) {
                if (e.error.category != com.mongodb.ErrorCategory.DUPLICATE_KEY) throw e
            }
        }
        throw IllegalStateException(
            "Could not find an unused reset code in $RESET_TOKEN_ATTEMPTS attempts."
        )
    }

    /**
     * Looks up the holder of a reset code. Expiry is checked by the caller.
     *
     * Expects an **already-normalised** code: `/auth/reset` runs
     * [ResetTokens.normalise] before calling, the same arrangement [findByEmail] has
     * with `Validation.email`. This is an exact match, so a lowercase or
     * hyphen-grouped code that skipped normalisation would silently find nothing and
     * be reported as `invalid_token`.
     */
    suspend fun findByResetToken(token: String): UserDocument? =
        mongo.users.find(Filters.eq(UserDocument::resetToken.name, token)).firstOrNull()

    /**
     * Sets a new password and **clears the reset token in the same update**, which
     * is what makes the token single-use.
     */
    suspend fun updatePassword(userId: String, newPlaintextPassword: String) {
        mongo.users.updateOne(
            Filters.eq("_id", userId),
            Updates.combine(
                Updates.set(UserDocument::passwordHash.name, PasswordHasher.hash(newPlaintextPassword)),
                Updates.unset(UserDocument::resetToken.name),
                Updates.unset(UserDocument::resetTokenExpiresAt.name)
            )
        )
    }

    private companion object {
        /** Draws before giving up on finding an unused reset code. See [createResetToken]. */
        const val RESET_TOKEN_ATTEMPTS = 5
    }
}
