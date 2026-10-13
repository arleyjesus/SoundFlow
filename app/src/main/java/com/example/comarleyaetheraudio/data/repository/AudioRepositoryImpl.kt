
package com.example.comarleyaetheraudio.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.comarleyaetheraudio.data.local.MusicDao
import com.example.comarleyaetheraudio.data.local.FolderAccessMismatchException
import com.example.comarleyaetheraudio.data.local.SongImportResult
import com.example.comarleyaetheraudio.data.local.dao.PlaylistDao
import com.example.comarleyaetheraudio.data.local.entity.FolderEntity
import com.example.comarleyaetheraudio.data.local.entity.PlaylistEntity
import com.example.comarleyaetheraudio.data.local.entity.SongEntity
import com.example.comarleyaetheraudio.data.local.util.FolderScanner
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.domain.model.LibraryFolder
import com.example.comarleyaetheraudio.domain.repository.AudioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class AudioRepositoryImpl(
    val context: Context,
    private val musicDao: MusicDao,
    private val playlistDao: PlaylistDao,
    private val scanner: FolderScanner
) : AudioRepository {
    private val audioInspector = com.example.comarleyaetheraudio.data.local.util.AndroidAudioInspector(context)

    override suspend fun inspectAudio(uri: Uri) = audioInspector.inspect(uri)

    // FAVORITOS PERSISTENTES EN ROOM

    override val favoriteIds: Flow<List<Long>> =
        musicDao.getFavoriteSongIds()

    override suspend fun toggleFavorite(songId: Long) {
        musicDao.toggleFavorite(songId)
    }

    // BIBLIOTECA MUSICAL

    override val allSongs: Flow<List<Song>> =
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
                    // The legacy stored flag was inferred without source bit depth; never trust it.
                    isHiRes = false,
                    path = entity.path,
                    folderPath = entity.folderName,
                    folderUri = entity.folderUri
                )
            }
        }.flowOn(Dispatchers.Default)

    // PLAYLISTS

    override val allPlaylists: Flow<List<Playlist>> get() = observePlaylists(allSongs)

    override fun observePlaylists(library: Flow<List<Song>>): Flow<List<Playlist>> = combine(
        playlistDao.getAllPlaylists(),
        playlistDao.getAllPlaylistSongsFlow(),
        library
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
    }.flowOn(Dispatchers.Default)

    // CARPETAS

    override val allFolders: Flow<List<LibraryFolder>> = musicDao.getAllFolders().map { folders ->
        folders.map { LibraryFolder(it.uriString, it.name, it.path, it.songCount, it.lastScannedMs) }
    }.flowOn(Dispatchers.Default)

    override suspend fun importFolder(uri: Uri): SongImportResult {
        val resolver = context.contentResolver
        if (resolver.persistedUriPermissions.none { it.uri == uri && it.isReadPermission }) {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val folderPath = uri.toString()
        val folderName = uri.lastPathSegment?.substringAfterLast(":") ?: "Carpeta"
        // El escaneo termina antes de abrir la transaccion de escritura.
        val songs = scanner.scanFolderUri(uri, folderName, musicDao.getSongsForFolderSnapshot(folderPath))
        return musicDao.importFolderSongs(
            FolderEntity(uriString = folderPath, name = folderName, path = folderPath), songs
        )
    }

    override suspend fun restoreFolderAccess(originalUri: String, selectedUri: Uri): SongImportResult {
        // Recuperar acceso nunca debe importar otra carpeta ni mover referencias.
        if (originalUri != selectedUri.toString()) throw FolderAccessMismatchException()
        return importFolder(selectedUri)
    }

    override suspend fun removeFolder(folderUri: String) {
        musicDao.removeFolderAndRelations(folderUri)
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

    // PLAYLISTS: CREAR

    override suspend fun createPlaylist(name: String) {
        val cleanName = name.trim()

        if (cleanName.isBlank()) return

        playlistDao.insertPlaylist(
            PlaylistEntity(name = cleanName)
        )
    }

    // PLAYLISTS: RENOMBRAR SIN ALTERAR CREATEDAT

    override suspend fun renamePlaylist(
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

    override suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylistAndRelations(
            PlaylistEntity(
                id = playlist.id,
                name = playlist.name
            )
        )
    }

    // PLAYLISTS: AGREGAR UNA CANCION

    override suspend fun addSongToPlaylist(
        playlistId: Long,
        songId: Long
    ) {
        playlistDao.addSongToPlaylist(playlistId, songId)
    }

    // PLAYLISTS: SINCRONIZAR SELECCION DE CANCIONES
    // Se conserva el nombre publico por compatibilidad. La lista es la
    // seleccion completa; para una adicion individual usar addSongToPlaylist.

    override suspend fun addSongsToPlaylist(
        playlistId: Long,
        songIds: List<Long>
    ) {
        playlistDao.setPlaylistSongs(playlistId, songIds)
    }

    // METADATOS

    override suspend fun updateSongTags(
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
    // Conservar el contrato existente sin volver a insertar URIs conocidas.

    suspend fun insertSongsEntities(
        songs: List<SongEntity>
    ) {
        if (songs.isEmpty()) return

        musicDao.insertSongsPreservingIds(songs)
    }
}
