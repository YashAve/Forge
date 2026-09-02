# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-09-02 · **On top of:** `64812e0`

Per Yash's instruction this round: **commit and push only** — no code completion, no build
verification here. But the two commits landing (`88948e2`, `64812e0`) are the completion pass
from a full plan-then-execute session earlier today, and *that* work was compile-verified after
every phase as it happened — see the phase-by-phase detail below.

## The six-phase completion pass (`88948e2`)

Driven by a written spec (`forge-completion-prompt.md`), confirmed phase-by-phase before writing
code, `./gradlew :app:compileDebugKotlin` run and green after each phase individually.

**1 — Fixes.** `getDoneThisWeek` was `suspend fun … : Flow<Int>`, which Room forbids (a Flow is
already async); it's a plain `Flow` now. `TaskApi.getTasks` no longer wraps Ktor in
`withContext(Dispatchers.IO)`. The 20 hardcoded sample tasks are gone from `domain/model/Task.kt`;
`canvas/CanvasAndDrawing.kt` and `AddTaskSheet.kt` deleted (zero reachable callers — every function
in the former was `private` with no importer, the latter had no references anywhere).
`util/CanvasAndDrawing.kt` **kept** — public, unreferenced, but read as deliberate reference
material, Yash's call.

**2 — Keyboard.** `ForgeNakedValidationText` had no `keyboardOptions` at all — `ImeAction` was
literally inexpressible. Added it plus a caller-supplyable `FocusRequester`. Project→Notes now
chains via `ImeAction.Next`/`Done`; Title deliberately excluded (Yash's call — `maxLines = 3`
conflicts with a chained action button). Both chained fields `bringIntoViewRequester` themselves
above the IME. `isTakeFocus` previously attached a requester nothing ever called
`requestFocus()` on — fixed with a `LaunchedEffect(isTakeFocus)`.

**3 — Stats.** Every hardcoded figure replaced: streak, week chart (Monday-first, per-day
`GROUP BY`), "Done this week", "Best day". The week boundary moved **out of SQL into Kotlin**
(`TaskRepository.startOfCurrentWeekMillis`) because SQLite's `weekday` modifier can't express
"the current week" without reporting the *previous* one on Sundays. Bucketing uses `'localtime'`
— a UTC-day bucket would file a late-night IST completion under the wrong date. "Personal best"
and the whole "Best day" row now hide themselves rather than assert something the data doesn't
support (empty week → no claim, not a false one).

**4 — Debug seeder.** ~18 tasks behind a debug-only Settings row, guarded by `BuildConfig.DEBUG`
at both the UI and the function (belt-and-braces — R8 strips the body in release regardless).
Written `SyncState.SYNCED` so the seeder can't push fixtures to a real account. Completion dates
are chosen, not random: gaps at day 2 and day 8 produce a **current streak of 2 against a longest
of 5** — verified by mirroring the streak math in Python before trusting it, output:
`current streak = 2, longest streak = 5, "personal best" shown? False` — the harder case, since
the old hardcoded card asserted "personal best" unconditionally and could never be wrong the way
this now correctly is.

