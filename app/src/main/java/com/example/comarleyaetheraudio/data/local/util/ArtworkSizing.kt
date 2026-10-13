package com.example.comarleyaetheraudio.data.local.util

/** Power-of-two sampling, including panoramic covers and odd source dimensions. */
object ArtworkSizing {
    fun sampleSize(width: Int, height: Int, maxDimension: Int): Int {
        require(width > 0 && height > 0 && maxDimension > 0)
        var sample = 1
        while (sample < (1 shl 30) &&
            ((width.toLong() + sample - 1) / sample > maxDimension ||
                (height.toLong() + sample - 1) / sample > maxDimension)) {
            sample *= 2
        }
        return sample
    }
}
