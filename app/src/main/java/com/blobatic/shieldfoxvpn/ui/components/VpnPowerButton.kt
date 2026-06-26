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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blobatic.shieldfoxvpn.data.model.VpnState
import com.blobatic.shieldfoxvpn.ui.theme.*

/**
 * ShieldFox — Premium Tactile Power Button.
 *
 * Designed to look like a premium control button with:
 * - Concentric sonar/radar pulse waves when connecting or active.
 * - Glassmorphic dial plate with dual neon gradient border.
 * - Dynamic color transitions: Sapphire (connecting) -> NeonEmerald (connected) -> Rose (failed) -> SpaceGrey (idle).
 */
@Composable
fun VpnPowerButton(
    vpnState: VpnState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp
) {
    val isConnected  = vpnState is VpnState.Connected
    val isConnecting = vpnState is VpnState.Connecting || vpnState is VpnState.Disconnecting
    val isError      = vpnState is VpnState.Error
    val isIdle       = vpnState is VpnState.Disconnected

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Press shrink animation
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow
        ),
        label = "button_press"
    )

    // State Colors
    val activeColor by animateColorAsState(
        targetValue = when {
            isConnected  -> NeonEmerald
            isError      -> Rose
            isConnecting -> Sapphire
            else         -> GlassBorder
        },
        animationSpec = tween(600),
        label = "active_color"
    )

    val activeGlowColor by animateColorAsState(
        targetValue = when {
            isConnected  -> NeonEmerald.copy(alpha = 0.2f)
            isError      -> Rose.copy(alpha = 0.2f)
            isConnecting -> Sapphire.copy(alpha = 0.2f)
            else         -> Color.Transparent
        },
        animationSpec = tween(600),
        label = "glow_color"
    )

    // Radar/Sonar pulser animations (connecting/connected states)
    val pulseTransition = rememberInfiniteTransition(label = "radar_pulser")
    
    val pulse1Progress by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse1"
    )

    val pulse2Progress by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = EaseOutQuad, delayMillis = 800),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse2"
    )

    val pulse3Progress by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = EaseOutQuad, delayMillis = 1600),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse3"
    )

    // Breathing glow animation when connected
    val breatheTransition = rememberInfiniteTransition(label = "breathe_pulse")
    val breatheGlowScale by breatheTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    // Spinning sweep arc during connection setup
    val spinTransition = rememberInfiniteTransition(label = "spin_arc")
    val spinAngle by spinTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(buttonScale),
        contentAlignment = Alignment.Center
    ) {
        // ── Sonar Pulse Waves (Background) ────────────────────────────────────
        if (isConnected || isConnecting) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val baseRadius = (size.toPx() / 2f)
                val activePulseColor = activeColor

                // Helper to draw a expanding fading ring
                val drawPulseRing = { progress: Float ->
                    if (progress > 0f) {
                        val radius = baseRadius * (1f + progress * 0.4f)
                        val alpha = (1f - progress) * 0.6f
                        drawCircle(
                            color = activePulseColor.copy(alpha = alpha),
                            radius = radius,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }
                }

                // Draw up to 3 concentric rings
                drawPulseRing(pulse1Progress)
                drawPulseRing(pulse2Progress)
                drawPulseRing(pulse3Progress)
            }
        }

        // ── Outer Breathing Glow Plate (Connected only) ───────────────────────
        if (isConnected) {
            Box(
                modifier = Modifier
                    .size(size * 0.88f)
                    .scale(breatheGlowScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(activeGlowColor, Color.Transparent),
                            radius = (size * 0.44f).value
                        )
                    )
            )
        }

        // ── Ring Canvas (Middle Layer) ────────────────────────────────────────
        Canvas(modifier = Modifier.size(size * 0.86f)) {
            val stroke = 3.dp.toPx()
            val r = (size.toPx() * 0.86f) / 2f - stroke / 2f

            when {
                isConnecting -> {
                    // Dim reference ring
                    drawCircle(
                        color = Sapphire.copy(alpha = 0.15f),
                        radius = r,
                        style = Stroke(width = stroke)
                    )
                    // High-speed spinning indicator
                    drawArc(
                        color = Sapphire,
                        startAngle = spinAngle,
                        sweepAngle = 100f,
                        useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(center.x - r, center.y - r),
                        size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
                isConnected -> {
                    // Double Ring structure for a premium visual signature
                    drawCircle(
                        color = NeonEmerald.copy(alpha = 0.2f),
                        radius = r + 4.dp.toPx(),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = NeonEmerald,
                        radius = r,
                        style = Stroke(width = stroke)
                    )
                }
                isError -> {
                    drawCircle(
                        color = Rose,
                        radius = r,
                        style = Stroke(width = stroke)
                    )
                }
                else -> {
                    // Minimal metallic style ring for disconnected
                    drawCircle(
                        color = GlassBorder,
                        radius = r,
                        style = Stroke(width = stroke)
                    )
                }
            }
        }

        // ── Tactile Button Core (Glass Plate) ─────────────────────────────────
        Box(
            modifier = Modifier
                .size(size * 0.68f)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SurfaceElevated, SurfaceGlass)
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            White.copy(alpha = 0.1f),
                            Color.Transparent,
                            activeColor.copy(alpha = 0.25f)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // Internal radial highlight
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                activeColor.copy(alpha = 0.06f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Inner glowing dot if connected
            if (isConnected) {
                Box(
                    modifier = Modifier
                        .size(size * 0.68f)
                        .clip(CircleShape)
                        .background(NeonEmerald.copy(alpha = 0.03f))
                )
            }

            // High-fidelity power icon that glows/scales
            val iconScale by animateFloatAsState(
                targetValue = if (isConnected) 1.08f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "icon_scale"
            )

            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = "Connect / Disconnect VPN",
                tint = if (isIdle) TextMuted else activeColor,
                modifier = Modifier
                    .size(size * 0.24f)
                    .scale(iconScale)
            )
        }
    }
}
