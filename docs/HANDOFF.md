# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-07-14 · **On top of:** `c8091ba` + WIP

## ✅ Tree is green again — the red patch is fixed

Yash resolved the `ForgeTopBar` slot question himself at home. He picked
**plain slots with the click baked in at the call site** (the Material
`TopAppBar(navigationIcon = { ... })` pattern), not parameterized slots:

- `ForgeTopBar(onStart, onEnd)` slots are now typed plain
  `@Composable () -> Unit` (no more `(onClick) -> Unit`).
- Callers wrap the click themselves, e.g. in `ForgeNavHost.kt`:
  `onStart = { OnForgeOnBack { backStack.removeLastOrNull() } }` — the back
  action lives in the nav host again, where the back stack actually is.
- `OnForgeOnSkip` (in `ForgeOnSkip.kt`) is no longer a copy of the back
  button — it's now the real spec: a "Skip" text button, 14.sp, W600,
  `#7A7060`, letterSpacing `(-0.005).em`, `clickable(indication = null)` for
  a plain (non-ripple) tap.

`./gradlew :app:compileDebugKotlin :lib:compileKotlin` → **BUILD SUCCESSFUL**.

## New this round: onboarding is wired into one flow screen

- **`ForgeNavKey.kt`**: `OnboardingPromise` / `OnboardingMechanic` /
  `OnboardingDiscipline` nav keys are gone, collapsed into a single
  `OnboardingFlow` key. The nav host no longer has 3 back-stack entries for
  onboarding — it's one screen that manages its own step state internally.
- **`OnboardingFlowScreen.kt`** (new) — owns `currentStep` via
  `rememberSaveable { mutableIntStateOf(0) }`, switches the 3 step
  composables in a `Box(weight(1f))`, and drives the shared bottom chrome:
  `ForgePaginationDots` + `ForgeButton` with a `derivedStateOf` label
  ("Show me how" → "Got it" → "Forge your first task"). Skip only shows on
  steps 0/1 (`ForgeTopBar { OnForgeOnSkip { onFinish() } }` vs
  `ForgeTopBar { }` on step 2) — matches the spec note "Skip hidden on the
  last step."
  - **Heads up:** `onFinish` is a no-op default (`{ }`); nothing in
    `ForgeNavHost` passes a real one yet, so tapping Skip or finishing the
    flow currently does nothing observable. Also the step switch is a plain
    `when` with no transition/animation between steps.
- **`ForgePaginationDots.kt`** (new, `core/components/canvas/`) — built to
  spec: 3 dots, active 24.dp amber / inactive 8.dp cream-18%, animated via
  `animateDpAsState` + `animateColorAsState` (200ms EaseOut). Matches the
  OnboardingPromise KDoc bullet exactly.
- **`OnboardingMechanicScreen.kt`** and **`OnboardingDisciplineScreen.kt`**
  gained full KDoc specs (HeroGauge horizontal timer bar; HeroTaskStack
  3-card stack) — bodies are still empty stubs, specs only, same pattern as
  the Promise screen before its hero was built.
- **`OnboardingPromiseScreen.kt`**: small real fix — dropped redundant
  `.fillMaxSize()` on both inner `Column`s (the outer `Column` already
  fills, and the inner ones were sitting inside a `weight(1f)`-driven Box in
  the new `OnboardingFlowScreen` layout, so `fillMaxSize()` there was
  fighting the weight-based sizing). One of the ring-alpha/keyframes edges
  from prior notes is still open, see below.

## Carried over (still open)

- Ring alpha should hit 0 at 80% of the cycle and hold (needs `keyframes`,
  currently plain `tween`); built-in `EaseOut` vs the design's 0.22/1/0.36/1
  token (`Motion.kt` still an empty TODO); missing 12.dp eyebrow→title
  spacer in Promise.
- `OnboardingFlowScreen.onFinish` unwired from the nav host (see above) —
  needs a real callback once there's a next destination after onboarding.
- The long tail, unchanged: EmailSent countdown bugs ("0:0:45", no entry
  cooldown, `onResend`/`onOpenEmail` unwired) · EmailSent px ember/icon/width
  /textAlign · ForgeApplication dead font warm-up · ResetPassword→EmailSent
  backwards wiring · Success "Continue"→Splash loop (deliberate?) · email
  not threaded through nav · ForgotPassword ignores `modifier` · footer
  double-Login back stack · Login 8-char rule vs spec · headline
  letterSpacing typos · `isLoading` restore trap · unused
  `interactionSource` in LoginScreen · inline ForgeDivider · token/easing
  duplication (designsystem extraction overdue) · ForgeSecondaryButton
  Google default · `ForgeNavKey.Registration: NavKey` (still breaks sealed
  exhaustiveness) · `animation-core`/`foundation` 1.11.4 pins · email regex
  in RegistrationScreen · ForgeTextField focus ring still deleted (8th
  note running — `git show 0c6ffa9:docs/HANDOFF.md` has the restore block).

## Screen status — real vs stub

Login/Registration/ForgotPassword/ResetPassword/EmailSent/Success
near-complete (known bugs above). Onboarding is now ONE flow screen:
Promise has the animated hero + text + working dots/CTA/Skip; Mechanic and
Discipline are spec-only stubs. Home/tasks/stats/settings scaffold
untouched.

Design source is still the **Forge Design System** (external Claude Design
project).

## Verified

`./gradlew :app:compileDebugKotlin :lib:compileKotlin` → **BUILD
SUCCESSFUL** (17s, incremental). Last fully green: this commit. Full
`assembleDebug` was last green at `01ad95e` (toolchain/JBR in
`CROSS-MACHINE-SETUP.md`).
