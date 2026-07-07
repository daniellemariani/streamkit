package com.dmariani.streamkit.core.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response shape for Mux's `GET /video/v1/assets` — a page of assets plus
 * the cursor for fetching the next page.
 */
@Serializable
data class MuxListAssetsResponse(
    val data: List<MuxAssetDto>,
    @SerialName("next_cursor") val nextCursor: String?,
)

/**
 * A single Mux asset as returned by the API — mapped into `VideoEntity`
 * by the repository, never used outside the data layer.
 */
@Serializable
data class MuxAssetDto(
    val id: String,
    val status: String,
    val duration: Double?,
    @SerialName("playback_ids") val playbackIds: List<MuxPlaybackIdDto>?,
)

/**
 * A single playback ID on a Mux asset, used to derive thumbnail and
 * stream URLs.
 */
@Serializable
data class MuxPlaybackIdDto(
    val id: String,
    val policy: String,
)
