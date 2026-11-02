package com.example.comarleyaetheraudio.presentation.updates

import com.example.comarleyaetheraudio.data.remote.GitHubUpdateChecker.UpdateInfo
import com.example.comarleyaetheraudio.data.repository.VersionNoticeStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException

data class VersionNoticeState(val showChangelog: Boolean = false, val update: UpdateInfo? = null)

/** Retained by LibraryViewModel. No background job, network loop or Android UI dependency here. */
class VersionNoticeCoordinator(
    private val store: VersionNoticeStore,
    private val currentCode: Int,
    private val check: suspend () -> UpdateInfo?,
    private val clock: () -> Long = { System.nanoTime() / 1_000_000 },
    private val cooldownMs: Long = 60_000
) {
    private val lock = Mutex()
    private val mutableState = MutableStateFlow(VersionNoticeState())
    val state = mutableState.asStateFlow()
    private var initialized = false
    private var enabled = false
    private var lastAttempt: Long? = null
    private var pending: UpdateInfo? = null
    private var generation = 0L

    suspend fun onForeground(onlineUpdates: Boolean) {
        try {
            val requestGeneration = lock.withLock {
                if (enabled != onlineUpdates) generation++
                enabled = onlineUpdates
                if (!initialized) {
                    val show = store.claimInstalledVersion(currentCode)
                    mutableState.value = mutableState.value.copy(showChangelog = show)
                    initialized = true
                }
                if (!enabled) {
                    pending = null
                    mutableState.value = mutableState.value.copy(update = null)
                    return@withLock null
                }
                val now = clock()
                val previous = lastAttempt
                if (previous != null && now >= previous && now - previous < cooldownMs) return@withLock null
                lastAttempt = now
                generation
            } ?: return
            // Do not hold the state lock over network IO: dismissal/privacy must respond immediately.
            val info = check()
            lock.withLock {
                if (enabled && generation == requestGeneration) {
                    if (info?.hasUpdate == true) pending = info
                    offerPending()
                }
            }
        } catch (e: CancellationException) { throw e }
        catch (_: IOException) { /* Storage/network failure must not interrupt local music. */ }
    }

    suspend fun dismissChangelog() = lock.withLock {
        mutableState.value = mutableState.value.copy(showChangelog = false)
        try { offerPending() } catch (_: IOException) { }
    }

    suspend fun dismissUpdate() = lock.withLock {
        mutableState.value = mutableState.value.copy(update = null)
    }

    suspend fun noteManualPresentation(version: String) = lock.withLock {
        store.claimAvailableVersion(version)
        if (mutableState.value.update?.latestVersionName == version) {
            mutableState.value = mutableState.value.copy(update = null)
        }
        if (pending?.latestVersionName == version) pending = null
    }

    private suspend fun offerPending() {
        if (!enabled || mutableState.value.showChangelog || mutableState.value.update != null) return
        val info = pending ?: return
        if (store.claimAvailableVersion(info.latestVersionName)) {
            mutableState.value = mutableState.value.copy(update = info)
        }
        pending = null
    }
}
