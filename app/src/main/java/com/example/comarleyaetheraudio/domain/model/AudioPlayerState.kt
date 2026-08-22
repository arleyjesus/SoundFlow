package com.example.comarleyaetheraudio.domain.model

/**
 * Representa el estado actual del reproductor multimedia para la UI.
 */
data class AudioPlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isHiResActive: Boolean = false,
    val isShuffleEnabled: Boolean = false,     // NUEVO
    val artworkData: ByteArray? = null        // NUEVO
)