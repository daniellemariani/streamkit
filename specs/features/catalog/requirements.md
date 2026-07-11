# Catalog — Requirements

**Version:** 0.2.2
**Status:** Draft
**Phase:** 1 (Android)
**Owner:** Danielle Mariani
**Created at:** 2026-06-28
**Last Updated:** 2026-07-10

---

## Introduction

The Catalog is StreamKit's start destination — the single-scroll, portrait-only screen a user lands on at launch, combining a Live carousel and a VOD grid. It is the root of all navigation: both the Live Player and Player screens are entered exclusively from here, and Settings is reachable from the top app bar. There is no separate "home" or "browse" distinction — Catalog is the entirety of the browse experience for Phase 1.

The screen renders from a local Room cache immediately on launch, then refreshes VOD content from Mux in the background. Live content is static and always available, independent of any network call. Catalog has no completion state or lifecycle beyond this — it is shown every time the app is foregrounded, not just on first launch.

---

## Scope Boundaries

### In Scope

- Top app bar with app title/logo and Settings entry point
- Live carousel: 3 static entries, swipeable, always rendered
- VOD grid: 2-column, populated from the Mux `List Assets` API
- Cache-first render from Room, with background refresh against Mux
- Loading, empty, and error states for the VOD grid; independent state handling for the Live carousel
- Navigation entry points to Live Player, Player, and Settings
- Android mobile (phone/tablet), Phase 1

### Out of Scope

