package com.example.comarleyaetheraudio.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.comarleyaetheraudio.data.local.util.ArtistImageFetcher
import com.example.comarleyaetheraudio.data.local.util.CoverCacheManager
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.settings.LocalNetworkSettings
import com.example.comarleyaetheraudio.ui.theme.LocalAppearance
import java.io.File

/** Same fallback in Home and Library: unambiguous match, local cover, then initial. */
@Composable
fun ArtistPortrait(name: String, sample: Song?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val online = LocalNetworkSettings.current.artistImages
    var image by remember(name, sample?.id, online) { mutableStateOf<File?>(null) }
    LaunchedEffect(name, sample?.id, sample?.albumArtUri, sample?.path, online) {
        val remote = ArtistImageFetcher.getOrFetchArtistPicture(context, name, online)
        image = remote?.let(::File) ?: sample?.let {
            CoverCacheManager.getOrFetchCover(context, it.id, it.albumArtUri, it.path)
        }
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        if (image != null) AsyncImage(
            model = ImageRequest.Builder(context).data(image).crossfade(LocalAppearance.current.motionEnabled).build(),
            contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Text(name.trim().take(1).uppercase(java.util.Locale.ROOT),
            style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}
