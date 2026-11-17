package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.CameraRear
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.R
import com.example.camera.CameraPreviewView
import com.example.ui.components.AnimatedNewsBackground
import com.example.ui.components.HEADLINE_ANIMATION_OPTIONS
import com.example.ui.components.KinemasterClipEditorSheet
import com.example.ui.components.KinemasterHandwritingCanvas
import com.example.ui.components.KinemasterKeyframeDialog
import com.example.ui.components.KinemasterLayerMenu
import com.example.ui.components.KinemasterMediaWheel
import com.example.ui.components.KinemasterSideBar
import com.example.ui.components.KinemasterTimelineView
import com.example.ui.components.NewsAnimationTheme
import com.example.ui.components.NewsAudioSoundboard
import com.example.ui.components.NewsOverlayFrame
import com.example.ui.components.NewsVideoPlayer
import com.example.ui.components.ScreenLayoutCustomizerDialog
import com.example.ui.components.TICKER_ANIMATION_OPTIONS
import com.example.ui.components.VideoEditingToolsBar
import com.example.util.GalleryMediaSaver
import com.example.ui.theme.NewsAmber
import com.example.ui.theme.NewsCardBg
import com.example.ui.theme.NewsDeepNavy
import com.example.ui.theme.NewsGold
import com.example.ui.theme.NewsRedDark
import com.example.ui.theme.NewsRedPrimary
import com.example.viewmodel.BackgroundSource
import com.example.viewmodel.NewsStudioViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(
    viewModel: NewsStudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentProject by viewModel.currentProject.collectAsState()
    val backgroundSource by viewModel.backgroundSource.collectAsState()
    val customMediaUri by viewModel.customMediaUri.collectAsState()
    val isFrontCamera by viewModel.isFrontCamera.collectAsState()
    val isTorchEnabled by viewModel.isTorchEnabled.collectAsState()
    val isTeleprompterActive by viewModel.isTeleprompterActive.collectAsState()
    val teleprompterSpeed by viewModel.teleprompterSpeed.collectAsState()
    val teleprompterFontSize by viewModel.teleprompterFontSize.collectAsState()
    val isBreakingFlashActive by viewModel.isBreakingFlashActive.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val recordingSeconds by viewModel.recordingSeconds.collectAsState()
    val exportProgress by viewModel.exportProgress.collectAsState()
    val isVideoPlaying by viewModel.isVideoPlaying.collectAsState()

    // KineMaster Editing Suite States
    val timelinePositionMs by viewModel.timelinePositionMs.collectAsState()
    val timelineDurationMs by viewModel.timelineDurationMs.collectAsState()
    val selectedTrack by viewModel.selectedTrack.collectAsState()
    val isKinemasterInspectorOpen by viewModel.isKinemasterInspectorOpen.collectAsState()
    val kinemasterInspectorTab by viewModel.kinemasterInspectorTab.collectAsState()
    val isVoiceRecording by viewModel.isVoiceRecording.collectAsState()
    val isTimelineExpanded by viewModel.isTimelineExpanded.collectAsState()

    var showKinemasterLayerMenu by remember { mutableStateOf(false) }
    var isHandwritingActive by remember { mutableStateOf(false) }
    var showKeyframeDialog by remember { mutableStateOf(false) }
    var showQuickTextDialog by remember { mutableStateOf(false) }

    // Gallery picker for KineMaster media wheel
    val kinemasterMediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.setVideoUri(uri)
        }
    }

    // PIP Secondary Media picker for KineMaster Layer -> Media
    val kinemasterPipMediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.setSecondaryMediaUri(uri)
            viewModel.updateLayoutMode("PIP_CORNER")
        }
    }

    var showOverlaySettingsDialog by remember { mutableStateOf(false) }
    var showTeleprompterEditor by remember { mutableStateOf(false) }
    var showScreenLayoutDialog by remember { mutableStateOf(false) }

    // Camera permission request launcher
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) {
            viewModel.setBackgroundSource(BackgroundSource.CAMERA)
        }
    }

    // Audio recording permission launcher for KineMaster Voiceover Mic
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted) {
            viewModel.toggleVoiceRecording(context)
        }
    }

    val selectedAnimationTheme by viewModel.selectedAnimationTheme.collectAsState()
    val savedGalleryUri by viewModel.savedGalleryUri.collectAsState()
    val isSavingToGallery by viewModel.isSavingToGallery.collectAsState()

    // Video picker launcher (Zero-permission Android Photo Picker for Videos)
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.setVideoUri(uri)
        }
    }

    // Media picker launcher (Zero-permission Android Photo Picker for Image/Video)
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.setCustomMediaUri(uri)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ================= TOP CANVAS PREVIEW =================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("studio_preview_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // News Overlay Frame
                    val timelineProg = (timelinePositionMs.toFloat() / timelineDurationMs.coerceAtLeast(1L)).coerceIn(0f, 1f)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        NewsOverlayFrame(
                            project = currentProject,
                            isBreakingFlashActive = isBreakingFlashActive,
                            showTeleprompter = isTeleprompterActive,
                            teleprompterSpeed = teleprompterSpeed,
                            teleprompterFontSize = teleprompterFontSize,
                            timelineProgress = timelineProg,
                            onUpdateElementOffset = { element, ox, oy ->
                                viewModel.updateElementDragOffset(element, ox, oy)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            when (backgroundSource) {
                                BackgroundSource.CAMERA -> {
                                    if (hasCameraPermission) {
                                        CameraPreviewView(
                                            isFrontCamera = isFrontCamera,
                                            isTorchEnabled = isTorchEnabled,
                                            isRecording = isRecording,
                                            onRecordingFinished = { capturedUri ->
                                                viewModel.onLiveCameraRecordingFinished(context, capturedUri)
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color(0xFF1E293B)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "कैमरा अनुमति आवश्यक है",
                                                    color = Color.White,
                                                    fontSize = 12.sp
                                                )
                                                Button(
                                                    onClick = {
                                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = NewsRedPrimary
                                                    )
                                                ) {
                                                    Text("अनुमति दें", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }

                                BackgroundSource.GALLERY_MEDIA -> {
                                    if (customMediaUri != null) {
                                        val uriString = customMediaUri.toString()
                                        val mimeType = context.contentResolver.getType(customMediaUri!!)
                                        val isImageOnly = (mimeType?.startsWith("image") == true) ||
                                                uriString.endsWith(".jpg", ignoreCase = true) ||
                                                uriString.endsWith(".jpeg", ignoreCase = true) ||
                                                uriString.endsWith(".png", ignoreCase = true) ||
                                                uriString.endsWith(".webp", ignoreCase = true)
                                        if (isImageOnly) {
                                            AsyncImage(
                                                model = customMediaUri,
                                                contentDescription = "गैलरी मीडिया",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            NewsVideoPlayer(
                                                videoUri = customMediaUri!!,
                                                isPlaying = isVideoPlaying,
                                                speed = currentProject.videoSpeed,
                                                volume = currentProject.videoVolume,
                                                onPlaybackStateChange = { viewModel.setVideoPlaying(it) },
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    } else {
                                        VirtualStudioFallback()
                                    }
                                }

                                BackgroundSource.VIRTUAL_STUDIO -> {
                                    AnimatedNewsBackground(
                                        theme = selectedAnimationTheme,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }

                        // Recording Badge in preview
                        if (isRecording) {
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .background(Color.Red, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .align(Alignment.BottomEnd)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color.White, CircleShape)
                                    )
                                    Text(
                                        text = "REC %02d:%02d".format(
                                            recordingSeconds / 60,
                                            recordingSeconds % 60
                                        ),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        // KineMaster Handwriting Finger-Drawing Overlay Canvas
                        KinemasterHandwritingCanvas(
                            isActive = isHandwritingActive,
                            onSaveAndClose = { strokeCount ->
                                isHandwritingActive = false
                                viewModel.saveHandwritingStrokes(strokeCount)
                            },
                            onClearAll = {
                                viewModel.clearHandwriting()
                            },
                            modifier = Modifier.matchParentSize()
                        )

                        // KineMaster Vertical Left Action Toolbar
                        KinemasterSideBar(
                            isTimelineExpanded = isTimelineExpanded,
                            onUndo = { viewModel.undoAction() },
                            onRedo = { viewModel.redoAction() },
                            onCaptureFrame = { viewModel.captureFrame(context) },
                            onOpenKeyframe = { showKeyframeDialog = true },
                            onDuplicateLayer = { viewModel.duplicateCurrentLayer() },
                            onDeleteLayer = { viewModel.deleteCurrentLayer() },
                            onToggleExpandTimeline = { viewModel.toggleTimelineExpanded() },
                            onJumpToBeginning = { viewModel.seekTimelineTo(0L) },
                            onJumpToEnd = { viewModel.seekTimelineTo(timelineDurationMs) },
                            onOpenSettings = { showScreenLayoutDialog = true },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 6.dp)
                        )
                    }

                    // Canvas Quick Bar (Aspect Ratio, Resolution/FPS Badge, Camera Controls, Layers)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Aspect Presets (16:9, 9:16, 1:1, 4:5)
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            listOf("16:9", "9:16", "1:1", "4:5").forEach { aspect ->
                                val selected = currentProject.aspectPreset == aspect
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (selected) NewsRedPrimary else Color(0xFF1E293B)
                                        )
                                        .clickable { viewModel.updateAspectPreset(aspect) }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = aspect,
                                        color = if (selected) Color.White else NewsAmber,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Camera controls (if in camera mode)
                        if (backgroundSource == BackgroundSource.CAMERA) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { viewModel.toggleCamera() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFrontCamera) Icons.Default.CameraFront else Icons.Default.CameraRear,
                                        contentDescription = "कैमरा बदलें",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.toggleTorch() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                        contentDescription = "फ़्लैश",
                                        tint = if (isTorchEnabled) NewsGold else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Layer settings button
                        IconButton(
                            onClick = { showOverlaySettingsDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "लेयर्स",
                                tint = NewsGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Screen Layout Customizer button (स्क्रीन जमाएं)
                        Button(
                            onClick = { showScreenLayoutDialog = true },
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("btn_arrange_screen"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DashboardCustomize,
                                contentDescription = null,
                                tint = NewsGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "स्क्रीन जमाएं",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Finger Drag & Set Guidance Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "👆 उंगली से लोगो, हेडलाइन, टिकर, LIVE, स्टिकर को जहाँ चाहें सेट करें",
                                color = NewsAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        Button(
                            onClick = { viewModel.resetAllElementPositions() },
                            modifier = Modifier.height(26.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Text("🔄 रीसेट", color = Color.White, fontSize = 9.sp)
                        }
                    }
                }
            }
        }

        // ================= KINEMASTER MULTI-LAYER TIMELINE =================
        item {
            KinemasterTimelineView(
                project = currentProject,
                currentPositionMs = timelinePositionMs,
                totalDurationMs = timelineDurationMs,
                selectedTrack = selectedTrack,
                isTimelineExpanded = isTimelineExpanded,
                onSelectTrack = { track -> viewModel.setSelectedTrack(track) },
                onSeekTo = { posMs -> viewModel.seekTimelineTo(posMs) },
                onTrimLeft = { viewModel.trimTimelineLeft() },
                onSplitAtPlayhead = { viewModel.splitTimelineAtPlayhead() },
                onTrimRight = { viewModel.trimTimelineRight() },
                onOpenToolSheet = { viewModel.openKinemasterToolTab(0) },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            )
        }

        // ================= KINEMASTER CIRCULAR MEDIA WHEEL & ALL PRO SERVICES =================
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("kinemaster_wheel_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                border = androidx.compose.foundation.BorderStroke(1.dp, NewsGold.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // KineMaster Circular Control Wheel
                        KinemasterMediaWheel(
                            isPlaying = isVideoPlaying,
                            isVoiceRecording = isVoiceRecording,
                            onTogglePlayPause = { viewModel.toggleVideoPlayback() },
                            onOpenMediaPicker = {
                                kinemasterMediaPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                )
                            },
                            onOpenLayerMenu = { showKinemasterLayerMenu = true },
                            onOpenAudioSoundboard = {
                                viewModel.setSelectedTrack("AUDIO")
                                viewModel.openKinemasterToolTab(3)
                            },
                            onToggleVoiceRecording = {
                                if (hasAudioPermission) {
                                    viewModel.toggleVoiceRecording(context)
                                } else {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        )

                        // Right Side: Authentic KineMaster Inspector Tool Grid
                        Column(
                            modifier = Modifier.weight(1f).padding(start = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "काइनमास्टर सेवाएं (Pro Tools)",
                                    color = NewsGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "${currentProject.exportResolution}•${currentProject.exportFps}fps",
                                    color = Color.Cyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF1E293B))
                                        .clickable { viewModel.openKinemasterToolTab(10) }
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }

                            // 2-column grid of KineMaster core services
                            val kmTools = listOf(
                                Triple("✂️ ट्रिम/कट", 0, currentProject.videoTrimStart > 0f || currentProject.videoTrimEnd < 1f),
                                Triple("🟩 क्रोमा की", 1, currentProject.chromaKeyEnabled),
                                Triple("🔍 पैन व ज़ूम", 2, currentProject.panZoomStartScale != 1f || currentProject.panZoomEndScale != 1f),
                                Triple("🔊 ऑडियो ${(currentProject.audioVolumeBoost * 100).toInt()}%", 3, currentProject.audioVolumeBoost > 1f),
                                Triple("🎙️ आवाज़ बदलें", 4, currentProject.voiceChangerEffect != "NORMAL"),
                                Triple("⚡ स्पीड ${currentProject.videoSpeed}x", 5, currentProject.videoSpeed != 1f),
                                Triple("✨ एनिमेशन", 6, currentProject.inAnimation != "NONE"),
                                Triple("🎞️ 3D ट्रांज़िशन", 7, currentProject.activeTransition != "NONE"),
                                Triple("🎨 कलर ग्रेड", 8, currentProject.videoContrast != 1f || currentProject.videoBrightness != 0f),
                                Triple("🔑 कीफ़्रेम", 9, currentProject.keyframeEnabled)
                            )

                            kmTools.chunked(2).forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    pair.forEach { (label, tabIdx, isActive) ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isActive) Color(0xFFB71C1C) else Color(0xFF1E293B))
                                                .border(
                                                    width = 0.7.dp,
                                                    color = if (isActive) NewsGold else Color(0x33FFFFFF),
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .clickable {
                                                    if (tabIdx == 9) {
                                                        showKeyframeDialog = true
                                                    } else {
                                                        viewModel.openKinemasterToolTab(tabIdx)
                                                    }
                                                }
                                                .padding(vertical = 7.dp, horizontal = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isActive) Color.White else Color.LightGray,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }

                            // Handwriting & Layer Menu quick row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isHandwritingActive) Color(0xFFD81B60) else Color(0xFF1E293B))
                                        .border(0.7.dp, NewsAmber.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                        .clickable { isHandwritingActive = !isHandwritingActive }
                                        .padding(vertical = 7.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isHandwritingActive) "✍️ पेन चालू" else "✍️ हैंडराइटिंग",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF0D47A1))
                                        .border(0.7.dp, NewsGold.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                                        .clickable { showKinemasterLayerMenu = true }
                                        .padding(vertical = 7.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "📑 5 लेयर मेनू",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ================= KINEMASTER CLIP INSPECTOR SHEET (INLINE) =================
        if (isKinemasterInspectorOpen) {
            item {
                KinemasterClipEditorSheet(
                    project = currentProject,
                    selectedTrack = selectedTrack,
                    initialTab = kinemasterInspectorTab,
                    onClose = { viewModel.setKinemasterInspectorOpen(false) },
                    onTrimLeft = { viewModel.trimTimelineLeft() },
                    onSplitAtPlayhead = { viewModel.splitTimelineAtPlayhead() },
                    onTrimRight = { viewModel.trimTimelineRight() },
                    onUpdateChromaKey = { enabled, color, similarity ->
                        viewModel.updateChromaKey(enabled, color, similarity)
                    },
                    onUpdateVoiceChanger = { effect ->
                        viewModel.updateVoiceChanger(effect)
                    },
                    onUpdateTransition = { transition ->
                        viewModel.updateTransition(transition)
                    },
                    onUpdateAnimation = { inAnim, outAnim ->
                        viewModel.updateAnimation(inAnim, outAnim)
                    },
                    onUpdateSpeed = { speed ->
                        viewModel.updateSpeedAndTrim(speed, currentProject.videoTrimStart, currentProject.videoTrimEnd, currentProject.videoVolume)
                    },
                    onUpdateContrastAndBrightness = { contrast, brightness ->
                        viewModel.updateContrastAndBrightness(contrast, brightness)
                    },
                    onUpdatePanZoom = { startScale, endScale, locked ->
                        viewModel.updatePanZoom(startScale, endScale, locked)
                    },
                    onUpdateAudioMixer = { volumeBoost, pitch, ducking ->
                        viewModel.updateAudioMixer(volumeBoost, pitch, ducking)
                    },
                    onToggleAudioFade = { fadeIn, fadeOut ->
                        viewModel.toggleAudioFade(fadeIn, fadeOut)
                    },
                    onUpdateExportSettings = { res, fps ->
                        viewModel.updateExportSettings(res, fps)
                    },
                    onOpenKeyframeDialog = {
                        showKeyframeDialog = true
                    }
                )
            }
        }

        // ================= DYNAMIC USER-ORDERED STUDIO SECTIONS =================
        val hiddenSectionsSet = currentProject.hiddenSections
            .split(",")
            .filter { it.isNotBlank() }
            .toSet()
        val orderedSections = currentProject.studioSectionsOrder
            .split(",")
            .filter { it.isNotBlank() && !hiddenSectionsSet.contains(it) }

        orderedSections.forEach { sectionId ->
            when (sectionId) {
                "VIDEO" -> item(key = "section_video") {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("video_add_play_card"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VideoLibrary,
                                        contentDescription = null,
                                        tint = NewsGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "वीडियो जोड़ें और प्ले करें (Video Player Controls)",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                if (customMediaUri != null && backgroundSource == BackgroundSource.GALLERY_MEDIA) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isVideoPlaying) Color(0xFF2E7D32) else NewsRedPrimary,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isVideoPlaying) "▶ प्ले हो रहा है" else "⏸ रुका हुआ",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // "वीडियो जोड़ें (Add Video)" button
                                Button(
                                    onClick = {
                                        videoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                        )
                                    },
                                    modifier = Modifier.weight(1.2f).height(42.dp).testTag("btn_add_video"),
                                    colors = ButtonDefaults.buttonColors(containerColor = NewsRedPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("वीडियो जोड़ें (Add Video)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // "प्ले / पॉज (Play / Pause)" toggle button
                                Button(
                                    onClick = {
                                        if (customMediaUri == null || backgroundSource != BackgroundSource.GALLERY_MEDIA) {
                                            videoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                            )
                                        } else {
                                            viewModel.toggleVideoPlayback()
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(42.dp).testTag("btn_play_pause_video"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isVideoPlaying && customMediaUri != null && backgroundSource == BackgroundSource.GALLERY_MEDIA)
                                            Color(0xFF2E7D32) else Color(0xFF1565C0)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isVideoPlaying && customMediaUri != null && backgroundSource == BackgroundSource.GALLERY_MEDIA)
                                            Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isVideoPlaying && customMediaUri != null && backgroundSource == BackgroundSource.GALLERY_MEDIA)
                                            "रोकें (Pause)" else "चलाएं (Play)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                "RECORD" -> item(key = "section_record") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Record Button
                            Button(
                                onClick = { viewModel.toggleRecording(context) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("btn_record_toggle"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isRecording) Color.Red else Color(0xFFC62828)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (isRecording) "रोकें (%02d:%02d)".format(
                                            recordingSeconds / 60,
                                            recordingSeconds % 60
                                        ) else "लाइव रिकॉर्डिंग शुरू",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Save Video with Frame Button (Fast 2-3s)
                            Button(
                                onClick = { viewModel.saveToGallery(context, asVideo = true) },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(44.dp)
                                    .testTag("btn_save_video_with_frame"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1565C0)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = null,
                                        tint = NewsGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (isSavingToGallery) "⚡ 2s में सेव..." else "⚡ फ़ास्ट वीडियो सेव",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Save Poster Button
                            Button(
                                onClick = { viewModel.saveToGallery(context, asVideo = false) },
                                modifier = Modifier
                                    .weight(0.7f)
                                    .height(44.dp)
                                    .testTag("btn_save_poster"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1E293B)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "पोस्टर",
                                    color = NewsGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }

                            // Quick Save Project Button
                            IconButton(
                                onClick = { viewModel.saveCurrentProject() },
                                modifier = Modifier
                                    .height(44.dp)
                                    .width(44.dp)
                                    .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                    .testTag("btn_save_project")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "सहेजें",
                                    tint = NewsGold
                                )
                            }
                        }

                        // Instant 1-second Direct Save Option
                        if (customMediaUri != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = { viewModel.saveToGallery(context, asVideo = true, instantDirectSave = true) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .testTag("btn_instant_fast_save"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = NewsGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "⚡ तुरंत 1 सेकंड में डायरेक्ट गैलरी में सेव करें",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        // Export Progress Bar
                        if (exportProgress != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = { exportProgress ?: 0f },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = NewsGold,
                                    trackColor = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "एचडी न्यूज़ वीडियो रेंडर हो रहा है: ${(exportProgress!! * 100).toInt()}%",
                                    color = NewsAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                "SOURCE" -> item(key = "section_source") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF162035))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "बैकग्राउंड मीडिया स्रोत (Background Source)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = backgroundSource == BackgroundSource.VIRTUAL_STUDIO,
                                    onClick = { viewModel.setBackgroundSource(BackgroundSource.VIRTUAL_STUDIO) },
                                    label = { Text("वर्चुअल स्टूडियो", fontSize = 11.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(14.dp))
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NewsRedPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                FilterChip(
                                    selected = backgroundSource == BackgroundSource.CAMERA,
                                    onClick = {
                                        if (hasCameraPermission) {
                                            viewModel.setBackgroundSource(BackgroundSource.CAMERA)
                                        } else {
                                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    },
                                    label = { Text("लाइव कैमरा", fontSize = 11.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NewsRedPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                FilterChip(
                                    selected = backgroundSource == BackgroundSource.GALLERY_MEDIA,
                                    onClick = {
                                        mediaPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                        )
                                    },
                                    label = { Text("गैलरी से चुनें", fontSize = 11.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp))
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NewsRedPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (backgroundSource == BackgroundSource.VIRTUAL_STUDIO) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "एनीमेशन वीडियो बैकग्राउंड थीम्स:",
                                    color = NewsAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(
                                        NewsAnimationTheme.GLOBE_SPIN to "🌐 3D ग्लोब",
                                        NewsAnimationTheme.BREAKING_BEAMS to "⚡ ब्रेकिंग बीम्स",
                                        NewsAnimationTheme.DIGITAL_STUDIO to "💻 डिजिटल न्यूज़",
                                        NewsAnimationTheme.GOLDEN_LUXURY to "✨ गोल्डन बुलेटिन"
                                    ).forEach { (theme, label) ->
                                        val selected = selectedAnimationTheme == theme
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (selected) NewsRedPrimary else Color(0xFF0F172A))
                                                .clickable { viewModel.setAnimationTheme(theme) }
                                                .padding(vertical = 5.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (selected) Color.White else Color.Gray,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "TOOLS" -> item(key = "section_tools") {
                    VideoEditingToolsBar(
                        project = currentProject,
                        onUpdateLogoConfig = { type, uri, pos, scale, opacity ->
                            viewModel.updateLogoConfig(type, uri, pos, scale, opacity)
                        },
                        onUpdateContrastAndBrightness = { contrast, brightness ->
                            viewModel.updateContrastAndBrightness(contrast, brightness)
                        },
                        onUpdateFilterAndVfx = { filter, vfx ->
                            viewModel.updateFilterAndVfx(filter, vfx)
                        },
                        onUpdateSpeedAndTrim = { speed, trimStart, trimEnd, volume ->
                            viewModel.updateSpeedAndTrim(speed, trimStart, trimEnd, volume)
                        },
                        onUpdateLayoutMode = { mode ->
                            viewModel.updateLayoutMode(mode)
                        },
                        onToggleSticker = { tag ->
                            viewModel.toggleSticker(tag)
                        },
                        onPickSecondaryMedia = { uri ->
                            viewModel.setSecondaryMediaUri(uri)
                        }
                    )
                }

                "SOUND" -> item(key = "section_sound") {
                    NewsAudioSoundboard(
                        onTriggerBreakingEffect = {
                            viewModel.triggerBreakingFlash()
                        }
                    )
                }

                "PROMPTER" -> item(key = "section_prompter") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF162035))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RecordVoiceOver,
                                        contentDescription = null,
                                        tint = NewsGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "टेलीप्रॉम्प्टर (समाचार वाचक सहायता)",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                Switch(
                                    checked = isTeleprompterActive,
                                    onCheckedChange = { viewModel.setTeleprompterActive(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = NewsRedPrimary
                                    )
                                )
                            }

                            if (isTeleprompterActive) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "स्क्रॉल गति: ${"%.1f".format(teleprompterSpeed)}x",
                                            color = NewsAmber,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "फॉन्ट साइज़: ${teleprompterFontSize.toInt()}sp",
                                            color = Color.White,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Slider(
                                        value = teleprompterSpeed,
                                        onValueChange = { viewModel.setTeleprompterSpeed(it) },
                                        valueRange = 0.5f..2.5f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = NewsGold,
                                            activeTrackColor = NewsRedPrimary
                                        )
                                    )

                                    OutlinedButton(
                                        onClick = { showTeleprompterEditor = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("प्रॉम्प्टर स्क्रिप्ट संपादित करें / बदलें", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                "HEADLINE" -> item(key = "section_headline") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF162035))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "हेडलाइन एवं ब्रेकिंग पट्टी संपादन + ऐनिमेशन",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )

                            // Quick Category Chips
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("ब्रेकिंग न्यूज़", "विशेष ख़बर", "मुख्य समाचार", "सुपरफास्ट 100", "ग्राउंड रिपोर्ट").forEach { cat ->
                                    val selected = currentProject.category == cat
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (selected) NewsRedPrimary else Color(0xFF1E293B))
                                            .clickable { viewModel.updateCategory(cat) }
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = cat,
                                            color = if (selected) Color.White else NewsAmber,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Headline Input
                            OutlinedTextField(
                                value = currentProject.headline,
                                onValueChange = { viewModel.updateHeadline(it) },
                                label = { Text("मुख्य हेडलाइन (Hindi Headline)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_headline"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NewsGold,
                                    unfocusedBorderColor = Color(0x66FFFFFF),
                                    focusedLabelColor = NewsGold,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                maxLines = 3
                            )

                            // Headline Animation Selector
                            Text(
                                text = "✨ हेडलाइन ऐनिमेशन (Headline Animation):",
                                color = NewsGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                HEADLINE_ANIMATION_OPTIONS.forEach { (key, label) ->
                                    val selected = currentProject.headlineAnimation == key
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (selected) NewsRedPrimary else Color(0xFF1E293B))
                                            .border(0.6.dp, if (selected) NewsGold else Color.Transparent, RoundedCornerShape(6.dp))
                                            .clickable { viewModel.updateHeadlineAnimation(key) }
                                            .padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (selected) Color.White else Color.LightGray,
                                            fontSize = 10.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }

                            // Sub-headline Input
                            OutlinedTextField(
                                value = currentProject.subHeadline,
                                onValueChange = { viewModel.updateSubHeadline(it) },
                                label = { Text("उप-शीर्षक (Sub-headline)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_subheadline"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NewsAmber,
                                    unfocusedBorderColor = Color(0x66FFFFFF),
                                    focusedLabelColor = NewsAmber,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true
                            )

                            // Ticker Tag Input ("⚡ ताज़ा अपडेट")
                            OutlinedTextField(
                                value = currentProject.tickerTagText,
                                onValueChange = { viewModel.updateTickerAnimationAndSpeed(tagText = it) },
                                label = { Text("ब्रेकिंग पट्टी टैग (जैसे ⚡ ताज़ा अपडेट)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_ticker_tag"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NewsAmber,
                                    unfocusedBorderColor = Color(0x66FFFFFF),
                                    focusedLabelColor = NewsAmber,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true
                            )

                            // Ticker Input
                            OutlinedTextField(
                                value = currentProject.tickerText,
                                onValueChange = { viewModel.updateTicker(it) },
                                label = { Text("चलती पट्टी टेक्स्ट (Scrolling Ticker)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_ticker"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NewsGold,
                                    unfocusedBorderColor = Color(0x66FFFFFF),
                                    focusedLabelColor = NewsGold,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                maxLines = 3
                            )

                            // Breaking Ticker Animation Selector
                            Text(
                                text = "⚡ ब्रेकिंग न्यूज़ पट्टी ऐनिमेशन (Ticker Animation):",
                                color = NewsAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                TICKER_ANIMATION_OPTIONS.forEach { (key, label) ->
                                    val selected = currentProject.tickerAnimation == key
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (selected) NewsRedPrimary else Color(0xFF1E293B))
                                            .border(0.6.dp, if (selected) NewsGold else Color.Transparent, RoundedCornerShape(6.dp))
                                            .clickable { viewModel.updateTickerAnimationAndSpeed(anim = key) }
                                            .padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (selected) Color.White else Color.LightGray,
                                            fontSize = 10.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }

                            // Ticker Scroll Speed Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("पट्टी चलने की गति (Speed):", color = Color.LightGray, fontSize = 10.sp)
                                Text("${"%.1f".format(currentProject.tickerSpeed)}x", color = NewsGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = currentProject.tickerSpeed.coerceIn(0.4f, 2.5f),
                                onValueChange = { viewModel.updateTickerAnimationAndSpeed(speed = it) },
                                valueRange = 0.4f..2.5f,
                                colors = SliderDefaults.colors(
                                    thumbColor = NewsGold,
                                    activeTrackColor = NewsRedPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        // ================= QUICK TEMPLATES =================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "त्वरित ताज़ा ख़बरें (Quick Hindi Templates)",
                    color = NewsGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                viewModel.headlineTemplates.take(4).forEach { template ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.applyHeadlineTemplate(template) },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0x33FFFFFF))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = template.category,
                                    color = NewsAmber,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = template.headline,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "लागू करें",
                                tint = NewsGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // ================= OVERLAYS TOGGLE DIALOG =================
    if (showOverlaySettingsDialog) {
        AlertDialog(
            onDismissRequest = { showOverlaySettingsDialog = false },
            title = {
                Text("ऑन-स्क्रीन लेयर्स चालू/बंद करें", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LayerSwitchRow(
                        title = "चैनल लोगो (Channel Logo)",
                        checked = currentProject.showLogo,
                        onChecked = { viewModel.toggleOverlayElement(logo = it) }
                    )
                    LayerSwitchRow(
                        title = "LIVE सूचक (Live Badge)",
                        checked = currentProject.showLiveBadge,
                        onChecked = { viewModel.toggleOverlayElement(live = it) }
                    )
                    LayerSwitchRow(
                        title = "रिपोर्टर पट्टी (Reporter Lower Third)",
                        checked = currentProject.showReporterCard,
                        onChecked = { viewModel.toggleOverlayElement(reporter = it) }
                    )
                    LayerSwitchRow(
                        title = "हेडलाइन बॉक्स (Main Headline)",
                        checked = currentProject.showHeadline,
                        onChecked = { viewModel.toggleOverlayElement(headline = it) }
                    )
                    LayerSwitchRow(
                        title = "चलती पट्टी (Scrolling Ticker)",
                        checked = currentProject.showTicker,
                        onChecked = { viewModel.toggleOverlayElement(ticker = it) }
                    )
                    LayerSwitchRow(
                        title = "स्थान / जिला बैज (Location Chip)",
                        checked = currentProject.showLocation,
                        onChecked = { viewModel.toggleOverlayElement(location = it) }
                    )
                    LayerSwitchRow(
                        title = "ब्रेकिंग स्टिकर्स (Stickers Row)",
                        checked = currentProject.showStickers,
                        onChecked = { viewModel.toggleOverlayElement(stickers = it) }
                    )
                    LayerSwitchRow(
                        title = "घड़ी / समय (Time Clock)",
                        checked = currentProject.showClock,
                        onChecked = { viewModel.toggleOverlayElement(clock = it) }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            showOverlaySettingsDialog = false
                            showScreenLayoutDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NewsRedPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("🎛️ पूरा मैनुअल कंट्रोल खोलें", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOverlaySettingsDialog = false }) {
                    Text("पूर्ण", color = NewsGold, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // ================= TELEPROMPTER SCRIPT DIALOG =================
    if (showTeleprompterEditor) {
        var scriptText by remember { mutableStateOf(currentProject.teleprompterText) }
        AlertDialog(
            onDismissRequest = { showTeleprompterEditor = false },
            title = {
                Text("प्रॉम्प्टर स्क्रिप्ट संपादित करें", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "वाचक इसे स्क्रीन पर देखकर सहजता से बोल सकेंगे:",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    OutlinedTextField(
                        value = scriptText,
                        onValueChange = { scriptText = it },
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        maxLines = 8
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateTeleprompterText(scriptText)
                    showTeleprompterEditor = false
                }) {
                    Text("सेव करें", color = NewsGold, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTeleprompterEditor = false }) {
                    Text("रद्द करें")
                }
            }
        )
    }

    // ================= SAVED TO GALLERY SUCCESS DIALOG =================
    if (savedGalleryUri != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSavedGalleryDialog() },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "गैलरी में सुरक्षित सेव हुआ!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "आपकी H.K NEWS वीडियो / ब्रॉडकास्ट पोस्टर आपके फोन की गैलरी (Google Photos / Movies / Pictures) में सेव कर दी गई है।",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "हेडलाइन: ${currentProject.headline.take(30)}...",
                        color = NewsGold,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        GalleryMediaSaver.shareMedia(context, savedGalleryUri!!)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.IosShare, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("व्हाट्सएप पर शेयर करें", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissSavedGalleryDialog() }) {
                    Text("ठीक है (Done)", color = Color.White)
                }
            },
            containerColor = Color(0xFF131D31)
        )
    }

    // ================= SCREEN LAYOUT & ARRANGEMENT CUSTOMIZER DIALOG =================
    if (showScreenLayoutDialog) {
        ScreenLayoutCustomizerDialog(
            project = currentProject,
            initialTab = 0,
            onDismiss = { showScreenLayoutDialog = false },
            onReorderSection = { sectionId, moveUp ->
                viewModel.reorderSection(sectionId, moveUp)
            },
            onToggleSectionVisibility = { sectionId ->
                viewModel.toggleSectionVisibility(sectionId)
            },
            onResetSections = {
                viewModel.resetSectionsOrder()
            },
            onUpdateElementPosition = { element, pos ->
                viewModel.updateElementPosition(element, pos)
            },
            onUpdateLogoScale = { scale ->
                viewModel.updateLogoConfig(
                    type = currentProject.logoType,
                    uri = currentProject.customLogoUri,
                    position = currentProject.logoPosition,
                    scale = scale,
                    opacity = currentProject.logoOpacity
                )
            },
            onUpdateLogoAnimation = { anim ->
                viewModel.updateLogoAnimation(anim)
            },
            onApplyPreset = { presetName ->
                viewModel.applyLayoutPreset(presetName)
            },
            onToggleOverlayElement = { logo, live, reporter, headline, ticker, clock, location, stickers ->
                viewModel.toggleOverlayElement(logo, live, reporter, headline, ticker, clock, location, stickers)
            },
            onUpdateElementScale = { element, scale ->
                viewModel.updateElementScale(element, scale)
            },
            onUpdateHeadlineAnimation = { anim ->
                viewModel.updateHeadlineAnimation(anim)
            },
            onUpdateTickerAnimationAndSpeed = { anim, speed, tagText ->
                viewModel.updateTickerAnimationAndSpeed(anim, speed, tagText)
            },
            onUpdateManualTexts = { liveText, manualClock, tickerTag ->
                viewModel.updateManualTexts(liveText, manualClock, tickerTag)
            },
            onResetAllPositions = {
                viewModel.resetAllElementPositions()
            }
        )
    }

    // ================= KINEMASTER 5-LAYER POPUP MENU =================
    KinemasterLayerMenu(
        isOpen = showKinemasterLayerMenu,
        onDismiss = { showKinemasterLayerMenu = false },
        onSelectMedia = {
            kinemasterPipMediaPicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
            )
        },
        onSelectEffect = {
            viewModel.openKinemasterToolTab(8) // Color Grading / FX
        },
        onSelectSticker = {
            showOverlaySettingsDialog = true
        },
        onSelectText = {
            showQuickTextDialog = true
        },
        onSelectHandwriting = {
            isHandwritingActive = true
        }
    )

    // ================= KINEMASTER KEYFRAME ANIMATION DIALOG =================
    KinemasterKeyframeDialog(
        isOpen = showKeyframeDialog,
        selectedTarget = selectedTrack,
        currentPlayheadMs = timelinePositionMs,
        totalDurationMs = timelineDurationMs,
        onDismiss = { showKeyframeDialog = false },
        onAddKeyframe = { timeMs, scale, opacity ->
            viewModel.addKeyframe(timeMs, scale, opacity)
        }
    )

    // ================= KINEMASTER QUICK TEXT / HEADLINE LAYER DIALOG =================
    if (showQuickTextDialog) {
        var quickHeadline by remember { mutableStateOf(currentProject.headline) }
        var quickTicker by remember { mutableStateOf(currentProject.tickerText) }
        AlertDialog(
            onDismissRequest = { showQuickTextDialog = false },
            title = {
                Text("📝 टेक्स्ट लेयर संपादित करें (Text Layer)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quickHeadline,
                        onValueChange = { quickHeadline = it },
                        label = { Text("मुख्य हेडलाइन टेक्स्ट") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = quickTicker,
                        onValueChange = { quickTicker = it },
                        label = { Text("चलती पट्टी (Ticker)") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateHeadline(quickHeadline)
                        viewModel.updateTicker(quickTicker)
                        viewModel.toggleOverlayElement(headline = true, ticker = true)
                        showQuickTextDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NewsRedPrimary)
                ) {
                    Text("लागू करें", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickTextDialog = false }) {
                    Text("रद्द करें", color = Color.LightGray)
                }
            },
            containerColor = Color(0xFF0F172A)
        )
    }
}

@Composable
private fun LayerSwitchRow(
    title: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 12.sp, color = Color.White)
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NewsRedPrimary
            )
        )
    }
}

@Composable
private fun VirtualStudioFallback() {
    Image(
        painter = painterResource(id = R.drawable.bg_news_studio),
        contentDescription = "न्यूज़ स्टूडियो",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}
