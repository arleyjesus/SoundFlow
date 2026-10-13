package com.example.comarleyaetheraudio.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.compositeOver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeContrastTest {
    @Test
    fun darkBackgroundIsPureBlackAcrossPalettes() {
        AppTheme.entries.forEach { assertEquals(Color.Black, soundFlowColorScheme(it, true).background) }
    }

    @Test
    fun glassPanelsKeepTextReadableOverSupportedSurfaces() {
        for (theme in AppTheme.entries) for (dark in listOf(false, true)) {
            val scheme = soundFlowColorScheme(theme, dark)
            for (color in panelColors(scheme, dark, true)) {
                for (underlay in listOf(scheme.background, scheme.surface)) {
                    val actual = color.compositeOver(underlay)
                    assertTrue("$theme / $dark / panel text", contrast(actual, scheme.onSurface) >= 4.5f)
                    assertTrue("$theme / $dark / secondary text", contrast(actual, scheme.onSurfaceVariant) >= 4.5f)
                }
            }
        }
    }

    @Test
    fun disablingTransparenciesUsesOpaquePanels() {
        for (theme in AppTheme.entries) for (dark in listOf(false, true)) {
            val scheme = soundFlowColorScheme(theme, dark)
            panelColors(scheme, dark, false).forEach { assertEquals(1f, it.alpha, 0f) }
        }
    }

    @Test
    fun contentColorsHaveReadableContrastAcrossEveryPaletteAndMode() {
        for (theme in AppTheme.entries) for (dark in listOf(false, true)) {
            val scheme = soundFlowColorScheme(theme, dark)
            val pairs = listOf(
                scheme.primary to scheme.onPrimary, scheme.primaryContainer to scheme.onPrimaryContainer,
                scheme.surface to scheme.onSurface, scheme.background to scheme.onBackground,
                scheme.surfaceVariant to scheme.onSurfaceVariant
            )
            pairs.forEach { (background, foreground) ->
                assertTrue("$theme / dark=$dark / $background", contrast(background, foreground) >= 4.5f)
            }
        }
    }

    private fun contrast(first: Color, second: Color): Float {
        val a = first.luminance()
        val b = second.luminance()
        return (maxOf(a, b) + 0.05f) / (minOf(a, b) + 0.05f)
    }
}
