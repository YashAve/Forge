# Handoff note — laptop → desktop

**From:** laptop Claude session · **Date:** 2026-07-20 · **On top of:** `197b1c4`

Tree is green: `./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL**.

**This is a work-in-progress push.** The Stats screen is mid-build — the new
`ForgeProgressRing` Canvas is complete and real, but the screen around it stops partway
(see ⚠️ 1). Committing it so the desktop has the ring component and can continue from the
same point, not because Stats is done.

## ⚠️ Read first

**1. `StatsScreen` is a partial screen — it ends inside the streak card.** What's built and
real: the header (title + mono subtitle), the hero `ForgeProgressRing`, and the
"$done of $total tasks done today" caption. Then the streak `ForgeCard` shell renders with
its 64dp fire-icon tile on the left — but **its right-hand `Column(weight = 1f)` is empty**,
so the "9 / days / Current streak · personal best" content the spec calls for is missing.
The card shows an icon and blank space. And **sections 4 (the two supporting stat cards) and
5 (the week chart / WeekBars) don't exist yet** — the spec for them survives as a KDoc block
inside the file. So on screen right now: title, ring, caption, half a card. It compiles and
won't crash; it's just visibly unfinished. Pick up at the empty `Column`.

**2. The real, finished piece here is `ForgeProgressRing`** — and it's the app's first
arc-based Canvas, so it's worth a look even though the screen isn't done. Two `drawArc`s
(dark 360° track + ember-gradient progress sweep) inside an `inset(strokePx/2)` so the round
caps don't clip, `startAngle = -90f` to begin at 12 o'clock, and the sweep-from-0 entry
animation done correctly: `var target by remember { mutableFloatStateOf(0f) }` +
`LaunchedEffect(value) { target = value }` feeding `animateFloatAsState`, because
`animateFloatAsState` alone won't animate on first composition. Plus a breathing ambient
glow via `rememberInfiniteTransition` (alpha 0.20↔0.40, 3600ms reverse). This is the "Canvas
ring animation" task from the fixture (`task9`) actually built.

**3. The ring value round-trips through an integer percent and loses precision.**
`StatsScreen` computes `percentage = done * 100 / total` (Int), then passes
`value = percentage.toFloat() / 100f` to the ring while also using `"$percentage%"` as the
label. So the arc and the number always agree — good — but both are quantised to whole
percents (e.g. a true 65.7% draws as 65% / 0.65). Fine for a prototype; note it if the ring
ever needs to look continuous.

## What this push adds (Yash's code)

**`ForgeProgressRing(value, size, stroke, centerLabel, centerSub)`** — described above.
Defaults to a 224dp / 18dp-stroke ring at 65%. Centre label is JetBrains Mono 40sp over a
mono uppercase sub-label. It's a top-level `@Composable` in `StatsScreen.kt` (not its own
file — worth extracting to `core/components` once Stats settles, since it's reusable).

**`StatsScreen` wired to real task data** — `total = tasks.size`, `done = tasks.count { done }`,
integer `percentage`. With the current 20-task fixture (2 done) that's 10% → "2 of 20 tasks
done today". Uses the whole list, matching the spec's "prototype uses all" note.

**`ForgeNavHost`**: one cosmetic line — `rememberNavBackStack(ForgeNavKey.Splash)` →
`rememberNavBackStack(Splash)`, using the import alias already present. No behavioural change.

### Note on a carried default that happens to be right here

`StatsScreen`'s streak card is `ForgeCard(modifier = Modifier.fillMaxWidth())` with no
`accent` argument — and the spec wants `accent = true`. It gets it, because `ForgeCard`'s
default is still `accent = true` (the long-standing "default drifts from spec" item). So
this call site is correct *by accident of the wrong default*. Flagging because the day that
default is fixed to `false`, this card silently loses its accent styling.

## Rough edges (documented, not fixed)

**`StatsScreen.kt` has no trailing newline** (`\ No newline at end of file`) — same shape as
`ForgeNavKey.kt` last push.

**Two `@Preview`s in one file** — both `ForgeProgressRing` and `StatsScreen` are annotated,
which is handy for iterating the ring in isolation but means the file renders two preview
tiles. Intentional-looking; noting so it's not mistaken for a stray.

**The section-4/5 spec is now a live KDoc block *inside* the composable body**, not above it
— it sits between the caption and the streak card as a `/** … */`. It'll want deleting once
those sections are built, the way the other screens' spec KDocs were consumed.

## Carried over (still open)

- **Task List bucket overlap**: `overdue && due == null` lands in both TODAY and SOMEDAY →
  duplicate `LazyColumn` key → crash. Still latent (no fixture task hits it). One-line fix:
  `someday = active.filter { it.due == null && !it.overdue }`.
- **`TaskListScreen`'s `onBack` / `onAdd` are dead params** since the top bar moved to the
  nav host, but `ForgeNavHost` still passes them. Plus the orphaned `// ── Top bar ──`
  comment + `Spacer(18.dp)` where the inline bar used to be.
- **`ForgeNavKey.kt`** still missing its trailing newline and the class-level KDoc explaining
  why every key must be `@Serializable` (Nav3 back-stack save/restore across process death).
- **No mutable state anywhere.** `onToggle` / `onComplete` / `onToggleSub` / `onAdd` /
  `onEdit` are all TODO stubs against the immutable top-level `val tasks`. Four screens now
  navigate perfectly and mutate nothing. A ViewModel (or hoisted `mutableStateListOf`) is
  still the one change that brings the whole app to life — and Stats now depends on it too
  for a live completion count.
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
- `designsystem/Color.kt` still empty of palette — `0xFFF5EDDE` inlined ~40×, now also
  `0xFF7A7060` / `0xFF251F14` / the ember hex triplet in the ring.
- `gradle.properties`: dead `kapt.*`; inert `org.gradle.tooling.parallel=true`; deprecated
  `useClasspathSnapshot` warns.
- `SuccessScreen` "Reset prototype" button still not deleted.
- **`codelabs/original/` drift still unreverted** — two files with `fun HomeScreen` →
  `private fun HomeScreen`. `git checkout 55575c3 -- codelabs/original/`.
- Auth tail unchanged: EmailSent countdown/cooldown/unwired resend · px ember/width/
  textAlign · ForgeApplication dead font warm-up · email not threaded through nav ·
  letterSpacing typos · `isLoading` restore trap · inline ForgeDivider ·
  **ForgeTextField focus ring still deleted (19th note running — restore block at
  `git show 0c6ffa9:docs/HANDOFF.md`).**

## Toolchain

Unchanged. Laptop runs Android Studio Quail 2's healthy JBR 21.0.10. No repo toolchain
config touched.

## Screen status — real vs stub

Auth six near-complete. Onboarding feature-complete. **Home / Task List / Task Detail:
visually complete, fully navigable, inert below the nav layer.** **Stats: in progress** —
`ForgeProgressRing` done, screen built through the header/ring/caption and into a
half-finished streak card; stat cards + week chart not started. **Settings**: stub. Bottom
nav: tabs replace correctly, active colour works, icon swap doesn't. Boot seeds at `Splash`.

Design source is still the **Forge Design System** (external Claude Design project).

## Verified

`./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL** (full run on this exact tree).
Pre-existing warnings only: deprecated `useClasspathSnapshot`, and the KSP/Hilt "no
dependencies reported" note. **Compile only — the app was not launched**, so the ring's
entry sweep and breathing glow are verified to build, not verified to animate on device.
