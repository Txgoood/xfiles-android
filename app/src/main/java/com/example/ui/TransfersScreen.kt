package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.StorageDriveInfo
import com.example.viewmodel.TransferItem
import com.example.viewmodel.XFilesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransfersScreen(
    modifier: Modifier = Modifier,
    viewModel: XFilesViewModel
) {
    val transfers by viewModel.transfers.collectAsState()
    var selectedTab by remember { mutableStateOf("all") } // all, ongoing, completed, failed

    val ongoingList = remember(transfers) { transfers.filter { !it.isCompleted && !it.isFailed } }
    val completedList = remember(transfers) { transfers.filter { it.isCompleted } }
    val failedList = remember(transfers) { transfers.filter { it.isFailed } }

    val filteredList = when (selectedTab) {
        "ongoing" -> ongoingList
        "completed" -> completedList
        "failed" -> failedList
        else -> transfers
    }

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
                            Icon(Icons.Default.Folder, contentDescription = null, tint = OnPrimaryContainer, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("XFiles", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Primary)
                        Text(" / ", fontSize = 14.sp, color = Outline)
                        Text("Aktarımlar", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearTransfers() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Temizle", tint = OnSurfaceVariant)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Status & Speed Pill Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (ongoingList.isNotEmpty()) Secondary else Outline)
                        )
                        Text(
                            text = "Aktarım Yöneticisi",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = SurfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                            Text(
                                text = if (ongoingList.isNotEmpty()) "24.1 MB/sn" else "Boşta",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                    }
                }
            }

            // Filter Tabs (Tümü, Devam Eden, Tamamlanan, Başarısız)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterTabButton(
                        text = "Tümü",
                        count = transfers.size,
                        isSelected = selectedTab == "all",
                        onClick = { selectedTab = "all" },
                        modifier = Modifier.weight(1f)
                    )
                    FilterTabButton(
                        text = "Devam",
                        count = ongoingList.size,
                        isSelected = selectedTab == "ongoing",
                        onClick = { selectedTab = "ongoing" },
                        modifier = Modifier.weight(1f)
                    )
                    FilterTabButton(
                        text = "Biten",
                        count = completedList.size,
                        isSelected = selectedTab == "completed",
                        onClick = { selectedTab = "completed" },
                        modifier = Modifier.weight(1f)
                    )
                    FilterTabButton(
                        text = "Hata",
                        count = failedList.size,
                        isSelected = selectedTab == "failed",
                        onClick = { selectedTab = "failed" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Active Transfer Hero Card (If ongoing transfers exist)
            if (ongoingList.isNotEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceContainer,
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Sync, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                        Text("Kuyrukta ${ongoingList.size} Dosya", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Primary)
                                    }
                                    Text("Aktif Aktarım", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SurfaceContainerHighest
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(14.dp))
                                        Text("14 sn", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = OnSurface)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SurfaceContainerLow,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.size(32.dp).clip(CircleShape).background(PrimaryContainer.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Downloading, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                        }
                                        Column {
                                            Text("Ortalama Hız", fontSize = 10.sp, color = OnSurfaceVariant)
                                            Text("24.1 MB/sn", fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, color = OnSurface)
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SurfaceContainerLow,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.size(32.dp).clip(CircleShape).background(SecondaryContainer.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = Secondary, modifier = Modifier.size(16.dp))
                                        }
                                        Column {
                                            Text("Kalan Boyut", fontSize = 10.sp, color = OnSurfaceVariant)
                                            Text("480 MB", fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, color = OnSurface)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Transfer Cards List
            if (filteredList.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 36.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.SwapVert, contentDescription = null, tint = Outline, modifier = Modifier.size(48.dp))
                            Text("Aktarım kuyruğu boş", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = OnSurface)
                            Text("Dosyaları kopyaladığınızda veya taşıdığınızda ilerleme burada görünür.", fontSize = 12.sp, color = Outline)
                        }
                    }
                }
            } else {
                items(filteredList) { item ->
                    TransferCardItem(
                        item = item,
                        onCancel = { viewModel.cancelTransfer(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterTabButton(
    text: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) Primary else SurfaceContainer,
        modifier = modifier
            .height(44.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) OnPrimary else OnSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
                shape = CircleShape,
                color = if (isSelected) OnPrimary.copy(alpha = 0.2f) else SurfaceContainerHighest
            ) {
                Text(
                    text = "$count",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) OnPrimary else OnSurface,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
fun TransferCardItem(
    item: TransferItem,
    onCancel: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                item.isFailed -> ColorErrorContainer.copy(alpha = 0.3f)
                                item.isCompleted -> SecondaryContainer.copy(alpha = 0.2f)
                                else -> PrimaryContainer.copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        when {
                            item.isFailed -> Icons.Default.ErrorOutline
                            item.isCompleted -> Icons.Default.CheckCircle
                            item.isMove -> Icons.Default.DriveFileMove
                            else -> Icons.Default.ContentCopy
                        },
                        contentDescription = null,
                        tint = when {
                            item.isFailed -> ColorError
                            item.isCompleted -> Secondary
                            else -> Primary
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.fileName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = when {
                                item.isFailed -> "Başarısız"
                                item.isCompleted -> "Tamamlandı"
                                else -> "%${(item.progress * 100).toInt()}"
                            },
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                item.isFailed -> ColorError
                                item.isCompleted -> Secondary
                                else -> Primary
                            }
                        )
                    }

                    Text(
                        text = if (item.isMove) "Taşınıyor: ${item.destPath}" else "Kopyalanıyor: ${item.destPath}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(2.5.dp))
                    .background(SurfaceContainerHighest)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = item.progress.coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(
                            when {
                                item.isFailed -> ColorError
                                item.isCompleted -> Secondary
                                else -> Primary
                            }
                        )
                )
            }

            // Bottom telemetry & actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${StorageDriveInfo.formatBytes(item.transferredBytes)} / ${StorageDriveInfo.formatBytes(item.totalBytes)}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = OnSurfaceVariant
                )

                if (!item.isCompleted && !item.isFailed) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(item.speed, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Primary)
                        TextButton(
                            onClick = onCancel,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("İptal", color = ColorError, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
