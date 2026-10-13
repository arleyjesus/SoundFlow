package com.example.comarleyaetheraudio.ui.theme

import android.animation.ValueAnimator
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/** Resolve platform policy without overwriting the persisted user choices. */
@Composable
fun platformAppearance(preferences: AppearanceSettings): AppearanceSettings {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val systemDark = isSystemInDarkTheme()
    fun animationsAllowed(): Boolean = if (Build.VERSION.SDK_INT >= 26) {
        ValueAnimator.areAnimatorsEnabled()
    } else {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }
    var systemAnimations by remember { mutableStateOf(animationsAllowed()) }
    DisposableEffect(owner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) systemAnimations = animationsAllowed()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    return preferences.copy(isDarkMode = preferences.darkMode(systemDark), systemAnimationsEnabled = systemAnimations)
}
