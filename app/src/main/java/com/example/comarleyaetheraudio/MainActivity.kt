package com.example.comarleyaetheraudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HomeMax
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.ManageSearch
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.comarleyaetheraudio.ui.theme.ComarleyjesusaetheraudioTheme
import com.example.comarleyaetheraudio.ui.theme.getThemeColors
import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: LibraryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = MusicDatabase.getDatabase(applicationContext)
        val scanner = FolderScanner(applicationContext)

        val repository = AudioRepositoryImpl(
            applicationContext,
            database.musicDao(),
            database.playlistDao(),
            scanner
        )

        val playerHandler = AudioPlayerHandler(applicationContext)
        val settingsRepository = SettingsRepository(applicationContext)

        viewModel = LibraryViewModel(repository, playerHandler, settingsRepository)

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val currentTheme by viewModel.currentTheme.collectAsState()
            val themeColors = remember(currentTheme, isDarkMode) { getThemeColors(currentTheme, isDarkMode) }

            ComarleyjesusaetheraudioTheme(darkTheme = isDarkMode, appTheme = currentTheme) {
                MaterialTheme(
                    colorScheme = MaterialTheme.colorScheme.copy(
                        primary = themeColors.primary,
                        primaryContainer = themeColors.primaryContainer,
                        background = if (isDarkMode) Color(0xFF09090B) else Color(0xFFFAFAFC),
                        surface = if (isDarkMode) Color(0xFF141417) else Color(0xFFF1F1F5),
                        surfaceVariant = themeColors.surfaceVariant
                    )
                ) {
                    MainAppStructure(viewModel = viewModel, isDarkMode = isDarkMode)
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

    val songs by viewModel.songs.collectAsState()
    val artistGrouped by viewModel.artistGrouped.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()

    var showFullPlayer by remember { mutableStateOf(false) }
    var updateInfoState by remember { mutableStateOf<GitHubUpdateChecker.UpdateInfo?>(null) }

    LaunchedEffect(Unit) {
        val info = GitHubUpdateChecker.checkForUpdates()
        if (info != null && info.hasUpdate) {
            updateInfoState = info
        }
    }

    val navContainerColor = if (isDarkMode) Color(0xFF09090B) else Color(0xFFFAFAFC)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(navContainerColor)
            ) {
                if (playerState.currentSong != null && !showFullPlayer) {
                    val cornerGradient = if (isDarkMode) {
                        listOf(
                            Color(0xFF09090B),
                            Color(0xFF2E1C40),
                            Color(0xFF221A16)
                        )
                    } else {
                        listOf(
                            Color(0xFFFAFAFC),
                            Color(0xFFF3E8FF),
                            Color(0xFFFFFDE7).copy(alpha = 0.20f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (isDarkMode) Modifier.shadow(4.dp, RoundedCornerShape(16.dp)) else Modifier)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { showFullPlayer = true },
                            color = Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier.background(
                                    Brush.horizontalGradient(colors = cornerGradient)
                                )
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        MiniPlayerSongCover(song = playerState.currentSong!!)

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = playerState.currentSong!!.title,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = playerState.currentSong!!.artist,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        IconButton(onClick = { viewModel.onTogglePlayPause() }) {
                                            Icon(
                                                imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                                contentDescription = "Play/Pause",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        IconButton(onClick = { viewModel.playerHandler.playNext() }) {
                                            Icon(
                                                imageVector = Icons.Rounded.SkipNext,
                                                contentDescription = "Siguiente",
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    val progress = if (playerState.duration > 0) {
                                        (playerState.currentPosition.toFloat() / playerState.duration.toFloat()).coerceIn(0f, 1f)
                                    } else 0f

                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(2.5.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                    )
                                }
                            }
                        }
                    }
                }

                NavigationBar(
                    containerColor = navContainerColor,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val navItems = listOf(
                        Pair(Screen.Home, Icons.Outlined.HomeMax),
                        Pair(Screen.Library, Icons.Outlined.LibraryMusic),
                        Pair(Screen.Search, Icons.Outlined.ManageSearch),
                        Pair(Screen.Profile, Icons.Outlined.PersonOutline)
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
                                unselectedIconColor = if (isDarkMode) Color.Gray else Color.DarkGray,
                                unselectedTextColor = if (isDarkMode) Color.Gray else Color.DarkGray,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
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
        Box(modifier = Modifier.padding(paddingValues)) {
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
                val currentSong = playerState.currentSong
                val isCurrentFavorite = currentSong?.let { favoriteIds.contains(it.id) } ?: false

                FullPlayerSheet(
                    isDarkMode = isDarkMode,
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
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (song.albumArtUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(song.albumArtUri)
                    .crossfade(true)
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