package com.example.comarleyaetheraudio.presentation.library

import com.example.comarleyaetheraudio.ui.theme.LocalAppearance

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
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
import com.example.comarleyaetheraudio.presentation.components.items.SongOptionsMenuSheet
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
    isDarkMode: Boolean = false,
    onSongClick: (Song) -> Unit,
    onSongClickWithPlaylist: (Song, List<Song>) -> Unit = { song, _ -> onSongClick(song) },
    onToggleFavorite: (Song) -> Unit,
    onAddToPlaylist: (Long, Long) -> Unit = { _, _ -> },
    onAddSongsToPlaylist: (Long, List<Long>) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onRenamePlaylist: (Playlist, String) -> Unit,
    onDeletePlaylist: (Playlist) -> Unit,
    onPlayContext: (Song, List<Song>, Boolean) -> Unit = { song, queue, _ -> onSongClickWithPlaylist(song, queue) },
    libraryOrder: com.example.comarleyaetheraudio.domain.library.LibraryOrder = com.example.comarleyaetheraudio.domain.library.LibraryOrder.ADDED_NEWEST,
    onLibraryOrder: (com.example.comarleyaetheraudio.domain.library.LibraryOrder) -> Unit = {}
) {
    val tabs = remember { listOf("Canciones", "Artistas", "Playlists") }
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    val animationsEnabled = LocalAppearance.current.motionEnabled

    var selectedArtistName by remember { mutableStateOf<String?>(null) }
    var songOptionsId by remember { mutableStateOf<Long?>(null) }
    var selectedPlaylistForDetail by remember { mutableStateOf<Playlist?>(null) }

    // ⚡ Estado para el filtro de ordenamiento en la pestaña de canciones
    var showOrderMenu by remember { mutableStateOf(false) }

    // 🎨 ASIGNACIÓN DE COLORES DE FONDO Y TARJETAS UNIFORMES
    val backgroundColor = com.example.comarleyaetheraudio.ui.theme.screenBackgroundColor()
    val cardBgColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface

    val artistList by remember(artistGrouped) {
        derivedStateOf { artistGrouped.keys.sortedWith(String.CASE_INSENSITIVE_ORDER) }
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
            Text("Biblioteca", style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = pagerState.currentPage == index
                    Surface(
                        onClick = { scope.launch { if (animationsEnabled) pagerState.animateScrollToPage(index) else pagerState.scrollToPage(index) } },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else cardBgColor,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        tonalElevation = if (isSelected) 6.dp else 0.dp,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
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
                        // ⚡ Lista de canciones ordenada dinámicamente
                        val sortedSongs = remember(songs, libraryOrder) {
                            com.example.comarleyaetheraudio.domain.library.orderedLibrary(songs, libraryOrder,
                                { it.id }, { it.title }, { it.artist }, { it.addedAtMs })
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item(contentType = "header_stats") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = totalStatsText,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box {
                                    TextButton(onClick = { showOrderMenu = true }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Sort,
                                            contentDescription = "Ordenar",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = libraryOrder.label,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                    DropdownMenu(expanded = showOrderMenu, onDismissRequest = { showOrderMenu = false }) {
                                        com.example.comarleyaetheraudio.domain.library.LibraryOrder.entries.forEach { order ->
                                            DropdownMenuItem(text = { Text(order.label) }, onClick = {
                                                onLibraryOrder(order); showOrderMenu = false
                                            })
                                        }
                                    }
                                    }
                                }
                            }
                            items(sortedSongs, key = { song -> song.id }) { song ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = cardBgColor,
                                    modifier = Modifier.fillMaxWidth().testTag("library_song_${song.id}")
                                ) {
                                    SongItem(
                                        song = song,
                                        isActive = playerState.currentSong?.id == song.id,
                                        onClick = { onPlayContext(song, sortedSongs, false) },
                                        onMoreOptionsClick = { songOptionsId = song.id }
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
                                    onPlayContext(targetSong, playlist.songs, isShuffle)
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

        songs.find { it.id == songOptionsId }?.let { song ->
            key(song.id) {
                SongOptionsMenuSheet(
                    song = song,
                    playlists = playlists,
                    isFavorite = song.id in favoriteIds,
                    onDismiss = { songOptionsId = null },
                    onToggleFavorite = { onToggleFavorite(song) },
                    onAddToPlaylist = { playlistId -> onAddToPlaylist(playlistId, song.id) }
                )
            }
        }

        // DETALLE DE PLAYLIST A PANTALLA COMPLETA
        selectedPlaylistForDetail?.let { playlist ->
            val currentPlaylist = playlists.find { it.id == playlist.id } ?: playlist

            PlaylistDetailScreen(
                onPlayContext = onPlayContext,
                playlist = currentPlaylist,
                allSongs = songs,
                playerState = playerState,
                favoriteIds = favoriteIds,
                isDarkMode = isDarkMode,
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
                onSongClick = { song -> onPlayContext(song, artistGrouped[artist].orEmpty(), false) }
            )
        }
    }
}

@Composable
private fun ArtistListItem(artistName: String, songCount: Int, sampleSong: Song?, onClick: () -> Unit) {
    ListItem(modifier = Modifier.clickable(onClick = onClick),
        leadingContent = {
            com.example.comarleyaetheraudio.presentation.components.ArtistPortrait(
                artistName, sampleSong, Modifier.size(52.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer))
        },
        headlineContent = { Text(artistName, style = MaterialTheme.typography.titleMedium) },
        supportingContent = { Text("$songCount canciones") })
}
