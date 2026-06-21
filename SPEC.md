# StreamKit

**Version:** 0.1.3
**Status:** Draft
**Owner:** Danielle Mariani
**Created at:** 2026-06-15
**Last Updated:** 2026-06-20

## Overview

StreamKit is a personal, single-user streaming reference application built for learning purposes. It demonstrates end-to-end streaming technology across Android mobile, Fire TV, and a backend pipeline. The app presents a simple video catalog, supports adaptive bitrate playback of VOD and live content, Picture in Picture, Chromecast, and Fire TV — backed by a real video ingestion pipeline, DRM, and player telemetry. StreamKit is not intended for public distribution.

**App name:** StreamKit

---

## Glossary

| Term | Definition |
|---|---|
| **ABR** | Adaptive Bitrate Streaming. A technique where the player dynamically switches between multiple quality renditions of a stream based on available network bandwidth. |
| **HLS** | HTTP Live Streaming. Apple's adaptive streaming protocol using `.m3u8` manifest files and `.ts` or `.fmp4` media segments. Supported natively on Android and Fire TV via ExoPlayer. |
| **DASH** | Dynamic Adaptive Streaming over HTTP. An open standard adaptive streaming protocol using `.mpd` manifest files. Supported on Android and Fire TV via ExoPlayer. |
| **Manifest** | A metadata file (`.m3u8` for HLS, `.mpd` for DASH) that describes the available renditions, segment locations, and timing of a stream. |
| **Rendition** | A single quality variant of a video stream (e.g. 1080p at 5Mbps, 720p at 2.5Mbps). A manifest references multiple renditions; the player picks one at a time. |
| **Segment** | A short chunk of encoded video (typically 2–10 seconds) that the player downloads and plays sequentially. |
| **VOD** | Video on Demand. Pre-recorded video content available for playback at any time, as opposed to a live stream. |
| **Live Stream** | A real-time video broadcast delivered as a continuous stream. StreamKit uses Red Bull TV's 24/7 "Best of Red Bull" stream as its live stream source. |
| **DVR Window** | The buffer of a live stream that can be seeked backwards into. Allows the user to rewind a live broadcast without leaving the live stream. |
| **Live Button** | A UI control that jumps the playback position back to the current real-time position in a live stream, exiting any DVR seek. |
| **Red Bull TV** | A publicly accessible, 24/7 HLS live stream ("Best of Red Bull") delivered via Akamai. Used as StreamKit's live stream source. **Unverified as of 2026-06-20** — manual playback testing of the candidate stream URL did not produce video, possibly due to geo-restriction. See `specs/technical/data-model.md` Open Schema Question #4 before relying on this source in Phase 1. |
| **ExoPlayer / Media3** | Google's open-source Android media playback library. The player engine used in StreamKit's `core` module. |
| **DRM** | Digital Rights Management. Technology used to protect video content from unauthorized access. StreamKit uses Widevine on Android and Fire TV. |
| **Widevine** | Google's DRM system, supported on Android and Fire TV. StreamKit targets Widevine L3 using a public test license server. |
| **License Server** | A backend service that issues DRM license tokens to authenticated players, permitting decryption and playback of protected content. |
| **Mux** | A video infrastructure platform used in StreamKit as the source for catalog metadata and VOD stream delivery. |
| **CDN** | Content Delivery Network. A distributed network of servers that delivers video segments to players with low latency. Mux and Red Bull TV (via Akamai) handle CDN delivery in StreamKit. |
| **FFmpeg** | An open-source command-line tool used in StreamKit's backend pipeline to transcode raw video into multiple renditions. |
| **Shaka Packager** | An open-source tool used to package transcoded video renditions into HLS or DASH format, optionally with DRM encryption. |
| **Ingestion Pipeline** | The backend process that accepts a raw video upload, transcodes it into multiple renditions via FFmpeg, packages it into HLS, and makes it available for playback. |
| **PiP** | Picture in Picture. An Android feature (API 31+) that shrinks the video player into a floating overlay, allowing the user to multitask without stopping playback. |
| **Cast** | The act of sending video playback from the Android phone to an external display device (Chromecast or Fire TV). |
| **Chromecast** | Google's casting protocol and hardware. StreamKit uses the Cast SDK with the Default Media Receiver — no custom receiver app required. |
| **Lean-back UI** | A TV-optimized interface designed for viewing from a distance (approximately 10 feet). Navigated via D-pad rather than touch. |
| **D-pad** | Directional pad. The primary input method on Fire TV remotes. StreamKit's `tv` module is fully navigable via D-pad. |
| **10-foot UI** | A UI design convention for TV apps: large text, high contrast, minimal density, D-pad focus management. |
| **QoE** | Quality of Experience. A set of player metrics — startup time, rebuffer rate, bitrate switches, errors — that measure the perceived quality of a streaming session. |
| **Telemetry** | The collection and transmission of player events and QoE metrics during a playback session. |
| **Startup Time** | The elapsed time between the user pressing play and the first video frame rendering. A key QoE metric. |
| **Rebuffer Rate** | The percentage of playback time spent stalling while the player waits for new segments to download. A key QoE metric. |
| **Core Module** | The `core` Android module containing all shared logic — player setup, Mux integration, DRM, telemetry, and data models. Consumed by both `app` and `tv` modules. |
| **App Module** | The `app` Android module containing the mobile UI (touch, portrait, phone/tablet). Depends on `core`. |
| **TV Module** | The `tv` Android module containing the Fire TV UI (lean-back, D-pad). Depends on `core`. |
| **SDD** | Spec-Driven Development. The methodology used in StreamKit: specs are written before code, and AI tooling (Cursor, Claude) implements against them. |

