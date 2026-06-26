package com.blobatic.shieldfoxvpn.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blobatic.shieldfoxvpn.data.model.VpnState
import com.blobatic.shieldfoxvpn.ui.theme.Amber
import com.blobatic.shieldfoxvpn.ui.theme.Canvas
import com.blobatic.shieldfoxvpn.ui.theme.Emerald
import com.blobatic.shieldfoxvpn.ui.theme.Indigo
import com.blobatic.shieldfoxvpn.ui.theme.Rose
import com.blobatic.shieldfoxvpn.ui.theme.Surface1
import com.blobatic.shieldfoxvpn.ui.theme.White15

/**
 * ShieldFox — Minimalist Power Button.
 *
 * The button is a clean circle. The ring IS the state indicator:
 *   Idle       → dim gray ring, ghost power icon
 *   Connecting → spinning indigo arc (dashed feel)
 *   Connected  → solid emerald ring, breathing glow
 *   Error      → rose ring
 *
 * No gradient plates, no halo boxes — just the ring and the icon.
 */
@Composable
fun VpnPowerButton(
    vpnState: VpnState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp
) {
    val isConnected  = vpnState is VpnState.Connected
    val isConnecting = vpnState is VpnState.Connecting || vpnState is VpnState.Disconnecting
    val isError      = vpnState is VpnState.Error

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Press shrink
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow
        ),
        label = "press"
    )

    // Ring + icon color
    val ringColor by animateColorAsState(
        targetValue = when {
            isConnected  -> Emerald
            isError      -> Rose
            isConnecting -> Indigo
            else         -> Color(0xFF2A2E3F)    // dim idle ring
        },
        animationSpec = tween(600),
        label = "ring"
    )

    val iconColor by animateColorAsState(
        targetValue = when {
            isConnected  -> Emerald
            isError      -> Rose
            isConnecting -> Indigo
            else         -> Color(0xFF3D4460)    // dim idle icon
        },
        animationSpec = tween(600),
        label = "icon"
    )

    // Spinning arc — connecting state
    val spin = rememberInfiniteTransition(label = "spin")
    val arcAngle by spin.animateFloat(
        initialValue = -90f,
        targetValue  = if (isConnecting) 270f else -90f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "arc"
    )

    // Breathing opacity — connected state
    val breathe = rememberInfiniteTransition(label = "breathe")
    val breatheAlpha by breathe.animateFloat(
        initialValue = 0.6f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            tween(2200, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // ── Outer ring drawn on Canvas ────────────────────────────────────────
        Canvas(modifier = Modifier.size(size)) {
            val stroke = 2.dp.toPx()
            val r = size.toPx() / 2f - stroke / 2f

            when {
                // Spinning arc — connecting
                isConnecting -> {
                    // Full dim ring
                    drawCircle(
                        color  = Indigo.copy(alpha = 0.15f),
                        radius = r,
                        style  = Stroke(width = stroke)
                    )
                    // Bright spinning sweep (270° head + 60° fade)
                    drawArc(
                        color      = Indigo,
                        startAngle = arcAngle,
                        sweepAngle = 220f,
                        useCenter  = false,
                        topLeft    = androidx.compose.ui.geometry.Offset(
                            center.x - r, center.y - r
                        ),
                        size  = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
                // Solid ring — connected with breathing alpha
                isConnected -> {
                    drawCircle(
                        color  = Emerald.copy(alpha = breatheAlpha),
                        radius = r,
                        style  = Stroke(width = 2.5.dp.toPx())
                    )
                }
                // Error ring
                isError -> {
                    drawCircle(
                        color  = Rose.copy(alpha = 0.7f),
                        radius = r,
                        style  = Stroke(width = stroke)
                    )
                }
                // Idle hairline ring
                else -> {
                    drawCircle(
                        color  = Color(0xFF1E2235),
                        radius = r,
                        style  = Stroke(width = stroke)
                    )
                }
            }
        }

        // ── Button circle ─────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(size * 0.74f)
                .clip(CircleShape)
                .background(Surface1)
                .clickable(
                    interactionSource = interactionSource,
                    indication        = null
                ) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            // Subtle inner glow when connected
            if (isConnected) {
                Box(
                    modifier = Modifier
                        .size(size * 0.74f)
                        .clip(CircleShape)
                        .background(Emerald.copy(alpha = 0.05f * breatheAlpha))
                )
            }

            Icon(
                imageVector         = Icons.Default.PowerSettingsNew,
                contentDescription  = "Connect / Disconnect VPN",
                tint                = iconColor,
                modifier            = Modifier.size(size * 0.28f)
            )
        }
    }
}
