# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-07-13 · **On top of:** `699f555`

This is the latest cross-machine handoff. Whoever syncs next can overwrite it.

## 🔴 Read this first: THIS TREE DOES NOT COMPILE — deliberately

Yash pushed mid-rework on purpose ("don't worry about compile errors, I will
resolve them at home"). **He is finishing this himself — don't fix it out from
under him.** The four errors, all one root cause:

```
ForgeTopBar.kt:28  No value passed for parameter 'onClick'.   (onStart())
ForgeTopBar.kt:30  No value passed for parameter 'onClick'.   (onEnd())
ForgeNavHost.kt:85 Argument type mismatch: actual type is 'Unit',
                   but 'ComposableFunction1<() -> Unit, Unit>?' was expected.
ForgeNavHost.kt:85 No value passed for parameter 'onClick'.   (OnForgeOnBack())
```

**Last green commit: `699f555`** (`:app:compileDebugKotlin` + `:lib:compileKotlin`).

## What's mid-flight: ForgeTopBar slot rework

The old `onBack: (() -> Unit)?` param is gone. New shape:

- `ForgeTopBar(onStart, onEnd)` — two optional slots typed
  `@Composable (onClick: () -> Unit) -> Unit`, for back-button-left and
  Skip-right (the onboarding top bar from the OnboardingPromise spec).
- `core/components/ForgeOnBack.kt` — `OnForgeOnBack`, a reusable top-level
  *composable-lambda val* rendering the 44.dp back IconButton (the code that
  used to live inline in ForgeTopBar).
- `core/components/ForgeOnSkip.kt` — `OnForgeOnSkip`, currently a **verbatim
  copy of the back button** (arrow icon, "Back" description, even a
  copy-pasted `OnForgeOnBackPreview`). Placeholder — the spec wants a "Skip"
  *text* button: 14.sp, W600, #7A7060, letterSpacing (-0.005).em.

The unresolved design question (Yash's to answer): **who supplies `onClick`?**
The slots declare an `onClick` parameter, but `ForgeTopBar` invokes them bare —
it has no callback to hand over — and the nav host's old
`backStack.removeLastOrNull()` back action was removed with nothing replacing
it. Either the slots become plain `@Composable () -> Unit` with the click baked
in at the call site, or ForgeTopBar grows callback params it threads into the
slots. He'll pick at home.

## Carried over (still open)

- **Entry point is STILL OnboardingPromise** (round 5) —
  `rememberNavBackStack(ForgeNavKey.OnboardingPromise)` at `ForgeNavHost.kt:61`.
- **ForgeTextField focus ring still deleted** (7th note):
  `git show 0c6ffa9:docs/HANDOFF.md` has the restore block. Yash's call.
- OnboardingPromise edges from the last note: ring alpha should hit 0 at 80%
  of the cycle and hold (`keyframes`), built-in `EaseOut` vs the design's
  0.22/1/0.36/1 token (Motion.kt still an empty TODO), hero/text 50/50
  `weight(1f)` split vs bottom-pinned chrome, missing 12.dp eyebrow→title
  spacer, `onNext`/`onSkip` KDoc-only, dots + CTA still unbuilt.
- The long tail, unchanged: EmailSent countdown bugs ("0:0:45", no entry
  cooldown, `onResend`/`onOpenEmail` unwired, row alignment/gap) · EmailSent px
  ember + icon style + width + textAlign · ForgeApplication dead font warm-up ·
  ResetPassword → EmailSent backwards wiring · Success "Continue" → Splash loop
  (deliberate?) · email not threaded through nav · ForgotPassword ignores
  `modifier` · footer double-Login back stack · Login 8-char rule vs spec ·
  headline letterSpacing typos (Login/ForgotPassword/ResetPassword) ·
  `isLoading` restore trap · unused `interactionSource` in LoginScreen · inline
  ForgeDivider · token/easing duplication (designsystem extraction overdue) ·
  ForgeSecondaryButton Google default · `ForgeNavKey.Registration: NavKey` ·
  `animation-core`/`foundation` 1.11.4 pins · email regex in RegistrationScreen.

## Screen status — real vs stub

Unchanged from the last note except the top bar: Login/Registration/
ForgotPassword/ResetPassword/EmailSent/Success near-complete (known bugs above),
OnboardingPromise has the animated hero + text block (dots/CTA/Skip pending —
the Skip work is what this red push starts), Mechanic/Discipline empty stubs
without specs, home/tasks/stats/settings scaffold untouched.

Design source is still the **Forge Design System** (external Claude Design
project).

## Verified

`./gradlew :app:compileDebugKotlin` → **BUILD FAILED** with exactly the four
errors above (expected). Last fully green: `699f555`. Full `assembleDebug` was
last green at `01ad95e` (toolchain/JBR in `CROSS-MACHINE-SETUP.md`).
