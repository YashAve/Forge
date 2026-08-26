# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-08-26 · **On top of:** `d64dab8`

`./gradlew :app:compileDebugKotlin :server:compileKotlin :shared:compileKotlin --rerun-tasks` →
**BUILD SUCCESSFUL in 20s**. Two commits this round on top of your `ff279fb`: `f3628aa` (the
forgot/reset flow) and `d64dab8` (its wiring). Verified, committed, documented — I didn't change
any of it.

Last note flagged three breaks in the reset flow. **Two are fixed. One is not, and it is now the
only thing between this flow and working end to end.**

## 🔴 `ResetPasswordScreen` is still unwired — and now users can reach it

`ForgeNavHost.kt:369–374` still calls it with only `modifier` and `onResetPassword`:

```kotlin
entry<ResetPassword> {
    ResetPasswordScreen(
        modifier = basicModifier,
        onResetPassword = { authViewModel.onIntent(AuthIntent.SubmitResetPassword) }
    )
}
```

The screen takes `state`, `onUpdateResetToken`, `onUpdatePassword` and `onUpdateConfirmPassword`.
All four have defaults, so this compiles. At runtime the screen renders against a fresh
`AuthState()`, every field's `onValueChange` is the no-op `{ }`, and `isEnabled` — derived from that
permanently-empty state — is `false` forever. **Typing does nothing; the button can never be
pressed.**

This was already true last round. What changed is that it used to be unreachable, so it was
invisible; `d64dab8` added the "Enter token" route, so a user can now walk into a screen that
cannot accept input and cannot be left except by going back. **Reachable-and-dead is worse than
unreachable-and-dead.**

The fix is the four lines the `ForgotPassword` entry twenty lines above now has:

```kotlin
state = authState.value,
onUpdateResetToken = { authViewModel.onIntent(AuthIntent.UpdateResetToken(it)) },
onUpdatePassword = { authViewModel.onIntent(AuthIntent.UpdatePassword(it)) },
onUpdateConfirmPassword = { authViewModel.onIntent(AuthIntent.UpdateConfirmPassword(it)) },
```

With those four lines the whole flow closes: forgot → token in the server console → email-sent →
enter token → reset → login. Everything else on that path is now wired.

## ✅ Fixed this round

- **`ForgotPasswordScreen` no longer sends the wrong email.** It dropped its local `emailInput` for
  the hoisted state and dispatches `UpdateEmail`, so the address that gets a reset token is the one
  typed on that screen — not whatever Login last left in the shared ViewModel. `INVALID_EMAIL` now
  renders on the field via `emailMessage()` instead of silently disabling the button.
- **It also honours its `modifier` now.** The `Column` said `modifier = Modifier`, shadowing the
  `basicModifier` the nav host passes — so the screen was ignoring the shell's insets and padding.
  That one wasn't in my last note; good catch.
- **`ResetPassword` has a route in.** "Open mail app" became "Enter token", with the scope reasoning
  written next to the commented-out original rather than deleted — the right way to record a
  deliberate deviation from `Screens.jsx`.
- **`EmailSentScreen` reads the real email**, and its resend label no longer renders
  `"Resend in 0:0:45"` — the old string interpolated a second `0:` inside the branch.

## ⚠️ New: tapping "Resend link" pushes a duplicate `EmailSent` onto the back stack

`onResend` dispatches `SubmitForgotPassword`. On success the ViewModel emits
`AuthEffect.NavigateToEmailSent`, and the collector at `ForgeNavHost.kt:125–127` does an
unconditional `add(EmailSent)`. There's only one handler and no dedup, so a resend from the
Email Sent screen **navigates to Email Sent again**, on top of itself.

Two consequences:

1. `ForgeNavKey.EmailSent` is a `data object` — a singleton — so the back stack now holds the same
   key twice. Nav3 scopes each entry's saved state by key and expects them to be distinct; duplicate
   identical keys are outside what `NavDisplay` is specified to handle. I can't say from reading
   whether that throws or just confuses state restoration, but it isn't a supported shape.
2. **The cooldown defeats itself.** `isCoolingDown` and `timer` are `rememberSaveable` inside the
   screen. The resend pushes a *new* `EmailSent` entry whose composable starts fresh — timer at 45,
   `isCoolingDown` false — so the button that just started a 45-second cooldown is immediately
   enabled again on the screen now in front of the user.

The effect fires for both entry points into that screen, which is the underlying issue: the
collector can't tell "first request" from "resend". Guarding the `add` on whether `EmailSent` is
already on top is the small fix; a separate effect for resend is the honest one.

## Worth knowing

- **`validEmailRegularExpression` now has zero usages.** It's a public top-level `val` in
  `RegistrationScreen.kt:39` — `ForgotPasswordScreen` was its last caller before this round's
  button changed to `state.email.isNotEmpty()`. Top-level and public, so the compiler won't warn.
  Validation is entirely in `AuthViewModel.emailRegex` now, which is the right place; this is just
  the leftover.
