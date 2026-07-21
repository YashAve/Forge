# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-07-21 · **On top of:** `d23700a`

Tree is green: `./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL**, no Kotlin warnings.
Compile-only — not launched on device.

## What this push adds (Yash's code)

**The Add / Edit Task bottom sheet gets its first real build** (`AddTaskSheet.kt`, was a stub):

- `AddTaskSheet` — a Material3 `ModalBottomSheet` with a custom 40×4 drag handle, the
  `~55%` black scrim, `bgElev` container, and top-rounded shape. It hosts `AddTaskContent`.
- `AddTaskContent` — the scrolling body: the ambient amber radial-glow `drawBehind`, the
  header (`"New task"` / `"Edit task"` + subtitle + close X), the hero Title field, the Notes
  field, a "Due" section label, and the four quick-date chips in a `FlowRow`.
- **`ForgeSelectionChip.kt`** (new) — a real fully-rounded pill with animated background/text
  colour (140ms ease-out) for the quick-date chips. The single-select-with-toggle-off logic in
  `AddTaskContent` (clear all, then set the tapped one unless it was already active) is clean.
- **`ForgePrioritySegment.kt`** (new) — spec-only for now: a full KDoc describing the sliding
  amber-indicator segmented control, but `ForgePrioritySegmented(...)` has an **empty body** and
  isn't called yet. Spec-first placeholder, same pattern as everything else.
- **`ForgeTextField`** — gains `big: Boolean` (72dp/18sp vs 64dp/16sp) and `autoFocus: Boolean`
  (a `FocusRequester` + `LaunchedEffect(Unit)` that requests focus on first composition). Also
  `modifier` moved to the **first** parameter position.

## A real wiring gap (compiles clean, wrong on screen)

`AddTaskSheet` passes **nothing** into `AddTaskContent()`:

```kotlin
) {
    AddTaskContent()          // editingTask, onSave, onDismiss all dropped
}
```

Because every `AddTaskContent` parameter has a default, this compiles — but two things are
wrong at runtime:

- **The X button doesn't close the sheet.** The `IconButton` inside `AddTaskContent` calls the
  content's own `onDismiss`, which here is the defaulted no-op `{ }` — not `AddTaskSheet`'s real
  `onDismiss`. Scrim-tap and drag-down still dismiss (those go through `ModalBottomSheet`'s
  `onDismissRequest`), but tapping X does nothing.
- **Editing never prefills.** `editingTask` never reaches the content, so the header is always
  `"New task"` and the fields always start empty, even when a task is being edited.

`onSave` is also never threaded (and `AddTaskContent` has no `onSave` param + no Save button
yet — see WIP below). Threading `editingTask`/`onSave`/`onDismiss` from `AddTaskSheet` into
`AddTaskContent(...)` is the fix.

## Spec-vs-code drifts

- **Notes field is `big = true`.** The spec is explicit that only the Title is the `big` hero
  (72dp/18sp) and Notes is a normal field (64dp/16sp). Right now both render as 72dp heroes —
  looks like a copy-paste from the Title field. `autoFocus` is correct on both (Title true,
  Notes false).
- **Asymmetric top corners on the sheet.** `RoundedCornerShape(topStart = 14.dp, topEnd = 24.dp)`
  — the two top corners get different radii; the spec says both top corners are 24dp. `14` reads
  like a typo for `24`.

## WIP / not built yet (expected — spec-first)

- `AddTaskContent` stops after the quick-date chips. The picker-fields Row (date + time) is an
  empty `Row { }`; and the whole **Reminder group (§4)**, **Priority segment (§5)**, and **Save
  button (§6)** from the spec aren't in the body at all.
- `ForgePickerField` (the tappable date/time pill the spec calls for) doesn't exist yet.
- `ForgePrioritySegmented` exists but is an empty stub, not yet called.
- `AddTaskSheet` isn't wired into `ForgeNavHost` or the bottom-nav Add button — still a
  standalone component. The Add button's `onAddClick` remains inert (carried from before).

## Minor

- **`ForgePreferences.kt`** gained a redundant self-import:
  `import ...ForgePreferences.Companion.DEFAULT_IS_DARK`. `DEFAULT_IS_DARK` lives in this class's
  own `private companion object`, so `isDarkTheme()` already reaches it via companion scope — the
  import is dead weight (almost certainly an IDE auto-add). Harmless, compiles clean, no warning.
  Behaviour is byte-for-byte identical to your DataStore version.
- `ForgeBottomNavigationBar.kt` — formatting-only reflow this round, no logic change.
- The `modifier`-first reorder on `ForgeTextField` broke no call sites — every caller
  (`ForgePasswordField`, the three auth screens, both `AddTaskSheet` fields) uses named args.
  Verified by grep + clean build.

## Carried over (still open)

- **Your flagged Task List bucket-overlap crash is still live** — `overdue && due == null` lands
  in both TODAY and SOMEDAY → duplicate `LazyColumn` key → crash. Your one-liner
  (`someday = active.filter { it.due == null && !it.overdue }`) is still the fix. Untouched.
- **No mutable task state anywhere** — `onToggle`/`onComplete`/`onAdd` still TODO stubs over the
  immutable top-level `val tasks`. A ViewModel / hoisted `mutableStateListOf` is still the
  bring-it-to-life change.
- Stats week-chart data still mostly hardcoded; `BackHandler` still needs an on-device check;
  onboarding finish/skip → `Login` vs spec's `Registration`; `Motion.kt` empty TODO;
  `SuccessScreen` "Reset prototype" button not deleted; `codelabs` practice-ground drift
  (`com/example/jetnews`, `com/codelab/basiclayouts`) unreverted.
- **ForgeTextField focus ring is STILL an empty stub** — `.drawBehind { /* focus ring, unchanged
  */ }`. Notable this round because Yash *edited this exact file* (added `big`/`autoFocus`) and
  the ring didn't come back with it. **15th note running — restore block at
  `git show 0c6ffa9:docs/HANDOFF.md`.**
- `gradle.properties`: dead `kapt.*`; deprecated `useClasspathSnapshot` warns.

## Toolchain — untouched this round

No `gradle.properties` / `build.gradle.kts` / version-catalog changes from me. AGP still
8.13.2. `ksp.incremental=false`, `hilt { enableAggregatingTask = false }`, `:app:compileJava`
alias all as before. (DataStore dep from your `d23700a` is in and resolving.)

## Screen status — real vs stub

Auth six near-complete. Onboarding feature-complete. Home / Task List / Task Detail visually
complete, navigable, inert below the nav layer. Stats visually complete (numbers placeholder).
Settings feature-complete with the one real piece of app state (theme flag). **Add Task sheet:
container + header + title/notes + quick-date chips built; pickers, reminder, priority, and
save still to come; not yet wired to the Add button.**

Design source is still the **Forge Design System** (external Claude Design project).

## Verified

`./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL**, no Kotlin compiler warnings.
Compile-only — the `AddTaskContent()` wiring gap, the `big`-Notes drift, and the asymmetric
sheet corners all compile clean and only show up on screen or by reading the code.
