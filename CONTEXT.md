# CONTEXT.md — StreamKit

**Version:** 0.1.9
**Status:** Active
**Owner:** Danielle Mariani
**Created at:** 2026-06-16
**Last Updated:** 2026-06-26

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
| Live Streams | Red Bull TV ("Best of Red Bull"), DW English, NHK World-Japan (3-item carousel, HLS) | All three unverified pending manual playback test. DW English and NHK World-Japan are newly proposed (2026-06-26) and also pending Dani's confirmation — see Open Question #7 and `content-catalog.md` |
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
| `SPEC.md` | ✅ Complete | v0.1.6 — applied `navigation.md` decisions: BR-CAT-02 scoped to VOD, BR-LIV-04/05 and BR-PLY-07–11 added, BR-PIP-01 reworded, Settings added to Feature Index. BR-PLY-03 overlay default flipped from off to on. BR-CAT-03 plural rewording still deferred (Open Question #5/#7) |
| `ARCHITECTURE.md` | ✅ Complete | v0.1.2 — added Screen Orientation subsection (per-destination `requestedOrientation` handling); orientation note from Pending Document Updates is now applied |
| `ROADMAP.md` | ✅ Complete | v0.1.0 |
| `CONTEXT.md` | ✅ Complete | v0.1.9 |
| `specs/technical/data-model.md` | ✅ Complete | v0.1.4 — generalized `Video.id` and related notes to three static live keys (`redbull_tv`, `dw_english`, `nhk_world`); added Open Schema Question #5 for the two new candidates |
| `specs/technical/api-contract.md` | ✅ Complete | v0.1.0 — covers Phase 4 (ingestion), Phase 5 (DRM), Phase 6 (telemetry); no auth/tenancy, no sync API (not applicable to StreamKit) |
| `specs/technical/content-catalog.md` | ✅ Complete | v0.1.1 — added Live Source 2 (DW English) and Live Source 3 (NHK World-Japan) as proposed candidates, both unverified and pending Dani's confirmation |
| `specs/design/navigation.md` | ✅ Complete | v0.1.3 — Catalog, Live Player, Player, and Settings screen flows, route inventory, global nav patterns. Bitrate/resolution/buffer overlay default on; that document's own internal Open Question #1 (live carousel sources) updated to reflect proposed candidates — see this file's Open Questions #5/#7 for the consolidated tracking |

---

## Pending Spec Documents

| Document | Status | Notes |
|---|---|---|
| `specs/design/design.md` | 🔜 Next | UI guidelines for mobile and TV. Should follow the decisions already locked in `navigation.md` (autoplay vs tap-to-play, maximize/minimize behavior, etc.) |
| `specs/features/catalog/` | Not Started | requirements.md, design.md, tasks.md |
| `specs/features/media-player/` | Not Started | requirements.md, design.md, tasks.md |
| `specs/features/live-player/` | Not Started | requirements.md, design.md, tasks.md |
| `specs/features/pip/` | Not Started | requirements.md, design.md, tasks.md |
| `specs/features/cast/` | Not Started | requirements.md, design.md, tasks.md |
| `specs/features/fire-tv/` | Not Started | requirements.md, design.md, tasks.md |
| `specs/features/ingestion-pipeline/` | Not Started | requirements.md, design.md, tasks.md |
| `specs/features/drm/` | Not Started | requirements.md, design.md, tasks.md |
| `specs/features/telemetry/` | Not Started | requirements.md, design.md, tasks.md |
| `specs/features/settings/` | Not Started | requirements.md, design.md, tasks.md — now listed in `SPEC.md`'s Phase 1 Feature Index as of v0.1.5 |

> `specs/technical/streaming-glossary.md` was dropped from scope on 2026-06-20 — `SPEC.md`'s existing glossary (35+ terms) already covers what's needed for business-rule context, and deeper protocol-level detail (HLS tag structure, DASH hierarchy, codec specifics) is better looked up live from primary sources (Apple's HLS spec, ExoPlayer docs) during implementation than pre-written speculatively now.

---

## Pending Document Updates (triggered by `navigation.md`)

These are changes `navigation.md`'s decisions imply for other documents. Tracked here rather than duplicated inside `navigation.md` itself.

### `SPEC.md` — ✅ Applied 2026-06-26 (v0.1.6)

- ~~**BR-CAT-02** — reword to scope explicitly to VOD~~ — done; Live is now a named exception via new `BR-LIV-04`.
- ~~**New BR-LIV-04**~~ — added.
- ~~**New BR-PLY-07**~~ — added (long-press 1.5x speed).
- ~~**BR-PIP-01**~~ — reworded; auto-enter PiP only while actively playing.
- ~~**New rule(s) for fullscreen/orientation behavior**~~ — added as `BR-PLY-08`–`BR-PLY-11` (Media Player), cross-referenced from `BR-LIV-05` (Live Player) rather than duplicated — flagging this structural choice in case you'd rather have the rules fully duplicated under Live Player instead of cross-referenced.
- ~~**Feature Index**~~ — Settings row added to the Phase 1 table.
- **Found during the edit, not originally listed here:** `BR-PLY-03` said the bitrate/resolution/buffer overlay "must display" during playback — unconditional. That conflicted with the Settings toggle introduced by `navigation.md`. Reworded to a toggle; default flipped from off to on on 2026-06-26 (Dani's call — diagnostics should be visible by default for this learning project). `PRODUCT.md`'s Phase 1 Success Criteria has matching unconditional-overlay phrasing and needs no change, since it already assumed the overlay is shown.
- **Still deferred:** `BR-CAT-03` plural rewording — waiting on the two additional live sources (Open Question #5/#7).

### `ARCHITECTURE.md` — ✅ Applied 2026-06-26 (v0.1.2)

- ~~Note on orientation handling~~ — added as a new "Screen Orientation" subsection: per-destination `requestedOrientation` (portrait-locked Catalog/Settings vs. sensor-driven Player/Live Player), with the toggle and physical rotation treated as two inputs into one state rather than competing ones.
- **Found during the edit, not originally listed:** the Overview's module description called `app` "portrait" as a blanket descriptor, which is now inaccurate given landscape support on the player screens — reworded. Also corrected a stale "NASA TV" reference in Related Documents to Red Bull TV (leftover from before the live-source swap).

**All `navigation.md`-triggered document updates are now applied.** Remaining open items are content, not structure — see Open Questions #5/#7.

### `data-model.md` / `content-catalog.md` — ✅ Drafted 2026-06-26, pending confirmation

- ~~No schema change needed~~ — confirmed; `videos` already supported multiple `LIVE`/`STATIC` rows, so adding two more static entries required no migration.
- Added DW English and NHK World-Japan as proposed Live Source 2/3 in `content-catalog.md` (v0.1.1), generalized the singular `redbull_tv` references in `data-model.md` (v0.1.4) to all three, and added Open Schema Question #5 for their (unverified) playback status.
- **Not yet done:** `SPEC.md`'s `BR-CAT-03` still says singular and is intentionally left untouched — flipping it to plural means treating these two candidates as finalized, which is Dani's call, not mine to make unilaterally. Once confirmed, that's a one-line edit.
- **Still open regardless of confirmation:** none of the three live URLs (including the original Red Bull TV one) have actually been manually playback-tested yet.

### `api-contract.md`

- No changes needed for Phase 1 — the new Settings diagnostics (bytes downloaded, network type, dropped frames) are local ExoPlayer/Media3 analytics with no backend dependency yet. Worth revisiting at Phase 6 kickoff if any get promoted into `TelemetryEventType`.

---

## Open Questions

| # | Question | Status |
|---|---|---|
| 1 | Mux free tier limits — confirm upload and delivery quotas sufficient for Phase 1 | Open |
| 2 | Offline playback — should Phase 5 include ExoPlayer DownloadManager for offline DRM content? | Open |
| 3 | Telemetry dashboard — simple FastAPI-served HTML vs Grafana for Phase 6 QoE visualization | Open |
| 4 | ~~Cross-device resume — should Phase 6 persist playback position to backend (phone → Fire TV)?~~ | **Resolved** — yes, Phase 6 persists position to backend; resume is video-scoped (not device-scoped), enabling phone ↔ TV resume. See `data-model.md` `PlaybackSession` notes |
| 5 | **Do any of StreamKit's three live stream URLs actually play?** Red Bull TV's candidate produced no video on manual testing (suspected geo-restriction). DW English and NHK World-Japan are newly proposed candidates with even less verification depth (no HTTP/manifest check was possible) — see `content-catalog.md` Live Sources 1–3 for exact detail on each. | **Open — blocking Phase 1 live-source implementation** for all three. See `data-model.md` Open Schema Questions #4–5 |
| 6 | Local network exposure for Fire TV hardware testing — `api-contract.md` notes the backend is `localhost`-only, but Fire TV is a separate physical device needing LAN access in Phase 5/6 | Open — needs a decision before Phase 5 hardware testing |
| 7 | ~~Two additional live stream URLs needed~~ | **Candidates proposed** — DW English and NHK World-Japan (see Question #5 above for verification status). Need Dani's sign-off on these two specific brands before they're locked in; not a structural blocker on `navigation.md` itself |
| 8 | ~~Confirm back-button behavior while a player screen is maximized (landscape)~~ | **Resolved** — back exits fullscreen to portrait first, staying on the same screen; second back exits to Catalog. Matches video streaming app convention (YouTube, Netflix, Hulu) |
| 9 | ~~Should the bitrate/resolution/buffer health overlay (Settings toggle) default to on or off?~~ | **Resolved** — default on |

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

**Awaiting Dani's confirmation on the two proposed live sources** (DW English, NHK World-Japan — Open Question #5/#7) before finalizing `SPEC.md`'s `BR-CAT-03` to plural. That confirmation is the only thing left blocking full closure of the `navigation.md` Live carousel content; it does not block starting `specs/design/design.md`, which can proceed independently using the screen behaviors already locked in `navigation.md`.

**Separately, regardless of confirmation:** none of the three live URLs (Red Bull TV included) have been manually playback-tested yet. That verification step is still needed before any of them is wired into Android implementation.

**Mux test asset selection** (Open Question #1 / `content-catalog.md`) — pick and upload actual VOD test content. Still open, unrelated to the live-source work above.

Start a new chat session and reference this file (`CONTEXT.md`) plus `SPEC.md`, `ARCHITECTURE.md`, and `navigation.md` for full context.

---

## Changelog

| Version | Date | Author | Notes |
|---|---|---|---|
| 0.1.0 | 2026-06-16 | Danielle Mariani | Initial draft — root specs complete, data-model next |
| 0.1.1 | 2026-06-20 | Danielle Mariani | data-model.md, api-contract.md, and content-catalog.md completed and moved to Completed Spec Documents; NASA TV replaced with Red Bull TV throughout (flagged unverified); cross-device resume open question resolved; added open questions on Red Bull TV verification and Fire TV LAN exposure; Next Step updated to streaming-glossary.md |
| 0.1.2 | 2026-06-20 | Danielle Mariani | Dropped `specs/technical/streaming-glossary.md` from scope — `SPEC.md`'s existing glossary is sufficient; deeper protocol detail is better referenced live during implementation. Removed from repository structure and pending docs. Next Step updated to `specs/design/design.md` |
| 0.1.3 | 2026-06-25 | Danielle Mariani | `specs/design/navigation.md` completed — moved to Completed Spec Documents; added Pending Document Updates section tracking the SPEC.md/ARCHITECTURE.md changes it triggers; added Open Questions #7–9 (additional live sources, back-button-while-maximized confirmation, overlay default state); Next Step updated to resolving those updates, then `design.md` |
| 0.1.4 | 2026-06-25 | Danielle Mariani | Resolved Open Questions #8 and #9 — back-button-while-maximized confirmed (exits fullscreen first, stays on screen) and bitrate/resolution/buffer overlay defaults to off; `navigation.md` reference bumped to v0.1.1 |
| 0.1.5 | 2026-06-26 | Danielle Mariani | `SPEC.md` updated to v0.1.5 applying the `navigation.md`-triggered changes (BR-CAT-02, BR-LIV-04/05, BR-PLY-03/07–11, BR-PIP-01, Settings Feature Index row); flagged an additional BR-PLY-03 conflict found during the edit and a matching `PRODUCT.md` phrasing that wasn't in scope; `ARCHITECTURE.md` orientation note remains the only open item from that list; Next Step updated accordingly |
| 0.1.6 | 2026-06-26 | Danielle Mariani | `ARCHITECTURE.md` updated to v0.1.2 — added Screen Orientation subsection (per-destination `requestedOrientation` handling); also fixed a stale "portrait" descriptor and a stale NASA TV reference found during the edit. All `navigation.md`-triggered document updates are now applied; Next Step updated to `design.md` with nothing structural blocking |
| 0.1.7 | 2026-06-26 | Danielle Mariani | Flipped BR-PLY-03's overlay default from off to on per direction; updated `SPEC.md` (v0.1.6) and `navigation.md` (v0.1.2) to match; confirmed no change needed to `PRODUCT.md`, which already assumed the overlay is shown by default; Open Question #9 resolution text updated accordingly |
| 0.1.8 | 2026-06-26 | Danielle Mariani | Searched for and proposed two additional live source candidates — DW English and NHK World-Japan — to fill `navigation.md`'s Live carousel; updated `content-catalog.md` (v0.1.1) and `data-model.md` (v0.1.4) accordingly; both candidates are unverified and pending Dani's confirmation, so `SPEC.md`'s `BR-CAT-03` was deliberately left singular rather than flipped to plural; consolidated Open Questions #5 and #7 around this; Next Step updated to reflect the pending confirmation |
| 0.1.9 | 2026-06-26 | Danielle Mariani | Fixed three stale "Open Question #1" cross-references that should have read #5/#7 after the live-source question renumbering in v0.1.8 — confirmed the underlying SPEC.md and ARCHITECTURE.md work itself was already fully applied (BR-PLY-03 default, the "portrait" wording fix, and the NASA TV reference fix); this was a documentation cross-reference bug, not unfinished work |