# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-07-17 · **On top of:** `647e6c7`

Tree is green: `./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL**, no warnings.

## What this push adds (Yash's code)

**The Home screen's building blocks go from stubs to real.** This is the first push where
the components the HomeScreen spec asked for actually exist:

- **`ForgeChips.kt`** — `DueChip` and `PriorityChip` are now real. `PriorityChip` takes a
  `Priority` (`LOW/MED/HIGH` from the already-tracked `domain/model/Task.kt`) and a `dense`
  flag, and switches background/text/dot colour per level (amber fill for HIGH, amber-16% for
  MED, dark chip + cream-10% border for LOW). `DueChip` is a schedule-icon + mono-label row
  that turns red (`#E8624D`) when `overdue`.
- **`ForgeSectionLabel.kt`** — real now: a `SpaceBetween` row with an uppercase mono label on
  the left and an optional `ForgeTextButton` action on the right.
- **`ForgeTextButton.kt`** — gains a `fontSize` parameter (defaults to 14sp) so
  `ForgeSectionLabel` can render its action at the spec's 13sp without a second button variant.

**`HomeScreen` gets a real body** (was a one-line centered stub): a scrolling `Column` with a
greeting row (greeting + mono date/done-count on the left, `ForgeWordmark` on the right) and
the beginnings of the focus card.

**The bottom nav's Add button now renders.** New `ForgeBottomNavigationBarAddButton` — the
amber pill (44×32, 12dp radius), the amber-35% `dropShadow` glow offset 4dp down, the dark
`#1A1207` plus-icon. The top 1dp hairline border is now a real `drawLine` (was an empty
`drawBehind {}` stub the last two rounds), and the dead `context`/`selected` locals I flagged
last round are gone. `current` is now threaded into each `ForgeBottomNavigationBarItem`, so the
active-state colour is live across all three tabs.

## Rough edges (documented, not fixed)

**The Add button renders beautifully but is wired to nothing — the deeper version of last
round's "renders but does nothing".** `ForgeBottomNavigationBarAddButton` takes an `onSelect`
lambda, but `ForgeBottomNavigationBar` passes it an empty `{ }`. So the bar's own `onAddClick`
parameter is *still* never invoked, and `ForgeNavHost` passes an empty trailing lambda on top
of that. Two dead links in a row: tapping Add does nothing, and there's no compile signal for
it because empty lambdas are perfectly legal. Same shape as the earlier inert `onSelect`, just
one layer down.

**The focus card is a raw Material3 `Card`, not the spec'd amber-accent `ForgeCard`.** The
KDoc inside `HomeScreen` describes a full card (priority chip + project name, then title, then
due chip, then a "Mark it done" primary button). What's built so far is only the priority-chip
`Row` — no project name, title, due chip, or button yet — inside a default M3 `Card`, so it'll
render as a plain grey surface, not the amber-tinted variant. `ForgeCard` still doesn't exist
as a component; the spec calls for it but this is standing in with `Card` for now. Clearly WIP,
flagging so it's not mistaken for finished.

**`ForgeSectionLabel` isn't baseline-aligned, though its own doc says it should be.** The KDoc:
"aligned by their text BASELINE rather than centred vertically." The `Row` sets no
`verticalAlignment`, so it defaults to `Alignment.Top` — the 11sp mono label and the 13sp
Space Grotesk action won't share a baseline. Wants `Alignment.Bottom` with baseline-aware
sizing, or `Modifier.alignByBaseline()` on each child inside a Row. Cosmetic, but it's the one
spot where the code and its own comment directly disagree.

**`ForgeSectionLabel`'s render gating diverges from the spec.** Spec says
`action: String? = null, onAction: (() -> Unit)? = null` and "for 'The only task': action =
null, so nothing renders on the right." The impl instead makes `action` non-null (`"All
tasks"` default), makes `onAction` default to a non-null `{ }`, and gates the right side on
`onAction != null`. The HomeScreen call site works around this correctly by passing
`onAction = null` to hide the button — so the behaviour is right at the call site, it's just
reached through a different switch than the spec describes. Minor.