- **The reset-token field's helper still reads "From the reset email."** — second note. It
  contradicts the KDoc six lines above explaining the token comes from the server console. Whoever
  tests this will go looking in an inbox.
- **`EmailSentScreen` says the token expires in 60 minutes.** `ServerConfig.resetTokenValidityMillis`
  is the source of truth and the `/auth/forgot` log line prints the real number — worth a glance to
  confirm they agree, since the string is hardcoded.
- **`submitResetPassword` doesn't check the passwords match** — only the screen's `isEnabled` does.
  So the 8-char rule lives in the ViewModel and the match rule lives in the UI.
- **`safeCall`'s KDoc still claims `expectSuccess = false` is configured.** Fourth note. One line in
  `NetworkModule` makes it true.
- `remember(state.password, state.confirmPassword) { derivedStateOf { … } }` in
  `ResetPasswordScreen` re-creates the derived state on exactly the changes it exists to absorb.
  Harmless, no benefit. `EmailSentScreen`'s `derivedStateOf` is keyless and correct — good contrast
  between the two if you want the rule of thumb in one file each.
- `createGoogleUser` can still throw `email_taken` on `/auth/google`; `invalid_google_token` and
  `id_token_required` are still the only auth codes unmapped on the client.

## Environment

- **`BASE_URL` is `10.37.153.25`** — your LAN, unreachable from this desktop (`172.1.2.197`).
  Untouched deliberately. `local.properties` remains the longest-standing avoidable item here.
- **My Android OAuth client is untouched** — `…2r9g1i1m…`, SHA-1 `CF:C1:4C:98:…:3A:08`. Web client
  `…gshnt5i4…`.
- **`.idea/` is no longer a standing diff** — stashed at `stash@{0}`. Tree is genuinely clean.
- The two `codec-probe@example.com` rows are presumably still in the dev database; no Mongo here.

## Carried over (still open, untouched)

- **The Home back-button bug — sixteenth note running.** Still a product decision, still unmade.
- **The sign-out credential leak** — `ResetFields` not dispatched in the
  `SettingsEffect.NavigateToLogin` handler (`ForgeNavHost.kt:414`). One line. `submitResetPassword`
  and `ForgotPasswordScreen`'s `onRemembered` both now do this correctly; the logout path is the
  last one that doesn't.
- **The orphaned pre-rename token** under `"auth_is_stored"` — unreachable, undeletable plaintext.
- **`userEmail()` has zero call sites** — `SettingsScreen` still shows `karishma.kapoor@cosmiq.co.in`.
  Note `EmailSentScreen` just demonstrated the pattern for reading the real address from state.
- **The release variant still does not compile** — `BASE_URL` is `debug`-only, `NetworkModule` is in
  `src/main`.
- **Zero test files under `server/src`.** Your codec bug remains the argument.
- The 409 `invalid_auth_provider` branch remains unexercised.
- `TaskDao.deleteTask`/`deleteTasks` duplicate `@Delete vararg`, only one `suspend`.
- `core/mvi` base still has zero implementors while four ViewModels hand-roll the same shape.
- The dead `else -> { }` at `ForgeNavHost.kt:134:21` — still the only source warning in `:app`.
- `:shared` still declares `implementation(libs.kotlinx.serialization.json)`, not `api(...)`.
- **The whole task half of the app is still unwired** — no `TaskApi`, no repository, nothing wired
  to Room, `AppDatabase` never opened, though `/tasks` is implemented and JWT-protected.
- **New-task capture unreachable**; `ForgeDatePickerDialog` NPEs on OK-without-selection; save
  hardcoded; `AddTaskSheet.kt` orphaned; Task List bucket-overlap crash.
- Server `TaskDto` exposes `due: String?` vs. the local typed `Instant?`.
- Stats hardcoded; `Motion.kt` empty; `SuccessScreen` reset button not deleted.
- **`ForgeTextField` focus ring still an empty `drawBehind { }` stub — 37th note running.**
- Server-side: dev JWT secret in source, `/auth/forgot` logs the reset token, CORS `anyHost()`,
  `PUT /tasks/{id}` is a full replace not a merge.
- Mongo password sits in `ServerConfig.kt:28`. Private repo, so hygiene not exposure.
- No Room schema export dir; `gradle.properties` has dead `kapt.*` and a deprecated
  `useClasspathSnapshot`.
- **Gradle daemon SIGSEGV = JBR 21 G1 GC fault, not memory pressure.** `-XX:+UseParallelGC` on every
  invocation here too. No crash this round.
- Several auth files still end without a trailing newline.

## Verified

Forced `--rerun-tasks` across `:app`, `:shared`, `:server` → BUILD SUCCESSFUL in 20s. Warnings
unchanged and all pre-existing: `ForgeNavHost.kt:134:21`, two `@BsonId` KT-73255 notices, the Room
schema-export and KSP incremental notices.

**Compile-only. Nothing was run this round** — no server, no device, no endpoint exercised, no Mongo
on this machine. The `ResetPasswordScreen` and duplicate-`EmailSent` findings are read from the call
sites at the line numbers given, not observed at runtime.
