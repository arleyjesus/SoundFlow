package com.example.comarleyaetheraudio.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.file.Files

class VersionNoticeStoreTest {
    @Test fun localVersionSurvivesStoreReopening() = runBlocking {
        val directory = Files.createTempDirectory("soundflow-notices-").toFile()
        val file = File(directory, "notices.preferences_pb")
        suspend fun open(action: suspend (VersionNoticeStore) -> Unit) {
            val job = SupervisorJob()
            val store = PreferenceDataStoreFactory.create(scope = CoroutineScope(job + Dispatchers.IO), produceFile = { file })
            try { action(VersionNoticeStore(store)) } finally { job.cancelAndJoin() }
        }
        try {
            open { assertTrue(it.claimInstalledVersion(42)); assertTrue(it.claimAvailableVersion("v4.0.2")) }
            open { assertFalse(it.claimInstalledVersion(42)); assertFalse(it.claimAvailableVersion("4.0.2")) }
        } finally { directory.deleteRecursively() }
    }
    @Test fun onlyAnUpgradeGetsAnotherLocalChangelog() = runBlocking { withStore {
        assertTrue(it.claimInstalledVersion(42)); assertFalse(it.claimInstalledVersion(41))
        assertTrue(it.claimInstalledVersion(43)); assertFalse(it.claimInstalledVersion(43))
    } }
    @Test fun remoteNoticeRejectsRepeatsRollbacksAndPrereleases() = runBlocking { withStore {
        assertTrue(it.claimAvailableVersion("v4.0.2")); assertFalse(it.claimAvailableVersion("4.0.2"))
        assertFalse(it.claimAvailableVersion("4.0.1")); assertFalse(it.claimAvailableVersion("4.0.3-rc.1"))
        assertTrue(it.claimAvailableVersion("4.0.3"))
    } }
    @Test fun concurrentClaimsHaveOnlyOneWinner() = runBlocking { withStore { store ->
        val results = coroutineScope { (1..12).map { async { store.claimAvailableVersion("4.0.2") } }.awaitAll() }
        assertEquals(1, results.count { it })
    } }
    @Test fun appearanceResetKeepsNoticesAndExistingSettings() = runBlocking {
        withStoreAndRepository { notices, settings ->
            assertTrue(notices.claimInstalledVersion(42)); settings.resetAppearance()
            assertFalse(notices.claimInstalledVersion(42))
        }
    }
    private suspend fun withStore(action: suspend (VersionNoticeStore) -> Unit) =
        withStoreAndRepository { store, _ -> action(store) }
    private suspend fun withStoreAndRepository(action: suspend (VersionNoticeStore, SettingsRepository) -> Unit) {
        val directory = Files.createTempDirectory("soundflow-notices-").toFile()
        val job = SupervisorJob()
        val store = PreferenceDataStoreFactory.create(scope = CoroutineScope(job + Dispatchers.IO), produceFile = { File(directory, "notices.preferences_pb") })
        try { action(VersionNoticeStore(store), SettingsRepository(store)) }
        finally { job.cancelAndJoin(); directory.deleteRecursively() }
    }
}
