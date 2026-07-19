# Handoff note — laptop → desktop

**From:** laptop Claude session · **Date:** 2026-07-20 · **On top of:** `041ae76`

Tree is green: full `--rerun-tasks` `./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL**,
no new warnings.

**Stats screen is now feature-complete.** The last push stopped inside an empty streak
card; this one fills in the streak content, both supporting stat cards, and the whole
animated week chart. See ⚠️ 1 for the one thing still worth knowing about it.

## ⚠️ Read first

**1. The week chart's data is mostly fake, and three "weekly completions" numbers
disagree.** `StatsScreen` builds `data = listOf("M" to 3, "T" to 5, "W" to 2, "T" to 6,
"F" to 4, "S" to 1, "S" to done)` — six hardcoded placeholder ints, and only the last
entry (Sunday) uses the real `done` count. Separately, the "Done this week" stat card
hardcodes `"1"`. So right now: the Sunday bar shows `done` (2, with the current 20-task
fixture), the stat card says `1`, and neither is wired to the other or to anything real.
The "Best day: Thursday" footer text is also a hardcoded string, not
`data.maxBy { it.second }` — it happens to match the synthetic peak (Thursday's fake `6`
is the highest value) purely by construction; change the placeholder numbers and it'll
silently go stale. None of this is a bug to *fix* so much as a marker for where real
weekly-completion tracking needs to plug in — today's percentage (from `total`/`done`)
is the only genuinely live number on the whole screen.

**2. `ForgeProgressRing` is still the best-built piece here** — the app's first arc-based
Canvas, unchanged since last push: two `drawArc`s inside `inset(strokePx/2)`,
`startAngle = -90f`, correct sweep-from-0 entry animation, breathing ambient glow. The
week-chart bars reuse the same animation pattern (remembered `target` + `LaunchedEffect`
+ `animateDpAsState` instead of `animateFloatAsState`) for their per-bar entry animation,
so it's now used twice in one file — a candidate for extracting into a shared helper.

**3. The ring value still round-trips through an integer percent** (`percentage = done *
100 / total`, Int division) — arc and label always agree, both quantised to whole
percents. Unchanged from last push, still fine for a prototype.

## What this push adds (Yash's code)

**Streak card content** — "9" in mono 44sp baseline-aligned with "days" at 15sp, then a
two-tone "Current streak · personal best" caption via `buildAnnotatedString` +
`SpanStyle` ("Current streak" in `0xFFB5A990`, " · personal best" in `FgTertiary`).

**Two supporting stat cards** (`Row` of two `ForgeCard(accent = false)`) — outlined
`CheckCircle` icon over `"$percentage%"` / "Completion rate", and outlined `TaskAlt` icon
over the hardcoded `"1"` / "Done this week" (see ⚠️ 1).

**Week chart** — `ForgeSectionLabel("This week")` + a `ForgeCard` containing seven
animated bars (`Modifier.dropShadow` + `Shadow` for the peak bar's glow, gradient fill on
peak vs. flat translucent amber on the rest), a hairline divider, and a "Best day" footer
row with an `EmojiEvents` trophy icon.

## Rough edges (documented, not fixed)

**`StatsScreen.kt` still has no trailing newline.** Same shape as `ForgeNavKey.kt` and the
prior Stats push.

**Two `@Preview`s in one file** (`ForgeProgressRing` + `StatsScreen`) — unchanged, still
looks intentional.

**`Amber` / `Amber500` / `FgTertiary` are declared in `ForgeTextField.kt`**, not in
`designsystem/Color.kt` (which is still empty of palette) — Stats imports them from
there. Worth relocating whenever the palette file finally gets populated.

**`Modifier.dropShadow` / `Shadow`** are newer Compose graphics APIs used for the peak-bar
glow — compiled clean with no opt-in warning on this toolchain, but worth a specific
on-device look since shadow/elevation APIs are exactly the kind of thing that can render
differently than expected.

## Carried over (still open)

- **Task List bucket overlap**: `overdue && due == null` lands in both TODAY and SOMEDAY →
  duplicate `LazyColumn` key → crash. Still latent. One-line fix:
  `someday = active.filter { it.due == null && !it.overdue }`.
- **`TaskListScreen`'s `onBack` / `onAdd` are dead params** since the top bar moved to the
  nav host, but `ForgeNavHost` still passes them. Plus the orphaned `// ── Top bar ──`
  comment + `Spacer(18.dp)` where the inline bar used to be.
