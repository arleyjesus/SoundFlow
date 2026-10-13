package com.example.comarleyaetheraudio.domain.playback

import org.junit.Assert.assertEquals
import org.junit.Test

class AudioFileInfoTest {
    @Test fun missingMetadataRemainsUnknown() {
        val info = AudioFileInfo(" ", 0, -1)
        assertEquals("No disponible", info.format)
        assertEquals("No disponible", info.sampleRateLabel)
        assertEquals("No disponible", info.bitrateLabel)
    }
    @Test fun reportsStoredSampleRateWithoutRoundingOrClassification() {
        assertEquals("44100 Hz", AudioFileInfo("audio/flac", 44100, 0).sampleRateLabel)
        assertEquals("96000 Hz", AudioFileInfo("audio/flac", 96000, 0).sampleRateLabel)
    }
    @Test fun bitrateUsesTheRepositoriesKilobitUnit() {
        val info = AudioFileInfo(" audio/mpeg ", 44100, 320)
        assertEquals("320 kbps", info.bitrateLabel)
        assertEquals("audio/mpeg", info.format)
    }
}
