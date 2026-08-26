# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-08-26 · **On top of:** `f3628aa`

`./gradlew :app:compileDebugKotlin :server:compileKotlin :shared:compileKotlin --rerun-tasks` →
**BUILD SUCCESSFUL**. Your `ff279fb` pulled clean. This round is Yash's forgot/reset password
flow — verified, committed, documented. I didn't finish or change any of it.

First: **your codec finding was the good kind of correction.** My last note called the nullable
`authProvider` a *fallback* to try *if* `DataClassCodec` turned out not to apply constructor
defaults. It never applies them, for any field, ever — so that wasn't a fallback, it was the only
correct shape, and I had shipped the broken one. I verified the fix is complete rather than merely
applied: all three read sites (`AuthRoutes.kt:102`, `:192`) go through `.provider`, both write
sites are explicit, and `provider` is top-level so the codec can't see it. The warning block in
`Documents.kt` is the right place for it.

## 🔴 The reset flow is fully implemented and cannot be reached or used

Three independent breaks, none a compile error. The build is clean and every one of these is dead
at runtime.

**1. `ResetPasswordScreen` gets none of its new inputs** (`ForgeNavHost.kt:356–360`). The screen was
converted from local `rememberSaveable` state to hoisted state and gained four new parameters. The
call site still passes only `modifier` and `onResetPassword`. Every new parameter has a default, so
this compiles — and at runtime the screen renders against a fresh `AuthState()`, every field's
`onValueChange` is the default no-op `{ }`, and `isEnabled` (computed from that permanently-empty
state) is `false` forever. **Typing does nothing and the button can never be pressed.** The four
missing lines are the same shape as the `Login`/`Registration` entries directly above it.

**2. Nothing ever navigates to `ResetPassword`.** There is no `add(ResetPassword)` anywhere in
`ForgeNavHost.kt` — the `entry<ResetPassword>` block has no route in. `EmailSent` is where the flow
stops: `EmailSentScreen(basicModifier)` is called with no callbacks at all, so its "Open email" and
"Resend" buttons are inert defaults too. So even fully wired, the screen is unreachable.

**3. `ForgotPasswordScreen` sends the wrong email address.** This is the one that will look like a
server bug. The screen still keeps its own `emailInput` in `rememberSaveable` (`:68`) and never
dispatches `UpdateEmail`. But `onSendLink` fires `SubmitForgotPassword`, and the ViewModel reads
`_authState.value.email` — which holds **whatever was last typed on the Login screen**, because
`LoginScreen` does dispatch `UpdateEmail` into the same nav-host-scoped ViewModel. So the address
the user types on the Forgot screen is validated by the button's `isEnabled`, then thrown away, and
a reset token is generated for a different account. Arriving with an empty Login field instead
yields `INVALID_EMAIL`, which the screen has no `state` parameter to render — the button just
appears to do nothing. Its local `isLoading` is likewise never driven by the real request.

The pattern in all three: **a screen migrated to hoisted state while its call site kept the old
signature.** Kotlin default arguments make that silent every time. It's the same failure as
`signInWithGoogle()`'s empty body two notes ago, one layer up.

## What's genuinely good in this round

- **`safeCall` became `private suspend inline fun <reified T>`.** That's the right generalisation:
  `/auth/forgot` and `/auth/reset` return `MessageResponse`, not `AuthResponse`, and the reified `T`
  lets `response.body()` resolve per call site without a second copy of the status/error/catch
  block. The helper now covers five endpoints.
- **The error map is complete.** `token_required`, `invalid_token`, `expired_token` — I checked all
  three against the server strings (`AuthRoutes.kt:142`, `:147`, `:151`) and every code
  `Validation.kt` can throw. The only auth codes still falling to `AuthError.UNKNOWN` are
  `invalid_google_token` and `id_token_required`.
- **The reset-token field is an honest design change, documented as one.** The screen's KDoc says
  plainly that it deviates from `Screens.jsx` because email delivery is out of scope. That beats
  building a screen that waits for an email nothing sends.
- **`error?.let { … return }` → `if (error != null)`** in `submitRegistration`. The old form worked
  (non-local return through an inline lambda) but nested `it` inside `it`.
- **Login/Registration buttons now gate on non-empty fields**, not just `!isLoading`.

## Worth knowing

- **The reset-token field's helper text reads "From the reset email."** — directly contradicting the
  KDoc six lines above it, which explains there is no email and the token comes from the server
  console. Whoever tests this will go looking in an inbox. One string.
- **`submitResetPassword` doesn't check that the passwords match.** Only `ResetPasswordScreen`'s
  `isEnabled` enforces it. Consistent with how this codebase gates elsewhere, but it means the
  match rule lives only in the UI while the 8-char rule lives in the ViewModel — worth knowing if
  the screen is ever driven from anywhere else.
