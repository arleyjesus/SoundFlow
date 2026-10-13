package com.example.comarleyaetheraudio.presentation.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.domain.library.*
import com.example.comarleyaetheraudio.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.comarleyaetheraudio.presentation.components.SoundFlowPanel
import com.example.comarleyaetheraudio.presentation.components.items.SongItem
import com.example.comarleyaetheraudio.ui.theme.SoundFlowIcons
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription

private data class SmartResult(val session: List<Long>, val matches: List<PossibleDuplicates>)

@Composable
fun SmartLibraryScreen(songs: List<Song>, favorites: List<Long>, seedId: Long?,
    onBack: () -> Unit, onPlaySession: (List<Song>) -> Unit, onPlaySong: (Song) -> Unit,
    listeningCounts: Map<Long, Int> = emptyMap(), listeningEnabled: Boolean = false,
    onListeningEnabled: (Boolean) -> Unit = {}, onClearHistory: () -> Unit = {},
    onPlayContext: (Song, List<Song>) -> Unit = { song, _ -> onPlaySong(song) }) {
    var style by remember { mutableStateOf(SessionStyle.VARIETY) }
    var result by remember(songs, favorites, seedId, style) { mutableStateOf<SmartResult?>(null) }
    val byId = remember(songs) { songs.associateBy { it.id } }
    LaunchedEffect(songs, favorites, seedId, style) {
        result = withContext(Dispatchers.Default) {
            val tracks = songs.map { SmartTrack(it.id, it.title, it.artist, it.album,
                it.genre, it.duration, it.contentUri.toString()) }
            SmartResult(suggestSession(tracks, favorites.toSet(), style, seedId), findPossibleDuplicates(tracks))
        }
    }
    val current = result
    var confirmClear by remember { mutableStateOf(false) }
    val mostPlayed = remember(listeningCounts, songs) {
        listeningCounts.entries.filter { it.key in byId }.sortedWith(
            compareByDescending<Map.Entry<Long, Int>> { it.value }.thenBy { it.key }).take(10)
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Volver") }
                Text("Biblioteca inteligente", Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall)
            }
        }
        item { SoundFlowPanel {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(SoundFlowIcons.SmartDJ, null, Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                Text("Tu próxima sesión", style = MaterialTheme.typography.headlineSmall)
                Text("Smart DJ combina tu biblioteca en sesiones de hasta 30 canciones y alterna artistas. Elige el punto de partida.")
                Text("Usa metadatos y favoritos; no analiza el sonido ni mezcla pistas.", style = MaterialTheme.typography.bodySmall)
            }
        } }
        item { LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(SessionStyle.entries) { option ->
            FilterChip(selected = style == option, onClick = { style = option }, label = {
                Text(when (option) { SessionStyle.VARIETY -> "Variedad"; SessionStyle.FAVORITES -> "Favoritos"; SessionStyle.DISCOVERY -> "Explorar no favoritas" })
            })
        } } }
        item { SoundFlowPanel {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Más escuchadas", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                    Switch(checked = listeningEnabled, onCheckedChange = onListeningEnabled,
                        modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = "Guardar historial de escuchas local" })
                }
                Text("Guardar escuchas solo en este dispositivo. Cuenta después de 30 segundos de avance reproducido, o la mitad de una canción más corta. Los saltos de posición no suman tiempo.",
                    style = MaterialTheme.typography.bodySmall)
                if (mostPlayed.isEmpty()) Text(if (listeningEnabled) "Tu ranking aparecerá al escuchar música." else "Activa el historial para comenzar. No se reconstruyen escuchas anteriores.")
                if (listeningCounts.isNotEmpty()) TextButton(onClick = { confirmClear = true }) { Text("Borrar historial de escuchas") }
            }
        } }
        items(mostPlayed, key = { "rank-${it.key}" }) { count ->
            byId[count.key]?.let { song ->
                ListItem(headlineContent = { Text(song.title) }, supportingContent = { Text("${song.artist} · ${count.value} escuchas") },
                    trailingContent = { IconButton(onClick = { onPlayContext(song, mostPlayed.mapNotNull { byId[it.key] }) }) { Icon(SoundFlowIcons.Play, "Reproducir ${song.title}") } })
            }
        }
        if (current == null) item { Text("Preparando sugerencias…") }
        else {
            item {
                Button(enabled = current.session.isNotEmpty(), onClick = {
                    val queue = current.session.mapNotNull(byId::get)
                    if (queue.isNotEmpty()) onPlaySession(queue)
                }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Reproducir sesión (${current.session.size})") }
                Text("Reemplaza la cola actual solo al pulsar Reproducir. No modifica tus playlists.", style = MaterialTheme.typography.bodySmall)
                if (current.session.isEmpty()) Text("No hay canciones para esta selección.")
            }
            items(current.session, key = { "session-$it" }) { id -> byId[id]?.let { song ->
                SongItem(song, onClick = { onPlayContext(song, current.session.mapNotNull(byId::get)) })
            } }
            item {
                HorizontalDivider()
                Text("Posibles duplicados (${current.matches.size} grupos)", style = MaterialTheme.typography.titleLarge)
                Text("Coincidencias de URI o de título, artista y duración (hasta 2 s de diferencia). Pueden ser versiones diferentes. No se borran ni fusionan canciones.")
                if (current.matches.isEmpty()) Text("No se encontraron coincidencias con estos criterios.")
            }
            if (current.matches.size > 50) item { Text("Se muestran los primeros 50 grupos para mantener la vista ligera.") }
            items(current.matches.take(50)) { match ->
                Card {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (match.kind == MatchKind.SAME_SOURCE) "Misma URI registrada" else "Metadatos similares", style = MaterialTheme.typography.titleSmall)
                        if (match.ids.size > 10) Text("Grupo de ${match.ids.size} canciones; se muestran las primeras 10.")
                        match.ids.take(10).forEach { id -> byId[id]?.let { song ->
                            Text("${song.title} · ${song.artist}")
                            Text(song.folderPath.ifBlank { song.folderUri.ifBlank { "Carpeta no informada" } }, style = MaterialTheme.typography.bodySmall)
                            TextButton(onClick = { onPlaySong(song) }) { Text("Escuchar esta versión") }
                        } }
                    }
                }
            }
        }
    }
    if (confirmClear) AlertDialog(onDismissRequest = { confirmClear = false }, title = { Text("Borrar historial") },
        text = { Text("Se borra el ranking de escuchas de este dispositivo. Tu música, favoritos y playlists se conservan.") },
        confirmButton = { TextButton(onClick = { onClearHistory(); confirmClear = false }) { Text("Borrar") } },
        dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancelar") } })
}
