package com.example.comarleyaetheraudio.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.comarleyaetheraudio.domain.model.AudioEffectsState
import com.example.comarleyaetheraudio.domain.model.EqualizerBand
import com.example.comarleyaetheraudio.presentation.settings.AudioFxPanel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioFxPanelTest {
    @get:Rule val compose = createComposeRule()

    @Test fun noSessionCannotPretendToActivateAnEffect() {
        compose.setContent {
            MaterialTheme { AudioFxPanel(AudioEffectsState(isConnecting = false), {}, { _, _ -> }, {}, {}, {}, {}, {}) }
        }
        compose.onNodeWithContentDescription("Activar efectos").assertIsNotEnabled()
        compose.onNodeWithText("Reproduce una canción para comprobar los efectos disponibles.").assertExists()
    }

    @Test fun activationWaitsForConfirmedStateAndDoesNotApplyOnRender() {
        var enabledRequests = 0
        val state = AudioEffectsState(isConnecting = false, sessionId = 10, bassAvailable = true)
        compose.setContent {
            MaterialTheme { AudioFxPanel(state, { if (it) enabledRequests++ }, { _, _ -> }, {}, {}, {}, {}, {}) }
        }
        compose.runOnIdle { assertEquals(0, enabledRequests) }
        compose.onNodeWithContentDescription("Activar efectos").performClick()
        compose.runOnIdle { assertEquals(1, enabledRequests) }
        compose.onNodeWithText("Efectos desactivados").assertExists()
        compose.onNodeWithContentDescription("Intensidad de graves").assertIsNotEnabled()
    }

    @Test fun supportedBandSendsItsIndexAndUnsupportedControlIsDisabled() {
        var bandRequest = -1 to 0
        val state = AudioEffectsState(isConnecting = false, sessionId = 10, enabled = true,
            bands = listOf(EqualizerBand(0, 1000, 0)), minBandLevelMb = -1500, maxBandLevelMb = 1500)
        compose.setContent {
            MaterialTheme { AudioFxPanel(state, {}, { index, value -> bandRequest = index to value }, {}, {}, {}, {}, {}) }
        }
        compose.onNodeWithContentDescription("1000 Hz").performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(500f) }
        compose.runOnIdle { assertEquals(0 to 500, bandRequest) }
        compose.onNodeWithContentDescription("Intensidad de espacialidad").performScrollTo().assertIsNotEnabled()
    }

    @Test fun resetHasAnExplicitAction() {
        var resets = 0
        compose.setContent {
            MaterialTheme { AudioFxPanel(AudioEffectsState(isConnecting = false, sessionId = 10,
                bassAvailable = true), {}, { _, _ -> }, {}, {}, { resets++ }, {}, {}) }
        }
        compose.onNodeWithText("Restablecer y desactivar").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, resets) }
    }

    @Test fun applyingAnAdjustmentDisablesControlsUntilConfirmed() {
        compose.setContent {
            MaterialTheme { AudioFxPanel(AudioEffectsState(isConnecting = false, isUpdating = true,
                sessionId = 10, enabled = true, bassAvailable = true), {}, { _, _ -> }, {}, {}, {}, {}, {}) }
        }
        compose.onNodeWithContentDescription("Activar efectos").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Intensidad de graves").assertIsNotEnabled()
        compose.onNodeWithText("Aplicando ajuste…").assertExists()
    }

    @Test fun anErrorBeforeObtainingASessionCanBeRetriedExplicitly() {
        var retries = 0
        compose.setContent {
            MaterialTheme { AudioFxPanel(AudioEffectsState(isConnecting = false,
                errorMessage = "Sin respuesta"), {}, { _, _ -> }, {}, {}, {}, { retries++ }, {}) }
        }
        compose.onNodeWithContentDescription("Activar efectos").assertIsNotEnabled()
        compose.onNodeWithText("Volver a comprobar").performClick()
        compose.runOnIdle { assertEquals(1, retries) }
    }
}
