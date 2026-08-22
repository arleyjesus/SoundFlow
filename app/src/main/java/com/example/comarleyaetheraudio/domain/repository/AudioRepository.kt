package com.example.comarleyaetheraudio.domain.repository

import android.net.Uri
import com.example.comarleyaetheraudio.data.local.entity.FolderEntity
import com.example.comarleyaetheraudio.domain.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * Contrato actualizado de la capa Domain para gestionar canciones y carpetas locales con Room.
 */
interface AudioRepository {

    /**
     * Flujo reactivo en tiempo real con todas las canciones guardadas en la BD local.
     */
    fun getSongsFlow(): Flow<List<Song>>

    /**
     * Flujo reactivo con todas las carpetas seleccionadas por el usuario.
     */
    fun getFoldersFlow(): Flow<List<FolderEntity>>

    /**
     * Guarda los permisos de una nueva carpeta seleccionada via SAF y la analiza.
     */
    suspend fun addAndScanFolder(folderUri: Uri)

    /**
     * Elimina una carpeta de la BD local y remueve sus canciones asociadas.
     */
    suspend fun removeFolder(folderUri: String)

    suspend fun toggleFavorite(songId: Long, isFavorite: Boolean)

    fun getFavoriteSongIds(): Flow<List<Long>>
}