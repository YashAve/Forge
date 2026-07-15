# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-07-15 · **On top of:** `b76dca5`

Tree is green: `./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL**.

## What this push adds — real navigation + the Discipline hero (Yash's code)

**`ForgeNavHost.kt` got a full navigation spec and real routing**, replacing the
placeholder wiring:
- Back stack now starts at `Splash` again (both dev-view hooks from my last note —
  boot-to-`OnboardingFlow`, `currentStep = 1` — are reverted).
- `Home` is a real nav key now. Splash → Onboarding (always, for now — see below);
  Login submit → clear() + Home (skips Success, by design: Success is registration-only);
  Registration submit → clear() + Success; Success continue → clear() + Home;
  Onboarding finish/skip → clear() + Login.
- Login/Registration are documented as **siblings, not a chain** — switching between
  them replaces the top of the stack rather than pushing, so bouncing between the two
  forms can't grow the stack unbounded. Deliberate rejection of what the design file
  shows.
- A top-level `BackHandler` was added whose `when` only matches `Login, Registration`
  and calls `backStack.clear()` on either.

**`OnboardingDisciplineScreen.kt` — HeroTaskStack fully built.** Three overlapping
cards per spec: cards 1/2 (dim, receding backlog) drawn in one `Canvas` via `scale()`;
card 3 (the active task) is a real composable `Box` with `dropShadow` + a straddling
amber focus-ring `drawBehind` + the "NOW · 25:00 · Draft the proposal" content row.
Also: the missing 12dp eyebrow→title `Spacer` (flagged in the last two handoffs) is
now added on **all three** onboarding screens — Promise, Mechanic, Discipline.

## Two things worth your eye — spec vs. code mismatches

1. **Onboarding finish/skip routes to `Login`, but the KDoc spec three lines above it
   says `REGISTRATION`.** Both `OnForgeOnSkip` and the step-2 CTA call the same
   `onFinish()`, wired in `ForgeNavHost` to `backStack.clear(); backStack.add(Login)`
   (line ~212). Might be intentional (finishing onboarding into Login rather than
   Registration is a defensible product call) but it disagrees with the comment
   directly above it — worth a decision either way, not silently changed.
2. **`BackHandler { ... clear() }` on Login/Registration may not achieve "EXIT APP".**
   The spec says Back on either screen should exit the app; `backStack.clear()` just
   empties the app-owned back stack, it doesn't finish the Activity. Separately,
   `NavDisplay` already gets its own `onBack = { backStack.removeLastOrNull() }` — with
   two back-press consumers now in the tree (the top-level `BackHandler` and
   `NavDisplay`'s internal one), which one actually wins on a real back press depends on
   `OnBackPressedDispatcher` registration order, which I didn't chase down by reading
   alone. Worth an actual device back-button test on Login/Registration/elsewhere
   before trusting this section — I didn't have a device up this round to check it.

Splash's routing is also still hardcoded to always go to Onboarding (`persisted state`
routing described in the KDoc — never-seen → onboarding, seen+logged-out → login,
seen+logged-in → home — isn't implemented yet, no persistence layer exists). That one's
clearly a known stub, not a mismatch.

## Carried over (still open)

- Onboarding "mark complete in persisted state" (Skip/Finish) — no persistence layer,
  noted above.
- Bar not pinned inside the 72dp label box on Mechanic (still a sibling); fill-gradient
  stops still in track-coords not fill-coords; `.border()` still before `.clip()`/
  `.background()`; tick row inline `#7A7060` vs `FgTertiary` token elsewhere. Ember-dot
  polish items from the laptop's note (highlight-offset comment mismatch, 1.5dp vs 2dp
  rim, glow bleeding past the 14dp Canvas) — all untouched.
- `Motion.kt` still an empty TODO, three screens hand-roll easing; ring alpha wants
  `keyframes` (hold at 0 from 80%), currently plain `tween`.
- Two dead `gradle.properties` blocks (deprecated `kotlin.incremental.useClasspathSnapshot`
  warns every build; dead `kapt.*` section) — untouched again this round.
- The long auth-flow tail, unchanged: EmailSent countdown ("0:0:45", no entry cooldown,
  `onResend`/`onOpenEmail` unwired) · EmailSent px ember/width/textAlign ·
  ForgeApplication dead font warm-up · ResetPassword→EmailSent chain is a prototype
  simplification (real flow is a deep link) · email not threaded through nav · headline
  letterSpacing typos · `isLoading` restore trap · inline ForgeDivider ·
  `ForgeNavKey.Registration: NavKey` sealed-exhaustiveness break · **ForgeTextField
  focus ring still deleted (11th note running — restore block at
  `git show 0c6ffa9:docs/HANDOFF.md`).**
- `SuccessScreen` still has the "Reset prototype" secondary button the new nav KDoc
  says to delete (Claude Design demo artifact, no place in the real app) — noted in
  the spec comment itself, not yet acted on.

## Toolchain — untouched this round

`ksp.incremental=false`, `hilt { enableAggregatingTask = false }`, and the
`:app:compileJava` alias are all exactly as before. No gradle config touched.

## Screen status — real vs stub

Auth six near-complete (known bugs above). Onboarding: **Promise** done, **Mechanic**
hero essentially complete (ember built, bar-in-box pinning + gradient-coord polish
open), **Discipline** now feature-complete (text + HeroTaskStack both built). Real
navigation now connects Splash → Onboarding → Login/Registration → Success/Home.
Home/tasks/stats/settings still scaffold stubs (`HomeScreen` + `FocusCard` exist,
untouched this round).

Design source is still the **Forge Design System** (external Claude Design project).

## Verified

`./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL**. Compile-only — the two
back-navigation mismatches above need an actual device/emulator pass, which I didn't
have running this round.
