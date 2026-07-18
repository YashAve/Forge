# Handoff note — laptop → desktop

**From:** laptop Claude session · **Date:** 2026-07-18 · **On top of:** `d5c5ebb`

Tree is green: `./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL**.

## ⚠️ Read first

**1. Back stack is seeded at `Home` again.** `rememberNavBackStack(ForgeNavKey.Home)` —
I reported this fixed to `Splash` last push (`d5c5ebb`); Yash flipped it back this round
to iterate on Home. It's a dev-view hook, not a regression to chase. Restore `Splash`
before any real run-through.

**2. Two `codelabs/original/` files were modified — almost certainly by accident.**
`AccessibilityCodelab/.../ui/home/HomeScreen.kt` (two overloads) and
`BasicLayoutsCodelab/.../MainActivity.kt` each had `fun HomeScreen` changed to
**`private fun HomeScreen`**. Nothing in this round's Forge work touches those modules,
and all three are named `HomeScreen` — this reads like an IDE quick-fix ("make private")
or a rename that got applied project-wide while working on Forge's own `HomeScreen`.
Per the codelabs convention, `original/` is **untouched reference solutions** (`mine/` is
where Yash works), so this is drift that should probably be reverted:
`git checkout <commit> -- codelabs/original/`. Committed as-is rather than silently
reverted, per the usual rule — flagging so it isn't mistaken for intentional.

**3. `gradle.properties` gained `org.gradle.tooling.parallel=true`** (comment: "Enabled
parallel sync for Gradle 9.4+"). This project is on **Gradle 8.13**, so the flag is inert
today — harmless, but it buys nothing until a 9.x upgrade. Worth knowing it's there so
it isn't credited for a speedup it can't deliver. The deprecated
`kotlin.incremental.useClasspathSnapshot` still warns on every build (confirmed in this
run's output) — still untouched, one change at a time.

## What this push adds (Yash's code)

**`Task.kt` becomes a real domain model + fixture set.** `Task` gains `done`, `due`,
`overdue`, `project`, `notes`; `Priority` gains a `label`; and the empty `sampleTasks`
list is replaced with a proper `tasks` fixture — varied priorities, projects, due states,
notes and subtasks. This is what makes Home renderable without a data layer.

**`HomeScreen` is now fully assembled** — the biggest jump this round. Real greeting
(time-of-day aware via `Calendar`, live date + done-count), the accent `ForgeCard` focus
card built to spec (priority chip + project, 26sp title, due chip, "Mark it done"), the
**Up Next** list via the new `ForgeTaskRow`, a "See all" section label, and
`ForgeEmptyState` when nothing's open. It derives `focus`/`upNext`/`doneToday` from the
task list rather than hardcoding — the hardcoded greeting strings I flagged are gone.

**`ForgeTaskRow` is new and real** — checkbox + title/metadata + priority chip, with the
whole-row 0.5-alpha fade for done tasks, line-through title, ellipsis truncation via
`weight(1f)`, and compact/normal sizing. Fully KDoc-specced.

**`ForgeEmptyState` is real** — 88dp amber badge, title, 280dp-constrained body, CTA.

**`TaskListScreen`**: full design spec added as KDoc (113 lines), body still the
placeholder stub. New `ForgeNavKey.TaskList` + a `NavDisplay` entry for it.

### Fixed from my last note ✅

- **`ForgeCheckbox`'s check mark now animates** — the headline gap is closed: scale via
  the overshoot curve `CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)` over 180ms, separate
  120ms ease-out alpha, both applied through one `graphicsLayer` on the Icon. The mark no
  longer renders when unchecked. The stray empty `graphicsLayer { }` and the duplicated
  KDoc block are gone, and `onChange` is now `(Boolean) -> Unit` called as
  `onChange(!checked)` — matches spec.
- **`ForgeSectionLabel` now matches its spec**: `action: String? = null`,
  `onAction: (() -> Unit)? = null`, right side gated on `action != null`. Note the
  alignment fix landed as `Alignment.CenterVertically`, not true **baseline** alignment —
  much better than the old `Top` default, but the KDoc still says "text BASELINE", so
  that one-line disagreement survives (`alignByBaseline()` if you want it exact).

## Rough edges (documented, not fixed)

**Home renders beautifully but every interaction is inert — the same "renders, wired to
nothing" shape as the Add button.** `HomeScreen` correctly exposes `onToggle`, `onOpen`,
`onSeeAll`, `onAdd`, but `ForgeNavHost` calls `HomeScreen(basicModifier)` and passes none
of them, so all four fall back to `{ }` defaults. Tapping the focus card, "Mark it done",
a task row, or "See all" does nothing.

**Even if they were wired, toggling can't work yet.** `tasks` is a top-level `val` list
of immutable `Task`s with no state holder, so `onToggle(id)` has nothing to mutate — Home
needs a ViewModel (or at minimum a `remember { mutableStateListOf() }`) before the
checkbox can actually complete a task. The checkbox animation is real; the state change
behind it isn't.

**`ForgeNavKey.TaskList` is registered but unreachable.** The entry exists in
`NavDisplay`, but nothing anywhere calls `backStack.add(TaskList)` — and the screen it
routes to is still the placeholder stub. `HomeScreen`'s "See all" (`onSeeAll`) is the
intended trigger; it's the natural first wiring once the callbacks are threaded.

**`HomeScreen`'s `tasks` default is the fully-qualified
`com.development.forge.domain.model.tasks`** inline in the parameter list rather than an
import — inconsistent with every other reference in the file. Cosmetic.

**`ForgeEmptyState`'s default `body` hardcodes a `\n`** ("line up\nwhat's next.") to force
the wrap, while also having `widthIn(max = 280.dp)` + `TextAlign.Center` doing that job
properly — the manual break will fight the constraint at other font scales.

**`ForgeCard` defaults unchanged** from last note: `accent = true` and
`onClick: (() -> Unit)? = { }` (non-null empty lambda) vs spec's `false`/`null`. Now less
load-bearing since `HomeScreen` passes both explicitly, but a bare `ForgeCard {}` is still
an accent card that's tappable-but-dead.

## Carried over (still open)

- Tabs still push instead of replacing (`backStack.add`, no `clear()`) + debug `Toast` on
  every tab tap. Icon outline→filled swap still doesn't happen (`activeIcon` always).
- Bottom-nav Add button still wired to nothing (two dead `{ }` links: bar → nav host).
- `DueChip`'s `"High"` default label; `PriorityChip`'s non-exhaustive `when(level)`.
- Onboarding finish/skip → `Login` vs spec's `Registration`; `BackHandler { clear() }` vs
  NavDisplay's `onBack` still unverified on device.
- Mechanic polish (bar-vs-72dp-box, fill-gradient coords, `.border()` before `.clip()`,
  inline `#7A7060`); `Motion.kt` still an empty TODO while screens hand-roll easing.
