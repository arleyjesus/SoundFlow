package com.example.comarleyaetheraudio.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.ui.theme.LocalAppearance
import com.example.comarleyaetheraudio.ui.theme.panelColors

@Composable
fun SoundFlowPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    val appearance = LocalAppearance.current
    val brush = remember(colors, appearance.isDarkMode, appearance.transparenciesEnabled) {
        Brush.linearGradient(panelColors(colors, appearance.isDarkMode, appearance.transparenciesEnabled))
    }
    val shape = RoundedCornerShape(24.dp)
    CompositionLocalProvider(LocalContentColor provides colors.onSurface) {
        Column(modifier.clip(shape).background(brush)
            .border(1.dp, colors.outlineVariant, shape), content = content)
    }
}

@Composable
fun SettingsActionCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    SoundFlowPanel(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().clickable(onClick = onClick).heightIn(min = 72.dp).padding(18.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Icon(icon, null, Modifier.padding(10.dp).size(24.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
