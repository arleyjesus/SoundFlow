package com.example.comarleyaetheraudio.presentation.library

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.data.local.entity.FolderEntity
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.folders.FoldersScreen
import com.example.comarleyaetheraudio.ui.theme.BrandGradient
import com.example.comarleyaetheraudio.ui.theme.LightLavender

@Composable
fun LibraryScreen(
    songs: List<Song>,
    playlists: List<Playlist>,
    folders: List<FolderEntity>,
    favoriteIds: List<Long>,
    onSongClick: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToPlaylist: (Long, Long) -> Unit,
    onFolderClick: (String) -> Unit,
    onAddFolder: (Uri) -> Unit,
    onRemoveFolder: (String) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onCreatePlaylistClick: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Canciones", "Artistas", "Carpetas")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Título de Sección
        Text(
            text = "Tu Biblioteca",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 12.dp)
        )

        // Pestañas Superiores de Navegación
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        // Vistas de la Pestaña Seleccionada
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTabIndex) {
                0 -> SongsScreen(
                    songs = songs,
                    playlists = playlists,
                    favoriteIds = favoriteIds,
                    onSongClick = onSongClick,
                    onToggleFavorite = onToggleFavorite,
                    onAddToPlaylist = onAddToPlaylist
                )
                1 -> ArtistsTab(songs = songs, onSongClick = onSongClick)
                2 -> FoldersScreen(
                    folders = folders,
                    onAddFolder = onAddFolder,
                    onRemoveFolder = onRemoveFolder,
                    onFolderClick = onFolderClick
                )
            }
        }
    }
}

@Composable
fun ArtistsTab(
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    val artists = remember(songs) {
        songs.groupBy { it.artist }.toList().sortedBy { it.first.lowercase() }
    }

    if (artists.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay artistas disponibles", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(artists) { (artistName, artistSongs) ->
                ArtistRowItem(
                    artistName = artistName,
                    songCount = artistSongs.size,
                    onClick = {
                        if (artistSongs.isNotEmpty()) {
                            onSongClick(artistSongs.first())
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ArtistRowItem(
    artistName: String,
    songCount: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Círculo con Inicial del Artista
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(BrandGradient),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = artistName.take(1).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artistName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$songCount ${if (songCount == 1) "canción" else "canciones"}",
                style = MaterialTheme.typography.bodyMedium,
                color = LightLavender
            )
        }
    }
}