---

## User Roles

StreamKit is a single-user personal application. There is no authentication, no account creation, and no role-based access control. The user has full access to all features.

---

## Feature Index

### Phase 1 — Adaptive Bitrate Player (Android)

| Feature | Spec | Description | Status |
|---|---|---|---|
| Catalog | specs/features/catalog/requirements.md | Browse a grid/list of VOD and live content sourced from Mux | Not Started |
| Media Player | specs/features/media-player/requirements.md | Play VOD streams with ABR, player controls, and bitrate overlay | Not Started |
| Live Player | specs/features/live-player/requirements.md | Play live streams with DVR seek and Live button | Not Started |
| Picture in Picture | specs/features/pip/requirements.md | Shrink player to floating overlay while multitasking | Not Started |

### Phase 2 — Multi-screen / Chromecast (Android)

| Feature | Spec | Description | Status |
|---|---|---|---|
| Cast | specs/features/cast/requirements.md | Cast VOD and live streams from Android to Chromecast | Not Started |

### Phase 3 — Smart TV: Fire TV Support (Fire TV)

| Feature | Spec | Description | Status |
|---|---|---|---|
| Fire TV | specs/features/fire-tv/requirements.md | Native Fire TV app with lean-back UI and D-pad navigation | Not Started |

### Phase 4 — Video Ingestion Pipeline (Backend)

| Feature | Spec | Description | Status |
|---|---|---|---|
| Ingestion Pipeline | specs/features/ingestion-pipeline/requirements.md | Upload, transcode, package, and serve custom HLS video content | Not Started |

### Phase 5 — DRM & Content Protection (Android + Backend)

| Feature | Spec | Description | Status |
|---|---|---|---|
| DRM | specs/features/drm/requirements.md | Widevine L3 license acquisition and protected stream playback | Not Started |

### Phase 6 — Player Telemetry & QoE (Android + Backend)

| Feature | Spec | Description | Status |
|---|---|---|---|
| Telemetry | specs/features/telemetry/requirements.md | Collect and visualize player QoE metrics from Android and Fire TV | Not Started |

---

## Data Model Summary

| Entity | Key Fields | Notes |
|---|---|---|
| `Video` | id, title, description, type (VOD/LIVE), thumbnail_url, stream_url, duration_seconds, is_drm_protected | Sourced from Mux for VOD; static config for Red Bull TV live stream |
| `PlaybackSession` | id, video_id, device_type (MOBILE/TV), started_at, ended_at, last_position_seconds | Tracks per-session playback state; foundation for cross-device resume in Phase 6 |
| `TelemetryEvent` | id, session_id, event_type, timestamp, bitrate_kbps, resolution, buffer_health_seconds, error_code | One row per player event; aggregated for QoE dashboard in Phase 6 |
| `VideoRendition` | id, video_id, resolution, bitrate_kbps, codec, segment_duration_seconds, manifest_url | Created by the ingestion pipeline in Phase 4; describes each ABR rendition |
| `IngestJob` | id, video_id, status (PENDING/PROCESSING/COMPLETE/FAILED), created_at, updated_at, error_message | Tracks the state of a video through the ingestion pipeline in Phase 4 |

> Full schema lives in specs/technical/data-model.md. This table is a high-level index only.

---

## Business Rules

> Rules are permanent once assigned. IDs follow the pattern BR-[FEAT]-NN.

### Catalog
- **BR-CAT-01:** VOD and live content must be visually distinguishable in the catalog (e.g. a LIVE badge on live entries)
- **BR-CAT-02:** Tapping a catalog item always opens a detail view before playback begins
- **BR-CAT-03:** Catalog content is sourced from the Mux API; the Red Bull TV live stream is a static entry always present in the catalog

### Media Player
- **BR-PLY-01:** Seek forward and seek back controls always move exactly 10 seconds
- **BR-PLY-02:** Seeking before the 10-second mark seeks to position 0:00, not a negative timestamp
- **BR-PLY-03:** A real-time overlay must display current bitrate (kbps), resolution, and buffer health during playback
- **BR-PLY-04:** The player must begin bitrate adaptation within 5 seconds of a simulated network degradation
- **BR-PLY-05:** Playback must resume from the same position after an interruption (e.g. incoming call, PiP exit)
- **BR-PLY-06:** The player must support free seeking via a progress bar — the user can drag to any position within the playable range of the stream

### Live Player
- **BR-LIV-01:** The Live button is only visible when the playback position is behind the live edge
- **BR-LIV-02:** Tapping the Live button always seeks to the current live edge immediately
- **BR-LIV-03:** DVR seek must not seek beyond the available DVR window boundary

