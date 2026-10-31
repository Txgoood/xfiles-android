package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.theme.*
import com.example.viewmodel.CategorySummary
import com.example.viewmodel.PinnedFolderInfo
import com.example.viewmodel.StorageDriveInfo
import com.example.viewmodel.XFilesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: XFilesViewModel,
    onNavigateToFiles: (String) -> Unit,
    onNavigateToAnalysis: () -> Unit
) {
    val internalStorage by viewModel.internalStorage.collectAsState()
    val sdCardStorage by viewModel.sdCardStorage.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val pinnedFolders by viewModel.pinnedFolders.collectAsState()
    val recentFiles by viewModel.recentFiles.collectAsState()
    val context = LocalContext.current

    var selectedRecentFileForMenu by remember { mutableStateOf<FileNode?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Surface,
        topBar = {
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
                                imageVector = Icons.Default.Folder,
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
                        Text(
                            text = " / ",
                            fontSize = 14.sp,
                            color = Outline
                        )
                        Text(
                            text = "Ana Sayfa",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToFiles(viewModel.rootStoragePath) }) {
                        Icon(Icons.Default.Search, contentDescription = "Ara", tint = OnSurfaceVariant)
                    }
                    IconButton(onClick = { onNavigateToAnalysis() }) {
                        Icon(Icons.Default.PieChart, contentDescription = "Analiz", tint = OnSurfaceVariant)
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profil",
                            tint = OnPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Search & Filter Bar
            item {
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = SurfaceContainer,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable { onNavigateToFiles(viewModel.rootStoragePath) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = Outline,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Dosya, klasör veya uzantı ara...",
                            color = Outline,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Filtreler",
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Storage Overview Header & Horizontal Track
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Depolama Alanı",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = SurfaceContainerHigh,
                            modifier = Modifier.clickable { onNavigateToAnalysis() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.PieChart,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Analiz",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Primary
                                )
                            }
                        }
                    }

                    // Horizontal storage cards track
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        internalStorage?.let { drive ->
                            StorageCard(
                                drive = drive,
                                icon = Icons.Default.Smartphone,
                                iconTint = Primary,
                                iconBg = PrimaryContainer.copy(alpha = 0.2f),
                                progressColor = Primary,
                                onClick = { onNavigateToFiles(drive.path) }
                            )
                        }

                        sdCardStorage?.let { sd ->
                            StorageCard(
                                drive = sd,
                                icon = Icons.Default.SdCard,
                                iconTint = Secondary,
                                iconBg = SecondaryContainer.copy(alpha = 0.2f),
                                progressColor = Secondary,
                                onClick = {
                                    if (sd.isMounted) onNavigateToFiles(sd.path)
                                }
                            )
                        }
                    }
                }
            }

            // Categories (4x2 Bento Grid)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kategoriler",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface
                        )
                        Text(
                            text = "8 Tür",
                            fontSize = 13.sp,
                            color = Outline
                        )
                    }

                    CategoriesBentoGrid(
                        categories = categories,
                        onCategoryClick = { cat ->
                            when (cat.id) {
                                "downloads" -> onNavigateToFiles(cat.folderPath ?: viewModel.rootStoragePath)
                                "images" -> viewModel.setCategoryFilter(FileType.IMAGE, cat.folderPath)
                                "videos" -> viewModel.setCategoryFilter(FileType.VIDEO, cat.folderPath)
                                "audio" -> viewModel.setCategoryFilter(FileType.AUDIO, cat.folderPath)
                                "documents" -> viewModel.setCategoryFilter(FileType.DOCUMENT, cat.folderPath)
                                "apks" -> viewModel.setCategoryFilter(FileType.APK, cat.folderPath)
                                "archives" -> viewModel.setCategoryFilter(FileType.ARCHIVE, cat.folderPath)
                                else -> onNavigateToFiles(viewModel.rootStoragePath)
                            }
                            if (cat.id != "downloads") {
                                onNavigateToFiles(cat.folderPath ?: viewModel.rootStoragePath)
                            }
                        }
                    )
                }
            }

            // Pinned Folders Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sık Kullanılan Klasörler",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface
                        )
                        Text(
                            text = "Tümü",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onNavigateToFiles(viewModel.rootStoragePath) }
                                .padding(4.dp)
                        )
                    }

                    PinnedFoldersGrid(
                        pinnedFolders = pinnedFolders,
                        onFolderClick = { folder ->
                            onNavigateToFiles(folder.path)
                        }
                    )
                }
            }

            // Recent Files List Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Son Kullanılan Dosyalar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface
                        )
                        Text(
                            text = "Son Günler",
                            fontSize = 12.sp,
                            color = Outline
                        )
                    }

                    if (recentFiles.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp, horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    tint = Outline,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "Son kullanılan dosya bulunamadı",
                                    fontSize = 14.sp,
                                    color = OnSurfaceVariant
                                )
                                Text(
                                    text = "Cihaza yeni dosya eklediğinizde burada listelenecektir.",
                                    fontSize = 12.sp,
                                    color = Outline
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                recentFiles.forEachIndexed { index, file ->
                                    RecentFileRow(
                                        file = file,
                                        onOpen = { viewModel.openFile(file, context) },
                                        onShare = { viewModel.shareFile(file, context) }
                                    )
                                    if (index < recentFiles.size - 1) {
                                        HorizontalDivider(
                                            thickness = 1.dp,
                                            color = SurfaceContainerHighest.copy(alpha = 0.5f)
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
}

@Composable
fun StorageCard(
    drive: StorageDriveInfo,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    progressColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceContainer,
        modifier = Modifier
            .width(270.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = drive.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnSurface
                        )
                        Text(
                            text = drive.path,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Text(
                    text = "%${drive.usedPercent}",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = iconTint
                )
            }

            // Linear Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(SurfaceContainerHighest)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = (drive.usedPercent / 100f).coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(3.dp))
                        .background(progressColor)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${drive.formattedUsed} / ${drive.formattedTotal}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = OnSurfaceVariant
                )
                Text(
                    text = if (drive.isMounted) "${drive.formattedFree} boş" else "Bağlı Değil",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (drive.isMounted) Secondary else Outline
                )
            }
        }
    }
}

