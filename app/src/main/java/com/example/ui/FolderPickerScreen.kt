package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.FileNode
import com.example.ui.theme.*
import com.example.viewmodel.XFilesViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderPickerScreen(
    currentPath: String,
    files: List<FileNode>,
    isMove: Boolean,
    onNavigate: (String) -> Unit,
    onNavigateUp: () -> Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onCreateFolder: (String, String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    BackHandler {
        if (!onNavigateUp()) {
            onDismiss()
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = SurfaceContainerHigh,
            title = { Text("Yeni Klasör Oluştur", color = OnSurface) },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    placeholder = { Text("Klasör adı") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface,
                        focusedBorderColor = Primary
                    ),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            onCreateFolder(currentPath, newFolderName.trim())
                            showCreateDialog = false
                            newFolderName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
                ) {
                    Text("Oluştur", color = OnPrimaryContainer)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("İptal", color = OnSurfaceVariant)
                }
            }
        )
    }

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceContainer,
                    titleContentColor = OnSurface
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        if (!onNavigateUp()) onDismiss()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = OnSurface)
                    }
                },
                title = {
                    Column {
                        Text(
                            text = if (isMove) "Taşınacak Hedef Klasör" else "Kopyalanacak Hedef Klasör",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            text = currentPath,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = "Yeni Klasör", tint = Primary)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = SurfaceContainer,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurfaceVariant)
                    ) {
                        Text("İptal")
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
                    ) {
                        Text(
                            text = if (isMove) "Buraya Taşı" else "Buraya Kopyala",
                            color = OnPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { padding ->
        val folderList = remember(files) { files.filter { it.isDirectory } }

        if (folderList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        tint = Outline,
                        modifier = Modifier.size(48.dp)
                    )
                    Text("Bu klasörde alt klasör yok", color = OnSurfaceVariant, fontSize = 14.sp)
                    Text("Mevcut konumu seçmek için 'Buraya' butonuna dokunun.", color = Outline, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(folderList) { folder ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(folder.path) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryContainer.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = folder.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = OnSurface
                            )
                            Text(
                                text = "${folder.childCount} öge",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Outline
                            )
                        }
                    }
                    HorizontalDivider(color = SurfaceContainerHighest.copy(alpha = 0.4f))
                }
            }
        }
    }
}
