package com.example.comarleyaetheraudio.data.local.util

import java.io.File
import java.security.MessageDigest
import java.util.Locale

object ArtistCachePolicy {
    const val MAX_IMAGE_BYTES = 2 * 1024 * 1024
    const val MAX_DISK_BYTES = 32 * 1024 * 1024L
    const val FRESH_MS = 7 * 24 * 60 * 60 * 1000L

    fun key(artist: String): String = MessageDigest.getInstance("SHA-256")
        .digest(artist.trim().lowercase(Locale.ROOT).toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it.toInt() and 255) }

    /** Only generated artist images; never delete unrelated files or atomic write sidecars. */
    fun trim(directory: File, protected: File, limitBytes: Long = MAX_DISK_BYTES) {
        require(limitBytes > 0)
        val files = directory.listFiles { file -> file.isFile &&
            file.name.matches(Regex("artist_(?:[a-f0-9]{64}|-?\\d+)\\.jpg")) } ?: return
        var bytes = files.sumOf { it.length() }
        if (bytes <= limitBytes) return
        for (file in files.sortedBy { it.lastModified() }) {
            if (bytes <= limitBytes * 4 / 5) break
            if (file == protected) continue
            val length = file.length()
            if (file.delete()) bytes -= length
        }
    }
}

/** Fixed-size negative cache, so offline scrolling does not create unbounded retained keys. */
class ArtistFailureBackoff(private val capacity: Int = 128, private val now: () -> Long = System::currentTimeMillis) {
    init { require(capacity > 0) }
    private val failed = LinkedHashMap<String, Long>()
    @Synchronized fun isBlocked(key: String): Boolean {
        val at = failed[key] ?: return false
        if (now() - at in 0 until 60_000) return true
        failed.remove(key)
        return false
    }
    @Synchronized fun recordFailure(key: String) {
        failed.remove(key)
        failed[key] = now()
        while (failed.size > capacity) failed.remove(failed.keys.first())
    }
    @Synchronized fun clear(key: String) { failed.remove(key) }
}
