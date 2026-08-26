# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-08-26 · **On top of:** `af72c08`

`./gradlew :app:compileDebugKotlin :server:compileKotlin :shared:compileKotlin --rerun-tasks` →
**BUILD SUCCESSFUL in 7s**. Three commits this round on top of your `ff279fb`: `f3628aa` (the
forgot/reset flow), `d64dab8` (its navigation), `af72c08` (the last four lines of wiring).

**The reset flow is now closed end to end** — forgot → token from the server console → email-sent →
enter token → reset → login. Every link in that chain is wired. It has never been run.

## ⚠️ The reset screen inherits the password typed on Login

Now that the flow is reachable, this is the first thing a real run will hit.

`AuthState.password` is shared by Login, Registration and Reset, and the
Login → Forgot transition is the one transition that **doesn't** clear it:

```kotlin
onForgotPassword = { backStack.add(ForgotPassword) },   // ForgeNavHost.kt:319
```

Compare `:321` (Login → Registration), `:338` (Registration → Login) and `:353`
(Forgot → back to Login) — all three dispatch `AuthIntent.ResetFields` first.

So the real path is: user types their email **and a password** on Login, it fails, they tap "Forgot
password" — and three screens later `ResetPasswordScreen`'s "New password" field is pre-filled with
that failed password. It's a `ForgePasswordField`, so it renders as dots and nothing indicates it
holds anything. What follows:

- Typing into it **appends** to the invisible existing text, so the new password silently becomes
  `oldpasswordnewpassword`.
- Or they type a fresh password into "Confirm password" and get **"Passwords don't match."** against
  a field whose contents they can't see and never entered.
- Worst case, they retype the old password to make the error go away, the fields match, the button
  enables, and they reset their password **to the one they just failed to log in with.**

Every one of those looks like a broken screen rather than stale state, which is what makes it worth
fixing before the first device run rather than after.

**The fix is a decision, not a line.** Dispatching `ResetFields` at `:319` matches the other three
transitions exactly — but it also clears `email`, and carrying the email forward is the one piece
of state sharing here that genuinely helps: it pre-fills the Forgot screen with the address they
just typed. Clearing only the password needs a new intent (`ClearPassword`, or a `resetToken`/
`password`-scoped variant); giving Reset its own field in `AuthState` costs one more field to
remember in `resetFields()`. I've left it for Yash rather than pick.

Worth noting the same shared-`password` question is what makes the sign-out leak below a leak.

## ✅ Closed this round

- **`ResetPasswordScreen` is wired** (`af72c08`). It was rendering against a default `AuthState()`
  with no-op callbacks — typing did nothing, the button was permanently disabled — while `d64dab8`
  had just given users a route to it. Four lines, matching the `ForgotPassword` entry above it.
- **`ForgotPasswordScreen` sends the right email.** Local `emailInput` dropped for hoisted state
  plus `UpdateEmail`, so the address receiving a token is the one typed on that screen rather than
  whatever Login left behind. `INVALID_EMAIL` renders on the field now instead of silently
  disabling the button.
- **It honours its `modifier`** — the `Column` said `modifier = Modifier`, dropping the nav host's
  `basicModifier` and with it the shell's insets. Not something I'd flagged.
- **`EmailSent` → `ResetPassword` exists**, replacing the design's "Open mail app" with "Enter
  token", with the scope reasoning kept beside the commented-out original.
- **The resend label** no longer renders `"Resend in 0:0:45"`.
- **`safeCall` is `inline fun <reified T>`**, so `MessageResponse` and `AuthResponse` share one
  status/error/catch path across five endpoints.
- **The client error map is complete** for every code the auth routes emit except
  `invalid_google_token` and `id_token_required` — I checked each string against `AuthRoutes.kt`
  and `Validation.kt`.

## ⚠️ Still open: "Resend link" pushes a duplicate `EmailSent`

Unchanged from my last note. `onResend` dispatches `SubmitForgotPassword`; on success the ViewModel
emits `NavigateToEmailSent`; the collector at `ForgeNavHost.kt:125–127` does an unconditional
`add(EmailSent)`. So a resend *from* Email Sent navigates to Email Sent again, on top of itself.

- `ForgeNavKey.EmailSent` is a `data object`, so the same key is now in the back stack twice. Nav3
  scopes saved state per key and expects them distinct; I can't tell from reading whether that
  throws or just confuses restoration, but it isn't a supported shape.
