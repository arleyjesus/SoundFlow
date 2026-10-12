
package com.example.comarleyaetheraudio.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.comarleyaetheraudio.data.local.entity.PlaylistEntity
import com.example.comarleyaetheraudio.data.local.entity.PlaylistSongCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    // PLAYLISTS

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query(
        "UPDATE playlists SET name = :newName WHERE id = :playlistId"
    )
    suspend fun updatePlaylistName(
        playlistId: Long,
        newName: String
    )

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    // RELACIONES ENTRE PLAYLISTS Y CANCIONES

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSongToPlaylist(
        crossRef: PlaylistSongCrossRef
    )

    @Query(
        "SELECT songId FROM playlist_song_cross_ref WHERE playlistId = :playlistId"
    )
    suspend fun getSongIdsForPlaylist(
        playlistId: Long
    ): List<Long>

    @Query("SELECT EXISTS(SELECT 1 FROM playlists WHERE id = :playlistId)")
    suspend fun playlistExists(playlistId: Long): Boolean

    // Comprobar la playlist e insertar en la misma transaccion evita
    // recrear relaciones si la lista fue eliminada desde otra pantalla.
    @Transaction
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        if (!playlistExists(playlistId)) return
        insertSongToPlaylist(PlaylistSongCrossRef(playlistId, songId))
    }

    // La lista recibida representa la seleccion completa, no una adicion.
    // Leer, agregar y retirar deben confirmarse o revertirse juntos.
    @Transaction
    suspend fun setPlaylistSongs(playlistId: Long, songIds: List<Long>) {
        if (!playlistExists(playlistId)) return

        val currentIds = getSongIdsForPlaylist(playlistId).toSet()
        val requestedIds = songIds.toSet()

        (requestedIds - currentIds).forEach { songId ->
            insertSongToPlaylist(PlaylistSongCrossRef(playlistId, songId))
        }
        (currentIds - requestedIds).forEach { songId ->
            removeSongFromPlaylist(playlistId, songId)
        }
    }

    @Query("SELECT * FROM playlist_song_cross_ref")
    fun getAllPlaylistSongsFlow(): Flow<List<PlaylistSongCrossRef>>

    @Query(
        """
        DELETE FROM playlist_song_cross_ref
        WHERE playlistId = :playlistId
          AND songId = :songId
        """
    )
    suspend fun removeSongFromPlaylist(
        playlistId: Long,
        songId: Long
    )

    @Query(
        "DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId"
    )
    suspend fun deletePlaylistRelations(playlistId: Long)

    // ELIMINAR PLAYLIST Y SUS RELACIONES
    // SIN DEJAR REFERENCIAS HUERFANAS

    @Transaction
    suspend fun deletePlaylistAndRelations(
        playlist: PlaylistEntity
    ) {
        deletePlaylistRelations(playlist.id)
        deletePlaylist(playlist)
    }
}
