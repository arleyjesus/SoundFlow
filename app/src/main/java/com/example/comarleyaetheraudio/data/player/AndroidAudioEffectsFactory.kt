package com.example.comarleyaetheraudio.data.player

import android.media.audiofx.AudioEffect
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer

internal class AndroidAudioEffectsFactory(private val onChanged: () -> Unit) : AudioEffectsFactory {
    override fun equalizer(sessionId: Int): EqualizerEffect {
        val effect = Equalizer(0, sessionId)
        return object : EqualizerEffect, EffectHandle by handle(effect) {
            override val minLevelMb get() = effect.bandLevelRange[0].toInt()
            override val maxLevelMb get() = effect.bandLevelRange[1].toInt()
            override val frequenciesHz get() = (0 until effect.numberOfBands.toInt()).map {
                effect.getCenterFreq(it.toShort()) / 1000
            }
            override fun level(band: Int) = effect.getBandLevel(band.toShort()).toInt()
            override fun setLevel(band: Int, levelMb: Int) { effect.setBandLevel(band.toShort(), levelMb.toShort()) }
        }
    }

    override fun bass(sessionId: Int): StrengthEffect {
        val effect = BassBoost(0, sessionId)
        try { check(effect.strengthSupported) } catch (e: Exception) { effect.release(); throw e }
        return object : StrengthEffect, EffectHandle by handle(effect) {
            override var strength: Int
                get() = effect.roundedStrength.toInt()
                set(value) { effect.setStrength(value.toShort()) }
        }
    }

    @Suppress("DEPRECATION")
    override fun virtualizer(sessionId: Int): StrengthEffect {
        val effect = Virtualizer(0, sessionId)
        try { check(effect.strengthSupported) } catch (e: Exception) { effect.release(); throw e }
        return object : StrengthEffect, EffectHandle by handle(effect) {
            override var strength: Int
                get() = effect.roundedStrength.toInt()
                set(value) { effect.setStrength(value.toShort()) }
        }
    }

    private fun handle(effect: AudioEffect): AndroidHandle = try { AndroidHandle(effect) }
        catch (e: Exception) { runCatching { effect.release() }; throw e }

    private inner class AndroidHandle(private val effect: AudioEffect) : EffectHandle {
        init {
            effect.setControlStatusListener { _, _ -> onChanged() }
            effect.setEnableStatusListener { _, _ -> onChanged() }
        }
        override val hasControl get() = effect.hasControl()
        override var enabled: Boolean
            get() = effect.enabled
            set(value) { check(effect.setEnabled(value) == AudioEffect.SUCCESS) }
        override fun release() {
            runCatching { effect.setControlStatusListener(null) }
            runCatching { effect.setEnableStatusListener(null) }
            effect.release()
        }
    }
}
