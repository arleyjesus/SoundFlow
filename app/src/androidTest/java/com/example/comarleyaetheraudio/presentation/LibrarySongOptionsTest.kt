package com.example.comarleyaetheraudio.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.comarleyaetheraudio.data.local.entity.PlaylistEntity
import com.example.comarleyaetheraudio.presentation.library.LibraryScreen
import com.example.comarleyaetheraudio.testutil.RoomTestLibrary
import com.example.comarleyaetheraudio.testutil.testSong
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibrarySongOptionsTest {
    @get:Rule
    val compose = createComposeRule()

    private lateinit var library: RoomTestLibrary
    private val showLibrary = mutableStateOf(true)
    private var playlistId = 0L

    @Before
    fun setUp() {
        library = RoomTestLibrary()
        runBlocking {
            // Mismo titulo: la seleccion debe distinguir IDs, no buscar por nombre.
            library.database.musicDao().insertSongs(
                listOf(testSong(1, "Mismo titulo", "Artista A"), testSong(2, "Mismo titulo", "Artista B"))
            )
            playlistId = library.database.playlistDao().insertPlaylist(PlaylistEntity(name = "Destino"))
            library.repository.addSongToPlaylist(playlistId, 2)
            library.repository.toggleFavorite(2)
        }
    }

    @After
    fun tearDown() {
        // Retirar los observadores antes de cerrar la base de prueba.
        compose.runOnIdle { showLibrary.value = false }
        compose.waitForIdle()
        library.close()
    }

    @Test
    fun songMenuAddsSelectedIdWithoutReplacingExistingPlaylistSongs() {
        showScreen()
        openOptionsForSong(1)
        compose.onNodeWithText("Añadir a una lista").performClick()
        compose.onNodeWithText("Destino").performClick()
        compose.onNodeWithText("Selecciona una lista:").assertDoesNotExist()

        runBlocking {
            assertEquals(setOf(1L, 2L), library.database.playlistDao().getSongIdsForPlaylist(playlistId).toSet())
        }
    }

    @Test
    fun songMenuTogglesOnlySelectedFavoriteAndReflectsRoomState() {
        showScreen()
        openOptionsForSong(1)
        compose.onNodeWithText("Añadir a Favoritos").performClick()
        assertFavorites(listOf(1, 2))

        openOptionsForSong(1)
        compose.onNodeWithText("Quitar de Favoritos").performClick()
        assertFavorites(listOf(2))
    }

    private fun showScreen() {
        val repository = library.repository
        val initialSongs = runBlocking { withTimeout(5_000) { repository.allSongs.first() } }
        val initialPlaylists = runBlocking {
            withTimeout(5_000) {
                repository.allPlaylists.first { playlists ->
                    playlists.singleOrNull()?.songs?.map { it.id } == listOf(2L)
                }
            }
        }
        val initialFavoriteIds = runBlocking { withTimeout(5_000) { repository.favoriteIds.first() } }
        compose.setContent {
            if (showLibrary.value) {
                val songs by repository.allSongs.collectAsState(initialSongs)
                val playlists by repository.allPlaylists.collectAsState(initialPlaylists)
                val favoriteIds by repository.favoriteIds.collectAsState(initialFavoriteIds)
                MaterialTheme {
                    LibraryScreen(
                        songs = songs,
                        artistGrouped = emptyMap(),
                        playlists = playlists,
                        favoriteIds = favoriteIds,
                        onSongClick = {},
                        onToggleFavorite = { song -> runBlocking { repository.toggleFavorite(song.id) } },
                        onAddToPlaylist = { id, songId ->
                            runBlocking { repository.addSongToPlaylist(id, songId) }
                        },
                        onAddSongsToPlaylist = { _, _ -> error("El menu individual no debe sustituir la seleccion") },
                        onCreatePlaylist = {},
                        onRenamePlaylist = { _, _ -> },
                        onDeletePlaylist = {}
                    )
                }
            }
        }
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodes(hasTestTag("library_song_1")).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun openOptionsForSong(songId: Long) {
        compose.onNode(
            hasContentDescription("Opciones") and hasAnyAncestor(hasTestTag("library_song_$songId")),
            useUnmergedTree = true
        ).performClick()
    }

    private fun assertFavorites(expected: List<Long>) = runBlocking {
        withTimeout(5_000) { library.repository.favoriteIds.first { it == expected } }
        compose.waitForIdle()
    }
}
