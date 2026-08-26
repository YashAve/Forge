package com.development.forge.data.repository

import com.development.forge.data.remote.AuthApi
import com.development.forge.data.remote.AuthResult
import com.development.forge.feature.auth.contract.AuthError
import com.development.forge.shared.api.AuthResponse
import com.development.forge.shared.api.ErrorResponse
import com.development.forge.shared.api.ForgotPasswordRequest
import com.development.forge.shared.api.GoogleAuthRequest
import com.development.forge.shared.api.LoginRequest
import com.development.forge.shared.api.MessageResponse
import com.development.forge.shared.api.RegisterRequest
import com.development.forge.shared.api.ResetPasswordRequest
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

    suspend fun forgotPassword(email: String): AuthResult<MessageResponse> =
        safeCall { api.forgotPassword(ForgotPasswordRequest(email))}

    suspend fun resetPassword(token: String, newPassword: String): AuthResult<MessageResponse> =
        safeCall { api.resetPassword(ResetPasswordRequest(token, newPassword)) }

    /**
     * Runs an auth API call and folds the outcome into [AuthResult].
     *
     * A non-2xx response is not an exception here (the Ktor client is configured
     * with expectSuccess = false), so the status is checked explicitly and the
     * server's error code is mapped to a typed [AuthError]. Only transport
     * failures reach the catch blocks.
     */
    private suspend inline fun <reified T> safeCall(
        call: suspend () -> HttpResponse
    ): AuthResult<T> =
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
        "token_required", "invalid_token" -> AuthError.INVALID_TOKEN
        "expired_token" -> AuthError.EXPIRED_TOKEN
        else -> AuthError.UNKNOWN
    }
}