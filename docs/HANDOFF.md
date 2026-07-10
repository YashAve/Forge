# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-07-10 · **On top of:** `befcd7b` (pulled clean, `compileDebugKotlin` verified on the desktop)

This is the latest cross-machine handoff. Whoever syncs next can overwrite it.

## The big change: Login is real again, and it's the entry screen

Yash rebuilt `LoginScreen.kt` from the stub your push left behind — this time as the
actual **Login** design ("Welcome back."), not the sign-up content that moved out.
The full screen spec lives as KDoc at the top of the file, including sub-specs for
the two new components it needed. **Splash now advances to `Login`** (you had pointed
it at Registration); Registration is reached via the "Forge an account" footer link.
Deliberate — don't flip it back.

## What this push adds

- `feature/auth/LoginScreen.kt` — full login layout: headline + subtext, email
  (`ForgeTextField`) + password (`ForgePasswordField`), right-aligned
  "Forgot password?" (`ForgeTextButton`), flexible spacer, Continue
  (`ForgeButton` wired to `canSubmit`/`isLoading`/conditional arrow), inline
  "or" divider, "Continue with Google" (`ForgeSecondaryButton`), centered
  "New here? Forge an account" footer.
- `core/components/ForgeSecondaryButton.kt` — dark surface button: `Box` +
  `clickable(indication = null)`, bg `#1C180F` → `#3A3020` pressed, 1.5.dp cream
  18%-alpha border, small black shadow at rest (gone pressed), press scale 0.985
  on the 120ms ease-out, disabled = 0.4 alpha via `graphicsLayer`, slot-based
  `leadingIcon`/`trailingIcon` composables.
- `core/components/ForgeTextButton.kt` — amber text-only button, pressed = 0.7
  alpha fade, no ripple.
- `res/drawable/google_icon.xml` — the 4-color G as a vector (tinted
  `Color.Unspecified` at call sites to keep brand colors).
- `feature/auth/ForgotPasswordScreen.kt` / `ResetPasswordScreen.kt` — **KDoc
  design specs added, bodies still empty stubs.** The specs are complete (layout,
  validation, the ResetPassword dynamic helper/error rules, the "Hammer it in"
  CTA) — either machine can implement them next.
- `navigation/ForgeNavHost.kt` — Login wired: forgot-password → `ForgotPassword`,
  forge-account → `Registration`, login → `Success`. Registration got an
  `onRegister` callback → `Success`.
- `feature/auth/RegistrationScreen.kt` — `@Preview` added, `onRegister` hooked
  into the Continue click.

## Known rough edges (left as-is, not silently fixed)

1. **Login's `canSubmit` contradicts its own spec** — the KDoc says Login does
   NOT apply the 8-character rule ("any non-empty password is fine"), but the
   implementation copied Registration's `passwordInput.length >= 8`. Spec wants
   `passwordInput.isNotEmpty()`.
2. **Headline letterSpacing typo** — spec `(-0.03).em`, code `(-0.003).em`
   (same typo family as the button one you fixed). Registration has the correct
   value, so the two headlines currently track differently.
3. **`isLoading` restore trap (sneaky)** — Login/Registration set
   `isLoading = true` then push `Success` *on top* (no `clear()`). `isLoading`
   is `rememberSaveable` and the NavDisplay uses the saveable-state-holder
   decorator, so navigating back restores `isLoading = true`: button stuck on
   the spinner forever. Will resolve properly once real auth resets the flag,
   but the back-nav path needs thought.
4. **Login KDoc `@param` names drifted** — doc lists `onRegister`, `onGoogle`,
   `onSubmit`; actual params are `onForgeAccount`, `onLogin`, and there is no
   Google callback: the Google button currently calls `onLogin()` → straight to
   `Success` (fake login). Needs its own callback eventually.
5. **Unused `interactionSource`/`isPressed`** at the top of `LoginScreen` —
   dead leftovers from a component copy.
6. **`ForgeDivider` spec'd but not extracted** — the KDoc defines it as a
   component; the implementation is inline `Spacer`+`background` rows in
   LoginScreen. Extract when a second screen needs it.
7. **Token/easing duplication is spreading** — `EaseOutForge` is now a private
   val in BOTH `ForgeButton.kt` and `ForgeSecondaryButton.kt`; `ForgeTextButton`
   uses Compose's built-in `EaseOut` instead of the 0.22/1/0.36/1 token;
   `ForgeSecondaryButton` privately redefines `BgRaised`/`BgPress`/`Cream`
   (same values as ForgeTextField's `DeepEarthBrown`/`FgPrimary`). The
   `core/designsystem` extraction is overdue.
8. **`ForgeSecondaryButton` defaults `leadingIcon` to the Google image** — a
   core component shipping a brand icon as its default; probably should default
   to `null`.
9. **Carried over (3rd handoff now):** `ForgeNavKey.Registration` is still
   `data object Registration: NavKey` — doesn't extend `ForgeNavKey`, breaks
   `when` exhaustiveness. Also still open from your note: explicit
   `animation-core`/`foundation` 1.11.4 pins alongside the BOM, email regex
   living in `RegistrationScreen.kt`.

## Screen status — real vs stub

| Screen | State |
|---|---|
| `LoginScreen` | **Near-complete.** All layout + wiring real. Missing: real auth, Google flow, spec'd password rule (edge 1), error states. |
| `RegistrationScreen` | Near-complete (unchanged this push, + `onRegister`). Missing: link handlers, real submit, error states. |
| `ForgotPasswordScreen`, `ResetPasswordScreen` | **Spec KDoc only — empty stubs.** Ready to implement from the docs. |
| `EmailSentScreen`, `SuccessScreen` | **Empty stubs.** Note: `Success` is now *reachable* from both Login and Registration — landing there shows a blank screen. |

Design source is still the **Forge Design System** (external Claude Design project).

## Verified

`./gradlew :app:compileDebugKotlin` → BUILD SUCCESSFUL on the desktop with this
exact tree. Full `assembleDebug` was last green at `01ad95e` (toolchain/JBR setup
in `CROSS-MACHINE-SETUP.md`).
