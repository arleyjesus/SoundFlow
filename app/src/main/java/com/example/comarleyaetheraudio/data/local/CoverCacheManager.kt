package com.example.comarleyaetheraudio.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object CoverCacheManager {

    private const val COVER_DIR = "album_covers"
    private const val TARGET_SIZE = 250 // Resolución óptima para listas y posters

    suspend fun getOrFetchCover(context: Context, songId: Long, uri: Uri?, path: String?): File? = withContext(Dispatchers.IO) {
        val coverDir = File(context.cacheDir, COVER_DIR).apply { if (!exists()) mkdirs() }
        val coverFile = File(coverDir, "cover_$songId.jpg")

        // 1. Si la carátula ya existe en disco, devolverla inmediatamente (0 ms)
        if (coverFile.exists() && coverFile.length() > 0) {
            return@withContext coverFile
        }

        // 2. Si no existe, extraerla del MP3 en segundo plano
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
                val originalBitmap = BitmapFactory.decodeByteArray(art, 0, art.size)
                if (originalBitmap != null) {
                    // Redimensionar para reducir peso y consumo de RAM
                    val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, TARGET_SIZE, TARGET_SIZE, true)

                    // Guardar en almacenamiento interno
                    FileOutputStream(coverFile).use { out ->
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    }
                    if (scaledBitmap != originalBitmap) originalBitmap.recycle()
                    return@withContext coverFile
                }
            }
        } catch (_: Exception) {
            // Manejo silencioso de excepciones
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }

        return@withContext null
    }
}