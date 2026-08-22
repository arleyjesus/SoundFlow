package com.example.comarleyaetheraudio.domain.usecase

import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.domain.repository.AudioRepository
import kotlinx.coroutines.flow.Flow

/**
 * Caso de uso: Obtener todos los archivos de audio locales.
 * Su única responsabilidad es pedirle al repositorio la música y entregársela a la UI.
 */
class GetLocalAudioFilesUseCase(
    private val repository: AudioRepository
) {
    operator fun invoke(): Flow<List<Song>> {
        return repository.getSongsFlow()
    }
}