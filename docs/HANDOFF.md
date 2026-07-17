# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-07-17 · **On top of:** `9d54480`

Tree is green: `./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL**.

## What this push adds (Yash's code)

**The bottom-nav bug from last round is fixed** — not patched, redesigned. `ForgeNavKey.kt`
gains a `BottomNavigationSpecifications` interface (`label`, `activeIcon`, `inactiveIcon`,
`activeColor`/`inactiveColor` defaults) that `Home`, `Statistics`, and `Settings` implement
directly. That replaces the old `List<List<Any>>` side-table entirely — no more unchecked
casts, and the active/inactive comparison is now a real `ForgeNavKey == ForgeNavKey` check
computed per-item inside `ForgeBottomNavigationBarItem`, not hoisted above a loop. Confirmed
by reading it, not just compiling: `current == navigations[selection]` now correctly
distinguishes all three tabs.

Same pass also fixes the `Registration : NavKey` sealed-exhaustiveness gap (`Registration` is
now `: ForgeNavKey`, flagged for a couple rounds) and drops the `Add` nav key entirely —
correct, since Add was never a real destination.

**The bottom nav is wired into `ForgeNavHost`** for the first time: `bottomBar` renders it for
Home/Statistics/Settings, and both screens get real `entry<>` registrations — before this
round, tapping Stats or Settings would have had no destination for `NavDisplay` to land on.
`StatsScreen`/`SettingsScreen` also get minimal visible bodies (`fillMaxSize` + centered amber
label) instead of unstyled top-left text, so they're actually legible when you land on them.

## Not a bug, but loud: Splash/Onboarding/Login are currently unreachable

```kotlin
val backStack = rememberNavBackStack(ForgeNavKey.Home)   // was: rememberNavBackStack(Splash)
```

The app now boots straight to Home. Every screen before it — Splash, Onboarding, Login,
Registration, the whole auth chain — is still there and still wired, just not reachable by
launching the app, since nothing seeds the stack with `Splash` anymore. Almost certainly a
"jump straight to what I'm working on" convenience while building the Home/nav-bar pair.
Flagging it because it directly contradicts the NAVIGATION SPEC KDoc immediately above it in
the same file (still says Splash routes by persisted state), and it's an easy one to forget
about once Home looks done — not touching it, your call on when it goes back.

## Spec vs. code: tabs push instead of replacing

`ForgeBottomNavigationBar.kt`'s own doc comment is explicit about this:

> onSelect(key: ForgeNavKey) — for Focus, Stats and You. The host responds by clearing the
> back stack and adding the new key, since tabs are siblings that replace rather than push.

`ForgeNavHost`'s wiring doesn't do that part yet:

```kotlin
ForgeBottomNavigationBar(current = currentDestination, onSelect = { value ->
    Toast.makeText(context, value.toString(), Toast.LENGTH_LONG).show()
    backStack.add(value)   // spec says: backStack.clear(); backStack.add(value)
}) { }
```

Concrete effect: switching Focus → Stats → You → Focus pushes four entries instead of
replacing one, so Back has to be tapped once per tab-switch before it'll leave the
Home/Stats/Settings cluster at all — tabs don't behave as siblings yet, they behave as a
chain. The rule is written down, just in a different file (the nav bar's own KDoc, not
`ForgeNavHost`'s NAVIGATION SPEC block, which doesn't mention tab-switching at all) — easy to
miss when wiring quickly.

Also in that same callback: the `Toast.makeText(...)` line reads like a manual "did onSelect
actually fire with the right key" check from while this was being wired up (Stats/Settings
only show a centered label right now, so there's no other visible confirmation it worked).
Flagging in case it's not meant to ship.

## Also incomplete (documented, not fixed)

- **Add button doesn't render at all** — `navigations` is now `[Home, Statistics, Settings]`,
  and the bar only ever builds three `ForgeBottomNavigationBarItem`s. The amber pill from the
  spec isn't just unstyled, it's absent — no fourth item in the `Row`. `onAddClick` is still a
  parameter on `ForgeBottomNavigationBar` but nothing calls it and nothing renders in its slot.
- `inactiveIcon` is defined on `BottomNavigationSpecifications` and set correctly for all three
  tabs, but nothing reads it yet — `ForgeBottomNavigationBarItem` always renders
  `item.activeIcon`. Only the color animates; the outline→filled swap from the spec doesn't
  happen yet.
- `activeColor`/`inactiveColor` defaults on the same interface are also unused —
  `ForgeBottomNavigationBarItem` reaches for the `Amber`/`FgTertiary` tokens directly instead.
  Not wrong, just means the interface isn't fully load-bearing yet.
- Top 1dp hairline border is still an empty `drawBehind { }` stub, untouched again this round.
- Two dead locals in `ForgeBottomNavigationBar.kt`: `val context = LocalContext.current` (the
  Toast that would've used it lives in `ForgeNavHost` instead) and `var selected by
  rememberSaveable {...}` (nothing reads or writes it now that active-state comes from the
  `current` parameter). Neither trips a compiler warning — confirmed by forcing a recompile of
  just this file and grepping the output; Kotlin doesn't warn on unused locals from the command
  line, only via IDE inspection — so these are invisible unless you're looking, same shape as
  last round's bug, just harmless this time.
- `StatsScreen`'s placeholder text now reads "StatisticsScreen" while the composable/file are
  still `StatsScreen` — trivial, cosmetic, a stub string not worth chasing.

## Carried over (still open)

- Onboarding finish/skip routes to `Login`; the NAVIGATION SPEC KDoc directly above it still
  says `Registration` — still unresolved, flagged again, untouched since.
- The `BackHandler { clear() }` on Login/Registration for "exit app" — still unverified on a
  real device; a second back-press consumer (`NavDisplay`'s own `onBack`) is also in the tree
  and nobody's chased down which wins.
- Mechanic: bar still a sibling of the 72dp label box, not pinned inside it; fill-gradient
  stops still in track-coords not fill-coords; `.border()` before `.clip()`/`.background()`;
  tick row inline `#7A7060` vs `FgTertiary` token elsewhere. Ember-dot polish (highlight-offset
  comment mismatch, 1.5dp vs 2dp rim, glow bleeding past the 14dp Canvas) — untouched.
