package com.example.comarleyaetheraudio.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.BuildConfig
import com.example.comarleyaetheraudio.presentation.components.SettingsActionCard
import com.example.comarleyaetheraudio.presentation.components.SoundFlowPanel
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import com.example.comarleyaetheraudio.domain.model.NetworkFeature

@Composable
fun SettingsScreen(
    onNavigateToAudioFx: () -> Unit,
    onNavigateToAppearance: () -> Unit = {},
    onBack: () -> Unit = {},
    onNetworkFeature: (NetworkFeature, Boolean) -> Unit = { _, _ -> }
) {
    var showAbout by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        .verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Volver") }
            Text("Ajustes", style = MaterialTheme.typography.headlineMedium)
        }
        Text("Haz SoundFlow tuyo", color = MaterialTheme.colorScheme.onSurfaceVariant)
        SettingsActionCard("Apariencia", "Paletas, modo oscuro y efectos visuales", Icons.Rounded.Palette, onNavigateToAppearance)
        SettingsActionCard("Panel Ecualizador & DSP", "Ecualizador, graves y espacialidad", Icons.Rounded.Equalizer, onNavigateToAudioFx)
        Text("Privacidad y servicios en línea", style = MaterialTheme.typography.titleMedium)
        Text("La música se reproduce localmente. Estas consultas opcionales envían el nombre del artista, el título o datos normales de conexión al proveedor indicado. No suben tus archivos de audio.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        val network = LocalNetworkSettings.current
        NetworkToggle("Imágenes de artistas", "Deezer: nombre del artista. Al desactivar se conserva la caché local.", network.artistImages) {
            onNetworkFeature(NetworkFeature.ARTIST_IMAGES, it)
        }
        NetworkToggle("Buscar letras en línea", "LRCLIB: título y artista. Las letras locales siguen disponibles.", network.lyrics) {
            onNetworkFeature(NetworkFeature.LYRICS, it)
        }
        NetworkToggle("Comprobar actualizaciones", "GitHub: consulta de versiones, sin instalación automática.", network.updates) {
            onNetworkFeature(NetworkFeature.UPDATES, it)
        }
        Text("Almacenamiento", style = MaterialTheme.typography.titleMedium)
        SoundFlowPanel(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(18.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Rounded.Storage, null, tint = MaterialTheme.colorScheme.primary)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Caché de portadas", style = MaterialTheme.typography.titleSmall)
                    Text("Miniaturas en disco: límite de 500 MB. Imágenes de artistas: 32 MiB. Memoria de imágenes administrada por Coil.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Text("Acerca de", style = MaterialTheme.typography.titleMedium)
        SettingsActionCard("SoundFlow ${BuildConfig.VERSION_NAME}", "Información de la aplicación", Icons.Rounded.Info) { showAbout = true }
    }
    if (showAbout) AlertDialog(onDismissRequest = { showAbout = false }, title = { Text("SoundFlow ${BuildConfig.VERSION_NAME}") },
        text = { Text("Reproductor de música local con Media3, biblioteca Room e interfaz Jetpack Compose.") },
        confirmButton = { TextButton({ showAbout = false }) { Text("Entendido") } })
}

@Composable
private fun NetworkToggle(title: String, detail: String, enabled: Boolean, onChange: (Boolean) -> Unit) {
    Card {
        Row(Modifier.fillMaxWidth().toggleable(enabled, role = Role.Switch, onValueChange = onChange)
            .padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(enabled, onCheckedChange = null)
        }
    }
}