- **`ForgeNavKey.kt`** still missing its trailing newline and the class-level KDoc
  explaining why every key must be `@Serializable`.
- **No mutable state anywhere.** `onToggle` / `onComplete` / `onToggleSub` / `onAdd` /
  `onEdit` are all TODO stubs against the immutable top-level `val tasks`. Five screens now
  navigate and render perfectly and mutate nothing. A ViewModel (or hoisted
  `mutableStateListOf`) is still the one change that brings the whole app to life — and
  now it's also the one change that would let Stats compute a *real* week chart instead of
  six placeholder numbers.
- **`BackHandler`** swallows hardware back on Home / Stats / Settings / Splash / Onboarding
  (explicit `else -> {}`); `TaskDetail` isn't in the `when`, so its hardware back rides on
  `NavDisplay.onBack`. Top-bar back works everywhere. Needs one on-device check.
- `ForgeTaskTopBar`'s `onStart` / `onEnd` are `@Composable` slots named like callbacks;
  default `label = "All Tasks"` vs call-site `"All tasks"`.
- `OnForgeOnAdd` / `OnForgeOnEdit` / `OnForgeOnBack` are top-level `val` composable lambdas,
  not `@Composable fun`s (house pattern, but un-previewable without wrappers).
- `TaskDetail` if-guard fails silently; its top bar (outside the guard) shows over blank.
- `SOMEDAY` section still never renders; no `ForgeEmptyState` on Task List.
- Bottom-nav icon outline→filled swap still doesn't happen (`activeIcon` always); Add button
  named `onAddClick` but empty body.
- `Home` / `TaskList` / `Stats` all default `tasks` to the fully-qualified path inline.
- `ForgeTaskRow` KDoc still says `onOpen` "called with the task id" — it's `() -> Unit` now.
- `ForgeEmptyState` default body hardcodes `\n`; `ForgeSectionLabel` centres vs KDoc's
  baseline; `DueChip` `"High"` default; `PriorityChip` non-exhaustive `when`.
- Onboarding finish/skip → `Login` vs spec's `Registration`; `Motion.kt` empty TODO.
- `designsystem/Color.kt` still empty of palette — see rough edge above re: `Amber` etc.
- `gradle.properties`: dead `kapt.*`; inert `org.gradle.tooling.parallel=true`; deprecated
  `useClasspathSnapshot` warns.
- `SuccessScreen` "Reset prototype" button still not deleted.
- **`codelabs/original/` drift still unreverted** — two files with `fun HomeScreen` →
  `private fun HomeScreen`. `git checkout 55575c3 -- codelabs/original/`.
- Auth tail unchanged: EmailSent countdown/cooldown/unwired resend · px ember/width/
  textAlign · ForgeApplication dead font warm-up · email not threaded through nav ·
  letterSpacing typos · `isLoading` restore trap · inline ForgeDivider ·
  **ForgeTextField focus ring still deleted (20th note running — restore block at
  `git show 0c6ffa9:docs/HANDOFF.md`).**

## Toolchain

Unchanged. Laptop runs Android Studio Quail 2's healthy JBR 21.0.10. No repo toolchain
config touched.

## Screen status — real vs stub

Auth six near-complete. Onboarding feature-complete. **Home / Task List / Task Detail:
visually complete, fully navigable, inert below the nav layer. Stats: visually complete**
too now — ring, streak card, stat cards, and week chart all render real layout, though the
week chart's underlying numbers are mostly placeholder (see ⚠️ 1). **Settings**: stub.
Bottom nav: tabs replace correctly, active colour works, icon swap doesn't. Boot seeds at
`Splash`.

Design source is still the **Forge Design System** (external Claude Design project).

## Verified

`./gradlew :app:compileDebugKotlin --rerun-tasks` → **BUILD SUCCESSFUL** (full run, not
incremental, on this exact tree). Pre-existing warnings only: deprecated
`useClasspathSnapshot`, and the KSP/Hilt "no dependencies reported" note. **Compile only —
the app was not launched**, so the week chart's per-bar entry animation and the peak bar's
dropShadow glow are verified to build, not verified to render/animate on device.
