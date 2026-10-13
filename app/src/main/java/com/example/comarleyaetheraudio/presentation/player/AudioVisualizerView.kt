package com.example.comarleyaetheraudio.presentation.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import kotlin.math.PI
import kotlin.math.sin
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.comarleyaetheraudio.ui.theme.ElectricPurple
import com.example.comarleyaetheraudio.ui.theme.LightLavender
import com.example.comarleyaetheraudio.ui.theme.LocalAppearance

@Composable
fun AudioVisualizerView(
    isPlaying: Boolean,
    primaryColor: Color = ElectricPurple,
    secondaryColor: Color = LightLavender,
    barCount: Int = 48,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var inForeground by remember(lifecycle) {
        mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, _ ->
            inForeground = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    // Read phase in the draw scope: animation invalidates drawing, not composition/layout.
    val stillPhase = remember { mutableFloatStateOf(0f) }
    val phase = if (isPlaying && inForeground && LocalAppearance.current.motionEnabled) animatedPhase() else stillPhase
    val waveBrush = remember(primaryColor, secondaryColor) {
        Brush.horizontalGradient(listOf(primaryColor.copy(alpha = 0.35f), secondaryColor,
            primaryColor, secondaryColor.copy(alpha = 0.35f)))
    }
    val waves = remember { List(3) { Path() } }
    Canvas(modifier = modifier) {
        val time = phase.value * PI * 2
        val samples = barCount.coerceIn(24, 96)
        // Decorative flowing ribbons, not a sampled audio signal. No blur or bitmap per frame.
        waves.forEachIndexed { layer, path ->
            path.reset()
            for (point in 0..samples) {
                val position = point.toFloat() / samples
                val envelope = sin(position * PI).toFloat()
                val amplitude = if (isPlaying) 0.30f - layer * 0.06f else 0.045f
                val carrier = sin(position * PI * (4 + layer) - time + layer * 0.85)
                val detail = sin(position * PI * 2 + time) * 0.18
                val x = position * size.width
                val y = size.height * (0.5f + (carrier + detail).toFloat() * envelope * amplitude)
                if (point == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, waveBrush, alpha = if (layer == 0) 1f else 0.28f,
                style = Stroke(width = (if (layer == 0) 2.2.dp else 1.2.dp).toPx(), cap = StrokeCap.Round))
        }
    }
}

@Composable
private fun animatedPhase(): State<Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "VisualizerAnimation")
    return infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PhaseAnim"
    )
}
