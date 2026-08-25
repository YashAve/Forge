# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-08-25 · **On top of:** `2a9e249`

`./gradlew :server:compileKotlin :shared:compileKotlin --rerun-tasks` → **BUILD SUCCESSFUL**.
This round is a server feature: `POST /auth/google`, spec'd by Yash and built to that spec after a
plan review. The Android half is deliberately untouched — that's being wired separately.

## ⚠️ Read first: `/auth/google` is inert until `GOOGLE_WEB_CLIENT_ID` is set

`ServerConfig.googleWebClientId` falls back to a placeholder, so **every call answers
`401 invalid_google_token`** until the real value is exported. The server logs a loud warning at
startup when it's using the placeholder (same pattern as `isUsingDevJwtSecret`), so check the boot
log before debugging a failing sign-in.

It must be the **Web** client ID, not the Android one. Google mints ID tokens whose `aud` is the
web client even when the request originates on Android, so pinning the Android id rejects every
real token. Two candidate ids are currently parked as comments in `app/src/main/AndroidManifest.xml`
(Yash's uncommitted `:app` WIP — left alone this round, and not copied into git history here). The
web one of that pair is what the server wants:

```
GOOGLE_WEB_CLIENT_ID=<web client id> ./gradlew :server:run
```

## What was built

`POST /auth/google` sits beside register/login/forgot/reset. It takes
`GoogleAuthRequest(idToken)`, verifies the token against Google's certs with the audience pinned to
the configured web client id, then finds-or-creates by **verified** email and returns the same
`AuthResponse(token, user)` shape `/auth/login` does — carrying *our* JWT. The Google token is
proof of identity for that one request and is never stored.

Outcomes: unknown email → new Google account; existing Google account → straight sign-in; existing
**password** account → `409 invalid_auth_provider`.

## The design decision, and the constraint that forced it

`UserDocument` gained `authProvider: AuthProvider = AuthProvider.PASSWORD`. Google accounts carry
`passwordHash = ""`.

Nullable `passwordHash: String?` is the type-honest model and was the first choice. It was rejected
because `PasswordHasher.verify(body.password, user.passwordHash)` in `/auth/login` takes a non-null
`String` — nullable stops that line compiling and forces an edit to a route that was explicitly
frozen. The full reasoning now lives in the `UserDocument` KDoc rather than only in a note, at
Yash's request.

`""` is safe rather than merely convenient: no BCrypt hash matches it, and `PasswordHasher.verify`
already returns `false` on a malformed stored hash instead of throwing. A Google account attempting
password login therefore fails as an ordinary `invalid_credentials` 401 — **indistinguishable from
a wrong password**, so anti-enumeration holds and `/auth/login` never learns this field exists.
Branch on `authProvider`, never on `passwordHash.isEmpty()`.

`Mappers.kt` needed no change: `toDto()` is a hand-written whitelist, so the new field is invisible
to clients by default.

## `/auth/forgot` was guarded — a hole this feature would otherwise have opened

Yash caught this in plan review and asked for it closed rather than TODO'd. Without it,
`/auth/forgot` would happily issue a reset token for a Google account and `/auth/reset` would then
write a real `passwordHash` onto it — silently converting it into a password account the owner
never created, a way in that bypasses Google entirely.

The route now branches three ways (unknown email / Google account / password account) and returns
the **identical** `200 MessageResponse` in all three. Only the log line differs, so the
anti-enumeration behaviour is byte-for-byte what it was.

## Why the 409 is not an enumeration vector

Worth stating explicitly, since anti-enumeration was a stated constraint. `invalid_auth_provider`
does disclose that an account exists — but verification happens **before** the lookup, so the only
caller who can reach that branch already holds a Google-signed token for that exact address. You
can only probe emails you already control, and you're being told about your own account. Had the
order been reversed, the same status code would be a live oracle.

## ⚠️ Not verified, and it needs your machine

**The two existing Mongo rows were never checked against the new field.** `optims@gmail.com` and
`optimusprime@gmail.com` predate `authProvider`, so reading them relies on `bson-kotlin`'s
`DataClassCodec` applying the Kotlin constructor default for an absent BSON field. I believe it
does; I did not prove it.

It could not be proven here: **this desktop has no Mongo at all** — no container, and no
`~/forge-mongo` directory. Those rows exist only on your machine. A single `POST /auth/login`
against either account settles it. If they fail to deserialize, the fallback is to make
`authProvider` nullable (`AuthProvider? = null`, read as `PASSWORD`), which removes the dependency
on default-value support entirely.

Everything this round is compile-verified only. No server was started and no endpoint was exercised.

## Carried over (still open, untouched)

- **The sign-out credential leak — still live.** `AuthViewModel` is nav-host-scoped, and nothing
  clears `_authState` on logout, so the post-logout Login screen is still pre-filled with the
  previous user's email and password (revealable — `ForgePasswordField` has a visibility toggle).
  `AuthIntent.ResetFields` exists and is dispatched at `ForgeNavHost.kt:293`/`:309`, but **not** in
  the `SettingsEffect.NavigateToLogin` handler at `:373`. One line closes it. Same gap after a
  successful login: `submitLogin()`/`submitRegistration()` leave the plaintext password in state.
- **The orphaned pre-rename token.** `KEY_AUTH_TOKEN` moved `"auth_is_stored"` → `"auth_token"`;
  `deleteAuth()` removes only the new keys, so the JWT written under the old name is unreachable
  plaintext no code path — logout included — can now delete. Expect existing installs to appear
  logged out; a re-login is needed before authed testing.
- **`userEmail()` still has zero call sites**, so `SettingsScreen` still shows
  `karishma.kapoor@cosmiq.co.in` to every user. The plumbing is all in place now.
- **The release variant still does not compile** — `BASE_URL` is declared only in the `debug`
  buildType while `NetworkModule` is in `src/main`. A product decision, untouched.
- **`BASE_URL` is `10.46.89.25`** — correct for you, unreachable from this desktop (`172.1.2.197`,
  different subnet). Untouched deliberately; it needs the `local.properties` fix, not another edit.
  `10.0.2.2` is already whitelisted in `network_security_config.xml:5` if you go the emulator route.
- **The Home back-button bug — eleventh note running.** Still a product decision, still unmade.
- **`TaskDao.deleteTask`/`deleteTasks` duplicate `@Delete vararg` signature**, only one `suspend`.
- **`core/mvi` base still has zero implementors** while four ViewModels hand-roll the same shape.
- The dead `else -> { }` in `ForgeNavHost`'s auth-effect collector (`ForgeNavHost.kt:123:21`).
- `:shared` still declares `implementation(libs.kotlinx.serialization.json)`, not `api(...)`.
- **The whole task half of the app is still unwired** — no `TaskApi`, no repository, nothing wired
  to Room, `AppDatabase` never opened at runtime, though `/tasks` is implemented and JWT-protected.
- **New-task capture unreachable** (`+` always opens "Edit Task" on task[0]); `ForgeDatePickerDialog`
  NPEs on OK-without-selection; save hardcoded; `AddTaskSheet.kt` (~24KB) orphaned; Task List
  bucket-overlap crash (`overdue && due == null`).
- Server `TaskDto` exposes `due: String?` only vs. the local typed `Instant?`.
- Stats hardcoded; `Motion.kt` empty; `SuccessScreen` reset button not deleted.
- **`ForgeTextField` focus ring still an empty `drawBehind { }` stub — 32nd note running.**
- Server-side: dev JWT secret in source, `/auth/forgot` logs the reset token to console, CORS
  `anyHost()`, `PUT /tasks/{id}` is a full replace not a merge.
- No Room schema export dir; `gradle.properties` has dead `kapt.*` and a deprecated
  `useClasspathSnapshot`.
- **Gradle daemon SIGSEGV = JBR 21 G1 GC fault, not memory pressure** — your diagnosis, now also in
  this session's persistent memory. No crash on the desktop this round.
- `.idea/` still uncommitted, diff unchanged. `app/src/main/AndroidManifest.xml` is also modified
  and uncommitted — Yash's Google client-id WIP, deliberately left alone under the "no `:app`
  changes" constraint.

## Verified

`./gradlew :server:compileKotlin :shared:compileKotlin --rerun-tasks` (forced) → BUILD SUCCESSFUL.
Two warnings, both pre-existing `@BsonId` annotation-target notices (KT-73255) at shifted line
numbers — not new. `google-api-client:2.8.0` resolves from Maven Central. One compile error was hit
and fixed on the way: `GoogleIdTokenVerifier.verify()` returns `GoogleIdToken`, not its `Payload`.

**Not** verified: nothing was run. No Mongo on this machine — see the section above.
