package com.example.comarleyaetheraudio.presentation.components.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.comarleyaetheraudio.data.remote.ApkUpdateInstaller
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.comarleyaetheraudio.data.remote.GitHubUpdateChecker

@Composable
fun UpdateAvailableDialog(
    updateInfo: GitHubUpdateChecker.UpdateInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }
    var downloaded by remember { mutableStateOf<File?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var permissionRequested by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = {
            Text(
                text = "🚀 Nueva Versión ${updateInfo.latestVersionName}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Novedades de esta actualización:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = updateInfo.releaseNotes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Text("Android te pedirá confirmar la instalación. No desinstales SoundFlow para actualizar.",
                    style = MaterialTheme.typography.bodySmall)
                if (busy) {
                    LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth())
                    Text("Descargando y verificando… $progress %", style = MaterialTheme.typography.bodySmall)
                }
                if (permissionRequested) Text("Permite instalaciones desde SoundFlow y vuelve a pulsar Instalar APK.",
                    style = MaterialTheme.typography.bodySmall)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(
                enabled = !busy,
                onClick = {
                    error = null
                    if (updateInfo.apkUrl == null) {
                        try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.downloadUrl))) }
                        catch (_: Exception) { error = "No se pudo abrir la publicación." }
                    } else scope.launch {
                        busy = true
                        try {
                            val file = downloaded ?: ApkUpdateInstaller.download(context.applicationContext, updateInfo) { progress = it }
                                .also { downloaded = it }
                            if (ApkUpdateInstaller.needsInstallPermission(context)) {
                                permissionRequested = true
                                ApkUpdateInstaller.openInstallPermission(context)
                            } else {
                                permissionRequested = false
                                ApkUpdateInstaller.install(context, file, updateInfo.latestVersionName)
                            }
                        } catch (e: CancellationException) { throw e }
                        catch (e: Exception) { error = e.message ?: "No se pudo actualizar. Puedes reintentar." }
                        finally { busy = false }
                    }
                }
            ) {
                Text(if (updateInfo.apkUrl == null) "Ver publicación" else if (downloaded != null) "Instalar APK" else "Descargar actualización")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) {
                Text("Más Tarde")
            }
        }
    )
}
