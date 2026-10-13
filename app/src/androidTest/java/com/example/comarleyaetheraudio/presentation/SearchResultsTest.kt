package com.example.comarleyaetheraudio.presentation

import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.search.SearchScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SearchResultsTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun rapidQueryAndSourceChangesShowOnlyTheLatestResultAndSelectItsId() {
        val first = song(-90001, "Primera")
        val second = song(-90002, "Segunda")
        var songs by mutableStateOf(listOf(first, second))
        var selected: Long? = null
        compose.setContent { MaterialTheme { SearchScreen(songs = songs, onSongClick = { selected = it.id }) } }
        val input = compose.onNode(hasSetTextAction())
        input.performTextReplacement("Primera")
        input.performTextReplacement("Segunda")
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Segunda").fetchSemanticsNodes().size >= 2 }
        compose.onNodeWithText("Primera").assertDoesNotExist()
        val replacement = second.copy(id = -90003, title = "Segunda nueva")
        compose.runOnIdle { songs = listOf(replacement) }
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Segunda nueva").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Segunda nueva").performClick()
        compose.runOnIdle { assertEquals(-90003L, selected) }
    }

    private fun song(id: Long, title: String) = Song(id, title, "Artista", "Album", duration = 1_000,
        contentUri = Uri.parse("content://soundflow.test/$id"), albumArtUri = null, size = 1,
        mimeType = "audio/mpeg")
}
