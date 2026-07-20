# Handoff note — laptop → desktop

**From:** laptop Claude session · **Date:** 2026-07-20 · **On top of:** `8afa64c`

Tree is green: full `--rerun-tasks` `./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL**
(17/17 tasks executed). Only the pre-existing KSP "No dependencies reported" note. No new
warnings. **Compile only — the app was not launched.**

**All colours are now centralised into a dual-theme system, and a light theme exists.**
This push replaces the old scattered `val Amber` / `val FgTertiary` / inline `Color(0xFF…)`
literals with a single `ForgeColorScheme` read through `ForgeTheme.colors.*`, adds a
`ForgeLightColors` instance, and wires a persisted `darkTheme` flag through `MainActivity`.
40 files (39 changed + 1 new).

## ⚠️ Read first

**1. DEVIATION — `ForgePreferences` is SharedPreferences-backed, NOT DataStore.** The brief's
STEP 5 asked for a Jetpack `DataStore<Preferences>`. DataStore is not on this module's
classpath (not declared, not transitive) and the constraints forbid touching Gradle, so I
backed it with `SharedPreferences` instead. **The public API is identical to the DataStore
version** — `isDarkTheme(): Flow<Boolean>` (a `callbackFlow` over an
`OnSharedPreferenceChangeListener`) + `suspend setDarkTheme(Boolean)` — so swapping in real
DataStore later is a body-only change with zero call-site impact. Full rationale is in the
class KDoc (`core/datastore/ForgePreferences.kt`). It's a `@Singleton` with an
`@Inject constructor(@ApplicationContext …)`, field-injected into `MainActivity`.

**2. The light theme is switchable but has NO UI to switch it yet.** The plumbing is fully
in place — `MainActivity` reads `forgePreferences.isDarkTheme().collectAsStateWithLifecycle(true)`
and passes it to `ForgeTheme(darkTheme = isDark)` — but **nothing calls `setDarkTheme()`**.
The obvious home for the toggle is the Settings screen, which is still a one-line stub. So
right now the app always renders dark (the stored default). Dropping a switch into Settings
that calls `forgePreferences.setDarkTheme(...)` from a coroutine is the natural next task and
is what makes all of this visible.

**3. Light-theme values are compile-verified, not eye-verified.** `ForgeLightColors` uses the
exact hex set from the design brief (cream backgrounds `#F0E6D2`→`#F5EDDE`→…, dark text
`#1A1207`, same amber ramp, borders = dark @ 6/10/18%). Nobody has seen it rendered. Two
things to look at on device once a toggle exists: (a) the amber accent is identical in both
themes, so amber-on-cream contrast in light mode is worth a glance; (b) the ambient
amber wash in `ForgeNavHost` and the card gradients were tuned for a dark ground.

**4. `danger` token is a slightly different hue than the old `Danger` val.** The old
`ForgeTextField` had `val Danger = Color(0xFFE5484D)`; the scheme token is
`danger = #E8624D` (matching the design's danger colour, which `ForgeChips` already used
inline). Migrating `ForgeTextField`'s error border/label to `ForgeTheme.colors.danger`
therefore shifts that red very slightly. Intentional — it unifies the two reds onto one
token — but flagging it since it's a visible (if tiny) change to the text-field error state.

## How the system works (for when you extend it)

- `core/designsystem/Color.kt` — `AmberA50…A700` ramp vals (theme-invariant), the
  `ForgeColorScheme` data class (18 fields), `ForgeDarkColors` + `ForgeLightColors` instances,
  `val LocalForgeColors = compositionLocalOf { ForgeDarkColors }`, and
  `object ForgeTheme { val colors @Composable get() = LocalForgeColors.current }`.
- `object ForgeTheme` and `fun ForgeTheme(...)` coexist — different namespaces, same name,
  mirroring Material3's `MaterialTheme`. `Theme.kt`'s composable provides `LocalForgeColors`
  then wraps `MaterialTheme(typography = Typography, …)`. I kept `typography = Typography`
  (the short brief omitted it) so bare `Text()` calls don't regress.
