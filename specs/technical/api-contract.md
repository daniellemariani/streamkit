# API Contract — StreamKit

**Version:** 0.1.0
**Status:** Draft
**Owner:** Danielle Mariani
**Created at:** 2026-06-20
**Last Updated:** 2026-06-20

---

## Overview

This document is the canonical API contract for the StreamKit backend (FastAPI). It defines all endpoints, request/response schemas, and validation rules for Phase 4 (Video Ingestion Pipeline), Phase 5 (DRM & Content Protection), and Phase 6 (Player Telemetry & QoE).

This document owns the **transport layer** — the wire format exchanged between the Android client (`app` and `tv` modules, via the shared `core` module) and the backend. Transport schemas defined here are distinct from the canonical database schema in `specs/technical/data-model.md`. They look similar by design, but are independently versioned and may diverge as the API evolves — for example, the backend `videos` table includes a `source` column that the client never needs to see.

StreamKit has no authentication, no user accounts, and no multi-tenancy (SPEC.md, "User Roles" — single-user personal application). Every endpoint in this document is unauthenticated and operates on a single implicit user's data. This is appropriate only because the backend runs locally during development (ARCHITECTURE.md, "Runtime: Local only") and is never exposed beyond `localhost`.

The backend does not exist before Phase 4. Phases 1–3 are Android-only, with the Mux API and NASA TV's public HLS stream as the only network dependencies — there is no StreamKit-owned backend for those phases, and consequently nothing in this document applies until Phase 4 begins.

---

## Related Documents

| Document | Purpose |
|---|---|
| SPEC.md | Global business rules and feature index |
| ARCHITECTURE.md | Stack decisions, backend layering, API design conventions |
| specs/technical/data-model.md | Canonical database schema — source of truth for field definitions |
| specs/features/ingestion-pipeline/requirements.md | Full Phase 4 feature spec |
| specs/features/drm/requirements.md | Full Phase 5 feature spec |
| specs/features/telemetry/requirements.md | Full Phase 6 feature spec |

---

## Design Conventions

These conventions apply globally to every endpoint. Individual endpoint definitions do not repeat them.

### Base URL and Versioning

All endpoints are versioned under `/api/v1/`. See the **Versioning Strategy** section for compatibility guarantees.

```
http://localhost:8000/api/v1/
```

StreamKit's backend is local-only (ARCHITECTURE.md). There is no staging or production host — `localhost` (or the dev machine's LAN IP, for testing from a physical Fire TV device) is the only environment.

### Naming

- Endpoint paths are lowercase hyphenated (e.g. `/ingest-jobs`, not `/ingestJobs`)
- Resource names are plural nouns (e.g. `/videos`, `/telemetry-events`)
- Sub-resources are nested only when the parent ID is required for context (e.g. `/videos/{video_id}/renditions`)

### Request Format

- All request bodies are JSON (`Content-Type: application/json`), except the video upload endpoint, which uses `multipart/form-data`
- Query parameters are URL-encoded, used on `GET` requests only — `POST` and `PATCH` use JSON request bodies

### Response Format

- All responses are JSON
- HTTP status codes are used semantically (see Error Reference)
- All timestamps are Unix epoch **milliseconds** (integers, UTC) — matching the `BIGINT` millisecond convention in `data-model.md`, not seconds
- There are no monetary fields in this API

### Response Envelope

All responses share a consistent envelope. The client always reads from `data`.

**Single resource:**
```json
{
  "data": { "...resource object..." },
  "meta": {
    "request_id": "string (UUID — for tracing and debugging)",
    "timestamp": "integer (Unix UTC ms — when the server processed the request)"
  }
}
```

**List resource:**
```json
{
  "data": ["array of resource objects"],
  "pagination": { "...PaginationSchema..." },
  "meta": {
    "request_id": "string (UUID)",
    "timestamp": "integer (Unix UTC ms)"
  }
}
```

`meta` is present on every response and is never null.

