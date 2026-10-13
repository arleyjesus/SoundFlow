package com.example.comarleyaetheraudio.domain.library

import java.text.Normalizer
import java.util.Locale

/** Metadata-only projection. No audio reads, persistence or network access. */
data class SmartTrack(val id: Long, val title: String, val artist: String,
    val album: String, val genre: String, val durationMs: Long, val source: String)

enum class SessionStyle { VARIETY, FAVORITES, DISCOVERY }
enum class MatchKind { SAME_SOURCE, SIMILAR_METADATA }
data class PossibleDuplicates(val kind: MatchKind, val ids: List<Long>)

private fun normalized(value: String): String = Normalizer.normalize(value.trim(), Normalizer.Form.NFKC)
    .lowercase(Locale.ROOT).replace(Regex("\\s+"), " ")

private fun known(value: String): Boolean = value.isNotBlank() && value !in
    setOf("desconocido", "desconocida", "unknown", "<unknown>", "unknown artist", "artista desconocido")

/** Deterministic, explainable suggestions. Keeps IDs and avoids repeating an exact source. */
fun suggestSession(tracks: List<SmartTrack>, favorites: Set<Long>, style: SessionStyle,
    seedId: Long? = null, limit: Int = 30): List<Long> {
    require(limit in 1..100)
    val unique = tracks.distinctBy { it.id }
    val seed = unique.firstOrNull { it.id == seedId }
    val candidates = unique.filter { when (style) {
        SessionStyle.VARIETY -> true
        SessionStyle.FAVORITES -> it.id in favorites
        SessionStyle.DISCOVERY -> it.id !in favorites
    } }
    fun score(track: SmartTrack): Int {
        val genre = normalized(track.genre)
        return (if (track.id in favorites) 2 else 0) +
            (if (seed != null && known(genre) && genre == normalized(seed.genre)) 4 else 0)
    }
    val groups = candidates.sortedWith(compareByDescending<SmartTrack> { score(it) }.thenBy { it.id })
        .groupBy { normalized(it.artist) }.values.map { it.toMutableList() }
    val result = ArrayList<Long>()
    val ids = HashSet<Long>()
    val sources = HashSet<String>()
    fun add(track: SmartTrack) {
        if (track.id in ids || (track.source.isNotBlank() && track.source in sources)) return
        ids.add(track.id)
        if (track.source.isNotBlank()) sources.add(track.source)
        result.add(track.id)
    }
    if (style == SessionStyle.VARIETY && seed != null) add(seed)
    // Round-robin artists keeps a single artist from occupying the entire suggestion.
    var index = 0
    while (result.size < limit && groups.any { index < it.size }) {
        for (group in groups) {
            if (result.size == limit) break
            group.getOrNull(index)?.let(::add)
        }
        index++
    }
    return result
}

/** Candidates only, never proof of identical audio. No hashing and no delete operation. */
fun findPossibleDuplicates(tracks: List<SmartTrack>): List<PossibleDuplicates> {
    val unique = tracks.distinctBy { it.id }
    val results = ArrayList<PossibleDuplicates>()
    unique.filter { it.source.isNotBlank() }.groupBy { it.source }.values.filter { it.size > 1 }
        .forEach { results.add(PossibleDuplicates(MatchKind.SAME_SOURCE, it.map { track -> track.id })) }
    unique.filter { known(normalized(it.title)) && known(normalized(it.artist)) && it.durationMs > 0 }
        .groupBy { normalized(it.title) to normalized(it.artist) }.values.forEach { group ->
            val sorted = group.sortedWith(compareBy<SmartTrack> { it.durationMs }.thenBy { it.id })
            var start = 0
            while (start < sorted.size) {
                var end = start + 1
                while (end < sorted.size && sorted[end].durationMs - sorted[start].durationMs <= 2000L) end++
                val cluster = sorted.subList(start, end)
                if (cluster.size > 1 && cluster.map { it.source }.distinct().size > 1) {
                    results.add(PossibleDuplicates(MatchKind.SIMILAR_METADATA, cluster.map { it.id }))
                }
                start = end
            }
        }
    return results
}
