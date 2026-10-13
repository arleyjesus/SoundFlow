package com.example.comarleyaetheraudio.data.local.util

import android.content.Context
import android.media.AudioFormat
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import com.example.comarleyaetheraudio.domain.playback.AudioInspection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Local, on-demand metadata inspection. Does not decode, modify files, or access Room. */
class AndroidAudioInspector(context: Context) {
    private val appContext = context.applicationContext
    private val mutex = Mutex()

    suspend fun inspect(uri: Uri): AudioInspection = withContext(Dispatchers.IO) {
        mutex.withLock {
            currentCoroutineContext().ensureActive()
            if (uri.scheme != "content" && uri.scheme != "file") {
                return@withLock AudioInspection(detail = "Solo se inspeccionan archivos locales.")
            }
            try {
                val flac = appContext.contentResolver.openInputStream(uri)?.use(FlacStreamInfo::read)
                currentCoroutineContext().ensureActive()
                if (flac != null) return@withLock flac
                inspectTrack(uri)
            } catch (e: CancellationException) {
                throw e
            } catch (_: SecurityException) {
                AudioInspection(detail = "Sin permiso para leer el archivo. Restaura el acceso desde Carpetas.")
            } catch (_: Exception) {
                AudioInspection(detail = "No se pudo leer el formato. El archivo puede no estar disponible o no ser compatible con el inspector.")
            }
        }
    }

    private suspend fun inspectTrack(uri: Uri): AudioInspection {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(appContext, uri, null)
            for (track in 0 until extractor.trackCount) {
                currentCoroutineContext().ensureActive()
                val format = extractor.getTrackFormat(track)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                if (!mime.startsWith("audio/")) continue
                // PCM encoding of a compressed codec may describe decoder output, not source depth.
                val bits = if (mime == "audio/raw") when (format.positiveInt(MediaFormat.KEY_PCM_ENCODING)) {
                    AudioFormat.ENCODING_PCM_8BIT -> 8
                    AudioFormat.ENCODING_PCM_16BIT -> 16
                    AudioFormat.ENCODING_PCM_24BIT_PACKED -> 24
                    AudioFormat.ENCODING_PCM_32BIT -> 32
                    else -> null // Float precision is not equivalent to 32-bit integer source resolution.
                } else null
                return AudioInspection(mime, format.positiveInt(MediaFormat.KEY_SAMPLE_RATE),
                    format.positiveInt(MediaFormat.KEY_CHANNEL_COUNT), bits,
                    format.positiveInt(MediaFormat.KEY_BIT_RATE)?.div(1000)?.takeIf { it > 0 },
                    "Primera pista de audio informada por Android. Los campos ausentes permanecen sin verificar.")
            }
            return AudioInspection(detail = "Android no identificó una pista de audio compatible.")
        } finally {
            extractor.release()
        }
    }

    private fun MediaFormat.positiveInt(key: String): Int? =
        if (containsKey(key)) runCatching { getInteger(key).takeIf { it > 0 } }.getOrNull() else null
}
