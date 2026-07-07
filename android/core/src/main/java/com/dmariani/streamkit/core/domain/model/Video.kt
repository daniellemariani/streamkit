package com.dmariani.streamkit.core.domain.model

/**
 * Domain model for a single catalog entry — either a VOD asset or a
 * live stream. Mirrors `VideoEntity` with no Room dependency.
 */
data class Video(
    val id: String,
    val title: String,
    val description: String?,
    val type: String,
    val thumbnailUrl: String?,
    val streamUrl: String,
    val durationSeconds: Int?,
    val isDrmProtected: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
