package com.example.ui.components

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
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

@Composable
fun NewsVideoPlayer(
    videoUri: Uri,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    speed: Float = 1.0f,
    volume: Float = 1.0f,
    showControls: Boolean = true,
    onPlaybackStateChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }

    var durationMs by remember { mutableIntStateOf(0) }
    var currentPositionMs by remember { mutableIntStateOf(0) }
    var userIsSeeking by remember { mutableStateOf(false) }
    var controlsVisible by remember { mutableStateOf(showControls) }

    // Periodic time update ticker
    LaunchedEffect(videoViewRef, isPlaying) {
        while (true) {
            videoViewRef?.let { vv ->
                if (vv.isPlaying && !userIsSeeking) {
                    currentPositionMs = vv.currentPosition
                    durationMs = vv.duration
                }
            }
            delay(500)
        }
    }

    // React to play/pause state changes
    LaunchedEffect(isPlaying, videoViewRef) {
        videoViewRef?.let { vv ->
            if (isPlaying && !vv.isPlaying) {
                vv.start()
            } else if (!isPlaying && vv.isPlaying) {
                vv.pause()
            }
        }
    }

    // React to speed & volume changes
    LaunchedEffect(speed, volume, mediaPlayerRef) {
        mediaPlayerRef?.let { mp ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val params = mp.playbackParams
                    params.speed = speed.coerceIn(0.25f, 3.0f)
                    mp.playbackParams = params
                }
                mp.setVolume(volume, volume)
            } catch (_: Exception) {}
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
                VideoView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setVideoURI(videoUri)
                    setOnPreparedListener { mp ->
                        mediaPlayerRef = mp
                        mp.isLooping = true
                        durationMs = mp.duration
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val params = mp.playbackParams
                                params.speed = speed.coerceIn(0.25f, 3.0f)
                                mp.playbackParams = params
                            }
                            mp.setVolume(volume, volume)
                        } catch (_: Exception) {}
                        if (isPlaying) {
                            start()
                        }
                    }
                    setOnCompletionListener {
                        start()
                    }
                    videoViewRef = this
                }
            },
            update = { vv ->
                videoViewRef = vv
            }
        )

        // Overlay Controls (Play/Pause, Slider, Time)
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x55000000))
            ) {
                // Center Big Play/Pause Toggle
                IconButton(
                    onClick = {
                        val next = !isPlaying
                        onPlaybackStateChange(next)
                        if (next) videoViewRef?.start() else videoViewRef?.pause()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xAA000000), CircleShape)
                        .align(Alignment.Center)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "रोकें" else "चलाएं",
                        tint = NewsGold,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Bottom Timeline Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color(0xDD0F172A))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(currentPositionMs),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (durationMs > 0) formatTime(durationMs) else "00:00",
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
                                videoViewRef?.seekTo(currentPositionMs)
                            },
                            valueRange = 0f..durationMs.toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = NewsGold,
                                activeTrackColor = NewsRedPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().height(24.dp)
                        )
                    }
                }
            }
        }
    }

    DisposableEffect(videoUri) {
        onDispose {
            try {
                videoViewRef?.stopPlayback()
                mediaPlayerRef?.release()
                mediaPlayerRef = null
                videoViewRef = null
            } catch (_: Exception) {}
        }
    }
}

private fun formatTime(millis: Int): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
