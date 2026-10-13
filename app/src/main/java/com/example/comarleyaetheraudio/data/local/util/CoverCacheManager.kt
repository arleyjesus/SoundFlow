package com.example.comarleyaetheraudio.data.local.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.AtomicFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.coroutineContext

object CoverCacheManager {
    private const val COVER_DIR = "album_covers"
    private const val TARGET_SIZE = 250
    private const val MAX_CACHE_BYTES = 500 * 1024 * 1024L
    // Coil owns the decoded image cache. This layer only stores thumbnail files.
    // One extraction at a time bounds native retrievers and original embedded byte arrays.
    private val requests = Mutex()
    private val diskLock = Any()

    suspend fun getOrFetchCover(context: Context, songId: Long, uri: Uri?, path: String?): File? =
        withContext(Dispatchers.IO) {
            val coverDir = File(context.cacheDir, COVER_DIR)
            if (!coverDir.isDirectory && !coverDir.mkdirs() && !coverDir.isDirectory) return@withContext null
            val coverFile = File(coverDir, "cover_$songId.jpg")
            // A slow provider miss must not block unrelated, already cached covers.
            cachedCover(coverFile)?.let { return@withContext it }
            requests.withLock {
                // Recheck after waiting: a preceding request may have filled this key.
                cachedCover(coverFile)?.let { return@withLock it }
                if (uri == null && path.isNullOrBlank()) return@withLock null
                val retriever = MediaMetadataRetriever()
                try {
                    if (uri != null) retriever.setDataSource(context, uri)
                    else retriever.setDataSource(path)
                    coroutineContext.ensureActive()
                    val bytes = retriever.embeddedPicture ?: return@withLock null
                    val bitmap = ArtworkDecoder.decode(bytes, TARGET_SIZE) ?: return@withLock null
                    try {
                        coroutineContext.ensureActive()
                        synchronized(diskLock) {
                            val atomic = AtomicFile(coverFile)
                            val output = atomic.startWrite()
                            try {
                                check(bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output))
                                atomic.finishWrite(output)
                            } catch (e: Exception) {
                                atomic.failWrite(output)
                                throw e
                            }
                            pruneCacheIfNeeded(coverDir, coverFile)
                        }
                    } finally {
                        // Private to the file writer; Compose/Coil never receive this bitmap.
                        bitmap.recycle()
                    }
                    coverFile
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    null
                } finally {
                    try { retriever.release() } catch (_: Exception) { }
                }
            }
        }

    private fun cachedCover(file: File): File? {
        if (!file.isFile || file.length() == 0L) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        file.setLastModified(System.currentTimeMillis())
        return file
    }

    private fun pruneCacheIfNeeded(coverDir: File, protectedFile: File) {
        val files = coverDir.listFiles { file ->
            file.isFile && file.name.matches(Regex("cover_-?\\d+\\.jpg"))
        } ?: return
        var size = files.sumOf { it.length() }
        if (size <= MAX_CACHE_BYTES) return
        for (file in files.sortedBy { it.lastModified() }) {
            if (size <= MAX_CACHE_BYTES * 4 / 5) break
            if (file == protectedFile) continue
            val length = file.length()
            if (file.delete()) size -= length
        }
    }

    fun clearCacheForSong(songId: Long, context: Context) {
        synchronized(diskLock) {
            AtomicFile(File(File(context.cacheDir, COVER_DIR), "cover_$songId.jpg")).delete()
        }
    }
}
