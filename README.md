# StreamKit

A personal, mobile-first streaming reference implementation built to develop hands-on expertise across the full streaming technology stack — adaptive bitrate playback, video ingestion, DRM, multi-screen experiences, Smart TV, and player telemetry.

> This is a learning project. It is not intended for public distribution.

---

## Goals

- Build a working streaming application using real protocols (HLS, DASH), real DRM (Widevine), and real devices (Android, Fire TV, Chromecast)
- Develop portfolio-ready expertise in the streaming domain (Disney+, ESPN, Hulu, Netflix)
- Practice Spec-Driven Development (SDD) with AI-assisted tooling (Cursor, Claude)

---

## Platforms

| Platform | Technology | Status |
|---|---|---|
| Android (mobile) | Kotlin, Jetpack Compose, Media3 | Phase 1 |
| Android (Fire TV) | Kotlin, Compose for TV, Media3 | Phase 3 |
| Backend | Python, FastAPI, PostgreSQL | Phase 4 |

---

## Phases

| Phase | Name | Platform |
|---|---|---|
| 1 | Adaptive Bitrate Player | Android |
| 2 | Multi-screen / Chromecast | Android |
| 3 | Smart TV: Fire TV | Fire TV |
| 4 | Video Ingestion Pipeline | Backend |
| 5 | DRM & Content Protection | Android + Backend |
| 6 | Player Telemetry & QoE | Android + Backend |

---

## Features

- Browse a video catalog (VOD and live) sourced from the Mux API
- Play adaptive bitrate streams (HLS/DASH) with real-time bitrate and buffer overlay
- Play live streams (NASA TV) with DVR seek and Live button
- Picture in Picture — auto-enter on navigation, continues playback uninterrupted
- Cast to Chromecast — starts from current position, phone acts as remote
- Native Fire TV app with lean-back UI and full D-pad navigation
- Video ingestion pipeline — upload, transcode (FFmpeg), package (Shaka Packager), serve as HLS
- Widevine L3 DRM — license acquisition, protected stream playback
- Player telemetry — startup time, rebuffer rate, bitrate switches, QoE dashboard

---

## Architecture

StreamKit is a monorepo with three top-level areas:

```
streamkit/
├── android/        # Single Gradle project — core, app, tv modules
├── backend/        # FastAPI + PostgreSQL
└── specs/          # All spec documents
```

### Android Modules

```
android/
├── core/           # Shared — player, DRM, networking, data models
├── app/            # Mobile UI (Jetpack Compose, touch)
└── tv/             # Fire TV UI (Compose for TV, D-pad)
```

`app` and `tv` depend on `core`. `core` has no UI and no knowledge of either client.

### Key Technology Choices

| Concern | Choice |
|---|---|
| Player engine | ExoPlayer / Media3 |
| Architecture pattern | MVI + Repository |
| Dependency injection | Hilt |
| Networking | Ktor Client |
| Local database | Room |
| Preferences | DataStore |
| Video transcoding | FFmpeg |
| Video packaging | Shaka Packager |
| Backend framework | FastAPI |
| Database | PostgreSQL |

Full architecture details: [`ARCHITECTURE.md`](ARCHITECTURE.md)

---

## Content Sources

| Type | Source |
|---|---|
| VOD catalog | Mux API |
| Live stream | NASA TV (public HLS) |
| Custom VOD (Phase 4+) | Local backend pipeline |
| DRM test streams (Phase 5) | Shaka / Axinom test server |

---

## Spec Documents

This project follows Spec-Driven Development (SDD). All decisions are specced before implementation.

| Document | Purpose |
|---|---|
| [`PRODUCT.md`](PRODUCT.md) | Why this project exists, goals, success criteria |
| [`SPEC.md`](SPEC.md) | Feature index, glossary, business rules, NFRs |
| [`ARCHITECTURE.md`](ARCHITECTURE.md) | Technical decisions, module structure, stack |
| [`ROADMAP.md`](ROADMAP.md) | Phase delivery order and scope |
| [`CONTEXT.md`](CONTEXT.md) | Session continuity — current state and next steps |

---

## Requirements

### Android
- Android Studio Hedgehog or later
- Min SDK: API 31 (Android 12)
- Kotlin 1.9+

### Backend
- Python 3.11+
- PostgreSQL 15+
- FFmpeg
- Shaka Packager
- Docker (for local PostgreSQL via Docker Compose)

---

## Status

🚧 In spec — implementation not yet started.