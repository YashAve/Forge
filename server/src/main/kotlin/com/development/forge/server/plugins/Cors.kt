package com.development.forge.server.plugins

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

/**
 * CORS for local development.
 *
 * ⚠️ `anyHost()` is a development convenience — it lets a browser page or web
 * tooling on any origin call this server. The Android client is a native app and
 * isn't subject to CORS at all, so this exists purely for browser-based poking
 * (mongo-express-style tools, a local web console, `curl` from a page).
 * Restrict to known origins before this is exposed anywhere real.
 */
fun Application.configureCors() {
    install(CORS) {
        anyHost()

        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)

        // Authorization must be allowed explicitly — it's the JWT bearer header.
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)

        allowCredentials = true
    }
}
