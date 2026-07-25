package com.development.forge.server.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.development.forge.server.config.ServerConfig
import com.development.forge.server.db.UserDocument
import java.util.Date

/**
 * Issues and verifies the JWTs that authenticate every `/tasks` request.
 *
 * ── Claims carried ──────────────────────────────────────────────
 *   sub    the user's id  ← the ownership key; `TaskRoutes` reads this and
 *                            every query is filtered by it
 *   email  the user's email (convenience for the client; never trusted for authz)
 *   iss    ServerConfig.jwtIssuer    — rejected if it doesn't match on verify
 *   aud    ServerConfig.jwtAudience  — rejected if it doesn't match on verify
 *   exp    now + ServerConfig.jwtValidityMillis
 *
 * Signed HMAC256 with [ServerConfig.jwtSecret]. Tokens are **stateless**: nothing
 * is stored server-side, so a token stays valid until it expires. Changing
 * `JWT_SECRET` invalidates every outstanding token at once — the blunt logout-all.
 */
class JwtService(
    private val secret: String = ServerConfig.jwtSecret,
    private val issuer: String = ServerConfig.jwtIssuer,
    private val audience: String = ServerConfig.jwtAudience,
    private val validityMillis: Long = ServerConfig.jwtValidityMillis
) {

    private val algorithm: Algorithm = Algorithm.HMAC256(secret)

    /**
     * The verifier handed to Ktor's `jwt { }` block. It enforces signature,
     * issuer and audience; expiry is checked by java-jwt automatically.
     */
    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    /** Mints a token for a freshly registered or just-authenticated user. */
    fun issue(user: UserDocument): String = JWT.create()
        .withIssuer(issuer)
        .withAudience(audience)
        .withSubject(user.id)
        .withClaim(CLAIM_EMAIL, user.email)
        .withIssuedAt(Date())
        .withExpiresAt(Date(System.currentTimeMillis() + validityMillis))
        .sign(algorithm)

    companion object {
        const val CLAIM_EMAIL = "email"
    }
}
