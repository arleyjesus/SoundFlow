package com.example.comarleyaetheraudio.data.player

import com.example.comarleyaetheraudio.domain.model.AudioEffectsState
import com.example.comarleyaetheraudio.domain.model.EqualizerBand

internal interface EffectHandle {
    val hasControl: Boolean get() = true
    var enabled: Boolean
    fun release()
}

internal interface EqualizerEffect : EffectHandle {
    val minLevelMb: Int
    val maxLevelMb: Int
    val frequenciesHz: List<Int>
    fun level(band: Int): Int
    fun setLevel(band: Int, levelMb: Int)
}

internal interface StrengthEffect : EffectHandle {
    var strength: Int
}

internal interface AudioEffectsFactory {
    fun equalizer(sessionId: Int): EqualizerEffect
    fun bass(sessionId: Int): StrengthEffect
    fun virtualizer(sessionId: Int): StrengthEffect
}

/** All access is serialized by the service worker; no Android dependency in this policy. */
internal class AudioEffectsEngine(private val factory: AudioEffectsFactory) {
    private var equalizer: EqualizerEffect? = null
    private var bass: StrengthEffect? = null
    private var virtualizer: StrengthEffect? = null
    var state = AudioEffectsState(isConnecting = false)
        private set

    fun attach(sessionId: Int, force: Boolean = false) {
        if (!force && sessionId == state.sessionId) return
        releaseHandles()
        state = AudioEffectsState(isConnecting = false, sessionId = sessionId.coerceAtLeast(0))
        if (sessionId <= 0) return
        equalizer = create({ factory.equalizer(sessionId) }) { eq ->
            require(eq.minLevelMb <= 0 && eq.maxLevelMb > 0 && eq.frequenciesHz.isNotEmpty())
            eq.frequenciesHz.indices.forEach { eq.setLevel(it, 0) }
        }
        bass = create({ factory.bass(sessionId) }) { it.strength = 0 }
        virtualizer = create({ factory.virtualizer(sessionId) }) { it.strength = 0 }
        refresh()
    }

    private fun <T : EffectHandle> create(build: () -> T, neutral: (T) -> Unit): T? {
        var handle: T? = null
        return try {
            build().also { handle = it; check(it.hasControl); it.enabled = false; check(!it.enabled); neutral(it) }
        } catch (_: Exception) {
            runCatching { handle?.release() }
            null
        }
    }

    fun setEnabled(enabled: Boolean) {
        if (!state.hasEffects) return
        applySafely {
            handles().forEach { it.enabled = enabled; check(it.enabled == enabled) }
            refresh(enabled)
        }
    }

    fun setBandLevel(band: Int, value: Int) {
        if (!state.enabled || band !in state.bands.indices) return
        applySafely {
            equalizer?.setLevel(band, value.coerceIn(state.minBandLevelMb, state.maxBandLevelMb))
            refresh(state.enabled)
        }
    }

    fun setBassStrength(value: Int) = setStrength(bass, value)
    fun setVirtualizerStrength(value: Int) = setStrength(virtualizer, value)

    private fun setStrength(effect: StrengthEffect?, value: Int) {
        if (!state.enabled || effect == null) return
        applySafely { effect.strength = value.coerceIn(0, 1000); refresh(state.enabled) }
    }

    fun reset() {
        applySafely {
            handles().forEach { it.enabled = false; check(!it.enabled) }
            equalizer?.let { eq -> state.bands.indices.forEach { eq.setLevel(it, 0) } }
            bass?.strength = 0
            virtualizer?.strength = 0
            refresh(false)
        }
    }

    fun reconcile() {
        if (handles().isEmpty()) return
        applySafely {
            check(handles().all { it.hasControl })
            if (state.enabled) check(handles().all { it.enabled })
            else handles().filter { it.enabled }.forEach { it.enabled = false; check(!it.enabled) }
            refresh(state.enabled)
        }
    }

    private fun refresh(enabled: Boolean = false) {
        applySafely {
            val eq = equalizer
            state = state.copy(enabled = enabled, errorMessage = null,
                bands = eq?.frequenciesHz?.mapIndexed { index, hz -> EqualizerBand(index, hz, eq.level(index)) }.orEmpty(),
                minBandLevelMb = eq?.minLevelMb ?: 0, maxBandLevelMb = eq?.maxLevelMb ?: 0,
                bassAvailable = bass != null, bassStrength = bass?.strength ?: 0,
                virtualizerAvailable = virtualizer != null, virtualizerStrength = virtualizer?.strength ?: 0)
        }
    }

    private inline fun applySafely(action: () -> Unit) {
        try { action() } catch (_: Exception) {
            val sessionId = state.sessionId
            releaseHandles()
            state = AudioEffectsState(isConnecting = false, sessionId = sessionId,
                errorMessage = "No se pudo aplicar el ajuste. Vuelve a comprobar la disponibilidad de los efectos.")
        }
    }

    private fun handles(): List<EffectHandle> = listOfNotNull(equalizer, bass, virtualizer)

    private fun releaseHandles() {
        handles().forEach { runCatching { it.enabled = false }; runCatching { it.release() } }
        equalizer = null
        bass = null
        virtualizer = null
    }

    fun release() {
        releaseHandles()
        state = AudioEffectsState(isConnecting = false)
    }
}
