# ARCHITECTURE.md — StreamKit

**Version:** 0.1.0
**Status:** Draft
**Owner:** Danielle Mariani
**Created at:** 2026-06-15
**Last Updated:** 2026-06-15

---

## Overview

StreamKit is a streaming-first application. The primary architectural constraint is that every platform — Android mobile, Fire TV, and backend — is built around real adaptive bitrate streaming protocols (HLS/DASH), real DRM (Widevine), and real telemetry. No mocked playback, no stubbed pipelines.

The Android project is a single Gradle monorepo with three modules: `core` (shared player logic, networking, DRM, telemetry, data models), `app` (mobile UI — Jetpack Compose, touch, portrait), and `tv` (Fire TV UI — Compose for TV, D-pad, lean-back). The backend is a FastAPI + PostgreSQL service responsible for video ingestion, packaging, DRM license proxying, and telemetry collection. Both Android and backend live in the same repository.

Phase 1 establishes the player foundation on Android. Subsequent phases extend the platform surface (Chromecast, Fire TV) and introduce the backend pipeline (ingestion, DRM, telemetry). No phase should be started before the prior phase spec is complete and its core acceptance criteria are met.

---

## Repository Structure

```
streamkit/
├── android/                         # Single Android Gradle project
│   ├── core/                        # Shared module — player, DRM, networking, data
│   ├── app/                         # Mobile UI module (phone/tablet)
│   ├── tv/                          # Fire TV UI module (lean-back, D-pad)
│   └── build.gradle.kts
├── backend/                         # FastAPI + PostgreSQL
│   ├── app/
│   └── ...
├── specs/
│   ├── features/                    # One subfolder per feature
│   │   ├── catalog/
│   │   ├── media-player/
│   │   ├── live-player/
│   │   ├── pip/
│   │   ├── cast/
│   │   ├── fire-tv/
│   │   ├── ingestion-pipeline/
│   │   ├── drm/
│   │   └── telemetry/
│   ├── design/
│   │   ├── design.md
│   │   └── navigation.md
│   └── technical/
│       ├── data-model.md
│       ├── api-contract.md
│       ├── streaming-glossary.md
│       └── content-catalog.md
├── .cursor/
│   └── rules/                       # AI coding rules (.mdc files)
├── PRODUCT.md
├── SPEC.md
├── ARCHITECTURE.md
├── ROADMAP.md
└── CONTEXT.md
```

---

## Phase Overview

| Phase | Name | Platform | Backend | Status |
|---|---|---|---|---|
| 1 | Adaptive Bitrate Player | Android (`app`) | None | Not Started |
| 2 | Multi-screen / Chromecast | Android (`app`) | None | Not Started |
| 3 | Smart TV: Fire TV | Android (`tv`) | None | Not Started |
| 4 | Video Ingestion Pipeline | — | FastAPI + PostgreSQL | Not Started |
| 5 | DRM & Content Protection | Android (`app` + `tv`) | FastAPI (license proxy) | Not Started |
| 6 | Player Telemetry & QoE | Android (`app` + `tv`) | FastAPI + PostgreSQL | Not Started |

---

## Android Architecture

### Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI — Mobile | Jetpack Compose |
| UI — Fire TV | Compose for TV (`androidx.tv`) |
| Architecture Pattern | MVI + Repository |
| Dependency Injection | Hilt |
| Player Engine | ExoPlayer / Media3 |
| Networking | Ktor Client |
| Local Database | Room |
| Preferences | DataStore |
| Async | Coroutines + Flow |
| Build | Gradle (Kotlin DSL) |
| Min SDK | API 31 (Android 12) |

---

### Module Structure

```
android/
├── core/          # No UI. Pure logic — consumed by app and tv
├── app/           # Depends on core. Mobile UI only
└── tv/            # Depends on core. Fire TV UI only
```

Dependency rule: `app → core` and `tv → core`. Neither `app` nor `tv` imports the other. `core` has no knowledge of `app` or `tv`.

---

### MVI Pattern

