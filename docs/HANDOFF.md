# Handoff note — laptop → desktop

**From:** laptop Claude session · **Date:** 2026-08-18 · **On top of:** `bc711b8`

`./gradlew :shared:compileKotlin :server:compileKotlin :app:compileDebugKotlin --rerun-tasks` →
**BUILD SUCCESSFUL** (forced, not cached — the first run came back all-UP-TO-DATE off the IDE's
build dir, which proves nothing). Still compile-only; nothing new runs. This is Yash's own WIP —
I committed, verified and documented it, and per his instruction did **not** finish or fix any of
it. Everything called out below is left exactly as found.

## ⚠️ Read first — the dev server URL is a hardcoded DHCP address, and it's the laptop's

`app/build.gradle.kts` now sets, for debug builds only:

```kotlin
buildConfigField("String", "BASE_URL", "\"http://10.97.32.25:9292\"")
```

and `app/src/debug/res/xml/network_security_config.xml` permits cleartext to that same literal
`10.97.32.25` plus `10.0.2.2`. I confirmed `10.97.32.25` is this laptop's **current DHCP lease**
today — it is not stable and it is not yours. Third distinct value we've seen for this host
(`10.26.135.25` → `10.69.129.25` → `10.97.32.25`).

Two consequences for you:

- **Change it in two places or you get a misleading failure.** Updating only `BASE_URL` and
  leaving the security config pointing at the old literal surfaces as
  `CLEARTEXT communication to <host> not permitted by network security policy` — which reads like
  a TLS/config problem, not a wrong-IP problem, and sends you down the wrong path.
- `10.0.2.2` (the emulator's alias for its host loopback) is already permitted in the security
  config, but `BASE_URL` does **not** point at it. If you're running the app in an emulator on the
  same machine as the server, `http://10.0.2.2:9292` is the value you want and the cleartext rule
  for it is already in place.

Neither `BuildConfig.BASE_URL` nor anything else in `app/src/debug/` has a call site yet — grepped,
zero hits. So none of this is live; it's staged for whoever writes the Ktor client next. Worth
agreeing on a non-hardcoded mechanism (a `local.properties` value read in `build.gradle.kts`, say)
before it gets baked into a client and starts costing both of us edits on every lease change.

## `:shared` is a real Gradle module now

The API contract moved out of `:server` into its own module. The three DTO files
(`TaskDtos.kt`, `AuthDtos.kt`, `ErrorResponse.kt`) went from
`server/src/main/kotlin/…/shared/api/` to `shared/src/main/kotlin/…/shared/api/` — git recorded
all three as **100% renames**, and because the package was already
`com.development.forge.shared.api` rather than `…forge.server.…`, **not one import changed** on
either side. Both `:app` and `:server` now declare `implementation(project(":shared"))`.

`shared/build.gradle.kts` is a plain Kotlin JVM module: `kotlin.jvm` + `kotlin.serialization`,
`jvmToolchain(17)` compiling down to **JVM 11** bytecode (source/target 11) so the Android module
can consume it; `:server` keeps its own toolchain 17. No new version-catalog entries were needed —
every alias already existed — and **no plugin or toolchain versions were changed**, per the
standing constraint.

**One latent footgun:** `:shared` declares `implementation(libs.kotlinx.serialization.json)`, not
`api(...)`. That's fine *today* only because both consumers declare kotlinx-serialization
themselves, so the runtime is on their compile classpaths independently. The moment a third
consumer depends on `:shared` alone, its `@Serializable` types won't resolve. `api(...)` is the
correct declaration for a module whose public types are annotated — left alone deliberately.

Also added `/shared/build` to `.gitignore`; the new module was dropping 1.5M of untracked build
output into `git status`.

## Auth state has a skeleton — with five live crash sites

`AuthContract.kt` gained an `AuthState(email, password, error, isLoading)`, an `AuthError` enum
(`INVALID_EMAIL`, `WEAK_PASSWORD`, `EMPTY_PASSWORD`, `INVALID_CREDENTIAL`, `EMAIL_ALREADY_EXISTS`,
`INVALID_AUTH_PROVIDER`, `NETWORK_ERROR`, `UNKNOWN`), and `UpdateEmail`/`UpdatePassword` intents.
New `AuthViewModel` holds a `MutableStateFlow<AuthState>` plus an effect `Channel`, and implements
exactly the two field-update intents.

**The other five branches are literal `TODO()`** — `SubmitLogin`, `SubmitRegistration`,
`SignInWithGoogle`, `SubmitForgotPassword`, `SubmitResetPassword`. `TODO()` throws
`NotImplementedError`, so the first screen wired to dispatch any of them crashes the app on tap
rather than no-op'ing. Nothing dispatches to this ViewModel yet (grepped — `AuthViewModel`,
`AuthIntent` and `authState` have zero references outside their own two files), so it's inert
right now. Just don't wire a submit button to it and expect a quiet stub.

