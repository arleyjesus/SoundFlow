package com.example.comarleyaetheraudio.presentation.components.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.ui.theme.soundFlow

/** Etiqueta "HI-RES 96" (kHz si se conoce). Renderiza nada si la pista no es Hi-Res. */
@Composable
fun HiResBadge(song: Song, modifier: Modifier = Modifier) {
    if (!song.isHiRes) return
    val khz = formatKhz(song.sampleRate)
    val label = if (khz != null) "HI-RES $khz" else "HI-RES"
    val spoken = if (khz != null) "Audio de alta resolución, $khz kilohercios" else "Audio de alta resolución"

    Surface(
        modifier = modifier.semantics { contentDescription = spoken },
        color = MaterialTheme.soundFlow.hiRes,
        contentColor = MaterialTheme.soundFlow.onHiRes,
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}

private fun formatKhz(sampleRateHz: Int): String? {
    if (sampleRateHz <= 0) return null
    val khz = sampleRateHz / 1000f
    return if (khz % 1f == 0f) khz.toInt().toString() else String.format(java.util.Locale.US, "%.1f", khz)
}
