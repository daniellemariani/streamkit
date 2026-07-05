# Catalog — Tasks

**Version:** 0.1.1
**Status:** Draft
**Phase:** 1 (Android)
**Owner:** Danielle Mariani
**Created at:** 2026-07-04
**Last Updated:** 2026-07-04

---

## Overview

This file defines the implementation task breakdown for the Catalog feature. Since Catalog is Phase 1's first feature, it includes the full project and infrastructure foundation. Subsequent features will skip Groups 0–2.

Tasks are intentionally small to keep PRs reviewable. Each task targets a single file or a tightly related pair of files. Effort estimates: S = ~1–2h, M = ~2–4h, L = ~4–8h.

**Group execution order:**

| Group | Name | Can start when |
|---|---|---|
| 0 | Project Foundation | — |
| 1 | Database Foundation | TSK-CAT-04 done |
| 2 | Network Foundation | TSK-CAT-04 done (parallel with Group 1) |
| 3 | Data Layer | TSK-CAT-06, TSK-CAT-09 done |
| 4 | Constants & Resources | TSK-CAT-01 done (parallel with Groups 1–3) |
| 5 | Navigation Shell | TSK-CAT-14, TSK-CAT-15 done |
| 6 | Use Cases | TSK-CAT-10 done |
| 7 | ViewModel | TSK-CAT-13, TSK-CAT-18, TSK-CAT-19 done |
| 8 | Shared Components | TSK-CAT-03, TSK-CAT-15 done (parallel with Group 7) |
| 9 | Screen Components | TSK-CAT-17, TSK-CAT-21, TSK-CAT-22, Groups 8 done |
| 10 | Testing | TSK-CAT-21, TSK-CAT-22, TSK-CAT-13 done |

---

## Task Summary

| ID | Title | Group | Effort | Status |
|---|---|---|---|---|
| TSK-CAT-01 | Create Android project skeleton | Project Foundation | M | Not Started |
| TSK-CAT-02 | Set up base package structure | Project Foundation | S | Not Started |
| TSK-CAT-03 | Configure AppTheme | Project Foundation | M | Not Started |
| TSK-CAT-04 | Configure Hilt application module | Project Foundation | S | Not Started |
| TSK-CAT-05 | Define VideoEntity and StreamKitDatabase | Database Foundation | S | Not Started |
| TSK-CAT-06 | Define VideoDao | Database Foundation | M | Not Started |
| TSK-CAT-07 | Configure Ktor HttpClient and NetworkModule | Network Foundation | S | Not Started |
| TSK-CAT-08 | Define Mux network DTOs | Network Foundation | S | Not Started |
| TSK-CAT-09 | Implement MuxApiClient.listAssets() | Network Foundation | M | Not Started |
| TSK-CAT-10 | Define VideoRepository interface | Data Layer | S | Not Started |
| TSK-CAT-11 | Implement VideoRepositoryImpl — observe queries | Data Layer | S | Not Started |
| TSK-CAT-12 | Implement VideoRepositoryImpl — seedLiveEntries() | Data Layer | S | Not Started |
| TSK-CAT-13 | Implement VideoRepositoryImpl — syncVodCatalog() | Data Layer | M | Not Started |
| TSK-CAT-14 | Implement DatabaseModule and RepositoryModule | Data Layer | S | Not Started |
| TSK-CAT-15 | Define catalog string resources | Constants & Resources | S | Not Started |
| TSK-CAT-16 | Define static live entry seed config | Constants & Resources | S | Not Started |
| TSK-CAT-17 | Implement MainActivity and AppNavGraph shell | Navigation Shell | M | Not Started |
| TSK-CAT-18 | Implement SeedLiveEntriesUseCase | Use Cases | S | Not Started |
| TSK-CAT-19 | Implement SyncVodCatalogUseCase | Use Cases | S | Not Started |
| TSK-CAT-20 | Define CatalogUiState, VodState, CatalogEvent, CatalogUiEffect | ViewModel | S | Not Started |
| TSK-CAT-21 | Implement CatalogViewModel — init and Flow subscriptions | ViewModel | M | Not Started |
| TSK-CAT-22 | Implement CatalogViewModel — event handlers | ViewModel | S | Not Started |
| TSK-CAT-23 | Implement LiveBadge shared component | Shared Components | S | Not Started |
| TSK-CAT-24 | Implement CatalogTopAppBar | Screen Components | S | Not Started |
| TSK-CAT-25 | Implement VodPosterCard | Screen Components | S | Not Started |
| TSK-CAT-26 | Implement VodSkeletonGrid | Screen Components | S | Not Started |
| TSK-CAT-27 | Implement CatalogEmptyState and CatalogErrorState | Screen Components | S | Not Started |
| TSK-CAT-28 | Implement LiveCarouselCard | Screen Components | S | Not Started |
| TSK-CAT-29 | Implement LiveCarousel with auto-advance | Screen Components | M | Not Started |
| TSK-CAT-30 | Implement VodGrid (state router) | Screen Components | S | Not Started |
| TSK-CAT-31 | Implement CatalogScreen | Screen Components | M | Not Started |
| TSK-CAT-32 | Unit tests — CatalogViewModel | Testing | M | Not Started |
| TSK-CAT-33 | Unit tests — SyncVodCatalogUseCase and SeedLiveEntriesUseCase | Testing | S | Not Started |
| TSK-CAT-34 | Room integration tests — VideoDao and VideoRepositoryImpl | Testing | M | Not Started |

---

## Group 0 — Project Foundation

---

