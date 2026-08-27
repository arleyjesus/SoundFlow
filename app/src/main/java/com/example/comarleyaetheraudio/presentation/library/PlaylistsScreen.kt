package com.example.comarleyaetheraudio.presentation.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.components.SongItem

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    allSongs: List<Song>,
    onPlaylistClick: (Playlist) -> Unit,
    onCreatePlaylistClick: (String) -> Unit,
    onRenamePlaylist: (Playlist, String) -> Unit,
    onDeletePlaylist: (Playlist) -> Unit,
    onAddSongToPlaylist: (Long, Long) -> Unit
) {
    var selectedPlaylistForDetail by remember { mutableStateOf<Playlist?>(null) }
    var playlistMenuTarget by remember { mutableStateOf<Playlist?>(null) }
    var playlistToRename by remember { mutableStateOf<Playlist?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showImportSongsSheet by remember { mutableStateOf(false) }

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
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Colección de Listas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
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
                            .clip(RoundedCornerShape(14.dp))
                            .combinedClickable(
                                onClick = { selectedPlaylistForDetail = playlist },
                                onLongClick = { playlistMenuTarget = playlist }
                            ),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
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

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = playlist.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${playlist.songs.size} canciones",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box {
                                IconButton(onClick = { playlistMenuTarget = playlist }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
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

    // PANEL DETALLE DE CANCIONES DE LA PLAYLIST TOCADA
    selectedPlaylistForDetail?.let { playlist ->
        ModalBottomSheet(
            onDismissRequest = { selectedPlaylistForDetail = null },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(playlist.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("${playlist.songs.size} canciones", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(onClick = { showImportSongsSheet = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Añadir")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()

                if (playlist.songs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Esta playlist aún no tiene canciones.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(vertical = 12.dp)) {
                        items(playlist.songs, key = { it.id }) { song ->
                            SongItem(
                                song = song,
                                onClick = {
                                    onPlaylistClick(playlist)
                                    selectedPlaylistForDetail = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // PANEL PARA IMPORTAR CANCIONES A LA PLAYLIST SELECCIONADA
    if (showImportSongsSheet && selectedPlaylistForDetail != null) {
        val currentPlaylist = selectedPlaylistForDetail!!
        ModalBottomSheet(
            onDismissRequest = { showImportSongsSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Añadir a '${currentPlaylist.name}'",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                LazyColumn {
                    items(allSongs, key = { it.id }) { song ->
                        val isAlreadyInPlaylist = currentPlaylist.songs.any { it.id == song.id }
                        SongItem(
                            song = song,
                            onClick = {
                                if (!isAlreadyInPlaylist) {
                                    onAddSongToPlaylist(currentPlaylist.id, song.id)
                                }
                            },
                            modifier = Modifier.background(
                                if (isAlreadyInPlaylist) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }
        }
    }
}