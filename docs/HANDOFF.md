# Handoff note — laptop → desktop

**From:** laptop Claude session · **Date:** 2026-07-20 · **On top of:** `846ad76`

Tree is green: `./gradlew :app:compileDebugKotlin --rerun-tasks` → **BUILD SUCCESSFUL**
(17/17 tasks) and `./gradlew :app:lintDebug` → **BUILD SUCCESSFUL**, no new warnings.
**Compile + lint only — the app was not launched on device/emulator.**

**The theme switch now has a real UI.** The previous push centralised colours and added
`ForgePreferences` (the persisted dark/light flag), but nothing called `setDarkTheme()` yet.
This push builds the Settings screen for real and wires a working toggle end to end.

## What changed

- **`ForgeToggle.kt`** — was a `Text("ForgeToggle")` stub. Now a real pill switch:
  `checked: Boolean`, `onChange: (Boolean) -> Unit`, animated track colour
  (`amber` ↔ `bgPress`, 200ms) and animated knob position/colour via
  `animateFloatAsState` + `BiasAlignment`. Own `clickable` with `indication = null` so it
  doesn't inherit ripple from a parent row.
- **`SettingRow.kt`** — was a stub. Now the shared row used by the whole screen: icon +
  title (+ optional `value` text) + optional `trailing` slot, `danger` flag (recolours icon
  + title to `colors.danger`), press-state background wash to `colors.bgPress` over 120ms
  (`collectIsPressedAsState` + `animateColorAsState`), trailing chevron when clickable with
  no trailing slot.
- **`SettingsScreen.kt`** — fully built out: profile card (unchanged from before) then
  **Preferences** (Notifications toggle — remembered, default on; Completion sound toggle —
  remembered, default off; **Theme row** — icon/value follow `isDark`, `onClick =
  onToggleTheme`; Focus preferences, static), **About** (About Forge, Privacy), **Sign out**
  (`danger = true`), and a centred `"FORGE · V1.0.0"` version footer. Screen no longer owns
  theme state — it takes `isDark: Boolean` + `onToggleTheme: () -> Unit` and just renders +
  triggers.
- **`ForgeNavHost.kt`** — now takes `isDark: Boolean` and `onToggleTheme: () -> Unit`
  parameters and threads them into the `Settings` entry. Also wired `onSignOut` there
  (clears the back stack, lands on `Login`) since it was a free correctness win sitting right
  next to the theme work.
- **`MainActivity.kt`** — added `rememberCoroutineScope()` in `setContent`; `onToggleTheme`
  now does `coroutineScope.launch { forgePreferences.setDarkTheme(!isDark) }`. That's the
  whole loop: tap the Theme row → flips the stored flag → `isDarkTheme()` Flow emits →
  `collectAsStateWithLifecycle` recomposes → `ForgeTheme(darkTheme = isDark)` re-provides
  `LocalForgeColors` → **everything on screen recolours**, including the Settings screen
  itself (icon swaps light_mode/dark_mode, value text swaps Light/Dark).

## ⚠️ Read first

**1. Not yet verified by eye.** Compile + lint only. Nobody has tapped the toggle on a
device/emulator to confirm the animation, the recomposition, and the light-theme palette
actually look right together. That's the natural next step before trusting this fully.

**2. `ForgePreferences` is still SharedPreferences-backed, not DataStore** (unchanged from
last push — DataStore isn't on the classpath, Gradle is off-limits). No new deviation here,
just restating it since this push is what finally exercises `setDarkTheme()` for the first
time.

**3. Notifications / Completion-sound toggles are cosmetic only.** Their state is a local
`remember { mutableStateOf(...) }` in `SettingsScreen` — nothing persists them and nothing
reads them elsewhere. Only the Theme row is wired to a real system (DataStore-shim +
recomposition). That's by design per the brief (only the theme switch needed to be real) but
worth knowing if asked "does the sound toggle do anything" — no.

**4. "Focus preferences", "About Forge", "Privacy" rows are inert** (`onClick = { }`), same
status as lots of other rows across the app (add-task sheets, edit sheets, etc. — see the
carried-over TODO list below). Not a regression, just not built yet.

## Carried over (still open — untouched by this push)

- **Task List bucket overlap crash**: `overdue && due == null` lands in both TODAY and
  SOMEDAY → duplicate `LazyColumn` key → crash. One-liner:
  `someday = active.filter { it.due == null && !it.overdue }`.
- **No mutable state anywhere for tasks** — `onToggle`/`onComplete`/`onAdd`/etc. are all TODO
  stubs over the immutable top-level `val tasks`. A ViewModel (or hoisted
  `mutableStateListOf`) is still the one change that brings the app to life.
- **Stats week chart data is still mostly fake** — six hardcoded ints, "Done this week"
  hardpins `"1"`, "Best day: Thursday" is a hardcoded string. Unchanged.
- **`BackHandler`** swallows hardware back on Home/Stats/Settings/Splash/Onboarding; needs an
  on-device check. Top-bar back works.
- Onboarding finish/skip → `Login` vs spec's `Registration`; `Motion.kt` empty TODO;
  `SuccessScreen` "Reset prototype" button not deleted; `codelabs/original/` drift unreverted
  (`git checkout 55575c3 -- codelabs/original/`).
- **ForgeTextField focus ring still deleted** (running note — restore block at
  `git show 0c6ffa9:docs/HANDOFF.md`).
- `gradle.properties`: dead `kapt.*`; deprecated `useClasspathSnapshot` warns.

## Screen status — real vs stub

Auth six near-complete. Onboarding feature-complete. Home / Task List / Task Detail: visually
complete, navigable, inert below the nav layer. Stats: visually complete (numbers mostly
placeholder). **Settings: now feature-complete and is the one screen with a real, working
piece of app state (the theme flag) — everything else on it is still cosmetic.**

Design source is still the **Forge Design System** (external Claude Design project).

## Verified

`./gradlew :app:compileDebugKotlin --rerun-tasks` → **BUILD SUCCESSFUL** (17/17).
`./gradlew :app:lintDebug` → **BUILD SUCCESSFUL**, no new lint warnings. **Not launched on
device** — the toggle animation, the theme recomposition end-to-end, and the light palette
are verified to build, not verified to render.

## Toolchain

Unchanged. No Gradle/dependency/toolchain edits.