- `Motion.kt` still an empty TODO, three screens hand-roll easing; ring alpha wants
  `keyframes` (hold at 0 from 80%), currently plain `tween`.
- Two dead `gradle.properties` blocks (deprecated
  `kotlin.incremental.useClasspathSnapshot`; dead `kapt.*` section) — untouched.
- `SuccessScreen`'s "Reset prototype" button — the nav spec KDoc says delete it, not yet done.
- The long auth-flow tail, unchanged: EmailSent countdown ("0:0:45", no entry cooldown,
  `onResend`/`onOpenEmail` unwired) · EmailSent px ember/width/textAlign · ForgeApplication
  dead font warm-up · ResetPassword→EmailSent chain is a prototype simplification (real flow
  is a deep link) · email not threaded through nav · headline letterSpacing typos · `isLoading`
  restore trap · inline ForgeDivider · **ForgeTextField focus ring still deleted (13th note
  running — restore block at `git show 0c6ffa9:docs/HANDOFF.md`).**

## Toolchain — untouched this round

No `gradle.properties` or `build.gradle.kts` changes. `ksp.incremental=false`,
`hilt { enableAggregatingTask = false }`, and the `:app:compileJava` alias are all exactly as
before.

## Screen status — real vs stub

Auth six near-complete. Onboarding feature-complete. Real navigation connects all of
Splash → Onboarding → Login/Registration → Success/Home, **but the live back stack currently
seeds at Home**, so only Home/Stats/Settings/bottom-nav are reachable by just running the app
(see above). **Home**: spec written, screen body still a one-line stub (now visible: centered
amber "HomeScreen" text). **Stats/Settings**: same shape, stub bodies now visible/legible, no
real content. **Bottom nav bar**: active/inactive color state genuinely works now for its
three tabs; icon-swap, Add button, and top hairline still pending; wired into `ForgeNavHost`
for the first time, tab-switching pushes rather than replaces (see above).

Design source is still the **Forge Design System** (external Claude Design project).

## Verified

`./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL**, twice — once as a normal
incremental build, once after forcing `ForgeBottomNavigationBar.kt` to recompile in isolation
to check for warnings on the two dead locals (none). Compile-only, as always — the
push-vs-replace tab issue and the Home-seeded back stack don't surface as compile errors,
only on-screen or by reading the code.
