# Handoff note — laptop → desktop

**From:** laptop Claude session · **Date:** 2026-08-27 · **On top of:** `aae597a`

`./gradlew :app:compileDebugKotlin :server:compileKotlin :shared:compileKotlin --rerun-tasks` →
**BUILD SUCCESSFUL**, and `:app` now has **zero source warnings** — your dead-`else` removal landed
and holds. Only the two `@BsonId` KT-73255 notices remain, in `:server`.

Small round from me by design: **I ran the things you built.** Your last note's closing line was
"still nothing has been run — the rate limiter has never throttled anything, the 8-character code
has never been typed." All of that is now exercised against a live server and real Mongo.

## ✅ The question you aimed at this machine: the unique index is safe

You flagged that `users.resetToken`'s new unique partial index **fails at boot** if two rows already
share a code, and that you couldn't check without Mongo. Checked, by the only test that actually
matters — starting the server:

```
INFO c.d.forge.server.db.MongoDatabase - Mongo indexes ensured on 'forge' (users, tasks)
INFO Forge - Forge server listening on 0.0.0.0:9292 · mongo db 'forge'
```

No `E11000`, no duplicate-key failure. There *were* several live reset tokens on the collection from
my earlier testing (mixed 64-char hex and 8-digit), so this wasn't a vacuous pass — the index built
over real rows with real tokens in them. **`dcc6807` is safe to deploy.**

## ✅ `dcc6807` works, including the parts that are easy to get wrong

Every claim in your note about the reset code checks out end to end:

| Test | Result |
|---|---|
| Code format | `CH4NBFL5` — 8 chars, all within `ABCDEFGHJKLMNPQRSTUVWXYZ23456789` |
| `forgot` → `reset` → `login` with new password | 200 → 200 → 200 |
| Login with the *old* password afterwards | **401** — the write really landed |
| Submitted as `"  dx7x-dnyt  "` for code `DX7XDNYT` | **200** — lowercase, hyphen and whitespace all normalised |

That last row is the one worth having: case-folding and separator-stripping are exactly what a
human typing off a console will need, and they now demonstrably work rather than merely being
written.

## ✅ The rate limiter throttles — and one behaviour worth a decision

13 consecutive wrong codes from one IP:

```
attempts  1–10 → 400 invalid_token
attempt  11    → 429 too_many_attempts
attempts 12–13 → 429
```

Exactly the 10-per-window design, and `/auth/login` stayed unaffected throughout, so the limiter is
correctly scoped to `/auth/reset`.

**The behaviour to decide on:** because the check runs *before* the token lookup — which is right,
it's what stops the lookup being the oracle — a **correct** code is also rejected once you're
limited:

```
POST /auth/forgot            → 200   (still issues a fresh code)
POST /auth/reset  <correct>  → 429   (blocked anyway)
```

That's defensible and I'd not change it silently, but it's a real UX edge: a user who fat-fingers
their code ten times is locked out of their own reset for fifteen minutes, and `/auth/forgot`
cheerfully keeps minting codes that can't be used. The honest options are to count only *malformed*
attempts against the limit, to exempt a correct code, or to leave it and make the client surface
the wait clearly — which it currently can't, because `too_many_attempts` is unmapped.

## ✅ `b2c01c9` also runs — including the ownership check

You reasoned carefully about this one and measured the UUID leniency in isolation. All of it holds
against a live server and real Mongo.

| Case | Result |
|---|---|
| New client-generated UUID | **201** + stored task |
| Same id re-sent, different body | **200**, returning the **stored** task — not the retry body |
| No id supplied | **201**, server-minted |

The second row is the one the client has to understand, and it behaves exactly as your note
predicted: I re-sent id `fb476c8e…` with a different title and priority and got the *original*
back. So an offline client that edits between a failed create and its retry must follow the 200
with a `PUT`, or the edit is silently lost.

**Id validation rejects all five hostile shapes**, including the two `UUID.fromString` alone would
have swallowed:

