package com.example.comarleyaetheraudio.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.comarleyaetheraudio.domain.model.NetworkFeature
import com.example.comarleyaetheraudio.domain.model.NetworkSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.file.Files

class NetworkSettingsTest {
    @Test fun defaultsPreserveExistingBehaviorAndSwitchesAreIndependent() = runBlocking {
        withStore { open ->
            open { repository ->
                assertEquals(NetworkSettings(), repository.networkSettings.first())
                repository.setNetworkFeature(NetworkFeature.ARTIST_IMAGES, false)
                assertEquals(NetworkSettings(false, true, true), repository.networkSettings.first())
                repository.setNetworkFeature(NetworkFeature.LYRICS, false)
                repository.setNetworkFeature(NetworkFeature.UPDATES, false)
                repository.setNetworkFeature(NetworkFeature.LYRICS, true)
                assertEquals(NetworkSettings(false, true, false), repository.networkSettings.first())
            }
        }
    }

    @Test fun networkChoicesSurviveReopeningAndAppearanceReset() = runBlocking {
        withStore { open ->
            open { repository -> NetworkFeature.entries.forEach { repository.setNetworkFeature(it, false) } }
            open { repository ->
                assertEquals(NetworkSettings(false, false, false), repository.networkSettings.first())
                repository.resetAppearance()
                assertEquals(NetworkSettings(false, false, false), repository.networkSettings.first())
            }
        }
    }

    private suspend fun withStore(action: suspend (suspend (suspend (SettingsRepository) -> Unit) -> Unit) -> Unit) {
        val directory = Files.createTempDirectory("soundflow-network-").toFile()
        val file = File(directory, "isolated.preferences_pb")
        suspend fun open(block: suspend (SettingsRepository) -> Unit) {
            val job = SupervisorJob()
            val store = PreferenceDataStoreFactory.create(scope = CoroutineScope(job + Dispatchers.IO), produceFile = { file })
            try { withTimeout(5_000) { block(SettingsRepository(store)) } } finally { job.cancelAndJoin() }
        }
        try { action(::open) } finally { directory.deleteRecursively() }
    }
}
