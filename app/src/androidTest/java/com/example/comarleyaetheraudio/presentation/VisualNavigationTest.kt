package com.example.comarleyaetheraudio.presentation

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.comarleyaetheraudio.presentation.profile.ProfileScreen
import com.example.comarleyaetheraudio.ui.theme.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class VisualNavigationTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun profileActionsStillReachAppearanceAudioAndSettingsWithOpaquePanels() {
        var appearance = 0
        var audio = 0
        var settings = 0
        val preferences = AppearanceSettings(transparenciesEnabled = false, animationsEnabled = false)
        compose.setContent {
            CompositionLocalProvider(LocalAppearance provides preferences) {
                ComarleyjesusaetheraudioTheme(true) {
                    ProfileScreen(AppTheme.PRINCIPAL, true, emptyList(), {}, {}, {}, {},
                        { settings++ }, { audio++ }, onNavigateToAppearance = { appearance++ })
                }
            }
        }
        compose.onNodeWithText("Apariencia").performScrollTo().performClick()
        compose.onNodeWithText("Ecualizador & DSP").performScrollTo().performClick()
        compose.onNodeWithText("Ajustes").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, appearance); assertEquals(1, audio); assertEquals(1, settings) }
    }
}
