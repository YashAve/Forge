# Handoff note — laptop → desktop

**From:** laptop Claude session · **Date:** 2026-07-19 · **On top of:** `65050ea`

Tree is green: `./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL**.

This is the push where the task flow becomes real end to end: Home → Task List → Task
Detail all navigate, with a shared top bar and an id-based detail route.

## ⚠️ Read first

**1. The task-bucket overlap I flagged last note is still there.** `TaskListScreen`:

```kotlin
// mutually exclusive buckets — every active task lands in exactly one   ← not true
val today   = active.filter { it.overdue || (it.due?.startsWith("Today") == true) }
val someday = active.filter { it.due == null }
```

A task with **`overdue = true` and `due = null`** matches both, putting the same `it.id`
key twice in one `LazyColumn` → `IllegalArgumentException: Key "N" was already used…`.
I re-audited all **20** fixture tasks this round: `task3` is the only overdue one and it
has `due = "Yesterday"`, and there are no duplicate ids — so this is still **latent, not a
live crash**. Fix stays one line: `val someday = active.filter { it.due == null && !it.overdue }`.

**2. `TaskListScreen`'s `onBack` and `onAdd` are now dead parameters.** The top bar was
(correctly) extracted to `ForgeTaskTopBar` and hoisted into the nav host's `Scaffold`, so
nothing inside the screen calls either one any more — but `ForgeNavHost` still passes
`onBack = { backStack.removeLastOrNull() }` and an `onAdd` TODO to it. Back *does* work,
via the top bar's own `OnForgeOnBack`; the screen params are just wiring that looks live
and isn't. Same shape in the screen body: a `// ── Top bar ───` comment now heads an empty
region, followed by an orphaned `Spacer(Modifier.height(18.dp))` that used to separate the
bar from the list.

**3. `ForgeNavKey.kt` lost its trailing newline** (`\ No newline at end of file`) and its
class-level KDoc — the block explaining that every key is `@Serializable` so Nav3 can
save/restore the back stack across process death. That "why" isn't recorded anywhere else
now; worth restoring since the next person adding a key needs to know it's a hard
requirement, not a style choice.

## What this push adds (Yash's code)

