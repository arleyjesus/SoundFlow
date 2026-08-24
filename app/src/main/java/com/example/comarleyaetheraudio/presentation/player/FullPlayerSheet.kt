package com.example.comarleyaetheraudio.presentation.player

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.comarleyaetheraudio.data.local.LrcParser
import com.example.comarleyaetheraudio.data.remote.LrcLibService
import com.example.comarleyaetheraudio.domain.model.AudioPlayerState
import com.example.comarleyaetheraudio.domain.model.LyricLine
import com.example.comarleyaetheraudio.presentation.player.LyricsView
import com.example.comarleyaetheraudio.presentation.components.TagEditorDialog

// Paleta Oficial v2.0.0
// Paleta Oficial v2.0.0
val ElectricPurple = Color(0xFF8A2BE2)
val LightLavender = Color(0xFFB388FF)
val PastelPink = Color(0xFFF48FB1) // <--- ASEGÚRATE DE INCLUIR ESTA LÍNEA
val BrandGradient = Brush.linearGradient(listOf(ElectricPurple, LightLavender))
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerSheet(
    // Agrega estos tres parámetros al final de la firma de la función:
    currentTimerMinutes: Int = 0,
    onStartTimer: (Int, Boolean) -> Unit = { _, _ -> },
    onCancelTimer: () -> Unit = {},
    playerState: AudioPlayerState,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onToggleShuffle: () -> Unit,
    onEditTags: (String, String, String) -> Unit
) {

    var showTimerDialog by remember { mutableStateOf(false) }
    val song = playerState.currentSong ?: return
    var sliderPosition by remember { mutableStateOf<Float?>(null) }

    var showLyrics by remember { mutableStateOf(false) }
    var showTagEditor by remember { mutableStateOf(false) }

    var fetchedLyrics by remember(song) { mutableStateOf<List<LyricLine>?>(null) }
    var isLoadingLyrics by remember { mutableStateOf(false) }

    LaunchedEffect(showLyrics, song) {
        if (showLyrics && fetchedLyrics == null) {
            isLoadingLyrics = true
            val localLyrics = LrcParser.parseLrcForSong(song.path)
            if (localLyrics.isNotEmpty()) {
                fetchedLyrics = localLyrics
            } else {
                val cleanTitle = song.title.replace(Regex("(?i)\\(official.*?\\)|\\[official.*?\\]|\\.(mp3|flac|m4a)"), "").trim()
                val cleanArtist = if (song.artist.contains("Unknown", ignoreCase = true)) "" else song.artist.trim()
                val remoteLyrics = LrcLibService.fetchSyncedLyrics(cleanTitle, cleanArtist)
                fetchedLyrics = remoteLyrics ?: emptyList()
            }
            isLoadingLyrics = false
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background, // Fondo AMOLED
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // BARRA SUPERIOR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showTimerDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Timer",
                        tint = if (currentTimerMinutes > 0) LightLavender else Color.Gray
                    )
                }
                Row {
                    IconButton(onClick = { showTagEditor = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.LightGray)
                    }
                    IconButton(onClick = { showLyrics = !showLyrics }) {
                        Icon(
                            Icons.Default.FormatQuote,
                            contentDescription = "Letras",
                            tint = if (showLyrics) LightLavender else Color.LightGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val artworkData = playerState.artworkData
            val bitmap = remember(artworkData) {
                artworkData?.let { bytes -> BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
            }

            // CARÁTULA DINÁMICA CON EFECTO NEÓN
            // Si hay letras, la carátula ocupa el 50% del ancho. Si no, ocupa casi todo el ancho.
            val imageFraction = if (showLyrics) 0.5f else 1f
            val imageShape = if (showLyrics) CircleShape else RoundedCornerShape(24.dp)

            Box(
                modifier = Modifier
                    .fillMaxWidth(imageFraction)
                    .aspectRatio(1f)
                    .shadow(
                        elevation = if (showLyrics) 24.dp else 32.dp,
                        shape = imageShape,
                        spotColor = ElectricPurple, // Sombra de Neón Morada
                        ambientColor = LightLavender
                    )
                    .clip(imageShape)
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.DarkGray
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.padding(48.dp),
                            tint = LightLavender
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // INFO Y LETRAS
            if (showLyrics) {
                if (isLoadingLyrics) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ElectricPurple)
                    }
                } else if (!fetchedLyrics.isNullOrEmpty()) {
                    LyricsView(
                        lyrics = fetchedLyrics!!,
                        currentPositionMs = playerState.currentPosition,
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                } else {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("No se encontraron letras.", color = Color.Gray)
                    }
                }
            } else {
                // INFO DE CANCIÓN (Modo Normal)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.titleMedium,
                            color = LightLavender,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (isFavorite) PastelPink else Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            // SLIDER
            val currentPos = sliderPosition ?: playerState.currentPosition.toFloat()
            val totalDuration = playerState.duration.coerceAtLeast(1L).toFloat()

            Slider(
                value = currentPos,
                onValueChange = { sliderPosition = it },
                onValueChangeFinished = {
                    sliderPosition?.let { onSeekTo(it.toLong()) }
                    sliderPosition = null
                },
                valueRange = 0f..totalDuration,
                colors = SliderDefaults.colors(
                    thumbColor = LightLavender,
                    activeTrackColor = ElectricPurple,
                    inactiveTrackColor = Color.DarkGray
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(currentPos.toLong()), style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(formatTime(playerState.duration), style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CONTROLES DE REPRODUCCIÓN (Neón)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "Aleatorio",
                        tint = if (playerState.isShuffleEnabled) LightLavender else Color.Gray
                    )
                }
                IconButton(onClick = onPrevious) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", tint = Color.White, modifier = Modifier.size(40.dp))
                }

                // Botón Play/Pause con Gradiente
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(16.dp, CircleShape, spotColor = ElectricPurple)
                        .background(BrandGradient, CircleShape)
                        .clip(CircleShape)
                        .clickable { onTogglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                IconButton(onClick = onNext) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Siguiente", tint = Color.White, modifier = Modifier.size(40.dp))
                }
                IconButton(onClick = { /* Todo: Sleep Timer */ }) {
                    Icon(Icons.Default.Timer, contentDescription = "Timer", tint = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showTagEditor) {
            TagEditorDialog(
                song = song,
                onDismiss = { showTagEditor = false },
                onSave = { newTitle, newArtist, newAlbum ->
                    onEditTags(newTitle, newArtist, newAlbum)
                    showTagEditor = false
                }
            )
        }
    }


    if (showTimerDialog) {
        com.example.comarleyaetheraudio.presentation.components.SleepTimerDialog(
            currentTimerMinutes = currentTimerMinutes,
            onStartTimer = onStartTimer,
            onCancelTimer = onCancelTimer,
            onDismiss = { showTimerDialog = false }
        )
    }
}
private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return String.format("%02d:%02d", m, s)
}


