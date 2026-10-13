package com.example.comarleyaetheraudio.presentation.profile

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.BuildConfig
import com.example.comarleyaetheraudio.domain.model.LibraryFolder
import com.example.comarleyaetheraudio.presentation.components.SettingsActionCard
import com.example.comarleyaetheraudio.presentation.components.SoundFlowPanel
import com.example.comarleyaetheraudio.presentation.folders.FoldersScreen
import com.example.comarleyaetheraudio.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentTheme: AppTheme,
    isDarkMode: Boolean,
    folders: List<LibraryFolder>,
    onSelectTheme: (AppTheme) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onAddFolder: (Uri) -> Unit,
    onRemoveFolder: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAudioFx: () -> Unit,
    isUpdatingFolders: Boolean = false,
    folderMessage: String? = null,
    onDismissFolderMessage: () -> Unit = {},
    folderAccessToRestore: String? = null,
    onRestoreFolderAccess: (String, Uri) -> Unit = { _, _ -> },
    onNavigateToAppearance: () -> Unit = onNavigateToSettings
) {
    var showFoldersDialog by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        .verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Perfil", style = MaterialTheme.typography.headlineLarge)
        Text("Tu espacio en SoundFlow", color = MaterialTheme.colorScheme.onSurfaceVariant)
        SoundFlowPanel(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(22.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Rounded.Headphones, null, Modifier.size(42.dp), tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("Tu biblioteca, tu sonido", style = MaterialTheme.typography.titleMedium)
                    Text("${folders.size} carpetas · ${currentTheme.displayName}", color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Text("Personalización", style = MaterialTheme.typography.titleMedium)
        SettingsActionCard("Apariencia", "Colores, animaciones y superficies de cristal", Icons.Rounded.Palette, onNavigateToAppearance)
        SoundFlowPanel(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().toggleable(isDarkMode, role = Role.Switch, onValueChange = onToggleDarkMode)
                .padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (isDarkMode) Icons.Rounded.DarkMode else Icons.Rounded.LightMode, null, tint = MaterialTheme.colorScheme.primary)
                Text("Modo oscuro", Modifier.weight(1f).padding(horizontal = 16.dp))
                Switch(isDarkMode, onCheckedChange = null)
            }
        }
        Text("Biblioteca y herramientas", style = MaterialTheme.typography.titleMedium)
        SettingsActionCard("Carpetas de Música", "Gestiona las carpetas y sus permisos", Icons.Rounded.FolderOpen) { showFoldersDialog = true }
        SettingsActionCard("Ecualizador & DSP", "Ajusta el sonido de tu reproducción", Icons.Rounded.Equalizer, onNavigateToAudioFx)
        SettingsActionCard("Ajustes", "Apariencia, almacenamiento e información", Icons.Rounded.Settings, onNavigateToSettings)
        Text("SoundFlow ${BuildConfig.VERSION_NAME} · Música local", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
    }
    if (showFoldersDialog) {
        ModalBottomSheet(onDismissRequest = { showFoldersDialog = false }) {
            FoldersScreen(folders, onAddFolder, onRemoveFolder, {}, isUpdatingFolders, folderMessage,
                onDismissFolderMessage, folderAccessToRestore, onRestoreFolderAccess)
        }
    }
}
