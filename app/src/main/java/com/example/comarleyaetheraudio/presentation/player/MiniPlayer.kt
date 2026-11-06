package com.example.comarleyaetheraudio.presentation.player

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.components.common.CoverImage
import com.example.comarleyaetheraudio.ui.theme.Dimens
import com.example.comarleyaetheraudio.ui.theme.MotionTokens
import kotlinx.coroutines.launch

/**
 * Mini player sin estado (MASTER.md §7).
 *
 * - [progress] es una lambda leída solo al dibujar: el avance cada 500 ms no recompone el componente.
 * - Deslizar ← siguiente / → anterior (con acciones de accesibilidad equivalentes).
 * - Colores 100 % por tokens de tema → funciona en los 4 temas y en claro/oscuro.
 */
@Composable
fun MiniPlayer(
    song: Song,
    isPlaying: Boolean,
    progress: () -> Float,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val swipeThresholdPx = with(LocalDensity.current) { 72.dp.toPx() }
    val offsetX = remember { Animatable(0f) }
    val drag = remember { FloatArray(1) }

    val colors = MaterialTheme.colorScheme
    val container = if (com.example.comarleyaetheraudio.ui.theme.LocalAppearance.current.transparenciesEnabled)
        colors.primary.copy(alpha = 0.06f).compositeOver(colors.surfaceContainerHigh) else colors.surfaceContainerHigh

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.Space8, vertical = Dimens.Space4)
            .graphicsLayer { translationX = offsetX.value }
            .pointerInput(song.id) {
                detectHorizontalDragGestures(
                    onDragStart = { drag[0] = 0f },
                    onHorizontalDrag = { change, amount ->
                        change.consume()
                        drag[0] += amount
                        scope.launch { offsetX.snapTo(drag[0] / 3f) }
                    },
                    onDragEnd = {
                        when {
                            drag[0] <= -swipeThresholdPx -> onNext()
                            drag[0] >= swipeThresholdPx -> onPrevious()
                        }
                        scope.launch { offsetX.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)) }
                    },
                    onDragCancel = { scope.launch { offsetX.animateTo(0f) } }
                )
            }
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction("Siguiente pista") { onNext(); true },
                    CustomAccessibilityAction("Pista anterior") { onPrevious(); true }
                )
            },
        shape = MaterialTheme.shapes.large,
        color = container,
        border = BorderStroke(1.dp, colors.outlineVariant),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.clickable(
                role = Role.Button,
                onClickLabel = "Abrir reproductor",
                onClick = onOpen
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.MiniPlayerHeight - Dimens.MiniPlayerProgress)
                    .padding(start = Dimens.Space8, end = Dimens.Space4),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CoverImage(song = song, modifier = Modifier.size(Dimens.MiniPlayerCover))

                Spacer(Modifier.width(Dimens.Space12))

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedIconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier.size(Dimens.TouchTarget),
                    border = BorderStroke(1.dp, colors.primary),
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        contentColor = colors.primary
                    )
                ) {
                    Crossfade(targetState = isPlaying, animationSpec = tween(if (com.example.comarleyaetheraudio.ui.theme.LocalAppearance.current.motionEnabled) MotionTokens.Quick else 0), label = "miniPlayPause") { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (playing) "Pausar" else "Reproducir"
                        )
                    }
                }

                IconButton(onClick = onNext, modifier = Modifier.size(Dimens.TouchTarget)) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Siguiente", tint = colors.onSurface)
                }
            }

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.MiniPlayerProgress),
                color = colors.primary,
                trackColor = colors.onSurface.copy(alpha = 0.12f),
                drawStopIndicator = {}
            )
        }
    }
}
