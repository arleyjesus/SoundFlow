package com.example.comarleyaetheraudio.data.repository

import android.content.Context
import android.net.Uri
import com.example.comarleyaetheraudio.data.local.FolderScanner
import com.example.comarleyaetheraudio.data.local.MusicDao
import com.example.comarleyaetheraudio.data.local.entity.FolderEntity
import com.example.comarleyaetheraudio.data.local.entity.SongEntity
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class AudioRepositoryImpl(
    val context: Context,
    private val musicDao: MusicDao,
    private val scanner: FolderScanner
) {
    // ESTADOS EN MEMORIA (Evita que Room crashee por tablas inexistentes)
    private val _favoriteIds = MutableStateFlow<List<Long>>(emptyList())
    val favoriteIds: Flow<List<Long>> = _favoriteIds.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val allPlaylists: Flow<List<Playlist>> = _playlists.asStateFlow()

    // TRANSFORMACIÓN CORREGIDA CON URIs Y TODOS LOS PARÁMETROS
    val allSongs: Flow<List<Song>> = musicDao.getAllSongs().map { entities ->
        entities.map { entity ->
            Song(
                id = entity.id,
                title = entity.title,
                artist = entity.artist,
                album = entity.album,
                duration = entity.duration,
                path = entity.path,
                contentUri = Uri.parse(entity.contentUri),
                albumArtUri = entity.albumArtUri?.let { Uri.parse(it) },
                size = entity.size,
                mimeType = entity.mimeType
            )
        }
    }

    val allFolders: Flow<List<FolderEntity>> = musicDao.getAllFolders()

    suspend fun toggleFavorite(songId: Long) {
        val currentList = _favoriteIds.value.toMutableList()
        if (currentList.contains(songId)) currentList.remove(songId) else currentList.add(songId)
        _favoriteIds.value = currentList
    }

    // GESTIÓN REACTIVA DE PLAYLISTS
    suspend fun createPlaylist(name: String) {
        val current = _playlists.value.toMutableList()
        current.add(Playlist(id = System.currentTimeMillis(), name = name, songs = emptyList()))
        _playlists.value = current
    }

    suspend fun renamePlaylist(playlistId: Long, newName: String) {
        _playlists.value = _playlists.value.map {
            if (it.id == playlistId) it.copy(name = newName) else it
        }
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        val current = _playlists.value.toMutableList()
        current.removeAll { it.id == playlist.id }
        _playlists.value = current
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        // Implementación futura cuando la base de datos de Playlists esté construida
    }

    // MÉTODOS DE BASE DE DATOS
    suspend fun updateSongTags(songId: Long, newTitle: String, newArtist: String, newAlbum: String) {
        musicDao.updateSongTags(songId, newTitle, newArtist, newAlbum)
    }

    suspend fun insertSongsEntities(songs: List<SongEntity>) {
        musicDao.insertSongs(songs)
    }

    suspend fun insertFolder(folder: FolderEntity) {
        musicDao.insertFolder(folder)
    }

    suspend fun deleteFolderByPath(path: String) {
        musicDao.deleteFolderByPath(path)
    }

    suspend fun deleteSongsByFolderUri(folderUri: String) {
        musicDao.deleteSongsByFolderUri(folderUri)
    }
}