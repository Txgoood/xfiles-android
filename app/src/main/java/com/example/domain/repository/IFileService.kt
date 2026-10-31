package com.example.domain.repository

import com.example.domain.model.FileNode
import java.io.File
import kotlinx.coroutines.flow.Flow

enum class ConflictStrategy { OVERWRITE, SKIP, KEEP_BOTH }

interface IFileService {
    suspend fun listFiles(path: String): List<FileNode>
    suspend fun deleteFile(fileNode: FileNode): Boolean
    suspend fun copyFile(source: File, destination: File, strategy: ConflictStrategy): Boolean
    suspend fun moveFile(source: File, destination: File, strategy: ConflictStrategy): Boolean
    suspend fun renameFile(fileNode: FileNode, newName: String): Boolean
    suspend fun createDirectory(path: String, name: String): Boolean
}
