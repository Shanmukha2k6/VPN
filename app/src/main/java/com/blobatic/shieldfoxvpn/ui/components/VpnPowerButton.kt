package com.blobatic.shieldfoxvpn.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blobatic.shieldfoxvpn.data.model.VpnState
import com.blobatic.shieldfoxvpn.ui.theme.AccentBlue
import com.blobatic.shieldfoxvpn.ui.theme.AccentBlueGlow
import com.blobatic.shieldfoxvpn.ui.theme.ErrorRed
import com.blobatic.shieldfoxvpn.ui.theme.SecureGreen
import com.blobatic.shieldfoxvpn.ui.theme.SecureGreenGlow
import com.blobatic.shieldfoxvpn.ui.theme.WarningAmber

/**
 * ShieldFox Power Button — premium tactile VPN connect/disconnect control.
 *
 * Visual states:
 *  • Idle        → muted gray ring, ghost icon
 *  • Connecting  → spinning electric-blue arc, pulsing ring
 *  • Connected   → emerald glow with steady solid ring, shadow bloom
 *  • Error       → red warning ring
 */
@Composable
fun VpnPowerButton(
    vpnState: VpnState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp
) {
    val isConnected   = vpnState is VpnState.Connected
    val isConnecting  = vpnState is VpnState.Connecting || vpnState is VpnState.Disconnecting
    val isError       = vpnState is VpnState.Error

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth tactile press scale
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pressScale"
    )

    // State color — drives icon tint, ring, shadow
    val stateColor by animateColorAsState(
        targetValue = when {
            isConnected  -> SecureGreen
            isError      -> ErrorRed
            isConnecting -> AccentBlue
            else         -> Color(0xFF3D4A5C) // Idle ghost
        },
        animationSpec = tween(500),
        label = "stateColor"
    )

    // Connected halo glow color
    val glowColor by animateColorAsState(
        targetValue = when {
            isConnected -> SecureGreenGlow
            isError     -> ErrorRed.copy(alpha = 0.15f)
            else        -> Color.Transparent
        },
        animationSpec = tween(700),
        label = "glowColor"
    )

    // Spinning arc — only during connecting/disconnecting
    val rotateAnim = rememberInfiniteTransition(label = "arc_rotate")
    val arcRotation by rotateAnim.animateFloat(
        initialValue = 0f,
        targetValue = if (isConnecting) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "arcRotation"
    )

    // Connected ring slow pulse — gentle "breathing"
    val breatheAnim = rememberInfiniteTransition(label = "breathe")
    val breatheAlpha by breatheAnim.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )
    val ringAlpha = if (isConnected) breatheAlpha else 1.0f

    Box(
        modifier = modifier
            .size(size)
            .scale(pressScale),
        contentAlignment = Alignment.Center
    ) {
        // ── Outer glow halo (connected only) ─────────────────────────────────
        if (isConnected || isError) {
            Box(
                modifier = Modifier
                    .size(size * 1.18f)
                    .clip(CircleShape)
                    .background(glowColor)
            )
        }

        // ── Spinning connecting arc ───────────────────────────────────────────
        if (isConnecting) {
            Canvas(modifier = Modifier.size(size * 1.04f)) {
                val r = (size.toPx() * 1.04f / 2f) - 3.dp.toPx()
                // Dim full ring
                drawCircle(
                    color = AccentBlue.copy(alpha = 0.12f),
                    radius = r,
                    style = Stroke(width = 2.5.dp.toPx())
                )
                // Bright spinning sweep
                drawArc(
                    color = AccentBlue,
                    startAngle = arcRotation,
                    sweepAngle = 100f,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(center.x - r, center.y - r),
                    size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                // Trailing fade arc
                drawArc(
                    color = AccentBlue.copy(alpha = 0.3f),
                    startAngle = arcRotation + 100f,
                    sweepAngle = 60f,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(center.x - r, center.y - r),
                    size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // ── Static state ring ─────────────────────────────────────────────────
        if (!isConnecting) {
            Canvas(modifier = Modifier.size(size)) {
                val r = (size.toPx() / 2f) - 5.dp.toPx()
                drawCircle(
                    color = stateColor.copy(alpha = if (isConnected) 0.5f * ringAlpha else 0.2f),
                    radius = r,
                    style = Stroke(width = if (isConnected) 2.dp.toPx() else 1.5.dp.toPx())
                )
            }
        }

        // ── Central tactile button plate ──────────────────────────────────────
        Box(
            modifier = Modifier
                .size(size * 0.76f)
                .shadow(
                    elevation = when {
                        isConnected  -> 20.dp
                        isConnecting -> 8.dp
                        else         -> 4.dp
                    },
                    shape = CircleShape,
                    ambientColor = stateColor.copy(alpha = 0.25f),
                    spotColor   = stateColor.copy(alpha = 0.5f)
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = when {
                            isConnected -> listOf(
                                Color(0xFF1A2E1E),
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                            isConnecting -> listOf(
                                Color(0xFF0F1E2C),
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                            else -> listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    )
                )
                .border(
                    width = if (isConnected) 1.5.dp else 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            stateColor.copy(alpha = if (isConnected) 0.6f else 0.2f),
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = "VPN Power — tap to connect or disconnect",
                modifier = Modifier.size(size * 0.30f),
                tint = stateColor
            )
        }
    }
}
