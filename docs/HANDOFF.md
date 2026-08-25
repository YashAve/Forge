# Handoff note — laptop → desktop

**From:** laptop Claude session · **Date:** 2026-08-25 · **On top of:** `dde50df`

Short round on my side: I pulled your six commits and closed the third of the three gaps your last
note listed — **the missing ID-token source**. That was the one you correctly called "a dependency
decision, not more wiring", so it was mine to make and not Yash's to write. I did not touch his
feature code; the other two gaps are still open exactly as you described them.

## ✅ Credential Manager is now on the classpath (`ad94d07`)

You wrote: *"There is no Google Sign-In / Credential Manager / Google Identity Services dependency
in `app/build.gradle.kts` at all."* Correct, and now there is. Credential Manager, not the
deprecated `GoogleSignInClient`:

| Artifact | Version | Why it's needed |
|---|---|---|
| `androidx.credentials:credentials` | 1.6.0 | The `CredentialManager` API surface |
| `androidx.credentials:credentials-play-services-auth` | 1.6.0 | The provider that actually serves Google accounts |
| `com.google.android.libraries.identity.googleid:googleid` | 1.2.0 | `GetGoogleIdOption` / `GoogleIdTokenCredential` |

**All three are required together** — this is the classic trap. With only `credentials`, the code
compiles, `CredentialManager.getCredential()` is callable, and every single request fails at
runtime with `NoCredentialException`, because nothing is registered to answer it. There is no
compile error and no warning; it looks wired and returns nothing.

Verified on `debugRuntimeClasspath`, not assumed: `credentials:1.6.0`,
`credentials-play-services-auth:1.6.0`, `googleid:1.2.0` (the transitive `googleid:1.1.0` correctly
upgrades to 1.2.0), pulling in `play-services-auth:21.1.1`. `:app:compileDebugKotlin` →
**BUILD SUCCESSFUL**, no new warnings.

`compileSdk` is 36 and `minSdk` 24, both comfortably above what credentials 1.6.0 requires, so no
AGP/SDK bump was needed — unlike the `lifecycle-viewmodel-navigation3` situation still parked in
the catalog comment.

## What is still missing before "Continue with Google" works

Adding the dependency does **not** make the flow work. Four things remain, in order, and none of
them is a compile error:

1. **`AuthViewModel.signInWithGoogle(idToken)` is still an empty body** (`AuthViewModel.kt:118`) —
   your item #1, untouched. Still the most deceptive one in the codebase, because it reads as
   implemented in a way `-> {}` did not.
2. **`AuthRepository.googleAuth()` still has zero call sites** — your item #2, untouched.
3. **Nobody calls `CredentialManager` yet.** The library is present; there is no construction site.
   I added the capability, not the code.
4. **`LoginScreen`'s Google button still calls `onLogin()`** (`LoginScreen.kt:212–221`) — your 🐛.
   `onGoogle` must exist as a parameter before there is anywhere to hang this. Note that the KDoc
   already documents `@param onGoogle` while the signature has no such parameter, so the docs are
   describing an API that was never written.

Whoever does this next: the `GetGoogleIdOption` needs the **Web** client ID — the same one your
note flagged for the server's `GOOGLE_WEB_CLIENT_ID`. Google mints ID tokens whose `aud` is the web
client even on Android, so **both ends must be given the same Web ID**. Your note recorded the pair
(`486134999809-2r9g1i1…` / `486134999809-gshnt5i4…`) but not which is which; that still needs
settling in the Cloud console, and the two failure modes are indistinguishable from the app side —
a wrong ID at the client yields no token, a wrong ID at the server yields `401
invalid_google_token`, and neither says which half is wrong.

## On your `expectSuccess` finding

Good catch, and I agree it's worth pinning. The `catch (_: Exception)` in `safeCall` swallowing a
`ClientRequestException` into `AuthError.UNKNOWN` is exactly the kind of failure that survives
review — the mapping code is all still there and still looks correct, it just never runs. One line
in `NetworkModule`. Left it for Yash rather than editing his file behind him.

## Answered from my side

- **Mongo/`authProvider`.** I have Mongo here and both rows (`optims@gmail.com`,
  `optimusprime@gmail.com`) — they exist, from the 18-call session I ran last round. I did **not**
  re-run the server this time, so the `DataClassCodec` default question is still formally
  unproven. One `POST /auth/login` against `optims@gmail.com` settles it and I can do that next
  round if you want it closed.
- **`BASE_URL` is still `10.46.89.25`** and still wrong for you. Nobody has taken the
  `local.properties` fix, which is now the longest-running avoidable item in these notes.

## Carried over (still open, untouched)

- **The Home back-button bug — thirteenth note running.** Still a product decision, still unmade.
- **The sign-out credential leak** — `ResetFields` not dispatched in the
  `SettingsEffect.NavigateToLogin` handler (`ForgeNavHost.kt:373`). One line.
- **The orphaned pre-rename token** under `"auth_is_stored"` — unreachable, undeletable plaintext.
- **`userEmail()` has zero call sites** — `SettingsScreen` still shows `karishma.kapoor@cosmiq.co.in`.
- **The release variant still does not compile** — `BASE_URL` is `debug`-only, `NetworkModule` is
  in `src/main`.
- **`TaskDao.deleteTask`/`deleteTasks`** duplicate `@Delete vararg` signature, only one `suspend`.
- **`core/mvi` base still has zero implementors** while four ViewModels hand-roll the same shape.
- The dead `else -> { }` at `ForgeNavHost.kt:123:21` — still the only source warning in `:app`.
- `:shared` still declares `implementation(libs.kotlinx.serialization.json)`, not `api(...)`.
- **The whole task half of the app is still unwired** — no `TaskApi`, no repository, nothing wired
  to Room, `AppDatabase` never opened, though `/tasks` is implemented and JWT-protected.
- **New-task capture unreachable** (`+` always opens "Edit Task" on task[0]); `ForgeDatePickerDialog`
  NPEs on OK-without-selection; save hardcoded; `AddTaskSheet.kt` (~24KB) orphaned; Task List
  bucket-overlap crash (`overdue && due == null`).
- Server `TaskDto` exposes `due: String?` only vs. the local typed `Instant?`.
- Stats hardcoded; `Motion.kt` empty; `SuccessScreen` reset button not deleted.
- **`ForgeTextField` focus ring still an empty `drawBehind { }` stub — 34th note running.**
- Server-side: dev JWT secret in source, `/auth/forgot` logs the reset token to console, CORS
  `anyHost()`, `PUT /tasks/{id}` is a full replace not a merge.
- No Room schema export dir; `gradle.properties` has dead `kapt.*` and a deprecated
  `useClasspathSnapshot`.
- **Gradle daemon SIGSEGV = JBR 21 G1 GC fault, not memory pressure.** No crash this round either —
  I still passed `-XX:+UseParallelGC` on every invocation, so it remains worked around rather than
  disproven.
- `.idea/` still uncommitted, diff unchanged. Standing "your call" item.

## Verified

`:app:compileDebugKotlin` → BUILD SUCCESSFUL in 1m18s (20 tasks executed, not cached).
`:app:dependencies --configuration debugRuntimeClasspath` confirms all three credentials artifacts
resolve at the expected versions. Warnings unchanged and all pre-existing.

Compile-and-resolve only. **The server was not started this round and no endpoint was exercised** —
everything above about the Google path being inert is read from the code, not observed at runtime,
same caveat as your note.
