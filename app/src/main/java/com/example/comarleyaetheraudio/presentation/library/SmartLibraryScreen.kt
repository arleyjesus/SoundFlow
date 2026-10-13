package com.example.comarleyaetheraudio.presentation.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

private data class SmartResult(val session: List<Long>, val matches: List<PossibleDuplicates>)

@Composable
fun SmartLibraryScreen(songs: List<Song>, favorites: List<Long>, seedId: Long?,
    onBack: () -> Unit, onPlaySession: (List<Song>) -> Unit, onPlaySong: (Song) -> Unit) {
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
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Volver") }
                Text("Biblioteca inteligente", Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall)
            }
        }
        item { Text("Smart DJ local", style = MaterialTheme.typography.titleLarge) }
        item { Text("Sugerencias de hasta 30 canciones, alternando artistas. Variedad prioriza el género de la canción actual y favoritos; no analiza el sonido ni realiza mezclas.") }
        items(SessionStyle.entries) { option ->
            FilterChip(selected = style == option, onClick = { style = option }, label = {
                Text(when (option) { SessionStyle.VARIETY -> "Variedad"; SessionStyle.FAVORITES -> "Favoritos"; SessionStyle.DISCOVERY -> "Explorar no favoritas" })
            })
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
                ListItem(headlineContent = { Text(song.title) }, supportingContent = { Text(song.artist) })
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
}
