package com.dmariani.streamkit.app.feature.catalog.ui

import com.dmariani.streamkit.core.domain.model.Video

/**
 * MVI state for `CatalogScreen`. Live and VOD sections evaluate their
 * loading/empty/error states independently (RQ-CAT-14).
 */
data class CatalogUiState(
    val liveItems: List<Video> = emptyList(),
    val vodState: VodState = VodState.Loading,
    val showRefreshErrorBanner: Boolean = false,
)

/**
 * Loading/content/empty/error states for the VOD grid.
 */
sealed class VodState {
    data object Loading : VodState()
    data class Content(val items: List<Video>) : VodState()
    data object Empty : VodState()
    data object Error : VodState()
}

/**
 * User intents handled by `CatalogViewModel.onEvent`.
 */
sealed class CatalogEvent {
    data class LiveItemTapped(val videoId: String) : CatalogEvent()
    data class VodItemTapped(val videoId: String) : CatalogEvent()
    data object SettingsTapped : CatalogEvent()
    data object RetryFetch : CatalogEvent()
}

/**
 * One-time navigation side effects emitted by `CatalogViewModel`.
 */
sealed class CatalogUiEffect {
    data class NavigateToLivePlayer(val videoId: String) : CatalogUiEffect()
    data class NavigateToPlayer(val videoId: String) : CatalogUiEffect()
    data object NavigateToSettings : CatalogUiEffect()
}
