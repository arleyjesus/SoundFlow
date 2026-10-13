package com.example.comarleyaetheraudio.presentation.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.comarleyaetheraudio.ui.theme.SoundFlowIcons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.domain.model.AudioPlayerState
import com.example.comarleyaetheraudio.presentation.components.SoundFlowPanel

@Composable
internal fun PlaybackStatusBanner(state: AudioPlayerState, onRetry: () -> Unit) {
    // Short seeks and local track changes should not flash a banner or resize the player.
    // Key the delay to preparation only, not the progress updates arriving every 500 ms.
    val showBuffering by produceState(false, state.isBuffering, state.isConnecting, state.errorMessage) {
        value = false
        if (state.isBuffering && !state.isConnecting && state.errorMessage == null) {
            delay(450)
            value = true
        }
    }
    val message = state.errorMessage ?: when {
        state.isConnecting -> "Conectando reproductor…"
        state.isBuffering && showBuffering -> "Preparando audio…"
        else -> return
    }
    Surface(
        color = if (state.errorMessage != null) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state.errorMessage == null) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                }
                Text(message, style = MaterialTheme.typography.bodySmall)
            }
            if (state.errorMessage != null) {
                TextButton(onClick = onRetry, enabled = !state.isConnecting) { Text("Reintentar") }
            }
        }
    }
}

@Composable
internal fun MiniPlayerCard(
    state: AudioPlayerState,
    onExpand: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onRetry: () -> Unit,
    artwork: @Composable () -> Unit
) {
    val song = state.currentSong ?: return
    SoundFlowPanel(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
            PlaybackStatusBanner(state, onRetry)
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clickable(onClickLabel = "Abrir reproductor", onClick = onExpand)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                artwork()
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(song.title, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artist, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                OutlinedIconButton(onClick = onTogglePlayPause, enabled = !state.isConnecting,
                    shape = CircleShape, border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    colors = IconButtonDefaults.outlinedIconButtonColors(containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary), modifier = Modifier.size(48.dp)) {
                    Icon(if (state.isPlaying) SoundFlowIcons.Pause else SoundFlowIcons.Play,
                        contentDescription = if (state.isPlaying) "Pausar" else "Reproducir")
                }
                IconButton(onClick = onNext, enabled = !state.isConnecting, modifier = Modifier.size(48.dp)) {
                    Icon(SoundFlowIcons.Next, contentDescription = "Siguiente")
                }
            }
            val progress = if (state.duration > 0)
                (state.currentPosition.toFloat() / state.duration).coerceIn(0f, 1f) else 0f
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(3.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant)
    }
}
