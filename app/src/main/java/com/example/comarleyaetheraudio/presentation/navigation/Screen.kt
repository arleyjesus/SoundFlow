package com.example.comarleyaetheraudio.presentation.navigation

import com.example.comarleyaetheraudio.ui.theme.SoundFlowIcons
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
    object Home : Screen("home", "Inicio", SoundFlowIcons.Home)
    object Library : Screen("library", "Biblioteca", SoundFlowIcons.Library)
    object Search : Screen("search", "Buscar", SoundFlowIcons.Search)
    object Profile : Screen("profile", "Perfil", SoundFlowIcons.Profile)

    // RUTAS SECUNDARIAS
    object Settings : Screen("settings", "Ajustes", Icons.Rounded.Settings)
    object AudioFx : Screen("audio_fx", "Ecualizador", Icons.Rounded.Equalizer)
    object Appearance : Screen("appearance", "Apariencia", Icons.Rounded.Palette)
    object SmartLibrary : Screen("smart_library", "Biblioteca inteligente", SoundFlowIcons.Library)
}
