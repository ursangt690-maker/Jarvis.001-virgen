package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.AssistantStatus
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisElectricBlue
import com.example.ui.theme.JarvisHoloViolet
import com.example.ui.theme.JarvisLaserRed
import com.example.ui.theme.JarvisNeonMint
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JarvisOrb(
    status: AssistantStatus,
    rmsLevel: Float = 0f,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_orb_transition")

    // Slow continuous outer ring rotation
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rotation"
    )

    // Inner counter-rotation (accelerates during THINKING)
    val innerDuration = if (status == AssistantStatus.THINKING) 2500 else 9000
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = innerDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rotation"
    )

    // Core pulsing scale
    val pulseDuration = when (status) {
        AssistantStatus.LISTENING -> 800
        AssistantStatus.THINKING -> 600
        AssistantStatus.SPEAKING -> 900
        AssistantStatus.ERROR -> 500
        AssistantStatus.IDLE -> 3200
    }

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = pulseDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Dynamic color transition based on status
    val primaryColor by animateColorAsState(
        targetValue = when (status) {
            AssistantStatus.IDLE -> JarvisCyan
            AssistantStatus.LISTENING -> JarvisNeonMint
            AssistantStatus.THINKING -> JarvisElectricBlue
            AssistantStatus.SPEAKING -> JarvisHoloViolet
            AssistantStatus.ERROR -> JarvisLaserRed
        },
        animationSpec = tween(400),
        label = "orb_primary_color"
    )

    val secondaryColor by animateColorAsState(
        targetValue = when (status) {
            AssistantStatus.IDLE -> JarvisElectricBlue
            AssistantStatus.LISTENING -> JarvisCyan
            AssistantStatus.THINKING -> JarvisHoloViolet
            AssistantStatus.SPEAKING -> JarvisCyan
            AssistantStatus.ERROR -> Color(0xFF880825)
        },
        animationSpec = tween(400),
        label = "orb_secondary_color"
    )

    Box(
        modifier = modifier
            .size(size)
            .testTag("jarvis_central_orb")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = size / 2),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 2f

            // 1. Ambient Glow Field
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = if (status == AssistantStatus.LISTENING) 0.35f else 0.22f),
                        secondaryColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 0.98f
                ),
                radius = baseRadius * 0.95f,
                center = center
            )

            // 2. Outer Holographic Ring with Dashes
            rotate(outerRotation, pivot = center) {
                drawCircle(
                    color = primaryColor.copy(alpha = 0.45f),
                    radius = baseRadius * 0.90f,
                    center = center,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 16f, 8f, 16f), 0f)
                    )
                )

                // Outer telemetry tick marks
                val ticks = 12
                for (i in 0 until ticks) {
                    val angle = (i * 360f / ticks) * (PI / 180f).toFloat()
                    val r1 = baseRadius * 0.88f
                    val r2 = baseRadius * 0.94f
                    val p1 = Offset(center.x + r1 * cos(angle), center.y + r1 * sin(angle))
                    val p2 = Offset(center.x + r2 * cos(angle), center.y + r2 * sin(angle))
                    drawLine(
                        color = secondaryColor.copy(alpha = 0.5f),
                        start = p1,
                        end = p2,
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
            }

            // 3. Middle Counter-Rotating Ring with Segmented Arcs
            rotate(innerRotation, pivot = center) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(primaryColor, secondaryColor, Color.Transparent, primaryColor)
                    ),
                    startAngle = 10f,
                    sweepAngle = 100f,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(secondaryColor, primaryColor, Color.Transparent, secondaryColor)
                    ),
                    startAngle = 190f,
                    sweepAngle = 100f,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 4. Reactive Listening / Speaking Audio Waveforms
            if (status == AssistantStatus.LISTENING || status == AssistantStatus.SPEAKING) {
                val waveBoost = (rmsLevel * 2f).coerceIn(0f, 20f)
                val waveRadius = baseRadius * 0.76f + waveBoost
                drawCircle(
                    color = primaryColor.copy(alpha = 0.5f),
                    radius = waveRadius,
                    center = center,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                    )
                )
            }

            // 5. High-Energy Luminous Core Sphere
            val effectiveCoreRadius = baseRadius * 0.44f * pulseScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        primaryColor,
                        secondaryColor,
                        primaryColor.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = effectiveCoreRadius
                ),
                radius = effectiveCoreRadius,
                center = center
            )

            // 6. Center Nuclear Reticle
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = 5.dp.toPx(),
                center = center
            )
        }
    }
}
