package com.development.forge.server.plugins

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json

/**
 * JSON in, JSON out, via kotlinx.serialization.
 */
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            Json {
                // Tolerate fields the server doesn't know: an older server must not
                // 400 just because a newer client sent an extra key.
                ignoreUnknownKeys = true
                // Emit defaults so the Android client always sees every field
                // populated rather than having to rely on its own defaults.
                encodeDefaults = true
                // Keep nulls explicit — `"project": null` is clearer to a client than
                // an absent key, and matches the nullable DTO fields.
                explicitNulls = true
                prettyPrint = false
            }
        )
    }
}
