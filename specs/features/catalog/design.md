# Catalog — Design

**Version:** 0.1.2
**Status:** Draft
**Phase:** 1 (Android)
**Owner:** Danielle Mariani
**Created at:** 2026-07-03
**Last Updated:** 2026-07-10

---

## Overview

This document defines the technical solution design for the Catalog feature on Android mobile (Phase 1). It covers the component structure, UiState and event model, composable tree, shared components, ViewModel design, navigation, dependency injection, and testing strategy.

All decisions here are consistent with `specs/features/catalog/requirements.md` (functional requirements) and `specs/design/design.md` (visual design system). Where this document references design tokens, those tokens are defined in `specs/design/design.md` and mapped to Material 3 via the app's theme.

---

## Related Documents

| Document | Purpose |
|---|---|
| specs/features/catalog/requirements.md | Functional requirements and acceptance criteria |
| specs/design/design.md | Design system — color tokens, typography, spacing, elevation |
| specs/design/navigation.md | Navigation flows and AppNavGraph structure |
| specs/technical/data-model.md | `VideoEntity`, `VideoDao`, `Video` domain model |
| ARCHITECTURE.md | MVI + Repository pattern, module structure, DI conventions |

---

## Architecture Overview

Catalog is a composable destination inside `MainActivity`'s `AppNavGraph` — it has no Activity or NavHost of its own. The ViewModel is scoped to the nav back stack entry (default `hiltViewModel()` behavior in Compose Navigation).

```
MainActivity
    └── AppNavGraph (NavHost)
            ├── CatalogScreen       ──► route: "catalog"   ← start destination
            ├── PlayerScreen        ──► route: "player/{video_id}"
            ├── LivePlayerScreen    ──► route: "live_player/{video_id}"
            └── SettingsScreen      ──► route: "settings"

core/data/local/
    └── VideoDao  ──► videos table (VideoEntity)

core/data/remote/
    └── MuxApiClient  ──► GET /video/v1/assets (Phase 1 VOD source)
```

**Key architectural constraints:**

- Catalog is the start destination. System back exits the app by default — no `BackHandler` is registered.
- All data access flows through `VideoRepository` (interface in `core/domain/`, implementation in `core/data/`). The ViewModel never touches `VideoDao` or `MuxApiClient` directly.
- `VideoRepository.observeVodItems()` and `observeLiveItems()` return `Flow<List<Video>>` from Room — the ViewModel subscribes in `init {}` and drives `CatalogUiState` from the emissions.
- Mux sync runs in parallel with Room observation. Cache-first: Room emits cached items immediately; sync updates them in place.
- Live entries are always seeded before the first UI frame from `CatalogViewModel.init {}`. Since seeding is a local Room upsert (no network), it completes synchronously before the Flow subscriptions produce their first emission.
- Portrait orientation is enforced at the `AppNavGraph` level by setting `requestedOrientation = SCREEN_ORIENTATION_PORTRAIT` when `CatalogScreen` is the current destination (per `ARCHITECTURE.md` Screen Orientation).

---

## Component Structure

```
app/feature/catalog/
├── ui/
│   ├── CatalogScreen.kt              # Root composable; collects UiState and UiEffect
│   ├── CatalogViewModel.kt           # MVI reducer; emits CatalogUiState and CatalogUiEffect
│   └── components/
│       ├── CatalogTopAppBar.kt       # "StreamKit" label + Settings gear icon
│       ├── LiveCarousel.kt           # HorizontalPager wrapper + pager dots
│       ├── LiveCarouselCard.kt       # 16:9 card with scrim, badge, stream name
│       ├── VodGrid.kt                # Delegates to Content / Loading / Empty / Error
│       ├── VodPosterCard.kt          # 2:3 card with title label below
│       ├── VodSkeletonGrid.kt        # Shimmer placeholder grid (loading state)
│       ├── CatalogEmptyState.kt      # Zero ready assets state
│       └── CatalogErrorState.kt      # Fetch failed, no cache state
└── di/
    └── CatalogModule.kt              # Placeholder; no Catalog-specific bindings in Phase 1

app/feature/catalog/domain/
├── SyncVodCatalogUseCase.kt          # Calls VideoRepository.syncVodCatalog()
└── SeedLiveEntriesUseCase.kt         # Calls VideoRepository.seedLiveEntries()
```

