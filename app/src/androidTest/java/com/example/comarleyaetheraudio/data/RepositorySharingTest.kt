package com.example.comarleyaetheraudio.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.comarleyaetheraudio.data.local.entity.PlaylistEntity
import com.example.comarleyaetheraudio.domain.repository.AudioRepository
import com.example.comarleyaetheraudio.testutil.RoomTestLibrary
import com.example.comarleyaetheraudio.testutil.testSong
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class RepositorySharingTest {
    @Test
    fun legacyHiResFlagIsNotPublishedOrRewritten() = runBlocking {
        val library = RoomTestLibrary()
        try {
            library.database.musicDao().insertSongs(listOf(testSong(1).copy(isHiRes = true)))
            assertFalse(library.repository.allSongs.first().single().isHiRes)
            assertTrue(library.database.musicDao().getAllSongsSnapshot().single().isHiRes)
        } finally { library.close() }
    }

    @Test
    fun playlistsUseTheProvidedLibraryAndReactToRelationChanges() = runBlocking {
        val library = RoomTestLibrary()
        try {
            library.database.musicDao().insertSongs(listOf(testSong(1), testSong(2)))
            val id = library.database.playlistDao().insertPlaylist(PlaylistEntity(name = "Compartida"))
            val repository: AudioRepository = library.repository
            val snapshot = repository.allSongs.first()
            val supplied = MutableStateFlow(snapshot.map { it.copy(title = "Desde el flujo compartido") })
            repository.addSongToPlaylist(id, 1)
            val observed = repository.observePlaylists(supplied)
            val initial = withTimeout(5_000) { observed.first { it.single().songs.size == 1 } }
            assertEquals("Desde el flujo compartido", initial.single().songs.single().title)
            repository.addSongToPlaylist(id, 2)
            val updated = withTimeout(5_000) { observed.first { it.single().songs.size == 2 } }
            assertEquals(setOf(1L, 2L), updated.single().songs.map { it.id }.toSet())
            supplied.value = supplied.value.filter { it.id == 2L }
            assertEquals(listOf(2L), withTimeout(5_000) { observed.first() }.single().songs.map { it.id })
            assertEquals(2, library.database.musicDao().getAllSongsSnapshot().size)
        } finally { library.close() }
    }

    @Test
    fun libraryAndPlaylistsShareOneUpstreamSubscription() = runBlocking {
        val library = RoomTestLibrary()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        try {
            library.database.musicDao().insertSongs(listOf(testSong(1)))
            val repository: AudioRepository = library.repository
            val subscriptions = AtomicInteger()
            val shared = repository.allSongs.onStart { subscriptions.incrementAndGet() }
                .stateIn(scope, SharingStarted.Eagerly, emptyList())
            withTimeout(5_000) { shared.first { it.isNotEmpty() } }
            withTimeout(5_000) { repository.observePlaylists(shared).first() }
            assertEquals(1, subscriptions.get())
        } finally {
            scope.coroutineContext[Job]!!.cancelAndJoin()
            library.close()
        }
    }
}
