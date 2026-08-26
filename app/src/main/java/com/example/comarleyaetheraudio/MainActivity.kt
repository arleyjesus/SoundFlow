package com.example.comarleyaetheraudio

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.comarleyaetheraudio.data.local.FolderScanner
import com.example.comarleyaetheraudio.data.local.MusicDatabase
import com.example.comarleyaetheraudio.data.player.AudioPlayerHandler
import com.example.comarleyaetheraudio.data.remote.UpdateChecker
import com.example.comarleyaetheraudio.data.remote.UpdateInfo
import com.example.comarleyaetheraudio.data.repository.AudioRepositoryImpl
import com.example.comarleyaetheraudio.data.repository.SettingsRepository
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.Screen
import com.example.comarleyaetheraudio.presentation.components.ChangelogDialog
import com.example.comarleyaetheraudio.presentation.components.CreatePlaylistDialog
import com.example.comarleyaetheraudio.presentation.components.MiniPlayer
import com.example.comarleyaetheraudio.presentation.folders.FolderDetailScreen
import com.example.comarleyaetheraudio.presentation.home.HomeScreen
import com.example.comarleyaetheraudio.presentation.library.LibraryScreen
import com.example.comarleyaetheraudio.presentation.library.LibraryViewModel
import com.example.comarleyaetheraudio.presentation.player.FullPlayerSheet
import com.example.comarleyaetheraudio.presentation.profile.ProfileScreen
import com.example.comarleyaetheraudio.presentation.search.SearchScreen
import com.example.comarleyaetheraudio.presentation.settings.AudioFxScreen
import com.example.comarleyaetheraudio.presentation.settings.SettingsScreen
import com.example.comarleyaetheraudio.ui.theme.ComarleyjesusaetheraudioTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: LibraryViewModel
    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = MusicDatabase.getDatabase(applicationContext)
        val scanner = FolderScanner(applicationContext)
        val repository = AudioRepositoryImpl(applicationContext, database.musicDao(), scanner)
        val playerHandler = AudioPlayerHandler(applicationContext)
        settingsRepository = SettingsRepository(applicationContext)

        viewModel = LibraryViewModel(repository, playerHandler, settingsRepository, database.playlistDao())

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val currentTheme by viewModel.currentTheme.collectAsState()

            ComarleyjesusaetheraudioTheme(darkTheme = isDarkMode, appTheme = currentTheme) {
                MainAppStructure(
                    viewModel = viewModel,
                    settingsRepository = settingsRepository
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppStructure(
    viewModel: LibraryViewModel,
    settingsRepository: SettingsRepository
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val songs by viewModel.songs.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()

    var showFullPlayer by remember { mutableStateOf(false) }
    var showChangelogDialog by remember { mutableStateOf(false) }
    var updateInfoState by remember { mutableStateOf<UpdateInfo?>(null) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        settingsRepository.showChangelog.collect { shouldShow ->
            if (shouldShow) showChangelogDialog = true
        }
    }

    LaunchedEffect(Unit) {
        val currentCode = 4
        val info = UpdateChecker.checkForUpdates(currentCode)
        if (info != null) {
            updateInfoState = info
        }
    }

    val screens = listOf(
        Screen.Home,
        Screen.Library,
        Screen.Search,
        Screen.Profile
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text("SoundFlow") }) },
        bottomBar = {
            Column {
                playerState.currentSong?.let { song ->
                    Box(modifier = Modifier.clickable { showFullPlayer = true }) {
                        MiniPlayer(
                            song = song,
                            isPlaying = playerState.isPlaying,
                            artworkData = playerState.artworkData,
                            onTogglePlayPause = { viewModel.onTogglePlayPause() }
                        )
                    }
                }

                NavigationBar {
                    screens.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
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
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { fadeOut() },
                popEnterTransition = { fadeIn() },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) {
                composable(Screen.Home.route) {
                    val favoriteIds by viewModel.favoriteIds.collectAsState()

                    HomeScreen(
                        playerState = playerState,
                        allSongs = songs,
                        favoriteIds = favoriteIds,
                        onSongClick = { selectedSong ->
                            viewModel.playerHandler.playSong(selectedSong, songs)
                        },
                        onFavoritesClick = {
                            navController.navigate(Screen.Library.route)
                        }
                    )
                }

                composable(Screen.Library.route) {
                    val favoriteIds by viewModel.favoriteIds.collectAsState()

                    LibraryScreen(
                        songs = songs,
                        playlists = playlists,
                        folders = folders,
                        favoriteIds = favoriteIds,
                        onSongClick = { selectedSong -> viewModel.playerHandler.playSong(selectedSong, songs) },
                        onToggleFavorite = { song -> viewModel.toggleFavorite(song.id) },
                        onAddToPlaylist = { playlistId, songId -> viewModel.addSongToPlaylist(playlistId, songId) },
                        onFolderClick = { folderPath -> navController.navigate(Screen.FolderDetail.createRoute(folderPath)) },
                        onAddFolder = { uri -> viewModel.onAddFolder(uri) },
                        onRemoveFolder = { uriString -> viewModel.onRemoveFolder(uriString) },
                        onPlaylistClick = { playlist -> navController.navigate(Screen.PlaylistDetail.createRoute(playlist.id)) },
                        onCreatePlaylistClick = { /* Diálogo de crear */ }
                    )
                }

                composable(Screen.Search.route) {
                    SearchScreen(
                        songs = songs,
                        onSongClick = { selectedSong -> viewModel.playerHandler.playSong(selectedSong, songs) }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateToAudioFx = { navController.navigate(Screen.AudioFx.route) }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        isDarkMode = isDarkMode,
                        currentAppTheme = currentTheme,
                        onToggleDarkMode = { viewModel.onToggleDarkMode(it) },
                        onSelectTheme = { viewModel.onSelectTheme(it) },
                        onNavigateToAudioFx = { navController.navigate(Screen.AudioFx.route) }
                    )
                }

                composable(Screen.AudioFx.route) {
                    AudioFxScreen(
                        playerHandler = viewModel.playerHandler,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.FolderDetail.route,
                    arguments = listOf(navArgument("folderPath") { type = NavType.StringType })
                ) { backStackEntry ->
                    val encodedPath = backStackEntry.arguments?.getString("folderPath") ?: ""
                    val folderPath = Uri.decode(encodedPath) ?: ""
                    val songsInFolder = remember(folderPath, songs) {
                        songs.filter { song ->
                            song.path.contains(folderPath) || song.contentUri.toString().contains(folderPath)
                        }
                    }

                    FolderDetailScreen(
                        folderName = folderPath.substringAfterLast("/").ifEmpty { "Carpeta" },
                        songs = songsInFolder,
                        onBackClick = { navController.popBackStack() },
                        onSongClick = { selectedSong ->
                            viewModel.playerHandler.playSong(selectedSong, songsInFolder)
                        }
                    )
                }
            }

            if (showFullPlayer) {
                val favoriteIds by viewModel.favoriteIds.collectAsState()
                val currentSong = playerState.currentSong
                val isCurrentFavorite = currentSong?.let { favoriteIds.contains(it.id) } ?: false

                FullPlayerSheet(
                    playerState = playerState,
                    isFavorite = isCurrentFavorite,
                    onToggleFavorite = { currentSong?.let { viewModel.toggleFavorite(it.id) } },
                    onDismiss = { showFullPlayer = false },
                    onTogglePlayPause = { viewModel.onTogglePlayPause() },
                    onSeekTo = { pos -> viewModel.playerHandler.seekTo(pos) },
                    onNext = { viewModel.playerHandler.playNext() },
                    onPrevious = { viewModel.playerHandler.playPrevious() },
                    onRewind = { viewModel.playerHandler.seekRewind() },
                    onForward = { viewModel.playerHandler.seekForward() },
                    onToggleShuffle = { viewModel.playerHandler.toggleShuffle() },
                    onEditTags = { newTitle, newArtist, newAlbum ->
                        currentSong?.let { song ->
                            viewModel.updateSongTags(song, newTitle, newArtist, newAlbum)
                        }
                    }
                )
            }

            if (showChangelogDialog) {
                ChangelogDialog(
                    onDismiss = {
                        showChangelogDialog = false
                        coroutineScope.launch {
                            settingsRepository.markChangelogAsShown()
                        }
                    }
                )
            }

            updateInfoState?.let { update ->
                AlertDialog(
                    onDismissRequest = { updateInfoState = null },
                    title = { Text("¡Nueva versión ${update.versionName} disponible! 🚀") },
                    text = { Text(update.changelog) },
                    confirmButton = {
                        Button(onClick = {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                Uri.parse(update.apkUrl)
                            )
                            context.startActivity(intent)
                            updateInfoState = null
                        }) {
                            Text("Descargar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { updateInfoState = null }) {
                            Text("Más tarde")
                        }
                    }
                )
            }
        }
    }
}