**TSK-CAT-01 — Create Android project skeleton**
- Effort: M
- Phase: 1
- Group: Project Foundation
- Requirements: —
- Acceptance Criteria: —
- Status: Not Started
- Depends on: None
- Creates:
  - `android/settings.gradle.kts`
  - `android/build.gradle.kts`
  - `android/gradle/libs.versions.toml`
  - `android/core/build.gradle.kts`
  - `android/app/build.gradle.kts`
  - `android/tv/build.gradle.kts`
  - `android/app/src/main/AndroidManifest.xml`
- Details:
  Create a new Android Gradle project with three modules (`core`, `app`, `tv`) as defined in `ARCHITECTURE.md`. Use Gradle Kotlin DSL throughout.

  **`libs.versions.toml` must declare:**
  - `kotlin`, `agp`, `compose-bom`, `compose-compiler`, `hilt`, `room`, `ktor`, `datastore`, `media3`, `navigation-compose`, `coroutines`

  **Module dependency rules (enforce in each `build.gradle.kts`):**
  - `app` depends on `core`
  - `tv` depends on `core`
  - Neither `app` nor `tv` declares a dependency on each other

  Package name: `com.streamkit` (apply consistently across all modules).
  Min SDK: 31 | Target SDK: 35 | Compile SDK: 35

---

**TSK-CAT-02 — Set up base package structure**
- Effort: S
- Phase: 1
- Group: Project Foundation
- Requirements: —
- Acceptance Criteria: —
- Status: Not Started
- Depends on: TSK-CAT-01
- Creates:
  - `android/core/src/main/java/com/streamkit/core/domain/model/` (empty)
  - `android/core/src/main/java/com/streamkit/core/domain/repository/` (empty)
  - `android/core/src/main/java/com/streamkit/core/data/local/` (empty)
  - `android/core/src/main/java/com/streamkit/core/data/remote/` (empty)
  - `android/core/src/main/java/com/streamkit/core/data/repository/` (empty)
  - `android/core/src/main/java/com/streamkit/core/di/` (empty)
  - `android/core/src/main/java/com/streamkit/core/ui/components/` (empty)
  - `android/app/src/main/java/com/streamkit/app/ui/navigation/` (empty)
  - `android/app/src/main/java/com/streamkit/app/feature/catalog/` (empty)
- Details:
  Create the directory scaffolding per `ARCHITECTURE.md`. No Kotlin files yet — package-level `.gitkeep` files only if needed to preserve empty directories in Git.

---

**TSK-CAT-03 — Configure AppTheme**
- Effort: M
- Phase: 1
- Group: Project Foundation
- Requirements: —
- Acceptance Criteria: —
- Status: Not Started
- Depends on: TSK-CAT-01
- Creates:
  - `android/core/src/main/java/com/streamkit/core/ui/theme/Color.kt`
  - `android/core/src/main/java/com/streamkit/core/ui/theme/Type.kt`
  - `android/core/src/main/java/com/streamkit/core/ui/theme/Theme.kt`
- Details:
  Map all design tokens from `specs/design/design.md` to Material 3.

  **`Color.kt`:** define all hex color vals for the dark-only palette: `Background`, `SurfaceCard`, `SurfaceAlt`, `BorderDefault`, `TextPrimary`, `TextSecondary`, `AccentPrimary`, `AccentOn`, `SemanticError`, `SemanticWarning`. No light theme equivalents.

  **`Type.kt`:** define a `StreamKitTypography` val mapping the 7 typography roles from `specs/design/design.md` (Heading1 → Caption) to `TextStyle` with the correct `sp` sizes, `FontWeight`, and `lineHeight`. Use Roboto (system default) — no custom font registration needed.

  **`Theme.kt`:** assemble `AppTheme` wrapping `MaterialTheme` with the dark `ColorScheme` and `StreamKitTypography`. No `isSystemInDarkTheme()` branch — always dark.

  Token-to-Material 3 role mapping is defined in `specs/design/design.md` — Platform Adaptation section.

---

**TSK-CAT-04 — Configure Hilt application module**
- Effort: S
- Phase: 1
- Group: Project Foundation
- Requirements: —
- Acceptance Criteria: —
- Status: Not Started
- Depends on: TSK-CAT-01
- Creates:
  - `android/app/src/main/java/com/streamkit/app/StreamKitApp.kt`
- Details:
  Annotate `StreamKitApp : Application()` with `@HiltAndroidApp`. Register it in `AndroidManifest.xml` (`android:name=".StreamKitApp"`). No DI modules yet — those come in later tasks.

---

## Group 1 — Database Foundation

---

**TSK-CAT-05 — Define VideoEntity and StreamKitDatabase**
- Effort: S
- Phase: 1
- Group: Database Foundation
- Requirements: RQ-CAT-17, RQ-CAT-20
- Acceptance Criteria: —
- Status: Not Started
- Depends on: TSK-CAT-04
- Creates:
  - `android/core/src/main/java/com/streamkit/core/data/local/VideoEntity.kt`
  - `android/core/src/main/java/com/streamkit/core/data/local/StreamKitDatabase.kt`
- Details:
  Define `VideoEntity` per `specs/technical/data-model.md`. Required fields: `id` (String, primary key), `title`, `description`, `type` (enum: VOD / LIVE), `thumbnailUrl`, `streamUrl`, `durationSeconds` (nullable), `isDrmProtected`, `createdAt`, `updatedAt`.

  `StreamKitDatabase` is a Room `@Database` with `entities = [VideoEntity::class]`, `version = 1`. Include a `fun videoDao(): VideoDao` abstract accessor. No migration strategy needed for Phase 1 — destructive migration is acceptable.

---

