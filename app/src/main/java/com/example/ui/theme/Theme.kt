package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun XFilesTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = DarkColors,
    typography = Typography,
    content = content
  )
}
