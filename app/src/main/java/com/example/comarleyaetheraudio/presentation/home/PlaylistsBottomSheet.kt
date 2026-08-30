package com.example.comarleyaetheraudio.presentation.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.playlist.AddSongsToPlaylistDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsBottomSheet(
    playlists: List<Playlist>,
    allAvailableSongs: List<Song> = emptyList(),
    onDismiss: () -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onDeletePlaylist: (Playlist) -> Unit,
    onAddSongsToPlaylist: (Long, List<Long>) -> Unit = { _, _ -> },
    onSongClick: (Song) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    // Control para desplegar el contenido de una playlist
    var selectedPlaylistDetail by remember { mutableStateOf<Playlist?>(null) }
    // Control para abrir el diálogo de añadir canciones
    var playlistIdToAddSongs by remember { mutableStateOf<Long?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedPlaylistDetail?.name ?: "Tus Playlists",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (selectedPlaylistDetail == null) {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Crear Playlist")
                    }
                } else {
                    TextButton(onClick = { selectedPlaylistDetail = null }) {
                        Text("Volver")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // VISTA DETALLADA DE LA PLAYLIST SELECCIONADA
            if (selectedPlaylistDetail != null) {
                val currentPlaylist = selectedPlaylistDetail!!

                if (currentPlaylist.songs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Esta playlist no contiene canciones",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(onClick = { playlistIdToAddSongs = currentPlaylist.id }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Añadir canciones")
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { playlistIdToAddSongs = currentPlaylist.id }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Añadir más")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxHeight(0.6f)
                    ) {
                        items(currentPlaylist.songs, key = { it.id }) { song ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSongClick(song) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = song.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(text = song.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // LISTADO PRINCIPAL DE TODAS LAS PLAYLISTS
                if (playlists.isEmpty()) {
                    Text(
                        text = "No has creado ninguna lista de reproducción aún.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxHeight(0.6f)
                    ) {
                        items(playlists, key = { it.id }) { playlist ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedPlaylistDetail = playlist }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = playlist.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${playlist.songs.size} canciones",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = { playlistIdToAddSongs = playlist.id }) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir canciones")
                                    }
                                    IconButton(onClick = { onDeletePlaylist(playlist) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar playlist", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // DIÁLOGO PARA CREAR NUEVA PLAYLIST
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Nueva Playlist") },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Nombre de la lista") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            onCreatePlaylist(newPlaylistName.trim())
                            newPlaylistName = ""
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Crear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // DIÁLOGO DE SELECCIÓN MÚLTIPLE DE CANCIONES CON IDs EXISTENTES (CAMBIO DE COLOR INSTANTÁNEO)
    playlistIdToAddSongs?.let { playlistId ->
        val currentPlaylist = playlists.find { it.id == playlistId }
        val existingIds = currentPlaylist?.songs?.map { it.id } ?: emptyList()

        AddSongsToPlaylistDialog(
            availableSongs = allAvailableSongs,
            alreadyAddedSongIds = existingIds,
            onDismiss = { playlistIdToAddSongs = null },
            onAddSongsConfirmed = { selectedIds ->
                onAddSongsToPlaylist(playlistId, selectedIds)
                playlistIdToAddSongs = null
            }
        )
    }
}