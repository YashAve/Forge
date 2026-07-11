# Handoff note — laptop → desktop

**From:** laptop Claude session · **Date:** 2026-07-11 · **On top of:** `beced9b` (pulled clean, `compileDebugKotlin` verified on the laptop)

This is the latest cross-machine handoff. Whoever syncs next can overwrite it.

## ⚠️ Read this first: the focus ring is (probably accidentally) gone

`ForgeTextField`'s amber focus halo was deleted in this push. The `drawBehind` block
is now literally:

```kotlin
.drawBehind { /* focus ring, unchanged */ }
```

— an empty lambda whose comment *claims* nothing changed, and the
`CornerRadius`/`Stroke` imports it needed are gone too. This has the shape of a
pasted snippet that elided the body for brevity, not a design decision. It compiles
fine; focused fields just silently lose their glow. To restore, put back:

```kotlin
if (focused) {
    drawRoundRect(
        color = Amber.copy(alpha = 0.14f),
        cornerRadius = CornerRadius(16.dp.toPx()),
        style = Stroke(width = 4.dp.toPx())
    )
}
```

Confirm with Yash before restoring — but don't build more field styling on top
without resolving it.

## What this push adds

- `feature/auth/ForgotPasswordScreen.kt` — **implemented from its KDoc spec**:
  "Cool down,\nreset it." headline + subtext, email `ForgeTextField` (mail icon),
  flexible spacer, "Send reset link" `ForgeButton` gated on the email regex,
  centered "Remembered it? / Log in" footer (`ForgeTextButton`). New params
  `onSendLink` / `onRemembered`.
- `navigation/ForgeNavHost.kt` — wires them: `onSendLink` → push `EmailSent`
  (**still a blank stub, now reachable**), `onRemembered` → pop + push `Login`
  (see rough edge 3).
- `core/components/ForgeTextField.kt` — floating-label geometry rework: field is a
  fixed `64.dp` (was `heightIn(min)`), the label box is `fillMaxHeight()` so offsets
  anchor to the field, label rides `20.dp → 10.dp` (absolute from field top), input
  pinned with `padding(top = 28.dp)` + `TopStart`. Intentional (comment: "THE FIX").
- `core/components/ForgeTopBar.kt` — `@Preview` added.

## New rough edges (left as-is, not silently fixed)

1. **The focus-ring deletion above** — top priority to resolve.
2. **`ForgotPasswordScreen` ignores its `modifier` param** — the root `Column` uses
   a fresh `Modifier.fillMaxSize()`, so the nav host's `basicModifier` (padding /
   insets) is dropped on this screen only.
3. **Footer "Log in" duplicates Login on the back stack** — spec says "go back
   (same as the back arrow)", but the code does `removeLastOrNull()` **+
   `add(Login)`**: stack goes `[Login, ForgotPassword]` → `[Login, Login]`, so
   back from Login lands on another Login. Plain pop is enough.
4. **letterSpacing typo, third sighting** — headline uses `(-0.003).em`
   (spec `-0.03`), same family as Login's (still-open) edge 2 from the last note.
5. **Button vs spec on this screen:** no arrow icon when valid (spec: arrow when
   valid & not loading); `isEnabled` ignores `isLoading` (spec: regex AND not
   loading); `isLoading` is declared but never set — `onSendLink` navigates to
   EmailSent immediately (fake send, expected WIP).
6. **Empty trailing slot** — the email field is called with a trailing lambda
   `{ }`; `trailing` is nullable and `!= null` gates spacing, so an empty
   composable still occupies the slot. Should be omitted.
7. **KDoc `@param` drift again** — doc says `onLogin`/`onSubmit`, actuals are
   `onRemembered`/`onSendLink` (same pattern as Login's edge 4).

## Carried over from the desktop note (all 9 still open)

None of the previous note's rough edges were addressed this push: Login's 8-char
`canSubmit` rule, Login headline letterSpacing, the `isLoading` restore trap,
Login KDoc param drift, unused `interactionSource` in LoginScreen, inline
`ForgeDivider`, token/easing duplication (ForgotPassword hardcodes `#F5EDDE` /
`#B5A990` again — the `core/designsystem` extraction is *very* overdue),
`ForgeSecondaryButton`'s Google default, `ForgeNavKey.Registration: NavKey`, the
1.11.4 pins, and the email regex living in `RegistrationScreen.kt` — which
`ForgotPasswordScreen` now imports cross-file, so it finally needs a shared home.

## Screen status — real vs stub

| Screen | State |
|---|---|
| `LoginScreen` | Near-complete (unchanged this push). |
| `RegistrationScreen` | Near-complete (unchanged this push). |
| `ForgotPasswordScreen` | **Near-complete (new).** Missing: real send, loading state, arrow icon, error states. |
| `ResetPasswordScreen` | Spec KDoc only — empty stub, ready to implement. |
| `EmailSentScreen`, `SuccessScreen` | **Empty stubs — both now reachable** (EmailSent via Send reset link, Success via Login/Registration), so both show blank screens in flow. |

Design source is still the **Forge Design System** (external Claude Design project).

## Verified

`./gradlew :app:compileDebugKotlin` → BUILD SUCCESSFUL on the laptop with this exact
tree (task up-to-date — the tree was already compiled after the edits). Full
`assembleDebug` was last green at `01ad95e` (toolchain/JBR setup in
`CROSS-MACHINE-SETUP.md`).
