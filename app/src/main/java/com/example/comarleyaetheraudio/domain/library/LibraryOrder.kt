package com.example.comarleyaetheraudio.domain.library

import java.text.Normalizer
import java.util.Locale

enum class LibraryOrder(val label: String) {
    ADDED_NEWEST("Añadidas recientemente"), ADDED_OLDEST("Añadidas antes"),
    TITLE("Título A–Z"), ARTIST("Artista A–Z")
}

private fun orderKey(value: String) = Normalizer.normalize(value.trim(), Normalizer.Form.NFKD)
    .replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT)

fun <T> orderedLibrary(items: List<T>, order: LibraryOrder, id: (T) -> Long,
    title: (T) -> String, artist: (T) -> String): List<T> = when (order) {
    LibraryOrder.ADDED_NEWEST -> items.sortedByDescending(id)
    LibraryOrder.ADDED_OLDEST -> items.sortedBy(id)
    LibraryOrder.TITLE -> items.sortedWith(compareBy<T> { orderKey(title(it)) }.thenBy(id))
    LibraryOrder.ARTIST -> items.sortedWith(compareBy<T> { orderKey(artist(it)) }
        .thenBy { orderKey(title(it)) }.thenBy(id))
}