```
"1-1-1-1-1"                     -> 400 invalid_task_id   (fromString pads to 00000001-…)
"FB476C8E-…-310CE9323084"       -> 400 invalid_task_id   (fromString case-folds)
"1787835378027-a8fa008f-…"      -> 400 invalid_task_id   (the old getPrimaryId format)
""                              -> 400 invalid_task_id
"hello-world"                   -> 400 invalid_task_id
```

The round-trip in `Validation.taskId` closes both aliasing holes. The uppercase case is the one that
would have hurt: `Filters.eq("_id", …)` is exact, so accepting it really would have produced two
documents for one UUID.

**The ownership check does what you said it does.** A second account trying to claim user 1's task
id gets a deliberately vague `409 task_id_taken` and **no data**:

```
user2 POST /tasks {id: <user1's task id>}  -> 409 task_id_taken
user2 GET  /tasks                          -> []          (no leak)
user1 GET  /tasks                          -> both of their own tasks
```

So the "catch duplicate, return the existing task" shortcut you rejected would indeed have handed
one user another user's task. Scoping holds on both the read and write paths.

## 🔧 The task form was wired up, and the edit path was duplicating tasks

Two commits after the note above was first written — flagging them because the second is a data bug,
not plumbing.

`8efc2f1` (Yash's) connects `AddOrEditTaskScreen` to `TaskViewModel`. It had been called with only a
modifier and an `onBack`, so nothing typed into it ever reached the ViewModel. `SaveTask` also
changed shape to carry its subtask titles as `List<String>`.

`6f8fbc6` then wires the four routes that reach that screen. **All four navigated without telling
the ViewModel what they were opening**, and `TaskViewModel` is scoped to the nav host, so its state
outlives any single visit:

| Route | Was |
|---|---|
| TaskList top-bar `+` | an empty lambda — did nothing at all |
| Bottom-nav add, TaskList `onAdd` | navigated without clearing; form held the last task edited |
| TaskDetail edit | navigated without `StartEdit`, so `editingTaskId` was never set |

That last one matters: with `editingTaskId` null, `saveTask()` takes the create branch, so **editing
a task and saving would have written a second task** rather than updating it. Silent duplication,
and only reachable once `8efc2f1` made the screen functional.

`TaskIntent.ClearForm` and `TaskIntent.StartEdit` already existed and were already dispatched in
`onIntent` — neither had a caller. The fix only adds the dispatches.

This is also the long-carried "`+` always opens Edit Task on task[0]" item: nothing ever reset the
form, so once any task had been opened for editing, every later "add" reopened it. That line can
come off the carried-over list.

**Not run.** The save path still has no device pass, and `TaskRepository.createTask` sits on the
offline-first Room layer that doesn't build yet.

## What the Android client still owes (unchanged, now with evidence)

`too_many_attempts` is reachable in under a second of tapping, and it maps to `AuthError.UNKNOWN`.
So the user gets a generic failure with no indication they're throttled or for how long. Same for
`invalid_task_id`, `task_id_taken`, `invalid_google_token`, `id_token_required`.

## ⚠️ `BASE_URL` moved again — `10.162.134.25` (`6a0b8dd`)

**Seventh value in three days.** It was `10.37.153.25` in your note, and had already moved twice
more before I got to it. `local.properties` is now nineteen notes old as an open item and has cost
a round-trip in most of them. It is the cheapest thing on either of our lists.

## Housekeeping

- **A collision worth knowing about**: I had independently written an 8-digit numeric reset code on
  this machine before pulling — Yash asked for it here too. Yours is strictly better (40 bits vs 27,
  no ambiguous glyphs, unbiased masking, plus the rate limiting and unique index), so mine was
  dropped rather than merged. `stash@{0}` on this machine now holds only that superseded change and
  a stale IP; it can be dropped.
- The dev database has accumulated throwaway rows from testing: `codec-probe@example.com`,
  `codec-probe2@example.com`, `reset-walk@example.com`, `token-walk@example.com`. Two real Google
  accounts also exist (`agarwalyash929@gmail.com`, `enlightmentyash3769@gmail.com`) plus
  `karishmakapoor@gmail.com`, whose password is currently `eight-digit-test` from an earlier walk.
- Your uncommitted Room breakage is still uncommitted here too — nothing in this pull touches it.

## Carried over (still open, untouched)

- **The Home back-button bug — nineteenth note running.** Still a product decision, still unmade.
- **The sign-out credential leak, logout half** — `ResetFields` still not dispatched in the
  `SettingsEffect.NavigateToLogin` handler. The login half is fixed; logout is the last path.
- **The reset screen inherits the password typed on Login** — `onForgotPassword` is the one
  transition that doesn't clear. Still a design call about whether `email` keeps carrying forward.
- **"Resend link" pushes a duplicate `EmailSent`** and resets its own cooldown.
- **The orphaned pre-rename token** under `"auth_is_stored"`.
- **`userEmail()` has zero call sites** — `SettingsScreen` still shows `karishma.kapoor@cosmiq.co.in`.
- **The release variant still does not compile** — `BASE_URL` is `debug`-only.
- **Zero test files under `server/src`.** A rate limiter and a UUID validator now ride on
  compile-checking alone; I exercised both by hand this round, which is not the same as a test.
- The reset-token field's helper still reads "From the reset email."; `EmailSentScreen` hardcodes
  "60 minutes". I confirmed last round that 60 is currently *correct* — it matches
  `resetTokenValidityMillis` — but it will drift the moment `RESET_TOKEN_VALIDITY_MS` is set.
- `safeCall`'s KDoc still claims `expectSuccess = false` is configured. Seventh note.
- `createGoogleUser` can still throw `email_taken` on `/auth/google`.
- The 409 `invalid_auth_provider` branch remains unexercised — it needs a Google token for an
  address that already exists as a password account.
- `TaskDao.deleteTask`/`deleteTasks` duplicate `@Delete vararg`, only one `suspend`.
- `core/mvi` base still has zero implementors while four ViewModels hand-roll the same shape.
- `:shared` still declares `implementation(libs.kotlinx.serialization.json)`, not `api(...)`.
- `ForgeDatePickerDialog` NPEs on OK-without-selection; save hardcoded; `AddTaskSheet.kt`
  orphaned; Task List bucket-overlap crash. (New-task capture is now reachable — see above.)
- Server `TaskDto` exposes `due: String?` vs. the local typed `Instant?`.
- Stats hardcoded; `Motion.kt` empty; `SuccessScreen` reset button not deleted.
- **`ForgeTextField` focus ring still an empty `drawBehind { }` stub — 40th note running.**
- Server-side: dev JWT secret in source, `/auth/forgot` logs the reset code, CORS `anyHost()`,
  `PUT /tasks/{id}` is a full replace not a merge.
- Mongo password in `ServerConfig.kt:28`. Private repo, so hygiene not exposure.
- No Room schema export dir; `gradle.properties` has dead `kapt.*` and a deprecated
  `useClasspathSnapshot`.
- **Gradle daemon SIGSEGV = JBR 21 G1 GC fault, not memory pressure.** `-XX:+UseParallelGC` on every
  invocation. No crash this round.

## Verified

Forced `--rerun-tasks` across all three modules → BUILD SUCCESSFUL, `:app` with zero source
warnings. Server started against real Mongo with the new unique index — **it built**. Reset code
generated, normalised, redeemed, and confirmed to have changed the password; old password rejected
afterwards. Rate limiter tripped at attempt 11 and confirmed to block a valid code too.

`b2c01c9` exercised end to end against the live server — see the section above.

**Not run this round**: no device pass on the new code — the phone was unplugged, so nothing here
went through the app's own HTTP client. The offline-first Room layer remains unbuilt and unrun.
