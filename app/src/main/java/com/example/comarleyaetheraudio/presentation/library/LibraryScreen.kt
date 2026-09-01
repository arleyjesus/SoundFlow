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
import com.example.comarleyaetheraudio.data.local.util.ArtistImageFetcher
import com.example.comarleyaetheraudio.data.local.util.CoverCacheManager
import com.example.comarleyaetheraudio.domain.model.AudioPlayerState
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.components.items.SongItem
import com.example.comarleyaetheraudio.presentation.library.components.ArtistDetailBottomSheet
import com.example.comarleyaetheraudio.presentation.playlist.PlaylistDetailScreen
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    songs: List<Song>,
    artistGrouped: Map<String, List<Song>>,
    playlists: List<Playlist>,
    favoriteIds: List<Long>,
    playerState: AudioPlayerState = AudioPlayerState(),
    isDarkMode: Boolean = false, // ⚡ PASO EXPLÍCITO DE MODO OSCURO / CLARO
    onSongClick: (Song) -> Unit,
    onSongClickWithPlaylist: (Song, List<Song>) -> Unit = { song, _ -> onSongClick(song) },
    onToggleFavorite: (Song) -> Unit,
    onAddToPlaylist: (Long, Long) -> Unit = { _, _ -> },
    onAddSongsToPlaylist: (Long, List<Long>) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onRenamePlaylist: (Playlist, String) -> Unit,
    onDeletePlaylist: (Playlist) -> Unit
) {
    val tabs = remember { listOf("Canciones", "Artistas", "Playlists") }
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    var selectedArtistName by remember { mutableStateOf<String?>(null) }
    var songTargetForPlaylist by remember { mutableStateOf<String?>(null) }
    var selectedPlaylistForDetail by remember { mutableStateOf<Playlist?>(null) }

    // 🎨 ASIGNACIÓN DE COLORES DE FONDO Y TARJETAS UNIFORMES
    val backgroundColor = if (isDarkMode) Color(0xFF09090B) else Color(0xFFFAFAFC)
    val cardBgColor = if (isDarkMode) Color(0xFF141417) else Color(0xFFF1F1F5)
    val textColor = if (isDarkMode) Color.White else Color(0xFF111111)

    val artistList by remember(artistGrouped) {
        derivedStateOf { artistGrouped.keys.toList() }
    }

    val totalStatsText by remember(songs) {
        derivedStateOf {
            val totalMs = songs.sumOf { it.duration }
            val hours = totalMs / (1000 * 60 * 60)
            val minutes = (totalMs % (1000 * 60 * 60)) / (1000 * 60)
            if (hours > 0) "${songs.size} Canciones • ${hours} h ${minutes} min"
            else "${songs.size} Canciones • ${minutes} min"
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                        color = if (isSelected) MaterialTheme.colorScheme.primary else cardBgColor,
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
                            items(songs, key = { song -> song.id }) { song ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = cardBgColor,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    SongItem(
                                        song = song,
                                        onClick = { onSongClick(song) },
                                        onMoreOptionsClick = { songTargetForPlaylist = song.title }
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(artistList, key = { artist -> artist }) { artist ->
                                val songsOfArtist = artistGrouped[artist]
                                val count = songsOfArtist?.size ?: 0
                                val sampleSong = songsOfArtist?.firstOrNull()

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = cardBgColor,
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
                        PlaylistsScreen(
                            playlists = playlists,
                            allSongs = songs,
                            playerState = playerState,
                            favoriteIds = favoriteIds,
                            onPlaylistClick = { playlist ->
                                selectedPlaylistForDetail = playlist
                            },
                            onSongClick = { song, playlistSongs -> onSongClickWithPlaylist(song, playlistSongs) },
                            onPlayAllPlaylist = { playlist, isShuffle ->
                                if (playlist.songs.isNotEmpty()) {
                                    val targetSong = if (isShuffle) playlist.songs.shuffled().first() else playlist.songs.first()
                                    onSongClickWithPlaylist(targetSong, playlist.songs)
                                }
                            },
                            onCreatePlaylistClick = onCreatePlaylist,
                            onRenamePlaylist = onRenamePlaylist,
                            onDeletePlaylist = onDeletePlaylist,
                            onAddSongsToPlaylist = onAddSongsToPlaylist
                        )
                    }
                }
            }
        }

        // DETALLE DE PLAYLIST A PANTALLA COMPLETA CON PROPAGACIÓN DEL MODO
        selectedPlaylistForDetail?.let { playlist ->
            val currentPlaylist = playlists.find { it.id == playlist.id } ?: playlist

            PlaylistDetailScreen(
                playlist = currentPlaylist,
                allSongs = songs,
                playerState = playerState,
                favoriteIds = favoriteIds,
                isDarkMode = isDarkMode, // ⚡ MANTIENE EL MODO CLARO O OSCURO EN LA PLAYLIST
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

        // DETALLE DE ARTISTA
        selectedArtistName?.let { artist ->
            ArtistDetailBottomSheet(
                artistName = artist,
                artistSongs = artistGrouped[artist] ?: emptyList(),
                onDismiss = { selectedArtistName = null },
                onSongClick = onSongClick
            )
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
    var artistImagePath by remember(artistName) { mutableStateOf<String?>(null) }
    var artistCoverFile by remember(artistName) { mutableStateOf<File?>(null) }

    LaunchedEffect(artistName, sampleSong) {
        val onlinePath = ArtistImageFetcher.getOrFetchArtistPicture(context, artistName)
        if (onlinePath != null) {
            artistImagePath = onlinePath
        } else {
            sampleSong?.let { song ->
                artistCoverFile = CoverCacheManager.getOrFetchCover(context, song.id, song.albumArtUri, song.path)
            }
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
                when {
                    artistImagePath != null -> {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(File(artistImagePath!!))
                                .crossfade(true)
                                .build(),
                            contentDescription = artistName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    artistCoverFile != null -> {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(artistCoverFile)
                                .crossfade(false)
                                .build(),
                            contentDescription = artistName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        },
        headlineContent = { Text(artistName, fontWeight = FontWeight.Bold) },
        supportingContent = { Text("$songCount Canciones") }
    )
}