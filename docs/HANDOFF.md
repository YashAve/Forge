# Handoff note — laptop → desktop

**From:** laptop Claude session · **Date:** 2026-08-26 · **On top of:** `dde50df`

`./gradlew :app:compileDebugKotlin :server:compileKotlin :shared:compileKotlin --rerun-tasks` →
**BUILD SUCCESSFUL**. Big round. **Google sign-in works end to end on a real device**, and I found
and fixed a bug that had silently broken login for every account in the database.

## 🔴 Read this first: `/auth/login` was 500ing for every pre-existing user

Your last note asked whether the two old Mongo rows still deserialize now that `UserDocument` has
`authProvider`, and said it was unprovable on your machine. **They did not. It was broken, and the
answer is worse than the fallback you proposed — that fallback is the only correct fix.**

```
POST /auth/login  optims@gmail.com  → 500
CodecConfigurationException: Unable to invoke primary constructor of UserDocument
  Caused by: NullPointerException: Parameter specified as non-null is null:
    method UserDocument.<init>, parameter authProvider
```

**The Kotlin constructor default is never applied.** I read `DataClassCodec.decode` in the
bytecode: before it touches the document it pre-seeds *every* constructor parameter into its value
map as `null` (`Map.put(param, null)`). A field absent from stored BSON therefore arrives at
`callBy` as an **explicitly supplied null**, not an omitted parameter, so the default-argument
mechanism never engages at all. A non-null parameter then fails its intrinsic null-check.

So `authProvider: AuthProvider = PASSWORD` was fatal, while `resetToken: String? = null` was always
fine. **Nullable is what makes an added field safe — the default value is decorative.** Fixed in
`e255a9f`: `AuthProvider? = null` plus a top-level `UserDocument.provider` accessor resolving null →
`PASSWORD`. It's a top-level extension, not a member, so the codec can't see it. `createUser` now
writes `PASSWORD` explicitly, reserving `null` for "row predates Google sign-in".

**This will happen again.** `TaskDocument` has four non-null-with-default fields (`done`,
`priority`, `overdue`, `subtasks`) that are safe only because every task document was written with
them present. The next non-null field added to either document 500s every existing row. There's now
a warning block at the top of `Documents.kt`.

Blast radius was `findByEmail` only — `/auth/login`, `/auth/forgot`, `/auth/google`. `/tasks` was
untouched, because JWT validation reads the `sub` claim and never loads the user
(`Security.kt:34-37`).

## ✅ Google sign-in works — first real end-to-end run

Real Galaxy S24 Ultra over USB, against the live server. The row that landed:

```json
{ "_id": "ec5a4992-…", "email": "agarwalyash929@gmail.com",
  "passwordHash": "", "createdAt": 1787686569124, "authProvider": "GOOGLE" }
```

Read from the driver's own `insert` command trace, so that's literal BSON. `find` → empty →
`insert` → `POST /auth/google → 200`. That's the create branch of find-or-create working as
specified, and it incidentally proves the one enum value the `e255a9f` fix couldn't exercise:
`GOOGLE` round-trips. `/auth/forgot` on that address then logged
`Password reset requested for a Google account (no-op)` and issued **zero** update commands — so
the Google guard holds, with the identical 200.

Still untested: the 409 `invalid_auth_provider` branch. It needs a Google token for an address that
already exists as a password account.

## ⚠️ The thing that will bite you next: your SHA-1 is the one in the console

**This is why you should read before you build.** `getCredential()` failed with
`ConnectionResult{statusCode=DEVELOPER_ERROR}` for hours. The account picker opened, closed, and
produced **no token, no exception the user could see, and no HTTP request at all** — so the OkHttp
Profiler was empty and the server log was empty, which looks exactly like "the network layer is
broken". It is visible *only* via `adb logcat | grep -i credential`.

Cause: the Android OAuth client `…2r9g1i1m…` carries SHA-1
`CF:C1:4C:98:B2:0C:BE:DF:0F:E6:A9:69:D1:66:F1:7F:F6:56:3A:08` — **almost certainly yours**, created
25 Aug. The laptop's debug keystore is `D2:86:8C:A0:3B:C7:7D:AB:6C:0D:F1:6F:42:76:46:0F:A8:37:C6:34`.

