# Handoff note — laptop → desktop

**From:** laptop Claude session · **Date:** 2026-07-19 · **On top of:** `28f3f04`

Tree is green: `./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL**.

## ⚠️ Read first

**1. The task buckets are not mutually exclusive — latent duplicate-key crash.**
`TaskListScreen` splits active tasks three ways and the inline comment claims "every
active task lands in exactly one". It doesn't:

```kotlin
val today   = active.filter { it.overdue || (it.due?.startsWith("Today") == true) }
val someday = active.filter { it.due == null }
```

A task with **`overdue = true` and `due = null`** satisfies both — it lands in `today`
*and* `someday`. Both sections render `items(..., key = { it.id })` into the **same**
`LazyColumn`, so the same key is used twice and Compose's saveable state holder throws
`IllegalArgumentException: Key "N" was already used…` as soon as the second copy composes
(possibly only after a scroll, which makes it worse to reproduce). Minimal fix:

```kotlin
val someday = active.filter { it.due == null && !it.overdue }
```

**Not reachable with today's fixture** — no active task has that combination — so this is
latent, not a live crash. It becomes live the moment real data produces an overdue task
with no due string. Documented, not fixed, per the usual rule.

**2. Back stack is still seeded at `Home`.** `rememberNavBackStack(ForgeNavKey.Home)` —
unchanged from my last note, still a dev-view hook so Yash lands on Home without walking
the auth flow. Restore `Splash` before any real run-through.

**3. The `codelabs/original/` drift from last push is still there.** Both
`AccessibilityCodelab/.../ui/home/HomeScreen.kt` and `BasicLayoutsCodelab/.../MainActivity.kt`
still carry `fun HomeScreen` → **`private fun HomeScreen`** from an IDE quick-fix that
leaked while working on Forge's own `HomeScreen`. Not reverted this round.
`git checkout 55575c3 -- codelabs/original/` restores them.

## What this push adds (Yash's code)

**`TaskListScreen` goes stub → real — and it's the first `LazyColumn` in the codebase.**
Four grouped sections (TODAY / UPCOMING / SOMEDAY / DONE), each with a live count in the
header (`"TODAY · ${today.size}"`), each skipped entirely when empty, done always last.
Rows are `ForgeTaskRow(compact = false)` — the roomy variant — with `key = { it.id }` on
every `items()` call, exactly as the spec asked. Section spacing is 18dp between active
groups and 24dp before DONE.

**A hand-rolled top bar**: back arrow + "All tasks" (24sp Space Grotesk, W600) on the left,
add action on the right.

**"See all" is wired — the first real Home → detail navigation in the app.**
`HomeScreen(basicModifier, onSeeAll = { backStack.add(ForgeNavKey.TaskList) })`, and
`TaskListScreen(basicModifier, onBack = { backStack.removeLastOrNull() })` for the return
trip, plus a matching `ForgeNavKey.TaskList` branch in the `BackHandler` for hardware back.

**Chrome routing matches the spec's decision**: `TaskList` was added to the *no top bar*
branch (the screen draws its own) and deliberately left out of the `bottomBar` `when`, so
no bottom nav — correct for a pushed screen rather than a nav tab.

**`task6`** content refreshed: October → November in both title and notes.

### Fixed from my last note ✅

- **`ForgeNavKey.TaskList` is no longer registered-but-unreachable**, and it no longer
  routes to a placeholder. Both halves of that item are closed in one push — the route is
  live from Home's "See all" and the destination is a real screen.

## Rough edges (documented, not fixed)

**The `SOMEDAY` section has never actually rendered.** Every active task in the fixture has
a `due`; the only `due == null` task is `task8`, which is `done`. So that whole branch is
untested visually — worth adding a null-due active task to the fixture just to eyeball it
(and it would also surface the bucket bug above if it were also overdue).

**No empty state.** The spec calls for `ForgeEmptyState` when active *and* done are both
empty ("No tasks yet." / "Add your first one and let Forge keep the fire lit." / "Add a
task"). The implementation has no such branch, so an empty list renders as a top bar over
blank space. `ForgeEmptyState` is real as of last push, so this is a few lines.

**The top bar is local, not `ForgeTopBar`.** The spec offered two routes — extend
`ForgeTopBar` to take a title, or build a small titled variant — and this took neither; it's
a `Row` with two raw `IconButton`s inline in the screen. Fine for now, but it means the
"title + trailing action" bar can't be reused by the next pushed screen. The
`.offset(x = (-10).dp)` / `.offset(x = 10.dp)` on the two icon buttons are manual optical
corrections fighting the Row's own 24dp padding — dropping the padding to 14dp and deleting
both offsets gets the same result without the two-step.

**Everything except navigation is still inert.** `ForgeNavHost` passes `onSeeAll` to Home
and `onBack` to TaskList, and nothing else — `onToggle`, `onOpen`, `onAdd` fall back to
`{ }` on **both** screens. And even fully threaded they'd be no-ops: `tasks` is still an
immutable top-level `val` with no state holder, so a checkbox tap has nothing to mutate.
**This is now the single biggest blocker** — two visually complete screens with one live
route between them and zero state behind either. A ViewModel (or at minimum a hoisted
`mutableStateListOf`) is the unblocking move, and it lights up both screens at once.

**`ForgeTaskRow`'s callbacks are `(String) -> Unit`** but the call sites pass
`onToggle = { onToggle(task.id) }` — ignoring the `it` the row already supplies and
re-deriving the id from the captured `task`. Compiles and behaves identically; just
redundant, and it'll read as a bug to the next person.

**`BackHandler` still swallows back everywhere else.** It's unconditionally enabled and its
`when` has no `else`, so on Home / Stats / Settings / Splash / Onboarding, back silently
does nothing. TaskList is safe by coincidence — the new branch and `NavDisplay`'s own
`onBack` both call `removeLastOrNull()`, so it works whichever one wins the dispatcher.
Still unverified on device (carried item, now load-bearing on a path that's actually used).

**The 113-line design spec KDoc was consumed** by the implementation, as intended. If you
want to diff intent against result: `git show 28f3f04:app/src/main/java/com/development/forge/feature/tasks/TaskListScreen.kt`.

## Carried over (still open)

- Tabs still push instead of replacing (`backStack.add`, no `clear()`) + debug `Toast` on
  every tab tap. Icon outline→filled swap still doesn't happen (`activeIcon` always).
- Bottom-nav Add button still wired to nothing (two dead `{ }` links: bar → nav host).
- `HomeScreen`/`TaskListScreen` both default `tasks` to the fully-qualified
  `com.development.forge.domain.model.tasks` inline rather than importing it.
- `ForgeEmptyState`'s default body hardcodes a `\n` while `widthIn(max = 280.dp)` +
  `TextAlign.Center` already handle the wrap.
- `ForgeCard` defaults still `accent = true` / `onClick = { }` vs spec's `false` / `null`.
- `ForgeSectionLabel` aligns `CenterVertically` while its KDoc says **baseline**.
- `DueChip`'s `"High"` default label; `PriorityChip`'s non-exhaustive `when(level)`.
- Onboarding finish/skip → `Login` vs spec's `Registration`.
- Mechanic polish (bar-vs-72dp-box, fill-gradient coords, `.border()` before `.clip()`,
  inline `#7A7060`); `Motion.kt` still an empty TODO while screens hand-roll easing.