**TSK-CAT-06 — Define VideoDao**
- Effort: M
- Phase: 1
- Group: Database Foundation
- Requirements: RQ-CAT-11, RQ-CAT-12, RQ-CAT-13, RQ-CAT-17, RQ-CAT-18, RQ-CAT-21
- Acceptance Criteria: —
- Status: Not Started
- Depends on: TSK-CAT-05
- Creates:
  - `android/core/src/main/java/com/streamkit/core/data/local/VideoDao.kt`
- Details:
  Define all DAO methods needed by the Catalog data layer:

  - `@Upsert fun upsertAll(videos: List<VideoEntity>)` — used by both seeding and sync
  - `@Query fun observeVodItems(): Flow<List<VideoEntity>>` — filters `type = 'VOD'`, `status = 'ready'` (BR-CAT-04)
  - `@Query fun observeLiveItems(): Flow<List<VideoEntity>>` — filters `type = 'LIVE'`
  - `@Query fun getLiveIds(): List<String>` — returns IDs of all non-VOD rows; used by `deleteStale`
  - `@Query fun deleteStale(activeIds: List<String>)` — deletes rows whose `id` is not in `activeIds`; called after a successful Mux sync with `activeIds = freshVodIds + liveIds`

  All Flow-returning queries must be marked `suspend`-free (Room handles the thread). Non-Flow queries must be `suspend`.

---

## Group 2 — Network Foundation

---

**TSK-CAT-07 — Configure Ktor HttpClient and NetworkModule**
- Effort: S
- Phase: 1
- Group: Network Foundation
- Requirements: RQ-CAT-14
- Acceptance Criteria: —
- Status: Not Started
- Depends on: TSK-CAT-04
- Creates:
  - `android/core/src/main/java/com/streamkit/core/di/NetworkModule.kt`
- Details:
  Define a Hilt `@Module @InstallIn(SingletonComponent::class)` that provides a singleton Ktor `HttpClient` configured with:
  - `ContentNegotiation` plugin with `json()` (kotlinx.serialization)
  - `Logging` plugin (log level `INFO` for debug builds only)
  - Default request timeout of 15s

  The Mux Token ID and Secret are read from `BuildConfig` fields sourced from `local.properties` (never hardcoded, never committed). The client is configured with Basic Auth using those values. See `specs/features/catalog/requirements.md` DS-CAT-05.

---

**TSK-CAT-08 — Define Mux network DTOs**
- Effort: S
- Phase: 1
- Group: Network Foundation
- Requirements: RQ-CAT-14, RQ-CAT-15
- Acceptance Criteria: —
- Status: Not Started
- Depends on: TSK-CAT-07
- Creates:
  - `android/core/src/main/java/com/streamkit/core/data/remote/MuxAssetDto.kt`
- Details:
  Define `@Serializable` DTOs matching the Mux `GET /video/v1/assets` response shape:

  - `MuxListAssetsResponse(data: List<MuxAssetDto>, next_cursor: String?)`
  - `MuxAssetDto(id: String, status: String, duration: Double?, playback_ids: List<MuxPlaybackIdDto>?)`
  - `MuxPlaybackIdDto(id: String, policy: String)`

  Use `@SerialName` for any snake_case fields. These DTOs live in the `remote` package and must never be used outside the data layer — mapping to domain models happens in the repository.

---

**TSK-CAT-09 — Implement MuxApiClient.listAssets()**
- Effort: M
- Phase: 1
- Group: Network Foundation
- Requirements: RQ-CAT-14
- Acceptance Criteria: —
- Status: Not Started
- Depends on: TSK-CAT-08
- Creates:
  - `android/core/src/main/java/com/streamkit/core/data/remote/MuxApiClient.kt`
- Details:
  Implement `suspend fun listAssets(): Result<List<MuxAssetDto>>`.

  Internally, paginate `GET https://api.mux.com/video/v1/assets` by calling with `?page_token={cursor}` until the response's `next_cursor` is null, accumulating all pages into a single list. Return `Result.success(allAssets)` on completion, or `Result.failure(exception)` on any network or deserialization error.

  Do not filter by `status` here — filtering to `status == "ready"` happens in the repository (BR-CAT-04).

---

## Group 3 — Data Layer

---

**TSK-CAT-10 — Define VideoRepository interface**
- Effort: S
- Phase: 1
- Group: Data Layer
- Requirements: RQ-CAT-11, RQ-CAT-12, RQ-CAT-13, RQ-CAT-14, RQ-CAT-17, RQ-CAT-21
- Acceptance Criteria: —
- Status: Not Started
- Depends on: TSK-CAT-05
- Creates:
  - `android/core/src/main/java/com/streamkit/core/domain/repository/VideoRepository.kt`
  - `android/core/src/main/java/com/streamkit/core/domain/model/Video.kt`
- Details:
  Define the `Video` domain model (mirrors `VideoEntity` fields; no Room annotations) and the `VideoRepository` interface with:

  - `fun observeVodItems(): Flow<List<Video>>`
  - `fun observeLiveItems(): Flow<List<Video>>`
  - `suspend fun seedLiveEntries(): Result<Unit>`
  - `suspend fun syncVodCatalog(): Result<Unit>`

  These are the only methods needed for Phase 1 Catalog. The interface is the contract; implementation details (Room, Mux) are invisible to callers.

---

**TSK-CAT-11 — Implement VideoRepositoryImpl — observe queries**
- Effort: S
- Phase: 1
- Group: Data Layer
- Requirements: RQ-CAT-08, RQ-CAT-11, RQ-CAT-12
- Acceptance Criteria: AC-CAT-01, AC-CAT-02
- Status: Not Started
- Depends on: TSK-CAT-06, TSK-CAT-10
- Creates:
  - `android/core/src/main/java/com/streamkit/core/data/repository/VideoRepositoryImpl.kt` (partial — observe methods only)
