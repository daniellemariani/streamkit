# data-model.md — StreamKit

**Version:** 0.1.3
**Status:** Draft
**Owner:** Danielle Mariani
**Created at:** 2026-06-16
**Last Updated:** 2026-06-20
**Location:** specs/technical/data-model.md

---

## Overview

This document defines the complete data schema for StreamKit across two persistence layers:

- **Android (Room)** — local SQLite database used for catalog caching, playback position persistence, and telemetry event buffering
- **Backend (PostgreSQL)** — server-side database used for catalog management, ingestion pipeline state, and telemetry aggregation

Entities that exist in both layers share the same logical identity but differ in fields and purpose. The Android Room schema is optimized for local caching and offline resilience. The PostgreSQL schema is the source of truth for ingestion and telemetry.

All timestamps are stored as Unix epoch milliseconds (UTC):
- Android Room: `Long` (Kotlin)
- PostgreSQL: `BIGINT`

---

## Entity Index

| Entity | Android (Room) | Backend (PostgreSQL) | Phase Introduced |
|---|---|---|---|
| `Video` | ✅ | ✅ | 1 (Android) / 4 (Backend) |
| `PlaybackSession` | ✅ (position only) | ✅ (full session) | 1 (Android) / 6 (Backend) |
| `VideoRendition` | — | ✅ | 4 (Backend only) |
| `IngestJob` | — | ✅ | 4 (Backend only) |
| `TelemetryEvent` | ✅ (buffer) | ✅ (sink) | 6 |

**Reading this table:** Phase 1 is Android-only — there is no backend service running at all in Phase 1. The `videos` and `playback_sessions` PostgreSQL tables are listed here for completeness but aren't created until their respective backend phases (`videos` at Phase 4 when the backend project is scaffolded; `playback_sessions` writes begin at Phase 6). Until then, Android Room is the only persistence layer that exists.

---

## Entities

---

### `Video`

Represents a single catalog entry — either a VOD asset or a live stream.

**Phase introduced:** 1

