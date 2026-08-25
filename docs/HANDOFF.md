# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-08-25 · **On top of:** `2b19b55`

`./gradlew :app:compileDebugKotlin :server:compileKotlin :shared:compileKotlin --rerun-tasks` →
**BUILD SUCCESSFUL**. This round is Yash's client-side Google auth plumbing, landing against the
`/auth/google` endpoint added last round. I verified, committed, and documented it — didn't finish
or change any of it.

## ⚠️ The Google path is wired at both ends and connected at neither

The server endpoint is real and the client transport is real. Between them, three links are
missing, and none of them is a compile error — the build is clean:

1. **`AuthViewModel.signInWithGoogle(idToken)` has an empty body** (`AuthViewModel.kt:118`). The
   intent dispatches to it, so this *looks* implemented in a way `-> {}` did not. It does nothing.
2. **`AuthRepository.googleAuth()` therefore has zero call sites.** Fully written, reaches
   `AuthApi.googleAuth()`, never invoked by anything.
3. **Nothing in `:app` can produce an ID token.** There is no Google Sign-In / Credential Manager /
   Google Identity Services dependency in `app/build.gradle.kts` at all — the only `google` entry
   is `ui-text-google-fonts`. So `AuthIntent.SignInWithGoogle(idToken)` has no producer: grep finds
   the declaration and the dispatch, and no construction site anywhere.

So the honest status is: transport plumbing complete, no ID token source, no ViewModel body. The
next real step is a dependency decision (Credential Manager is the current-generation answer;
the legacy `GoogleSignInClient` is deprecated), not more wiring.

## 🐛 "Continue with Google" currently submits a password login

`LoginScreen.kt:212–221` — the `ForgeSecondaryButton` carrying the Google icon calls `onLogin()`,
i.e. it fires `AuthIntent.SubmitLogin` with whatever is in the email/password fields. The screen's
KDoc documents an `@param onGoogle`, but the actual signature has no such parameter
(`LoginScreen.kt:112–120`), so there is nothing else it could call.

This predates this round, but it matters now in a way it didn't before: with `/auth/google` live,
the one control a user would press to exercise it does something entirely different. Worth fixing
in the same pass that adds the token source — `onGoogle` needs to exist as a parameter first.

## Resolved / improved this round

- **`AuthRepository` deduplicated.** `login`/`register`/`googleAuth` now share one private
  `safeCall { }` that does the status check, the `ErrorResponse` → `AuthError` mapping and the
  transport-failure catches. Three copies of that block became one; adding the third call would
  otherwise have been a third copy.
- **`invalid_auth_provider` is mapped.** `toAuthError()` now translates it to
  `AuthError.INVALID_AUTH_PROVIDER`, the enum case that has been sitting unused since before the
  server could emit it. String matches the server's `ConflictException` code exactly — checked.
- **`okio.IOException` → `java.io.IOException`.** Same type on JVM (Okio 3 typealiases it), but the
  import no longer routes a core exception through a transitive dependency.
- **`SignInWithGoogle` became a `data class` carrying `idToken`**, which is the right shape — the
  token is per-attempt data, not a bare signal.

## Worth knowing

- **`safeCall`'s KDoc says the client "is configured with `expectSuccess = false`". It isn't
  configured at all** — `NetworkModule.provideHttpClient` never sets `expectSuccess`, and `false`
  is simply Ktor's default. The behaviour is correct today, so this is a wording issue, not a bug.
  It's worth tightening because the consequence is invisible: if anyone ever sets
  `expectSuccess = true`, every non-2xx becomes a thrown `ClientRequestException`, gets caught by
  `safeCall`'s `catch (_: Exception)`, and collapses to `AuthError.UNKNOWN` — silently destroying
  the entire error-code mapping, with no compile error and no test to catch it. Setting it
  explicitly to `false` in `NetworkModule` would make the KDoc true and pin the assumption.
- **The two OAuth client IDs are now committed** (as comments in `AndroidManifest.xml`). Last
  round's note pointed here for the `GOOGLE_WEB_CLIENT_ID` value, so leaving the file uncommitted
  would have made that instruction impossible to follow. OAuth *client IDs* are public identifiers
  — unlike client *secrets* — so this is not a credential in git. The pair is
  `486134999809-2r9g1i1…` and `486134999809-gshnt5i4…`; which is Web and which is Android isn't
  recorded, and the server needs the **Web** one.

## Still open from last round

- **`/auth/google` answers `401 invalid_google_token` for everything until `GOOGLE_WEB_CLIENT_ID`
  is exported.** A startup warning fires when the placeholder is in use — check the boot log first.
  Must be the **Web** client ID; Google mints ID tokens whose `aud` is the web client even on
  Android.
