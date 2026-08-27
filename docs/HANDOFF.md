# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-08-27 · **On top of:** `b2c01c9`

Two server commits pushed this round: `dcc6807` (8-character reset code + rate limiting) and
`b2c01c9` (client-generated task ids). Both compile-verified with forced `--rerun-tasks`.

**`origin/work` is green. The desktop working tree is not** — Yash's in-flight offline-first work
in `:app` fails `:app:kspDebugKotlin`, so I deliberately did **not** commit it. Pulling this branch
gets you a building repo; the broken half is described below so you know it exists.

## 🔴 Uncommitted on the desktop: Room can't build `getPendingTasks()`

```
e: [ksp] domain/model/Task.kt:30: Cannot figure out how to read this property from a statement.
e: [ksp] data/local/dao/Daos.kt:30: The columns returned by the query does not have the
   properties [id,subtasks] in com.development.forge.domain.model.Task
```

The DAO returns the **domain** model from a Room query:

```kotlin
@Query("SELECT * FROM tasks WHERE sync_state != 'SYNCED'")
suspend fun getPendingTasks(): List<Task>          // domain.model.Task
```

Three separate reasons that can't map, and they are worth separating because only one of them is a
typo:

1. `Task.id` vs the column `task_id`, and `Task.dueAt` vs `due_at` — plain name mismatches.
2. `Task.subtasks` is a **relation**, not a column. No `@ColumnInfo` fixes that; it needs
   `@Relation` on an intermediate type, which `TaskWithSubTask` already is.
3. `Task` carries **both** `due: String?` and `dueAt: Instant?` while the table has only `due` and
   `due_at`. The domain model is the display shape, not the storage shape.

The fix is one line, and it also wants the annotation the compiler is asking for separately —
sync needs subtasks anyway, since `POST /tasks` sends them:

```kotlin
@Transaction
@Query("SELECT * FROM tasks WHERE sync_state != 'SYNCED'")
suspend fun getPendingTasks(): List<TaskWithSubTask>
```

Two warnings from the same file are still live after that:

- **`Daos.kt:28` — `getTaskById` needs `@Transaction`.** It returns `Flow<TaskWithSubTask>`, which
  contains a `@Relation`; without the annotation the task and its subtasks are read in separate
  queries and can disagree. `getVisibleTasks` and `getTaskWithSubtask` right beside it both have it.
- Same line: the return type is `Flow<TaskWithSubTask>`, **not nullable**. A task that is missing —
  or that a sync just deleted — makes Room emit `null` into a non-null Kotlin type. `Flow<TaskWithSubTask?>`
  is the honest signature.

## 🔗 `getPrimaryId()` and `invalid_task_id` are two halves of one change

Worth seeing together, because they must ship together. `Entities.kt` dropped this:

```kotlin
fun getPrimaryId(): String = "${System.currentTimeMillis()}-${UUID.randomUUID()}"
```

in favour of a plain `UUID.randomUUID().toString()`. That is not cosmetic. `b2c01c9` made
`POST /tasks` accept the client's id and validate it as a canonical UUID, and the old format has six
dash-separated groups, so `UUID.fromString` rejects it outright. Checked on the JBR:

```
old getPrimaryId()  1787835378027-a8fa008f-e6a8-401a-8e54-8d0c7d1a872c -> REJECTED (invalid_task_id)
new randomUUID()    31e16aaf-7fc2-4ccb-9b8e-908749624290               -> ACCEPTED
```

So every task created by the old client would have been refused a primary key by the new server.
Any Room row still carrying an old-format id can never sync — see the next section, which as it
happens deletes them all anyway.

## ⚠️ `fallbackToDestructiveMigration(true)` on an offline-first database

`StorageModule.kt` now builds the database with destructive migration, and `AppDatabase` went from
version 1 to 2 (the `sync_state` column). On upgrade **every local task is dropped**.

Right call for today — there is no schema to preserve and no real data. But it is precisely the
wrong setting for the thing being built: the entire point of `SyncState.PENDING_CREATE` is that a
task exists locally *before* the server has seen it, and destructive migration throws exactly that
away, silently, on the next version bump. Worth a `TODO` next to it now, while the reason is
obvious, rather than discovering it after a schema change eats an unsynced task.

Related and currently invisible: `AppDatabase.kt:13` still warns that no schema export directory is
configured. With real migrations that file is what you diff against.

## ✅ Committed and pushed this round

### `dcc6807` — the reset code is now typeable

8 characters from `ABCDEFGHJKLMNPQRSTUVWXYZ23456789` — A–Z without `I`/`O`, digits 2–9.
**40 bits**, keyspace 1,099,511,627,776. Verified: 32 distinct symbols, none ambiguous, and every
symbol hit exactly 8 times per 256 bytes, so `byte and 31` is unbiased. The alphabet is 32 rather
than the obvious 31 (alphanumerics minus `0O1IL`) precisely for that — `% 31` over a 0–255 byte
would favour the first eight symbols 9-to-8.

