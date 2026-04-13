package com.weightflow.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

// Neutral base — warm dark
val Neutral950 = Color(0xFF0F0E0B)
val Neutral900 = Color(0xFF1A1914)
val Neutral800 = Color(0xFF2A2922)
val Neutral100 = Color(0xFFEEEDE8)

// ─── Palette accent colours ─────────────────────────────────────────────────

private object Lime {
    val primary          = Color(0xFFC8FF00)
    val onPrimary        = Color(0xFF1A2200)
    val primaryContainer = Color(0xFF3A4800)
    val secondary        = Color(0xFFAACC00)
    val bg               = Neutral950
    val surface          = Neutral900
    val surfaceVariant   = Neutral800
}

private object Forest {
    val primary          = Color(0xFF4CAF50)
    val onPrimary        = Color(0xFF00210A)
    val primaryContainer = Color(0xFF0A2E0B)
    val secondary        = Color(0xFF81C784)
    val bg               = Color(0xFF0D1410)
    val surface          = Color(0xFF151D16)
    val surfaceVariant   = Color(0xFF252D26)
}

private object Ocean {
    val primary          = Color(0xFF00BCD4)
    val onPrimary        = Color(0xFF002C33)
    val primaryContainer = Color(0xFF003640)
    val secondary        = Color(0xFF4DD0E1)
    val bg               = Color(0xFF0A1419)
    val surface          = Color(0xFF131E23)
    val surfaceVariant   = Color(0xFF213035)
}

private object Sunset {
    val primary          = Color(0xFFFF6B35)
    val onPrimary        = Color(0xFF3A1200)
    val primaryContainer = Color(0xFF3D1800)
    val secondary        = Color(0xFFFF8A65)
    val bg               = Color(0xFF150E0B)
    val surface          = Color(0xFF201512)
    val surfaceVariant   = Color(0xFF302420)
}

private object Rose {
    val primary          = Color(0xFFFF4081)
    val onPrimary        = Color(0xFF3D0028)
    val primaryContainer = Color(0xFF3D0018)
    val secondary        = Color(0xFFFF80AB)
    val bg               = Color(0xFF150B10)
    val surface          = Color(0xFF201318)
    val surfaceVariant   = Color(0xFF302028)
}

private object Violet {
    val primary          = Color(0xFFBB86FC)
    val onPrimary        = Color(0xFF21005C)
    val primaryContainer = Color(0xFF21005C)
    val secondary        = Color(0xFFCF9FFF)
    val bg               = Color(0xFF100B15)
    val surface          = Color(0xFF1A1420)
    val surfaceVariant   = Color(0xFF282030)
}

private object Gold {
    val primary          = Color(0xFFFFD700)
    val onPrimary        = Color(0xFF3A2D00)
    val primaryContainer = Color(0xFF3D3000)
    val secondary        = Color(0xFFFFE57F)
    val bg               = Color(0xFF131109)
    val surface          = Color(0xFF1E1C12)
    val surfaceVariant   = Color(0xFF2E2B1E)
}

private object Ice {
    val primary          = Color(0xFF80DEEA)
    val onPrimary        = Color(0xFF003740)
    val primaryContainer = Color(0xFF003740)
    val secondary        = Color(0xFFB2EBF2)
    val bg               = Color(0xFF0B1315)
    val surface          = Color(0xFF141E20)
    val surfaceVariant   = Color(0xFF222C2E)
}

// ─── Palette → ColorScheme ───────────────────────────────────────────────────

fun colorSchemeForPalette(palette: String): ColorScheme = when (palette) {
    "lime"   -> limePalette()
    "forest" -> forestPalette()
    "ocean"  -> oceanPalette()
    "sunset" -> sunsetPalette()
    "rose"   -> rosePalette()
    "violet" -> violetPalette()
    "gold"   -> goldPalette()
    "ice"    -> icePalette()
    else     -> limePalette()
}

