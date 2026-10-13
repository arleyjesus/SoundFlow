package com.example.comarleyaetheraudio.domain.model

data class EqualizerBand(val index: Int, val frequencyHz: Int, val levelMb: Int)

data class AudioEffectsState(
    val isConnecting: Boolean = true,
    val isUpdating: Boolean = false,
    val sessionId: Int = 0,
    val revision: Long = 0,
    val enabled: Boolean = false,
    val bands: List<EqualizerBand> = emptyList(),
    val minBandLevelMb: Int = 0,
    val maxBandLevelMb: Int = 0,
    val bassAvailable: Boolean = false,
    val bassStrength: Int = 0,
    val virtualizerAvailable: Boolean = false,
    val virtualizerStrength: Int = 0,
    val errorMessage: String? = null
) {
    val hasEffects: Boolean get() = bands.isNotEmpty() || bassAvailable || virtualizerAvailable
    val canEdit: Boolean get() = !isConnecting && !isUpdating && enabled && errorMessage == null
}
