package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

enum class NewsAnimationTheme {
    GLOBE_SPIN,        // 3D Rotating Globe & Orbit
    BREAKING_BEAMS,    // Sweeping Red Broadcast Beams
    DIGITAL_STUDIO,    // Perspective Grid & Equalizer
    GOLDEN_LUXURY      // Golden Studio Spotlight & Bokeh
}

@Composable
fun AnimatedNewsBackground(
    theme: NewsAnimationTheme,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "newsBgAnim")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )

    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulsePhase"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wavePhase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF070B14),
                        Color(0xFF0F172A),
                        Color(0xFF050914)
                    )
                )
            )
    ) {
        when (theme) {
            NewsAnimationTheme.GLOBE_SPIN -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = (size.minDimension / 2.6f).coerceAtLeast(80f)

                    // Outer orbit ring
                    drawCircle(
                        color = Color(0x3300E5FF),
                        radius = radius * 1.35f,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Rotating Globe circle
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFF0D47A1), Color(0xFF0A192F), Color(0xFF030712)),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )

                    drawCircle(
                        color = Color(0xFF00E5FF),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Longitude lines (animated elliptical rotation)
                    val rad = Math.toRadians(rotationAngle.toDouble())
                    for (i in 0..4) {
                        val offsetDeg = i * 36.0
                        val currentRad = rad + Math.toRadians(offsetDeg)
                        val xFactor = cos(currentRad).toFloat()
                        drawOval(
                            color = Color(0x6600E5FF),
                            topLeft = Offset(center.x - radius * xFactor.coerceIn(-1f, 1f), center.y - radius),
                            size = Size(2 * radius * kotlin.math.abs(xFactor), 2 * radius),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    // Latitude lines
                    for (lat in listOf(-0.6f, -0.3f, 0f, 0.3f, 0.6f)) {
                        val latY = center.y + radius * lat
                        val latW = radius * kotlin.math.sqrt(1f - lat * lat)
                        drawOval(
                            color = Color(0x4400E5FF),
                            topLeft = Offset(center.x - latW, latY - 8.dp.toPx()),
                            size = Size(2 * latW, 16.dp.toPx()),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    // Orbiting Satellite Node
                    val satAngle = Math.toRadians((rotationAngle * 1.5).toDouble())
                    val satX = center.x + radius * 1.35f * cos(satAngle).toFloat()
                    val satY = center.y + radius * 0.7f * sin(satAngle).toFloat()
                    drawCircle(color = Color(0xFFFFD54F), radius = 6.dp.toPx(), center = Offset(satX, satY))
                    drawCircle(color = Color(0xFFFF5252), radius = 3.dp.toPx(), center = Offset(satX, satY))
                }
            }

            NewsAnimationTheme.BREAKING_BEAMS -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Sweeping light beams
                    val sweepProgress = pulsePhase
                    val beamWidth = size.width * 0.45f
                    val beamX = (sweepProgress * (size.width + beamWidth * 2)) - beamWidth

                    drawRect(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0x33FF1744),
                                Color(0x88FF1744),
                                Color(0x33FF1744),
                                Color.Transparent
                            ),
                            startX = beamX - beamWidth,
                            endX = beamX + beamWidth
                        ),
                        topLeft = Offset(0f, 0f),
                        size = size
                    )

                    // Dynamic diagonal hazard pattern at top and bottom
                    val barHeight = 16.dp.toPx()
                    var x = -barHeight
                    while (x < size.width + barHeight) {
                        drawLine(
                            color = Color(0x55FFD54F),
                            start = Offset(x + wavePhase * 20f, 0f),
                            end = Offset(x + 12f + wavePhase * 20f, barHeight),
                            strokeWidth = 3.dp.toPx()
                        )
                        drawLine(
                            color = Color(0x55FFD54F),
                            start = Offset(x - wavePhase * 20f, size.height - barHeight),
                            end = Offset(x + 12f - wavePhase * 20f, size.height),
                            strokeWidth = 3.dp.toPx()
                        )
                        x += 24.dp.toPx()
                    }
                }
            }

            NewsAnimationTheme.DIGITAL_STUDIO -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val horizonY = size.height * 0.55f

                    // Perspective floor lines
                    val numLines = 14
                    for (i in 0..numLines) {
                        val bottomX = (size.width / numLines) * i
                        drawLine(
                            color = Color(0x4400E5FF),
                            start = Offset(size.width / 2f, horizonY),
                            end = Offset(bottomX, size.height),
                            strokeWidth = 1.2.dp.toPx()
                        )
                    }

                    // Scrolling horizontal depth rings
                    for (d in 1..6) {
                        val depthOffset = ((d + pulsePhase) % 6f) / 6f
                        val y = horizonY + (size.height - horizonY) * (depthOffset * depthOffset)
                        drawLine(
                            color = Color(0x3300E5FF),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Studio Equalizer Bars
                    val numBars = 24
                    val barW = size.width / (numBars * 1.5f)
                    for (b in 0 until numBars) {
                        val hFactor = kotlin.math.abs(sin(wavePhase * 4f + b * 0.4f)).toFloat()
                        val barH = (35.dp.toPx() * hFactor) + 6.dp.toPx()
                        val barX = b * (barW * 1.5f) + 8.dp.toPx()
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFFFFD54F), Color(0xFFFF1744))
                            ),
                            topLeft = Offset(barX, horizonY - barH),
                            size = Size(barW, barH)
                        )
                    }
                }
            }

            NewsAnimationTheme.GOLDEN_LUXURY -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val sweepProgress = pulsePhase
                    val spotX = (sin(sweepProgress * Math.PI * 2) * size.width * 0.35f + size.width / 2f).toFloat()

                    // Golden Spotlight Cone
                    val path = Path().apply {
                        moveTo(size.width / 2f, 0f)
                        lineTo(spotX - 180.dp.toPx(), size.height)
                        lineTo(spotX + 180.dp.toPx(), size.height)
                        close()
                    }

                    drawPath(
                        path = path,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0x66FFD54F),
                                Color(0x22FFA000),
                                Color.Transparent
                            )
                        )
                    )

                    // Floating golden particles
                    for (p in 0..20) {
                        val px = (kotlin.math.abs(sin((p * 77 + rotationAngle).toDouble())) * size.width).toFloat()
                        val py = ((p * 45 + pulsePhase * size.height) % size.height).toFloat()
                        drawCircle(
                            color = Color(0xAAFFE082),
                            radius = (2 + (p % 4)).dp.toPx(),
                            center = Offset(px, py)
                        )
                    }
                }
            }
        }
    }
}
