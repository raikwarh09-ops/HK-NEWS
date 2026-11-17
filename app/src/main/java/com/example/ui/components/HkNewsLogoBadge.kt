package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.NewsAmber
import com.example.ui.theme.NewsGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

val LOGO_ANIMATION_FILTERS = listOf(
    "SHINE_SWEEP" to "✨ शाइन स्वीप (Gloss)",
    "SPIN_3D" to "🔄 3D टीवी रोटेशन",
    "PULSE_GLOW" to "💓 पल्स व ग्लो",
    "NEON_BORDER" to "🌈 नियॉन RGB लाइट",
    "FLOAT_BOB" to "🌊 फ्लोटिंग वेव",
    "HOLOGRAM_GLITCH" to "⚡ होलोग्राम ग्लिच",
    "BOUNCE_POP" to "💥 बाउंस व पॉप",
    "GOLDEN_FLARE" to "🔥 गोल्डन फ्लेयर",
    "RADIAL_RIPPLE" to "🌟 रेडियल औरा",
    "TV_SCANLINE" to "📺 रेट्रो टीवी स्कैन",
    "SPARKLE_BURST" to "✨ स्पार्कल बर्स्ट",
    "CHROME_BEVEL" to "💎 3D क्रोम शाइन",
    "NONE" to "स्थिर (No Animation)"
)

