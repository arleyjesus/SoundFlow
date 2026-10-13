package com.example.comarleyaetheraudio.data.remote

import com.example.comarleyaetheraudio.domain.model.LyricLine
import com.example.comarleyaetheraudio.domain.playback.SyncedLyricsParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import kotlin.coroutines.coroutineContext

object LrcLibService {
    private val reader = BoundedHttpReader()
    suspend fun fetchSyncedLyrics(trackName: String, artistName: String): List<LyricLine>? {
        return withContext(Dispatchers.IO) {
            try {
                coroutineContext.ensureActive()
                val encodedTrack = URLEncoder.encode(trackName, "UTF-8")
                val encodedArtist = URLEncoder.encode(artistName, "UTF-8")
                val urlString = "https://lrclib.net/api/get?artist_name=$encodedArtist&track_name=$encodedTrack"

                val taskContext = coroutineContext
                val bytes = reader.read(URL(urlString), 256 * 1024) { taskContext.ensureActive() }
                val json = JSONObject(String(bytes, Charsets.UTF_8))
                SyncedLyricsParser.parse(json.optString("syncedLyrics", "")).takeIf { it.isNotEmpty() }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
        }
    }

}
