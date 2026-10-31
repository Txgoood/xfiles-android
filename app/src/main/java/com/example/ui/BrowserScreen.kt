package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.FileNode
import com.example.domain.model.FileType
import com.example.domain.repository.ConflictStrategy
import com.example.ui.theme.*
import com.example.viewmodel.FileUiState
import com.example.viewmodel.SortMode
import com.example.viewmodel.XFilesViewModel
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.data.service.ZipService
import android.content.Intent
import android.content.ClipData
import android.os.Build
import android.provider.Settings
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    startPath: String,
    modifier: Modifier = Modifier,
    viewModel: XFilesViewModel
) {
    val currentPath by viewModel.currentPath.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val isSelectionMode = selectedFiles.isNotEmpty()
    val isGridView by viewModel.isGridView.collectAsState()
    val sortMode by viewModel.sortMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showHiddenFiles by viewModel.showHiddenFiles.collectAsState()
    val internalStorage by viewModel.internalStorage.collectAsState()
    val sdCardStorage by viewModel.sdCardStorage.collectAsState()

    val targetPickerPath by viewModel.targetPickerPath.collectAsState()
    val targetPickerFiles by viewModel.targetPickerFiles.collectAsState()
    val pendingIsMove by viewModel.pendingOperationIsMove.collectAsState()
    val conflictState by viewModel.conflictState.collectAsState()

    val context = LocalContext.current

    var archiveFile by remember { mutableStateOf<File?>(null) }
    archiveFile?.let { file ->
        ZipBrowserDialog(file, onDismiss = { archiveFile = null }, onExtracted = { viewModel.refresh() })
    }
    fun openNode(file: FileNode) {
        if (file.name.endsWith(".zip", ignoreCase = true)) archiveFile = file.file
        else viewModel.openFile(file, context)
    }

    var showSearchInput by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showDriveDropdown by remember { mutableStateOf(false) }

    var renameTargetFile by remember { mutableStateOf<FileNode?>(null) }
    var newFolderNameInput by remember { mutableStateOf("") }
    var renameInput by remember { mutableStateOf("") }

    // Intercept back button for folder hierarchy
    BackHandler(enabled = isSelectionMode || currentPath != viewModel.rootStoragePath) {
        if (isSelectionMode) {
            viewModel.clearSelection()
        } else {
            viewModel.navigateUp()
        }
    }

    // Target Folder Picker Overlay
    if (targetPickerPath != null) {
        FolderPickerScreen(
            currentPath = targetPickerPath!!,
            files = targetPickerFiles,
            isMove = pendingIsMove,
            onNavigate = { viewModel.navigateTargetPicker(it) },
            onNavigateUp = { viewModel.navigateTargetPickerUp() },
            onConfirm = { viewModel.executeTargetPickerSelection() },
            onDismiss = { viewModel.closeTargetPicker() },
            onCreateFolder = { path, name -> viewModel.createFolder(path, name) }
        )
        return
    }

    // Name Conflict Resolution Dialog
    conflictState?.let { conflict ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelConflict() },
            containerColor = SurfaceContainerHigh,
            title = {
                Text(
                    text = "Aynı İsimde Dosya Mevcut",
                    color = OnSurface,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Hedef klasörde '${conflict.targetFile.name}' isimli bir dosya zaten var.",
                        color = OnSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Nasıl devam etmek istersiniz?",
                        color = Outline,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { viewModel.resolveConflict(ConflictStrategy.KEEP_BOTH) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
                    ) {
                        Text("İkisini de Sakla (Önerilen)", color = OnPrimaryContainer)
                    }
                    Button(
                        onClick = { viewModel.resolveConflict(ConflictStrategy.OVERWRITE) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorContainer)
                    ) {
                        Text("Üzerine Yaz", color = OnErrorContainer)
                    }
                    OutlinedButton(
                        onClick = { viewModel.resolveConflict(ConflictStrategy.SKIP) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Bu Dosyayı Atla", color = OnSurfaceVariant)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelConflict() }) {
                    Text("İptal", color = Outline)
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = SurfaceContainerHigh,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Error)
                    Text("Silme Onayı", color = OnSurface, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "${selectedFiles.size} öge kalıcı olarak silinecektir. Bu işlem geri alınamaz. Emin misiniz?",
                    color = OnSurfaceVariant,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSelected(currentPath) {
                            showDeleteConfirmDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorContainer)
                ) {
                    Text("Kalıcı Olarak Sil", color = OnErrorContainer, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("İptal", color = OnSurfaceVariant)
                }
            }
        )
    }

    // Create New Folder Dialog
    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            containerColor = SurfaceContainerHigh,
            title = { Text("Yeni Klasör Oluştur", color = OnSurface, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newFolderNameInput,
                    onValueChange = { newFolderNameInput = it },
                    placeholder = { Text("Klasör adı") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface,
                        focusedBorderColor = Primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderNameInput.isNotBlank()) {
                            viewModel.createFolder(currentPath, newFolderNameInput.trim())
                            showCreateFolderDialog = false
                            newFolderNameInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
                ) {
                    Text("Oluştur", color = OnPrimaryContainer)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) {
                    Text("İptal", color = OnSurfaceVariant)
                }
            }
        )
    }

    // Rename Dialog
    if (showRenameDialog && renameTargetFile != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            containerColor = SurfaceContainerHigh,
            title = { Text("Yeniden Adlandır", color = OnSurface, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface,
                        focusedBorderColor = Primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            viewModel.renameFile(renameTargetFile!!, renameInput.trim(), currentPath)
                            showRenameDialog = false
                            renameTargetFile = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
                ) {
                    Text("Kaydet", color = OnPrimaryContainer)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("İptal", color = OnSurfaceVariant)
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Surface,
        topBar = {
            if (isSelectionMode) {
                // Multi-Selection Contextual Top Bar (matching oklu_se_im_modu)
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SurfaceContainer,
                        titleContentColor = OnSurface
                    ),
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, contentDescription = "Vazgeç", tint = OnSurface)
                        }
                    },
                    title = {
                        Text(
                            text = "${selectedFiles.size} seçildi",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    },
                    actions = {
                        IconButton(onClick = { viewModel.selectAll() }) {
                            Icon(Icons.Default.SelectAll, contentDescription = "Tümünü Seç", tint = OnSurfaceVariant)
                        }
                        IconButton(onClick = { viewModel.startTargetPicker(isMove = false) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Kopyala", tint = Primary)
                        }
                        IconButton(onClick = { viewModel.startTargetPicker(isMove = true) }) {
                            Icon(Icons.Default.DriveFileMove, contentDescription = "Taşı", tint = Secondary)
                        }
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Error)
                        }
                    }
                )
            } else {
                // Standard Files Explorer Top Bar
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SurfaceContainer.copy(alpha = 0.95f),
                        titleContentColor = OnSurface
                    ),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = OnPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "XFiles",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Primary
                            )
                            Text(text = " / ", fontSize = 14.sp, color = Outline)
                            Text(
                                text = "Dosyalar",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OnSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSearchInput = !showSearchInput }) {
                            Icon(Icons.Default.Search, contentDescription = "Ara", tint = OnSurfaceVariant)
                        }
                        IconButton(onClick = { viewModel.toggleGridView() }) {
                            Icon(
                                if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                                contentDescription = "Görünüm",
                                tint = OnSurfaceVariant
                            )
                        }
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Seçenekler", tint = OnSurfaceVariant)
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                modifier = Modifier.background(SurfaceContainerHigh)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Yeni Klasör", color = OnSurface) },
                                    onClick = {
                                        showSortMenu = false
                                        showCreateFolderDialog = true
                                    },
                                    leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = Primary) }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (showHiddenFiles) "Gizli Dosyaları Gizle" else "Gizli Dosyaları Göster", color = OnSurface) },
                                    onClick = {
                                        showSortMenu = false
                                        viewModel.toggleHiddenFiles()
                                    },
                                    leadingIcon = { Icon(if (showHiddenFiles) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = Secondary) }
                                )
                                HorizontalDivider(color = SurfaceContainerHighest)
                                DropdownMenuItem(
                                    text = { Text("Sıralama: A-Z", color = OnSurface) },
                                    onClick = {
                                        showSortMenu = false
                                        viewModel.setSortMode(SortMode.NAME_ASC)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sıralama: En Yeni", color = OnSurface) },
                                    onClick = {
                                        showSortMenu = false
                                        viewModel.setSortMode(SortMode.DATE_DESC)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sıralama: En Büyük", color = OnSurface) },
                                    onClick = {
                                        showSortMenu = false
                                        viewModel.setSortMode(SortMode.SIZE_DESC)
                                    }
                                )
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Storage Drive Selector Dropdown & Quick Capacity
            Surface(
                color = Surface,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDriveDropdown = !showDriveDropdown }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PrimaryContainer.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.PhoneAndroid,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = internalStorage?.name ?: "Dahili Depolama",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = OnSurface
                                    )
                                    Text(
                                        text = "${internalStorage?.formattedUsed ?: "64 GB"} / ${internalStorage?.formattedTotal ?: "128 GB"} Kullanılıyor",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = OnSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                if (showDriveDropdown) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = OnSurfaceVariant
                            )
                        }
                    }

                    // Dropdown popup for drives
                    if (showDriveDropdown) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceContainerHigh,
                            shadowElevation = 6.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PrimaryContainer.copy(alpha = 0.2f))
                                        .clickable {
                                            viewModel.navigateToPath(viewModel.rootStoragePath)
                                            showDriveDropdown = false
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                                        Text("Dahili Depolama", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Primary)
                                    }
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                }

                                sdCardStorage?.let { sd ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                if (sd.isMounted) viewModel.navigateToPath(sd.path)
                                                showDriveDropdown = false
                                            }
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.SdCard, contentDescription = null, tint = Secondary, modifier = Modifier.size(18.dp))
                                            Column {
                                                Text("SD Kart", fontSize = 13.sp, color = OnSurface)
                                                Text(if (sd.isMounted) "${sd.formattedUsed} / ${sd.formattedTotal}" else "Cihaza takılı değil", fontSize = 10.sp, color = Outline)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Capacity indicator bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(SurfaceContainerHighest)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = ((internalStorage?.usedPercent ?: 50) / 100f).coerceIn(0f, 1f))
                                .clip(RoundedCornerShape(2.dp))
                                .background(Primary)
                        )
                    }
                }
            }

            // Horizontal Breadcrumbs Scroll Track
            BreadcrumbTrack(
                currentPath = currentPath,
                rootPath = viewModel.rootStoragePath,
                onNavigate = { viewModel.navigateToPath(it) }
            )

            // Search Filter input (if open)
            if (showSearchInput) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceContainer,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Outline, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Bu klasörde filtrele...", fontSize = 13.sp) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = OnSurface,
                                unfocusedTextColor = OnSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Temizle", tint = Outline, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // View and Sort Toolbar
            val currentFiles = (uiState as? FileUiState.Success)?.files ?: emptyList()
            val totalBytes = currentFiles.filter { !it.isDirectory }.sumOf { it.size }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${currentFiles.size} Öğe",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = OnSurfaceVariant
                    )
                    Text(text = "·", fontSize = 12.sp, color = Outline)
                    Text(
                        text = com.example.viewmodel.StorageDriveInfo.formatBytes(totalBytes),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = OnSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Sort mode button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceContainer,
                        modifier = Modifier.clickable {
                            val next = when (sortMode) {
                                SortMode.NAME_ASC -> SortMode.DATE_DESC
                                SortMode.DATE_DESC -> SortMode.SIZE_DESC
                                SortMode.SIZE_DESC -> SortMode.NAME_ASC
                                else -> SortMode.NAME_ASC
                            }
                            viewModel.setSortMode(next)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.SortByAlpha, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                            Text(text = sortMode.label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = OnSurface)
                        }
                    }

                    // View mode toggle
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceContainer,
                        modifier = Modifier.size(32.dp).clickable { viewModel.toggleGridView() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                                contentDescription = null,
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Quick new folder
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceContainer,
                        modifier = Modifier.size(32.dp).clickable { showCreateFolderDialog = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Main Content: Loading, Error, or File Items (List / Grid)
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (val state = uiState) {
                    is FileUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }
                    is FileUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Error, modifier = Modifier.size(48.dp))
                                Text(text = state.message, color = OnSurfaceVariant, fontSize = 14.sp)
                                Button(onClick = { viewModel.loadFiles(currentPath) }) {
                                    Text("Yeniden Dene")
                                }
                            }
                        }
                    }
                    is FileUiState.Success -> {
                        if (state.files.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Outline, modifier = Modifier.size(48.dp))
                                    Text("Bu klasör boş", color = OnSurfaceVariant, fontSize = 14.sp)
                                    Text("Buraya dosya kopyalayabilir veya yeni klasör oluşturabilirsiniz.", color = Outline, fontSize = 12.sp)
                                }
                            }
                        } else if (isGridView) {
                            // Grid Mode
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(state.files) { file ->
                                    val isSelected = selectedFiles.contains(file)
                                    GridFileCard(
                                        file = file,
                                        isSelected = isSelected,
                                        onClick = {
                                            if (isSelectionMode) {
                                                viewModel.toggleSelection(file)
                                            } else if (file.isDirectory) {
                                                viewModel.navigateToPath(file.path)
                                            } else {
                                                openNode(file)
                                            }
                                        },
                                        onLongClick = { viewModel.toggleSelection(file) }
                                    )
                                }
                            }
                        } else {
                            // List Mode (Stitch 56dp item card)
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(state.files) { file ->
                                    val isSelected = selectedFiles.contains(file)
                                    ListFileCard(
                                        file = file,
                                        isSelected = isSelected,
                                        onClick = {
                                            if (isSelectionMode) {
                                                viewModel.toggleSelection(file)
                                            } else if (file.isDirectory) {
                                                viewModel.navigateToPath(file.path)
                                            } else {
                                                openNode(file)
                                            }
                                        },
                                        onLongClick = { viewModel.toggleSelection(file) },
                                        onOpen = { openNode(file) },
                                        onRename = {
                                            renameTargetFile = file
                                            renameInput = file.name
                                            showRenameDialog = true
                                        },
                                        onShare = { viewModel.shareFile(file, context) },
                                        onCopy = {
                                            viewModel.toggleSelection(file)
                                            viewModel.startTargetPicker(isMove = false)
                                        },
                                        onMove = {
                                            viewModel.toggleSelection(file)
                                            viewModel.startTargetPicker(isMove = true)
                                        },
                                        onDelete = {
                                            viewModel.toggleSelection(file)
                                            showDeleteConfirmDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BreadcrumbTrack(
    currentPath: String,
    rootPath: String,
    onNavigate: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    // Break down path segments relative to root
    val segments = remember(currentPath) {
        val list = mutableListOf<Pair<String, String>>()
        list.add(Pair("Dahili Depolama", rootPath))

        if (currentPath.startsWith(rootPath) && currentPath != rootPath) {
            val relative = currentPath.removePrefix(rootPath).trimStart('/')
            val parts = relative.split('/')
            var accumulated = rootPath
            for (p in parts) {
                if (p.isNotEmpty()) {
                    accumulated = "$accumulated/$p"
                    list.add(Pair(p, accumulated))
                }
            }
        } else if (!currentPath.startsWith(rootPath) && currentPath != "/") {
            val parts = currentPath.trimStart('/').split('/')
            var accumulated = ""
            for (p in parts) {
                if (p.isNotEmpty()) {
                    accumulated = "$accumulated/$p"
                    list.add(Pair(p, accumulated))
                }
            }
        }
        list
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Home icon chip
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = SurfaceContainer,
            modifier = Modifier.size(32.dp).clickable { onNavigate(rootPath) }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Home, contentDescription = "Ana Dizin", tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
            }
        }

        segments.forEachIndexed { index, (name, path) ->
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Outline, modifier = Modifier.size(14.dp))

            val isLast = index == segments.size - 1
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isLast) PrimaryContainer.copy(alpha = 0.2f) else SurfaceContainer,
                modifier = Modifier.height(32.dp).clickable { onNavigate(path) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isLast) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                    }
                    Text(
                        text = name,
                        fontSize = 11.sp,
                        fontWeight = if (isLast) FontWeight.Bold else FontWeight.Medium,
                        color = if (isLast) Primary else OnSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListFileCard(
    file: FileNode,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    val (icon, bg, tint) = getFileVisuals(file)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PrimaryContainer.copy(alpha = 0.25f) else SurfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // MIME Icon
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = file.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (file.isHidden) OnSurfaceVariant.copy(alpha = 0.7f) else OnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (file.isHidden) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SurfaceContainerHighest,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("Gizli", fontSize = 9.sp, color = Outline)
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = file.extensionLabel,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = tint
                        )
                        Text(text = "·", fontSize = 10.sp, color = Outline)
                        Text(
                            text = file.formattedSize,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = OnSurfaceVariant
                        )
                        Text(text = "·", fontSize = 10.sp, color = Outline)
                        Text(
                            text = file.formattedDate,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Outline
                        )
                    }
                }
            }

            Box {
                IconButton(
                    onClick = { expandedMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Seçenekler",
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false },
                    modifier = Modifier.background(SurfaceContainerHigh)
                ) {
                    if (!file.isDirectory) {
                        DropdownMenuItem(
                            text = { Text("Aç", color = OnSurface) },
                            onClick = { expandedMenu = false; onOpen() },
                            leadingIcon = { Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Primary) }
                        )
                        DropdownMenuItem(
                            text = { Text("Paylaş", color = OnSurface) },
                            onClick = { expandedMenu = false; onShare() },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = Secondary) }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Yeniden Adlandır", color = OnSurface) },
                        onClick = { expandedMenu = false; onRename() },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = OnSurfaceVariant) }
                    )
                    DropdownMenuItem(
                        text = { Text("Kopyala", color = OnSurface) },
                        onClick = { expandedMenu = false; onCopy() },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Primary) }
                    )
                    DropdownMenuItem(
                        text = { Text("Taşı", color = OnSurface) },
                        onClick = { expandedMenu = false; onMove() },
                        leadingIcon = { Icon(Icons.Default.DriveFileMove, contentDescription = null, tint = Secondary) }
                    )
                    HorizontalDivider(color = SurfaceContainerHighest)
                    DropdownMenuItem(
                        text = { Text("Sil", color = Error) },
                        onClick = { expandedMenu = false; onDelete() },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Error) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridFileCard(
    file: FileNode,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val (icon, bg, tint) = getFileVisuals(file)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PrimaryContainer.copy(alpha = 0.25f) else SurfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = file.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = file.formattedSize,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Outline
                    )
                    Text(
                        text = file.extensionLabel,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = tint
                    )
                }
            }
        }
    }
}

