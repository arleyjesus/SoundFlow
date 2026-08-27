package com.example.comarleyaetheraudio.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.comarleyaetheraudio.data.local.CoverCacheManager
import com.example.comarleyaetheraudio.domain.model.AudioPlayerState
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.library.ArtistDetailBottomSheet
import com.example.comarleyaetheraudio.ui.theme.ElectricPurple
import com.example.comarleyaetheraudio.ui.theme.SoftPink
import java.io.File

@Composable
fun HomeScreen(
    playerState: AudioPlayerState,
    allSongs: List<Song>,
    playlists: List<Playlist>,
    favoriteIds: List<Long>,
    onSongClick: (Song) -> Unit,
    onFavoritesClick: () -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onDeletePlaylist: (Playlist) -> Unit
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

    // MANCHAS CON TAMAÑO E INTENSIDAD LIGERAMENTE REDUCIDOS
    val topBlobColor = if (isDark) SoftPink.copy(alpha = 0.20f) else Color(0xFFFFD1DC).copy(alpha = 0.50f)
    val midBlobColor = if (isDark) ElectricPurple.copy(alpha = 0.16f) else Color(0xFFFFF2D6).copy(alpha = 0.55f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(baseBgColor)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(topBlobColor, Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.04f),
                        radius = size.width * 0.55f // Tamaño reducido de 0.7f a 0.55f
                    ),
                    center = Offset(size.width * 0.85f, size.height * 0.04f),
                    radius = size.width * 0.55f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(midBlobColor, Color.Transparent),
                        center = Offset(size.width * 0.12f, size.height * 0.28f),
                        radius = size.width * 0.50f // Tamaño reducido de 0.65f a 0.50f
                    ),
                    center = Offset(size.width * 0.12f, size.height * 0.28f),
                    radius = size.width * 0.50f
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

            Text(
                text = "Colecciones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showFavoritesSheet = true },
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

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showPlaylistsSheet = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Playlists", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("${playlists.size} listas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        if (showFavoritesSheet) {
            FavoritesBottomSheet(
                favoriteSongs = favoriteSongs,
                onDismiss = { showFavoritesSheet = false },
                onSongClick = { song -> onSongClick(song) }
            )
        }

        if (showPlaylistsSheet) {
            PlaylistsBottomSheet(
                playlists = playlists,
                onDismiss = { showPlaylistsSheet = false },
                onCreatePlaylist = onCreatePlaylist,
                onDeletePlaylist = onDeletePlaylist,
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

@Composable
fun RecentPosterCard(song: Song, onClick: () -> Unit) {
    val context = LocalContext.current
    var coverFile by remember(song.id) { mutableStateOf<File?>(null) }

    LaunchedEffect(song.id) {
        coverFile = CoverCacheManager.getOrFetchCover(context, song.id, song.albumArtUri, song.path)
    }

    // TARJETA DE RECIENTES CON ALTA TRANSPARENCIA (0.28f)
    Card(
        modifier = Modifier
            .width(135.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                if (coverFile != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(coverFile)
                            .crossfade(false)
                            .build(),
                        contentDescription = "Portada",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = song.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ArtistCircleCard(artistName: String, sampleSong: Song?, onClick: () -> Unit) {
    val context = LocalContext.current
    var artistCoverFile by remember(artistName) { mutableStateOf<File?>(null) }

    LaunchedEffect(artistName, sampleSong) {
        sampleSong?.let { song ->
            artistCoverFile = CoverCacheManager.getOrFetchCover(context, song.id, song.albumArtUri, song.path)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            if (artistCoverFile != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(artistCoverFile)
                        .crossfade(false)
                        .build(),
                    contentDescription = artistName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = artistName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = artistName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}