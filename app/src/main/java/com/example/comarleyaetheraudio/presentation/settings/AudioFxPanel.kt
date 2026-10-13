package com.example.comarleyaetheraudio.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.domain.model.AudioEffectsState
import com.example.comarleyaetheraudio.presentation.components.SoundFlowPanel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AudioFxPanel(
    state: AudioEffectsState,
    onEnabled: (Boolean) -> Unit,
    onBand: (Int, Int) -> Unit,
    onBass: (Int) -> Unit,
    onVirtualizer: (Int) -> Unit,
    onReset: () -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    val busy = state.isConnecting || state.isUpdating
    Scaffold(topBar = {
        TopAppBar(title = { Text("Sonido a tu medida") }, navigationIcon = {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Rounded.Equalizer, contentDescription = null, modifier = Modifier.size(32.dp))
                    Text("Ecualizador y efectos", style = MaterialTheme.typography.headlineSmall)
                    Text("Ajusta el sonido con los efectos disponibles para la reproducción actual.",
                        style = MaterialTheme.typography.bodyMedium)
                    Row(Modifier.fillMaxWidth().toggleable(value = state.enabled,
                        enabled = !busy && state.hasEffects && state.errorMessage == null,
                        role = Role.Switch, onValueChange = onEnabled)
                        .heightIn(min = 56.dp).semantics { contentDescription = "Activar efectos" },
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(if (state.enabled) "Efectos activados" else "Efectos desactivados", Modifier.weight(1f))
                        Switch(checked = state.enabled, onCheckedChange = null,
                            enabled = !busy && state.hasEffects && state.errorMessage == null)
                    }
                }
            }

            val message = state.errorMessage ?: when {
                state.isConnecting -> "Conectando con el reproductor…"
                state.isUpdating -> "Aplicando ajuste…"
                state.sessionId <= 0 -> "Reproduce una canción para comprobar los efectos disponibles."
                !state.hasEffects -> "No hay efectos disponibles para la reproducción actual."
                !state.enabled -> "Activa los efectos para ajustar el sonido."
                else -> "Ajustes confirmados por el motor de audio."
            }
            Surface(color = if (state.errorMessage != null) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp).semantics { liveRegion = LiveRegionMode.Polite }) {
                    if (busy) LinearProgressIndicator(Modifier.fillMaxWidth().padding(bottom = 12.dp))
                    Text(message, style = MaterialTheme.typography.bodyMedium)
                    if (state.errorMessage != null || (state.sessionId > 0 && !state.hasEffects)) {
                        TextButton(onClick = onRefresh, enabled = !busy) { Text("Volver a comprobar") }
                    }
                }
            }

            EffectSection("Ecualizador", "Balance por frecuencias") {
                if (state.bands.isEmpty()) Text("Ecualizador no disponible para esta reproducción.")
                else state.bands.forEach { band ->
                    key(state.sessionId, band.index) {
                        EffectSlider("${band.frequencyHz} Hz", band.levelMb,
                            state.minBandLevelMb, state.maxBandLevelMb, state.canEdit,
                            format = { "%.1f dB".format(it / 100f) }) { onBand(band.index, it) }
                    }
                }
            }
            EffectSection("Graves", "Refuerzo de frecuencias bajas") {
                if (!state.bassAvailable) Text("Control de graves no disponible para esta reproducción.")
                EffectSlider("Intensidad de graves", state.bassStrength, 0, 1000,
                    state.canEdit && state.bassAvailable, format = { "${it / 10}%" }, onCommit = onBass)
            }
            EffectSection("Espacialidad", "Virtualización de audio") {
                if (!state.virtualizerAvailable) Text("Virtualización no disponible para esta reproducción.")
                EffectSlider("Intensidad de espacialidad", state.virtualizerStrength, 0, 1000,
                    state.canEdit && state.virtualizerAvailable, format = { "${it / 10}%" }, onCommit = onVirtualizer)
            }
            OutlinedButton(onClick = onReset, enabled = !busy && state.hasEffects,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Restablecer y desactivar") }
            Text("Los efectos empiezan desactivados. Restablecer deja las bandas en 0 dB y los refuerzos en cero. " +
                "Una nueva sesión de audio vuelve a estos valores; no se guardan como un perfil permanente.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EffectSection(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    SoundFlowPanel(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}

@Composable
private fun EffectSlider(label: String, value: Int, min: Int, max: Int, enabled: Boolean,
    format: (Int) -> String, onCommit: (Int) -> Unit) {
    var draft by remember(value) { mutableFloatStateOf(value.toFloat()) }
    // Reset an unconfirmed drag on disabling/error, even when the confirmed value is unchanged.
    LaunchedEffect(enabled, value) { if (!enabled) draft = value.toFloat() }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(format(draft.roundToInt()), style = MaterialTheme.typography.labelLarge)
    }
    Slider(value = draft.coerceIn(min.toFloat(), max.coerceAtLeast(min).toFloat()),
        onValueChange = { draft = it }, onValueChangeFinished = { onCommit(draft.roundToInt()) },
        enabled = enabled && max > min, valueRange = min.toFloat()..max.coerceAtLeast(min + 1).toFloat(),
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).semantics { contentDescription = label })
}
