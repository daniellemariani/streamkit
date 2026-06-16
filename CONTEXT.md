# CONTEXT.md — StreamKit

**Version:** 0.1.0
**Status:** Active
**Owner:** Danielle Mariani
**Created at:** 2026-06-16
**Last Updated:** 2026-06-16

---

## Purpose

This file tracks session continuity for StreamKit. It summarizes key decisions, agreements, and current state so any new AI session (Cursor, Claude) can pick up exactly where the last one left off without re-litigating prior decisions.

---

## Project Summary

StreamKit is a personal, single-user streaming reference application built for learning purposes. It demonstrates end-to-end streaming technology across Android mobile, Fire TV, and a backend pipeline. It is not intended for public distribution.

The goal is to upskill in streaming domain technologies and build a portfolio project that demonstrates competency to hiring teams at streaming-focused companies (Disney+, ESPN, Hulu, Netflix).

Full details: `PRODUCT.md`

---

## Repository Structure

```
streamkit/
├── android/
│   ├── core/          # Shared — player, DRM, networking, data models
│   ├── app/           # Mobile UI (Jetpack Compose, touch)
│   └── tv/            # Fire TV UI (Compose for TV, D-pad)
├── backend/           # FastAPI + PostgreSQL
├── specs/
│   ├── features/
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
├── PRODUCT.md
├── SPEC.md
├── ARCHITECTURE.md
├── ROADMAP.md
└── CONTEXT.md
```

---

## Phase Plan

| Phase | Name | Platform | Status |
|---|---|---|---|
| 1 | Adaptive Bitrate Player | Android (`app`) | Not Started |
| 2 | Multi-screen / Chromecast | Android (`app`) | Not Started |
| 3 | Smart TV: Fire TV | Android (`tv`) | Not Started |
| 4 | Video Ingestion Pipeline | Backend | Not Started |
| 5 | DRM & Content Protection | Android + Backend | Not Started |
| 6 | Player Telemetry & QoE | Android + Backend | Not Started |

---

## Key Technical Decisions

### Android

| Decision | Choice | Rationale |
|---|---|---|
| Language | Kotlin | — |
| UI — Mobile | Jetpack Compose | Modern Android UI standard |
| UI — Fire TV | Compose for TV (`androidx.tv`) | Same language, D-pad optimized |
| Architecture | MVI + Repository | Player state is complex and continuous — MVI prevents state divergence |
| Dependency Injection | Hilt | — |
| Player Engine | ExoPlayer / Media3 | Google's standard Android media library |
| Networking | Ktor Client | Small API surface; good Kotlin-native fit; KMP-ready |
| Local Database | Room | Catalog cache, playback positions, telemetry buffer |
| Preferences | DataStore | Environment setting (Mux / Local) |
| Async | Coroutines + Flow | — |
| Build | Gradle (Kotlin DSL) | — |
| Min SDK | API 31 (Android 12) | Required for modern auto-enter PiP behavior |
| Module structure | `core`, `app`, `tv` | Single Gradle project; `app` and `tv` depend on `core`; `core` has no UI |

### Backend

| Decision | Choice | Rationale |
|---|---|---|
| Language | Python | — |
| Framework | FastAPI | Consistent with Capital (Budget App) |
| ORM | SQLModel | — |
| Database | PostgreSQL | — |
| Migrations | Alembic | — |
| Video Transcoding | FFmpeg | Industry standard |
| Video Packaging | Shaka Packager | HLS/DASH packaging with optional DRM |
| Runtime | Local only | No cloud deployment planned |

### Content Sources

| Content Type | Source | Notes |
|---|---|---|
| VOD Catalog | Mux API | Dynamic catalog; free tier assumed sufficient for Phase 1 |
| Live Stream | NASA TV (HLS) | Stable public stream, no auth required |
| Custom VOD (Phase 4+) | Local backend | Packaged via FFmpeg + Shaka Packager |
| DRM Test Streams (Phase 5) | Shaka or Axinom test server | Widevine L3 test credentials |

### Environment Switching

The Settings screen allows switching between two environments:

