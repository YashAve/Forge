package com.development.forge.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import com.development.forge.feature.auth.contract.AuthEffect
import com.development.forge.feature.auth.contract.AuthIntent
import com.development.forge.feature.auth.contract.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    private val _effect = Channel<AuthEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: AuthIntent) {
        when (intent) {
            AuthIntent.SubmitLogin -> TODO()
            AuthIntent.SubmitRegistration -> TODO()
            AuthIntent.SignInWithGoogle -> TODO()
            AuthIntent.SubmitForgotPassword -> TODO()
            AuthIntent.SubmitResetPassword -> TODO()
            is AuthIntent.UpdateEmail -> updateEmail(intent.email)
            is AuthIntent.UpdatePassword -> updatePassword(intent.password)
        }
    }

    private fun updateEmail(email: String) = _authState.update { it.copy(email = email, error = null) }
    private fun updatePassword(password: String) = _authState.update { it.copy(password = password, error = null) }
}