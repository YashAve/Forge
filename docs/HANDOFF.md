# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-08-24 · **On top of:** `1e52158`

`./gradlew :app:compileDebugKotlin --rerun-tasks` → **BUILD SUCCESSFUL**. This round is Yash's own
follow-up feature pass — Registration wired to `AuthState`, Onboarding and Settings converted to
MVI, plus a new project README — I verified, committed, and documented it, didn't finish or fix
any of it myself.

## ⚠️ Still open — two blockers, one new and severe

- **Registration is broken twice over, and neither bug shows up at compile time.**
  1. `RegistrationScreen.kt:83` and `:91` — `onValueChange = { onUpdateEmail(state.email) }` (and
     the same shape for password). `ForgeTextField`/`ForgePasswordField`'s
     `onValueChange: (String) -> Unit` hands back the *new* typed text as its parameter — but
     both lambdas ignore that parameter and resend `state.email`/`state.password`, the stale
     value already in state. Since `value = state.email` makes this a controlled field, every
     keystroke round-trips back to the same value it already had — the fields are inert. Compare
     `LoginScreen.kt`, which does this correctly: `onValueChange = onUpdateEmail` (a direct
     reference, so the field's own new value flows straight through untouched).
  2. Even past that: `AuthViewModel.kt:38` — `AuthIntent.SubmitRegistration -> TODO()`.
     `submitRegistration()` is fully implemented two methods below (validates, calls
     `authRepository.register()`, stores the token, emits `NavigateToSuccess` on success) — but
     nothing dispatches to it. Tapping "Continue" on Registration throws `NotImplementedError`
     immediately.

  Both are runtime-only; the forced rebuild below is clean either way. Right now Registration is
  unusable end to end — can't meaningfully type into it, and the submit path crashes regardless.

- **The stale BASE_URL blocker from previous notes is still untouched.** Nothing touched
  `NetworkModule.kt` or `app/build.gradle.kts` this round either: `BASE_URL` is still hardcoded to
  a laptop DHCP IP, with a real call site (`install(DefaultRequest) { url(BuildConfig.BASE_URL) }`).
  Anyone running Login/Registration against a local server on a different machine still needs to
  update **both** `BASE_URL` and the matching literal in `network_security_config.xml`.

## Resolved this round

- **Registration wired to `AuthState`/`AuthViewModel`** — same architectural move Login got last
  round. Local `remember`/`rememberSaveable` state (`emailInput`, `passwordInput`, `isLoading`,
  `canSubmit`) is gone; fields bind to `state.email`/`state.password`, errors surface via new
  `AuthError?.emailMessage()`/`.passwordMessage()` extensions. The wiring itself has the two live
  bugs above, but the shape is right and matches Login's pattern.
- **`AuthViewModel.updateEffect()` — deleted.** Last note flagged it as "zero call sites, worth
  checking whether it's still needed" — resolved by removal rather than by finding it a use.
- **`Success` screen's `onContinue` fixed.** Was `authViewModel.onIntent(AuthIntent.SubmitLogin)`
  — nonsensically re-submitting a login from the post-registration success screen. Now correctly
  does `backStack.clear(); backStack.add(Home)`.
- **Onboarding converted to MVI** — new `OnboardingContract`/`OnboardingViewModel`. Finishing (or
  skipping — both buttons call the same `onFinish`) now sends `OnboardingIntent.Finish`, which
  calls `preferences.setOnboardingDone(true)` and emits `NavigateToRegister`. This closes two
  long-carried items at once: `setOnboardingDone` finally has a call site, and onboarding now
  routes to Registration instead of Login, matching the design spec.
- **Side effect of the above: the "Splash always routes to Onboarding" bug is actually fixed now,
  without Splash itself changing.** Re-read `SplashViewModel.kt` — it's byte-for-byte what it's
  been for many rounds: `!onboardingDone -> Onboarding`, else check the token. That logic was
  always correct; it just never had real data, because nothing ever set `onboardingDone` to
  `true`. Now that `OnboardingViewModel` does, a second cold start after completing onboarding
  once will correctly fall through to the token check instead of looping back to Onboarding.
- **Settings converted to MVI** — new `SettingsContract`/`SettingsViewModel`. Sign-out now goes
  through `SettingsIntent.Logout` → `SettingsViewModel.logout()`, which calls a new
  `ForgePreferences.clearAuthToken()` before navigating to Login. Previously sign-out only cleared
  the nav back stack — the stored token was never actually removed. Wasn't a previously-flagged
  item, but a real fix worth noting.
- `AuthApi`: the `withContext(Dispatchers.IO)` wrapper moved down from `AuthRepository` into
  `AuthApi` itself (one level closer to the actual I/O), and a `register()` call was added
  alongside `login()`.
- Added a project `README.md` — architecture, tech stack, and design-decision writeup for the
  repo, committed separately from the feature work.

## Worth knowing — new things this pass surfaced

- **`SettingsViewModel` has the same `TODO()` shape as the Registration crash above, but it's
  currently harmless.** `onIntent()` maps `ToggleNotifications`/`ToggleSound` to `TODO()` too —
  but `SettingsScreen`'s two toggles are still fully local (`remember { mutableStateOf(...) }`),
  never wired to the ViewModel, so those branches are unreachable today. Worth knowing before
  someone wires the toggles up expecting the ViewModel side to already work — it doesn't yet.
- **`SettingsScreen`'s `email` param is never passed from the nav host.**
  `entry<ForgeNavKey.Settings>` calls `SettingsScreen(modifier, isDark, onToggleTheme, onSignOut)`
  — no `email` argument — so it silently falls back to the composable's own default,
  `"karishma.kapoor@cosmiq.co.in"`. Every real user currently sees a fake name and email on their
  own Settings screen. Not new this round (the default's been there since the screen was first
  built) — just noticed it now while checking this round's nav-host wiring; hadn't been flagged
  before.
- The dead `else -> { }` in `ForgeNavHost`'s auth-effect collector (flagged last note) is still
  there, and the compiler still calls it out — same warning, now at line 123 instead of 116
  (shifted by this round's additions, nothing else changed about it).

## Carried over (still open, untouched)

- **The Home back-button bug — eighth note running.** Still a product decision, still unmade.
- **`TaskDao.deleteTask`/`deleteTasks` duplicate `@Delete vararg` signature**, only one `suspend`.
  Main-thread-crash trap for the repository layer. Untouched.
- **`isAuthTokenStored()` returns `Flow<String>`** (the token), not a `Boolean`, despite the `is…`
  name — real tokens now sit in plain unencrypted DataStore on any machine that's logged in once.
  Now meaningfully exercised for the first time (see Splash above), still misnamed.
- **`core/mvi` base's orphan count is now four, not two.** `OnboardingViewModel` and
  `SettingsViewModel` both hand-roll the identical `Channel`-effect / intent-dispatch shape that
  `AuthViewModel` and `SplashViewModel` already duplicated. Four independent copies of the same
  pattern is a stronger case for actually deciding this one way or the other — worth doing before
  a fifth feature copies it again.
- **`:shared` still declares `implementation(libs.kotlinx.serialization.json)`, not `api(...)`** —
  fine while both consumers redeclare it themselves. Left alone deliberately, still.
- Both DAOs minimal; nothing wired to Room; `AppDatabase` never opened at runtime.
- **New-task capture unreachable** — `AddOrEditTaskScreen(editingTask = tasks[0])` default, **+**
  always opens "Edit Task" on task[0].
- **`ForgeDatePickerDialog` NPEs** on OK-without-selection (`selectedDateMillis!!`).
- Save hardcoded (`derivedStateOf { true }`); date/time picker selections not plumbed back.
- **`AddTaskSheet.kt` (~24KB) still orphaned** — safe to delete.
- **Task List bucket-overlap crash still live** (`overdue && due == null` → duplicate `LazyColumn`
  key). Fix: `someday = active.filter { it.due == null && !it.overdue }`.
- Server `TaskDto` still exposes `due: String?` only vs. the local typed `Instant?`.
- Stats hardcoded; `Motion.kt` empty; `SuccessScreen` reset button not deleted.
- **`ForgeTextField` focus ring still an empty `drawBehind { }` stub — 29th note running.**
- Server-side: dev JWT secret in source, `/auth/forgot` logs the reset token to console, CORS
  `anyHost()`, `PUT /tasks/{id}` is a full replace not a merge.
- No Room schema export dir; `gradle.properties` has dead `kapt.*` and a deprecated
  `useClasspathSnapshot` warning.
- **Environment**: memory-pressure symptoms (Gradle daemon SIGSEGV, `forge-mongo` restart-looping)
  from earlier notes — not rechecked this round, no new data point either way.
- `.idea/` still uncommitted, and this round's diff is identical to last round's — same
  bytecode/language level 17→11, same `gradle.xml` `:shared` addition, same
  `deploymentTargetSelector.xml` run-config entry. These have simply been sitting uncommitted,
  unchanged, since before the last note. Standing "your call" item, untouched again.

## Verified

`./gradlew :app:compileDebugKotlin --rerun-tasks` (forced, not cached) → BUILD SUCCESSFUL. One
warning, not an error, same as last note: `ForgeNavHost.kt:123:21` — `'when' is exhaustive so
'else' is redundant here`. Compile-only — neither Registration bug above is catchable by the
compiler, and the network paths weren't exercised against a live server this round.