@Composable
fun ChannelLogoOverlay(
    logoType: String,
    customLogoUri: String?,
    scale: Float,
    opacity: Float,
    logoAnimation: String = "SHINE_SWEEP",
    channelName: String = "हर खबर H.K NEWS",
    location: String = "नरसिंहपुर",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .scale(scale.coerceIn(0.6f, 1.8f))
            .alpha(opacity.coerceIn(0.2f, 1.0f))
    ) {
        LogoAnimatedEffectContainer(animation = logoAnimation) {
            when (logoType) {
                "CUSTOM_IMAGE" -> {
                    if (!customLogoUri.isNullOrBlank()) {
                        AsyncImage(
                            model = customLogoUri,
                            contentDescription = "कस्टम चैनल लोगो",
                            modifier = Modifier
                                .size(64.dp)
                                .shadow(6.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        HkNewsBrandedBadge(channelName = channelName, location = location)
                    }
                }

                "HK_NEWS" -> {
                    HkNewsBrandedBadge(channelName = channelName, location = location)
                }

                "TEXT_BADGE" -> {
                    TextLogoBadge(channelName = channelName)
                }

                else -> {
                    HkNewsBrandedBadge(channelName = channelName, location = location)
                }
            }
        }
    }
}

/**
 * Animated Container applying unlimited professional TV broadcast animation filters
 */
@Composable
fun LogoAnimatedEffectContainer(
    animation: String,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logoAnimation")

    when (animation) {
        "SPIN_3D" -> {
            val rotationY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "spin3d"
            )
            Box(
                modifier = Modifier.graphicsLayer {
                    this.rotationY = rotationY
                    cameraDistance = 14f * density
                }
            ) {
                content()
            }
        }

        "PULSE_GLOW" -> {
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 0.98f,
                targetValue = 1.10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseScale"
            )
            val glowAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.85f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glowAlpha"
            )
            Box(
                modifier = Modifier
                    .scale(pulseScale)
                    .shadow(12.dp, RoundedCornerShape(8.dp), spotColor = NewsGold.copy(alpha = glowAlpha))
            ) {
                content()
            }
        }

        "NEON_BORDER" -> {
            val borderRotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "neonRotation"
            )
            val neonBrush = Brush.sweepGradient(
                listOf(
                    Color(0xFF00E5FF),
                    Color(0xFFFFD54F),
                    Color(0xFFFF1744),
                    Color(0xFF00E676),
                    Color(0xFF00E5FF)
                )
            )
            Box(
                modifier = Modifier
                    .graphicsLayer { this.rotationZ = 0f }
                    .border(2.dp, neonBrush, RoundedCornerShape(8.dp))
                    .padding(2.dp)
            ) {
                content()
            }
        }

        "FLOAT_BOB" -> {
            val floatY by infiniteTransition.animateFloat(
                initialValue = -5f,
                targetValue = 5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "floatY"
            )
            val tilt by infiniteTransition.animateFloat(
                initialValue = -2.5f,
                targetValue = 2.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "tilt"
            )
            Box(
                modifier = Modifier
                    .offset { IntOffset(0, floatY.roundToInt()) }
                    .graphicsLayer { rotationZ = tilt }
            ) {
                content()
            }
        }

        "HOLOGRAM_GLITCH" -> {
            val glitchOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(150, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glitchOffset"
            )
            val hologramAlpha by infiniteTransition.animateFloat(
                initialValue = 0.85f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(220, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "hologramAlpha"
            )
            Box(
                modifier = Modifier
                    .offset { IntOffset(glitchOffset.roundToInt(), 0) }
                    .alpha(hologramAlpha)
            ) {
                content()
                // Holographic cyan line overlay
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0x1A00E5FF))
                )
            }
        }

        "BOUNCE_POP" -> {
            val bounceY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bounce"
            )
            Box(
                modifier = Modifier.offset { IntOffset(0, bounceY.roundToInt()) }
            ) {
                content()
            }
        }

        "GOLDEN_FLARE" -> {
            val flareOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "flare"
            )
            Box {
                content()
                // Sparkling flare across diagonal
                Canvas(modifier = Modifier.matchParentSize()) {
                    val flareX = size.width * flareOffset
                    val flareY = size.height * flareOffset
                    drawCircle(
                        color = NewsGold.copy(alpha = 0.6f),
                        radius = 9.dp.toPx(),
                        center = Offset(flareX, flareY)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f),
                        radius = 3.dp.toPx(),
                        center = Offset(flareX, flareY)
                    )
                }
            }
        }

        "RADIAL_RIPPLE" -> {
            val rippleRadius by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1.35f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "ripple"
            )
            val rippleAlpha by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 0.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rippleAlpha"
            )
            Box {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        color = NewsGold.copy(alpha = rippleAlpha),
                        radius = (size.minDimension / 1.5f) * rippleRadius,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                content()
            }
        }

        "TV_SCANLINE" -> {
            val scanlineY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "scanline"
            )
            Box {
                content()
                Canvas(modifier = Modifier.matchParentSize()) {
                    val y = size.height * scanlineY
                    drawLine(
                        color = Color.White.copy(alpha = 0.4f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }

        "SPARKLE_BURST" -> {
            val sparklePhase by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1600, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "sparkle"
            )
            Box {
                content()
                Canvas(modifier = Modifier.matchParentSize()) {
                    val p1 = Offset(size.width * 0.15f, size.height * 0.2f)
                    val p2 = Offset(size.width * 0.85f, size.height * 0.3f)
                    val p3 = Offset(size.width * 0.8f, size.height * 0.8f)

                    val alpha1 = ((sin(sparklePhase * 2 * PI).toFloat() + 1f) / 2f).coerceIn(0.1f, 1f)
                    val alpha2 = ((cos(sparklePhase * 2 * PI).toFloat() + 1f) / 2f).coerceIn(0.1f, 1f)

                    drawCircle(color = Color.White.copy(alpha = alpha1), radius = 3.dp.toPx(), center = p1)
                    drawCircle(color = NewsGold.copy(alpha = alpha2), radius = 3.5.dp.toPx(), center = p2)
                    drawCircle(color = Color.Cyan.copy(alpha = alpha1), radius = 2.5.dp.toPx(), center = p3)
                }
            }
        }

        "CHROME_BEVEL" -> {
            val chromeShift by infiniteTransition.animateFloat(
                initialValue = -1f,
                targetValue = 2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2800, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "chrome"
            )
            val chromeBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFB0BEC5),
                    Color(0xFFECEFF1),
                    Color(0xFFFFD54F),
                    Color(0xFFB0BEC5)
                ),
                start = Offset(0f, 0f),
                end = Offset(150f * chromeShift, 150f * chromeShift)
            )
            Box(
                modifier = Modifier
                    .border(1.5.dp, chromeBrush, RoundedCornerShape(8.dp))
                    .padding(1.dp)
            ) {
                content()
            }
        }

        "SHINE_SWEEP" -> {
            // Default Gloss Sweep
            val shineOffset by infiniteTransition.animateFloat(
                initialValue = -100f,
                targetValue = 250f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2400, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "shine"
            )
            Box {
                content()
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    val shineWidth = 40.dp.toPx()
                    val x = shineOffset.dp.toPx()
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.55f),
                                Color(0xFFFFD54F).copy(alpha = 0.45f),
                                Color.Transparent
                            ),
                            start = Offset(x, 0f),
                            end = Offset(x + shineWidth, size.height)
                        ),
                        size = size
                    )
                }
            }
        }

        else -> {
            Box {
                content()
            }
        }
    }
}

/**
 * Authentic replica of the user's uploaded "हर खबर - नरसिंहपुर समाचार - H.K NEWS" logo
 */
@Composable
fun HkNewsBrandedBadge(
    channelName: String = "हर खबर",
    location: String = "नरसिंहपुर समाचार",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(6.dp))
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFFFFD54F), Color(0xFFFF9100), Color(0xFF00E5FF))
                ),
                shape = RoundedCornerShape(6.dp)
            )
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF0D47A1))
    ) {
        Image(
            painter = painterResource(id = R.drawable.hk_news_logo),
            contentDescription = "हर खबर H.K NEWS नरसिंहपुर समाचार",
            modifier = Modifier.size(width = 66.dp, height = 66.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun TextLogoBadge(
    channelName: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xCC000000), RoundedCornerShape(4.dp))
            .border(1.dp, NewsGold, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = channelName,
            color = NewsGold,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}
