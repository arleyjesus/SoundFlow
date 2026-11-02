package com.example.comarleyaetheraudio.presentation.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.comarleyaetheraudio.BuildConfig
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
                    text = "SoundFlow ${BuildConfig.VERSION_NAME}",
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
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ChangelogItem("🔄", "Búsqueda automática de nuevas versiones al abrir o volver a SoundFlow y mientras la app está visible.")
                ChangelogItem("🔔", "El aviso automático se muestra una sola vez por versión. Puedes volver a buscarla desde Ajustes.")
                ChangelogItem("✨", "Esta ventana muestra la versión instalada y sus novedades una sola vez, incluso sin conexión.")
                ChangelogItem("🔒", "La descarga conserva las comprobaciones del APK y la confirmación de instalación de Android.")
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
