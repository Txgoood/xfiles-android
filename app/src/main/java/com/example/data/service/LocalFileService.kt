package com.example.data.service

import com.example.domain.model.FileType
import com.example.domain.model.FileNode
import com.example.domain.repository.ConflictStrategy
import com.example.domain.repository.IFileService
import java.io.File
import java.util.Date

class LocalFileService : IFileService {

    override suspend fun listFiles(path: String): List<FileNode> {
        val directory = File(path)
        if (!directory.exists() || !directory.isDirectory) return emptyList()

        val files = directory.listFiles() ?: return emptyList()

        return files.map { file ->
            val isDir = file.isDirectory
            FileNode(
                file = file,
                name = file.name,
                path = file.absolutePath,
                type = getFileType(file),
                size = if (isDir) 0L else file.length(),
                lastModified = Date(file.lastModified()),
                isDirectory = isDir,
                canWrite = file.canWrite(),
                canRead = file.canRead(),
                childCount = if (isDir) (file.list()?.size ?: 0) else 0,
                isHidden = file.name.startsWith(".")
            )
        }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }

    override suspend fun deleteFile(fileNode: FileNode): Boolean {
        return if (fileNode.isDirectory) {
            fileNode.file.deleteRecursively()
        } else {
            fileNode.file.delete()
        }
    }

    override suspend fun copyFile(source: File, destination: File, strategy: ConflictStrategy): Boolean {
        return try {
            val target = resolveDestination(destination, strategy) ?: return true // skipped
            if (source.isDirectory) {
                source.copyRecursively(target, overwrite = (strategy == ConflictStrategy.OVERWRITE))
            } else {
                source.copyTo(target, overwrite = (strategy == ConflictStrategy.OVERWRITE)).exists()
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun moveFile(source: File, destination: File, strategy: ConflictStrategy): Boolean {
        return try {
            val target = resolveDestination(destination, strategy) ?: return true // skipped
            val moved = source.renameTo(target)
            if (moved) {
                true
            } else {
                // If renameTo fails (across mount points or drives), fallback to copy then verify then delete
                val copied = if (source.isDirectory) {
                    source.copyRecursively(target, overwrite = (strategy == ConflictStrategy.OVERWRITE))
                } else {
                    source.copyTo(target, overwrite = (strategy == ConflictStrategy.OVERWRITE)).exists()
                }
                if (copied && target.exists() && (source.isDirectory || target.length() == source.length())) {
                    if (source.isDirectory) source.deleteRecursively() else source.delete()
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun resolveDestination(destination: File, strategy: ConflictStrategy): File? {
        if (!destination.exists()) return destination
        return when (strategy) {
            ConflictStrategy.OVERWRITE -> destination
            ConflictStrategy.SKIP -> null
            ConflictStrategy.KEEP_BOTH -> {
                val parent = destination.parentFile ?: return destination
                val baseName = destination.nameWithoutExtension
                val extension = destination.extension
                var counter = 1
                var candidate: File
                do {
                    val newName = if (extension.isNotEmpty()) "$baseName ($counter).$extension" else "$baseName ($counter)"
                    candidate = File(parent, newName)
                    counter++
                } while (candidate.exists())
                candidate
            }
        }
    }

    override suspend fun renameFile(fileNode: FileNode, newName: String): Boolean {
        val parent = fileNode.file.parentFile ?: return false
        val newFile = File(parent, newName)
        if (newFile.exists()) return false
        return fileNode.file.renameTo(newFile)
    }

    override suspend fun createDirectory(path: String, name: String): Boolean {
        val newDir = File(path, name)
        return if (!newDir.exists()) newDir.mkdirs() else false
    }

    fun getFileType(file: File): FileType {
        if (file.isDirectory) return FileType.DIRECTORY
        val name = file.name.lowercase()
        return when {
            name.endsWith(".pdf") -> FileType.PDF
            name.endsWith(".xlsx") || name.endsWith(".xls") || name.endsWith(".csv") -> FileType.SPREADSHEET
            name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") ||
                name.endsWith(".webp") || name.endsWith(".gif") || name.endsWith(".heic") -> FileType.IMAGE
            name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".mov") || name.endsWith(".avi") -> FileType.VIDEO
            name.endsWith(".mp3") || name.endsWith(".m4a") || name.endsWith(".flac") ||
                name.endsWith(".wav") || name.endsWith(".aac") || name.endsWith(".ogg") -> FileType.AUDIO
            name.endsWith(".docx") || name.endsWith(".doc") || name.endsWith(".txt") ||
                name.endsWith(".pptx") || name.endsWith(".ppt") -> FileType.DOCUMENT
            name.endsWith(".zip") || name.endsWith(".rar") || name.endsWith(".7z") ||
                name.endsWith(".tar") || name.endsWith(".gz") -> FileType.ARCHIVE
            name.endsWith(".apk") || name.endsWith(".xapk") -> FileType.APK
            else -> FileType.FILE
        }
    }
}
