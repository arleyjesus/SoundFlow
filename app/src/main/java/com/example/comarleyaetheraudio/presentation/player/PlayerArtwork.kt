package com.example.comarleyaetheraudio.presentation.player

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.comarleyaetheraudio.ui.theme.DynamicThemeExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Carátula ya decodificada: versión grande (UI), miniatura (fondo borroso) y color dominante crudo. */
@Immutable
data class PlayerArtwork(
    val art: ImageBitmap,
    val background: ImageBitmap,
    val dominant: Color
)

/**
 * Decodifica fuera del hilo principal y con submuestreo (antes: bitmap a tamaño completo en composición).
 * Palette se ejecuta sobre la miniatura → mucho más rápido.
 */
@Composable
fun rememberPlayerArtwork(artworkData: ByteArray?, fallbackDominant: Color): State<PlayerArtwork?> =
    produceState<PlayerArtwork?>(initialValue = null, artworkData, fallbackDominant) {
        value = if (artworkData == null) null else withContext(Dispatchers.Default) {
            val art = decodeSampled(artworkData, 1024)
            val small = decodeSampled(artworkData, 96)
            if (art == null || small == null) null
            else PlayerArtwork(
                art = art.asImageBitmap(),
                background = small.asImageBitmap(),
                dominant = DynamicThemeExtractor.extractDominantColor(small, fallbackDominant)
            )
        }
    }

private fun decodeSampled(bytes: ByteArray, targetSize: Int): Bitmap? =
    com.example.comarleyaetheraudio.data.local.util.ArtworkDecoder.decode(bytes, targetSize)
