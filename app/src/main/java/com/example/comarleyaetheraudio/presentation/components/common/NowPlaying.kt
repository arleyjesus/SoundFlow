package com.example.comarleyaetheraudio.presentation.components.common

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf

/**
 * Estado mínimo "qué suena". Se provee una sola vez en la raíz de la app y lo consumen
 * todas las filas de pista, sin tener que enhebrar `playerState` por cada pantalla.
 * Solo cambia al cambiar de pista o de play/pause (no con cada tick de progreso).
 */
@Immutable
data class NowPlayingInfo(val songId: Long? = null, val isPlaying: Boolean = false)

val LocalNowPlaying = compositionLocalOf { NowPlayingInfo() }
