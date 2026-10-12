
package com.example.comarleyaetheraudio.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.comarleyaetheraudio.data.local.entity.FavoriteSongEntity
import com.example.comarleyaetheraudio.data.local.entity.FolderEntity
import com.example.comarleyaetheraudio.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    // CANCIONES

    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs")
    suspend fun getAllSongsSnapshot(): List<SongEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

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
}
