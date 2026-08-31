# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-08-31 · **On top of:** `8dc89e8`

This round was scoped by Yash to **commit and push only** — no code completion, no build
verification. So unlike every prior note, there is no `BUILD SUCCESSFUL` line below: I read the
diff and reasoned about it, but did not run Gradle. Treat everything here as read-from-the-code,
not observed.

## What landed — `8dc89e8`

`TaskViewModel` now collects `taskRepository.getVisibleTasks()` into `TaskState.tasks`, so
`TaskDetail` reads the live task by id instead of the static sample list in `domain.model.tasks`
(`ForgeNavHost.kt` — the `com.development.forge.domain.model.tasks.find { … }` line is gone).
Toggling done/complete now dispatches `TaskIntent.ToggleDone`, which flips the task and marks its
subtasks complete together via the new `completeAllSubtasks`.

`StartCreate` and `StartEdit` centralize the two ways into `AddOrEditTask` — both now load the
existing-projects list via the new `getTopProjects` query, and every entry point (`TaskList` top
bar, bottom-nav add, `TaskDetail` edit) goes through one of them. Previously only some did, which
was the seed of the duplicate-task bug from two rounds back — this round's whole point is that class
of gap.

The system back handler now routes `AddOrEditTask` through `ClearForm` before popping, matching the
explicit `onBack` the screen already had, and falls back to a plain pop for any destination not
named in the two existing `clearToHome` / `resetToHome` sets, rather than doing nothing.

## ⚠️ Worth a look, found reading only — not verified, not fixed

- **`TaskEffect.NavigateBack` now clears the whole back stack**, where it used to be a no-op.
  `AddOrEditTask` is reachable from both `TaskList` and `TaskDetail` now, so a save from either path
  currently returns all the way to `Home` rather than one screen up. That may be intentional — it's
  a defensible product choice — but it's a behaviour change riding on a rename from `{}` to
  `backStack.clear()`, and I'd confirm it was deliberate rather than the obvious-looking fix for "the
  effect used to do nothing."
- **Two callbacks are still empty**, unchanged this round: `TaskList`'s `onToggle` (`ForgeNavHost.kt`
  entry for `TaskList`) and `TaskDetail`'s `onToggleSub`. The done-toggle wiring landed on
  `TaskDetail`'s `onToggle`/`onComplete` but not on `TaskList`'s row-level toggle, and subtask
  completion has no toggle at all yet — only the "mark all done when the task completes" path from
  `ToggleDone`.

## Carried over from the laptop's last note (unchanged; I did not re-verify any of it)

- **`TaskApi` still can't write** — one method, `getTasks(token)`, no POST/PUT/DELETE, and the
  client `TaskRepository` never references `TaskApi`. `PENDING_CREATE`/`PENDING_UPDATE`/
  `PENDING_DELETE` are written to Room and nothing drains them.
- **`PUT /tasks/{id}` is a measured full replace** — an omitted field is destroyed, not left alone,
  and `priority` reverts to the DTO default. The client's `updateTask` currently always passes all
  seven arguments from one call site, so this is dormant until a second call site omits one.
- **`BASE_URL` is `10.162.134.25`** (`6a0b8dd`) — eighth value in three days, wrong for this desktop.
  `local.properties` remains the cheapest unfixed item across every note.
- **The reset code (`dcc6807`) and client id (`b2c01c9`) are both verified against live Mongo** by
  the laptop last round — see its note for the walkthroughs. Nothing here touched either.
- `too_many_attempts`, `invalid_task_id`, `task_id_taken`, `invalid_google_token`,
  `id_token_required` are all still unmapped on the client, collapsing to `AuthError.UNKNOWN`.
- The sign-out credential leak (logout half), the reset-screen password inheritance from Login, the
  duplicate-`EmailSent` push, the orphaned pre-rename token, `userEmail()`'s zero call sites, the
  non-compiling release variant, zero server tests, `safeCall`'s stale `expectSuccess` KDoc,
  `createGoogleUser`'s `email_taken` on `/auth/google`, the unexercised `invalid_auth_provider` 409,
  the duplicate `@Delete vararg` on `TaskDao`, `core/mvi`'s zero implementors, `:shared`'s
  `implementation` vs `api` on `kotlinx-serialization-json`, `ForgeDatePickerDialog`'s NPE,
  hardcoded save/stats, `Motion.kt` empty, `SuccessScreen`'s undeleted reset button,
  `ForgeTextField`'s empty focus-ring stub (41st note), the dev JWT secret, `/auth/forgot` logging
  the code to console, CORS `anyHost()`, the Mongo password in source, no Room schema-export dir,
  the deprecated `useClasspathSnapshot` — all still open, all untouched this round.
- **Gradle daemon SIGSEGV = JBR 21 G1 GC fault, not memory pressure.** Not exercised this round —
  no Gradle command was run.

## Not done this round, deliberately

Per Yash's instruction: no code was completed or fixed, and `./gradlew … compileDebugKotlin …` was
not run. The two items in the "worth a look" section above are exactly that — observations from
reading the diff, left for whoever picks this up next rather than acted on.
