package com.example.comarleyaetheraudio.presentation.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.domain.playback.SleepTimerState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SleepTimerDialog(
    currentTimerMinutes: Int,
    onStartTimer: (Int, Boolean) -> Unit,
    onCancelTimer: () -> Unit,
    onDismiss: () -> Unit,
    sleepTimerState: SleepTimerState = if (currentTimerMinutes > 0)
        SleepTimerState.CountingDown(currentTimerMinutes) else SleepTimerState.Off
) {
    var selectedMinutes by remember { mutableStateOf(if (currentTimerMinutes > 0) currentTimerMinutes else 30) }
    var finishLastSong by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Temporizador de Apagado ⏱️", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                if (sleepTimerState.isActive) {
                    Text(when (sleepTimerState) {
                        is SleepTimerState.CountingDown -> "Tiempo restante: ${sleepTimerState.minutes} min"
                        SleepTimerState.WaitingForTrackEnd -> "Esperando a que termine la canción"
                        SleepTimerState.Stopping -> "Deteniendo reproducción…"
                        SleepTimerState.Off -> ""
                    }, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Text("Detener la música automáticamente en:", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(16.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(15, 30, 45, 60).forEach { mins ->
                        FilterChip(
                            selected = selectedMinutes == mins,
                            onClick = { selectedMinutes = mins },
                            label = { Text("${mins}m") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .toggleable(finishLastSong, role = Role.Checkbox, onValueChange = { finishLastSong = it })
                ) {
                    Checkbox(
                        checked = finishLastSong,
                        onCheckedChange = null
                    )
                    Text("Esperar a que termine la canción actual", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onStartTimer(selectedMinutes, finishLastSong)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Iniciar")
            }
        },
        dismissButton = {
            if (sleepTimerState.isActive) {
                TextButton(onClick = {
                    onCancelTimer()
                    onDismiss()
                }) {
                    Text("Cancelar Timer", color = MaterialTheme.colorScheme.error)
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
