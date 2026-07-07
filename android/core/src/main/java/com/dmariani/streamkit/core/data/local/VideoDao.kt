package com.dmariani.streamkit.core.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Upsert
    suspend fun upsertAll(videos: List<VideoEntity>)

    @Query("SELECT * FROM videos WHERE type = 'VOD'")
    fun observeVodItems(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE type = 'LIVE'")
    fun observeLiveItems(): Flow<List<VideoEntity>>

    @Query("SELECT id FROM videos WHERE type != 'VOD'")
    suspend fun getLiveIds(): List<String>

    @Query("DELETE FROM videos WHERE id NOT IN (:activeIds)")
    suspend fun deleteStale(activeIds: List<String>)
}
