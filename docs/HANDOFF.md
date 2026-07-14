# Handoff note — laptop → desktop

**From:** laptop Claude session · **Date:** 2026-07-15 · **On top of:** `d42758c`

Tree is green: `./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL**.

## ⚠️ Read this first — two temporary dev-view hooks are live

1. **`ForgeNavHost` still starts at `OnboardingFlow`, not `Splash`** (line 59,
   `rememberNavBackStack(ForgeNavKey.OnboardingFlow)`). You set this last round;
   carried as-is. Restore `Splash` before a real run-through.
2. **`OnboardingFlowScreen` now starts at `currentStep = 1`** (was `0`) so the
   Mechanic screen shows on launch while Yash builds its ember. Step 0 (Promise) is
   the real first screen — flip it back to `0` when done iterating.

## What this push adds — the Mechanic ember dot (Yash's code)

`OnboardingMechanicScreen.kt`: the 83%-boundary ember is now **fully built** as a
single `Canvas`, four stacked `drawCircle` layers (matches the KDoc plan):
- outer glow — amber @0.4, radialGradient radius 36dp;
- inner glow — amber @0.8, radius 18dp;
- the coal — off-center 4-stop gradient (`#FFE0A8 → #FFAA2E@0.30 → Amber@0.65 →
  #A85100`), highlight shifted up-and-left, gradient radius `radius * 1.3`;
- a `Stroke` rim in bg-base `#14110C`.

The **Canvas is now `matchParentSize()`** (was `fillMaxWidth()` + `align`, which
measured to 0px height and would have pinned `center.y` at the top edge). It now
inherits the 14dp bar height, so the ember centers vertically on the bar.

### On "the ember renders solid black in the @Preview panel"

If you (or Yash) see the ember as a **solid black circle in Android Studio's Preview**:
that's a **Layoutlib shader-rendering fallback, not a code bug**. The tell is that
changing the gradient color does nothing — if Skia were evaluating it, Amber vs Green
would differ. The color/overload/radius are all valid. **Verify on the emulator/device;
the black becomes the amber glow.** (This is what Yash was chasing this round.)

## New rough edges (left as-is, documented not fixed)

- **Coal highlight offset — comment vs code mismatch.** The offset is
  `center.x - radius*0.20f` / `center.y - radius*0.30f`, but the inline comment
  computes it off the **28dp diameter** ("≈ 5.6dp / 8.4dp"). `radius` is 14dp, so the
  real shift is ~2.8dp / ~4.2dp — half what the comment says. Highlight is subtler
  than intended; not wrong, just off-doc.
- **Rim stroke is 1.5dp; spec said 2dp.** Also `Stroke` centers on the path, so it
  straddles the coal edge (half over the glow). Minor.
- **Glow overflows the bar box.** The 72dp outer glow is drawn in a 14dp-tall Canvas;
  Canvas doesn't clip, so it bleeds up toward the top labels and down toward the tick
  row. Faint (0.4α) so probably fine, but worth an eye on device.
- `.idea/misc.xml` lost its `<?xml …?>` declaration — pure IDE churn, folded into this
  commit to keep the tree clean. No app impact.

## Carried over (still open — from your last note + earlier)

- **Bar not pinned inside the 72dp label box** — still a sibling of it, not bottom-
  pinned 16dp up as the spec wants.
- **Fill gradient stops in track-coords, not fill-coords** — the 20%/60% stops are of
  the whole track, not of the 83% fill (slight gradient drift). `.border()` still runs
  before `.clip()`/`.background()`. Tick row uses inline `#7A7060` while COOL DOWN uses
  the `FgTertiary` token — same colour, two spellings.
- `OnboardingDisciplineScreen` hero still `Text("android")` placeholder; HeroTaskStack
  not started.
- `OnboardingFlowScreen` CTA still cycles `0→1→2→0` (dev convenience); `onFinish` is a
  no-op default and unwired from `ForgeNavHost` — Skip does nothing.
- `Motion.kt` still an empty TODO while three screens hand-roll easing; ring alpha wants
  `keyframes` (hold at 0 from 80%), currently plain `tween`.
- Two dead `gradle.properties` blocks (deprecated
  `kotlin.incremental.useClasspathSnapshot` warns every build; dead `kapt.*` section).
- The long auth-flow tail, unchanged: EmailSent countdown ("0:0:45", no entry cooldown,
  `onResend`/`onOpenEmail` unwired) · EmailSent px ember/width/textAlign · ForgeApplication
  dead font warm-up · ResetPassword→EmailSent backwards wiring · Success "Continue"→Splash
  loop · email not threaded through nav · headline letterSpacing typos · `isLoading`
  restore trap · inline ForgeDivider · `ForgeNavKey.Registration: NavKey` sealed-
  exhaustiveness break · **ForgeTextField focus ring still deleted (10th note running —
  restore block at `git show 0c6ffa9:docs/HANDOFF.md`).**

## Toolchain — untouched this round

I did not touch gradle config. `ksp.incremental=false` + `hilt { enableAggregatingTask
= false }` and the `:app:compileJava` alias are all as you left them. Your KSP/PSI-crash
rationale in `gradle.properties` stands.

## Screen status — real vs stub

Auth six near-complete (known bugs above). Onboarding is one flow screen: **Promise**
done (animated hero), **Mechanic** hero now essentially complete (ember built; bar-in-box
pinning + gradient-coord polish open), **Discipline** text done / hero placeholder.
Home/tasks/stats/settings still scaffold stubs.

Design source is still the **Forge Design System** (external Claude Design project).

## Verified

`./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL**. Ember not yet eyeballed on a
running emulator from this machine — that's the one confirmation still worth doing
(see the black-in-preview note above).
