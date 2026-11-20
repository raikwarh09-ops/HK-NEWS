package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.NewsProject
import com.example.ui.theme.NewsAmber
import com.example.ui.theme.NewsGold
import com.example.ui.theme.NewsRedDark
import com.example.ui.theme.NewsRedPrimary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.roundToInt

import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.runtime.rememberUpdatedState

@Composable
fun NewsOverlayFrame(
    project: NewsProject,
    modifier: Modifier = Modifier,
    isBreakingFlashActive: Boolean = false,
    showTeleprompter: Boolean = false,
    teleprompterSpeed: Float = 1.0f,
    teleprompterFontSize: Float = 18f,
    timelineProgress: Float = 0f,
    splitPointRatio: Float? = null,
    hasNativeColorMatrixApplied: Boolean = false,
    onDragElementDelta: ((element: String, dx: Float, dy: Float) -> Unit)? = null,
    onUpdateElementOffset: ((element: String, offsetX: Float, offsetY: Float) -> Unit)? = null,
    backgroundContent: @Composable () -> Unit
) {
    val latestProject by rememberUpdatedState(project)
    val latestOnDragDelta by rememberUpdatedState(onDragElementDelta)
    val latestOnUpdateOffset by rememberUpdatedState(onUpdateElementOffset)
    fun dispatchDrag(element: String, dx: Float, dy: Float) {
        val deltaCb = latestOnDragDelta
        if (deltaCb != null) {
            deltaCb(element, dx, dy)
        } else {
            val p = latestProject
            val cur = when (element) {
                "LOGO" -> p.logoOffsetX to p.logoOffsetY
                "HEADLINE" -> p.headlineOffsetX to p.headlineOffsetY
                "TICKER" -> p.tickerOffsetX to p.tickerOffsetY
                "REPORTER" -> p.reporterOffsetX to p.reporterOffsetY
                "LIVE_BADGE" -> p.liveBadgeOffsetX to p.liveBadgeOffsetY
                "LOCATION" -> p.locationOffsetX to p.locationOffsetY
                "CLOCK" -> p.clockOffsetX to p.clockOffsetY
                "STICKERS" -> p.stickersOffsetX to p.stickersOffsetY
                else -> 0f to 0f
            }
            latestOnUpdateOffset?.invoke(element, cur.first + dx, cur.second + dy)
        }
    }
    // Current running time
    var currentTimeString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val sdf = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
            currentTimeString = sdf.format(Date())
            delay(1000)
        }
    }

    // Live pulsing indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Parse primary theme color
    val primaryColor = remember(project.themeColorHex) {
        try {
            Color(android.graphics.Color.parseColor(project.themeColorHex))
        } catch (_: Exception) {
            NewsRedPrimary
        }
    }

    // Calculate aspect ratio
    val aspectMultiplier = when (project.aspectPreset) {
        "9:16" -> 9f / 16f
        "1:1" -> 1f
        "4:5" -> 4f / 5f
        else -> 16f / 9f
    }

    // Live Pan & Zoom scale interpolated across timelineProgress
    val livePanZoomScale = if (project.panZoomLocked) {
        project.panZoomStartScale
    } else {
        project.panZoomStartScale + (project.panZoomEndScale - project.panZoomStartScale) * timelineProgress.coerceIn(0f, 1f)
    }.coerceIn(1.0f, 2.5f)

    // VN Motion FX continuous phase
    val vnMotionPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vnMotion"
    )

    val vnFxScale = when (project.vnMotionFx) {
        "ZOOM_PULSE" -> 1f + (vnMotionPhase * 0.08f)
        "EXPANSION" -> 1f + (vnMotionPhase * 0.14f)
        "JITTER" -> 1.03f + (if (vnMotionPhase > 0.5f) 0.04f else 0f)
        else -> 1f
    }
    val vnFxTx = if (project.vnMotionFx == "SHAKE") (vnMotionPhase - 0.5f) * 14f else 0f
    val vnFxTy = if (project.vnMotionFx == "SHAKE") ((vnMotionPhase * 2f) % 1f - 0.5f) * 10f else 0f

    val vnBgModifier = when (project.vnBackgroundStyle) {
        "NAVY_STUDIO" -> Modifier.background(Color(0xFF0A192F))
        "CRIMSON_NEWS" -> Modifier.background(Color(0xFF3E0B12))
        "GOLD_ROYAL" -> Modifier.background(Color(0xFF3B2F08))
        "GRADIENT_DARK" -> Modifier.background(
            Brush.verticalGradient(listOf(Color(0xFF0D1B2A), Color(0xFF1B263B), Color(0xFF000000)))
        )
        else -> Modifier.background(Color.Black)
    }
    val vnBorderColor = remember(project.vnBorderColorHex) {
        try {
            Color(android.graphics.Color.parseColor(project.vnBorderColorHex))
        } catch (_: Exception) {
            NewsGold
        }
    }

    // Clip In-Animation, Out-Animation & Split/Clip Transition computation
    val normProg = timelineProgress.coerceIn(0f, 1f)
    val inT = if (normProg < 0.14f) (normProg / 0.14f).coerceIn(0f, 1f) else 1f
    val outT = if (normProg > 0.86f) ((1f - normProg) / 0.14f).coerceIn(0f, 1f) else 1f
    val transCenter = splitPointRatio ?: 0.06f
    val distToTrans = kotlin.math.abs(normProg - transCenter)
    val transProg = if (project.activeTransition != "NONE" && distToTrans < 0.07f) {
        (1f - (distToTrans / 0.07f)).coerceIn(0f, 1f)
    } else 0f

    Box(
        modifier = modifier
            .aspectRatio(aspectMultiplier, matchHeightConstraintsFirst = false)
            .clip(RoundedCornerShape(8.dp))
            .then(vnBgModifier)
            .then(
                if (project.vnBorderWidthDp > 0.5f) {
                    Modifier.border(project.vnBorderWidthDp.dp, vnBorderColor, RoundedCornerShape(8.dp))
                } else Modifier
            )
            .testTag("news_overlay_frame"),
        contentAlignment = Alignment.Center
    ) {
        // ================= BACKGROUND & LAYOUT MODE (FULL, SPLIT, PIP) WITH CROP, ANIMATIONS & VN TRANSFORMS =================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (project.vnBorderWidthDp > 0.5f) Modifier.padding(project.vnBorderWidthDp.dp) else Modifier
                )
                .clipToBounds()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val panX = project.videoCropPanX.coerceIn(-1f, 1f)
                        val panY = project.videoCropPanY.coerceIn(-1f, 1f)
                        val cropZ = maxOf(
                            project.videoCropZoom.coerceIn(1.0f, 3.0f),
                            1f + maxOf(kotlin.math.abs(panX), kotlin.math.abs(panY)) * 0.45f
                        )
                        val inScale = when (project.inAnimation) {
                            "POP_UP" -> 0.55f + 0.45f * inT
                            else -> 1f
                        }
                        val transScale = when (project.activeTransition) {
                            "ZOOM_BURST" -> 1f + transProg * 0.35f
                            "CUBE_ROTATE" -> (1f - transProg * 0.25f)
                            "3D_NEWS_FLIP" -> (1f - transProg * 0.15f)
                            else -> 1f
                        }
                        val baseSx = livePanZoomScale * vnFxScale * cropZ * inScale * transScale
                        val baseSy = livePanZoomScale * vnFxScale * cropZ * inScale * transScale
                        scaleX = if (project.vnMirrorHorizontal) -baseSx else baseSx
                        scaleY = if (project.vnFlipVertical) -baseSy else baseSy
                        val inRot = if (project.inAnimation == "SPIN_CLOCKWISE") (1f - inT) * -360f else 0f
                        rotationZ = project.vnRotationDegrees + inRot
                        if (project.activeTransition == "3D_NEWS_FLIP") {
                            rotationY = transProg * 90f
                            cameraDistance = 12f * density
                        } else if (project.activeTransition == "CUBE_ROTATE") {
                            rotationY = transProg * 55f
                            cameraDistance = 10f * density
                        }
                        val inTx = if (project.inAnimation == "SLIDE_RIGHT") -(1f - inT) * size.width else 0f
                        val inTy = if (project.inAnimation == "SLIDE_UP") (1f - inT) * size.height else 0f
                        val outTx = if (project.outAnimation == "SLIDE_LEFT") -(1f - outT) * size.width else 0f
                        val outTy = if (project.outAnimation == "SLIDE_DOWN") (1f - outT) * size.height else 0f
                        val maxShiftX = ((cropZ - 1f) * 0.5f) * size.width
                        val maxShiftY = ((cropZ - 1f) * 0.5f) * size.height
                        translationX = vnFxTx + (panX * maxShiftX) + inTx + outTx +
                            if (project.activeTransition == "WIPE_RIGHT") transProg * size.width * 0.25f else 0f
                        translationY = vnFxTy + (panY * maxShiftY) + inTy + outTy
                        val inAlpha = if (project.inAnimation == "FADE_IN") inT else 1f
                        val outAlpha = if (project.outAnimation == "FADE_OUT") outT else 1f
                        val transAlpha = if (project.activeTransition == "CROSSFADE") (1f - transProg * 0.65f) else 1f
                        alpha = (project.vnVideoOpacity.coerceIn(0.2f, 1.0f) * inAlpha * outAlpha * transAlpha).coerceIn(0f, 1f)
                    }
            ) {
                when (project.layoutMode) {
                    "SPLIT_SCREEN" -> {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                backgroundContent()
                            }
                            Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(NewsGold))
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF1E293B))) {
                                if (!project.secondaryMediaUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = project.secondaryMediaUri,
                                        contentDescription = "ग्राउंड फुटेज",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("ग्राउंड रिपोर्ट फुटेज", color = Color.Gray, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        // Fullscreen background
                        backgroundContent()

                        // Picture In Picture (PIP) floating box
                        if (project.layoutMode == "PIP_CORNER") {
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(width = 110.dp, height = 75.dp)
                                    .align(Alignment.CenterEnd)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.5.dp, NewsGold, RoundedCornerShape(6.dp))
                            ) {
                                if (!project.secondaryMediaUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = project.secondaryMediaUri,
                                        contentDescription = "PIP मीडिया",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("PIP विंडो", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .background(NewsRedPrimary, RoundedCornerShape(bottomEnd = 4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                        .align(Alignment.TopStart)
                                ) {
                                    Text("लाइव", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Video Edge Crop Bars (from manual Left/Top/Right/Bottom sliders + 21:9 Cinema Preset)
            val cLeft = project.videoCropLeft.coerceIn(0f, 0.4f)
            val cRight = project.videoCropRight.coerceIn(0f, 0.4f)
            val cTop = (project.videoCropTop + if (project.videoCropPreset == "21:9") 0.12f else 0f).coerceIn(0f, 0.42f)
            val cBottom = (project.videoCropBottom + if (project.videoCropPreset == "21:9") 0.12f else 0f).coerceIn(0f, 0.42f)
            val pSide = when (project.videoCropPreset) {
                "1:1" -> 0.15f
                "4:5" -> 0.18f
                "9:16" -> 0.24f
                "4:3" -> 0.08f
                else -> 0f
            }
            val effLeft = (cLeft + pSide).coerceIn(0f, 0.42f)
            val effRight = (cRight + pSide).coerceIn(0f, 0.42f)

            if (effLeft > 0.005f || effRight > 0.005f || cTop > 0.005f || cBottom > 0.005f) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (effLeft > 0.005f) {
                        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(effLeft).align(Alignment.CenterStart).background(Color.Black))
                    }
                    if (effRight > 0.005f) {
                        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(effRight).align(Alignment.CenterEnd).background(Color.Black))
                    }
                    if (cTop > 0.005f) {
                        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(cTop).align(Alignment.TopCenter).background(Color.Black))
                    }
                    if (cBottom > 0.005f) {
                        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(cBottom).align(Alignment.BottomCenter).background(Color.Black))
                    }
                }
            }
        }

        // ================= COLOR FILTERS, ANIMATION FILTERS, VN ADJUSTMENTS & VFX OVERLAY =================
        VideoEffectsRenderer(
            activeFilter = project.activeFilter,
            activeVfx = project.activeVfx,
            activeAnimationFilter = project.activeAnimationFilter,
            contrast = project.videoContrast,
            brightness = project.videoBrightness,
            saturation = project.vnSaturation,
            warmth = project.vnWarmth,
            vignette = project.vnVignette,
            vnMotionFx = project.vnMotionFx,
            vnMaskType = project.vnMaskType,
            hasNativeColorMatrixApplied = hasNativeColorMatrixApplied,
            modifier = Modifier.fillMaxSize()
        )

        // Chroma Key Live Preview Studio Frame Halo + Wipe/Glitch Transition Feedback
        if (project.chromaKeyEnabled) {
            val keyBorder = when (project.chromaKeyColor.uppercase()) {
                "#0000FF" -> Color(0xFF2979FF)
                "#FFFFFF" -> Color.White
                else -> Color(0xFF00E676)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = (1.5f + project.chromaKeySimilarity * 2.5f).dp,
                        color = keyBorder.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(6.dp)
                    )
            )
        }

        if (transProg > 0.05f && project.activeTransition == "GLITCH_TV") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF00E5FF).copy(alpha = transProg * 0.28f))
            )
        }

        // Active Pro Status Chips (Chroma Key / VN Freeze / VN Reverse / VN Speed Curve)
        if (project.chromaKeyEnabled || project.vnFreezeFrameActive || project.vnReversePlayback || project.vnSpeedCurve != "NONE") {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (project.chromaKeyEnabled) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xCC1B5E20), RoundedCornerShape(4.dp))
                            .border(0.5.dp, Color.Green, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🟩 क्रोमा की (${(project.chromaKeySimilarity * 100).toInt()}%)",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (project.vnFreezeFrameActive) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xCC006064), RoundedCornerShape(4.dp))
                            .border(0.5.dp, Color.Cyan, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("🧊 FREEZE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (project.vnReversePlayback) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xCCB71C1C), RoundedCornerShape(4.dp))
                            .border(0.5.dp, NewsGold, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("⏪ REVERSE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (project.vnSpeedCurve != "NONE") {
                    Box(
                        modifier = Modifier
                            .background(Color(0xCC0F172A), RoundedCornerShape(4.dp))
                            .border(0.5.dp, Color.Cyan, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("⚡ ${project.vnSpeedCurve}", color = Color.Cyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // VN Subtitle / Auto-Caption Overlay
        if (project.vnSubtitleText.isNotBlank()) {
            val subBg = when (project.vnSubtitleStyle) {
                "NEWS_BAR" -> Color(0xDD8E0000)
                "YELLOW_BOLD" -> Color(0xCC000000)
                "MINIMAL" -> Color.Transparent
                else -> Color(0xBB000000)
            }
            val subColor = when (project.vnSubtitleStyle) {
                "YELLOW_BOLD" -> NewsGold
                else -> Color.White
            }
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 18.dp)
                    .offset(y = 26.dp)
                    .background(subBg, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = project.vnSubtitleText,
                    color = subColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Flash Breaking News strobe effect if triggered
        if (isBreakingFlashActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red.copy(alpha = 0.35f))
            )
        }

        // ================= TOP BAR & LOGO OVERLAYS =================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            val displayClockText = project.manualClockText.ifBlank { currentTimeString }
            val displayLiveText = project.liveBadgeText.ifBlank { "LIVE" }

            // Main Top Bar Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left Column: Top-Left Logo / Badges / Left-positioned LIVE or Clock
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (project.showLogo && project.logoPosition == "TOP_LEFT") {
                        ChannelLogoOverlay(
                            logoType = project.logoType,
                            customLogoUri = project.customLogoUri,
                            scale = project.logoScale,
                            opacity = project.logoOpacity,
                            logoAnimation = project.logoAnimation,
                            channelName = project.channelName,
                            location = project.location,
                            modifier = Modifier
                                .offset { IntOffset(project.logoOffsetX.roundToInt(), project.logoOffsetY.roundToInt()) }
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        dispatchDrag("LOGO", dragAmount.x, dragAmount.y)
                                    }
                                }
                        )
                    }

                    if (project.showLiveBadge && project.liveBadgePosition == "TOP_LEFT") {
                        LiveBadgeChip(
                            text = displayLiveText,
                            pulseAlpha = pulseAlpha,
                            scale = project.liveBadgeScale,
                            offsetX = project.liveBadgeOffsetX,
                            offsetY = project.liveBadgeOffsetY,
                            onDrag = { dx, dy -> dispatchDrag("LIVE_BADGE", dx, dy) }
                        )
                    }

                    if (project.showClock && project.clockPosition == "TOP_LEFT" && displayClockText.isNotEmpty()) {
                        ClockBadgeChip(
                            timeText = displayClockText,
                            scale = project.clockScale,
                            offsetX = project.clockOffsetX,
                            offsetY = project.clockOffsetY,
                            onDrag = { dx, dy -> dispatchDrag("CLOCK", dx, dy) }
                        )
                    }
                }

                // Right Column: Top-Right Logo (Default), Location, Right-positioned LIVE or Clock
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (project.showLiveBadge && project.liveBadgePosition == "TOP_RIGHT") {
                        LiveBadgeChip(
                            text = displayLiveText,
                            pulseAlpha = pulseAlpha,
                            scale = project.liveBadgeScale,
                            offsetX = project.liveBadgeOffsetX,
                            offsetY = project.liveBadgeOffsetY,
                            onDrag = { dx, dy -> dispatchDrag("LIVE_BADGE", dx, dy) }
                        )
                    }

                    if (project.showLogo && project.logoPosition == "TOP_RIGHT") {
                        ChannelLogoOverlay(
                            logoType = project.logoType,
                            customLogoUri = project.customLogoUri,
                            scale = project.logoScale,
                            opacity = project.logoOpacity,
                            logoAnimation = project.logoAnimation,
                            channelName = project.channelName,
                            location = project.location,
                            modifier = Modifier
                                .offset { IntOffset(project.logoOffsetX.roundToInt(), project.logoOffsetY.roundToInt()) }
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        dispatchDrag("LOGO", dragAmount.x, dragAmount.y)
                                    }
                                }
                        )
                    }

                    if (project.showLocation && project.location.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(project.locationOffsetX.roundToInt(), project.locationOffsetY.roundToInt()) }
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        dispatchDrag("LOCATION", dragAmount.x, dragAmount.y)
                                    }
                                }
                                .background(
                                    Color(0xCC0F172A),
                                    RoundedCornerShape(4.dp)
                                )
                                .border(0.5.dp, Color(0x66FFFFFF), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "स्थान",
                                    tint = NewsAmber,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = project.location,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    if (project.showClock && project.clockPosition == "TOP_RIGHT" && displayClockText.isNotEmpty()) {
                        ClockBadgeChip(
                            timeText = displayClockText,
                            scale = project.clockScale,
                            offsetX = project.clockOffsetX,
                            offsetY = project.clockOffsetY,
                            onDrag = { dx, dy -> dispatchDrag("CLOCK", dx, dy) }
                        )
                    }
                }
            }

            // Bottom-Left / Bottom-Right Logo support
            if (project.showLogo && project.logoPosition == "BOTTOM_LEFT") {
                ChannelLogoOverlay(
                    logoType = project.logoType,
                    customLogoUri = project.customLogoUri,
                    scale = project.logoScale,
                    opacity = project.logoOpacity,
                    logoAnimation = project.logoAnimation,
                    channelName = project.channelName,
                    location = project.location,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 80.dp, start = 4.dp)
                        .offset { IntOffset(project.logoOffsetX.roundToInt(), project.logoOffsetY.roundToInt()) }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                dispatchDrag("LOGO", dragAmount.x, dragAmount.y)
                            }
                        }
                )
            } else if (project.showLogo && project.logoPosition == "BOTTOM_RIGHT") {
                ChannelLogoOverlay(
                    logoType = project.logoType,
                    customLogoUri = project.customLogoUri,
                    scale = project.logoScale,
                    opacity = project.logoOpacity,
                    logoAnimation = project.logoAnimation,
                    channelName = project.channelName,
                    location = project.location,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 80.dp, end = 4.dp)
                        .offset { IntOffset(project.logoOffsetX.roundToInt(), project.logoOffsetY.roundToInt()) }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                dispatchDrag("LOGO", dragAmount.x, dragAmount.y)
                            }
                        }
                )
            }

            // ================= ACTIVE STICKERS ROW =================
            if (project.showStickers) {
                val activeStickerTags = project.activeStickers.split(",")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .padding(top = 38.dp, start = 4.dp)
                        .offset { IntOffset(project.stickersOffsetX.roundToInt(), project.stickersOffsetY.roundToInt()) }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                dispatchDrag("STICKERS", dragAmount.x, dragAmount.y)
                            }
                        },
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (activeStickerTags.contains("EXCLUSIVE")) {
                        StickerBadge("विशेष ख़बर", NewsGold, Color.Black)
                    }
                    if (activeStickerTags.contains("BIG_BREAKING")) {
                        StickerBadge("बड़ा खुलासा", NewsRedPrimary, Color.White)
                    }
                    if (activeStickerTags.contains("GROUND_ZERO")) {
                        StickerBadge("ग्राउंड ज़ीरो", Color(0xFF0D47A1), Color.White)
                    }
                    if (activeStickerTags.contains("VIRAL")) {
                        StickerBadge("वायरल वीडियो", Color(0xFFE65100), Color.White)
                    }
                    if (activeStickerTags.contains("WEATHER_ALERT")) {
                        StickerBadge("मौसम अलर्ट", Color(0xFF00838F), Color.White)
                    }
                }
            }

            // ================= TELEPROMPTER OVERLAY =================
            if (showTeleprompter && project.teleprompterText.isNotBlank()) {
                TeleprompterView(
                    text = project.teleprompterText,
                    speed = teleprompterSpeed,
                    fontSize = teleprompterFontSize,
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .height(105.dp)
                        .align(Alignment.Center)
                )
            }

            // ================= BOTTOM OVERLAYS (Reporter Card, Headline, Ticker) =================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Reporter Lower Third
                if (project.showReporterCard && project.reporterName.isNotBlank() && project.headlinePosition != "CENTER") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset { IntOffset(project.reporterOffsetX.roundToInt(), project.reporterOffsetY.roundToInt()) }
                            .graphicsLayer {
                                scaleX = project.reporterScale.coerceIn(0.7f, 1.6f)
                                scaleY = project.reporterScale.coerceIn(0.7f, 1.6f)
                            }
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    dispatchDrag("REPORTER", dragAmount.x, dragAmount.y)
                                }
                            },
                        contentAlignment = if (project.reporterPosition == "BOTTOM_RIGHT") Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        ReporterLowerThird(
                            name = project.reporterName,
                            role = project.reporterRole,
                            location = project.location,
                            primaryColor = primaryColor
                        )
                    }
                }

                // Main News Headline Card (when in BOTTOM position)
                if (project.showHeadline && project.headline.isNotBlank() && project.headlinePosition == "BOTTOM") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset { IntOffset(project.headlineOffsetX.roundToInt(), project.headlineOffsetY.roundToInt()) }
                            .graphicsLayer {
                                scaleX = project.headlineScale.coerceIn(0.7f, 1.6f)
                                scaleY = project.headlineScale.coerceIn(0.7f, 1.6f)
                            }
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    dispatchDrag("HEADLINE", dragAmount.x, dragAmount.y)
                                }
                            }
                    ) {
                        MainHeadlineCard(
                            category = project.category,
                            headline = project.headline,
                            subHeadline = project.subHeadline,
                            primaryColor = primaryColor,
                            animationStyle = project.headlineAnimation
                        )
                    }
                }

                // Scrolling Ticker Tape (when in BOTTOM position)
                if (project.showTicker && project.tickerText.isNotBlank() && project.tickerPosition == "BOTTOM") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset { IntOffset(project.tickerOffsetX.roundToInt(), project.tickerOffsetY.roundToInt()) }
                            .graphicsLayer {
                                scaleX = project.tickerScale.coerceIn(0.7f, 1.6f)
                                scaleY = project.tickerScale.coerceIn(0.7f, 1.6f)
                            }
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    dispatchDrag("TICKER", dragAmount.x, dragAmount.y)
                                }
                            }
                    ) {
                        ScrollingTicker(
                            tickerText = project.tickerText,
                            tagText = project.tickerTagText,
                            tickerSpeed = project.tickerSpeed,
                            animationStyle = project.tickerAnimation,
                            primaryColor = primaryColor
                        )
                    }
                }
            }

            // Headline in CENTER position (Pop / Breaking Style)
            if (project.showHeadline && project.headline.isNotBlank() && project.headlinePosition == "CENTER") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.96f)
                        .align(Alignment.Center)
                        .offset { IntOffset(project.headlineOffsetX.roundToInt(), project.headlineOffsetY.roundToInt()) }
                        .graphicsLayer {
                            scaleX = project.headlineScale.coerceIn(0.7f, 1.6f)
                            scaleY = project.headlineScale.coerceIn(0.7f, 1.6f)
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                dispatchDrag("HEADLINE", dragAmount.x, dragAmount.y)
                            }
                        }
                ) {
                    MainHeadlineCard(
                        category = project.category,
                        headline = project.headline,
                        subHeadline = project.subHeadline,
                        primaryColor = primaryColor,
                        animationStyle = project.headlineAnimation
                    )
                }
            }

            // Headline in TOP position
            if (project.showHeadline && project.headline.isNotBlank() && project.headlinePosition == "TOP") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 42.dp)
                        .offset { IntOffset(project.headlineOffsetX.roundToInt(), project.headlineOffsetY.roundToInt()) }
                        .graphicsLayer {
                            scaleX = project.headlineScale.coerceIn(0.7f, 1.6f)
                            scaleY = project.headlineScale.coerceIn(0.7f, 1.6f)
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                dispatchDrag("HEADLINE", dragAmount.x, dragAmount.y)
                            }
                        }
                ) {
                    MainHeadlineCard(
                        category = project.category,
                        headline = project.headline,
                        subHeadline = project.subHeadline,
                        primaryColor = primaryColor,
                        animationStyle = project.headlineAnimation
                    )
                }
            }

            // Scrolling Ticker in TOP position
            if (project.showTicker && project.tickerText.isNotBlank() && project.tickerPosition == "TOP") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .offset { IntOffset(project.tickerOffsetX.roundToInt(), project.tickerOffsetY.roundToInt()) }
                        .graphicsLayer {
                            scaleX = project.tickerScale.coerceIn(0.7f, 1.6f)
                            scaleY = project.tickerScale.coerceIn(0.7f, 1.6f)
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                dispatchDrag("TICKER", dragAmount.x, dragAmount.y)
                            }
                        }
                ) {
                    ScrollingTicker(
                        tickerText = project.tickerText,
                        tagText = project.tickerTagText,
                        tickerSpeed = project.tickerSpeed,
                        animationStyle = project.tickerAnimation,
                        primaryColor = primaryColor
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveBadgeChip(
    text: String,
    pulseAlpha: Float,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    onDrag: (Float, Float) -> Unit
) {
    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .graphicsLayer {
                scaleX = scale.coerceIn(0.7f, 1.6f)
                scaleY = scale.coerceIn(0.7f, 1.6f)
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
            .background(
                Color.Red.copy(alpha = pulseAlpha),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(Color.White, CircleShape)
            )
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ClockBadgeChip(
    timeText: String,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    onDrag: (Float, Float) -> Unit
) {
    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .graphicsLayer {
                scaleX = scale.coerceIn(0.7f, 1.6f)
                scaleY = scale.coerceIn(0.7f, 1.6f)
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
            .background(Color(0xE6000000), RoundedCornerShape(4.dp))
            .border(0.5.dp, NewsGold.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = timeText,
            color = NewsGold,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp
        )
    }
}

@Composable
private fun StickerBadge(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(4.dp))
            .background(bgColor, RoundedCornerShape(4.dp))
            .border(0.5.dp, Color.White, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Black,
            fontSize = 9.sp
        )
    }
}

@Composable
private fun ReporterLowerThird(
    name: String,
    role: String,
    location: String,
    primaryColor: Color
) {
    Box(
        modifier = Modifier
            .padding(start = 2.dp, bottom = 2.dp)
            .shadow(4.dp, RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
            .background(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xE61E293B),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(listOf(primaryColor, NewsGold, Color.Transparent)),
                shape = RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(primaryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "रिपोर्टर",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }

            Column {
                Text(
                    text = name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    lineHeight = 13.sp
                )
                val subtitle = buildString {
                    if (role.isNotBlank()) append(role)
                    if (role.isNotBlank() && location.isNotBlank()) append(" • ")
                    if (location.isNotBlank()) append(location)
                }
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        color = NewsGold,
                        fontWeight = FontWeight.Medium,
                        fontSize = 9.sp,
                        lineHeight = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MainHeadlineCard(
    category: String,
    headline: String,
    subHeadline: String,
    primaryColor: Color,
    animationStyle: String = "SHINE_SWEEP"
) {
    val transition = rememberInfiniteTransition(label = "headlineAnim")
    val animPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "headlinePhase"
    )
    val pulseFactor by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "headlinePulse"
    )

    val cardScale = if (animationStyle == "ZOOM_POP") 1f + (pulseFactor * 0.028f) else 1f
    val borderBrush = when (animationStyle) {
        "PULSE_GLOW" -> Brush.horizontalGradient(
            listOf(NewsGold.copy(alpha = pulseFactor), primaryColor.copy(alpha = pulseFactor), NewsGold.copy(alpha = pulseFactor))
        )
        "NEON_FLASH" -> {
            val flashColor = if (pulseFactor > 0.68f) Color.Cyan else NewsGold
            Brush.horizontalGradient(listOf(flashColor, primaryColor, flashColor))
        }
        "SHINE_SWEEP" -> Brush.horizontalGradient(
            listOf(primaryColor, NewsGold, Color.White.copy(alpha = 0.8f))
        )
        else -> Brush.horizontalGradient(listOf(primaryColor, Color(0x66FFFFFF)))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .shadow(8.dp)
    ) {
        // Category Ribbon (ब्रेकिंग न्यूज़ / विशेष ख़बर)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(primaryColor, NewsRedDark, Color(0xFF1E293B))
                    )
                )
                .border(
                    width = 0.7.dp,
                    color = if (animationStyle == "NEON_FLASH" && pulseFactor > 0.7f) Color.Cyan else NewsGold,
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                )
        ) {
            if (animationStyle == "SHINE_SWEEP" || animationStyle == "SLIDE_REVEAL") {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                colorStops = arrayOf(
                                    (animPhase - 0.25f).coerceIn(0f, 1f) to Color.Transparent,
                                    animPhase.coerceIn(0f, 1f) to Color.White.copy(alpha = 0.42f),
                                    (animPhase + 0.25f).coerceIn(0f, 1f) to Color.Transparent
                                )
                            )
                        )
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            if (animationStyle != "NONE") NewsGold.copy(alpha = pulseFactor) else NewsGold,
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = category.ifBlank { "ब्रेकिंग न्यूज़" }.uppercase(),
                    color = NewsGold,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Headline Main Banner Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xF00A0E1A), Color(0xF8151D2F))
                    )
                )
                .border(
                    width = if (animationStyle == "PULSE_GLOW" || animationStyle == "NEON_FLASH") 1.5.dp else 1.dp,
                    brush = borderBrush,
                    shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
                )
        ) {
            // Animated Shine Sweep or Slide Reveal Highlight inside Banner
            if (animationStyle == "SHINE_SWEEP") {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                colorStops = arrayOf(
                                    (animPhase - 0.18f).coerceIn(0f, 1f) to Color.Transparent,
                                    animPhase.coerceIn(0f, 1f) to NewsGold.copy(alpha = 0.24f),
                                    (animPhase + 0.18f).coerceIn(0f, 1f) to Color.Transparent
                                )
                            )
                        )
                )
            } else if (animationStyle == "SLIDE_REVEAL") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animPhase.coerceIn(0.08f, 1f))
                        .height(2.5.dp)
                        .align(Alignment.BottomStart)
                        .background(Brush.horizontalGradient(listOf(primaryColor, NewsGold, Color.White)))
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = headline,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (subHeadline.isNotBlank()) {
                    Text(
                        text = subHeadline,
                        color = NewsAmber,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ScrollingTicker(
    tickerText: String,
    tagText: String = "⚡ ताज़ा अपडेट",
    tickerSpeed: Float = 1.0f,
    animationStyle: String = "BREAKING_SWEEP",
    primaryColor: Color
) {
    val transition = rememberInfiniteTransition(label = "tickerAnim")
    val animPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tickerShinePhase"
    )
    val pulseFactor by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tickerPulse"
    )

    val effectiveTag = tagText.ifBlank { "⚡ ताज़ा अपडेट" }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // "ताज़ा अपडेट" Tag placed directly ABOVE the running ticker line
        Box(
            modifier = Modifier
                .padding(start = 2.dp)
                .shadow(4.dp, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(
                    brush = if (animationStyle == "FLASH_TAG" && pulseFactor > 0.7f) {
                        Brush.horizontalGradient(listOf(primaryColor, Color.Red, NewsGold))
                    } else {
                        Brush.horizontalGradient(listOf(NewsGold, Color(0xFFFF9100), primaryColor))
                    }
                )
                .border(
                    width = 0.6.dp,
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                )
        ) {
            if (animationStyle == "BREAKING_SWEEP" || animationStyle == "WAVE_GLOW") {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                colorStops = arrayOf(
                                    (animPhase - 0.25f).coerceIn(0f, 1f) to Color.Transparent,
                                    animPhase.coerceIn(0f, 1f) to Color.White.copy(alpha = 0.45f),
                                    (animPhase + 0.25f).coerceIn(0f, 1f) to Color.Transparent
                                )
                            )
                        )
                )
            }
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            if (animationStyle != "NONE") Color.Red.copy(alpha = pulseFactor) else Color.Red,
                            CircleShape
                        )
                )
                Text(
                    text = effectiveTag,
                    color = if (animationStyle == "FLASH_TAG" && pulseFactor > 0.7f) Color.White else Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Full Width Running Ticker Line (चलने वाली लाइन)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(
                    if (animationStyle == "WAVE_GLOW") {
                        Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0f to Color(0xF00A0E1A),
                                animPhase.coerceIn(0.05f, 0.95f) to primaryColor.copy(alpha = 0.55f),
                                1f to Color(0xF00A0E1A)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xF00A0E1A), Color(0xF5111A2E)))
                    }
                )
                .border(
                    width = if (animationStyle == "PULSE_BORDER") 1.3.dp else 0.8.dp,
                    color = if (animationStyle == "PULSE_BORDER") {
                        NewsGold.copy(alpha = pulseFactor)
                    } else {
                        NewsGold.copy(alpha = 0.8f)
                    }
                )
        ) {
            if (animationStyle == "BREAKING_SWEEP") {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                colorStops = arrayOf(
                                    (animPhase - 0.15f).coerceIn(0f, 1f) to Color.Transparent,
                                    animPhase.coerceIn(0f, 1f) to NewsGold.copy(alpha = 0.22f),
                                    (animPhase + 0.15f).coerceIn(0f, 1f) to Color.Transparent
                                )
                            )
                        )
                )
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                val containerWidthPx = constraints.maxWidth.toFloat()
                val scrollDurationMs = (14000f / tickerSpeed.coerceIn(0.4f, 2.5f)).toInt().coerceAtLeast(2500)
                val scrollOffset by transition.animateFloat(
                    initialValue = containerWidthPx,
                    targetValue = -containerWidthPx * 1.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = scrollDurationMs, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "tickerOffset"
                )

                Text(
                    text = "$tickerText        ★        $tickerText",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.offset { IntOffset(scrollOffset.roundToInt(), 0) }
                )
            }
        }
    }
}

@Composable
private fun TeleprompterView(
    text: String,
    speed: Float,
    fontSize: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xB3000000), RoundedCornerShape(8.dp))
            .border(1.dp, NewsGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        val transition = rememberInfiniteTransition(label = "teleprompterScroll")
        val scrollDuration = (25000 / speed.coerceIn(0.5f, 3.0f)).toInt()
        val scrollY by transition.animateFloat(
            initialValue = 120f,
            targetValue = -350f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = scrollDuration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "teleprompterY"
        )

        Text(
            text = text,
            color = Color(0xFFFFFF8D),
            fontSize = fontSize.sp,
            lineHeight = (fontSize * 1.4).sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, scrollY.roundToInt()) }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(Color.Red.copy(alpha = 0.5f))
                .align(Alignment.Center)
        )
    }
}
