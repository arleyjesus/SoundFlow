package com.example.comarleyaetheraudio.domain.playback

/** Stored file metadata only: it does not measure the decoder or Android audio output. */
data class AudioFileInfo(val mimeType: String, val sampleRate: Int, val bitrate: Int) {
    val format: String get() = mimeType.trim().ifBlank { "No disponible" }
    val sampleRateLabel: String get() = if (sampleRate > 0) "$sampleRate Hz" else "No disponible"
    val bitrateLabel: String get() = if (bitrate > 0) "$bitrate kbps" else "No disponible"
}
