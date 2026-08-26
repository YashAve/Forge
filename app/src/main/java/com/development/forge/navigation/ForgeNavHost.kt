package com.development.forge.navigation

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.development.forge.feature.auth.contract.AuthEffect
import com.development.forge.feature.auth.contract.AuthError
import com.development.forge.feature.auth.contract.AuthIntent
import com.development.forge.feature.auth.ui.EmailSentScreen
import com.development.forge.feature.auth.ui.ForgotPasswordScreen
import com.development.forge.feature.auth.ui.LoginScreen
import com.development.forge.feature.auth.ui.RegistrationScreen
import com.development.forge.feature.auth.ui.ResetPasswordScreen
import com.development.forge.feature.auth.ui.SuccessScreen
import com.development.forge.feature.auth.ui.rememberGoogleSignIn
import com.development.forge.feature.auth.viewmodel.AuthViewModel
import com.development.forge.feature.home.HomeScreen
import com.development.forge.feature.home.components.ForgeBottomNavigationBar
import com.development.forge.feature.onboarding.contract.OnboardingEffect
import com.development.forge.feature.onboarding.contract.OnboardingIntent
import com.development.forge.feature.onboarding.ui.OnboardingFlowScreen
import com.development.forge.feature.onboarding.viewmodel.OnboardingViewModel
import com.development.forge.feature.settings.contract.SettingsEffect
import com.development.forge.feature.settings.contract.SettingsIntent
import com.development.forge.feature.settings.ui.SettingsScreen
import com.development.forge.feature.settings.viewmodel.SettingsViewModel
import com.development.forge.feature.splash.contract.SplashEffect
import com.development.forge.feature.splash.contract.SplashIntent
import com.development.forge.feature.splash.ui.SplashScreen
import com.development.forge.feature.splash.viewmodel.SplashViewModel
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
import kotlinx.coroutines.launch

