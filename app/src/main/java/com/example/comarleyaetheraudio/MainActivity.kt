package com.example.comarleyaetheraudio

import android.os.Bundle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.comarleyaetheraudio.data.local.MusicDatabase
import com.example.comarleyaetheraudio.data.local.util.CoverCacheManager
import com.example.comarleyaetheraudio.data.local.util.FolderScanner
import com.example.comarleyaetheraudio.data.player.AudioPlayerHandler
import com.example.comarleyaetheraudio.data.remote.GitHubUpdateChecker
import com.example.comarleyaetheraudio.data.repository.AudioRepositoryImpl
import com.example.comarleyaetheraudio.data.repository.SettingsRepository
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.components.dialogs.UpdateAvailableDialog
import com.example.comarleyaetheraudio.presentation.library.LibraryViewModel
import com.example.comarleyaetheraudio.presentation.navigation.AppNavigation
import com.example.comarleyaetheraudio.presentation.navigation.Screen
import com.example.comarleyaetheraudio.presentation.player.FullPlayerSheet
import com.example.comarleyaetheraudio.presentation.player.MiniPlayerCard
import com.example.comarleyaetheraudio.presentation.player.PlaybackStatusBanner
import com.example.comarleyaetheraudio.ui.theme.ComarleyjesusaetheraudioTheme
import com.example.comarleyaetheraudio.ui.theme.LocalAppearance
import com.example.comarleyaetheraudio.ui.theme.platformAppearance
import com.example.comarleyaetheraudio.presentation.settings.LocalNetworkSettings
import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: LibraryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(LibraryViewModel::class.java))
                val database = MusicDatabase.getDatabase(applicationContext)
                val repository = AudioRepositoryImpl(applicationContext, database.musicDao(),
                    database.playlistDao(), FolderScanner(applicationContext))
                @Suppress("UNCHECKED_CAST")
                return LibraryViewModel(repository, AudioPlayerHandler(applicationContext),
                    SettingsRepository(applicationContext)) as T
            }
        })[LibraryViewModel::class.java]

        setContent {
            val appearance by viewModel.appearance.collectAsStateWithLifecycle()
            val network by viewModel.networkSettings.collectAsStateWithLifecycle()
            // Wait for the first persisted snapshot instead of flashing the default palette.
            appearance?.takeIf { network != null }?.let { preferences ->
                val settings = platformAppearance(preferences)
                SideEffect {
                    val bars = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT) { settings.isDarkMode }
                    enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
                }
                CompositionLocalProvider(LocalAppearance provides settings, LocalNetworkSettings provides requireNotNull(network)) {
                    ComarleyjesusaetheraudioTheme(settings.isDarkMode, settings.theme) {
                        MainAppStructure(viewModel = viewModel, isDarkMode = settings.isDarkMode)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppStructure(viewModel: LibraryViewModel, isDarkMode: Boolean) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val artistGrouped by viewModel.artistGrouped.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val playerState by viewModel.navigationPlayerState.collectAsStateWithLifecycle()
    val currentTheme = LocalAppearance.current.theme

    var showFullPlayer by rememberSaveable { mutableStateOf(false) }
    var updateInfoState by remember { mutableStateOf<GitHubUpdateChecker.UpdateInfo?>(null) }

    val onlineUpdates = LocalNetworkSettings.current.updates
    LaunchedEffect(onlineUpdates) {
        if (!onlineUpdates) { updateInfoState = null; return@LaunchedEffect }
        val info = GitHubUpdateChecker.checkForUpdates()
        if (info != null && info.hasUpdate) {
            updateInfoState = info
        }
    }

    val navContainerColor = MaterialTheme.colorScheme.background

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(navContainerColor)
            ) {
                if (!showFullPlayer) MiniPlayerHost(viewModel) { showFullPlayer = true }
                NavigationBar(
                    containerColor = navContainerColor,
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val navItems = listOf(
                        Pair(Screen.Home, Icons.Rounded.Home),
                        Pair(Screen.Library, Icons.Rounded.LibraryMusic),
                        Pair(Screen.Search, Icons.Rounded.Search),
                        Pair(Screen.Profile, Icons.Rounded.Person)
                    )

                    navItems.forEach { (screen, iconVector) ->
                        NavigationBarItem(
                            icon = { Icon(iconVector, contentDescription = screen.title) },
                            label = {
                                Text(
                                    text = screen.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            selected = currentRoute == screen.route,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).consumeWindowInsets(paddingValues)) {
            AppNavigation(
                navController = navController,
                viewModel = viewModel,
                songs = songs,
                artistGrouped = artistGrouped,
                playlists = playlists,
                favoriteIds = favoriteIds,
                folders = folders,
                playerState = playerState,
                currentTheme = currentTheme,
                isDarkMode = isDarkMode
            )

            if (showFullPlayer) {
                FullPlayerHost(viewModel, isDarkMode) { showFullPlayer = false }
            }

            updateInfoState?.let { info ->
                UpdateAvailableDialog(
                    updateInfo = info,
                    onDismiss = { updateInfoState = null }
                )
            }
        }
    }
}

@Composable
private fun MiniPlayerHost(viewModel: LibraryViewModel, onExpand: () -> Unit) {
    val state by viewModel.playerState.collectAsStateWithLifecycle()
    val song = state.currentSong
    if (song == null) PlaybackStatusBanner(state, viewModel.playerHandler::retryPlayback)
    else MiniPlayerCard(state, onExpand, viewModel::onTogglePlayPause,
        viewModel.playerHandler::playNext, viewModel.playerHandler::retryPlayback,
        artwork = { MiniPlayerSongCover(song) })
}

@Composable
private fun FullPlayerHost(viewModel: LibraryViewModel, isDarkMode: Boolean, onDismiss: () -> Unit) {
    val state by viewModel.playerState.collectAsStateWithLifecycle()
    val timer by viewModel.playerHandler.sleepTimerState.collectAsStateWithLifecycle()
    val favorites by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val song = state.currentSong
    FullPlayerSheet(isDarkMode = isDarkMode, playerState = state,
        currentTimerMinutes = timer.remainingMinutes, sleepTimerState = timer,
        onStartTimer = viewModel.playerHandler::startSleepTimer,
        onCancelTimer = viewModel.playerHandler::cancelSleepTimer,
        isFavorite = song?.id in favorites, onToggleFavorite = { song?.let { viewModel.toggleFavorite(it.id) } },
        onDismiss = onDismiss, onTogglePlayPause = viewModel::onTogglePlayPause,
        onSeekTo = viewModel.playerHandler::seekTo, onNext = viewModel.playerHandler::playNext,
        onPrevious = viewModel.playerHandler::playPrevious, onRewind = { viewModel.playerHandler.seekRewind() },
        onForward = { viewModel.playerHandler.seekForward() }, onToggleShuffle = viewModel.playerHandler::toggleShuffle,
        onRetry = viewModel.playerHandler::retryPlayback,
        inspectAudio = viewModel::inspectAudio,
        onEditTags = { title, artist, album -> song?.let { viewModel.updateSongTags(it, title, artist, album) } })
}

@Composable
private fun MiniPlayerSongCover(song: Song) {
    val context = LocalContext.current
    var cachedCoverFile by remember(song.id) { mutableStateOf<File?>(null) }

    LaunchedEffect(song.id) {
        cachedCoverFile = CoverCacheManager.getOrFetchCover(
            context = context,
            songId = song.id,
            uri = song.albumArtUri,
            path = song.path
        )
    }

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (cachedCoverFile != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(cachedCoverFile)
                    .crossfade(LocalAppearance.current.motionEnabled)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (song.albumArtUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(song.albumArtUri)
                    .crossfade(LocalAppearance.current.motionEnabled)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
