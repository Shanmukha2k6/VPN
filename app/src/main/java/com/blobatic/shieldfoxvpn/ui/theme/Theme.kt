package com.blobatic.shieldfoxvpn.ui.theme

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
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun setDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
    }
}

// ─── ShieldFox Color Palette ──────────────────────────────────────────────────
// Primary accent: Electric Sky Blue — confident, technical, calm
val AccentBlue      = Color(0xFF38BDF8)   // Sky-400
val AccentBlueDim   = Color(0xFF0EA5E9)   // Sky-500 (hover / pressed)
val AccentBlueGlow  = Color(0x2038BDF8)   // Glow ring alpha

// Secure state: Emerald green
val SecureGreen     = Color(0xFF22C55E)   // Green-500
val SecureGreenDim  = Color(0xFF16A34A)   // Green-600
val SecureGreenGlow = Color(0x2022C55E)

// Neutral semantic
val ErrorRed        = Color(0xFFEF4444)   // Red-500
val WarningAmber    = Color(0xFFF59E0B)   // Amber-400
val InfoCyan        = Color(0xFF22D3EE)   // Cyan-400

// ─── Dark Canvas ──────────────────────────────────────────────────────────────
// Near-black with a faint cool undertone — not pure black, premium depth
val BgDark          = Color(0xFF0D0F14)   // Deep canvas
val SurfaceDark     = Color(0xFF161B22)   // GitHub-dark-inspired
val CardDark        = Color(0xFF1E2430)   // Elevated card
val CardDarkBorder  = Color(0xFF262D3A)   // Subtle card border
val DividerDark     = Color(0xFF1F2937)   // Inter-row divider

// ─── Text ─────────────────────────────────────────────────────────────────────
val TextPrimary     = Color(0xFFF0F4FF)   // Slightly warm white
val TextSecondary   = Color(0xFF8B96A9)   // Cool-gray muted
val TextDisabled    = Color(0xFF3D4656)

// ─── Light Canvas ──────────────────────────────────────────────────────────────
val BgLight         = Color(0xFFF5F8FF)
val SurfaceLight    = Color(0xFFFFFFFF)
val CardLight       = Color(0xFFEFF3FA)

// ─── Color Schemes ────────────────────────────────────────────────────────────

private val DarkColorScheme = darkColorScheme(
    primary            = AccentBlue,
    onPrimary          = Color(0xFF0A2E44),
    primaryContainer   = Color(0xFF103A54),
    onPrimaryContainer = AccentBlue,

    secondary          = SecureGreen,
    onSecondary        = Color(0xFF052E16),
    secondaryContainer = Color(0xFF14532D),
    onSecondaryContainer = SecureGreen,

    tertiary           = InfoCyan,
    onTertiary         = Color(0xFF083344),

    background         = BgDark,
    onBackground       = TextPrimary,

    surface            = SurfaceDark,
    onSurface          = TextPrimary,
    surfaceVariant     = CardDark,
    onSurfaceVariant   = TextSecondary,

    error              = ErrorRed,
    onError            = Color.White,
    errorContainer     = Color(0xFF3B0A0A),
    onErrorContainer   = Color(0xFFFCA5A5),

    outline            = CardDarkBorder,
    outlineVariant     = DividerDark,

    inverseSurface     = TextPrimary,
    inverseOnSurface   = BgDark,
)

private val LightColorScheme = lightColorScheme(
    primary            = AccentBlueDim,
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFDBEFFD),
    onPrimaryContainer = Color(0xFF0369A1),

    secondary          = SecureGreenDim,
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFFDCFCE7),
    onSecondaryContainer = Color(0xFF15803D),

    background         = BgLight,
    onBackground       = Color(0xFF0F172A),

    surface            = SurfaceLight,
    onSurface          = Color(0xFF0F172A),
    surfaceVariant     = CardLight,
    onSurfaceVariant   = Color(0xFF475569),

    error              = ErrorRed,
    onError            = Color.White,
    errorContainer     = Color(0xFFFEE2E2),
    onErrorContainer   = Color(0xFF991B1B),

    outline            = Color(0xFFCBD5E1),
    outlineVariant     = Color(0xFFE2E8F0)
)

// ─── Theme Builder ────────────────────────────────────────────────────────────

@Composable
fun ShieldFoxTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val barColor = if (darkTheme) BgDark.toArgb() else BgLight.toArgb()
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
        typography = ShieldFoxTypography,
        content = content
    )
}
