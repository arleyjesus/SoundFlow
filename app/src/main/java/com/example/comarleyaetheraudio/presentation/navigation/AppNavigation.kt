package com.example.comarleyaetheraudio.presentation.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.comarleyaetheraudio.domain.model.LibraryFolder
import com.example.comarleyaetheraudio.domain.model.AudioPlayerState
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.home.HomeScreen
import com.example.comarleyaetheraudio.presentation.library.LibraryScreen
import com.example.comarleyaetheraudio.presentation.library.LibraryViewModel
import com.example.comarleyaetheraudio.presentation.library.SmartLibraryScreen
import com.example.comarleyaetheraudio.presentation.profile.ProfileScreen
import com.example.comarleyaetheraudio.presentation.search.SearchScreen
import com.example.comarleyaetheraudio.presentation.settings.AudioFxScreen
import com.example.comarleyaetheraudio.presentation.settings.SettingsScreen
import com.example.comarleyaetheraudio.ui.theme.AppTheme
import com.example.comarleyaetheraudio.ui.theme.LocalAppearance
import com.example.comarleyaetheraudio.presentation.settings.AppearanceScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: LibraryViewModel,
    songs: List<Song>,
    artistGrouped: Map<String, List<Song>>,
    playlists: List<Playlist>,
    favoriteIds: List<Long>,
    folders: List<LibraryFolder>,
    playerState: AudioPlayerState,
    currentTheme: AppTheme,
    isDarkMode: Boolean
) {
    val appearance = LocalAppearance.current
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            if (!appearance.motionEnabled) EnterTransition.None else
            fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing)) +
                    scaleIn(initialScale = 0.95f, animationSpec = tween(280, easing = FastOutSlowInEasing))
        },
        exitTransition = {
            if (!appearance.motionEnabled) ExitTransition.None else
            fadeOut(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                    scaleOut(targetScale = 1.03f, animationSpec = tween(220, easing = FastOutSlowInEasing))
        },
        popEnterTransition = {
            if (!appearance.motionEnabled) EnterTransition.None else
            fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing)) +
                    scaleIn(initialScale = 1.03f, animationSpec = tween(280, easing = FastOutSlowInEasing))
        },
        popExitTransition = {
            if (!appearance.motionEnabled) ExitTransition.None else
            fadeOut(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                    scaleOut(targetScale = 0.95f, animationSpec = tween(220, easing = FastOutSlowInEasing))
        }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onPlayContext = { song, queue, shuffle -> viewModel.playerHandler.playSong(song, queue, shuffle) },
                onRenamePlaylist = { playlist, name -> viewModel.renamePlaylist(playlist.id, name) },
                onNavigateToSmartLibrary = { navController.navigate(Screen.SmartLibrary.route) { launchSingleTop = true } },
                artistGrouped = artistGrouped,
                playerState = playerState,
                allSongs = songs,
                playlists = playlists,
                favoriteIds = favoriteIds,
                isDarkMode = isDarkMode,
                onSongClick = { selectedSong ->
                    viewModel.playerHandler.playSong(selectedSong, songs)
                },
                onSongClickWithPlaylist = { song, playlistSongs ->
                    viewModel.playerHandler.playSong(song, playlistSongs)
                },
                onFavoritesClick = {
                    navController.navigate(Screen.Library.route)
                },
                onCreatePlaylist = { name -> viewModel.createPlaylist(name) },
                onDeletePlaylist = { playlist -> viewModel.deletePlaylist(playlist) },
                onAddSongsToPlaylist = { playlistId, songIds ->
                    viewModel.addSongsToPlaylist(playlistId, songIds)
                }
            )
        }

        composable(Screen.Library.route) {
            val libraryOrder by viewModel.libraryOrder.collectAsStateWithLifecycle()
            LibraryScreen(
                libraryOrder = libraryOrder,
                onLibraryOrder = viewModel::onLibraryOrder,
                onPlayContext = { song, queue, shuffle -> viewModel.playerHandler.playSong(song, queue, shuffle) },
                songs = songs,
                artistGrouped = artistGrouped,
                playlists = playlists,
                favoriteIds = favoriteIds,
                playerState = playerState,
                isDarkMode = isDarkMode,
                onSongClick = { song ->
                    viewModel.playerHandler.playSong(song, songs)
                },
                onSongClickWithPlaylist = { song, playlistSongs ->
                    viewModel.playerHandler.playSong(song, playlistSongs)
                },
                onToggleFavorite = { song ->
                    viewModel.toggleFavorite(song.id)
                },
                onAddToPlaylist = { playlistId, songId ->
                    viewModel.addSongToPlaylist(playlistId, songId)
                },
                onAddSongsToPlaylist = { playlistId, songIds ->
                    viewModel.addSongsToPlaylist(playlistId, songIds)
                },
                onCreatePlaylist = { name ->
                    viewModel.createPlaylist(name)
                },
                onRenamePlaylist = { playlist, newName ->
                    viewModel.renamePlaylist(playlist.id, newName)
                },
                onDeletePlaylist = { playlist ->
                    viewModel.deletePlaylist(playlist)
                }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onPlayContext = { song, queue -> viewModel.playerHandler.playSong(song, queue) },
                songs = songs,
                isDarkMode = isDarkMode,
                onSongClick = { song -> viewModel.playerHandler.playSong(song, songs) }
            )
        }

        composable(Screen.Profile.route) {
            val isUpdatingFolders by viewModel.isUpdatingFolders.collectAsStateWithLifecycle()
            val folderMessage by viewModel.folderMessage.collectAsStateWithLifecycle()
            val folderAccessToRestore by viewModel.folderAccessToRestore.collectAsStateWithLifecycle()
            ProfileScreen(
                currentTheme = currentTheme,
                isDarkMode = isDarkMode,
                folders = folders,
                onSelectTheme = { theme -> viewModel.onSelectTheme(theme) },
                onToggleDarkMode = { enabled -> viewModel.onToggleDarkMode(enabled) },
                onAddFolder = { uri -> viewModel.onAddFolder(uri) },
                onRemoveFolder = { path -> viewModel.onRemoveFolder(path) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToAudioFx = { navController.navigate(Screen.AudioFx.route) },
                isUpdatingFolders = isUpdatingFolders,
                folderMessage = folderMessage,
                onDismissFolderMessage = viewModel::clearFolderMessage,
                folderAccessToRestore = folderAccessToRestore,
                onRestoreFolderAccess = viewModel::onRestoreFolderAccess,
                onNavigateToAppearance = { navController.navigate(Screen.Appearance.route) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNetworkFeature = viewModel::onNetworkFeature,
                onNavigateToAudioFx = { navController.navigate(Screen.AudioFx.route) },
                onNavigateToAppearance = { navController.navigate(Screen.Appearance.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SmartLibrary.route) {
            SmartLibraryScreen(songs, favoriteIds, playerState.currentSong?.id,
                onBack = { navController.popBackStack() },
                onPlaySession = { queue -> viewModel.playerHandler.playSong(queue.first(), queue) },
                onPlaySong = { song -> viewModel.playerHandler.playSong(song, listOf(song)) })
        }

        composable(Screen.Appearance.route) {
            AppearanceScreen(
                settings = appearance,
                onBack = { navController.popBackStack() },
                onTheme = viewModel::onSelectTheme,
                onDarkMode = viewModel::onToggleDarkMode,
                onAnimations = viewModel::onToggleAnimations,
                onTransparencies = viewModel::onToggleTransparencies,
                onFollowSystem = viewModel::onFollowSystemTheme,
                onReset = viewModel::onResetAppearance
            )
        }

        composable(Screen.AudioFx.route) {
            AudioFxScreen(
                playerHandler = viewModel.playerHandler,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
