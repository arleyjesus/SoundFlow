package com.example.comarleyaetheraudio.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.comarleyaetheraudio.data.local.FolderImportConflictException
import com.example.comarleyaetheraudio.data.local.entity.FolderEntity
import com.example.comarleyaetheraudio.data.local.entity.PlaylistEntity
import com.example.comarleyaetheraudio.testutil.RoomTestLibrary
import com.example.comarleyaetheraudio.testutil.testSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FolderIntegrityTest {
    private lateinit var library: RoomTestLibrary
    private val folder = FolderEntity("content://soundflow.test/folder", "Pruebas", "content://soundflow.test/folder", 2, 123)
    private val other = FolderEntity("content://soundflow.test/other", "Otra", "content://soundflow.test/other", 1, 456)

    @Before
    fun setUp() = runBlocking {
        library = RoomTestLibrary()
        library.database.musicDao().apply {
            insertSongs(listOf(testSong(1), testSong(2), testSong(3).copy(folderUri = other.uriString)))
            insertFolder(folder)
            insertFolder(other)
        }
    }

    @After
    fun tearDown() = library.close()

    @Test
    fun reimportPreservesIdsEditedTagsFavoritesAndPlaylist() = runBlocking {
        library.repository.toggleFavorite(1)
        val playlistId = library.database.playlistDao().insertPlaylist(PlaylistEntity(name = "Conservar"))
        library.repository.addSongToPlaylist(playlistId, 1)
        library.repository.updateSongTags(1, "Titulo editado", "Mi artista", "Mi album")

        val result = library.database.musicDao().importFolderSongs(folder, listOf(testSong(1), testSong(2)))
        library.reopen()

        assertEquals(0, result.added)
        assertEquals(2, result.alreadyPresent)
        val songs = library.database.musicDao().getAllSongsSnapshot()
        assertEquals(setOf(1L, 2L, 3L), songs.map { it.id }.toSet())
        assertEquals("Titulo editado", songs.first { it.id == 1L }.title)
        assertTrue(library.database.musicDao().isFavorite(1))
        assertEquals(listOf(1L), library.database.playlistDao().getSongIdsForPlaylist(playlistId))
    }

    @Test
    fun newUriIsInsertedOnceAndIgnoresScannerId() = runBlocking {
        val incoming = testSong(4).copy(id = 999)
        val result = library.database.musicDao().importFolderSongs(folder, listOf(testSong(1), incoming, incoming))
        library.reopen()
        val songs = library.database.musicDao().getAllSongsSnapshot()
        assertEquals(1, result.added)
        assertEquals(1, result.alreadyPresent)
        assertEquals(4, songs.size)
        assertEquals(1, songs.count { it.contentUri == incoming.contentUri })
        assertNotEquals(999L, songs.single { it.contentUri == incoming.contentUri }.id)
        assertEquals(3, library.database.musicDao().countSongsForFolder(folder.uriString))
    }

    @Test
    fun emptyAndPartialScansPreservePreviouslyImportedSongs() = runBlocking {
        val dao = library.database.musicDao()
        dao.importFolderSongs(folder, emptyList())
        dao.importFolderSongs(folder, listOf(testSong(1)))
        library.reopen()
        assertEquals(setOf(1L, 2L, 3L), library.database.musicDao().getAllSongsSnapshot().map { it.id }.toSet())
        val folders = withTimeout(5_000) { library.database.musicDao().getAllFolders().first() }
        assertEquals(2, folders.single { it.uriString == folder.uriString }.songCount)
        assertEquals(other, folders.single { it.uriString == other.uriString })
    }

    @Test
    fun concurrentImportsDoNotDuplicateNewUri() = runBlocking {
        coroutineScope {
            repeat(2) {
                launch(Dispatchers.IO) {
                    library.database.musicDao().importFolderSongs(folder, listOf(testSong(4)))
                }
            }
        }
        library.reopen()
        assertEquals(1, library.database.musicDao().getAllSongsSnapshot().count { it.contentUri == testSong(4).contentUri })
    }

    @Test
    fun folderWriteFailureRollsBackNewSongs() = runBlocking {
        library.database.openHelper.writableDatabase.execSQL(
            """CREATE TRIGGER phase2_fail_import BEFORE INSERT ON folders
                WHEN NEW.uriString = '${folder.uriString}'
                BEGIN SELECT RAISE(ABORT, 'phase2_failure'); END"""
        )
        expectSqlFailure { library.database.musicDao().importFolderSongs(folder, listOf(testSong(4))) }
        library.reopen()
        assertEquals(setOf(1L, 2L, 3L), library.database.musicDao().getAllSongsSnapshot().map { it.id }.toSet())
        assertEquals(folder, withTimeout(5_000) { library.database.musicDao().getAllFolders().first() }.single { it.uriString == folder.uriString })
    }

    @Test
    fun removingFolderCleansOnlyItsSongsAndRelations() = runBlocking {
        library.repository.toggleFavorite(1)
        library.repository.toggleFavorite(3)
        val playlistId = library.database.playlistDao().insertPlaylist(PlaylistEntity(name = "Conservar lista"))
        library.repository.addSongsToPlaylist(playlistId, listOf(1, 2, 3))

        library.repository.removeFolder(folder.path)
        library.reopen()

        assertEquals(listOf(3L), library.database.musicDao().getAllSongsSnapshot().map { it.id })
        assertEquals(listOf(3L), withTimeout(5_000) { library.repository.favoriteIds.first() })
        assertEquals(listOf(3L), library.database.playlistDao().getSongIdsForPlaylist(playlistId))
        assertTrue(library.database.playlistDao().playlistExists(playlistId))
        assertEquals(listOf(other), withTimeout(5_000) { library.database.musicDao().getAllFolders().first() })
    }

    @Test
    fun removalFailureRestoresSongsFavoritesAndRelations() = runBlocking {
        library.repository.toggleFavorite(1)
        val playlistId = library.database.playlistDao().insertPlaylist(PlaylistEntity(name = "Rollback"))
        library.repository.addSongsToPlaylist(playlistId, listOf(1, 2))
        library.database.openHelper.writableDatabase.execSQL(
            """CREATE TRIGGER phase2_fail_remove BEFORE DELETE ON folders
                WHEN OLD.uriString = '${folder.uriString}'
                BEGIN SELECT RAISE(ABORT, 'phase2_failure'); END"""
        )
        expectSqlFailure { library.repository.removeFolder(folder.path) }
        library.reopen()
        assertEquals(setOf(1L, 2L, 3L), library.database.musicDao().getAllSongsSnapshot().map { it.id }.toSet())
        assertTrue(library.database.musicDao().isFavorite(1))
        assertEquals(setOf(1L, 2L), library.database.playlistDao().getSongIdsForPlaylist(playlistId).toSet())
        assertEquals(folder.uriString, library.database.musicDao().getFolderUriByPath(folder.path))
    }

    @Test
    fun overlappingExactUrisRejectImportWithoutMovingOwnership() = runBlocking {
        try {
            library.database.musicDao().importFolderSongs(other, listOf(testSong(1).copy(folderUri = other.uriString)))
            fail("Debe rechazarse el solapamiento")
        } catch (_: FolderImportConflictException) {
            // La transaccion conserva la pertenencia original.
        }
        library.reopen()
        assertEquals(folder.uriString, library.database.musicDao().getAllSongsSnapshot().single { it.id == 1L }.folderUri)
        assertEquals(1, library.database.musicDao().countSongsForFolder(other.uriString))
    }

    private suspend fun expectSqlFailure(action: suspend () -> Unit) {
        var failure: Exception? = null
        try { action() } catch (e: Exception) { failure = e }
        assertNotNull(failure)
        assertTrue(failure.toString().contains("phase2_failure"))
    }
}
