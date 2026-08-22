package com.example.comarleyaetheraudio.presentation.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.components.SongItem

enum class ViewMode { LIST, GRID }
enum class SortMode { NAME_ASC, NAME_DESC, RECENT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsScreen(
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }
    var sortMode by remember { mutableStateOf(SortMode.NAME_ASC) }
    var showMenu by remember { mutableStateOf(false) }

    val filteredSongs = remember(songs, searchQuery, sortMode) {
        val filtered = songs.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artist.contains(searchQuery, ignoreCase = true)
        }
        when (sortMode) {
            SortMode.NAME_ASC -> filtered.sortedBy { it.title.lowercase() }
            SortMode.NAME_DESC -> filtered.sortedByDescending { it.title.lowercase() }
            SortMode.RECENT -> filtered.sortedByDescending { it.id }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Barra Superior Unificada: Búsqueda + Filtro + Cuadrícula/Lista
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Buscar canción...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Alternar Lista / Cuadrícula
            IconButton(onClick = {
                viewMode = if (viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
            }) {
                Icon(
                    imageVector = if (viewMode == ViewMode.LIST) Icons.Default.GridView else Icons.Default.ViewList,
                    contentDescription = "Cambiar vista"
                )
            }

            // Menú Unificado de Ordenamiento
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.Sort, contentDescription = "Filtrar")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Nombre (A - Z)") },
                        onClick = { sortMode = SortMode.NAME_ASC; showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Nombre (Z - A)") },
                        onClick = { sortMode = SortMode.NAME_DESC; showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Agregados Recientemente") },
                        onClick = { sortMode = SortMode.RECENT; showMenu = false }
                    )
                }
            }
        }

        if (filteredSongs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay canciones disponibles.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (viewMode == ViewMode.LIST) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredSongs) { song ->
                    SongItem(song = song, onClick = { onSongClick(song) })
                }
            }
        } else {
            // VISTA EN CUADRÍCULA (GRID)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                items(filteredSongs) { song ->
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable { onSongClick(song) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp)),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Icon(
                                    Icons.Default.MusicNote,
                                    contentDescription = null,
                                    modifier = Modifier.padding(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(song.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(song.artist, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}