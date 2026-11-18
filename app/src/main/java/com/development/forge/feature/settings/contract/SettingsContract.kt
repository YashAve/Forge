package com.development.forge.feature.settings.contract

data class SettingsState(
    val email: String = "",
    val isNotification: Boolean = false,
    val isSound: Boolean = false
)

sealed interface SettingsIntent {
    data object ToggleNotifications : SettingsIntent
    data object ToggleSound : SettingsIntent
    data object ToggleTheme : SettingsIntent
    data object Logout : SettingsIntent
}

sealed interface SettingsEffect {
    data object NavigateToLogin : SettingsEffect
}