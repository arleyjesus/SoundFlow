package com.example.comarleyaetheraudio.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ArtworkLoader {

    // Caché en memoria RAM (usa máximo 20MB de RAM para imágenes)
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8
    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    // Procesa la imagen en hilo secundario (IO) sin congelar los 120 FPS
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
                val bitmap = BitmapFactory.decodeByteArray(art, 0, art.size)
                if (bitmap != null) {
                    memoryCache.put(cacheKey, bitmap)
                }
                return@withContext bitmap
            }
        } catch (_: Exception) {
            // Silencioso para evitar cierres
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
        return@withContext null
    }
}