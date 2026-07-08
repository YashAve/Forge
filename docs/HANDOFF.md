# Handoff note — laptop → desktop

**From:** laptop Claude session · **Date:** 2026-07-09 · **On top of:** `01ad95e` (pulled clean, `assembleDebug` verified on the laptop)

This is the latest cross-machine handoff. Whoever syncs next can overwrite it. It exists
because this push contains **in-progress** auth-flow code — some screens are real, most are
empty stubs, so don't read a wired-up destination as a finished screen.

## What this push adds

The auth-flow navigation is scaffolded end to end and the first screen is being built:

- `navigation/ForgeNavHost.kt` — wires the full auth graph and a per-destination top bar:
  Splash → (on finish) Login → Registration / ForgotPassword → EmailSent → ResetPassword → Success.
  Splash auto-advances via a new `onFinished` callback; `Success` has no top bar, Login/Registration
  show a bare wordmark, the rest show a back button.
- `navigation/ForgeNavKey.kt` — adds the auth destination keys.
- `feature/auth/` — the six screen composables (see status below).
- `feature/splash/SplashScreen.kt` — `onFinished: () -> Unit` (via `rememberUpdatedState`) to leave the splash.
- `core/components/ForgeTopBar.kt` — real top bar: optional back button + `ForgeWordmark`.
- `core/components/ForgeWordmark.kt` — the wordmark's dot is now a glowing amber radial gradient + solid core.

## Screen status — real vs stub

| Screen | State |
|---|---|
| `LoginScreen` | **WIP.** Heading ("Forge an account.") + subtitle done. Email `TextField` just started — currently a default-styled Material `TextField`; the full field spec (amber focus ring, floating label, mail icon, colors) is pasted as a comment in the file, not yet implemented. Password field / submit / links not started. |
| `RegistrationScreen`, `ForgotPasswordScreen`, `EmailSentScreen`, `ResetPasswordScreen`, `SuccessScreen` | **Empty stubs** — `@Composable` body is blank. Wired into nav so the app compiles/runs, but they render nothing. |

Design source for all of this is the **Forge Design System** (external Claude Design project).

## Known rough edges (left as-is, not silently fixed)

- `ForgeNavKey.Registration` is declared `data object Registration: NavKey` — every sibling extends
  `ForgeNavKey`. It compiles (still a `NavKey`, reachable as `ForgeNavKey.Registration`), but it's an
  inconsistency; likely meant to be `: ForgeNavKey`.
- `LoginScreen`'s `TextField` is a placeholder — it does not match the spec in its own comment yet.

## Verified

`./gradlew :app:compileDebugKotlin` → BUILD SUCCESSFUL on the laptop with this exact tree. Full
`assembleDebug` was green at `01ad95e` (see `CROSS-MACHINE-SETUP.md` for the toolchain/JBR setup).
