# Handoff note — laptop → desktop

**From:** laptop Claude session · **Date:** 2026-08-09 · **On top of:** `23a5229`

`./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL** with your portrait-lock commit and
this one combined. **Compile-only — not run.** Small round: one WIP checkpoint plus a
`.gitignore` addition.

I rebased onto your `23a5229` rather than merging, so history stays linear.

## ⚠️ This push is Yash's in-progress code, committed deliberately unfinished

A **checkpoint of work in flight**, not a finished feature. I was explicitly asked to commit it
*without completing it*, so **nothing here was written, corrected or filled in by me** — the
scaffolding is exactly as Yash left it. Please **don't finish it either** unless he asks; the
empty DAOs and unfinished builder below are known gaps he's mid-way through, not oversights for
a passing agent to tidy.

## What landed: the start of a Room persistence layer

Four new files — the app's first local-storage scaffolding:

- **`data/local/entity/Entities.kt`** — `TaskEntity` (title, done, priority, due, overdue,
  project, notes) and `SubTaskEntity` (title, isDone). Both use `String` primary keys defaulted
  from a private top-level `id` generator (`"${currentTimeMillis}-${UUID}"`).
- **`data/local/dao/Daos.kt`** — `TaskDao` and `SubtaskDao`, both `@Dao` and **completely empty**.
- **`data/local/database/AppDatabase.kt`** — `@Database(entities = [TaskEntity, SubTaskEntity],
  version = 1)` exposing both DAOs.
- **`di/StorageModule.kt`** — Hilt `@Module` providing the database.

Room was already a dependency (`room-runtime`, `room-ktx`, `room-compiler` via KSP) — **no build
file or catalog changes**.

## Known-unfinished points (deliberate — do not fix unprompted)

1. **Both DAOs are empty interfaces.** No `@Query`/`@Insert`/`@Update`/`@Delete` yet.
2. **`StorageModule.provideDatabase` returns the *builder*, not the database.** It stops at
   `Room.databaseBuilder(context, AppDatabase::class.java, "app_database")` — no `.build()`. It
   compiles (inferred return type) and nothing injects `AppDatabase` yet, so nothing breaks
   today, but injecting `AppDatabase` won't work until `.build()` is added.
3. **The task↔subtask relationship is modelled parent-side and looks inverted.** `TaskEntity`
   holds a nullable `subtask_id` with a `ForeignKey` to `SubTaskEntity` — i.e. each task has *at
   most one* subtask. The domain model and the finished Add/Edit UI have a task owning a *list*,
   which normally means the FK lives on the child (`SubTaskEntity.taskId` → `tasks.task_id`).
   Most likely thing to need rework; **left alone.**
4. **No `Priority` type converter yet.** `TaskEntity.priority` is the domain enum; Room needs a
   `@TypeConverter` before it can round-trip. Not an error today because nothing queries it.
5. **No timestamps in the entities** — the server has `createdAt`/`updatedAt` epoch-millis; the
   local entities don't carry them.
6. **Nothing is wired.** No repository, no ViewModel, no UI reads or writes this; the database
   is never opened at runtime.

Worth knowing: the entities passed through a red state this session —
`@PrimaryKey(autoGenerate = true)` on a `String` is rejected by Room (*"its type must be int,
Integer, long or Long"*, plus `AUTOINCREMENT is only allowed on an INTEGER PRIMARY KEY`). Yash
dropped `autoGenerate` and it compiles. Noted only so it isn't reintroduced.

## Also in this push

- **`.gitignore`**: added `hs_err_pid*.log`, `replay_pid*.log`, `core.*`. A 256KB
  `hs_err_pid165247.log` was sitting untracked in the repo root — a **Gradle daemon JVM crash**
  (SIGSEGV in G1 GC, `G1ParScanThreadState::do_oop_evac`, JBR 21.0.10, ~23s into a build). Left
  on disk, now ignored instead of committed.

## ⚠️ Your Home-back bug is still open — nothing here touched it

Carrying your note forward, because it's the most serious thing outstanding: **pressing system
back on Home blanks the app.** `BackHandler` is registered unconditionally, and `Home` now
shares the `backStack.clear()` bucket with `Login`/`Registration` — but those two always have a
callback that re-adds an entry, and Home doesn't. So back on Home empties the stack, `NavDisplay`
has nothing to render, and back is fully swallowed so it can't even fall through to exiting the
app. You flagged the fix as a product decision (put `Home` back in the `else` no-op bucket vs.
finish the activity); **still undecided, still unfixed.** Since it's one gesture to a dead
screen, it probably deserves picking off before more feature work.

## Environment warnings

- **Two independent memory-pressure symptoms now.** The new Gradle daemon SIGSEGV above, and
  `forge-mongo` restart-looping (`RestartCount 7657`, `ExitCode 0`, over 5 weeks, host at
  14Gi/14Gi RAM as of the backend push). Individually dismissable; together they suggest this
  box is genuinely starved — worth treating as a real problem rather than noise. This machine
  also has prior form for JVM/JBR trouble.
- **`.idea/` still uncommitted** (JDK 11→17 flip + stale `gradle.xml` `lib` entry). Standing
  "your call" item — I stashed it across the rebase and restored it untouched.

## Where the project stands

- **UI: complete** (`390c43d`).
- **Backend: complete** (`5123181`) — Ktor + MongoDB + JWT, user-scoped, smoke-tested live.
- **The gap between them: still open.** The app doesn't talk to the server. No ViewModel, no
  repository, no mutable task state. This Room layer is the start of the local half.

## Carried over (still open — untouched this round)

- **No ViewModel / no mutable task state** — `onToggle`/`onComplete`/`onAdd`/`onSave` are TODO
  stubs over the immutable top-level `val tasks`. The entire remaining wiring job.
- **New-task capture unreachable** — `AddOrEditTaskScreen(editingTask = tasks[0])` default + nav
  never passing `null`, so **+** always opens "Edit Task" on task[0].
- **`ForgeDatePickerDialog` NPEs** on OK-without-selection (`selectedDateMillis!!`).
- Save hardcoded (`derivedStateOf { true }`); date/time picker selections not plumbed back.
- **`AddTaskSheet.kt` (~24KB) still orphaned** — safe to delete.
- **Task List bucket-overlap crash still live** (`overdue && due == null` → duplicate
  `LazyColumn` key). Fix: `someday = active.filter { it.due == null && !it.overdue }`.
- `dueAt: Instant?` unused; the server DTO mirrors `due: String?` only — if `dueAt` becomes real
  client-side, the DTO needs it too.
- Stats hardcoded; onboarding finish/skip → `Login` vs spec's `Registration`; `Motion.kt` empty;
  `SuccessScreen` reset button not deleted.
- **ForgeTextField focus ring still an empty `drawBehind { }` stub — 23rd note running.**
- Server-side: dev JWT secret in source (loud WARN at startup), `/auth/forgot` logs the reset
  token to console, CORS `anyHost()`, `PUT /tasks/{id}` is a full replace not a merge.
- `gradle.properties`: dead `kapt.*`; deprecated `useClasspathSnapshot` warns.

## Verified

`./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL** post-rebase, i.e. your changes and
this WIP together. Compile-only; nothing was run, and there's nothing to run yet — no code path
opens the database. Server untouched this round.
