package com.example.comarleyaetheraudio.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Choose the higher-contrast text color for these opaque theme surfaces. */
fun contrastingContent(background: Color): Color =
    if ((background.luminance() + 0.05f) / 0.05f >= 1.05f / (background.luminance() + 0.05f))
        Color.Black else Color.White

fun soundFlowColorScheme(theme: AppTheme, dark: Boolean): ColorScheme {
    val colors = getThemeColors(theme, dark)
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = colors.primary,
        onPrimary = contrastingContent(colors.primary),
        primaryContainer = colors.primaryContainer,
        onPrimaryContainer = contrastingContent(colors.primaryContainer),
        secondary = colors.primary,
        onSecondary = contrastingContent(colors.primary),
        secondaryContainer = colors.surfaceVariant,
        onSecondaryContainer = contrastingContent(colors.surfaceVariant),
        tertiary = colors.primary,
        onTertiary = contrastingContent(colors.primary),
        tertiaryContainer = colors.primaryContainer,
        onTertiaryContainer = contrastingContent(colors.primaryContainer),
        background = colors.background,
        onBackground = contrastingContent(colors.background),
        surface = colors.surface,
        onSurface = contrastingContent(colors.surface),
        surfaceVariant = colors.surfaceVariant,
        onSurfaceVariant = if (dark) Color(0xFFC2C2CC) else Color(0xFF53535F),
        outline = if (dark) Color(0xFF777781) else Color(0xFF757580),
        outlineVariant = if (dark) Color(0xFF34343B) else Color(0xFFDADAE3),
        surfaceTint = colors.primary,
        surfaceBright = colors.surfaceVariant,
        surfaceDim = colors.background,
        surfaceContainerLowest = colors.background,
        surfaceContainerLow = colors.surface,
        surfaceContainer = colors.surface,
        surfaceContainerHigh = colors.surfaceVariant,
        surfaceContainerHighest = colors.surfaceVariant
    )
}

@Composable
fun ComarleyjesusaetheraudioTheme(
    darkTheme: Boolean,
    appTheme: AppTheme = AppTheme.PRINCIPAL,
    content: @Composable () -> Unit
) {
    val scheme = remember(appTheme, darkTheme) { soundFlowColorScheme(appTheme, darkTheme) }
    MaterialTheme(colorScheme = scheme, typography = Typography,
        shapes = Shapes(extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(18.dp), large = RoundedCornerShape(24.dp), extraLarge = RoundedCornerShape(28.dp)),
        content = { CompositionLocalProvider(LocalContentColor provides scheme.onBackground) { content() } })
}
