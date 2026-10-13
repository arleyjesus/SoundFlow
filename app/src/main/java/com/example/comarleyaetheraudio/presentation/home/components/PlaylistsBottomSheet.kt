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
    onSongClick: (Song) -> Unit,
    onPlayContext: (Song, List<Song>, Boolean) -> Unit = { song, _, _ -> onSongClick(song) },
    onOpenPlaylist: ((Playlist) -> Unit)? = null,
    onRenamePlaylist: (Playlist, String) -> Unit = { _, _ -> }
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        PlaylistsScreen(
            playlists = playlists,
            allSongs = allAvailableSongs,
            playerState = playerState,
            favoriteIds = favoriteIds,
            onPlaylistClick = { playlist ->
                if (onOpenPlaylist != null) onOpenPlaylist(playlist)
                else playlist.songs.firstOrNull()?.let { onPlayContext(it, playlist.songs, false) }
                onDismiss()
            },
            onSongClick = { song, queue ->
                onPlayContext(song, queue, false)
                onDismiss()
            },
            onPlayAllPlaylist = { playlist, shuffle ->
                if (playlist.songs.isNotEmpty()) {
                    onPlayContext(if (shuffle) playlist.songs.random() else playlist.songs.first(), playlist.songs, shuffle)
                }
                onDismiss()
            },
            onCreatePlaylistClick = onCreatePlaylist,
            onRenamePlaylist = onRenamePlaylist,
            onDeletePlaylist = onDeletePlaylist,
            onAddSongsToPlaylist = onAddSongsToPlaylist
        )
    }
}