- **`remember(state.password, state.confirmPassword) { derivedStateOf { … } }`** in
  `ResetPasswordScreen` re-creates the derived state whenever the values it derives from change,
  which is precisely what `derivedStateOf` exists to avoid. Harmless — just no benefit over
  computing directly, which is what the neighbouring `isEnabled` already does.
- **`safeCall`'s KDoc still claims `expectSuccess = false` is configured.** Third note. One line in
  `NetworkModule` makes it true and pins the assumption.
- **`createGoogleUser` can still throw `email_taken` on `/auth/google`** — your finding, unchanged.
  The right answer there is to re-run `findByEmail` and sign in.
- Several auth files still end without a trailing newline (`AuthApi.kt`, `AuthRepository.kt`,
  `AuthViewModel.kt`, `ResetPasswordScreen.kt`).

## Environment

- **`BASE_URL` is `10.37.153.25`** — your LAN, unreachable from this desktop (`172.1.2.197`).
  Untouched deliberately. Now the sixth value in three days; `local.properties` remains the longest
  standing avoidable item in these notes.
- **My Android OAuth client is untouched**, per your warning — `…2r9g1i1m…` with SHA-1
  `CF:C1:4C:98:…:3A:08`. Confirmed I did not edit it. Web client `…gshnt5i4…` recorded.
- **`.idea/` is no longer a standing diff** — Yash stashed it this session (`stash@{0}`). The
  working tree is now genuinely clean. Eight stashes deep in total, oldest from July.
- **The two codec-probe rows** (`codec-probe@example.com`, `codec-probe2@example.com`) are still in
  the dev database as far as this machine knows — no Mongo here to check or delete.

## Carried over (still open, untouched)

- **The Home back-button bug — fifteenth note running.** Still a product decision, still unmade.
- **The sign-out credential leak** — `ResetFields` not dispatched in the
  `SettingsEffect.NavigateToLogin` handler (`ForgeNavHost.kt:401`). One line. Note that
  `submitResetPassword` *does* call `resetFields()` before navigating — the correct pattern now
  exists in the file, three hundred lines from where it's missing.
- **The orphaned pre-rename token** under `"auth_is_stored"` — unreachable, undeletable plaintext.
- **`userEmail()` has zero call sites** — `SettingsScreen` still shows `karishma.kapoor@cosmiq.co.in`.
- **The release variant still does not compile** — `BASE_URL` is `debug`-only, `NetworkModule` is in
  `src/main`.
- **Zero test files under `server/src`.** Your codec bug is still the argument.
- The 409 `invalid_auth_provider` branch remains unexercised.
- `TaskDao.deleteTask`/`deleteTasks` duplicate `@Delete vararg`, only one `suspend`.
- `core/mvi` base still has zero implementors while four ViewModels hand-roll the same shape.
- The dead `else -> { }` at `ForgeNavHost.kt:133:21` — still the only source warning in `:app`.
- `:shared` still declares `implementation(libs.kotlinx.serialization.json)`, not `api(...)`.
- **The whole task half of the app is still unwired** — no `TaskApi`, no repository, nothing wired
  to Room, `AppDatabase` never opened, though `/tasks` is implemented and JWT-protected.
- **New-task capture unreachable**; `ForgeDatePickerDialog` NPEs on OK-without-selection; save
  hardcoded; `AddTaskSheet.kt` orphaned; Task List bucket-overlap crash.
- Server `TaskDto` exposes `due: String?` vs. the local typed `Instant?`.
- Stats hardcoded; `Motion.kt` empty; `SuccessScreen` reset button not deleted.
- **`ForgeTextField` focus ring still an empty `drawBehind { }` stub — 36th note running.**
- Server-side: dev JWT secret in source, `/auth/forgot` logs the reset token, CORS `anyHost()`,
  `PUT /tasks/{id}` is a full replace not a merge.
- Mongo password sits in `ServerConfig.kt:28`. Private repo, so hygiene not exposure.
- No Room schema export dir; `gradle.properties` has dead `kapt.*` and a deprecated
  `useClasspathSnapshot`.
- **Gradle daemon SIGSEGV = JBR 21 G1 GC fault, not memory pressure.** Worked around with
  `-XX:+UseParallelGC` on every invocation here too. No crash this round.

## Verified

Forced `--rerun-tasks` across `:app`, `:shared`, `:server` → BUILD SUCCESSFUL in 18s. Warnings
unchanged and all pre-existing: `ForgeNavHost.kt:133:21`, two `@BsonId` KT-73255 notices, the Room
schema-export and KSP incremental notices.

**Compile-only. Nothing was run this round** — no server, no device, no endpoint exercised. There is
no Mongo on this desktop. Every claim above about the reset flow being dead is read from the call
sites, not observed at runtime; the three breaks are each a missing argument or a missing
`add(...)`, so they're checkable by eye at the line numbers given.
