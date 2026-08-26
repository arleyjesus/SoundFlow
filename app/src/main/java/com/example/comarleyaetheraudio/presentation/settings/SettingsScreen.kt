package com.example.comarleyaetheraudio.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    currentAppTheme: AppTheme,
    onToggleDarkMode: (Boolean) -> Unit,
    onSelectTheme: (AppTheme) -> Unit,
    onNavigateToAudioFx: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes y Apariencia", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Modo de Pantalla",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ListItem(
                headlineContent = { Text("Modo Oscuro", fontWeight = FontWeight.Medium) },
                supportingContent = { Text("Fondo nocturno con acentos de color") },
                leadingContent = {
                    Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Switch(checked = isDarkMode, onCheckedChange = onToggleDarkMode)
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = "Paleta de Colores de la App",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AppTheme.values().forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectTheme(theme) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = theme.displayName,
                                fontWeight = if (currentAppTheme == theme) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            RadioButton(
                                selected = currentAppTheme == theme,
                                onClick = { onSelectTheme(theme) }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            ListItem(
                headlineContent = { Text("Ecualizador y Efectos DSP", fontWeight = FontWeight.Medium) },
                supportingContent = { Text("Ajusta frecuencias, graves y sonido 3D") },
                leadingContent = {
                    Icon(Icons.Default.Equalizer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                modifier = Modifier.clickable { onNavigateToAudioFx() }
            )
        }
    }
}