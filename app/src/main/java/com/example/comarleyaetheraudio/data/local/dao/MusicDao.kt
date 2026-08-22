package com.example.comarleyaetheraudio.data.local.dao

import androidx.room.*
import com.example.comarleyaetheraudio.data.local.FavoriteSongEntity
import com.example.comarleyaetheraudio.data.local.PlaylistEntity
import com.example.comarleyaetheraudio.data.local.PlaylistSongCrossRef
import com.example.comarleyaetheraudio.data.local.entity.FolderEntity
import com.example.comarleyaetheraudio.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    // Canciones
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE folderUri = :folderUri ORDER BY title ASC")
    fun getSongsByFolder(folderUri: String): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Query("DELETE FROM songs WHERE folderUri = :folderUri")
    suspend fun deleteSongsByFolder(folderUri: String)

    // Carpetas
    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE uriString = :folderUri")
    suspend fun deleteFolder(folderUri: String)

    // --- FAVORITOS ---
    @Query("SELECT songId FROM favorite_songs")
    fun getFavoriteSongIds(): kotlinx.coroutines.flow.Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteSongEntity)

    @Query("DELETE FROM favorite_songs WHERE songId = :songId")
    suspend fun deleteFavorite(songId: Long)

    // --- PLAYLISTS ---
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): kotlinx.coroutines.flow.Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongToPlaylist(crossRef: PlaylistSongCrossRef)

    @Query("SELECT songId FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    fun getSongIdsForPlaylist(playlistId: Long): kotlinx.coroutines.flow.Flow<List<Long>>


}