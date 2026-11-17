package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NewsProject
import com.example.ui.theme.NewsAmber
import com.example.ui.theme.NewsGold
import com.example.ui.theme.NewsRedPrimary
import kotlin.math.roundToInt

/**
 * Professional KineMaster-style Multi-Layer Timeline with Draggable Playhead,
 * Scissor Trimming/Splitting, Track Selection, and Time Ruler
 */
@Composable
fun KinemasterTimelineView(
    project: NewsProject,
    currentPositionMs: Long,
    totalDurationMs: Long,
    selectedTrack: String, // "MAIN_VIDEO", "LAYER_OVERLAY", "AUDIO", "HANDWRITING"
    isTimelineExpanded: Boolean = false,
    onSelectTrack: (String) -> Unit,
    onSeekTo: (Long) -> Unit,
    onTrimLeft: () -> Unit,
    onSplitAtPlayhead: () -> Unit,
    onTrimRight: () -> Unit,
    onOpenToolSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val durationSafe = totalDurationMs.coerceAtLeast(1000L)
    val progress = (currentPositionMs.toFloat() / durationSafe).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0B101E))
            .border(1.dp, Color(0x33FFFFFF))
            .testTag("kinemaster_timeline")
    ) {
        // ================= TOP ACTION BAR (Scissors & Playhead Timestamp) =================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF131D31))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timestamp: 00:03.45 / 00:10.00
            val curSec = currentPositionMs / 1000
            val curMs = (currentPositionMs % 1000) / 10
            val totSec = durationSafe / 1000
            val totMs = (durationSafe % 1000) / 10
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.Red, CircleShape)
                )
                Text(
                    text = "%02d:%02d.%02d / %02d:%02d.%02d".format(
                        curSec / 60, curSec % 60, curMs,
                        totSec / 60, totSec % 60, totMs
                    ),
                    color = NewsGold,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            // KineMaster Scissor Action Buttons (कैंची से काटें)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Trim Left
                OutlinedButton(
                    onClick = onTrimLeft,
                    modifier = Modifier.height(28.dp),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.ContentCut, contentDescription = null, tint = NewsAmber, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("बाएं काटें", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                // Split at playhead
                OutlinedButton(
                    onClick = onSplitAtPlayhead,
                    modifier = Modifier.height(28.dp),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.ContentCut, contentDescription = null, tint = Color.Cyan, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("बीच से काटें", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                // Trim Right
                OutlinedButton(
                    onClick = onTrimRight,
                    modifier = Modifier.height(28.dp),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.ContentCut, contentDescription = null, tint = NewsAmber, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("दाएं काटें", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                // Selected Track Properties Inspector
                IconButton(
                    onClick = onOpenToolSheet,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "टूल्स", tint = NewsGold, modifier = Modifier.size(16.dp))
                }
            }
        }

        // ================= TIME RULER & MULTI-TRACK CONTAINER =================
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isTimelineExpanded) 195.dp else 148.dp)
                .background(Color(0xFF070B14))
        ) {
            val totalWidthPx = constraints.maxWidth.toFloat()
            val playheadX = (totalWidthPx * progress).coerceIn(0f, totalWidthPx)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(totalDurationMs) {
                        detectTapGestures { offset ->
                            val tappedRatio = (offset.x / totalWidthPx).coerceIn(0f, 1f)
                            onSeekTo((tappedRatio * durationSafe).toLong())
                        }
                    }
            ) {
                // 1. Time Ruler (00:00, 00:02, 00:04, 00:06, 00:08, 00:10...)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .background(Color(0xFF0F172A))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val numTicks = 10
                        val step = size.width / numTicks
                        for (i in 0..numTicks) {
                            val x = i * step
                            drawLine(
                                color = Color.Gray,
                                start = Offset(x, size.height - 6f),
                                end = Offset(x, size.height),
                                strokeWidth = 1f
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("00:00", color = Color.LightGray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        Text("00:02", color = Color.LightGray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        Text("00:04", color = Color.LightGray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        Text("00:06", color = Color.LightGray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        Text("00:08", color = Color.LightGray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        Text("00:10", color = Color.LightGray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                // 2. Track 1: Primary Video / Footage Track
                val isVideoSelected = selectedTrack == "MAIN_VIDEO"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF1565C0), Color(0xFF0D47A1))
                            )
                        )
                        .border(
                            width = if (isVideoSelected) 2.dp else 0.5.dp,
                            color = if (isVideoSelected) NewsGold else Color(0x44FFFFFF),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable { onSelectTrack("MAIN_VIDEO") }
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Text(
                            text = if (project.mediaUri != null) "🎬 मुख्य न्यूज़ वीडियो क्लिप (${project.videoSpeed}x)" else "🏛️ वर्चुअल टीवी स्टूडियो बैकड्रॉप",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    // Trim Start/End indicators
                    if (project.videoTrimStart > 0f || project.videoTrimEnd < 1f) {
                        Text(
                            text = "कट: ${(project.videoTrimStart * 100).toInt()}% - ${(project.videoTrimEnd * 100).toInt()}%",
                            color = NewsAmber,
                            fontSize = 9.sp,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }

                // 3. Track 2: TV Graphics & Headline Layer Track
                val isLayerSelected = selectedTrack == "LAYER_OVERLAY"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFFE65100), Color(0xFFC62828))
                            )
                        )
                        .border(
                            width = if (isLayerSelected) 2.dp else 0.5.dp,
                            color = if (isLayerSelected) NewsGold else Color(0x44FFFFFF),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable { onSelectTrack("LAYER_OVERLAY") }
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Text(
                            text = "📺 हेडलाइन: ${project.headline.take(24)}... + टिकर",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                // 4. Track 3: Audio / Sound & Voiceover Track
                val isAudioSelected = selectedTrack == "AUDIO"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF4A148C), Color(0xFF7B1FA2))
                            )
                        )
                        .border(
                            width = if (isAudioSelected) 2.dp else 0.5.dp,
                            color = if (isAudioSelected) NewsGold else Color(0x44FFFFFF),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable { onSelectTrack("AUDIO") }
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Audiotrack, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Text(
                            text = "🎵 ऑडियो ${(project.audioVolumeBoost * 100).toInt()}% • वॉइस: ${project.voiceChangerEffect}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                // 5. Track 4 (Shown when expanded or Handwriting / Keyframe active)
                if (isTimelineExpanded || project.handwritingStrokes.isNotBlank() || project.keyframeEnabled) {
                    val isFxTrackSelected = selectedTrack == "HANDWRITING"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFF00695C), Color(0xFF004D40))
                                )
                            )
                            .border(
                                width = if (isFxTrackSelected) 2.dp else 0.5.dp,
                                color = if (isFxTrackSelected) NewsGold else Color(0x44FFFFFF),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { onSelectTrack("HANDWRITING") }
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        val badgeInfo = buildString {
                            append("✍️ हैंडराइटिंग/कीफ़्रेम लेयर")
                            if (project.keyframeEnabled) append(" • 🔑 Keyframe सक्रिय")
                            if (project.chromaKeyEnabled) append(" • 🟩 क्रोमा की")
                        }
                        Text(
                            text = badgeInfo,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // ================= KINEMASTER RED PLAYHEAD CURSOR =================
            // Draggable vertical red line with top indicator
            Box(
                modifier = Modifier
                    .offset { IntOffset(playheadX.roundToInt(), 0) }
                    .fillMaxHeight()
                    .width(18.dp)
                    .pointerInput(totalDurationMs) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val newX = (playheadX + dragAmount.x).coerceIn(0f, totalWidthPx)
                            val newRatio = newX / totalWidthPx
                            onSeekTo((newRatio * durationSafe).toLong())
                        }
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                // Red playhead vertical needle
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(Color.Red)
                )

                // Top triangular needle head
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(Color.Red, RoundedCornerShape(2.dp))
                        .border(1.dp, Color.White, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}
