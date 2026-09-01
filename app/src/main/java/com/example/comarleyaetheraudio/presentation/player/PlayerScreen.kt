package com.example.comarleyaetheraudio.presentation.player

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.comarleyaetheraudio.domain.model.AudioPlayerState
import kotlin.math.abs

@Composable
fun PlayerScreen(
    playerState: AudioPlayerState,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val activity = context as? Activity

    var totalDragX by remember { mutableStateOf(0f) }
    var totalDragY by remember { mutableStateOf(0f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // CARÁTULA CON DETECCIÓN DE GESTOS TÁCTILES
        Box(
            modifier = Modifier
                .size(320.dp)
                .clip(RoundedCornerShape(24.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            totalDragX = 0f
                            totalDragY = 0f
                        },
                        onDragEnd = {
                            // Gesto Horizontal: Cambio de pista
                            if (abs(totalDragX) > abs(totalDragY) && abs(totalDragX) > 100f) {
                                if (totalDragX < 0) {
                                    onPlayNext()
                                } else {
                                    onPlayPrevious()
                                }
                            }
                            totalDragX = 0f
                            totalDragY = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            totalDragX += dragAmount.x
                            totalDragY += dragAmount.y

                            // Gesto Vertical: Brillo (Izquierda) / Volumen (Derecha)
                            if (abs(totalDragY) > abs(totalDragX)) {
                                val isLeftSide = change.position.x < (size.width / 2)
                                val sensitivity = 0.005f

                                if (isLeftSide) {
                                    activity?.window?.attributes?.let { layoutParams ->
                                        val currentBrightness = if (layoutParams.screenBrightness < 0) 0.5f else layoutParams.screenBrightness
                                        val newBrightness = (currentBrightness - (dragAmount.y * sensitivity)).coerceIn(0.01f, 1.0f)
                                        layoutParams.screenBrightness = newBrightness
                                        activity.window.attributes = layoutParams
                                    }
                                } else {
                                    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                    val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                    val delta = if (dragAmount.y < 0) 1 else -1

                                    if (abs(totalDragY) > 40f) {
                                        val targetVolume = (currentVolume + delta).coerceIn(0, maxVolume)
                                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, AudioManager.FLAG_SHOW_UI)
                                        totalDragY = 0f
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(playerState.artworkData)
                    .crossfade(true)
                    .build(),
                contentDescription = "Cover",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}