package com.development.forge.server.auth

import java.security.SecureRandom

/**
 * The `/auth/forgot` reset code: 8 characters, typed by a human.
 *
 * Email delivery is out of scope, so the code is logged to the server console and
 * the account owner types it into the Android app by hand. That is the whole
 * reason it is short — and the reason every choice here is about transcription
 * rather than storage.
 *
 * ## Alphabet
 * [ALPHABET] is A–Z without `I` and `O`, plus the digits 2–9. The excluded
 * characters are the ones that swap for each other when read off a screen:
 * `0`/`O` and `1`/`I`/`l`. `L` stays — with `1` and `I` gone there is nothing left
 * for it to be confused with, and keeping it is what makes the alphabet exactly
 * **32** characters rather than 31.
 *
 * That power of two is not cosmetic. With 32 symbols every index is exactly five
 * bits, so `byte and 31` selects one uniformly: 256 byte values divide into 32
 * buckets of 8. A 31-character alphabet would need `% 31`, and 256 is *not* a
 * multiple of 31 (256 = 8×31 + 8), so the first eight symbols would come up about
 * 3% more often than the rest. Biased token generation is invisible until someone
 * measures it, which is usually too late.
 *
 * ## Entropy
 * 5 bits per character × 8 characters = **40 bits** — a keyspace of 32⁸ =
 * 1,099,511,627,776. That is far short of the 256-bit token this replaced, which
 * is why `/auth/reset` is now rate limited (see
 * [com.development.forge.server.security.AttemptLimiter]). Forty bits is
 * comfortable *only* because guessing is throttled and the code dies in an hour;
 * the length alone would not carry it.
 *
 * Residual look-alikes at this size are `5`/`S`, `2`/`Z`, `8`/`B` and `6`/`G`.
 * Crockford Base32 keeps all four too — they separate cleanly in the monospace
 * faces this code is shown in at both ends (server console, and the mono-styled
 * token field on Android).
 */
object ResetTokens {

    /** Characters per code. Eight is the most a person will retype without resenting it. */
    const val LENGTH = 8

    /** 32 symbols: A–Z minus `I`/`O`, then 2–9. See the class doc for why 32 and not 31. */
    const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    /**
     * One instance, seeded once. [SecureRandom] is thread-safe, and re-creating it
     * per call would re-seed from the OS every time for no benefit.
     */
    private val random = SecureRandom()

    /**
     * A new code, already in stored form.
     *
     * [SecureRandom] rather than [kotlin.random.Random] or anything derived from
     * the clock: this value is the *sole* proof of identity for a password change,
     * so it has to be unpredictable to someone who has seen previous ones. A
     * `Random` seeded from the current time makes the whole keyspace searchable
     * from an approximate timestamp.
     */
    fun generate(): String {
        val bytes = ByteArray(LENGTH)
        random.nextBytes(bytes)
        // `and 31` keeps the low five bits. Bytes are signed, but the mask makes
        // that irrelevant: -1 and 31 == 31, and every value lands in 0..31.
        return buildString(LENGTH) {
            for (byte in bytes) append(ALPHABET[byte.toInt() and 31])
        }
    }

    /**
     * The stored form of a code: uppercase, with whitespace and hyphens removed.
     *
     * Applied on **write** as well as read, so "stored codes are normalised" is
     * something this guarantees rather than something [generate] happens to do.
     * `/auth/reset` normalises before the lookup, which is what lets a lowercase
     * code typed on a phone keyboard match.
     *
     * Whitespace and hyphens go because people group codes when they type them
     * (`ABCD-2345`) and pasted values carry trailing spaces. Nothing else is
     * stripped: a character outside [ALPHABET] survives normalisation and simply
     * fails to match, which is the correct outcome — silently deleting it would
     * turn one typo into a different valid-looking code.
     */
    fun normalise(raw: String): String =
        raw.filterNot { it.isWhitespace() || it == '-' }.uppercase()
}
