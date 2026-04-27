package com.development.forge.server

import com.development.forge.server.task.TaskRepository
import com.development.forge.server.task.tasksAsTable
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.exception
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    install(StatusPages) {
        exception<IllegalStateException> { call, cause ->
            call.respondText("Application is in illegal state: ${cause.message}")
        }
    }
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
        staticResources("/content", "content")

        get("/exception") {
            throw IllegalStateException("Illegal exception")
        }

        get("/tasks") {
            /*call.respondText(contentType = ContentType.Text.Html, text = """
                <h3>TODO:</h3>
                <ol>
                    <li>A table of all the tasks</li>
                    <li>A form to submit new tasks</li>
                </ol>    
            """.trimIndent())*/
            call.respondText(
                contentType = ContentType.Text.Html,
                text = TaskRepository.tasks.tasksAsTable()
            )
        }
    }
}
