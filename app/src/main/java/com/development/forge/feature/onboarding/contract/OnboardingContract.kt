package com.development.forge.feature.onboarding.contract

sealed interface OnboardingIntent {
    data object Finish : OnboardingIntent
}

sealed interface OnboardingEffect {
    data object NavigateToRegister : OnboardingEffect
}