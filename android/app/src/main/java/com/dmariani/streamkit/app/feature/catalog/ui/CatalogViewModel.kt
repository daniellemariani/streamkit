package com.dmariani.streamkit.app.feature.catalog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmariani.streamkit.app.feature.catalog.domain.SeedLiveEntriesUseCase
import com.dmariani.streamkit.app.feature.catalog.domain.SyncVodCatalogUseCase
import com.dmariani.streamkit.core.domain.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel for `CatalogScreen` — seeds Live entries, subscribes to the
 * Room cache, and syncs the VOD catalog from Mux.
 */
@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val seedLiveEntriesUseCase: SeedLiveEntriesUseCase,
    private val syncVodCatalogUseCase: SyncVodCatalogUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<CatalogUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            seedLiveEntriesUseCase.execute()

            launch {
                videoRepository.observeLiveItems().collect { items ->
                    _uiState.update { it.copy(liveItems = items) }
                }
            }

            launch {
                videoRepository.observeVodItems().collect { items ->
                    if (items.isNotEmpty()) {
                        _uiState.update { it.copy(vodState = VodState.Content(items)) }
                    }
                }
            }

            launch {
                syncVodCatalog()
            }
        }
    }

    fun onEvent(event: CatalogEvent) {
        when (event) {
            is CatalogEvent.LiveItemTapped ->
                viewModelScope.launch {
                    _uiEffect.send(CatalogUiEffect.NavigateToLivePlayer(event.videoId))
                }

            is CatalogEvent.VodItemTapped ->
                viewModelScope.launch {
                    _uiEffect.send(CatalogUiEffect.NavigateToPlayer(event.videoId))
                }

            CatalogEvent.SettingsTapped ->
                viewModelScope.launch {
                    _uiEffect.send(CatalogUiEffect.NavigateToSettings)
                }

            CatalogEvent.RetryFetch ->
                viewModelScope.launch {
                    _uiState.update { it.copy(vodState = VodState.Loading) }
                    syncVodCatalog()
                }
        }
    }

    private suspend fun syncVodCatalog() {
        val result = syncVodCatalogUseCase.execute()
        result.onFailure {
            _uiState.update { state ->
                if (state.vodState is VodState.Content) {
                    state.copy(showRefreshErrorBanner = true)
                } else {
                    state.copy(vodState = VodState.Error)
                }
            }
        }
        result.onSuccess {
            _uiState.update { it.copy(showRefreshErrorBanner = false) }
            // Room's invalidation tracker doesn't fire on true no-op writes, so if sync
            // succeeds with zero ready assets and the VOD table was already empty, the
            // Flow subscription in init{} never re-emits and vodState stays Loading.
            _uiState.update { state ->
                if (state.vodState is VodState.Loading) state.copy(vodState = VodState.Empty) else state
            }
        }
    }
}
