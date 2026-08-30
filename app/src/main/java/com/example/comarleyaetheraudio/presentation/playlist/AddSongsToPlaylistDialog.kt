package com.example.comarleyaetheraudio.presentation.playlist

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.comarleyaetheraudio.domain.model.Song

@Composable
fun AddSongsToPlaylistDialog(
    availableSongs: List<Song>,
    alreadyAddedSongIds: List<Long> = emptyList(),
    onDismiss: () -> Unit,
    onAddSongsConfirmed: (List<Long>) -> Unit
) {
    val selectedIds = remember { mutableStateListOf<Long>().apply { addAll(alreadyAddedSongIds) } }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Añadir a la Playlist",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // CONTADOR DE CANCIONES
                Text(
                    text = if (selectedIds.isEmpty()) "Toca o mantén presionado para seleccionar"
                    else "${selectedIds.size} canción(es) seleccionada(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableSongs, key = { it.id }) { song ->
                        val initialSelected = selectedIds.contains(song.id)

                        AddSongItemRow(
                            song = song,
                            isSelectedInitial = initialSelected,
                            onToggleSelection = { isNowSelected ->
                                if (isNowSelected) {
                                    if (!selectedIds.contains(song.id)) selectedIds.add(song.id)
                                } else {
                                    selectedIds.remove(song.id)
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onAddSongsConfirmed(selectedIds.toList())
                            onDismiss()
                        },
                        enabled = true,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Guardar (${selectedIds.size})")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AddSongItemRow(
    song: Song,
    isSelectedInitial: Boolean,
    onToggleSelection: (Boolean) -> Unit
) {
    var isSelectedState by remember(song.id, isSelectedInitial) { mutableStateOf(isSelectedInitial) }

    // ⚡ CONTROLADOR HÁPTICO DE VIBRACIÓN DE ANDROID
    val haptic = LocalHapticFeedback.current

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelectedState)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        label = "BgInstantColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelectedState)
            MaterialTheme.colorScheme.onPrimaryContainer
        else
            MaterialTheme.colorScheme.onSurface,
        label = "TextInstantColor"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = {
                    isSelectedState = !isSelectedState
                    onToggleSelection(isSelectedState)
                },
                onLongClick = {
                    // ACTIVA LA VIBRACIÓN AL MANTENER PRESIONADO
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (!isSelectedState) {
                        isSelectedState = true
                        onToggleSelection(true)
                    }
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelectedState,
                onCheckedChange = { checked ->
                    isSelectedState = checked
                    onToggleSelection(checked)
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelectedState) contentColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}