@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun ForgeNavHost(
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    initialDestination: ForgeNavKey = Splash,
    onToggleTheme: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    val backStack = rememberNavBackStack(initialDestination)
    val currentDestination = backStack.lastOrNull()
    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarHost = @Composable { SnackbarHost(snackBarHostState) }

    val amber = ForgeTheme.colors.amber   // captured for the drawBehind ambient wash below

    // One AuthViewModel for the whole auth flow. Activity-scoped in practice because the
    // Nav3 ViewModel-store decorator isn't wired (see libs.versions.toml), so every
    // hiltViewModel() below resolves to the same instance. The ViewModel owns all auth
    // decisions; screens only send intents.
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState = authViewModel.authState.collectAsStateWithLifecycle()

    // Auth effects are collected here, beside NavDisplay, tied to the nav host's lifecycle —
    // not inside an entry, where it would be at the mercy of entryProvider recomposition.
    LaunchedEffect(Unit) {
        authViewModel.effect.collect { effect ->
            with(backStack) {
                when (effect) {
                    AuthEffect.NavigateToHome -> {
                        clear()
                        add(ForgeNavKey.Home)
                    }

                    AuthEffect.NavigateToSuccess -> {
                        clear()
                        add(Success)
                    }

                    AuthEffect.NavigateToEmailSent -> {
                        add(EmailSent)
                    }

                    AuthEffect.NavigateToLogin -> {
                        clear()
                        add(Login)
                    }

                    else -> {

                    }
                }
            }
        }
    }

    LaunchedEffect(authState.value.error) {
        val error = authState.value.error
        if (error == AuthError.INVALID_AUTH_PROVIDER) {
            snackBarHostState.showSnackbar("That email uses a password. Sign in with it instead")
        }
    }

    BackHandler {
        when (currentDestination) {
            Login, Registration, ForgeNavKey.Home -> backStack.clear()
            ForgeNavKey.TaskList -> backStack.removeLastOrNull()
            ForgeNavKey.Statistics, ForgeNavKey.Settings -> {
                backStack.clear()
                backStack.add(ForgeNavKey.Home)
            }

            else -> {}
        }
    }

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
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
            snackbarHost = snackBarHost,
            topBar = {
                when (currentDestination) {
                    is Splash,
                    is Success,
                    is ForgeNavKey.OnboardingFlow,
                    is ForgeNavKey.Home,
                    is ForgeNavKey.Statistics,
                    is ForgeNavKey.Settings,
                    is ForgeNavKey.AddOrEditTask -> {
                    }

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
                        val viewModel: SplashViewModel = hiltViewModel()

                        LaunchedEffect(Unit) {
                            with(backStack) {
                                viewModel.effect.collect { effect ->
                                    clear()
                                    when (effect) {
                                        SplashEffect.NavigateToOnboarding -> add(ForgeNavKey.OnboardingFlow)
                                        SplashEffect.NavigateToLogin -> add(Login)
                                        SplashEffect.NavigateToHome -> add(ForgeNavKey.Home)
                                    }
                                }
                            }
                        }

                        LaunchedEffect(Unit) {
                            viewModel.onIntent(SplashIntent.CheckAuthStatus)
                        }

                        SplashScreen(basicModifier) {
                            viewModel.onIntent(SplashIntent.AnimationFinished)
                        }
                    }

                    entry<ForgeNavKey.OnboardingFlow> {
                        val viewModel: OnboardingViewModel = hiltViewModel()

                        LaunchedEffect(Unit) {
                            viewModel.effect.collect { effect ->
                                when (effect) {
                                    is OnboardingEffect.NavigateToRegister -> {
                                        backStack.clear()
                                        backStack.add(Registration)
                                    }
                                }
                            }
                        }

                        OnboardingFlowScreen(basicModifier) {
                            viewModel.onIntent(OnboardingIntent.Finish)
                        }
                    }

                    entry<Login> {

                        val googleSignIn = rememberGoogleSignIn(onToken = { token ->
                            authViewModel.onIntent(AuthIntent.SignInWithGoogle(token))
                        }) {
                            scope.launch {
                                snackBarHostState.showSnackbar("Couldn't sign in with Google")
                            }
                        }

                        LoginScreen(
                            modifier = basicModifier,
                            state = authState.value,
                            onUpdateEmail = { authViewModel.onIntent(AuthIntent.UpdateEmail(it)) },
                            onUpdatePassword = { authViewModel.onIntent(AuthIntent.UpdatePassword(it)) },
                            onForgotPassword = { backStack.add(ForgotPassword) },
                            onForgeAccount = {
                                authViewModel.onIntent(AuthIntent.ResetFields)
                                backStack.removeLastOrNull()
                                backStack.add(Registration)
                            },
                            onLogin = { authViewModel.onIntent(AuthIntent.SubmitLogin) },
                            onGoogleSignIn = googleSignIn
                        )
                    }

                    entry<Registration> {
                        RegistrationScreen(
                            modifier = basicModifier,
                            state = authState.value,
                            onUpdateEmail = { authViewModel.onIntent(AuthIntent.UpdateEmail(it)) },
                            onUpdatePassword = { authViewModel.onIntent(AuthIntent.UpdatePassword(it)) },
                            onRegister = { authViewModel.onIntent(AuthIntent.SubmitRegistration) },
                            onSignIn = {
                                authViewModel.onIntent(AuthIntent.ResetFields)
                                backStack.removeLastOrNull()
                                backStack.add(Login)
                            }
                        )
                    }

                    entry<ForgotPassword> {
                        ForgotPasswordScreen(
                            modifier = basicModifier,
                            state = authState.value,
                            onUpdateEmail = { authViewModel.onIntent(AuthIntent.UpdateEmail(it)) },
                            onSendLink = { authViewModel.onIntent(AuthIntent.SubmitForgotPassword) },
                            onRemembered = {
                                backStack.removeLastOrNull()
                                authViewModel.onIntent(AuthIntent.ResetFields)
                            }
                        )
                    }

                    entry<EmailSent> {
                        EmailSentScreen(
                            basicModifier,
                            state = authState.value,
                            onEnterToken = {
                                backStack.removeLastOrNull()
                                backStack.add(ResetPassword)
                            },
                            onResend = { authViewModel.onIntent(AuthIntent.SubmitForgotPassword) }
                        )
                    }

                    entry<ResetPassword> {
                        ResetPasswordScreen(
                            modifier = basicModifier,
                            state = authState.value,
                            onUpdateResetToken = { authViewModel.onIntent(AuthIntent.UpdateResetToken(it)) },
                            onUpdatePassword = { authViewModel.onIntent(AuthIntent.UpdatePassword(it)) },
                            onUpdateConfirmPassword = { authViewModel.onIntent(AuthIntent.UpdateConfirmPassword(it)) },
                            onResetPassword = { authViewModel.onIntent(AuthIntent.SubmitResetPassword) }
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
                        val viewModel: SettingsViewModel = hiltViewModel()

                        LaunchedEffect(Unit) {
                            viewModel.effect.collect { effect ->
                                when (effect) {
                                    is SettingsEffect.NavigateToLogin -> {
                                        backStack.clear()
                                        backStack.add(Login)
                                    }
                                }
                            }
                        }

                        SettingsScreen(
                            modifier = basicModifier,
                            isDark = isDark,
                            onToggleTheme = onToggleTheme,
                            onSignOut = {
                                viewModel.onIntent(SettingsIntent.Logout)
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
        ForgeNavHost(initialDestination = ForgeNavKey.Home) // Bypasses Splash ViewModel execution
    }
}