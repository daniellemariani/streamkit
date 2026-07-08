package com.dmariani.streamkit.core.data.local

/**
 * Static metadata for the 3 always-on Live carousel entries. Holds
 * everything except `id`/`createdAt`/`updatedAt`, which `VideoRepositoryImpl`
 * generates once, on the actual first app launch (see `seedLiveEntries()`).
 */
object LiveSeedConfig {

    data class Entry(
        val title: String,
        val description: String,
        val streamUrl: String,
        val durationSeconds: Int?,
        val isDrmProtected: Boolean,
    )

    val entries: List<Entry> = listOf(
        Entry(
            title = "Red Bull TV — Best of Red Bull",
            description = "24/7 curated stream of Red Bull original content.",
            streamUrl = "https://rbmn-live.akamaized.net/hls/live/590964/BoRB-AT/master.m3u8",
            durationSeconds = null,
            isDrmProtected = false,
        ),
        Entry(
            title = "DW English",
            description = "24/7 international news and current affairs from Deutsche Welle (Germany).",
            streamUrl = "https://dwamdstream107.akamaized.net/hls/live/2017968/dwstream107/stream05/streamPlaylist.m3u8",
            durationSeconds = null,
            isDrmProtected = false,
        ),
        Entry(
            title = "NHK World-Japan",
            description = "24/7 English-language news and culture coverage from Japan's NHK World.",
            streamUrl = "https://media-tyo.hls.nhkworld.jp/hls/w/live/master.m3u8",
            durationSeconds = null,
            isDrmProtected = false,
        ),
    )
}
