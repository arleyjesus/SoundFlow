
package com.example.comarleyaetheraudio.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.comarleyaetheraudio.data.local.entity.FavoriteSongEntity
import com.example.comarleyaetheraudio.data.local.entity.FolderEntity
import com.example.comarleyaetheraudio.data.local.entity.SongEntity
import com.example.comarleyaetheraudio.data.local.util.songIdentity
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    // CANCIONES

    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs")
    suspend fun getAllSongsSnapshot(): List<SongEntity>

    @Query("SELECT * FROM songs WHERE folderUri = :folderUri")
    suspend fun getSongsForFolderSnapshot(folderUri: String): List<SongEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNewSongs(songs: List<SongEntity>)

    @Query("SELECT contentUri FROM songs")
    suspend fun getSongUris(): List<String>

    @Query("SELECT contentUri FROM songs WHERE folderUri != :folderUri")
    suspend fun getSongUrisOutsideFolder(folderUri: String): List<String>

    // Mismo documento SAF: proveedor + ID opaco. Otras fuentes: URI exacta.
    // Conservar ID, URI de acceso y metadatos; no fusionar filas historicas.
    @Transaction
    suspend fun insertSongsPreservingIds(songs: List<SongEntity>): SongImportResult {
        require(songs.all { it.contentUri.isNotBlank() })
        val uniqueSongs = songs.distinctBy { songIdentity(it.contentUri) }
        val existingIdentities = getSongUris().mapTo(hashSetOf()) { songIdentity(it) }
        val newSongs = uniqueSongs.filter { songIdentity(it.contentUri) !in existingIdentities }
        if (newSongs.isNotEmpty()) insertNewSongs(newSongs.map { it.copy(id = 0) })
        return SongImportResult(newSongs.size, uniqueSongs.size - newSongs.size)
    }

    @Query("SELECT COUNT(*) FROM songs WHERE folderUri = :folderUri")
    suspend fun countSongsForFolder(folderUri: String): Int

    @Transaction
    suspend fun importFolderSongs(folder: FolderEntity, songs: List<SongEntity>): SongImportResult {
        require(songs.all { it.folderUri == folder.uriString })
        val requestedIdentities = songs.mapTo(hashSetOf()) { songIdentity(it.contentUri) }
        if (getSongUrisOutsideFolder(folder.uriString).any { songIdentity(it) in requestedIdentities }) {
            throw FolderImportConflictException()
        }

        val result = insertSongsPreservingIds(songs)
        // Un resultado vacio o parcial nunca retira canciones anteriores.
        insertFolder(folder.copy(songCount = countSongsForFolder(folder.uriString)))
        return result
    }

    @Query("DELETE FROM songs WHERE folderUri = :folderUri")
    suspend fun deleteSongsByFolderUri(folderUri: String)

    @Query("DELETE FROM songs WHERE path = :path")
    suspend fun deleteSongsByPath(path: String)

    @Query("DELETE FROM songs")
    suspend fun clearAllSongs()

    @Query(
        """
        UPDATE songs
        SET title = :newTitle,
            artist = :newArtist,
            album = :newAlbum
        WHERE id = :songId
        """
    )
    suspend fun updateSongTags(
        songId: Long,
        newTitle: String,
        newArtist: String,
        newAlbum: String
    )

    // FAVORITOS PERSISTENTES

    @Query("SELECT songId FROM favorite_songs ORDER BY songId")
    fun getFavoriteSongIds(): Flow<List<Long>>

    @Query(
        "SELECT EXISTS(SELECT 1 FROM favorite_songs WHERE songId = :songId)"
    )
    suspend fun isFavorite(songId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(favorite: FavoriteSongEntity)

    @Query("DELETE FROM favorite_songs WHERE songId = :songId")
    suspend fun deleteFavorite(songId: Long)

    @Transaction
    suspend fun toggleFavorite(songId: Long) {
        if (isFavorite(songId)) {
            deleteFavorite(songId)
        } else {
            insertFavorite(FavoriteSongEntity(songId))
        }
    }

    // CARPETAS

    @Query("SELECT * FROM folders")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE path = :path")
    suspend fun deleteFolderByPath(path: String)

    @Query("SELECT uriString FROM folders WHERE path = :path LIMIT 1")
    suspend fun getFolderUriByPath(path: String): String?

    @Query("DELETE FROM favorite_songs WHERE songId IN (SELECT id FROM songs WHERE folderUri = :folderUri)")
    suspend fun deleteFavoritesForFolder(folderUri: String)

    @Query("DELETE FROM playlist_song_cross_ref WHERE songId IN (SELECT id FROM songs WHERE folderUri = :folderUri)")
    suspend fun deletePlaylistRelationsForFolder(folderUri: String)

    @Transaction
    suspend fun removeFolderAndRelations(path: String) {
        val uri = getFolderUriByPath(path) ?: return
        deleteFavoritesForFolder(uri)
        deletePlaylistRelationsForFolder(uri)
        deleteSongsByFolderUri(uri)
        deleteFolderByPath(path)
    }
}
