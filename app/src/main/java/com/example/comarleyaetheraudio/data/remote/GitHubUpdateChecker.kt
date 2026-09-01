package com.example.comarleyaetheraudio.data.remote

import com.example.comarleyaetheraudio.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object GitHubUpdateChecker {

    // ⚡ REEMPLAZA CON TU USUARIO Y TU REPOSITORIO REAL DE GITHUB
    private const val GITHUB_RELEASES_URL = "https://api.github.com/repos/TU_USUARIO/TU_REPOSITORIO/releases/latest"

    data class UpdateInfo(
        val hasUpdate: Boolean,
        val latestVersionName: String,
        val releaseNotes: String,
        val downloadUrl: String
    )

    suspend fun checkForUpdates(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_RELEASES_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(responseText)

                val tagName = json.optString("tag_name", "")
                val releaseNotes = json.optString("body", "")

                val assets = json.optJSONArray("assets")
                var downloadUrl = ""
                if (assets != null && assets.length() > 0) {
                    downloadUrl = assets.getJSONObject(0).optString("browser_download_url", "")
                }

                val remoteVersionClean = tagName.replace("v", "").trim()
                val currentVersionClean = BuildConfig.VERSION_NAME.trim()

                val hasUpdate = remoteVersionClean.isNotEmpty() && remoteVersionClean != currentVersionClean

                return@withContext UpdateInfo(
                    hasUpdate = hasUpdate,
                    latestVersionName = tagName,
                    releaseNotes = releaseNotes,
                    downloadUrl = downloadUrl
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
}