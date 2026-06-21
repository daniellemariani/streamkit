# content-catalog.md — StreamKit

**Version:** 0.1.0
**Status:** Draft
**Owner:** Danielle Mariani
**Created at:** 2026-06-20
**Last Updated:** 2026-06-20
**Location:** specs/technical/content-catalog.md

---

## Overview

This document catalogs the actual content sources StreamKit uses during development and testing: where VOD content comes from, what the live stream source is, and how each maps onto the `Video` entity defined in `specs/technical/data-model.md`.

This is a **sources** document, not a content strategy document. It does not define what categories of content StreamKit should have (that's a product decision, out of scope for a personal learning project) — it defines where the bytes actually come from, what format they arrive in, and what's been verified to work versus what hasn't.

This document inherits the verification status of its live source directly from `specs/technical/data-model.md`. Where the two documents might drift, `data-model.md`'s Open Schema Questions section is authoritative — this document should be updated to match it, not the other way around.

---

## Related Documents

| Document | Purpose |
|---|---|
| SPEC.md | Glossary definitions for Mux, Red Bull TV, CDN, and related terms |
| ARCHITECTURE.md | Environment switching (Mux vs Local), NetworkModule design |
| specs/technical/data-model.md | `Video` entity schema; Open Schema Question #4 (Red Bull TV verification) |
| specs/technical/api-contract.md | `GET /api/v1/videos` and related ingestion endpoints (Phase 4+) |

---

## Content Sources Summary

| Type | Source | Phase Available | Status |
|---|---|---|---|
| VOD catalog | Mux API | 1 | Generic — no specific test assets selected yet |
| Live stream | Red Bull TV ("Best of Red Bull") | 1 | **Unverified** — see below |
| Custom VOD | Local backend (ingestion pipeline) | 4 | Not started |
| DRM test streams | Shaka or Axinom test license server | 5 | Not started |

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
| `title` | Set manually in Mux dashboard, or via passthrough metadata at upload | Mux doesn't enforce a title field on the asset itself — StreamKit will need a convention (see Open Questions) |
| `description` | Same as `title` — not a native Mux asset field | |
| `type` | Always `"VOD"` for Mux-sourced content | Mux is not used for StreamKit's live source |
| `thumbnail_url` | Mux's auto-generated thumbnail endpoint (`https://image.mux.com/{PLAYBACK_ID}/thumbnail.jpg`) | Requires the asset's Playback ID, not the Asset ID |
| `stream_url` | Mux's HLS playback URL (`https://stream.mux.com/{PLAYBACK_ID}.m3u8`) | This is what ExoPlayer actually loads |
| `duration_seconds` | Returned by the Mux Asset API after upload processing completes | Not available until the asset finishes processing |
| `is_drm_protected` | `false` for all Phase 1–4 Mux content | Mux supports DRM on paid tiers; StreamKit does not exercise this — Widevine in Phase 5 is proxied through StreamKit's own backend, not Mux's |

### Recommended Test Content (To Be Selected)

When test assets are chosen, they should cover a reasonable spread for exercising ABR and player behavior:

- At least one short clip (under 2 minutes) for fast iteration during player development
- At least one longer asset (10+ minutes) to meaningfully exercise seeking, progress bar dragging (BR-PLY-06), and resume-from-position behavior
- Royalty-free or Creative Commons content recommended, given this is a personal project with no content licensing in place (PRODUCT.md non-goals: "no content licensing or rights management beyond Widevine test credentials")

### Open Item

Once assets are uploaded, this section should be updated with actual Asset IDs, titles, and confirmed `stream_url` values — replacing the generic mapping above with real, tested entries.

---

## Live Source — Red Bull TV

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
3. **Re-evaluate the live source entirely** — if Red Bull TV cannot be made to work reliably, the live-source decision should be reopened rather than forcing a fragile workaround. This was explicitly deferred per Dani's direction (a backup/fallback source was considered but intentionally not adopted as a Plan B at this stage) — if Red Bull TV fails, that conversation should be revisited rather than silently defaulting to whatever's convenient.

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

## Custom VOD — Local Ingestion Pipeline (Phase 4+)

Not yet started. Once the Phase 4 ingestion pipeline is built, this section should document any test source video files used to exercise the FFmpeg/Shaka Packager pipeline — for example, a small set of royalty-free raw video files (mp4 or mov) kept locally for repeated ingestion testing, distinct from the Mux-sourced and Red Bull TV catalog entries.

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
| 2 | Which specific Mux test assets to upload, and what naming/title convention to use for them | Open — deferred until Mux account setup and first upload |
| 3 | Mux free tier quota sufficiency (carried over from `PRODUCT.md`) | Open — unconfirmed until real usage against the free tier is observed |
| 4 | Thumbnail source for the Red Bull TV catalog entry (Mux has a thumbnail API; Red Bull TV does not) | Open — likely resolved with a bundled static image asset rather than a remote fetch |
| 5 | Shaka vs Axinom for Phase 5 DRM test license server | Deferred to Phase 5 kickoff, per `ARCHITECTURE.md` |

---

## Changelog

| Version | Date | Author | Notes |
|---|---|---|---|
| 0.1.0 | 2026-06-20 | Danielle Mariani | Initial draft. Documents Mux as the generic VOD source (no assets selected yet) and Red Bull TV as the live source, replacing the discontinued NASA TV. Carries forward the unverified status of the Red Bull TV stream URL from `data-model.md` v0.1.3 |