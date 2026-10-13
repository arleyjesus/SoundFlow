package com.example.comarleyaetheraudio.presentation.player

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.comarleyaetheraudio.ui.theme.SoundFlowIcons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
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
import com.example.comarleyaetheraudio.data.local.util.LrcParser
import com.example.comarleyaetheraudio.data.local.util.ArtworkDecoder
import com.example.comarleyaetheraudio.data.remote.LrcLibService
import com.example.comarleyaetheraudio.domain.model.AudioPlayerState
import com.example.comarleyaetheraudio.domain.model.LyricLine
import com.example.comarleyaetheraudio.domain.playback.AudioInspection
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.domain.playback.SleepTimerState
import com.example.comarleyaetheraudio.presentation.components.dialogs.SleepTimerDialog
import com.example.comarleyaetheraudio.presentation.components.dialogs.TagEditorDialog
import com.example.comarleyaetheraudio.ui.theme.DynamicThemeExtractor
import com.example.comarleyaetheraudio.ui.theme.LocalAppearance
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerSheet(
    isDarkMode: Boolean = false, // ⚡ AHORA RECIBE DIRECTAMENTE EL ESTADO
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
    onEditTags: (String, String, String) -> Unit,
    onRetry: () -> Unit = {},
    sleepTimerState: SleepTimerState = if (currentTimerMinutes > 0)
        SleepTimerState.CountingDown(currentTimerMinutes) else SleepTimerState.Off,
    inspectAudio: suspend (Song) -> AudioInspection = { AudioInspection() }
) {
    val song = playerState.currentSong ?: return
    var sliderPosition by remember(song.id) { mutableStateOf<Float?>(null) }

    var showLyrics by remember { mutableStateOf(false) }
    var showTagEditor by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }
    var showMenuOptions by remember { mutableStateOf(false) }
    var showFileInfo by remember(song.id) { mutableStateOf(false) }

    val onlineLyrics = com.example.comarleyaetheraudio.presentation.settings.LocalNetworkSettings.current.lyrics
    var fetchedLyrics by remember(song, onlineLyrics) { mutableStateOf<List<LyricLine>?>(null) }
    var isLoadingLyrics by remember { mutableStateOf(false) }

    LaunchedEffect(showLyrics, song, onlineLyrics) {
        if (showLyrics && fetchedLyrics == null) {
            isLoadingLyrics = true
            try {
                val localLyrics = withContext(Dispatchers.IO) { LrcParser.parseLrcForSong(song.path) }
                fetchedLyrics = if (localLyrics.isNotEmpty()) localLyrics else if (!onlineLyrics) emptyList() else {
                    val cleanTitle = song.title.replace(Regex("(?i)\\(official.*?\\)|\\[official.*?\\]|\\.(mp3|flac|m4a)"), "").trim()
                    val cleanArtist = if (song.artist.contains("Unknown", ignoreCase = true)) "" else song.artist.trim()
                    LrcLibService.fetchSyncedLyrics(cleanTitle, cleanArtist) ?: emptyList()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                fetchedLyrics = emptyList()
            } finally {
                isLoadingLyrics = false
            }
        }
    }

    val artworkData = playerState.artworkData
    val bitmapState = produceState<Bitmap?>(null, artworkData) {
        value = withContext(Dispatchers.Default) {
            artworkData?.let { bytes ->
                ArtworkDecoder.decode(bytes, 1024)
            }
        }
    }

    val bitmap = bitmapState.value
    val transparencies = LocalAppearance.current.transparenciesEnabled
    val dominantColor = MaterialTheme.colorScheme.primary
    val artworkAccent by produceState(dominantColor, bitmap, dominantColor) {
        value = withContext(Dispatchers.Default) {
            DynamicThemeExtractor.extractDominantColor(bitmap, dominantColor)
        }
    }
    val backdropColor = if (transparencies) artworkAccent else dominantColor
    val playContentColor = com.example.comarleyaetheraudio.ui.theme.contrastingContent(artworkAccent)

    val baseBgColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onSurface
    val contentScroll = rememberScrollState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = baseBgColor,
        dragHandle = null,
        shape = RectangleShape,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(baseBgColor)
        ) {
            if (transparencies && bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(40.dp)
                        .background(baseBgColor.copy(alpha = if (isDarkMode) 0.50f else 0.88f)),
                    contentScale = ContentScale.Crop,
                    alpha = if (isDarkMode) 0.35f else 0.10f
                )
            }

            if (transparencies) Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                backdropColor.copy(alpha = if (isDarkMode) 0.35f else 0.08f),
                                baseBgColor.copy(alpha = 0.90f),
                                baseBgColor
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                    .then(if (showLyrics) Modifier else Modifier.verticalScroll(contentScroll))
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                SoundFlowIcons.Back,
                                contentDescription = "Volver",
                                tint = textColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text("Ahora suena", style = MaterialTheme.typography.titleMedium, color = textColor,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp), textAlign = TextAlign.Center)
                    Box {
                        Surface(
                            onClick = { showMenuOptions = true },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    SoundFlowIcons.More,
                                    contentDescription = "Opciones",
                                    tint = textColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showMenuOptions,
                            onDismissRequest = { showMenuOptions = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Información del archivo") },
                                leadingIcon = { Icon(SoundFlowIcons.Info, contentDescription = null) },
                                onClick = { showMenuOptions = false; showFileInfo = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Editar información") },
                                leadingIcon = { Icon(SoundFlowIcons.Edit, contentDescription = null) },
                                onClick = {
                                    showMenuOptions = false
                                    showTagEditor = true
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                PlaybackStatusBanner(playerState, onRetry)

                if (showLyrics) {
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
                                Icon(SoundFlowIcons.Music, contentDescription = null, modifier = Modifier.padding(24.dp))
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
                            Text(if (onlineLyrics) "No se encontraron letras." else "No hay letras locales. La búsqueda en línea está desactivada en Ajustes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 360.dp)
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
                                Icon(SoundFlowIcons.Music, contentDescription = null, modifier = Modifier.padding(52.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

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
                            color = textColor,
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
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) SoundFlowIcons.FavoriteSelected else SoundFlowIcons.Favorite,
                            contentDescription = if (isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                            tint = if (isFavorite) dominantColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { showTimerDialog = true },
                            enabled = !playerState.isConnecting && playerState.errorMessage == null,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                SoundFlowIcons.Timer,
                                contentDescription = "Temporizador",
                                tint = if (sleepTimerState.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { showLyrics = !showLyrics },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                SoundFlowIcons.Lyrics,
                                contentDescription = "Letras",
                                tint = if (showLyrics) dominantColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    AudioVisualizerView(
                        isPlaying = playerState.isPlaying,
                        primaryColor = artworkAccent,
                        secondaryColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .padding(horizontal = 4.dp)
                    )

                    val currentPos = (sliderPosition ?: playerState.currentPosition.toFloat())
                        .coerceIn(0f, playerState.duration.coerceAtLeast(1L).toFloat())
                    val totalDuration = playerState.duration.coerceAtLeast(1L).toFloat()

                    Slider(
                        enabled = !playerState.isConnecting && playerState.duration > 0,
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
                            .heightIn(min = 48.dp)
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

                Row(
                    modifier = Modifier.fillMaxWidth(0.75f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onToggleShuffle, enabled = !playerState.isConnecting) {
                        Icon(
                            SoundFlowIcons.Shuffle,
                            contentDescription = "Aleatorio",
                            tint = if (playerState.isShuffleEnabled) dominantColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onRewind, enabled = !playerState.isConnecting) {
                        Icon(
                            SoundFlowIcons.Rewind,
                            contentDescription = "Retroceder 10 segundos",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onForward, enabled = !playerState.isConnecting) {
                        Icon(SoundFlowIcons.Forward, contentDescription = "Adelantar 10 segundos",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPrevious, enabled = !playerState.isConnecting) {
                        Icon(SoundFlowIcons.Previous, contentDescription = "Anterior", tint = textColor, modifier = Modifier.size(38.dp))
                    }

                    Surface(
                        onClick = onTogglePlayPause,
                        enabled = !playerState.isConnecting,
                        modifier = Modifier
                            .size(68.dp)
                            .shadow(14.dp, CircleShape, spotColor = artworkAccent),
                        shape = CircleShape,
                        color = artworkAccent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (playerState.isPlaying) SoundFlowIcons.Pause else SoundFlowIcons.Play,
                                contentDescription = if (playerState.isPlaying) "Pausar" else "Reproducir",
                                modifier = Modifier.size(34.dp),
                                tint = playContentColor
                            )
                        }
                    }

                    IconButton(onClick = onNext, enabled = !playerState.isConnecting) {
                        Icon(SoundFlowIcons.Next, contentDescription = "Siguiente", tint = textColor, modifier = Modifier.size(38.dp))
                    }
                }
            }
        }

        if (showFileInfo) {
            AudioInspectorDialog(song, inspectAudio, onDismiss = { showFileInfo = false })
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
                sleepTimerState = sleepTimerState,
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
    return String.format(Locale.ROOT, "%02d:%02d", m, s)
}
