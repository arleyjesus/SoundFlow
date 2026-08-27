package com.example.comarleyaetheraudio.ui.theme

import androidx.compose.ui.graphics.Color

enum class AppTheme(val displayName: String) {
    PRINCIPAL("Principal"),
    CLASSIC("Clásico"),
    COOL("Cool"),
    SIMPLE("Minimalista")
}

data class ThemeColors(
    val primary: Color,
    val primaryContainer: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color
)

fun getThemeColors(theme: AppTheme, isDark: Boolean): ThemeColors {
    return when (theme) {
        AppTheme.PRINCIPAL -> ThemeColors(
            primary = if (isDark) Color(0xFFD0BCFF) else Color(0xFF6750A4),
            primaryContainer = if (isDark) Color(0xFF4F378B) else Color(0xFFEADDFF),
            background = if (isDark) Color(0xFF141218) else Color(0xFFFEF7FF),
            surface = if (isDark) Color(0xFF211F26) else Color(0xFFF7F2FA),
            surfaceVariant = if (isDark) Color(0xFF49454F) else Color(0xFFE7E0EC)
        )
        AppTheme.CLASSIC -> ThemeColors(
            primary = if (isDark) Color(0xFFFFB4AB) else Color(0xFF904A42),
            primaryContainer = if (isDark) Color(0xFF73332C) else Color(0xFFFFDAD6),
            background = if (isDark) Color(0xFF1A1110) else Color(0xFFFFF8F7),
            surface = if (isDark) Color(0xFF271D1C) else Color(0xFFFCEAE8),
            surfaceVariant = if (isDark) Color(0xFF534341) else Color(0xFFF5DDDA)
        )
        AppTheme.COOL -> ThemeColors(
            primary = if (isDark) Color(0xFF80D6FF) else Color(0xFF00658F),
            primaryContainer = if (isDark) Color(0xFF004C6D) else Color(0xFFC6E7FF),
            background = if (isDark) Color(0xFF0F1417) else Color(0xFFF6FAFE),
            surface = if (isDark) Color(0xFF1B2024) else Color(0xFFEDF4FA),
            surfaceVariant = if (isDark) Color(0xFF40484E) else Color(0xFFDCE3E9)
        )
        AppTheme.SIMPLE -> ThemeColors(
            primary = if (isDark) Color(0xFFC6C6C6) else Color(0xFF464646),
            primaryContainer = if (isDark) Color(0xFF303030) else Color(0xFFE2E2E2),
            background = if (isDark) Color(0xFF111111) else Color(0xFFF9F9F9),
            surface = if (isDark) Color(0xFF1C1C1C) else Color(0xFFF0F0F0),
            surfaceVariant = if (isDark) Color(0xFF373737) else Color(0xFFE0E0E0)
        )
    }
}