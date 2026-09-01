package com.example.comarleyaetheraudio.presentation.home.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import com.example.comarleyaetheraudio.domain.model.AudioPlayerState
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.library.PlaylistsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsBottomSheet(
    playlists: List<Playlist>,
    allAvailableSongs: List<Song>,
    playerState: AudioPlayerState = AudioPlayerState(),
    favoriteIds: List<Long> = emptyList(),
    onDismiss: () -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onDeletePlaylist: (Playlist) -> Unit,
    onAddSongsToPlaylist: (Long, List<Long>) -> Unit,
    onSongClick: (Song) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        PlaylistsScreen(
            playlists = playlists,
            allSongs = allAvailableSongs,
            playerState = playerState,
            favoriteIds = favoriteIds,
            onPlaylistClick = { playlist ->
                if (playlist.songs.isNotEmpty()) {
                    onSongClick(playlist.songs.first())
                }
                onDismiss()
            },
            onSongClick = { song, _ ->
                onSongClick(song)
                onDismiss()
            },
            onPlayAllPlaylist = { playlist, _ ->
                if (playlist.songs.isNotEmpty()) {
                    onSongClick(playlist.songs.first())
                }
                onDismiss()
            },
            onCreatePlaylistClick = onCreatePlaylist,
            onRenamePlaylist = { _, _ -> },
            onDeletePlaylist = onDeletePlaylist,
            onAddSongsToPlaylist = onAddSongsToPlaylist
        )
    }
}