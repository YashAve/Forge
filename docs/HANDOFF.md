# Handoff note — laptop → desktop

**From:** laptop Claude session · **Date:** 2026-07-25 · **On top of:** `c82703b`

Tree is green: forced `./gradlew :app:compileDebugKotlin --rerun-tasks` → **BUILD SUCCESSFUL**
(17/17 executed), only the benign Hilt/KSP `ComponentTreeDeps` note. **Compile-only — not
launched on device.**

## 🎉 THE UI IS COMPLETE

Every screen in the app is now visually built. The Add / Edit Task screen — the last one with
unbuilt pieces — is finished, which means the **UI-construction phase is done**. The whole
design system (Forge Design System, the external Claude Design project) has been translated to
Compose: auth (6), onboarding, splash, home, task list, task detail, stats, settings, and the
full-screen add/edit flow.

**Read this carefully — "UI complete" ≠ "app complete".** It means every screen renders and is
navigable. It does **not** mean the app is functional: there is still **no ViewModel / no real
mutable task state**, Save is inert, pickers aren't plumbed back, and several correctness bugs
remain (all listed under "The remaining phase" below). The next phase is **wiring**, not
drawing. Please don't read "UI complete" as license to skip the open items — they're the whole
of what's left.

## What this push finishes (the MORE DETAILS section)

Last push built Project inside MORE DETAILS; this one builds the other three and closes it out:

- **Subtasks** — a dynamic, add/remove list backed by
  `mutableStateListOf<DynamicSubTaskUiItem>` (seeded from `editingTask?.subtasks`). New
  component **`ForgeSubTaskRow`** (`feature/tasks/components/ForgeSubTaskRow.kt`): a 7dp dot +
  a naked input (weight 1f) + a 32dp delete button (Close icon). Rows are **stateless** — the
  title lives in the parent list, so Save always sees current text. Behaviours: a freshly added
  row autofocuses (`isNew` → `isTakeFocus`); pressing **Done** on a row adds another empty row
  (`KeyboardActions(onDone = onAddSubTask)`, which also clears every other row's `isNew`); an
  "Add subtask" button does the same; the section label shows a live `· N` count.
- **Reminder** — a `remind` toggle plus `remindStart` / `remindEnd` time strings (defaults
  "9:00 AM" / "6:00 PM") with their own show-picker flags. `formatTime(hour, minute)` helper
  added.
- **Notes** — a normal-size `ForgeNakedValidationText` bound to `notes`, capped at 500 chars.

Supporting changes:
- **`NakedValidationText` renamed → `ForgeNakedValidationText`** (namespacing consistency with
  the other `Forge*` components) and gained a `keyboardActions: KeyboardActions` param so
  subtask rows can hook **Done**. All call sites updated; no stale references remain (grep
  clean).
- **`DynamicSubTaskUiItem`** (`id: UUID`, `title`, `isNew`) is the UI-list item type, mapped
  from the domain `Subtask` when editing.

## The remaining phase — wiring (all still open, nothing here touches it)

This is now the entire backlog. The UI is done; everything below is functional work:

- **No ViewModel / no mutable task state anywhere.** `onToggle` / `onComplete` / `onAdd` /
  `onSave` are all TODO stubs over the immutable top-level `val tasks`. This one change (a
  ViewModel or hoisted `mutableStateListOf`) is what brings the whole app to life — and would
  let Stats compute a real week chart instead of the hardcoded ints.
- **New-task capture still unreachable** — `AddOrEditTaskScreen(editingTask = tasks[0])` default
  + nav never passing `null`, so **+** always opens "Edit Task" on task[0] (`AddOrEditTask` is a
  `data object`, can't carry an id). Needs a nav key that carries an optional task id (or a
  distinct Add vs Edit route).
- **`ForgeDatePickerDialog` still NPEs** on OK-without-selection (`selectedDateMillis!!`).
- **Save is hardcoded** (`derivedStateOf { true }`); date/time picker selections aren't plumbed
  back (`onDateSelected`/`onTimeSelected`), and the new Reminder pickers are display-only.
- **`AddTaskSheet.kt` (~24KB) is still orphaned** (dead since the full-screen pivot) — safe to
  delete.
- **Task List bucket-overlap crash still live** (`overdue && due == null` → duplicate
  `LazyColumn` key). Fix: `someday = active.filter { it.due == null && !it.overdue }`.
- `dueAt: Instant?` still unused (two parallel due notions); Stats numbers hardcoded; onboarding
  finish/skip → `Login` vs spec's `Registration`; `Motion.kt` empty; `SuccessScreen` reset
  button not deleted.
- **ForgeTextField focus ring STILL an empty `drawBehind { }` stub — 20th note running.** Restore
  block at `git show 0c6ffa9:docs/HANDOFF.md`.
- `gradle.properties`: dead `kapt.*`; deprecated `useClasspathSnapshot` warns.

## Housekeeping

- **`.idea/` still uncommitted, intentionally** — the JDK/bytecode **11 → 17** flip in
  `misc.xml`/`compiler.xml` and the stale `gradle.xml` `lib` entry are modified in the working
  tree and **not** staged. Still your call, same standing note.
- **Toolchain unchanged** — no build-file or version-catalog edits this push. **AGP still
  8.13.2**, `ksp.incremental=false`, `hilt { enableAggregatingTask = false }`, `:app:compileJava`
  alias all as before. (The redundant `foundation-layout` dep from last push is still there;
  drop it whenever you tidy deps.)

## Verified

Forced `./gradlew :app:compileDebugKotlin --rerun-tasks` → **BUILD SUCCESSFUL** (17/17), no
Kotlin warnings. **Compile-only** — subtask autofocus, Done-adds-a-row, the delete button, and
the reminder pickers are verified to build, not verified to render or behave. The natural next
session opens the app on a device/emulator and starts the wiring phase.