- **`ForgeTheme.colors` is `@Composable`-only.** Inside `DrawScope` (`Canvas`, `drawBehind`)
  and inside non-`@Composable` lambdas (`remember { }`), capture the colour into a `val`
  in composable scope *first*, then use the val. You'll see this pattern in `SplashScreen`,
  `ForgeWordmark`, both `Onboarding{Promise,Mechanic,Discipline}` heroes, `StatsScreen`'s
  ring, `EmailSentScreen`/`SuccessScreen` badges, and `ForgeNavHost`'s wash.
- `@Preview`s render **dark** without any wrapper, because the `compositionLocalOf` default
  is `ForgeDarkColors`.
- **Amber-ramp gradient literals are intentionally left as raw `Color(0xFF…)`** inside
  gradient stops (the ember coals in Promise/Mechanic, the heat bar, the progress-ring arc,
  the peak week-bar). The brief explicitly permits this — they're theme-invariant ramp
  shades, not tokens. The single primary `Amber` token inside those gradients *was* migrated.

## Notable per-file decisions

- **`ForgeNavKey.kt`** — deleted the dead `activeColor`/`inactiveColor` getters from
  `BottomNavigationSpecifications` (defined, never consumed) and their now-unused imports.
- **`ForgeWordmark.kt`** — the wordmark text colour is measured inside `remember { }`, which
  can't read the `@Composable` `ForgeTheme.colors` getter. I capture `fgPrimary` above it and
  key the `remember` on `(fgPrimary, fontSize)` so it re-measures when the theme flips. Side
  effect worth knowing: it now also re-measures on `fontSize` change (previously the keyless
  `remember` captured the first value only — a latent bug this incidentally closes).
- **`OnboardingPromiseScreen.kt`** — the top-level `DrawScope.drawRing` helper is not
  `@Composable`, so it can't read the theme; I added a `color: Color` parameter and pass the
  captured `amber` from the call site.
- **`ForgeTextField.kt` / `ForgeSecondaryButton.kt`** — these were the two files that *declared*
  the scattered vals. All their internal uses were migrated, then the declarations deleted
  (see list below).
- **`util/CanvasAndDrawing.kt`** — a `@Preview`-only Canvas practice file. I migrated only the
  one `Amber` token reference (it was going away); the arbitrary demo literals
  (`Color.Red/Blue/White/Black`, `Color(0xFFFF7A18)`, etc.) are left as-is — they're not app
  UI. `0xFFFF7A18` in particular is a deliberate practice "orange" (the codelab note says
  "Compose has no Color.Orange"), not a theme colour.

## Vals deleted this push

From `ForgeTextField.kt` (were `private`, migrated + removed immediately): `BgRaised`,
`FgPrimary`, `FgSecondary`, `BorderStrong`, `Danger`. From `ForgeTextField.kt` (were public,
imported across ~10 screens; removed once every importer was migrated): `Amber`, `Amber500`,
`FgTertiary`, `DeepEarthBrown`, `DarkEspresso`, `AgedBrass`. From `ForgeSecondaryButton.kt`
(`private`): `BgRaised`, `BgPress`, `Cream`. From `ForgeNavKey.kt`: the `activeColor` /
`inactiveColor` getters. The old Material `Purple/Pink` scheme + dynamic-colour logic in
`Theme.kt` is gone.

## Flagged literals (could NOT / should NOT be mapped to a token)

- **`0xFFFF7A18`** (×2, `util/CanvasAndDrawing.kt`) — practice-file demo orange, not in the
  amber ramp, not app UI. Left as a literal on purpose.
