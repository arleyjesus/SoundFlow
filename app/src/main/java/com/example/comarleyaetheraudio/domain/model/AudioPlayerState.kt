package com.example.comarleyaetheraudio.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class AudioPlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val artworkData: ByteArray? = null
)