package com.dmariani.streamkit.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

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

enum class VideoType { VOD, LIVE }
