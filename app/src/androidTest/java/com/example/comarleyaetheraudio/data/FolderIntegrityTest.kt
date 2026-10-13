package com.example.comarleyaetheraudio.data

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.comarleyaetheraudio.data.local.FolderImportConflictException
import com.example.comarleyaetheraudio.data.local.FolderAccessMismatchException
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

    @Test
    fun sameFolderDocumentAliasPreservesStoredIdUriTagsAndRelations() = runBlocking {
        val original = "content://soundflow.documents/tree/rootA/document/music%3A8"
        val alias = "content://soundflow.documents/tree/rootB/document/music%3A8"
        val stored = testSong(8, "Titulo editado").copy(contentUri = original)
        library.database.musicDao().insertSongs(listOf(stored))
        library.repository.toggleFavorite(8)
        val playlistId = library.database.playlistDao().insertPlaylist(PlaylistEntity(name = "Alias"))
        library.repository.addSongToPlaylist(playlistId, 8)

        val result = library.database.musicDao().importFolderSongs(
            folder, listOf(testSong(0).copy(contentUri = alias))
        )
        library.reopen()
        assertEquals(0, result.added)
        assertEquals(1, result.alreadyPresent)
        assertEquals(stored, library.database.musicDao().getAllSongsSnapshot().single { it.id == 8L })
        assertTrue(library.database.musicDao().isFavorite(8))
        assertEquals(listOf(8L), library.database.playlistDao().getSongIdsForPlaylist(playlistId))
    }

    @Test
    fun aliasInAnotherFolderRejectsWholeImportBeforeAddingAnySongs() = runBlocking {
        library.database.musicDao().insertSongs(listOf(testSong(8).copy(
            contentUri = "content://soundflow.documents/tree/rootA/document/music%3A8"
        )))
        val before = library.database.musicDao().getAllSongsSnapshot()
        try {
            library.database.musicDao().importFolderSongs(other, listOf(
                testSong(0).copy(contentUri = "content://soundflow.documents/tree/rootB/document/music%3A8", folderUri = other.uriString),
                testSong(9).copy(folderUri = other.uriString)
            ))
            fail("Debe rechazarse el documento compartido entre arboles")
        } catch (_: FolderImportConflictException) { }
        library.reopen()
        assertEquals(before.sortedBy { it.id }, library.database.musicDao().getAllSongsSnapshot().sortedBy { it.id })
        assertEquals(other, withTimeout(5_000) { library.database.musicDao().getAllFolders().first() }.single { it.uriString == other.uriString })
    }

    @Test
    fun aliasesWithinOneScanInsertOnlyOneDocument() = runBlocking {
        val result = library.database.musicDao().importFolderSongs(folder, listOf(
            testSong(0).copy(contentUri = "content://soundflow.documents/tree/rootA/document/music%3A8"),
            testSong(0).copy(contentUri = "content://soundflow.documents/tree/rootB/document/music%3A8")
        ))
        library.reopen()
        assertEquals(1, result.added)
        assertEquals(0, result.alreadyPresent)
        assertEquals(4, library.database.musicDao().getAllSongsSnapshot().size)
    }

    @Test
    fun deniedPersistablePermissionLeavesLibraryFavoritesAndPlaylistUntouched() = runBlocking {
        library.repository.toggleFavorite(1)
        val playlistId = library.database.playlistDao().insertPlaylist(PlaylistEntity(name = "Permiso"))
        library.repository.addSongsToPlaylist(playlistId, listOf(1, 2))
        val songsBefore = library.database.musicDao().getAllSongsSnapshot()
        val foldersBefore = withTimeout(5_000) { library.database.musicDao().getAllFolders().first() }
        try {
            // No existe una concesion del sistema para este proveedor aislado.
            library.repository.importFolder(Uri.parse("content://soundflow.test.denied/tree/no-grant"))
            fail("Debe propagarse la denegacion de permiso antes de escribir")
        } catch (_: SecurityException) { }
        library.reopen()
        assertEquals(songsBefore.sortedBy { it.id }, library.database.musicDao().getAllSongsSnapshot().sortedBy { it.id })
        assertEquals(foldersBefore.sortedBy { it.uriString }, withTimeout(5_000) { library.database.musicDao().getAllFolders().first() }.sortedBy { it.uriString })
        assertTrue(library.database.musicDao().isFavorite(1))
        assertEquals(setOf(1L, 2L), library.database.playlistDao().getSongIdsForPlaylist(playlistId).toSet())
    }

    @Test
    fun restoringAnotherFolderRejectsBeforePermissionOrDatabaseChanges() = runBlocking {
        library.repository.toggleFavorite(1)
        val playlistId = library.database.playlistDao().insertPlaylist(PlaylistEntity(name = "Restaurar"))
        library.repository.addSongToPlaylist(playlistId, 1)
        val before = library.database.musicDao().getAllSongsSnapshot()
        try {
            library.repository.restoreFolderAccess(folder.uriString, Uri.parse(other.uriString))
            fail("No debe importarse otra carpeta durante recuperacion")
        } catch (_: FolderAccessMismatchException) { }
        library.reopen()
        assertEquals(before.sortedBy { it.id }, library.database.musicDao().getAllSongsSnapshot().sortedBy { it.id })
        assertTrue(library.database.musicDao().isFavorite(1))
        assertEquals(listOf(1L), library.database.playlistDao().getSongIdsForPlaylist(playlistId))
        assertEquals(folder, withTimeout(5_000) { library.database.musicDao().getAllFolders().first() }.single { it.uriString == folder.uriString })
    }

    @Test
    fun historicalDuplicatesKeepBothIdsAndTheirReferences() = runBlocking {
        val uri = "content://soundflow.documents/tree/root/document/music%3A8"
        val duplicates = listOf(testSong(8, "Primera").copy(contentUri = uri), testSong(9, "Segunda").copy(contentUri = uri))
        library.database.musicDao().insertSongs(duplicates)
        library.repository.toggleFavorite(8)
        library.repository.toggleFavorite(9)
        val playlistId = library.database.playlistDao().insertPlaylist(PlaylistEntity(name = "Historicos"))
        library.repository.addSongsToPlaylist(playlistId, listOf(8, 9))
        val result = library.database.musicDao().importFolderSongs(folder, listOf(testSong(0).copy(contentUri = uri)))
        library.reopen()
        assertEquals(0, result.added)
        assertEquals(duplicates, library.database.musicDao().getSongsForFolderSnapshot(folder.uriString).filter { it.id >= 8 }.sortedBy { it.id })
        assertTrue(library.database.musicDao().isFavorite(8))
        assertTrue(library.database.musicDao().isFavorite(9))
        assertEquals(setOf(8L, 9L), library.database.playlistDao().getSongIdsForPlaylist(playlistId).toSet())
    }

    @Test
    fun tenThousandSongsReimportWithoutDuplicatesOrLostReferences() = runBlocking {
        val batch = (100L until 10_100L).map { testSong(it) }
        assertEquals(10_000, library.database.musicDao().importFolderSongs(folder, batch).added)
        val before = library.database.musicDao().getSongsForFolderSnapshot(folder.uriString).sortedBy { it.id }
        val protectedId = before.last().id
        library.repository.toggleFavorite(protectedId)
        val playlistId = library.database.playlistDao().insertPlaylist(PlaylistEntity(name = "Grande"))
        library.repository.addSongToPlaylist(playlistId, protectedId)
        val second = library.database.musicDao().importFolderSongs(folder, batch + batch)
        library.reopen()
        assertEquals(0, second.added)
        assertEquals(10_000, second.alreadyPresent)
        assertEquals(before, library.database.musicDao().getSongsForFolderSnapshot(folder.uriString).sortedBy { it.id })
        assertEquals(10_002, library.database.musicDao().countSongsForFolder(folder.uriString))
        assertTrue(library.database.musicDao().isFavorite(protectedId))
        assertEquals(listOf(protectedId), library.database.playlistDao().getSongIdsForPlaylist(playlistId))
    }

    private suspend fun expectSqlFailure(action: suspend () -> Unit) {
        var failure: Exception? = null
        try { action() } catch (e: Exception) { failure = e }
        assertNotNull(failure)
        assertTrue(failure.toString().contains("phase2_failure"))
    }
}
