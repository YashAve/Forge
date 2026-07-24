package com.development.forge.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

interface BottomNavigationSpecifications {
    val label: String
    val activeIcon: ImageVector
    val inactiveIcon: ImageVector
}

sealed interface ForgeNavKey : NavKey {

    @Serializable data object Splash : ForgeNavKey
    @Serializable data object Login : ForgeNavKey
    @Serializable data object Registration : ForgeNavKey
    @Serializable data object ForgotPassword : ForgeNavKey
    @Serializable data object EmailSent : ForgeNavKey
    @Serializable data object ResetPassword : ForgeNavKey
    @Serializable data object Success : ForgeNavKey
    @Serializable data object OnboardingFlow : ForgeNavKey
    @Serializable data object TaskList : ForgeNavKey

    @Serializable
    data class TaskDetail(val taskId: String) : ForgeNavKey

    @Serializable
    data object Home : ForgeNavKey, BottomNavigationSpecifications {
        override val label = "Focus"
        override val activeIcon = Icons.Filled.Bolt
        override val inactiveIcon = Icons.Outlined.Bolt
    }

    @Serializable
    data object Statistics : ForgeNavKey, BottomNavigationSpecifications {
        override val label = "Stats"
        override val activeIcon = Icons.Filled.Insights
        override val inactiveIcon = Icons.Outlined.Insights
    }

    @Serializable
    data object Settings : ForgeNavKey, BottomNavigationSpecifications {
        override val label = "You"
        override val activeIcon = Icons.Filled.Person
        override val inactiveIcon = Icons.Outlined.Person
    }

    @Serializable
    data object AddOrEditTask: ForgeNavKey
}