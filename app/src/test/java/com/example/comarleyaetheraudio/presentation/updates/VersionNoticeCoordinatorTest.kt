package com.example.comarleyaetheraudio.presentation.updates

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.comarleyaetheraudio.data.remote.GitHubUpdateChecker.UpdateInfo
import com.example.comarleyaetheraudio.data.repository.VersionNoticeStore
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.Files

class VersionNoticeCoordinatorTest {
    private val update = UpdateInfo(true, "v4.0.2", "New release", "https://github.com/arleyjesus/SoundFlow/releases/latest")
    @Test fun changelogWorksOfflineWithoutNetworkAndShowsOnce() = runBlocking { withStore { store ->
        var calls = 0
        val coordinator = VersionNoticeCoordinator(store, 42, { calls++; update })
        coordinator.onForeground(false)
        assertTrue(coordinator.state.value.showChangelog); assertEquals(0, calls)
        coordinator.dismissChangelog(); coordinator.onForeground(false)
        assertFalse(coordinator.state.value.showChangelog)
    } }
    @Test fun availableNoticeWaitsForInstalledNotesAndSurvivesReopeningAsSeen() = runBlocking { withStore { store ->
        val first = VersionNoticeCoordinator(store, 42, { update })
        first.onForeground(true)
        assertTrue(first.state.value.showChangelog); assertNull(first.state.value.update)
        first.dismissChangelog(); assertEquals(update, first.state.value.update)
        first.dismissUpdate()
        val reopened = VersionNoticeCoordinator(store, 42, { update })
        reopened.onForeground(true)
        assertFalse(reopened.state.value.showChangelog); assertNull(reopened.state.value.update)
    } }
    @Test fun unseenDeferredUpdateIsNotConsumedByClosingDuringChangelog() = runBlocking { withStore { store ->
        VersionNoticeCoordinator(store, 42, { update }).onForeground(true)
        val reopened = VersionNoticeCoordinator(store, 42, { update })
        reopened.onForeground(true)
        assertEquals(update, reopened.state.value.update)
    } }
    @Test fun cooldownAvoidsRepeatedResumeRequestsButAllowsPeriodicCheck() = runBlocking { withStore { store ->
        var now = 0L; var calls = 0
        val coordinator = VersionNoticeCoordinator(store, 42, { calls++; null }, { now })
        coordinator.onForeground(true); coordinator.onForeground(true); assertEquals(1, calls)
        now = 60_000; coordinator.onForeground(true); assertEquals(2, calls)
    } }
    @Test fun transientFailureDoesNotConsumeFutureNotice() = runBlocking { withStore { store ->
        var now = 0L; var fail = true
        val coordinator = VersionNoticeCoordinator(store, 42, { if (fail) throw IOException("offline") else update }, { now })
        coordinator.onForeground(true); coordinator.dismissChangelog()
        fail = false; now = 60_000; coordinator.onForeground(true)
        assertEquals(update, coordinator.state.value.update)
    } }
    @Test fun disablingOnlineUpdatesSuppressesAnInFlightResult() = runBlocking { withStore { store ->
        val entered = CompletableDeferred<Unit>(); val response = CompletableDeferred<UpdateInfo>()
        val coordinator = VersionNoticeCoordinator(store, 42, { entered.complete(Unit); response.await() })
        val request = launch { coordinator.onForeground(true) }
        entered.await()
        withTimeout(2000) { coordinator.onForeground(false); coordinator.dismissChangelog() }
        response.complete(update); request.join()
        assertNull(coordinator.state.value.update)
    } }
    @Test fun noUpdateDoesNotCreateAnAnnouncement() = runBlocking { withStore { store ->
        val coordinator = VersionNoticeCoordinator(store, 42, { update.copy(hasUpdate = false) })
        coordinator.onForeground(true); coordinator.dismissChangelog()
        assertNull(coordinator.state.value.update)
    } }
    @Test fun manualPresentationPreventsLaterAutomaticDuplicate() = runBlocking { withStore { store ->
        val coordinator = VersionNoticeCoordinator(store, 42, { update })
        coordinator.onForeground(false); coordinator.dismissChangelog()
        coordinator.noteManualPresentation(update.latestVersionName)
        coordinator.onForeground(true)
        assertNull(coordinator.state.value.update)
    } }
    private suspend fun withStore(action: suspend CoroutineScope.(VersionNoticeStore) -> Unit) = coroutineScope {
        val directory = Files.createTempDirectory("soundflow-notice-ui-").toFile()
        val job = SupervisorJob()
        val store = PreferenceDataStoreFactory.create(scope = CoroutineScope(job + Dispatchers.IO), produceFile = { File(directory, "notices.preferences_pb") })
        try { action(VersionNoticeStore(store)) } finally { job.cancelAndJoin(); directory.deleteRecursively() }
    }
}
