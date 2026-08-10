# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-08-10 · **On top of:** `989ae41`

`./gradlew :app:compileDebugKotlin --rerun-tasks` → **BUILD SUCCESSFUL**. Compile-only, same as
your last note — nothing opens the database yet. This is Yash continuing his own Room work; I
verified and documented, didn't write or fix any of it.

## The Room layer grew up — three of your six open items are now resolved

Good news first, since you flagged these explicitly last note:

1. **FK direction fixed.** `SubTaskEntity` now carries `@ColumnInfo("task_id") val taskId: String`
   with the `ForeignKey(TaskEntity::class, childColumns = ["task_id"], parentColumns =
   ["task_id"])` living on the *child* table, plus `indices = [Index("task_id")]` — the missing-
   index KSP warning you'd have hit is gone. `TaskEntity` no longer holds a `subtask_id` at all.
   One task → many subtasks now models correctly.
2. **`StorageModule.provideDatabase` calls `.build()`.** No longer returns a bare builder.
   `provideTaskDao`/`provideSubtaskDao` were added alongside it, both trivial `database.xDao()`
   providers.
3. **`dueAt` is real on the entity now.** `TaskEntity.dueAt: Instant?` (`kotlin.time.Instant`,
   stdlib — not kotlinx-datetime) with a matching `TypeConverters` class
   (`fromInstantToMilliseconds`/`fromMillisecondsToInstant`, epoch-millis `Long` under the hood,
   consistent with the server's timestamp convention). Registered on `AppDatabase` via
   `@TypeConverters(com.development.forge.data.local.entity.TypeConverters::class)` — note the
   **fully-qualified reference**: the converter class is also named `TypeConverters`, same simple
   name as the `androidx.room.TypeConverters` annotation. Yash sidestepped the collision with the
   FQN rather than renaming either one. Worth knowing if you add a second converter class nearby
   and reach for an unqualified import — it'll clash.
4. **New:** `TaskWithSubTask` (`@Embedded task` + `@Relation` list of subtasks) and
   `TaskDao.getTaskWithSubtask(): Flow<List<TaskWithSubTask>>` — the first real query, and the
   shape a future repository would actually collect from.
5. **Priority round-trips with no manual converter**, confirmed — this round added the first
   `@Upsert`/`@Query` methods that actually exercise `TaskEntity` through KSP, and it compiled
   clean. Room 2.7.1 has native enum-column support, so your "no Priority converter yet, might
   not survive being queried" concern doesn't need one after all.

## ⚠️ Read first — one likely mistake, not fixed

**`TaskDao` has two delete-all-in-one-call methods that look like a copy/paste slip:**

```kotlin
@Delete
suspend fun deleteTask(vararg task: TaskEntity)
...
@Delete
fun deleteTasks(vararg task: TaskEntity)   // same signature, not suspend
```

Identical `@Delete`, identical `vararg task: TaskEntity` parameter — the only difference is
`deleteTasks` isn't `suspend`. Room still generates it, so it compiles clean and calling it from
a background thread works fine. The trap is the name: whoever wires the repository later
(possibly you) has a ~50/50 shot of reaching for `deleteTasks` on the plural-sounding call site
and getting `IllegalStateException: Cannot access database on the main thread` on a UI-thread
call, since nothing stops you calling a non-suspend Room method from `onClick`. Didn't touch it —
not clear if `deleteTasks` was meant to be deleted, meant to become the batch-suspend version
with `deleteTask` renamed/removed, or is deliberate for some synchronous-transaction use case.

## Still open on the Room layer (unchanged, carried from your note)

- **Both DAOs otherwise minimal** — no update-in-place beyond `@Upsert`, no subtask-by-task
  query (e.g. `getSubtasksForTask`), no read-single-task.
- **Nothing wired** — no repository, no ViewModel, no UI reads/writes this; `AppDatabase` is
  never opened at runtime.
- **No schema export directory** — KSP still warns `room.schemaLocation` isn't set. Harmless
  until migrations matter.

## Carried over (still open, untouched this round)

- **The Home back-button bug — still nobody's picked it off.** Third note running it forward:
  `BackHandler` unconditionally swallows system back, `Home` shares the `backStack.clear()`
  bucket with `Login`/`Registration` but has no follow-up callback to repopulate the stack, so
  back-on-Home blanks the screen with no way out. Still a product decision (no-op bucket vs.
  finish the activity), still nobody's made it.
- **No ViewModel / no mutable task state** — `onToggle`/`onComplete`/`onAdd`/`onSave` are TODO
  stubs over the immutable top-level `val tasks`. The entire remaining wiring job — now with
  both a backend *and* a local Room layer waiting to be plugged in.
- **New-task capture unreachable** — `AddOrEditTaskScreen(editingTask = tasks[0])` default + nav
  never passing `null`, so **+** always opens "Edit Task" on task[0].
- **`ForgeDatePickerDialog` NPEs** on OK-without-selection (`selectedDateMillis!!`).
- Save hardcoded (`derivedStateOf { true }`); date/time picker selections not plumbed back.
- **`AddTaskSheet.kt` (~24KB) still orphaned** — safe to delete.
- **Task List bucket-overlap crash still live** (`overdue && due == null` → duplicate
  `LazyColumn` key). Fix: `someday = active.filter { it.due == null && !it.overdue }`.
- Server's `Task` DTO still mirrors `due: String?` only — now that `dueAt` is a real typed field
  locally, the DTO gap is more likely to matter soon.
- Stats hardcoded; onboarding finish/skip → `Login` vs spec's `Registration`; `Motion.kt` empty;
  `SuccessScreen` reset button not deleted.
- **ForgeTextField focus ring still an empty `drawBehind { }` stub — 24th note running.**
- Server-side: dev JWT secret in source (loud WARN at startup), `/auth/forgot` logs the reset
  token to console, CORS `anyHost()`, `PUT /tasks/{id}` is a full replace not a merge.
- `gradle.properties`: dead `kapt.*`; deprecated `useClasspathSnapshot` warns.
- **Environment**: your last note flagged two independent memory-pressure symptoms (Gradle
  daemon SIGSEGV + `forge-mongo` restart-looping at 14Gi/14Gi host RAM) — no new data point from
  this machine, carrying forward since you said it's worth treating as real.
- `.idea/` still uncommitted (JDK 11→17 flip + stale `gradle.xml` `lib` entry). Standing "your
  call" item, untouched again.

## Verified

`./gradlew :app:compileDebugKotlin --rerun-tasks` (forced, not cached) → BUILD SUCCESSFUL.
Compile-only — same as you, nothing to run yet. Server untouched this round.
