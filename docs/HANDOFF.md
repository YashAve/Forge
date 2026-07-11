# Handoff note — laptop → desktop

**From:** laptop Claude session · **Date:** 2026-07-11 · **On top of:** `f02dbed` (this tree `compileDebugKotlin` verified on the laptop)

This is the latest cross-machine handoff. Whoever syncs next can overwrite it.

## ⚠️ Read this first

1. **The app entry point is temporarily EmailSent** (was ResetPassword last
   commit — the dev-iteration entry just moved to the screen under
   construction). `ForgeNavHost` does `rememberNavBackStack(EmailSent)`.
   Restore `rememberNavBackStack(Splash)` before treating any build as "the app."
2. **The ForgeTextField focus ring is still deleted** (3rd note in a row):
   `.drawBehind { /* focus ring, unchanged */ }` is an empty lambda — the amber
   halo `drawRoundRect` and its `CornerRadius`/`Stroke` imports are gone.
   Restore block is in git history: `git show 0c6ffa9:docs/HANDOFF.md`.
   Confirm with Yash before restoring.

## What this push adds

- `feature/auth/SuccessScreen.kt` — **implemented from its newly added KDoc
  spec**: 80.dp Canvas badge (amber 10% fill, 1.5.dp amber 30% ring — correctly
  `dp.toPx()`), 40.dp Check icon, "You're in." headline, "Signed in as" + the
  email in **JetBrains Mono**, flexible spacer, "Continue" CTA. Two streaks
  break here, pleasantly: the headline letterSpacing is **correct**
  (`(-0.025).em`, no typo this time) and the button **actually passes the arrow
  icon** (`Icons.AutoMirrored.Default.ArrowForward`) — first screen to do
  either. The spec's secondary "Reset prototype" button was skipped (spec
  itself says prototype-only, "your call").
- `feature/auth/EmailSentScreen.kt` — the spec **finally exists** (it was the
  one stub without a KDoc) and the **centered middle block is implemented**:
  96.dp badge with ember dot, mark_email_read icon, "Check your email."
  headline, "We sent a reset link to" + mono email, "expires in 30 minutes."
  The **bottom CTA stack is NOT built yet** — "Open mail app" + the 45s resend
  cooldown (the LaunchedEffect countdown exercise) remain; that spec chunk sits
  as a trailing comment inside the Column as a visible to-do.
- `core/designsystem/ForgeFonts.kt` — adds **JetBrains Mono** (Medium + W700)
  via the Google Fonts provider, for the mono email/token styles.
- `ForgeApplication.kt` — `onCreate` launches a coroutine touching both font
  families as a warm-up. See rough edge #1 — as written it preloads nothing.
