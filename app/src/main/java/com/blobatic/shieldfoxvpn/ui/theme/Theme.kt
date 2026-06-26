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
// Philosophy: one strong accent, maximum restraint everywhere else.

// Accent — electric indigo/violet. Unique in the VPN space, premium feel.
val Indigo       = Color(0xFF818CF8)   // indigo-400
val IndigoDim    = Color(0xFF6366F1)   // indigo-500
val IndigoSubtle = Color(0x18818CF8)   // indigo glow

// Semantic states
val Emerald      = Color(0xFF34D399)   // emerald-400  — Connected / secure
val EmeraldDim   = Color(0xFF10B981)   // emerald-500
val EmeraldSubtle= Color(0x1534D399)

val Amber        = Color(0xFFFBBF24)   // amber-400    — Connecting / caution
val Rose         = Color(0xFFF87171)   // rose-400     — Error

// Dark canvas — near-black with a hair of cool indigo tint
val Canvas       = Color(0xFF09090E)   // deepest background
val Surface1     = Color(0xFF111318)   // cards / sheets
val Surface2     = Color(0xFF1A1D27)   // elevated elements
val Border       = Color(0xFF22263A)   // hairline separators

// Text
val White        = Color(0xFFFFFFFF)
val White60      = Color(0x99FFFFFF)   // 60% white
val White35      = Color(0x59FFFFFF)   // 35% white
val White15      = Color(0x26FFFFFF)   // 15% white

// ─── Color Schemes ────────────────────────────────────────────────────────────

private val DarkScheme = darkColorScheme(
    primary              = Indigo,
    onPrimary            = Color(0xFF1E1B4B),
    primaryContainer     = Color(0xFF312E81),
    onPrimaryContainer   = Indigo,

    secondary            = Emerald,
    onSecondary          = Color(0xFF022C22),
    secondaryContainer   = Color(0xFF064E3B),
    onSecondaryContainer = Emerald,

    tertiary             = Amber,
    onTertiary           = Color(0xFF3B2000),

    background           = Canvas,
    onBackground         = White,

    surface              = Surface1,
    onSurface            = White,
    surfaceVariant       = Surface2,
    onSurfaceVariant     = White60,

    error                = Rose,
    onError              = White,
    errorContainer       = Color(0xFF3B0A0A),
    onErrorContainer     = Color(0xFFFCA5A5),

    outline              = Border,
    outlineVariant       = Color(0xFF161920),
)

private val LightScheme = lightColorScheme(
    primary              = IndigoDim,
    onPrimary            = White,
    primaryContainer     = Color(0xFFEEF2FF),
    onPrimaryContainer   = Color(0xFF3730A3),

    secondary            = EmeraldDim,
    onSecondary          = White,
    secondaryContainer   = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF065F46),

    background           = Color(0xFFF8F9FF),
    onBackground         = Color(0xFF0F0F1A),

    surface              = White,
    onSurface            = Color(0xFF0F0F1A),
    surfaceVariant       = Color(0xFFF0F1FF),
    onSurfaceVariant     = Color(0xFF4B5280),

    error                = Rose,
    onError              = White,
    outline              = Color(0xFFDDE1F5),
    outlineVariant       = Color(0xFFEEF0FF),
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
            val bg = if (darkTheme) Canvas.toArgb() else Color(0xFFF8F9FF).toArgb()
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
