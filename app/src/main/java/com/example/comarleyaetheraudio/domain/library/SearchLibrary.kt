package com.example.comarleyaetheraudio.domain.library

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Preserves source order and cooperatively cancels obsolete queries. Call on Default. */
suspend fun <T> searchLibrary(items: List<T>, query: String, matches: (T, String) -> Boolean): List<T> {
    val context = currentCoroutineContext()
    context.ensureActive()
    val text = query.trim()
    if (text.isEmpty()) return emptyList()
    val result = ArrayList<T>()
    items.forEachIndexed { index, item ->
        if (index % 64 == 0) context.ensureActive()
        if (matches(item, text)) result.add(item)
    }
    context.ensureActive()
    return result
}
