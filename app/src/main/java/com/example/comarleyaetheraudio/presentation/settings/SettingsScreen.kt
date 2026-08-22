package com.example.comarleyaetheraudio.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    var equalizerEnabled by remember { mutableStateOf(true) }
    var bassLevel by remember { mutableStateOf(0.5f) }
    var midLevel by remember { mutableStateOf(0.5f) }
    var trebleLevel by remember { mutableStateOf(0.5f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // --- SECCIÓN 1: APARIENCIA ---
        Text(
            text = "Apariencia",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tema Oscuro", fontWeight = FontWeight.Medium)
                    Text(
                        "Cambiar entre tema claro y oscuro",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onToggleDarkMode
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SECCIÓN 2: ECUALIZADOR ---
        Text(
            text = "Ecualizador de Audio",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Activar Ecualizador", fontWeight = FontWeight.Medium)
                    Switch(
                        checked = equalizerEnabled,
                        onCheckedChange = { equalizerEnabled = it }
                    )
                }

                if (equalizerEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Graves (Bass Boost)", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = bassLevel,
                        onValueChange = { bassLevel = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Medios (Vocales)", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = midLevel,
                        onValueChange = { midLevel = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Agudos (Treble)", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = trebleLevel,
                        onValueChange = { trebleLevel = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}