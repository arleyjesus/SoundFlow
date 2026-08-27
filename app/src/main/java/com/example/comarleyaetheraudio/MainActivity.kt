package com.example.comarleyaetheraudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.comarleyaetheraudio.data.local.FolderScanner
import com.example.comarleyaetheraudio.data.local.MusicDatabase
import com.example.comarleyaetheraudio.data.player.AudioPlayerHandler
import com.example.comarleyaetheraudio.data.repository.AudioRepositoryImpl
import com.example.comarleyaetheraudio.data.repository.SettingsRepository
import com.example.comarleyaetheraudio.presentation.Screen
import com.example.comarleyaetheraudio.presentation.components.MiniPlayer
import com.example.comarleyaetheraudio.presentation.home.HomeScreen
import com.example.comarleyaetheraudio.presentation.library.LibraryScreen
import com.example.comarleyaetheraudio.presentation.library.LibraryViewModel
import com.example.comarleyaetheraudio.presentation.player.FullPlayerSheet
import com.example.comarleyaetheraudio.presentation.profile.ProfileScreen
import com.example.comarleyaetheraudio.presentation.search.SearchScreen
import com.example.comarleyaetheraudio.presentation.settings.AudioFxScreen
import com.example.comarleyaetheraudio.presentation.settings.SettingsScreen
import com.example.comarleyaetheraudio.ui.theme.ComarleyjesusaetheraudioTheme
import com.example.comarleyaetheraudio.ui.theme.getThemeColors

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: LibraryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = MusicDatabase.getDatabase(applicationContext)
        val scanner = FolderScanner(applicationContext)

        // 1. Aquí solo le pasamos 3 parámetros (sin playlistDao)
        val repository = AudioRepositoryImpl(applicationContext, database.musicDao(), scanner)

        val playerHandler = AudioPlayerHandler(applicationContext)
        val settingsRepository = SettingsRepository(applicationContext)

        // 2. Aquí también solo pasamos 3 parámetros
        viewModel = LibraryViewModel(repository, playerHandler, settingsRepository)

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val currentTheme by viewModel.currentTheme.collectAsState()
            val themeColors = remember(currentTheme, isDarkMode) { getThemeColors(currentTheme, isDarkMode) }

            ComarleyjesusaetheraudioTheme(darkTheme = isDarkMode) {
                // Aplicamos los colores del tema dinámico al esquema del sistema
                MaterialTheme(
                    colorScheme = MaterialTheme.colorScheme.copy(
                        primary = themeColors.primary,
                        primaryContainer = themeColors.primaryContainer,
                        background = themeColors.background,
                        surface = themeColors.surface,
                        surfaceVariant = themeColors.surfaceVariant
                    )
                ) {
                    MainAppStructure(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppStructure(viewModel: LibraryViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val songs by viewModel.songs.collectAsState()
    val artistGrouped by viewModel.artistGrouped.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()

    var showFullPlayer by remember { mutableStateOf(false) }

    val bottomNavigationScreens = listOf(
        Screen.Home,
        Screen.Library,
        Screen.Search,
        Screen.Profile
    )

    Scaffold(
        bottomBar = {
            Column {
                playerState.currentSong?.let { song ->
                    Box(modifier = Modifier.clickable { showFullPlayer = true }) {
                        MiniPlayer(
                            song = song,
                            isPlaying = playerState.isPlaying,
                            isShuffleEnabled = playerState.isShuffleEnabled, // PASS DE ESTADO
                            artworkData = playerState.artworkData,
                            onTogglePlayPause = { viewModel.onTogglePlayPause() },
                            onNext = { viewModel.playerHandler.playNext() },
                            onPrevious = { viewModel.playerHandler.playPrevious() }
                        )
                    }
                }

                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    bottomNavigationScreens.forEach { screen ->
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
                enterTransition = {
                    fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing)) +
                            scaleIn(initialScale = 0.95f, animationSpec = tween(280, easing = FastOutSlowInEasing))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                            scaleOut(targetScale = 1.03f, animationSpec = tween(220, easing = FastOutSlowInEasing))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing)) +
                            scaleIn(initialScale = 1.03f, animationSpec = tween(280, easing = FastOutSlowInEasing))
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                            scaleOut(targetScale = 0.95f, animationSpec = tween(220, easing = FastOutSlowInEasing))
                }
            ) {
                // 1. INICIO
                composable(Screen.Home.route) {
                    HomeScreen(
                        playerState = playerState,
                        allSongs = songs,
                        playlists = playlists,
                        favoriteIds = favoriteIds,
                        onSongClick = { selectedSong ->
                            viewModel.playerHandler.playSong(selectedSong, songs)
                        },
                        onFavoritesClick = {
                            navController.navigate(Screen.Library.route)
                        },
                        onCreatePlaylist = { name -> viewModel.createPlaylist(name) },
                        onDeletePlaylist = { playlist -> viewModel.deletePlaylist(playlist) }
                    )
                }

                // 2. BIBLIOTECA
                composable(Screen.Library.route) {
                    LibraryScreen(
                        songs = songs,
                        artistGrouped = artistGrouped,
                        playlists = playlists,
                        favoriteIds = favoriteIds,
                        onSongClick = { song -> viewModel.playerHandler.playSong(song, songs) },
                        onToggleFavorite = { song -> viewModel.toggleFavorite(song.id) },
                        onAddToPlaylist = { playlistId, songId -> viewModel.addSongToPlaylist(playlistId, songId) },
                        onCreatePlaylist = { name -> viewModel.createPlaylist(name) },
                        onRenamePlaylist = { playlist, newName -> viewModel.renamePlaylist(playlist.id, newName) },
                        onDeletePlaylist = { playlist -> viewModel.deletePlaylist(playlist) }
                    )
                }

                // 3. BUSCAR
                composable(Screen.Search.route) {
                    SearchScreen(
                        songs = songs,
                        onSongClick = { song -> viewModel.playerHandler.playSong(song, songs) }
                    )
                }

                // 4. PERFIL
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        currentTheme = currentTheme,
                        isDarkMode = isDarkMode,
                        folders = folders,
                        onSelectTheme = { theme -> viewModel.onSelectTheme(theme) },
                        onToggleDarkMode = { enabled -> viewModel.onToggleDarkMode(enabled) },
                        onAddFolder = { uri -> viewModel.onAddFolder(uri) },
                        onRemoveFolder = { path -> viewModel.onRemoveFolder(path) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateToAudioFx = { navController.navigate(Screen.AudioFx.route) }
                    )
                }

                // RUTAS SECUNDARIAS
                // DENTRO DE MAINACTIVITY.KT EN composable(Screen.Settings.route):
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onNavigateToAudioFx = { navController.navigate(Screen.AudioFx.route) }
                    )
                }

                composable(Screen.AudioFx.route) {
                    AudioFxScreen(
                        playerHandler = viewModel.playerHandler,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            if (showFullPlayer) {
                val currentSong = playerState.currentSong
                val isCurrentFavorite = currentSong?.let { favoriteIds.contains(it.id) } ?: false

                FullPlayerSheet(
                    playerState = playerState,
                    isFavorite = isCurrentFavorite,
                    onToggleFavorite = {
                        currentSong?.let { song -> viewModel.toggleFavorite(song.id) }
                    },
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
        }
    }
}