### PATCH Semantics

The one `PATCH` endpoint in this API (`IngestJob` status transitions) follows these rules:

- **Field omitted** → property is unchanged
- **Field present with a value** → property is updated to that value
- Sending `null` on a non-nullable field returns `400 VALIDATION_ERROR`

### Deletes

StreamKit's API uses **hard deletes only** — there is no soft-delete concept anywhere in this contract. This is a deliberate departure from typical multi-user API conventions: there is no sync layer, no multi-client consistency to protect, and no audit requirement for a single-user local learning project. `data-model.md` does not define a `deleted_at` column on any table, and this document does not invent one. The one exception is `TelemetryEvent`, which is append-only and not deletable via the API at all (ARCHITECTURE.md, Data Integrity).

### Pagination

All list endpoints use offset-based pagination.

```json
{
  "page": "integer (1-based)",
  "page_size": "integer",
  "total": "integer",
  "has_next": "boolean"
}
```

---

## Versioning Strategy

### Current Version

The API is currently at `v1`. All endpoints live under `/api/v1/`.

### Backward Compatibility

Within `v1`, all changes must be **additive and non-breaking**:

- New optional fields may be added to request or response schemas
- New endpoints may be added
- Existing required fields must not be removed or renamed
- Existing response field types must not change
- Enum values must not be removed; new values may be added and the client must handle unknown enum values gracefully

### Breaking Changes

Breaking changes require a new API version (`v2`). A breaking change is any change that would cause the existing Android client to malfunction if not updated, including: removing or renaming a required field, changing a field type, changing HTTP method or status codes, removing an endpoint.

In practice, for a single-developer, single-client project, a `v2` migration just means updating the Android `NetworkModule` client calls in the same change as the backend route — there's no external client population to coordinate with. The versioning discipline is kept anyway, since it mirrors how Disney+/Netflix/Hulu-scale teams operate, which is part of the point of this project (PRODUCT.md, Project Goals).

---

## Shared Schemas

### Error Schema

```json
{
  "error": {
    "code": "string (e.g. VALIDATION_ERROR, NOT_FOUND, INGEST_NOT_COMPLETE)",
    "message": "string (human-readable)",
    "details": "object | null (field-level validation errors)"
  },
  "meta": {
    "request_id": "string",
    "timestamp": "integer"
  }
}
```

This matches the consistent error response schema specified in ARCHITECTURE.md's Cross-Cutting Concerns section.

### Error Reference

| HTTP Status | Code | When used |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Request body fails validation |
| 404 | `NOT_FOUND` | Resource does not exist |
| 409 | `CONFLICT` | Unique constraint violation (e.g. duplicate `IngestJob` for a video already `PROCESSING`) |
| 422 | `INGEST_NOT_COMPLETE` | Playback or rendition data requested for a video whose `IngestJob.status` is not `COMPLETE` (BR-ING-03) |
| 422 | `INVALID_STATUS_TRANSITION` | Attempted `IngestJob` status change violates the one-directional state machine (ARCHITECTURE.md, Data Integrity) |
| 422 | `LICENSE_DENIED` | DRM license request rejected by the upstream license server (Phase 5) |
| 500 | `INTERNAL_ERROR` | Unexpected server error, including FFmpeg/Shaka Packager subprocess failures not otherwise classified |

---

## Phase 4 — Ingestion Pipeline API

Backed by `videos`, `video_renditions`, and `ingest_jobs` in `data-model.md`. Implements BR-ING-01, BR-ING-02, BR-ING-03, and NFR-RE-02.

### Video Canonical Object

The canonical `Video` object, as returned to the client (note: this is the **transport** shape, not the raw `videos` table — `source` is included because the catalog screen may eventually want to badge ingested-vs-Mux content, but no other internal column is exposed):

