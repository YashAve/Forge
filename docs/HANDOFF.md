# Handoff note — desktop → laptop

**From:** desktop Claude session · **Date:** 2026-09-01 · **On top of:** `d4eeac4`

Same scope as the last three rounds: **commit and push only** — no code completion, no build
verification. Everything below is read from the diff, not observed at runtime.

## Update — `d4eeac4`: the `onToggleSub` gap from below is now closed

`TaskDetail`'s per-subtask checkboxes now dispatch `TaskIntent.ToggleSubtask`, which reaches
`SubtaskDao.toggleSubtaskDone`. That resolves the first bullet of the "carried over" list below —
delete that bullet when you get to it, I'm leaving the rest of this note as originally written.
`TaskEntity` also gained `completed_at` (set on done, cleared on un-done; DB now version 3),
`StatsScreen` takes live tasks instead of the sample list, and `TaskList`'s "open" now routes to
`TaskDetail` instead of straight into the edit form — the old direct-to-edit block is left
commented out in `ForgeNavHost.kt` rather than deleted, in case that was intentional and gets
reverted.

## `TaskEffect.NavigateBack` question from two notes ago — answered

I'd flagged that `backStack.clear()` might have been the "obvious-looking fix for a no-op" rather
than a deliberate choice, since `AddOrEditTask` is now reachable from both `TaskList` and
`TaskDetail`. It's now `backStack.removeLastOrNull()` — a single pop, returning to whichever screen
opened the form. That resolves the question the way I'd have guessed the product intent pointed.

## Subtask ids are now preserved across edits — this was a real duplication bug

`SaveTask` and both repository create/update calls carry `List<Subtask>` (id + title + isDone) end
to end now, instead of `List<String>`. The reason is in `AddOrEditTaskScreen`'s new KDoc, and it's
worth restating because it's non-obvious: the screen seeds its local `DynamicSubTaskUiItem` list
from `state.subtasks`, and **every previous save re-derived a subtask's id from scratch** rather than
carrying the stored one forward. Since `SubtaskDao.upsertSubTask` is `@Upsert`, a "same title, new
id" write doesn't update the existing row — it inserts a second one. So **every edit of a task with
subtasks was quietly duplicating them**, one extra row per subtask per edit, with the display title
looking correct because both rows had the same text.

Fixed by carrying the id forward when seeding the local list, and minting a fresh UUID only at the
moment a brand-new row is added via "Add subtask". Worth a look at whatever dev-database task
already has subtasks and has been edited more than once — the duplicates would have accumulated
silently and the UI would show the count as correct-looking (title-deduplicated) even if the
underlying table has extras.

## 🔴 Worth a look, found reading only: debug builds always skip auth

`SplashViewModel` gained:

```kotlin
companion object {
    private const val SKIP_AUTH_FOR_TESTING = true
}
...
if (BuildConfig.DEBUG && SKIP_AUTH_FOR_TESTING) {
    pendingDestination = SplashEffect.NavigateToHome
    return@launch
}
```

As written, **every debug build now routes straight to Home**, bypassing onboarding and login
unconditionally — `SKIP_AUTH_FOR_TESTING` is `true` in source, not a build variant or a local
override. That's presumably deliberate for iterating on the task screens without re-logging-in each
run, but it's worth flagging because it's easy to forget is on: nothing distinguishes "I meant to
leave this on" from "I forgot to flip it back," and it silently defeats manual testing of the
auth flow on debug builds — including the forgot/reset walkthrough the laptop verified two rounds
ago — until someone remembers this flag exists.

## Not done this round, deliberately

Per instruction: no code was completed or fixed, and no Gradle command was run. The subtask-id fix
and the `NavigateBack` change were already in the working tree when I started; I read them, verified
they're internally consistent by tracing the call sites (`Subtask` flows from `AddOrEditTaskScreen`
→ `TaskViewModel.saveTask` → `TaskRepository.createTask`/`updateTask` → `saveTask`'s
`SubTaskEntity` construction without a gap), and committed as-is.

## Carried over (unchanged since the last two notes; not re-verified)

- **`TaskList`'s row toggle and `TaskDetail`'s `onToggleSub` gap** — `TaskList`'s `onToggle` was
  fixed two rounds ago; `onToggleSub` (per-subtask completion) is still empty. Only "mark all
  subtasks done when the task completes" exists.
- **`TaskApi` still can't write** — one method, `getTasks(token)`, no POST/PUT/DELETE, and the
  client `TaskRepository` never references `TaskApi`.
- **`PUT /tasks/{id}` is a measured full replace** that destroys omitted fields. Dormant until a
  call site omits an argument — still only one call site, still passes everything.
- **`BASE_URL`**, the sign-out credential leak, the reset-screen password inheritance, the duplicate
  `EmailSent` push, the orphaned pre-rename token, `userEmail()`'s zero call sites, the
  non-compiling release variant, zero server tests, and the rest of the long-carried list — all
  unchanged, all untouched this round. See the last two notes for the full inventory.
- **Gradle daemon SIGSEGV = JBR 21 G1 GC fault, not memory pressure.** Not exercised — no Gradle
  command was run this round either.
