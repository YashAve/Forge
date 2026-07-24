# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-07-24 · **On top of:** `9cc6517`

Tree is green: forced `./gradlew :app:compileDebugKotlin --rerun-tasks` → **BUILD SUCCESSFUL**.
Only warning is the pre-existing Hilt/KSP `ForgeApplication_ComponentTreeDeps` incremental note
(benign, unrelated to this change). Compile-only — not launched on device.

This is a **big round**: a large cleanup + a new full-screen capture flow.

## ⚠️ Heads-up: this push deletes ~780 files

- **The entire `codelabs/` practice ground is gone** (774 paths) and **the `lib/` coroutines
  sandbox module is gone** (9 files, `com.example.lib`). `settings.gradle.kts` drops
  `include(":lib")`. This finally clears the long-standing "codelabs drift unreverted" carry-over.
- When you pull, these will vanish from your tree too. No app/server code referenced either
  (`grep` for `com.example.lib` / `:lib` is clean), so nothing breaks — but don't be alarmed by
  the huge deletion count in the diff.

## What this push adds (Yash's code)

**The Add-Task flow was rearchitected from a bottom sheet to a full pushed screen.**
New spec is "Add / Edit Task v3, FULL SCREEN" — naked borderless inputs on the canvas, two-stage
(fast capture → expandable detail).

- **NEW `feature/tasks/AddOrEditTaskScreen.kt`** — the flagship screen. Built so far: top bar
  (back · title · Save), the hero title as a naked `BasicTextField` (autofocus, amber caret,
  `What needs forging?` placeholder, 120-char cap + a `n/120` counter that appears past 96 chars),
  the **DUE** section = quick-date chips (`FlowRow`, single-select toggle) + two `ForgePickerField`
  pills (date / time) that open dialogs.
- **NEW `feature/tasks/components/ForgePickerField.kt`** — compact fully-rounded pill (icon + mono
  value/placeholder), no ripple, opens a picker on tap.
- **NEW `ForgeDatePickerDialog.kt` / `ForgeTimePickerDialog.kt`** — Material3 DatePicker /
  TimePicker wrapped and fully re-themed to the Forge palette (amber selection, bgElev container,
  etc.). Time picker is a custom `Dialog`+`Surface`; date picker uses `DatePickerDialog`.
- **`ForgeSelectionChip.kt`** — reworked to animate the border *width* via `animateDpAsState`
  (0dp selected ↔ 1.5dp unselected) instead of toggling the border modifier; added `@Preview`.
- **Nav wiring (`ForgeNavKey` + `ForgeNavHost`)** — new `data object AddOrEditTask`; the bottom-nav
  Add button, the TaskList add, and the TaskDetail top-bar edit now all push it; an
  `entry<AddOrEditTask>` renders the screen with no top bar / no bottom nav.
- **`ForgeBottomNavigationBar.kt`** — the Add button's `onSelect` is now `{ onAddClick() }`
  (last round it was an empty `{ }`), so the amber pill finally fires. **That prior bug is fixed.**
- **`ForgeApplication.kt`** — dropped the `MainScope().launch { ForgeFonts.… }` font warm-up; app
  class is now bare. Fonts lazy-load on first use (possible first-frame font jank, acceptable).
- **`Task.kt`** — sample `task1.due` `"Today · 6:00 PM"` → `"Today"`, so edit-mode chip
  pre-selection (`labels.map { it == due }`) can actually match a quick-date chip.

## Real bugs (compile clean, wrong/crash at runtime) — NOT fixed, per our arrangement

