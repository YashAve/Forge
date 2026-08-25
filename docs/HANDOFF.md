# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-08-25 · **On top of:** `ebbbc5f`

`./gradlew :app:compileDebugKotlin --rerun-tasks` → **BUILD SUCCESSFUL**, one pre-existing warning.
Your note landed and was read in full. This round is Yash's own session-data pass — the token and
user email are now stored together, `AuthIntent.ResetFields` exists, and Registration got a sign-in
footer. I verified, committed, and documented it; I didn't finish or change any of his code.

## ⚠️ The sign-out credential leak is still live — and it is now one line from fixed

Flagged by a security review of `741d793`, still open. `AuthViewModel` is hoisted at nav-host level
(`ForgeNavHost.kt:96`), so it is activity-scoped and survives `backStack.clear()`. Nothing clears
`_authState` on logout, so **the Login screen you land on after signing out is pre-filled with the
previous user's email and password** — and `ForgePasswordField` has a reveal toggle, so the next
person holding the phone can read it in plaintext.

What changed this round is that the mechanism now exists. `ResetFields` is dispatched at exactly
two sites — `ForgeNavHost.kt:293` (Login → Registration) and `:309` (Registration → Login) — and
**not** in the `SettingsEffect.NavigateToLogin` handler at `:373`, which still only does
`backStack.clear(); backStack.add(Login)`. Adding `authViewModel.onIntent(AuthIntent.ResetFields)`
there closes it. I left it alone: whether logout should reach into the auth ViewModel or whether
`SettingsViewModel` should own that is a design call, not a mechanical one.

Related and still open: **the password is never cleared after a *successful* login either.**
`submitLogin()` (`:71`) and `submitRegistration()` (`:105`) both do `it.copy(isLoading = false)` on
success, leaving the plaintext password in the `StateFlow` for the rest of the app session.
`ResetFields` would do it; it just isn't called there.

## ⚠️ The DataStore key rename orphans the old token permanently

`KEY_AUTH_TOKEN` changed from `stringPreferencesKey("auth_is_stored")` to
`stringPreferencesKey("auth_token")`. Two consequences, neither visible at compile time:

1. **Every existing install silently logs out.** Splash reads `authToken()`, finds the new key
   unset, gets `""`, and routes to Login. Harmless in dev — but it means **the `optims@gmail.com`
   session you established on the laptop yesterday is gone**, and a re-login is needed before any
   authed testing. Worth knowing before you conclude something regressed.
2. **The old JWT is now unreachable garbage that nothing will ever delete.** `deleteAuth()` removes
   `"auth_token"` and `"user_email"` only. The real token written under `"auth_is_stored"` during
   yesterday's 18-call session is still sitting in plaintext in
   `forge_preferences.preferences_pb`, and no code path in the app can now reach it — logout
   included. Clearing app storage is the only way to shift it.

Not an argument against the rename — the old name was one of the longest-carried complaints in
these notes. Just: a one-shot `it.remove(stringPreferencesKey("auth_is_stored"))` inside
`deleteAuth()` would sweep it, or clear app data on both machines and move on.

## Resolved this round

- **`isAuthTokenStored()` is finally gone.** The `is…`-prefixed method that returned
  `Flow<String>` — flagged in every note for many rounds — is now `authToken()`, with
  `userEmail()` beside it. The key name was corrected to match (`"auth_is_stored"` → `"auth_token"`);
  see the caveat above.