- Details:
  Implement `observeVodItems()` and `observeLiveItems()` by delegating to `VideoDao` and mapping `VideoEntity → Video` via a private `VideoEntity.toDomain()` extension. Keep the mapper in the same file for now.

  Each method applies `.map { entities -> entities.map { it.toDomain() } }` on the DAO Flow. No additional filtering beyond what the DAO already applies.

---

**TSK-CAT-12 — Implement VideoRepositoryImpl — seedLiveEntries()**
- Effort: S
- Phase: 1
- Group: Data Layer
- Requirements: RQ-CAT-20, RQ-CAT-21
- Acceptance Criteria: AC-CAT-07
- Status: Not Started
- Depends on: TSK-CAT-11, TSK-CAT-16
- Modifies:
  - `android/core/src/main/java/com/streamkit/core/data/repository/VideoRepositoryImpl.kt`
- Details:
  Implement `seedLiveEntries()`. Read the 3 static `VideoEntity` definitions from `LiveSeedConfig` (created in TSK-CAT-16), call `videoDao.upsertAll(liveEntries)`, return `Result.success(Unit)`.

  Must be idempotent: `@Upsert` ensures repeated calls produce no duplicates and do not overwrite existing data. Wrap in `try/catch` and return `Result.failure(e)` on any DAO exception.

---

**TSK-CAT-13 — Implement VideoRepositoryImpl — syncVodCatalog()**
- Effort: M
- Phase: 1
- Group: Data Layer
- Requirements: RQ-CAT-14, RQ-CAT-15, RQ-CAT-16, RQ-CAT-18, RQ-CAT-19, RQ-CAT-21
- Acceptance Criteria: AC-CAT-01, AC-CAT-02, AC-CAT-03, AC-CAT-04, AC-CAT-05
- Status: Not Started
- Depends on: TSK-CAT-09, TSK-CAT-12
- Modifies:
  - `android/core/src/main/java/com/streamkit/core/data/repository/VideoRepositoryImpl.kt`
