package com.dmariani.streamkit.core.domain.model

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
