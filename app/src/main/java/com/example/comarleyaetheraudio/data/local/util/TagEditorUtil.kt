package com.example.comarleyaetheraudio.data.local.util

import com.example.comarleyaetheraudio.domain.model.Song
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File

object TagEditorUtil {
    fun editSongTags(song: Song, newTitle: String, newArtist: String, newAlbum: String): Boolean {
        return try {
            val file = File(song.path)
            // Verificamos que el archivo físico exista y tengamos permisos de escritura
            if (file.exists() && file.canWrite()) {
                val audioFile = AudioFileIO.read(file)
                val tag = audioFile.tagOrCreateAndSetDefault

                tag.setField(FieldKey.TITLE, newTitle)
                tag.setField(FieldKey.ARTIST, newArtist)
                tag.setField(FieldKey.ALBUM, newAlbum)

                audioFile.commit()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}