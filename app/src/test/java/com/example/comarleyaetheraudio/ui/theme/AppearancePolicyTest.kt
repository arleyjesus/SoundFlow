package com.example.comarleyaetheraudio.ui.theme

import org.junit.Assert.*
import org.junit.Test

class AppearancePolicyTest {
    @Test
    fun explicitModeIgnoresSystemAndFollowingModeTracksIt() {
        for (manual in listOf(false, true)) for (system in listOf(false, true)) {
            assertEquals(manual, AppearanceSettings(isDarkMode = manual).darkMode(system))
            assertEquals(system, AppearanceSettings(isDarkMode = manual, followSystemTheme = true).darkMode(system))
        }
    }

    @Test
    fun motionRequiresUserAndPlatformPermissionWithoutMutatingPreference() {
        for (user in listOf(false, true)) for (platform in listOf(false, true)) {
            val settings = AppearanceSettings(animationsEnabled = user, systemAnimationsEnabled = platform)
            assertEquals(user && platform, settings.motionEnabled)
            assertEquals(user, settings.animationsEnabled)
        }
    }
}
