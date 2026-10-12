
package com.example.comarleyaetheraudio.data.repository

import android.content.Context
import android.net.Uri
import com.example.comarleyaetheraudio.data.local.MusicDao
import com.example.comarleyaetheraudio.data.local.dao.PlaylistDao
import com.example.comarleyaetheraudio.data.local.entity.FolderEntity
import com.example.comarleyaetheraudio.data.local.entity.PlaylistEntity
import com.example.comarleyaetheraudio.data.local.entity.SongEntity
import com.example.comarleyaetheraudio.data.local.util.FolderScanner
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class AudioRepositoryImpl(
    val context: Context,
    private val musicDao: MusicDao,
    private val playlistDao: PlaylistDao,
    private val scanner: FolderScanner
) {

    // FAVORITOS PERSISTENTES EN ROOM

    val favoriteIds: Flow<List<Long>> =
        musicDao.getFavoriteSongIds()

    suspend fun toggleFavorite(songId: Long) {
        musicDao.toggleFavorite(songId)
    }

    // BIBLIOTECA MUSICAL

    val allSongs: Flow<List<Song>> =
        musicDao.getAllSongs().map { entities ->
            entities.map { entity ->
                Song(
                    id = entity.id,
                    title = entity.title,
                    artist = entity.artist,
                    album = entity.album,
                    genre = entity.genre,
                    year = entity.year,
                    trackNumber = entity.trackNumber,
                    duration = entity.duration,
                    contentUri = Uri.parse(entity.contentUri),
                    albumArtUri = entity.albumArtUri?.let(Uri::parse),
                    size = entity.size,
                    mimeType = entity.mimeType,
                    bitrate = entity.bitrate,
                    sampleRate = entity.sampleRate,
                    isHiRes = entity.isHiRes,
                    path = entity.path,
                    folderPath = entity.folderName,
                    folderUri = entity.folderUri
                )
            }
        }

    // PLAYLISTS

    val allPlaylists: Flow<List<Playlist>> = combine(
        playlistDao.getAllPlaylists(),
        playlistDao.getAllPlaylistSongsFlow(),
        allSongs
    ) { playlistEntities, crossRefs, songs ->

        val songsById = songs.associateBy { it.id }

        val songIdsByPlaylist = crossRefs
            .groupBy { it.playlistId }

        playlistEntities.map { playlistEntity ->

            val playlistSongs = songIdsByPlaylist[
                playlistEntity.id
            ].orEmpty().mapNotNull { crossRef ->
                songsById[crossRef.songId]
            }

            Playlist(
                id = playlistEntity.id,
                name = playlistEntity.name,
                songs = playlistSongs
            )
        }
    }

    // CARPETAS

    val allFolders: Flow<List<FolderEntity>> =
        musicDao.getAllFolders()

    suspend fun insertFolder(folder: FolderEntity) {
        musicDao.insertFolder(folder)
    }

    suspend fun deleteFolderByPath(path: String) {
        musicDao.deleteFolderByPath(path)
    }

    suspend fun deleteSongsByFolderUri(folderUri: String) {
        musicDao.deleteSongsByFolderUri(folderUri)
    }

    // PLAYLISTS: CREAR

    suspend fun createPlaylist(name: String) {
        val cleanName = name.trim()

        if (cleanName.isBlank()) return

        playlistDao.insertPlaylist(
            PlaylistEntity(name = cleanName)
        )
    }

    // PLAYLISTS: RENOMBRAR SIN ALTERAR CREATEDAT

    suspend fun renamePlaylist(
        playlistId: Long,
        newName: String
    ) {
        val cleanName = newName.trim()

        if (cleanName.isBlank()) return

        playlistDao.updatePlaylistName(
            playlistId = playlistId,
            newName = cleanName
        )
    }

    // PLAYLISTS: ELIMINAR CON SUS RELACIONES

    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylistAndRelations(
            PlaylistEntity(
                id = playlist.id,
                name = playlist.name
            )
        )
    }

    // PLAYLISTS: AGREGAR UNA CANCION

    suspend fun addSongToPlaylist(
        playlistId: Long,
        songId: Long
    ) {
        playlistDao.addSongToPlaylist(playlistId, songId)
    }

    // PLAYLISTS: SINCRONIZAR SELECCION DE CANCIONES
    // Se conserva el nombre publico por compatibilidad. La lista es la
    // seleccion completa; para una adicion individual usar addSongToPlaylist.

    suspend fun addSongsToPlaylist(
        playlistId: Long,
        songIds: List<Long>
    ) {
        playlistDao.setPlaylistSongs(playlistId, songIds)
    }

    // METADATOS

    suspend fun updateSongTags(
        songId: Long,
        newTitle: String,
        newArtist: String,
        newAlbum: String
    ) {
        musicDao.updateSongTags(
            songId = songId,
            newTitle = newTitle,
            newArtist = newArtist,
            newAlbum = newAlbum
        )
    }

    // INSERCION DE CANCIONES
    // La sincronizacion antiduplicados se abordara
    // en la siguiente etapa de forma transaccional.

    suspend fun insertSongsEntities(
        songs: List<SongEntity>
    ) {
        if (songs.isEmpty()) return

        musicDao.insertSongs(songs)
    }
}
