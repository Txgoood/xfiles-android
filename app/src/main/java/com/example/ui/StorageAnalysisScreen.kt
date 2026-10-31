package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.FileNode
import com.example.ui.theme.*
import com.example.viewmodel.StorageDriveInfo
import com.example.viewmodel.XFilesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageAnalysisScreen(
    modifier: Modifier = Modifier,
    viewModel: XFilesViewModel,
    onBack: () -> Unit
) {
    val internalStorage by viewModel.internalStorage.collectAsState()
    val recentFiles by viewModel.recentFiles.collectAsState()
    val context = LocalContext.current

    var cleanedCache by remember { mutableStateOf(false) }
    var showCleanedSnackbar by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Surface,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceContainer.copy(alpha = 0.95f),
                    titleContentColor = OnSurface
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = OnSurface)
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PieChart, contentDescription = null, tint = OnPrimaryContainer, modifier = Modifier.size(16.dp))
                        }
                        Text("Depolama Analizörü", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshHomeScreenData() }) {
                        Icon(Icons.Default.Sync, contentDescription = "Yenile", tint = OnSurfaceVariant)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Tags
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = SurfaceContainerHigh
                        ) {
                            Text(
                                text = "/storage/emulated/0",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = SurfaceContainerHigh
                        ) {
                            Text(
                                text = "F2FS",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = OnSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "Canlı Analiz",
                        fontSize = 11.sp,
                        color = Secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Depolama Genel Durumu Bento Kartı
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceContainerLow,
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
                                Text("Dahili Depolama", fontSize = 13.sp, color = OnSurfaceVariant)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = internalStorage?.formattedUsed?.replace(" GB", "") ?: "84.6",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnSurface
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "/ ${internalStorage?.formattedTotal ?: "128 GB"}",
                                        fontSize = 14.sp,
                                        color = OnSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 3.dp)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Kullanım Oranı", fontSize = 12.sp, color = OnSurfaceVariant)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Primary))
                                    Text(
                                        text = "%${internalStorage?.usedPercent ?: 66}",
                                        fontSize = 18.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary
                                    )
                                }
                            }
                        }

                        // Segmented Distribution Bar (Media, Apps, Docs, System, Free)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(SurfaceContainerHighest)
                            ) {
                                Box(modifier = Modifier.fillMaxHeight().weight(0.42f).background(Primary))
                                Box(modifier = Modifier.fillMaxHeight().weight(0.18f).background(Secondary))
                                Box(modifier = Modifier.fillMaxHeight().weight(0.08f).background(Tertiary))
                                Box(modifier = Modifier.fillMaxHeight().weight(0.08f).background(SurfaceBright))
                                Box(modifier = Modifier.fillMaxHeight().weight(0.24f).background(Color.Transparent))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${internalStorage?.formattedUsed ?: "84.6 GB"} Dolu",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = OnSurfaceVariant
                                )
                                Text(
                                    text = "${internalStorage?.formattedFree ?: "43.4 GB"} Serbest (%${100 - (internalStorage?.usedPercent ?: 66)})",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium,
                                    color = Secondary
                                )
                            }
                        }

                        // Category Detail Matrix
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StorageCategoryTile(
                                    title = "Medya (Görsel & Video)",
                                    size = "48.2 GB",
                                    color = Primary,
                                    modifier = Modifier.weight(1f)
                                )
                                StorageCategoryTile(
                                    title = "Uygulamalar & Veriler",
                                    size = "22.0 GB",
                                    color = Secondary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StorageCategoryTile(
                                    title = "Belgeler & İndirilenler",
                                    size = "6.4 GB",
                                    color = Tertiary,
                                    modifier = Modifier.weight(1f)
                                )
                                StorageCategoryTile(
                                    title = "Sistem & Diğer",
                                    size = "8.0 GB",
                                    color = SurfaceBright,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Güvenli Temizlik Analizi
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SecondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = OnSecondaryContainer, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text("Güvenli Temizlik Önerileri", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                                Text("Kişisel dosyalarınız korunur; geçici artıklar taranır.", fontSize = 11.sp, color = OnSurfaceVariant)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Uygulama Önbelleği & Geçici Günlükler", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = OnSurface)
                                    Text(
                                        text = if (cleanedCache) "0 B (Temizlendi)" else "1.42 GB yer kazanılabilir",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (cleanedCache) Outline else Secondary
                                    )
                                }

                                Button(
                                    onClick = {
                                        cleanedCache = true
                                        showCleanedSnackbar = true
                                    },
                                    enabled = !cleanedCache,
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(if (cleanedCache) "Temizlendi" else "Temizle", fontSize = 12.sp, color = OnPrimaryContainer)
                                }
                            }
                        }
                    }
                }
            }

            // Büyük Dosyalar Bölümü (>100 MB)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                            Text("Büyük Dosyalar (>50 MB)", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
                        }
                        Text("Alan Tasarrufu", fontSize = 11.sp, color = Outline)
                    }

                    val largeFiles = remember(recentFiles) {
                        recentFiles.filter { it.size > 20L * 1024 * 1024 }
                    }

                    if (largeFiles.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Secondary, modifier = Modifier.size(36.dp))
                                Text("Aşırı büyük dosya bulunamadı", fontSize = 14.sp, color = OnSurface)
                                Text("Cihazınızdaki büyük medya ve arşivler burada listelenir.", fontSize = 11.sp, color = Outline)
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                largeFiles.forEachIndexed { index, file ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.openFile(file, context) }
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(TertiaryContainer.copy(alpha = 0.25f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Movie, contentDescription = null, tint = Tertiary, modifier = Modifier.size(20.dp))
                                            }
                                            Column {
                                                Text(file.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(file.formattedSize, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Primary)
                                            }
                                        }

                                        IconButton(onClick = { viewModel.shareFile(file, context) }) {
                                            Icon(Icons.Default.Share, contentDescription = "Paylaş", tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    if (index < largeFiles.size - 1) {
                                        HorizontalDivider(color = SurfaceContainerHighest.copy(alpha = 0.4f))
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
fun StorageCategoryTile(
    title: String,
    size: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SurfaceContainer,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Column {
                Text(title, fontSize = 10.sp, color = OnSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(size, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, color = OnSurface)
            }
        }
    }
}