Accepted case-insensitively, with whitespace and hyphens stripped, normalised on write and read.

Because 40 bits is guessable, two things came with it:

- **`/auth/reset` is rate limited** — 10 failed attempts per address and 100 server-wide per 15
  minutes, then `429 too_many_attempts`. Failures only; the check runs before the token lookup.
  The design is forced by one fact: **the request carries no account identity**, so a per-token
  counter cannot bind an attacker (different token every guess) and a wrong guess matches no user,
  so there is no row to count against. The address is the only thing a sweep cannot vary for free;
  the global ceiling covers rotation. `security/AttemptLimiter.kt` carries the full reasoning.
- **`users.resetToken` gained a unique partial index.** At 40 bits two live codes can collide, and
  `findByResetToken` matches on the code alone, so a collision would let one account's code reset
  another's password. `createResetToken` now redraws on duplicate key.

`TooManyRequestsException` slots into the existing sealed `ApiException` and needed **no**
`StatusPages` change — the `exception<ApiException>` handler already renders any subclass.

**Yash was going to run `db.users.find({resetToken: {$exists: true}})` before restarting** — the
unique index fails at boot if two rows already share a code. Impossible with the old 256-bit hex
tokens, but it is a startup failure if I'm wrong, and there is no Mongo on this desktop.

I recommended **against** a PIN-style lockout and he agreed: it cannot be built where it would help.
Attacker failures match no account, so there is nothing to lock; the only attributable failures are
an owner mistyping their own code. It would be incapable of touching an attacker and perfectly
capable of locking out the user.

### `b2c01c9` — `POST /tasks` takes the client's id

`TaskUpsertRequest` gained `id: String? = null`. Nullable rather than required because that type is
also the body for `PUT /tasks/{id}`, where the id lives in the path — a mandatory field one of its
two routes must ignore is an invitation to honour it there, which is a re-homing primitive. PUT now
documents that it reads the path. An absent id still gets a server-minted one **and an INFO log**; a
blank one is `400 invalid_task_id` rather than falling through.

**`UUID.fromString` alone is not a sufficient check**, which is the part worth remembering. Verified
on JBR 21.0.10 and OpenJDK 21.0.12:

```
"1-1-1-1-1"  ->  00000001-0001-0001-0001-000000000001    (accepted, and padded)
"A1B2…4C5D"  ->  a1b2…4c5d                               (accepted, case-folded)
```

The first hands a client a short guessable key it can deliberately squat on. The second matters
more than it looks: `Filters.eq("_id", …)` is an exact match, so `A1B2…` and `a1b2…` would be **two
documents for one UUID** — the aliasing client-chosen ids exist to remove. `Validation.taskId`
round-trips `UUID.fromString(id).toString()` against the input, which closes both in one expression.

**Duplicate handling is idempotent for the caller's own id**: `200` + the stored task, against `201`
for a genuine insert. For an offline-first client a re-sent create is the *normal* path — the
network drops after the write lands but before the 201 does — so making the client decode an error
code for it is backwards.

**The ownership check there is load-bearing, not cosmetic.** `_id` is unique across the whole
collection, **not per user**: the owner filter that scopes every other method in `TaskRepository`
does not scope the uniqueness namespace. So a duplicate-key error can come from another account's
document, and the obvious "catch duplicate, return the existing task" would hand one user another
user's task. The implementation never reads a document the caller doesn't own — it tries
`findForUser`, then lets Mongo's uniqueness produce a deliberately vague `409 task_id_taken`. The
re-read inside the catch also closes the race where two concurrent creates from the same client both
miss the first lookup.

No new index (`_id` is already unique) and no migration (existing ids came from
`UUID.randomUUID().toString()` and are already canonical).

## What the Android client owes both endpoints

- **`too_many_attempts`, `invalid_task_id`, `task_id_taken`** are all unmapped — they collapse to
  `AuthError.UNKNOWN`. So does `invalid_google_token` and `id_token_required`, from two rounds back.
- **A `200` from `POST /tasks` is success**, not an error. Only `201` means "written just now"; both
  should set a synced flag.
- The 200 path returns the **stored** task and does not apply the request body. A client that edits
  between a failed create and the retry must follow the 200 with a `PUT`.

## Also uncommitted on the desktop (all of it Yash's, none of it broken)

- **`AuthViewModel.submitLogin` now calls `resetFields()` on success** — that closes the
  password-persists-after-login half of the credential leak.
- **The dead `else -> { }` in the auth-effect collector is gone.** `:app` now compiles with **zero**
  source warnings for the first time in these notes — the Room warnings above are KSP, not the
  Kotlin compiler.
- `SuccessScreen` now receives `email = authState.value.email` and dispatches `ResetFields` on
  continue — the first real use of state-supplied email, and the pattern `userEmail()` needs.
