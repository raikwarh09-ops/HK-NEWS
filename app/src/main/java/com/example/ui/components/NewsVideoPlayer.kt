package com.example.ui.components

import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.RenderEffect
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.net.Uri
import android.os.Build
import android.view.Surface
import android.view.TextureView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.NewsAmber
import com.example.ui.theme.NewsGold
import com.example.ui.theme.NewsRedPrimary
import kotlinx.coroutines.delay

/**
 * TextureView-backed video player with live RenderEffect color grading,
 * audio session Equalizer/Reverb DSP preview, bidirectional timeline seek sync,
 * trim bounds clamping, speed curves, and Freeze Frame control.
 */
@Composable
fun NewsVideoPlayer(
    videoUri: Uri,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    speed: Float = 1.0f,
    volume: Float = 1.0f,
    trimStart: Float = 0.0f,
    trimEnd: Float = 1.0f,
    freezeFrame: Boolean = false,
    speedCurve: String = "NONE",
    audioVolumeBoost: Float = 1.0f,
    audioPitch: Float = 1.0f,
    voiceChangerEffect: String = "NORMAL",
    activeFilter: String = "NONE",
    contrast: Float = 1.0f,
    brightness: Float = 0.0f,
    saturation: Float = 1.0f,
    warmth: Float = 0.0f,
    sharpen: Float = 0.0f,
    showControls: Boolean = true,
    timelineSeekRequest: Pair<Long, Long>? = null,
    onPositionUpdate: (Long, Long) -> Unit = { _, _ -> },
    onPlaybackStateChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var textureViewRef by remember { mutableStateOf<TextureView?>(null) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
    var activeSurface by remember { mutableStateOf<Surface?>(null) }
    var equalizerRef by remember { mutableStateOf<Equalizer?>(null) }
    var reverbRef by remember { mutableStateOf<PresetReverb?>(null) }
    var isPrepared by remember { mutableStateOf(false) }

    var videoWidth by remember { mutableIntStateOf(0) }
    var videoHeight by remember { mutableIntStateOf(0) }
    var durationMs by remember { mutableIntStateOf(0) }
    var currentPositionMs by remember { mutableIntStateOf(0) }
    var userIsSeeking by remember { mutableStateOf(false) }
    var controlsVisible by remember { mutableStateOf(showControls) }
    var lastHandledSeekNonce by remember { mutableStateOf<Long?>(null) }

    val latestIsPlaying by rememberUpdatedState(isPlaying)
    val latestFreeze by rememberUpdatedState(freezeFrame)
    val latestSpeed by rememberUpdatedState(speed)
    val latestSpeedCurve by rememberUpdatedState(speedCurve)
    val latestVolume by rememberUpdatedState(volume)
    val latestBoost by rememberUpdatedState(audioVolumeBoost)
    val latestPitch by rememberUpdatedState(audioPitch)
    val latestVoiceEffect by rememberUpdatedState(voiceChangerEffect)
    val latestTrimStart by rememberUpdatedState(trimStart)
    val latestTrimEnd by rememberUpdatedState(trimEnd)
    val latestFilter by rememberUpdatedState(activeFilter)
    val latestContrast by rememberUpdatedState(contrast)
    val latestBrightness by rememberUpdatedState(brightness)
    val latestSaturation by rememberUpdatedState(saturation)
    val latestWarmth by rememberUpdatedState(warmth)
    val latestSharpen by rememberUpdatedState(sharpen)
    val latestOnPositionUpdate by rememberUpdatedState(onPositionUpdate)

    fun releaseAudioFx() {
        try {
            equalizerRef?.release()
        } catch (_: Exception) {}
        equalizerRef = null
        try {
            reverbRef?.release()
        } catch (_: Exception) {}
        reverbRef = null
    }

    fun applyCenterCropTransform(tv: TextureView, vidW: Int, vidH: Int) {
        if (vidW <= 0 || vidH <= 0 || tv.width <= 0 || tv.height <= 0) return
        val viewW = tv.width.toFloat()
        val viewH = tv.height.toFloat()
        val videoAspect = vidW.toFloat() / vidH.toFloat()
        val viewAspect = viewW / viewH
        val scaleX: Float
        val scaleY: Float
        if (videoAspect > viewAspect) {
            scaleX = videoAspect / viewAspect
            scaleY = 1f
        } else {
            scaleX = 1f
            scaleY = viewAspect / videoAspect
        }
        val matrix = Matrix()
        matrix.setScale(scaleX, scaleY, viewW / 2f, viewH / 2f)
        tv.setTransform(matrix)
    }

    fun applyTextureColorEffect(tv: TextureView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val cm = VideoFilterPalettes.buildAndroidColorMatrix(
                    filterId = latestFilter,
                    contrast = latestContrast,
                    brightness = latestBrightness,
                    saturation = latestSaturation,
                    warmth = latestWarmth,
                    sharpen = latestSharpen
                )
                tv.setRenderEffect(
                    RenderEffect.createColorFilterEffect(ColorMatrixColorFilter(cm))
                )
            } catch (_: Exception) {}
        }
    }

    fun applyVoiceDspAudioSession(mp: MediaPlayer, effectId: String) {
        try {
            val sessionId = mp.audioSessionId
            if (sessionId == 0) return
            if (equalizerRef == null) {
                equalizerRef = try {
                    Equalizer(0, sessionId)
                } catch (_: Exception) { null }
            }
            if (reverbRef == null) {
                reverbRef = try {
                    PresetReverb(0, sessionId)
                } catch (_: Exception) { null }
            }

            equalizerRef?.let { eq ->
                val bands = eq.numberOfBands.toInt()
                val range = eq.bandLevelRange
                val minLvl = range[0].toInt()
                val maxLvl = range[1].toInt()
                if (effectId == "NORMAL") {
                    eq.enabled = false
                } else {
                    eq.enabled = true
                    for (b in 0 until bands) {
                        val centerKhz = eq.getCenterFreq(b.toShort()) / 1_000_000f
                        val targetRatio = when (effectId) {
                            "CHATGPT_COVE" -> when {
                                centerKhz < 0.35f -> 0.60f
                                centerKhz in 0.8f..2.2f -> 0.25f
                                centerKhz > 4.0f -> -0.20f
                                else -> 0.1f
                            }
                            "CHATGPT_BREEZE" -> when {
                                centerKhz in 1.2f..4.2f -> 0.65f
                                centerKhz < 0.35f -> 0.30f
                                else -> 0.15f
                            }
                            "CHATGPT_EMBER" -> when {
                                centerKhz in 0.25f..2.8f -> 0.65f
                                centerKhz > 3.5f -> 0.35f
                                else -> 0.1f
                            }
                            "CHATGPT_JUNIPER" -> when {
                                centerKhz > 1.4f -> 0.68f
                                centerKhz < 0.3f -> -0.15f
                                else -> 0.2f
                            }
                            "CHATGPT_SKY" -> when {
                                centerKhz > 1.8f -> 0.62f
                                centerKhz in 0.4f..1.5f -> 0.25f
                                else -> 0f
                            }
                            "CHATGPT_SOL" -> when {
                                centerKhz < 0.5f -> 0.55f
                                centerKhz in 1.0f..2.5f -> 0.40f
                                else -> 0f
                            }
                            "DEEP_ANCHOR", "SLOW_CINEMA" -> if (centerKhz < 0.4f) 0.7f else -0.35f
                            "FEMALE_ANCHOR", "CHIPMUNK" -> if (centerKhz > 1.5f) 0.65f else -0.25f
                            "RADIO_MIC", "MEGAPHONE" -> if (centerKhz in 0.8f..3.2f) 0.85f else -0.8f
                            "ROBOT" -> if (b % 2 == 0) 0.8f else -0.7f
                            else -> 0f
                        }
                        val level = ((maxLvl - minLvl) * 0.45f * targetRatio).toInt()
                            .coerceIn(minLvl, maxLvl)
                            .toShort()
                        eq.setBandLevel(b.toShort(), level)
                    }
                }
            }

            reverbRef?.let { rv ->
                when (effectId) {
                    "ECHO_STUDIO" -> {
                        rv.preset = PresetReverb.PRESET_LARGEHALL
                        rv.enabled = true
                    }
                    "ROBOT", "CHATGPT_COVE", "CHATGPT_SKY", "CHATGPT_SOL" -> {
                        rv.preset = PresetReverb.PRESET_SMALLROOM
                        rv.enabled = true
                    }
                    else -> {
                        rv.enabled = false
                    }
                }
            }
        } catch (_: Exception) {}
    }

    fun applyAudioAndSpeedParams(mp: MediaPlayer, progressRatio: Float) {
        val shouldPlayNow = latestIsPlaying && !latestFreeze
        try {
            val effectiveGain = (latestVolume * latestBoost).coerceIn(0f, 1f)
            mp.setVolume(effectiveGain, effectiveGain)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val curveMult = when (latestSpeedCurve) {
                    "MONTAGE" -> if (progressRatio < 0.35f || progressRatio > 0.7f) 1.45f else 0.7f
                    "HERO" -> if (progressRatio in 0.3f..0.65f) 0.5f else 1.35f
                    "BULLET" -> if (progressRatio in 0.25f..0.75f) 0.35f else 1.5f
                    "JUMP_CUT" -> if ((progressRatio * 4).toInt() % 2 == 0) 1.5f else 0.85f
                    "FLASH_IN" -> 1.5f - (progressRatio * 0.6f)
                    "FLASH_OUT" -> 0.8f + (progressRatio * 0.7f)
                    else -> 1.0f
                }
                val effSpeed = (latestSpeed * curveMult).coerceIn(0.25f, 3.0f)
                val effPitch = if (latestPitch == 1.0f) {
                    com.example.audio.NewsAudioEffects.resolveDefaultPitch(latestVoiceEffect)
                } else {
                    latestPitch
                }.coerceIn(0.5f, 1.8f)

                val params = PlaybackParams().apply {
                    setSpeed(effSpeed)
                    setPitch(effPitch)
                }
                mp.playbackParams = params
                // Guard against Android devices where setting PlaybackParams unpauses playback
                if (!shouldPlayNow && mp.isPlaying) {
                    mp.pause()
                }
            }
            applyVoiceDspAudioSession(mp, latestVoiceEffect)
        } catch (_: Exception) {}
    }

    // Apply live RenderEffect color grading whenever filter/adjustments change
    LaunchedEffect(
        textureViewRef,
        activeFilter,
        contrast,
        brightness,
        saturation,
        warmth,
        sharpen
    ) {
        val tv = textureViewRef ?: return@LaunchedEffect
        applyTextureColorEffect(tv)
    }

    // Sync timeline scrubbing / jump-to-start / jump-to-end into MediaPlayer
    LaunchedEffect(timelineSeekRequest, mediaPlayerRef, isPrepared, durationMs) {
        val req = timelineSeekRequest ?: return@LaunchedEffect
        val mp = mediaPlayerRef ?: return@LaunchedEffect
        if (!isPrepared || durationMs <= 0) return@LaunchedEffect
        if (lastHandledSeekNonce == req.second) return@LaunchedEffect
        lastHandledSeekNonce = req.second
        val startMs = (durationMs * latestTrimStart.coerceIn(0f, 0.95f)).toInt()
        val endMs = (durationMs * latestTrimEnd.coerceIn((latestTrimStart + 0.03f).coerceAtMost(1f), 1f)).toInt()
        val clampedSeek = req.first.toInt().coerceIn(startMs, endMs.coerceAtLeast(startMs))
        try {
            mp.seekTo(clampedSeek)
            currentPositionMs = clampedSeek
        } catch (_: Exception) {}
    }

    // Periodic time ticker + Trim boundaries + Curve modulation + ViewModel sync
    LaunchedEffect(mediaPlayerRef, isPrepared, isPlaying, freezeFrame, trimStart, trimEnd, speedCurve) {
        while (true) {
            val mp = mediaPlayerRef
            if (mp != null && isPrepared && durationMs > 0) {
                try {
                    val startMs = (durationMs * latestTrimStart.coerceIn(0f, 0.95f)).toInt()
                    val endMs = (durationMs * latestTrimEnd.coerceIn((latestTrimStart + 0.03f).coerceAtMost(1f), 1f)).toInt()
                    if (!userIsSeeking) {
                        var pos = mp.currentPosition
                        if (pos < startMs || pos >= endMs) {
                            mp.seekTo(startMs)
                            pos = startMs
                        }
                        currentPositionMs = pos
                        latestOnPositionUpdate(pos.toLong(), durationMs.toLong())
                        if (latestSpeedCurve != "NONE" && mp.isPlaying) {
                            val span = (endMs - startMs).coerceAtLeast(1)
                            val norm = ((pos - startMs).toFloat() / span).coerceIn(0f, 1f)
                            applyAudioAndSpeedParams(mp, norm)
                        }
                    }
                } catch (_: Exception) {}
            }
            delay(180)
        }
    }

    // Play / Pause / Freeze sync
    LaunchedEffect(isPlaying, freezeFrame, mediaPlayerRef, isPrepared) {
        val mp = mediaPlayerRef ?: return@LaunchedEffect
        if (!isPrepared) return@LaunchedEffect
        try {
            val shouldPlay = isPlaying && !freezeFrame
            if (shouldPlay && !mp.isPlaying) {
                mp.start()
            } else if (!shouldPlay && mp.isPlaying) {
                mp.pause()
            }
        } catch (_: Exception) {}
    }

    // Speed, Volume Boost, Pitch & Voice Effect sync
    LaunchedEffect(speed, volume, audioVolumeBoost, audioPitch, voiceChangerEffect, speedCurve, mediaPlayerRef, isPrepared) {
        val mp = mediaPlayerRef ?: return@LaunchedEffect
        if (!isPrepared) return@LaunchedEffect
        val norm = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
        applyAudioAndSpeedParams(mp, norm)
    }

    // Re-apply aspect matrix whenever TextureView dimensions or video dimensions change
    LaunchedEffect(textureViewRef, videoWidth, videoHeight) {
        val tv = textureViewRef ?: return@LaunchedEffect
        if (videoWidth > 0 && videoHeight > 0) {
            applyCenterCropTransform(tv, videoWidth, videoHeight)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable { controlsVisible = !controlsVisible }
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                TextureView(ctx).apply {
                    isOpaque = false
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                            val s = Surface(surfaceTexture)
                            activeSurface = s
                            textureViewRef = this@apply
                            releaseAudioFx()
                            try {
                                mediaPlayerRef?.release()
                            } catch (_: Exception) {}
                            isPrepared = false

                            val mp = MediaPlayer()
                            mediaPlayerRef = mp
                            try {
                                if (videoUri.scheme == "file" && !videoUri.path.isNullOrBlank()) {
                                    mp.setDataSource(videoUri.path)
                                } else {
                                    mp.setDataSource(ctx, videoUri)
                                }
                                mp.setSurface(s)
                                mp.isLooping = true
                                mp.setOnErrorListener { _, _, _ ->
                                    isPrepared = false
                                    true
                                }
                                mp.setOnVideoSizeChangedListener { _, vw, vh ->
                                    videoWidth = vw
                                    videoHeight = vh
                                    applyCenterCropTransform(this@apply, vw, vh)
                                }
                                mp.setOnPreparedListener { preparedMp ->
                                    isPrepared = true
                                    durationMs = preparedMp.duration
                                    videoWidth = preparedMp.videoWidth
                                    videoHeight = preparedMp.videoHeight
                                    applyCenterCropTransform(this@apply, preparedMp.videoWidth, preparedMp.videoHeight)
                                    applyTextureColorEffect(this@apply)
                                    val startPos = (preparedMp.duration * latestTrimStart.coerceIn(0f, 0.95f)).toInt()
                                    if (startPos > 0) preparedMp.seekTo(startPos)
                                    currentPositionMs = startPos
                                    latestOnPositionUpdate(startPos.toLong(), preparedMp.duration.toLong())
                                    applyAudioAndSpeedParams(preparedMp, 0f)
                                    if (latestIsPlaying && !latestFreeze) {
                                        preparedMp.start()
                                    }
                                }
                                mp.prepareAsync()
                            } catch (_: Exception) {}
                        }

                        override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                            applyCenterCropTransform(this@apply, videoWidth, videoHeight)
                        }

                        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                            isPrepared = false
                            releaseAudioFx()
                            try {
                                mediaPlayerRef?.stop()
                                mediaPlayerRef?.release()
                            } catch (_: Exception) {}
                            mediaPlayerRef = null
                            try {
                                activeSurface?.release()
                            } catch (_: Exception) {}
                            activeSurface = null
                            return true
                        }

                        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
                    }
                    textureViewRef = this
                }
            },
            update = { tv ->
                textureViewRef = tv
                if (videoWidth > 0 && videoHeight > 0) {
                    applyCenterCropTransform(tv, videoWidth, videoHeight)
                }
                applyTextureColorEffect(tv)
            }
        )

        // Overlay Controls (Play/Pause center pill + compact bar spaced clear of bottom headline/ticker)
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x33000000))
            ) {
                IconButton(
                    onClick = {
                        val next = !isPlaying
                        onPlaybackStateChange(next)
                        try {
                            if (next) mediaPlayerRef?.start() else mediaPlayerRef?.pause()
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xAA000000), CircleShape)
                        .align(Alignment.Center)
                ) {
                    Icon(
                        imageVector = if (isPlaying && !freezeFrame) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying && !freezeFrame) "रोकें" else "चलाएं",
                        tint = NewsGold,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(start = 8.dp, end = 8.dp, bottom = 58.dp)
                        .background(Color(0xCC0F172A), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatPlayerTime(currentPositionMs),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (durationMs > 0) formatPlayerTime(durationMs) else "00:00",
                            color = NewsAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (durationMs > 0) {
                        Slider(
                            value = currentPositionMs.toFloat().coerceIn(0f, durationMs.toFloat()),
                            onValueChange = { pos ->
                                userIsSeeking = true
                                currentPositionMs = pos.toInt()
                            },
                            onValueChangeFinished = {
                                userIsSeeking = false
                                try {
                                    mediaPlayerRef?.seekTo(currentPositionMs)
                                    latestOnPositionUpdate(currentPositionMs.toLong(), durationMs.toLong())
                                } catch (_: Exception) {}
                            },
                            valueRange = 0f..durationMs.toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = NewsGold,
                                activeTrackColor = NewsRedPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().height(22.dp)
                        )
                    }
                }
            }
        }
    }

    // Handle URI changes cleanly on active surface
    LaunchedEffect(videoUri) {
        val surface = activeSurface ?: return@LaunchedEffect
        val tv = textureViewRef ?: return@LaunchedEffect
        isPrepared = false
        releaseAudioFx()
        try {
            mediaPlayerRef?.reset()
            val mp = mediaPlayerRef ?: MediaPlayer().also { mediaPlayerRef = it }
            if (videoUri.scheme == "file" && !videoUri.path.isNullOrBlank()) {
                mp.setDataSource(videoUri.path)
            } else {
                mp.setDataSource(context, videoUri)
            }
            mp.setSurface(surface)
            mp.isLooping = true
            mp.setOnErrorListener { _, _, _ ->
                isPrepared = false
                true
            }
            mp.setOnVideoSizeChangedListener { _, vw, vh ->
                videoWidth = vw
                videoHeight = vh
                applyCenterCropTransform(tv, vw, vh)
            }
            mp.setOnPreparedListener { preparedMp ->
                isPrepared = true
                durationMs = preparedMp.duration
                videoWidth = preparedMp.videoWidth
                videoHeight = preparedMp.videoHeight
                applyCenterCropTransform(tv, preparedMp.videoWidth, preparedMp.videoHeight)
                applyTextureColorEffect(tv)
                val startPos = (preparedMp.duration * latestTrimStart.coerceIn(0f, 0.95f)).toInt()
                if (startPos > 0) preparedMp.seekTo(startPos)
                currentPositionMs = startPos
                latestOnPositionUpdate(startPos.toLong(), preparedMp.duration.toLong())
                applyAudioAndSpeedParams(preparedMp, 0f)
                if (latestIsPlaying && !latestFreeze) preparedMp.start()
            }
            mp.prepareAsync()
        } catch (_: Exception) {}
    }

    DisposableEffect(Unit) {
        onDispose {
            isPrepared = false
            releaseAudioFx()
            try {
                mediaPlayerRef?.stop()
                mediaPlayerRef?.release()
            } catch (_: Exception) {}
            mediaPlayerRef = null
            try {
                activeSurface?.release()
            } catch (_: Exception) {}
            activeSurface = null
        }
    }
}

private fun formatPlayerTime(millis: Int): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