- **Amber-ramp stops** `0xFFFFE0A8 / 0xFFFFC766 / 0xFFFFAA2E / 0xFFA85100 / 0xFF6E3500 /
  0xFFF58A00 / 0xFFD46E00` inside gradient `listOf(...)` blocks — kept literal per the brief.
  (Where `0xFFFFAA2E` appears as a *solid* colour — e.g. Stats' "Best day" trophy/label and
  the MED priority chip — it *was* mapped to `amberA300`.)
- **`Color.Transparent` / `Color.Black` / `Color.Unspecified`** — kept everywhere (card &
  secondary-button shadows are genuinely black; the Google icon tint is `Unspecified` to keep
  its brand colours). Not theme colours.

Everything else mapped confidently and 1:1.

## Under-scope caught mid-run (so you don't distrust the sweep)

My first pass keyed off files that *imported* the named vals and missed 13 files that inline
`Color(0xFF…)` directly (the auth screens, Splash, Home, `ForgeNavHost`, `ForgeWordmark`,
`ForgeTaskTopBar`, and the four `OnForge*`/`ForgeOn*` lambda components). A full-repo grep for
theme-variant hexes caught them; they're all migrated now, and that same grep is clean:
`grep -rE "Color\(0x(FFF5EDDE|FFB5A990|FF7A7060|FF14110C|FF1C180F|FF251F14|FF3A3020|FF4A4338|FF1A1207|FF0D0B08|FFE8624D|FFE5484D)\)"`
over `app/src/main/java` returns only `Color.kt` definitions and KDoc comments.

## Carried over (still open — untouched by this push)

- **Task List bucket overlap crash**: `overdue && due == null` lands in both TODAY and
  SOMEDAY → duplicate `LazyColumn` key → crash. One-liner:
  `someday = active.filter { it.due == null && !it.overdue }`.
- **No mutable state anywhere** — `onToggle`/`onComplete`/`onAdd`/etc. are all TODO stubs over
  the immutable top-level `val tasks`. A ViewModel (or hoisted `mutableStateListOf`) is still
  the one change that brings the app to life — and now also the thing that would let Stats
  compute a real week chart instead of the six placeholder ints (see the last push's ⚠️).
- **Stats week chart data is still mostly fake** — six hardcoded ints, "Done this week" hardpins
  `"1"`, "Best day: Thursday" is a hardcoded string. Unchanged.
- **`BackHandler`** swallows hardware back on Home/Stats/Settings/Splash/Onboarding; needs an
  on-device check. Top-bar back works.
- Onboarding finish/skip → `Login` vs spec's `Registration`; `Motion.kt` empty TODO;
  `SuccessScreen` "Reset prototype" button not deleted; `codelabs/original/` drift unreverted
  (`git checkout 55575c3 -- codelabs/original/`).
- **ForgeTextField focus ring still deleted** (running note — restore block at
  `git show 0c6ffa9:docs/HANDOFF.md`). Its `drawBehind { /* focus ring, unchanged */ }` is
  now an empty lambda; untouched by the colour work.
- `gradle.properties`: dead `kapt.*`; deprecated `useClasspathSnapshot` warns.

## Screen status — real vs stub

Auth six near-complete. Onboarding feature-complete. Home / Task List / Task Detail: visually
complete, navigable, inert below the nav layer. Stats: visually complete (numbers mostly
placeholder). **Settings: still a stub — and now the obvious place for the theme toggle.**
Everything renders through `ForgeTheme.colors`; boot seeds at `Splash`; app currently always
dark until a toggle calls `setDarkTheme()`.

Design source is still the **Forge Design System** (external Claude Design project).

## Verified

`./gradlew :app:compileDebugKotlin --rerun-tasks` → **BUILD SUCCESSFUL** (full run, 17/17,
this exact tree). Pre-existing warnings only (KSP "no dependencies reported"). **Compile only
— not launched**, so the light theme, the DataStore-shim persistence round-trip, and every
`@Composable`→`DrawScope` colour capture are verified to build, not verified to render.

## Toolchain

Unchanged. No Gradle/dependency/toolchain edits (the DataStore deviation exists precisely to
avoid one).
