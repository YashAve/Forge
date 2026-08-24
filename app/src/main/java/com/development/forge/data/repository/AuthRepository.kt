package com.development.forge.data.repository

import com.development.forge.data.remote.AuthApi
import com.development.forge.data.remote.AuthResult
import com.development.forge.feature.auth.contract.AuthError
import com.development.forge.shared.api.AuthResponse
import com.development.forge.shared.api.ErrorResponse
import com.development.forge.shared.api.LoginRequest
import com.development.forge.shared.api.RegisterRequest
import io.ktor.client.call.body
import io.ktor.http.isSuccess
import okio.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApi
) {

    suspend fun login(email: String, password: String): AuthResult<AuthResponse> =
        try {
            val response = api.login(LoginRequest(email, password))
            if (response.status.isSuccess()) {
                AuthResult.Success(response.body())
            } else {
                AuthResult.Failure(response.body<ErrorResponse>().error.toAuthError())
            }
        } catch (_: IOException) {
            AuthResult.Failure(AuthError.NETWORK_ERROR)
        } catch (_: Exception) {
            AuthResult.Failure(AuthError.UNKNOWN)
        }

    suspend fun register(email: String, password: String): AuthResult<AuthResponse> =
        try {
            val response = api.register(RegisterRequest(email, password))
            if (response.status.isSuccess()) {
                AuthResult.Success(response.body())
            } else {
                AuthResult.Failure(response.body<ErrorResponse>().error.toAuthError())
            }
        } catch (_: IOException) {
            AuthResult.Failure(AuthError.NETWORK_ERROR)
        } catch (_: Exception) {
            AuthResult.Failure(AuthError.UNKNOWN)
        }

    private fun String.toAuthError(): AuthError = when (this) {
        "email_required", "invalid_email" -> AuthError.INVALID_EMAIL
        "password_required" -> AuthError.EMPTY_PASSWORD
        "weak_password" -> AuthError.WEAK_PASSWORD
        "invalid_credentials" -> AuthError.INVALID_CREDENTIAL
        "email_taken" -> AuthError.EMAIL_ALREADY_EXISTS
        else -> AuthError.UNKNOWN
    }
}