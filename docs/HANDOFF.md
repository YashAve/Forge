# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-07-27 · **On top of:** `5123181`

Small, hand-written round — three files touched, no new screens. Verified:
`./gradlew :app:compileDebugKotlin --rerun-tasks` → **BUILD SUCCESSFUL** (only the two
pre-existing benign warnings: SDK XML v4 notice, Hilt/KSP `ForgeApplication_ComponentTreeDeps`
incremental note).

## What changed

- **`ForgeNavHost.kt` — app locked to portrait.** A `DisposableEffect(Unit)` now sets
  `activity?.requestedOrientation = SCREEN_ORIENTATION_PORTRAIT` for the lifetime of the nav
  host (reset to `UNSPECIFIED` in `onDispose`, which in practice only fires on Activity
  teardown). `@SuppressLint("SourceLockedOrientationActivity")` silences the lint that flags
  hardcoding orientation on a non-Activity — appears intentional.
- **`ForgeNavHost.kt` — `BackHandler` widened.** `Login, Registration -> backStack.clear()`
  became `Login, Registration, Home -> backStack.clear()`, and a new branch handles
  `Statistics, Settings -> { backStack.clear(); backStack.add(Home) }` (back from either tab
  now lands on Home instead of doing nothing).
- **`SettingsScreen.kt`** — preview/default `email` param changed
  (`sanmeet.kaur@…` → `karishma.kapoor@…`). Cosmetic, no behavior change.
- **`StatsScreen.kt`** — bottom spacing moved from a `Column.padding(bottom = 32.dp)` to a
  trailing `Spacer(height = 32.dp)`. Same rendered result, different mechanism.

## ⚠️ Read first — likely bug, not fixed (per practice-mode convention)

**Back button on Home now blanks the screen.** `BackHandler` is registered unconditionally
(no `enabled = false` anywhere), so it always swallows the system back gesture — there's no
fallback to default Activity-finish behavior. Previously `Home` fell into the `else -> {}`
branch, so back on Home was a no-op (back button inert on the root screen). Now `Home` is in
the same bucket as `Login`/`Registration` — `backStack.clear()` — but unlike those two,
**nothing repopulates the stack afterward**. `Login`'s clear() is always followed by an
`onLogin`/`onRegister` callback that re-adds an entry; Home's clear() has no such follow-up.
Net effect: pressing system back on the Home tab empties `backStack`, `NavDisplay` has nothing
to show, and the app is stuck on a blank screen with no way back in (back is fully swallowed,
so it doesn't even fall through to exiting the app). Did not touch this — not sure yet whether
the intended fix is "add `Home` back to the `else` no-op bucket" or "finish the activity", and
that's a product decision, not mine to make silently.

## Carried over (unchanged since last note, still open)

The whole list from the last handoff is still accurate — this round didn't touch any of it:

- **No ViewModel / no mutable task state** — `onToggle`/`onComplete`/`onAdd`/`onSave` are TODO
  stubs over the immutable top-level `val tasks`. This is the entire remaining wiring job now
  that both the UI and the MongoDB/JWT backend exist.
- **New-task capture unreachable** — `AddOrEditTaskScreen(editingTask = tasks[0])` default +
  nav never passing `null`, so **+** always opens "Edit Task" on task[0].
- **`ForgeDatePickerDialog` NPEs** on OK-without-selection (`selectedDateMillis!!`).
- Save hardcoded (`derivedStateOf { true }`); date/time picker selections not plumbed back.
- **`AddTaskSheet.kt` (~24KB) still orphaned** — safe to delete.
- **Task List bucket-overlap crash still live** (`overdue && due == null` → duplicate
  `LazyColumn` key). Fix: `someday = active.filter { it.due == null && !it.overdue }`.
- `dueAt: Instant?` unused; server's `Task` mirrors the enumerated `due: String?` only — if
  `dueAt` becomes real client-side, the DTO needs it too.
- Stats hardcoded; onboarding finish/skip → `Login` vs spec's `Registration`; `Motion.kt`
  empty; `SuccessScreen` reset button not deleted.
- **ForgeTextField focus ring still an empty `drawBehind { }` stub — 22nd note running.**
- `.idea/` still uncommitted (JDK 11→17 flip + stale `gradle.xml` `lib` entry). Your call.
- `gradle.properties`: dead `kapt.*`; deprecated `useClasspathSnapshot` warns.
- Server-side items from the backend push (dev JWT secret in source, `/auth/forgot` stub logs
  to console, Mongo container restart-looping — see git log on `5123181` for full detail if
  needed).

## Verified

`./gradlew :app:compileDebugKotlin --rerun-tasks` (forced, not cached) → BUILD SUCCESSFUL.
Did not run the server this round — no server files touched.
