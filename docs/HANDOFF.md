# Handoff note — laptop → desktop

**From:** laptop Claude session · **Date:** 2026-07-13 · **On top of:** `793011e` (this tree `compileDebugKotlin` + `:lib:compileKotlin` verified on the laptop)

This is the latest cross-machine handoff. Whoever syncs next can overwrite it.

## ⚠️ Read this first

1. **The entry point is temporarily OnboardingPromise** (the dev-iteration
   pattern, round 3). Restore `rememberNavBackStack(Splash)` before treating
   any build as "the app."
2. **Hands off the OnboardingPromise hero.** Its KDoc ends with "Ask me before
   building the hero and I'll teach it properly" — Yash is building the
   `rememberInfiniteTransition` pulsing-rings piece HIMSELF as a learning
   exercise, guided by the design project. The current static-rings Canvas is
   deliberately mid-flight. Don't finish it, don't "fix" it — the drift notes
   below are for *when he asks*, not an invitation.
3. **The ForgeTextField focus ring is still deleted** (5th note in a row):
   `.drawBehind { /* focus ring, unchanged */ }` is an empty lambda.
   Restore block: `git show 0c6ffa9:docs/HANDOFF.md`. Confirm with Yash.

## What this push adds

- **`feature/onboarding/` — the onboarding flow scaffold** (design source:
  Onboarding.jsx, 3 steps):
  - `OnboardingPromiseScreen` — full design spec in the KDoc (wordmark + Skip
    top bar, hero ember with three staggered pulsing rings, eyebrow/title/body,
    animated pagination dots, primary CTA) + a first WIP: three static
    radial-gradient circles in a 220.dp Canvas. No params yet (`onNext`/
    `onSkip` exist only in the KDoc), body column empty.
  - `OnboardingMechanicScreen`, `OnboardingDisciplineScreen` — bare stubs,
    **no KDoc specs yet**.
  - Three new `@Serializable` nav keys + bare `entry<>` wirings in the host.
- **`:lib` — a plain-Kotlin coroutines playground, NOT app code.**
  `com.example.lib`, seven small exercise files (~120 lines total: launch/
  async/joinAll, thread-vs-coroutine comparisons) with study comments;
  `kotlinx-coroutines-core:1.10.1`; `/build` gitignored. Wired into
  `settings.gradle.kts` as `include(":lib")`.
- **`.idea` churn is fallout from `:lib`:** the JVM-11 module dropped the
  IDE-level `bytecodeTargetLevel`/`languageLevel` from 17 → 11 in
  `compiler.xml`/`misc.xml` (plus the module registration in `gradle.xml`).
  IDE metadata only — Gradle configs are untouched and both modules build.
  Desktop Studio may flip these back on sync; don't fight over it, it's noise.

## Notes on the Promise WIP (for when Yash asks — see warning #2)

1. **px-vs-dp again, plus radius-vs-diameter:** `180f / 130f / 90f` are raw
   pixel values fed to `drawCircle(radius = ...)`, but the spec gives
   **diameters in dp** (180/130/90.dp). So the rings are both misscaled
   (~130.dp outer diameter on a ~2.75x screen instead of 180.dp) and
   density-dependent. `LocalDensity.current` is captured and unused —
   presumably the conversion is planned. (Same px habit as the EmailSent
   ember, still open there too.)
2. **Gradient stops are inverted vs spec:** code has `0f to Amber` (FULL
   alpha at the center; spec: **16%** there) and `0.70f to Amber.copy(0.16f)`
   with **no transparent end stop**, so the fill holds 16% amber all the way
   to the circle edge instead of fading out at 70%. The spec's ring reads
   "faint in the middle, gone by 70%"; the current one is a bright disc.
3. The top bar is wrong for onboarding: the nav host's `when` sends the three
   onboarding keys into the `else` branch → back-arrow `ForgeTopBar`. Spec
   wants wordmark + **Skip** (steps 1–2 only, no back). The spec's own
   suggestion: an optional `trailing` slot on `ForgeTopBar`.
4. The HeroEmber spec block is pasted into the body as a comment (same
   working style as EmailSent's, which is also still sitting there).

## Carried over (still open)

From the previous notes, none addressed: the **focus-ring deletion** (banner
above), **EmailSent's countdown bugs** — label renders "Resend in 0:0:45"
(doubled "0:" prefix), no cooldown on entry, `onResend`/`onOpenEmail` never
wired so "Open mail app" spins forever, row text top-aligned in its 48.dp
strip, 8.dp vs spec 14.dp gap — EmailSent's px ember/ring + Outlined-vs-FILLED
icon + 200 vs 280.dp width + missing `textAlign`, `ForgeApplication`'s dead
font warm-up (real preload lives in MainActivity), **ResetPassword →
EmailSent backwards wiring**, **Success "Continue" → Splash loop**
(deliberate? ask Yash), email never threaded through nav, ForgotPassword
ignoring its `modifier` param, the "Log in" footer double-Login back stack,
Login's 8-char rule vs spec, the letterSpacing typo in Login/ForgotPassword/
ResetPassword headlines, the `isLoading` restore trap on Login/Registration,
unused `interactionSource` in LoginScreen, inline `ForgeDivider`, token/easing
duplication (`core/designsystem` extraction long overdue — onboarding now
imports `Amber` from `core.components` too), `ForgeSecondaryButton`'s Google
default icon, `ForgeNavKey.Registration: NavKey`, the `animation-core`/
`foundation` 1.11.4 pins, and the email regex in `RegistrationScreen.kt`.

## Screen status — real vs stub

| Screen | State |
|---|---|
| `LoginScreen`, `RegistrationScreen` | Near-complete. |
| `ForgotPasswordScreen`, `ResetPasswordScreen` | Near-complete (Reset wired backwards to EmailSent). |
| `EmailSentScreen` | Structurally complete; countdown bugs above. |
| `SuccessScreen` | Near-complete. |
| `OnboardingPromiseScreen` | **WIP (new)** — spec in KDoc; static hero only; no params. **Yash is building the animated hero himself — hands off.** |
| `OnboardingMechanicScreen`, `OnboardingDisciplineScreen` | **Empty stubs (new), no specs yet.** |

Onboarding isn't reachable from the real flow yet (only via the temporary
entry point) — where it slots in (before Login? after Success?) is undecided.
Design source is still the **Forge Design System** (external Claude Design
project).

## Verified

`./gradlew :app:compileDebugKotlin :lib:compileKotlin` → BUILD SUCCESSFUL on
the laptop with this exact tree. Full `assembleDebug` was last green at
`01ad95e` (toolchain/JBR setup in `CROSS-MACHINE-SETUP.md`).
