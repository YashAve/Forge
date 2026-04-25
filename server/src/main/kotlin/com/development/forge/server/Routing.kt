package com.development.forge.server

import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        get("/{language}") {
            val language = call.parameters["language"]
            call.respondText("Forge server in $language is running!", ContentType.Text.Plain)
        }
        get("/html/{language}") {
            val language = call.parameters["language"]
            val contentType = ContentType.Text.Html
            val text = "<h1>Hello from $language</h1>"
            call.respondText(text, contentType)
        }
    }
}
