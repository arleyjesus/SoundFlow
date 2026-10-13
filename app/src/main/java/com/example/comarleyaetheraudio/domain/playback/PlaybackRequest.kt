package com.example.comarleyaetheraudio.domain.playback

data class PlaybackRequest(val ids: List<Long>, val startIndex: Int, val shuffle: Boolean)

/** A new user selection snapshots the visible order; never silently keeps an old shuffle mode. */
fun playbackRequest(selectedId: Long, visibleIds: List<Long>, shuffle: Boolean = false): PlaybackRequest {
    val ids = if (selectedId in visibleIds) visibleIds.distinct() else listOf(selectedId)
    return PlaybackRequest(ids, ids.indexOf(selectedId), shuffle)
}
