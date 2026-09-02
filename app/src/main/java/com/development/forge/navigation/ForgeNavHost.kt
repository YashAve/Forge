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
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
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
import com.development.forge.BuildConfig
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
import com.development.forge.feature.tasks.contract.TaskEffect
import com.development.forge.feature.tasks.contract.TaskIntent
import com.development.forge.feature.tasks.ui.AddOrEditTaskScreen
import com.development.forge.feature.tasks.ui.TaskDetailScreen
import com.development.forge.feature.tasks.ui.TaskListScreen
import com.development.forge.feature.tasks.viewmodel.TaskViewModel
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
    val activity = LocalActivity.current

    val scope = rememberCoroutineScope()
    val backStack = rememberNavBackStack(initialDestination)
    val currentDestination = backStack.lastOrNull()

    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarHost = @Composable { SnackbarHost(snackBarHostState) }
    val amber = ForgeTheme.colors.amber

    val authViewModel: AuthViewModel = hiltViewModel()
    val taskViewModel: TaskViewModel = hiltViewModel()
    val authState = authViewModel.authState.collectAsStateWithLifecycle()
    val taskState = taskViewModel.taskState.collectAsStateWithLifecycle()


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
                }
            }
        }
    }

    // App start: push anything that accumulated while the server was unreachable.
    // Keyed on Unit so it runs once per nav host, not on every recomposition.
    LaunchedEffect(Unit) {
        taskViewModel.onIntent(TaskIntent.Sync)
    }

    LaunchedEffect(Unit) {
        taskViewModel.effect.collect { effect ->
            when (effect) {
                TaskEffect.NavigateBack -> backStack.removeLastOrNull()
            }
        }
    }

    LaunchedEffect(authState.value.error) {
        val error = authState.value.error
        if (error == AuthError.INVALID_AUTH_PROVIDER) {
            snackBarHostState.showSnackbar("That email uses a password. Sign in with it instead")
        }
    }

    BackHandler { navigateBack(currentDestination, backStack, taskViewModel) }

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
                ForgeNavTopBar(currentDestination, backStack, taskViewModel)
            },
            bottomBar = {
                ForgeNavBottomBar(currentDestination, backStack, taskViewModel)
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
                            onUpdateResetToken = {
                                authViewModel.onIntent(
                                    AuthIntent.UpdateResetToken(
                                        it
                                    )
                                )
                            },
                            onUpdatePassword = { authViewModel.onIntent(AuthIntent.UpdatePassword(it)) },
                            onUpdateConfirmPassword = {
                                authViewModel.onIntent(
                                    AuthIntent.UpdateConfirmPassword(
                                        it
                                    )
                                )
                            },
                            onResetPassword = { authViewModel.onIntent(AuthIntent.SubmitResetPassword) }
                        )
                    }

                    entry<Success> {
                        SuccessScreen(
                            modifier = basicModifier,
                            email = authState.value.email,
                            onContinue = {
                                authViewModel.onIntent(AuthIntent.ResetFields)
                                backStack.clear()
                                backStack.add(ForgeNavKey.Home)
                            }
                        )
                    }

                    entry<ForgeNavKey.Home> {
                        HomeScreen(
                            modifier = basicModifier,
                            tasks = taskState.value.tasks,
                            onSeeAll = { backStack.add(ForgeNavKey.TaskList) },
                            onOpen = { taskId -> backStack.add(ForgeNavKey.TaskDetail(taskId)) },
                            onToggle = { taskViewModel.onIntent(TaskIntent.ToggleDone(it)) },
                            onAdd = {
                                taskViewModel.onIntent(TaskIntent.StartCreate)
                                backStack.add(ForgeNavKey.AddOrEditTask)
                            }
                        )
                    }

                    entry<ForgeNavKey.Statistics> {
                        StatsScreen(
                            basicModifier,
                            tasks = taskState.value.tasks,
                            doneThisWeek = taskState.value.doneThisWeek,
                            weekCompletions = taskState.value.weekCompletions,
                            currentStreak = taskState.value.currentStreak,
                            longestStreak = taskState.value.longestStreak
                        )
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
                            },
                            showDebugTools = BuildConfig.DEBUG,
                            onSeedData = {
                                taskViewModel.onIntent(TaskIntent.SeedDebugData)
                            }
                        )
                    }

                    entry<ForgeNavKey.TaskList> {
                        TaskListScreen(
                            modifier = basicModifier,
                            tasks = taskState.value.tasks,
                            onBack = { backStack.removeLastOrNull() },
                            onOpen = { taskId ->
                                backStack.add(ForgeNavKey.TaskDetail(taskId))
                                /*taskViewModel.onIntent(TaskIntent.StartEdit(taskId))
                                backStack.add(ForgeNavKey.AddOrEditTask)*/
                            },
                            onToggle = { taskViewModel.onIntent(TaskIntent.ToggleDone(it)) },
                            onAdd = {
                                taskViewModel.onIntent(TaskIntent.StartCreate)
                                backStack.add(ForgeNavKey.AddOrEditTask)
                            }
                        )
                    }

                    entry<ForgeNavKey.TaskDetail> { key ->
                        val task = taskState.value.tasks.find { it.id == key.taskId }
                        if (task != null) {
                            TaskDetailScreen(
                                modifier = basicModifier,
                                task = task,
                                onToggle = { taskViewModel.onIntent(TaskIntent.ToggleDone(it)) },
                                onComplete = { taskViewModel.onIntent(TaskIntent.ToggleDone(it)) },
                                onToggleSub = { taskViewModel.onIntent(TaskIntent.ToggleSubtask(it)) },
                                onEdit = {
                                    taskViewModel.onIntent(TaskIntent.StartEdit(task.id))
                                    backStack.add(ForgeNavKey.AddOrEditTask)
                                }
                            )
                        }
                    }

                    entry<ForgeNavKey.AddOrEditTask> {
                        AddOrEditTaskScreen(
                            basicModifier,
                            state = taskState.value,
                            existingProjects = taskState.value.existingProjects,
                            onUpdateTitle = { taskViewModel.onIntent(TaskIntent.UpdateTitle(it)) },
                            onUpdatePriority = { taskViewModel.onIntent(TaskIntent.UpdatePriority(it)) },
                            onUpdateProject = { taskViewModel.onIntent(TaskIntent.UpdateProject(it)) },
                            onUpdateDueAt = { taskViewModel.onIntent(TaskIntent.UpdateDueAt(it)) },
                            onUpdateNotes = { taskViewModel.onIntent(TaskIntent.UpdateNotes(it)) },
                            onSave = { taskViewModel.onIntent(TaskIntent.SaveTask(it)) },
                            onBack = {
                                taskViewModel.onIntent(TaskIntent.ClearForm)
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

// ═══════════════════════════════════════════════════════════════
// CHROME — the three `when`s that decide what wraps the current screen
// ═══════════════════════════════════════════════════════════════
// Pulled out of [ForgeNavHost] so the Scaffold reads as a shape rather than as
// three nested dispatch tables. They stay in this file, and keep taking the
// back stack and the ViewModel directly, because they are nav-host internals
// rather than screens — the "screens never touch a ViewModel" rule is about the
// composables under `feature/`, which these are not.
//
// The entry table itself is deliberately NOT extracted: it is long because the
// app has many screens, and scattering it across functions would hide the
// navigation graph rather than reveal it.

// Typed Set<NavKey> rather than left to inference: the back stack is
// NavBackStack<NavKey>, so `currentDestination` is NavKey?, and `in` against an
// inferred Set<ForgeNavKey> cannot resolve its type parameter.
/** Destinations where back exits the app rather than going anywhere. */
private val CLEAR_TO_HOME: Set<NavKey> = setOf(Login, Registration, ForgeNavKey.Home)

/** Bottom-nav destinations where back returns to Home rather than popping. */
private val RESET_TO_HOME: Set<NavKey> = setOf(ForgeNavKey.Statistics, ForgeNavKey.Settings)

/** The top bar for [currentDestination] — nothing at all on the full-bleed screens. */
@Composable
private fun ForgeNavTopBar(
    currentDestination: NavKey?,
    backStack: NavBackStack<NavKey>,
    taskViewModel: TaskViewModel
) {
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
                    onEnd = {
                        OnForgeOnAdd {
                            // ClearForm first: TaskViewModel is scoped to the nav host, so
                            // without it the form still holds the last task that was edited.
                            taskViewModel.onIntent(TaskIntent.ClearForm)
                            backStack.add(ForgeNavKey.AddOrEditTask)
                        }
                    }
                )

                is ForgeNavKey.TaskDetail -> ForgeTaskTopBar(
                    label = "Task",
                    onStart = { OnForgeOnBack { backStack.removeLastOrNull() } },
                    onEnd = {
                        OnForgeOnEdit {
                            // StartEdit sets editingTaskId, which is what makes saveTask()
                            // update instead of create. Without it this opened a blank form.
                            taskViewModel.onIntent(
                                TaskIntent.StartEdit(currentDestination.taskId)
                            )
                            backStack.add(ForgeNavKey.AddOrEditTask)
                        }
                    }
                )

                else -> ForgeTopBar(
                    onStart = { OnForgeOnBack { backStack.removeLastOrNull() } }
                )
            }
}

/** The bottom nav, shown only on the three tabbed destinations. */
@Composable
private fun ForgeNavBottomBar(
    currentDestination: NavKey?,
    backStack: NavBackStack<NavKey>,
    taskViewModel: TaskViewModel
) {
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
                            taskViewModel.onIntent(TaskIntent.ClearForm)
                            backStack.add(ForgeNavKey.AddOrEditTask)
                        }
                    )
                }

                else -> {}
            }
}

/** System-back handling. Not composable — it runs in response to an event, not a recomposition. */
private fun navigateBack(
    currentDestination: NavKey?,
    backStack: NavBackStack<NavKey>,
    taskViewModel: TaskViewModel
) {
    // The null branch is first so the rest smart-casts to non-null NavKey, which is
    // what lets `in` resolve against Set<NavKey>. An empty back stack cannot happen
    // in practice; clearing is the harmless reading of "nowhere to go back to".
    when {
        currentDestination == null -> backStack.clear()

        currentDestination in CLEAR_TO_HOME -> backStack.clear()

        currentDestination in RESET_TO_HOME -> {
            backStack.clear()
            backStack.add(ForgeNavKey.Home)
        }

        currentDestination == ForgeNavKey.AddOrEditTask -> {
            taskViewModel.onIntent(TaskIntent.ClearForm)
            backStack.removeLastOrNull()
        }

        else -> backStack.removeLastOrNull()
    }
}
