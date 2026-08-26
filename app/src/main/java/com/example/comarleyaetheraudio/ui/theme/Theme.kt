package com.example.comarleyaetheraudio.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ComarleyjesusaetheraudioTheme(
    darkTheme: Boolean,
    appTheme: AppTheme = AppTheme.PRINCIPAL,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.PRINCIPAL -> if (darkTheme) {
            darkColorScheme(
                primary = ElectricPurple,
                secondary = LightLavender,
                tertiary = SoftPink,
                background = Color.Black,
                surface = Color(0xFF121212),
                onBackground = Color.White,
                onSurface = Color.White
            )
        } else {
            lightColorScheme(
                primary = ElectricPurple,
                secondary = LightLavender,
                tertiary = SoftPink,
                background = Color(0xFFFBFBFE),
                surface = Color.White,
                onBackground = Color.Black,
                onSurface = Color.Black
            )
        }

        AppTheme.CLASSIC -> if (darkTheme) {
            darkColorScheme(
                primary = ClassicWarmRose,
                secondary = ClassicBurgundy,
                background = Color(0xFF1B1210),
                surface = ClassicCoffeeDark,
                onBackground = Color(0xFFF5EBE6),
                onSurface = Color(0xFFF5EBE6)
            )
        } else {
            lightColorScheme(
                primary = ClassicCoffeeDark,
                secondary = ClassicBurgundy,
                tertiary = ClassicWarmRose,
                background = Color(0xFFFAF6F0),
                surface = ClassicCoffeeLight,
                onBackground = Color(0xFF2C1D18),
                onSurface = Color(0xFF2C1D18)
            )
        }

        AppTheme.COOL -> if (darkTheme) {
            darkColorScheme(
                primary = CoolCyan,
                secondary = CoolEmerald,
                background = CoolNavyDark,
                surface = Color(0xFF112240),
                onBackground = Color.White,
                onSurface = Color.White
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF00838F),
                secondary = CoolEmerald,
                background = CoolBlueLight,
                surface = Color.White,
                onBackground = Color(0xFF00272B),
                onSurface = Color(0xFF00272B)
            )
        }

        AppTheme.SIMPLE -> if (darkTheme) {
            darkColorScheme(
                primary = Color.White,
                secondary = SimpleAccent,
                background = Color.Black,
                surface = SimpleNeutralDark,
                onBackground = Color.White,
                onSurface = Color.White
            )
        } else {
            lightColorScheme(
                primary = Color.Black,
                secondary = SimpleAccent,
                background = SimpleNeutralLight,
                surface = Color.White,
                onBackground = Color.Black,
                onSurface = Color.Black
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}