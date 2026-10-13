package com.example.comarleyaetheraudio.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.comarleyaetheraudio.data.local.util.CoverCacheManager
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.ui.theme.SoundFlowIcons
import java.io.File

@Composable
fun PlaylistCover(songs: List<Song>, modifier: Modifier = Modifier) {
    val tiles = remember(songs) { songs.take(4) }
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primaryContainer),
        verticalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(if (tiles.size <= 1) 1 else 2) { row ->
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(if (tiles.size <= 1) 1 else 2) { column ->
                    PlaylistTile(tiles.getOrNull(row * 2 + column), Modifier.weight(1f).fillMaxHeight())
                }
            }
        }
    }
}

@Composable
private fun PlaylistTile(song: Song?, modifier: Modifier) {
    val context = LocalContext.current
    var file by remember(song?.id, song?.albumArtUri) { mutableStateOf<File?>(null) }
    LaunchedEffect(song?.id, song?.albumArtUri, song?.path) {
        file = song?.let { CoverCacheManager.getOrFetchCover(context, it.id, it.albumArtUri, it.path) }
    }
    Box(modifier, contentAlignment = androidx.compose.ui.Alignment.Center) {
        if (file != null) AsyncImage(file, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Icon(SoundFlowIcons.Playlist, null, tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(6.dp).size(24.dp))
    }
}
