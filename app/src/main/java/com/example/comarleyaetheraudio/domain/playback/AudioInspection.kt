package com.example.comarleyaetheraudio.domain.playback

enum class AudioCompression { LOSSLESS, LOSSY, UNKNOWN }
enum class AudioResolution { HIGH_RESOLUTION, STANDARD, UNVERIFIED }

/** Source metadata only. Neither a decoder capability nor a measurement of the audio output. */
data class AudioInspection(
    val codecMime: String? = null,
    val sampleRateHz: Int? = null,
    val channels: Int? = null,
    val bitDepth: Int? = null,
    val bitrateKbps: Int? = null,
    val detail: String = "No se ha podido inspeccionar el archivo."
) {
    val compression: AudioCompression get() = when (codecMime?.trim()?.lowercase(java.util.Locale.ROOT)) {
        "audio/flac", "audio/alac", "audio/raw" -> AudioCompression.LOSSLESS
        "audio/mpeg", "audio/mp4a-latm", "audio/aac", "audio/opus", "audio/vorbis",
        "audio/3gpp", "audio/amr-wb", "audio/ac3", "audio/eac3" -> AudioCompression.LOSSY
        else -> AudioCompression.UNKNOWN
    }

    // Conservative application convention; unknown depth never certifies high resolution.
    val resolution: AudioResolution get() = when {
        compression == AudioCompression.LOSSY -> AudioResolution.STANDARD
        compression != AudioCompression.LOSSLESS || bitDepth == null || bitDepth !in 4..32 ||
            sampleRateHz == null || sampleRateHz !in 1..1048575 -> AudioResolution.UNVERIFIED
        bitDepth > 16 && sampleRateHz > 48000 -> AudioResolution.HIGH_RESOLUTION
        else -> AudioResolution.STANDARD
    }
}
