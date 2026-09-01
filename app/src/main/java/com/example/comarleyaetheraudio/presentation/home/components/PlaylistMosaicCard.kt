package com.example.comarleyaetheraudio.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.comarleyaetheraudio.data.local.util.CoverCacheManager
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import java.io.File

@Composable
fun PlaylistMosaicCard(
    playlist: Playlist,
    onClick: () -> Unit
) {
    val sampleSongs = remember(playlist.songs) { playlist.songs.take(4) }

    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            if (sampleSongs.isEmpty()) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            } else if (sampleSongs.size < 4) {
                MosaicItemImage(song = sampleSongs.first(), modifier = Modifier.fillMaxSize())
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.weight(1f)) {
                        MosaicItemImage(song = sampleSongs[0], modifier = Modifier.weight(1f).fillMaxHeight())
                        MosaicItemImage(song = sampleSongs[1], modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                    Row(modifier = Modifier.weight(1f)) {
                        MosaicItemImage(song = sampleSongs[2], modifier = Modifier.weight(1f).fillMaxHeight())
                        MosaicItemImage(song = sampleSongs[3], modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = playlist.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${playlist.songs.size} canciones",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MosaicItemImage(
    song: Song,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var coverFile by remember(song.id) { mutableStateOf<File?>(null) }

    LaunchedEffect(song.id) {
        coverFile = CoverCacheManager.getOrFetchCover(
            context = context,
            songId = song.id,
            uri = song.albumArtUri,
            path = song.path
        )
    }

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(coverFile ?: song.albumArtUri)
            .crossfade(true)
            .build(),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}