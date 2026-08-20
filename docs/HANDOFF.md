# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-08-20 · **On top of:** `07ea74c`

`./gradlew :app:compileDebugKotlin --rerun-tasks` → **BUILD SUCCESSFUL**, same warnings as last
note. This is Yash's own WIP (Login wired to a real `AuthApi`/`AuthRepository`) — I verified,
committed, and documented it, didn't finish or fix any of it.

## ⚠️ Read first — Login now makes a real network call, and two things stop it working today

1. **`BASE_URL` just got a call site, and it's still the laptop's stale IP.** Your last note
   flagged `http://10.97.32.25:9292` in `app/build.gradle.kts` as inert — "grepped, zero hits."
   That's no longer true: `NetworkModule.kt` now does
   `install(DefaultRequest) { url(BuildConfig.BASE_URL) }`, so every Ktor request goes there by
   default. Whoever runs Login against a local server next needs to update **both**
   `BASE_URL` and the matching literal in `app/src/debug/res/xml/network_security_config.xml` (same
   trap you called out: change only one and the failure reads as a cleartext/TLS problem instead of
   a wrong-IP one). The non-hardcoded mechanism you suggested last note is worth doing now, not
   later — this is the second machine to get bitten by the same literal.

2. **`LoginScreen`'s own `canSubmit` contradicts its own doc comment, and now it actually matters.**
   `LoginScreen.kt:79` says *"Login does NOT check the 8-character rule. Any non-empty password is
   fine"* — but `LoginScreen.kt:135` is `passwordInput.length >= 8`, and `ForgeButton`'s
   `isEnabled = canSubmit` means the button is inert, not just unstyled, below 8 characters. This
   predates this round untouched, but it was cosmetic before: `onLogin` used to navigate
   unconditionally once tapped, so the gate never blocked a real submission. Now `onLogin` dispatches
   `AuthIntent.SubmitLogin`, which never fires unless the client-side gate opens first — so any
   account with a shorter password (assuming the server allows one) is locked out of the UI
   entirely, before a single network byte is sent. Fix is a one-line call-site question: keep the
   8-char rule and update the comment, or drop `>= 8` to match the comment — not mine to pick.

## Also worth knowing — two latent traps for whoever wires Registration next

- **`SuccessScreen`'s `onContinue` now dispatches `SubmitLogin`, not a navigation call.**
  `ForgeNavHost.kt:301` — used to be `backStack.clear(); backStack.add(Home)`, now it's
  `authViewModel.onIntent(AuthIntent.SubmitLogin)`. `RegistrationScreen` has no
  `onUpdateEmail`/`onUpdatePassword` wiring at all (grepped — only `onRegister` exists), so
  `AuthState.email`/`.password` stay at their `""` defaults through the whole registration flow.
  `SubmitRegistration` is still `TODO()`, so none of this is reachable *yet* — but the moment
  someone implements it following `submitLogin()`'s pattern, landing on Success and tapping
  "Continue" will hit `submitLogin()`'s first guard (`!email.matches(emailRegex)`), set
  `AuthError.INVALID_EMAIL`, and go nowhere. Silent-looking dead end, not a crash.
- **`AuthEffect.NavigateToRegistration` is unreachable from both ends.** Added to the contract this
  round; grepped for dispatch sites — zero, only its own declaration. And even if something sent
  it, `ForgeNavHost.kt`'s new effect collector (lines 96–118) is a `when` over `AuthEffect` that
  ends in `else -> { }` instead of listing all five cases — so adding this arm didn't just leave it
  unhandled, it made the *compiler* stop being able to tell you that. `AuthViewModel.updateEffect()`
  (a new public method, also zero call sites) looks like the intended way to fire it — plausibly for
  a "create account" link that doesn't need real submission — but nothing calls that either yet.

## Splash: the picture is subtler than "the flag's still unwired"

`storeAuthToken` now has a real call site (`AuthViewModel.submitLogin()`, on success) — that closes
the gap the last two notes flagged. But `SplashViewModel.checkAuthStatus()`
(`feature/splash/viewmodel/SplashViewModel.kt:43-47`) checks `!onboardingDone` **first** and
short-circuits to `NavigateToOnboarding` before ever looking at the token:

