package com.dmariani.streamkit.core.data.repository

import com.dmariani.streamkit.core.data.local.LiveSeedConfig
import com.dmariani.streamkit.core.data.local.VideoDao
import com.dmariani.streamkit.core.data.local.VideoEntity
import com.dmariani.streamkit.core.data.local.VideoType
import com.dmariani.streamkit.core.domain.model.Video
import com.dmariani.streamkit.core.domain.repository.VideoRepository
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Default `VideoRepository` implementation — sources catalog data from
 * the local Room cache, kept in sync with Mux and the static live seed.
 */
class VideoRepositoryImpl @Inject constructor(
    private val videoDao: VideoDao,
) : VideoRepository {

    override fun observeVodItems(): Flow<List<Video>> =
        videoDao.observeVodItems().map { entities -> entities.map { it.toDomain() } }

    override fun observeLiveItems(): Flow<List<Video>> =
        videoDao.observeLiveItems().map { entities -> entities.map { it.toDomain() } }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun seedLiveEntries(): Result<Unit> = try {
        if (videoDao.getLiveIds().isEmpty()) {
            val now = System.currentTimeMillis()
            val liveEntries = LiveSeedConfig.entries.map { entry ->
                VideoEntity(
                    id = Uuid.random().toString(),
                    title = entry.title,
                    description = entry.description,
                    type = VideoType.LIVE.name,
                    thumbnailUrl = null,
                    streamUrl = entry.streamUrl,
                    durationSeconds = entry.durationSeconds,
                    isDrmProtected = entry.isDrmProtected,
                    createdAt = now,
                    updatedAt = now,
                )
            }
            videoDao.upsertAll(liveEntries)
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun syncVodCatalog(): Result<Unit> {
        TODO("Implemented in TSK-CAT-13")
    }

    private fun VideoEntity.toDomain() = Video(
        id = id,
        title = title,
        description = description,
        type = type,
        thumbnailUrl = thumbnailUrl,
        streamUrl = streamUrl,
        durationSeconds = durationSeconds,
        isDrmProtected = isDrmProtected,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
