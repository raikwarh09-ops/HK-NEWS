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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.unit.dp
import kotlin.random.Random

object VideoFilterPalettes {
    fun getColorMatrix(filterId: String): ColorMatrix? {
        return when (filterId) {
            "NOIR" -> ColorMatrix().apply {
                setToSaturation(0f)
            }
            "VINTAGE" -> ColorMatrix(
                floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f,     0f,     0f,     1f, 0f
                )
            )
            "NIGHT_VISION" -> ColorMatrix(
                floatArrayOf(
                    0.1f, 0.1f, 0.1f, 0f, 0f,
                    0.4f, 1.2f, 0.4f, 0f, 40f,
                    0.1f, 0.1f, 0.1f, 0f, 0f,
                    0f,   0f,   0f,   1f, 0f
                )
            )
            "VIVID" -> ColorMatrix().apply {
                setToSaturation(1.6f)
            }
            "BREAKING_ALERT" -> ColorMatrix(
                floatArrayOf(
                    1.4f, 0.1f, 0.1f, 0f, 15f,
                    0.1f, 1.0f, 0.1f, 0f, 0f,
                    0.1f, 0.1f, 0.9f, 0f, -10f,
                    0f,   0f,   0f,   1f, 0f
                )
            )
            "COOL_STUDIO" -> ColorMatrix(
                floatArrayOf(
                    0.9f, 0.0f, 0.1f, 0f, -10f,
                    0.0f, 1.0f, 0.1f, 0f, 0f,
                    0.1f, 0.2f, 1.3f, 0f, 25f,
                    0f,   0f,   0f,   1f, 0f
                )
            )
            "GOLDEN_HOUR" -> ColorMatrix(
                floatArrayOf(
                    1.2f, 0.2f, 0.0f, 0f, 20f,
                    0.1f, 1.1f, 0.0f, 0f, 10f,
                    0.0f, 0.0f, 0.8f, 0f, -20f,
                    0f,   0f,   0f,   1f, 0f
                )
            )
            "CYBER" -> ColorMatrix(
                floatArrayOf(
                    1.2f, 0.0f, 0.3f, 0f, 10f,
                    0.0f, 1.1f, 0.2f, 0f, 0f,
                    0.3f, 0.1f, 1.4f, 0f, 30f,
                    0f,   0f,   0f,   1f, 0f
                )
            )
            "DRAMATIC" -> ColorMatrix().apply {
                setToSaturation(0.4f)
            }
            "SAFFRON" -> ColorMatrix(
                floatArrayOf(
                    1.3f, 0.1f, 0.0f, 0f, 25f,
                    0.1f, 1.0f, 0.0f, 0f, 5f,
                    0.0f, 0.0f, 0.7f, 0f, -15f,
                    0f,   0f,   0f,   1f, 0f
                )
            )
            else -> null
        }
    }
}

@Composable
fun VideoEffectsRenderer(
    activeFilter: String,
    activeVfx: String,
    contrast: Float = 1.0f,
    brightness: Float = 0.0f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vfxTransition")

    // Animated Phase for Scanlines and Glitches
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "animProgress"
    )

    // Siren alternating flash
    val sirenFlash by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sirenFlash"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Video Contrast Adjustment Layer
        if (contrast > 1.05f) {
            val contrastFactor = ((contrast - 1.0f) * 0.45f).coerceIn(0f, 0.65f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = contrastFactor)
                        ),
                        center = center,
                        radius = size.maxDimension / 1.35f
                    )
                )
            }
        } else if (contrast < 0.95f) {
            val softFactor = ((1.0f - contrast) * 0.35f).coerceIn(0f, 0.45f)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray.copy(alpha = softFactor))
            )
        }

        // Video Brightness Adjustment Layer
        if (brightness > 0.02f) {
            val brightAlpha = (brightness * 0.5f).coerceIn(0f, 0.45f)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = brightAlpha))
            )
        } else if (brightness < -0.02f) {
            val darkAlpha = ((-brightness) * 0.6f).coerceIn(0f, 0.6f)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = darkAlpha))
            )
        }

        // Color Filter Tint Layer
        when (activeFilter) {
            "BREAKING_ALERT" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x33FF0000))
                )
            }
            "COOL_STUDIO" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x2200E5FF))
                )
            }
            "GOLDEN_HOUR" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x26FFA000))
                )
            }
            "NIGHT_VISION" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x3300FF00))
                )
            }
            "VINTAGE" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x2B795548))
                )
            }
            "SAFFRON" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x2EFF6D00))
                )
            }
        }

        // Visual VFX Layer
        when (activeVfx) {
            "SCANLINES" -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = 4.dp.toPx()
                    var y = 0f
                    while (y < size.height) {
                        drawLine(
                            color = Color(0x33000000),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                        y += step
                    }
                }
            }

            "VIGNETTE" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.Transparent, Color(0x99000000)),
                                radius = 600f
                            )
                        )
                )
            }

            "FLASH_STROBE" -> {
                val alpha = if ((animProgress * 10).toInt() % 2 == 0) 0.25f else 0.0f
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Red.copy(alpha = alpha))
                )
            }

            "EMERGENCY_SIREN" -> {
                val color = if (sirenFlash > 0.5f) Color(0x33FF1744) else Color(0x332979FF)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color)
                )
            }

            "GLITCH" -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val y1 = (animProgress * size.height) % size.height
                    val h1 = 12.dp.toPx()
                    drawRect(
                        color = Color(0x2200E5FF),
                        topLeft = Offset(0f, y1),
                        size = androidx.compose.ui.geometry.Size(size.width, h1)
                    )
                    drawRect(
                        color = Color(0x22FF0055),
                        topLeft = Offset(0f, (y1 + 18.dp.toPx()) % size.height),
                        size = androidx.compose.ui.geometry.Size(size.width, 6.dp.toPx())
                    )
                }
            }

            "RAIN_STORM" -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val random = Random(42)
                    for (i in 0..50) {
                        val startX = (random.nextFloat() * size.width + animProgress * 150f) % size.width
                        val startY = (random.nextFloat() * size.height + animProgress * 400f) % size.height
                        drawLine(
                            color = Color(0x44B0BEC5),
                            start = Offset(startX, startY),
                            end = Offset(startX - 6f, startY + 24f),
                            strokeWidth = 1.2.dp.toPx()
                        )
                    }
                }
            }
        }
    }
}
