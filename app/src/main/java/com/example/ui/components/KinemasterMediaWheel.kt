package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NewsAmber
import com.example.ui.theme.NewsGold
import com.example.ui.theme.NewsRedPrimary

/**
 * Authentic KineMaster Circular Control Wheel
 * Featuring Media, Layer, Audio, Rec, and Center Play/Pause Control
 */
@Composable
fun KinemasterMediaWheel(
    isPlaying: Boolean,
    isVoiceRecording: Boolean,
    onTogglePlayPause: () -> Unit,
    onOpenMediaPicker: () -> Unit,
    onOpenLayerMenu: () -> Unit,
    onOpenAudioSoundboard: () -> Unit,
    onToggleVoiceRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wheelPulse")
    val recordingGlow by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recordingGlow"
    )

    Box(
        modifier = modifier
            .size(190.dp)
            .shadow(12.dp, CircleShape)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF263238), Color(0xFF0F172A), Color(0xFF050811))
                )
            )
            .border(2.dp, NewsGold.copy(alpha = 0.85f), CircleShape)
            .testTag("kinemaster_wheel"),
        contentAlignment = Alignment.Center
    ) {
        // Subtle dividing cross lines of KineMaster wheel
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = this.center
            drawLine(
                color = Color(0x33FFFFFF),
                start = androidx.compose.ui.geometry.Offset(center.x, 0f),
                end = androidx.compose.ui.geometry.Offset(center.x, size.height),
                strokeWidth = 1.2f
            )
            drawLine(
                color = Color(0x33FFFFFF),
                start = androidx.compose.ui.geometry.Offset(0f, center.y),
                end = androidx.compose.ui.geometry.Offset(size.width, center.y),
                strokeWidth = 1.2f
            )
        }

        // 1. TOP: MEDIA (मीडिया - Video & Photos)
        WheelSectorButton(
            title = "मीडिया",
            subtitle = "Media",
            icon = Icons.Default.VideoLibrary,
            color = Color(0xFF0288D1),
            onClick = onOpenMediaPicker,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 8.dp)
        )

        // 2. RIGHT: LAYER (लेयर - Text, Stickers, Overlays)
        WheelSectorButton(
            title = "लेयर",
            subtitle = "Layer",
            icon = Icons.Default.Layers,
            color = Color(0xFFE65100),
            onClick = onOpenLayerMenu,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-8).dp)
        )

        // 3. BOTTOM: AUDIO (ऑडियो - BGM, Jingles, Mic)
        WheelSectorButton(
            title = "ऑडियो",
            subtitle = "Audio",
            icon = Icons.Default.Audiotrack,
            color = Color(0xFF7B1FA2),
            onClick = onOpenAudioSoundboard,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-8).dp)
        )

        // 4. LEFT: REC / VOICEOVER (आवाज़ रिकॉर्डिंग)
        WheelSectorButton(
            title = if (isVoiceRecording) "रोकें" else "रिकॉर्ड",
            subtitle = "Rec",
            icon = if (isVoiceRecording) Icons.Default.Stop else Icons.Default.Mic,
            color = if (isVoiceRecording) Color.Red.copy(alpha = recordingGlow) else Color(0xFFC2185B),
            onClick = onToggleVoiceRecording,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 8.dp)
        )

        // 5. CENTER: PLAY / PAUSE BUTTON (बीच का प्ले बटन)
        Box(
            modifier = Modifier
                .size(62.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isPlaying) Color(0xFFB71C1C) else Color(0xFF1E88E5),
                            Color(0xFF0D47A1),
                            Color(0xFF071228)
                        )
                    )
                )
                .border(2.5.dp, if (isPlaying) Color.Red else NewsGold, CircleShape)
                .clickable { onTogglePlayPause() }
                .testTag("wheel_play_pause"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "रोकें" else "चलाएं",
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

@Composable
private fun WheelSectorButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color, CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp
        )
    }
}