**5 — Sync.** `TaskApi` gained `createTask`/`updateTask`/`deleteTask`, matching `getTasks`'s shape
(token as a parameter, no DataStore reads inside `TaskApi`). `TaskRepository.syncPendingTasks()`
drains `getPendingTasks()` by `syncState` on app start and after every local mutation
(save/toggleDone/toggleSubtask). **Silence on an unreachable host is the specified contract, not
a shortcut** — the server is a LAN machine usually off, so nothing is surfaced to the user. A
transport failure (`IOException`) aborts the whole run (if the host didn't answer for task 1 it
won't answer for task 18); a protocol failure for one row skips only that row.

One thing added beyond the letter of the spec: **`deleteTask` now branches on `syncState`.** A
task still `PENDING_CREATE` (never uploaded) is hard-deleted immediately rather than tombstoned —
tombstoning it would overwrite the only evidence that the server never saw it, and the sync would
later send a `DELETE` for a task the server has no record of. Everything else is soft-deleted
(`PENDING_DELETE`) as before.

**6 — Cleanup.** `ForgeNavHost`'s `topBar`/`bottomBar`/`BackHandler` `when`s are now private
functions (`ForgeNavTopBar`, `ForgeNavBottomBar`, `navigateBack`); `entryProvider` deliberately
**not** touched, per spec. Hit a real type error extracting these: the destination sets inferred
`Set<ForgeNavKey>` inside `ForgeNavHost` (where `in` compiled against context), but
`currentDestination` is `NavKey?` from `rememberNavBackStack`, so the same check failed to
type-check once moved to a standalone function. Fixed by typing the sets `Set<NavKey>` explicitly
and restructuring `navigateBack` to check `currentDestination == null` first so the rest
smart-casts.

`SplashViewModel.SKIP_AUTH_FOR_TESTING` flipped to `false` — flagged independently in an earlier
note, now actually off. Its KDoc was also stale (called the class a *"structure-only stub wired
into MviViewModel"*; it's a plain `ViewModel()` with real routing logic) — rewritten to match
reality while touching the file.

`core/mvi/` deleted (both files). **Was not quite zero-references** as the earlier plan claimed:
`SplashViewModel` had a dangling `import … MviViewModel` referenced only in that same stale KDoc
comment, not in code. Caught during deletion, not before — worth noting since "grep says zero
users" and "actually zero users" differ by exactly this kind of dead-comment reference.

## `64812e0` — a caught bug, and a related one flagged but not fixed

Yash caught that `StatsScreen.kt:225` still read `"KEPT THE FIRE LIT FOR 9 DAYS"` — a literal that
survived Phase 3 because it's a *caption* above the streak card rather than part of the card
itself, so it wasn't in the enumerated list of elements checked against real data. Fixed: derives
from `currentStreak`, correct singular/plural, and the whole line now hides at zero (same rule as
"personal best" and "Best day" — a caption with nothing to report disappears rather than claiming
"FOR 0 DAYS").

**A related bug was found during the audit and is not yet fixed — flagging it here rather than
letting it sit only in commit history.** `centerSub = "today"` and
`"$done of $total tasks done today"` both derive from `tasks = getVisibleTasks()`, which has **no
date filter** — `total`/`done`/`percentage` are all-time figures, not today's. Seed the debug data
and the hero ring will claim something like "47% / TODAY" and "9 of 19 tasks done today" when the
real span is the last ten days. The numbers are correct; the word "today" is not. Recommended fix
is a one-word relabel (`"done"` instead of `"today"`) since "today's total" has no real denominator
— tasks aren't scoped to a day — but that's a copy decision, left for Yash rather than guessed at.

## Not verified this round or last

Nothing in `88948e2` has been run on a device or against a live server:

- Whether the IME actually surfaces the `Next` button on the chained fields, and whether
  `bringIntoView` cooperates with `.imePadding()`.
- Every HTTP call in the sync layer — no request has ever left the app. `createTask`/`updateTask`/
  `deleteTask` are typed against the `:shared` DTOs and the server contract as specified, but
  unexercised.
- The stats queries against real rows — compile-verified only; the seeded fixtures were
  hand-verified against a Python mirror of the streak math, not against Room itself.

**The week boundary is captured once at `TaskViewModel` init** — an app left open across a Monday
midnight won't roll "this week" over until the ViewModel is recreated. Known, not fixed; flagged
as a real edge case rather than built out, since it only bites a specific overnight scenario.

## Carried over from before this session (unchanged, not re-verified)

Everything in the last several handoff notes — `BASE_URL`, the sign-out credential leak, the
reset-screen password inheritance from Login, the duplicate-`EmailSent` push, the orphaned
pre-rename token, `userEmail()`'s zero call sites, the non-compiling release variant, zero server
tests, `safeCall`'s stale `expectSuccess` KDoc, `createGoogleUser`'s `email_taken` on
`/auth/google`, the unexercised `invalid_auth_provider` 409, `TaskDao`'s duplicate `@Delete
vararg`, `:shared`'s `implementation` vs `api` on `kotlinx-serialization-json`, the dev JWT
secret, `/auth/forgot` logging the reset code to console, CORS `anyHost()`, the Mongo password in
source, no Room schema-export dir, the deprecated `useClasspathSnapshot` — all still open.
`Gradle daemon SIGSEGV = JBR 21 G1 GC fault, not memory pressure` still holds; not exercised this
round either way.

## Verified

`88948e2`: `./gradlew :app:compileDebugKotlin` run and green after each of the six phases
individually, plus a final `--rerun-tasks` pass. `64812e0`: compiled clean before commit.
**This round's commit-and-push was scope-limited to exactly that** — no build command was run as
part of *this* handoff, per instruction; the green builds above are from the work session that
produced the commits, not from this push step.