- **The two existing Mongo rows are still unverified against `authProvider`.** `optims@gmail.com`
  and `optimusprime@gmail.com` predate the field, so reading them depends on `bson-kotlin`'s
  `DataClassCodec` applying the Kotlin constructor default for an absent BSON field. **This desktop
  still has no Mongo** — no container, no `~/forge-mongo` — so it remains unprovable here. One
  `POST /auth/login` against either account settles it. If it fails, making `authProvider` nullable
  (`AuthProvider? = null`, read as `PASSWORD`) removes the dependency on default support entirely.
- **The sign-out credential leak — still live.** Nothing clears `_authState` on logout, so the
  post-logout Login screen is still pre-filled with the previous user's email and password
  (revealable via `ForgePasswordField`'s visibility toggle). `AuthIntent.ResetFields` is dispatched
  at `ForgeNavHost.kt:293`/`:309` but **not** in the `SettingsEffect.NavigateToLogin` handler at
  `:373`. One line. Same gap after a successful login — `submitLogin()`/`submitRegistration()`
  leave the plaintext password in state.
- **The orphaned pre-rename token.** `KEY_AUTH_TOKEN` moved `"auth_is_stored"` → `"auth_token"`;
  `deleteAuth()` removes only the new keys, so the JWT under the old name is unreachable plaintext
  no code path — logout included — can delete. Existing installs will appear logged out.
- **`userEmail()` still has zero call sites** — `SettingsScreen` still shows
  `karishma.kapoor@cosmiq.co.in` to every user.
- **The release variant still does not compile** — `BASE_URL` is declared only in the `debug`
  buildType while `NetworkModule` is in `src/main`.
- **`BASE_URL` is `10.46.89.25`** — correct for you, unreachable from this desktop
  (`172.1.2.197`, different subnet). Untouched deliberately; needs the `local.properties` fix.
  `10.0.2.2` is already whitelisted in `network_security_config.xml:5` for the emulator route.

## Carried over (still open, untouched)

- **The Home back-button bug — twelfth note running.** Still a product decision, still unmade.
- **`TaskDao.deleteTask`/`deleteTasks` duplicate `@Delete vararg` signature**, only one `suspend`.
- **`core/mvi` base still has zero implementors** while four ViewModels hand-roll the same shape.
- The dead `else -> { }` in `ForgeNavHost`'s auth-effect collector (`ForgeNavHost.kt:123:21`) —
  still the only source warning in the app module.
- `:shared` still declares `implementation(libs.kotlinx.serialization.json)`, not `api(...)`.
- **The whole task half of the app is still unwired** — no `TaskApi`, no repository, nothing wired
  to Room, `AppDatabase` never opened at runtime, though `/tasks` is implemented and JWT-protected.
- **New-task capture unreachable** (`+` always opens "Edit Task" on task[0]); `ForgeDatePickerDialog`
  NPEs on OK-without-selection; save hardcoded; `AddTaskSheet.kt` (~24KB) orphaned; Task List
  bucket-overlap crash (`overdue && due == null`).
- Server `TaskDto` exposes `due: String?` only vs. the local typed `Instant?`.
- Stats hardcoded; `Motion.kt` empty; `SuccessScreen` reset button not deleted.
- **`ForgeTextField` focus ring still an empty `drawBehind { }` stub — 33rd note running.**
- Server-side: dev JWT secret in source, `/auth/forgot` logs the reset token to console, CORS
  `anyHost()`, `PUT /tasks/{id}` is a full replace not a merge.
- No Room schema export dir; `gradle.properties` has dead `kapt.*` and a deprecated
  `useClasspathSnapshot`.
- **Gradle daemon SIGSEGV = JBR 21 G1 GC fault, not memory pressure** — your diagnosis, also in
  this session's persistent memory. No crash on the desktop this round.
- `.idea/` still uncommitted, diff unchanged. Standing "your call" item.

## Verified

`./gradlew :app:compileDebugKotlin :server:compileKotlin :shared:compileKotlin --rerun-tasks`
(forced, not cached) → BUILD SUCCESSFUL in 22s. Warnings unchanged and all pre-existing: the
`ForgeNavHost.kt:123:21` redundant `else`, two `@BsonId` annotation-target notices (KT-73255), and
the Room schema-export / KSP incremental notices.

Compile-only again. **Nothing was run this round either** — no server, no device, no endpoint
exercised. Every claim above about the Google path being inert is read from the code, not observed
at runtime.
