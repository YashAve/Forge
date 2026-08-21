# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-08-21 · **On top of:** `dc58ef7`

`./gradlew :app:compileDebugKotlin --rerun-tasks` → **BUILD SUCCESSFUL**. This round is Yash's own
follow-up fix pass, addressing two of the four issues the last note flagged — I verified, committed,
and documented it, didn't finish or fix any of it myself.

## ⚠️ Still open — the stale BASE_URL blocker from last note is untouched

Nothing touched `NetworkModule.kt` or `app/build.gradle.kts` this round, so this is exactly as the
last note left it: `BASE_URL` is still hardcoded to the laptop's DHCP IP, and it has a real call site
(`install(DefaultRequest) { url(BuildConfig.BASE_URL) }`). Anyone running Login against a local server
on a different machine still needs to update **both** `BASE_URL` and the matching literal in
`network_security_config.xml`. Restating this so it doesn't get lost between the fix pass below and
the actual blocker.

## Resolved this round

- **`LoginScreen`'s `canSubmit`/8-character contradiction — removed, not patched.** Rather than
  picking between the two options the last note offered (keep the 8-char rule and fix the comment, or
  drop the rule), Yash removed client-side gating entirely. `LoginScreen` now takes a real
  `state: AuthState` parameter instead of its own `remember`/`rememberSaveable` locals — `emailInput`,
  `passwordInput`, `isLoading`, `canSubmit` are all gone — and `ForgeNavHost.kt`'s `entry<Login>` feeds
  it from `authViewModel.authState.collectAsStateWithLifecycle()`. The button is now enabled any time
  `!state.isLoading`; validation (email regex, non-empty password) happens where it already lived, in
  `AuthViewModel.submitLogin()`, and failures come back as `state.error` instead of a disabled button.
  New `AuthError?.emailMessage()` / `.passwordMessage()` extensions surface
  `INVALID_EMAIL`/`EMPTY_PASSWORD`/`INVALID_CREDENTIAL`/`NETWORK_ERROR` under the relevant field via
  `ForgeTextField`/`ForgePasswordField`'s existing `error` param.
- **`AuthEffect.NavigateToRegistration` — deleted, not wired up.** Closes the
  unreachable-from-both-ends gap the last note flagged. `AuthViewModel.updateEffect()` is still there
  with zero call sites (it wasn't the mechanism used for anything this round), so it's carried below
  rather than marked resolved.

## Worth knowing — three small things this fix pass surfaced

- **The nav host's auth-effect `when` now has a live, named compiler warning.** With
  `NavigateToRegistration` gone, all four remaining `AuthEffect` cases are listed explicitly in
  `ForgeNavHost.kt`'s effect collector, so the build now emits
  `'when' is exhaustive so 'else' is redundant here` pointing at line 116. The `else -> { }` at lines
  116–118 can just be deleted — leaving it doesn't break anything, but the compiler is now telling you
  it's dead.
- **`LoginScreen`'s design-spec comment (lines 71–74) describes a mechanism that no longer exists.** It
  still reads "canSubmit = email matches the regex AND password is NOT empty AND not loading" — but
  there's no `canSubmit` anywhere in the file now. The rule itself is still accurate (that's exactly
  what `submitLogin()` checks), it's just enforced at submit time in the ViewModel instead of gating
  the button beforehand. Not a bug, just a comment describing the old architecture — worth a rewrite
  next time someone's in this file.
- **An unrecognized login failure is now silent, not just unhandled.** Neither `emailMessage()` nor
  `passwordMessage()` has a case for `AuthError.UNKNOWN` — so if the server returns a code neither
  function maps, or `AuthRepository.login()`'s catch-all `catch (exception: Exception)` fires,
  `isLoading` goes back to `false` and the button becomes tappable again with **zero visible feedback**
  that anything failed. Grepped both branches that produce it (`AuthRepository.kt:35` and `:45`) — it's
  a real, reachable path from a login attempt, not a hypothetical. `WEAK_PASSWORD`/
  `EMAIL_ALREADY_EXISTS`/`INVALID_AUTH_PROVIDER` are also unmapped here, but those look like
  registration-only server responses a login call shouldn't realistically produce — `UNKNOWN` is the
  one that can actually happen on this screen today.

