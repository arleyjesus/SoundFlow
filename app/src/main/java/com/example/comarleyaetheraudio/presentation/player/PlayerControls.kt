package com.example.comarleyaetheraudio.presentation.player

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.domain.model.RepeatMode
import com.example.comarleyaetheraudio.ui.theme.Dimens
import com.example.comarleyaetheraudio.ui.theme.MotionTokens

/**
 * Fila de controles dentro de la zona del pulgar:
 * shuffle (48) · anterior (56) · play/pause (72) · siguiente (56) · repetir (48).
 */
@Composable
fun PlayerControls(
    isPlaying: Boolean,
    isShuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    accent: Color,
    onAccent: Color,
    onTogglePlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val inactive = MaterialTheme.colorScheme.onSurfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToggleIcon(
            icon = Icons.Rounded.Shuffle,
            label = "Aleatorio",
            stateText = if (isShuffleEnabled) "Activado" else "Desactivado",
            selected = isShuffleEnabled,
            accent = accent,
            inactive = inactive,
            enabled = enabled,
            onClick = onToggleShuffle
        )

        IconButton(onClick = onPrevious, enabled = enabled, modifier = Modifier.size(Dimens.SkipButton)) {
            Icon(Icons.Rounded.SkipPrevious, contentDescription = "Anterior", tint = onSurface, modifier = Modifier.size(36.dp))
        }

        FilledIconButton(
            onClick = onTogglePlayPause,
            enabled = enabled,
            modifier = Modifier.size(Dimens.PlayButton),
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = accent, contentColor = onAccent)
        ) {
            Crossfade(targetState = isPlaying, animationSpec = tween(if (com.example.comarleyaetheraudio.ui.theme.LocalAppearance.current.motionEnabled) MotionTokens.Quick else 0), label = "playPause") { playing ->
                Icon(
                    imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (playing) "Pausar" else "Reproducir",
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        IconButton(onClick = onNext, enabled = enabled, modifier = Modifier.size(Dimens.SkipButton)) {
            Icon(Icons.Rounded.SkipNext, contentDescription = "Siguiente", tint = onSurface, modifier = Modifier.size(36.dp))
        }

        ToggleIcon(
            icon = if (repeatMode == RepeatMode.ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
            label = "Repetir",
            stateText = when (repeatMode) {
                RepeatMode.OFF -> "Desactivado"
                RepeatMode.ALL -> "Repetir lista"
                RepeatMode.ONE -> "Repetir pista"
            },
            selected = repeatMode != RepeatMode.OFF,
            accent = accent,
            inactive = inactive,
            enabled = enabled,
            onClick = onCycleRepeat
        )
    }
}

@Composable
private fun ToggleIcon(
    icon: ImageVector,
    label: String,
    stateText: String,
    selected: Boolean,
    accent: Color,
    inactive: Color,
    onClick: () -> Unit,
    enabled: Boolean
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(Dimens.TouchTarget)
            .semantics { stateDescription = stateText }
    ) {
        Icon(icon, contentDescription = label, tint = if (selected) accent else inactive, modifier = Modifier.size(24.dp))
    }
}
