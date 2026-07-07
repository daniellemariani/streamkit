package com.dmariani.streamkit.core.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MuxListAssetsResponse(
    val data: List<MuxAssetDto>,
    @SerialName("next_cursor") val nextCursor: String?,
)

@Serializable
data class MuxAssetDto(
    val id: String,
    val status: String,
    val duration: Double?,
    @SerialName("playback_ids") val playbackIds: List<MuxPlaybackIdDto>?,
)

@Serializable
data class MuxPlaybackIdDto(
    val id: String,
    val policy: String,
)
