# Handoff note — laptop → desktop

**From:** laptop Claude session · **Date:** 2026-07-25 · **On top of:** `390c43d`

Tree is green: `./gradlew :server:compileKotlin --rerun-tasks` → **BUILD SUCCESSFUL**, and
`./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL** (the app was re-verified because
this push touches the shared version catalog). Unlike every previous push, **this one was
actually run and exercised** — see "Verified" at the bottom.

## The backend exists now

The `:server` module was a Ktor tutorial scaffold (HTML task tables, in-memory `TaskRepository`,
a `/{language}` echo route). **All of it was deleted** and replaced with the real Forge backend:
MongoDB-backed, JWT-authenticated, user-scoped. `build.gradle.kts` and `logback.xml` were the
only things kept.

The UI is complete (last push) and now has a server to talk to. **Nothing on the Android side
is wired to it yet** — that's the next job, and it's the same "wiring phase" the last handoff
described.

## Shape of it

```
server/src/main/kotlin/com/development/forge/
├── shared/api/            ← THE WIRE CONTRACT — reusable by the Android client
│   ├── TaskDtos.kt        Priority, SubtaskDto, SubtaskInput, TaskDto, TaskUpsertRequest
│   ├── AuthDtos.kt        Register/Login/Forgot/Reset requests, UserDto, AuthResponse
│   └── ErrorResponse.kt   the single error shape every 4xx/5xx returns
└── server/
    ├── Application.kt     entry point (main + forgeModule)
    ├── Routing.kt         the whole route table: /health, /auth/*, /tasks/*
    ├── config/            ServerConfig — every setting env-overridable, dev fallbacks
    ├── db/                Documents (Mongo shapes), MongoDatabase (client+indexes), Mappers
    ├── auth/              JwtService, PasswordHasher (BCrypt), AuthRepository, AuthRoutes
    ├── task/              TaskRepository (user-scoped), TaskRoutes
    ├── plugins/           Security(JWT), Serialization, StatusPages, Cors, Monitoring
    ├── error/             ApiException hierarchy → HTTP status + error code
    └── validation/        email / password / task-title checks
```

**Why `com.development.forge.shared.api` is not under `.server.`** — so it can be lifted into a
real shared module later *without changing a single import on the Android side*. If you extract
it, keep the package name identical. It holds `@Serializable` types only: no Ktor, no BSON,
nothing secret.

## Design decisions you should know before extending it

- **Documents ≠ DTOs, deliberately.** `db/Documents.kt` holds the Mongo shapes (including
  `passwordHash` and the reset token); `shared/api` holds the wire shapes. Mapping is explicit
  in `db/Mappers.kt`, so a new field on a document is invisible to clients until someone adds
  it to a mapper. There is intentionally no `UserDocument.toDto()` that includes the hash.
- **User scoping is enforced by the query, not by a post-load check.** Every method on
  `TaskRepository` takes `userId` first and folds it into the Mongo filter
  (`_id == taskId AND userId == userId`). There is no `findById(id)` overload that omits the
  owner — a route *cannot* ask for a task without saying whose it is. `userId` comes only from
  the verified JWT `sub` claim, never from a body/query/header.
- **404, not 403, for someone else's task.** A 403 would confirm the id exists in another
  account. Missing and not-yours are deliberately indistinguishable.
- **Ids are app-generated UUID strings, not `ObjectId`.** One id shape works for tasks, JWT
  subjects and the Android client with no conversion. `@BsonId` maps `id` → `_id`.
- **Subtasks are embedded** in the task document (`subtasks: [...]`), never a separate
  collection — confirmed by inspecting the stored document.
- **Timestamps are epoch-millis `Long`s.** Avoids a kotlinx-datetime dependency and BSON date
  codec wrinkles; trivial to read on the client.
- **Duplicate email is a database guarantee.** `users.email` has a UNIQUE index and
  `createUser` catches Mongo's duplicate-key error → 409. That closes the race a
  read-then-write check would leave open.
- **`/auth/forgot` always returns 200**, even for an unknown address, so it can't be used to
  enumerate accounts. `/auth/login` returns the same 401 body for "no such user" and "wrong
  password" for the same reason.
- **Two Kotlin gotchas hit during this build, worth remembering:** (1) Kotlin block comments
  **nest**, so a KDoc containing `` `/auth/*` `` opens a nested comment and silently swallows
  the rest of the file — that's why the paths in those KDocs read `/auth/…`. (2) `@BsonId` on a
  constructor param emits the same "annotation will also apply to field in future" warning as
  `@ApplicationContext` did; left as-is because changing the target risks breaking id mapping.

## ⚠️ Read first

**1. `/auth/forgot` is a stub, by scope.** Email sending was explicitly out of scope, so the
reset token is **logged to the server console** and delivered nowhere. To exercise `/auth/reset`
you scrape the token from the server log. There's a `TODO(email)` at the log site.

**2. Firebase is deliberately absent.** No dependency, no code — as instructed. Two clearly
marked `TODO(FCM)` comments mark where it goes when scoped: `UserDocument` (an
`fcmTokens: List<String>`, plural, since one account can have several devices) and
`AuthRoutes` (a JWT-authenticated `POST /auth/fcm-token`).

**3. The dev JWT secret is in the source.** `ServerConfig.jwtSecret` falls back to
`"forge-dev-secret-do-not-use-in-production"` so `:server:run` works with zero setup. Anyone
with the repo can mint tokens for any user. The server **logs a loud WARN at startup** when
it's in use. Set `JWT_SECRET` anywhere that isn't this laptop. Changing it invalidates every
outstanding token (the blunt logout-all).

**4. CORS is `anyHost()`.** Development convenience for browser tooling; the Android client is
native and isn't subject to CORS at all. Restrict before this is exposed anywhere real.

**5. ⚠️ The Mongo container is restart-looping — pre-existing, not caused by this work.**
`docker inspect forge-mongo` reports **RestartCount 7657** with `ExitCode 0` over 5 weeks
(≈ one restart every 6 minutes), and the host was sitting at **14Gi/14Gi memory used, 967Mi
available**. It bounced twice during testing. The Mongo driver reconnects on its own (the
server survived both bounces and all tests passed), but this is worth fixing at the source —
likely memory pressure from Gradle/Studio daemons plus the container. `restart: unless-stopped`
is masking it.

**6. `PUT /tasks/{id}` is a full replace, not a merge.** Omitted fields fall back to DTO
defaults — so a partial PUT will blank fields. The Android client must send the whole task.

## Toolchain — additive only, no versions changed

Per the constraints: **the `[plugins]` block and every existing version are untouched**, and
**`app/` has zero changes** (verified with `git status --short app/`). Added to
`gradle/libs.versions.toml`:

- `mongodb = "5.2.1"` → `mongodb-driver-kotlin-coroutine`
- `bcrypt = "0.10.2"` → `at.favre.lib:bcrypt`
- `ktor-server-auth`, `ktor-server-auth-jwt`, `ktor-server-cors` — all reusing the **existing**
  `ktor` version ref (3.1.3), no new ktor version.

`settings.gradle.kts` untouched (no new module — hence the shared-package approach above).
Koin was **kept** as the DI mechanism since the module already used it.

## Route table

| Method | Path | Auth | Success | Errors |
|---|---|---|---|---|
| GET | `/health` | — | 200 | — |
| POST | `/auth/register` | — | 201 + token+user | 400, 409 |
| POST | `/auth/login` | — | 200 + token+user | 400, 401 |
| POST | `/auth/forgot` | — | 200 (always) | 400 |
| POST | `/auth/reset` | — | 200 | 400 |
| GET | `/tasks` | JWT | 200 + `[TaskDto]` | 401 |
| POST | `/tasks` | JWT | 201 + `TaskDto` | 400, 401 |
| PUT | `/tasks/{id}` | JWT | 200 + `TaskDto` | 400, 401, 404 |
| DELETE | `/tasks/{id}` | JWT | 200 | 400, 401, 404 |

No `/stats` endpoint — stats are computed client-side from the task list, as specified.

## Run it

```bash
sg docker -c "docker compose -f ~/forge-mongo/docker-compose.yml up -d"   # Mongo must be up
./gradlew :server:run                                                     # → http://0.0.0.0:9292
```

## Still open — the Android wiring phase (unchanged by this push)

The server is done; the app still doesn't talk to it. Carried over verbatim:

- **No ViewModel / no mutable task state.** `onToggle`/`onComplete`/`onAdd`/`onSave` are TODO
  stubs over the immutable top-level `val tasks`. **This is now also the thing standing between
  the finished UI and the finished backend** — a repository calling these endpoints via the Ktor
  client (already an app dependency) plus a ViewModel is the whole remaining job.
- **New-task capture unreachable** — `AddOrEditTaskScreen(editingTask = tasks[0])` default + nav
  never passing `null`, so **+** always opens "Edit Task" on task[0].
- **`ForgeDatePickerDialog` NPEs** on OK-without-selection (`selectedDateMillis!!`).
- Save hardcoded (`derivedStateOf { true }`); date/time picker selections not plumbed back.
- **`AddTaskSheet.kt` (~24KB) still orphaned** — safe to delete.
- **Task List bucket-overlap crash still live** (`overdue && due == null` → duplicate
  `LazyColumn` key). Fix: `someday = active.filter { it.due == null && !it.overdue }`.
- `dueAt: Instant?` unused. **Note:** the spec'd server `Task` has no `dueAt` field — the
  server mirrors the enumerated list (`due: String?` only). If `dueAt` becomes real on the
  client, the DTO needs it too.
- Stats hardcoded; onboarding finish/skip → `Login` vs spec's `Registration`; `Motion.kt` empty;
  `SuccessScreen` reset button not deleted.
- **ForgeTextField focus ring STILL an empty `drawBehind { }` stub — 21st note running.**
- `.idea/` **still uncommitted** (JDK 11→17 flip + stale `gradle.xml` `lib` entry). Your call.
- `gradle.properties`: dead `kapt.*`; deprecated `useClasspathSnapshot` warns.

## Verified — actually run, not just compiled

A 34-check end-to-end smoke test was run against the live server + live Mongo: **33 passed, 1
failed on a test-harness log-flush race** (the reset token *was* logged correctly; the script
grepped the file before logback flushed). The reset flow was then re-verified by hand and passes
completely.

Covered: health · register 201 · duplicate email 409 · invalid email/short password/malformed
JSON 400 · login 200 · wrong password & unknown user 401 · missing and garbage token 401 ·
task create 201 with server-minted id, `userId` stamped from the JWT and subtask ids minted ·
blank title 400 · list/update/delete · unknown id 404 · **cross-user isolation: a second user
sees an empty list, gets 404 on PUT and DELETE of the first user's task, and the first user's
task is provably unmodified afterwards** · forgot 200 for known *and* unknown emails · reset →
login with new password 200 → old password 401 → token reuse 400 (single-use) · unknown route
returns JSON 404, not HTML.

Also confirmed by inspecting Mongo directly: BCrypt `$2a$12$…` hashes stored (no plaintext
anywhere), `_id` holding the UUID, `email_1 UNIQUE` and `userId_1` indexes created at startup,
and subtasks embedded inside the task document. Test data was cleaned up afterwards (4 users,
1 task removed — `users` and `tasks` are empty).
