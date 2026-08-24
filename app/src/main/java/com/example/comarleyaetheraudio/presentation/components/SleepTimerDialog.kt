package com.example.comarleyaetheraudio.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.ui.theme.BrandGradient
import com.example.comarleyaetheraudio.ui.theme.ElectricPurple
import com.example.comarleyaetheraudio.ui.theme.LightLavender

@Composable
fun SleepTimerDialog(
    currentTimerMinutes: Int,
    onStartTimer: (minutes: Int, finishCurrentSong: Boolean) -> Unit,
    onCancelTimer: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161618), // Gris OLED
        title = {
            Text(
                text = "Temporizador de Apagado",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (currentTimerMinutes > 0) {
                    Text(
                        text = "Tiempo restante: ~$currentTimerMinutes min",
                        color = LightLavender,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = {
                            onCancelTimer()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Desactivar Temporizador", color = Color.Red)
                    }
                    HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 4.dp))
                }

                Text("Selecciona un tiempo:", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)

                // Opciones de Minutos en Grid 2x2
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimerOptionChip("15 min", Modifier.weight(1f)) { onStartTimer(15, false); onDismiss() }
                    TimerOptionChip("30 min", Modifier.weight(1f)) { onStartTimer(30, false); onDismiss() }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimerOptionChip("45 min", Modifier.weight(1f)) { onStartTimer(45, false); onDismiss() }
                    TimerOptionChip("60 min", Modifier.weight(1f)) { onStartTimer(60, false); onDismiss() }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Opción Especial: Al finalizar canción
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BrandGradient)
                        .clickable {
                            onStartTimer(0, true)
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Al terminar esta canción",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}

@Composable
private fun TimerOptionChip(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF242428)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}