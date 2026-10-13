package com.example.comarleyaetheraudio.data.local

data class SongImportResult(override val added: Int, override val alreadyPresent: Int) :
    com.example.comarleyaetheraudio.domain.model.FolderImportResult

class FolderAccessMismatchException : IllegalArgumentException("Selecciona la misma carpeta para restaurar el acceso")

class FolderImportConflictException : IllegalStateException(
    "La carpeta comparte canciones con otra carpeta registrada"
)
