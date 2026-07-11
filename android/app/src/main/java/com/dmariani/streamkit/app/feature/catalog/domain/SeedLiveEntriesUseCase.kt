package com.dmariani.streamkit.app.feature.catalog.domain

import com.dmariani.streamkit.core.domain.repository.VideoRepository
import javax.inject.Inject

/**
 * Seeds the 3 static Live entries into the catalog cache on first launch.
 */
class SeedLiveEntriesUseCase @Inject constructor(
    private val videoRepository: VideoRepository,
) {
    suspend fun execute(): Result<Unit> = videoRepository.seedLiveEntries()
}
