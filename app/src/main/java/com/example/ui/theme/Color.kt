package com.example.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

// Stitch Precision Android File Governance Design System Tokens
val Surface = Color(0xFF101319)
val SurfaceDim = Color(0xFF101319)
val SurfaceBright = Color(0xFF36393F)
val SurfaceContainerLowest = Color(0xFF0B0E13)
val SurfaceContainerLow = Color(0xFF191C21)
val SurfaceContainer = Color(0xFF1D2025)
val SurfaceContainerHigh = Color(0xFF272A2F)
val SurfaceContainerHighest = Color(0xFF32353A)

val OnSurface = Color(0xFFE1E2E9)
val OnSurfaceVariant = Color(0xFFC3C6D7)
val Outline = Color(0xFF8D90A0)
val OutlineVariant = Color(0xFF434655)

val Primary = Color(0xFFB4C5FF)
val OnPrimary = Color(0xFF002A78)
val PrimaryContainer = Color(0xFF2563EB)
val OnPrimaryContainer = Color(0xFFEEEFFF)

val Secondary = Color(0xFF4EDEA3)
val OnSecondary = Color(0xFF003824)
val SecondaryContainer = Color(0xFF00A572)
val OnSecondaryContainer = Color(0xFF00311F)

val Tertiary = Color(0xFFD0BCFF)
val OnTertiary = Color(0xFF3C0091)
val TertiaryContainer = Color(0xFF7D4CE7)
val OnTertiaryContainer = Color(0xFFF6EDFF)

val Error = Color(0xFFFFB4AB)
val OnError = Color(0xFF690005)
val ErrorContainer = Color(0xFF93000A)
val OnErrorContainer = Color(0xFFFFDAD6)

val ColorError = Error
val ColorErrorContainer = ErrorContainer
val ColorOnError = OnError
val ColorOnErrorContainer = OnErrorContainer

// Semantic MIME colors per Stitch spec
val MimeDocument = Color(0xFF10B981) // Green
val MimeArchive = Color(0xFFF59E0B)  // Amber / Gold
val MimeMediaVideo = Color(0xFF8B5CF6) // Purple
val MimeMediaAudio = Color(0xFF06B6D4) // Cyan
val MimeImage = Color(0xFF4EDEA3)     // Emerald green
val MimeApp = Color(0xFF3B82F6)       // Blue
val MimePdf = Color(0xFFEF4444)       // Red

val DarkColors = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceContainerHighest,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
    background = Surface,
    onBackground = OnSurface
)
