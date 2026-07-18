# Handoff note — laptop → desktop

**From:** laptop Claude session · **Date:** 2026-07-18 · **On top of:** `55575c3`

Tree is green: `./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL**.

## ⚠️ Laptop toolchain incident — corrupted JBR (read this, it can happen to you)

Adding `uiUnit` to `libs.versions.toml` on the laptop produced `Unresolved reference:
libs` on every line of the root `build.gradle.kts`. Root cause was **not** the catalog:
the laptop's `android-studio-otter/jbr` had a corrupted `lib/modules` jimage (one class
inside `jdk.compiler` failed CRC — `javac -version` worked, real compiles died with
`DataFormatException: incorrect data check`; `jimage verify <jbr>/lib/modules` exposes it).

Mechanism worth knowing on any machine:
- Catalog accessors (`LibrariesForLibs*`) are compiled by **the daemon JVM's javac** and
  cached in `~/.gradle/caches/<ver>/dependencies-accessors/<hash>/`, keyed by catalog
  *shape* — so editing a version value reuses cached classes (works), while **adding any
  alias** forces a fresh javac run (failed).
- A failed run leaves a **poisoned entry** (`sources/` + empty `classes/`) that Gradle
  treats as cached forever — even a healthy JDK won't fix it until the entry is deleted.
- Diagnosis: `find ~/.gradle/caches/*/dependencies-accessors/*/classes -name '*.class' |
  wc -l` per entry; delete the empty ones, restart the daemon.

Laptop fix (no repo files changed for this): downloaded Android Studio **Quail 2 stable
(2026.1.2.10)**, sha256-verified, extracted to `~/software/android-studio-quail2`; its
healthy JBR **21.0.10** now sits at the old otter path, so the laptop's machine-local
`org.gradle.java.home` is unchanged. Corrupted JBR deleted after verification.
`gradle.properties` / `build.gradle.kts` untouched — your KSP/Hilt settings stand.

## What this push adds (Yash's code)

- **`ForgeCard` is real** — the full spec'd card: plain/accent variants (base `#1C180F`,
  accent adds the diagonal amber wash + amber-28% border + the big 12dp/32dp shadow),
  press feedback via `collectIsPressedAsState` → 0.99 scale in `graphicsLayer`, no
  ripple, and the KDoc's exact modifier order (shadow → clip → backgrounds → border →
  clickable). This resolves your "focus card is a raw M3 Card" flag — `HomeScreen` now
  wraps the focus card in `ForgeCard { }`.
- **`ForgeCheckbox` is mostly real** — 24dp rounded square, animated border/background
  colors (160ms ease-out), amber glow `dropShadow` when checked, ripple-less clickable.
  But see rough edges: the check-mark's own animations aren't in yet.
- **Back stack restored to `Splash`** (`rememberNavBackStack(Splash)`) — your #1 carried
  item is done; the app boots through Splash again instead of seeding at Home.
- **New dependency**: `androidx.compose.ui:ui-unit` via new catalog alias
  `androidx-compose-ui-unit` (1.11.4, matching the other pins) + `implementation` in
  `:app`. This is the change that tripped the JBR mine above.

## Rough edges (documented, not fixed)

**`ForgeCheckbox`: the spec's headline feature is missing — the check mark neither
animates nor hides.** There's an empty `graphicsLayer { }` on the *box* (leftover), and
the Icon has no scale/alpha state at all: no `animateFloatAsState`, no overshoot bezier
`CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)`, no 120ms alpha fade. Consequence today:
**when unchecked, the dark check mark still renders** — a `#1A1207` check floating in a
transparent box. The box colors animate; the mark itself is static and always visible.

**`ForgeCheckbox` API drift**: `onChange: () -> Unit` instead of the spec's
`onChange(!checked)` (caller must own the toggle); `size` default 24dp vs spec 26 (24 is
the compact-row value); `checked` defaults `true`; and the "THE CHECK MARK" KDoc block is
pasted **twice** (full copy right above `@Preview`).

**`ForgeCard` null-gating drift** (same shape as your `ForgeSectionLabel` note):
`accent = true` by default (spec: false) and `onClick: (() -> Unit)? = { }` — a non-null
empty lambda — so **every default ForgeCard is a tappable no-op with press animation**;
spec wants `null` = inert. `HomeScreen`'s focus card currently *relies* on both flipped
defaults (it passes neither `accent` nor `onClick` and gets the accent look + dead tap).

**`HomeScreen`**: focus-card content is still just the priority-chip row (no title/due
chip/"Mark it done" yet), and the ForgeCard color spec is pasted as a long inline comment
above the call site.

## Carried over (still open)

- ~~Back stack seeds at Home~~ → **fixed this push.**
- Tabs still push instead of replacing (`backStack.add`, no clear) + debug `Toast` on
  every tab tap. Icon outline→filled swap still doesn't happen (`activeIcon` always).
- Add button still wired to nothing (two dead `{ }` links: bar → nav host).
- `ForgeSectionLabel` baseline alignment + null-gating divergence; `DueChip` "High"
  default label; `PriorityChip` non-exhaustive `when`.
- Onboarding finish/skip → `Login` vs spec's `Registration`; `BackHandler { clear() }`
  vs NavDisplay `onBack` unverified on device.
- Mechanic polish (bar-vs-72dp-box, fill-gradient coords, `.border()` before `.clip()`,
  inline `#7A7060`); `Motion.kt` still empty while screens hand-roll easing.
- Two dead `gradle.properties` blocks (deprecated `kotlin.incremental.useClasspathSnapshot`
  warns every build; dead `kapt.*`). `SuccessScreen` "Reset prototype" button not deleted.
- Auth tail unchanged: EmailSent countdown/cooldown/unwired resend · px ember/width/
  textAlign · ForgeApplication dead font warm-up · email not threaded through nav ·
  letterSpacing typos · `isLoading` restore trap · inline ForgeDivider ·
  **ForgeTextField focus ring still deleted (15th note running — restore block at
  `git show 0c6ffa9:docs/HANDOFF.md`).**

## Screen status — real vs stub

Auth six near-complete. Onboarding feature-complete. **Home**: greeting row + focus card
now in a real accent `ForgeCard` (content still chip-only). **Stats/Settings**: stubs.
**Bottom nav**: active color works; Add button renders, wired to nothing. Boot flow is
Splash-first again.

Design source is still the **Forge Design System** (external Claude Design project).

## Verified

`./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL** (full 17-task run on this
exact tree). JBR regression-tested: a scratch catalog alias compiles accessors cleanly
through the daemon again (added, built green, removed).