private fun getFileVisuals(file: FileNode): Triple<ImageVector, Color, Color> {
    return if (file.isDirectory) {
        Triple(Icons.Default.Folder, PrimaryContainer.copy(alpha = 0.2f), Primary)
    } else {
        when (file.type) {
            FileType.PDF -> Triple(Icons.Default.PictureAsPdf, ColorErrorContainer.copy(alpha = 0.35f), ColorError)
            FileType.SPREADSHEET -> Triple(Icons.Default.TableChart, SecondaryContainer.copy(alpha = 0.2f), Secondary)
            FileType.IMAGE -> Triple(Icons.Default.Image, SecondaryContainer.copy(alpha = 0.2f), Secondary)
            FileType.VIDEO -> Triple(Icons.Default.Movie, TertiaryContainer.copy(alpha = 0.3f), Tertiary)
            FileType.AUDIO -> Triple(Icons.Default.Headphones, Secondary.copy(alpha = 0.15f), Secondary)
            FileType.ARCHIVE -> Triple(Icons.Default.FolderZip, TertiaryContainer.copy(alpha = 0.3f), Tertiary)
            FileType.APK -> Triple(Icons.Default.Android, SecondaryContainer.copy(alpha = 0.2f), Secondary)
            else -> Triple(Icons.Default.Description, PrimaryContainer.copy(alpha = 0.2f), Primary)
        }
    }
}

