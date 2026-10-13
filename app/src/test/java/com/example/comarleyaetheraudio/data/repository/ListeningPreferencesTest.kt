package com.example.comarleyaetheraudio.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collect
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.file.Files

class ListeningPreferencesTest {
    @Test fun counterWritesDoNotRestartEnabledObservation() = runBlocking {
        withStore { repository ->
            val changes = kotlinx.coroutines.channels.Channel<Boolean>(kotlinx.coroutines.channels.Channel.UNLIMITED)
            val collector = launch { repository.listeningEnabled.collect { changes.send(it) } }
            try {
                assertFalse(withTimeout(5000) { changes.receive() })
                repository.setListeningEnabled(true)
                assertTrue(withTimeout(5000) { changes.receive() })
                repository.recordListening(1)
                assertNull(withTimeoutOrNull(150) { changes.receive() })
            } finally { collector.cancelAndJoin(); changes.close() }
        }
    }
    @Test fun productionStoresKeepHistorySeparateFromGeneralSettings() = runBlocking {
        val directory = Files.createTempDirectory("soundflow-separated-").toFile()
        val job = SupervisorJob()
        val scope = CoroutineScope(job + Dispatchers.IO)
        val general = PreferenceDataStoreFactory.create(scope = scope, produceFile = { File(directory, "settings.preferences_pb") })
        val history = PreferenceDataStoreFactory.create(scope = scope, produceFile = { File(directory, "history.preferences_pb") })
        try {
            val repository = SettingsRepository(general, history)
            repository.setListeningEnabled(true); repository.recordListening(1)
            assertTrue(general.data.first().asMap().isEmpty())
            assertFalse(history.data.first().asMap().isEmpty())
            assertEquals(mapOf(1L to 1), repository.listeningCounts.first())
        } finally { job.cancelAndJoin(); directory.deleteRecursively() }
    }
    @Test fun listeningRequiresOptInAndCanBeDeletedIndependently() = runBlocking {
        withStore { repository ->
            assertFalse(repository.listeningEnabled.first())
            repository.recordListening(1)
            assertTrue(repository.listeningCounts.first().isEmpty())
            repository.setListeningEnabled(true); repository.recordListening(1); repository.recordListening(1)
            repository.setBackground("content://isolated/image")
            repository.resetAppearance()
            assertEquals(mapOf(1L to 2), repository.listeningCounts.first())
            assertNull(repository.appearance.first().backgroundUri)
            repository.clearListeningCounts()
            assertTrue(repository.listeningCounts.first().isEmpty())
            assertTrue(repository.listeningEnabled.first())
        }
    }
    @Test fun disablingRecordingPreservesExistingCounts() = runBlocking {
        withStore { repository ->
            repository.setListeningEnabled(true); repository.recordListening(3)
            repository.setListeningEnabled(false); repository.recordListening(3)
            assertEquals(mapOf(3L to 1), repository.listeningCounts.first())
        }
    }
    @Test fun wallpaperAndHistorySurviveReopeningStore() = runBlocking {
        val directory = Files.createTempDirectory("soundflow-history-").toFile()
        val file = File(directory, "isolated.preferences_pb")
        suspend fun open(action: suspend (SettingsRepository) -> Unit) {
            val job = SupervisorJob()
            val store = PreferenceDataStoreFactory.create(scope = CoroutineScope(job + Dispatchers.IO), produceFile = { file })
            try { action(SettingsRepository(store)) } finally { job.cancelAndJoin() }
        }
        try {
            open { it.setListeningEnabled(true); it.recordListening(9); it.setBackground("content://isolated/image") }
            open {
                assertEquals(mapOf(9L to 1), it.listeningCounts.first())
                assertEquals("content://isolated/image", it.appearance.first().backgroundUri)
            }
        } finally { directory.deleteRecursively() }
    }
    private suspend fun withStore(action: suspend (SettingsRepository) -> Unit) {
        val directory = Files.createTempDirectory("soundflow-history-").toFile()
        val job = SupervisorJob()
        val store = PreferenceDataStoreFactory.create(scope = CoroutineScope(job + Dispatchers.IO), produceFile = { File(directory, "isolated.preferences_pb") })
        try { action(SettingsRepository(store)) } finally { job.cancelAndJoin(); directory.deleteRecursively() }
    }
}
