package com.example.comarleyaetheraudio.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    // 📱 NUEVA BARRA INFERIOR (4 Opciones)
    object Home : Screen("home", "Inicio", Icons.Default.Home)
    object Library : Screen("library", "Biblioteca", Icons.Default.LibraryMusic)
    object Search : Screen("search", "Buscar", Icons.Default.Search)
    object Profile : Screen("profile", "Perfil", Icons.Default.Person)

    // 🔗 SUB-RUTAS (Ocultas de la barra principal)
    object Settings : Screen("settings", "Ajustes", Icons.Default.Settings)
    object AudioFx : Screen("audio_fx", "Ecualizador", Icons.Default.GraphicEq)

    object FolderDetail : Screen("folder_detail/{folderPath}", "Detalle", Icons.Default.Folder) {
        fun createRoute(folderPath: String) = "folder_detail/${android.net.Uri.encode(folderPath)}"
    }

    object PlaylistDetail : Screen("playlist_detail/{playlistId}", "Playlist", Icons.Default.QueueMusic) {
        fun createRoute(playlistId: Long) = "playlist_detail/$playlistId"
    }
}