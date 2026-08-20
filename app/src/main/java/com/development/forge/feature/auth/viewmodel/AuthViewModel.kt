package com.development.forge.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.development.forge.core.datastore.ForgePreferences
import com.development.forge.data.remote.AuthResult
import com.development.forge.data.repository.AuthRepository
import com.development.forge.feature.auth.contract.AuthEffect
import com.development.forge.feature.auth.contract.AuthError
import com.development.forge.feature.auth.contract.AuthIntent
import com.development.forge.feature.auth.contract.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val preferences: ForgePreferences,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    private val _effect = Channel<AuthEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val emailRegex = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun onIntent(intent: AuthIntent) {
        when (intent) {
            AuthIntent.SubmitLogin -> submitLogin()
            AuthIntent.SubmitRegistration -> TODO()
            AuthIntent.SignInWithGoogle -> TODO()
            AuthIntent.SubmitForgotPassword -> TODO()
            AuthIntent.SubmitResetPassword -> TODO()
            is AuthIntent.UpdateEmail -> updateEmail(intent.email)
            is AuthIntent.UpdatePassword -> updatePassword(intent.password)
        }
    }

    fun updateEffect(effect: AuthEffect) =
        viewModelScope.launch {
            _effect.send(effect)
        }

    private fun updateEmail(email: String) =
        _authState.update { it.copy(email = email, error = null) }

    private fun updatePassword(password: String) =
        _authState.update { it.copy(password = password, error = null) }

    private fun submitLogin() {
        val email = _authState.value.email.trim()
        val password = _authState.value.password

        if (!email.matches(emailRegex)) {
            _authState.update { it.copy(error = AuthError.INVALID_EMAIL) }
            return
        }
        if (password.isEmpty()) {
            _authState.update { it.copy(error = AuthError.EMPTY_PASSWORD) }
            return
        }

        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> {
                    preferences.storeAuthToken(result.data.token)
                    _authState.update { it.copy(isLoading = false) }
                    _effect.send(AuthEffect.NavigateToHome)
                }
                is AuthResult.Failure -> {
                    _authState.update { it.copy(isLoading = false, error = result.error) }
                }
            }
        }
    }
}