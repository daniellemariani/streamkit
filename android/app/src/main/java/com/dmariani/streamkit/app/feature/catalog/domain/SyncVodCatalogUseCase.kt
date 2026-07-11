package com.dmariani.streamkit.app.feature.catalog.domain

import com.dmariani.streamkit.core.domain.repository.VideoRepository
import javax.inject.Inject

/**
 * Refreshes the VOD catalog cache from Mux.
 */
class SyncVodCatalogUseCase @Inject constructor(
    private val videoRepository: VideoRepository,
) {
    suspend fun execute(): Result<Unit> = videoRepository.syncVodCatalog()
}
