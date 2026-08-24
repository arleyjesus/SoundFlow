package com.example.comarleyaetheraudio.presentation.home

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import com.example.comarleyaetheraudio.data.local.CoverCacheManager
import java.io.File
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.comarleyaetheraudio.data.local.ArtworkLoader
import com.example.comarleyaetheraudio.domain.model.AudioPlayerState
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.ui.theme.*

@Composable
fun HomeScreen(
    playerState: AudioPlayerState,
    recentSongs: List<Song>, // Recibe la lista ya ordenada
    favoriteIds: List<Long>,
    onSongClick: (Song) -> Unit,
    onFavoritesClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
    ) {
        item {
            Text(
                text = "SoundFlow",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1.5).sp
                ).merge(TextStyle(brush = BrandGradient)),
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        item {
            Text(
                text = "Recientes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 28.dp)
            ) {
                items(
                    items = recentSongs,
                    key = { song -> song.id },
                    contentType = { "recent_poster" }
                ) { song ->
                    RecentPosterCard(song = song, onClick = { onSongClick(song) })
                }
            }
        }

        item {
            Text(
                text = "Descubrir",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NeonSmartCard("Favoritos", Icons.Default.Favorite, PinkGradient) {
                    onFavoritesClick()
                }
                NeonSmartCard("Nuevas", Icons.Default.NewReleases, BrandGradient) {}
                NeonSmartCard("Aleatorio", Icons.Default.Shuffle, BrandGradient) {}
            }
        }
    }
}

@Composable
fun RecentPosterCard(song: Song, onClick: () -> Unit) {
    val context = LocalContext.current
    var coverFile by remember(song.id) { mutableStateOf<File?>(null) }

    // Cargar la carátula desde la caché en disco o procesarla en segundo plano
    LaunchedEffect(song.id) {
        coverFile = CoverCacheManager.getOrFetchCover(context, song.id, song.albumArtUri, song.path)
    }

    Box(
        modifier = Modifier
            .width(140.dp)
            .height(180.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = ElectricPurple,
                ambientColor = LightLavender
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E1E1E))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (coverFile != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(coverFile)
                    .crossfade(false) // Elimina el parpadeo al reciclar la vista en el LazyRow
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = LightLavender,
                modifier = Modifier.size(48.dp)
            )
        }

        // Sombra inferior degradada para destacar el texto sobre la carátula
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))))
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.labelSmall,
                color = LightLavender,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun NeonSmartCard(title: String, icon: ImageVector, gradient: Brush, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .height(64.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}