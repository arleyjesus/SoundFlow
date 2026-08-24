package com.example.comarleyaetheraudio.data.remote

import com.example.comarleyaetheraudio.domain.model.LyricLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object LrcLibService {
    suspend fun fetchSyncedLyrics(trackName: String, artistName: String): List<LyricLine>? {
        return withContext(Dispatchers.IO) {
            try {
                val encodedTrack = URLEncoder.encode(trackName, "UTF-8")
                val encodedArtist = URLEncoder.encode(artistName, "UTF-8")
                val urlString = "https://lrclib.net/api/get?artist_name=$encodedArtist&track_name=$encodedTrack"

                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                if (connection.responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseText)
                    val syncedLyricsText = json.optString("syncedLyrics", "")

                    if (syncedLyricsText.isNotEmpty()) {
                        parseLrcString(syncedLyricsText)
                    } else null
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun parseLrcString(lrcContent: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        val regex = "\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)".toRegex()

        lrcContent.lines().forEach { line ->
            val match = regex.find(line)
            if (match != null) {
                val (min, sec, ms, text) = match.destructured
                val totalMs = (min.toLong() * 60 * 1000) + (sec.toLong() * 1000) + ms.toLong()
                if (text.trim().isNotEmpty()) {
                    lines.add(LyricLine(totalMs, text.trim()))
                }
            }
        }
        return lines.sortedBy { it.timeMs }
    }
}