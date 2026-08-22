package com.example.comarleyaetheraudio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey val uriString: String, // URI persistente de la carpeta elegida mediante SAF
    val name: String,
    val path: String,
    val songCount: Int = 0,
    val lastScannedMs: Long = System.currentTimeMillis()
)