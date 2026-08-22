package com.example.comarleyaetheraudio.data.repository

import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.data.local.MediaStoreAudioScanner
import com.example.comarleyaetheraudio.domain.repository.AudioRepository

/**
 * Implementación concreta del repositorio.
 * Conecta el contrato de dominio con la fuente de datos real (MediaStore).
 */
class AudioRepositoryImpl(
    private val scanner: MediaStoreAudioScanner
) : AudioRepository {

    override suspend fun getLocalSongs(): List<Song> {
        // En el futuro, aquí podríamos agregar lógica para guardar en una base de datos local (Room)
        // Por ahora, leemos directamente del almacenamiento cada vez.
        return scanner.scanAudioFiles()
    }
}