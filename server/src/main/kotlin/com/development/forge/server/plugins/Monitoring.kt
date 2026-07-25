package com.development.forge.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import org.slf4j.event.Level

/**
 * Request logging.
 *
 * The format deliberately logs method, path and status only — never headers or
 * bodies, which would put bearer tokens and plaintext passwords in the log file.
 */
fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        format { call ->
            "${call.request.httpMethod.value} ${call.request.path()} → ${call.response.status()?.value ?: "-"}"
        }
    }
}
