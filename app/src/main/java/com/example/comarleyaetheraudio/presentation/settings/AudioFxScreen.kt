package com.example.comarleyaetheraudio.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.data.player.AudioPlayerHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioFxScreen(
    playerHandler: AudioPlayerHandler,
    onBackClick: () -> Unit
) {
    val equalizer = playerHandler.equalizer
    val numberOfBands = remember { equalizer?.numberOfBands?.toInt() ?: 0 }

    var isEnabled by remember { mutableStateOf(equalizer?.enabled ?: false) }
    var bassStrength by remember { mutableStateOf(0f) }
    var virtualizerStrength by remember { mutableStateOf(0f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Efectos DSP y Ecualizador", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Interruptor Principal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activar Ecualizador y DSP",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Switch(
                    checked = isEnabled,
                    onCheckedChange = {
                        isEnabled = it
                        equalizer?.enabled = it
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Control de Graves (Bass Boost)
            Text(
                text = "Amplificador de Graves (Bass Boost)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = bassStrength,
                onValueChange = { bassStrength = it },
                valueRange = 0f..1000f,
                enabled = isEnabled,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Control de Sonido Envolvente (Virtualizer)
            Text(
                text = "Virtualizador (Efecto 3D)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = virtualizerStrength,
                onValueChange = { virtualizerStrength = it },
                valueRange = 0f..1000f,
                enabled = isEnabled,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Bandas de Frecuencia del Ecualizador
            Text(
                text = "Bandas del Ecualizador",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (numberOfBands > 0) {
                val minLevel = equalizer?.bandLevelRange?.get(0) ?: -1500
                val maxLevel = equalizer?.bandLevelRange?.get(1) ?: 1500

                for (i in 0 until numberOfBands) {
                    val bandIndex = i.toShort()
                    val centerFreq = (equalizer?.getCenterFreq(bandIndex) ?: 0) / 1000
                    var currentLevel by remember {
                        mutableStateOf((equalizer?.getBandLevel(bandIndex) ?: 0).toFloat())
                    }

                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "$centerFreq Hz", style = MaterialTheme.typography.bodySmall)
                            Text(text = "${currentLevel.toInt() / 100} dB", style = MaterialTheme.typography.bodySmall)
                        }
                        Slider(
                            value = currentLevel,
                            onValueChange = { newLevel ->
                                currentLevel = newLevel
                                equalizer?.setBandLevel(bandIndex, newLevel.toInt().toShort())
                            },
                            valueRange = minLevel.toFloat()..maxLevel.toFloat(),
                            enabled = isEnabled
                        )
                    }
                }
            } else {
                Text(
                    text = "Ecualizador no disponible en este dispositivo.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}