**Notes:**
- `VideoRepository` (interface) and `Video` domain model are in `core/domain/` — not Catalog-specific. No separate `CatalogRepository` is introduced.
- `LiveBadge` is a shared component defined in `core/ui/components/LiveBadge.kt`, not in this feature — it is reused by the Live Player screen's overlay as well.
- `CatalogModule.kt` exists as a structural placeholder consistent with the other feature modules. No `@Binds` or `@Provides` are needed in Phase 1 because `VideoRepository` is already bound in `core/di/RepositoryModule.kt` and use cases use `@Inject constructor`.

---

## String Resources

All user-facing copy is defined in `res/values/strings.xml`. No hardcoded strings in composables.

```xml
<!-- Catalog strings -->
<string name="catalog_app_bar_title">StreamKit</string>
<string name="catalog_settings_icon_description">Settings</string>
<string name="catalog_section_live">Live</string>
<string name="catalog_section_vod">Videos</string>
<string name="catalog_vod_empty_message">No videos available.</string>
<string name="catalog_vod_error_message">Couldn\'t load videos. Check your connection and try again.</string>
<string name="catalog_vod_error_retry">Retry</string>
<string name="catalog_vod_refresh_error_banner">Showing saved content — refresh failed.</string>
```

---

## Data Models

### CatalogUiState

```kotlin
data class CatalogUiState(
    val liveItems: List<Video> = emptyList(),
    val vodState: VodState = VodState.Loading,
    val showRefreshErrorBanner: Boolean = false,  // true when fetch failed but stale cache is displayed
)

sealed class VodState {
    data object Loading : VodState()
    data class Content(val items: List<Video>) : VodState()
    data object Empty : VodState()
    data object Error : VodState()
}
```

**State transition rules:**

| Condition | `vodState` | `showRefreshErrorBanner` |
|---|---|---|
| App launch, no cache, sync in progress | `Loading` | `false` |
| Cache present (any sync state) | `Content(cachedItems)` | `false` |
| Sync succeeded, items returned | `Content(freshItems)` | `false` |
| Sync succeeded, zero ready assets | `Empty` | `false` |
| Sync failed, cache exists | `Content(cachedItems)` | `true` |
| Sync failed, no cache | `Error` | `false` |
| Retry tapped from `Error` state | `Loading` | `false` |

---

### CatalogEvent (MVI Intent)

```kotlin
sealed class CatalogEvent {
    data class LiveItemTapped(val videoId: String) : CatalogEvent()
    data class VodItemTapped(val videoId: String) : CatalogEvent()
    data object SettingsTapped : CatalogEvent()
    data object RetryFetch : CatalogEvent()
}
```

---

### CatalogUiEffect (Side Effects)

```kotlin
sealed class CatalogUiEffect {
    data class NavigateToLivePlayer(val videoId: String) : CatalogUiEffect()
    data class NavigateToPlayer(val videoId: String) : CatalogUiEffect()
    data object NavigateToSettings : CatalogUiEffect()
}
```

> Navigation side effects are emitted via a `Channel<CatalogUiEffect>(Channel.BUFFERED)`, collected as a `Flow` in `CatalogScreen`. Navigation calls are made in the composable's `LaunchedEffect` — no navigation logic in the ViewModel.

---

## Screen Design

### CatalogScreen

**Route:** `"catalog"`

**Composable tree:**

```
CatalogScreen
    ├── LaunchedEffect(Unit) — collect CatalogUiEffect → navController.navigate(...)
    └── Scaffold(
            topBar = CatalogTopAppBar,
            containerColor = color.background.primary
        )
            └── LazyVerticalGrid(columns = Fixed(2), contentPadding = PaddingValues(spacing.md))
                    │
                    ├── item(span = maxLineSpan) — Section header "Live"
                    │       Text("Live", style = Heading1, color = color.text.primary)
                    │
                    ├── item(span = maxLineSpan) — LiveCarousel
                    │       (see LiveCarousel below)
                    │
                    ├── item(span = maxLineSpan) — Section header "Videos"
                    │       Text("Videos", style = Heading1, color = color.text.primary)
                    │
                    └── [VOD section — one of the following, based on vodState]
                            ├── [Loading]  item(span = maxLineSpan)
                            │               VodSkeletonGrid()
                            │
                            ├── [Content]  items(videos) { video ->
                            │               VodPosterCard(video, onClick = …)
                            │             }
                            │             + if showRefreshErrorBanner:
                            │               item(span = maxLineSpan)
                            │               RefreshErrorBanner()
                            │
                            ├── [Empty]    item(span = maxLineSpan)
                            │               CatalogEmptyState()
                            │
                            └── [Error]    item(span = maxLineSpan)
                                            CatalogErrorState(onRetry = …)
```

