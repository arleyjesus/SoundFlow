package com.example.comarleyaetheraudio.presentation.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.comarleyaetheraudio.data.local.CoverCacheManager
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.components.SongItem
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    songs: List<Song>,
    artistGrouped: Map<String, List<Song>>,
    playlists: List<Playlist>,
    favoriteIds: List<Long>,
    onSongClick: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToPlaylist: (Long, Long) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onRenamePlaylist: (Playlist, String) -> Unit,
    onDeletePlaylist: (Playlist) -> Unit
) {
    val tabs = remember { listOf("Canciones", "Artistas", "Playlists") }
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    var selectedArtistName by remember { mutableStateOf<String?>(null) }
    var songTargetForPlaylist by remember { mutableStateOf<Song?>(null) }

    val artistList by remember(artistGrouped) {
        derivedStateOf { artistGrouped.keys.toList() }
    }

    // Novedad: Cálculo formatado del total de canciones y tiempo total de reproducción
    val totalStatsText by remember(songs) {
        derivedStateOf {
            val totalMs = songs.sumOf { it.duration }
            val hours = totalMs / (1000 * 60 * 60)
            val minutes = (totalMs % (1000 * 60 * 60)) / (1000 * 60)
            if (hours > 0) {
                "${songs.size} Canciones • ${hours} h ${minutes} min"
            } else {
                "${songs.size} Canciones • ${minutes} min"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // PESTAÑAS FLOTANTES
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = pagerState.currentPage == index
                Surface(
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    tonalElevation = if (isSelected) 6.dp else 0.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> {
                    // PESTAÑA CANCIONES CON CABECERA DE DURACIÓN Y OPTIMIZACIÓN DE REUSO
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item(contentType = "header_stats") {
                            Text(
                                text = totalStatsText,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                            )
                        }

                        items(
                            items = songs,
                            key = { song -> song.id },
                            contentType = { "song_item" }
                        ) { song ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SongItem(
                                    song = song,
                                    onClick = { onSongClick(song) },
                                    onMoreOptionsClick = { songTargetForPlaylist = song }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // PESTAÑA ARTISTAS CON REUSO DE NODOS
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(
                            items = artistList,
                            key = { artist -> artist },
                            contentType = { "artist_item" }
                        ) { artist ->
                            val songsOfArtist = artistGrouped[artist]
                            val count = songsOfArtist?.size ?: 0
                            val sampleSong = songsOfArtist?.firstOrNull()

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ArtistListItem(
                                    artistName = artist,
                                    songCount = count,
                                    sampleSong = sampleSong,
                                    onClick = { selectedArtistName = artist }
                                )
                            }
                        }
                    }
                }
                2 -> {
                    // PESTAÑA PLAYLISTS COMPLETA
                    PlaylistsScreen(
                        playlists = playlists,
                        allSongs = songs,
                        onPlaylistClick = { playlist ->
                            if (playlist.songs.isNotEmpty()) onSongClick(playlist.songs.first())
                        },
                        onCreatePlaylistClick = onCreatePlaylist,
                        onRenamePlaylist = onRenamePlaylist,
                        onDeletePlaylist = onDeletePlaylist,
                        onAddSongToPlaylist = onAddToPlaylist
                    )
                }
            }
        }

        // DETALLE DE CANCIONES DE ARTISTA
        selectedArtistName?.let { artist ->
            val songsOfArtist = artistGrouped[artist] ?: emptyList()
            ArtistDetailBottomSheet(
                artistName = artist,
                artistSongs = songsOfArtist,
                onDismiss = { selectedArtistName = null },
                onSongClick = onSongClick
            )
        }

        // MENU EMERGENTE PARA IMPORTAR
        songTargetForPlaylist?.let { targetSong ->
            ModalBottomSheet(onDismissRequest = { songTargetForPlaylist = null }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Añadir '${targetSong.title}' a...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    if (playlists.isEmpty()) {
                        Text("No tienes playlists creadas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyColumn {
                            items(playlists, key = { it.id }) { playlist ->
                                ListItem(
                                    modifier = Modifier.clickable {
                                        onAddToPlaylist(playlist.id, targetSong.id)
                                        songTargetForPlaylist = null
                                    },
                                    leadingContent = { Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null) },
                                    headlineContent = { Text(playlist.name, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistListItem(
    artistName: String,
    songCount: Int,
    sampleSong: Song?,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var artistCoverFile by remember(artistName) { mutableStateOf<File?>(null) }

    LaunchedEffect(artistName, sampleSong) {
        sampleSong?.let { song ->
            artistCoverFile = CoverCacheManager.getOrFetchCover(context, song.id, song.albumArtUri, song.path)
        }
    }

    ListItem(
        modifier = Modifier.clickable { onClick() },
        leadingContent = {
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
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
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        },
        headlineContent = { Text(artistName, fontWeight = FontWeight.Bold) },
        supportingContent = { Text("$songCount Canciones") }
    )
}