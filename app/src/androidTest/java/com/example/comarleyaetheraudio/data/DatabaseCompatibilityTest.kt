package com.example.comarleyaetheraudio.data

import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.comarleyaetheraudio.data.local.MusicDatabase
import com.example.comarleyaetheraudio.data.local.entity.FavoriteSongEntity
import com.example.comarleyaetheraudio.data.local.entity.FolderEntity
import com.example.comarleyaetheraudio.data.local.entity.PlaylistEntity
import com.example.comarleyaetheraudio.testutil.testSong
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Comprueba la politica real de apertura, sin tocar soundflow_database. */
@RunWith(AndroidJUnit4::class)
class DatabaseCompatibilityTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val name = "soundflow_compatibility_test_${UUID.randomUUID()}"
    private var database: MusicDatabase? = null

    @Before
    fun seedDatabase() = runBlocking {
        val db = MusicDatabase.buildDatabase(context, name)
        database = db
        db.musicDao().insertSongs(listOf(testSong(7)))
        db.musicDao().insertFolder(FolderEntity("content://soundflow.test/folder", "Pruebas", "Pruebas", 1, 123))
        db.musicDao().insertFavorite(FavoriteSongEntity(7))
        db.playlistDao().insertPlaylist(PlaylistEntity(9, "Conservar", 456))
        db.playlistDao().addSongToPlaylist(9, 7)
        closeDatabase()
    }

    @After
    fun tearDown() {
        closeDatabase()
        context.deleteDatabase(name)
    }

    @Test
    fun currentVersionReopensWithAllPersistentData() = runBlocking {
        val db = MusicDatabase.buildDatabase(context, name)
        database = db
        assertEquals(listOf(testSong(7)), db.musicDao().getAllSongsSnapshot())
        assertEquals(listOf(7L), withTimeout(5_000) { db.musicDao().getFavoriteSongIds().first() })
        assertEquals(listOf(PlaylistEntity(9, "Conservar", 456)), withTimeout(5_000) { db.playlistDao().getAllPlaylists().first() })
        assertEquals(listOf(7L), db.playlistDao().getSongIdsForPlaylist(9))
        assertEquals(listOf(FolderEntity("content://soundflow.test/folder", "Pruebas", "Pruebas", 1, 123)), withTimeout(5_000) { db.musicDao().getAllFolders().first() })
    }

    @Test
    fun missingUpgradeMigrationRejectsOpeningWithoutErasingData() {
        rawDatabase().use { it.version = 1 }
        assertOpeningRejected("migration")
        assertRawDataPreserved(expectedVersion = 1)
    }

    @Test
    fun missingDowngradeMigrationRejectsOpeningWithoutErasingData() {
        rawDatabase().use { it.version = 3 }
        assertOpeningRejected("migration")
        assertRawDataPreserved(expectedVersion = 3)
    }

    @Test
    fun unknownIdentityAtVersionTwoIsNotSilentlyReplaced() {
        rawDatabase().use {
            it.execSQL("UPDATE room_master_table SET identity_hash = 'phase1_unknown_identity' WHERE id = 42")
        }
        assertOpeningRejected("integrity")
        assertRawDataPreserved(expectedVersion = 2)
        rawDatabase(readOnly = true).use { raw ->
            raw.rawQuery("SELECT identity_hash FROM room_master_table WHERE id = 42", null).use {
                assertTrue(it.moveToFirst())
                assertEquals("phase1_unknown_identity", it.getString(0))
            }
        }
    }

    private fun assertOpeningRejected(expectedMessage: String) {
        val db = MusicDatabase.buildDatabase(context, name)
        database = db
        var failure: Exception? = null
        try {
            db.openHelper.writableDatabase
        } catch (e: Exception) {
            failure = e
        } finally {
            closeDatabase()
        }
        assertNotNull("La apertura incompatible debe fallar sin recrear tablas", failure)
        val messages = generateSequence(failure as Throwable?) { it.cause }
            .joinToString(" ") { it.message.orEmpty() }
        assertTrue(messages, messages.contains(expectedMessage, ignoreCase = true))
    }

    private fun assertRawDataPreserved(expectedVersion: Int) {
        rawDatabase(readOnly = true).use { raw ->
            assertEquals(expectedVersion, raw.version)
            val expectedRows = mapOf(
                "SELECT id, title, contentUri FROM songs" to listOf("7", "Cancion 7", "content://soundflow.test/audio/7"),
                "SELECT songId FROM favorite_songs" to listOf("7"),
                "SELECT id, name, createdAt FROM playlists" to listOf("9", "Conservar", "456"),
                "SELECT playlistId, songId FROM playlist_song_cross_ref" to listOf("9", "7"),
                "SELECT uriString, name, songCount, lastScannedMs FROM folders" to listOf("content://soundflow.test/folder", "Pruebas", "1", "123")
            )
            expectedRows.forEach { (query, expected) ->
                raw.rawQuery(query, null).use { cursor ->
                    assertEquals(query, 1, cursor.count)
                    assertTrue(cursor.moveToFirst())
                    assertEquals(query, expected, (0 until cursor.columnCount).map { cursor.getString(it) })
                }
            }
        }
    }

    private fun rawDatabase(readOnly: Boolean = false): SQLiteDatabase =
        SQLiteDatabase.openDatabase(
            context.getDatabasePath(name).absolutePath,
            null,
            if (readOnly) SQLiteDatabase.OPEN_READONLY else SQLiteDatabase.OPEN_READWRITE
        )

    private fun closeDatabase() {
        database?.close()
        database = null
    }
}
