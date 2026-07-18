package com.development.forge.navigation

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.development.forge.core.components.ForgeTopBar
import com.development.forge.core.components.OnForgeOnBack
import com.development.forge.feature.auth.EmailSentScreen
import com.development.forge.feature.auth.ForgotPasswordScreen
import com.development.forge.feature.auth.LoginScreen
import com.development.forge.feature.auth.RegistrationScreen
import com.development.forge.feature.auth.ResetPasswordScreen
import com.development.forge.feature.auth.SuccessScreen
import com.development.forge.feature.home.HomeScreen
import com.development.forge.feature.home.components.ForgeBottomNavigationBar
import com.development.forge.feature.onboarding.OnboardingFlowScreen
import com.development.forge.feature.settings.SettingsScreen
import com.development.forge.feature.splash.SplashScreen
import com.development.forge.feature.stats.StatsScreen
import com.development.forge.feature.tasks.TaskListScreen
import com.development.forge.navigation.ForgeNavKey.EmailSent
import com.development.forge.navigation.ForgeNavKey.ForgotPassword
import com.development.forge.navigation.ForgeNavKey.Login
import com.development.forge.navigation.ForgeNavKey.Registration
import com.development.forge.navigation.ForgeNavKey.ResetPassword
import com.development.forge.navigation.ForgeNavKey.Splash
import com.development.forge.navigation.ForgeNavKey.Success

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

/**
 * NAVIGATION SPEC — Splash, Auth, and Onboarding.
 *
 * SPLASH — clears itself always, never returnable, routes based on persisted state:
 *   - never seen onboarding      → ONBOARDING
 *   - seen onboarding, logged out → LOGIN
 *   - seen onboarding, logged in  → HOME
 *
 * ONBOARDING — ONE nav destination with internal step state (0/1/2), NOT three
 * separate nav keys.
 *   - Next: currentStep++ (no back stack involvement)
 *   - Back on step 1 or 2: currentStep-- (needs an explicit BackHandler, doesn't
 *     happen automatically)
 *   - Back on step 0: EXIT APP (nothing behind it, Splash was cleared)
 *   - Skip (steps 0 and 1 only): → REGISTRATION, clear()+add, mark onboarding
 *     complete in persisted state
 *   - Finish (step 2's CTA): → REGISTRATION, clear()+add, mark onboarding complete
 *
 * REGISTRATION and LOGIN are SIBLINGS, not a chain — switching between them
 * REPLACES the top of the stack, it does not push. (Design note: the provided
 * design treats these as push targets; reject that — it lets the stack grow
 * unbounded as a user bounces between the two forms.)
 *   - Back on either: EXIT APP
 *   - Registration submit: → SUCCESS
 *   - Login submit: → HOME directly (SKIP Success — Success is for registration
 *     only, showing it after every login is unnecessary friction)
 *   - Login → "Forgot password?": → FORGOT_PASSWORD
 *
 * FORGOT_PASSWORD → EMAIL_SENT → RESET_PASSWORD — a genuine linear PUSH chain,
 * Back walks backward through it normally.
 *   - EMAIL_SENT/FORGOT_PASSWORD → "Log in" footer: → LOGIN
 *   - RESET_PASSWORD submit: → LOGIN, and CLEAR the entire forgot/reset chain
 *     (a user should never Back into a reset flow they just completed)
 *   - NOTE: RESET_PASSWORD is realistically a DEEP LINK target reached from the
 *     reset email, not reached by tapping forward from EMAIL_SENT. Chaining it
 *     here is a prototype-only simplification.
 *
 * SUCCESS — registration only, never shown after login.
 *   - NO back button, no top bar
 *   - DELETE the "Reset prototype" secondary button — it's a Claude Design
 *     prototype artifact (restarts the design demo) with no place in the real app
 *   - "Continue": → HOME, clear() the ENTIRE stack — this is the hard auth-to-app
 *     boundary; Back must never be able to cross back into auth from inside the app
 *
 * STACK-CLEARING SUMMARY
 *   Splash        → anything      : clear() + add
 *   Onboarding    → Registration  : clear() + add
 *   Login         ↔ Registration  : replace top, never push
 *   ResetPassword → Login         : clear the whole forgot/reset chain
 *   Success       → Home          : clear() the entire stack
 */

