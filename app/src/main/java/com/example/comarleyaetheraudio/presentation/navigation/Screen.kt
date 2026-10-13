package com.example.comarleyaetheraudio.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    // 4 SECCIONES PRINCIPALES DEL MENÚ INFERIOR
    object Home : Screen("home", "Inicio", Icons.Rounded.Home)
    object Library : Screen("library", "Biblioteca", Icons.Rounded.LibraryMusic)
    object Search : Screen("search", "Buscar", Icons.Rounded.Search)
    object Profile : Screen("profile", "Perfil", Icons.Rounded.Person)

    // RUTAS SECUNDARIAS
    object Settings : Screen("settings", "Ajustes", Icons.Rounded.Settings)
    object AudioFx : Screen("audio_fx", "Ecualizador", Icons.Rounded.Equalizer)
    object Appearance : Screen("appearance", "Apariencia", Icons.Rounded.Palette)
    object SmartLibrary : Screen("smart_library", "Biblioteca inteligente", Icons.Rounded.LibraryMusic)
}
