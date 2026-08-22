package com.example.comarleyaetheraudio.data.local

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import com.example.comarleyaetheraudio.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Encargado de consultar la base de datos de MediaStore de Android
 * para extraer las pistas de audio locales y sus metadatos técnicos.
 */
class MediaStoreAudioScanner(private val context: Context) {

    suspend fun scanAudioFiles(): List<Song> = withContext(Dispatchers.IO) {
        val songList = mutableListOf<Song>()

        // Colección de almacenamiento de audio público
        val collection: Uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        // Columnas que le pediremos a MediaStore
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE
        )

        // Filtro: Solo archivos que la plataforma reconoce como música y mayores a 30 segundos
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 30000"

        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Desconocido"
                val artist = cursor.getString(artistColumn) ?: "Artista Desconocido"
                val album = cursor.getString(albumColumn) ?: "Álbum Desconocido"
                val duration = cursor.getLong(durationColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val size = cursor.getLong(sizeColumn)
                val mimeType = cursor.getString(mimeTypeColumn) ?: ""

                // URI para reproducir el archivo directamente
                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                // URI de la carátula del álbum
                val albumArtUri: Uri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                )

                // Extraemos Bitrate y SampleRate para evaluar calidad Hi-Res
                val (bitrate, sampleRate) = extractAudioMetadata(context, contentUri)

                // Criterio Hi-Res: Formato lossless (FLAC/WAV) o Frecuencia de Muestreo > 48kHz o Bitrate > 1411 kbps
                val isLosslessFormat = mimeType.contains("flac", ignoreCase = true) || mimeType.contains("wav", ignoreCase = true)
                val isHiResQuality = isLosslessFormat || sampleRate >= 48000 || bitrate > 1411

                songList.add(
                    Song(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        contentUri = contentUri,
                        albumArtUri = albumArtUri,
                        size = size,
                        mimeType = mimeType,
                        bitrate = bitrate,
                        sampleRate = sampleRate,
                        isHiRes = isHiResQuality
                    )
                )
            }
        }

        return@withContext songList
    }

    /**
     * Utiliza MediaMetadataRetriever para inspeccionar los parámetros del stream de audio.
     */
    private fun extractAudioMetadata(context: Context, uri: Uri): Pair<Int, Int> {
        var bitrate = 0
        var sampleRate = 0
        val retriever = MediaMetadataRetriever()

        try {
            retriever.setDataSource(context, uri)
            val bitrateStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
            val sampleRateStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)

            if (!bitrateStr.isNullOrEmpty()) {
                bitrate = bitrateStr.toInt() / 1000 // Convertir a kbps
            }
            if (!sampleRateStr.isNullOrEmpty()) {
                sampleRate = sampleRateStr.toInt()
            }
        } catch (_: Exception) {
            // Si el archivo está corrupto o no permite inspección, fallback seguro
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }

        return Pair(bitrate, sampleRate)
    }
}