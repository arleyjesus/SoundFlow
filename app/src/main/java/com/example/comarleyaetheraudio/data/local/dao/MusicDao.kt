package com.example.comarleyaetheraudio.data.local

import androidx.room.*
import com.example.comarleyaetheraudio.data.local.entity.FolderEntity
import com.example.comarleyaetheraudio.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Query("DELETE FROM songs WHERE folderUri = :folderUri")
    suspend fun deleteSongsByFolderUri(folderUri: String)

    @Query("DELETE FROM songs WHERE path = :path")
    suspend fun deleteSongsByPath(path: String)

    @Query("DELETE FROM songs")
    suspend fun clearAllSongs()

    @Query("UPDATE songs SET title = :newTitle, artist = :newArtist, album = :newAlbum WHERE id = :songId")
    suspend fun updateSongTags(songId: Long, newTitle: String, newArtist: String, newAlbum: String)

    // GESTIÓN DE CARPETAS
    @Query("SELECT * FROM folders")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE path = :path")
    suspend fun deleteFolderByPath(path: String)
}