```kotlin
pendingDestination = when {
    !onboardingDone -> SplashEffect.NavigateToOnboarding
    authToken.isEmpty() -> SplashEffect.NavigateToLogin
    else -> SplashEffect.NavigateToHome
}
```

`setOnboardingDone` still has zero call sites (`OnboardingFlowScreen`'s `onFinish` at
`ForgeNavHost.kt:259-262` is still bare `backStack.clear(); backStack.add(Login)`), so
`onboardingDone` is permanently `false` and Splash's **observable behavior is unchanged** — still
routes to Onboarding on every launch, even after a real, successful, token-storing login. The token
write isn't dead code anymore, but nothing can reach it through Splash yet.

## Resolved this round

- `storeAuthToken` has a real call site — the zero-call-sites gap from the last two notes.
- `AuthState.email`/`.password` are non-null `String = ""` now, not `String?` — closes the
  `?: ""`-everywhere friction flagged last note.

## Carried over (still open, untouched)

- **The Home back-button bug — sixth note running.** Still a product decision, still unmade.
- **`TaskDao.deleteTask`/`deleteTasks` duplicate `@Delete vararg` signature**, only one `suspend`.
  Main-thread-crash trap for the repository layer. Untouched.
- **Splash routing race** — `pendingDestination` is a plain `var`, read once on animation finish,
  no retry if the DataStore reads lose the race. Untouched.
- **`isAuthTokenStored()` returns `Flow<String>`** (the token), not a `Boolean`, despite the `is…`
  name — and now that a real login actually populates it, that token sits in **plain unencrypted
  DataStore**. Was a someday-concern; there's a real token in there now on any machine that's
  logged in once.
- **`core/mvi` base still has zero implementors**, and `AuthViewModel` just grew real business logic
  (`submitLogin`, error mapping, token storage) on top of the same hand-rolled shape `SplashViewModel`
  uses instead of it. The longer this sits, the more there is to migrate if the base ever comes back
  — worth actually deciding, not re-flagging a fourth time.
- **`:shared` still declares `implementation(libs.kotlinx.serialization.json)`, not `api(...)`** —
  fine while both consumers redeclare it themselves. Left alone deliberately per last note.
- Both DAOs minimal; nothing wired to Room; `AppDatabase` never opened at runtime.
- **New-task capture unreachable** — `AddOrEditTaskScreen(editingTask = tasks[0])` default, **+**
  always opens "Edit Task" on task[0].
- **`ForgeDatePickerDialog` NPEs** on OK-without-selection (`selectedDateMillis!!`).
- Save hardcoded (`derivedStateOf { true }`); date/time picker selections not plumbed back.
- **`AddTaskSheet.kt` (~24KB) still orphaned** — safe to delete.
- **Task List bucket-overlap crash still live** (`overdue && due == null` → duplicate `LazyColumn`
  key). Fix: `someday = active.filter { it.due == null && !it.overdue }`.
- Server `TaskDto` still exposes `due: String?` only vs. the local typed `Instant?`.
- Stats hardcoded; onboarding finish/skip → `Login` vs spec's `Registration`; `Motion.kt` empty;
  `SuccessScreen` reset button not deleted.
- **`ForgeTextField` focus ring still an empty `drawBehind { }` stub — 27th note running.**
- Server-side: dev JWT secret in source, `/auth/forgot` logs the reset token to console, CORS
  `anyHost()`, `PUT /tasks/{id}` is a full replace not a merge.
- No Room schema export dir; `gradle.properties` has dead `kapt.*` and a deprecated
  `useClasspathSnapshot` warning.
- **Environment**: still carrying the two memory-pressure symptoms (Gradle daemon SIGSEGV,
  `forge-mongo` restart-looping at 14Gi/14Gi host RAM) from earlier notes. No new data point.
- `.idea/` still uncommitted. Standing "your call" item, untouched again.

## Verified

`./gradlew :app:compileDebugKotlin --rerun-tasks` (forced, not cached) → BUILD SUCCESSFUL.
Compile-only — Login's network path wasn't exercised against a live server this round.