@Composable
private fun ZipBrowserDialog(archive: File, onDismiss: () -> Unit, onExtracted: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var entries by remember(archive) { mutableStateOf<List<String>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }
    var requestInstallPermission by remember { mutableStateOf(false) }
    if (requestInstallPermission) {
        AlertDialog(
            onDismissRequest = { requestInstallPermission = false },
            title = { Text("Uygulama yükleme izni") },
            text = { Text("APK kurmak için XFiles uygulamasına bu kaynaktan uygulama yükleme iznini verin. Ardından ZIP içindeki APK'ya yeniden dokunun.") },
            confirmButton = { TextButton(onClick = {
                requestInstallPermission = false
                try {
                    context.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}")))
                } catch (e: Exception) { error = "İzin ayarları açılamadı: ${e.message}" }
            }) { Text("Ayarlara git") } },
            dismissButton = { TextButton(onClick = { requestInstallPermission = false }) { Text("İptal") } }
        )
    }
    LaunchedEffect(archive) {
        try { entries = withContext(Dispatchers.IO) { ZipService.entries(archive) } }
        catch (e: Exception) { error = e.message ?: "ZIP açılamadı" }
        busy = false
    }
    fun runAction(entry: String?) {
        if (entry != null && entry.endsWith(".apk", ignoreCase = true) &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
            requestInstallPermission = true
            return
        }
        error = null
        busy = true
        scope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    if (entry == null) {
                        var dest = File(archive.parentFile, archive.nameWithoutExtension)
                        var number = 1
                        while (dest.exists()) dest = File(archive.parentFile, "${archive.nameWithoutExtension} (${number++})")
                        ZipService.extract(archive, dest)
                    } else {
                        val root = File(context.cacheDir, "archives")
                        root.mkdirs()
                        val dest = File(root, java.util.UUID.randomUUID().toString())
                        ZipService.extract(archive, dest, entry)
                    }
                }
                if (entry == null) {
                    message = "Çıkarıldı: ${result.name}"
                    onExtracted()
                } else {
                    val ext = result.extension.lowercase()
                    val mime = if (ext == "apk") "application/vnd.android.package-archive"
                        else MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", result)
                    val intent = Intent(Intent.ACTION_VIEW).setDataAndType(uri, mime)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .apply { clipData = ClipData.newRawUri("dosya", uri) }
                    if (ext == "apk") context.startActivity(intent)
                    else context.startActivity(Intent.createChooser(intent, "Dosyayı aç"))
                }
            } catch (e: Exception) { error = e.message ?: "İşlem başarısız" }
            busy = false
        }
    }
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(archive.name) },
        text = {
            Column {
                if (busy) { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()); Text("İşleniyor…") }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                message?.let { Text(it) }
                if (!busy && error == null && entries.isEmpty()) Text("Arşiv boş")
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(entries) { name ->
                        Text(name, modifier = Modifier.fillMaxWidth().clickable(enabled = !busy && !name.endsWith('/')) { runAction(name) }.padding(vertical = 10.dp))
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = { TextButton(enabled = !busy && error == null, onClick = { runAction(null) }) { Text("Tümünü çıkar") } },
        dismissButton = { TextButton(enabled = !busy, onClick = onDismiss) { Text("Kapat") } }
    )
}
