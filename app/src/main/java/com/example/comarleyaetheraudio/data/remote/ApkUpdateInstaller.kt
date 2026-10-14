package com.example.comarleyaetheraudio.data.remote

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.comarleyaetheraudio.domain.playback.UpdatePackagePolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.UUID
import kotlin.coroutines.coroutineContext

object ApkUpdateInstaller {
    private val downloadLock = Mutex()
    private const val MAX_BYTES = 150L * 1024 * 1024

    suspend fun download(context: Context, info: GitHubUpdateChecker.UpdateInfo,
        onProgress: (Int) -> Unit): File = withContext(Dispatchers.IO) {
        downloadLock.withLock {
            val source = info.apkUrl ?: throw IOException("La publicación no incluye un APK único compatible.")
            check(UpdatePackagePolicy.officialAssetUrl(source)) { "Enlace de APK no autorizado." }
            val directory = File(context.cacheDir, "updates").apply { check(isDirectory || mkdirs()) }
            // Only our own updater cache. Keep the two latest complete candidates for installer hand-off.
            directory.listFiles { file -> file.isFile && file.name.matches(Regex("soundflow-[0-9a-f-]{36}\\.apk")) }
                ?.sortedByDescending { it.lastModified() }?.drop(2)?.forEach { it.delete() }
            val destination = File(directory, "soundflow-${UUID.randomUUID()}.apk")
            var completed = false
            val started = System.nanoTime()
            val taskContext = coroutineContext
            fun checkBudget() {
                taskContext.ensureActive()
                if ((System.nanoTime() - started) / 1_000_000 > 180_000) throw IOException("La descarga tardó demasiado. Reintenta.")
            }
            try {
                var url = source
                var redirects = 0
                var downloaded = false
                while (!downloaded) {
                    checkBudget()
                    check(UpdatePackagePolicy.allowedDownloadUrl(url)) { "Redirección de descarga no autorizada." }
                    val connection = URL(url).openConnection() as HttpURLConnection
                    try {
                        connection.connectTimeout = 10_000
                        connection.readTimeout = 10_000
                        connection.instanceFollowRedirects = false
                        connection.setRequestProperty("Accept", "application/octet-stream")
                        val status = connection.responseCode
                        if (status in listOf(301, 302, 303, 307, 308)) {
                            if (++redirects > 5) throw IOException("Demasiadas redirecciones.")
                            url = URL(URL(url), connection.getHeaderField("Location") ?: throw IOException("Redirección vacía.")).toString()
                            continue
                        }
                        if (status != 200) throw IOException("No se pudo descargar el APK (HTTP $status).")
                        val length = connection.contentLengthLong
                        if (length > MAX_BYTES) throw IOException("El APK supera el límite de descarga.")
                        val digest = MessageDigest.getInstance("SHA-256")
                        var count = 0L
                        var lastProgress = -1
                        connection.inputStream.use { input -> destination.outputStream().use { output ->
                            val buffer = ByteArray(32 * 1024)
                            while (true) {
                                checkBudget()
                                val read = input.read(buffer)
                                if (read < 0) break
                                count += read
                                if (count > MAX_BYTES) throw IOException("El APK supera el límite de descarga.")
                                digest.update(buffer, 0, read)
                                output.write(buffer, 0, read)
                                val progress = if (length > 0) (count * 100 / length).coerceAtMost(100).toInt() else 0
                                if (progress != lastProgress) {
                                    lastProgress = progress
                                    withContext(Dispatchers.Main) { onProgress(progress) }
                                }
                            }
                        } }
                        if (count == 0L || (length >= 0 && count != length)) throw IOException("La descarga está incompleta.")
                        val hash = digest.digest().joinToString("") { "%02x".format(it) }
                        if (info.apkSha256 != null && !hash.equals(info.apkSha256, true)) throw IOException("La integridad del APK no coincide.")
                        downloaded = true
                    } finally { connection.disconnect() }
                }
                checkBudget()
                validate(context, destination, info.latestVersionName)
                completed = true
                destination
            } finally { if (!completed) destination.delete() }
        }
    }

    @Suppress("DEPRECATION")
    fun validate(context: Context, file: File, versionName: String) {
        val manager = context.packageManager
        val flags = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
        val installed = manager.getPackageInfo(context.packageName, flags)
        val archive = manager.getPackageArchiveInfo(file.absolutePath, flags) ?: throw IOException("El APK no es válido.")
        fun version(info: PackageInfo) = if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else info.versionCode.toLong()
        fun signers(info: PackageInfo): Set<String> {
            val signatures = if (Build.VERSION.SDK_INT >= 28) info.signingInfo?.apkContentsSigners else info.signatures
            return signatures.orEmpty().map { signature -> MessageDigest.getInstance("SHA-256")
                .digest(signature.toByteArray()).joinToString("") { "%02x".format(it) } }.toSet()
        }
        UpdatePackagePolicy.packageError(archive.packageName, context.packageName, version(archive), version(installed),
            archive.versionName, versionName, signers(archive), signers(installed))?.let { throw IOException(it) }
    }

    fun needsInstallPermission(context: Context) = Build.VERSION.SDK_INT >= 26 && !context.packageManager.canRequestPackageInstalls()

    fun openInstallPermission(context: Context) {
        context.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}")))
    }

    suspend fun install(context: Context, file: File, versionName: String) {
        withContext(Dispatchers.IO) { validate(context, file, versionName) }
        withContext(Dispatchers.Main) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.updates", file)
            context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
        }
    }
}
