package com.example.comarleyaetheraudio.ui.theme

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** Duraciones de movimiento (MASTER.md §6). */
object MotionTokens {
    const val Press = 100
    const val Quick = 150
    const val Standard = 300
    const val Emphasized = 400
    const val ShimmerCycle = 1200
}

/** `true` si el usuario desactivó las animaciones del sistema. */
@Composable
fun rememberReducedMotion(): Boolean = !LocalAppearance.current.motionEnabled
