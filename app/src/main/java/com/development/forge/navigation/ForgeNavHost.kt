package com.development.forge.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.development.forge.feature.splash.SplashScreen
import com.development.forge.navigation.ForgeNavKey.Splash

/**
 * The single Navigation 3 host for the app.
 *
 * The back stack is ordinary app-owned state ([rememberNavBackStack]); [NavDisplay]
 * renders the top entry, and [entryProvider] maps each [ForgeNavKey] to its screen.
 * The saveable-state decorator preserves per-entry UI state across config changes.
 *
 * TODO(nav3): add rememberViewModelStoreNavEntryDecorator() to scope ViewModels per
 * NavEntry once this project moves to AGP 9 (lifecycle-viewmodel-navigation3 is stable
 * only in lifecycle 2.11.0, which requires AGP 9.1.0). See docs/CROSS-MACHINE-SETUP.md.
 */
@Composable
fun ForgeNavHost() {
    val backStack = rememberNavBackStack(Splash)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<Splash> { SplashScreen() }
        },
    )
}
