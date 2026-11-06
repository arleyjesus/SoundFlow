package com.example.comarleyaetheraudio.presentation.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.comarleyaetheraudio.data.local.util.CoverCacheManager
import com.example.comarleyaetheraudio.domain.model.Song
import java.io.File

/**
 * Carátula de una pista: shimmer mientras se resuelve la caché → imagen con crossfade → placeholder tonal.
 * Reemplaza las 3 copias de la lógica CoverCacheManager+AsyncImage (lista, mini player, etc.).
 *
 * @param contentDescription `null` si la carátula es decorativa (el título ya está en el texto adyacente).
 */
@Composable
fun CoverImage(
    song: Song,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
    contentDescription: String? = null,
    crossfade: Boolean = true
) {
    val context = LocalContext.current
    var resolved by remember(song.id, song.albumArtUri, song.path) { mutableStateOf(false) }
    var cachedFile by remember(song.id, song.albumArtUri, song.path) { mutableStateOf<File?>(null) }

    LaunchedEffect(song.id, song.albumArtUri, song.path) {
        cachedFile = CoverCacheManager.getOrFetchCover(context, song.id, song.albumArtUri, song.path)
        resolved = true
    }

    val model: Any? = cachedFile ?: song.albumArtUri

    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        when {
            model != null -> AsyncImage(
                model = ImageRequest.Builder(context).data(model).crossfade(crossfade && com.example.comarleyaetheraudio.ui.theme.LocalAppearance.current.motionEnabled).build(),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            !resolved -> Box(Modifier.fillMaxSize().shimmer())
            else -> Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
