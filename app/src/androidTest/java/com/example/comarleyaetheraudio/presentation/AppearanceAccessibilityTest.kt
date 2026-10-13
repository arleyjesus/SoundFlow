package com.example.comarleyaetheraudio.presentation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import com.example.comarleyaetheraudio.presentation.settings.AppearanceScreen
import com.example.comarleyaetheraudio.ui.theme.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** Checks reachable controls at large font scale in all eight palette/mode pairs. */
@RunWith(Parameterized::class)
class AppearanceAccessibilityTest(private val theme: AppTheme, private val dark: Boolean) {
    @get:Rule val compose = createComposeRule()

    @Test
    fun largeTextKeepsAppearanceActionsReachableWithEffectsDisabled() {
        var back = 0
        var reset = 0
        val settings = AppearanceSettings(isDarkMode = dark, theme = theme,
            animationsEnabled = false, transparenciesEnabled = false)
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalAppearance provides settings,
                LocalDensity provides Density(density.density, fontScale = 2f)) {
                ComarleyjesusaetheraudioTheme(dark, theme) {
                    AppearanceScreen(settings, { back++ }, {}, {}, {}, {}, onReset = { reset++ })
                }
            }
        }
        compose.onNodeWithContentDescription("Volver").performClick()
        compose.onNodeWithText("Restablecer apariencia").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithText("Restablecer", substring = false).assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, back); assertEquals(1, reset) }
    }

    companion object {
        @JvmStatic @Parameterized.Parameters(name = "{0}, dark={1}")
        fun cases(): List<Array<Any>> = AppTheme.entries.flatMap { theme ->
            listOf(false, true).map { dark -> arrayOf<Any>(theme, dark) }
        }
    }
}
