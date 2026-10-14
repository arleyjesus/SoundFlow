package com.example.comarleyaetheraudio.domain.library

import java.text.Normalizer
import java.util.Locale

enum class LibraryOrder(val label: String) {
    ADDED_NEWEST("Más recientes"), ADDED_OLDEST("Más antiguas"),
    TITLE("Título A–Z"), ARTIST("Artista A–Z")
}

private val combiningMarks = Regex("\\p{M}+")

private fun orderKey(value: String) = Normalizer.normalize(value.trim(), Normalizer.Form.NFKD)
    .replace(combiningMarks, "").lowercase(Locale.ROOT)

fun <T> orderedLibrary(items: List<T>, order: LibraryOrder, id: (T) -> Long,
    title: (T) -> String, artist: (T) -> String, addedAt: (T) -> Long = { 0L }): List<T> = when (order) {
    LibraryOrder.ADDED_NEWEST -> items.sortedWith(compareByDescending<T> { addedAt(it).coerceAtLeast(0) }
        .thenByDescending(id))
    LibraryOrder.ADDED_OLDEST -> items.sortedWith(compareBy<T> { addedAt(it) <= 0 }
        .thenBy { addedAt(it).coerceAtLeast(0) }.thenBy(id))
    LibraryOrder.TITLE, LibraryOrder.ARTIST -> {
        // Normalize once per item, not once per comparator invocation (O(n log n)).
        val keyed = items.map { KeyedItem(it, id(it), orderKey(title(it)),
            if (order == LibraryOrder.ARTIST) orderKey(artist(it)) else "") }
        keyed.sortedWith(compareBy<KeyedItem<T>> { it.artist }
            .thenBy { it.title }.thenBy { it.id }).map { it.item }
    }
}

private data class KeyedItem<T>(val item: T, val id: Long, val title: String, val artist: String)
