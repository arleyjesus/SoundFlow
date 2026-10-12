package com.example.comarleyaetheraudio.presentation.folders

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.data.local.entity.FolderEntity

@Composable
fun FoldersScreen(
    folders: List<FolderEntity>,
    onAddFolder: (Uri) -> Unit,
    onRemoveFolder: (String) -> Unit,
    onFolderClick: (FolderEntity) -> Unit,
    isUpdating: Boolean = false,
    message: String? = null,
    onDismissMessage: () -> Unit = {}
) {
    var folderToRemove by remember { mutableStateOf<FolderEntity?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { onAddFolder(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // CABECERA CON TÍTULO Y BOTÓN DE AÑADIR COMPACTO
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Carpetas de Música",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Gestiona los directorios escaneados",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))
            FilledTonalButton(
                onClick = { launcher.launch(null) },
                enabled = !isUpdating,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Añadir",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isUpdating) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text(
                "Actualizando biblioteca…",
                modifier = Modifier.padding(vertical = 8.dp).semantics { liveRegion = LiveRegionMode.Polite },
                style = MaterialTheme.typography.bodySmall
            )
        }
        message?.let { status ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(status, style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = onDismissMessage, modifier = Modifier.align(Alignment.End)) {
                        Text("Entendido")
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ESTADO SI NO HAY CARPETAS REGISTRADAS
        if (folders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No has añadido carpetas externas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // LISTA DE CARPETAS CON BOTÓN DE ELIMINACIÓN DIRECTA
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(folders, key = { it.path }) { folder ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onFolderClick(folder) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = folder.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${folder.songCount} canciones en la biblioteca",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = folder.path,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = { onAddFolder(Uri.parse(folder.uriString)) },
                                enabled = !isUpdating
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Actualizar ${folder.name}")
                            }
                            IconButton(
                                onClick = { folderToRemove = folder },
                                enabled = !isUpdating
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Retirar ${folder.name}",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    folderToRemove?.let { folder ->
        AlertDialog(
            onDismissRequest = { folderToRemove = null },
            title = { Text("¿Retirar ${folder.name}?") },
            text = {
                Text("Sus canciones se retirarán de la biblioteca, favoritos y playlists. Los archivos originales permanecerán en tu dispositivo.")
            },
            confirmButton = {
                TextButton(
                    enabled = !isUpdating,
                    onClick = {
                        folderToRemove = null
                        onRemoveFolder(folder.path)
                    }
                ) { Text("Retirar") }
            },
            dismissButton = {
                TextButton(onClick = { folderToRemove = null }) { Text("Cancelar") }
            }
        )
    }
}
