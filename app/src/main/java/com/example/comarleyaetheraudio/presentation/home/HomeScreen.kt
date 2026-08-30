package com.example.comarleyaetheraudio.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.comarleyaetheraudio.domain.model.AudioPlayerState
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.home.components.ArtistCircleCard
import com.example.comarleyaetheraudio.presentation.home.components.PlaylistMosaicCard
import com.example.comarleyaetheraudio.presentation.home.components.PlaylistsBottomSheet
import com.example.comarleyaetheraudio.presentation.home.components.RecentPosterCard
import com.example.comarleyaetheraudio.presentation.library.ArtistDetailBottomSheet
import com.example.comarleyaetheraudio.ui.theme.ElectricPurple
import com.example.comarleyaetheraudio.ui.theme.SoftPink

@Composable
fun HomeScreen(
    playerState: AudioPlayerState,
    allSongs: List<Song>,
    playlists: List<Playlist>,
    favoriteIds: List<Long>,
    onSongClick: (Song) -> Unit,
    onFavoritesClick: () -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onDeletePlaylist: (Playlist) -> Unit,
    onAddSongsToPlaylist: (Long, List<Long>) -> Unit = { _, _ -> }
) {
    var showFavoritesSheet by remember { mutableStateOf(false) }
    var showPlaylistsSheet by remember { mutableStateOf(false) }
    var selectedArtistName by remember { mutableStateOf<String?>(null) }

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
    val favoriteSongs = remember(allSongs, favoriteIds) {
        allSongs.filter { favoriteIds.contains(it.id) }
    }

    val artistGrouped = remember(allSongs) {
        allSongs.groupBy { it.artist }
    }
    val topArtists = remember(artistGrouped) {
        artistGrouped.entries.take(8).map { entry ->
            Pair(entry.key, entry.value.firstOrNull())
        }
    }

    val isDark = isSystemInDarkTheme()
    val baseBgColor = MaterialTheme.colorScheme.background

    val topBlobColor = if (isDark) SoftPink.copy(alpha = 0.03f) else Color(0xFFFFD1DC).copy(alpha = 0.35f)
    val midBlobColor = if (isDark) ElectricPurple.copy(alpha = 0.02f) else Color(0xFFFFF2D6).copy(alpha = 0.40f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(baseBgColor)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(topBlobColor, Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.04f),
                        radius = size.width * 0.40f
                    ),
                    center = Offset(size.width * 0.85f, size.height * 0.04f),
                    radius = size.width * 0.40f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(midBlobColor, Color.Transparent),
                        center = Offset(size.width * 0.12f, size.height * 0.28f),
                        radius = size.width * 0.35f
                    ),
                    center = Offset(size.width * 0.12f, size.height * 0.28f),
                    radius = size.width * 0.35f
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

            // RECIENTES
            if (recentSongs.isNotEmpty()) {
                Text(
                    text = "Recently Played",
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

            // TOP ARTISTAS
            if (topArtists.isNotEmpty()) {
                Text(
                    text = "Top Artists",
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

            // SECCIÓN TUS PLAYLISTS EN EL INICIO
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
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
                            onClick = { showPlaylistsSheet = true }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // FAVORITOS
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
                    .clickable { showFavoritesSheet = true },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
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

        // ⚡ LLAMADA CORREGIDA USANDO LOS CALLBACKS NATIVOS DE HOMESCREEN
        if (showPlaylistsSheet) {
            PlaylistsBottomSheet(
                playlists = playlists,
                allAvailableSongs = allSongs,
                onDismiss = { showPlaylistsSheet = false },
                onCreatePlaylist = onCreatePlaylist,
                onDeletePlaylist = onDeletePlaylist,
                onAddSongsToPlaylist = onAddSongsToPlaylist,
                onSongClick = onSongClick
            )
        }

        // ⚡ CONECTAR EL GUARDADO REAL DE CANCIONES A LA PLAYLIST
        if (showPlaylistsSheet) {
            PlaylistsBottomSheet(
                playlists = playlists,
                allAvailableSongs = allSongs,
                onDismiss = { showPlaylistsSheet = false },
                onCreatePlaylist = onCreatePlaylist,
                onDeletePlaylist = onDeletePlaylist,
                onAddSongsToPlaylist = { playlistId, songIds ->
                    onAddSongsToPlaylist(playlistId, songIds) // 👈 Llama al ViewModel para guardar en Room
                },
                onSongClick = onSongClick
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