- Details:
  Implement `syncVodCatalog()`:

  1. Call `muxApiClient.listAssets()` — return `Result.failure` immediately on network error.
  2. Filter to `status == "ready"` only (BR-CAT-04).
  3. Map `MuxAssetDto → VideoEntity` using the first playback ID with `policy = "public"`. Fields: `thumbnailUrl = "https://image.mux.com/{playback_id}/thumbnail.jpg"`, `streamUrl = "https://stream.mux.com/{playback_id}.m3u8"`, `durationSeconds = dto.duration?.roundToInt()`.
  4. Call `videoDao.upsertAll(freshEntities)`.
  5. Call `videoDao.getLiveIds()` to get the always-retain IDs.
  6. Call `videoDao.deleteStale(activeIds = freshVodIds + liveIds)` (RQ-CAT-21).
  7. Return `Result.success(Unit)`.

  Wrap steps 2–6 in a `try/catch`; return `Result.failure(e)` on any failure. Note: `title` / `description` fallback to `dto.id` per RQ-CAT-19 until the naming convention is resolved (`content-catalog.md` Open Question #2).

---

**TSK-CAT-14 — Implement DatabaseModule and RepositoryModule**
- Effort: S
- Phase: 1
- Group: Data Layer
- Requirements: —
- Acceptance Criteria: —
- Status: Not Started
- Depends on: TSK-CAT-13
- Creates:
  - `android/core/src/main/java/com/streamkit/core/di/DatabaseModule.kt`
  - `android/core/src/main/java/com/streamkit/core/di/RepositoryModule.kt`
- Details:
  **`DatabaseModule`:** `@Singleton` `StreamKitDatabase` built with `Room.databaseBuilder`. Provide `VideoDao` from the database instance.

  **`RepositoryModule`:** `@Binds` `VideoRepository` → `VideoRepositoryImpl`. Install in `SingletonComponent`.

  No `CatalogModule` bindings are needed in Phase 1 — use cases use `@Inject constructor`.

---

## Group 4 — Constants & Resources

---

**TSK-CAT-15 — Define catalog string resources**
- Effort: S
- Phase: 1
- Group: Constants & Resources
- Requirements: RQ-CAT-01, RQ-CAT-02, RQ-CAT-13, RQ-CAT-15
- Acceptance Criteria: —
- Status: Not Started
- Depends on: TSK-CAT-01
- Modifies:
  - `android/app/src/main/res/values/strings.xml`
- Details:
  Add all catalog-scoped strings as defined in `specs/features/catalog/design.md` — String Resources section. No hardcoded strings may remain in any catalog composable after this task.

---

**TSK-CAT-16 — Define static live entry seed config**
- Effort: S
- Phase: 1
- Group: Constants & Resources
- Requirements: RQ-CAT-20
- Acceptance Criteria: AC-CAT-07
- Status: Not Started
- Depends on: TSK-CAT-05
- Creates:
  - `android/core/src/main/java/com/streamkit/core/data/local/LiveSeedConfig.kt`
- Details:
  Define a `LiveSeedConfig` object with a `val entries: List<VideoEntity>` containing the 3 static live entries (Red Bull TV, DW English, NHK World-Japan). Each entry must have:
  - A hardcoded, stable UUID as `id` (assign here — once set, never change)
  - `type = VideoType.LIVE`
  - `streamUrl` per the candidate URLs in `specs/technical/content-catalog.md`
  - `isDrmProtected = false`
  - `durationSeconds = null`

  These IDs fulfill RQ-CAT-20's "assigned at implementation time" requirement. Record the three assigned UUIDs in a comment at the top of this file and in `CONTEXT.md`.

---

## Group 5 — Navigation Shell

---

**TSK-CAT-17 — Implement MainActivity and AppNavGraph shell**
- Effort: M
- Phase: 1
- Group: Navigation Shell
- Requirements: RQ-CAT-01, RQ-CAT-03, RQ-CAT-06, RQ-CAT-09
- Acceptance Criteria: AC-CAT-08, AC-CAT-09
- Status: Not Started
- Depends on: TSK-CAT-14, TSK-CAT-15
- Creates:
  - `android/app/src/main/java/com/streamkit/app/ui/MainActivity.kt`
  - `android/app/src/main/java/com/streamkit/app/ui/navigation/AppNavGraph.kt`
  - `android/app/src/main/java/com/streamkit/app/ui/navigation/AppRoutes.kt`
- Details:
  **`AppRoutes`:** define route constants and builder functions per `specs/features/catalog/design.md` — Navigation section. `CATALOG` is the start destination.

  **`AppNavGraph`:** a `@Composable` function hosting a `NavHost` with 4 destinations: `CatalogScreen`, `PlayerScreen` (stub), `LivePlayerScreen` (stub), `SettingsScreen` (stub). Stub screens are empty composables returning `Box(Modifier.fillMaxSize())` with a centered placeholder `Text`.

  **`MainActivity`:** `@AndroidEntryPoint`, `ComponentActivity`. Sets content to `AppTheme { AppNavGraph() }`. Handles per-destination orientation locking: observe `navController.currentBackStackEntryAsState()` and call `requestedOrientation = SCREEN_ORIENTATION_PORTRAIT` when destination is `CATALOG` or `SETTINGS` — per `ARCHITECTURE.md` Screen Orientation.

---

## Group 6 — Use Cases

---

**TSK-CAT-18 — Implement SeedLiveEntriesUseCase**
- Effort: S
- Phase: 1
- Group: Use Cases
- Requirements: RQ-CAT-20
- Acceptance Criteria: AC-CAT-07
- Status: Not Started
- Depends on: TSK-CAT-10
- Creates:
  - `android/app/src/main/java/com/streamkit/app/feature/catalog/domain/SeedLiveEntriesUseCase.kt`
- Details:
  Single-purpose use case: `suspend fun execute(): Result<Unit>` delegates to `videoRepository.seedLiveEntries()` and returns the result as-is. `@Inject constructor(private val videoRepository: VideoRepository)`.

---

**TSK-CAT-19 — Implement SyncVodCatalogUseCase**
- Effort: S
- Phase: 1
- Group: Use Cases
- Requirements: RQ-CAT-14, RQ-CAT-17, RQ-CAT-18, RQ-CAT-21
- Acceptance Criteria: AC-CAT-01, AC-CAT-02, AC-CAT-03
- Status: Not Started
- Depends on: TSK-CAT-10
- Creates:
  - `android/app/src/main/java/com/streamkit/app/feature/catalog/domain/SyncVodCatalogUseCase.kt`
- Details:
  Single-purpose use case: `suspend fun execute(): Result<Unit>` delegates to `videoRepository.syncVodCatalog()` and returns the result as-is. `@Inject constructor(private val videoRepository: VideoRepository)`.

---

## Group 7 — ViewModel

---

**TSK-CAT-20 — Define CatalogUiState, VodState, CatalogEvent, CatalogUiEffect**
- Effort: S
- Phase: 1
- Group: ViewModel
- Requirements: RQ-CAT-08, RQ-CAT-11, RQ-CAT-12, RQ-CAT-13, RQ-CAT-14, RQ-CAT-15, RQ-CAT-16
- Acceptance Criteria: —
- Status: Not Started
- Depends on: TSK-CAT-10
- Creates:
  - `android/app/src/main/java/com/streamkit/app/feature/catalog/ui/CatalogUiState.kt`
- Details:
  Define all MVI data types in a single file per `specs/features/catalog/design.md` — Data Models section:
  - `CatalogUiState` data class
  - `VodState` sealed class (`Loading`, `Content(items)`, `Empty`, `Error`)
  - `CatalogEvent` sealed class
  - `CatalogUiEffect` sealed class

  Keep these as pure data/sealed definitions with no logic. Logic lives in `CatalogViewModel`.

---

**TSK-CAT-21 — Implement CatalogViewModel — init and Flow subscriptions**
- Effort: M
- Phase: 1
- Group: ViewModel
- Requirements: RQ-CAT-08, RQ-CAT-11, RQ-CAT-12, RQ-CAT-14, RQ-CAT-16, RQ-CAT-19, RQ-CAT-22
- Acceptance Criteria: AC-CAT-01, AC-CAT-02, AC-CAT-05, AC-CAT-07
- Status: Not Started
- Depends on: TSK-CAT-18, TSK-CAT-19, TSK-CAT-20
- Creates:
  - `android/app/src/main/java/com/streamkit/app/feature/catalog/ui/CatalogViewModel.kt` (partial — init only)
- Details:
  Implement `CatalogViewModel : ViewModel()` with `@HiltViewModel @Inject constructor`.

  **`init {}`** launches on `viewModelScope`:
  1. Call `seedLiveEntriesUseCase.execute()`.
  2. Launch a coroutine collecting `videoRepository.observeLiveItems()` → `_uiState.update { it.copy(liveItems = items) }`.
  3. Launch a coroutine collecting `videoRepository.observeVodItems()` and driving `vodState` per the state-transition table in `specs/features/catalog/design.md` — CatalogUiState section.
  4. Call `syncVodCatalog()` (defined in TSK-CAT-22).

  Steps 2, 3, 4 launch concurrently within `viewModelScope`. Step 1 completes before 2–4 start (sequential within the outer coroutine) so live entries are seeded before the first Room emission is processed.

---

**TSK-CAT-22 — Implement CatalogViewModel — event handlers**
- Effort: S
- Phase: 1
- Group: ViewModel
- Requirements: RQ-CAT-03, RQ-CAT-06, RQ-CAT-09, RQ-CAT-15, RQ-CAT-16
- Acceptance Criteria: AC-CAT-04, AC-CAT-06, AC-CAT-08, AC-CAT-09
- Status: Not Started
- Depends on: TSK-CAT-21
- Modifies:
  - `android/app/src/main/java/com/streamkit/app/feature/catalog/ui/CatalogViewModel.kt`
- Details:
  Add `fun onEvent(event: CatalogEvent)` and `private suspend fun syncVodCatalog()` per `specs/features/catalog/design.md` — ViewModel section.

  **`syncVodCatalog()`:** call `syncVodCatalogUseCase.execute()`. On failure: if `vodState is Content`, set `showRefreshErrorBanner = true`; else set `vodState = Error`. On success: set `showRefreshErrorBanner = false` (Room Flow handles content update).

  **`onEvent` cases:**
  - `LiveItemTapped` → emit `NavigateToLivePlayer(videoId)`
  - `VodItemTapped` → emit `NavigateToPlayer(videoId)`
  - `SettingsTapped` → emit `NavigateToSettings`
  - `RetryFetch` → set `vodState = Loading`, re-run `syncVodCatalog()`

---

## Group 8 — Shared Components

---

**TSK-CAT-23 — Implement LiveBadge shared component**
- Effort: S
- Phase: 1
- Group: Shared Components
- Requirements: RQ-CAT-07
- Acceptance Criteria: AC-CAT-07
- Status: Not Started
- Depends on: TSK-CAT-03
- Creates:
  - `android/core/src/main/java/com/streamkit/core/ui/components/LiveBadge.kt`
- Details:
  Implement per `specs/design/design.md` — Live Badge section:
  - Solid `color.semantic.error` (`#FF3B3F`) background
  - Text: `stringResource(R.string.live_badge_label)` — add this string to the `app` module's `strings.xml`
  - Typography: `Label` scale (12sp, Medium)
  - Shape: `radius.sm` (6dp) rounded corners
  - Padding: `spacing.xs` (4dp) vertical / `spacing.sm` (8dp) horizontal
  - Not clickable — display-only

  Lives in `core/ui/components/` so it can be reused by the Live Player overlay (Phase 1) without importing `app`.

---

## Group 9 — Screen Components

---

**TSK-CAT-24 — Implement CatalogTopAppBar**
- Effort: S
- Phase: 1
- Group: Screen Components
- Requirements: RQ-CAT-01, RQ-CAT-02, RQ-CAT-03
- Acceptance Criteria: AC-CAT-09
- Status: Not Started
- Depends on: TSK-CAT-03, TSK-CAT-15
- Creates:
  - `android/app/src/main/java/com/streamkit/app/feature/catalog/ui/components/CatalogTopAppBar.kt`
- Details:
  Material 3 `TopAppBar` with `containerColor = color.background.primary`. Title: `Text(stringResource(R.string.catalog_app_bar_title), style = Heading1, color = TextPrimary)`. Action: `IconButton` with `Icons.Outlined.Settings` (20dp), `contentDescription = stringResource(R.string.catalog_settings_icon_description)`, `tint = TextSecondary`. On click: invoke `onSettingsTapped` callback. No elevation.

---

**TSK-CAT-25 — Implement VodPosterCard**
- Effort: S
- Phase: 1
- Group: Screen Components
- Requirements: RQ-CAT-08, RQ-CAT-09
- Acceptance Criteria: AC-CAT-08
- Status: Not Started
- Depends on: TSK-CAT-03
- Creates:
  - `android/app/src/main/java/com/streamkit/app/feature/catalog/ui/components/VodPosterCard.kt`
- Details:
  Implement per `specs/features/catalog/design.md` — VodPosterCard section. `Column` with `AsyncImage` (aspect ratio 2:3, `ContentScale.Crop`, `radius.lg` clip) and `Text` label below (Body Small, TextPrimary, `maxLines = 2`, `TextOverflow.Ellipsis`, `spacing.xs` top padding). The entire card is wrapped in a `Modifier.clickable { onTap(video.id) }`.

  Use Coil's `AsyncImage` for image loading. Provide a `placeholder` and `error` drawable using `color.surface.alt` so the card has a visible fallback when `thumbnailUrl` is null or fails to load.

---

**TSK-CAT-26 — Implement VodSkeletonGrid**
- Effort: S
- Phase: 1
- Group: Screen Components
- Requirements: RQ-CAT-11
- Acceptance Criteria: AC-CAT-01
- Status: Not Started
- Depends on: TSK-CAT-03
- Creates:
  - `android/app/src/main/java/com/streamkit/app/feature/catalog/ui/components/VodSkeletonGrid.kt`
- Details:
  Render 6 placeholder cards in a 2-column arrangement (not lazy — fixed count). Each card matches `VodPosterCard`'s dimensions: `fillMaxWidth()`, aspect ratio 2:3, `radius.lg` clip, background `color.surface.alt`.

  Shimmer: use `InfiniteTransition` with a translating horizontal gradient (`color.surface.alt → color.surface.card → color.surface.alt`), cycling on a 1 200ms loop. Apply as a `drawWithContent` modifier on each placeholder card.

  The 2-column layout uses a non-lazy `FlowRow` or simple `Column` + `Row` arrangement — do not use a nested `LazyVerticalGrid` here (it is placed inside the root `LazyVerticalGrid` via a full-span item in `CatalogScreen`).

---

**TSK-CAT-27 — Implement CatalogEmptyState and CatalogErrorState**
- Effort: S
- Phase: 1
- Group: Screen Components
- Requirements: RQ-CAT-13, RQ-CAT-15
- Acceptance Criteria: AC-CAT-04, AC-CAT-06
- Status: Not Started
- Depends on: TSK-CAT-03, TSK-CAT-15
- Creates:
  - `android/app/src/main/java/com/streamkit/app/feature/catalog/ui/components/CatalogEmptyState.kt`
  - `android/app/src/main/java/com/streamkit/app/feature/catalog/ui/components/CatalogErrorState.kt`
- Details:
  Both composables follow the anatomy in `specs/features/catalog/design.md` — centered `Column` with icon + message text (± retry button for error). Keep in separate files for clarity.

  **`CatalogEmptyState`:** `Icons.Outlined.VideoLibrary` (48dp, `TextSecondary`), `R.string.catalog_vod_empty_message` (Body, TextSecondary).

  **`CatalogErrorState`:** `Icons.Outlined.CloudOff` (48dp, `SemanticError`), `R.string.catalog_vod_error_message` (Body, TextPrimary), filled `Button` (`R.string.catalog_vod_error_retry`, `AccentPrimary` container) that invokes `onRetry()`.

---

**TSK-CAT-28 — Implement LiveCarouselCard**
- Effort: S
- Phase: 1
- Group: Screen Components
- Requirements: RQ-CAT-04, RQ-CAT-05, RQ-CAT-06, RQ-CAT-07
- Acceptance Criteria: AC-CAT-07, AC-CAT-08
- Status: Not Started
- Depends on: TSK-CAT-23, TSK-CAT-03
- Creates:
  - `android/app/src/main/java/com/streamkit/app/feature/catalog/ui/components/LiveCarouselCard.kt`
- Details:
  Implement per `specs/features/catalog/design.md` — LiveCarousel section (card anatomy). A `Box` with `fillMaxWidth()`, `aspectRatio(16f / 9f)`, `radius.lg` clip, `color.surface.card` background, wrapped in `Modifier.clickable { onTap(video.id) }`.

  Layers (bottom to top):
  1. `AsyncImage` (`ContentScale.Crop`, `fillMaxSize`). Coil with `color.surface.alt` placeholder.
  2. Scrim `Box` (`fillMaxSize`): `Brush.verticalGradient(transparent → Black.copy(alpha = 0.6f))` applied from 60% to 100% of height.
  3. `LiveBadge` aligned `TopStart`, padding `spacing.sm`.
  4. `Text(video.title)` aligned `BottomStart`, Label style, TextPrimary, padding `spacing.sm`.

---

**TSK-CAT-29 — Implement LiveCarousel with auto-advance**
- Effort: M
- Phase: 1
- Group: Screen Components
- Requirements: RQ-CAT-04, RQ-CAT-05, RQ-CAT-06, RQ-CAT-07
- Acceptance Criteria: AC-CAT-07
- Status: Not Started
- Depends on: TSK-CAT-28
- Creates:
  - `android/app/src/main/java/com/streamkit/app/feature/catalog/ui/components/LiveCarousel.kt`
- Details:
  Implement `LiveCarousel(items: List<Video>, isVisible: Boolean, onItemClick: (String) -> Unit)` per `specs/features/catalog/design.md` — LiveCarousel and Auto-Advance Implementation Note sections.

  **Auto-advance `LaunchedEffect`:** key on `pagerState.settledPage` and `isVisible`. Return early if `!isVisible`. Otherwise `delay(8_000)` then `pagerState.animateScrollToPage((settledPage + 1) % items.size)`. Using `settledPage` (not `currentPage`) ensures the timer only starts after a page fully settles — never mid-swipe.

  Below the `HorizontalPager`, add a `HorizontalPagerIndicator` (active dot: `AccentPrimary`, inactive: `TextSecondary`, centered, `spacing.sm` top margin).

---

**TSK-CAT-30 — Implement VodGrid (state router)**
- Effort: S
- Phase: 1
- Group: Screen Components
- Requirements: RQ-CAT-08, RQ-CAT-09, RQ-CAT-10, RQ-CAT-11, RQ-CAT-12, RQ-CAT-13, RQ-CAT-15, RQ-CAT-16
- Acceptance Criteria: AC-CAT-01, AC-CAT-02, AC-CAT-04, AC-CAT-05, AC-CAT-06
- Status: Not Started
- Depends on: TSK-CAT-25, TSK-CAT-26, TSK-CAT-27
- Creates:
  - `android/app/src/main/java/com/streamkit/app/feature/catalog/ui/components/VodGrid.kt`
- Details:
  A non-composable router function (or a `@Composable` that switches on `vodState`) rather than a full lazy layout — it returns the appropriate content for the caller (`CatalogScreen`'s `LazyVerticalGrid`) to place as full-span or cell items.

  Actually implement as a set of extension functions on `LazyGridScope`:

  ```kotlin
  fun LazyGridScope.vodGridItems(
      vodState: VodState,
      showRefreshErrorBanner: Boolean,
      onVodTap: (String) -> Unit,
      onRetry: () -> Unit,
  )
  ```

  This keeps all VOD-related item declarations in one place while letting `CatalogScreen` own the single root `LazyVerticalGrid`. Switch on `vodState` and emit the appropriate items/item blocks.

---

**TSK-CAT-31 — Implement CatalogScreen**
- Effort: M
- Phase: 1
- Group: Screen Components
- Requirements: All RQ-CAT
- Acceptance Criteria: All AC-CAT
- Status: Not Started
- Depends on: TSK-CAT-17, TSK-CAT-21, TSK-CAT-22, TSK-CAT-24, TSK-CAT-29, TSK-CAT-30
- Creates:
  - `android/app/src/main/java/com/streamkit/app/feature/catalog/ui/CatalogScreen.kt`
- Details:
  Root composable for the Catalog destination. Collects `uiState` via `collectAsStateWithLifecycle()`. Collects `uiEffect` in a `LaunchedEffect(Unit)` and calls `navController.navigate(...)` for each effect.

  Structure: `Scaffold(topBar = CatalogTopAppBar, containerColor = Background)` with a `LazyVerticalGrid(columns = Fixed(2), state = lazyGridState)` as the content.

  Grid items (full-span unless noted):
  1. Section header "Live"
  2. `LiveCarousel(items = uiState.liveItems, isVisible = isCarouselVisible, ...)`
  3. Section header "Videos"
  4. `vodGridItems(...)` (from TSK-CAT-30)

  **Visibility detection:** derive `isCarouselVisible` from `snapshotFlow { lazyGridState.layoutInfo.visibleItemsInfo.any { it.index == CAROUSEL_ITEM_INDEX } }`, collected as `State<Boolean>`. Pass to `LiveCarousel`. `CAROUSEL_ITEM_INDEX` is the grid item index of the carousel (1, given the Live header is index 0).

  Register `CatalogScreen` as the `"catalog"` destination in `AppNavGraph` (modify TSK-CAT-17's stub).

---

## Group 10 — Testing

---

**TSK-CAT-32 — Unit tests — CatalogViewModel**
- Effort: M
- Phase: 1
- Group: Testing
- Requirements: All RQ-CAT
- Acceptance Criteria: All AC-CAT
- Status: Not Started
- Depends on: TSK-CAT-21, TSK-CAT-22
- Creates:
  - `android/app/src/test/java/com/streamkit/app/feature/catalog/CatalogViewModelTest.kt`
- Details:
  Use `kotlinx-coroutines-test` with `StandardTestDispatcher`. Use hand-written fakes for `SeedLiveEntriesUseCase`, `SyncVodCatalogUseCase`, and `VideoRepository` — avoid Mockito.

  Test cases per `specs/features/catalog/design.md` — Testing Strategy section:
  - Initial state is `VodState.Loading`
  - Room emits non-empty list → `VodState.Content`
  - Room emits empty list after sync success → `VodState.Empty`
  - Sync failure, no prior content → `VodState.Error`
  - Sync failure, existing `Content` → `showRefreshErrorBanner = true`
  - `RetryFetch` → resets to `Loading`, re-runs sync
  - `LiveItemTapped(id)` → `NavigateToLivePlayer(id)` effect emitted
  - `VodItemTapped(id)` → `NavigateToPlayer(id)` effect emitted
  - `SettingsTapped` → `NavigateToSettings` effect emitted
  - `liveItems` updates independently of `vodState`

---

**TSK-CAT-33 — Unit tests — SyncVodCatalogUseCase and SeedLiveEntriesUseCase**
- Effort: S
- Phase: 1
- Group: Testing
- Requirements: RQ-CAT-14, RQ-CAT-20
- Acceptance Criteria: AC-CAT-01, AC-CAT-07
- Status: Not Started
- Depends on: TSK-CAT-18, TSK-CAT-19
- Creates:
  - `android/app/src/test/java/com/streamkit/app/feature/catalog/SyncVodCatalogUseCaseTest.kt`
  - `android/app/src/test/java/com/streamkit/app/feature/catalog/SeedLiveEntriesUseCaseTest.kt`
- Details:
  Both use cases delegate directly to the repository. Test cases:
  - Happy path: `Result.success` returned when repository succeeds
  - Failure path: `Result.failure` returned when repository returns failure
  - Use a fake `VideoRepository` that returns a configurable `Result`.

---

**TSK-CAT-34 — Room integration tests — VideoDao and VideoRepositoryImpl**
- Effort: M
- Phase: 1
- Group: Testing
- Requirements: RQ-CAT-18, RQ-CAT-20, RQ-CAT-21
- Acceptance Criteria: AC-CAT-03, AC-CAT-07
- Status: Not Started
- Depends on: TSK-CAT-06, TSK-CAT-13
- Creates:
  - `android/core/src/androidTest/java/com/streamkit/core/data/VideoRoomIntegrationTest.kt`
- Details:
  Use `Room.inMemoryDatabaseBuilder` with a fresh instance per test. Instrumented tests (run on device or emulator).

  Test cases per `specs/features/catalog/design.md` — Testing Strategy section:
  - `upsertAll` with 3 live entries → `observeLiveItems()` emits exactly 3 rows
  - Second `upsertAll` with same entries → no duplicates
  - `observeVodItems()` never returns LIVE type rows
  - `deleteStale(activeIds = freshVodIds + liveIds)` removes stale VOD rows only
  - `deleteStale` does not delete live entries when their IDs are included in `activeIds`
  - Assets with `status != "ready"` filtered by DAO query — do not appear in `observeVodItems()` emission

---

## Changelog

| Version | Date | Author | Notes |
|---|---|---|---|
| 0.1.0 | 2026-07-04 | Danielle Mariani | Initial draft — 34 tasks across 10 groups covering project foundation through testing; tasks intentionally scoped to single-file or tightly related file pairs for reviewable PRs |
| 0.1.1 | 2026-07-04 | Danielle Mariani | Fixed `VideoType` enum discrepancy found during pre-implementation review: TSK-CAT-05, TSK-CAT-06, TSK-CAT-16, and TSK-CAT-34 incorrectly described a 3-value Android enum (`VOD/LIVE/STATIC`); corrected to match `data-model.md`'s authoritative 2-value enum (`VOD/LIVE`). All three seeded Live entries now specified as `type = VideoType.LIVE`, consistent with `data-model.md` Open Schema Question #3 (no Android `source` field in Phase 1) |