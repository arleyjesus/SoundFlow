package com.example.comarleyaetheraudio.presentation.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.components.SongItem
import com.example.comarleyaetheraudio.presentation.components.SongOptionsMenuSheet

@Composable
fun SongsScreen(
    songs: List<Song>,
    playlists: List<Playlist>,
    favoriteIds: List<Long>,
    onSongClick: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToPlaylist: (playlistId: Long, songId: Long) -> Unit
) {
    // Estado para saber qué canción abrió el menú de 3 puntos
    var selectedSongForMenu by remember { mutableStateOf<Song?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "Tus Canciones",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 16.dp, top = 32.dp, bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp) // Espacio para el MiniPlayer
        ) {
            items(
                items = songs,
                key = { song -> song.id },
                contentType = { "song_item" }
            ) { song ->
                SongItem(
                    song = song,
                    onClick = { onSongClick(song) },
                    onMoreOptionsClick = { selectedSongForMenu = song }
                )
            }
        }
    }

    // Modal BottomSheet cuando se tocan los 3 puntos de una canción
    selectedSongForMenu?.let { song ->
        SongOptionsMenuSheet(
            song = song,
            playlists = playlists,
            isFavorite = favoriteIds.contains(song.id),
            onDismiss = { selectedSongForMenu = null },
            onToggleFavorite = { onToggleFavorite(song) },
            onAddToPlaylist = { playlistId ->
                onAddToPlaylist(playlistId, song.id)
            }
        )
    }
}