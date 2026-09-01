package com.example.comarleyaetheraudio.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    // 4 SECCIONES PRINCIPALES DEL MENÚ INFERIOR
    object Home : Screen("home", "Inicio", Icons.Default.Home)
    object Library : Screen("library", "Biblioteca", Icons.Default.LibraryMusic)
    object Search : Screen("search", "Buscar", Icons.Default.Search)
    object Profile : Screen("profile", "Perfil", Icons.Default.Person)

    // RUTAS SECUNDARIAS
    object Settings : Screen("settings", "Ajustes", Icons.Default.Settings)
    object AudioFx : Screen("audio_fx", "Ecualizador", Icons.Default.Equalizer)
}
