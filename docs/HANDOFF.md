# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-07-09 · **On top of:** `42903a6` (pulled clean, `compileDebugKotlin` verified on the desktop)

This is the latest cross-machine handoff. Whoever syncs next can overwrite it. This push
continues the Login screen: the form components are now real (largely hand-written by Yash
as Compose practice), but the button is only partially up to spec and a couple of known
issues are left in deliberately — read the rough edges before "fixing" anything silently.

## What this push adds

- `core/components/ForgeTextField.kt` — **the real design-system text field**: floating
  label driven by a single 0→1 `animateFloatAsState` progress (position + size + weight
  lerp), amber focus ring drawn with a straddling `drawBehind` stroke, animated
  border/icon/label colors (default → focused amber → danger), leading icon slot,
  trailing slot, helper/error supporting line (error wins). Matches the spec that used
  to sit as a comment in LoginScreen.
- `core/components/ForgePasswordField.kt` — wraps `ForgeTextField` with a lock leading
  icon, `PasswordVisualTransformation`, and a show/hide visibility toggle in the
  trailing slot.
- `core/components/ForgeButton.kt` — the primary button, **WIP against its spec**. The
  full Forge Design System button spec (base + PrimaryButton + loading state + Compose
  gotchas) is in the KDoc on the composable — treat that as the source of truth.
  Implemented so far: 56.dp height, 16.dp radius, Space Grotesk 16.sp W600, animated
  container/content colors, amber `dropShadow`, optional trailing arrow.
- `feature/auth/LoginScreen.kt` — email (`ForgeTextField` + mail icon) and password
  (`ForgePasswordField`) wired with hoisted `rememberSaveable` state; `canSubmit`
  derived from email regex + password length ≥ 8; `ForgeButton` placed at the bottom.

## Known rough edges (left as-is, not silently fixed)

1. **`LoginScreen.canSubmit` uses `rememberSaveable { derivedStateOf { … } }`** — a
   `DerivedState` can't be saved to a `Bundle`, so this compiles but will throw once
   saveable state registration kicks in. Should be `remember { derivedStateOf { … } }`
   (derived values don't need saving — their inputs are already saveable).
2. **`canSubmit` isn't consumed yet** — the button is called as `ForgeButton()` with
   defaults; `enabled = canSubmit` still needs wiring (the spec's hoisting principle).
3. **`ForgeButton` color `when` order** — `isEnabled -> Amber` is checked *before*
   `isPressed`, so the pressed color (`Amber500`) is unreachable while enabled.
   Spec order is disabled → pressed → default.
4. **`ForgeButton` vs its own KDoc spec, still missing:** press scale 0.985 on the
   120ms cubic-bezier ease-out; loading spinner (the `loading` var is unused); shadow
   should be two layers and disappear when pressed/disabled (currently one, always on);
   letterSpacing is `-0.0005.em` (typo — spec says `-0.005em`); icon is 22.dp (spec 20.dp);
   `isIcon: Boolean` should probably become `leadingIcon`/`trailingIcon` usage.
5. **Design tokens live in the wrong file** — `Amber`, `Amber500`, `DarkEspresso`,
   `DeepEarthBrown`, `AgedBrass`, `FgTertiary` etc. are file-level vals in
   `ForgeTextField.kt` (marked TODO). They belong in `core/designsystem` as real tokens.
6. **Carried over from the last note:** `ForgeNavKey.Registration` is still declared
   `data object Registration: NavKey` while every sibling extends `ForgeNavKey`.
   Compiles, but breaks `when` exhaustiveness over the sealed interface.

## Screen status — real vs stub

| Screen | State |
|---|---|
| `LoginScreen` | **WIP.** Heading + subtitle + email + password fields real; submit button present but not wired to `canSubmit`; "forgot password?" / register links not started. |
| `RegistrationScreen`, `ForgotPasswordScreen`, `EmailSentScreen`, `ResetPasswordScreen`, `SuccessScreen` | **Empty stubs** — unchanged from the last push. |

Design source is still the **Forge Design System** (external Claude Design project).

## Verified

`./gradlew :app:compileDebugKotlin` → BUILD SUCCESSFUL on the desktop with this exact
tree. Full `assembleDebug` was last green at `01ad95e` (toolchain/JBR setup in
`CROSS-MACHINE-SETUP.md`).