> **Layout note:** `LazyVerticalGrid` is the root lazy container, not `LazyColumn`. This is required because nesting a `LazyVerticalGrid` inside a `LazyColumn` is unsupported in Compose (both are lazy, with conflicting intrinsic height measurement). Full-width items (headers, carousel, state views) use `span = { GridItemSpan(maxLineSpan) }`. VOD card items use the default single-column span. See Implementation Notes.

**Design token reference:**

| Element | Token | Value |
|---|---|---|
| Screen background | `color.background.primary` | `#0B0D10` |
| Section header typography | Heading 1 | 22sp, Medium |
| Section header top padding | `spacing.lg` | 24dp |
| Grid horizontal padding | `spacing.md` | 16dp |
| Grid cell gap (horizontal + vertical) | `spacing.sm` | 8dp |
| VOD grid bottom padding | `spacing.xl` | 32dp |

**Back navigation:** Catalog is the start destination — system back exits the app by default. No `BackHandler` is registered.

---

### CatalogTopAppBar

**File:** `app/feature/catalog/ui/components/CatalogTopAppBar.kt`

```
CatalogTopAppBar
    └── TopAppBar(
            colors = TopAppBarColors(containerColor = color.background.primary)
        )
                ├── title: Text — "StreamKit" (Heading 1, color.text.primary, start-aligned)
                └── actions: IconButton — Settings gear (Material Symbols Outlined, 20dp, color.text.secondary)
                        └── Icon(Icons.Outlined.Settings, contentDescription = R.string.catalog_settings_icon_description)
```

**Design token reference:**

| Element | Token | Value |
|---|---|---|
| App bar background | `color.background.primary` | `#0B0D10` |
| Title typography | Heading 1 | 22sp, Medium |
| Title color | `color.text.primary` | `#F4F5F6` |
| Settings icon size | — | 20dp |
| Settings icon color | `color.text.secondary` | `#93999E` |

---

### LiveCarousel

**File:** `app/feature/catalog/ui/components/LiveCarousel.kt`

```
LiveCarousel
    └── Column
            ├── HorizontalPager(
            │       pageCount = 3,
            │       state = pagerState,
            │       userScrollEnabled = true   ← manual swipe supported; auto-advance also active
            │   )
            │       └── LiveCarouselCard(item, onClick)
            │               └── Box(
            │                       modifier = fillMaxWidth().aspectRatio(16f / 9f).clip(radius.lg),
            │                       color.surface.card
            │                   )
            │                       ├── AsyncImage(
            │                       │       model = video.thumbnailUrl,
            │                       │       contentScale = ContentScale.Crop,
            │                       │       fillMaxSize
            │                       │   )
            │                       ├── Box(  ← functional scrim for label legibility
            │                       │       modifier = fillMaxSize(),
            │                       │       background = Brush.verticalGradient(
            │                       │           transparent → Color.Black.copy(alpha = 0.6f)
            │                       │       )
            │                       │   )
            │                       ├── LiveBadge(  ← top-left corner
            │                       │       modifier = align(TopStart).padding(spacing.sm)
            │                       │   )
            │                       └── Text(  ← stream name, bottom-left
            │                               text = video.title,
            │                               style = Label,
            │                               color = color.text.primary,
            │                               modifier = align(BottomStart).padding(spacing.sm)
            │                           )
            │
            └── HorizontalPagerIndicator(   ← pager dots, centered below carousel
                    pagerState = pagerState,
                    pageCount = 3,
                    activeColor = color.accent.primary,
                    inactiveColor = color.text.secondary,
                    modifier = align(CenterHorizontally).padding(top = spacing.sm)
                )
```

**Design token reference:**

