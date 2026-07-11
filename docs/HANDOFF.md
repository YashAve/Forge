# Handoff note — laptop → desktop

**From:** laptop Claude session · **Date:** 2026-07-12 · **On top of:** `4e60f22` (this tree `compileDebugKotlin` verified on the laptop)

This is the latest cross-machine handoff. Whoever syncs next can overwrite it.

## ⚠️ Read this first

**The ForgeTextField focus ring is still deleted** (4th note in a row):
`.drawBehind { /* focus ring, unchanged */ }` is an empty lambda — the amber
halo `drawRoundRect` and its `CornerRadius`/`Stroke` imports are gone.
Restore block is in git history: `git show 0c6ffa9:docs/HANDOFF.md`.
Confirm with Yash before restoring.

(Last note's other warning is cleared: **the entry point is back to
`rememberNavBackStack(Splash)`** — builds are "the real app" again.)

## What this push adds

- `MainActivity.kt` — **the real font preload** (replaces last note's no-op
  warm-up): a `createFontFamilyResolver(applicationContext, backgroundScope
  .coroutineContext)` resolver `resolve()`s both `ForgeFonts` families on
  `Dispatchers.Default` at startup. `resolve()` kicks off the async Google
  Fonts fetch, and downloaded typefaces land in the shared async cache, so
  first text draw hits warm — this one actually works. (Stricter alternative
  if you ever want to await/fail loudly: the suspend `preload()`.)
- `feature/auth/EmailSentScreen.kt` — **the bottom CTA stack is built**:
  "Open mail app" ForgeButton (with arrow icon) + the resend row, driven by a
  `LaunchedEffect(send)` countdown — tap flips `send`, the effect relaunches,
  counts 45→0 at 1s ticks, then re-enables and resets. The middle block also
  got the spec's `weight(1f)` + centered structure (last note's layout caveat —
  addressed). The countdown has real bugs though — see below.
- `navigation/ForgeNavHost.kt` — entry point restored to `Splash`. One line.

## New rough edges (left as-is, not silently fixed)

1. **The resend label doubles the "0:" prefix.** The `derivedStateOf` builds
   `"Resend in 0:${if (timer > 9) "0:$timer" else "0:0$timer"}"` — the outer
   template already has `0:`, and both inner branches add another, so it
   renders **"Resend in 0:0:45"** for the whole countdown. Inner branches
   should be seconds only (`"$timer"` / `"0$timer"`) to get the spec's m:ss.
2. **The cooldown doesn't start on entry.** Spec: start at 45 (row disabled,
   counting, "Resend in 0:45"). Actual: `send` starts `false`, so the row
   shows an enabled "Resend link" immediately on arrival. (Note the spec's
   state model drives the row off `timer > 0` / `timer == 0`; the impl drives
   it off separate `send`/`enabled` flags instead — `enabled` is always
   `!send` modulo one frame, so it could just be derived.)
3. **`onResend` is never invoked** — the tap only sets `send = true`; the
   param stays unused. And the nav host still instantiates
   `EmailSentScreen(basicModifier)` bare: no email, no `onResend`, no
   `onOpenEmail`. Which means:
4. **"Open mail app" spins forever** — it sets `isLoading = true` and calls
   the default no-op, and `rememberSaveable` keeps it spinning across restore.
   Fourth member of the Login/Registration/ResetPassword trap family. (Spec
   also says this button is "always enabled" — a loading latch isn't in it.)
5. **Resend row geometry:** the Row isn't `fillMaxWidth()`, so its
   `Arrangement.Center` is inert (the outer Column centers it anyway), and it
   has no `verticalAlignment` — the text sits at the TOP of the 48.dp strip,
   not centered in it. Gap above the row is 8.dp (spec: **14.dp**). The spec's
   two-part layout ("Resend in" sans W600 + time in mono W500, 8.dp apart)
   was collapsed into one all-mono Text with a weight flip. And the pasted
   spec-block comment for items 10–11 still sits above the now-implemented
   CTA code — stale, delete it.
6. **`ForgeApplication.onCreate`'s warm-up is now dead weight** — the working
   preload lives in MainActivity, so the Application's `MainScope().launch`
   touching the two families (last note's edge #1, still a no-op) is pure
   leftover. Remove one of the two; arguably the working resolver belongs in
   the Application (where the dead code currently is) — MainActivity rebuilds
   the scope + resolver on every recreation and never cancels the scope
   (cheap, but pointless work).

## Carried over (still open)

From the previous notes, none addressed: the **focus-ring deletion** (banner
above), EmailSent's middle-block drift — the px-vs-dp ember/ring
(`Stroke(1.5f)`, ember at `Offset(size.width - 22f, 14f)` with no solid dot),
Outlined vs FILLED mark_email_read, `widthIn(max = 200.dp)` vs spec 280.dp,
missing `textAlign = Center` — the **ResetPassword → EmailSent backwards
wiring** (real chain: Forgot → EmailSent → Reset → Success; and its
`isLoading` bites on back-navigation), **Success "Continue" → Splash loop**
(deliberate? ask Yash), email never threaded through nav (both screens show
`you@forge.app`), ForgotPassword ignoring its `modifier` param, the "Log in"
footer double-Login back stack, Login's 8-char `canSubmit` rule vs its spec,
the letterSpacing typo in Login/ForgotPassword/ResetPassword headlines,
the `isLoading` restore trap on Login/Registration, unused
`interactionSource` in LoginScreen, inline `ForgeDivider`, token/easing
duplication (`#F5EDDE`/`#B5A990`/`#7A7060` hardcoded per screen, `Amber`
imported from `core.components` — `core/designsystem` extraction long
overdue), `ForgeSecondaryButton`'s Google default icon,
`ForgeNavKey.Registration: NavKey`, the `animation-core`/`foundation` 1.11.4
pins, and the email regex living in `RegistrationScreen.kt`.

## Screen status — real vs stub

| Screen | State |
|---|---|
| `LoginScreen`, `RegistrationScreen` | Near-complete (unchanged this push). |
| `ForgotPasswordScreen` | Near-complete (unchanged this push). |
| `ResetPasswordScreen` | Near-complete; `onResetPassword` still wired to EmailSent (backwards). |
| `EmailSentScreen` | **Structurally complete (new)** — countdown + CTAs built, but edges #1–5: label renders 0:0:SS, no cooldown on entry, `onResend`/`onOpenEmail` inert. |
| `SuccessScreen` | Near-complete (unchanged this push). |

The full flow is navigable from Splash again. Design source is still the
**Forge Design System** (external Claude Design project).

## Verified

`./gradlew :app:compileDebugKotlin` → BUILD SUCCESSFUL on the laptop with this
exact tree. Full `assembleDebug` was last green at `01ad95e` (toolchain/JBR
setup in `CROSS-MACHINE-SETUP.md`).