- `designsystem/Color.kt` exists but carries none of the palette — `0xFFF5EDDE` alone is
  inlined 38× across the app. Not new drift, but it grows every push.
- `gradle.properties`: dead `kapt.*` block; `org.gradle.tooling.parallel=true` still inert
  on Gradle 8.13; deprecated `kotlin.incremental.useClasspathSnapshot` still warns.
- `SuccessScreen` "Reset prototype" button still not deleted.
- Auth tail unchanged: EmailSent countdown/cooldown/unwired resend · px ember/width/
  textAlign · ForgeApplication dead font warm-up · email not threaded through nav ·
  letterSpacing typos · `isLoading` restore trap · inline ForgeDivider ·
  **ForgeTextField focus ring still deleted (17th note running — restore block at
  `git show 0c6ffa9:docs/HANDOFF.md`).**

## Toolchain

Unchanged since the JBR fix. Laptop runs Android Studio Quail 2's healthy JBR 21.0.10;
launcher and `org.gradle.java.home` both resolve to it. No repo toolchain config touched.

## Screen status — real vs stub

Auth six near-complete. Onboarding feature-complete. **Home: visually complete**, one live
outbound route (See all), everything else inert. **Task List: now visually complete** —
grouped LazyColumn, back navigation works; no empty state, all row interactions inert.
**Stats/Settings**: stubs. **Bottom nav**: active colour works; Add button renders, wired to
nothing; hidden on Task List as intended. Boot seeds at Home.

Design source is still the **Forge Design System** (external Claude Design project).

## Verified

`./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL** (full run on this exact tree).
Pre-existing warnings only: deprecated `useClasspathSnapshot`, and the KSP/Hilt "no
dependencies reported" note that comes with `ksp.incremental=false`.
