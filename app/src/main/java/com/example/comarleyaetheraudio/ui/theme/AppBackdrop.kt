package com.example.comarleyaetheraudio.ui.theme

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@Composable
fun screenBackgroundColor(): Color = if (LocalAppearance.current.backgroundUri != null &&
    LocalAppearance.current.transparenciesEnabled) Color.Transparent else MaterialTheme.colorScheme.background

/** One bounded image behind the app; opaque panels and a theme scrim protect text. */
@Composable
fun appBackdropModifier(): Modifier {
    val settings = LocalAppearance.current
    val color = MaterialTheme.colorScheme.background
    val uri = settings.backgroundUri
    if (uri == null || !settings.transparenciesEnabled) return Modifier.background(color)
    val context = LocalContext.current
    val request = remember(context, uri) { ImageRequest.Builder(context).data(Uri.parse(uri))
        .size(1080, 1920).crossfade(false).build() }
    val painter = rememberAsyncImagePainter(request)
    return Modifier.background(color).paint(painter, sizeToIntrinsics = false, contentScale = ContentScale.Crop)
        .background(color.copy(alpha = if (settings.isDarkMode) 0.87f else 0.92f))
}
