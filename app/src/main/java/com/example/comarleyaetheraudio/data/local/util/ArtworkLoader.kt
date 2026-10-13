package com.example.comarleyaetheraudio.data.local.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

object ArtworkLoader {

    // Legacy loader (currently without callers): retain a hard 20 MiB ceiling.
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = minOf(maxMemory / 8, 20 * 1024).coerceAtLeast(1)
    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return (bitmap.allocationByteCount / 1024).coerceAtLeast(1)
        }
    }

    // Decode off the UI thread; frame rate must be measured on a device.
    suspend fun loadArtworkAsync(context: Context, songId: Long, uri: Uri?, path: String?): Bitmap? = withContext(Dispatchers.IO) {
        val cacheKey = "song_$songId"

        // 1. Revisar si ya está en caché
        memoryCache.get(cacheKey)?.let { return@withContext it }

        // 2. Extraer del archivo de audio de forma segura
        val retriever = MediaMetadataRetriever()
        try {
            if (uri != null) {
                retriever.setDataSource(context, uri)
            } else if (!path.isNullOrEmpty()) {
                retriever.setDataSource(path)
            } else {
                return@withContext null
            }

            val art = retriever.embeddedPicture
            if (art != null) {
                coroutineContext.ensureActive()
                val bitmap = ArtworkDecoder.decode(art, 1024)
                if (bitmap != null) {
                    memoryCache.put(cacheKey, bitmap)
                }
                return@withContext bitmap
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Silencioso para evitar cierres
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
        return@withContext null
    }
}