@Composable
fun ForgeNavHost(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val backStack = rememberNavBackStack(ForgeNavKey.Home)
    val currentDestination = backStack.lastOrNull()

    BackHandler {
        when (currentDestination) {
            Login, Registration -> backStack.clear()
            ForgeNavKey.TaskList -> backStack.removeLastOrNull()
        }
    }

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
                            Color(0xFFF58A00).copy(alpha = 0.10f), Color.Transparent
                        ), center = Offset(size.width * 0.18f, size.height * 0.12f), radius = 420f
                    )
                )
            }) {
        Scaffold(
            modifier = modifier, containerColor = Color.Transparent, topBar = {
                when (currentDestination) {
                    is Splash, is Success, is ForgeNavKey.OnboardingFlow, is ForgeNavKey.Home, is ForgeNavKey.Statistics, is ForgeNavKey.Settings, is ForgeNavKey.TaskList -> {}
                    is Login, is Registration -> ForgeTopBar()

                    else -> ForgeTopBar(
                        onStart = {
                            OnForgeOnBack {
                                backStack.removeLastOrNull()
                            }
                        }
                    )
                }
            }, bottomBar = {
                when (currentDestination) {
                    is ForgeNavKey.Home, is ForgeNavKey.Statistics, is ForgeNavKey.Settings -> {
                        ForgeBottomNavigationBar(current = currentDestination, onSelect = { value ->
                            Toast.makeText(context, value.toString(), Toast.LENGTH_LONG).show()
                            backStack.add(value)
                        }) {

                        }
                    }

                    else -> {

                    }
                }
            }) { padding ->
            val basicModifier =
                Modifier
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
                            backStack.add(ForgeNavKey.OnboardingFlow)
                        }
                    }
                    entry<Login> {
                        LoginScreen(basicModifier, onForgotPassword = {
                            backStack.add(ForgotPassword)
                        }, onForgeAccount = {
                            backStack.add(Registration)
                        }, onLogin = {
                            backStack.clear()
                            backStack.add(ForgeNavKey.Home)
                        })
                    }
                    entry<Registration> {
                        RegistrationScreen(basicModifier, onRegister = {
                            backStack.clear()
                            backStack.add(Success)
                        })
                    }
                    entry<ForgotPassword> {
                        ForgotPasswordScreen(basicModifier, onSendLink = {
                            backStack.add(EmailSent)
                        }, onRemembered = {
                            backStack.removeLastOrNull()
                            backStack.add(Login)
                        })
                    }
                    entry<EmailSent> { EmailSentScreen(basicModifier) }
                    entry<ResetPassword> {
                        ResetPasswordScreen(basicModifier, onResetPassword = {
                            backStack.add(EmailSent)
                        })
                    }
                    entry<Success> {
                        SuccessScreen(basicModifier, onContinue = {
                            backStack.clear()
                            backStack.add(ForgeNavKey.Home)
                        })
                    }
                    entry<ForgeNavKey.OnboardingFlow> {
                        OnboardingFlowScreen(basicModifier) {
                            backStack.clear()
                            backStack.add(Login)
                        }
                    }
                    entry<ForgeNavKey.Home> {
                        HomeScreen(basicModifier, onSeeAll = {
                            backStack.add(ForgeNavKey.TaskList)
                        })
                    }
                    entry<ForgeNavKey.Statistics> { StatsScreen(basicModifier) }
                    entry<ForgeNavKey.Settings> { SettingsScreen(basicModifier) }
                    entry<ForgeNavKey.TaskList> {
                        TaskListScreen(basicModifier, onBack = {
                            backStack.removeLastOrNull()
                        })
                    }
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
