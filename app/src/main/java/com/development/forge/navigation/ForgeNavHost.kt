package com.development.forge.navigation

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
import com.development.forge.core.components.ForgeTaskTopBar
import com.development.forge.core.components.ForgeTopBar
import com.development.forge.core.components.OnForgeOnAdd
import com.development.forge.core.components.OnForgeOnBack
import com.development.forge.core.components.OnForgeOnEdit
import com.development.forge.core.designsystem.ForgeTheme
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
import com.development.forge.feature.tasks.AddOrEditTaskScreen
import com.development.forge.feature.tasks.TaskDetailScreen
import com.development.forge.feature.tasks.TaskListScreen
import com.development.forge.navigation.ForgeNavKey.EmailSent
import com.development.forge.navigation.ForgeNavKey.ForgotPassword
import com.development.forge.navigation.ForgeNavKey.Login
import com.development.forge.navigation.ForgeNavKey.Registration
import com.development.forge.navigation.ForgeNavKey.ResetPassword
import com.development.forge.navigation.ForgeNavKey.Splash
import com.development.forge.navigation.ForgeNavKey.Success

@Composable
fun ForgeNavHost(
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    onToggleTheme: () -> Unit = {}
) {
    val context = LocalContext.current
    val backStack = rememberNavBackStack(Splash)
    val currentDestination = backStack.lastOrNull()

    val amber = ForgeTheme.colors.amber   // captured for the drawBehind ambient wash below

    BackHandler {
        when (currentDestination) {
            Login, Registration -> backStack.clear()
            ForgeNavKey.TaskList -> backStack.removeLastOrNull()
            else -> { }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ForgeTheme.colors.bgBase)
            .statusBarsPadding()
            .drawBehind {
                drawRect(
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, size.height),
                    brush = Brush.radialGradient(
                        listOf(
                            amber.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.18f, size.height * 0.12f),
                        radius = 420f
                    )
                )
            }
    ) {
        Scaffold(
            modifier = modifier,
            containerColor = Color.Transparent,
            topBar = {
                when (currentDestination) {
                    is Splash,
                    is Success,
                    is ForgeNavKey.OnboardingFlow,
                    is ForgeNavKey.Home,
                    is ForgeNavKey.Statistics,
                    is ForgeNavKey.Settings,
                    is ForgeNavKey.AddOrEditTask -> { }

                    is Login,
                    is Registration -> ForgeTopBar()

                    is ForgeNavKey.TaskList -> ForgeTaskTopBar(
                        label = "All tasks",
                        onStart = { OnForgeOnBack { backStack.removeLastOrNull() } },
                        onEnd = { OnForgeOnAdd { /* TODO: open add sheet */ } }
                    )

                    is ForgeNavKey.TaskDetail -> ForgeTaskTopBar(
                        label = "Task",
                        onStart = { OnForgeOnBack { backStack.removeLastOrNull() } },
                        onEnd = { OnForgeOnEdit { backStack.add(ForgeNavKey.AddOrEditTask) } }
                    )

                    else -> ForgeTopBar(
                        onStart = { OnForgeOnBack { backStack.removeLastOrNull() } }
                    )
                }
            },
            bottomBar = {
                when (currentDestination) {
                    is ForgeNavKey.Home,
                    is ForgeNavKey.Statistics,
                    is ForgeNavKey.Settings -> {
                        ForgeBottomNavigationBar(
                            current = currentDestination,
                            onSelect = { key ->
                                backStack.clear()
                                backStack.add(key)
                            },
                            onAddClick = {
                                backStack.add(ForgeNavKey.AddOrEditTask)
                            }
                        )
                    }

                    else -> {}
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
                    rememberSaveableStateHolderNavEntryDecorator()
                ),
                entryProvider = entryProvider {

                    entry<Splash> {
                        SplashScreen(basicModifier) {
                            backStack.clear()
                            backStack.add(ForgeNavKey.OnboardingFlow)
                        }
                    }

                    entry<ForgeNavKey.OnboardingFlow> {
                        OnboardingFlowScreen(basicModifier) {
                            backStack.clear()
                            backStack.add(Login)
                        }
                    }

                    entry<Login> {
                        LoginScreen(
                            modifier = basicModifier,
                            onForgotPassword = { backStack.add(ForgotPassword) },
                            onForgeAccount = {
                                // replace top — siblings, not a push
                                backStack.removeLastOrNull()
                                backStack.add(Registration)
                            },
                            onLogin = {
                                backStack.clear()
                                backStack.add(ForgeNavKey.Home)
                            }
                        )
                    }

                    entry<Registration> {
                        RegistrationScreen(
                            modifier = basicModifier,
                            onRegister = {
                                backStack.clear()
                                backStack.add(Success)
                            }
                        )
                    }

                    entry<ForgotPassword> {
                        ForgotPasswordScreen(
                            modifier = basicModifier,
                            onSendLink = { backStack.add(EmailSent) },
                            onRemembered = {
                                backStack.removeLastOrNull()
                                backStack.add(Login)
                            }
                        )
                    }

                    entry<EmailSent> {
                        EmailSentScreen(basicModifier)
                    }

                    entry<ResetPassword> {
                        ResetPasswordScreen(
                            modifier = basicModifier,
                            onResetPassword = {
                                // clear the whole forgot/reset chain, land on Login
                                while (backStack.lastOrNull() !is Login &&
                                    backStack.size > 1
                                ) {
                                    backStack.removeLastOrNull()
                                }
                                backStack.add(Login)
                            }
                        )
                    }

                    entry<Success> {
                        SuccessScreen(
                            modifier = basicModifier,
                            onContinue = {
                                backStack.clear()
                                backStack.add(ForgeNavKey.Home)
                            }
                        )
                    }

                    entry<ForgeNavKey.Home> {
                        HomeScreen(
                            modifier = basicModifier,
                            onSeeAll = {
                                backStack.add(ForgeNavKey.TaskList)
                            },
                            onOpen = { taskId ->
                                backStack.add(ForgeNavKey.TaskDetail(taskId))
                            },
                            onToggle = {
                                // TODO: toggle task done state
                            },
                            onAdd = {

                            }
                        )
                    }

                    entry<ForgeNavKey.Statistics> {
                        StatsScreen(basicModifier)
                    }

                    entry<ForgeNavKey.Settings> {
                        SettingsScreen(
                            modifier = basicModifier,
                            isDark = isDark,
                            onToggleTheme = onToggleTheme,
                            onSignOut = {
                                backStack.clear()
                                backStack.add(Login)
                            }
                        )
                    }

                    entry<ForgeNavKey.TaskList> {
                        TaskListScreen(
                            modifier = basicModifier,
                            onBack = { backStack.removeLastOrNull() },
                            onOpen = { taskId ->
                                backStack.add(ForgeNavKey.TaskDetail(taskId))
                            },
                            onToggle = {
                                // TODO: toggle task done state
                            },
                            onAdd = {
                                backStack.add(ForgeNavKey.AddOrEditTask)
                            }
                        )
                    }

                    entry<ForgeNavKey.TaskDetail> { key ->
                        val task = com.development.forge.domain.model.tasks
                            .find { it.id == key.taskId }
                        if (task != null) {
                            TaskDetailScreen(
                                modifier = basicModifier,
                                task = task,
                                onToggle = {
                                    // TODO: reopen task
                                },
                                onComplete = {
                                    // TODO: complete task
                                },
                                onToggleSub = {
                                    // TODO: toggle subtask
                                },
                                onEdit = {

                                }
                            )
                        }
                    }
                    entry<ForgeNavKey.AddOrEditTask> {
                        AddOrEditTaskScreen(basicModifier, onBack = {
                            backStack.removeLastOrNull()
                        })
                    }
                }
            )
        }
    }
}

@Preview
@Composable
fun ForgeNavHostPreview() {
    val mockDispatcherOwner = object : NavigationEventDispatcherOwner {
        override val navigationEventDispatcher = NavigationEventDispatcher()
    }
    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides mockDispatcherOwner) {
        ForgeNavHost()
    }
}