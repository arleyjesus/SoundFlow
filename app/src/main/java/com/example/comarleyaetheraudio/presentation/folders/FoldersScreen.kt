package com.example.comarleyaetheraudio.presentation.folders

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.data.local.entity.FolderEntity

@Composable
fun FoldersScreen(
    folders: List<FolderEntity>,
    onAddFolder: (Uri) -> Unit,
    onRemoveFolder: (String) -> Unit
) {
    // Launcher para abrir el selector nativo de carpetas de Android (SAF)
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { selectedFolderUri ->
            onAddFolder(selectedFolderUri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Botón principal para seleccionar una nueva carpeta
        Button(
            onClick = { folderPickerLauncher.launch(null) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CreateNewFolder, contentDescription = "Agregar carpeta")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Seleccionar Carpeta de Música")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (folders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No has agregado carpetas aún.\nToca el botón de arriba para seleccionar tus carpetas de música.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Text(
                text = "Carpetas analizadas:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(folders) { folder ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        ListItem(
                            leadingContent = {
                                Icon(Icons.Default.Folder, contentDescription = null)
                            },
                            headlineContent = { Text(folder.name) },
                            supportingContent = { Text("${folder.songCount} canciones encontradas") },
                            trailingContent = {
                                IconButton(onClick = { onRemoveFolder(folder.uriString) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Eliminar carpeta",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}