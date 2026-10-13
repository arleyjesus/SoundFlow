package com.example.comarleyaetheraudio.domain.playback

import org.junit.Assert.*
import org.junit.Test

class AudioInspectionTest {
    @Test fun losslessIsNotAutomaticallyHighResolution() {
        assertEquals(AudioResolution.STANDARD, AudioInspection("audio/flac", 44100, 2, 16).resolution)
        assertEquals(AudioCompression.LOSSLESS, AudioInspection("audio/flac").compression)
        assertEquals(AudioResolution.UNVERIFIED, AudioInspection("audio/flac", 96000).resolution)
    }
    @Test fun highBitrateOrHighRateOfLossyAudioDoesNotCertifyResolution() {
        for (mime in listOf("audio/mpeg", "audio/mp4a-latm", "audio/opus", "audio/vorbis")) {
            val info = AudioInspection(mime, 192000, 2, 24, 2000)
            assertEquals(AudioCompression.LOSSY, info.compression)
            assertEquals(AudioResolution.STANDARD, info.resolution)
        }
    }
    @Test fun conservativeThresholdRequiresBothDepthAndRate() {
        assertEquals(AudioResolution.STANDARD, AudioInspection("audio/flac", 48000, 2, 24).resolution)
        assertEquals(AudioResolution.STANDARD, AudioInspection("audio/flac", 96000, 2, 16).resolution)
        assertEquals(AudioResolution.HIGH_RESOLUTION, AudioInspection("audio/flac", 96000, 2, 24).resolution)
    }
    @Test fun containerNamesAndUnknownCodecCannotCertifyLossless() {
        for (mime in listOf("audio/wav", "audio/mp4", "audio/ogg", "song.flac", "", null)) {
            assertEquals(AudioCompression.UNKNOWN, AudioInspection(mime, 96000, 2, 24).compression)
            assertEquals(AudioResolution.UNVERIFIED, AudioInspection(mime, 96000, 2, 24).resolution)
        }
    }
    @Test fun invalidMetadataRemainsUnverified() {
        for (rate in listOf(null, 0, -1, Int.MAX_VALUE)) {
            assertEquals(AudioResolution.UNVERIFIED, AudioInspection("audio/raw", rate, 2, 24).resolution)
        }
        for (bits in listOf(null, 0, -1, 33)) {
            assertEquals(AudioResolution.UNVERIFIED, AudioInspection(" AUDIO/FLAC ", 96000, 2, bits).resolution)
        }
    }
}
