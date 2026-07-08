package com.development.forge.feature.splash

import com.development.forge.core.mvi.UiEffect
import com.development.forge.core.mvi.UiIntent
import com.development.forge.core.mvi.UiState

/** Immutable state for the splash screen. Structure only — no real fields yet. */
data class SplashState(
    val isLoading: Boolean = true,
) : UiState

/** Intents the splash screen can dispatch. None defined yet. */
sealed interface SplashIntent : UiIntent

/** One-off effects from the splash screen (e.g. navigate onward). None defined yet. */
sealed interface SplashEffect : UiEffect