- `navigation/ForgeNavHost.kt` — nav keys now imported directly (cosmetic);
  `ResetPasswordScreen.onResetPassword` wired → push EmailSent (see edge #4);
  `SuccessScreen.onContinue` wired → `clear()` + Splash; entry point → EmailSent.
- `core/components/ForgeButton.kt` — `@Preview` added (valid: every param has
  a default; label defaults to "Continue").

## New rough edges (left as-is, not silently fixed)

1. **The font warm-up is a no-op.** `ForgeFonts.spaceGrotesk` /
   `.jetbrainsMono` as bare expressions only class-initialize the `ForgeFonts`
   object — constructing a `FontFamily` doesn't fetch anything for downloadable
   Google Fonts; the download starts when a resolver first resolves the family
   for real text. Actual preload is
   `createFontFamilyResolver(this).preload(ForgeFonts.spaceGrotesk)` (suspend,
   in that same launch). The bare `MainScope()` is also never cancelled —
   harmless in an Application, but pointless while the body does nothing.
2. **EmailSent's badge Canvas mixes raw px with dp.** The ring is
   `Stroke(1.5f)` — raw pixels (a hairline at ~2.75x density; SuccessScreen
   does the same ring correctly with `1.5.dp.toPx()`), and the ember is
   `radius = 12f`, `center = Offset(size.width - 22f, 14f)` — also raw px, so
   it renders as a speck hugging the corner instead of the spec's 8.dp dot +
   12 glow at (−22.dp, 14.dp). Only the glow gradient is drawn, too — no solid
   amber dot at its center (the ForgeWordmark pattern is dot + glow).
3. **EmailSent middle-block spec drift:** icon is `Icons.Outlined.MarkEmailRead`
   (spec: FILLED); subtext `widthIn(max = 200.dp)` (spec: **280.dp**) with an
   added W600 the spec doesn't ask for; no `textAlign = Center`, so if the
   subtext wraps it left-aligns inside its centered box; "expires in 30
   minutes." is set in mono W500 (spec says only 13.sp #7A7060). KDoc says
   `onOpenMail`, actual param is `onOpenEmail` — the @param drift streak
   continues. Also: once the CTA stack lands, the layout needs the spec's
   weight(1f) middle-block structure — the current `Arrangement.Center` on the
   outer Column centers everything as one group, it won't pin CTAs to the
   bottom. (The inner `Column` + `align` is redundant as-is.)
4. **ResetPassword → EmailSent is backwards flow-wise** — the real chain is
   Forgot → EmailSent → Reset → Success; this wiring looks like a dev hop into
   the screen under construction. And it makes the `isLoading` trap bite for
   real now: "Hammer it in" sets `isLoading = true`, pushes EmailSent, and
   navigating BACK shows the button still spinning forever (`rememberSaveable`
   restores it).
5. **Success "Continue" → `clear()` + Splash** loops the whole app back to the
   splash — coherent as a demo/prototype loop (matches the spec's
   "Reset prototype" semantics), but not a real-app destination. Deliberate?
   Ask Yash.
6. **Email is never threaded through nav** — both new screens default to
   `"you@forge.app"`; the address typed on ForgotPassword isn't carried to
   EmailSent (needs an arg on the nav key), same for Success.
7. Minor: Success's "Signed in as" adds W600 + letterSpacing the spec doesn't
   specify; the mono email weight differs between screens (W600 in Success,
   W500 in EmailSent); both new files are missing trailing newlines.

## Carried over (still open)

From the previous notes, none addressed: the **focus-ring deletion** (banner
above), ForgotPassword ignoring its `modifier` param, the "Log in" footer
double-Login back stack, Login's 8-char `canSubmit` rule vs its spec, Login
headline letterSpacing (the typo streak stopped in the new screens but the old
sightings are still in Login/ForgotPassword/ResetPassword), the `isLoading`
restore trap on Login/Registration (now demonstrably reachable on
ResetPassword too, edge #4), unused `interactionSource` in LoginScreen, inline
`ForgeDivider`, token/easing duplication — both new screens hardcode
`#F5EDDE`/`#B5A990`/`#7A7060` again AND import `Amber` from `core.components`;
`core/designsystem` is clearly the right home and `ForgeFonts` already lives
there, extraction long overdue — `ForgeSecondaryButton`'s Google default icon,
`ForgeNavKey.Registration: NavKey`, the `animation-core`/`foundation` 1.11.4
pins, and the email regex living in `RegistrationScreen.kt`.

## Screen status — real vs stub

| Screen | State |
|---|---|
| `LoginScreen`, `RegistrationScreen` | Near-complete (unchanged this push). |
| `ForgotPasswordScreen` | Near-complete (unchanged this push). |
| `ResetPasswordScreen` | Near-complete; `onResetPassword` now wired but to EmailSent (edge #4). |
| `EmailSentScreen` | **Middle block done (new); CTA stack + 45s resend countdown still to build.** Spec now in the KDoc. |
| `SuccessScreen` | **Near-complete (new).** Missing: real email pass-through; "Continue" destination decision (edge #5). |

Design source is still the **Forge Design System** (external Claude Design project).

## Verified

`./gradlew :app:compileDebugKotlin` → BUILD SUCCESSFUL on the laptop with this
exact tree. Full `assembleDebug` was last green at `01ad95e` (toolchain/JBR
setup in `CROSS-MACHINE-SETUP.md`).
