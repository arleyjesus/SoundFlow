package com.example.comarleyaetheraudio.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.comarleyaetheraudio.data.local.FolderScanner
import com.example.comarleyaetheraudio.data.local.dao.MusicDao
import com.example.comarleyaetheraudio.data.local.entity.FolderEntity
import com.example.comarleyaetheraudio.data.local.entity.SongEntity
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.domain.repository.AudioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AudioRepositoryImpl(
    private val context: Context,
    private val dao: MusicDao,
    private val scanner: FolderScanner
) : AudioRepository {

    override fun getSongsFlow(): Flow<List<Song>> {
        return dao.getAllSongs().map { entities -> entities.map { it.toDomainModel() } }
    }

    override fun getFoldersFlow(): Flow<List<FolderEntity>> {
        return dao.getAllFolders()
    }

    override suspend fun addAndScanFolder(folderUri: Uri) {
        val contentResolver = context.contentResolver
        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
        contentResolver.takePersistableUriPermission(folderUri, takeFlags)

        val folderDoc = DocumentFile.fromTreeUri(context, folderUri)
        val folderName = folderDoc?.name ?: "Carpeta Música"
        val uriString = folderUri.toString()

        // 1. ELIMINAR REGISTROS PREVIOS:
        // Si la carpeta ya existía, borramos sus canciones antiguas para evitar duplicados.
        dao.deleteSongsByFolder(uriString)

        // 2. ESCANEAR NUEVAMENTE E INSERTAR
        val songs = scanner.scanFolderUri(folderUri, folderName)
        if (songs.isNotEmpty()) {
            dao.insertSongs(songs)
        }

        // 3. ACTUALIZAR O REGISTRAR LA CARPETA
        val folderEntity = FolderEntity(
            uriString = uriString,
            name = folderName,
            path = folderUri.path ?: "",
            songCount = songs.size
        )
        dao.insertFolder(folderEntity)
    }

    override suspend fun removeFolder(folderUri: String) {
        dao.deleteSongsByFolder(folderUri)
        dao.deleteFolder(folderUri)
    }

    private fun SongEntity.toDomainModel(): Song {
        return Song(
            id = id,
            title = title,
            artist = artist,
            album = album,
            genre = genre,
            year = year,
            trackNumber = trackNumber,
            duration = duration,
            contentUri = Uri.parse(contentUri),
            albumArtUri = albumArtUri?.let { Uri.parse(it) },
            size = size,
            mimeType = mimeType,
            bitrate = bitrate,
            sampleRate = sampleRate,
            isHiRes = isHiRes,
            folderPath = folderName,
            folderUri = folderUri
        )
    }
}