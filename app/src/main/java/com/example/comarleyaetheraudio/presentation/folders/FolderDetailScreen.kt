package com.example.comarleyaetheraudio.presentation.folders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.components.SongItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDetailScreen(
    folderName: String,
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    onBackClick: () -> Unit
) {
    // Limpieza de nombre dentro del cuerpo de la función
    val cleanFolderName = folderName.substringAfterLast(":").trim()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (cleanFolderName.isEmpty()) "Carpeta" else cleanFolderName,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = "No hay canciones en esta carpeta",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(
                    items = songs,
                    key = { song -> song.id },
                    contentType = { "song_item" }
                ) { song ->
                    SongItem(
                        song = song,
                        onClick = { onSongClick(song) }
                    )
                }
            }
        }
    }
}