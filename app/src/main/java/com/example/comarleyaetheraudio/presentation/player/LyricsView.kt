package com.example.comarleyaetheraudio.presentation.player

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comarleyaetheraudio.domain.model.LyricLine
import kotlinx.coroutines.launch

@Composable
fun LyricsView(
    lyrics: List<LyricLine>,
    currentPositionMs: Long,
    modifier: Modifier = Modifier
) {
    if (lyrics.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay letras disponibles (.lrc)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        return
    }

    // Encontrar la línea actual según el tiempo de reproducción
    val activeIndex = remember(currentPositionMs, lyrics) {
        val index = lyrics.indexOfLast { it.timeMs <= currentPositionMs }
        if (index != -1) index else 0
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Scroll automático al avanzar la letra
    LaunchedEffect(activeIndex) {
        coroutineScope.launch {
            if (activeIndex >= 0 && activeIndex < lyrics.size) {
                listState.animateScrollToItem(activeIndex)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        itemsIndexed(lyrics) { index, line ->
            val isActive = index == activeIndex
            Text(
                text = line.text,
                fontSize = if (isActive) 18.sp else 14.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 16.dp)
            )
        }
    }
}