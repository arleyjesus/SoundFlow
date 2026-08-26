package com.example.comarleyaetheraudio.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Inicio", Icons.Default.PlayCircle)
    object Songs : Screen("songs", "Canciones", Icons.Default.MusicNote)
    object Playlists : Screen("playlists", "Listas", Icons.Default.QueueMusic)
    object Folders : Screen("folders", "Carpetas", Icons.Default.Folder)
    object Settings : Screen("settings", "Ajustes", Icons.Default.Settings)
    object AudioFx : Screen("audio_fx", "Ecualizador", Icons.Default.Equalizer)

    // Detalle de Carpeta
    object FolderDetail : Screen("folder_detail/{folderPath}", "Detalle Carpeta", Icons.Default.Folder) {
        fun createRoute(folderPath: String) = "folder_detail/${android.net.Uri.encode(folderPath)}"
    }

    // NUEVA RUTA: Detalle de Playlist por ID
    object PlaylistDetail : Screen("playlist_detail/{playlistId}", "Detalle Lista", Icons.Default.QueueMusic) {
        fun createRoute(playlistId: Long) = "playlist_detail/$playlistId"
    }
}