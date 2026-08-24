# Handoff note — laptop → desktop

**From:** laptop Claude session · **Date:** 2026-08-25 · **On top of:** `c3cd160`

`./gradlew :app:compileDebugKotlin :shared:compileKotlin :server:compileKotlin --rerun-tasks` →
**BUILD SUCCESSFUL**. And for the first time in this project's history, **the backend was actually
run and exercised end to end from the app** — see below. Yash fixed the Registration bugs your last
note flagged; I added the OkHttp Profiler and fixed the dev-host IP, and did not finish or change
any of his feature code.

## ✅ Both Registration blockers from your last note are fixed — and verified against a live server

1. **The inert fields.** `onValueChange = { onUpdateEmail(state.email) }` → `onValueChange =
   onUpdateEmail` on both fields, matching `LoginScreen`.
2. **`SubmitRegistration -> TODO()`** now dispatches the already-written `submitRegistration()`.

Also: **every remaining unimplemented intent branch is now an empty lambda instead of `TODO()`** —
`SignInWithGoogle`, `SubmitForgotPassword`, `SubmitResetPassword` in `AuthViewModel`, and
`ToggleNotifications`/`ToggleSound` in `SettingsViewModel`. That closes the crash-on-tap class of
bug entirely: "Send link" on Forgot Password was still a live `NotImplementedError` two taps from
the launch screen, which neither of the last two notes caught. Those buttons now do nothing rather
than killing the app.

## 🎉 The stack works end to end — first real proof, not a compile check

I started the server (`:server:run`), Mongo connected, indexes ensured on `forge`. Then Yash drove
the app against it. Reconstructed from the server log plus the Mongo command traces — **18 calls**:

| Time | Call | Status | Request |
|---|---|---|---|
| 00:37–00:45 | `POST /auth/login` ×4 | 401 | `optimus@gmail.com` — **no such user row** |
| 00:48:14 | `POST /auth/register` | **201** | `optims@gmail.com`, bcrypt `$2a$12$…` written |
| 00:48:30 | `POST /auth/login` | **200** | same account |
| 00:48:59 | `POST /auth/register` | **201** | `optimusprime@gmail.com` |
| 00:50:25–46 | `POST /auth/login` ×3 | 401 | user row **found** → password rejected |
| 00:51:09–21 | `POST /auth/login` ×3 | 200 / 401 / 200 | `optims@gmail.com` |

Register → login round-trips correctly (`201` then `200` on the same credentials). The 401s split
into two causes, and the split is the useful part: four were an email that was never registered
(`firstBatch: []`), four were an existing user whose password BCrypt rejected. **No server-side bug
is implicated** — the API behaved correctly in all 18 calls. `GET /tasks` without a token correctly
returns `401 {"error":"unauthorized"}`.

Note there are now **two real accounts in the `forge` database** — `optims@gmail.com` (which looks
like a typo of "optimus", and is the one that works) and `optimusprime@gmail.com`.

## OkHttp Profiler — debug-only, and why there is now a `src/release`

Debug builds stream every request/response to logcat for the Android Studio **OkHttp Profiler**
plugin. Ktor's `OkHttpConfig` exposes `addInterceptor()`, so this hooks into the existing
`HttpClient(OkHttp)` engine block — no raw `OkHttpClient` needed.

The wiring is worth understanding before you touch it. The dependency is `debugImplementation`, so
`src/main` **cannot name the interceptor class**: `NetworkModule` compiles for every variant, and
referencing `OkHttpProfilerInterceptor` there would break the release compile. So
`debugNetworkInterceptors()` is declared twice — `src/debug` returns the profiler, the **new
`src/release`** returns `emptyList()` — and `NetworkModule` just calls it. Verified the artifact is
on `debugRuntimeClasspath` and **absent** from `releaseRuntimeClasspath`.

On your machine you'll need the IDE plugin too (Settings → Plugins → Marketplace → "OkHttp
Profiler"). Version 1.0.38 declares `since-build="242"` with no upper bound. Note the plugin
version (1.0.38) and the Gradle artifact version (`com.localebro:okhttpprofiler:1.0.8`) are
unrelated numbering — easy to confuse.

## ⚠️ Two real problems, neither of them app code

- **The release variant does not compile, and hasn't since `07ea74c`.**
  `e: NetworkModule.kt:45:29 Unresolved reference 'BASE_URL'`. `buildConfigField` for `BASE_URL` is
  declared **only in the `debug` buildType**, but `NetworkModule` lives in `src/main` and is
  compiled for every variant. Nobody had built release, so nobody noticed. My profiler change is
  explicitly *not* implicated — I re-ran release afterwards and it still reports exactly this one
  error and nothing profiler-related. Left unfixed: what release should point at is a product
  decision, not a mechanical one.
