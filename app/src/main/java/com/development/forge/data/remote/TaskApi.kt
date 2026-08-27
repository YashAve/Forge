package com.development.forge.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.request
import io.ktor.http.HttpMethod
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskApi @Inject constructor(
    private val httpClient: HttpClient
) {

    suspend fun getTasks(token: String) = withContext(Dispatchers.IO) {
        httpClient.request {
            method = HttpMethod.Get
            url {
                appendPathSegments("tasks")
            }
            bearerAuth(token)
        }
    }
}