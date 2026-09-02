package com.development.forge.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.HttpMethod
import io.ktor.http.appendPathSegments
import com.development.forge.shared.api.TaskUpsertRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskApi @Inject constructor(
    private val httpClient: HttpClient
) {

    suspend fun getTasks(token: String) =
        httpClient.request {
            method = HttpMethod.Get
            url {
                appendPathSegments("tasks")
            }
            bearerAuth(token)
        }

    /**
     * `POST /tasks` — the id travels in the **body**, not the path.
     *
     * The server answers 201 when it creates the task and 200 when that id is
     * already present for this user, which is an idempotent re-send rather than a
     * failure; both are success. 409 means another account owns that id.
     */
    suspend fun createTask(token: String, request: TaskUpsertRequest) =
        httpClient.request {
            method = HttpMethod.Post
            url {
                appendPathSegments("tasks")
            }
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }

    /** `PUT /tasks/{id}` — a **full replace**: any field omitted from the body is erased. */
    suspend fun updateTask(token: String, taskId: String, request: TaskUpsertRequest) =
        httpClient.request {
            method = HttpMethod.Put
            url {
                appendPathSegments("tasks", taskId)
            }
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }

    /** `DELETE /tasks/{id}`. A 404 means the server has no such task — the desired end state. */
    suspend fun deleteTask(token: String, taskId: String) =
        httpClient.request {
            method = HttpMethod.Delete
            url {
                appendPathSegments("tasks", taskId)
            }
            bearerAuth(token)
        }
}