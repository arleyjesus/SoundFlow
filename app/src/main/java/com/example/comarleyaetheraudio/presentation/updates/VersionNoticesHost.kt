package com.example.comarleyaetheraudio.presentation.updates

import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.comarleyaetheraudio.presentation.components.dialogs.ChangelogDialog
import com.example.comarleyaetheraudio.presentation.components.dialogs.UpdateAvailableDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VersionNoticesHost(coordinator: VersionNoticeCoordinator, onlineUpdates: Boolean) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val state by coordinator.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    LaunchedEffect(lifecycle, coordinator, onlineUpdates) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            do {
                coordinator.onForeground(onlineUpdates)
                if (onlineUpdates) delay(5 * 60_000L)
            } while (onlineUpdates)
        }
    }
    if (state.showChangelog) ChangelogDialog { scope.launch { coordinator.dismissChangelog() } }
    else if (onlineUpdates) state.update?.let { info ->
        UpdateAvailableDialog(info) { scope.launch { coordinator.dismissUpdate() } }
    }
}
