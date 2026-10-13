package com.example.comarleyaetheraudio.presentation

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.presentation.settings.AppearanceScreen
import com.example.comarleyaetheraudio.ui.theme.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class AppearancePanelTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun followingSystemDisablesManualSwitchAndResetNeedsConfirmation() {
        var settings by mutableStateOf(AppearanceSettings())
        var resets = 0
        compose.setContent {
            ComarleyjesusaetheraudioTheme(settings.isDarkMode, settings.theme) {
                AppearanceScreen(settings, {}, {}, {}, {}, {},
                    onFollowSystem = { settings = settings.copy(followSystemTheme = it) },
                    onReset = { resets++ })
            }
        }
        compose.onNodeWithText("Seguir tema del sistema").performScrollTo().performClick()
        compose.onNodeWithText("Modo oscuro").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Restablecer apariencia").performScrollTo().assertHeightIsAtLeast(48.dp).performClick()
        compose.onNodeWithText("Cancelar").performClick()
        compose.runOnIdle { assertEquals(0, resets) }
        compose.onNodeWithText("Restablecer apariencia").performScrollTo().performClick()
        compose.onNodeWithText("Restablecer", substring = false).performClick()
        compose.runOnIdle { assertEquals(1, resets) }
    }

    @Test
    fun selectsPaletteAndChangesEffectsIndependently() {
        var settings by mutableStateOf(AppearanceSettings())
        compose.setContent {
            ComarleyjesusaetheraudioTheme(settings.isDarkMode, settings.theme) {
                AppearanceScreen(settings, {}, { settings = settings.copy(theme = it) },
                    { settings = settings.copy(isDarkMode = it) },
                    { settings = settings.copy(animationsEnabled = it) },
                    { settings = settings.copy(transparenciesEnabled = it) })
            }
        }
        compose.onNodeWithText("Cool").performScrollTo().performClick()
        compose.onNodeWithText("Animaciones").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(AppTheme.COOL, settings.theme)
            assertFalse(settings.animationsEnabled)
            assertTrue(settings.transparenciesEnabled)
        }
        compose.onNodeWithText("Transparencias").performScrollTo().performClick()
        compose.runOnIdle { assertFalse(settings.transparenciesEnabled) }
    }
}
