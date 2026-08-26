package com.example.comarleyaetheraudio.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val changelog: String
)

object UpdateChecker {
    // Reemplaza con la URL RAW de tu archivo update.json en GitHub
    private const val UPDATE_JSON_URL = "https://raw.githubusercontent.com/arleyjesus/SoundFlow/main/app/update.json"

    suspend fun checkForUpdates(currentVersionCode: Int): UpdateInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(UPDATE_JSON_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                if (connection.responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseText)
                    val remoteVersionCode = json.optInt("versionCode", 0)

                    if (remoteVersionCode > currentVersionCode) {
                        UpdateInfo(
                            versionCode = remoteVersionCode,
                            versionName = json.optString("versionName", ""),
                            apkUrl = json.optString("apkUrl", ""),
                            changelog = json.optString("changelog", "")
                        )
                    } else null
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}