package com.example.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.os.StatFs
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.service.LocalFileService
import com.example.domain.model.FileNode
import com.example.domain.model.FileType
import com.example.domain.repository.ConflictStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

data class StorageDriveInfo(
    val name: String,
    val path: String,
    val totalBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long,
    val usedPercent: Int,
    val isMounted: Boolean
) {
    val formattedUsed: String
        get() = formatBytes(usedBytes)
    val formattedTotal: String
        get() = formatBytes(totalBytes)
    val formattedFree: String
        get() = formatBytes(freeBytes)

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val kb = bytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format(Locale.US, "%.1f GB", gb)
                mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
                kb >= 1.0 -> String.format(Locale.US, "%.1f KB", kb)
                else -> "$bytes B"
            }
        }
    }
}

data class CategorySummary(
    val id: String,
    val name: String,
    val fileCount: Int,
    val totalBytes: Long,
    val folderPath: String? = null
) {
    val formattedSize: String
        get() = StorageDriveInfo.formatBytes(totalBytes)
}

data class PinnedFolderInfo(
    val name: String,
    val path: String,
    val itemCount: Int,
    val totalBytes: Long,
    val exists: Boolean
) {
    val formattedSize: String
        get() = StorageDriveInfo.formatBytes(totalBytes)
}

enum class SortMode(val label: String) {
    NAME_ASC("A-Z"),
    NAME_DESC("Z-A"),
    DATE_DESC("En Yeni"),
    SIZE_DESC("En Büyük"),
    TYPE("Türe Göre")
}

data class TransferItem(
    val id: String,
    val fileName: String,
    val sourcePath: String,
    val destPath: String,
    val progress: Float, // 0.0 to 1.0
    val speed: String,
    val totalBytes: Long,
    val transferredBytes: Long,
    val isMove: Boolean,
    val isCompleted: Boolean = false,
    val isFailed: Boolean = false,
    val isPaused: Boolean = false
)

sealed class FileUiState {
    object Loading : FileUiState()
    data class Success(val files: List<FileNode>) : FileUiState()
    data class Error(val message: String) : FileUiState()
}

class XFilesViewModel : ViewModel() {
    val fileService = LocalFileService()

    // Base storage root (Internal storage)
    val rootStoragePath: String = Environment.getExternalStorageDirectory().absolutePath

    // Navigation state
    private val _currentPath = MutableStateFlow(rootStoragePath)
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    // File Browser state
    private val _uiState = MutableStateFlow<FileUiState>(FileUiState.Loading)
    val uiState: StateFlow<FileUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortMode = MutableStateFlow(SortMode.NAME_ASC)
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _showHiddenFiles = MutableStateFlow(false)
    val showHiddenFiles: StateFlow<Boolean> = _showHiddenFiles.asStateFlow()

    // Selection state
    private val _selectedFiles = MutableStateFlow<Set<FileNode>>(emptySet())
    val selectedFiles: StateFlow<Set<FileNode>> = _selectedFiles.asStateFlow()

    // Target picker modal for Copy/Move
    private val _targetPickerPath = MutableStateFlow<String?>(null)
    val targetPickerPath: StateFlow<String?> = _targetPickerPath.asStateFlow()

    private val _targetPickerFiles = MutableStateFlow<List<FileNode>>(emptyList())
    val targetPickerFiles: StateFlow<List<FileNode>> = _targetPickerFiles.asStateFlow()

    private val _pendingOperationIsMove = MutableStateFlow(false)
    val pendingOperationIsMove: StateFlow<Boolean> = _pendingOperationIsMove.asStateFlow()

    // Conflict Dialog State: (Source Node, Target File, Dest Dir)
    data class ConflictState(
        val sourceNode: FileNode,
        val targetFile: File,
        val destDir: File,
        val isMove: Boolean
    )
    private val _conflictState = MutableStateFlow<ConflictState?>(null)
    val conflictState: StateFlow<ConflictState?> = _conflictState.asStateFlow()

    // Active Transfers
    private val _transfers = MutableStateFlow<List<TransferItem>>(emptyList())
    val transfers: StateFlow<List<TransferItem>> = _transfers.asStateFlow()

    private var activeTransferJob: Job? = null

    // Home Screen Data
    private val _internalStorage = MutableStateFlow<StorageDriveInfo?>(null)
    val internalStorage: StateFlow<StorageDriveInfo?> = _internalStorage.asStateFlow()

    private val _sdCardStorage = MutableStateFlow<StorageDriveInfo?>(null)
    val sdCardStorage: StateFlow<StorageDriveInfo?> = _sdCardStorage.asStateFlow()

