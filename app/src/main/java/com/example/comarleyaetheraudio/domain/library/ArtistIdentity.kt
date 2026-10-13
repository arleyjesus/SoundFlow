package com.example.comarleyaetheraudio.domain.library

import java.text.Normalizer
import java.util.Locale

fun artistNameKey(value: String): String = Normalizer.normalize(value.trim(), Normalizer.Form.NFKC)
    .lowercase(Locale.ROOT).replace(Regex("\\s+"), " ")

fun sameArtistName(requested: String, candidate: String): Boolean {
    val key = artistNameKey(requested)
    return key.isNotEmpty() && key !in setOf("unknown", "<unknown>", "desconocido", "artista desconocido") &&
        key == artistNameKey(candidate)
}