| Element | Token | Value |
|---|---|---|
| Card background (behind image) | `color.surface.card` | `#16191D` |
| Card aspect ratio | — | 16:9 |
| Card corner radius | `radius.lg` | 16dp |
| Scrim gradient | — | transparent → `Color.Black.copy(0.6f)`, vertical, bottom 40% |
| Stream name typography | Label | 12sp, Medium |
| Stream name color | `color.text.primary` | `#F4F5F6` |
| Stream name / badge padding | `spacing.sm` | 8dp |
| Active pager dot color | `color.accent.primary` | `#2EE6C7` |
| Inactive pager dot color | `color.text.secondary` | `#93999E` |
| Dots top margin | `spacing.sm` | 8dp |
| Auto-advance interval | — | 8 000ms |

---

### VodPosterCard

**File:** `app/feature/catalog/ui/components/VodPosterCard.kt`

```
VodPosterCard
    └── Column(modifier = fillMaxWidth())
            ├── AsyncImage(
            │       model = video.thumbnailUrl,
            │       contentScale = ContentScale.Crop,
            │       modifier = fillMaxWidth().aspectRatio(2f / 3f).clip(radius.lg)
            │   )
            └── Text(
                    text = video.title,
                    style = BodySmall,
                    color = color.text.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = padding(top = spacing.xs)
                )
```

**Design token reference:**

| Element | Token | Value |
|---|---|---|
| Card aspect ratio | — | 2:3 |
| Card corner radius | `radius.lg` | 16dp |
| Title typography | Body Small | 13sp, Regular |
| Title color | `color.text.primary` | `#F4F5F6` |
| Title top padding | `spacing.xs` | 4dp |
| Title max lines | — | 2 |

---

### VodSkeletonGrid

**File:** `app/feature/catalog/ui/components/VodSkeletonGrid.kt`

Renders 6 placeholder cards (3 rows × 2 columns) to approximate the shape of a loaded VOD grid. Each placeholder matches `VodPosterCard`'s dimensions exactly (2:3 aspect ratio, `radius.lg`).

```
VodSkeletonGrid
    └── [Repeat 6 times]
            SkeletonCard(
                modifier = fillMaxWidth().aspectRatio(2f / 3f).clip(radius.lg),
                color = color.surface.alt,   ← shimmer target color
                shimmer = true
            )
```

Shimmer implementation: a left-to-right animated gradient from `color.surface.alt` → `color.surface.card` → `color.surface.alt`, cycling on a 1200ms loop. Use `InfiniteTransition` with a translating offset.

---

### CatalogEmptyState

**File:** `app/feature/catalog/ui/components/CatalogEmptyState.kt`

```
CatalogEmptyState
    └── Column(horizontalAlignment = CenterHorizontally, verticalArrangement = Center)
            ├── Icon(Icons.Outlined.VideoLibrary, tint = color.text.secondary, size = 48dp)
            └── Text(
                    text = R.string.catalog_vod_empty_message,  // "No videos available."
                    style = Body,
                    color = color.text.secondary,
                    textAlign = Center
                )
```

---

### CatalogErrorState

**File:** `app/feature/catalog/ui/components/CatalogErrorState.kt`

Shown only when the Mux fetch fails and no cache exists. Not shown when a stale cache is visible — that case uses `RefreshErrorBanner` (inline, non-blocking).

```
CatalogErrorState
    └── Column(horizontalAlignment = CenterHorizontally, verticalArrangement = Center)
            ├── Icon(Icons.Outlined.CloudOff, tint = color.semantic.error, size = 48dp)
            ├── Text(
            │       text = R.string.catalog_vod_error_message,
            │       style = Body,
            │       color = color.text.primary,
            │       textAlign = Center
            │   )
            └── Button(
                    onClick = { onRetry() },
                    colors = ButtonDefaults.buttonColors(containerColor = color.accent.primary)
                )
                    Text(R.string.catalog_vod_error_retry, color = color.accent.on)
```

---

## Shared Components

### LiveBadge

**File:** `core/ui/components/LiveBadge.kt`

Shared with the Live Player screen's overlay. Not defined in this feature.

```kotlin
@Composable
fun LiveBadge(modifier: Modifier = Modifier)
```

Renders a solid `color.semantic.error` pill with "Live" in `Label` typography. See `specs/design/design.md` — Live Badge section for full spec.

---

## ViewModel

### CatalogViewModel

**File:** `app/feature/catalog/ui/CatalogViewModel.kt`

Scoped to the `CatalogScreen` nav back stack entry via `hiltViewModel()`.