- Fire TV layout — covered in `specs/features/fire-tv/requirements.md` (Phase 3); this document's data layer (`core` module) is shared with it
- Search, filtering, sorting, or personalization (per SPEC.md "Out of Scope")
- Detail metadata beyond the `Video` entity fields defined in `data-model.md`
- Manual playback verification of the 3 live stream URLs — tracked separately (`data-model.md` Open Schema Questions #4–5)
- Mux test asset selection — tracked separately (`content-catalog.md` Open Question #2). The `title`/`description` naming convention itself is now specified below (RQ-CAT-18, RQ-CAT-19), resolved via Mux `passthrough` metadata
- Settings screen behavior — covered in `specs/features/settings/requirements.md`

---

## Requirements

### Top App Bar

**RQ-CAT-01 — App bar display**
The Catalog screen displays a top app bar at all times, regardless of scroll position or content state. It is not dismissible and does not collapse on scroll.

**RQ-CAT-02 — App bar content**
The top app bar contains a text label displaying the app name "StreamKit" (left-aligned) and a Settings gear icon (right-aligned). No other controls are present in Phase 1.

**RQ-CAT-03 — Settings navigation**
Tapping the gear icon navigates to the Settings screen.

---

### Live Carousel

**RQ-CAT-04 — Carousel composition**
The Live carousel displays exactly 3 items, horizontally swipeable, with pager dots reflecting the active item. All 3 items are always present regardless of any network state, since they are sourced from static config, not Mux.

**RQ-CAT-05 — Card content**
Each card displays a poster image with a bottom-left overlapping stream name label, and nothing else (per `navigation.md`, `design.md`).

**RQ-CAT-06 — Tap behavior**
Tapping a Live card navigates to Live Player with that item's `video_id`. Playback begins automatically on arrival (BR-CAT-02).

**RQ-CAT-07 — Live badge**
Each Live card displays a LIVE badge, visually distinguishing it from VOD content (BR-CAT-01).

---

### VOD Grid

**RQ-CAT-08 — Grid composition**
The VOD grid is a 2-column layout; each item displays a poster image and a title label.

**RQ-CAT-09 — Tap behavior**
Tapping a VOD item navigates to Player with that item's `video_id`. Playback does not begin automatically — Player is tap-to-play (BR-CAT-02).

**RQ-CAT-10 — Item ordering**
Grid items render in the order returned by Mux's `List Assets` response. No client-side sort is applied in Phase 1.

---

### Catalog Lifecycle: Loading, Empty & Error States

**RQ-CAT-11 — Initial loading state**
If no Room cache exists yet (first launch, before any successful Mux fetch has completed), the VOD grid displays a loading placeholder rather than an empty state.

**RQ-CAT-12 — Live carousel has no loading state**
The Live carousel never shows a loading state — its 3 entries are seeded from static config and are available immediately, before any network activity.

**RQ-CAT-13 — VOD empty state**
If Mux returns zero assets with `status == "ready"`, the VOD grid displays an empty state with a neutral message (e.g. "No videos available.").

**RQ-CAT-14 — Independent states**
The Live carousel's and VOD grid's loading/empty/error states are evaluated and rendered independently — one section's state never affects the other's render.

**RQ-CAT-15 — VOD error state**
If the Mux fetch fails (network error, auth failure, non-2xx response): if a prior successful cache exists, the grid retains that cached content with a non-blocking inline error indicator; if no cache exists, the grid shows an explicit error state, visually distinct from the empty state in RQ-CAT-13.

**RQ-CAT-16 — Stale-while-revalidate**
A failed background refresh never clears or replaces an already-rendered cache. The Room cache is only mutated following a successful Mux fetch.

---

### Background: Mux Sync & Static Seeding

**RQ-CAT-17 — Mux fetch & pagination**
On each catalog refresh, the VOD grid's source data is populated by calling `GET /video/v1/assets`, paginating via the response's `next_cursor` until exhausted.

**RQ-CAT-18 — Field mapping**
Mux asset fields map to the `Video` entity as: `id` ← Mux `id`; `thumbnailUrl` ← `https://image.mux.com/{playback_id}/thumbnail.jpg`; `streamUrl` ← `https://stream.mux.com/{playback_id}.m3u8`; `durationSeconds` ← Mux `duration`, rounded to the nearest second; `title`/`description` ← parsed from Mux `passthrough` JSON metadata (see RQ-CAT-19).

**RQ-CAT-19 — Title/description mapping and fallback**
`title`/`description` are parsed from the Mux asset's `passthrough` field — a JSON string set at upload time in the shape `{"title": "string", "description": "string?"}`. If `passthrough` is missing, blank, or fails to parse (malformed JSON or a missing `title` key), `Video.title` falls back to the Mux asset `id` and `Video.description` falls back to `null`, so the grid never renders a blank label. See `content-catalog.md`'s "VOD Source — Mux → Naming Convention" section for the full convention detail.

**RQ-CAT-20 — Static live seeding**
The 3 Live entries are seeded directly into the `videos` Room table on first app launch (or on a seed-version bump), independent of any Mux call. Each entry is assigned a stable UUID as its `Video.id`:

| UUID | Title | Notes |
|---|---|---|
| _(assigned at runtime, on first app launch)_ | Red Bull TV | Candidate URL, unverified |
| _(assigned at runtime, on first app launch)_ | DW English | Candidate URL, unverified |
| _(assigned at runtime, on first app launch)_ | NHK World-Japan | Candidate URL, unverified |

**RQ-CAT-21 — Stale row cleanup**
Following a successful Mux fetch, Mux-sourced rows no longer present in the latest response are deleted from the cache via `VideoDao.deleteStale`. Static Live entries are excluded from this deletion and are never removed by a Mux sync.

**RQ-CAT-22 — Render performance budget**
Catalog's render from the Room cache completes within 2 seconds of launch (NFR-PE-02).

---

## Business Rules

> Reference rules from SPEC.md by ID. Never copy rule text here.

- BR-CAT-01: VOD and Live content must be visually distinguishable
- BR-CAT-02: VOD is detail-first (tap-to-play); Live is exempt (autoplay)
- BR-CAT-03: Catalog sources from Mux (VOD) and static config (Live)
- NFR-PE-02: Catalog render budget of 2 seconds

**Feature-specific rules:**

**BR-CAT-04 — Ready-only assets**
A VOD asset must not appear in the catalog unless its Mux `status` is `ready`.

> This rule is also being added to `data-model.md` as a general Mux-mapping note, since it applies to any future feature that consumes Mux assets, not just the Catalog.

---

## Acceptance Criteria

**AC-CAT-01 — Cold launch, no cache**
Given the app launches with no existing Room cache,
When the VOD grid initializes,
Then it shows a loading state, then populates from Mux within 2 seconds of a successful response.

**AC-CAT-02 — Warm launch, cache present**
Given a Room cache exists from a prior session,
When the app launches,
Then Catalog renders the cached content within 2 seconds, then silently updates if the background Mux refresh returns different data.

**AC-CAT-03 — Non-ready assets excluded**
Given Mux returns one or more assets with a `status` other than `ready`,
When the catalog refresh completes,
Then none of those assets appear in the VOD grid.

**AC-CAT-04 — Fetch fails, no cache**
Given the Mux fetch fails and no prior cache exists,
When the VOD grid renders,
Then an explicit error state is shown — not the empty state from AC-CAT-06.

**AC-CAT-05 — Fetch fails, cache exists**
Given the Mux fetch fails and a prior cache exists,
When the VOD grid renders,
Then the cached grid remains visible, with a non-blocking inline error indicator and no destructive UI change.

**AC-CAT-06 — Zero ready assets**
Given Mux returns zero assets with `status == "ready"`,
When the VOD grid renders,
Then a neutral empty state message is shown, distinct from the error state in AC-CAT-04.

**AC-CAT-07 — Live carousel always available**
Given any state of the Mux fetch (loading, error, or success),
When the Catalog screen renders,
Then all 3 Live entries are visible with correct LIVE badges, unaffected by VOD state.

**AC-CAT-08 — Navigation split**
Given the Catalog screen,
When the user taps a VOD item versus a Live item,
Then the VOD tap opens Player without autoplay, and the Live tap opens Live Player with autoplay.

**AC-CAT-09 — Settings entry point**
Given the Catalog screen,
When the user taps the gear icon in the top app bar,
Then the Settings screen opens.

---

## Error Handling

| Scenario | Error Type | User-Facing Message | Recovery |
|---|---|---|---|
| Mux fetch fails, no cache exists | Full-section error state (VOD grid only) | "Couldn't load videos. Check your connection and try again." | Inline retry control re-runs the fetch |
| Mux fetch fails, cache exists | Non-blocking inline indicator | "Showing saved content — refresh failed." | Next successful background refresh clears it silently |
| Mux returns zero `ready` assets | Empty state (VOD grid only) | "No videos available." | None — resolves once Mux assets exist; not a user-correctable error |
| Live stream fails to load (post-tap, in Live Player) | Out of scope here | — | Covered in `specs/features/live-player/requirements.md` |

---

## Dependencies

| Dependency | Type | Notes |
|---|---|---|
| Room database, `Video` entity | Internal | Entity schema defined in `data-model.md`; `VideoDao.deleteStale` must support excluding seeded/static rows by type or origin flag |
| Mux API credentials (Token ID + Secret) | Internal | `GET /video/v1/assets` requires Basic Auth. The secret is embedded in the Android client — an accepted risk for this non-distributed project; stored via `local.properties`/`BuildConfig`, never committed, consistent with `ARCHITECTURE.md`'s existing "no secrets committed" rule |
| Settings environment flag | Internal | Phase 1 "Local" environment is inert — Catalog always sources VOD from Mux regardless of the Settings picker state |
| Static live config source | Internal | The 3 `Video` rows in RQ-CAT-20 live in `core` static config; UUIDs are generated once at runtime, on first app launch, and must remain stable across app updates thereafter |
| Design tokens: poster aspect ratio, LIVE badge color | Design | Defined in `design.md` (broadcast red for Live badge per the locked color palette) |

---

## Platform: Fire TV (Phase 3)

- **Layout:** Catalog's Live carousel and VOD grid are restructured for 10-foot UI using Compose for TV, with d-pad focus order replacing touch taps as the primary input.
- **Top app bar:** Replaced by a side navigation rail or equivalent Fire TV navigation pattern — not a top bar with a gear icon.
- **Data layer:** Unchanged — the same `core` module Mux sync, Room cache, and static live seeding (RQ-CAT-17–21) are reused as-is.
- **Navigation:** The same VOD-to-Player / Live-to-Live Player split (RQ-CAT-06, RQ-CAT-09) holds, triggered by remote "select" rather than touch.
- **Performance budget:** NFR-PE-02's 2-second render budget is re-evaluated for Fire TV hardware at Phase 3 kickoff — not assumed to carry over unchanged.

Full Fire TV spec is defined in `specs/features/fire-tv/requirements.md` at Phase 3 kickoff.

---

## Changelog

| Version | Date | Author | Notes |
|---|---|---|---|
| 0.1.0 | 2026-06-28 | Danielle Mariani | Initial draft. Mux `List Assets` confirmed as the real Phase 1 VOD source. Surfaced two items not previously captured elsewhere: the `status == "ready"` filter (BR-CAT-04) and the Mux secret-in-client dependency |
| 0.2.0 | 2026-06-28 | Danielle Mariani | Added top app bar requirements (RQ-CAT-01–03, AC-CAT-09). Simplified VOD empty state to neutral message (RQ-CAT-13). Updated static live seeding to use UUIDs as `Video.id` (RQ-CAT-20). Clarified BR-CAT-04 promotion to `data-model.md`. Removed template note on ID abbreviation length |
| 0.2.1 | 2026-07-07 | Danielle Mariani | Corrected RQ-CAT-20's UUID table and the "Static live config source" Dependencies row — both said UUIDs are "assigned at implementation" (hardcoded by the developer at coding time). Revised during TSK-CAT-12/16 implementation: the UUIDs are now generated with a real random-UUID call exactly once, on the device's genuine first app launch (detected via `VideoDao.getLiveIds()` being empty), rather than hardcoded as source constants. Reworded both to "assigned at runtime, on first app launch" |
| 0.2.2 | 2026-07-10 | Danielle Mariani | Resolved RQ-CAT-19: `title`/`description` are now sourced from the Mux asset's `passthrough` JSON metadata (set at upload time), falling back to the asset `id`/`null` only when `passthrough` is missing or malformed — previously the id-fallback was the unconditional default. Updated RQ-CAT-18's field-mapping list to include the new `passthrough` source. Narrowed the Out of Scope line to reflect that the naming convention is now specified here, not just tracked externally; test-asset *selection* remains out of scope (`content-catalog.md` Open Question #2). Implementation tracked in `catalog_tasks.md` TSK-CAT-35/TSK-CAT-36, since TSK-CAT-08/TSK-CAT-13 were already `Done` when this was resolved |