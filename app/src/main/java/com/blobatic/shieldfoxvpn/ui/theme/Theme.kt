package com.blobatic.shieldfoxvpn.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ─── Theme Manager ────────────────────────────────────────────────────────────

object ThemeManager {
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()
    fun setDarkTheme(enabled: Boolean) { _isDarkTheme.value = enabled }
}

// ─── Palette ──────────────────────────────────────────────────────────────────
// Philosophy: Electric cyber-security vibes. Dark sapphire and neon emerald.

// Sapphire Brand Primary
val Sapphire      = Color(0xFF3B82F6)   // vibrant sapphire blue
val SapphireDim   = Color(0xFF1D4ED8)   // dark sapphire
val SapphireGlow  = Color(0x183B82F6)   // glow overlay

// Semantic states
val NeonEmerald   = Color(0xFF10B981)   // active secure state
val NeonEmeraldDim= Color(0xFF047857)
val NeonEmeraldGlow = Color(0x1510B981)

val Amber         = Color(0xFFF59E0B)   // connecting / warning
val Rose          = Color(0xFFEF4444)   // error state

// Dark Space Canvas
val SpaceBlack    = Color(0xFF070913)   // deep space navy-black background
val SurfaceGlass  = Color(0xFF0F1322)   // glass container surface
val SurfaceElevated = Color(0xFF181C30) // elevated inputs/cards
val GlassBorder   = Color(0xFF1E2640)   // subtle cyber outline

// Text
val White         = Color(0xFFFFFFFF)
val TextPrimary   = Color(0xFFF1F5F9)   // slate 100
val TextSecondary = Color(0xFF94A3B8)   // slate 400
val TextMuted     = Color(0xFF64748B)   // slate 500
val GlassOpacity  = Color(0x12FFFFFF)   // transparent hover overlay

// ─── Color Schemes ────────────────────────────────────────────────────────────

private val DarkScheme = darkColorScheme(
    primary              = Sapphire,
    onPrimary            = Color(0xFFFFFFFF),
    primaryContainer     = Color(0xFF1E3A8A),
    onPrimaryContainer   = Sapphire,

    secondary            = NeonEmerald,
    onSecondary          = Color(0xFFFFFFFF),
    secondaryContainer   = Color(0xFF064E3B),
    onSecondaryContainer = NeonEmerald,

    tertiary             = Amber,
    onTertiary           = Color(0xFFFFFFFF),

    background           = SpaceBlack,
    onBackground         = TextPrimary,

    surface              = SurfaceGlass,
    onSurface            = TextPrimary,
    surfaceVariant       = SurfaceElevated,
    onSurfaceVariant     = TextSecondary,

    error                = Rose,
    onError              = Color(0xFFFFFFFF),
    errorContainer       = Color(0xFF7F1D1D),
    onErrorContainer     = Color(0xFFFCA5A5),

    outline              = GlassBorder,
    outlineVariant       = Color(0xFF1E293B),
)

private val LightScheme = lightColorScheme(
    primary              = SapphireDim,
    onPrimary            = Color(0xFFFFFFFF),
    primaryContainer     = Color(0xFFEFF6FF),
    onPrimaryContainer   = Color(0xFF1E40AF),

    secondary            = NeonEmeraldDim,
    onSecondary          = Color(0xFFFFFFFF),
    secondaryContainer   = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF065F46),

    background           = Color(0xFFF8FAFC), // slate 50
    onBackground         = Color(0xFF0F172A), // slate 900

    surface              = Color(0xFFFFFFFF),
    onSurface            = Color(0xFF0F172A),
    surfaceVariant       = Color(0xFFF1F5F9), // slate 100
    onSurfaceVariant     = Color(0xFF475569), // slate 600

    error                = Rose,
    onError              = Color(0xFFFFFFFF),
    outline              = Color(0xFFE2E8F0),
    outlineVariant       = Color(0xFFCBD5E1),
)

// ─── Theme ────────────────────────────────────────────────────────────────────

@Composable
fun ShieldFoxTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkScheme else LightScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val bg = if (darkTheme) SpaceBlack.toArgb() else Color(0xFFF8FAFC).toArgb()
            window.statusBarColor = bg
            window.navigationBarColor = bg
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars    = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = ShieldFoxTypography,
        content     = content
    )
}