```json
{
  "id": "string",
  "title": "string",
  "description": "string | null",
  "type": "string (VOD | LIVE)",
  "thumbnail_url": "string | null",
  "stream_url": "string",
  "duration_seconds": "integer | null",
  "is_drm_protected": "boolean",
  "source": "string (MUX | LOCAL | STATIC)",
  "created_at": "integer (Unix UTC ms)",
  "updated_at": "integer (Unix UTC ms)"
}
```

---

#### GET /api/v1/videos

**Purpose:** List all locally-ingested videos available in the catalog. Mux-sourced and static (NASA TV) entries are not stored or served by this backend in Phase 1–3 — the Android client fetches those directly from the Mux API and from static config. This endpoint exists to extend the catalog with `LOCAL` content starting in Phase 4.

**Query parameters:**
- `page` (integer, default 1)
- `page_size` (integer, default 20, max 100)
- `type` (string, optional) — filter by `VOD` or `LIVE`

**Filtering behavior:** only videos whose most recent `IngestJob.status` is `COMPLETE` are returned (BR-ING-03). A video stuck in `PENDING`, `PROCESSING`, or `FAILED` is invisible to this endpoint — there is no `include_incomplete` override, by design, to guarantee the client never renders a half-ingested video.

**Sort order:** `created_at` descending (most recently ingested first).

**Response schema:**
```json
{
  "data": ["array of canonical Video objects"],
  "pagination": { "...PaginationSchema..." },
  "meta": { "...Metadata..." }
}
```

---

#### GET /api/v1/videos/{video_id}

**Purpose:** Retrieve a single ingested video.

**Response schema:**
```json
{
  "data": { "...canonical Video object..." },
  "meta": { "...Metadata..." }
}
```

HTTP 404 if the video does not exist. **HTTP 422 `INGEST_NOT_COMPLETE`** if the video exists but its `IngestJob.status` is not `COMPLETE` — distinct from 404 so the client (and you, debugging) can tell "doesn't exist" apart from "exists but isn't ready yet."

---

#### POST /api/v1/videos/{video_id}/ingest

**Purpose:** Upload a raw video file and create an `IngestJob` to transcode and package it. This is the entry point to the pipeline described in ARCHITECTURE.md's `ingest_service.py`.

**Request:** `multipart/form-data`
- `file` (binary, required) — the raw video file
- `title` (string, required)
- `description` (string, optional)

**Behavior:**
1. The server creates a `Video` row with `source = 'LOCAL'` and an `ingest_jobs` row with `status = 'PENDING'`.
2. The uploaded file is written to disk; `ingest_jobs.input_file_path` is set.
3. The response returns immediately with the created `IngestJob` — transcoding happens asynchronously. The client polls `GET /api/v1/ingest-jobs/{job_id}` for status.

**Response schema:**
```json
{
  "data": { "...canonical IngestJob object, see below..." },
  "meta": { "...Metadata..." }
}
```

HTTP 202 Accepted (not 201 — the `Video` resource is created, but it is not yet usable; 202 signals "accepted for processing").

**Validation rules:**
- `file` must be present and a supported container/codec combination FFmpeg can read; unsupported input returns `400 VALIDATION_ERROR`
- `title` must be non-empty

**Business rule references:** BR-ING-01, BR-ING-02, BR-ING-03

---

#### DELETE /api/v1/videos/{video_id}

**Purpose:** Hard-delete a locally-ingested video and its associated renditions and ingest job history.

**Behavior:** Deletes the `Video` row, cascades to `video_renditions` and `ingest_jobs` for that `video_id`, and removes the packaged HLS output from disk. This is a real delete, not a soft delete — see the global Deletes convention above.

**Response schema:** HTTP 204 No Content.

**Note:** Mux-sourced and static (NASA TV) entries cannot be deleted via this endpoint at all — return `400 VALIDATION_ERROR` if attempted against a `video_id` whose `source` is not `LOCAL`. Deletion only makes sense for content this backend actually owns on disk.

---

### IngestJob Canonical Object