**State:**
```kotlin
private val _uiState = MutableStateFlow(CatalogUiState())
val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()
```

**Side effects:**
```kotlin
private val _uiEffect = Channel<CatalogUiEffect>(Channel.BUFFERED)
val uiEffect = _uiEffect.receiveAsFlow()
```

**Initialization:**

Runs once in `init {}`. Sequence:

1. Call `SeedLiveEntriesUseCase.execute()` — upserts the 3 static Live entries into Room (idempotent). Fast, local-only.
2. Subscribe to `videoRepository.observeLiveItems()` — updates `uiState.liveItems` on each emission.
3. Subscribe to `videoRepository.observeVodItems()` — drives `uiState.vodState` per the transition rules in the Data Models section.
4. Call `syncVodCatalog()` — triggers the Mux fetch.

Steps 2, 3, and 4 launch concurrently within `viewModelScope`.

**Key event handlers:**

| Event | Handler |
|---|---|
| `LiveItemTapped(videoId)` | Emit `CatalogUiEffect.NavigateToLivePlayer(videoId)` |
| `VodItemTapped(videoId)` | Emit `CatalogUiEffect.NavigateToPlayer(videoId)` |
| `SettingsTapped` | Emit `CatalogUiEffect.NavigateToSettings` |
| `RetryFetch` | Set `vodState = VodState.Loading`, re-run `syncVodCatalog()` |

**`syncVodCatalog()` logic:**

```kotlin
private suspend fun syncVodCatalog() {
    val result = syncVodCatalogUseCase.execute()
    result.onFailure {
        _uiState.update { state ->
            if (state.vodState is VodState.Content) {
                state.copy(showRefreshErrorBanner = true)   // stale cache: show banner, keep content
            } else {
                state.copy(vodState = VodState.Error)        // no cache: show error state
            }
        }
    }
    result.onSuccess {
        _uiState.update { it.copy(showRefreshErrorBanner = false) }
        // vodState normally updates via the Room Flow subscription (step 3 in init{}) —
        // but Room's invalidation tracker only fires on rows actually written. If sync
        // succeeds with zero ready assets AND the VOD table was already empty (e.g. the
        // very first sync ever), upsertAll/deleteStale are true no-ops and the Flow never
        // re-emits, leaving vodState stuck at Loading. So: explicitly check here too.
        _uiState.update { state ->
            if (state.vodState is VodState.Loading) state.copy(vodState = VodState.Empty) else state
        }
    }
}
```

---

## Navigation

### Route Constants

```kotlin
// app/ui/navigation/AppRoutes.kt
object AppRoutes {
    const val CATALOG = "catalog"
    const val PLAYER = "player/{video_id}"
    const val LIVE_PLAYER = "live_player/{video_id}"
    const val SETTINGS = "settings"

    fun player(videoId: String) = "player/$videoId"
    fun livePlayer(videoId: String) = "live_player/$videoId"
}
```

### Effect Collection in CatalogScreen

```kotlin
@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is CatalogUiEffect.NavigateToPlayer ->
                    navController.navigate(AppRoutes.player(effect.videoId))
                is CatalogUiEffect.NavigateToLivePlayer ->
                    navController.navigate(AppRoutes.livePlayer(effect.videoId))
                CatalogUiEffect.NavigateToSettings ->
                    navController.navigate(AppRoutes.SETTINGS)
            }
        }
    }

    // ... composable content
}
```

---

## Dependency Injection

### DatabaseModule (core — already exists)

```kotlin
// core/di/DatabaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): StreamKitDatabase =
        Room.databaseBuilder(ctx, StreamKitDatabase::class.java, "streamkit.db").build()

    @Provides
    fun provideVideoDao(db: StreamKitDatabase): VideoDao = db.videoDao()
}
```

### RepositoryModule (core — already exists)

```kotlin
// core/di/RepositoryModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindVideoRepository(impl: VideoRepositoryImpl): VideoRepository
}
```

### CatalogModule

```kotlin
// app/feature/catalog/di/CatalogModule.kt
@Module
@InstallIn(ViewModelComponent::class)
abstract class CatalogModule {
    // No Catalog-specific bindings in Phase 1.
    // SyncVodCatalogUseCase and SeedLiveEntriesUseCase use @Inject constructor
    // and resolve VideoRepository from core's RepositoryModule.
}
```

