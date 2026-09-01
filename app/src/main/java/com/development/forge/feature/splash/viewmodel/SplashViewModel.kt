package com.development.forge.feature.splash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.development.forge.BuildConfig
import com.development.forge.core.datastore.ForgePreferences
import com.development.forge.core.mvi.MviViewModel
import com.development.forge.feature.splash.contract.SplashEffect
import com.development.forge.feature.splash.contract.SplashIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Splash ViewModel. Structure-only stub: it wires the feature into [MviViewModel]
 * with an initial state and an empty intent reducer. Logic arrives in a later phase.
 */

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preferences: ForgePreferences
) : ViewModel() {

    companion object {
        private const val SKIP_AUTH_FOR_TESTING = true
    }

    private val _effect = Channel<SplashEffect>()
    val effect = _effect.receiveAsFlow()

    private var pendingDestination: SplashEffect? = null

    fun onIntent(intent: SplashIntent) {
        when (intent) {
            SplashIntent.CheckAuthStatus -> checkAuthStatus()
            SplashIntent.AnimationFinished -> onAnimationFinished()
        }
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            if (BuildConfig.DEBUG && SKIP_AUTH_FOR_TESTING) {
                pendingDestination = SplashEffect.NavigateToHome
                return@launch
            }

            val onboardingDone = preferences.isOnboardingDone().first()
            val authToken = preferences.authToken().first()
            pendingDestination = when {
                !onboardingDone -> SplashEffect.NavigateToOnboarding
                authToken.isEmpty() -> SplashEffect.NavigateToLogin
                else -> SplashEffect.NavigateToHome
            }
        }
    }

    private fun onAnimationFinished() {
        viewModelScope.launch {
            pendingDestination?.let { _effect.send(it) }
        }
    }
}