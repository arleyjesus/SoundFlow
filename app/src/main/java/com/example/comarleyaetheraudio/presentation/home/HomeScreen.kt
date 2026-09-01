package com.example.comarleyaetheraudio.presentation.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comarleyaetheraudio.domain.model.AudioPlayerState
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.home.components.ArtistCircleCard
import com.example.comarleyaetheraudio.presentation.home.components.PlaylistMosaicCard
import com.example.comarleyaetheraudio.presentation.home.components.PlaylistsBottomSheet
import com.example.comarleyaetheraudio.presentation.home.components.RecentPosterCard
import com.example.comarleyaetheraudio.presentation.library.components.ArtistDetailBottomSheet
import com.example.comarleyaetheraudio.presentation.playlist.PlaylistDetailScreen

@Composable
fun HomeScreen(
    playerState: AudioPlayerState,
    allSongs: List<Song>,
    playlists: List<Playlist>,
    favoriteIds: List<Long>,
    isDarkMode: Boolean = false, // ⚡ RECIBE EL MODO REAL DE LA APP
    onSongClick: (Song) -> Unit,
    onSongClickWithPlaylist: (Song, List<Song>) -> Unit = { song, _ -> onSongClick(song) },
    onFavoritesClick: () -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onDeletePlaylist: (Playlist) -> Unit,
    onAddSongsToPlaylist: (Long, List<Long>) -> Unit = { _, _ -> }
) {
    var showFavoritesSheet by remember { mutableStateOf(false) }
    var showPlaylistsSheet by remember { mutableStateOf(false) }
    var selectedArtistName by remember { mutableStateOf<String?>(null) }
    var selectedPlaylistForDetail by remember { mutableStateOf<Playlist?>(null) }

    val greetings = remember {
        listOf(
            "¡Hoy es un buen día para escuchar música! 🎵",
            "Siente el flujo del sonido ✨",
            "Tu banda sonora de hoy 🎧",
            "Descubre tu ritmo perfecto 🎶",
            "Música para cada momento 🚀"
        )
    }
    val randomGreeting = remember { greetings.random() }

    val recentSongs = remember(allSongs) { allSongs.take(8) }

    val artistGrouped = remember(allSongs) {
        allSongs.groupBy { it.artist }
    }
    val topArtists = remember(artistGrouped) {
        artistGrouped.entries.take(8).map { entry ->
            Pair(entry.key, entry.value.firstOrNull())
        }
    }

    // 🎨 EVALUACIÓN ESTRICTA DEL FONDO DE INICIO SEGÚN EL MODO SELECCIONADO
    val baseBgColor = if (isDarkMode) Color(0xFF09090B) else Color(0xFFFAFAFC)

    val topBlobCenterColor = if (isDarkMode) {
        Color(0xFF8E24AA).copy(alpha = 0.10f)
    } else {
        Color(0xFFFDE68A).copy(alpha = 0.28f)
    }

    val midBlobCenterColor = if (isDarkMode) {
        Color(0xFFD32F2F).copy(alpha = 0.07f)
    } else {
        Color(0xFFE9D5FF).copy(alpha = 0.25f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(baseBgColor)
            .drawBehind {
                drawOval(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to topBlobCenterColor,
                            0.4f to topBlobCenterColor.copy(alpha = topBlobCenterColor.alpha * 0.4f),
                            0.8f to topBlobCenterColor.copy(alpha = topBlobCenterColor.alpha * 0.1f),
                            1.0f to Color.Transparent
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.05f),
                        radius = size.width * 0.75f
                    ),
                    topLeft = Offset(size.width * 0.20f, -size.height * 0.10f),
                    size = Size(size.width * 1.0f, size.height * 0.35f)
                )

                drawOval(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to midBlobCenterColor,
                            0.4f to midBlobCenterColor.copy(alpha = midBlobCenterColor.alpha * 0.4f),
                            0.8f to midBlobCenterColor.copy(alpha = midBlobCenterColor.alpha * 0.1f),
                            1.0f to Color.Transparent
                        ),
                        center = Offset(size.width * 0.10f, size.height * 0.30f),
                        radius = size.width * 0.70f
                    ),
                    topLeft = Offset(-size.width * 0.30f, size.height * 0.15f),
                    size = Size(size.width * 0.90f, size.height * 0.38f)
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 120.dp)
        ) {
            Column(
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 12.dp)
            ) {
                Text(
                    text = "SoundFlow",
                    style = MaterialTheme.typography.displaySmall.copy(
                        letterSpacing = (-1).sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = randomGreeting,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (recentSongs.isNotEmpty()) {
                Text(
                    text = "Reproducidas Recientemente",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(recentSongs, key = { it.id }) { song ->
                        RecentPosterCard(song = song, onClick = { onSongClick(song) })
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            if (topArtists.isNotEmpty()) {
                Text(
                    text = "Artistas Principales",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(topArtists, key = { it.first }) { artistPair ->
                        ArtistCircleCard(
                            artistName = artistPair.first,
                            sampleSong = artistPair.second,
                            onClick = { selectedArtistName = artistPair.first }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tus Playlists",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = { showPlaylistsSheet = true }) {
                    Text("Ver todas")
                }
            }

            if (playlists.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clickable { showPlaylistsSheet = true },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) Color(0xFF141417) else Color(0xFFF1F1F5)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Crear Playlist", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Toca para añadir una lista de reproducción", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        PlaylistMosaicCard(
                            playlist = playlist,
                            onClick = { selectedPlaylistForDetail = playlist }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Colecciones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable { onFavoritesClick() },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF141417) else Color(0xFFF1F1F5)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Favoritos", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("${favoriteIds.size} temas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        if (showPlaylistsSheet) {
            PlaylistsBottomSheet(
                playlists = playlists,
                allAvailableSongs = allSongs,
                playerState = playerState,
                favoriteIds = favoriteIds,
                onDismiss = { showPlaylistsSheet = false },
                onCreatePlaylist = onCreatePlaylist,
                onDeletePlaylist = onDeletePlaylist,
                onAddSongsToPlaylist = onAddSongsToPlaylist,
                onSongClick = onSongClick
            )
        }

        selectedPlaylistForDetail?.let { playlist ->
            val currentPlaylist = playlists.find { it.id == playlist.id } ?: playlist

            BackHandler {
                selectedPlaylistForDetail = null
            }

            PlaylistDetailScreen(
                playlist = currentPlaylist,
                allSongs = allSongs,
                playerState = playerState,
                favoriteIds = favoriteIds,
                isDarkMode = isDarkMode, // ⚡ LE PASA EL MODO EXACTO DE LA APLICACIÓN
                onBackClick = { selectedPlaylistForDetail = null },
                onAddSongsConfirmed = { selectedIds ->
                    onAddSongsToPlaylist(currentPlaylist.id, selectedIds)
                },
                onPlayAllClick = { isShuffle ->
                    if (currentPlaylist.songs.isNotEmpty()) {
                        val targetSong = if (isShuffle) currentPlaylist.songs.shuffled().first() else currentPlaylist.songs.first()
                        onSongClickWithPlaylist(targetSong, currentPlaylist.songs)
                    }
                },
                onSongClick = { song ->
                    onSongClickWithPlaylist(song, currentPlaylist.songs)
                },
                onRemoveSongClick = { songToRemove ->
                    val updatedIds = currentPlaylist.songs.map { it.id }.toMutableList()
                    updatedIds.remove(songToRemove.id)
                    onAddSongsToPlaylist(currentPlaylist.id, updatedIds)
                }
            )
        }

        selectedArtistName?.let { artist ->
            val songsOfArtist = artistGrouped[artist] ?: emptyList()
            ArtistDetailBottomSheet(
                artistName = artist,
                artistSongs = songsOfArtist,
                onDismiss = { selectedArtistName = null },
                onSongClick = onSongClick
            )
        }
    }
}