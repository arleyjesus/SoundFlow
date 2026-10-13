package com.example.comarleyaetheraudio.data.remote

import com.example.comarleyaetheraudio.BuildConfig
import com.example.comarleyaetheraudio.domain.playback.ReleaseVersion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

object GitHubUpdateChecker {

    private const val GITHUB_RELEASES_URL = "https://api.github.com/repos/arleyjesus/SoundFlow/releases/latest"
    private const val RELEASE_PAGE = "https://github.com/arleyjesus/SoundFlow/releases/latest"
    private val reader = BoundedHttpReader()

    data class UpdateInfo(
        val hasUpdate: Boolean,
        val latestVersionName: String,
        val releaseNotes: String,
        val downloadUrl: String
    )

    suspend fun checkForUpdates(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val taskContext = currentCoroutineContext()
            val bytes = reader.read(URL(GITHUB_RELEASES_URL), 256 * 1024) { taskContext.ensureActive() }
            val json = JSONObject(String(bytes, Charsets.UTF_8))
            if (json.optBoolean("draft") || json.optBoolean("prerelease")) return@withContext null
            val tag = json.optString("tag_name", "")
            return@withContext UpdateInfo(
                hasUpdate = ReleaseVersion.isNewer(tag, BuildConfig.VERSION_NAME),
                latestVersionName = tag,
                releaseNotes = json.optString("body", "").take(16_000),
                downloadUrl = RELEASE_PAGE
            )
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Offline, rate-limited or missing release: playback continues without an update prompt.
        }
        return@withContext null
    }
}
