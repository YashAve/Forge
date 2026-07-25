package com.development.forge.server

import com.development.forge.server.auth.AuthRepository
import com.development.forge.server.auth.JwtService
import com.development.forge.server.config.ServerConfig
import com.development.forge.server.db.MongoDatabase
import com.development.forge.server.di.appModule
import com.development.forge.server.plugins.configureCors
import com.development.forge.server.plugins.configureMonitoring
import com.development.forge.server.plugins.configureSecurity
import com.development.forge.server.plugins.configureSerialization
import com.development.forge.server.plugins.configureStatusPages
import com.development.forge.server.task.TaskRepository
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory

/**
 * ═══════════════════════════════════════════════════════════════
 * FORGE SERVER — entry point
 * ═══════════════════════════════════════════════════════════════
 * Run it with:  ./gradlew :server:run
 *
 * Requires MongoDB to be up. On this machine:
 *   sg docker -c "docker compose -f ~/forge-mongo/docker-compose.yml up -d"
 *
 * Every setting (port, Mongo URI, JWT secret) is overridable by environment
 * variable — see [ServerConfig].
 */
fun main() {
    embeddedServer(
        factory = Netty,
        port = ServerConfig.port,
        host = ServerConfig.host,
        module = Application::forgeModule
    ).start(wait = true)
}

fun Application.forgeModule() {
    val log = LoggerFactory.getLogger("Forge")

    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }

    val mongo by inject<MongoDatabase>()
    val jwtService by inject<JwtService>()
    val authRepository by inject<AuthRepository>()
    val taskRepository by inject<TaskRepository>()

    // Cross-cutting plugins first, then routes.
    configureSerialization()
    configureMonitoring()
    configureCors()
    configureStatusPages()
    configureSecurity(jwtService)
    configureRouting(authRepository, taskRepository, jwtService)

    // Unique index on users.email + owner index on tasks.userId. Blocking on
    // purpose: if Mongo is unreachable the server should fail loudly at startup
    // rather than serve requests that will fail one by one.
    runBlocking { mongo.ensureIndexes() }

    if (ServerConfig.isUsingDevJwtSecret) {
        log.warn(
            "JWT_SECRET is not set — using the built-in DEV secret. " +
                "Anyone with the source can mint tokens. Set JWT_SECRET outside local dev."
        )
    }
    log.info(
        "Forge server listening on {}:{} · mongo db '{}'",
        ServerConfig.host, ServerConfig.port, ServerConfig.mongoDatabase
    )

    // Release the Mongo client (and its connection pool) on shutdown.
    monitor.subscribe(io.ktor.server.application.ApplicationStopped) { mongo.close() }
}
