package com.example.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Ana Sayfa", Icons.Default.Home)
    object Files : Screen("files", "Dosyalar", Icons.Default.Folder)
    object Transfers : Screen("transfers", "Aktarımlar", Icons.Default.SwapVert)
    object Settings : Screen("settings", "Ayarlar", Icons.Default.Settings)
}

val items = listOf(Screen.Home, Screen.Files, Screen.Transfers, Screen.Settings)
