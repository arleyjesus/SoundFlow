package com.example.comarleyaetheraudio.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onNavigateToAudioFx: () -> Unit // PARÁMETRO AÑADIDO
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Configuración",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ListItem(
            headlineContent = { Text("Modo Oscuro (AMOLED)", fontWeight = FontWeight.Medium) },
            supportingContent = { Text("Fondo negro puro y acentos morados") },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingContent = {
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onToggleDarkMode
                )
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        ListItem(
            headlineContent = { Text("Ecualizador y Efectos DSP", fontWeight = FontWeight.Medium) },
            supportingContent = { Text("Ajusta frecuencias, graves y sonido 3D") },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.clickable { onNavigateToAudioFx() }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        ListItem(
            headlineContent = { Text("SoundFlow", fontWeight = FontWeight.Medium) },
            supportingContent = { Text("Versión 1.4.0 • Reproductor Hi-Fi Local") },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        )
    }
}