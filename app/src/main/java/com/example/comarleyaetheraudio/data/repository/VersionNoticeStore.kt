package com.example.comarleyaetheraudio.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.comarleyaetheraudio.domain.playback.ReleaseVersion

/** Atomic claims keep automatic notices from repeating after reopening or concurrent checks. */
class VersionNoticeStore(private val store: DataStore<Preferences>) {
    private val installed = intPreferencesKey("notice_installed_version_code")
    private val available = stringPreferencesKey("notice_latest_available_version")

    suspend fun claimInstalledVersion(code: Int): Boolean {
        var claimed = false
        store.edit {
            if (code > (it[installed] ?: 0)) { it[installed] = code; claimed = true }
        }
        return claimed
    }

    suspend fun claimAvailableVersion(version: String): Boolean {
        var claimed = false
        val normalized = version.trim().removePrefix("v")
        store.edit {
            if (ReleaseVersion.isNewer(normalized, it[available] ?: "0.0.0")) {
                it[available] = normalized
                claimed = true
            }
        }
        return claimed
    }
}
