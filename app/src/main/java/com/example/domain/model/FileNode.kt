package com.example.domain.model

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FileType {
    DIRECTORY,
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT,
    PDF,
    SPREADSHEET,
    ARCHIVE,
    APK,
    FILE
}

data class FileNode(
    val file: File,
    val name: String,
    val path: String,
    val type: FileType,
    val size: Long,
    val lastModified: Date,
    val isDirectory: Boolean,
    val canWrite: Boolean = true,
    val canRead: Boolean = true,
    val childCount: Int = 0,
    val isHidden: Boolean = name.startsWith(".")
) {
    val formattedSize: String
        get() {
            if (isDirectory) return if (childCount >= 0) "$childCount öğe" else "Klasör"
            val kb = size / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format(Locale.US, "%.1f GB", gb)
                mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
                kb >= 1.0 -> String.format(Locale.US, "%.1f KB", kb)
                else -> "$size B"
            }
        }

    val formattedDate: String
        get() {
            val now = System.currentTimeMillis()
            val diff = now - lastModified.time
            val minutes = diff / (1000 * 60)
            val hours = minutes / 60
            val days = hours / 24

            return when {
                minutes < 1 -> "Az önce"
                minutes < 60 -> "$minutes dk önce"
                hours < 24 -> "$hours saat önce"
                days == 1L -> "Dün " + SimpleDateFormat("HH:mm", Locale("tr")).format(lastModified)
                days < 7 -> "$days gün önce"
                else -> SimpleDateFormat("d MMM", Locale("tr")).format(lastModified)
            }
        }

    val extensionLabel: String
        get() {
            if (isDirectory) return "KLASÖR"
            val ext = file.extension.uppercase()
            return if (ext.isNotEmpty()) ext else "DOSYA"
        }
}
