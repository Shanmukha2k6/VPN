package com.securevpn.app.ui.components

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
import com.securevpn.app.data.model.VpnState
import com.securevpn.app.ui.theme.VpnGreen
import com.securevpn.app.ui.theme.VpnTeal

/**
 * A highly styled, tactile Power Button for the VPN connection.
 * Includes a rotating loading ring when connecting, and tactile scaling.
 */
@Composable
fun VpnPowerButton(
    vpnState: VpnState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp
) {
    val isConnected = vpnState is VpnState.Connected
    val isConnecting = vpnState is VpnState.Connecting || vpnState is VpnState.Disconnecting
    val isError = vpnState is VpnState.Error

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth press scale for tactile feel
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "pressScale"
    )

    // Rotating loading arc when connecting
    val rotateAnim = rememberInfiniteTransition(label = "rotate")
    val rotation by rotateAnim.animateFloat(
        initialValue = 0f,
        targetValue = if (isConnecting) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing)
        ),
        label = "rotation"
    )

    // Dynamic color transition based on state
    val stateColor by animateColorAsState(
        targetValue = when {
            isConnected -> VpnGreen
            isError -> Color(0xFFEF4444)
            isConnecting -> VpnTeal
            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        },
        animationSpec = tween(450),
        label = "stateColor"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(pressScale),
        contentAlignment = Alignment.Center
    ) {
        // Rotating loading ring (only visible when connecting/disconnecting)
        if (isConnecting) {
            Canvas(modifier = Modifier.size(size * 1.05f)) {
                val radius = (size.toPx() * 1.05f / 2) - 4.dp.toPx()
                // Dim background ring
                drawCircle(
                    color = stateColor.copy(alpha = 0.1f),
                    radius = radius,
                    style = Stroke(width = 3.dp.toPx())
                )
                // Spinning indicator sweep
                drawArc(
                    color = stateColor,
                    startAngle = rotation,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Inner glowing border or ring
        Canvas(modifier = Modifier.size(size)) {
            val radius = (size.toPx() / 2) - 6.dp.toPx()
            drawCircle(
                color = stateColor.copy(alpha = if (isConnected) 0.3f else 0.12f),
                radius = radius,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Tactile central button plate
        Box(
            modifier = Modifier
                .size(size * 0.78f)
                .shadow(
                    elevation = if (isConnected) 12.dp else 4.dp,
                    shape = CircleShape,
                    ambientColor = stateColor.copy(alpha = 0.2f),
                    spotColor = stateColor.copy(alpha = 0.4f)
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (isConnected) stateColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            // Power settings icon
            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = "VPN Connect Power Switch",
                modifier = Modifier.size(size * 0.32f),
                tint = stateColor
            )
        }
    }
}