StreamKit uses MVI (Model-View-Intent) across all features in both `app` and `tv`. Player state is complex and continuous — buffering, bitrate, DVR position, PiP mode, cast session — and MVI's single immutable state snapshot prevents state divergence across multiple observables.

```
Composable (View)
    │
    │  Intent (user action or player event)
    ▼
ViewModel
    │  reduces Intent + current State → new State
    ▼
UiState (single immutable data class)
    │
    ▼
Composable re-renders
```

Key rules:
- Every screen has exactly one `UiState` data class representing its complete state
- The `ViewModel` is the only entry point for state mutation — Composables never mutate state directly
- Player events (bitrate change, buffer stall, position update) are modeled as `Intent` and flow through the same reducer as user actions
- Side effects (navigation, one-time errors) are emitted as a separate `UiEffect` channel, never embedded in `UiState`
- All `ViewModel` logic is covered by unit tests via fake repositories

---

### Layered Architecture

```
Composable (View)
    └── ViewModel (MVI reducer, UiState, UiEffect)
        └── UseCase (single business operation)
            └── Repository (interface — data access contract)
                ├── LocalDataSource (Room / DataStore)
                └── RemoteDataSource (Ktor Client — Phase 2+)
```

Rules:
- Composables never access Repository or UseCase directly
- UseCases are single-purpose — one operation per class
- Repositories are interfaces in `core/domain/`; implementations live in `core/data/`
- All data access is asynchronous via Coroutines and Flow
- Mappers convert between domain models, Room entities, and network DTOs at layer boundaries

---

### Core Module Structure

```
core/
├── player/
│   ├── StreamKitPlayer.kt          # ExoPlayer/Media3 wrapper
│   ├── PlayerState.kt              # Bitrate, buffer, position, playback state
│   └── PlayerEventMapper.kt        # Maps ExoPlayer events to domain events
├── domain/
│   ├── model/
│   │   ├── Video.kt
│   │   ├── PlaybackSession.kt
│   │   ├── VideoRendition.kt
│   │   └── TelemetryEvent.kt
│   └── repository/
│       ├── VideoRepository.kt
│       ├── PlaybackRepository.kt
│       └── TelemetryRepository.kt
├── data/
│   ├── local/
│   │   ├── StreamKitDatabase.kt
│   │   ├── VideoDao.kt
│   │   ├── PlaybackSessionDao.kt
│   │   └── TelemetryEventDao.kt
│   ├── remote/                     # Phase 4+
│   │   ├── MuxApiClient.kt
│   │   └── StreamKitApiClient.kt
│   └── repository/
│       ├── VideoRepositoryImpl.kt
│       ├── PlaybackRepositoryImpl.kt
│       └── TelemetryRepositoryImpl.kt
├── drm/                            # Phase 5
│   ├── WidevineManager.kt
│   └── LicenseRequestHandler.kt
├── telemetry/                      # Phase 6
│   ├── TelemetryCollector.kt
│   └── TelemetryDispatcher.kt
└── di/
    ├── PlayerModule.kt
    ├── DatabaseModule.kt
    ├── NetworkModule.kt
    └── RepositoryModule.kt
```

---

### App Module Structure

```
app/
├── MainActivity.kt
├── AppNavGraph.kt
└── features/
    ├── catalog/
    │   ├── CatalogScreen.kt
    │   ├── CatalogViewModel.kt
    │   └── CatalogUiState.kt
    ├── player/
    │   ├── PlayerScreen.kt
    │   ├── PlayerViewModel.kt
    │   ├── PlayerUiState.kt
    │   └── PlayerUiEffect.kt
    ├── live/
    │   ├── LivePlayerScreen.kt
    │   ├── LivePlayerViewModel.kt
    │   └── LivePlayerUiState.kt
    ├── pip/
    │   └── PipManager.kt
    ├── cast/                       # Phase 2
    │   ├── CastManager.kt
    │   └── CastUiState.kt
    └── settings/
        ├── SettingsScreen.kt
        └── SettingsViewModel.kt
```

---

### TV Module Structure