### Picture in Picture
- **BR-PIP-01:** PiP activates automatically when the user navigates away from the player screen (auto-enter PiP, Android 12+)
- **BR-PIP-02:** Playback must continue without interruption when entering or exiting PiP
- **BR-PIP-03:** PiP overlay must include play/pause control at minimum

### Cast
- **BR-CST-01:** A cast icon is only visible in the player when a Chromecast device is available on the local network
- **BR-CST-02:** Casting begins from the current playback position — the stream does not restart from the beginning
- **BR-CST-03:** The phone acts as a remote control during a cast session (play, pause, seek)
- **BR-CST-04:** Ending a cast session returns playback to the phone at the current position

### DRM
- **BR-DRM-01:** DRM-protected streams must not be playable without a valid Widevine license
- **BR-DRM-02:** License acquisition must happen transparently before playback begins — the user must not see a license request step
- **BR-DRM-03:** A clear error message must be shown if license acquisition fails

### Ingestion Pipeline
- **BR-ING-01:** Every uploaded video must be transcoded into a minimum of three renditions (e.g. 1080p, 720p, 480p)
- **BR-ING-02:** The pipeline must produce a valid HLS manifest (`.m3u8`) playable by ExoPlayer
- **BR-ING-03:** A video is not available for playback until its `IngestJob` status is COMPLETE

### Telemetry
- **BR-TEL-01:** Telemetry events must be collected from both `app` (mobile) and `tv` (Fire TV) modules via shared `core` logic
- **BR-TEL-02:** Event collection must not degrade playback performance
- **BR-TEL-03:** The QoE dashboard must display at minimum: startup time, rebuffer rate, and bitrate switch count per session

---

## Global Non-Functional Requirements

### Performance
- **NFR-PE-01:** Video playback must begin (first frame rendered) within 3 seconds of tapping play on a stable network connection
- **NFR-PE-02:** The catalog screen must render within 2 seconds of app launch
- **NFR-PE-03:** Bitrate adaptation must not cause a visible stall — the player must pre-buffer the new rendition before switching

### Reliability
- **NFR-RE-01:** A network interruption during playback must trigger automatic retry — the player must not display an error unless retry fails after 3 attempts
- **NFR-RE-02:** The ingestion pipeline must report job failure clearly and leave no partial/corrupt output available for playback

### Compatibility
- **NFR-CO-01:** Android `app` module minimum SDK is API 31 (Android 12)
- **NFR-CO-02:** Fire TV `tv` module must support Fire TV Stick 4K (2nd gen) or newer
- **NFR-CO-03:** All streams must be delivered over HTTPS

### Offline
> Offline support is deferred. No phase currently requires offline playback. Offline download via ExoPlayer DownloadManager is a candidate for a future DRM phase extension.

### Accessibility
> Not required for StreamKit. Personal learning project with a single known user.

### Data Safety
> Not applicable. No user data is collected, stored, or transmitted beyond local playback state and anonymous telemetry events within the developer's own backend.

---

## Out of Scope

- No user authentication, accounts, or login flows
- No subscriptions, payments, or monetization
- No Google Play or Amazon Appstore distribution
- No iOS, Apple TV, tvOS support
- No Roku, LG webOS, or Samsung Tizen support (potential future phases beyond Phase 6)
- No content recommendations or personalization engine
- No social features (watchlists, ratings, comments, sharing)
- No localization or multi-language support
- No analytics data collection beyond the developer's own telemetry backend
- No production CDN setup — Mux handles delivery for VOD; Red Bull TV (via Akamai) for live

---

## Changelog

| Version | Date | Author | Notes |
|---|---|---|---|
| 0.1.0 | 2026-06-15 | Danielle Mariani | Initial draft |
| 0.1.1 | 2026-06-16 | Danielle Mariani | Added BR-PLY-06 — progress bar free seeking |
| 0.1.2 | 2026-06-20 | Danielle Mariani | Replaced NASA TV with Red Bull TV's 24/7 "Best of Red Bull" stream as the live content source — NASA's 24/7 NTV1-HLS channel was discontinued in 2024 and its public stream URLs no longer resolve |
| 0.1.3 | 2026-06-20 | Danielle Mariani | Flagged Red Bull TV stream as unverified pending manual playback test — see data-model.md Open Schema Question #4 |

---

## Related Documents

| Document | Purpose |
|---|---|
| PRODUCT.md | Vision, learning goals, success criteria |
| ARCHITECTURE.md | Technical decisions, module structure, stack |
| ROADMAP.md | Phase overview and delivery order |
| CONTEXT.md | Session continuity and current decisions |
| specs/technical/data-model.md | Full entity schema |
| specs/technical/api-contract.md | Backend API endpoint definitions |
| specs/technical/streaming-glossary.md | Extended streaming domain reference |
| specs/technical/content-catalog.md | Test stream sources and metadata |
| specs/design/design.md | UI guidelines for mobile and TV |
| specs/design/navigation.md | App navigation flows |