**`DueChip`'s default label is `"High"`** — looks copy-pasted from `PriorityChip`; a *due*
chip should default to a time string. Only affects the `@Preview`; `DueChip` isn't used in
`HomeScreen` yet.

**`PriorityChip`'s `when(level)` uses `else ->` for the LOW branch.** Works today, but because
`Priority` is a closed enum the `when` could be exhaustive without `else` — as written, a
future 4th priority would silently fall through to LOW styling instead of triggering a compile
error. Style call, not a bug.

Greeting strings are hardcoded (`"Good morning."`, `"WED 15 JUL · 3 DONE"`) — fine for the
prototype, no data layer behind Home yet.

## Carried over (still open)

- **Back stack still seeds at `Home`** (`rememberNavBackStack(ForgeNavKey.Home)`) — the app
  still boots straight past Splash/Onboarding/Login. Contradicts the NAVIGATION SPEC KDoc.
  Untouched this round.
- **Tabs still push instead of replacing.** `ForgeNavHost`'s `onSelect` is still
  `backStack.add(value)` with no `clear()` first, and the debug `Toast.makeText(...)` on every
  tab tap is still there. The nav bar's own doc says tabs are siblings that replace.
- **Icon outline→filled swap still doesn't happen.** `inactiveIcon` is defined on every tab but
  `ForgeBottomNavigationBarItem` still always renders `item.activeIcon`; only the colour
  animates. `activeColor`/`inactiveColor` interface defaults are still unread too.
- Onboarding finish/skip routes to `Login` while the spec KDoc says `Registration` — unresolved.
- `BackHandler { clear() }` vs `NavDisplay`'s own `onBack` — still unverified on a real device.
- Mechanic rough edges (bar-vs-72dp-box, fill-gradient coords, `.border()` before
  `.clip()`/`.background()`, inline `#7A7060` ticks; ember-dot polish) — untouched.
- `Motion.kt` still an empty TODO; three screens hand-roll easing; ring alpha wants `keyframes`.
- Two dead `gradle.properties` blocks (deprecated `kotlin.incremental.useClasspathSnapshot`;
  dead `kapt.*`) — untouched.
- `SuccessScreen`'s "Reset prototype" button — spec KDoc says delete it, not yet done.
- The long auth-flow tail, unchanged: EmailSent countdown/cooldown/unwired resend · EmailSent
  px ember/width/textAlign · ForgeApplication dead font warm-up · ResetPassword→EmailSent is a
  prototype simplification (real flow is a deep link) · email not threaded through nav ·
  headline letterSpacing typos · `isLoading` restore trap · inline ForgeDivider ·
  **ForgeTextField focus ring still deleted (14th note running — restore block at
  `git show 0c6ffa9:docs/HANDOFF.md`).**

## Toolchain — untouched this round

No `gradle.properties` or `build.gradle.kts` changes. `ksp.incremental=false`,
`hilt { enableAggregatingTask = false }`, and the `:app:compileJava` alias are exactly as
before.

## Screen status — real vs stub

Auth six near-complete. Onboarding feature-complete. **Home**: greeting row real; focus card
partially built (priority chip only, in a plain M3 `Card`); needs `ForgeCard`, the task title,
due chip, "Mark it done" button, and the whole up-next list still. **Stats/Settings**: still
visible-stub bodies. **Bottom nav**: three tabs' active colour works; Add button renders now
but is wired to nothing; icon-swap still pending. Live back stack still seeds at Home, so
that's what you land on when you run it.

Design source is still the **Forge Design System** (external Claude Design project).

## Verified

`./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL**, no Kotlin compiler warnings.
Compile-only as always — the Add-button dead wiring, the tab push-vs-replace, the missing
baseline alignment, and the Home-seeded back stack are all things that compile clean and only
show up on-screen or by reading the code.
