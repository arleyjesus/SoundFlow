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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.components.items.SongItem
import com.example.comarleyaetheraudio.presentation.library.components.ArtistDetailBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongsScreen(
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    val tabs = remember { listOf("Canciones", "Artistas") }
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    // Estado para abrir el panel del artista seleccionado
    var selectedArtistName by remember { mutableStateOf<String?>(null) }

    val artistGrouped = remember(songs) {
        songs.groupBy { it.artist }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TabRow Superior (Canciones / Artistas)
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> {
                    // Pestaña Todas las Canciones
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(songs, key = { it.id }) { song ->
                            SongItem(song = song, onClick = { onSongClick(song) })
                        }
                    }
                }
                1 -> {
                    // Pestaña Artistas
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(artistGrouped.keys.toList()) { artist ->
                            val count = artistGrouped[artist]?.size ?: 0

                            ListItem(
                                modifier = Modifier.clickable { selectedArtistName = artist },
                                leadingContent = {
                                    Surface(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                        }
                                    }
                                },
                                headlineContent = { Text(artist, fontWeight = FontWeight.Bold) },
                                supportingContent = { Text("$count Canciones") }
                            )
                        }
                    }
                }
            }
        }

        // PANEL EMERGENTE CUANDO SE SELECCIONA UN ARTISTA
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