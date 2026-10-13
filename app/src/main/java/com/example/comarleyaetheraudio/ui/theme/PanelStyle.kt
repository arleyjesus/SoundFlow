package com.example.comarleyaetheraudio.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/** Static colors: no screen capture, bitmap allocation or animated blur. */
fun panelColors(scheme: ColorScheme, dark: Boolean, translucent: Boolean): List<Color> {
    if (!translucent) return listOf(scheme.surface, scheme.surface)
    return listOf(
        lerp(scheme.surface, scheme.primary, if (dark) 0.16f else 0.06f).copy(alpha = 0.97f),
        scheme.surface.copy(alpha = 0.95f),
        lerp(scheme.surface, scheme.primaryContainer, 0.22f).copy(alpha = 0.97f)
    )
}
