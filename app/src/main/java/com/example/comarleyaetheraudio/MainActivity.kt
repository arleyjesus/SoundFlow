package com.example.comarleyaetheraudio

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.comarleyaetheraudio.presentation.library.SongsScreen
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.comarleyaetheraudio.presentation.components.CreatePlaylistDialog
import com.example.comarleyaetheraudio.presentation.library.PlaylistsScreen
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import com.example.comarleyaetheraudio.presentation.library.PlaylistsScreen
import com.example.comarleyaetheraudio.presentation.components.CreatePlaylistDialog
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
import com.example.comarleyaetheraudio.data.repository.AudioRepositoryImpl
import com.example.comarleyaetheraudio.data.repository.SettingsRepository
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.Screen
import com.example.comarleyaetheraudio.presentation.components.CreatePlaylistDialog
import com.example.comarleyaetheraudio.presentation.components.MiniPlayer
import com.example.comarleyaetheraudio.presentation.folders.FolderDetailScreen
import com.example.comarleyaetheraudio.presentation.folders.FoldersScreen
import com.example.comarleyaetheraudio.presentation.home.HomeScreen
import com.example.comarleyaetheraudio.presentation.library.LibraryViewModel
import com.example.comarleyaetheraudio.presentation.library.SongsScreen
import com.example.comarleyaetheraudio.presentation.player.FullPlayerSheet
import com.example.comarleyaetheraudio.presentation.settings.AudioFxScreen
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

        viewModel = LibraryViewModel(repository, playerHandler, settingsRepository, database.playlistDao())

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
        Screen.Playlists,
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
                startDestination = Screen.Home.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { fadeOut() },
                popEnterTransition = { fadeIn() },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) {
                composable(Screen.AudioFx.route) {
                    AudioFxScreen(
                        playerHandler = viewModel.playerHandler,
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable(Screen.Folders.route) {
                    FoldersScreen(
                        folders = folders,
                        onAddFolder = { uri -> viewModel.onAddFolder(uri) },
                        onRemoveFolder = { folderUri -> viewModel.onRemoveFolder(folderUri) },
                        onFolderClick = { folderPath ->
                            // IMPORTANTE: Encode seguro para evitar cierres de la app por barras '/' en la ruta
                            val encodedPath = Uri.encode(folderPath)
                            navController.navigate(Screen.FolderDetail.createRoute(encodedPath))
                        }
                    )
                }

                // DENTRO DE TU NAVHOST / MAIN NAVIGATION:

                composable(Screen.Playlists.route) {
                    // 1. Recopilas el estado de las playlists en vivo desde el ViewModel
                    val playlists by viewModel.playlists.collectAsState()

                    // 2. Estado para mostrar u ocultar el diálogo de creación
                    var showCreateDialog by remember { mutableStateOf(false) }

                    // 3. Renderizas la pantalla pasándole los datos y las acciones
                    PlaylistsScreen(
                        playlists = playlists,
                        onPlaylistClick = { playlist ->
                            // TODO: Navegar al detalle de la playlist seleccionada
                        },
                        onCreatePlaylistClick = {
                            showCreateDialog = true
                        }
                    )

                    // 4. Muestras el diálogo cuando el usuario pulsa el botón '+'
                    if (showCreateDialog) {
                        CreatePlaylistDialog(
                            onDismiss = { showCreateDialog = false },
                            onCreate = { name ->
                                viewModel.createPlaylist(name)
                            }
                        )
                    }
                }

                // 1. PANTALLA DE INICIO CORREGIDA
                composable(Screen.Home.route) {
                    val favoriteIds by viewModel.favoriteIds.collectAsState()
                    val recentSongs by viewModel.recentSongs.collectAsState() // <--- NUEVO ESTADO OPTIMIZADO

                    HomeScreen(
                        playerState = playerState,
                        recentSongs = recentSongs, // Pasa la lista procesada
                        favoriteIds = favoriteIds,
                        onSongClick = { selectedSong ->
                            viewModel.playerHandler.playSong(selectedSong, songs)
                        },
                        onFavoritesClick = {
                            navController.navigate("favorites_route")
                        }
                    )
                }

                // 2. PANTALLA DE CANCIONES CORREGIDA
                composable(Screen.Songs.route) {
                    val songs by viewModel.songs.collectAsState()
                    val playlists by viewModel.playlists.collectAsState()
                    val favoriteIds by viewModel.favoriteIds.collectAsState()

                    SongsScreen(
                        songs = songs,
                        playlists = playlists,
                        favoriteIds = favoriteIds,
                        onSongClick = { song ->
                            // CORRECCIÓN: Le pasamos la cola de reproducción completa
                            viewModel.playerHandler.playSong(song, songs)
                        },
                        onToggleFavorite = { song ->
                            viewModel.toggleFavorite(song.id)
                        },
                        onAddToPlaylist = { playlistId, songId ->
                            viewModel.addSongToPlaylist(playlistId, songId)
                        }
                    )
                }

                // 3. NUEVA RUTA PARA TUS FAVORITOS (Reutiliza SongsScreen)
                composable("favorites_route") {
                    val songs by viewModel.songs.collectAsState()
                    val playlists by viewModel.playlists.collectAsState()
                    val favoriteIds by viewModel.favoriteIds.collectAsState()

                    // Filtramos solo las que son favoritas
                    val favoriteSongs = songs.filter { favoriteIds.contains(it.id) }

                    SongsScreen(
                        songs = favoriteSongs,
                        playlists = playlists,
                        favoriteIds = favoriteIds,
                        onSongClick = { song ->
                            viewModel.playerHandler.playSong(song, favoriteSongs)
                        },
                        onToggleFavorite = { song ->
                            viewModel.toggleFavorite(song.id)
                        },
                        onAddToPlaylist = { playlistId, songId ->
                            viewModel.addSongToPlaylist(playlistId, songId)
                        }
                    )
                }
                composable(
                    route = Screen.FolderDetail.route,
                    arguments = listOf(navArgument("folderPath") { type = NavType.StringType })
                ) { backStackEntry ->
                    val encodedPath = backStackEntry.arguments?.getString("folderPath") ?: ""
                    val folderPath = Uri.decode(encodedPath) ?: ""

                    // Memoriza el filtro para que no se recalculen las canciones al cambiar de estado visual
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
                // RUTA DE AJUSTES QUE HACÍA FALTA
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = { viewModel.onToggleDarkMode(it) },
                        onNavigateToAudioFx = { navController.navigate(Screen.AudioFx.route) } // CONEXIÓN DE NAVEGACIÓN
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