```
tv/
├── MainActivity.kt
├── TvNavGraph.kt
└── features/
    ├── catalog/
    │   ├── TvCatalogScreen.kt
    │   └── TvCatalogViewModel.kt   # Reuses core UseCases
    └── player/
        ├── TvPlayerScreen.kt
        ├── TvPlayerViewModel.kt    # Reuses core UseCases
        └── TvPlayerUiState.kt
```

`tv` ViewModels reuse the same UseCases from `core` as `app` ViewModels. Only the Composables differ — Compose for TV components replace standard Compose components, and D-pad focus management replaces touch handling.

---

### Local Storage

| Store | Technology | Contents |
|---|---|---|
| `streamkit.db` | Room | Catalog cache, playback positions, telemetry event buffer |
| `settings.pb` | DataStore | Selected environment (Mux / Local), player preferences |

Rules:
- All Room entities include `created_at` and `updated_at` timestamps (Unix, UTC)
- Telemetry events are buffered in Room when the backend is unreachable and flushed on reconnect
- Playback position is persisted per `video_id` after every 5-second interval during playback
- Full schema: `specs/technical/data-model.md`

---

### Navigation

Jetpack Navigation with a nested nav graph architecture. `AppNavGraph` in the `app` module hosts the single `NavHost` and composes feature-owned nested nav graphs. `TvNavGraph` mirrors this pattern in the `tv` module using Compose for TV navigation.

Top-level destinations:

| Module | Destinations |
|---|---|
| `app` | Catalog, Player (VOD), Live Player, Settings |
| `tv` | Catalog (lean-back), Player (VOD), Live Player |

Rules:
- Features never import each other's nav graphs
- Deep links into the player always pass `video_id` as the navigation argument — never a full `Video` object
- PiP is not a navigation destination — it is a window mode transition managed by `PipManager`

---

### Dependency Injection

Hilt provides the DI graph. Modules are defined per concern in `core/di/`:

| Module | Provides |
|---|---|
| `PlayerModule` | `StreamKitPlayer`, `PlayerEventMapper` |
| `DatabaseModule` | `StreamKitDatabase`, all DAOs |
| `NetworkModule` | Ktor `HttpClient`, `MuxApiClient`, `StreamKitApiClient` |
| `RepositoryModule` | Binds all Repository interfaces to implementations |
| `DrmModule` | `WidevineManager`, `LicenseRequestHandler` (Phase 5) |
| `TelemetryModule` | `TelemetryCollector`, `TelemetryDispatcher` (Phase 6) |

---

### Environment Switching

The Settings screen exposes an environment selector with two options:

| Environment | Catalog Source | Stream Source |
|---|---|---|
| **Mux** | Mux API | Mux CDN delivery URLs |
| **Local** | Local backend API | Locally served HLS (Phase 4+) |

The selected environment is persisted in DataStore and injected at the `NetworkModule` level — no feature code references the environment directly.

---

## Backend Architecture

### Stack

| Layer | Technology |
|---|---|
| Language | Python |
| Framework | FastAPI |
| ORM | SQLModel |
| Database | PostgreSQL |
| Migrations | Alembic |
| Video Transcoding | FFmpeg |
| Video Packaging | Shaka Packager |
| Testing | pytest |
| Runtime | Local only (development) |

---

### Architecture

The backend follows a layered service architecture. Route handlers are thin — they validate input and delegate to the service layer. Business logic never lives in route handlers.

```
HTTP Request
    └── Route Handler (FastAPI router)
        └── Service (business logic)
            └── Repository (database access via SQLModel)
                └── PostgreSQL
```

Rules:
- Business logic lives in the service layer, never in route handlers
- Request and response schemas are separate Pydantic models — never the ORM model directly
- All database access is async via SQLModel + asyncpg
- FFmpeg and Shaka Packager are invoked as subprocesses from the ingestion service
- No authentication — local development only

---

### Package Structure

