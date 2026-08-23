package com.example.comarleyaetheraudio

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.comarleyaetheraudio.data.repository.AudioRepositoryImpl
import com.example.comarleyaetheraudio.data.repository.SettingsRepository
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.Screen
import com.example.comarleyaetheraudio.presentation.components.MiniPlayer
import com.example.comarleyaetheraudio.presentation.folders.FolderDetailScreen
import com.example.comarleyaetheraudio.presentation.folders.FoldersScreen
import com.example.comarleyaetheraudio.presentation.home.HomeScreen
import com.example.comarleyaetheraudio.presentation.library.LibraryViewModel
import com.example.comarleyaetheraudio.presentation.library.SongsScreen
import com.example.comarleyaetheraudio.presentation.player.FullPlayerSheet
import com.example.comarleyaetheraudio.presentation.settings.SettingsScreen
import com.example.comarleyaetheraudio.ui.theme.ComarleyjesusaetheraudioTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: LibraryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = MusicDatabase.getDatabase(applicationContext)
        val scanner = FolderScanner(applicationContext)
        val repository = AudioRepositoryImpl(applicationContext, database.musicDao(), scanner)
        val playerHandler = AudioPlayerHandler(applicationContext)
        val settingsRepository = SettingsRepository(applicationContext)

        viewModel = LibraryViewModel(repository, playerHandler, settingsRepository)

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            ComarleyjesusaetheraudioTheme(darkTheme = isDarkMode) {
                MainAppStructure(viewModel = viewModel)
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
    val folders by viewModel.folders.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    var showFullPlayer by remember { mutableStateOf(false) }

    val screens = listOf(
        Screen.Home,
        Screen.Songs,
        Screen.Folders,
        Screen.Settings
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SoundFlow") }
            )
        },
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
                startDestination = Screen.Home.route
            ) {
                composable(Screen.Home.route) {
                    val favoriteIds by viewModel.favoriteIds.collectAsState()

                    HomeScreen(
                        playerState = playerState,
                        allSongs = songs,
                        favoriteIds = favoriteIds,
                        onSongClick = { selectedSong ->
                            val favoriteSongs = songs.filter { favoriteIds.contains(it.id) }
                            viewModel.playerHandler.playSong(selectedSong, favoriteSongs)
                        }
                    )
                }
                composable(Screen.Songs.route) {
                    SongsScreen(
                        songs = songs,
                        onSongClick = { selectedSong ->
                            viewModel.playerHandler.playSong(selectedSong, songs)
                        }
                    )
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
                ) { backStackEntry: NavBackStackEntry ->
                    val encodedPath: String = backStackEntry.arguments?.getString("folderPath") ?: ""
                    val folderPath: String = Uri.decode(encodedPath) ?: ""

                    val songsInFolder = songs.filter { song: Song ->
                        val songPath = song.path
                        val songUriString = song.contentUri.toString()
                        songPath.contains(folderPath) || songUriString.contains(folderPath)
                    }

                    val folderName = folderPath.substringAfterLast("/")

                    FolderDetailScreen(
                        folderName = if (folderName.isNotEmpty()) folderName else "Carpeta",
                        songsInFolder = songsInFolder,
                        onBackClick = { navController.popBackStack() },
                        onSongClick = { selectedSong ->
                            viewModel.playerHandler.playSong(selectedSong, songsInFolder)
                        }
                    )
                }
                // RUTA DE AJUSTES QUE HACÍA FALTA
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = { viewModel.onToggleDarkMode(it) }
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
                    onToggleFavorite = {
                        currentSong?.let { song ->
                            viewModel.toggleFavorite(song.id)
                        }
                    },
                    onDismiss = { showFullPlayer = false },
                    onTogglePlayPause = { viewModel.onTogglePlayPause() },
                    onSeekTo = { pos -> viewModel.playerHandler.seekTo(pos) },
                    onNext = { viewModel.playerHandler.playNext() },
                    onPrevious = { viewModel.playerHandler.playPrevious() },
                    onRewind = { viewModel.playerHandler.seekRewind() },
                    onForward = { viewModel.playerHandler.seekForward() },
                    onToggleShuffle = { viewModel.playerHandler.toggleShuffle() },

                    // NUEVA CONEXIÓN PARA EL EDITOR
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