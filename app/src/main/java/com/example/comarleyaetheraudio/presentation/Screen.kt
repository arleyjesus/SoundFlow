package com.example.comarleyaetheraudio.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Representa las pantallas del menú de navegación inferior.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Inicio", Icons.Default.PlayCircle)
    object Songs : Screen("songs", "Canciones", Icons.Default.MusicNote)
    object Folders : Screen("folders", "Carpetas", Icons.Default.Folder)
    object Settings : Screen("settings", "Ajustes", Icons.Default.Settings)
}