- **The user's real email is now persisted.** `storeAuth(token, email)` writes both in a single
  `edit { }` (one file rewrite, and they can't drift apart), and `deleteAuth()` removes both
  together. Both `submitLogin()` and `submitRegistration()` now pass `response.data.user.email`.
- **`ForgePreferences` cleaned up** — reader/writer sections, the stale "currently holds only the
  theme flag" KDoc rewritten, and a self-import of its own companion constant removed.
- **Registration ↔ Login no longer carries credentials across.** Both cross-navigation footers now
  dispatch `ResetFields` first, so a half-typed password doesn't follow you to the other screen.
- **Registration gained an "Already forged one? / Sign in" footer**, mirroring Login's "New here?"
  row. Previously Registration was a one-way door unless you used the system back gesture.

## Worth knowing

- **`userEmail()` has zero call sites.** The plumbing for the hardcoded-email bug is now fully in
  place — email is stored on both auth paths and readable — but nothing consumes it yet, so
  `SettingsScreen` still shows `karishma.kapoor@cosmiq.co.in` to every user. This is the shortest
  path it has ever been from fixed: read it in `SettingsViewModel`, put it in `SettingsState`, pass
  it to the screen's existing `email` param.
- **`BASE_URL` is untouched at `10.46.89.25` — correct for you, wrong here.** This desktop's LAN
  address is `172.1.2.197`, a different subnet, so the desktop can't reach that host. Not changed,
  because flipping it would just break it on your side; this is the per-machine problem your note
  described, and it needs the `local.properties` fix rather than another edit. One useful detail
  your note didn't spell out: **`10.0.2.2` is already whitelisted** in
  `network_security_config.xml:5`, so the emulator route is a one-line `BASE_URL` change with no
  security-config edit. Caveat for the desktop's Dockerized emulator — `10.0.2.2` resolves to the
  *container's* host, so it only reaches a desktop-hosted server if that container uses host
  networking.
- The `AuthState.error` reset behaviour is worth a thought before Google sign-in lands: both
  `updateEmail` and `updatePassword` clear `error` on every keystroke, so a field-level message
  disappears as soon as the user starts fixing it. That's good UX and clearly deliberate — noting
  it only because `resetFields()` now also clears `isLoading`, which means a `ResetFields` fired
  mid-request would unblock the button while the coroutine is still in flight. No call site does
  that today (both are user-initiated navigation), but it's a live edge if `ResetFields` ever gets
  wired somewhere automatic.

## Carried over (still open, untouched)

- **The Home back-button bug — tenth note running.** Still a product decision, still unmade.
- **The release variant still does not compile** — `BASE_URL` is declared only in the `debug`
  buildType while `NetworkModule` lives in `src/main`. Untouched, per your note: what release
  should point at is a product decision.
- **`TaskDao.deleteTask`/`deleteTasks` duplicate `@Delete vararg` signature**, only one `suspend`.
- **`core/mvi` base still has zero implementors** while four ViewModels hand-roll the same shape.
- The dead `else -> { }` in `ForgeNavHost`'s auth-effect collector — still the only source warning
  in the app module (`ForgeNavHost.kt:123:21`).
- `:shared` still declares `implementation(libs.kotlinx.serialization.json)`, not `api(...)`.
- **The whole task half of the app is still unwired** — auth works end to end, tasks have no client
  at all (no `TaskApi`, no repository), though `/tasks` is implemented and JWT-protected
  server-side. Nothing wired to Room; `AppDatabase` never opened at runtime.
- **New-task capture unreachable** (`+` always opens "Edit Task" on task[0]); `ForgeDatePickerDialog`
  NPEs on OK-without-selection; save hardcoded; `AddTaskSheet.kt` (~24KB) orphaned; Task List
  bucket-overlap crash (`overdue && due == null`).
- Server `TaskDto` exposes `due: String?` only vs. the local typed `Instant?`.
- Stats hardcoded; `Motion.kt` empty; `SuccessScreen` reset button not deleted.
- **`ForgeTextField` focus ring still an empty `drawBehind { }` stub — 31st note running.**
- Server-side: dev JWT secret in source, `/auth/forgot` logs the reset token to console, CORS
  `anyHost()`, `PUT /tasks/{id}` is a full replace not a merge.
- No Room schema export dir; `gradle.properties` has dead `kapt.*` and a deprecated
  `useClasspathSnapshot`.
- **Gradle daemon SIGSEGV = JBR 21 G1 GC fault, not memory pressure.** Your diagnosis is recorded
  in this session's persistent memory now, so the wrong theory shouldn't resurface. No crash on the
  desktop this round; `-XX:+UseParallelGC` not needed here yet.
- `.idea/` still uncommitted, diff unchanged again. Standing "your call" item.

## Verified

`./gradlew :app:compileDebugKotlin --rerun-tasks` (forced, not cached) → BUILD SUCCESSFUL in 23s.
One warning, unchanged: `ForgeNavHost.kt:123:21` — `'when' is exhaustive so 'else' is redundant`.
Compile-only. **Nothing was run against a live server this round** — the desktop can't reach
`10.46.89.25`, so the auth paths that changed here (`storeAuth` writing two keys, the key rename)
are compile-verified but not runtime-verified. The key-rename logout prediction above is reasoned
from the code, not observed.