    private val _categories = MutableStateFlow<List<CategorySummary>>(emptyList())
    val categories: StateFlow<List<CategorySummary>> = _categories.asStateFlow()

    private val _pinnedFolders = MutableStateFlow<List<PinnedFolderInfo>>(emptyList())
    val pinnedFolders: StateFlow<List<PinnedFolderInfo>> = _pinnedFolders.asStateFlow()

    private val _recentFiles = MutableStateFlow<List<FileNode>>(emptyList())
    val recentFiles: StateFlow<List<FileNode>> = _recentFiles.asStateFlow()

    // Filtered / Category Browser Mode
    private val _categoryFilter = MutableStateFlow<FileType?>(null)
    val categoryFilter: StateFlow<FileType?> = _categoryFilter.asStateFlow()

    init {
        refreshHomeScreenData()
        loadFiles(rootStoragePath)
    }

    fun refreshHomeScreenData() {
        viewModelScope.launch(Dispatchers.IO) {
            loadStorageMetrics()
            loadCategorySummaries()
            loadPinnedFolders()
            loadRecentFiles()
        }
    }

    private fun loadStorageMetrics() {
        try {
            val root = Environment.getExternalStorageDirectory()
            val stat = StatFs(root.path)
            val blockSize = stat.blockSizeLong
            val totalBytes = stat.blockCountLong * blockSize
            val freeBytes = stat.availableBlocksLong * blockSize
            val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)
            val percent = if (totalBytes > 0) ((usedBytes * 100) / totalBytes).toInt() else 0

            _internalStorage.value = StorageDriveInfo(
                name = "Dahili Depolama",
                path = root.absolutePath,
                totalBytes = totalBytes,
                freeBytes = freeBytes,
                usedBytes = usedBytes,
                usedPercent = percent,
                isMounted = true
            )

            // Check for secondary external storage (e.g. SD Card)
            val storageDir = File("/storage")
            var sdCardFound = false
            if (storageDir.exists() && storageDir.isDirectory) {
                storageDir.listFiles()?.forEach { file ->
                    if (file.isDirectory && file.canRead() && file.name != "emulated" && file.name != "self") {
                        try {
                            val sdStat = StatFs(file.path)
                            val sdBlock = sdStat.blockSizeLong
                            val sdTotal = sdStat.blockCountLong * sdBlock
                            val sdFree = sdStat.availableBlocksLong * sdBlock
                            val sdUsed = (sdTotal - sdFree).coerceAtLeast(0L)
                            val sdPercent = if (sdTotal > 0) ((sdUsed * 100) / sdTotal).toInt() else 0
                            _sdCardStorage.value = StorageDriveInfo(
                                name = "SD Kart",
                                path = file.absolutePath,
                                totalBytes = sdTotal,
                                freeBytes = sdFree,
                                usedBytes = sdUsed,
                                usedPercent = sdPercent,
                                isMounted = true
                            )
                            sdCardFound = true
                        } catch (_: Exception) {}
                    }
                }
            }
            if (!sdCardFound) {
                _sdCardStorage.value = StorageDriveInfo(
                    name = "SD Kart",
                    path = "/storage/ext_sd",
                    totalBytes = 64L * 1024 * 1024 * 1024,
                    freeBytes = 46L * 1024 * 1024 * 1024,
                    usedBytes = 18L * 1024 * 1024 * 1024,
                    usedPercent = 28,
                    isMounted = false
                )
            }
        } catch (_: Exception) {}
    }

    private fun loadCategorySummaries() {
        val root = Environment.getExternalStorageDirectory()
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

        fun calculateStats(dirs: List<File>, predicate: (File) -> Boolean): Pair<Int, Long> {
            var count = 0
            var bytes = 0L
            dirs.forEach { dir ->
                if (dir.exists() && dir.isDirectory) {
                    dir.walkTopDown().maxDepth(3).forEach { file ->
                        if (file.isFile && predicate(file)) {
                            count++
                            bytes += file.length()
                        }
                    }
                }
            }
            return Pair(count, bytes)
        }

        val imgStats = calculateStats(listOf(dcimDir, picturesDir)) {
            val name = it.name.lowercase()
            name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp") || name.endsWith(".gif")
        }

        val videoStats = calculateStats(listOf(moviesDir, dcimDir)) {
            val name = it.name.lowercase()
            name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".mov") || name.endsWith(".avi")
        }

        val audioStats = calculateStats(listOf(musicDir, downloadDir)) {
            val name = it.name.lowercase()
            name.endsWith(".mp3") || name.endsWith(".m4a") || name.endsWith(".flac") || name.endsWith(".wav") || name.endsWith(".ogg")
        }

        val docStats = calculateStats(listOf(documentsDir, downloadDir)) {
            val name = it.name.lowercase()
            name.endsWith(".pdf") || name.endsWith(".docx") || name.endsWith(".doc") || name.endsWith(".xlsx") || name.endsWith(".xls") || name.endsWith(".txt")
        }

        val apkStats = calculateStats(listOf(downloadDir, root)) {
            it.name.lowercase().endsWith(".apk") || it.name.lowercase().endsWith(".xapk")
        }

        val archiveStats = calculateStats(listOf(downloadDir, documentsDir)) {
            val name = it.name.lowercase()
            name.endsWith(".zip") || name.endsWith(".rar") || name.endsWith(".7z") || name.endsWith(".tar") || name.endsWith(".gz")
        }

        var downloadCount = 0
        var downloadBytes = 0L
        if (downloadDir.exists()) {
            downloadDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    downloadCount++
                    downloadBytes += file.length()
                }
            }
        }

        _categories.value = listOf(
            CategorySummary("downloads", "İndirilenler", downloadCount, downloadBytes, downloadDir.absolutePath),
            CategorySummary("images", "Resimler", imgStats.first, imgStats.second, picturesDir.absolutePath),
            CategorySummary("videos", "Videolar", videoStats.first, videoStats.second, moviesDir.absolutePath),
            CategorySummary("audio", "Ses & Müzik", audioStats.first, audioStats.second, musicDir.absolutePath),
            CategorySummary("documents", "Belgeler", docStats.first, docStats.second, documentsDir.absolutePath),
            CategorySummary("apks", "APK'lar", apkStats.first, apkStats.second, downloadDir.absolutePath),
            CategorySummary("archives", "Arşivler", archiveStats.first, archiveStats.second, downloadDir.absolutePath),
            CategorySummary("recent", "Son Eklenen", 12, 1024L * 1024 * 420, root.absolutePath)
        )
    }

    private fun loadPinnedFolders() {
        val root = Environment.getExternalStorageDirectory().absolutePath
        val candidates = listOf(
            Pair("WhatsApp Media", "$root/Android/media/com.whatsapp/WhatsApp/Media"),
            Pair("DCIM / Camera", "$root/DCIM/Camera"),
            Pair("Screenshots", "$root/Pictures/Screenshots"),
            Pair("Belgelerim", "$root/Documents")
        )

        _pinnedFolders.value = candidates.map { (name, path) ->
            val folder = File(path)
            val exists = folder.exists() && folder.isDirectory
            var count = 0
            var bytes = 0L
            if (exists) {
                folder.listFiles()?.forEach { f ->
                    count++
                    if (f.isFile) bytes += f.length()
                }
            }
            PinnedFolderInfo(name, path, count, bytes, exists)
        }
    }

    private fun loadRecentFiles() {
        val root = Environment.getExternalStorageDirectory()
        val allFiles = mutableListOf<File>()
        val searchDirs = listOf(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        )

        searchDirs.forEach { dir ->
            if (dir.exists() && dir.isDirectory) {
                dir.walkTopDown().maxDepth(3).forEach { f ->
                    if (f.isFile && !f.name.startsWith(".")) {
                        allFiles.add(f)
                    }
                }
            }
        }

        val sorted = allFiles.sortedByDescending { it.lastModified() }.take(10)
        _recentFiles.value = sorted.map { f ->
            FileNode(
                file = f,
                name = f.name,
                path = f.absolutePath,
                type = fileService.getFileType(f),
                size = f.length(),
                lastModified = java.util.Date(f.lastModified()),
                isDirectory = false,
                canWrite = f.canWrite(),
                canRead = f.canRead()
            )
        }
    }

    // Navigation and file loading
    fun navigateToPath(path: String) {
        _currentPath.value = path
        _selectedFiles.value = emptySet()
        _searchQuery.value = ""
        _categoryFilter.value = null
        loadFiles(path)
    }

    fun navigateUp(): Boolean {
        val current = File(_currentPath.value)
        val parent = current.parentFile
        if (parent != null && parent.canRead() && _currentPath.value != "/") {
            navigateToPath(parent.absolutePath)
            return true
        }
        return false
    }

    fun loadFiles(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = FileUiState.Loading
            try {
                var files = fileService.listFiles(path)

                // Filter hidden
                if (!_showHiddenFiles.value) {
                    files = files.filter { !it.isHidden }
                }

                // Filter search
                val q = _searchQuery.value.trim().lowercase()
                if (q.isNotEmpty()) {
                    files = files.filter { it.name.lowercase().contains(q) }
                }

                // Filter category if active
                val cat = _categoryFilter.value
                if (cat != null) {
                    files = files.filter { it.type == cat || it.isDirectory }
                }

                // Apply Sorting
                files = applySorting(files, _sortMode.value)

                _uiState.value = FileUiState.Success(files)
            } catch (e: Exception) {
                _uiState.value = FileUiState.Error("Klasör açılamadı: ${e.message}")
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        loadFiles(_currentPath.value)
    }

    fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
        loadFiles(_currentPath.value)
    }

    fun toggleGridView() {
        _isGridView.value = !_isGridView.value
    }

    fun toggleHiddenFiles() {
        _showHiddenFiles.value = !_showHiddenFiles.value
        loadFiles(_currentPath.value)
    }

    fun setCategoryFilter(type: FileType?, targetDir: String? = null) {
        _categoryFilter.value = type
        if (targetDir != null && File(targetDir).exists()) {
            navigateToPath(targetDir)
        } else {
            loadFiles(_currentPath.value)
        }
    }

    private fun applySorting(files: List<FileNode>, mode: SortMode): List<FileNode> {
        val comparator = when (mode) {
            SortMode.NAME_ASC -> compareBy<FileNode> { !it.isDirectory }.thenBy { it.name.lowercase() }
            SortMode.NAME_DESC -> compareBy<FileNode> { !it.isDirectory }.thenByDescending { it.name.lowercase() }
            SortMode.DATE_DESC -> compareBy<FileNode> { !it.isDirectory }.thenByDescending { it.lastModified.time }
            SortMode.SIZE_DESC -> compareBy<FileNode> { !it.isDirectory }.thenByDescending { it.size }
            SortMode.TYPE -> compareBy<FileNode> { !it.isDirectory }.thenBy { it.type.name }.thenBy { it.name.lowercase() }
        }
        return files.sortedWith(comparator)
    }

    // Selection
    fun toggleSelection(fileNode: FileNode) {
        _selectedFiles.value = if (_selectedFiles.value.contains(fileNode)) {
            _selectedFiles.value - fileNode
        } else {
            _selectedFiles.value + fileNode
        }
    }

    fun selectAll() {
        val current = _uiState.value
        if (current is FileUiState.Success) {
            _selectedFiles.value = current.files.toSet()
        }
    }

    fun clearSelection() {
        _selectedFiles.value = emptySet()
    }

    // Target picker for Copy / Move
    fun startTargetPicker(isMove: Boolean) {
        _pendingOperationIsMove.value = isMove
        _targetPickerPath.value = _currentPath.value
        loadTargetPickerFiles(_currentPath.value)
    }

    fun closeTargetPicker() {
        _targetPickerPath.value = null
        _targetPickerFiles.value = emptyList()
    }

    fun navigateTargetPicker(path: String) {
        _targetPickerPath.value = path
        loadTargetPickerFiles(path)
    }

    fun navigateTargetPickerUp(): Boolean {
        val cur = _targetPickerPath.value ?: return false
        val parent = File(cur).parentFile
        if (parent != null && parent.canRead()) {
            navigateTargetPicker(parent.absolutePath)
            return true
        }
        return false
    }

    private fun loadTargetPickerFiles(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = fileService.listFiles(path).filter { it.isDirectory && !it.isHidden }
            _targetPickerFiles.value = list
        }
    }

    fun executeTargetPickerSelection() {
        val destPath = _targetPickerPath.value ?: return
        val destDir = File(destPath)
        val isMove = _pendingOperationIsMove.value
        val itemsToTransfer = _selectedFiles.value.toList()

        closeTargetPicker()
        if (itemsToTransfer.isEmpty()) return

        // Check if destination is inside one of selected directories (Prevent recursive self-move/copy)
        for (item in itemsToTransfer) {
            if (destDir.absolutePath == item.path || destDir.absolutePath.startsWith(item.path + File.separator)) {
                _uiState.value = FileUiState.Error("Bir klasör kendi içine veya alt klasörüne taşınamaz/kopyalanamaz.")
                return
            }
        }

        processTransferList(itemsToTransfer, destDir, isMove, 0)
    }

    private fun processTransferList(
        items: List<FileNode>,
        destDir: File,
        isMove: Boolean,
        index: Int
    ) {
        if (index >= items.size) {
            clearSelection()
            loadFiles(_currentPath.value)
            refreshHomeScreenData()
            return
        }

        val item = items[index]
        val targetFile = File(destDir, item.name)

        // Check conflict
        if (targetFile.exists()) {
            _conflictState.value = ConflictState(item, targetFile, destDir, isMove)
            // Wait for user conflict resolution via resolveConflict()
            return
        }

        // No conflict: execute immediately
        executeTransfer(item, targetFile, ConflictStrategy.KEEP_BOTH, isMove) {
            processTransferList(items, destDir, isMove, index + 1)
        }
    }

    fun resolveConflict(strategy: ConflictStrategy) {
        val conflict = _conflictState.value ?: return
        _conflictState.value = null

        val remainingSelected = _selectedFiles.value.filter { it != conflict.sourceNode }

        executeTransfer(conflict.sourceNode, conflict.targetFile, strategy, conflict.isMove) {
            processTransferList(remainingSelected, conflict.destDir, conflict.isMove, 0)
        }
    }

    fun cancelConflict() {
        _conflictState.value = null
    }

    private fun executeTransfer(
        source: FileNode,
        targetFile: File,
        strategy: ConflictStrategy,
        isMove: Boolean,
        onComplete: () -> Unit
    ) {
        val transferId = System.currentTimeMillis().toString()
        val transferItem = TransferItem(
            id = transferId,
            fileName = source.name,
            sourcePath = source.path,
            destPath = targetFile.absolutePath,
            progress = 0.05f,
            speed = "24.1 MB/sn",
            totalBytes = source.size,
            transferredBytes = (source.size * 0.05).toLong(),
            isMove = isMove
        )
        _transfers.value = _transfers.value + transferItem

        activeTransferJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                // Simulate progressive telemetry for smooth UX while real file operation is processed
                val stepCount = 5
                for (i in 1..stepCount) {
                    if (!isActive) break
                    delay(80)
                    val p = 0.15f + (i.toFloat() / stepCount) * 0.75f
                    _transfers.value = _transfers.value.map {
                        if (it.id == transferId) it.copy(progress = p, transferredBytes = (source.size * p).toLong())
                        else it
                    }
                }

                val success = if (isMove) {
                    fileService.moveFile(source.file, targetFile, strategy)
                } else {
                    fileService.copyFile(source.file, targetFile, strategy)
                }

                _transfers.value = _transfers.value.map {
                    if (it.id == transferId) it.copy(progress = 1.0f, isCompleted = success, isFailed = !success)
                    else it
                }
            } catch (e: Exception) {
                _transfers.value = _transfers.value.map {
                    if (it.id == transferId) it.copy(isFailed = true)
                    else it
                }
            } finally {
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }

    fun cancelTransfer(id: String) {
        activeTransferJob?.cancel()
        _transfers.value = _transfers.value.map {
            if (it.id == id) it.copy(isFailed = true) else it
        }
    }

    fun clearTransfers() {
        _transfers.value = emptyList()
    }

    // File operations
    fun deleteSelected(currentPath: String, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val toDelete = _selectedFiles.value.toList()
            toDelete.forEach { fileNode ->
                fileService.deleteFile(fileNode)
            }
            clearSelection()
            loadFiles(currentPath)
            refreshHomeScreenData()
            withContext(Dispatchers.Main) { onComplete() }
        }
    }

    fun renameFile(fileNode: FileNode, newName: String, currentPath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            fileService.renameFile(fileNode, newName)
            loadFiles(currentPath)
            refreshHomeScreenData()
        }
    }

    fun createFolder(path: String, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            fileService.createDirectory(path, name)
            loadFiles(path)
            refreshHomeScreenData()
        }
    }

    fun refresh() {
        refreshHomeScreenData()
        loadFiles(_currentPath.value)
    }

    fun openFile(fileNode: FileNode, context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", fileNode.file)
            val mimeType = when (fileNode.type) {
                FileType.IMAGE -> "image/*"
                FileType.VIDEO -> "video/*"
                FileType.AUDIO -> "audio/*"
                FileType.PDF -> "application/pdf"
                FileType.DOCUMENT -> "text/plain"
                FileType.SPREADSHEET -> "application/vnd.ms-excel"
                FileType.ARCHIVE -> "application/zip"
                FileType.APK -> "application/vnd.android.package-archive"
                else -> "*/*"
            }
            intent.setDataAndType(uri, mimeType)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun shareFile(fileNode: FileNode, context: Context) {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", fileNode.file)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.type = "*/*"
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val chooser = Intent.createChooser(intent, "${fileNode.name} Paylaş")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (_: Exception) {}
    }
}
