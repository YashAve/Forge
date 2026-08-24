package com.development.forge.data.remote

import com.development.forge.shared.api.LoginRequest
import com.development.forge.shared.api.RegisterRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthApi @Inject constructor(
    private val client: HttpClient
) {

    suspend fun login(request: LoginRequest): HttpResponse =
        withContext(Dispatchers.IO) {
            client.request {
                method = HttpMethod.Post
                url {
                    appendPathSegments("auth", "login")
                }
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    suspend fun register(request: RegisterRequest): HttpResponse =
        withContext(Dispatchers.IO) {
            client.request {
                method = HttpMethod.Post
                url {
                    appendPathSegments("auth", "register")
                }
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
}