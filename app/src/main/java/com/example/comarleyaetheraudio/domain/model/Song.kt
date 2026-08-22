package com.example.comarleyaetheraudio.domain.model

import android.net.Uri

/**
 * Modelo de datos puro que representa una pista de audio dentro del dominio de la app.
 */
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,            // Duración en milisegundos
    val contentUri: Uri,           // Ruta URI nativa para reproducir el archivo
    val albumArtUri: Uri?,         // Ruta de la carátula del álbum (puede ser nula)
    val size: Long,                // Tamaño del archivo en bytes
    val mimeType: String,          // Ej: "audio/flac", "audio/mpeg", "audio/wav"
    val bitrate: Int = 0,          // Tasa de bits en kbps (ej: 1411 kbps, 9216 kbps)
    val sampleRate: Int = 0,       // Frecuencia de muestreo en Hz (ej: 44100 Hz, 96000 Hz, 192000 Hz)
    val isHiRes: Boolean = false   // Flag para identificar audio de alta fidelidad sin pérdida
)