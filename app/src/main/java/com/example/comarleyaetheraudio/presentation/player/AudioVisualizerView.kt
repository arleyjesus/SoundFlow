package com.example.comarleyaetheraudio.presentation.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
    // Remove the decorative loop from composition while the screen is inactive.
    val animPhase = if (isPlaying && inForeground && LocalAppearance.current.motionEnabled) animatedPhase() else 0f
    val barBrush = remember(primaryColor, secondaryColor) {
        Brush.verticalGradient(colors = listOf(primaryColor, secondaryColor))
    }
    val ribbon = remember { Path() }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val count = barCount.coerceAtLeast(1)
        val barWidth = width / (count * 1.8f)
        val gap = barWidth * 0.8f
        ribbon.reset()
        for (point in 0..64) {
            val x = width * point / 64f
            val envelope = kotlin.math.sin(point / 64f * kotlin.math.PI).toFloat()
            val displacement = if (isPlaying) kotlin.math.sin(point * 0.19 + animPhase * kotlin.math.PI * 2).toFloat() else 0f
            val y = height / 2f + displacement * envelope * height * 0.12f
            if (point == 0) ribbon.moveTo(x, y) else ribbon.lineTo(x, y)
        }
        drawPath(ribbon, primaryColor.copy(alpha = 0.3f), style = Stroke(width = 1.dp.toPx()))

        for (i in 0 until count) {
            val factor = if (isPlaying) {
                // Decorative animation: this is not a measurement of the audio signal.
                val position = (i + 0.5f) / count
                val envelope = kotlin.math.sin(position * kotlin.math.PI).toFloat()
                val wave = kotlin.math.sin(i * 0.63 + animPhase * kotlin.math.PI * 2).toFloat()
                val detail = kotlin.math.cos(i * 1.17 - animPhase * kotlin.math.PI * 2).toFloat()
                0.12f + envelope * (0.28f + kotlin.math.abs(wave * 0.38f + detail * 0.18f))
            } else {
                0.15f
            }

            val barHeight = height * factor
            val x = i * (barWidth + gap) + gap / 2
            val y = (height - barHeight) / 2f

            drawRoundRect(
                brush = barBrush,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}

@Composable
private fun animatedPhase(): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "VisualizerAnimation")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PhaseAnim"
    )

    return phase
}
