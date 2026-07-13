# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-07-13 · **On top of:** `207d961` (pulled clean; `:app:compileDebugKotlin` + `:lib:compileKotlin` verified on the desktop)

This is the latest cross-machine handoff. Whoever syncs next can overwrite it.

## ⚠️ Read this first

1. **The entry point is STILL OnboardingPromise** (dev-iteration pattern,
   round 4) — `rememberNavBackStack(ForgeNavKey.OnboardingPromise)` at
   `ForgeNavHost.kt:60`. Restore `Splash` before treating any build as "the app."
2. **The ForgeTextField focus ring is still deleted** (6th note in a row):
   `.drawBehind { }` empty lambda. Restore block: `git show 0c6ffa9:docs/HANDOFF.md`.
   Yash was told and hasn't said restore yet — still his call.

## The big change: Yash built the pulsing-rings hero himself

The hands-off item from your note is done — `OnboardingPromiseScreen` now has the
animated HeroEmber, hand-written: `rememberInfiniteTransition` driving six floats
(scale + alpha × 3 rings) through two private `InfiniteTransition` extension
helpers (`ringScale`/`ringAlpha`), staggered via `StartOffset(0/700/1400)`, a
`DrawScope.drawRing` helper for the gradient rings, the two-layer amber glow, and
the 56.dp ember with the off-center highlight. The eyebrow/title/body text block
is in too (JetBrains Mono eyebrow ✓).

Scoreboard vs your drift notes:

1. ~~px-vs-dp / radius-vs-diameter~~ → **fixed**: radii are `90/65/45.dp.toPx()`
   = 180/130/90.dp diameters, density-independent. ✔
2. ~~inverted gradient stops~~ → **fixed**: `0f → Amber @ 0.16×alpha`,
   `0.7f → Transparent`. ✔
3. Ember highlight math is *exactly* right: `center − 0.20r / − 0.30r` ≡ the
   spec's 40% x / 35% y. Glow radii 40/80.dp, delays 0/700/1400 ✓.
4. Top bar: still open — onboarding keys still fall into the nav host's `else`
   → back-arrow ForgeTopBar; no Skip yet.
5. Spec-comment-in-body habit: the bottom-chrome block is pasted inline (same
   as EmailSent's).

## Known rough edges (left as-is, not silently fixed)

1. **Ring alpha timing** — spec: alpha reaches 0 at **80%** of the 2800ms cycle
   and holds; implementation tweens 0.95 → 0 over the full cycle. `keyframes {}`
   (0.95 at 0ms, 0 at 2240ms, 0 at 2800ms) would match.
2. **Easing** — rings use Compose's built-in `EaseOut`, not the design's
   `cubic-bezier(0.22, 1, 0.36, 1)` token. `core/designsystem/Motion.kt` exists
   but is still an empty TODO shell — the token belongs there (ForgeButton and
   ForgeSecondaryButton each still carry a private copy too).
3. **Layout split** — hero Box and text Column are both `weight(1f)` → a 50/50
   vertical split with the text centered in the lower half. Spec flows
   eyebrow/title/body directly under the hero and pins dots + CTA to the bottom.
   Will need restructuring when the bottom chrome lands.
4. **Missing 12.dp spacer** between eyebrow and title (spec step 3); title→body
   12.dp is there.
5. **`onNext`/`onSkip` exist only in the KDoc** — the signature has neither
   (same @param-drift pattern Login had).
6. **Still missing:** pagination dots, primary CTA ("Show me how" + arrow),
   wordmark+Skip top bar. `OnboardingMechanicScreen`/`OnboardingDisciplineScreen`
   remain bare stubs with no specs.

## Carried over (still open, unchanged from your note)

Focus ring (banner above) · EmailSent countdown bugs ("Resend in 0:0:45" doubled
prefix, no cooldown on entry, `onResend`/`onOpenEmail` unwired so "Open mail app"
spins forever, row alignment/gap) · EmailSent px ember/ring, Outlined-vs-FILLED
icon, 200 vs 280.dp width, missing `textAlign` · `ForgeApplication` dead font
warm-up · **ResetPassword → EmailSent backwards wiring** · **Success "Continue" →
Splash loop (deliberate? still unanswered)** · email never threaded through nav ·
ForgotPassword ignores its `modifier` param · "Log in" footer double-Login back
stack · Login 8-char rule vs spec · letterSpacing typos in Login/ForgotPassword/
ResetPassword headlines · `isLoading` restore trap on Login/Registration · unused
`interactionSource` in LoginScreen · inline `ForgeDivider` · token/easing
duplication (`core/designsystem` extraction overdue; onboarding imports `Amber`
from `core.components`) · `ForgeSecondaryButton` Google default icon ·
`ForgeNavKey.Registration: NavKey` · `animation-core`/`foundation` 1.11.4 pins ·
email regex in `RegistrationScreen.kt`.

## Screen status — real vs stub

| Screen | State |
|---|---|
| `LoginScreen`, `RegistrationScreen` | Near-complete. |
| `ForgotPasswordScreen`, `ResetPasswordScreen` | Near-complete (Reset wired backwards to EmailSent). |
| `EmailSentScreen` | Structurally complete; countdown bugs above. |
| `SuccessScreen` | Near-complete. |
| `OnboardingPromiseScreen` | **Hero animated + text block real.** Missing: dots, CTA, Skip top bar, params (edges 3–6). |
| `OnboardingMechanicScreen`, `OnboardingDisciplineScreen` | **Empty stubs, no specs yet.** |
| home/tasks/stats/settings package | Component stubs only (your scaffold, untouched). |

Onboarding's place in the real flow (before Login? after Success?) is still
undecided. Design source is still the **Forge Design System** (external Claude
Design project).

## Verified

`./gradlew :app:compileDebugKotlin :lib:compileKotlin` → BUILD SUCCESSFUL on the
desktop with this exact tree. Full `assembleDebug` was last green at `01ad95e`
(toolchain/JBR setup in `CROSS-MACHINE-SETUP.md`).
