package com.example.comarleyaetheraudio.domain.model

import android.net.Uri

/**
 * Representa una pista de audio en la biblioteca (v1.0).
 * Expandido para soportar organización por carpetas y metadatos completos.
 */
data class Song(
    val id: Long,                 // Identificador único (ahora será generado por nuestra BD local)
    val title: String,
    val artist: String,
    val album: String,
    val genre: String = "Desconocido", // Nuevo
    val year: String = "",             // Nuevo
    val trackNumber: String = "",      // Nuevo
    val duration: Long,
    val contentUri: Uri,
    val albumArtUri: Uri?,
    val size: Long,
    val mimeType: String,
    val bitrate: Int = 0,
    val sampleRate: Int = 0,
    val isHiRes: Boolean = false,
    val path: String = "",              // Nuevo: Ruta absoluta del archivo (necesaria para letras .lrc)
    val folderPath: String = "",       // Nuevo: Para agrupar las canciones por la carpeta que eligió el usuario
    val folderUri: String = ""         // Nuevo: El identificador de la carpeta padre
)