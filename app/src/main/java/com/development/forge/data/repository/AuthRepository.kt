package com.development.forge.data.repository

import com.development.forge.data.remote.AuthApi
import com.development.forge.data.remote.AuthResult
import com.development.forge.feature.auth.contract.AuthError
import com.development.forge.shared.api.AuthResponse
import com.development.forge.shared.api.ErrorResponse
import com.development.forge.shared.api.GoogleAuthRequest
import com.development.forge.shared.api.LoginRequest
import com.development.forge.shared.api.RegisterRequest
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApi
) {

    suspend fun login(email: String, password: String): AuthResult<AuthResponse> =
        safeCall { api.login(LoginRequest(email, password)) }

    suspend fun register(email: String, password: String): AuthResult<AuthResponse> =
        safeCall { api.register(RegisterRequest(email, password)) }

    suspend fun googleAuth(idToken: String): AuthResult<AuthResponse> =
        safeCall { api.googleAuth(GoogleAuthRequest(idToken)) }

    /**
     * Runs an auth API call and folds the outcome into [AuthResult].
     *
     * A non-2xx response is not an exception here (the Ktor client is configured
     * with expectSuccess = false), so the status is checked explicitly and the
     * server's error code is mapped to a typed [AuthError]. Only transport
     * failures reach the catch blocks.
     */
    private suspend fun safeCall(
        call: suspend () -> HttpResponse
    ): AuthResult<AuthResponse> =
        try {
            val response = call()
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
        "invalid_auth_provider" -> AuthError.INVALID_AUTH_PROVIDER
        else -> AuthError.UNKNOWN
    }
}