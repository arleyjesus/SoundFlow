package com.example.comarleyaetheraudio.domain.library

/** Credits once per playback after 30 s (half of a shorter track). Seeks do not add time. */
class ListeningTracker {
    private var id: Long? = null
    private var previousPosition: Long? = null
    private var previousTime = 0L
    private var played = 0L
    private var credited = false
    fun reset() { id = null; previousPosition = null; played = 0; credited = false }
    fun sample(songId: Long?, position: Long, duration: Long, playing: Boolean, now: Long): Long? {
        if (id != songId) { reset(); id = songId }
        val previous = previousPosition
        val elapsed = now - previousTime
        if (playing && previous != null && elapsed in 1..2500) {
            val advance = position - previous
            if (advance in 1..(elapsed + 250)) played += minOf(advance, elapsed)
        }
        previousPosition = if (playing) position else null
        previousTime = now
        val threshold = if (duration > 0) minOf(30_000L, (duration / 2).coerceAtLeast(1000)) else 30_000L
        if (songId != null && !credited && played >= threshold) { credited = true; return songId }
        return null
    }
}

fun decodeListeningCounts(raw: String?): Map<Long, Int> = raw.orEmpty().split(';').take(2000).mapNotNull {
    val pair = it.split(':', limit = 2)
    val id = pair.firstOrNull()?.toLongOrNull()
    val count = pair.getOrNull(1)?.toIntOrNull()
    if (id != null && count != null && count in 1..1_000_000) id to count else null
}.toMap()

fun encodeListeningCounts(counts: Map<Long, Int>): String = counts.entries
    .filter { it.value > 0 }.sortedWith(compareByDescending<Map.Entry<Long, Int>> { it.value }.thenBy { it.key })
    .take(2000).joinToString(";") { "${it.key}:${it.value.coerceAtMost(1_000_000)}" }
