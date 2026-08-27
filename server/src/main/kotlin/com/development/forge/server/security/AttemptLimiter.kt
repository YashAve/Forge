package com.development.forge.server.security

import com.development.forge.server.config.ServerConfig
import com.development.forge.server.error.TooManyRequestsException
import java.util.concurrent.ConcurrentHashMap

/**
 * Counts recent **failed** `/auth/reset` attempts in a sliding window and refuses
 * once there have been too many.
 *
 * ## Why this exists
 * The reset code carries 40 bits (see [com.development.forge.server.auth.ResetTokens]).
 * Unthrottled, a caller managing 1,000 requests a second gets ~3.6 million guesses
 * inside the code's one-hour life — roughly a 1-in-300,000 shot. At
 * [ServerConfig.resetMaxAttemptsPerIp] attempts per
 * [ServerConfig.resetAttemptWindowMillis] that becomes about forty guesses an hour,
 * or 1 in 27 billion. **The rate limit is what makes a short code defensible.**
 * Shortening the token without this would have been a straight downgrade.
 *
 * ## Why failures, and why per address
 * `/auth/reset` receives `{token, newPassword}` and nothing else — **no account
 * identity**. Two consequences shape this class:
 *
 *  - A counter keyed on the *submitted token* cannot bind an attacker, who sends a
 *    different token every time. It would only ever throttle an owner retyping
 *    their own code.
 *  - A wrong guess matches no user, so there is no account row to count against
 *    either.
 *
 * The caller's address is the only thing a brute-force sweep cannot vary for free,
 * which is why the per-address window is the limit that does the work. The global
 * ceiling ([ServerConfig.resetMaxAttemptsGlobal]) is the backstop for an attacker
 * who rotates addresses — precisely the case per-address counting is blind to. It
 * does mean one attacker can suspend password resets for everyone; that is
 * accepted deliberately, because it affects only this endpoint and lapses within
 * one window.
 *
 * Successful resets are never counted. Budget is spent by failure alone, so a user
 * who gets it right first time never approaches the limit.
 *
 * ## What this is not
 * In-memory and per-process: the window dies with the server, and a second
 * instance would keep its own counts. That is honest for a single-instance dev
 * server, and it is the first thing to move to Mongo or Redis if this ever runs
 * more than once.
 *
 * The key comes from `call.request.origin.remoteAddress`, the true peer for a
 * device talking to this server directly. Behind a reverse proxy it would be the
 * proxy, and `XForwardedHeaders` is deliberately **not** installed: honouring
 * `X-Forwarded-For` without a trusted-proxy allowlist would let any caller mint a
 * fresh bucket per request by setting a header, which is strictly worse than not
 * limiting at all.
 */
class AttemptLimiter(
    private val perKeyLimit: Int = ServerConfig.resetMaxAttemptsPerIp,
    private val globalLimit: Int = ServerConfig.resetMaxAttemptsGlobal,
    private val windowMillis: Long = ServerConfig.resetAttemptWindowMillis,
    private val clock: () -> Long = System::currentTimeMillis
) {

    /** Failure timestamps per key. Each deque is guarded by itself. */
    private val perKey = ConcurrentHashMap<String, ArrayDeque<Long>>()

    /** Failure timestamps across every key. */
    private val global = ArrayDeque<Long>()

    @Volatile
    private var lastSweepAt = 0L

    /**
     * Throws [TooManyRequestsException] when [key] — or the server as a whole — has
     * spent its budget for the current window.
     *
     * Call this **before** the token lookup, so a caller who is already over the
     * limit cannot keep probing the keyspace and learning from the responses.
     */
    fun check(key: String) {
        val cutoff = clock() - windowMillis

        val forKey = perKey[key]
        if (forKey != null && synchronized(forKey) { forKey.prune(cutoff); forKey.size } >= perKeyLimit) {
            throw tooManyAttempts()
        }
        if (synchronized(global) { global.prune(cutoff); global.size } >= globalLimit) {
            throw tooManyAttempts()
        }
    }

    /**
     * Records one failed attempt against [key] and against the global window.
     *
     * Only failures are recorded — see the class doc.
     */
    fun recordFailure(key: String) {
        val now = clock()
        val cutoff = now - windowMillis

        val forKey = perKey.computeIfAbsent(key) { ArrayDeque() }
        synchronized(forKey) {
            forKey.prune(cutoff)
            forKey.addLast(now)
        }
        synchronized(global) {
            global.prune(cutoff)
            global.addLast(now)
        }

        sweepIfDue(now)
    }

    /** Drops timestamps that have fallen out of the window. Caller holds the lock. */
    private fun ArrayDeque<Long>.prune(cutoff: Long) {
        while (isNotEmpty() && this[0] < cutoff) removeFirst()
    }

    /**
     * Drops keys with no live attempts, at most once per window.
     *
     * Without this the map grows one entry per distinct address forever, which is a
     * slow memory leak that an attacker could accelerate. The sweep races benignly
     * with [recordFailure]: a deque could be emptied and evicted while another
     * thread holds a reference and appends to it, losing that one attempt. That
     * costs a single uncounted failure in a window that was already empty, so it is
     * not worth a heavier lock.
     */
    private fun sweepIfDue(now: Long) {
        if (now - lastSweepAt < windowMillis) return
        lastSweepAt = now
        val cutoff = now - windowMillis
        perKey.entries.removeIf { (_, bucket) ->
            synchronized(bucket) {
                bucket.prune(cutoff)
                bucket.isEmpty()
            }
        }
    }

    /**
     * Deliberately vague: the caller learns they are throttled, not how close to the
     * limit they were or which limit they hit. Distinguishing the per-address ceiling
     * from the global one would tell an attacker whether anyone else is being blocked.
     */
    private fun tooManyAttempts() = TooManyRequestsException(
        "too_many_attempts",
        "Too many reset attempts. Wait a few minutes and try again."
    )
}
