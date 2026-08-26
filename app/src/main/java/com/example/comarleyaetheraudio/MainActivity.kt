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
import com.example.comarleyaetheraudio.presentation.folders.FoldersScreen
import com.example.comarleyaetheraudio.presentation.home.HomeScreen
import com.example.comarleyaetheraudio.presentation.library.LibraryViewModel
import com.example.comarleyaetheraudio.presentation.library.PlaylistDetailScreen
import com.example.comarleyaetheraudio.presentation.library.PlaylistsScreen
import com.example.comarleyaetheraudio.presentation.library.SongsScreen
import com.example.comarleyaetheraudio.presentation.player.FullPlayerSheet
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
            ComarleyjesusaetheraudioTheme(darkTheme = isDarkMode) {
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
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val songs by viewModel.songs.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

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
        val currentCode = 3 // versionCode v2.1.0
        val info = UpdateChecker.checkForUpdates(currentCode)
        if (info != null) {
            updateInfoState = info
        }
    }

    val screens = listOf(
        Screen.Home,
        Screen.Songs,
        Screen.Playlists,
        Screen.Folders,
        Screen.Settings
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
                            navController.navigate(Screen.Songs.route)
                        }
                    )
                }

                composable(Screen.Songs.route) {
                    val favoriteIds by viewModel.favoriteIds.collectAsState()

                    SongsScreen(
                        songs = songs,
                        playlists = playlists,
                        favoriteIds = favoriteIds,
                        onSongClick = { selectedSong ->
                            viewModel.playerHandler.playSong(selectedSong, songs)
                        },
                        onToggleFavorite = { song -> viewModel.toggleFavorite(song.id) },
                        onAddToPlaylist = { playlistId, songId -> viewModel.addSongToPlaylist(playlistId, songId) }
                    )
                }

                composable(Screen.Playlists.route) {
                    var showCreateDialog by remember { mutableStateOf(false) }

                    PlaylistsScreen(
                        playlists = playlists,
                        onPlaylistClick = { playlist ->
                            navController.navigate(Screen.PlaylistDetail.createRoute(playlist.id))
                        },
                        onCreatePlaylistClick = { showCreateDialog = true }
                    )

                    if (showCreateDialog) {
                        CreatePlaylistDialog(
                            onDismiss = { showCreateDialog = false },
                            onCreate = { name -> viewModel.createPlaylist(name) }
                        )
                    }
                }

                composable(
                    route = Screen.PlaylistDetail.route,
                    arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L
                    val selectedPlaylist = playlists.find { it.id == playlistId }
                    val playlistSongs = remember(selectedPlaylist) {
                        selectedPlaylist?.songs ?: emptyList()
                    }

                    if (selectedPlaylist != null) {
                        PlaylistDetailScreen(
                            playlist = selectedPlaylist,
                            playlistSongs = playlistSongs,
                            onBackClick = { navController.popBackStack() },
                            onSongClick = { song -> viewModel.playerHandler.playSong(song, playlistSongs) },
                            onPlayAllClick = {
                                if (playlistSongs.isNotEmpty()) {
                                    viewModel.playerHandler.playSong(playlistSongs.first(), playlistSongs)
                                }
                            }
                        )
                    }
                }

                composable(Screen.Folders.route) {
                    FoldersScreen(
                        folders = folders,
                        onAddFolder = { uri -> viewModel.onAddFolder(uri) },
                        onRemoveFolder = { folderUri -> viewModel.onRemoveFolder(folderUri) },
                        onFolderClick = { folderPath ->
                            navController.navigate(Screen.FolderDetail.createRoute(folderPath))
                        }
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
                    val folderName = remember(folderPath) { folderPath.substringAfterLast("/") }

                    FolderDetailScreen(
                        folderName = if (folderName.isNotEmpty()) folderName else "Carpeta",
                        songs = songsInFolder,
                        onBackClick = { navController.popBackStack() },
                        onSongClick = { selectedSong ->
                            viewModel.playerHandler.playSong(selectedSong, songsInFolder)
                        }
                    )
                }

                composable(Screen.AudioFx.route) {
                    AudioFxScreen(
                        playerHandler = viewModel.playerHandler,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = { viewModel.onToggleDarkMode(it) },
                        onNavigateToAudioFx = { navController.navigate(Screen.AudioFx.route) }
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
                            navController.context.startActivity(intent)
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