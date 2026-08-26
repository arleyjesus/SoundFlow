package com.example.comarleyaetheraudio.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.ui.theme.ElectricPurple

@Composable
fun ChangelogDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "¡Bienvenido a SoundFlow v2.2.0! 🎨",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Novedades de esta actualización",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ChangelogItem("🎨", "Color Dinámico: Adaptación de la interfaz a la carátula activa (Palette API).")
                ChangelogItem("📊", "Visualizador de Audio: Espectro audio-reactivo en tiempo real.")
                ChangelogItem("🎛️", "Personalización de temas (Claro, Oscuro y AMOLED Puro).")
                ChangelogItem("⚡", "Renderizado optimizado a 120 FPS.")
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("¡Disfrutar!")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun ChangelogItem(emoji: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(emoji, modifier = Modifier.padding(end = 8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}