| Environment | Catalog Source | Stream Source |
|---|---|---|
| Mux | Mux API | Mux CDN |
| Local | Local backend API | Locally served HLS (Phase 4+) |

Persisted in DataStore. Injected at the `NetworkModule` level.

---

## Completed Spec Documents

| Document | Status | Notes |
|---|---|---|
| `PRODUCT.md` | ✅ Complete | v0.1.0 |
| `SPEC.md` | ✅ Complete | v0.1.1 — BR-PLY-06 added (progress bar seeking) |
| `ARCHITECTURE.md` | ✅ Complete | v0.1.0 |
| `ROADMAP.md` | ✅ Complete | v0.1.0 |
| `CONTEXT.md` | ✅ Complete | v0.1.0 |

---

## Pending Spec Documents

| Document | Status | Notes |
|---|---|---|
| `specs/technical/data-model.md` | 🔜 Next | Full Room + PostgreSQL schema |
| `specs/technical/api-contract.md` | Not Started | Backend API endpoint definitions |
| `specs/technical/streaming-glossary.md` | Not Started | Extended streaming domain reference |
| `specs/technical/content-catalog.md` | Not Started | Test stream sources and metadata |
| `specs/design/design.md` | Not Started | UI guidelines for mobile and TV |
| `specs/design/navigation.md` | Not Started | App navigation flows |
| `specs/features/catalog/` | Not Started | requirements.md, design.md, tasks.md |
| `specs/features/media-player/` | Not Started | requirements.md, design.md, tasks.md |
| `specs/features/live-player/` | Not Started | requirements.md, design.md, tasks.md |
| `specs/features/pip/` | Not Started | requirements.md, design.md, tasks.md |
| `specs/features/cast/` | Not Started | requirements.md, design.md, tasks.md |
| `specs/features/fire-tv/` | Not Started | requirements.md, design.md, tasks.md |
| `specs/features/ingestion-pipeline/` | Not Started | requirements.md, design.md, tasks.md |
| `specs/features/drm/` | Not Started | requirements.md, design.md, tasks.md |
| `specs/features/telemetry/` | Not Started | requirements.md, design.md, tasks.md |

---

## Open Questions

| # | Question | Status |
|---|---|---|
| 1 | Mux free tier limits — confirm upload and delivery quotas sufficient for Phase 1 | Open |
| 2 | Offline playback — should Phase 5 include ExoPlayer DownloadManager for offline DRM content? | Open |
| 3 | Telemetry dashboard — simple FastAPI-served HTML vs Grafana for Phase 6 QoE visualization | Open |
| 4 | Cross-device resume — should Phase 6 persist playback position to backend (phone → Fire TV)? | Open |

---

## Data Model Summary (High Level)

Defined in `SPEC.md`. Full schema to be drafted in `specs/technical/data-model.md`.

| Entity | Platform | Phase |
|---|---|---|
| `Video` | Android (Room) + Backend (PostgreSQL) | 1 |
| `PlaybackSession` | Android (Room) + Backend (PostgreSQL) | 1 |
| `VideoRendition` | Backend (PostgreSQL) | 4 |
| `IngestJob` | Backend (PostgreSQL) | 4 |
| `TelemetryEvent` | Android (Room buffer) + Backend (PostgreSQL) | 6 |

---

## Next Step

**Draft `specs/technical/data-model.md`**

Define the full schema for all entities across both Android (Room) and Backend (PostgreSQL):
- `Video` — catalog entry, VOD and live
- `PlaybackSession` — per-session playback state and position tracking
- `VideoRendition` — ABR renditions produced by the ingestion pipeline
- `IngestJob` — ingestion pipeline job state machine
- `TelemetryEvent` — player QoE events buffered locally and flushed to backend

Start a new chat session and reference this file (`CONTEXT.md`) plus `SPEC.md` and `ARCHITECTURE.md` for full context.

---

## Changelog

| Version | Date | Author | Notes |
|---|---|---|---|
| 0.1.0 | 2026-06-16 | Danielle Mariani | Initial draft — root specs complete, data-model next |