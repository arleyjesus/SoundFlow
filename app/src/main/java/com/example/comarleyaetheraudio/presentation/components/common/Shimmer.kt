package com.example.comarleyaetheraudio.presentation.components.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.ui.theme.Dimens
import com.example.comarleyaetheraudio.ui.theme.MotionTokens
import com.example.comarleyaetheraudio.ui.theme.rememberReducedMotion

/**
 * Relleno "shimmer" para estados de carga (MASTER.md §7).
 * El estado animado solo se lee en la fase de dibujo → no provoca recomposición.
 * Con animaciones del sistema desactivadas se muestra un bloque estático.
 */
fun Modifier.shimmer(): Modifier = composed {
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = lerp(base, MaterialTheme.colorScheme.onSurface, 0.14f)
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    val state by lifecycle.currentStateFlow.collectAsState()
    if (rememberReducedMotion() || !state.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
        background(base)
    } else {
        val transition = rememberInfiniteTransition(label = "shimmer")
        val progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(MotionTokens.ShimmerCycle, easing = LinearEasing), RepeatMode.Restart),
            label = "shimmerProgress"
        )
        drawBehind {
            val w = size.width.coerceAtLeast(1f)
            val x = -w + progress * w * 3f
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(base, highlight, base),
                    start = Offset(x, 0f),
                    end = Offset(x + w, size.height)
                )
            )
        }
    }
}

/** Fila fantasma con las mismas medidas que [com.example.comarleyaetheraudio.presentation.components.items.TrackRow] (CLS 0). */
@Composable
fun ShimmerTrackRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.TrackRowMinHeight)
            .padding(horizontal = Dimens.ScreenMargin, vertical = Dimens.Space8),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(Dimens.TrackCover)
                .clip(MaterialTheme.shapes.small)
                .shimmer()
        )
        Spacer(Modifier.width(Dimens.Space12))
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space8)) {
            Box(Modifier.fillMaxWidth(0.6f).height(14.dp).clip(MaterialTheme.shapes.extraSmall).shimmer())
            Box(Modifier.fillMaxWidth(0.4f).height(12.dp).clip(MaterialTheme.shapes.extraSmall).shimmer())
        }
    }
}

/** Lista fantasma; oculta a lectores de pantalla (se anuncia "Cargando" en el contenedor padre si hace falta). */
@Composable
fun ShimmerTrackList(count: Int = 8, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clearAndSetSemantics { }) {
        repeat(count) { ShimmerTrackRow() }
    }
}
