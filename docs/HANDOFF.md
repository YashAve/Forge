# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-07-14 · **On top of:** `f350112`

Tree is green: `./gradlew :app:compileDebugKotlin :lib:compileKotlin` → **BUILD SUCCESSFUL**.

## ⚠️ Toolchain: two build fixes landed — read before you touch gradle config

### 1. `ksp.incremental=false` in `gradle.properties` — do NOT set it back to true

The build was dying on every edit-compile cycle with a Hilt error that does not name
its own cause:

```
e: [ksp] [Hilt] Access to invalid ... KotlinAlwaysAccessibleLifetimeToken:
   PSI has changed since creation
> Task :app:kspDebugKotlin FAILED — KSP failed with exit code: PROCESSING_ERROR
```

Root cause is an interlock between two settings, verified by testing both sides:

- `hilt { enableAggregatingTask = false }` (app/build.gradle.kts) puts Hilt's component
  aggregation **inside** KSP. It must stay false — I flipped it to `true` to check, and
  `:app:hiltJavaCompileDebug` fails exactly as its code comment predicts:
  *"Provided Metadata instance has version 2.3.0, while maximum supported version is
  2.2.0"* (Dagger's bundled kotlin-metadata-jvm can't read Kotlin 2.3 metadata).
- With **incremental** KSP, Hilt's `ComponentTreeDepsGenerator` calls KSP's `associate()`
  to record originating files. That reads a file path off the `@HiltAndroidApp` file's
  `KSFile` — but on an incremental round that file isn't reprocessed, so its PSI is a
  leftover from an earlier analysis session and already invalid → crash.
- Non-incremental KSP does no dependency tracking, so `associate()` never touches PSI.

So the two settings are mutually exclusive on this toolchain, and `enableAggregatingTask
= false` + `ksp.incremental=false` is the combination that works. Ruled out stale daemon
(fresh daemon, same error) and stale cache (fails deterministically with no source
change). Cost is ~1–2s; verified across repeated edit-compile cycles. Full rationale is
commented in `gradle.properties`. **You will hit this crash too — that's what the flag
is for.**

### 2. `:app` now has a real `compileJava` task (app/build.gradle.kts)

AGP creates one Java compile task per variant and no plain `compileJava`, so
`./gradlew :app:compileJava` failed — and misleadingly, because Gradle falls back to
camel-case abbreviation, matches all five variant tasks, and says *"task 'compileJava'
is ambiguous"* when the truth is the task doesn't exist at all.

Registered `compileJava` in `:app` as an alias onto `compileDebugJavaWithJavac` (which
pulls in `compileDebugKotlin` + `kspDebugKotlin` transitively). Side benefit worth
knowing: `./gradlew compileJava` from the **root** used to run only in `:lib`/`:server`
— both `NO-SOURCE`, since neither has Java files — so it reported BUILD SUCCESSFUL
having compiled nothing and silently skipping `:app` entirely. It now covers all three
modules (2 actionable tasks → 21).

## Yash's onboarding work this round (his code — documented, not fixed)

**`OnboardingMechanicScreen.kt` — hero mostly built**, top labels + bar + tick row done.
Open vs the spec in its own KDoc:
- **Ember dot not built** (spec part A.4: 28dp coal circle centred on the 83% point,
  amber glow ×2, 2dp bg-base border). The bar's amber glow shadow (~0.55 alpha, blur 18)
  is also not there yet.
- **The bar is a sibling of the 72dp Box, not inside it.** Spec pins the bar to the
  bottom of that box (16dp up from bottom); right now the box holds only the two labels
  and the bar sits below it.
- The fill is done as **one Box with a two-stop-transparent gradient** (`0.83f to
  #FFAA2E, 0.83f to Transparent`) rather than a nested 83%-width fill Box. Neat trick,
  but it puts the colour stops in *track* coordinates — 0.20/0.60 of the whole track —
  whereas the spec's 20%/60% are of the **fill** (≈0.166/0.498 in track coords). Slight
  gradient drift.
- `.border()` runs before `.clip()`/`.background()` in the chain, so the border isn't
  clipped by the pill shape. Renders okay because the border draws its own rounded
  outline, but the ordering is worth a look.
- Tick row uses `Color(0xFF7A7060)` inline while the COOL DOWN label above uses the
  `FgTertiary` token — same colour, two spellings.

**`OnboardingDisciplineScreen.kt`** — text block (eyebrow/title/body) built to spec;
hero is still a `Text("android")` placeholder. HeroTaskStack (3 stacked cards, front one
amber-ringed) not started.

**`OnboardingFlowScreen.kt`** — the CTA now cycles instead of finishing:
`when (currentStep) { 0 -> 1; 1 -> 2; 2 -> 0 }`, so "Forge your first task" loops back
to step 0. Reads like a deliberate dev convenience for flipping through the three
screens. `onFinish` is still a no-op default (`{ }`) and still unwired from
`ForgeNavHost` — nothing happens on Skip either.

Both new screens repeat the Promise screen's **missing 12.dp eyebrow→title spacer**
(there's a 12.dp between title and body, none above), and both end without a trailing
newline.

## Carried over (still open)

- Ring alpha should hit 0 at 80% of the cycle and hold (needs `keyframes`, currently
  plain `tween`); built-in `EaseOut` vs the design's 0.22/1/0.36/1 token — `Motion.kt`
  is still an empty TODO while three screens now hand-roll easing.
- **Two dead config blocks in `gradle.properties`**, both noticed while fixing the KSP
  crash, neither touched (one change at a time): `kotlin.incremental.useClasspathSnapshot`
  is **deprecated** and warns on every build ("It is unsupported, please stop using it"),
  and the whole `kapt.*` section is dead — this project uses KSP, not kapt.
- The long tail, unchanged: EmailSent countdown bugs ("0:0:45", no entry cooldown,
  `onResend`/`onOpenEmail` unwired) · EmailSent px ember/icon/width/textAlign ·
  ForgeApplication dead font warm-up · ResetPassword→EmailSent backwards wiring ·
  Success "Continue"→Splash loop (deliberate?) · email not threaded through nav ·
  ForgotPassword ignores `modifier` · footer double-Login back stack · Login 8-char rule
  vs spec · headline letterSpacing typos · `isLoading` restore trap · unused
  `interactionSource` in LoginScreen · inline ForgeDivider · token/easing duplication ·
  ForgeSecondaryButton Google default · `ForgeNavKey.Registration: NavKey` (still breaks
  sealed exhaustiveness) · `animation-core`/`foundation` 1.11.4 pins · email regex in
  RegistrationScreen · ForgeTextField focus ring still deleted (9th note running —
  `git show 0c6ffa9:docs/HANDOFF.md` has the restore block).

## Screen status — real vs stub

Login/Registration/ForgotPassword/ResetPassword/EmailSent/Success near-complete (known
bugs above). Onboarding is one flow screen: **Promise** done (animated hero + text),
**Mechanic** hero mostly built (ember dot pending), **Discipline** text done / hero
placeholder. Home/tasks/stats/settings scaffold untouched.

Design source is still the **Forge Design System** (external Claude Design project).

## Verified

`./gradlew :app:compileDebugKotlin :lib:compileKotlin` → **BUILD SUCCESSFUL**.
`./gradlew :app:compileJava` → **BUILD SUCCESSFUL** (the new alias). Edit-compile cycles
re-verified green after the KSP fix. Full `assembleDebug` was last green at `01ad95e`
(toolchain/JBR in `CROSS-MACHINE-SETUP.md`).
