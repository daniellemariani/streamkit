# ROADMAP.md — StreamKit

**Version:** 0.1.0
**Status:** Not Started
**Owner:** Danielle Mariani
**Created at:** 2026-06-16
**Last Updated:** 2026-06-16

---

## Phase 1 — Adaptive Bitrate Player (Android)

- [ ] Project setup (monorepo, Gradle modules — `core`, `app`, `tv`, Hilt, Room, DataStore, Ktor, Media3)
- [ ] catalog/ — see specs/features/catalog/tasks.md
- [ ] media-player/ — see specs/features/media-player/tasks.md
- [ ] live-player/ — see specs/features/live-player/tasks.md
- [ ] pip/ — see specs/features/pip/tasks.md
- [ ] settings/ — see specs/features/settings/tasks.md

## Phase 2 — Multi-screen / Chromecast (Android)

- [ ] Cast SDK integration (Google Cast SDK, Default Media Receiver)
- [ ] cast/ — see specs/features/cast/tasks.md

## Phase 3 — Smart TV: Fire TV (Fire TV)

- [ ] Fire TV project setup (`tv` module, Compose for TV, D-pad navigation)
- [ ] fire-tv/ — see specs/features/fire-tv/tasks.md

## Phase 4 — Video Ingestion Pipeline (Backend)

- [ ] Backend project setup (FastAPI, PostgreSQL, Alembic, FFmpeg, Shaka Packager, Docker Compose)
- [ ] ingestion-pipeline/ — see specs/features/ingestion-pipeline/tasks.md

## Phase 5 — DRM & Content Protection (Android + Backend)

- [ ] Widevine L3 integration (test license server — Shaka or Axinom)
- [ ] drm/ — see specs/features/drm/tasks.md

## Phase 6 — Player Telemetry & QoE (Android + Backend)

- [ ] Telemetry backend setup (PostgreSQL schema, event ingestion endpoint)
- [ ] telemetry/ — see specs/features/telemetry/tasks.md

---

## Changelog

| Version | Date | Author | Notes |
|---|---|---|---|
| 0.1.0 | 2026-06-16 | Danielle Mariani | Initial draft |