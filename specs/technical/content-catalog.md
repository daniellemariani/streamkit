# content-catalog.md — StreamKit

**Version:** 0.1.2
**Status:** Draft
**Owner:** Danielle Mariani
**Created at:** 2026-06-20
**Last Updated:** 2026-07-10
**Location:** specs/technical/content-catalog.md

---

## Overview

This document catalogs the actual content sources StreamKit uses during development and testing: where VOD content comes from, what the live stream sources are, and how each maps onto the `Video` entity defined in `specs/technical/data-model.md`.

This is a **sources** document, not a content strategy document. It does not define what categories of content StreamKit should have (that's a product decision, out of scope for a personal learning project) — it defines where the bytes actually come from, what format they arrive in, and what's been verified to work versus what hasn't.

This document inherits the verification status of its live sources directly from `specs/technical/data-model.md`. Where the two documents might drift, `data-model.md`'s Open Schema Questions section is authoritative — this document should be updated to match it, not the other way around.

---

## Related Documents

| Document | Purpose |
|---|---|
| SPEC.md | Glossary definitions for Mux, Red Bull TV, CDN, and related terms |
| ARCHITECTURE.md | Environment switching (Mux vs Local), NetworkModule design |
| specs/technical/data-model.md | `Video` entity schema; Open Schema Questions (live source verification) |
| specs/technical/api-contract.md | `GET /api/v1/videos` and related ingestion endpoints (Phase 4+) |
| specs/design/navigation.md | Catalog screen's 3-item Live carousel, which these three sources populate |

---

## Content Sources Summary

| Type | Source | Phase Available | Status |
|---|---|---|---|
| VOD catalog | Mux API | 1 | Generic — no specific test assets selected yet |
| Live stream 1 | Red Bull TV ("Best of Red Bull") | 1 | **Unverified** — see below |
| Live stream 2 | DW (Deutsche Welle) English | 1 | **Proposed, unverified** — see below |
| Live stream 3 | NHK World-Japan | 1 | **Proposed, unverified** — see below |
| Custom VOD | Local backend (ingestion pipeline) | 4 | Not started |
| DRM test streams | Shaka or Axinom test license server | 5 | Not started |

> Live streams 2 and 3 are newly proposed candidates (added 2026-06-26) to fill `navigation.md`'s 3-item Live carousel. They have not yet been confirmed by Dani — flagging that distinction explicitly since picking specific broadcaster brands is a content decision, not a structural one.

---

## VOD Source — Mux

### What Mux Provides

Mux is the source for catalog metadata and VOD stream delivery in Phase 1–3 (before the local ingestion pipeline exists in Phase 4). Per ARCHITECTURE.md's environment switching design, the Android client's `NetworkModule` calls the Mux API directly when the "Mux" environment is selected in Settings.

### Current State

No specific test assets have been selected or uploaded to Mux yet. This is expected at this stage of the project — Mux's free tier and asset upload flow haven't been exercised, and `PRODUCT.md`'s open question ("confirm Mux's free tier covers catalog management, video upload, and delivery volume sufficient for Phase 1") remains open.

This section documents the **expected shape** of Mux-sourced content, to be filled in with real asset IDs once test content is uploaded.

### Expected Mapping to `Video` Entity

| `Video` field | Mux source | Notes |
|---|---|---|
| `id` | Mux Asset ID | e.g. a string like `"a1b2c3d4e5"` — assigned by Mux on upload, not chosen by StreamKit |
| `title` | Parsed from the asset's `passthrough` field — JSON set at upload time: `{"title": "...", "description": "..."}` | Falls back to the Mux asset `id` if `passthrough` is missing or fails to parse. See "Naming Convention" below and `specs/features/catalog/requirements.md` RQ-CAT-18/19 |
| `description` | Parsed from the same `passthrough` JSON | Falls back to `null` if missing or fails to parse |
| `type` | Always `"VOD"` for Mux-sourced content | Mux is not used for StreamKit's live sources |
| `thumbnail_url` | Mux's auto-generated thumbnail endpoint (`https://image.mux.com/{PLAYBACK_ID}/thumbnail.jpg`) | Requires the asset's Playback ID, not the Asset ID |
| `stream_url` | Mux's HLS playback URL (`https://stream.mux.com/{PLAYBACK_ID}.m3u8`) | This is what ExoPlayer actually loads |
| `duration_seconds` | Returned by the Mux Asset API after upload processing completes | Not available until the asset finishes processing |
| `is_drm_protected` | `false` for all Phase 1–4 Mux content | Mux supports DRM on paid tiers; StreamKit does not exercise this — Widevine in Phase 5 is proxied through StreamKit's own backend, not Mux's |

### Naming Convention — Resolved (`passthrough` metadata)

Resolves the naming-convention half of Open Question #2 below. At upload time, each asset's `passthrough` field is set to a JSON string carrying human-readable metadata, e.g.:

```json
{"title": "Trip to Spain", "description": "Summer 2024 travel clip"}
```

Mux does not read, validate, or derive this from the uploaded filename — it stores whatever string is sent and echoes it back unchanged on every API response (`GET /video/v1/assets` and webhooks alike). The Android client parses this string on catalog sync and maps it to `Video.title` / `Video.description`.

If `passthrough` is missing, blank, or fails to parse, `title` falls back to the Mux asset `id` and `description` falls back to `null` — the same fallback previously documented as the default behavior, now scoped to the error path only.

Uploading a file named, say, `trip_spain.mp4` does **not** by itself populate `title` — the original filename is discarded by Mux after upload. The `passthrough` JSON must be set explicitly, either via the API at upload time or by editing the asset's metadata afterward.

### Recommended Test Content (To Be Selected)

When test assets are chosen, they should cover a reasonable spread for exercising ABR and player behavior:

- At least one short clip (under 2 minutes) for fast iteration during player development
- At least one longer asset (10+ minutes) to meaningfully exercise seeking, progress bar dragging (BR-PLY-06), and resume-from-position behavior
- Royalty-free or Creative Commons content recommended, given this is a personal project with no content licensing in place (PRODUCT.md non-goals: "no content licensing or rights management beyond Widevine test credentials")

### Open Item

Once assets are uploaded, this section should be updated with actual Asset IDs, titles, and confirmed `stream_url` values — replacing the generic mapping above with real, tested entries.

---

## Live Source 1 — Red Bull TV

### Why Red Bull TV

StreamKit originally specified NASA TV as its live stream source. NASA's 24/7 NTV1-HLS public channel was discontinued in August 2024, and its previously-documented stream URLs (`nasa-i.akamaihd.net`, `ntv1.akamaized.net`) no longer resolve reliably — confirmed when the originally-documented URL returned no content during testing. Red Bull TV's 24/7 "Best of Red Bull" curated stream was identified as a replacement candidate: it is HLS, genuinely continuous (not a scraped session-token URL like several other public streaming services), and appears to be served from a stable, first-party Akamai endpoint rather than a third-party aggregator.

### Candidate Stream Details

| Field | Value |
|---|---|
| Provider | Red Bull TV (Red Bull Media House) |
| Stream name | "Best of Red Bull" (24/7 curated channel) |
| Protocol | HLS |
| Candidate URL | `https://rbmn-live.akamaized.net/hls/live/590964/BoRB-AT/master.m3u8` |
| CDN | Akamai |
| Renditions observed | Six, ranging from 320×180 up to 1920×1080 (H.264 / AAC) |

### Verification Status: UNVERIFIED

**This URL has not been confirmed to play actual video.** Here is exactly what has and hasn't been checked, so this isn't mistaken for a green light:

**Confirmed:**
- The URL resolves and returns `Content-Type: application/x-mpegURL` (the correct MIME type for an HLS manifest)
- A third-party stream-testing tool (streamtest.in) parsed the manifest and reported a valid multi-rendition HLS structure: 6 video renditions, H.264 video / AAC audio, standard variant bitrates
- The same channel path (`590964/BoRB-AT`) appears consistently across multiple independent IPTV aggregator lists spanning several years — suggesting the channel ID itself is durable and not a short-lived session token (unlike, for example, Pluto TV's stitcher URLs, which embed device IDs and JWTs that expire)

**Not confirmed:**
- Direct manual playback testing of this exact URL produced **no video**
- The Akamai edge for this host has been observed returning an `X-GeoBlock: true` response header in at least one third-party uptime check — geo-restriction is the leading suspected cause of the playback failure, but this has not been confirmed against the specific network StreamKit will be developed and tested from

**This is a blocking item.** Do not wire this URL into Android implementation (catalog seeding, `NetworkModule` static config, ExoPlayer testing) until it has been manually verified to play from the actual development network — e.g. via VLC (`vlc https://rbmn-live.akamaized.net/hls/live/590964/BoRB-AT/master.m3u8`) or a direct ExoPlayer smoke test on the Pixel 9 Pro.

### If Verification Fails

If the candidate URL continues to fail playback after direct testing, the next steps, in order of effort:

1. **Try sibling rendition URLs** seen in aggregator lists for the same channel — `master_1660.m3u8` or `master_3360.m3u8` instead of the bare `master.m3u8`. It's possible the master playlist is blocked while a specific bitrate variant resolves, though this is speculative and unconfirmed.
2. **Test from a different network** — if geo-restriction is confirmed as the cause, testing from a different country/region (or via VPN, acknowledging this only diagnoses the problem rather than fixing it for normal use) would confirm the hypothesis.
3. **Fall back to one of the other two carousel sources** — now that DW English and NHK World-Japan are proposed (see below), a third live slot failing doesn't mean reopening the whole live-source decision the way it would have before; it just means the carousel temporarily runs with two verified sources while a replacement third is sourced.

### Mapping to `Video` Entity

| `Video` field | Value |
|---|---|
| `id` | `"redbull_tv"` (static key, per `data-model.md`) |
| `title` | `"Red Bull TV — Best of Red Bull"` |
| `description` | `"24/7 curated stream of Red Bull original content."` |
| `type` | `"LIVE"` |
| `thumbnail_url` | Not yet sourced — Red Bull TV does not expose a documented thumbnail API the way Mux does; a static asset bundled with the app may be simpler than scraping one |
| `stream_url` | The candidate URL above — **unverified, see warning** |
| `duration_seconds` | `null` (live stream) |
| `is_drm_protected` | `false` |
| `source` | `'STATIC'` (per `data-model.md`'s `source` enum on the backend `videos` table) |

---

## Live Source 2 — DW (Deutsche Welle) English

**Proposed 2026-06-26, pending Dani's confirmation.** Added to fill the second of three slots in `navigation.md`'s Live carousel.

### Why DW

DW is Germany's state-funded international broadcaster, comparable in global profile to BBC World News or France 24. Its live HLS streams are served from a first-party Akamai subdomain pattern (`dwamdstream*.akamaized.net`) that has appeared consistently across independently maintained IPTV reference sources over multiple years — the same durability signal (a stable channel path rather than a short-lived session token) that made Red Bull TV's candidate URL attractive in the first place.

### Candidate Stream Details

| Field | Value |
|---|---|
| Provider | Deutsche Welle (DW) |
| Stream name | DW English |
| Protocol | HLS |
| Candidate URL | `https://dwamdstream107.akamaized.net/hls/live/2017968/dwstream107/stream05/streamPlaylist.m3u8` |
| CDN | Akamai (first-party DW subdomain) |

### Verification Status: UNVERIFIED

**Confirmed:**
- The URL pattern (host `dwamdstream107`, channel ID `2017968`) appears consistently across independently maintained IPTV reference lists spanning multiple recent years, suggesting a durable channel path
- The domain is a first-party DW subdomain, not a third-party aggregator or reseller CDN

**Not confirmed:**
- No manual playback test has been performed — this candidate hasn't been tried in VLC, ExoPlayer, or any player
- Unlike Red Bull TV's original research, no HTTP content-type check or third-party manifest-parsing tool was run against this URL during this research pass — this candidate has had **less verification depth** than Red Bull TV, not equal depth

**This is a blocking item**, same as Red Bull TV — do not wire this into Android implementation until manually verified to play from the actual development network.

### Mapping to `Video` Entity

| `Video` field | Value |
|---|---|
| `id` | `"dw_english"` (static key) |
| `title` | `"DW English"` |
| `description` | `"24/7 international news and current affairs from Deutsche Welle (Germany)."` |
| `type` | `"LIVE"` |
| `thumbnail_url` | Not yet sourced — likely a bundled static asset, same approach as Red Bull TV |
| `stream_url` | The candidate URL above — **unverified, see warning** |
| `duration_seconds` | `null` (live stream) |
| `is_drm_protected` | `false` |
| `source` | `'STATIC'` |

---

## Live Source 3 — NHK World-Japan

**Proposed 2026-06-26, pending Dani's confirmation.** Added to fill the third of three slots in `navigation.md`'s Live carousel.

### Why NHK World-Japan

NHK World-Japan is the Japan Broadcasting Corporation's international English-language channel. Its live HLS endpoint has lived under an official `nhkworld.jp` family domain for years, though the specific path has migrated at least once — from an older `master.nhkworld.jp` URL to the current `media-tyo.hls.nhkworld.jp` URL. That migration history is worth naming up front: it's a higher churn signal than Red Bull TV's unchanged channel ID or DW's stable subdomain pattern, even though the domain itself is still first-party NHK infrastructure rather than a third-party reseller.

### Candidate Stream Details

| Field | Value |
|---|---|
| Provider | NHK (Japan Broadcasting Corporation) |
| Stream name | NHK World-Japan |
| Protocol | HLS |
| Candidate URL | `https://media-tyo.hls.nhkworld.jp/hls/w/live/master.m3u8` |
| CDN | NHK first-party infrastructure |

### Verification Status: UNVERIFIED

**Confirmed:**
- This is the most recently reported working URL across IPTV reference sources (as recently as January 2026), explicitly replacing an older `master.nhkworld.jp` path reported as no longer working
- The domain is a first-party NHK subdomain

**Not confirmed:**
- No manual playback test, HTTP content-type check, or manifest-parsing tool has been run against this URL
- Given the prior path migration, there's a real chance this URL has already changed again by the time it's tested — treat this as the least durable of the three candidates

**This is a blocking item**, same as the other two. Of the three, this is the one most worth re-checking for a current URL immediately before testing, rather than assuming the value documented here is still current.

### Mapping to `Video` Entity

| `Video` field | Value |
|---|---|
| `id` | `"nhk_world"` (static key) |
| `title` | `"NHK World-Japan"` |
| `description` | `"24/7 English-language news and culture coverage from Japan's NHK World."` |
| `type` | `"LIVE"` |
| `thumbnail_url` | Not yet sourced — likely a bundled static asset, same approach as Red Bull TV |
| `stream_url` | The candidate URL above — **unverified, and has changed paths before** |
| `duration_seconds` | `null` (live stream) |
| `is_drm_protected` | `false` |
| `source` | `'STATIC'` |

---

## Custom VOD — Local Ingestion Pipeline (Phase 4+)

Not yet started. Once the Phase 4 ingestion pipeline is built, this section should document any test source video files used to exercise the FFmpeg/Shaka Packager pipeline — for example, a small set of royalty-free raw video files (mp4 or mov) kept locally for repeated ingestion testing, distinct from the Mux-sourced and live catalog entries.

**Placeholder — to be filled in at Phase 4 kickoff:**
- Source file(s) for ingestion testing — format, resolution, length
- Where they're stored (not committed to source control, per typical media-asset hygiene — likely `.gitignore`'d under a local `backend/test-media/` directory or similar)
- Whether the same file is reused across multiple ingestion test runs or a fresh file is needed for each `IngestJob`

---

## DRM Test Streams (Phase 5+)

Not yet started. Per `ARCHITECTURE.md` and `PRODUCT.md`, Widevine L3 test credentials from a public test server (Shaka or Axinom) are assumed sufficient for Phase 5. This section should be filled in with whichever provider is selected, including:

- The specific test license server endpoint
- Test content associated with that license server (test providers typically pair their license server with specific sample DRM-protected streams)
- Whether the same Mux or local-ingestion content can be re-packaged with DRM, or whether a separate DRM-specific test asset is needed

---

## Open Questions

| # | Question | Status |
|---|---|---|
| 1 | Red Bull TV stream URL playback verification (see above) | **Open — blocking Phase 1 live-source implementation.** Tracked in `data-model.md` Open Schema Question #4; this document mirrors that status and should be updated in lockstep |
| 2 | Which specific Mux test assets to upload | Open — deferred until Mux account setup and first upload. **Naming/title convention resolved** — see "VOD Source — Mux → Naming Convention" above (`passthrough` metadata). Implementation tracked in `catalog_tasks.md` TSK-CAT-35/TSK-CAT-36 |
| 3 | Mux free tier quota sufficiency (carried over from `PRODUCT.md`) | Open — unconfirmed until real usage against the free tier is observed |
| 4 | Thumbnail source for the Red Bull TV catalog entry (Mux has a thumbnail API; Red Bull TV does not) | Open — likely resolved with a bundled static image asset rather than a remote fetch |
| 5 | Shaka vs Axinom for Phase 5 DRM test license server | Deferred to Phase 5 kickoff, per `ARCHITECTURE.md` |
| 6 | **DW English and NHK World-Japan stream URL playback verification** (see Live Source 2/3 above) | **Open — blocking, same caveat as #1.** Also pending Dani's confirmation that these are the right two sources to add at all |
| 7 | Thumbnail source for DW English and NHK World-Japan catalog entries | Open — likely resolved the same way as #4, bundled static images |

---

## Changelog

| Version | Date | Author | Notes |
|---|---|---|---|
| 0.1.0 | 2026-06-20 | Danielle Mariani | Initial draft. Documents Mux as the generic VOD source (no assets selected yet) and Red Bull TV as the live source, replacing the discontinued NASA TV. Carries forward the unverified status of the Red Bull TV stream URL from `data-model.md` v0.1.3 |
| 0.1.1 | 2026-06-26 | Danielle Mariani | Added Live Source 2 (DW English) and Live Source 3 (NHK World-Japan) as proposed candidates to fill `navigation.md`'s 3-item Live carousel, per Open Question #7 in `CONTEXT.md`. Both flagged unverified with less verification depth than the original Red Bull TV research (no HTTP/manifest check was possible in this pass). Pending Dani's confirmation before treated as final. |
| 0.1.2 | 2026-07-10 | Danielle Mariani | Resolved the naming-convention half of Open Question #2: `title`/`description` are now sourced from the Mux asset's `passthrough` field (JSON, set at upload time), falling back to the asset `id`/`null` only when `passthrough` is missing or malformed. Updated the "Expected Mapping to `Video` Entity" table and added a "Naming Convention" subsection. Test-asset selection itself remains open. Implementation tracked in `catalog_tasks.md` TSK-CAT-35/TSK-CAT-36 — note `specs/features/catalog/requirements.md` RQ-CAT-18/RQ-CAT-19 still describe the old id-fallback-by-default behavior and need a matching update (not yet done) |