## Carried over (still open, untouched)

- **The Home back-button bug — seventh note running.** Still a product decision, still unmade.
- **`TaskDao.deleteTask`/`deleteTasks` duplicate `@Delete vararg` signature**, only one `suspend`.
  Main-thread-crash trap for the repository layer. Untouched.
- **Splash still always routes to Onboarding**, token or no token — `SplashViewModel.checkAuthStatus()`
  checks `!onboardingDone` before ever looking at the auth token, and `setOnboardingDone` still has
  zero call sites. Re-verified this round (`SplashViewModel.kt` is byte-for-byte what the last note
  quoted); full reasoning is in that note's history, not repeated here.
- **`isAuthTokenStored()` returns `Flow<String>`** (the token), not a `Boolean`, despite the `is…` name
  — real tokens now sit in plain unencrypted DataStore on any machine that's logged in once.
- **`core/mvi` base still has zero implementors.** `AuthViewModel` and `SplashViewModel` both still
  hand-roll the same shape instead of extending it. Worth actually deciding, not re-flagging again.
- **`:shared` still declares `implementation(libs.kotlinx.serialization.json)`, not `api(...)`** — fine
  while both consumers redeclare it themselves. Left alone deliberately, still.
- **`AuthViewModel.updateEffect()` still has zero call sites.** Was the plausible mechanism for
  `NavigateToRegistration`; that effect is gone now, so this method has no clear purpose left — worth
  checking whether it's still needed for anything before it goes stale.
- Both DAOs minimal; nothing wired to Room; `AppDatabase` never opened at runtime.
- **New-task capture unreachable** — `AddOrEditTaskScreen(editingTask = tasks[0])` default, **+** always
  opens "Edit Task" on task[0].
- **`ForgeDatePickerDialog` NPEs** on OK-without-selection (`selectedDateMillis!!`).
- Save hardcoded (`derivedStateOf { true }`); date/time picker selections not plumbed back.
- **`AddTaskSheet.kt` (~24KB) still orphaned** — safe to delete.
- **Task List bucket-overlap crash still live** (`overdue && due == null` → duplicate `LazyColumn`
  key). Fix: `someday = active.filter { it.due == null && !it.overdue }`.
- Server `TaskDto` still exposes `due: String?` only vs. the local typed `Instant?`.
- Stats hardcoded; onboarding finish/skip → `Login` vs spec's `Registration`; `Motion.kt` empty;
  `SuccessScreen` reset button not deleted.
- **`ForgeTextField` focus ring still an empty `drawBehind { }` stub — 28th note running.**
- Server-side: dev JWT secret in source, `/auth/forgot` logs the reset token to console, CORS
  `anyHost()`, `PUT /tasks/{id}` is a full replace not a merge.
- No Room schema export dir; `gradle.properties` has dead `kapt.*` and a deprecated
  `useClasspathSnapshot` warning.
- **Environment**: memory-pressure symptoms (Gradle daemon SIGSEGV, `forge-mongo` restart-looping)
  from earlier notes — not rechecked this round, no new data point either way.
- `.idea/` still uncommitted (this round: `compiler.xml`/`misc.xml` bytecode/language level 17→11,
  `gradle.xml` adds `:shared` to linked Gradle projects, `deploymentTargetSelector.xml` gains a
  run-config entry). Standing "your call" item, untouched again.

## Verified

`./gradlew :app:compileDebugKotlin --rerun-tasks` (forced, not cached) → BUILD SUCCESSFUL. One new
warning, not an error: `ForgeNavHost.kt:116:21` — `'when' is exhaustive so 'else' is redundant here`
(see "Worth knowing" above). Compile-only — Login's network path wasn't exercised against a live
server this round.
