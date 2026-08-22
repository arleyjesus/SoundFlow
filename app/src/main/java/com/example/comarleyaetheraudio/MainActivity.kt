package com.example.comarleyaetheraudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.Screen
import com.example.comarleyaetheraudio.presentation.components.MiniPlayer
import com.example.comarleyaetheraudio.presentation.components.SongItem
import com.example.comarleyaetheraudio.presentation.folders.FoldersScreen
import com.example.comarleyaetheraudio.presentation.home.HomeScreen
import com.example.comarleyaetheraudio.presentation.library.LibraryViewModel
import com.example.comarleyaetheraudio.presentation.settings.SettingsScreen

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: LibraryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialización de la base de datos local y dependencias
        val database = MusicDatabase.getDatabase(applicationContext)
        val scanner = FolderScanner(applicationContext)
        val repository = AudioRepositoryImpl(applicationContext, database.musicDao(), scanner)
        val playerHandler = AudioPlayerHandler(applicationContext)

        viewModel = LibraryViewModel(repository, playerHandler)

        setContent {
            MaterialTheme {
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

    val screens = listOf(
        Screen.Home,
        Screen.Songs,
        Screen.Folders,
        Screen.Settings
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aether Audio") }
            )
        },
        bottomBar = {
            Column {
                // MiniPlayer flotante si hay canción activa
                playerState.currentSong?.let { song ->
                    MiniPlayer(
                        song = song,
                        isPlaying = playerState.isPlaying,
                        onTogglePlayPause = { viewModel.onTogglePlayPause() }
                    )
                }

                // Menú de navegación inferior de 4 secciones
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
                    HomeScreen(
                        playerState = playerState,
                        onTogglePlayPause = { viewModel.onTogglePlayPause() }
                    )
                }
                composable(Screen.Songs.route) {
                    if (songs.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay canciones. Ve a 'Carpetas' para agregar tu música.")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(songs) { song: Song ->
                                SongItem(song = song, onClick = { viewModel.onSongClick(song) })
                            }
                        }
                    }
                }
                composable(Screen.Folders.route) {
                    FoldersScreen(
                        folders = folders,
                        onAddFolder = { uri -> viewModel.onAddFolder(uri) },
                        onRemoveFolder = { folderUri -> viewModel.onRemoveFolder(folderUri) }
                    )
                }
                composable(Screen.Settings.route) {
                    SettingsScreen()
                }
            }
        }
    }
}