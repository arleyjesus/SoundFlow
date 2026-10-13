package com.example.comarleyaetheraudio.presentation.playlist

import com.example.comarleyaetheraudio.ui.theme.LocalAppearance

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
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
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
    onRemoveSongClick: (Song) -> Unit,
    onPlayContext: ((Song, List<Song>, Boolean) -> Unit)? = null
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
    fun playVisible(shuffle: Boolean) {
        if (onPlayContext == null) onPlayAllClick(shuffle)
        else if (sortedPlaylistSongs.isNotEmpty()) {
            val first = if (shuffle) sortedPlaylistSongs.random() else sortedPlaylistSongs.first()
            onPlayContext(first, sortedPlaylistSongs, shuffle)
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.background
    val cardBgColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface

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
                        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.QueueMusic, null, Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
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
                        text = playlist.name,
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
                            text = if (sortDescending) "Añadidas recientemente" else "Orden de la playlist",
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
                            onClick = { playVisible(true) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isShuffleActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isShuffleActive) MaterialTheme.colorScheme.primary else textColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Shuffle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (isShuffleActive) MaterialTheme.colorScheme.primary else textColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Aleatorio", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { playVisible(false) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .shadow(
                                    elevation = 6.dp,
                                    shape = RoundedCornerShape(12.dp),
                                    ambientColor = MaterialTheme.colorScheme.primary,
                                    spotColor = MaterialTheme.colorScheme.primary
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reproducir", fontWeight = FontWeight.Black)
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
                    color = if (isCurrentPlaying) MaterialTheme.colorScheme.primaryContainer else cardBgColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                        .clickable { if (onPlayContext != null) onPlayContext(song, sortedPlaylistSongs, false) else onSongClick(song) }
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
                            Icon(Icons.Rounded.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp).padding(end = 6.dp))
                        }

                        Box {
                            IconButton(onClick = { expandedMenuSongId = song.id }) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = textColor)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { showAddSongsDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Añadir más", tint = textColor)
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
            .crossfade(LocalAppearance.current.motionEnabled)
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
                    .crossfade(LocalAppearance.current.motionEnabled)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (song.albumArtUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(song.albumArtUri)
                    .crossfade(LocalAppearance.current.motionEnabled)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
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
