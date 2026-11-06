package com.example.comarleyaetheraudio.presentation.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import java.util.Locale

/**
 * Barra de progreso con zona táctil ≥ 48 dp (el Slider nativo ya la respeta; antes se forzaba a 12 dp).
 * Muestra la posición del arrastre en tiempo real y confirma el seek al soltar.
 */
@Composable
fun SeekBar(
    positionMs: Long,
    durationMs: Long,
    accent: Color,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trackKey: Long? = null
) {
    var dragging by remember(trackKey) { mutableStateOf<Float?>(null) }
    val current = dragging ?: positionMs.toFloat()
    val total = durationMs.coerceAtLeast(1L).toFloat()

    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            enabled = enabled,
            value = current.coerceIn(0f, total),
            onValueChange = { dragging = it },
            onValueChangeFinished = {
                dragging?.let { onSeekTo(it.toLong()) }
                dragging = null
            },
            valueRange = 0f..total,
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .semantics { stateDescription = "${formatSeekTime(current.toLong())} de ${formatSeekTime(durationMs)}" }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .clearAndSetSemantics { },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatSeekTime(current.toLong()), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatSeekTime(durationMs), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

internal fun formatSeekTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    return String.format(Locale.US, "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
}
