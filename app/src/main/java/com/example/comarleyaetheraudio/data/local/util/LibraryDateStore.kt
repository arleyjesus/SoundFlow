package com.example.comarleyaetheraudio.data.local.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import kotlin.coroutines.coroutineContext

private val Context.libraryDates by preferencesDataStore("library_dates")

/** Cosmetic metadata alongside Room: never changes song identities or relations. */
class LibraryDateStore(private val store: DataStore<Preferences>,
    private val readDate: suspend (String) -> Long) {
    constructor(context: Context) : this(context.applicationContext.libraryDates,
        { readLibraryDate(context.applicationContext, Uri.parse(it)) })
    private val lock = Mutex()

    suspend fun timestamps(uris: List<String>): Map<String, Long> = withContext(Dispatchers.IO) {
        lock.withLock {
            val snapshot = store.data.first()
            val pending = mutableMapOf<Preferences.Key<Long>, Long>()
            val result = uris.associateWith { uri ->
                coroutineContext.ensureActive()
                val key = longPreferencesKey("date_" + MessageDigest.getInstance("SHA-256")
                    .digest(songIdentity(uri).toString().toByteArray(Charsets.UTF_8))
                    .joinToString("") { "%02x".format(it) })
                snapshot[key] ?: pending.getOrPut(key) { readDate(uri).coerceAtLeast(0) }
            }
            // Never freeze an unavailable date: permissions/provider metadata may recover later.
            val valid = pending.filterValues { it > 0 }
            if (valid.isNotEmpty()) store.edit { prefs -> valid.forEach { (key, value) -> prefs[key] = value } }
            result
        }
    }
}

private fun readLibraryDate(context: Context, uri: Uri): Long {
    fun query(target: Uri, column: String, seconds: Boolean = false): Long = try {
        context.contentResolver.query(target, arrayOf(column), null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(column)
            if (!cursor.moveToFirst() || index < 0 || cursor.isNull(index)) 0L else {
                val raw = cursor.getLong(index)
                val time = if (seconds && raw in 1..Long.MAX_VALUE / 1000) raw * 1000 else if (seconds) 0 else raw
                if (time in 1..System.currentTimeMillis()) time else 0L
            }
        } ?: 0L
    } catch (e: CancellationException) { throw e } catch (_: Exception) { 0L }

    val mediaUri = if (uri.authority == MediaStore.AUTHORITY) uri else if (Build.VERSION.SDK_INT >= 29) {
        try { MediaStore.getMediaUri(context, uri) } catch (_: Exception) { null }
    } else null
    mediaUri?.let { query(it, MediaStore.MediaColumns.DATE_ADDED, seconds = true).takeIf { date -> date > 0 }?.let { return it } }
    // SAF exposes modification, not creation/download time. Freeze it to avoid reordering on tag edits.
    return query(uri, DocumentsContract.Document.COLUMN_LAST_MODIFIED)
}
