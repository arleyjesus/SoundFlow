package com.example.comarleyaetheraudio.domain.playback

internal fun playbackDuration(reported: Long, fallback: Long): Long =
    if (reported > 0) reported else fallback.coerceAtLeast(0)

internal fun boundedPlaybackPosition(position: Long, duration: Long): Long =
    position.coerceIn(0, duration.coerceAtLeast(0))

internal fun shiftedPlaybackPosition(position: Long, offset: Long, duration: Long): Long {
    val end = duration.coerceAtLeast(0)
    val current = boundedPlaybackPosition(position, end)
    return current + offset.coerceIn(-current, end - current)
}
