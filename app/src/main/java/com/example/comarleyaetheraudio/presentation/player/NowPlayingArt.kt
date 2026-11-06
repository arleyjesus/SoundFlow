package com.example.comarleyaetheraudio.presentation.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

/** Carátula del reproductor: imagen decodificada o placeholder tonal, con sombra teñida por el acento. */
@Composable
fun NowPlayingArt(
    artwork: PlayerArtwork?,
    accent: Color,
    shape: Shape,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    elevationDp: Int = 20
) {
    Box(
        modifier = modifier
            .shadow(elevationDp.dp, shape, spotColor = accent, ambientColor = accent)
            .clip(shape)
    ) {
        if (artwork != null) {
            Image(
                bitmap = artwork.art,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceVariant) {
                Icon(
                    Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(if (shape == CircleShape) 24.dp else 52.dp)
                )
            }
        }
    }
}
