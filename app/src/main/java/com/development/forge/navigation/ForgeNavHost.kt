package com.development.forge.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.development.forge.core.components.ForgeTopBar
import com.development.forge.feature.auth.EmailSentScreen
import com.development.forge.feature.auth.ForgotPasswordScreen
import com.development.forge.feature.auth.LoginScreen
import com.development.forge.feature.auth.RegistrationScreen
import com.development.forge.feature.auth.ResetPasswordScreen
import com.development.forge.feature.auth.SuccessScreen
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
fun ForgeNavHost(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(Splash)
    val currentDestination = backStack.lastOrNull()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14110C))
            .statusBarsPadding()
            .drawBehind {
                drawRect(
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, size.height),
                    brush = Brush.radialGradient(
                        listOf(
                            Color(0xFFF58A00).copy(alpha = 0.10f),
                            Color.Transparent
                        ), center = Offset(size.width * 0.18f, size.height * 0.12f), radius = 420f
                    )
                )
            }) {
        Scaffold(
            modifier = modifier,
            containerColor = Color.Transparent,
            topBar = {
                when (currentDestination) {
                    is Splash, is ForgeNavKey.Success -> {}
                    is ForgeNavKey.Login, is ForgeNavKey.Registration -> ForgeTopBar()
                    else -> ForgeTopBar(onBack = { backStack.removeLastOrNull() })
                }
            }
        ) { padding ->
            val basicModifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(padding)
                .imePadding()
                .statusBarsPadding()
            NavDisplay(
                modifier = Modifier.padding(padding),
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                ),
                entryProvider = entryProvider {
                    entry<Splash> {
                        SplashScreen(basicModifier) {
                            backStack.clear()
                            backStack.add(ForgeNavKey.Login)
                        }
                    }
                    entry<ForgeNavKey.Login> {
                        LoginScreen(basicModifier, onForgotPassword = {
                            backStack.add(ForgeNavKey.ForgotPassword)
                        }, onForgeAccount = {
                            backStack.add(ForgeNavKey.Registration)
                        }, onLogin = {
                            backStack.add(ForgeNavKey.Success)
                        })
                    }
                    entry<ForgeNavKey.Registration> {
                        RegistrationScreen(basicModifier, onRegister = {
                            backStack.add(ForgeNavKey.Success)
                        })
                    }
                    entry<ForgeNavKey.ForgotPassword> { ForgotPasswordScreen(basicModifier) }
                    entry<ForgeNavKey.EmailSent> { EmailSentScreen(basicModifier) }
                    entry<ForgeNavKey.ResetPassword> { ResetPasswordScreen(basicModifier) }
                    entry<ForgeNavKey.Success> { SuccessScreen(basicModifier) }
                },
            )
        }
    }
}

@Preview
@Composable
fun ForgeNavHostPreview() {
    // Create a minimal mock owner for the preview environment
    val mockDispatcherOwner = object : NavigationEventDispatcherOwner {
        override val navigationEventDispatcher = NavigationEventDispatcher()
    }

    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides mockDispatcherOwner) {
        ForgeNavHost()
    }
}
