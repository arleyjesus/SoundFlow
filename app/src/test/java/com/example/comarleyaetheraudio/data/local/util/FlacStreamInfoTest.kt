package com.example.comarleyaetheraudio.data.local.util

import com.example.comarleyaetheraudio.domain.playback.AudioResolution
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream

class FlacStreamInfoTest {
    private fun header() = ByteArray(42).apply {
        byteArrayOf(0x66, 0x4c, 0x61, 0x43, 0x80.toByte(), 0, 0, 34).copyInto(this)
        this[8] = 0x10; this[10] = 0x10 // 4096 samples per block
        this[18] = 0x17; this[19] = 0x70; this[20] = 3; this[21] = 0x70 // 96 kHz / stereo / 24-bit
    }
    @Test fun readsNativeHeaderWithoutConsumingAudioOrArtwork() {
        val input = ByteArrayInputStream(header() + ByteArray(1000))
        val info = FlacStreamInfo.read(input)!!
        assertEquals(96000, info.sampleRateHz); assertEquals(24, info.bitDepth); assertEquals(2, info.channels)
        assertEquals(AudioResolution.HIGH_RESOLUTION, info.resolution)
        assertEquals(1000, input.available())
    }
    @Test fun cdResolutionIsLosslessButNotHighResolution() {
        val bytes = header().apply { this[18] = 0x0a; this[19] = 0xc4.toByte(); this[20] = 0x42; this[21] = 0xf0.toByte() }
        val info = FlacStreamInfo.read(bytes.inputStream())!!
        assertEquals(44100, info.sampleRateHz); assertEquals(16, info.bitDepth)
        assertEquals(AudioResolution.STANDARD, info.resolution)
    }
    @Test fun truncatedPrefixesAreRejected() {
        for (length in 0 until 42) assertNull(FlacStreamInfo.read(header().copyOf(length).inputStream()))
    }
    @Test fun forgedExtensionCannotReplaceAValidHeader() {
        for (offset in listOf(0, 4, 5, 6, 7)) {
            val bytes = header().apply { this[offset] = 1 }
            assertNull(FlacStreamInfo.read(bytes.inputStream()))
        }
    }
    @Test fun invalidRateDepthAndBlockSizeAreRejected() {
        val zeroRate = header().apply { this[18] = 0; this[19] = 0 }
        val oneBit = header().apply { this[20] = 2; this[21] = 0 }
        val zeroBlock = header().apply { this[8] = 0 }
        for (bytes in listOf(zeroRate, oneBit, zeroBlock)) assertNull(FlacStreamInfo.read(bytes.inputStream()))
    }
}
