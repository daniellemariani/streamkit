package com.dmariani.streamkit.core.domain.repository

import com.dmariani.streamkit.core.domain.model.Video
import kotlinx.coroutines.flow.Flow

/**
 * Contract for accessing and refreshing the video catalog, hiding the
 * underlying Room cache and Mux sync details from callers.
 */
interface VideoRepository {
    fun observeVodItems(): Flow<List<Video>>
    fun observeLiveItems(): Flow<List<Video>>
    suspend fun seedLiveEntries(): Result<Unit>
    suspend fun syncVodCatalog(): Result<Unit>
}
