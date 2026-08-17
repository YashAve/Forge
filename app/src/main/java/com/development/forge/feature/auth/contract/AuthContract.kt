package com.development.forge.feature.auth.contract

sealed interface AuthIntent {
    data object SubmitLogin : AuthIntent
    data object SubmitRegistration : AuthIntent
    data object SignInWithGoogle : AuthIntent
    data object SubmitForgotPassword : AuthIntent
    data object SubmitResetPassword : AuthIntent
}

sealed interface AuthEffect {
    data object NavigateToHome : AuthEffect
    data object NavigateToSuccess : AuthEffect
    data object NavigateToEmailSent : AuthEffect
    data object NavigateToLogin : AuthEffect
}