1. **New-task capture is currently unreachable.** `AddOrEditTaskScreen(editingTask: Task? =
   tasks[0], …)` — the `@Preview` convenience default (`tasks[0]`) leaks into the real call path,
   because `ForgeNavHost` invokes `AddOrEditTaskScreen(basicModifier, onBack = …)` and never passes
   `editingTask = null`. So tapping **+** opens **"Edit Task" prefilled with "Draft the Q3
   proposal"** every time. Two linked causes: the non-null default, and `AddOrEditTask` being a
   `data object` (can't carry which task) rather than `data class AddOrEditTask(taskId: String?)`.
2. **`ForgeDatePickerDialog` can NPE.** `onDateSelected(datePickerState.selectedDateMillis!!)` —
   `rememberDatePickerState()` starts with **no selection**, so opening the date dialog and tapping
   **OK without picking a day** crashes on the `!!`. (Time picker is safe — hour/minute are always
   valid.)

## WIP / not yet wired on the new screen (deliberate — expected next passes)

- `saved = derivedStateOf { true // I will change it as I design }` → **Save is always enabled &
  amber** regardless of title. `canSave = title.trim().isNotEmpty()` not implemented yet.
- **Picker selections aren't plumbed back.** The screen calls the dialogs with only `onDismiss`;
  `onDateSelected` / `onTimeSelected` are left default, so `date` / `time` state never updates and
  the pills never fill.
- **`onSave` is inert** (nav passes only `onBack`); no Task assembly / persistence.
- **Whole lower half of the spec is unbuilt**: Priority segmented control, the "MORE DETAILS"
  toggle + `AnimatedVisibility` expanded section (Project chips + free-text, Subtasks rapid-entry,
  Reminder window, Notes). The screen stops after the date/time pickers.
- **`ForgePrioritySegmented` is still an empty stub** (unchanged).

## Nav wiring asymmetries worth knowing

- `entry<Home>`'s `onAdd = { }` is a **bare empty lambda** (inert), while `entry<TaskList>`'s
  `onAdd` is wired. The global bottom-nav Add button *is* wired, so Home still has a working add —
  just not via the screen's own `onAdd` slot.
- `entry<TaskDetail>`'s inner `onEdit = { }` is inert, but the TaskDetail **top-bar** edit action
  *is* wired to push `AddOrEditTask`.

## Orphaned code

- **`feature/tasks/AddTaskSheet.kt` (≈24KB) is now dead** — zero references anywhere after the pivot
  to the full screen. Left in place, not deleted. Its good parts (`ForgeSelectionChip`,
  `ForgePickerField`, chip logic) were reworked into the new screen/components. Delete when you're
  sure you won't reuse the sheet layout.

## `.idea/` — left uncommitted on purpose (your call)

Your IDE flipped `.idea/misc.xml` + `.idea/compiler.xml` from **JDK/bytecode target 11 → 17**, and
`.idea/gradle.xml` still lists the now-deleted `$PROJECT_DIR$/lib` linked project. I **did not
commit any `.idea/` changes** — the JDK-target flip is machine-specific IDE churn that would fight
your Android Studio (same reason we keep AGP pinned and `java.home` local). JDK 17 is arguably the
correct target for this toolchain, but that's a deliberate decision for you to make, not IDE noise
to sweep in. The stale `lib` entry in `gradle.xml` will self-heal on your next Gradle re-sync.

## Carried over (still open — untouched this round)

- **Your Task List bucket-overlap crash is still live** (`overdue && due == null` → both TODAY and
  SOMEDAY → duplicate `LazyColumn` key). Fix: `someday = active.filter { it.due == null &&
  !it.overdue }`.
- **`dueAt: Instant?` (added in 9cc6517) is still unused** — nothing reads/writes it; `Task` still
  carries two parallel due notions (`due: String?` + `overdue` vs `dueAt`). Reconcile eventually.
- **No mutable task state anywhere** — `onToggle`/`onComplete` still TODO stubs over the immutable
  top-level `val tasks`. The new screen is shaped stateless-for-a-ViewModel, ready to hoist.
- Stats week-chart data mostly hardcoded; `BackHandler` on-device check; onboarding finish/skip →
  `Login` vs spec's `Registration`; `Motion.kt` empty TODO; `SuccessScreen` "Reset prototype"
  button not deleted.
- **ForgeTextField focus ring STILL an empty `drawBehind { /* focus ring, unchanged */ }` stub —
  17th note running.** Restore block at `git show 0c6ffa9:docs/HANDOFF.md`.
- `gradle.properties`: dead `kapt.*`; deprecated `useClasspathSnapshot` warns.

## Toolchain — untouched this round

No `build.gradle.kts` / version-catalog changes; the only build-file edit is `settings.gradle.kts`
dropping `include(":lib")`. **AGP still 8.13.2.** `ksp.incremental=false`,
`hilt { enableAggregatingTask = false }`, `:app:compileJava` alias all as before.

## Verified

Forced `./gradlew :app:compileDebugKotlin --rerun-tasks` → **BUILD SUCCESSFUL**, no Kotlin
warnings (only the benign Hilt/KSP `ComponentTreeDeps` note). Compile-only — the two real bugs
above (edit-mode default, date-picker `!!`) and the WIP gaps don't surface at compile time, only by
running the Add flow. `FETCH_HEAD` left untracked; `.idea/*` left uncommitted (above).