- **The Gradle daemon crash is NOT memory pressure — that theory has been wrong for several
  notes.** It killed two builds today and left dumps. The cause is
  `SIGSEGV` in `InterpreterRuntime::at_safepoint` on the **`G1 Conc#1` thread**, JBR 21.0.10 —
  **with heap at 0% of 3 GiB and 8.2 GiB free on the host**. It is a GC-level JVM fault, not RAM
  starvation. Workaround that got every subsequent build through:
  `-Dorg.gradle.jvmargs="-XX:+UseParallelGC …"`. I did **not** edit `gradle.properties`; if this
  bites you too, switching the daemon's collector there or pointing `org.gradle.java.home` at a
  non-JBR JDK is the fix. (Three `hs_err_pid*.log` files, 1.2M, sit in the repo root — gitignored.)

## The hardcoded dev IP finally bit, exactly as predicted

`BASE_URL` and `network_security_config.xml` drifted apart — the security config had been updated
to the new lease, `BASE_URL` had not — producing
`CLEARTEXT communication to 10.97.32.25 not permitted by network security policy`. That message
names TLS policy, not the stale address actually responsible, which is precisely the trap the last
three notes warned about. Both now say `10.46.89.25`.

**That is the fourth value this host has had in a single day** (`10.69.129.25` → `10.97.32.25` →
`10.81.29.25` → `10.46.89.25`). Each change costs two edits plus a rebuild, because
`buildConfigField` only reaches the app through `BuildConfig` regeneration — editing the security
config alone silently does nothing. Two ways out, both small, neither taken because it's a shared
decision: read `forge.baseUrl` from `local.properties` (keeps it out of git, fixes it per-machine,
which also solves *your* copy pointing at a laptop IP), or use `10.0.2.2` if you're on an emulator
on the same host — it's a fixed alias and already cleartext-permitted.

## Carried over (still open, untouched)

- **The Home back-button bug — ninth note running.** Still a product decision, still unmade.
- **`TaskDao.deleteTask`/`deleteTasks` duplicate `@Delete vararg` signature**, only one `suspend`.
- **`SettingsScreen`'s `email` is never passed by the nav host**, so every user sees
  `karishma.kapoor@cosmiq.co.in` as their own account. Now that real accounts exist in Mongo and
  login returns a real `UserDto`, this one has gone from cosmetic to visibly wrong.
- **`isAuthTokenStored()` returns `Flow<String>`** (the token), not a `Boolean`. Real JWTs are now
  genuinely sitting in plain unencrypted DataStore — this stopped being theoretical today.
- **`core/mvi` base still has zero implementors** while four ViewModels hand-roll the same shape.
- The dead `else -> { }` in `ForgeNavHost`'s auth-effect collector — still the only source warning
  in the app module (`ForgeNavHost.kt:123:21`).
- `:shared` still declares `implementation(libs.kotlinx.serialization.json)`, not `api(...)`.
- Nothing wired to Room; `AppDatabase` never opened at runtime. **The whole task half of the app is
  still unwired** — auth now works end to end, tasks have no client at all (no `TaskApi`, no
  repository), even though `/tasks` is implemented and JWT-protected server-side.
- **New-task capture unreachable** (`+` always opens "Edit Task" on task[0]); `ForgeDatePickerDialog`
  NPEs on OK-without-selection; save hardcoded; `AddTaskSheet.kt` (~24KB) orphaned; Task List
  bucket-overlap crash (`overdue && due == null`).
- Server `TaskDto` exposes `due: String?` only vs. the local typed `Instant?`.
- Stats hardcoded; `Motion.kt` empty; `SuccessScreen` reset button not deleted.
- **`ForgeTextField` focus ring still an empty `drawBehind { }` stub — 30th note running.**
- Server-side: dev JWT secret in source (it logs a loud WARN at startup — I saw it fire),
  `/auth/forgot` logs the reset token to console, CORS `anyHost()`, `PUT /tasks/{id}` is a full
  replace not a merge.
- No Room schema export dir; `gradle.properties` has dead `kapt.*` and a deprecated
  `useClasspathSnapshot` that warns on every configure.
- `.idea/` still uncommitted, diff unchanged again. Standing "your call" item.

## Verified

`--rerun-tasks` across `:app`, `:shared`, `:server` → BUILD SUCCESSFUL. Server started, Mongo
connected, 18 real API calls served. Debug APK built and confirmed to carry
`BASE_URL = "http://10.46.89.25:9292"` in the **generated** `BuildConfig.java` — not just the
Gradle source, since that's the half that was stale.

One housekeeping note: a `git stash@{0}` on the laptop holds a superseded local WIP (an `AuthApi
.register()` and the `updateEffect()` removal) that `741d793` had already landed in better form.
Kept rather than dropped, purely as a safety net.
