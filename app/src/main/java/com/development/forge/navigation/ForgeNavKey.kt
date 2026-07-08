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
}