- **The cooldown defeats itself**: `isCoolingDown`/`timer` are `rememberSaveable` inside the screen,
  so the new entry starts fresh — the button that just began a 45-second cooldown is enabled again
  on the screen now in front of the user.

Root cause is effect identity: `NavigateToEmailSent` means both "requested" and "re-requested", and
the collector can't distinguish them. Guarding the `add` on what's already on top patches it; a
separate effect for resend is the honest fix.

## Worth knowing

- **`validEmailRegularExpression` has zero usages** — public top-level `val` at
  `RegistrationScreen.kt:39`; `ForgotPasswordScreen` was its last caller. Public and top-level, so
  no compiler warning. Validation now lives entirely in `AuthViewModel.emailRegex`, which is right.
- **The reset-token field's helper still reads "From the reset email."** — third note. The KDoc six
  lines above explains the token comes from the server console. Whoever tests this goes to an inbox.
- **`EmailSentScreen` hardcodes "The token expires in 60 minutes."** `ServerConfig.resetTokenValidityMillis`
  is the source of truth and `/auth/forgot` logs the real figure — worth confirming they agree.
- **`submitResetPassword` doesn't check the passwords match** — only the screen's `isEnabled` does,
  so the 8-char rule is in the ViewModel and the match rule is in the UI.
- **`safeCall`'s KDoc still claims `expectSuccess = false` is configured.** Fifth note. One line in
  `NetworkModule` makes it true and pins the assumption.
- `remember(state.password, state.confirmPassword) { derivedStateOf { … } }` in
  `ResetPasswordScreen` re-creates the derived state on exactly the changes it exists to absorb.
  `EmailSentScreen`'s keyless `derivedStateOf` is the correct counterpart — one of each in the same
  feature if you want the contrast.
- `createGoogleUser` can still throw `email_taken` on `/auth/google`, where the right answer is to
  re-run `findByEmail` and sign in.

## Environment

- **`BASE_URL` is `10.37.153.25`** — your LAN, unreachable from this desktop (`172.1.2.197`).
  Untouched deliberately. `local.properties` is still the longest-standing avoidable item here.
- **My Android OAuth client is untouched** — `…2r9g1i1m…`, SHA-1 `CF:C1:4C:98:…:3A:08`. Web client
  `…gshnt5i4…`.
- **`.idea/` is no longer a standing diff** — stashed at `stash@{0}`. Tree genuinely clean.
- The two `codec-probe@example.com` rows are presumably still in the dev database; no Mongo here.

## Carried over (still open, untouched)

- **The Home back-button bug — seventeenth note running.** Still a product decision, still unmade.
- **The sign-out credential leak** — `ResetFields` still not dispatched in the
  `SettingsEffect.NavigateToLogin` handler (`ForgeNavHost.kt:414`), which only does
  `clear()` + `add(Login)`. Four transitions in this file now clear auth state correctly; logout is
  the last one that doesn't, and it's the one where it matters most.
- **The orphaned pre-rename token** under `"auth_is_stored"` — unreachable, undeletable plaintext.
- **`userEmail()` has zero call sites** — `SettingsScreen` still shows `karishma.kapoor@cosmiq.co.in`.
  `EmailSentScreen` now demonstrates the pattern for reading the real address from state.
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
- **`ForgeTextField` focus ring still an empty `drawBehind { }` stub — 38th note running.**
- Server-side: dev JWT secret in source, `/auth/forgot` logs the reset token, CORS `anyHost()`,
  `PUT /tasks/{id}` is a full replace not a merge.
- Mongo password sits in `ServerConfig.kt:28`. Private repo, so hygiene not exposure.
- No Room schema export dir; `gradle.properties` has dead `kapt.*` and a deprecated
  `useClasspathSnapshot`.
- **Gradle daemon SIGSEGV = JBR 21 G1 GC fault, not memory pressure.** `-XX:+UseParallelGC` on every
  invocation here too. No crash this round.
- Several auth files still end without a trailing newline.

## Verified

Forced `--rerun-tasks` across `:app`, `:shared`, `:server` → BUILD SUCCESSFUL in 7s. Warnings
unchanged and all pre-existing: `ForgeNavHost.kt:134:21`, two `@BsonId` KT-73255 notices, the Room
schema-export and KSP incremental notices.

**Compile-only. Nothing was run this round** — no server, no device, no endpoint exercised, no Mongo
on this machine. "The reset flow is closed end to end" means every call site and effect handler on
that path is wired, read at the line numbers given. **Nobody has walked it.** You have the device
and the server; one pass from Login through to a changed password is the thing this feature has
never had.