private fun limePalette() = darkColorScheme(
    primary = Lime.primary,
    onPrimary = Lime.onPrimary,
    primaryContainer = Lime.primaryContainer,
    onPrimaryContainer = Color(0xFFD4FF6A),
    secondary = Lime.secondary,
    onSecondary = Color(0xFF1F2C00),
    background = Lime.bg,
    onBackground = Neutral100,
    surface = Lime.surface,
    onSurface = Neutral100,
    surfaceVariant = Lime.surfaceVariant,
    onSurfaceVariant = Neutral100,
)

private fun forestPalette() = darkColorScheme(
    primary = Forest.primary,
    onPrimary = Forest.onPrimary,
    primaryContainer = Forest.primaryContainer,
    onPrimaryContainer = Color(0xFFA8F5AC),
    secondary = Forest.secondary,
    onSecondary = Color(0xFF003911),
    background = Forest.bg,
    onBackground = Neutral100,
    surface = Forest.surface,
    onSurface = Neutral100,
    surfaceVariant = Forest.surfaceVariant,
    onSurfaceVariant = Neutral100,
)

private fun oceanPalette() = darkColorScheme(
    primary = Ocean.primary,
    onPrimary = Ocean.onPrimary,
    primaryContainer = Ocean.primaryContainer,
    onPrimaryContainer = Color(0xFF8CF4FF),
    secondary = Ocean.secondary,
    onSecondary = Color(0xFF003740),
    background = Ocean.bg,
    onBackground = Neutral100,
    surface = Ocean.surface,
    onSurface = Neutral100,
    surfaceVariant = Ocean.surfaceVariant,
    onSurfaceVariant = Neutral100,
)

private fun sunsetPalette() = darkColorScheme(
    primary = Sunset.primary,
    onPrimary = Sunset.onPrimary,
    primaryContainer = Sunset.primaryContainer,
    onPrimaryContainer = Color(0xFFFFBBA0),
    secondary = Sunset.secondary,
    onSecondary = Color(0xFF3D1700),
    background = Sunset.bg,
    onBackground = Neutral100,
    surface = Sunset.surface,
    onSurface = Neutral100,
    surfaceVariant = Sunset.surfaceVariant,
    onSurfaceVariant = Neutral100,
)

private fun rosePalette() = darkColorScheme(
    primary = Rose.primary,
    onPrimary = Rose.onPrimary,
    primaryContainer = Rose.primaryContainer,
    onPrimaryContainer = Color(0xFFFFB3D1),
    secondary = Rose.secondary,
    onSecondary = Color(0xFF3D0033),
    background = Rose.bg,
    onBackground = Neutral100,
    surface = Rose.surface,
    onSurface = Neutral100,
    surfaceVariant = Rose.surfaceVariant,
    onSurfaceVariant = Neutral100,
)

private fun violetPalette() = darkColorScheme(
    primary = Violet.primary,
    onPrimary = Violet.onPrimary,
    primaryContainer = Violet.primaryContainer,
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Violet.secondary,
    onSecondary = Color(0xFF31007F),
    background = Violet.bg,
    onBackground = Neutral100,
    surface = Violet.surface,
    onSurface = Neutral100,
    surfaceVariant = Violet.surfaceVariant,
    onSurfaceVariant = Neutral100,
)

private fun goldPalette() = darkColorScheme(
    primary = Gold.primary,
    onPrimary = Gold.onPrimary,
    primaryContainer = Gold.primaryContainer,
    onPrimaryContainer = Color(0xFFFFE570),
    secondary = Gold.secondary,
    onSecondary = Color(0xFF3A3000),
    background = Gold.bg,
    onBackground = Neutral100,
    surface = Gold.surface,
    onSurface = Neutral100,
    surfaceVariant = Gold.surfaceVariant,
    onSurfaceVariant = Neutral100,
)

private fun icePalette() = darkColorScheme(
    primary = Ice.primary,
    onPrimary = Ice.onPrimary,
    primaryContainer = Ice.primaryContainer,
    onPrimaryContainer = Color(0xFFCBF0F8),
    secondary = Ice.secondary,
    onSecondary = Color(0xFF004550),
    background = Ice.bg,
    onBackground = Neutral100,
    surface = Ice.surface,
    onSurface = Neutral100,
    surfaceVariant = Ice.surfaceVariant,
    onSurfaceVariant = Neutral100,
)
