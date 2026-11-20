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
            "BW", "NOIR" -> ColorMatrix().apply {
                setToSaturation(0f)
            }
            "CINEMA" -> ColorMatrix(
                floatArrayOf(
                    0.98f, 0.1f, 0.0f, 0f, 8f,
                    0.05f, 1.05f, 0.08f, 0f, 4f,
                    0.08f, 0.12f, 1.2f, 0f, 10f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            "WARM" -> ColorMatrix(
                floatArrayOf(
                    1.18f, 0.12f, 0.0f, 0f, 16f,
                    0.05f, 1.06f, 0.0f, 0f, 6f,
                    0.0f, 0.0f, 0.86f, 0f, -12f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
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
            "CINE_TEAL" -> ColorMatrix(
                floatArrayOf(
                    0.95f, 0.1f, 0.0f, 0f, 10f,
                    0.05f, 1.05f, 0.1f, 0f, 5f,
                    0.1f, 0.15f, 1.25f, 0f, 15f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            "MOODY_NEWS" -> ColorMatrix(
                floatArrayOf(
                    0.85f, 0.0f, 0.05f, 0f, -15f,
                    0.0f, 0.85f, 0.05f, 0f, -15f,
                    0.05f, 0.05f, 0.95f, 0f, -5f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            "WARM_VLOG" -> ColorMatrix(
                floatArrayOf(
                    1.2f, 0.1f, 0.0f, 0f, 15f,
                    0.05f, 1.08f, 0.0f, 0f, 8f,
                    0.0f, 0.0f, 0.85f, 0f, -10f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            "FRESH_DAY" -> ColorMatrix(
                floatArrayOf(
                    1.05f, 0.05f, 0.0f, 0f, 12f,
                    0.0f, 1.15f, 0.05f, 0f, 14f,
                    0.0f, 0.05f, 1.1f, 0f, 10f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            else -> null
        }
    }

    fun buildAndroidColorMatrix(
        filterId: String,
        contrast: Float,
        brightness: Float,
        saturation: Float,
        warmth: Float,
        sharpen: Float = 0f
    ): android.graphics.ColorMatrix {
        val cm = android.graphics.ColorMatrix()
        getColorMatrix(filterId)?.let { composeCm ->
            cm.postConcat(android.graphics.ColorMatrix(composeCm.values))
        }
        if (kotlin.math.abs(saturation - 1.0f) > 0.02f) {
            val satCm = android.graphics.ColorMatrix().apply {
                setSaturation(saturation.coerceIn(0f, 2f))
            }
            cm.postConcat(satCm)
        }
        val crispContrast = (contrast + sharpen.coerceIn(0f, 1f) * 0.22f).coerceIn(0.5f, 2.5f)
        val b = brightness * 255f
        val t = (128f * (1f - crispContrast)) + b
        val rWarm = if (warmth > 0f) warmth * 45f else 0f
        val bCool = if (warmth < 0f) (-warmth) * 45f else 0f
        cm.postConcat(
            android.graphics.ColorMatrix(
                floatArrayOf(
                    crispContrast, 0f, 0f, 0f, t + rWarm,
                    0f, crispContrast, 0f, 0f, t,
                    0f, 0f, crispContrast, 0f, t + bCool,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        )
        return cm
    }
}

@Composable
fun VideoEffectsRenderer(
    activeFilter: String,
    activeVfx: String,
    activeAnimationFilter: String = "NONE",
    contrast: Float = 1.0f,
    brightness: Float = 0.0f,
    saturation: Float = 1.0f,
    warmth: Float = 0.0f,
    vignette: Float = 0.0f,
    vnMotionFx: String = "NONE",
    vnMaskType: String = "NONE",
    hasNativeColorMatrixApplied: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vfxTransition")

    // Animated Phase for Scanlines, Glitches & Animation Filters
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "animProgress"
    )

    // Siren / Pulse alternating phase
    val sirenFlash by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sirenFlash"
    )

    Box(modifier = modifier.fillMaxSize()) {
        if (!hasNativeColorMatrixApplied) {
            // Software approximation only when native ColorMatrix/RenderEffect is unavailable (API < 31 or Virtual Studio)
            if (contrast > 1.05f) {
                val contrastFactor = ((contrast - 1.0f) * 0.16f).coerceIn(0f, 0.24f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = contrastFactor * 0.25f))
                )
            } else if (contrast < 0.95f) {
                val softFactor = ((1.0f - contrast) * 0.25f).coerceIn(0f, 0.35f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray.copy(alpha = softFactor))
                )
            }

            if (brightness > 0.02f) {
                val brightAlpha = (brightness * 0.45f).coerceIn(0f, 0.4f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = brightAlpha))
                )
            } else if (brightness < -0.02f) {
                val darkAlpha = ((-brightness) * 0.55f).coerceIn(0f, 0.55f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = darkAlpha))
                )
            }

            if (saturation < 0.85f) {
                val desatAlpha = ((1f - saturation) * 0.36f).coerceIn(0f, 0.45f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF808080).copy(alpha = desatAlpha))
                )
            } else if (saturation > 1.15f) {
                val boostAlpha = ((saturation - 1f) * 0.14f).coerceIn(0f, 0.22f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFF6D00).copy(alpha = boostAlpha))
                )
            }

            if (warmth > 0.04f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFF9100).copy(alpha = (warmth * 0.38f).coerceIn(0f, 0.24f)))
                )
            } else if (warmth < -0.04f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF00B0FF).copy(alpha = ((-warmth) * 0.38f).coerceIn(0f, 0.24f)))
                )
            }

            when (activeFilter) {
                "BREAKING_ALERT" -> Box(modifier = Modifier.fillMaxSize().background(Color(0x36FF0000)))
                "COOL_STUDIO" -> Box(modifier = Modifier.fillMaxSize().background(Color(0x2800E5FF)))
                "GOLDEN_HOUR", "WARM" -> Box(modifier = Modifier.fillMaxSize().background(Color(0x2CFFA000)))
                "NIGHT_VISION" -> Box(modifier = Modifier.fillMaxSize().background(Color(0x3800FF44)))
                "VINTAGE" -> Box(modifier = Modifier.fillMaxSize().background(Color(0x32795548)))
                "SAFFRON" -> Box(modifier = Modifier.fillMaxSize().background(Color(0x30FF6D00)))
                "CINE_TEAL", "CINEMA" -> Box(modifier = Modifier.fillMaxSize().background(Color(0x2E00838F)))
                "MOODY_NEWS" -> Box(modifier = Modifier.fillMaxSize().background(Color(0x42090D16)))
                "WARM_VLOG" -> Box(modifier = Modifier.fillMaxSize().background(Color(0x28FFAB40)))
                "FRESH_DAY" -> Box(modifier = Modifier.fillMaxSize().background(Color(0x2269F0AE)))
                "NOIR", "BW" -> Box(modifier = Modifier.fillMaxSize().background(Color(0x55222222)))
                "CYBER" -> Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.linearGradient(listOf(Color(0x2600E5FF), Color(0x26D500F9)))
                    )
                )
                "VIVID" -> Box(modifier = Modifier.fillMaxSize().background(Color(0x18FFEA00)))
            }
        }

        // ================= ANIMATION FILTERS LAYER =================
        when (activeAnimationFilter) {
            "NEON_PULSE_WAVE" -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val waveX = -size.width * 0.3f + (size.width * 1.6f * animProgress)
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.Transparent, Color(0x4400E5FF), Color(0x44FF1744), Color.Transparent),
                            start = Offset(waveX - 100f, 0f),
                            end = Offset(waveX + 120f, size.height)
                        )
                    )
                    drawRect(
                        color = Color(0xFF00E5FF).copy(alpha = 0.25f + sirenFlash * 0.45f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                    )
                }
            }

            "GOLDEN_SPARKLE" -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val rng = Random(108)
                    for (i in 0..28) {
                        val baseX = rng.nextFloat() * size.width
                        val baseY = rng.nextFloat() * size.height
                        val driftY = (baseY - animProgress * size.height * 0.6f + size.height) % size.height
                        val r = (2.5f + (i % 4) * 1.4f).dp.toPx()
                        val alpha = 0.35f + 0.5f * kotlin.math.abs(kotlin.math.sin((animProgress + i * 0.13f) * Math.PI)).toFloat()
                        drawCircle(
                            color = Color(0xFFFFD54F).copy(alpha = alpha),
                            radius = r,
                            center = Offset(baseX, driftY)
                        )
                    }
                }
            }

            "LIGHT_LEAK_SWEEP" -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val leakX = -size.width * 0.4f + size.width * 1.8f * animProgress
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x66FF6D00), Color(0x33FF1744), Color.Transparent),
                            center = Offset(leakX, size.height * 0.25f),
                            radius = size.maxDimension * 0.55f
                        ),
                        center = Offset(leakX, size.height * 0.25f),
                        radius = size.maxDimension * 0.55f
                    )
                }
            }

            "VHS_RETRO_WAVE" -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barY = (animProgress * size.height) % size.height
                    drawRect(
                        color = Color(0x33FFFFFF),
                        topLeft = Offset(0f, barY),
                        size = androidx.compose.ui.geometry.Size(size.width, 14.dp.toPx())
                    )
                    drawRect(
                        color = Color(0x22FF00AA),
                        topLeft = Offset(0f, (barY + 24.dp.toPx()) % size.height),
                        size = androidx.compose.ui.geometry.Size(size.width, 5.dp.toPx())
                    )
                    var y = 0f
                    val step = 6.dp.toPx()
                    while (y < size.height) {
                        drawLine(Color(0x1E000000), Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                        y += step
                    }
                }
            }

            "PRISM_AURORA" -> {
                val shift = animProgress
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0x2800E5FF).copy(alpha = 0.18f + shift * 0.14f),
                                    Color(0x28AA00FF).copy(alpha = 0.22f),
                                    Color(0x28FF4081).copy(alpha = 0.18f + (1f - shift) * 0.14f),
                                    Color(0x28FFD54F).copy(alpha = 0.18f)
                                )
                            )
                        )
                )
            }

            "CYBER_MATRIX" -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cols = 14
                    val colW = size.width / cols
                    for (c in 0 until cols) {
                        val x = c * colW + colW * 0.5f
                        val dropY = ((animProgress * 1.4f + (c * 0.19f)) % 1f) * size.height
                        drawLine(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0x6600E676), Color(0xAA00E5FF)),
                                startY = (dropY - 70f).coerceAtLeast(0f),
                                endY = dropY
                            ),
                            start = Offset(x, (dropY - 70f).coerceAtLeast(0f)),
                            end = Offset(x, dropY),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
            }

            "FIRE_EMBERS" -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val rng = Random(77)
                    for (i in 0..30) {
                        val ex = (rng.nextFloat() * size.width + kotlin.math.sin((animProgress * 6.28 + i)).toFloat() * 14f)
                            .coerceIn(0f, size.width)
                        val ey = (size.height - ((rng.nextFloat() + animProgress * 1.3f) % 1f) * size.height)
                        val er = (2f + (i % 3) * 1.2f).dp.toPx()
                        drawCircle(
                            color = if (i % 2 == 0) Color(0xCCFF6D00) else Color(0xCCFFD54F),
                            radius = er,
                            center = Offset(ex, ey)
                        )
                    }
                }
            }

            "SNOW_FALL" -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val rng = Random(303)
                    for (i in 0..34) {
                        val sx = (rng.nextFloat() * size.width + animProgress * 30f * (if (i % 2 == 0) 1f else -1f) + size.width) % size.width
                        val sy = ((rng.nextFloat() + animProgress) % 1f) * size.height
                        drawCircle(
                            color = Color(0xAAFFFFFF),
                            radius = (1.8f + (i % 3) * 0.9f).dp.toPx(),
                            center = Offset(sx, sy)
                        )
                    }
                }
            }
        }

        // VN Vignette Slider Overlay
        if (vignette > 0.03f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = (vignette * 0.85f).coerceIn(0f, 0.85f))),
                        center = center,
                        radius = size.maxDimension / 1.45f
                    )
                )
            }
        }

        // VN Mask Shape Overlays
        when (vnMaskType) {
            "LINEAR" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xAA000000), Color.Transparent, Color.Transparent, Color(0xAA000000))
                            )
                        )
                )
            }
            "MIRROR" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black, Color.Transparent, Color.Transparent, Color.Black)
                            )
                        )
                )
            }
            "RADIAL" -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.Transparent, Color.Transparent, Color(0xCC000000)),
                            center = center,
                            radius = size.minDimension * 0.6f
                        )
                    )
                }
            }
            "RECTANGLE" -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barH = size.height * 0.12f
                    drawRect(Color.Black, topLeft = Offset.Zero, size = androidx.compose.ui.geometry.Size(size.width, barH))
                    drawRect(Color.Black, topLeft = Offset(0f, size.height - barH), size = androidx.compose.ui.geometry.Size(size.width, barH))
                }
            }
        }

        // VN Motion FX Flash Cut
        if (vnMotionFx == "FLASH_CUT") {
            val flashAlpha = if ((animProgress * 8).toInt() % 4 == 0) 0.35f else 0f
            if (flashAlpha > 0f) {
                Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = flashAlpha)))
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