Two notes on shape, both left as-is:

- `AuthViewModel @Inject constructor()` takes **no dependencies** — no repository, no
  `ForgePreferences`. So it currently has no route to the backend and no way to persist a token.
  This is the natural home for the `storeAuthToken(...)` / `setOnboardingDone(true)` calls your
  last note correctly flagged as having zero call sites.
- `AuthState.email` and `.password` are `String?` defaulting to `null`, which every `TextField`
  binding will have to `?: ""` its way around.

**And this sharpens the MVI question from last round.** `core/mvi/MviViewModel.kt` +
`MviContract.kt` still exist with **zero implementors** — and `AuthViewModel` is now the *second*
ViewModel (after `SplashViewModel`) to hand-roll the same `MutableStateFlow` + `Channel` +
`onIntent(when)` shape independently. That's a decision worth actually making rather than drifting:
either the base comes back and both adopt it, or it gets deleted before a third copy appears.
`SplashViewModel` also still carries a stale `import …core.mvi.MviViewModel` and a KDoc claiming it
"wires the feature into MviViewModel" — the import survives only because the KDoc `[MviViewModel]`
link counts as a usage, which is exactly why no unused-import warning fires to flag the dead doc.

## Carried over (still open, untouched)

- **The Home back-button bug — fifth note running.** `BackHandler` swallows system back, `Home`
  shares the `backStack.clear()` bucket with `Login`/`Registration` but has no callback to
  repopulate, so back-on-Home blanks the app with no way out. Still a product decision (no-op
  bucket vs. finish the activity), still unmade.
- **`TaskDao.deleteTask` / `deleteTasks` duplicate signature** — identical `@Delete
  vararg TaskEntity`, only difference is the plural one isn't `suspend`. Main-thread-crash trap
  for whoever writes the repository. Untouched.
- **The Splash routing race** you flagged: `pendingDestination` is a plain `var` read exactly once
  on animation finish; if the DataStore reads lose the race the splash hangs permanently rather
  than late. Untouched.
- **Splash's two unreachable arms** — `setOnboardingDone`/`storeAuthToken` still have zero call
  sites, so Splash routes to Onboarding on every launch regardless.
- `isAuthTokenStored()` returns `Flow<String>` (the token itself), not a Boolean, despite the
  `is…` name and the `"auth_is_stored"` key. Cheap to rename now, awkward later. Also: that token
  will land in **plain unencrypted DataStore** once the write side exists.
- Both DAOs otherwise minimal; nothing wired to Room; `AppDatabase` never opened at runtime.
- **New-task capture unreachable** — `AddOrEditTaskScreen(editingTask = tasks[0])` default + nav
  never passing `null`, so **+** always opens "Edit Task" on task[0].
- **`ForgeDatePickerDialog` NPEs** on OK-without-selection (`selectedDateMillis!!`).
- Save hardcoded (`derivedStateOf { true }`); date/time picker selections not plumbed back.
- **`AddTaskSheet.kt` (~24KB) still orphaned** — safe to delete.
- **Task List bucket-overlap crash still live** (`overdue && due == null` → duplicate `LazyColumn`
  key). Fix: `someday = active.filter { it.due == null && !it.overdue }`.
- Server `TaskDto` still exposes `due: String?` only, while `TaskEntity.dueAt` is a typed
  `Instant?` locally — now that they're in one shared module the mismatch is easier to see and
  more likely to matter.
- Stats hardcoded; onboarding finish/skip → `Login` vs spec's `Registration`; `Motion.kt` empty;
  `SuccessScreen` reset button not deleted.
- **`ForgeTextField` focus ring still an empty `drawBehind { }` stub — 26th note running.**
- Server-side: dev JWT secret in source (loud WARN at startup), `/auth/forgot` logs the reset token
  to console, CORS `anyHost()`, `PUT /tasks/{id}` is a full replace not a merge.
- No Room schema export dir (`room.schemaLocation` warning); `gradle.properties` has dead `kapt.*`
  and a deprecated `useClasspathSnapshot` that warns on every configure.
- **Environment**: still carrying two memory-pressure symptoms from earlier notes (Gradle daemon
  SIGSEGV + `forge-mongo` restart-looping with the host at 14Gi/14Gi). No new data point.
- `.idea/` still uncommitted (JDK 11→17 flip + stale `gradle.xml` `lib` entry). Standing "your
  call" item, untouched again.

## Verified

`./gradlew :shared:compileKotlin :server:compileKotlin :app:compileDebugKotlin --rerun-tasks` →
BUILD SUCCESSFUL. Warnings are all pre-existing and benign: two `@BsonId` annotation-target
warnings in `server/db/Documents.kt`, the Room `schemaLocation` notice, and the Hilt/KSP
`ComponentTreeDeps` incremental note. Compile-only — the server wasn't run this round.
