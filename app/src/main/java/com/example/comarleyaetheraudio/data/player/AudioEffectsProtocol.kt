package com.example.comarleyaetheraudio.data.player

import android.os.Bundle
import androidx.media3.session.SessionCommand
import com.example.comarleyaetheraudio.domain.model.AudioEffectsState
import com.example.comarleyaetheraudio.domain.model.EqualizerBand

internal object AudioEffectsProtocol {
    const val ACTION = "com.example.comarleyaetheraudio.AUDIO_EFFECTS"
    const val GET = "get"
    const val ENABLE = "enable"
    const val BAND = "band"
    const val BASS = "bass"
    const val VIRTUALIZER = "virtualizer"
    const val RESET = "reset"
    const val RETRY = "retry"
    const val OP = "operation"
    const val VALUE = "value"
    const val INDEX = "index"
    val command get() = SessionCommand(ACTION, Bundle.EMPTY)

    fun encode(state: AudioEffectsState): Bundle = Bundle().apply {
        putInt("session", state.sessionId)
        putLong("revision", state.revision)
        putBoolean("enabled", state.enabled)
        putIntArray("frequencies", state.bands.map { it.frequencyHz }.toIntArray())
        putIntArray("levels", state.bands.map { it.levelMb }.toIntArray())
        putInt("min", state.minBandLevelMb)
        putInt("max", state.maxBandLevelMb)
        putBoolean("bassAvailable", state.bassAvailable)
        putInt("bass", state.bassStrength)
        putBoolean("virtualizerAvailable", state.virtualizerAvailable)
        putInt("virtualizer", state.virtualizerStrength)
        putString("error", state.errorMessage)
    }

    fun decode(bundle: Bundle): AudioEffectsState {
        require(bundle.containsKey("session"))
        val frequencies = bundle.getIntArray("frequencies") ?: intArrayOf()
        val levels = bundle.getIntArray("levels") ?: intArrayOf()
        require(frequencies.size == levels.size)
        return AudioEffectsState(isConnecting = false, sessionId = bundle.getInt("session"),
            revision = bundle.getLong("revision"), enabled = bundle.getBoolean("enabled"),
            bands = frequencies.mapIndexed { index, hz -> EqualizerBand(index, hz, levels[index]) },
            minBandLevelMb = bundle.getInt("min"), maxBandLevelMb = bundle.getInt("max"),
            bassAvailable = bundle.getBoolean("bassAvailable"), bassStrength = bundle.getInt("bass"),
            virtualizerAvailable = bundle.getBoolean("virtualizerAvailable"),
            virtualizerStrength = bundle.getInt("virtualizer"), errorMessage = bundle.getString("error"))
    }
}