**`TaskDetailScreen` is real** — scrollable detail with the priority/project header, large
title, due chip, notes, a `ForgeSectionLabel` subtask header carrying a live
`${task.subtasks.count { it.isDone }}/${task.subtasks.size}` count, per-subtask
`ForgeCheckbox`es each wired to `onToggleSub(subtask.id)` (correctly the *subtask* id, not
the task's), and complete/reopen actions with `Check` / `Undo` icons.

**`ForgeTaskTopBar` extracted as a shared component** — a titled bar with `onStart` /
`onEnd` composable slots, hoisted into the nav host `Scaffold`'s `topBar` `when`. TaskList
renders it as "All tasks" + Add; TaskDetail as "Task" + Edit. This is the reusable
title-bar variant the Task List spec asked for, and it also removes the manual
`.offset(x = ±10.dp)` optical hacks the inline version needed.

**`OnForgeOnAdd` and `OnForgeOnEdit`** join the existing `OnForgeOnBack` slot pattern.

**Navigation is wired end to end.** `TaskDetail` carries `taskId: String` (not a `Task` —
Nav3 keys must stay serializable primitives); the entry looks the task up with
`tasks.find { it.id == key.taskId }` behind an `if (task != null)` guard; both Home's focus
card / up-next rows and every Task List row push it. Boot is back on **`Splash`**, so the
full splash → onboarding → home → list → detail run-through works.

**Fixture doubled**: 10 → 20 tasks, and `Task` / `Subtask` / `Priority` are now
`@Serializable`.

**Onboarding Mechanic + Promise**: design-spec KDoc consumed by the implementations; the
Mechanic hero was restructured from the fixed 72dp positioning box to a 32dp label row with
`verticalArrangement = Arrangement.Center`.

### Fixed from my last notes ✅

- **Bottom-nav tabs now replace instead of pushing** — `backStack.clear()` then
  `backStack.add(key)`. This one had been carried since the first nav note; tapping tabs no
  longer grows the stack forever.
- **The debug `Toast` on every tab tap is gone.**
- **`ForgeBottomNavigationBar` now takes a named `onAddClick`** instead of a dead trailing
  lambda — still a TODO body, but it's a real named slot now.
- **`BackHandler`'s `when` has an explicit `else -> {}`** rather than silently falling
  through.
- **A live bug fixed in passing**: `HomeScreen`'s up-next `forEach` passed
  `onOpen = { onOpen(focus) }` — every up-next row would have opened the *focus* task's
  detail instead of its own. Now `onOpen(task.id)`.

## Rough edges (documented, not fixed)

**`BackHandler` still swallows hardware back on Home / Stats / Settings / Splash /
Onboarding** — the `else -> {}` makes that explicit now, so it reads as deliberate rather
than accidental. Note **`TaskDetail` is not in the `when` either**, so hardware back there
depends entirely on `NavDisplay`'s own `onBack` winning the dispatcher. The top-bar back
button works regardless, so this needs one on-device check rather than a code change —
still the longest-running unverified item.

**`ForgeTaskTopBar`'s `onStart` / `onEnd` are `@Composable` slots, not callbacks.** The
`on*` prefix reads as an event handler everywhere else in this codebase (`onBack`,
`onAdd`), so the signature `onStart: @Composable () -> Unit` is a small trap for the next
caller — `leading` / `trailing` would say what they are. Also the default
`label = "All Tasks"` (capital T) disagrees with the call site's `"All tasks"`.

**`OnForgeOnAdd` / `OnForgeOnEdit` are top-level `val` composable lambdas, not
`@Composable fun`s.** Consistent with `OnForgeOnBack`, so it's the house pattern rather
than drift — but PascalCase `val`s that read as functions can't be `@Preview`ed directly
(hence the separate wrapper previews in each file) and don't get the compiler's
restartable/skippable treatment the way a real composable function does. Harmless at three
call sites; worth a deliberate decision before the pattern spreads further.

**The `TaskDetail` if-guard fails silently.** An id with no match renders nothing — but
`ForgeTaskTopBar` for `TaskDetail` lives in the `Scaffold`'s `topBar`, *outside* the guard,
so a bad id gives a "Task" bar over blank space with no clue why. Fine while every id comes
from the same in-memory list; a real trap once ids arrive from a database or deep link.

**`@Serializable` on `Task` / `Subtask` / `Priority` is now vestigial for its original
purpose** — the nav key carries only a `String`. Harmless, and it'll be wanted the moment
anything is persisted, but it's no longer load-bearing for navigation.

**SOMEDAY still never renders.** Even at 20 tasks, every active task has a `due`; the only
`due == null` task is `task8`, which is `done`. That branch has still never been seen on
screen.

**Still no `ForgeEmptyState` on Task List** for the all-empty case, per the spec.

**Still no state anywhere.** `onToggle`, `onComplete`, `onToggleSub`, `onAdd`, `onEdit` are
all TODO stubs against a top-level immutable `val tasks`. The detail screen's checkboxes
animate and then snap back; nothing survives a recomposition, let alone navigation. Three
screens are now visually complete and fully navigable with **zero** mutable state behind
them — a ViewModel (or a hoisted `mutableStateListOf`) is the single change that lights all
three up at once, and it's now clearly the next piece of work.

## Carried over (still open)

- Bottom-nav icon outline→filled swap still doesn't happen (`activeIcon` always).
- Bottom-nav Add button now has a named `onAddClick` but still an empty TODO body.
- `HomeScreen` / `TaskListScreen` both default `tasks` to the fully-qualified
  `com.development.forge.domain.model.tasks` inline rather than importing it.
- `ForgeTaskRow`'s KDoc still says `onOpen` is "called with the task id" — it's `() -> Unit`
  now, with the caller supplying the id.
- `ForgeEmptyState`'s default body hardcodes a `\n` while `widthIn(max = 280.dp)` +
  `TextAlign.Center` already handle the wrap.
- `ForgeCard` defaults still `accent = true` / `onClick = { }` vs spec's `false` / `null`.
- `ForgeSectionLabel` aligns `CenterVertically` while its KDoc says **baseline**.
- `DueChip`'s `"High"` default label; `PriorityChip`'s non-exhaustive `when(level)`.
- Onboarding finish/skip → `Login` vs spec's `Registration`.
- `Motion.kt` still an empty TODO while screens hand-roll easing.
- `designsystem/Color.kt` still carries none of the palette — `0xFFF5EDDE` alone is inlined
  ~40× across the app, including in all three new components.
- `gradle.properties`: dead `kapt.*` block; `org.gradle.tooling.parallel=true` inert on
  Gradle 8.13; deprecated `kotlin.incremental.useClasspathSnapshot` still warns.
- `SuccessScreen` "Reset prototype" button still not deleted.
- **`codelabs/original/` drift is still unreverted** — `AccessibilityCodelab/.../HomeScreen.kt`
  and `BasicLayoutsCodelab/.../MainActivity.kt` still carry `fun HomeScreen` →
  `private fun HomeScreen` from an IDE quick-fix. `git checkout 55575c3 -- codelabs/original/`.
- Auth tail unchanged: EmailSent countdown/cooldown/unwired resend · px ember/width/
  textAlign · ForgeApplication dead font warm-up · email not threaded through nav ·
  letterSpacing typos · `isLoading` restore trap · inline ForgeDivider ·
  **ForgeTextField focus ring still deleted (18th note running — restore block at
  `git show 0c6ffa9:docs/HANDOFF.md`).**

## Toolchain

Unchanged. Laptop runs Android Studio Quail 2's healthy JBR 21.0.10; launcher and
`org.gradle.java.home` both resolve to it. No repo toolchain config touched this round.

## Screen status — real vs stub

Auth six near-complete. Onboarding feature-complete. **Home / Task List / Task Detail: all
visually complete and fully navigable between each other** — and all three inert below the
navigation layer. **Stats / Settings**: stubs. **Bottom nav**: tabs now replace correctly,
active colour works, icon swap doesn't, Add button named but empty; correctly hidden on
Task List and Task Detail. Boot seeds at `Splash`, so the whole flow is walkable.

Design source is still the **Forge Design System** (external Claude Design project).

## Verified

`./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL** (full run on this exact tree).
Pre-existing warnings only: deprecated `useClasspathSnapshot`, and the KSP/Hilt "no
dependencies reported" note that comes with `ksp.incremental=false`. **Compile only — the
app was not launched this round**, so the splash → onboarding → home → list → detail flow
is verified to build, not verified to run.
