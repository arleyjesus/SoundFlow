package com.example.comarleyaetheraudio.data.local

data class SongImportResult(val added: Int, val alreadyPresent: Int)

class FolderImportConflictException : IllegalStateException(
    "La carpeta comparte canciones con otra carpeta registrada"
)
