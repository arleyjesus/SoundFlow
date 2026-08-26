package com.example.comarleyaetheraudio.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.ui.theme.ElectricPurple

@Composable
fun SleepTimerDialog(
    currentTimerMinutes: Int,
    onStartTimer: (Int, Boolean) -> Unit,
    onCancelTimer: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMinutes by remember { mutableStateOf(if (currentTimerMinutes > 0) currentTimerMinutes else 30) }
    var finishLastSong by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Temporizador de Apagado ⏱️", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Detener la música automáticamente en:", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = finishLastSong,
                        onCheckedChange = { finishLastSong = it }
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
                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
            ) {
                Text("Iniciar")
            }
        },
        dismissButton = {
            if (currentTimerMinutes > 0) {
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