**Android source:** Populated from the Mux API (VOD) or static config (Red Bull TV's 24/7 "Best of Red Bull" live stream). Serves as the local catalog cache.

**Backend source:** Created on ingestion (Phase 4) or managed as a static record for Mux-sourced and live content.

---

#### Android — Room Entity

```kotlin
@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val id: String,                   // Mux asset ID or "redbull_tv" for the live stream
    val title: String,
    val description: String?,
    val type: String,                             // "VOD" | "LIVE"
    val thumbnailUrl: String?,
    val streamUrl: String,                        // HLS/DASH manifest URL
    val durationSeconds: Int?,                    // null for live streams
    val isDrmProtected: Boolean,                  // false for Phase 1; true for Phase 5 DRM content
    val createdAt: Long,                          // Unix epoch ms, UTC
    val updatedAt: Long                           // Unix epoch ms, UTC
)
```

**Notes:**
- `id` for Mux VOD assets is the Mux asset ID (e.g. `"abc123xyz"`). The Red Bull TV live stream uses the static key `"redbull_tv"`.
- `streamUrl` is the Mux playback URL (VOD) or `"https://rbmn-live.akamaized.net/hls/live/590964/BoRB-AT/master.m3u8"` (Red Bull TV's "Best of Red Bull" 24/7 curated stream). **Unverified:** this URL resolves with the correct `application/x-mpegURL` content type and the channel ID is referenced consistently across multiple third-party HLS aggregators, but manual playback testing did not produce video. The Akamai edge for this host has been observed returning `X-GeoBlock: true`, so geo-restriction is the suspected cause, but this is unconfirmed. **Do not treat this URL as production-ready** — verify actual playback (e.g. via VLC or ExoPlayer directly, from the target network) before wiring it into Phase 1 implementation. See Open Schema Questions below.
- `isDrmProtected` is always `false` through Phase 4. Set to `true` for Widevine-protected assets introduced in Phase 5.
- The catalog is refreshed from the Mux API on each app launch. The Room cache is the read source for the catalog screen.
- `durationSeconds` is `null` for live streams. The UI infers live/VOD display logic from `type`, not from null duration.

---

#### Backend — PostgreSQL Table

```sql
CREATE TABLE videos (
    id              VARCHAR(64) PRIMARY KEY,      -- Mux asset ID or "redbull_tv"
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    type            VARCHAR(8) NOT NULL,          -- 'VOD' | 'LIVE'
    thumbnail_url   TEXT,
    stream_url      TEXT NOT NULL,
    duration_seconds INTEGER,                     -- NULL for LIVE
    is_drm_protected BOOLEAN NOT NULL DEFAULT FALSE,
    source          VARCHAR(16) NOT NULL,         -- 'MUX' | 'LOCAL' | 'STATIC'
    created_at      BIGINT NOT NULL,
    updated_at      BIGINT NOT NULL
);
```

**Notes:**
- `source` distinguishes the origin of the video record: `'MUX'` (Mux API VOD), `'LOCAL'` (ingested via the Phase 4 pipeline), or `'STATIC'` (Red Bull TV live stream, seeded at startup).
- Backend `Video` records for Phase 4 ingest-originated content reference their renditions via the `VideoRendition` table.
- `stream_url` for locally ingested content points to the packaged HLS manifest served by the backend (Phase 4+).

---

#### `VideoType` Enum

```kotlin
// Android
enum class VideoType { VOD, LIVE }
```

```sql
-- PostgreSQL: enforced via CHECK constraint
ALTER TABLE videos ADD CONSTRAINT chk_video_type CHECK (type IN ('VOD', 'LIVE'));
ALTER TABLE videos ADD CONSTRAINT chk_video_source CHECK (source IN ('MUX', 'LOCAL', 'STATIC'));
```

---

### `PlaybackSession`

Tracks a single playback session — the device, the video being played, start/end time, and the last known position.

**Phase introduced:** 1

---

#### Android — Room Entity

The Android Room layer stores only the **last known playback position** per video, not a full session history. One row per `videoId`, upserted every 5 seconds during active playback. This enables resume-from-position without requiring a backend connection in Phase 1.

```kotlin
@Entity(tableName = "playback_positions")
data class PlaybackPositionEntity(
    @PrimaryKey val videoId: String,              // FK → videos.id
    val lastPositionMs: Long,                     // Playback position in milliseconds
    val updatedAt: Long                           // Unix epoch ms, UTC — time of last position write
)
```

**Notes:**
- This is not a session log — it is a single resumption record. Only one row exists per `videoId`, and it is replaced (not appended) on each update.
- Written every 5 seconds during active playback (ARCHITECTURE.md requirement).
- On app launch, the player reads this row to offer resume. If no row exists, playback starts from 0:00.
- Not used for live streams — live playback always starts at the live edge (or the DVR boundary on resume, which is computed from the manifest, not from a stored position).

---

#### Backend — PostgreSQL Table

The backend `playback_sessions` table is the full session record — one row per session start. Used for cross-device resume (Phase 6) and telemetry correlation.

```sql
CREATE TABLE playback_sessions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    video_id        VARCHAR(64) NOT NULL REFERENCES videos(id),
    device_type     VARCHAR(8) NOT NULL,          -- 'MOBILE' | 'TV'
    started_at      BIGINT NOT NULL,              -- Unix epoch ms, UTC
    ended_at        BIGINT,                       -- NULL while session is active
    last_position_ms BIGINT NOT NULL DEFAULT 0,  -- Last known position, updated periodically
    created_at      BIGINT NOT NULL,
    updated_at      BIGINT NOT NULL
);

ALTER TABLE playback_sessions ADD CONSTRAINT chk_device_type CHECK (device_type IN ('MOBILE', 'TV'));
```

**Notes:**
- `ended_at` is `NULL` while the session is in progress. It is set when the user leaves the player screen or the app is backgrounded beyond PiP.
- `last_position_ms` is updated periodically from the client (Phase 6). In Phase 1, this table is not written to from Android — it is populated starting in Phase 6.
- `device_type` is a **telemetry/analytics dimension only** — it records which device a session happened on for QoE segmentation (e.g. "rebuffer rate on TV vs mobile"). It is **not** used to scope resume lookups.
- **Cross-device resume (Phase 6):** resume is scoped by `video_id`, not by `device_type`. To resume "where I left off" on any device — phone or Fire TV — the client queries for the most recent `playback_sessions` row for that `video_id` across all device types, ordered by `updated_at`. This resolves Open Question #4 in CONTEXT.md: Phase 6 does persist position to the backend, and resume is video-scoped so a session started on mobile can be resumed on TV (and vice versa), matching the Netflix-style behavior.
- A `TelemetryEvent` row references `session_id` from this table (Phase 6).

---

#### `DeviceType` Enum

```kotlin
// Android
enum class DeviceType { MOBILE, TV }
```

---

### `VideoRendition`

Describes a single ABR quality rendition of a video asset — one row per rendition per video. Created by the ingestion pipeline after transcoding.

**Phase introduced:** 4

**Platform:** Backend (PostgreSQL) only. ExoPlayer reads rendition information from the HLS manifest directly — the Android client does not query this table.

---

#### Backend — PostgreSQL Table

```sql
CREATE TABLE video_renditions (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    video_id                VARCHAR(64) NOT NULL REFERENCES videos(id),
    resolution              VARCHAR(16) NOT NULL,     -- e.g. '1080p', '720p', '480p'
    width_px                INTEGER NOT NULL,
    height_px               INTEGER NOT NULL,
    bitrate_kbps            INTEGER NOT NULL,
    codec                   VARCHAR(32) NOT NULL,     -- e.g. 'h264', 'h265'
    segment_duration_seconds INTEGER NOT NULL,        -- Typically 2–6 seconds
    manifest_url            TEXT NOT NULL,            -- URL to the rendition-level m3u8
    created_at              BIGINT NOT NULL,
    updated_at              BIGINT NOT NULL
);
```

**Notes:**
- Every ingested video must produce a minimum of three renditions: 1080p, 720p, and 480p (BR-ING-01).
- `manifest_url` is the URL to the rendition-level `.m3u8` file. The master manifest (referenced by `videos.stream_url`) lists all renditions. ExoPlayer uses the master manifest; this table is for backend inspection and pipeline verification.
- `codec` is `'h264'` for all Phase 4 content. `'h265'` is a possible future extension, deferred.
- `segment_duration_seconds` is the configured FFmpeg segment length — typically 4 seconds for VOD.

---

#### Rendition Standards (Phase 4 Baseline)

| Label | Resolution | Width × Height | Target Bitrate |
|---|---|---|---|
| 480p | 480p | 854 × 480 | 800 kbps |
| 720p | 720p | 1280 × 720 | 2500 kbps |
| 1080p | 1080p | 1920 × 1080 | 5000 kbps |
| 4K (optional) | 2160p | 3840 × 2160 | 14000 kbps |

**On 4K:** BR-ING-01 requires a minimum of three renditions (1080p, 720p, 480p) — 4K is not required to satisfy Phase 4 acceptance criteria. Whether to include it depends on the source footage actually being 4K-capable and on Fire TV Stick 4K (2nd gen) being the target test device (NFR-CO-02), which does support 4K decode. If the source video uploaded for ingestion is below 2160p, the pipeline should not upscale to fabricate a 4K rendition — `VideoRendition` rows are only created for resolutions the source can natively support. Recommend treating 4K as a stretch goal for Phase 4, added once the three-rendition baseline is working end-to-end.

---

### `IngestJob`

Tracks the state of a video through the ingestion pipeline. One row per ingestion attempt.

**Phase introduced:** 4

**Platform:** Backend (PostgreSQL) only.

---

#### Backend — PostgreSQL Table

```sql
CREATE TABLE ingest_jobs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    video_id        VARCHAR(64) NOT NULL REFERENCES videos(id),
    status          VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    input_file_path TEXT NOT NULL,                -- Path to the uploaded raw video file
    output_dir      TEXT,                         -- Path to the packaged HLS output directory
    error_message   TEXT,                         -- NULL unless status = 'FAILED'
    created_at      BIGINT NOT NULL,
    updated_at      BIGINT NOT NULL
);

ALTER TABLE ingest_jobs ADD CONSTRAINT chk_ingest_status
    CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETE', 'FAILED'));
```

**Notes:**
- Status transitions are strictly one-directional: `PENDING → PROCESSING → COMPLETE | FAILED` (ARCHITECTURE.md, Data Integrity). No transition backwards is permitted.
- A video is not available for playback until its `IngestJob.status` is `COMPLETE` (BR-ING-03). The catalog endpoint filters on this condition.
- `output_dir` is `NULL` until transcoding begins. On `FAILED`, `output_dir` may be set but its contents are incomplete — the service layer must never expose partial output for playback.
- `error_message` is populated on `FAILED` with the FFmpeg or Shaka Packager error output. It is `NULL` for all other statuses.
- Multiple `IngestJob` rows can reference the same `video_id` if re-ingestion is triggered (e.g. after a failure). The most recent `COMPLETE` job's output is used.

---

#### `IngestStatus` Enum

```kotlin
// Not used in Android Phase 1–3; included here for backend reference
enum class IngestStatus { PENDING, PROCESSING, COMPLETE, FAILED }
```

---

### `TelemetryEvent`

Represents a single player event emitted during a playback session. Collected via the shared `core` module on both Android (`app`) and Fire TV (`tv`). Buffered locally in Room when the backend is unreachable and flushed on reconnect.

**Phase introduced:** 6

---

#### Android — Room Entity (Buffer)

```kotlin
@Entity(tableName = "telemetry_events")
data class TelemetryEventEntity(
    @PrimaryKey val id: String,                   // Client-generated UUID
    val sessionId: String,                        // Matches PlaybackSession.id on the backend (Phase 6)
    val videoId: String,                          // FK → videos.id (local reference)
    val deviceType: String,                       // "MOBILE" | "TV"
    val eventType: String,                        // See TelemetryEventType enum
    val timestampMs: Long,                        // Unix epoch ms, UTC — when the event occurred
    val bitrateKbps: Int?,                        // Current bitrate at event time; null if not applicable
    val resolution: String?,                      // e.g. "1920x1080"; null if not applicable
    val bufferHealthMs: Long?,                    // Remaining buffer in ms; null if not applicable
    val errorCode: String?,                       // Player error code; null unless eventType = ERROR
    val synced: Boolean,                          // false = pending flush to backend; true = delivered
    val createdAt: Long                           // Unix epoch ms, UTC — when the row was written locally
)
```

**Notes:**
- `id` is generated on the client (UUID v4) and preserved when the event is written to the backend. This enables idempotent flush — re-delivering an event after a retry does not produce a duplicate row on the backend.
- `synced` is `false` when the event is written locally. It is set to `true` after the backend acknowledges receipt. The `TelemetryDispatcher` queries for `synced = false` events on reconnect and batches them for delivery (ARCHITECTURE.md, Local Storage).
- `sessionId` references the backend `playback_sessions.id`. In Phase 6, the session ID is assigned when the session is created on the backend and passed back to the client. If the backend is unreachable at session start, a local UUID is used and reconciled on flush.
- `bufferHealthMs` is the number of milliseconds of video buffered ahead of the current playback position at the time of the event.
- Events are append-only — never updated or deleted from Room (except as part of a future cleanup policy, deferred beyond Phase 6).

---

#### Backend — PostgreSQL Table (Sink)

```sql
CREATE TABLE telemetry_events (
    id              UUID PRIMARY KEY,             -- Client-generated; matches TelemetryEventEntity.id
    session_id      UUID NOT NULL REFERENCES playback_sessions(id),
    video_id        VARCHAR(64) NOT NULL REFERENCES videos(id),
    device_type     VARCHAR(8) NOT NULL,          -- 'MOBILE' | 'TV'
    event_type      VARCHAR(32) NOT NULL,         -- See event type reference below
    timestamp_ms    BIGINT NOT NULL,              -- Unix epoch ms, UTC — when the event occurred
    bitrate_kbps    INTEGER,
    resolution      VARCHAR(16),                  -- e.g. '1920x1080'
    buffer_health_ms BIGINT,
    error_code      VARCHAR(64),
    created_at      BIGINT NOT NULL               -- Unix epoch ms, UTC — when the row was inserted
);

ALTER TABLE telemetry_events ADD CONSTRAINT chk_device_type_tel CHECK (device_type IN ('MOBILE', 'TV'));

-- Index for per-session QoE aggregation (Phase 6 dashboard)
CREATE INDEX idx_telemetry_session ON telemetry_events(session_id);
CREATE INDEX idx_telemetry_video ON telemetry_events(video_id);
```

**Notes:**
- The backend table has no `updated_at` — telemetry events are append-only (ARCHITECTURE.md, Data Integrity).
- `id` on the backend matches the client-generated UUID in the Room buffer. The backend uses `INSERT ... ON CONFLICT DO NOTHING` to handle re-delivered events idempotently.
- The QoE dashboard (Phase 6) aggregates from this table: startup time (first `PLAYBACK_STARTED` event per session), rebuffer rate (sum of `BUFFER_STALL` duration / total session duration), and bitrate switch count (`COUNT` of `BITRATE_CHANGE` events per session).

---

#### `TelemetryEventType` Reference

| Event Type | Description | Key Fields |
|---|---|---|
| `PLAYBACK_STARTED` | First frame rendered after play is tapped | `timestampMs` (used to compute startup time) |
| `PLAYBACK_PAUSED` | User paused playback | — |
| `PLAYBACK_RESUMED` | User resumed after pause | — |
| `PLAYBACK_ENDED` | Stream reached end of content (VOD) | — |
| `BITRATE_CHANGE` | ABR switched to a different rendition | `bitrateKbps`, `resolution` |
| `BUFFER_STALL` | Player entered a rebuffering state | `bufferHealthMs` (typically 0 at stall start) |
| `BUFFER_RECOVERED` | Player exited rebuffering state | `bufferHealthMs` |
| `SEEK` | User seeked to a new position | — |
| `ERROR` | Player encountered a non-recoverable error | `errorCode` |
| `SESSION_ENDED` | Player screen was exited or app backgrounded | — |

```kotlin
// Android
enum class TelemetryEventType {
    PLAYBACK_STARTED,
    PLAYBACK_PAUSED,
    PLAYBACK_RESUMED,
    PLAYBACK_ENDED,
    BITRATE_CHANGE,
    BUFFER_STALL,
    BUFFER_RECOVERED,
    SEEK,
    ERROR,
    SESSION_ENDED
}
```

---

## Room Database

**Class:** `StreamKitDatabase`
**File:** `core/data/local/StreamKitDatabase.kt`
**Version:** 1

```kotlin
@Database(
    entities = [
        VideoEntity::class,
        PlaybackPositionEntity::class,
        TelemetryEventEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class StreamKitDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun playbackPositionDao(): PlaybackPositionDao
    abstract fun telemetryEventDao(): TelemetryEventDao
}
```

**Notes:**
- `TelemetryEventEntity` is included from Phase 1 (the table exists but is not written to until Phase 6). The schema is defined upfront to avoid a migration at Phase 6 kickoff.
- All migrations are handled via Room's `Migration` class. Destructive migration is not permitted in any phase.
- Schema export is enabled (`exportSchema = true`) — exported schemas are committed to source control under `android/schemas/`.

---

## DAO Reference

### `VideoDao`

```kotlin
@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY title ASC")
    fun observeAll(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :id")
    suspend fun getById(id: String): VideoEntity?

    @Query("SELECT * FROM videos WHERE type = :type ORDER BY title ASC")
    fun observeByType(type: String): Flow<List<VideoEntity>>

    @Upsert
    suspend fun upsertAll(videos: List<VideoEntity>)

    @Query("DELETE FROM videos WHERE id NOT IN (:activeIds)")
    suspend fun deleteStale(activeIds: List<String>)
}
```

### `PlaybackPositionDao`

```kotlin
@Dao
interface PlaybackPositionDao {
    @Query("SELECT * FROM playback_positions WHERE videoId = :videoId")
    suspend fun getPosition(videoId: String): PlaybackPositionEntity?

    @Upsert
    suspend fun upsert(position: PlaybackPositionEntity)

    @Query("DELETE FROM playback_positions WHERE videoId = :videoId")
    suspend fun clear(videoId: String)
}
```

### `TelemetryEventDao`

```kotlin
@Dao
interface TelemetryEventDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(event: TelemetryEventEntity)

    @Query("SELECT * FROM telemetry_events WHERE synced = 0 ORDER BY timestampMs ASC LIMIT :limit")
    suspend fun getPendingEvents(limit: Int = 100): List<TelemetryEventEntity>

    @Query("UPDATE telemetry_events SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>)
}
```

---

## PostgreSQL Schema Summary

```sql
-- Phase 1
videos
playback_sessions

-- Phase 4
video_renditions
ingest_jobs

-- Phase 6
telemetry_events
```

Full migration files live in `backend/migrations/` (Alembic). Each phase introduces its tables in a dedicated migration revision.

---

## Cross-Platform ID Strategy

| Entity | Android Room PK | PostgreSQL PK | Notes |
|---|---|---|---|
| `Video` | `String` (Mux asset ID or `"redbull_tv"`) | `VARCHAR(64)` | Same value both sides — Mux ID is the canonical key |
| `PlaybackPosition` (Android) / `PlaybackSession` (Backend) | `String` (videoId, upserted) | `UUID` | Android stores one position row per video; backend stores full sessions |
| `TelemetryEvent` | `String` (client UUID v4) | `UUID` | Same UUID both sides — enables idempotent flush |
| `VideoRendition` | N/A | `UUID` | Backend only |
| `IngestJob` | N/A | `UUID` | Backend only |

---

## Open Schema Questions

| # | Question | Status |
|---|---|---|
| 1 | ~~Should `PlaybackSession` be written to the backend in Phase 1, or deferred entirely to Phase 6?~~ | **Resolved** — deferred to Phase 6. Android writes position locally only in Phase 1. Resume is video-scoped (not device-scoped) once Phase 6 lands, enabling phone → TV resume. See `PlaybackSession` notes above. |
| 2 | Should `TelemetryEventEntity` in Room be pruned after successful sync to prevent unbounded growth? | Deferred — add a cleanup policy in Phase 6 (e.g. delete synced events older than 7 days) |
| 3 | Should `VideoEntity` in Room include a `source` field (MUX / LOCAL / STATIC) mirroring the backend? | Not required for Phase 1 — all Phase 1 content is Mux or Red Bull TV; add in Phase 4 if catalog mixing requires it |
| 4 | **Open — does the Red Bull TV stream URL actually play?** Manual testing of `https://rbmn-live.akamaized.net/hls/live/590964/BoRB-AT/master.m3u8` produced no video, despite the URL resolving with a valid HLS content type. Suspected cause: geo-restriction (`X-GeoBlock: true` observed on the host). | **Blocking before Phase 1 implementation.** Verify playback directly (VLC, ExoPlayer test, or a different network/region) before relying on this as the live content source. If unresolvable, evaluate alternative variant URLs (`master_1660.m3u8`, `master_3360.m3u8`) or a different 24/7 HLS source entirely. |

---

## Changelog

| Version | Date | Author | Notes |
|---|---|---|---|
| 0.1.0 | 2026-06-16 | Danielle Mariani | Initial draft — all entities across all phases |
| 0.1.1 | 2026-06-20 | Danielle Mariani | Clarified Entity Index phase scoping (Android vs Backend); resolved cross-device resume to be video-scoped not device-scoped, clarifying `device_type` is a telemetry dimension only; added optional 4K rendition tier |
| 0.1.2 | 2026-06-20 | Danielle Mariani | Replaced NASA TV with Red Bull TV's 24/7 "Best of Red Bull" stream as the live content source — NASA's 24/7 NTV1-HLS channel was discontinued in 2024 and its public stream URLs no longer resolve. Updated static key (`"redbull_tv"`), `streamUrl` example, and all related notes |
| 0.1.3 | 2026-06-20 | Danielle Mariani | Flagged the Red Bull TV `streamUrl` as unverified — manual playback testing produced no video despite the URL resolving with a valid HLS content type; added as a blocking open question (#4) pending verification before Phase 1 implementation |

---

## Related Documents

| Document | Purpose |
|---|---|
| SPEC.md | Business rules and data model summary (source of entity definitions) |
| ARCHITECTURE.md | Module structure, Room database rules, local storage policy |
| specs/technical/api-contract.md | Backend API endpoints that read/write these tables |
| specs/technical/content-catalog.md | Test stream sources and their metadata (populates `Video` table) |