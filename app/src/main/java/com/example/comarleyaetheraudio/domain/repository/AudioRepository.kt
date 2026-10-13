package com.example.comarleyaetheraudio.domain.repository

import android.net.Uri
import com.example.comarleyaetheraudio.domain.model.LibraryFolder
import com.example.comarleyaetheraudio.domain.model.FolderImportResult
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * Contrato actualizado de la capa Domain para gestionar canciones y carpetas locales con Room.
 */
interface AudioRepository {
    val allSongs: Flow<List<Song>>
    val allPlaylists: Flow<List<Playlist>>
    val allFolders: Flow<List<LibraryFolder>>
    val favoriteIds: Flow<List<Long>>

    /** The owner supplies its shared library; this repository creates no immortal scope. */
    fun observePlaylists(library: Flow<List<Song>>): Flow<List<Playlist>>
    suspend fun importFolder(uri: Uri): FolderImportResult
    suspend fun restoreFolderAccess(originalUri: String, selectedUri: Uri): FolderImportResult
    suspend fun toggleFavorite(songId: Long)
    suspend fun createPlaylist(name: String)
    suspend fun renamePlaylist(playlistId: Long, newName: String)
    suspend fun deletePlaylist(playlist: Playlist)
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long)
    suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>)
    suspend fun updateSongTags(songId: Long, newTitle: String, newArtist: String, newAlbum: String)

    suspend fun inspectAudio(uri: Uri): com.example.comarleyaetheraudio.domain.playback.AudioInspection =
        com.example.comarleyaetheraudio.domain.playback.AudioInspection()

    /**
     * Flujo reactivo en tiempo real con todas las canciones guardadas en la BD local.
     */
    fun getSongsFlow(): Flow<List<Song>> = allSongs

    /**
     * Flujo reactivo con todas las carpetas seleccionadas por el usuario.
     */
    fun getFoldersFlow(): Flow<List<LibraryFolder>> = allFolders

    /**
     * Guarda los permisos de una nueva carpeta seleccionada via SAF y la analiza.
     */
    suspend fun addAndScanFolder(folderUri: Uri) { importFolder(folderUri) }

    /**
     * Elimina una carpeta de la BD local y remueve sus canciones asociadas.
     */
    suspend fun removeFolder(folderUri: String)

    fun getFavoriteSongIds(): Flow<List<Long>> = favoriteIds
}
