package com.securevpn.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
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

// ─── Theme Manager Singleton ─────────────────────────────────────────────────

object ThemeManager {
    private val _isDarkTheme = MutableStateFlow(true) // Default to Dark theme
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun setDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
    }
}

// ─── Color Palette ────────────────────────────────────────────────────────────

val VpnGreen       = Color(0xFF10B981) // Emerald
val VpnGreenDark   = Color(0xFF059669)
val VpnGreenGlow   = Color(0x1110B981)
val VpnTeal        = Color(0xFF0EA5E9) // Sky
val VpnBlue        = Color(0xFF6366F1) // Indigo

// Dark Mode Tokens
val BackgroundDark  = Color(0xFF0A0B10)
val SurfaceDark     = Color(0xFF12141C)
val CardDark        = Color(0xFF1A1D26)
val DividerDark     = Color(0xFF222631)

// Muted Text
val TextPrimary     = Color(0xFFF3F4F6)
val TextSecondary   = Color(0xFF9CA3AF)
val TextDisabled    = Color(0xFF4B5563)

val ErrorRed        = Color(0xFFEF4444)
val WarningAmber    = Color(0xFFF59E0B)

// ─── Color Schemes ────────────────────────────────────────────────────────────

private val DarkColorScheme = darkColorScheme(
    primary            = VpnGreen,
    onPrimary          = Color(0xFF064E3B),
    primaryContainer   = Color(0xFF065F46),
    onPrimaryContainer = VpnGreen,

    secondary          = VpnTeal,
    onSecondary        = Color(0xFF0C4A6E),
    secondaryContainer = Color(0xFF075985),
    onSecondaryContainer = VpnTeal,

    background         = BackgroundDark,
    onBackground       = TextPrimary,

    surface            = SurfaceDark,
    onSurface          = TextPrimary,
    surfaceVariant     = CardDark,
    onSurfaceVariant   = TextSecondary,

    error              = ErrorRed,
    onError            = Color.White,

    outline            = DividerDark
)

private val LightColorScheme = lightColorScheme(
    primary            = VpnGreen,
    onPrimary          = Color.White,
    primaryContainer   = VpnGreenGlow,
    onPrimaryContainer = VpnGreenDark,

    secondary          = VpnTeal,
    onSecondary        = Color.White,
    secondaryContainer = VpnTeal.copy(alpha = 0.1f),
    onSecondaryContainer = VpnTeal,

    background         = Color(0xFFF9FAFB),
    onBackground       = Color(0xFF111827),

    surface            = Color.White,
    onSurface          = Color(0xFF111827),
    surfaceVariant     = Color(0xFFF3F4F6),
    onSurfaceVariant   = Color(0xFF4B5563),

    error              = ErrorRed,
    onError            = Color.White,

    outline            = Color(0xFFE5E7EB)
)

// ─── Theme Builder ────────────────────────────────────────────────────────────

@Composable
fun WingerVpnTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val barColor = if (darkTheme) BackgroundDark.toArgb() else Color.White.toArgb()
            window.statusBarColor = barColor
            window.navigationBarColor = barColor
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VpnTypography,
        content = content
    )
}
