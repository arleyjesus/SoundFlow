package com.example.comarleyaetheraudio.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.comarleyaetheraudio.data.local.entity.PlaylistEntity
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.testutil.RoomTestLibrary
import com.example.comarleyaetheraudio.testutil.testSong
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaylistPersistenceTest {
    private lateinit var library: RoomTestLibrary

    @Before
    fun setUp() {
        library = RoomTestLibrary()
        runBlocking {
            library.database.musicDao().insertSongs((1L..5L).map { testSong(it) })
        }
    }

    @After
    fun tearDown() {
        library.close()
    }

    @Test
    fun favoritesSurviveCloseAndReopenIncludingRemoval() = runBlocking {
        library.repository.toggleFavorite(1)
        library.repository.toggleFavorite(2)
        assertEquals(listOf(1L, 2L), favoriteIds())

        library.reopen()
        assertEquals(listOf(1L, 2L), favoriteIds())

        library.repository.toggleFavorite(1)
        assertEquals(listOf(2L), favoriteIds())
        library.reopen()
        assertEquals(listOf(2L), favoriteIds())
    }

    @Test
    fun favoriteFlowEmitsPersistedChanges() = runBlocking {
        val initialEmission = CompletableDeferred<Unit>()
        val emissions = async {
            withTimeout(5_000) {
                library.repository.favoriteIds
                    .onEach { initialEmission.complete(Unit) }
                    .take(2)
                    .toList()
            }
        }
        withTimeout(5_000) { initialEmission.await() }
        library.repository.toggleFavorite(1)

        assertEquals(listOf(emptyList<Long>(), listOf(1L)), emissions.await())
    }

    @Test
    fun renamePreservesCreationTimeSongsAndOtherPlaylists() = runBlocking {
        val id = createPlaylist("Original", createdAt = 123456L)
        val otherId = createPlaylist("Otra", createdAt = 654321L)
        library.repository.addSongToPlaylist(id, 1)

        library.repository.renamePlaylist(id, "  Renombrada  ")
        library.reopen()

        val playlists = withTimeout(5_000) { library.database.playlistDao().getAllPlaylists().first() }
        assertEquals(PlaylistEntity(id, "Renombrada", 123456L), playlists.first { it.id == id })
        assertEquals(PlaylistEntity(otherId, "Otra", 654321L), playlists.first { it.id == otherId })
        assertEquals(setOf(1L), songIds(id))
    }

    @Test
    fun deleteRemovesOnlyItsRelationsAndPreservesSongsAndFavorites() = runBlocking {
        val id = createPlaylist("Eliminar")
        val otherId = createPlaylist("Conservar")
        library.repository.addSongsToPlaylist(id, listOf(1, 2))
        library.repository.addSongToPlaylist(otherId, 2)
        library.repository.toggleFavorite(1)

        library.repository.deletePlaylist(Playlist(id, "Eliminar"))
        library.reopen()

        assertEquals(emptySet<Long>(), songIds(id))
        assertEquals(setOf(2L), songIds(otherId))
        assertEquals(listOf(1L), favoriteIds())
        assertEquals(5, library.database.musicDao().getAllSongsSnapshot().size)
        val remaining = withTimeout(5_000) { library.database.playlistDao().getAllPlaylists().first() }
        assertEquals(listOf(otherId), remaining.map { it.id })
    }

    @Test
    fun addingOneSongIsAdditiveAndIdempotent() = runBlocking {
        val id = createPlaylist("Adicion")
        val otherId = createPlaylist("Otra")
        library.repository.addSongsToPlaylist(id, listOf(1, 2))
        library.repository.addSongToPlaylist(otherId, 4)

        library.repository.addSongToPlaylist(id, 3)
        library.repository.addSongToPlaylist(id, 3)
        library.reopen()

        assertEquals(setOf(1L, 2L, 3L), songIds(id))
        assertEquals(3, library.database.playlistDao().getSongIdsForPlaylist(id).size)
        assertEquals(setOf(4L), songIds(otherId))
    }

    @Test
    fun completeSelectionDeduplicatesAndCanClearWithoutDeletingSongs() = runBlocking {
        val id = createPlaylist("Seleccion")
        val otherId = createPlaylist("Otra")
        library.repository.addSongsToPlaylist(id, listOf(1, 2))
        library.repository.addSongToPlaylist(otherId, 1)

        library.repository.addSongsToPlaylist(id, listOf(2, 3, 3))
        library.reopen()
        assertEquals(setOf(2L, 3L), songIds(id))
        assertEquals(2, library.database.playlistDao().getSongIdsForPlaylist(id).size)

        library.repository.addSongsToPlaylist(id, emptyList())
        library.reopen()
        assertEquals(emptySet<Long>(), songIds(id))
        assertEquals(setOf(1L), songIds(otherId))
        assertEquals(5, library.database.musicDao().getAllSongsSnapshot().size)
    }

    @Test
    fun failureDuringSelectionRollsBackEarlierInserts() = runBlocking {
        val id = createPlaylist("Rollback")
        library.repository.addSongsToPlaylist(id, listOf(1, 2))
        // El algoritmo agrega 3 antes de retirar 1. Un fallo al retirar 1
        // debe revertir tambien esa insercion, no dejar [1, 2, 3].
        library.database.openHelper.writableDatabase.execSQL(
            """CREATE TRIGGER phase1a_fail_remove BEFORE DELETE ON playlist_song_cross_ref
                WHEN OLD.playlistId = $id AND OLD.songId = 1
                BEGIN SELECT RAISE(ABORT, 'phase1a_failure'); END"""
        )
        var failure: Exception? = null
        try {
            library.repository.addSongsToPlaylist(id, listOf(2, 3))
        } catch (e: Exception) {
            failure = e
        }

        assertNotNull("Debe dispararse el fallo SQL de prueba", failure)
        assertTrue(failure.toString().contains("phase1a_failure"))
        library.reopen()
        assertEquals(setOf(1L, 2L), songIds(id))
    }

    @Test
    fun failureDeletingPlaylistRestoresItsRelations() = runBlocking {
        val id = createPlaylist("Rollback eliminacion")
        library.repository.addSongsToPlaylist(id, listOf(1, 2))
        library.database.openHelper.writableDatabase.execSQL(
            """CREATE TRIGGER phase1a_fail_delete BEFORE DELETE ON playlists
                WHEN OLD.id = $id
                BEGIN SELECT RAISE(ABORT, 'phase1a_failure'); END"""
        )
        var failure: Exception? = null
        try {
            library.repository.deletePlaylist(Playlist(id, "Rollback eliminacion"))
        } catch (e: Exception) {
            failure = e
        }

        assertNotNull(failure)
        assertTrue(failure.toString().contains("phase1a_failure"))
        library.reopen()
        assertTrue(library.database.playlistDao().playlistExists(id))
        assertEquals(setOf(1L, 2L), songIds(id))
    }

    @Test
    fun concurrentSelectionsLeaveOneCompleteSelection() = runBlocking {
        val id = createPlaylist("Concurrencia")
        repeat(10) {
            library.repository.addSongsToPlaylist(id, listOf(1, 2))
            coroutineScope {
                launch(Dispatchers.IO) { library.repository.addSongsToPlaylist(id, listOf(2, 3)) }
                launch(Dispatchers.IO) { library.repository.addSongsToPlaylist(id, listOf(4, 5)) }
            }
            assertTrue(songIds(id) in setOf(setOf(2L, 3L), setOf(4L, 5L)))
        }
    }

    @Test
    fun staleEditsDoNotRecreateRelationsForDeletedPlaylist() = runBlocking {
        val id = createPlaylist("Eliminada")
        library.repository.deletePlaylist(Playlist(id, "Eliminada"))

        library.repository.addSongToPlaylist(id, 1)
        library.repository.addSongsToPlaylist(id, listOf(2, 3))

        assertEquals(emptySet<Long>(), songIds(id))
    }

    private suspend fun createPlaylist(name: String, createdAt: Long = 1000L): Long =
        library.database.playlistDao().insertPlaylist(PlaylistEntity(name = name, createdAt = createdAt))

    private suspend fun songIds(id: Long): Set<Long> =
        library.database.playlistDao().getSongIdsForPlaylist(id).toSet()

    private suspend fun favoriteIds(): List<Long> =
        withTimeout(5_000) { library.repository.favoriteIds.first() }
}
