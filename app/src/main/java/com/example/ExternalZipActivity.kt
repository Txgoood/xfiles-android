package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.ZipBrowserDialog
import com.example.ui.theme.XFilesTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class ExternalZipActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = if (intent?.action == Intent.ACTION_VIEW) intent?.data else null
        setContent {
            XFilesTheme {
                var archive by remember { mutableStateOf<File?>(null) }
                var failure by remember { mutableStateOf<String?>(null) }
                LaunchedEffect(uri) {
                    try {
                        archive = withContext(Dispatchers.IO) { copyIncomingZip(uri) }
                    } catch (e: Exception) { failure = e.message ?: "ZIP dosyası açılamadı" }
                }
                Surface(modifier = Modifier.fillMaxSize()) {
                    when {
                        failure != null -> Column(modifier = Modifier.padding(24.dp)) {
                            Text(failure!!)
                            Button(onClick = { finish() }) { Text("Kapat") }
                        }
                        archive != null -> ZipBrowserDialog(
                            archive = archive!!,
                            onDismiss = { finish() },
                            onExtracted = {},
                            extractionParent = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        )
                        else -> Column(modifier = Modifier.padding(24.dp)) {
                            Text("ZIP dosyası açılıyor…")
                            LinearProgressIndicator()
                        }
                    }
                }
            }
        }
    }

    private fun copyIncomingZip(uri: Uri?): File {
        require(uri != null && uri.scheme in setOf("content", "file")) { "Desteklenmeyen dosya bağlantısı" }
        var name: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) name = cursor.getString(0)
            }
        }
        val safeName = (name ?: uri.lastPathSegment ?: "arsiv.zip").substringAfterLast('/').substringAfterLast('\\')
            .replace(Regex("[\\x00-\\x1f]"), "_").take(160).ifBlank { "arsiv.zip" }
        val folder = File(cacheDir, "incoming-zip/${UUID.randomUUID()}")
        check(folder.mkdirs()) { "Geçici klasör oluşturulamadı" }
        val result = File(folder, safeName)
        try {
            val input = contentResolver.openInputStream(uri) ?: error("Dosya okunamadı")
            input.use { source -> result.outputStream().use { target ->
                val buffer = ByteArray(32768)
                var bytes = 0L
                while (true) {
                    val n = source.read(buffer)
                    if (n < 0) break
                    bytes += n
                    require(bytes <= 1024L * 1024 * 1024) { "ZIP dosyası 1 GB sınırını aşıyor" }
                    target.write(buffer, 0, n)
                }
            } }
            return result
        } catch (e: Exception) {
            folder.deleteRecursively()
            throw e
        }
    }
}
