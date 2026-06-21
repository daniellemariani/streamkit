# CONTEXT.md — StreamKit

**Version:** 0.1.1
**Status:** Active
**Owner:** Danielle Mariani
**Created at:** 2026-06-16
**Last Updated:** 2026-06-20

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
| VOD Catalog | Mux API | Dynamic catalog; free tier assumed sufficient for Phase 1; no test assets selected yet |
| Live Stream | Red Bull TV ("Best of Red Bull", HLS) | Replaces NASA TV (discontinued 2024). **⚠️ Candidate URL unverified** — see Open Questions #5 |
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
| `SPEC.md` | ✅ Complete | v0.1.3 — BR-PLY-06 added; NASA TV replaced with Red Bull TV (unverified, see Open Questions) |
| `ARCHITECTURE.md` | ✅ Complete | v0.1.0 |
| `ROADMAP.md` | ✅ Complete | v0.1.0 |
| `CONTEXT.md` | ✅ Complete | v0.1.1 |
| `specs/technical/data-model.md` | ✅ Complete | v0.1.3 — full Room + PostgreSQL schema; cross-device resume resolved (video-scoped); Red Bull TV stream URL flagged unverified |
| `specs/technical/api-contract.md` | ✅ Complete | v0.1.0 — covers Phase 4 (ingestion), Phase 5 (DRM), Phase 6 (telemetry); no auth/tenancy, no sync API (not applicable to StreamKit) |
| `specs/technical/content-catalog.md` | ✅ Complete | v0.1.0 — Mux documented generically (no assets selected yet); Red Bull TV carries forward unverified status from data-model.md |

---

## Pending Spec Documents

| Document | Status | Notes |
|---|---|---|
| `specs/technical/streaming-glossary.md` | 🔜 Next | Extended streaming domain reference |
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
| 4 | ~~Cross-device resume — should Phase 6 persist playback position to backend (phone → Fire TV)?~~ | **Resolved** — yes, Phase 6 persists position to backend; resume is video-scoped (not device-scoped), enabling phone ↔ TV resume. See `data-model.md` `PlaybackSession` notes |
| 5 | **Red Bull TV live stream URL — does it actually play?** Manual playback testing of the candidate URL (`rbmn-live.akamaized.net/.../master.m3u8`) produced no video, despite a valid HLS content type and structure. Suspected cause: geo-restriction. | **Open — blocking Phase 1 live-source implementation.** See `data-model.md` Open Schema Question #4 and `content-catalog.md` for full detail and fallback options |
| 6 | Local network exposure for Fire TV hardware testing — `api-contract.md` notes the backend is `localhost`-only, but Fire TV is a separate physical device needing LAN access in Phase 5/6 | Open — needs a decision before Phase 5 hardware testing |

---

## Data Model Summary (High Level)

Full schema is defined in `specs/technical/data-model.md` (complete as of v0.1.3). This table is retained here only as a quick-reference index — `data-model.md` is authoritative for field-level detail.

| Entity | Platform | Phase |
|---|---|---|
| `Video` | Android (Room) + Backend (PostgreSQL) | 1 (Android) / 4 (Backend) |
| `PlaybackSession` | Android (Room, position-only) + Backend (PostgreSQL, full session) | 1 (Android) / 6 (Backend) |
| `VideoRendition` | Backend (PostgreSQL) only | 4 |
| `IngestJob` | Backend (PostgreSQL) only | 4 |
| `TelemetryEvent` | Android (Room buffer) + Backend (PostgreSQL) | 6 |

---

## Next Step

**Draft `specs/technical/streaming-glossary.md`**

The remaining technical spec document. Extends `SPEC.md`'s existing glossary with deeper streaming-domain reference material — likely covering ABR algorithm behavior, manifest structure detail (HLS tags, DASH periods/adaptation sets), codec/container specifics, and DRM concepts beyond what `SPEC.md`'s top-level glossary needs for business-rule context.

**Before moving past the technical specs into feature specs, two blocking items need resolution:**
1. **Red Bull TV stream verification** (Open Question #5) — manually confirm the candidate live stream URL actually plays before any Android implementation depends on it. See `data-model.md` Open Schema Question #4 and `content-catalog.md` for full detail.
2. **Mux test asset selection** (Open Question, `content-catalog.md`) — pick and upload actual VOD test content; `content-catalog.md`'s Mux section is currently generic/placeholder.

Neither blocks drafting `streaming-glossary.md` itself, but both should be resolved before Phase 1 implementation begins.

Start a new chat session and reference this file (`CONTEXT.md`) plus `SPEC.md` and `ARCHITECTURE.md` for full context.

---

## Changelog

| Version | Date | Author | Notes |
|---|---|---|---|
| 0.1.0 | 2026-06-16 | Danielle Mariani | Initial draft — root specs complete, data-model next |
| 0.1.1 | 2026-06-20 | Danielle Mariani | data-model.md, api-contract.md, and content-catalog.md completed and moved to Completed Spec Documents; NASA TV replaced with Red Bull TV throughout (flagged unverified); cross-device resume open question resolved; added open questions on Red Bull TV verification and Fire TV LAN exposure; Next Step updated to streaming-glossary.md |