---

## Implementation Notes

### LazyVerticalGrid as Root Layout

`LazyColumn` cannot contain a `LazyVerticalGrid` as a direct or indirect child — Compose prohibits nested lazy containers because they produce conflicting intrinsic height measurements. Attempting this causes a crash at runtime.

`CatalogScreen` solves this by using `LazyVerticalGrid` as the **single root lazy container**. Full-width items (section headers, Live carousel, state views) use `GridItemSpan(maxLineSpan)` to span both columns:

```kotlin
LazyVerticalGrid(columns = GridCells.Fixed(2)) {
    item(span = { GridItemSpan(maxLineSpan) }) { SectionHeader("Live") }
    item(span = { GridItemSpan(maxLineSpan) }) { LiveCarousel(...) }
    item(span = { GridItemSpan(maxLineSpan) }) { SectionHeader("Videos") }

    when (vodState) {
        is VodState.Content -> {
            items(vodState.items) { video -> VodPosterCard(video) }
            if (showRefreshErrorBanner) {
                item(span = { GridItemSpan(maxLineSpan) }) { RefreshErrorBanner() }
            }
        }
        VodState.Loading ->
            item(span = { GridItemSpan(maxLineSpan) }) { VodSkeletonGrid() }
        VodState.Empty ->
            item(span = { GridItemSpan(maxLineSpan) }) { CatalogEmptyState() }
        VodState.Error ->
            item(span = { GridItemSpan(maxLineSpan) }) { CatalogErrorState(onRetry = ...) }
    }
}
```

---

### Idempotent Live Entry Seeding

`SeedLiveEntriesUseCase` calls `VideoRepository.seedLiveEntries()`, which calls `VideoDao.upsertAll(liveEntries)`. Because `upsertAll` uses `@Upsert`, running it multiple times produces no duplicates and does not overwrite data for existing rows with newer timestamps. The 3 Live entries defined in `specs/features/catalog/requirements.md` RQ-CAT-20 are the source of truth for this seed; UUIDs are stable and hardcoded in the implementation.

---

### Stale Row Cleanup Excludes Live Entries

`VideoDao.deleteStale(activeIds)` deletes rows whose `id` is not in the provided list. After a successful Mux sync, the call is:

```kotlin
val liveIds = videoDao.getLiveIds()          // always-retain: the 3 seeded UUIDs
val freshVodIds = fetchedAssets.map { it.id }
videoDao.deleteStale(activeIds = freshVodIds + liveIds)
```

This ensures that a Mux sync never deletes the static Live entries, regardless of what the Mux API returns.

---

### Live Carousel Auto-Advance

The Live carousel advances to the next page automatically every 8 seconds, cyclically (page 2 → page 0). Auto-advance is:
- **Reset** when the user manually swipes to a new page
- **Paused** when the carousel is scrolled out of view (user is browsing the VOD grid)

**Visibility detection:** `CatalogScreen` holds a `LazyGridState` passed to the root `LazyVerticalGrid`. A `snapshotFlow` over `lazyGridState.layoutInfo.visibleItemsInfo` derives `isCarouselVisible: Boolean`, which is passed down to `LiveCarousel` as a parameter.

**Timer implementation** inside `LiveCarousel`:

```kotlin
@Composable
fun LiveCarousel(
    items: List<Video>,
    isVisible: Boolean,
    onItemClick: (String) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { items.size })

    // Auto-advance: resets on each settled page change (swipe or auto); pauses when not visible
    LaunchedEffect(pagerState.settledPage, isVisible) {
        if (!isVisible) return@LaunchedEffect
        delay(8_000)
        val nextPage = (pagerState.settledPage + 1) % items.size
        pagerState.animateScrollToPage(nextPage)
    }

    // ... HorizontalPager and PagerIndicator
}
```

**Key decisions:**
- `pagerState.settledPage` (not `currentPage`) is the `LaunchedEffect` key — this ensures the 8s timer only starts once the page has fully settled, whether from a swipe or a prior auto-advance. Using `currentPage` would restart the timer mid-swipe on every intermediate page.
- `isVisible` is the second key — when the carousel scrolls back into view, the effect re-launches and the full 8s interval elapses before the next advance. This avoids an immediate jump when the user returns to the top.
- `animateScrollToPage` is used (not `scrollToPage`) so the transition is visible to the user rather than a hard cut.

