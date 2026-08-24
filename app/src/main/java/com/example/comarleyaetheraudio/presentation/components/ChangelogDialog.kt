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
                    text = "¡Bienvenido a SoundFlow v2.0.0! 🎉",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Novedades y Mejoras",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ChangelogItem("⚡", "Rendimiento ultra fluido a 120 FPS con caché local de carátulas.")
                ChangelogItem("🎨", "Nuevo diseño adaptable en Modo Claro / Oscuro con colores de marca.")
                ChangelogItem("📑", "Motor de Playlists completo para crear y organizar tus listas.")
                ChangelogItem("🗂️", "Escáner por carpetas sin cierres inesperados.")
                ChangelogItem("⋮", "Menú de 3 puntos en canciones para gestión rápida.")
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("¡Entendido!")
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