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
import com.example.comarleyaetheraudio.ui.theme.LocalAppearance
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
    onAddSongsToPlaylist: (Long, List<Long>) -> Unit = { _, _ -> },
    artistGrouped: Map<String, List<Song>>,
    onNavigateToSmartLibrary: () -> Unit = {},
    onPlayContext: (Song, List<Song>, Boolean) -> Unit = { song, queue, _ -> onSongClickWithPlaylist(song, queue) },
    onRenamePlaylist: (Playlist, String) -> Unit = { _, _ -> }
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

    val topArtists = remember(artistGrouped) {
        artistGrouped.entries.take(8).map { entry ->
            Pair(entry.key, entry.value.firstOrNull())
        }
    }

    // 🎨 EVALUACIÓN ESTRICTA DEL FONDO DE INICIO SEGÚN EL MODO SELECCIONADO
    val baseBgColor = MaterialTheme.colorScheme.background
    val accent = MaterialTheme.colorScheme.primary
    val effects = LocalAppearance.current
    val backgroundBrush = remember(baseBgColor, accent, effects.isDarkMode, effects.transparenciesEnabled) {
        Brush.verticalGradient(listOf(
            if (effects.transparenciesEnabled) androidx.compose.ui.graphics.lerp(baseBgColor, accent, 0.04f) else baseBgColor,
            baseBgColor, baseBgColor
        ))
    }
    Box(Modifier.fillMaxSize().background(backgroundBrush)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 12.dp)
            ) {
                Text(
                    text = "SoundFlow",
                    style = MaterialTheme.typography.displaySmall.copy(
                        letterSpacing = (-1).sp,
                        fontWeight = FontWeight.Bold
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

            Box(Modifier.padding(horizontal = 20.dp)) {
                com.example.comarleyaetheraudio.presentation.components.SettingsActionCard(
                    title = "Smart DJ y biblioteca inteligente", subtitle = "Redescubre tu música local",
                    icon = Icons.AutoMirrored.Filled.QueueMusic, onClick = onNavigateToSmartLibrary)
            }

            if (recentSongs.isNotEmpty()) {
                Text(
                    text = "Tu música",
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
                        RecentPosterCard(song = song, onClick = { onPlayContext(song, recentSongs, false) })
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            if (topArtists.isNotEmpty()) {
                Text(
                    text = "Tus artistas",
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
                        containerColor = MaterialTheme.colorScheme.surface
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
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
                onPlayContext = onPlayContext,
                onOpenPlaylist = { selectedPlaylistForDetail = it },
                onRenamePlaylist = onRenamePlaylist,
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
                onPlayContext = onPlayContext,
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
                onSongClick = { song -> onPlayContext(song, artistGrouped[artist].orEmpty(), false) }
            )
        }
    }
}
