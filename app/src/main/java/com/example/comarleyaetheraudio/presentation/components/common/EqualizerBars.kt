package com.example.comarleyaetheraudio.presentation.components.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.ui.theme.rememberReducedMotion

/** Indicador "ahora suena". Animado solo si [isPlaying] y las animaciones del sistema están activas. */
@Composable
fun EqualizerBars(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 20.dp
) {
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    val state by lifecycle.currentStateFlow.collectAsState()
    val animate = isPlaying && !rememberReducedMotion() && state.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)
    val factors: List<State<Float>> = if (animate) {
        val t = rememberInfiniteTransition(label = "eq")
        listOf(420, 560, 340).mapIndexed { i, ms ->
            t.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(ms, easing = FastOutSlowInEasing), RepeatMode.Reverse, initialStartOffset = androidx.compose.animation.core.StartOffset(i * 90)),
                label = "bar$i"
            )
        }
    } else {
        listOf(0.45f, 0.8f, 0.6f).map { mutableStateOf(it) }
    }

    Canvas(modifier.size(size)) {
        val gap = this.size.width * 0.12f
        val barW = (this.size.width - gap * 2) / 3f
        factors.forEachIndexed { i, f ->
            val h = this.size.height * f.value
            drawRoundRect(
                color = color,
                topLeft = Offset(i * (barW + gap), this.size.height - h),
                size = Size(barW, h),
                cornerRadius = CornerRadius(barW / 2f, barW / 2f)
            )
        }
    }
}
