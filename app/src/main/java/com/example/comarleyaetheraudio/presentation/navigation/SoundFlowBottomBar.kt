package com.example.comarleyaetheraudio.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private data class BottomDestination(val screen: Screen, val unselected: ImageVector, val selected: ImageVector)

private val destinations = listOf(
    BottomDestination(Screen.Home, Icons.Outlined.Home, Icons.Filled.Home),
    BottomDestination(Screen.Library, Icons.Outlined.LibraryMusic, Icons.Filled.LibraryMusic),
    BottomDestination(Screen.Search, Icons.Outlined.Search, Icons.Filled.Search),
    BottomDestination(Screen.Profile, Icons.Outlined.PersonOutline, Icons.Filled.Person)
)

/** Barra inferior (4 destinos) 100 % por tokens de tema; ícono relleno al seleccionar. */
@Composable
fun SoundFlowBottomBar(currentRoute: String?, onNavigate: (Screen) -> Unit) {
    val colors = MaterialTheme.colorScheme
    NavigationBar(containerColor = colors.background, tonalElevation = 0.dp) {
        destinations.forEach { d ->
            val selected = currentRoute == d.screen.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(d.screen) },
                icon = { Icon(if (selected) d.selected else d.unselected, contentDescription = null) },
                label = { Text(d.screen.title, style = MaterialTheme.typography.labelMedium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.onPrimaryContainer,
                    selectedTextColor = colors.onSurface,
                    unselectedIconColor = colors.onSurfaceVariant,
                    unselectedTextColor = colors.onSurfaceVariant,
                    indicatorColor = colors.primaryContainer
                )
            )
        }
    }
}
