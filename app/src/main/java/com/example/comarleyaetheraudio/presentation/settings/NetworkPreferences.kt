package com.example.comarleyaetheraudio.presentation.settings

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.comarleyaetheraudio.domain.model.NetworkSettings

// No remote lookup before the persisted preferences are available.
val LocalNetworkSettings = staticCompositionLocalOf { NetworkSettings(false, false, false) }
