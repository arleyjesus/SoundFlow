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
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.ui.theme.ElectricPurple
import com.example.comarleyaetheraudio.ui.theme.LightLavender

@Composable
fun AudioVisualizerView(
    isPlaying: Boolean,
    primaryColor: Color = ElectricPurple,
    secondaryColor: Color = LightLavender,
    barCount: Int = 30,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "VisualizerAnimation")

    val animPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PhaseAnim"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barWidth = width / (barCount * 1.8f)
        val gap = barWidth * 0.8f

        for (i in 0 until barCount) {
            val factor = if (isPlaying) {
                // Cálculo de altura oscilante basada en función seno para simular frecuencia de audio
                val wave = kotlin.math.sin((i.toFloat() * 0.5f) + (animPhase * kotlin.math.PI * 2)).toFloat()
                (kotlin.math.abs(wave) * 0.75f) + 0.25f
            } else {
                0.15f
            }

            val barHeight = height * factor
            val x = i * (barWidth + gap) + gap / 2
            val y = height - barHeight

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor, secondaryColor)
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}