---

### Back Behavior

Catalog is the `startDestination` of `AppNavGraph`. Compose Navigation's default back behavior for a start destination is to allow the system to handle back, which exits the app. No `BackHandler` is needed or registered.

---

## Error Handling

| Error scenario | Detection point | UiState change | User-facing behavior |
|---|---|---|---|
| Mux fetch fails, no cache | `SyncVodCatalogUseCase` returns `Result.failure` | `vodState = VodState.Error` | Full-section error state with message and Retry button |
| Mux fetch fails, cache exists | `SyncVodCatalogUseCase` returns `Result.failure` | `showRefreshErrorBanner = true` | Stale cached grid shown; non-blocking inline banner below grid |
| Retry tapped | `CatalogEvent.RetryFetch` | `vodState = VodState.Loading, showRefreshErrorBanner = false` | Loading state, re-triggers sync |
| Zero ready assets returned | Sync succeeds; Room emits empty list; `isSyncing` complete | `vodState = VodState.Empty` | Empty state with neutral message |
| Live stream fails post-tap | In `LivePlayerScreen`, not Catalog | — | Out of scope here — see `specs/features/live-player/design.md` |

All errors are surfaced as fields in `CatalogUiState` — no exceptions thrown to the UI layer. The ViewModel catches all exceptions from use cases and maps them to state. No Snackbars — errors are either full-section states or the inline refresh banner.

---

## Testing Strategy

### Unit Tests

| Class | What to test |
|---|---|
| `CatalogViewModel` | Initial state is `VodState.Loading`. Room emission of non-empty list transitions to `VodState.Content`. Room emission of empty list after sync success transitions to `VodState.Empty`. Sync failure with no prior content → `VodState.Error`. Sync failure with existing `VodState.Content` → `showRefreshErrorBanner = true`. `RetryFetch` resets state to `Loading` and re-runs sync. `LiveItemTapped` emits `NavigateToLivePlayer` effect. `VodItemTapped` emits `NavigateToPlayer` effect. `SettingsTapped` emits `NavigateToSettings` effect. Live items always populated from Room observation regardless of VOD state. |
| `SyncVodCatalogUseCase` | Happy path: returns `Result.success` after repository sync. Repository failure: returns `Result.failure`. |
| `SeedLiveEntriesUseCase` | Happy path: delegates to `VideoRepository.seedLiveEntries()`. Repository failure: returns `Result.failure`. |

### Integration Tests

| Scope | What to test |
|---|---|
| Room (in-memory DB) | `seedLiveEntries()` upserts exactly 3 Live rows on empty DB. Second call is a no-op (no duplicates, no field overwrites). `deleteStale` with live IDs included in the retain list does not delete live entries. `observeVodItems()` emits only `VOD` type rows. `observeLiveItems()` emits only `LIVE` type rows. |

### UI Tests

Deferred — consistent with the global testing strategy in `ARCHITECTURE.md`. UI tests for Catalog are not required for Phase 1.

---

## Changelog

| Version | Date | Author | Notes |
|---|---|---|---|
| 0.1.0 | 2026-07-03 | Danielle Mariani | Initial draft — Catalog as a `LazyVerticalGrid`-rooted destination in `AppNavGraph`; MVI with `CatalogUiState`/`CatalogEvent`/`CatalogUiEffect`; 16:9 Live carousel cards, 2:3 VOD poster cards; documented nested-lazy constraint and stale-entry-exclusion implementation notes |
| 0.1.1 | 2026-07-04 | Danielle Mariani | Added Live carousel auto-advance: 8s interval, resets on manual swipe (`pagerState.settledPage` key), pauses when carousel is out of view (`isVisible` key + `LazyGridState` visibility detection). Added tracking note for pending `specs/design/design.md` Motion Guidelines update |
| 0.1.2 | 2026-07-10 | Danielle Mariani | Fixed a misleading comment in `syncVodCatalog()`'s code sample, found during TSK-CAT-22 implementation: `// vodState updates automatically via Room Flow observation` doesn't hold when sync succeeds with zero ready assets and the VOD table was already empty — Room's invalidation tracker doesn't fire on true no-op writes, so the Flow never re-emits and `vodState` would stay stuck at `Loading`. Added an explicit `if (state.vodState is VodState.Loading) → Empty` check in the success branch |