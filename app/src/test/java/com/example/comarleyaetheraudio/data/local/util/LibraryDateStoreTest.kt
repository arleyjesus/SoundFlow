package com.example.comarleyaetheraudio.data.local.util

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.file.Files

class LibraryDateStoreTest {
    @Test fun cachedDatesSurviveReopeningAndDoNotChangeOnTagEdit() = runBlocking {
        val directory = Files.createTempDirectory("soundflow-dates-").toFile()
        val file = File(directory, "dates.preferences_pb")
        suspend fun open(action: suspend (LibraryDateStore) -> Unit, date: Long) {
            val job = SupervisorJob()
            val store = PreferenceDataStoreFactory.create(scope = CoroutineScope(job + Dispatchers.IO), produceFile = { file })
            try { action(LibraryDateStore(store) { date }) } finally { job.cancelAndJoin() }
        }
        try {
            open({ assertEquals(2000L, it.timestamps(listOf("content://test/song"))["content://test/song"]) }, 2000)
            open({ assertEquals(2000L, it.timestamps(listOf("content://test/song"))["content://test/song"]) }, 9000)
        } finally { directory.deleteRecursively() }
    }
    @Test fun equivalentSafUrisShareDateWithoutChangingTheirAccessUris() = runBlocking {
        withStore { factory ->
            var reads = 0
            val resolver = factory { reads++; 1234 }
            val uris = listOf("content://provider/tree/root/document/song", "content://provider/document/song")
            assertEquals(mapOf(uris[0] to 1234L, uris[1] to 1234L), resolver.timestamps(uris))
            assertEquals(1, reads)
        }
    }
    @Test fun cancellationDoesNotPersistAnIncompleteSnapshot() = runBlocking {
        withStore { factory ->
            val resolver = factory { if (it.endsWith("b")) throw CancellationException("cancel") else 2000 }
            try { resolver.timestamps(listOf("content://test/a", "content://test/b")); fail("Expected cancellation") }
            catch (_: CancellationException) { }
            assertEquals(3000L, factory { 3000 }.timestamps(listOf("content://test/a"))["content://test/a"])
        }
    }
    @Test fun unavailableDatesStayUnknownInsteadOfInventingDownloadTime() = runBlocking {
        withStore { factory ->
            assertEquals(0L, factory { -1 }.timestamps(listOf("content://test/a"))["content://test/a"])
        }
    }
    @Test fun unavailableDateCanRecoverOnNextSnapshot() = runBlocking {
        withStore { factory ->
            val uri = "content://test/a"
            assertEquals(0L, factory { 0 }.timestamps(listOf(uri))[uri])
            assertEquals(6000L, factory { 6000 }.timestamps(listOf(uri))[uri])
        }
    }
    private suspend fun withStore(action: suspend (((suspend (String) -> Long) -> LibraryDateStore)) -> Unit) {
        val directory = Files.createTempDirectory("soundflow-dates-").toFile()
        val job = SupervisorJob()
        val store = PreferenceDataStoreFactory.create(scope = CoroutineScope(job + Dispatchers.IO), produceFile = { File(directory, "dates.preferences_pb") })
        try { action { reader -> LibraryDateStore(store, reader) } } finally { job.cancelAndJoin(); directory.deleteRecursively() }
    }
}
