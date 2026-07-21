# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-07-21 · **On top of:** `9869da5`

Tree is green: `./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL**, no Kotlin warnings.
Compile-only — not launched on device. Small round.

## What this push adds (Yash's code)

**`Task` gains a real timestamp field** — `val dueAt: Instant? = null` (`kotlin.time.Instant`).
This is the "model a proper `Instant`, not a display string" groundwork the Home spec asked for.

- Additive with a `= null` default, so all 20 sample tasks and every construction site are
  untouched.
- Compiles clean on Kotlin 2.3.21: `kotlin.time.Instant` is **stable** here (no
  `@OptIn(ExperimentalTime)` needed), and **kotlinx-serialization 1.9.0 resolves a built-in
  serializer for it** — no missing-serializer error on the `@Serializable` class, verified by
  forcing a recompile of `Task.kt` and grepping for opt-in/serializer diagnostics (none).

**`AddTaskSheet.kt`** — whitespace only this round (a blank line moved above the `labels`
declaration). No functional change.

## Worth knowing

- **`dueAt` is defined but not yet wired.** Nothing writes it (the sheet still tracks quick-date
  selection as a local `mutableStateListOf(false,false,false,false)` and doesn't populate
  `dueAt`), and nothing reads it yet. Pure groundwork.
- **`Task` now holds two parallel "due" representations:** the old display-oriented
  `due: String?` + `overdue: Boolean`, and the new `dueAt: Instant?`. Fine as a migration step,
  but they'll want a single source of truth eventually — deriving `due`/`overdue` from `dueAt`
  — or they can drift apart.

## Carried over (still open — untouched this round)

- **The `AddTaskContent()` wiring gap from last push is still there:** `AddTaskSheet` calls
  `AddTaskContent()` with no args, so the X button (wired to the content's no-op default
  `onDismiss`) doesn't close the sheet, and editing never prefills (`editingTask` never reaches
  the content). `onSave` still not threaded; no Save button yet.
- **Notes field still `big = true`** (spec: only Title is the hero); **sheet top corners still
  asymmetric** (`topStart = 14.dp, topEnd = 24.dp`, likely a typo for 24/24).
- `AddTaskContent` still stops after the quick-date chips — picker Row is empty; reminder,
  priority segment, and Save button unbuilt; `ForgePickerField` doesn't exist;
  `ForgePrioritySegmented` is still an empty stub. `AddTaskSheet` not wired to the Add button.
- **Your Task List bucket-overlap crash is still live** (`overdue && due == null` → both TODAY
  and SOMEDAY → duplicate `LazyColumn` key). Fix:
  `someday = active.filter { it.due == null && !it.overdue }`.
- **No mutable task state anywhere** — `onToggle`/`onComplete`/`onAdd` still TODO stubs over the
  immutable top-level `val tasks`. Note the sheet is deliberately shaped stateless-for-a-ViewModel,
  so it's already set up to plug into whatever hoisted state you build.
- Stats week-chart data mostly hardcoded; `BackHandler` on-device check; onboarding finish/skip
  → `Login` vs spec's `Registration`; `Motion.kt` empty TODO; `SuccessScreen` "Reset prototype"
  button not deleted; `codelabs` practice-ground drift unreverted.
- **ForgeTextField focus ring STILL an empty `drawBehind { /* focus ring, unchanged */ }` stub
  — 16th note running.** Restore block at `git show 0c6ffa9:docs/HANDOFF.md`.
- `gradle.properties`: dead `kapt.*`; deprecated `useClasspathSnapshot` warns.

## Toolchain — untouched this round

No `gradle.properties` / `build.gradle.kts` / version-catalog changes. AGP still 8.13.2.
`ksp.incremental=false`, `hilt { enableAggregatingTask = false }`, `:app:compileJava` alias all
as before. (No new dep needed for `kotlin.time.Instant` — it's stdlib.)

## Screen status — real vs stub

Unchanged from last push except the `Task` model now carries `dueAt`. Auth six near-complete;
onboarding feature-complete; Home / Task List / Task Detail visually complete + navigable but
inert below the nav layer; Stats visually complete (placeholder numbers); Settings
feature-complete with the live theme flag; Add Task sheet = container + header + title/notes +
quick-date chips (pickers/reminder/priority/save still to come, not wired to the Add button).

Design source is still the **Forge Design System** (external Claude Design project).

## Verified

`./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL**, no Kotlin compiler warnings
(including a forced `Task.kt` recompile to confirm `kotlin.time.Instant` needs no opt-in and
serializes cleanly). Compile-only — the two-representations-of-due observation and the
carried-over wiring gaps don't surface as compile errors, only by reading or running.
