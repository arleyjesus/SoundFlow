package com.example.comarleyaetheraudio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Roles de color propios de SoundFlow que Material 3 no define (MASTER.md §3). */
data class SoundFlowColors(
    val hiRes: Color,
    val onHiRes: Color
)

val LocalSoundFlowColors = staticCompositionLocalOf {
    SoundFlowColors(hiRes = Color(0xFFFFD700), onHiRes = Color(0xFF1A1400))
}

/** Acceso: `MaterialTheme.soundFlow.hiRes`. */
val MaterialTheme.soundFlow: SoundFlowColors
    @Composable
    @ReadOnlyComposable
    get() = LocalSoundFlowColors.current
