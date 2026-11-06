package com.example.comarleyaetheraudio.presentation.components.items

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.components.common.LocalNowPlaying

@Composable
fun SongItem(song: Song, onClick: () -> Unit, onMoreOptionsClick: (() -> Unit)? = null,
             modifier: Modifier = Modifier, isActive: Boolean = false) {
    val now = LocalNowPlaying.current
    TrackRow(song, isCurrent = isActive || now.songId == song.id,
        isPlaying = now.songId == song.id && now.isPlaying, onClick = onClick,
        modifier = modifier, onMoreOptionsClick = onMoreOptionsClick)
}
