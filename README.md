# Forge

A full-stack task manager for Android, built as a portfolio piece to demonstrate
production-grade architecture end to end — from a Jetpack Compose UI down through an
MVI presentation layer, a Room cache, and a Ktor client, to a self-hosted Ktor +
MongoDB backend sharing DTOs with the app through a common Gradle module.

Forge is deliberately over-engineered for its size. A to-do app does not *need* a
JWT-authenticated backend or a shared serialization module. The point is to show the
patterns working together correctly, with the trade-offs made explicitly rather than
by default.

---

## Highlights

- **Single source of truth, everywhere.** UI state lives in one place per feature and
  the screen is a pure function of it. No screen owns a private copy of anything the
  ViewModel already holds.
- **MVI with a deliberate exception.** A shared contract of `State` / `Intent` /
  `Effect` drives the stateful features — and screens that have no state (the splash
  screen) intentionally *don't* use it, rather than inventing empty state to satisfy a
  base class.
- **One repo, three modules, shared DTOs.** `:app`, `:server`, and a `:shared`
  Kotlin/JVM module that both depend on, so the client and server can never disagree
  about the shape of a request or response.
- **Effects vs. navigation, drawn on purpose.** A ViewModel emits an effect only when
  it makes a *decision* (validate → call server → store token → navigate). Pure
  screen-to-screen moves are plain back-stack calls, not ceremony routed through the
  state machine.
- **Security-aware backend.** Passwords hashed with bcrypt, JWT-issued sessions,
  login and password-reset endpoints written to resist account enumeration.

---

## Architecture

Forge is a Gradle multi-module project:

```
:app       Android client — Compose UI, MVI, Room, DataStore, Ktor client
:server    Ktor + MongoDB backend — auth, tasks, JWT
:shared    Kotlin/JVM — request/response DTOs used by both :app and :server
```

`:shared` targets JVM 11 bytecode so the Android module can consume it while the
server compiles it on JDK 17 — one small toolchain detail that lets a single set of
DTOs serve both sides.

### Client (`:app`)

A single-Activity Compose app. Each feature is a self-contained package with a
`contract` (its `State`, `Intent`, `Effect`), a `ViewModel`, and stateless `ui`
composables.

- **UI** — Jetpack Compose with a hand-built component library and design system
  (typography, colour tokens, custom fields, buttons, and dividers). No Material
  theming shortcuts; the visual language is bespoke.
- **Navigation** — Navigation 3. ViewModels are resolved at the nav-host entry level
  and passed into screens as state + callbacks, so screen composables never touch a
  ViewModel directly and stay trivially previewable.
- **Presentation** — MVI. Unidirectional flow: the screen sends an `Intent`, the
  ViewModel updates `State` (a `StateFlow`) and emits one-shot `Effect`s (a buffered
  `Channel`) for navigation.
- **Local storage** — Room for tasks (with a `Task` → `SubTask` relation and typed
  columns), DataStore for preferences and the session token.
- **Networking** — Ktor client on the OkHttp engine, with a repository layer that maps
  transport failures and server error codes onto a typed domain error, so the UI never
  sees an `HttpResponse` or an exception.

### Backend (`:server`)

A Ktor server backed by MongoDB.

- Coroutine-based MongoDB driver.
- bcrypt password hashing.
- JWT-issued sessions; task routes require a valid token.
- Structured error responses with stable machine-readable codes.
- Anti-enumeration: login returns an identical `401` for "no such user" and "wrong
  password", and password-reset returns an identical `200` whether or not the email
  exists.

---

## Tech stack

**Client** — Kotlin, Jetpack Compose, Navigation 3, Hilt, Room, DataStore,
Ktor client (OkHttp), kotlinx.serialization, Coroutines/Flow.

**Backend** — Kotlin, Ktor (Netty), MongoDB (Kotlin coroutine driver), JWT, bcrypt,
Logback.

**Shared** — Kotlin/JVM, kotlinx.serialization.

Notable versions: Kotlin 2.3, AGP 8.13, Compose BOM 2026.06, Hilt 2.57, Room 2.7,
Ktor 3.1, Navigation 3 (1.1).

---

## Design decisions worth calling out

**Why MVI here.** Auth has real state that changes over time, async work in the
middle, and multiple ways to fail. That's exactly the case a state machine earns its
keep in. The splash screen, which only needs to fire a one-shot navigation, does *not*
use the MVI base — applying the pattern where it adds nothing would be cost without
benefit.

**Typed errors, not exceptions, across the boundary.** The repository catches
transport errors and maps server error codes to a domain `AuthError` enum. The
ViewModel and UI reason about `INVALID_CREDENTIALS` or `NETWORK_ERROR`, never about
HTTP status codes or `IOException`. Error *text* is chosen in the UI layer, in the
app's voice — business logic doesn't own copy.

**Validation lives in the ViewModel.** The submit button is never a silent gate.
Tapping it always does something; if input is invalid the ViewModel sets an error that
surfaces on the relevant field. This avoids the common failure where a disabled button
leaves a user stuck with no explanation.

**One session token, one owner.** The token is written on login/registration and
removed on logout. The splash screen reads it (plus an onboarding flag) to route a
cold start to Onboarding, Login, or Home.

---

## Project status

Forge is an active work in progress. Current state:

**Complete**

- Full UI: splash, onboarding, all six auth screens, home, task list, task detail,
  stats, settings, add/edit task, and the custom component library.
- `:server` — all auth endpoints (register, login, forgot, reset) and task routes,
  with JWT and MongoDB.
- `:shared` DTO module consumed by both sides.
- Room cache, Hilt graph, DataStore preferences.
- Login and registration wired end to end through the MVI stack; the cold-start auth
  gate (onboarding flag + token) wired.

**In progress / by design**

- On-device verification of the auth flows against the live backend is pending (the
  server runs on a separate machine on the LAN).
- **Password-reset email is intentionally out of scope.** The server issues and
  validates reset tokens exactly as production would, but delivery is stubbed — the
  token is logged server-side rather than emailed. Email delivery is a provider
  integration, not an Android concern, so it was deliberately left out.
- **Google sign-in is a planned second pass**, after email/password is verified end to
  end. It needs a dedicated server endpoint to verify the Google ID token.

Scope was cut deliberately and is documented as such, rather than hidden.

---

## Building

Each module builds with the Gradle wrapper.

```bash
# Android client
./gradlew :app:assembleDebug

# Backend (requires a local MongoDB)
./gradlew :server:run
```

The client reaches the backend over the LAN by IP; the base URL is a debug build
config field, and a debug-only network-security config permits cleartext to the
development server. Release builds carry neither.

---

## Why this project exists

Forge is the first of a series of Android projects built to demonstrate depth across
the platform — architecture, custom Compose UI, local persistence, networking, and a
real backend — rather than breadth of features. The code is written to be read: each
decision above has a reason, and the reasons are the point.
