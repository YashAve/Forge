# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-07-15 · **On top of:** `1681fae`

Tree is green: `./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL**.

## What this push adds (Yash's code)

**`HomeScreen.kt` gets a full design spec** (KDoc only — the composable body is
still the `Text("HomeScreen")` stub). Documents the greeting row, the focus card,
up-next list, empty state, and lists six new components to build first
(`ForgeCard`, `ForgeSectionLabel`, `ForgeCheckbox`, `ForgePriorityChip`,
`ForgeDueChip`, `ForgeTaskRow`) plus the `Task` model extension this screen needs
(id/title/done/priority/due/overdue/project/notes/subtasks). Same "spec first,
build the piece standalone, wire later" pattern as Mechanic/Discipline/Home before
it.

**New file `ForgeBottomNavigationBar.kt`** — first pass at the 4-tab bottom nav
(Focus/Add/Stats/You), spec written, not yet wired into `ForgeNavHost`. `Oil`
(`#1C180F`, the raised-surface colour) added to `Color.kt` for it; three new
`ForgeNavKey` entries (`Add`, `Statistics`, `Settings`) support it.

## A real bug in the new nav bar, not just a doc gap

Distinct from the usual "spec vs. code drifted" notes — this one **compiles clean
but is functionally broken**, the same flavor as the `Alignment.Center as
Alignment.Horizontal` cast from a few rounds back:

```kotlin
val color by animateColorAsState(
    targetValue = if (current == navigations[2]) Amber else FgTertiary, ...
)
navigations.forEachIndexed { index, navigationItem ->
    Column(...) { Icon(...); Text(..., color = color) }
}
```

- `color` is computed **once, outside** the `forEachIndexed` loop, then applied to
  **all four** tabs alike. It needs to be computed per-item, inside the loop,
  comparing each row's own key against `current`.
- The comparison itself can never be true regardless: `current` is a
  `ForgeNavKey`, but `navigations[2]` is the raw `List<Any>` row
  (`listOf(2, ForgeNavKey.Statistics, "Stats", icon)`) — comparing a `ForgeNavKey`
  to a `List<Any>` always evaluates `false`. It should be comparing `current` to
  `navigationItem[1] as ForgeNavKey` inside the loop.
- Kotlin doesn't warn on this — `==` between two unrelated reference types is
  legal (falls back to `Any.equals`), so there's no compiler diagnostic pointing
  at it. Net effect right now: every label always renders `FgTertiary`, no tab
  ever highlights, regardless of `current`.

Also incomplete (documented, not fixed — this is clearly still WIP, not a finished
component): the per-tab outline→filled icon swap from the spec isn't implemented
(icons are fixed at whatever's in the `navigations` list, never conditionally
swapped); the Add button doesn't get its amber-pill treatment yet, it renders as
an ordinary tab; the top 1dp hairline border is an empty `drawBehind { }` stub;
`onSelect`/`onAddClick` are never called — the `clickable` on each item only
writes to a local `selected` var that nothing else reads, so tapping currently
does nothing observable. `navigations` itself is a `List<List<Any>>` (index/key/
label/icon mixed in one row), so every read needs an unchecked cast — a fragile
shape, but consistent with "first pass, not wired in yet."

## Carried over (still open)

- Onboarding finish/skip routes to `Login`; the nav-spec KDoc directly above it
  says `Registration` — still unresolved, flagged last round, not touched since.
- The `BackHandler { clear() }` on Login/Registration for "exit app" — still
  unverified on a real device; a second back-press consumer (`NavDisplay`'s own
  `onBack`) is also in the tree and I haven't chased down which wins.
- Mechanic: bar still a sibling of the 72dp label box, not pinned inside it;
  fill-gradient stops still in track-coords not fill-coords; `.border()` before
  `.clip()`/`.background()`; tick row inline `#7A7060` vs `FgTertiary` token
  elsewhere. Ember-dot polish (highlight-offset comment mismatch, 1.5dp vs 2dp
  rim, glow bleeding past the 14dp Canvas) — untouched.
- `Motion.kt` still an empty TODO, three screens hand-roll easing; ring alpha
  wants `keyframes` (hold at 0 from 80%), currently plain `tween`.
- Two dead `gradle.properties` blocks (deprecated
  `kotlin.incremental.useClasspathSnapshot`; dead `kapt.*` section) — untouched.
- `SuccessScreen`'s "Reset prototype" button — the nav spec KDoc says delete it,
  not yet done.
- The long auth-flow tail, unchanged: EmailSent countdown ("0:0:45", no entry
  cooldown, `onResend`/`onOpenEmail` unwired) · EmailSent px ember/width/textAlign
  · ForgeApplication dead font warm-up · ResetPassword→EmailSent chain is a
  prototype simplification (real flow is a deep link) · email not threaded
  through nav · headline letterSpacing typos · `isLoading` restore trap · inline
  ForgeDivider · `ForgeNavKey.Registration: NavKey` sealed-exhaustiveness break ·
  **ForgeTextField focus ring still deleted (12th note running — restore block at
  `git show 0c6ffa9:docs/HANDOFF.md`).**

## Toolchain — untouched this round

`ksp.incremental=false`, `hilt { enableAggregatingTask = false }`, and the
`:app:compileJava` alias are all exactly as before. No gradle config touched.

## Screen status — real vs stub

Auth six near-complete. Onboarding: Promise done, Mechanic hero essentially
complete, Discipline feature-complete (text + HeroTaskStack). Real navigation
connects Splash → Onboarding → Login/Registration → Success/Home. **Home**: spec
fully written, screen body still a stub; its six supporting components
(ForgeCard, ForgeSectionLabel, ForgeCheckbox, ForgePriorityChip, ForgeDueChip,
ForgeTaskRow) don't exist yet. **Bottom nav bar**: spec written, first-pass build
in progress (bug above), not wired into `ForgeNavHost`. Tasks/Stats/Settings still
untouched scaffold stubs.

Design source is still the **Forge Design System** (external Claude Design project).

## Verified

`./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL**. Compile-only, as
always — the bottom nav bug above doesn't surface as a compile error or warning,
only on-screen (or by reading it, which is how this note found it).
