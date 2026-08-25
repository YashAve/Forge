package com.development.forge.server.auth

import com.development.forge.server.config.ServerConfig
import com.development.forge.server.error.UnauthorizedException
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Verifies Google ID tokens for `POST /auth/google`.
 *
 * The token arrives from the Android client, but it is **Google** that vouches for
 * it: the library checks the RS256 signature against Google's published certs, the
 * issuer, and the expiry. Nothing the client sends about who they are is trusted —
 * only what survives that verification.
 *
 * The audience is pinned to [ServerConfig.googleWebClientId]. That check is what
 * stops a token minted for some *other* application being replayed here; without
 * it, any valid Google token from anywhere would sign someone in.
 *
 * One client is built per process: [GoogleIdTokenVerifier] caches Google's certs
 * internally and refreshes them on expiry, so rebuilding it per request would
 * re-fetch them every time.
 */
class GoogleTokenVerifier(
    webClientId: String = ServerConfig.googleWebClientId
) {

    private val verifier: GoogleIdTokenVerifier =
        GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(listOf(webClientId))
            .build()

    /**
     * Returns the verified email address, or throws [UnauthorizedException] —
     * never returns an unverified one.
     *
     * `verify()` does blocking network I/O the first time it runs and whenever the
     * cert cache expires, so it is confined to [Dispatchers.IO] rather than left to
     * block a Netty event-loop thread.
     *
     * Every failure mode collapses to the same 401 `invalid_google_token`: a bad
     * signature, a wrong audience, an expired token and a malformed string are all
     * "we could not establish who you are", and telling them apart would only help
     * someone probing the endpoint.
     */
    suspend fun verifiedEmail(idToken: String): String {
        val payload = withContext(Dispatchers.IO) {
            // verify() returns null for a token that fails a check, and throws for a
            // malformed one or a cert fetch failure. Both mean the same thing here.
            runCatching { verifier.verify(idToken) }.getOrNull()
        }?.payload ?: throw invalidToken()

        // Google sets this false for an unconfirmed address on some account types.
        // Treating it as identity would let someone claim an email they don't own.
        if (payload.emailVerified != true) throw invalidToken()

        return payload.email?.takeIf { it.isNotBlank() } ?: throw invalidToken()
    }

    private fun invalidToken() = UnauthorizedException(
        "invalid_google_token",
        "That Google sign-in could not be verified."
    )
}
