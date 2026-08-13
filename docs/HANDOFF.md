# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-08-14 · **On top of:** `4c2f1b2`

`./gradlew :app:compileDebugKotlin --rerun-tasks` → **BUILD SUCCESSFUL**. Compile-only, same as
last note — nothing new opens the database at runtime. This is Yash continuing his own work
(`ForgePreferences` generalized, Splash wired to real routing); I verified, committed, and
documented, didn't write or fix any of it.

## ⚠️ Read first — Splash now *reads* auth/onboarding state, but nothing writes it yet

`ForgePreferences` gained a generic `preference<T>()`/`setPreference<T>()` helper — the
IOException→`emptyPreferences()` fallback from last note now covers all three preferences for
free — plus two new flags: `isOnboardingDone()`/`setOnboardingDone()` and
`isAuthTokenStored()`/`storeAuthToken()`. `SplashViewModel` was rewritten off the shared
`MviViewModel` base onto a plain `ViewModel` + `Channel<SplashEffect>`: it kicks off both flag
reads on launch, and once the splash animation's `onFinished` fires (~2.6s: 600ms + two 800ms
tweens + 400ms), sends whatever `pendingDestination` was computed — Onboarding, Login, or Home.

**The write side has zero call sites** (checked with grep — `setOnboardingDone` and
`storeAuthToken` only appear in their own declarations). `OnboardingFlowScreen`'s `onFinish` is
still just `backStack.clear(); backStack.add(Login)`, and `Login` entry's `onLogin` is still
`backStack.clear(); backStack.add(Home)` — pure navigation, no preferences call, no backend call.
`LoginScreen` itself never checks a real password rule either (its own design-spec comment: "Login
does NOT check the 8-character rule. Any non-empty password is fine."). Net effect: this compiles
clean and *looks* like a real auth gate, but neither flag can ever flip away from its default, so
Splash routes to Onboarding on every launch — same observable behavior as before this commit, just
now dressed up as a three-way branch where two arms are currently unreachable. Not a regression,
but easy to mistake for "done" on a skim. Whoever wires Login/Registration to the backend next is
the natural place to also add the `setOnboardingDone(true)` / `storeAuthToken(token)` calls.

Smaller, sharper-edged than it looks: `onAnimationFinished()` checks `pendingDestination` — a
plain nullable `var`, not a `StateFlow` — exactly once, when the animation's one-shot
`LaunchedEffect` completes. If `checkAuthStatus()`'s two sequential DataStore `.first()` reads
haven't landed by then, the `?.let` is a silent no-op and *nothing ever retries* — the splash
screen sits there permanently, not just delayed. The animation's ~2.6s floor makes this very
unlikely to lose the race against a couple of local DataStore reads, but there's no actual
ordering guarantee tying them together if that changes (e.g. DataStore on a cold/first launch, or
the tween durations getting shortened later).

## Room layer grew up — three of your six open items are now resolved

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

## Still unresolved from that note — not touched this round

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
call, since nothing stops you calling a non-suspend Room method from `onClick`. Still untouched —
still not clear if `deleteTasks` was meant to be deleted, meant to become the batch-suspend
version with `deleteTask` renamed/removed, or is deliberate for some synchronous-transaction use
case.

- **Both DAOs otherwise minimal** — no update-in-place beyond `@Upsert`, no subtask-by-task
  query (e.g. `getSubtasksForTask`), no read-single-task.
- **Nothing wired** — no repository; no ViewModel reads/writes the Room layer; `AppDatabase` is
  never opened at runtime.
- **No schema export directory** — KSP still warns `room.schemaLocation` isn't set. Harmless
  until migrations matter.

## Carried over (still open, untouched this round)

- **The Home back-button bug — still nobody's picked it off.** Fourth note running it forward:
  `BackHandler` unconditionally swallows system back, `Home` shares the `backStack.clear()`
  bucket with `Login`/`Registration` but has no follow-up callback to repopulate the stack, so
  back-on-Home blanks the screen with no way out. Still a product decision (no-op bucket vs.
  finish the activity), still nobody's made it.
- **No ViewModel / no mutable task state on Home** — `onToggle`/`onComplete`/`onAdd`/`onSave` are
  TODO stubs over the immutable top-level `val tasks`. The entire remaining wiring job — now with
  a backend, a local Room layer, *and* a real (if currently one-way) DataStore auth gate all
  waiting to be plugged together.
- **New-task capture unreachable** — `AddOrEditTaskScreen(editingTask = tasks[0])` default + nav
  never passing `null`, so **+** always opens "Edit Task" on task[0].
- **`ForgeDatePickerDialog` NPEs** on OK-without-selection (`selectedDateMillis!!`).
- Save hardcoded (`derivedStateOf { true }`); date/time picker selections not plumbed back.
- **`AddTaskSheet.kt` (~24KB) still orphaned** — safe to delete.
- **Task List bucket-overlap crash still live** (`overdue && due == null` → duplicate
  `LazyColumn` key). Fix: `someday = active.filter { it.due == null && !it.overdue }`.
- Server's `Task` DTO still mirrors `due: String?` only — now that `dueAt` is a real typed field
  locally, the DTO gap is more likely to matter soon.
- Stats hardcoded; onboarding finish/skip → `Login` vs spec's `Registration` (and per above,
  doesn't persist either way yet); `Motion.kt` empty; `SuccessScreen` reset button not deleted.
- **ForgeTextField focus ring still an empty `drawBehind { }` stub — 25th note running.**
- Server-side: dev JWT secret in source (loud WARN at startup), `/auth/forgot` logs the reset
  token to console, CORS `anyHost()`, `PUT /tasks/{id}` is a full replace not a merge. This is
  also where a real `onLogin`/`onRegister` would eventually need to call in, alongside
  `storeAuthToken`.
- `gradle.properties`: dead `kapt.*`; deprecated `useClasspathSnapshot` warns.
- **Environment**: two independent memory-pressure symptoms carried since the note before last
  (Gradle daemon SIGSEGV + `forge-mongo` restart-looping at 14Gi/14Gi host RAM) — no new data
  point this round, carrying forward. Separately (not in this doc's scope — see desktop-local
  memory instead): the desktop Studio process itself needed a manual cleanup this week after a
  display-session restart left stale JVMs piled up; unrelated to the app/repo.
- `.idea/` still uncommitted (JDK 11→17 flip + stale `gradle.xml` `lib` entry). Standing "your
  call" item, untouched again.

## Verified

`./gradlew :app:compileDebugKotlin --rerun-tasks` (forced, not cached) → BUILD SUCCESSFUL.
Compile-only — same as last note, nothing to run yet. Server untouched this round.
