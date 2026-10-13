package com.example.comarleyaetheraudio.presentation.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.presentation.components.PlaylistCover
import com.example.comarleyaetheraudio.presentation.components.SoundFlowPanel

@Composable
fun PlaylistMosaicCard(playlist: Playlist, onClick: () -> Unit) {
    SoundFlowPanel(Modifier.width(156.dp).clickable(onClick = onClick), subtle = true) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            PlaylistCover(playlist.songs, Modifier.fillMaxWidth().height(132.dp))
            Text(playlist.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${playlist.songs.size} canciones", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
