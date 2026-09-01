package com.example.comarleyaetheraudio.data.local.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.comarleyaetheraudio.data.local.entity.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FolderScanner(private val context: Context) {

    suspend fun scanFolderUri(folderUri: Uri, folderName: String): List<SongEntity> = withContext(Dispatchers.IO) {
        val songEntities = mutableListOf<SongEntity>()
        val rootDocument = DocumentFile.fromTreeUri(context, folderUri) ?: return@withContext emptyList()

        // Recorrido recursivo seguro para escanear carpetas y subcarpetas
        fun traverseDirectory(dir: DocumentFile) {
            val files = dir.listFiles()
            for (file in files) {
                if (file.isDirectory) {
                    traverseDirectory(file)
                } else if (file.isFile && isAudioFile(file.type, file.name)) {
                    val fileUri = file.uri
                    val metadata = extractMetadata(context, fileUri, file.name ?: "Desconocido")

                    songEntities.add(
                        SongEntity(
                            title = metadata.title,
                            artist = metadata.artist,
                            album = metadata.album,
                            genre = metadata.genre,
                            year = metadata.year,
                            trackNumber = metadata.trackNumber,
                            duration = metadata.duration,
                            contentUri = fileUri.toString(),
                            albumArtUri = fileUri.toString(), // ExoPlayer y CoverCacheManager extraen la portada directamente del Uri
                            size = file.length(),
                            mimeType = file.type ?: "audio/*",
                            bitrate = metadata.bitrate,
                            sampleRate = metadata.sampleRate,
                            isHiRes = metadata.isHiRes,
                            path = fileUri.toString(),
                            folderUri = folderUri.toString(),
                            folderName = folderName
                        )
                    )
                }
            }
        }

        traverseDirectory(rootDocument)

        // Filtro antipánico: elimina duplicados exactos por ruta antes de tocar la base de datos
        return@withContext songEntities.distinctBy { it.contentUri }
    }

    private fun isAudioFile(mimeType: String?, fileName: String?): Boolean {
        if (mimeType != null && mimeType.startsWith("audio/")) return true
        val name = fileName?.lowercase() ?: return false
        return name.endsWith(".mp3") || name.endsWith(".flac") || name.endsWith(".wav") ||
                name.endsWith(".m4a") || name.endsWith(".aac") || name.endsWith(".ogg")
    }

    private data class ParsedMetadata(
        val title: String,
        val artist: String,
        val album: String,
        val genre: String,
        val year: String,
        val trackNumber: String,
        val duration: Long,
        val bitrate: Int,
        val sampleRate: Int,
        val isHiRes: Boolean
    )

    private fun extractMetadata(context: Context, uri: Uri, fallbackName: String): ParsedMetadata {
        val retriever = MediaMetadataRetriever()
        var title = fallbackName.substringBeforeLast(".")
        var artist = "<Artista Desconocido>"
        var album = "<Álbum Desconocido>"
        var genre = "Desconocido"
        var year = ""
        var trackNumber = ""
        var duration = 0L
        var bitrate = 0
        var sampleRate = 0

        try {
            retriever.setDataSource(context, uri)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.let { if (it.isNotBlank()) title = it }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.let { if (it.isNotBlank()) artist = it }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)?.let { if (it.isNotBlank()) album = it }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)?.let { if (it.isNotBlank()) genre = it }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)?.let { if (it.isNotBlank()) year = it }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)?.let { if (it.isNotBlank()) trackNumber = it }

            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()?.let { duration = it }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull()?.let { bitrate = it / 1000 }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toIntOrNull()?.let { sampleRate = it }

        } catch (_: Exception) {
            // Manejo silencioso en archivos con etiquetas corruptas
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }

        val isLossless = uri.toString().contains(".flac", ignoreCase = true) || uri.toString().contains(".wav", ignoreCase = true)
        val isHiRes = isLossless || sampleRate >= 48000 || bitrate > 1411

        return ParsedMetadata(title, artist, album, genre, year, trackNumber, duration, bitrate, sampleRate, isHiRes)
    }
}