@Composable
fun CategoriesBentoGrid(
    categories: List<CategorySummary>,
    onCategoryClick: (CategorySummary) -> Unit
) {
    val items = if (categories.isNotEmpty()) categories else listOf(
        CategorySummary("downloads", "İndirilenler", 0, 0),
        CategorySummary("images", "Resimler", 0, 0),
        CategorySummary("videos", "Videolar", 0, 0),
        CategorySummary("audio", "Ses & Müzik", 0, 0),
        CategorySummary("documents", "Belgeler", 0, 0),
        CategorySummary("apks", "APK'lar", 0, 0),
        CategorySummary("archives", "Arşivler", 0, 0),
        CategorySummary("recent", "Son Eklenen", 0, 0)
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Row 1 (4 items)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.take(4).forEach { cat ->
                val (icon, tint, bg) = getCategoryVisuals(cat.id)
                CategoryGridTile(
                    modifier = Modifier.weight(1f),
                    title = cat.name,
                    subtitle = cat.formattedSize,
                    icon = icon,
                    iconTint = tint,
                    iconBg = bg,
                    onClick = { onCategoryClick(cat) }
                )
            }
        }

        // Row 2 (4 items)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.drop(4).take(4).forEach { cat ->
                val (icon, tint, bg) = getCategoryVisuals(cat.id)
                CategoryGridTile(
                    modifier = Modifier.weight(1f),
                    title = cat.name,
                    subtitle = if (cat.id == "recent") "Bugün" else cat.formattedSize,
                    icon = icon,
                    iconTint = tint,
                    iconBg = bg,
                    onClick = { onCategoryClick(cat) }
                )
            }
        }
    }
}

private fun getCategoryVisuals(id: String): Triple<ImageVector, Color, Color> {
    return when (id) {
        "downloads" -> Triple(Icons.Default.Download, Primary, PrimaryContainer.copy(alpha = 0.2f))
        "images" -> Triple(Icons.Default.Image, Secondary, SecondaryContainer.copy(alpha = 0.2f))
        "videos" -> Triple(Icons.Default.Movie, Tertiary, TertiaryContainer.copy(alpha = 0.3f))
        "audio" -> Triple(Icons.Default.Headphones, Secondary, Secondary.copy(alpha = 0.15f))
        "documents" -> Triple(Icons.Default.Description, Primary, PrimaryContainer.copy(alpha = 0.2f))
        "apks" -> Triple(Icons.Default.Android, Secondary, SecondaryContainer.copy(alpha = 0.2f))
        "archives" -> Triple(Icons.Default.FolderZip, Tertiary, TertiaryContainer.copy(alpha = 0.3f))
        else -> Triple(Icons.Default.History, OnSurface, SurfaceContainerHighest)
    }
}