```
backend/
├── app/
│   ├── api/
│   │   └── v1/
│   │       ├── routes/
│   │       │   ├── catalog.py        # Video catalog endpoints
│   │       │   ├── ingest.py         # Ingestion pipeline endpoints (Phase 4)
│   │       │   ├── drm.py            # License proxy endpoints (Phase 5)
│   │       │   └── telemetry.py      # QoE event endpoints (Phase 6)
│   │       └── router.py
│   ├── core/
│   │   ├── config.py                 # Environment settings
│   │   └── database.py               # Async PostgreSQL connection
│   ├── models/                       # SQLModel ORM models
│   ├── schemas/                      # Pydantic request/response schemas
│   ├── services/
│   │   ├── catalog_service.py
│   │   ├── ingest_service.py         # FFmpeg + Shaka Packager orchestration
│   │   ├── drm_service.py            # Widevine license proxy
│   │   └── telemetry_service.py
│   └── main.py
├── migrations/                       # Alembic migration files
├── tests/
│   ├── test_catalog.py
│   ├── test_ingest.py
│   └── test_telemetry.py
├── requirements.txt
└── docker-compose.yml                # PostgreSQL local dev setup
```

---

## Cross-Cutting Concerns

### Error Handling

- **Android:** errors are surfaced to the UI via sealed `UiEffect` events — never silently swallowed. Player errors (license failure, network timeout, unsupported format) map to specific user-facing messages defined in `core/player/PlayerError.kt`
- **Backend:** all endpoints return a consistent error response schema `{ "error": { "code": "...", "message": "..." } }`. HTTP status codes are used semantically (400, 404, 422, 500)
- **Ingestion pipeline:** job failures update `IngestJob.status` to `FAILED` with an `error_message`. No partial or corrupt output is ever made available for playback (BR-ING-03)

### Security

- All streams delivered over HTTPS in all environments
- Widevine license requests proxied through the backend — license server credentials never embedded in the Android client (Phase 5)
- No secrets committed to source control — environment variables via `.env` file (gitignored)
- No user data collected or transmitted beyond the developer's own telemetry backend

### Data Integrity

- All Room and PostgreSQL entities include `created_at` and `updated_at` timestamps (UTC)
- Telemetry events are append-only — never updated or deleted
- `IngestJob` status transitions are one-directional: `PENDING → PROCESSING → COMPLETE | FAILED`

### Testing Strategy

| Platform | Unit | Integration | UI |
|---|---|---|---|
| Android (`core`) | JUnit + Turbine (Flow testing) | In-memory Room DB | Deferred |
| Android (`app` / `tv`) | JUnit + ViewModel tests with fake repositories | — | Deferred |
| Backend | pytest | TestClient + test PostgreSQL DB | N/A |

> UI and end-to-end test coverage deferred — learning project priority is functional correctness, not test coverage targets.

---

## Open Technical Decisions

- **Mux free tier limits** — confirm upload and delivery quotas are sufficient for Phase 1 catalog before committing fully to Mux as the primary VOD source
- **Telemetry dashboard** — simple FastAPI-served HTML dashboard vs Grafana for Phase 6 QoE visualization; decision deferred to Phase 6 kickoff
- **Cross-device resume** — whether Phase 6 includes persisting playback position to the backend (enabling phone → Fire TV resume) or keeps position local only; decision deferred to Phase 6 kickoff
- **Offline playback** — whether a future phase extends DRM to include ExoPlayer `DownloadManager` for offline-licensed content; deferred beyond Phase 6

---

## Changelog

| Version | Date | Author | Notes |
|---|---|---|---|
| 0.1.0 | 2026-06-15 | Danielle Mariani | Initial draft |

---

## Related Documents

| Document | Purpose |
|---|---|
| PRODUCT.md | Vision, learning goals, success criteria |
| SPEC.md | Feature index, glossary, business rules, NFRs |
| ROADMAP.md | Phase delivery order and scope |
| CONTEXT.md | Session continuity and current decisions |
| specs/technical/data-model.md | Full Room and PostgreSQL schema |
| specs/technical/api-contract.md | Backend API endpoint definitions |
| specs/technical/content-catalog.md | Test stream sources (Mux, NASA TV) |
| specs/technical/streaming-glossary.md | Extended streaming domain reference |