```json
{
  "id": "string (UUID)",
  "video_id": "string",
  "status": "string (PENDING | PROCESSING | COMPLETE | FAILED)",
  "output_dir": "string | null",
  "error_message": "string | null",
  "created_at": "integer (Unix UTC ms)",
  "updated_at": "integer (Unix UTC ms)"
}
```

Note: `input_file_path` from the database schema is intentionally **not** exposed in the transport object — it's a server-local filesystem path with no meaning to the client and no reason to leak.

---

#### GET /api/v1/ingest-jobs/{job_id}

**Purpose:** Poll the status of an ingestion job. This is the primary mechanism the client uses to learn when a video becomes playable.

**Response schema:**
```json
{
  "data": { "...canonical IngestJob object..." },
  "meta": { "...Metadata..." }
}
```

HTTP 404 if the job does not exist.

---

#### GET /api/v1/videos/{video_id}/ingest-jobs

**Purpose:** List the full ingestion history for a video. Useful when a video has been re-ingested after a failure (`data-model.md` notes that multiple `IngestJob` rows can reference the same `video_id`).

**Sort order:** `created_at` descending (most recent attempt first).

**Response schema:**
```json
{
  "data": ["array of canonical IngestJob objects"],
  "pagination": { "...PaginationSchema..." },
  "meta": { "...Metadata..." }
}
```

---

#### PATCH /api/v1/ingest-jobs/{job_id}

