package com.development.forge.server.config

/**
 * All environment-driven configuration in one place.
 *
 * Every value has a **dev fallback** so `./gradlew :server:run` works with zero
 * setup on this machine, and every value can be overridden by an environment
 * variable in any other environment.
 *
 * ⚠️ The fallbacks are development-only. `JWT_SECRET` in particular MUST be set
 * to a real secret anywhere that isn't a laptop — see [jwtSecret].
 */
object ServerConfig {

    /** HTTP port. Keeps the port the old practice server used, so nothing else has to move. */
    val port: Int = env("PORT")?.toIntOrNull() ?: 9292

    val host: String = env("HOST") ?: "0.0.0.0"

    /**
     * MongoDB connection string.
     *
     * The dev fallback matches the project's existing `~/forge-mongo/docker-compose.yml`
     * (Mongo 8 in Docker, root user `yash`, `authSource=admin`). Start it with:
     * `sg docker -c "docker compose -f ~/forge-mongo/docker-compose.yml up -d"`.
     */
    val mongoUri: String = env("MONGO_URI")
        ?: "mongodb://yash:GYqwRIjipP6nlxTWTuF9@localhost:27017/?authSource=admin"

    /** Database name inside Mongo. The compose file seeds `forge`. */
    val mongoDatabase: String = env("MONGO_DB") ?: "forge"

    /**
     * HMAC256 signing secret for JWTs.
     *
     * ⚠️ DEV FALLBACK — anyone holding this string can mint tokens for any user.
     * Set `JWT_SECRET` to a long random value outside local development.
     * [isUsingDevJwtSecret] drives the loud warning logged at startup.
     */
    val jwtSecret: String = env("JWT_SECRET") ?: DEV_JWT_SECRET

    val jwtIssuer: String = env("JWT_ISSUER") ?: "forge-server"

    val jwtAudience: String = env("JWT_AUDIENCE") ?: "forge-android"

    val jwtRealm: String = env("JWT_REALM") ?: "forge"

    /** Token lifetime. Defaults to 30 days — long, because this is a personal task app. */
    val jwtValidityMillis: Long =
        env("JWT_VALIDITY_MS")?.toLongOrNull() ?: (30L * 24 * 60 * 60 * 1000)

    /** How long a `/auth/forgot` reset token stays usable. */
    val resetTokenValidityMillis: Long =
        env("RESET_TOKEN_VALIDITY_MS")?.toLongOrNull() ?: (60L * 60 * 1000)

    /** Minimum accepted password length, enforced on register and reset. */
    val minPasswordLength: Int = env("MIN_PASSWORD_LENGTH")?.toIntOrNull() ?: 8

    val isUsingDevJwtSecret: Boolean get() = jwtSecret == DEV_JWT_SECRET

    /** Blank env vars are treated as unset — an empty `JWT_SECRET=` shouldn't win. */
    private fun env(name: String): String? = System.getenv(name)?.takeIf { it.isNotBlank() }

    private const val DEV_JWT_SECRET = "forge-dev-secret-do-not-use-in-production"
}
