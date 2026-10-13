package com.example.comarleyaetheraudio.data.local.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory

object ArtworkDecoder {
    /** Never decode the full original just to create a thumbnail. Call off the UI thread. */
    fun decode(bytes: ByteArray, maxDimension: Int): Bitmap? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        if (options.outWidth <= 0 || options.outHeight <= 0) return null
        options.inSampleSize = ArtworkSizing.sampleSize(options.outWidth, options.outHeight, maxDimension)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }
}
