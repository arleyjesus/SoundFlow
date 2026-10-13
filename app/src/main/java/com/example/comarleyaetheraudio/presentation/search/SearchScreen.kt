package com.example.comarleyaetheraudio.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.components.items.SongItem
import com.example.comarleyaetheraudio.domain.library.searchLibrary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.comarleyaetheraudio.presentation.library.components.ArtistDetailBottomSheet
import com.example.comarleyaetheraudio.presentation.components.SoundFlowPanel

private enum class SearchCategory(val label: String) { ALL("Todo"), SONGS("Canciones"), ARTISTS("Artistas"), ALBUMS("Álbumes") }
private data class SearchGroup(val title: String, val subtitle: String, val songs: List<Song>)
private data class SearchSnapshot(val query: String, val source: List<Song>, val category: SearchCategory,
    val matches: List<Song>, val groups: List<SearchGroup>)

@Composable
fun SearchScreen(
    songs: List<Song>,
    isDarkMode: Boolean = false,
    onSongClick: (Song) -> Unit,
    onPlayContext: (Song, List<Song>) -> Unit = { song, _ -> onSongClick(song) }
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(SearchCategory.ALL) }
    var selectedGroup by remember { mutableStateOf<SearchGroup?>(null) }
    val focusManager = LocalFocusManager.current

    val backgroundColor = MaterialTheme.colorScheme.background
    val cardBgColor = MaterialTheme.colorScheme.surface

    val snapshot by produceState<SearchSnapshot?>(null, searchQuery, songs, category) {
        value = null
        val text = searchQuery.trim()
        value = withContext(Dispatchers.Default) {
            val matches = searchLibrary(songs, text) { song, query ->
                when (category) {
                    SearchCategory.SONGS -> song.title.contains(query, true)
                    SearchCategory.ARTISTS -> song.artist.contains(query, true)
                    SearchCategory.ALBUMS -> song.album.contains(query, true)
                    SearchCategory.ALL -> song.title.contains(query, true) || song.artist.contains(query, true) || song.album.contains(query, true)
                }
            }
            val groups = when (category) {
                SearchCategory.ARTISTS -> matches.groupBy { it.artist }.map { (artist, tracks) -> SearchGroup(artist, "${tracks.size} canciones", tracks) }
                SearchCategory.ALBUMS -> matches.groupBy { it.artist to it.album }.map { (key, tracks) -> SearchGroup(key.second, "${key.first} · ${tracks.size} canciones", tracks) }
                else -> emptyList()
            }
            SearchSnapshot(text, songs, category, matches, groups)
        }
    }
    // Match produceState's key equality, including an equivalent immutable list instance.
    val current = snapshot?.takeIf { it.query == searchQuery.trim() && it.source == songs && it.category == category }
    val filteredSongs = current?.matches.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Text(
            text = "Buscar",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 12.dp)
        )

        SoundFlowPanel(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Canciones, artistas o álbumes...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Rounded.Clear, contentDescription = "Limpiar Búsqueda")
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = cardBgColor,
                unfocusedContainerColor = cardBgColor,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent
            )
        )
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(SearchCategory.entries) { option ->
                FilterChip(selected = category == option, onClick = { category = option }, label = { Text(option.label) })
            }
        }

        if (searchQuery.isBlank()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Escribe para empezar a buscar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (current == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(32.dp))
            }
        } else if (filteredSongs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.SearchOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Sin coincidencias para \"$searchQuery\"", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item { Text("${filteredSongs.size} canciones", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(12.dp)) }
                if (category == SearchCategory.ARTISTS || category == SearchCategory.ALBUMS) {
                    items(current.groups) { group ->
                        ListItem(modifier = Modifier.clickable { selectedGroup = group },
                            headlineContent = { Text(group.title) }, supportingContent = { Text(group.subtitle) })
                    }
                } else {
                items(filteredSongs, key = { it.id }) { song ->
                    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
                        SongItem(
                            song = song,
                            onClick = {
                                focusManager.clearFocus()
                                onPlayContext(song, filteredSongs)
                            }
                        )
                    }
                }
                }
            }
        }
    }
    selectedGroup?.let { group ->
        ArtistDetailBottomSheet(artistName = group.title, artistSongs = group.songs,
            showArtistPortrait = category == SearchCategory.ARTISTS,
            onDismiss = { selectedGroup = null }, onSongClick = { onPlayContext(it, group.songs) })
    }
}
