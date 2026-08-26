package com.example.comarleyaetheraudio.ui.theme

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette

object DynamicThemeExtractor {

    fun extractDominantColor(bitmap: Bitmap?, defaultColor: Color = ElectricPurple): Color {
        if (bitmap == null) return defaultColor
        val palette = Palette.from(bitmap).generate()
        val dominantSwatch = palette.dominantSwatch ?: palette.vibrantSwatch ?: palette.darkVibrantSwatch

        return dominantSwatch?.let { Color(it.rgb) } ?: defaultColor
    }
}