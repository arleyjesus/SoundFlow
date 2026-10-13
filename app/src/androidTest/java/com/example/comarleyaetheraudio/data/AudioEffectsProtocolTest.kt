package com.example.comarleyaetheraudio.data

import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.comarleyaetheraudio.data.player.AudioEffectsProtocol
import com.example.comarleyaetheraudio.domain.model.AudioEffectsState
import com.example.comarleyaetheraudio.domain.model.EqualizerBand
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioEffectsProtocolTest {
    @Test fun roundTripPreservesConfirmedValuesAndRevision() {
        val state = AudioEffectsState(isConnecting = false, sessionId = 10, revision = 12, enabled = true,
            bands = listOf(EqualizerBand(0, 1000, -500)), minBandLevelMb = -1500, maxBandLevelMb = 1500,
            bassAvailable = true, bassStrength = 200, errorMessage = "Aviso")
        assertEquals(state, AudioEffectsProtocol.decode(AudioEffectsProtocol.encode(state)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun mismatchedBandArraysAreRejected() {
        AudioEffectsProtocol.decode(Bundle().apply {
            putInt("session", 10); putIntArray("frequencies", intArrayOf(60, 1000))
            putIntArray("levels", intArrayOf(0))
        })
    }
}
