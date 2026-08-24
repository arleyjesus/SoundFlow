package com.example.comarleyaetheraudio.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.comarleyaetheraudio.data.local.PlaylistEntity
import com.example.comarleyaetheraudio.data.local.PlaylistSongCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    // 1. Obtener todas las listas ordenadas por fecha de creación
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    // 2. Crear una nueva lista
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    // 3. Eliminar una lista
    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    // =====================================================
    // OPERACIONES PARA LAS CANCIONES DENTRO DE LA LISTA
    // =====================================================

    // 4. Agregar una canción a una lista (usando tu tabla cruzada)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSongToPlaylist(crossRef: PlaylistSongCrossRef)

    // 5. Obtener los IDs de las canciones de una lista específica
    @Query("SELECT songId FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun getSongIdsForPlaylist(playlistId: Long): List<Long>

    // 7. Obtener todas las relaciones para combinarlas en el ViewModel
    @Query("SELECT * FROM playlist_song_cross_ref")
    fun getAllPlaylistSongsFlow(): Flow<List<PlaylistSongCrossRef>>

    // 6. Eliminar una canción de una lista
    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
}