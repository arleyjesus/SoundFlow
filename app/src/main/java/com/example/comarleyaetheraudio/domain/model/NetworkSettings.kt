package com.example.comarleyaetheraudio.domain.model

/** Optional online features; existing installations retain their previous behavior. */
data class NetworkSettings(
    val artistImages: Boolean = true,
    val lyrics: Boolean = true,
    val updates: Boolean = true
)

enum class NetworkFeature { ARTIST_IMAGES, LYRICS, UPDATES }
