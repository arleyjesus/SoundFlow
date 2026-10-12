package com.example.comarleyaetheraudio.testutil

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.example.comarleyaetheraudio.data.local.MusicDatabase
import com.example.comarleyaetheraudio.data.local.entity.SongEntity
import com.example.comarleyaetheraudio.data.local.util.FolderScanner
import com.example.comarleyaetheraudio.data.repository.AudioRepositoryImpl
import java.util.UUID

/** Base en disco aislada: nunca abre ni elimina soundflow_database. */
internal class RoomTestLibrary {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val name = "soundflow_phase1a_test_${UUID.randomUUID()}"

    var database: MusicDatabase = openDatabase()
        private set

    val repository: AudioRepositoryImpl
        get() = AudioRepositoryImpl(
            context, database.musicDao(), database.playlistDao(), FolderScanner(context)
        )

    fun reopen() {
        database.close()
        database = openDatabase()
    }

    fun close() {
        database.close()
        context.deleteDatabase(name)
    }

    private fun openDatabase(): MusicDatabase =
        MusicDatabase.buildDatabase(context, name)
}

internal fun testSong(id: Long, title: String = "Cancion $id", artist: String = "Artista $id") =
    SongEntity(
        id = id,
        title = title,
        artist = artist,
        album = "Album de prueba",
        genre = "Prueba",
        year = "2026",
        trackNumber = id.toString(),
        duration = 60_000L,
        contentUri = "content://soundflow.test/audio/$id",
        albumArtUri = null,
        size = 1024L,
        mimeType = "audio/mpeg",
        bitrate = 128,
        sampleRate = 44_100,
        isHiRes = false,
        path = "content://soundflow.test/audio/$id",
        folderUri = "content://soundflow.test/folder",
        folderName = "Pruebas"
    )
