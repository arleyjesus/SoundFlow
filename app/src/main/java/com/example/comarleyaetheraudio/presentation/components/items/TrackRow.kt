package com.example.comarleyaetheraudio.presentation.components.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.components.common.CoverImage
import com.example.comarleyaetheraudio.presentation.components.common.EqualizerBars
import com.example.comarleyaetheraudio.presentation.components.common.HiResBadge
import com.example.comarleyaetheraudio.presentation.components.common.LocalNowPlaying
import com.example.comarleyaetheraudio.ui.theme.Dimens

/** Fila de pista sin estado (MASTER.md §7 `TrackRow`). */
@Composable
fun TrackRow(
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onMoreOptionsClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.TrackRowMinHeight)
            .clickable(role = Role.Button, onClickLabel = "Reproducir ${song.title}", onClick = onClick)
            .semantics(mergeDescendants = true) {
                if (isCurrent) stateDescription = if (isPlaying) "Reproduciendo" else "En pausa"
            }
            .padding(start = Dimens.ScreenMargin, end = if (onMoreOptionsClick != null) Dimens.Space4 else Dimens.ScreenMargin, top = Dimens.Space8, bottom = Dimens.Space8),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoverImage(song = song, modifier = Modifier.size(Dimens.TrackCover), crossfade = false)

        Spacer(Modifier.width(Dimens.Space12))

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isCurrent) {
            Spacer(Modifier.width(Dimens.Space8))
            EqualizerBars(isPlaying = isPlaying)
        }

        if (onMoreOptionsClick != null) {
            IconButton(onClick = onMoreOptionsClick, modifier = Modifier.size(Dimens.TouchTarget)) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Opciones de ${song.title}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
