package com.example.comarleyaetheraudio.presentation.player

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.example.comarleyaetheraudio.data.local.LrcParser
import com.example.comarleyaetheraudio.data.remote.LrcLibService
import com.example.comarleyaetheraudio.domain.model.AudioPlayerState
import com.example.comarleyaetheraudio.domain.model.LyricLine
import com.example.comarleyaetheraudio.presentation.components.SleepTimerDialog
import com.example.comarleyaetheraudio.presentation.components.TagEditorDialog
import com.example.comarleyaetheraudio.ui.theme.DynamicThemeExtractor
import com.example.comarleyaetheraudio.ui.theme.ElectricPurple
import com.example.comarleyaetheraudio.ui.theme.LightLavender

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerSheet(
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
    val song = playerState.currentSong ?: return
    var sliderPosition by remember { mutableStateOf<Float?>(null) }

    var showLyrics by remember { mutableStateOf(false) }
    var showTagEditor by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }

    // RECUPERADO: Estados para letras de red
    var fetchedLyrics by remember(song) { mutableStateOf<List<LyricLine>?>(null) }
    var isLoadingLyrics by remember { mutableStateOf(false) }

    // RECUPERADO: Efecto que busca letras en la red usando LRCLIB
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

    val artworkData = playerState.artworkData
    val bitmap = remember(artworkData) {
        artworkData?.let { bytes -> BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
    }

    val dominantColor = remember(bitmap) {
        DynamicThemeExtractor.extractDominantColor(bitmap, ElectricPurple)
    }

    // CORREGIDO: Detección del tema del sistema para el degradado
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color.Black else MaterialTheme.colorScheme.background
    val midColor = if (isDark) Color(0xFF0D0D0D) else MaterialTheme.colorScheme.surfaceVariant

    val dynamicGradient = remember(dominantColor, isDark) {
        Brush.verticalGradient(
            colors = listOf(
                dominantColor.copy(alpha = if (isDark) 0.45f else 0.2f),
                midColor,
                bgColor
            )
        )
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = bgColor, // Adaptable a Claro/Oscuro
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(dynamicGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // BARRA SUPERIOR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ExpandMore, contentDescription = "Minimizar", modifier = Modifier.size(32.dp))
                    }
                    Row {
                        IconButton(onClick = { showTimerDialog = true }) {
                            Icon(Icons.Default.Timer, contentDescription = "Timer", tint = if (currentTimerMinutes > 0) dominantColor else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { showTagEditor = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { showLyrics = !showLyrics }) {
                            Icon(Icons.Default.FormatQuote, contentDescription = "Letras", tint = if (showLyrics) dominantColor else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ZONA CENTRAL: Carátula o Visor de Letras RECUPERADO
                if (showLyrics) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(), contentDescription = null,
                                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(song.title, fontWeight = FontWeight.Bold, maxLines = 1, style = MaterialTheme.typography.titleMedium)
                            Text(song.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (isLoadingLyrics) {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = dominantColor)
                        }
                    } else if (!fetchedLyrics.isNullOrEmpty()) {
                        LyricsView(
                            lyrics = fetchedLyrics!!,
                            currentPositionMs = playerState.currentPosition,
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            Text("No se encontraron letras.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Carátula",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = dominantColor)
                                .clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(24.dp)),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.padding(72.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // VISUALIZADOR DE ESPECTRO
                    AudioVisualizerView(
                        isPlaying = playerState.isPlaying,
                        primaryColor = dominantColor,
                        secondaryColor = LightLavender,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(song.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(song.artist, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (isFavorite) dominantColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                // SLIDER DE TIEMPO
                val currentPos = sliderPosition ?: playerState.currentPosition.toFloat()
                val totalDuration = playerState.duration.coerceAtLeast(1L).toFloat()

                Slider(
                    value = currentPos,
                    onValueChange = { sliderPosition = it },
                    onValueChangeFinished = { sliderPosition?.let { onSeekTo(it.toLong()) }; sliderPosition = null },
                    valueRange = 0f..totalDuration,
                    colors = SliderDefaults.colors(thumbColor = dominantColor, activeTrackColor = dominantColor),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = formatTime(currentPos.toLong()), style = MaterialTheme.typography.labelMedium)
                    Text(text = formatTime(playerState.duration), style = MaterialTheme.typography.labelMedium)
                }
                Spacer(modifier = Modifier.height(12.dp))

                // CONTROLES DE REPRODUCCIÓN SECUNDARIOS
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleShuffle) {
                        Icon(Icons.Default.Shuffle, contentDescription = "Aleatorio", tint = if (playerState.isShuffleEnabled) dominantColor else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onRewind) { Icon(Icons.Default.Replay10, contentDescription = "-10s") }
                    IconButton(onClick = onForward) { Icon(Icons.Default.Forward10, contentDescription = "+10s") }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // CONTROLES DE REPRODUCCIÓN PRINCIPALES
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrevious) { Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", modifier = Modifier.size(48.dp)) }
                    FilledIconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier.size(76.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = dominantColor)
                    ) {
                        Icon(if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.White)
                    }
                    IconButton(onClick = onNext) { Icon(Icons.Default.SkipNext, contentDescription = "Siguiente", modifier = Modifier.size(48.dp)) }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (showTagEditor) {
            TagEditorDialog(song = song, onDismiss = { showTagEditor = false }, onSave = { newTitle, newArtist, newAlbum -> onEditTags(newTitle, newArtist, newAlbum); showTagEditor = false })
        }
        if (showTimerDialog) {
            SleepTimerDialog(currentTimerMinutes = currentTimerMinutes, onStartTimer = onStartTimer, onCancelTimer = onCancelTimer, onDismiss = { showTimerDialog = false })
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return String.format("%02d:%02d", m, s)
}