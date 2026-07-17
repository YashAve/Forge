package com.development.forge.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.development.forge.core.components.Amber
import com.development.forge.core.components.FgTertiary
import kotlinx.serialization.Serializable

interface BottomNavigationSpecifications {
    val label: String
    val activeIcon: ImageVector
    val inactiveIcon: ImageVector
    val activeColor: Color
        get() = Amber
    val inactiveColor: Color
        get() = FgTertiary
}

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
    data object Registration : ForgeNavKey

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
    data object Home : ForgeNavKey, BottomNavigationSpecifications {
        override val label: String = "Focus"
        override val activeIcon = Icons.Filled.Bolt
        override val inactiveIcon = Icons.Outlined.Bolt
    }

    @Serializable
    data object Statistics: ForgeNavKey, BottomNavigationSpecifications {
        override val label: String = "Stats"
        override val activeIcon = Icons.Filled.Insights
        override val inactiveIcon = Icons.Outlined.Insights
    }

    @Serializable
    data object Settings : ForgeNavKey, BottomNavigationSpecifications {
        override val label: String = "You"
        override val activeIcon = Icons.Filled.Person
        override val inactiveIcon = Icons.Outlined.Person
    }
}
