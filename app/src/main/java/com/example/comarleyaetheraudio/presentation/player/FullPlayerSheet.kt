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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var showMenuOptions by remember { mutableStateOf(false) }

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

    val artworkData = playerState.artworkData
    val bitmap = remember(artworkData) {
        artworkData?.let { bytes -> BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
    }

    val dominantColor = remember(bitmap) {
        DynamicThemeExtractor.extractDominantColor(bitmap, ElectricPurple)
    }

    val isDark = isSystemInDarkTheme()
    val baseBgColor = if (isDark) Color.Black else MaterialTheme.colorScheme.background

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = baseBgColor,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. CARÁTULA EN SEGUNDO PLANO (EFECTO ATMÓSFERA RESTAURADO)
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(35.dp)
                        .background(baseBgColor.copy(alpha = if (isDark) 0.45f else 0.2f)),
                    contentScale = ContentScale.Crop,
                    alpha = if (isDark) 0.35f else 0.25f
                )
            }

            // GRADIENTE ADAPTATIVO
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                dominantColor.copy(alpha = if (isDark) 0.35f else 0.15f),
                                baseBgColor.copy(alpha = 0.85f),
                                baseBgColor
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // BARRA SUPERIOR (Boton Volver + Opciones)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", modifier = Modifier.size(18.dp))
                        }
                    }

                    Box {
                        Surface(
                            onClick = { showMenuOptions = true },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.MoreHoriz, contentDescription = "Opciones", modifier = Modifier.size(20.dp))
                            }
                        }

                        DropdownMenu(
                            expanded = showMenuOptions,
                            onDismissRequest = { showMenuOptions = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Editar información") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    showMenuOptions = false
                                    showTagEditor = true
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. MODO LETRAS / MODO REPRODUCTOR NORMAL
                if (showLyrics) {
                    // CARÁTULA REDONDA LIGERAMENTE CONTENIDA EN MODO LETRAS
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.48f)
                            .aspectRatio(1f)
                            .shadow(16.dp, CircleShape, spotColor = dominantColor)
                            .clip(CircleShape)
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
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.padding(24.dp))
                            }
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
                    // CARÁTULA PRINCIPAL MÁS GRANDE EN MODO NORMAL
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.82f)
                            .aspectRatio(1f)
                            .shadow(20.dp, RoundedCornerShape(24.dp), spotColor = dominantColor)
                            .clip(RoundedCornerShape(24.dp))
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Carátula",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.padding(52.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3. TÍTULO Y ARTISTA LIMPIOS (SIN BURBUJA DE FONDO Y TIPOGRAFÍA AUMENTADA)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                }

                // 4. BARRAS SECUNDARIAS (FAVORITOS AL LADO IZQUIERDO Y LETRAS/TIMER A LA DERECHA)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (isFavorite) dominantColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { showTimerDialog = true },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = "Timer",
                                tint = if (currentTimerMinutes > 0) dominantColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { showLyrics = !showLyrics },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Default.FormatQuote,
                                contentDescription = "Letras",
                                tint = if (showLyrics) dominantColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // 5. LÍNEA DE TIEMPO DELGADA Y ESPECTRO AUDIO-REACTIVO
                Column(modifier = Modifier.fillMaxWidth()) {
                    AudioVisualizerView(
                        isPlaying = playerState.isPlaying,
                        primaryColor = dominantColor,
                        secondaryColor = LightLavender,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .padding(horizontal = 4.dp)
                    )

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
                            thumbColor = dominantColor,
                            activeTrackColor = dominantColor,
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 4.dp, top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = formatTime(currentPos.toLong()), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = formatTime(playerState.duration), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 6. FILA DE REPETICIÓN Y ALEATORIO (ELEVADOS)
                Row(
                    modifier = Modifier.fillMaxWidth(0.55f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onToggleShuffle) {
                        Icon(
                            Icons.Default.Shuffle,
                            contentDescription = "Aleatorio",
                            tint = if (playerState.isShuffleEnabled) dominantColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onRewind) {
                        Icon(
                            Icons.Default.Repeat,
                            contentDescription = "Repetir",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 7. CONTROLES PRINCIPALES SUBIDOS Y ESTILIZADOS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPrevious) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", modifier = Modifier.size(38.dp))
                    }

                    Surface(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(68.dp)
                            .shadow(14.dp, CircleShape, spotColor = dominantColor),
                        shape = CircleShape,
                        color = dominantColor
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(34.dp),
                                tint = Color.White
                            )
                        }
                    }

                    IconButton(onClick = onNext) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Siguiente", modifier = Modifier.size(38.dp))
                    }
                }
            }
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
        if (showTimerDialog) {
            SleepTimerDialog(
                currentTimerMinutes = currentTimerMinutes,
                onStartTimer = onStartTimer,
                onCancelTimer = onCancelTimer,
                onDismiss = { showTimerDialog = false }
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return String.format("%02d:%02d", m, s)
}