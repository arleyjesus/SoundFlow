package com.example.comarleyaetheraudio.presentation.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.domain.model.AudioPlayerState
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.playlist.AddSongsToPlaylistDialog

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    allSongs: List<Song>,
    playerState: AudioPlayerState,
    favoriteIds: List<Long>,
    onPlaylistClick: (Playlist) -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onPlayAllPlaylist: (Playlist, Boolean) -> Unit,
    onCreatePlaylistClick: (String) -> Unit,
    onRenamePlaylist: (Playlist, String) -> Unit,
    onDeletePlaylist: (Playlist) -> Unit,
    onAddSongsToPlaylist: (Long, List<Long>) -> Unit,
    onMoreSongOptionsClick: (Song) -> Unit = {}
) {
    var playlistMenuTarget by remember { mutableStateOf<Playlist?>(null) }
    var playlistToRename by remember { mutableStateOf<Playlist?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    var playlistIdToAddSongs by remember { mutableStateOf<Long?>(null) }

    var renameText by remember { mutableStateOf("") }
    var createText by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        if (playlists.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No tienes playlists creadas",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Crear Playlist")
                    }
                }
            }
        } else {
            // ⚡ SE REDUCE EL PADDING HORIZONTAL PARA EXPANDIR LAS PLAYLISTS CASI HASTA EL BORDE DE PANTALLA
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Colección de Listas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        IconButton(onClick = { showCreateDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Crear", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                items(playlists, key = { it.id }) { playlist ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .combinedClickable(
                                onClick = { onPlaylistClick(playlist) },
                                onLongClick = { playlistMenuTarget = playlist }
                            ),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(50.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = playlist.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${playlist.songs.size} canciones",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box {
                                IconButton(onClick = { playlistMenuTarget = playlist }) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "Opciones",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                DropdownMenu(
                                    expanded = playlistMenuTarget?.id == playlist.id,
                                    onDismissRequest = { playlistMenuTarget = null }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Cambiar nombre") },
                                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                        onClick = {
                                            renameText = playlist.name
                                            playlistToRename = playlist
                                            playlistMenuTarget = null
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Eliminar playlist") },
                                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            onDeletePlaylist(playlist)
                                            playlistMenuTarget = null
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIÁLOGO: CREAR PLAYLIST
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Nueva Playlist") },
            text = {
                OutlinedTextField(
                    value = createText,
                    onValueChange = { createText = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (createText.isNotBlank()) {
                        onCreatePlaylistClick(createText.trim())
                        createText = ""
                        showCreateDialog = false
                    }
                }) { Text("Crear") }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancelar") } }
        )
    }

    // DIÁLOGO: RENOMBRAR PLAYLIST
    playlistToRename?.let { playlist ->
        AlertDialog(
            onDismissRequest = { playlistToRename = null },
            title = { Text("Editar Playlist") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Nuevo Nombre") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (renameText.isNotBlank()) {
                        onRenamePlaylist(playlist, renameText.trim())
                        playlistToRename = null
                    }
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { playlistToRename = null }) { Text("Cancelar") } }
        )
    }

    // DIÁLOGO PARA IMPORTAR CANCIONES CON SELECCIÓN MÚLTIPLE
    playlistIdToAddSongs?.let { playlistId ->
        val currentPlaylist = playlists.find { it.id == playlistId }
        val existingIds = currentPlaylist?.songs?.map { it.id } ?: emptyList()

        AddSongsToPlaylistDialog(
            availableSongs = allSongs,
            alreadyAddedSongIds = existingIds,
            onDismiss = { playlistIdToAddSongs = null },
            onAddSongsConfirmed = { selectedIds ->
                onAddSongsToPlaylist(playlistId, selectedIds)
                playlistIdToAddSongs = null
            }
        )
    }
}