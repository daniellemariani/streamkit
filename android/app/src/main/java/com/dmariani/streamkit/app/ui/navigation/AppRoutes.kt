package com.dmariani.streamkit.app.ui.navigation

/**
 * Route constants and builder functions for `AppNavGraph`'s destinations.
 * `CATALOG` is the start destination — see `specs/design/navigation.md`.
 */
object AppRoutes {
    const val VIDEO_ID_ARG = "video_id"

    const val CATALOG = "catalog"
    const val PLAYER = "player/{$VIDEO_ID_ARG}"
    const val LIVE_PLAYER = "live_player/{$VIDEO_ID_ARG}"
    const val SETTINGS = "settings"

    fun player(videoId: String) = "player/$videoId"
    fun livePlayer(videoId: String) = "live_player/$videoId"
}
