package com.development.forge.feature.auth.contract

enum class AuthError {
    INVALID_EMAIL,
    WEAK_PASSWORD,
    EMPTY_PASSWORD,
    INVALID_CREDENTIAL,
    EMAIL_ALREADY_EXISTS,
    INVALID_AUTH_PROVIDER,
    NETWORK_ERROR,
    INVALID_TOKEN,
    EXPIRED_TOKEN,
    UNKNOWN
}

data class AuthState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val resetToken: String = "",
    val error: AuthError? = null,
    val isLoading: Boolean = false
)

sealed interface AuthIntent {
    data object SubmitLogin : AuthIntent
    data object SubmitRegistration : AuthIntent
    data class SignInWithGoogle(val idToken: String) : AuthIntent
    data object SubmitForgotPassword : AuthIntent
    data object SubmitResetPassword : AuthIntent
    data class UpdateEmail(val email: String) : AuthIntent
    data class UpdatePassword(val password: String) : AuthIntent
    data object ResetFields : AuthIntent
    data class UpdateConfirmPassword(val confirmPassword: String): AuthIntent
    data class UpdateResetToken(val resetToken: String) : AuthIntent
}

sealed interface AuthEffect {
    data object NavigateToHome : AuthEffect
    data object NavigateToSuccess : AuthEffect
    data object NavigateToEmailSent : AuthEffect
    data object NavigateToLogin : AuthEffect
}