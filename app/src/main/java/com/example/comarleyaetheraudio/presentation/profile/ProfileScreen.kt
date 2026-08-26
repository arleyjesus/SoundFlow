package com.example.comarleyaetheraudio.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.ui.theme.BrandGradient

@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToAudioFx: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Text(
            text = "Perfil",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Tarjeta de Usuario / Cabecera
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(BrandGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "SoundFlow User",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Versión 3.0.0 Pro",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Opciones Principales de Ajustes
        Text(
            text = "Configuración",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column {
                ListItem(
                    headlineContent = { Text("Ajustes de Apariencia y Tema") },
                    leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToSettings() }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ListItem(
                    headlineContent = { Text("Ecualizador de Audio (AudioFx)") },
                    leadingContent = { Icon(Icons.Default.GraphicEq, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToAudioFx() }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ListItem(
                    headlineContent = { Text("Acerca de SoundFlow") },
                    supportingContent = { Text("Construcción con Android Jetpack Compose & Media3") },
                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null) }
                )
            }
        }
    }
}