package com.example.comarleyaetheraudio.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.components.SongItem

@Composable
fun SearchScreen(
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredSongs = remember(searchQuery, songs) {
        if (searchQuery.trim().isEmpty()) {
            emptyList()
        } else {
            songs.filter { song ->
                song.title.contains(searchQuery, ignoreCase = true) ||
                        song.artist.contains(searchQuery, ignoreCase = true) ||
                        song.album.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "Buscar",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 12.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Canciones, artistas o álbumes...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = MaterialTheme.shapes.medium
        )

        if (searchQuery.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Escribe para empezar a buscar en tu música local",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (filteredSongs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Sin coincidencias para \"$searchQuery\"",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(filteredSongs) { song ->
                    SongItem(
                        song = song,
                        onClick = { onSongClick(song) }
                    )
                }
            }
        }
    }
}