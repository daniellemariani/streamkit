package com.dmariani.streamkit.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a single catalog row — either a Mux VOD asset
 * or one of the three static Live entries.
 */
@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val type: String, // "VOD" | "LIVE"
    val thumbnailUrl: String?,
    val streamUrl: String,
    val durationSeconds: Int?,
    val isDrmProtected: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * The two catalog content kinds a `VideoEntity` row can represent.
 */
enum class VideoType { VOD, LIVE }
