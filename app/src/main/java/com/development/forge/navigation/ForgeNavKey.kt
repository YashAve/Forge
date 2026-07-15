package com.development.forge.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Every Forge navigation destination. Keys are the app-owned back-stack elements
 * that [ForgeNavHost] renders. Each is [Serializable] so Nav3 can save and restore
 * the back stack across process death.
 */
sealed interface ForgeNavKey : NavKey {

    @Serializable
    data object Splash : ForgeNavKey

    @Serializable
    data object Login : ForgeNavKey

    @Serializable
    data object Registration : NavKey

    @Serializable
    data object ForgotPassword : ForgeNavKey

    @Serializable
    data object EmailSent : ForgeNavKey

    @Serializable
    data object ResetPassword : ForgeNavKey

    @Serializable
    data object Success : ForgeNavKey

    @Serializable
    data object OnboardingFlow : ForgeNavKey
    @Serializable
    data object Home : ForgeNavKey
    @Serializable
    data object Add : ForgeNavKey
    @Serializable
    data object Statistics: ForgeNavKey
    @Serializable
    data object Settings : ForgeNavKey
}
