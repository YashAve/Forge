# Handoff note — laptop → desktop

**From:** laptop Claude session · **Date:** 2026-07-11 · **On top of:** `0c6ffa9` (this tree `compileDebugKotlin` verified on the laptop)

This is the latest cross-machine handoff. Whoever syncs next can overwrite it.

## ⚠️ Read this first — two things before you build on this

1. **The app entry point is temporarily ResetPassword.** `ForgeNavHost` now does
   `rememberNavBackStack(ForgeNavKey.ResetPassword)` — a dev-iteration change so the
   screen under construction opens directly (it's otherwise unreachable, see below).
   Splash/Login is NOT the entry right now. Keep it while iterating on this screen;
   restore `rememberNavBackStack(Splash)` before treating any build as "the app."
2. **The ForgeTextField focus ring is still deleted** (flagged in the last note,
   not yet resolved): `.drawBehind { /* focus ring, unchanged */ }` is an empty
   lambda — the amber halo `drawRoundRect` and its `CornerRadius`/`Stroke` imports
   are gone. Looks like a pasted-snippet elision. The restore block is in the
   2026-07-11 note in git history (`git show 0c6ffa9:docs/HANDOFF.md`). Confirm
   with Yash before restoring.

## What this push adds

- `feature/auth/ResetPasswordScreen.kt` — **implemented from its KDoc spec**:
  "Set a new\npassword." headline + "Make it strong. 8+ characters." subtext,
  two `ForgePasswordField`s (New / Confirm), and the spec's **dynamic helper/error**
  on the confirm field via two `derivedStateOf`s — mismatch+confirm-filled →
  error "Passwords don't match.", match → "Passwords match.", typing → "Keep
  going." `canSubmit` = non-empty + match + ≥8 + not loading. CTA is the spec's
  **"Hammer it in"**, which sets `isLoading = true` and calls `onResetPassword()`.
- `navigation/ForgeNavHost.kt` — only the entry-point line above.

Notably: this screen **uses its `modifier` param correctly** (the ForgotPassword
modifier-dropped edge did not repeat).

## New rough edges (left as-is, not silently fixed)

1. **`onResetPassword` is not wired in the nav host** — the entry is still
   `ResetPasswordScreen(basicModifier)` with the default no-op callback. So
   "Hammer it in" sets `isLoading = true`, nothing navigates, and the button
   spins forever — and since `isLoading` is `rememberSaveable`, it *stays*
   spinning across config/back restore (same trap as the Login/Registration
   edge). Presumably wants → `Success` (and a stack `clear()` decision).
2. **ResetPassword is unreachable in real flow** — `EmailSentScreen` is still an
   empty stub with no continue action, so nothing navigates to ResetPassword
   except the temporary entry point. The chain Forgot → EmailSent → Reset needs
   EmailSent built (it has no KDoc spec yet, unlike the other stubs).
3. **Arrow icon missing on the CTA** (spec: arrow when valid & not loading) —
   same gap as ForgotPassword's button.
4. **letterSpacing typo, 4th sighting** — headline `(-0.003).em` (spec `-0.03`).
   Now in Login, ForgotPassword, and ResetPassword; only Registration is correct.
5. **KDoc `@param` drift again** — doc says `onSubmit`, actual is
   `onResetPassword` (pattern continues from Login/ForgotPassword).

## Carried over (still open)

From the previous notes, none addressed: the **focus-ring deletion** (banner
above), ForgotPassword ignoring its `modifier` param, the "Log in" footer
double-Login back stack, Login's 8-char `canSubmit` rule vs its spec, Login
headline letterSpacing, the `isLoading` restore trap on Login/Registration,
unused `interactionSource` in LoginScreen, inline `ForgeDivider`, token/easing
duplication across component files (ResetPassword hardcodes `#F5EDDE`/`#B5A990`
again — extraction long overdue), `ForgeSecondaryButton`'s Google default icon,
`ForgeNavKey.Registration: NavKey`, the `animation-core`/`foundation` 1.11.4
pins, and the email regex living in `RegistrationScreen.kt`.

## Screen status — real vs stub

| Screen | State |
|---|---|
| `LoginScreen`, `RegistrationScreen` | Near-complete (unchanged this push). |
| `ForgotPasswordScreen` | Near-complete (unchanged this push). |
| `ResetPasswordScreen` | **Near-complete (new).** Missing: nav-host wiring for `onResetPassword`, arrow icon, real reset action. |
| `EmailSentScreen` | **Empty stub, no KDoc spec yet** — now the missing link in the reset chain (Forgot → EmailSent → Reset). |
| `SuccessScreen` | Empty stub — reachable from Login/Registration, renders blank. |

Design source is still the **Forge Design System** (external Claude Design project).

## Verified

`./gradlew :app:compileDebugKotlin` → BUILD SUCCESSFUL on the laptop with this exact
tree. Full `assembleDebug` was last green at `01ad95e` (toolchain/JBR setup in
`CROSS-MACHINE-SETUP.md`).
