package com.example.comarleyaetheraudio.domain.repository

import com.example.comarleyaetheraudio.domain.model.Song

/**
 * Contrato de interfaz para gestionar los datos de audio en la app.
 */
interface AudioRepository {
    /**
     * Consulta el almacenamiento local y devuelve la lista de canciones encontradas.
     */
    suspend fun getLocalSongs(): List<Song>
}