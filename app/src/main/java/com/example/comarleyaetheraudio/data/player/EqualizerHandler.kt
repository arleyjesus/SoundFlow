package com.example.comarleyaetheraudio.data.player

import android.media.audiofx.Equalizer

class EqualizerHandler {

    private var equalizer: Equalizer? = null
    var isEnabled: Boolean = false
        private set

    fun initEqualizer(audioSessionId: Int) {
        try {
            if (audioSessionId != 0) {
                equalizer = Equalizer(0, audioSessionId).apply {
                    enabled = true
                }
                isEnabled = true
            }
        } catch (_: Exception) {
            isEnabled = false
        }
    }

    fun setEnabled(enable: Boolean) {
        try {
            equalizer?.enabled = enable
            isEnabled = enable
        } catch (_: Exception) {}
    }

    fun getBandCount(): Int = equalizer?.numberOfBands?.toInt() ?: 0

    fun getBandLevelRange(): Pair<Int, Int> {
        val range = equalizer?.bandLevelRange ?: shortArrayOf(-1500, 1500)
        return Pair(range[0].toInt(), range[1].toInt())
    }

    fun setBandLevel(band: Int, level: Int) {
        try {
            equalizer?.setBandLevel(band.toShort(), level.toShort())
        } catch (_: Exception) {}
    }

    fun getPresets(): List<String> {
        val presets = mutableListOf<String>()
        val count = equalizer?.numberOfPresets?.toInt() ?: 0
        for (i in 0 until count) {
            presets.add(equalizer?.getPresetName(i.toShort()) ?: "Preset $i")
        }
        return presets
    }

    fun usePreset(presetIndex: Int) {
        try {
            equalizer?.usePreset(presetIndex.toShort())
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            equalizer?.release()
            equalizer = null
        } catch (_: Exception) {}
    }
}