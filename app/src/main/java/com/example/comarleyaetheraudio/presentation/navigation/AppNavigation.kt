package com.example.comarleyaetheraudio.presentation.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.comarleyaetheraudio.data.local.entity.FolderEntity
import com.example.comarleyaetheraudio.domain.model.AudioPlayerState
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.home.HomeScreen
import com.example.comarleyaetheraudio.presentation.library.LibraryScreen
import com.example.comarleyaetheraudio.presentation.library.LibraryViewModel
import com.example.comarleyaetheraudio.presentation.profile.ProfileScreen
import com.example.comarleyaetheraudio.presentation.search.SearchScreen
import com.example.comarleyaetheraudio.presentation.settings.AudioFxScreen
import com.example.comarleyaetheraudio.presentation.settings.SettingsScreen
import com.example.comarleyaetheraudio.ui.theme.AppTheme

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: LibraryViewModel,
    songs: List<Song>,
    artistGrouped: Map<String, List<Song>>,
    playlists: List<Playlist>,
    favoriteIds: List<Long>,
    folders: List<FolderEntity>,
    playerState: AudioPlayerState,
    currentTheme: AppTheme,
    isDarkMode: Boolean
) {
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
        composable(Screen.Home.route) {
            HomeScreen(
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
            LibraryScreen(
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
                    viewModel.addSongsToPlaylist(playlistId, listOf(songId))
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
                songs = songs,
                isDarkMode = isDarkMode,
                onSongClick = { song -> viewModel.playerHandler.playSong(song, songs) }
            )
        }

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
}