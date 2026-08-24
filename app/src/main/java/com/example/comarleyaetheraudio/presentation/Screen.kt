package com.example.comarleyaetheraudio.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Inicio", Icons.Default.Home)
    object Songs : Screen("songs", "Canciones", Icons.Default.MusicNote)
    object Folders : Screen("folders", "Carpetas", Icons.Default.Folder)
    object Playlists : Screen("playlists", "Listas", Icons.Default.QueueMusic)
    object Settings : Screen("settings", "Ajustes", Icons.Default.Settings)

    // NUEVA RUTA PARA DETALLE DE CARPETA
    object FolderDetail : Screen("folder_detail/{folderPath}", "Detalle", Icons.Default.Folder) {
        fun createRoute(folderPath: String) = "folder_detail/${android.net.Uri.encode(folderPath)}"
    }

    object AudioFx : Screen("audio_fx", "Ecualizador", Icons.Default.Equalizer)
}