- `TaskApi.getTasks(token)` and a `data/repository/TaskRepository.createTask(...)`, plus
  `SyncState` (`SYNCED` / `PENDING_CREATE` / `PENDING_UPDATE` / `PENDING_DELETE`) on `TaskEntity`.
- `NetworkModule` reformatting only, no behaviour change.

Two observations on that, neither blocking:

- **`TaskApi` takes the token as a method parameter.** Every call site now has to fetch it. An
  `Auth` plugin or a `DefaultRequest` header would put it in one place — worth deciding before there
  are four call sites rather than one.
- **`createTaskWithSubtasks(subtaskDao, task, subtasks)` takes a DAO as an argument.** It works, but
  the caller has to supply the other DAO, so `TaskRepository` holds both anyway. An abstract-class
  DAO, or doing the transaction in the repository, keeps that seam inside one type.

## Carried over (still open, untouched)

- **The Home back-button bug — eighteenth note running.** Still a product decision, still unmade.
- **The sign-out credential leak, logout half** — `ResetFields` still not dispatched in the
  `SettingsEffect.NavigateToLogin` handler. The login half is fixed above; logout is the last path
  in the file that doesn't clear.
- **The reset screen inherits the password typed on Login** — `onForgotPassword` at
  `ForgeNavHost.kt:319` is the one transition that doesn't clear, so `ResetPasswordScreen`'s "New
  password" field arrives pre-filled and masked. Still open; still a design call about whether
  `email` should keep carrying forward.
- **"Resend link" pushes a duplicate `EmailSent`** onto the back stack and resets its own cooldown.
- **The orphaned pre-rename token** under `"auth_is_stored"` — unreachable, undeletable plaintext.
- **`userEmail()` has zero call sites** — `SettingsScreen` still shows `karishma.kapoor@cosmiq.co.in`.
- **The release variant still does not compile** — `BASE_URL` is `debug`-only, `NetworkModule` is in
  `src/main`.
- **Zero test files under `server/src`.** Two rounds of security-shaped logic — a rate limiter and a
  UUID validator — are now riding on compile-checking alone.
- The reset-token field's helper still reads "From the reset email."; `EmailSentScreen` hardcodes
  "60 minutes" rather than reading `resetTokenValidityMillis`.
- `safeCall`'s KDoc still claims `expectSuccess = false` is configured. Sixth note.
- `createGoogleUser` can still throw `email_taken` on `/auth/google`.
- The 409 `invalid_auth_provider` branch remains unexercised.
- `TaskDao.deleteTask`/`deleteTasks` duplicate `@Delete vararg`, only one `suspend`.
- `core/mvi` base still has zero implementors while four ViewModels hand-roll the same shape.
- `:shared` still declares `implementation(libs.kotlinx.serialization.json)`, not `api(...)`.
- **New-task capture unreachable**; `ForgeDatePickerDialog` NPEs on OK-without-selection; save
  hardcoded; `AddTaskSheet.kt` orphaned; Task List bucket-overlap crash.
- Server `TaskDto` exposes `due: String?` vs. the local typed `Instant?` — now visible in
  `domain.model.Task`, which carries both.
- Stats hardcoded; `Motion.kt` empty; `SuccessScreen` reset button not deleted.
- **`ForgeTextField` focus ring still an empty `drawBehind { }` stub — 39th note running.**
- Server-side: dev JWT secret in source, `/auth/forgot` logs the reset code, CORS `anyHost()`,
  `PUT /tasks/{id}` is a full replace not a merge.
- Mongo password in `ServerConfig.kt:28`. Private repo, so hygiene not exposure.
- **`BASE_URL` is `10.37.153.25`** — your LAN, unreachable from this desktop (`172.1.2.197`).
  `local.properties` is still the longest-standing avoidable item here.
- `.idea/` is stashed at `stash@{0}`, not a standing diff any more.
- **Gradle daemon SIGSEGV = JBR 21 G1 GC fault, not memory pressure.** `-XX:+UseParallelGC` on every
  invocation here too. No crash this round.

## Verified

`./gradlew :app:compileDebugKotlin :server:compileKotlin :shared:compileKotlin --rerun-tasks` at
`b2c01c9` → **BUILD SUCCESSFUL**. The same command against the desktop's *uncommitted* tree →
**BUILD FAILED** at `:app:kspDebugKotlin`, as above.

The alphabet, entropy and mask-uniformity figures for `dcc6807`, and the UUID leniency figures for
`b2c01c9`, were measured rather than asserted — the latter on both JBR 21.0.10 (Gradle's) and
OpenJDK 21.0.12.

**Still nothing has been run.** No server, no device, no endpoint exercised, no Mongo on this
machine. The rate limiter has never throttled anything, the 8-character code has never been typed
into the app, and `POST /tasks` has never received a client id. Every claim above about behaviour is
read from the code or measured in isolation, not observed end to end.
