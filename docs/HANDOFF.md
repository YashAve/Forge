# Handoff note — laptop → desktop

**From:** laptop Claude session · **Date:** 2026-07-10 · **On top of:** `59d4782` (pulled clean, `compileDebugKotlin` verified on the laptop)

This is the latest cross-machine handoff. Whoever syncs next can overwrite it.

## The big change: Login ↔ Registration content moved

The "Forge an account." screen was built in `LoginScreen.kt`, but per the design that
heading belongs to **sign-up**. This push moves it to where it belongs:

- `RegistrationScreen.kt` now holds the real screen (heading, subtitle, email +
  password fields, Continue button, Terms/Privacy microcopy).
- `LoginScreen.kt` is back to an **empty stub**.
- `ForgeNavHost`: Splash now advances to `ForgeNavKey.Registration` (was Login).

So don't be surprised that Login "lost" its content — it moved, deliberately.

## What this push adds

- `feature/auth/RegistrationScreen.kt` — full sign-up layout: hoisted
  `rememberSaveable` email/password + `isLoading`, `canSubmit` via
  `remember { derivedStateOf }` (email regex + password ≥ 8 + not loading), flexible
  spacer pushing the CTA stack to the bottom, and a `TermsMicrocopy` composable using
  `LinkAnnotation.Clickable` (handlers are TODO).
- `core/components/ForgeButton.kt` — rewritten from `ElevatedButton` to a plain
  `Box` + `clickable` (no ripple; color + 0.985 press scale on a 120ms
  `CubicBezierEasing(0.22, 1, 0.36, 1)` is the feedback, per spec). Loading spinner
  (`CircularProgressIndicator`, 18.dp), `icon: ImageVector?` param, shadow only when
  enabled-and-not-pressed, letterSpacing fixed to `-0.005.em`, icon 20.dp.
- `util/CanvasAndDrawing.kt` — new `GlowingDot` Canvas practice composable.
- `gradle/libs.versions.toml` / `app/build.gradle.kts` — explicit
  `androidx.compose.animation:animation-core` and `androidx.compose.foundation:foundation`
  at 1.11.4 (see rough edge 3 below).

## Rough-edge scoreboard (vs the previous desktop note)

Fixed in this push:

1. ~~`rememberSaveable { derivedStateOf }`~~ → now `remember { derivedStateOf }`. ✔
2. ~~`canSubmit` not consumed~~ → `ForgeButton(isEnabled = canSubmit, …)`, and the
   trailing arrow only shows when valid. ✔
3. ~~color `when` order~~ → disabled → pressed → default; pressed is reachable. ✔
4. ~~button spec gaps~~ → press scale ✔, spinner ✔, conditional shadow ✔ (still a
   single layer, not the spec's two), letterSpacing ✔, icon 20.dp ✔, `isIcon` →
   `icon` param ✔.

Still open / new:

1. **Design tokens still live in `ForgeTextField.kt`** (`Amber`, `DarkEspresso`, …,
   marked TODO) — and usage is spreading: `ForgeButton` and now `CanvasAndDrawing`
   import from there. Belongs in `core/designsystem`.
2. **`ForgeNavKey.Registration: NavKey`** — still doesn't extend `ForgeNavKey` like
   its siblings (breaks `when` exhaustiveness over the sealed interface).
3. **Explicit Compose artifact versions alongside the BOM** — `animation-core` and
   `foundation` are pinned at 1.11.4 while the BOM (2026.06.00) manages the rest.
   Explicit versions win over the BOM, so this can skew those two artifacts against
   BOM-managed siblings. Compiles today; consider dropping the explicit versions and
   letting the BOM resolve them.
4. **`isLoading` is never reset** — `onClick = { isLoading = true }` with no auth
   call yet, so the button spins forever after one tap. Expected WIP.
5. `validEmailRegularExpression` is a file-level val in `RegistrationScreen.kt`;
   will want a shared home once Login needs it too.

## Screen status — real vs stub

| Screen | State |
|---|---|
| `RegistrationScreen` | **Near-complete.** Heading, subtitle, email + password, Continue button (enabled/loading wired), Terms/Privacy microcopy. Missing: link handlers, real submit action, error states. |
| `LoginScreen`, `ForgotPasswordScreen`, `EmailSentScreen`, `ResetPasswordScreen`, `SuccessScreen` | **Empty stubs.** |

Design source is still the **Forge Design System** (external Claude Design project).

## Verified

`./gradlew :app:compileDebugKotlin` → BUILD SUCCESSFUL on the laptop with this exact
tree. Full `assembleDebug` was last green at `01ad95e` (toolchain/JBR setup in
`CROSS-MACHINE-SETUP.md`).
