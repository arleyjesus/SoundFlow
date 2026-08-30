package com.example.comarleyaetheraudio.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.comarleyaetheraudio.data.local.CoverCacheManager
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import java.io.File

@Composable
fun PlaylistMosaicCard(playlist: Playlist, onClick: () -> Unit) {
    val sampleSongs = remember(playlist.songs) { playlist.songs.take(4) }

    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                if (sampleSongs.isEmpty()) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                } else if (sampleSongs.size < 4) {
                    SingleTileImage(song = sampleSongs.first())
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(modifier = Modifier.weight(1f)) {
                            GridTileItem(song = sampleSongs[0], modifier = Modifier.weight(1f))
                            GridTileItem(song = sampleSongs[1], modifier = Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.weight(1f)) {
                            GridTileItem(song = sampleSongs[2], modifier = Modifier.weight(1f))
                            GridTileItem(song = sampleSongs[3], modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = playlist.name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${playlist.songs.size} canciones",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GridTileItem(song: Song, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var coverFile by remember(song.id) { mutableStateOf<File?>(null) }

    LaunchedEffect(song.id) {
        coverFile = CoverCacheManager.getOrFetchCover(context, song.id, song.albumArtUri, song.path)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(0.5.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        if (coverFile != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(coverFile)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun SingleTileImage(song: Song) {
    val context = LocalContext.current
    var coverFile by remember(song.id) { mutableStateOf<File?>(null) }

    LaunchedEffect(song.id) {
        coverFile = CoverCacheManager.getOrFetchCover(context, song.id, song.albumArtUri, song.path)
    }

    if (coverFile != null) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(coverFile)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    } else {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
            contentDescription = null,
            modifier = Modifier.size(36.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
    }
}