@Composable
fun CategoryGridTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceContainer,
        modifier = modifier
            .height(96.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = Outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PinnedFoldersGrid(
    pinnedFolders: List<PinnedFolderInfo>,
    onFolderClick: (PinnedFolderInfo) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val chunked = pinnedFolders.chunked(2)
        chunked.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { folder ->
                    PinnedFolderCard(
                        modifier = Modifier.weight(1f),
                        folder = folder,
                        onClick = { onFolderClick(folder) }
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun PinnedFolderCard(
    modifier: Modifier = Modifier,
    folder: PinnedFolderInfo,
    onClick: () -> Unit
) {
    val (icon, bg, tint) = when {
        folder.name.contains("WhatsApp", ignoreCase = true) ->
            Triple(Icons.Default.Folder, SecondaryContainer.copy(alpha = 0.2f), Secondary)
        folder.name.contains("DCIM", ignoreCase = true) ->
            Triple(Icons.Default.PhotoCamera, PrimaryContainer.copy(alpha = 0.2f), Primary)
        folder.name.contains("Screenshot", ignoreCase = true) ->
            Triple(Icons.Default.PhoneAndroid, SurfaceContainerHighest, OnSurfaceVariant)
        else ->
            Triple(Icons.Default.FolderShared, TertiaryContainer.copy(alpha = 0.2f), Tertiary)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = SurfaceContainer,
        modifier = modifier
            .height(56.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (folder.exists) "${folder.formattedSize} • ${folder.itemCount} öge" else "Henüz dosya yok",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RecentFileRow(
    file: FileNode,
    onOpen: () -> Unit,
    onShare: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    val (icon, bg, tint) = when (file.type) {
        FileType.PDF -> Triple(Icons.Default.PictureAsPdf, ColorErrorContainer.copy(alpha = 0.3f), ColorError)
        FileType.SPREADSHEET -> Triple(Icons.Default.TableChart, SecondaryContainer.copy(alpha = 0.2f), Secondary)
        FileType.IMAGE -> Triple(Icons.Default.Image, SecondaryContainer.copy(alpha = 0.2f), Secondary)
        FileType.VIDEO -> Triple(Icons.Default.Movie, TertiaryContainer.copy(alpha = 0.3f), Tertiary)
        FileType.AUDIO -> Triple(Icons.Default.Headphones, Secondary.copy(alpha = 0.2f), Secondary)
        FileType.ARCHIVE -> Triple(Icons.Default.FolderZip, TertiaryContainer.copy(alpha = 0.3f), Tertiary)
        FileType.APK -> Triple(Icons.Default.Android, SecondaryContainer.copy(alpha = 0.2f), Secondary)
        else -> Triple(Icons.Default.Description, PrimaryContainer.copy(alpha = 0.2f), Primary)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onOpen() }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = file.formattedSize,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Outline
                    )
                    Text(text = "•", fontSize = 11.sp, color = Outline)
                    Text(
                        text = file.formattedDate,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Outline
                    )
                    Text(text = "•", fontSize = 11.sp, color = Outline)
                    Text(
                        text = file.file.parentFile?.name ?: "/",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                    contentDescription = "Aksiyonlar",
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            DropdownMenu(
                expanded = expandedMenu,
                onDismissRequest = { expandedMenu = false },
                modifier = Modifier.background(SurfaceContainerHigh)
            ) {
                DropdownMenuItem(
                    text = { Text("Aç", color = OnSurface) },
                    onClick = {
                        expandedMenu = false
                        onOpen()
                    },
                    leadingIcon = { Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Primary) }
                )
                DropdownMenuItem(
                    text = { Text("Paylaş", color = OnSurface) },
                    onClick = {
                        expandedMenu = false
                        onShare()
                    },
                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = Secondary) }
                )
            }
        }
    }
}