- Dead `kapt.*` block in `gradle.properties`; `SuccessScreen` "Reset prototype" button
  still not deleted.
- Auth tail unchanged: EmailSent countdown/cooldown/unwired resend · px ember/width/
  textAlign · ForgeApplication dead font warm-up · email not threaded through nav ·
  letterSpacing typos · `isLoading` restore trap · inline ForgeDivider ·
  **ForgeTextField focus ring still deleted (16th note running — restore block at
  `git show 0c6ffa9:docs/HANDOFF.md`).**

## Toolchain

Laptop JBR incident from last note is **resolved** — the corrupted JBR was replaced with
Android Studio Quail 2 stable's healthy JBR 21.0.10 and the backup deleted after
verification. Catalog-accessor compilation was regression-tested and works. The laptop's
IDE launcher now points at the Quail 2 install; `org.gradle.java.home` still resolves to
the same healthy runtime. No repo toolchain config changed beyond the tooling flag noted
above.

## Screen status — real vs stub

Auth six near-complete. Onboarding feature-complete. **Home: effectively complete
visually** (greeting, focus card, up-next list, empty state) but fully inert and
data-less. **Task List**: spec'd, still a stub. **Stats/Settings**: stubs. **Bottom nav**:
active colour works; Add button renders, wired to nothing. Boot seeds at Home.

Design source is still the **Forge Design System** (external Claude Design project).

## Verified

`./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL** (full run on this exact tree).
Two pre-existing warnings only: the deprecated `useClasspathSnapshot` property and the
KSP/Hilt "no dependencies reported" note that comes with `ksp.incremental=false`.
