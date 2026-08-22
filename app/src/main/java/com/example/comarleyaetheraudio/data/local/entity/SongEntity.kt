package com.example.comarleyaetheraudio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val year: String,
    val trackNumber: String,
    val duration: Long,
    val contentUri: String,
    val albumArtUri: String?,
    val size: Long,
    val mimeType: String,
    val bitrate: Int,
    val sampleRate: Int,
    val isHiRes: Boolean,
    val folderUri: String,
    val folderName: String
)