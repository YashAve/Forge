package com.development.forge.feature.splash.contract

sealed interface SplashIntent {
    data object CheckAuthStatus : SplashIntent
    data object AnimationFinished : SplashIntent
}

sealed interface SplashEffect {
    data object NavigateToOnboarding : SplashEffect
    data object NavigateToLogin : SplashEffect
    data object NavigateToHome : SplashEffect
}

