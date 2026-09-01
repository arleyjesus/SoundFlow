package com.example.comarleyaetheraudio.presentation.playlist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.comarleyaetheraudio.data.local.util.CoverCacheManager
import com.example.comarleyaetheraudio.domain.model.AudioPlayerState
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import java.io.File

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    allSongs: List<Song> = emptyList(),
    playerState: AudioPlayerState,
    favoriteIds: List<Long>,
    isDarkMode: Boolean = false,
    onBackClick: () -> Unit,
    onAddSongsConfirmed: (List<Long>) -> Unit,
    onPlayAllClick: (Boolean) -> Unit,
    onSongClick: (Song) -> Unit,
    onRemoveSongClick: (Song) -> Unit
) {
    BackHandler {
        onBackClick()
    }

    val context = LocalContext.current
    var expandedMenuSongId by remember { mutableStateOf<Long?>(null) }
    var showAddSongsDialog by remember { mutableStateOf(false) }

    // ⚡ Estado de ordenamiento para las canciones de la playlist
    var sortDescending by remember { mutableStateOf(false) }

    val sortedPlaylistSongs = remember(playlist.songs, sortDescending) {
        if (sortDescending) playlist.songs.sortedByDescending { it.id }
        else playlist.songs
    }

    val backgroundColor = if (isDarkMode) Color(0xFF09090B) else Color(0xFFFAFAFC)
    val cardBgColor = if (isDarkMode) Color(0xFF141417) else Color(0xFFF1F1F5)
    val textColor = if (isDarkMode) Color.White else Color(0xFF111111)

    val isShuffleActive = playerState.isShuffleEnabled

    val totalStatsText by remember(playlist.songs) {
        derivedStateOf {
            val totalMs = playlist.songs.sumOf { it.duration }
            val hours = totalMs / (1000 * 60 * 60)
            val minutes = (totalMs % (1000 * 60 * 60)) / (1000 * 60)
            if (hours > 0) "${playlist.songs.size} CANCIONES • ${hours}H ${minutes}M"
            else "${playlist.songs.size} CANCIONES • ${minutes}M"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                ) {
                    val sampleSongs = remember(playlist.songs) { playlist.songs.take(4) }

                    if (sampleSongs.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(24.dp))
                        ) {
                            if (sampleSongs.size == 1) {
                                HeaderMosaicTile(song = sampleSongs[0], modifier = Modifier.fillMaxSize())
                            } else {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Row(modifier = Modifier.weight(1f)) {
                                        HeaderMosaicTile(song = sampleSongs[0], modifier = Modifier.weight(1f).fillMaxHeight())
                                        if (sampleSongs.size > 1) {
                                            Spacer(modifier = Modifier.width(2.dp))
                                            HeaderMosaicTile(song = sampleSongs[1], modifier = Modifier.weight(1f).fillMaxHeight())
                                        }
                                    }
                                    if (sampleSongs.size > 2) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(modifier = Modifier.weight(1f)) {
                                            HeaderMosaicTile(song = sampleSongs[2], modifier = Modifier.weight(1f).fillMaxHeight())
                                            if (sampleSongs.size > 3) {
                                                Spacer(modifier = Modifier.width(2.dp))
                                                HeaderMosaicTile(song = sampleSongs[3], modifier = Modifier.weight(1f).fillMaxHeight())
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data("https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=1000&auto=format&fit=crop")
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0.0f to Color.Transparent,
                                        0.6f to Color.Transparent,
                                        0.85f to backgroundColor.copy(alpha = 0.60f),
                                        1.0f to backgroundColor
                                    )
                                )
                            )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = playlist.name.uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp
                        ),
                        color = textColor,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = totalStatsText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ⚡ Botón de ordenamiento en la playlist
                    TextButton(onClick = { sortDescending = !sortDescending }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (sortDescending) "Más Recientes Primero" else "Orden Original",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onPlayAllClick(true) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isShuffleActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.20f) else (if (isDarkMode) Color(0xFF1E1E22) else Color(0xFFE8E8ED)),
                                contentColor = if (isShuffleActive) MaterialTheme.colorScheme.primary else textColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Shuffle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (isShuffleActive) MaterialTheme.colorScheme.primary else textColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SHUFFLE", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onPlayAllClick(false) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .shadow(
                                    elevation = 6.dp,
                                    shape = RoundedCornerShape(12.dp),
                                    ambientColor = Color(0xFFBB86FC),
                                    spotColor = Color(0xFFBB86FC)
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDarkMode) Color.White else MaterialTheme.colorScheme.primary,
                                contentColor = if (isDarkMode) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PLAY", fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // ⚡ Usando la lista ordenada
            items(sortedPlaylistSongs, key = { it.id }) { song ->
                val isCurrentPlaying = playerState.currentSong?.id == song.id
                val isFavorite = favoriteIds.contains(song.id)

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isCurrentPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else cardBgColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                        .clickable { onSongClick(song) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PlaylistSongCover(song = song, isCurrentPlaying = isCurrentPlaying)

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrentPlaying) MaterialTheme.colorScheme.primary else textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = song.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (isFavorite) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp).padding(end = 6.dp))
                        }

                        Box {
                            IconButton(onClick = { expandedMenuSongId = song.id }) {
                                Icon(Icons.Default.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            DropdownMenu(
                                expanded = expandedMenuSongId == song.id,
                                onDismissRequest = { expandedMenuSongId = null }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Eliminar de la playlist") },
                                    onClick = {
                                        onRemoveSongClick(song)
                                        expandedMenuSongId = null
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(backgroundColor.copy(alpha = 0.7f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = textColor)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { showAddSongsDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(backgroundColor.copy(alpha = 0.7f))
                ) {
                    Icon(Icons.Default.Collections, contentDescription = "Modificar Fotos", tint = MaterialTheme.colorScheme.primary)
                }

                IconButton(
                    onClick = { showAddSongsDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(backgroundColor.copy(alpha = 0.7f))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir más", tint = textColor)
                }
            }
        }

        if (showAddSongsDialog) {
            val existingIds = playlist.songs.map { it.id }
            AddSongsToPlaylistDialog(
                availableSongs = allSongs,
                alreadyAddedSongIds = existingIds,
                onDismiss = { showAddSongsDialog = false },
                onAddSongsConfirmed = { selectedIds ->
                    onAddSongsConfirmed(selectedIds)
                    showAddSongsDialog = false
                }
            )
        }
    }
}

@Composable
private fun HeaderMosaicTile(
    song: Song,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var cachedCoverFile by remember(song.id) { mutableStateOf<File?>(null) }

    LaunchedEffect(song.id) {
        cachedCoverFile = CoverCacheManager.getOrFetchCover(
            context = context,
            songId = song.id,
            uri = song.albumArtUri,
            path = song.path
        )
    }

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(cachedCoverFile ?: song.albumArtUri)
            .crossfade(true)
            .build(),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun PlaylistSongCover(
    song: Song,
    isCurrentPlaying: Boolean
) {
    val context = LocalContext.current
    var cachedCoverFile by remember(song.id) { mutableStateOf<File?>(null) }

    LaunchedEffect(song.id) {
        cachedCoverFile = CoverCacheManager.getOrFetchCover(
            context = context,
            songId = song.id,
            uri = song.albumArtUri,
            path = song.path
        )
    }

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (cachedCoverFile != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(cachedCoverFile)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (song.albumArtUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(song.albumArtUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }

        if (isCurrentPlaying) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.height(14.dp)
                ) {
                    Box(modifier = Modifier.width(3.dp).fillMaxHeight(0.6f).background(MaterialTheme.colorScheme.primary))
                    Box(modifier = Modifier.width(3.dp).fillMaxHeight(1.0f).background(MaterialTheme.colorScheme.primary))
                    Box(modifier = Modifier.width(3.dp).fillMaxHeight(0.4f).background(MaterialTheme.colorScheme.primary))
                }
            }
        }
    }
}