**Purpose:** Internal-use endpoint for the ingestion service itself to advance a job's status as FFmpeg/Shaka Packager subprocess stages complete. **Not called by the Android client** — included here because it is still part of the backend's HTTP surface (used by the pipeline's own worker process) and documenting it prevents someone six months from now wondering where status transitions happen.

**Request schema:**
```json
{
  "status": "string (PROCESSING | COMPLETE | FAILED)",
  "output_dir": "string (required if status = COMPLETE)",
  "error_message": "string (required if status = FAILED)"
}
```

**Immutable fields:** `video_id`, `id` — never change after creation.

**Validation rules:**
- The only legal transitions are `PENDING → PROCESSING`, `PROCESSING → COMPLETE`, and `PROCESSING → FAILED`. Any other transition (including re-entering `PENDING`, or moving `COMPLETE`/`FAILED` anywhere) returns **`422 INVALID_STATUS_TRANSITION`** (ARCHITECTURE.md, Data Integrity — status transitions are one-directional).
- `status = COMPLETE` requires a valid, non-empty `output_dir` containing a verified HLS manifest. If the manifest is missing or malformed, the server rejects the transition with `400 VALIDATION_ERROR` rather than allowing a job to report `COMPLETE` with broken output (NFR-RE-02, BR-ING-03 — no partial/corrupt output is ever exposed for playback).

**Response schema:**
```json
{
  "data": { "...updated IngestJob object..." },
  "meta": { "...Metadata..." }
}
```

**Business rule references:** BR-ING-03, NFR-RE-02

---

### VideoRendition Canonical Object

```json
{
  "id": "string (UUID)",
  "video_id": "string",
  "resolution": "string (e.g. 1080p)",
  "width_px": "integer",
  "height_px": "integer",
  "bitrate_kbps": "integer",
  "codec": "string",
  "segment_duration_seconds": "integer",
  "manifest_url": "string"
}
```

---

#### GET /api/v1/videos/{video_id}/renditions

**Purpose:** List the ABR renditions produced for a video. Primarily a debugging/verification endpoint — ExoPlayer never calls this directly, it reads the master manifest referenced by `Video.stream_url` (per `data-model.md`'s note that this table exists "for backend inspection and pipeline verification," not client consumption).

**Response schema:**
```json
{
  "data": ["array of canonical VideoRendition objects"],
  "meta": { "...Metadata..." }
}
```

HTTP 422 `INGEST_NOT_COMPLETE` if the video's ingest job hasn't finished — renditions may exist mid-pipeline but are not meaningful to expose until the job is `COMPLETE`.

**Business rule references:** BR-ING-01 (minimum three renditions)

---

## Phase 5 — DRM API

Backed by the DRM license proxy described in ARCHITECTURE.md (`drm_service.py`, `drm.py` routes). Implements BR-DRM-01, BR-DRM-02, BR-DRM-03.

There is no new database table for DRM in `data-model.md` — license proxying is stateless request/response, not persisted. This section is transport-only.

### Why a proxy at all

Per ARCHITECTURE.md's Security section: Widevine license requests are proxied through the backend specifically so license server credentials never live in the Android client. The endpoint below is a pass-through, not a new domain concept.

---

#### POST /api/v1/drm/license

**Purpose:** Proxy a Widevine license request from the ExoPlayer `MediaDrmCallback` on the client to the upstream test license server (Shaka or Axinom — see ARCHITECTURE.md's `WidevineManager`/`LicenseRequestHandler`), without the client ever holding the upstream server's credentials.

**Request:** `application/octet-stream` (the raw Widevine license challenge generated by ExoPlayer — this is binary, not JSON, which is why this one endpoint breaks the "all requests are JSON" convention stated above)

**Query parameters:**
- `video_id` (string, required) — identifies which video's DRM config to use when forwarding to the upstream license server

**Behavior:** the server attaches the appropriate upstream credentials, forwards the binary challenge to the configured test license server, and streams the binary license response back unmodified. License acquisition must complete transparently before playback begins — the client never surfaces a separate "requesting license" step to the user (BR-DRM-02).

**Response:** `application/octet-stream` (the raw Widevine license response) on success.

**Error response:** if the upstream license server rejects the request, the server returns **`422 LICENSE_DENIED`** using the standard JSON error envelope (not binary) so the client can map it to BR-DRM-03's "clear error message" requirement, distinct from a transport failure.

**Validation rules:**
- `video_id` must reference a video with `is_drm_protected = true`; requesting a license for unprotected content returns `400 VALIDATION_ERROR`

**Business rule references:** BR-DRM-01, BR-DRM-02, BR-DRM-03

---

## Phase 6 — Telemetry API

Backed by `playback_sessions` and `telemetry_events` in `data-model.md`. Implements BR-TEL-01, BR-TEL-02, BR-TEL-03, and the cross-device resume behavior resolved in `data-model.md` v0.1.1.

### PlaybackSession Canonical Object

```json
{
  "id": "string (UUID)",
  "video_id": "string",
  "device_type": "string (MOBILE | TV)",
  "started_at": "integer (Unix UTC ms)",
  "ended_at": "integer | null",
  "last_position_ms": "integer"
}
```

---

#### POST /api/v1/playback-sessions

**Purpose:** Create a new playback session when the player starts a video. Called once per playback start, from both `app` and `tv` modules via the shared `core` `TelemetryCollector`.

**Request schema:**
```json
{
  "video_id": "string (required)",
  "device_type": "string (MOBILE | TV, required)"
}
```

**Response schema:**
```json
{
  "data": { "...created PlaybackSession object..." },
  "meta": { "...Metadata..." }
}
```

HTTP 201. The returned `id` is the `session_id` the client attaches to every subsequent `TelemetryEvent` for this playback (per `data-model.md`'s cross-platform ID strategy).

**Offline behavior:** if the backend is unreachable when playback starts, the client generates a local UUID to use as `session_id` and reconciles it with the backend once connectivity returns, per `data-model.md`'s note on `TelemetryEventEntity.sessionId`. Reconciliation is a client-side concern (matching on the locally-generated UUID when this endpoint is finally reachable) — no separate reconciliation endpoint exists; the client simply calls this endpoint late and uses the returned canonical `id` going forward.

---

#### PATCH /api/v1/playback-sessions/{session_id}

**Purpose:** Update `last_position_ms`, or close out the session by setting `ended_at`.

**Request schema:**
```json
{
  "last_position_ms": "integer (optional)",
  "ended_at": "integer (Unix UTC ms, optional)"
}
```

**Immutable fields:** `video_id`, `device_type`, `started_at` — a session's origin never changes after creation.

**Response schema:**
```json
{
  "data": { "...updated PlaybackSession object..." },
  "meta": { "...Metadata..." }
}
```

---

#### GET /api/v1/videos/{video_id}/resume

**Purpose:** Cross-device resume lookup (Phase 6). Returns the most recent playback position for a video **across all device types** — this is the endpoint that lets a session started on the phone resume on Fire TV, per `data-model.md`'s resolution that resume is video-scoped, not device-scoped, and that `device_type` is a telemetry dimension only.

**Behavior:** queries `playback_sessions` for the given `video_id`, ordered by `updated_at` descending, and returns the single most recent row regardless of which `device_type` it was recorded on.

**Response schema:**
```json
{
  "data": {
    "session_id": "string (UUID)",
    "last_position_ms": "integer",
    "device_type": "string (MOBILE | TV, informational only — not used to filter the lookup)",
    "updated_at": "integer (Unix UTC ms)"
  },
  "meta": { "...Metadata..." }
}
```

HTTP 404 if no session exists yet for this video (i.e. it has never been played) — the client treats this as "start from 0:00," same as the Phase 1 local-only behavior.

---

### TelemetryEvent Canonical Object

```json
{
  "id": "string (UUID — client-generated)",
  "session_id": "string (UUID)",
  "video_id": "string",
  "device_type": "string (MOBILE | TV)",
  "event_type": "string (see TelemetryEventType reference in data-model.md)",
  "timestamp_ms": "integer (Unix UTC ms)",
  "bitrate_kbps": "integer | null",
  "resolution": "string | null",
  "buffer_health_ms": "integer | null",
  "error_code": "string | null"
}
```

---

#### POST /api/v1/telemetry-events

**Purpose:** Flush a batch of buffered telemetry events from the client's Room `telemetry_events` table to the backend. This is the one endpoint in this API designed for offline resilience — it's where the `TelemetryDispatcher`'s flush-on-reconnect behavior lands (ARCHITECTURE.md, Local Storage).

**Request schema:**
```json
{
  "events": [
    { "...TelemetryEvent canonical object, client-generated id included..." }
  ]
}
```

A single request may include any number of events up to `page_size`'s max (100) — the client should chunk larger buffers into multiple calls rather than sending an unbounded batch.

**Behavior:** for each event, the server performs `INSERT ... ON CONFLICT DO NOTHING` keyed on `id` — re-delivering an already-accepted event (e.g. after a retried request) is a no-op, not an error. This is what makes the client-generated UUID strategy from `data-model.md` actually idempotent end-to-end.

**Response schema:**
```json
{
  "data": {
    "accepted": ["array of event ids that were newly inserted"],
    "duplicates": ["array of event ids that already existed — not an error, just informational"],
    "rejected": [
      { "id": "string", "error": "string" }
    ]
  },
  "meta": { "...Metadata..." }
}
```

HTTP 200 even if some events are rejected — the response body, not the status code, carries per-event outcomes, since a batch is rarely all-or-nothing. The client uses `accepted` plus `duplicates` together to decide which local rows to mark `synced = true` (anything not in `rejected` is safe to mark synced).

**Validation rules:**
- Each event's `session_id` must reference an existing `PlaybackSession`; an event for an unknown session is rejected (not a 4xx for the whole batch — it lands in `rejected` for that one event)
- `event_type` must be one of the values in the `TelemetryEventType` reference; unrecognized values are rejected per-event, not fatal to the batch, since BR-TEL-02 ("event collection must not degrade playback performance") implies the client should never block on a single bad event invalidating an entire flush

**Business rule references:** BR-TEL-01, BR-TEL-02

---

#### GET /api/v1/sessions/{session_id}/qoe-summary

**Purpose:** Aggregate QoE metrics for a single session — the data source for the Phase 6 dashboard's per-session view. Implements the three minimum metrics required by BR-TEL-03.

**Response schema:**
```json
{
  "data": {
    "session_id": "string (UUID)",
    "video_id": "string",
    "device_type": "string (MOBILE | TV)",
    "startup_time_ms": "integer | null",
    "rebuffer_rate": "number (0.0–1.0, fraction of session duration spent stalled)",
    "bitrate_switch_count": "integer",
    "error_count": "integer"
  },
  "meta": { "...Metadata..." }
}
```

**Computation notes** (mirrors `data-model.md`'s dashboard aggregation note):
- `startup_time_ms` — elapsed time between session `started_at` and the first `PLAYBACK_STARTED` event's `timestamp_ms`; `null` if playback never started
- `rebuffer_rate` — total duration between paired `BUFFER_STALL`/`BUFFER_RECOVERED` events, divided by total session duration (`ended_at - started_at`, or now minus `started_at` if still active)
- `bitrate_switch_count` — count of `BITRATE_CHANGE` events for the session
- `error_count` — count of `ERROR` events for the session

**Business rule references:** BR-TEL-03

---

## Security Considerations

### HTTPS

StreamKit's backend runs locally over plain HTTP during development (ARCHITECTURE.md, "Runtime: Local only"). This is a deliberate departure from SPEC.md's NFR-CO-03 ("all streams must be delivered over HTTPS") — that requirement governs the **streaming CDN paths** (Mux, NASA TV, and the locally-packaged HLS output once served), not the local control-plane API defined in this document. If StreamKit is ever deployed beyond local development, this API would need TLS termination added before that happened — flagged here so the gap isn't silently inherited from this contract.

### No Authentication

There is no JWT, no session cookie, no API key. Every endpoint in this contract is open to anything that can reach `localhost:8000`. This is acceptable only under the explicit assumption that the backend never leaves the developer's machine (ARCHITECTURE.md, Security: "no authentication — local development only").

### DRM Credential Isolation

The one place this API does carry a real secret-handling concern: the upstream Widevine test license server's credentials live only in the backend's environment configuration (`.env`, gitignored per ARCHITECTURE.md), never in the Android client and never echoed back in any response body, including error responses.

### Input Validation

All incoming request bodies are validated against the schemas defined in this document before any database operation or subprocess invocation (FFmpeg, Shaka Packager) is performed. Validation failures return `400 VALIDATION_ERROR` with field-level detail in `error.details`.

---

## Open Questions

- **Resumable/chunked upload for `POST /api/v1/videos/{video_id}/ingest`** — the current contract assumes a single `multipart/form-data` upload completes in one request. For larger source files this may need to become a presigned-chunk or resumable upload flow. Deferred until Phase 4 implementation surfaces a real file-size pain point.
- **`qoe-summary` aggregation cost** — computing rebuffer rate by pairing `BUFFER_STALL`/`BUFFER_RECOVERED` events on every request may be fine at personal-project scale but could warrant a materialized/cached summary if the telemetry table grows. Deferred to Phase 6 kickoff.
- **DRM license response caching** — whether the backend should cache short-lived license responses to reduce load on the upstream test server, or always proxy fresh. Deferred to Phase 5 kickoff.
- **Local network exposure for Fire TV testing** — since the backend is `localhost`-only but Fire TV is a separate physical device, Phase 5/6 testing requires the backend to be reachable over the LAN. This contract doesn't currently address binding to `0.0.0.0` vs `127.0.0.1` or any minimal LAN-level access control. Worth a decision before Phase 5 hardware testing begins.

---

## Changelog

| Version | Date | Author | Notes |
|---|---|---|---|
| 0.1.0 | 2026-06-20 | Danielle Mariani | Initial draft — covers Phase 4 (ingestion), Phase 5 (DRM), and Phase 6 (telemetry) APIs. No auth/tenancy (single-user, local-only). No sync API (no offline sync model in StreamKit). |