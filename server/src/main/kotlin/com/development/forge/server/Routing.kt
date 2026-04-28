package com.development.forge.server

import com.development.forge.server.task.Priority
import com.development.forge.server.task.Task
import com.development.forge.server.task.TaskRepository
import com.development.forge.server.task.tasksAsTable
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.exception
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
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
        staticResources("/task-ui", "task-ui")

        get("/exception") {
            throw IllegalStateException("Illegal exception")
        }

        route("/tasks") {
            get {
                /*call.respondText(contentType = ContentType.Text.Html, text = """
                <h3>TODO:</h3>
                <ol>
                    <li>A table of all the tasks</li>
                    <li>A form to submit new tasks</li>
                </ol>    
            """.trimIndent())*/
                call.respondText(
                    contentType = ContentType.Text.Html,
                    text = TaskRepository.allTasks().tasksAsTable()
                )
            }

            get("/byPriority/{priority?}") {
                val priorityAsText = call.parameters["priority"]
                if (priorityAsText == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    val priorityAsEnum = Priority.valueOf(priorityAsText)
                    val tasks = TaskRepository.taskByPriority(priorityAsEnum)
                    if (tasks.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respondText(
                        contentType = ContentType.Text.Html,
                        text = tasks.tasksAsTable()
                    )
                } catch (_: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            post {
                val contentForm = call.receiveParameters()
                val input = with(contentForm) {
                    Triple(
                        get("name") ?: "",
                        get("description") ?: "",
                        get("priority") ?: ""
                    )
                }
                if (input.toList().any { it.isEmpty() }) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }
                try {
                    val priorityAsEnum = Priority.valueOf(input.third)
                    TaskRepository.addTask(
                        Task(
                            input.first, input.second, priorityAsEnum
                        )
                    )
                    call.respond(HttpStatusCode.NoContent)
                } catch (_: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                } catch (_: IllegalStateException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
    }
}
