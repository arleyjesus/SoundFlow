package com.example.comarleyaetheraudio.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.comarleyaetheraudio.data.local.util.ArtworkDecoder
import com.example.comarleyaetheraudio.data.local.util.CoverCacheManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.io.File

@RunWith(AndroidJUnit4::class)
class ArtworkCacheTest {
    private fun image(width: Int, height: Int): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        return try {
            ByteArrayOutputStream().use {
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
                it.toByteArray()
            }
        } finally { bitmap.recycle() }
    }

    @Test fun decoderBoundsLargeCoversBeforeDisplay() {
        val bitmap = requireNotNull(ArtworkDecoder.decode(image(2048, 1536), 250))
        try {
            assertTrue(bitmap.width <= 250 && bitmap.height <= 250)
            assertTrue(bitmap.allocationByteCount <= 250 * 250 * 4)
        } finally { bitmap.recycle() }
    }

    @Test fun portraitArtworkKeepsItsAspectRatio() {
        val bitmap = requireNotNull(ArtworkDecoder.decode(image(256, 1024), 250))
        try { assertEquals(bitmap.height, bitmap.width * 4) }
        finally { bitmap.recycle() }
    }

    @Test fun invalidArtworkReturnsNoBitmap() {
        assertNull(ArtworkDecoder.decode(byteArrayOf(1, 2, 3), 250))
    }

    @Test fun concurrentDiskHitsReturnTheSameIntactThumbnail() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val id = -System.nanoTime()
        val directory = File(context.cacheDir, "album_covers").apply { mkdirs() }
        val file = File(directory, "cover_$id.jpg")
        val bytes = image(32, 32)
        try {
            file.writeBytes(bytes)
            val results = coroutineScope {
                List(16) { async { CoverCacheManager.getOrFetchCover(context, id, null, null) } }.awaitAll()
            }
            results.forEach { assertEquals(file, it) }
            assertArrayEquals(bytes, file.readBytes())
            val decoded = BitmapFactory.decodeFile(file.absolutePath)
            try { assertNotNull(decoded) } finally { decoded?.recycle() }
        } finally { CoverCacheManager.clearCacheForSong(id, context) }
    }
}
