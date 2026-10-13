package com.example.comarleyaetheraudio.domain.model

/** Library data presented to the UI; not a Room entity. */
data class LibraryFolder(
    val uriString: String,
    val name: String,
    val path: String,
    val songCount: Int = 0,
    val lastScannedMs: Long = 0
)

interface FolderImportResult {
    val added: Int
    val alreadyPresent: Int
}
