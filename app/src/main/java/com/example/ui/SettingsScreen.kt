package com.example.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.XFilesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: XFilesViewModel,
    onNavigateToAnalysis: () -> Unit
) {
    val showHiddenFiles by viewModel.showHiddenFiles.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()
    val context = LocalContext.current
    var cacheCleaned by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ayarlar",
                        fontWeight = FontWeight.Bold,
                        color = OnSurface,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: Görünüm & Dosya Tercihleri
            item {
                SettingsSectionHeader(title = "Dosya & Görünüm")
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SettingsToggleRow(
                            icon = Icons.Default.Visibility,
                            title = "Gizli Dosyaları Göster",
                            subtitle = "Nokta (.) ile başlayan sistem dosyalarını listele",
                            checked = showHiddenFiles,
                            onCheckedChange = { viewModel.toggleHiddenFiles() }
                        )

                        HorizontalDivider(color = SurfaceContainerHigh, thickness = 1.dp)

                        SettingsToggleRow(
                            icon = Icons.Default.GridView,
                            title = "Varsayılan Izgara Görünümü",
                            subtitle = "Dosya listelerinde ızgara düzenini tercih et",
                            checked = isGridView,
                            onCheckedChange = { viewModel.toggleGridView() }
                        )
                    }
                }
            }

            // Section: Depolama & Bakım
            item {
                SettingsSectionHeader(title = "Depolama & Bakım")
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SettingsActionRow(
                            icon = Icons.Default.PieChart,
                            iconTint = Primary,
                            title = "Depolama Analizi",
                            subtitle = "Büyük dosyalar, kategori dağılımı ve alan kullanımı",
                            onClick = onNavigateToAnalysis
                        )

                        HorizontalDivider(color = SurfaceContainerHigh, thickness = 1.dp)

                        SettingsActionRow(
                            icon = Icons.Default.CleaningServices,
                            iconTint = Secondary,
                            title = if (cacheCleaned) "Önbellek Temizlendi" else "Geçici Dosyaları Temizle",
                            subtitle = if (cacheCleaned) "Uygulama önbelleği sıfırlandı" else "Uygulama geçici verilerini temizle",
                            onClick = {
                                try {
                                    context.cacheDir.deleteRecursively()
                                    cacheCleaned = true
                                } catch (_: Exception) {}
                            }
                        )

                        HorizontalDivider(color = SurfaceContainerHigh, thickness = 1.dp)

                        SettingsActionRow(
                            icon = Icons.Default.Security,
                            iconTint = Tertiary,
                            title = "Sistem İzinleri",
                            subtitle = "Cihaz depolama erişim ayarlarını aç",
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        )
                    }
                }
            }

            // Section: Hakkında
            item {
                SettingsSectionHeader(title = "Hakkında")
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.FolderZip,
                                    contentDescription = null,
                                    tint = OnPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "XFiles Dosya Yöneticisi",
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Sürüm 1.0 (Stitch Tasarım Sistemi)",
                                    color = Outline,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Text(
                            text = "Reklamsız, doğrudan yerel depolama erişimi ve yüksek performanslı dosya yönetimi.",
                            color = OnSurfaceVariant,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = Primary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                color = OnSurface,
                fontSize = 15.sp
            )
            Text(
                text = subtitle,
                color = Outline,
                fontSize = 12.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = OnPrimary,
                checkedTrackColor = Primary,
                uncheckedThumbColor = Outline,
                uncheckedTrackColor = SurfaceContainerHigh
            )
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                color = OnSurface,
                fontSize = 15.sp
            )
            Text(
                text = subtitle,
                color = Outline,
                fontSize = 12.sp
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Outline,
            modifier = Modifier.size(20.dp)
        )
    }
}