**An Android OAuth client holds exactly one package name and one SHA-1.** You cannot add a second
fingerprint to it. N dev machines need N Android clients — same package, different SHA-1, all
authorising against the single Web client ID. Yash created a second client for the laptop; **yours
is untouched and still works.** Don't "fix" it by editing the fingerprint on the existing one, or
you'll break whichever machine you're not sitting at. Full detail is in the `AndroidManifest.xml`
comment block now.

Also settled, after three notes of ambiguity: **`…gshnt5i4…` is the WEB client**, `…2r9g1i1m…` is
Android. Project `forge-506609` / `486134999809`.

## Worth knowing

- **The server needs `GOOGLE_WEB_CLIENT_ID` exported or every call is `401 invalid_google_token`.**
  The startup WARN fires when it isn't. Run it as
  `GOOGLE_WEB_CLIENT_ID=486134999809-gshnt5i4kmlga2bia5jvem7c14p5n6c3.apps.googleusercontent.com ./gradlew :server:run`.
- **`BASE_URL` is `10.37.153.25`** — the fifth value in two days, and wrong for you again. The
  `local.properties` fix is now the longest-running avoidable item in these notes.
- **`SIgnInWithGoogle.kt` has a typo'd filename** (capital `I`) and swallows
  `GetCredentialCancellationException` in total silence; the credential-type `if` has no `else`.
  Two of three failure paths produce nothing at all. Left alone — Yash's code.
- **`safeCall`'s KDoc still claims `expectSuccess = false` is configured.** Your finding, still
  unfixed, still worth one line in `NetworkModule` to pin it.
- The client maps neither `invalid_google_token` nor `id_token_required` — both collapse to
  `AuthError.UNKNOWN`. And `createGoogleUser` can throw `email_taken` (409), which is undocumented
  in the route KDoc and semantically wrong on `/auth/google`: it means a concurrent create of the
  same Google identity, where the right answer is to re-run `findByEmail` and sign in.

## Carried over (still open, untouched)

- **The Home back-button bug — fourteenth note running.** Still a product decision, still unmade.
- **The sign-out credential leak** — `ResetFields` not dispatched in the
  `SettingsEffect.NavigateToLogin` handler. One line.
- **The orphaned pre-rename token** under `"auth_is_stored"` — unreachable, undeletable plaintext.
- **`userEmail()` has zero call sites** — `SettingsScreen` still shows `karishma.kapoor@cosmiq.co.in`.
- **The release variant still does not compile** — `BASE_URL` is `debug`-only, `NetworkModule` is
  in `src/main`.
- **Zero test files under `server/src`.** One integration test would have caught the codec bug.
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
- **`ForgeTextField` focus ring still an empty `drawBehind { }` stub — 35th note running.**
- Server-side: dev JWT secret in source, `/auth/forgot` logs the reset token, CORS `anyHost()`,
  `PUT /tasks/{id}` is a full replace not a merge.
- Mongo password sits in `ServerConfig.kt:28`. Repo is private (checked), so hygiene, not exposure.
- No Room schema export dir; `gradle.properties` has dead `kapt.*` and a deprecated
  `useClasspathSnapshot`.
- **Gradle daemon SIGSEGV = JBR 21 G1 GC fault, not memory pressure.** Still worked around with
  `-XX:+UseParallelGC` on every invocation rather than disproven. No crash this round.
- `.idea/` still uncommitted, diff unchanged. Standing "your call" item.

## Verified

Forced `--rerun-tasks` across `:app`, `:shared`, `:server` → BUILD SUCCESSFUL, warnings all
pre-existing. Server run against real Mongo; login/forgot for both pre-existing accounts went
500 → 401/200; register/login for a new account round-trip 201/200; `POST /auth/google` → 200 from a
physical device with the document confirmed in `forge.users`. Debug APK rebuilt and the **generated**
`BuildConfig.java` confirmed to carry the new IP.

Dev database now also holds two throwaway rows from the codec investigation —
`codec-probe@example.com` and `codec-probe2@example.com`. Delete at will.
