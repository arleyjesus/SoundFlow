package com.example.comarleyaetheraudio.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.ui.theme.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import kotlinx.coroutines.CancellationException

@Composable
fun AppearanceScreen(
    settings: AppearanceSettings,
    onBack: () -> Unit,
    onTheme: (AppTheme) -> Unit,
    onDarkMode: (Boolean) -> Unit,
    onAnimations: (Boolean) -> Unit,
    onTransparencies: (Boolean) -> Unit,
    onFollowSystem: (Boolean) -> Unit = {},
    onReset: () -> Unit = {},
    onBackground: (String?) -> Unit = {}
) {
    var showReset by remember { mutableStateOf(false) }
    var backgroundError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val backgroundPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) try {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            onBackground(uri.toString()); backgroundError = null
        } catch (e: SecurityException) { backgroundError = "No se pudo conservar acceso a esta imagen. Elige otra ubicación." }
    }
    Column(
        Modifier.fillMaxSize().background(com.example.comarleyaetheraudio.ui.theme.screenBackgroundColor())
            .verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
            Text("Apariencia", modifier = Modifier.weight(1f), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        Text("Tu música, a tu estilo", style = MaterialTheme.typography.titleMedium)
        com.example.comarleyaetheraudio.presentation.components.SoundFlowPanel {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Fondo personalizado", style = MaterialTheme.typography.titleMedium)
                Text("Una imagen con velo claro u oscuro para mantener la lectura. Se oculta al desactivar Transparencias.",
                    style = MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick = { backgroundPicker.launch(arrayOf("image/*")) }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(if (settings.backgroundUri == null) "Elegir imagen" else "Cambiar imagen")
                }
                if (settings.backgroundUri != null) TextButton(onClick = { onBackground(null) }) { Text("Quitar fondo") }
                backgroundError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        }
        Text("Paleta de colores", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppTheme.entries.forEach { theme ->
                val selected = settings.theme == theme
                Card(colors = CardDefaults.cardColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )) {
                    Row(
                        Modifier.fillMaxWidth().selectable(selected, role = Role.RadioButton, onClick = { onTheme(theme) })
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(Modifier.size(28.dp).background(getThemeColors(theme, settings.isDarkMode).primary, CircleShape))
                        Text(theme.displayName, Modifier.weight(1f))
                        RadioButton(selected, onClick = null)
                    }
                }
            }
        }
        AppearanceToggle("Seguir tema del sistema", "Adapta el modo claro y oscuro a Android.", settings.followSystemTheme, onFollowSystem)
        AppearanceToggle("Modo oscuro", "Desactiva Seguir tema del sistema para elegir el modo manualmente.", settings.isDarkMode, onDarkMode,
            enabled = !settings.followSystemTheme)
        AppearanceToggle("Animaciones", "Navegación, pestañas, carátulas, selección y visualizador. Las letras siguen avanzando sin desplazamiento animado al desactivar.", settings.animationsEnabled, onAnimations)
        AppearanceToggle("Transparencias", "Degradados de cristal, ambiente de Inicio y fondo del reproductor. Desactiva para usar superficies opacas.", settings.transparenciesEnabled, onTransparencies)
        if (!settings.systemAnimationsEnabled) {
            Text("Android ha desactivado las animaciones. Se conserva tu preferencia para cuando vuelvas a activarlas en el sistema.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        OutlinedButton(onClick = { showReset = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text("Restablecer apariencia")
        }
    }
    if (showReset) AlertDialog(onDismissRequest = { showReset = false },
        title = { Text("Restablecer apariencia") },
        text = { Text("Volver al tema Principal, modo oscuro y efectos visuales activados. Se conserva tu biblioteca, favoritos, playlists y ajustes de audio.",
            modifier = Modifier.verticalScroll(rememberScrollState())) },
        confirmButton = { TextButton(onClick = { onReset(); showReset = false }) { Text("Restablecer") } },
        dismissButton = { TextButton(onClick = { showReset = false }) { Text("Cancelar") } })
}

@Composable
private fun AppearanceToggle(title: String, description: String, checked: Boolean, onChange: (Boolean) -> Unit, enabled: Boolean = true) {
    Card {
        Row(Modifier.fillMaxWidth().toggleable(value = checked, role = Role.Switch, enabled = enabled, onValueChange = onChange)
            .padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = null, enabled = enabled)
        }
    }
}
