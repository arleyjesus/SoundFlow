package com.example.comarleyaetheraudio.data.local.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AtomicFile
import com.example.comarleyaetheraudio.data.remote.BoundedHttpReader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.net.URLEncoder

object ArtistImageFetcher {
    private val reader = BoundedHttpReader()
    private val failures = ArtistFailureBackoff()
    private val slots = Semaphore(2)
    private val keys = List(16) { Mutex() }
    private val publication = Mutex()

    suspend fun getOrFetchArtistPicture(context: Context, artistName: String, networkAllowed: Boolean = true): String? = withContext(Dispatchers.IO) {
        val name = artistName.trim()
        if (name.isEmpty() || name.equals("<unknown>", true)) return@withContext null
        // A new namespace avoids reusing portraits selected by the old first-result heuristic.
        val key = ArtistCachePolicy.key("verified:$name")
        val directory = File(context.cacheDir, "artist_covers")
        if (!directory.isDirectory && !directory.mkdirs() && !directory.isDirectory) return@withContext null
        val file = File(directory, "artist_$key.jpg")
        fun cached(): String? {
            if (!file.isFile || file.length() !in 1..ArtistCachePolicy.MAX_IMAGE_BYTES.toLong()) return null
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            return file.absolutePath.takeIf { bounds.outWidth > 0 && bounds.outHeight > 0 }
        }
        fun fresh(): String? = cached()?.takeIf {
            System.currentTimeMillis() - file.lastModified() in 0 until ArtistCachePolicy.FRESH_MS
        }
        if (!networkAllowed) return@withContext cached()
        fresh()?.let { return@withContext it }
        keys[(key.hashCode() and Int.MAX_VALUE) % keys.size].withLock {
            fresh()?.let { return@withLock it }
            if (failures.isBlocked(key)) return@withLock cached()
            slots.withPermit {
                val taskContext = currentCoroutineContext()
                try {
                    val encoded = URLEncoder.encode(name, "UTF-8")
                    val response = reader.read(URL("https://api.deezer.com/search/artist?q=$encoded"), 128 * 1024) {
                        taskContext.ensureActive()
                    }
                    val results = JSONObject(String(response, Charsets.UTF_8)).optJSONArray("data")
                    val matches = (0 until (results?.length() ?: 0)).mapNotNull { results?.optJSONObject(it) }
                        .filter { com.example.comarleyaetheraudio.domain.library.sameArtistName(name, it.optString("name")) }
                        .distinctBy { it.optLong("id") }
                    val artist = matches.singleOrNull()
                    val picture = artist?.optString("picture_medium").orEmpty()
                    if (picture.isBlank()) {
                        failures.recordFailure(key)
                        return@withPermit cached()
                    }
                    val bytes = reader.read(URL(picture), ArtistCachePolicy.MAX_IMAGE_BYTES) { taskContext.ensureActive() }
                    val bitmap = ArtworkDecoder.decode(bytes, 250)
                    if (bitmap == null) {
                        failures.recordFailure(key)
                        return@withPermit cached()
                    }
                    try {
                        taskContext.ensureActive()
                        publication.withLock {
                            val atomic = AtomicFile(file)
                            val output = atomic.startWrite()
                            try {
                                check(bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output))
                                atomic.finishWrite(output)
                            } catch (e: Exception) { atomic.failWrite(output); throw e }
                            ArtistCachePolicy.trim(directory, file)
                        }
                    } finally { bitmap.recycle() }
                    failures.clear(key)
                    file.absolutePath
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    failures.recordFailure(key)
                    cached()
                }
            }
        }
    }
}
