# Product - StreamKit

**Version:** 0.1.0
**Status:** Draft
**Owner:** Danielle Mariani
**Created at:** 2026-06-15
**Last Updated:** 2026-06-15

---

## Problem Statement

Streaming technology is a core competency at every major media platform — Disney+, ESPN, Hulu, Netflix, Peacock — yet it is difficult to develop hands-on expertise without working inside one of those companies. Public resources cover theory well, but there is no single reference project that walks through the full streaming stack end-to-end: adaptive playback, video packaging, DRM, multi-screen experiences, Smart TV, and telemetry.

StreamKit exists to fill that gap. It is a personal, self-directed learning project designed to build deep, demonstrable expertise in streaming technologies across Android, Fire TV, and a backend pipeline — through actually building a working product, not just reading about it.

---

## Target User

StreamKit is a single-user personal project. There is no intended public distribution.

- **Who:** A senior Android engineer with 15 years of experience, returning to hands-on development after 5 years in management roles
- **Goal:** Upskill in streaming domain technologies and build a portfolio project that demonstrates competency to hiring teams at streaming-focused companies
- **Technical level:** Advanced Android (Kotlin, Jetpack), learning streaming protocols (HLS, DASH, DRM, ABR), new to FastAPI backend development
- **Devices:** Pixel 9 Pro (primary development and testing), Fire TV device (TV testing)

---

## Core User Workflow

1. **Launch:**
   - Open StreamKit on Android phone or Fire TV
   - Land on the catalog screen — a grid or list of available videos

2. **Browse:**
   - Scroll through catalog (VOD and live content clearly distinguished)
   - Select a video to open its detail view

3. **Watch (VOD):**
   - Tap play to start video
   - Control playback: pause, seek forward 10s, seek back 10s
   - Optionally enter Picture in Picture to continue watching while using other apps

4. **Watch (Live):**
   - Tap play on a live stream (e.g. NASA TV)
   - Seek back in the DVR window
   - Tap Live button to jump back to the current live position

5. **Cast:**
   - Tap cast icon while watching
   - Select Chromecast or Fire TV as target device
   - Video continues on TV; phone acts as remote

---

## Goals

### Product Goals
- Play VOD and live video content using real adaptive bitrate streaming (HLS/DASH)
- Support Picture in Picture so the user can multitask without interrupting playback
- Cast video to a TV (Chromecast and Fire TV)
- Run natively on Fire TV with a D-pad-optimized lean-back UI
- Package and serve custom video content through a real backend pipeline
- Protect content with DRM (Widevine)
- Collect and visualize player telemetry and QoE metrics

### Project Goals
- Build hands-on expertise across the full streaming technology stack
- Produce a portfolio project that demonstrates streaming competency to hiring teams at Disney+, ESPN, Hulu, Netflix, and similar companies
- Practice Spec-Driven Development (SDD) with AI-assisted tooling (Cursor, Claude)
- Establish reusable Android architecture patterns (modular: `core`, `app`, `tv`) applicable to professional streaming projects

---

## Non-Goals

> These items are explicitly out of scope. Writing them down prevents scope creep.

- No user accounts, authentication, or login
- No subscriptions or payments
- No monetization of any kind
- No Google Play or Amazon Appstore distribution (personal use only)
- No iOS or Apple TV support
- No multi-user or social features (watchlists, ratings, comments)
- No content licensing or rights management beyond Widevine test credentials
- No recommendation engine or personalization
- No localization or multi-language support
- Roku, LG webOS, and Samsung Tizen deferred — potential future phases beyond Phase 6

---

## Success Criteria

### Phase 1 — Adaptive Bitrate Player
- User can browse a catalog of videos sourced from the Mux API
- User can play a VOD stream with pause, seek forward 10s, and seek back 10s controls
- User can play a live stream (NASA TV) with DVR seek and a Live button
- A real-time overlay shows current bitrate, resolution, and buffer health during playback
- Simulating poor network conditions triggers a visible bitrate downgrade within 5 seconds
- User can enter and exit Picture in Picture without interrupting playback

### Phase 2 — Chromecast
- User can cast a VOD stream from the Android app to a Chromecast device
- Playback continues uninterrupted on the TV when the phone screen is locked

### Phase 3 — Fire TV
- StreamKit runs natively on Fire TV with full D-pad navigation
- All Phase 1 playback features work on Fire TV without modification to `core`

### Phase 4 — Video Ingestion Pipeline
- A raw video file can be uploaded, transcoded into multiple renditions via FFmpeg, packaged into HLS, and served for playback in the Android app

### Phase 5 — DRM
- A Widevine-protected stream plays successfully on Android using a test license server
- Unprotected playback on Fire TV continues to work alongside DRM-protected content on mobile

### Phase 6 — Telemetry & QoE
- Player events (startup time, rebuffer rate, bitrate switches) are collected and visible in a simple dashboard
- Events from Android and Fire TV both feed into the same backend

---

## Core Principles

> These are decision-making filters. When trade-offs arise, these resolve them.

- **Learning over polish:** UI quality is secondary to streaming functionality. A working ABR player with a plain UI is better than a beautiful app with fake playback.
- **Real over mocked:** Every phase should use real protocols, real APIs, and real devices — not simulated or stubbed implementations.
- **Depth over breadth:** It is better to fully understand one streaming concept than to superficially touch many. Each phase should be completable before moving to the next.
- **Spec first:** No code is written without a spec. Cursor and Claude are used to implement against specs, not to generate specs from code.
- **Modularity by default:** `core` logic is never coupled to `app` or `tv`. Every decision that could be shared should live in `core`.

---

## Assumptions

- Primary development and testing device is a Pixel 9 Pro running Android 15
- Minimum supported Android version is API 31 (Android 12) — required for modern PiP behavior
- Mux free tier is sufficient for Phase 1 catalog and video delivery needs
- NASA TV provides a stable, publicly accessible HLS live stream suitable for development
- Widevine L3 test credentials from a public test server (e.g. Shaka, Axinom) are sufficient for Phase 5 DRM implementation
- Backend runs locally during development; cloud deployment (AWS) is optional and deferred
- English only — no localization required
- A Chromecast device and a Fire TV device are available for physical testing

---

## Open Questions

> Remove items once decided and document the outcome in the changelog.

- **Mux free tier limits:** Confirm whether Mux's free tier covers catalog management, video upload, and delivery volume sufficient for Phase 1 before committing fully to Mux as the catalog source
- **Offline playback:** Should Phase 5 (DRM) include offline download and playback, or is streaming-only sufficient for the learning goal?
- **Telemetry dashboard:** Should the Phase 6 dashboard be a simple web UI (FastAPI + HTML) or a more capable tool (e.g. Grafana)? Decision affects backend complexity.
- **Cross-device resume:** Should Phase 6 include resuming playback position across devices (phone → Fire TV)? Requires backend state persistence.

---

## Changelog

| Version | Date | Author | Notes |
|---|---|---|---|
| 0.1.0 | 2026-06-15 | Danielle Mariani | Initial draft |
