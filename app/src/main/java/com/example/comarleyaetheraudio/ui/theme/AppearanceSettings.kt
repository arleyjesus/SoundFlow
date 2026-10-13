package com.example.comarleyaetheraudio.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

data class AppearanceSettings(
    val isDarkMode: Boolean = true,
    val theme: AppTheme = AppTheme.PRINCIPAL,
    val animationsEnabled: Boolean = true,
    val transparenciesEnabled: Boolean = true,
    val followSystemTheme: Boolean = false,
    // Runtime capability, never persisted over the user's preference.
    val systemAnimationsEnabled: Boolean = true,
    val backgroundUri: String? = null
) {
    val motionEnabled: Boolean get() = animationsEnabled && systemAnimationsEnabled
    fun darkMode(systemDark: Boolean): Boolean = if (followSystemTheme) systemDark else isDarkMode
}

val LocalAppearance = staticCompositionLocalOf { AppearanceSettings() }
