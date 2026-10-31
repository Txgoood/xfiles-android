package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.BrowserScreen
import com.example.ui.HomeScreen
import com.example.ui.SettingsScreen
import com.example.ui.StorageAnalysisScreen
import com.example.ui.TransfersScreen
import com.example.ui.items
import com.example.ui.theme.OnSurfaceVariant
import com.example.ui.theme.Primary
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.XFilesTheme
import com.example.viewmodel.XFilesViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XFilesTheme {
                val viewModel: XFilesViewModel = viewModel()

                val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_AUDIO
                    )
                } else {
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { _ ->
                    viewModel.refresh()
                }

                LaunchedEffect(Unit) {
                    permissionLauncher.launch(permissions)
                }

                var currentRoute by remember { mutableStateOf("home") }

                BackHandler(enabled = currentRoute != "home") {
                    currentRoute = "home"
                }

                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = SurfaceContainer
                        ) {
                            items.forEach { screen ->
                                val selected = currentRoute == screen.route
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = { Text(screen.title) },
                                    selected = selected,
                                    onClick = { currentRoute = screen.route },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Primary,
                                        selectedTextColor = Primary,
                                        unselectedIconColor = OnSurfaceVariant,
                                        unselectedTextColor = OnSurfaceVariant,
                                        indicatorColor = SurfaceContainer
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    when (currentRoute) {
                        "home" -> HomeScreen(
                            modifier = Modifier.padding(innerPadding),
                            viewModel = viewModel,
                            onNavigateToFiles = { path ->
                                viewModel.navigateToPath(path)
                                currentRoute = "files"
                            },
                            onNavigateToAnalysis = {
                                currentRoute = "analysis"
                            }
                        )
                        "files" -> {
                            val path by viewModel.currentPath.collectAsState()
                            BrowserScreen(
                                startPath = path,
                                modifier = Modifier.padding(innerPadding),
                                viewModel = viewModel
                            )
                        }
                        "transfers" -> TransfersScreen(
                            modifier = Modifier.padding(innerPadding),
                            viewModel = viewModel
                        )
                        "settings" -> SettingsScreen(
                            modifier = Modifier.padding(innerPadding),
                            viewModel = viewModel,
                            onNavigateToAnalysis = {
                                currentRoute = "analysis"
                            }
                        )
                        "analysis" -> StorageAnalysisScreen(
                            modifier = Modifier.padding(innerPadding),
                            viewModel = viewModel,
                            onBack = { currentRoute = "home" }
                        )
                        else -> HomeScreen(
                            modifier = Modifier.padding(innerPadding),
                            viewModel = viewModel,
                            onNavigateToFiles = { path ->
                                viewModel.navigateToPath(path)
                                currentRoute = "files"
                            },
                            onNavigateToAnalysis = {
                                currentRoute = "analysis"
                            }
                        )
                    }
                }
            }
        }
    }
}
