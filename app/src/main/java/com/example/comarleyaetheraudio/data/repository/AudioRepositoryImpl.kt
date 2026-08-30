package com.example.comarleyaetheraudio.data.repository

import android.content.Context
import android.net.Uri
import com.example.comarleyaetheraudio.data.local.FolderScanner
import com.example.comarleyaetheraudio.data.local.MusicDao
import com.example.comarleyaetheraudio.data.local.PlaylistEntity
import com.example.comarleyaetheraudio.data.local.PlaylistSongCrossRef
import com.example.comarleyaetheraudio.data.local.dao.PlaylistDao
import com.example.comarleyaetheraudio.data.local.entity.FolderEntity
import com.example.comarleyaetheraudio.data.local.entity.SongEntity
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class AudioRepositoryImpl(
    val context: Context,
    private val musicDao: MusicDao,
    private val playlistDao: PlaylistDao,
    private val scanner: FolderScanner
) {
    // FAVORITOS EN MEMORIA
    private val _favoriteIds = MutableStateFlow<List<Long>>(emptyList())
    val favoriteIds: Flow<List<Long>> = _favoriteIds.asStateFlow()

    // TRANSFORMACIÓN CORREGIDA DE CANCIONES CON URIs
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

    // OBTENER PLAYLISTS DESDE ROOM (PERSISTENCIA REAL EN BASE DE DATOS)
    val allPlaylists: Flow<List<Playlist>> = combine(
        playlistDao.getAllPlaylists(),
        playlistDao.getAllPlaylistSongsFlow(),
        allSongs
    ) { playlistEntities, crossRefs, songs ->
        playlistEntities.map { pEntity ->
            val songIdsInPlaylist = crossRefs
                .filter { it.playlistId == pEntity.id }
                .map { it.songId }

            val playlistSongs = songs.filter { songIdsInPlaylist.contains(it.id) }

            Playlist(
                id = pEntity.id,
                name = pEntity.name,
                songs = playlistSongs
            )
        }
    }

    val allFolders: Flow<List<FolderEntity>> = musicDao.getAllFolders()

    suspend fun toggleFavorite(songId: Long) {
        val currentList = _favoriteIds.value.toMutableList()
        if (currentList.contains(songId)) currentList.remove(songId) else currentList.add(songId)
        _favoriteIds.value = currentList
    }

    // GESTIÓN PERSISTENTE DE PLAYLISTS (ROOM)
    suspend fun createPlaylist(name: String) {
        val newEntity = PlaylistEntity(name = name, createdAt = System.currentTimeMillis())
        playlistDao.insertPlaylist(newEntity)
    }

    suspend fun renamePlaylist(playlistId: Long, newName: String) {
        val entity = PlaylistEntity(id = playlistId, name = newName, createdAt = System.currentTimeMillis())
        playlistDao.insertPlaylist(entity)
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        val entity = PlaylistEntity(id = playlist.id, name = playlist.name)
        playlistDao.deletePlaylist(entity)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        playlistDao.insertSongToPlaylist(PlaylistSongCrossRef(playlistId = playlistId, songId = songId))
    }

    suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        // 1. Obtenemos los IDs de las canciones que actualmente tiene la playlist
        val currentSongIds = playlistDao.getSongIdsForPlaylist(playlistId)

        // 2. Calculamos cuáles son nuevas y cuáles han sido desmarcadas
        val toAdd = songIds - currentSongIds.toSet()
        val toRemove = currentSongIds - songIds.toSet()

        // 3. Insertamos las canciones nuevas
        toAdd.forEach { songId ->
            playlistDao.insertSongToPlaylist(PlaylistSongCrossRef(playlistId = playlistId, songId = songId))
        }

        // 4. Eliminamos las canciones que desmarcaste
        toRemove.forEach { songId ->
            playlistDao.removeSongFromPlaylist(playlistId = playlistId, songId = songId)
        }
    }

    // MÉTODOS DE BASE DE DATOS PARA CANCIONES Y CARPETAS
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