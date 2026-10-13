package com.example.comarleyaetheraudio.presentation.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.domain.playback.*
import kotlinx.coroutines.CancellationException

@Composable
fun AudioInspectorDialog(song: Song, inspect: suspend (Song) -> AudioInspection, onDismiss: () -> Unit) {
    val loader by rememberUpdatedState(inspect)
    var result by remember(song.id, song.contentUri) { mutableStateOf<AudioInspection?>(null) }
    LaunchedEffect(song.id, song.contentUri) {
        result = try { loader(song) } catch (e: CancellationException) { throw e }
        catch (_: Exception) { AudioInspection(detail = "No se pudo completar la inspección. Vuelve a abrir el panel para reintentar.") }
    }
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text("Audio Inspector") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(song.title, style = MaterialTheme.typography.titleMedium)
                val info = result
                if (info == null) {
                    Text("Inspeccionando archivo…")
                } else {
                    Text("Archivo", style = MaterialTheme.typography.titleSmall)
                    Text("Códec: ${info.codecMime ?: "No disponible"}")
                    Text("Frecuencia: ${info.sampleRateHz?.let { "$it Hz" } ?: "No disponible"}")
                    Text("Canales: ${info.channels ?: "No disponible"}")
                    Text("Profundidad de bits: ${info.bitDepth?.let { "$it bits" } ?: "No disponible"}")
                    Text("Tasa de bits: ${info.bitrateKbps?.let { "$it kbps" } ?: "No disponible"}")
                    Text(when (info.compression) {
                        AudioCompression.LOSSLESS -> "Codificación sin pérdidas"
                        AudioCompression.LOSSY -> "Codificación con pérdidas"
                        AudioCompression.UNKNOWN -> "Codificación sin verificar"
                    })
                    Text(when (info.resolution) {
                        AudioResolution.HIGH_RESOLUTION -> "Alta resolución declarada en el archivo"
                        AudioResolution.STANDARD -> "No cumple el criterio de alta resolución de SoundFlow"
                        AudioResolution.UNVERIFIED -> "Resolución sin verificar"
                    })
                    Text(info.detail, style = MaterialTheme.typography.bodySmall)
                    Text("Criterio conservador: codificación sin pérdidas, más de 16 bits y más de 48 kHz. No certifica la calidad de la grabación.",
                        style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider()
                    Text("Reproducción y salida", style = MaterialTheme.typography.titleSmall)
                    Text("Decodificador activo: no medido\nSalida de Android / DAC: no medida\nBit-perfect: no verificado")
                    Text("El tamaño del archivo y sus metadatos no garantizan la salida. El mezclador, el volumen, los efectos y Bluetooth pueden modificarla.",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } })
}
