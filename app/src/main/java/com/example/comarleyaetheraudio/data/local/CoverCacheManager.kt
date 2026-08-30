package com.example.comarleyaetheraudio.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object CoverCacheManager {

    private const val COVER_DIR = "album_covers"
    private const val TARGET_SIZE = 250
    private const val MAX_CACHE_BYTES = 500 * 1024 * 1024L // Límite de 500 MB en Disco

    // Caché en Memoria RAM de Nivel 1 (0 ms de latencia)
    // Asigna hasta el 25% de la memoria RAM disponible para portadas instantáneas
    private val memoryCache: LruCache<Long, Bitmap> by lazy {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 4
        object : LruCache<Long, Bitmap>(cacheSize) {
            override fun sizeOf(key: Long, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
    }

    suspend fun getOrFetchCover(context: Context, songId: Long, uri: Uri?, path: String?): File? = withContext(Dispatchers.IO) {
        // 1. Si el Bitmap ya existe en la caché de RAM, se entrega de inmediato
        val memoryBitmap = memoryCache.get(songId)
        val coverDir = File(context.cacheDir, COVER_DIR).apply { if (!exists()) mkdirs() }
        val coverFile = File(coverDir, "cover_$songId.jpg")

        if (memoryBitmap != null && coverFile.exists()) {
            coverFile.setLastModified(System.currentTimeMillis())
            return@withContext coverFile
        }

        // 2. Si existe en el disco duro local, cargar y guardar en RAM
        if (coverFile.exists() && coverFile.length() > 0) {
            coverFile.setLastModified(System.currentTimeMillis())
            val bitmapFromDisk = BitmapFactory.decodeFile(coverFile.absolutePath)
            if (bitmapFromDisk != null) {
                memoryCache.put(songId, bitmapFromDisk)
                return@withContext coverFile
            }
        }

        // 3. Extracción asíncrona desde los metadatos del archivo de audio individual
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
                    val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, TARGET_SIZE, TARGET_SIZE, true)

                    // Guardar en RAM
                    memoryCache.put(songId, scaledBitmap)

                    // Guardar en Disco
                    FileOutputStream(coverFile).use { out ->
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    }
                    if (scaledBitmap != originalBitmap) originalBitmap.recycle()

                    // Verificar y podar almacenamiento si sobrepasa los 500 MB
                    pruneCacheIfNeeded(coverDir)

                    return@withContext coverFile
                }
            }
        } catch (_: Exception) {
            // Manejo silencioso para no interrumpir el hilo principal de la UI
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }

        return@withContext null
    }

    // Algoritmo LRU para limpiar imágenes antiguas si la carpeta excede 500 MB
    private fun pruneCacheIfNeeded(coverDir: File) {
        val files = coverDir.listFiles() ?: return
        var currentSize = files.sumOf { it.length() }

        if (currentSize > MAX_CACHE_BYTES) {
            val sortedFiles = files.sortedBy { it.lastModified() }
            val targetSize = (MAX_CACHE_BYTES * 0.8).toLong() // Reduce hasta 400 MB

            for (file in sortedFiles) {
                if (currentSize <= targetSize) break
                currentSize -= file.length()
                file.delete()
            }
        }
    }

    fun getMemoryBitmap(songId: Long): Bitmap? = memoryCache.get(songId)

    // Método para limpiar memoria caché si se actualizan etiquetas de canciones
    fun clearCacheForSong(songId: Long, context: Context) {
        memoryCache.remove(songId)
        val coverDir = File(context.cacheDir, COVER_DIR)
        val coverFile = File(coverDir, "cover_$songId.jpg")
        if (coverFile.exists()) {
            coverFile.delete()
        }
    }
}