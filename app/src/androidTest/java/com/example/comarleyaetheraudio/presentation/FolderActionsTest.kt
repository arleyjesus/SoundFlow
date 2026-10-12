package com.example.comarleyaetheraudio.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.comarleyaetheraudio.data.local.entity.FolderEntity
import com.example.comarleyaetheraudio.presentation.folders.FoldersScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FolderActionsTest {
    @get:Rule val compose = createComposeRule()
    private val folder = FolderEntity("content://soundflow.test/folder", "Pruebas", "content://soundflow.test/folder", 2)

    @Test
    fun removalRequiresConfirmationAndCanBeCancelled() {
        val removed = mutableListOf<String>()
        compose.setContent {
            MaterialTheme { FoldersScreen(listOf(folder), {}, { removed.add(it) }, {}) }
        }
        compose.onNodeWithContentDescription("Retirar Pruebas").performClick()
        compose.runOnIdle { assertEquals(emptyList<String>(), removed) }
        compose.onNodeWithText("Cancelar").performClick()
        compose.runOnIdle { assertEquals(emptyList<String>(), removed) }
        compose.onNodeWithContentDescription("Retirar Pruebas").performClick()
        compose.onNodeWithText("Retirar").performClick()
        compose.runOnIdle { assertEquals(listOf(folder.path), removed) }
    }

    @Test
    fun refreshUsesSelectedFolderUriAndShowsStoredCount() {
        val scanned = mutableListOf<String>()
        compose.setContent {
            MaterialTheme { FoldersScreen(listOf(folder), { scanned.add(it.toString()) }, {}, {}) }
        }
        compose.onNodeWithText("2 canciones en la biblioteca").assertExists()
        compose.onNodeWithContentDescription("Actualizar Pruebas").performClick()
        compose.runOnIdle { assertEquals(listOf(folder.uriString), scanned) }
    }

    @Test
    fun busyStateDisablesMutationsAndShowsMessage() {
        compose.setContent {
            MaterialTheme {
                FoldersScreen(listOf(folder), {}, {}, {}, isUpdating = true, message = "Resultado anterior")
            }
        }
        compose.onNodeWithText("Añadir").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Actualizar Pruebas").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Retirar Pruebas").assertIsNotEnabled()
        compose.onNodeWithText("Actualizando biblioteca…").assertExists()
        compose.onNodeWithText("Resultado anterior").assertExists()
    }
}
