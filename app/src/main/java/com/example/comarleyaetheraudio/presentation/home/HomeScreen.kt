package com.example.comarleyaetheraudio.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.domain.model.AudioPlayerState
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.components.SongItem

enum class SmartType { FAVORITES, RECENT, RANDOM }

@Composable
fun HomeScreen(
    playerState: AudioPlayerState,
    allSongs: List<Song>,
    favoriteIds: List<Long>,
    onSongClick: (Song) -> Unit
) {
    var selectedSmartList by remember { mutableStateOf(SmartType.FAVORITES) }

    // Generador Dinámico de la Lista Inteligente
    val displaySongs = remember(allSongs, favoriteIds, selectedSmartList) {
        when (selectedSmartList) {
            SmartType.FAVORITES -> allSongs.filter { favoriteIds.contains(it.id) }
            SmartType.RECENT -> allSongs.sortedByDescending { it.id }.take(50)
            SmartType.RANDOM -> allSongs.shuffled().take(50)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Listas Inteligentes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Fila de Tarjetas (Scroll Horizontal)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmartCard(
                title = "Favoritos",
                icon = Icons.Default.Favorite,
                isSelected = selectedSmartList == SmartType.FAVORITES
            ) { selectedSmartList = SmartType.FAVORITES }

            SmartCard(
                title = "Recientes",
                icon = Icons.Default.NewReleases,
                isSelected = selectedSmartList == SmartType.RECENT
            ) { selectedSmartList = SmartType.RECENT }

            SmartCard(
                title = "Descubrir",
                icon = Icons.Default.Shuffle,
                isSelected = selectedSmartList == SmartType.RANDOM
            ) { selectedSmartList = SmartType.RANDOM }
        }

        Text(
            text = "Canciones (${displaySongs.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (displaySongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay canciones para mostrar en esta lista.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(displaySongs) { song ->
                    SongItem(
                        song = song,
                        onClick = { onSongClick(song) }
                    )
                }
            }
        }
    }
}

@Composable
fun SmartCard(title: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(32.dp)
                    .padding(bottom = 8.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}