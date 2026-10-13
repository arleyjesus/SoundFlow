package com.example.comarleyaetheraudio.domain.playback

/** Reuse only an unchanged source/metadata snapshot and an unchanged player timeline. */
fun <T> canReusePlaybackQueue(requested: List<T>, previous: List<T>, actualCount: Int,
    id: (T) -> Long, actualIdAt: (Int) -> Long?): Boolean =
    requested.isNotEmpty() && requested == previous && actualCount == requested.size &&
        requested.indices.all { actualIdAt(it) == id(requested[it]) }
