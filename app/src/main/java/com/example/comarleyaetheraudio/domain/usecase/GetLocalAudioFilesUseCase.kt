package com.example.comarleyaetheraudio.domain.usecase

import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.domain.repository.AudioRepository

/**
 * Caso de uso: Obtener todos los archivos de audio locales.
 * Su única responsabilidad es pedirle al repositorio la música y entregársela a la UI.
 */
class GetLocalAudioFilesUseCase(
    private val repository: AudioRepository
) {
    suspend operator fun invoke(): List<Song> {
        val songs = repository.getLocalSongs()

        // Aquí podríamos aplicar lógica de negocio, por ejemplo:
        // Filtrar archivos corruptos o priorizar las canciones Hi-Res al principio de la lista.
        return songs
    }
}