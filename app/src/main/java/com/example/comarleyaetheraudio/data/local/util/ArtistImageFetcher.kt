package com.example.comarleyaetheraudio.data.local.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object ArtistImageFetcher {

    private const val ARTIST_DIR = "artist_covers"

    suspend fun getOrFetchArtistPicture(context: Context, artistName: String): String? = withContext(Dispatchers.IO) {
        if (artistName.isBlank() || artistName.equals("<unknown>", ignoreCase = true)) return@withContext null

        val cleanName = artistName.trim().lowercase()
        val safeFileName = "artist_${cleanName.hashCode()}.jpg"
        val artistDir = File(context.cacheDir, ARTIST_DIR).apply { if (!exists()) mkdirs() }
        val artistFile = File(artistDir, safeFileName)

        // 1. Si ya se descargó previamente la foto del cantante a disco, la usamos inmediatamente
        if (artistFile.exists() && artistFile.length() > 0) {
            return@withContext artistFile.absolutePath
        }

        // 2. Consulta ultra liviana a la API de Deezer para obtener la imagen real del artista
        try {
            val encodedName = URLEncoder.encode(artistName, "UTF-8")
            val apiUrl = "https://api.deezer.com/search/artist?q=$encodedName"
            val url = URL(apiUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 3000
                requestMethod = "GET"
            }

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val data = json.optJSONArray("data")
                if (data != null && data.length() > 0) {
                    val firstArtist = data.getJSONObject(0)
                    val pictureUrl = firstArtist.optString("picture_medium", "")

                    if (pictureUrl.isNotBlank()) {
                        val imgUrl = URL(pictureUrl)
                        val imgBytes = imgUrl.readBytes()
                        artistFile.writeBytes(imgBytes)
                        return@withContext artistFile.absolutePath
                    }
                }
            }
        } catch (_: Exception) {
            // Manejo silencioso: si se está fuera de línea, la app continuará sin romper la UI
        }

        return@withContext null
    }
}