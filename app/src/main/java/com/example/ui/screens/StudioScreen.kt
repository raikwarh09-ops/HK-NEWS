package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.CameraRear
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
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
import com.example.ui.components.BackgroundSourceCard
import com.example.ui.components.ExportProgressAndSavedBanner
import com.example.ui.components.KinemasterClipEditorSheet
import com.example.ui.components.KinemasterHandwritingCanvas
import com.example.ui.components.KinemasterKeyframeDialog
import com.example.ui.components.KinemasterLayerMenu
import com.example.ui.components.KinemasterSideBar
import com.example.ui.components.KinemasterTimelineView
import com.example.ui.components.KinemasterWheelToolsSection
import com.example.ui.components.NewsAudioSoundboard
import com.example.ui.components.NewsHeadlineEditorCard
import com.example.ui.components.NewsOverlayFrame
import com.example.ui.components.NewsVideoPlayer
import com.example.ui.components.QuickTemplatesCard
import com.example.ui.components.RecordAndExportCard
import com.example.ui.components.ScreenLayoutCustomizerDialog
import com.example.ui.components.StudioMainMenuBar
import com.example.ui.components.StudioMenuCategory
import com.example.ui.components.StudioQuickMenuGridDialog
import com.example.ui.components.TeleprompterControlCard
import com.example.ui.components.VideoEditingToolsBar
import com.example.ui.components.VideoFilterPalettes
import com.example.ui.components.VideoPlayerControlCard
import com.example.ui.components.VnEditorSuiteCard
import com.example.ui.theme.NewsAmber
import com.example.ui.theme.NewsGold
import com.example.ui.theme.NewsRedPrimary
import com.example.util.GalleryMediaSaver
import com.example.viewmodel.BackgroundSource
import com.example.viewmodel.NewsStudioViewModel

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

    val timelinePositionMs by viewModel.timelinePositionMs.collectAsState()
    val timelineDurationMs by viewModel.timelineDurationMs.collectAsState()
    val splitPointRatio by viewModel.splitPointRatio.collectAsState()
    val seekRequestEvent by viewModel.seekRequestEvent.collectAsState()
    val selectedTrack by viewModel.selectedTrack.collectAsState()
    val isKinemasterInspectorOpen by viewModel.isKinemasterInspectorOpen.collectAsState()
    val kinemasterInspectorTab by viewModel.kinemasterInspectorTab.collectAsState()
    val isVoiceRecording by viewModel.isVoiceRecording.collectAsState()
    val isTimelineExpanded by viewModel.isTimelineExpanded.collectAsState()
    val isVnPanelOpen by viewModel.isVnPanelOpen.collectAsState()
    val vnActiveToolTab by viewModel.vnActiveToolTab.collectAsState()
    val selectedAnimationTheme by viewModel.selectedAnimationTheme.collectAsState()
    val savedGalleryUri by viewModel.savedGalleryUri.collectAsState()
    val isSavingToGallery by viewModel.isSavingToGallery.collectAsState()

    // Organized Main Menu Navigation State
    var selectedMenuCategory by remember { mutableStateOf(StudioMenuCategory.KINEMASTER) }
    var showQuickMenuGrid by remember { mutableStateOf(false) }

    var showKinemasterLayerMenu by remember { mutableStateOf(false) }
    var isHandwritingActive by remember { mutableStateOf(false) }
    var showKeyframeDialog by remember { mutableStateOf(false) }
    var showQuickTextDialog by remember { mutableStateOf(false) }
    var showOverlaySettingsDialog by remember { mutableStateOf(false) }
    var showTeleprompterEditor by remember { mutableStateOf(false) }
    var showScreenLayoutDialog by remember { mutableStateOf(false) }

    val kinemasterMediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) viewModel.setVideoUri(uri) }

    val kinemasterPipMediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.setSecondaryMediaUri(uri)
            viewModel.updateLayoutMode("PIP_CORNER")
        }
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) viewModel.setBackgroundSource(BackgroundSource.CAMERA)
    }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted) viewModel.toggleVoiceRecording(context)
    }

    var pendingSaveAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val legacyStorageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val action = pendingSaveAction
        pendingSaveAction = null
        if (granted && action != null) action()
    }

    fun runWithGalleryPermission(action: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            action()
        } else {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                action()
            } else {
                pendingSaveAction = action
                legacyStorageLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) viewModel.setVideoUri(uri) }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) viewModel.setCustomMediaUri(uri) }

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
                    val timelineProg = (timelinePositionMs.toFloat() / timelineDurationMs.coerceAtLeast(1L)).coerceIn(0f, 1f)
                    val galleryIsImageOnly = if (backgroundSource == BackgroundSource.GALLERY_MEDIA && customMediaUri != null) {
                        val uriStr = customMediaUri.toString()
                        val mimeType = try { context.contentResolver.getType(customMediaUri!!) } catch (_: Exception) { null }
                        (mimeType?.startsWith("image") == true) ||
                            uriStr.endsWith(".jpg", ignoreCase = true) ||
                            uriStr.endsWith(".jpeg", ignoreCase = true) ||
                            uriStr.endsWith(".png", ignoreCase = true) ||
                            uriStr.endsWith(".webp", ignoreCase = true)
                    } else {
                        false
                    }
                    val hasNativeColor = backgroundSource == BackgroundSource.GALLERY_MEDIA &&
                        customMediaUri != null &&
                        (galleryIsImageOnly || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)

                    Box(modifier = Modifier.fillMaxWidth()) {
                        NewsOverlayFrame(
                            project = currentProject,
                            isBreakingFlashActive = isBreakingFlashActive,
                            showTeleprompter = isTeleprompterActive,
                            teleprompterSpeed = teleprompterSpeed,
                            teleprompterFontSize = teleprompterFontSize,
                            timelineProgress = timelineProg,
                            splitPointRatio = splitPointRatio,
                            hasNativeColorMatrixApplied = hasNativeColor,
                            onDragElementDelta = { element, dx, dy ->
                                viewModel.applyElementDragDelta(element, dx, dy)
                            },
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
                                                Text("कैमरा अनुमति आवश्यक है", color = Color.White, fontSize = 12.sp)
                                                Button(
                                                    onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = NewsRedPrimary)
                                                ) {
                                                    Text("अनुमति दें", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }

                                BackgroundSource.GALLERY_MEDIA -> {
                                    if (customMediaUri != null) {
                                        if (galleryIsImageOnly) {
                                            val imgMatrix = VideoFilterPalettes.buildAndroidColorMatrix(
                                                filterId = currentProject.activeFilter,
                                                contrast = currentProject.videoContrast,
                                                brightness = currentProject.videoBrightness,
                                                saturation = currentProject.vnSaturation,
                                                warmth = currentProject.vnWarmth,
                                                sharpen = currentProject.vnSharpen
                                            )
                                            AsyncImage(
                                                model = customMediaUri,
                                                contentDescription = "गैलरी मीडिया",
                                                colorFilter = ColorFilter.colorMatrix(ColorMatrix(imgMatrix.array)),
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            NewsVideoPlayer(
                                                videoUri = customMediaUri!!,
                                                isPlaying = isVideoPlaying,
                                                speed = currentProject.videoSpeed,
                                                volume = currentProject.videoVolume,
                                                trimStart = currentProject.videoTrimStart,
                                                trimEnd = currentProject.videoTrimEnd,
                                                freezeFrame = currentProject.vnFreezeFrameActive,
                                                speedCurve = currentProject.vnSpeedCurve,
                                                audioVolumeBoost = currentProject.audioVolumeBoost,
                                                audioPitch = currentProject.audioPitch,
                                                voiceChangerEffect = currentProject.voiceChangerEffect,
                                                activeFilter = currentProject.activeFilter,
                                                contrast = currentProject.videoContrast,
                                                brightness = currentProject.videoBrightness,
                                                saturation = currentProject.vnSaturation,
                                                warmth = currentProject.vnWarmth,
                                                sharpen = currentProject.vnSharpen,
                                                timelineSeekRequest = seekRequestEvent,
                                                onPositionUpdate = { pos, dur ->
                                                    viewModel.syncPlaybackFromPlayer(pos, dur)
                                                },
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
                                    Box(modifier = Modifier.size(8.dp).background(Color.White, CircleShape))
                                    val recStr = if (recordingSeconds >= 3600) {
                                        "REC %02d:%02d:%02d".format(
                                            recordingSeconds / 3600,
                                            (recordingSeconds % 3600) / 60,
                                            recordingSeconds % 60
                                        )
                                    } else {
                                        "REC %02d:%02d".format(recordingSeconds / 60, recordingSeconds % 60)
                                    }
                                    Text(text = recStr, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            }
                        }

                        KinemasterHandwritingCanvas(
                            isActive = isHandwritingActive,
                            onSaveAndClose = { strokeCount ->
                                isHandwritingActive = false
                                viewModel.saveHandwritingStrokes(strokeCount)
                            },
                            onClearAll = { viewModel.clearHandwriting() },
                            modifier = Modifier.matchParentSize()
                        )

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

                    // Canvas Quick Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            listOf("16:9", "9:16", "1:1", "4:5").forEach { aspect ->
                                val selected = currentProject.aspectPreset == aspect
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (selected) NewsRedPrimary else Color(0xFF1E293B))
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

                        if (backgroundSource == BackgroundSource.CAMERA) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { viewModel.toggleCamera() }, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        imageVector = if (isFrontCamera) Icons.Default.CameraFront else Icons.Default.CameraRear,
                                        contentDescription = "कैमरा बदलें",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(onClick = { viewModel.toggleTorch() }, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                        contentDescription = "फ़्लैश",
                                        tint = if (isTorchEnabled) NewsGold else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { showOverlaySettingsDialog = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Layers, contentDescription = "लेयर्स", tint = NewsGold, modifier = Modifier.size(18.dp))
                        }

                        Button(
                            onClick = { showScreenLayoutDialog = true },
                            modifier = Modifier.height(30.dp).testTag("btn_arrange_screen"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.DashboardCustomize, contentDescription = null, tint = NewsGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("स्क्रीन जमाएं", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👆 उंगली से लोगो, हेडलाइन, टिकर, LIVE, स्टिकर को जहाँ चाहें सेट करें",
                            color = NewsAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { viewModel.resetAllElementPositions() },
                            modifier = Modifier.height(26.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
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
                splitPointRatio = splitPointRatio,
                selectedTrack = selectedTrack,
                isTimelineExpanded = isTimelineExpanded,
                onSelectTrack = { track ->
                    viewModel.setSelectedTrack(track)
                    if (selectedMenuCategory != StudioMenuCategory.ALL_TOOLS) {
                        selectedMenuCategory = StudioMenuCategory.KINEMASTER
                    }
                },
                onSeekTo = { posMs -> viewModel.seekTimelineTo(posMs) },
                onTrimLeft = { viewModel.trimTimelineLeft() },
                onSplitAtPlayhead = { viewModel.splitTimelineAtPlayhead() },
                onTrimRight = { viewModel.trimTimelineRight() },
                onOpenToolSheet = {
                    if (selectedMenuCategory != StudioMenuCategory.ALL_TOOLS) {
                        selectedMenuCategory = StudioMenuCategory.KINEMASTER
                    }
                    viewModel.openKinemasterToolTab(0)
                },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            )
        }

        // ================= LIVE EXPORT PROGRESS & SAVED GALLERY BANNER =================
        if (exportProgress != null || savedGalleryUri != null) {
            item(key = "export_status_banner") {
                ExportProgressAndSavedBanner(
                    exportProgress = exportProgress,
                    savedGalleryUri = savedGalleryUri,
                    onCancelExport = { viewModel.cancelGallerySave() },
                    onOpenInGallery = { uri -> GalleryMediaSaver.openInGallery(context, uri) },
                    onShareMedia = { uri -> GalleryMediaSaver.shareMedia(context, uri) }
                )
            }
        }

        // ================= ORGANIZED CATEGORIZED TOOL MENU BAR =================
        item(key = "studio_main_menu_dock") {
            StudioMainMenuBar(
                selectedCategory = selectedMenuCategory,
                onSelectCategory = { selectedMenuCategory = it },
                onOpenQuickMenuGrid = { showQuickMenuGrid = true }
            )
        }

        // ================= ACTIVE MENU CONTENT =================
        when (selectedMenuCategory) {
            StudioMenuCategory.KINEMASTER -> {
                item(key = "menu_kinemaster_wheel") {
                    KinemasterWheelToolsSection(
                        project = currentProject,
                        isVideoPlaying = isVideoPlaying,
                        isVoiceRecording = isVoiceRecording,
                        isHandwritingActive = isHandwritingActive,
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
                        },
                        onOpenToolTab = { tabIdx -> viewModel.openKinemasterToolTab(tabIdx) },
                        onOpenKeyframe = { showKeyframeDialog = true },
                        onToggleHandwriting = { isHandwritingActive = !isHandwritingActive }
                    )
                }

                if (isKinemasterInspectorOpen) {
                    item(key = "menu_kinemaster_inspector") {
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
                            onUpdateVoiceChanger = { viewModel.updateVoiceChanger(it) },
                            onUpdateTransition = { viewModel.updateTransition(it) },
                            onUpdateAnimation = { inA, outA -> viewModel.updateAnimation(inA, outA) },
                            onUpdateSpeed = { speed ->
                                viewModel.updateSpeedAndTrim(speed, currentProject.videoTrimStart, currentProject.videoTrimEnd, currentProject.videoVolume)
                            },
                            onUpdateContrastAndBrightness = { c, b -> viewModel.updateContrastAndBrightness(c, b) },
                            onUpdatePanZoom = { s, e, l -> viewModel.updatePanZoom(s, e, l) },
                            onUpdateAudioMixer = { v, p, d -> viewModel.updateAudioMixer(v, p, d) },
                            onToggleAudioFade = { fIn, fOut -> viewModel.toggleAudioFade(fIn, fOut) },
                            onUpdateExportSettings = { r, fps -> viewModel.updateExportSettings(r, fps) },
                            onOpenKeyframeDialog = { showKeyframeDialog = true }
                        )
                    }
                }
            }

            StudioMenuCategory.VN_TOOLS -> {
                item(key = "menu_vn_tools") {
                    VnEditorSuiteCard(
                        project = currentProject,
                        isPanelOpen = isVnPanelOpen,
                        activeTab = vnActiveToolTab,
                        onSelectToolTab = { viewModel.openVnToolTab(it) },
                        onClosePanel = { viewModel.closeVnPanel() },
                        onUpdateSpeedCurve = { viewModel.updateVnSpeedCurve(it) },
                        onUpdateFilter = { viewModel.updateFilterAndVfx(it, currentProject.activeVfx) },
                        onUpdateContrastBrightness = { c, b -> viewModel.updateContrastAndBrightness(c, b) },
                        onUpdateAdjustments = { sat, warm, vig, sharp -> viewModel.updateVnAdjustments(sat, warm, vig, sharp) },
                        onUpdateBackgroundAndBorder = { bg, bw, bColor -> viewModel.updateVnBackgroundAndBorder(bg, bw, bColor) },
                        onRotate90 = { viewModel.rotateVnVideo90() },
                        onToggleMirrorHorizontal = { viewModel.toggleVnMirrorHorizontal() },
                        onToggleFlipVertical = { viewModel.toggleVnFlipVertical() },
                        onUpdateAspectPreset = { viewModel.updateAspectPreset(it) },
                        onUpdateMotionFx = { viewModel.updateVnMotionFx(it) },
                        onToggleFreezeFrame = { viewModel.toggleVnFreezeFrame() },
                        onToggleReverse = { viewModel.toggleVnReversePlayback() },
                        onTrimLeft = { viewModel.trimTimelineLeft() },
                        onSplitAtPlayhead = { viewModel.splitTimelineAtPlayhead() },
                        onTrimRight = { viewModel.trimTimelineRight() },
                        onUpdateMaskAndOpacity = { m, o -> viewModel.updateVnMaskAndOpacity(m, o) },
                        onUpdateSubtitleAndBeat = { t, s, b -> viewModel.updateVnSubtitleAndBeat(t, s, b) },
                        onResetVnTools = { viewModel.resetVnTools() },
                        onUpdateAnimationFilter = { viewModel.updateAnimationFilter(it) },
                        onUpdateVideoCrop = { preset, zoom, px, py, l, t, r, b ->
                            viewModel.updateVideoCrop(preset, zoom, px, py, l, t, r, b)
                        },
                        onResetVideoCrop = { viewModel.resetVideoCrop() },
                        onUpdateVoiceChanger = { viewModel.updateVoiceChanger(it) },
                        onUpdateVoicePitch = { viewModel.updateVoicePitch(it) }
                    )
                }
            }

            StudioMenuCategory.NEWS_SCREEN -> {
                item(key = "menu_news_headline") {
                    NewsHeadlineEditorCard(
                        project = currentProject,
                        onUpdateCategory = { viewModel.updateCategory(it) },
                        onUpdateHeadline = { viewModel.updateHeadline(it) },
                        onUpdateSubHeadline = { viewModel.updateSubHeadline(it) },
                        onUpdateHeadlineAnimation = { viewModel.updateHeadlineAnimation(it) },
                        onUpdateTicker = { viewModel.updateTicker(it) },
                        onUpdateTickerAnimSpeedTag = { a, s, t -> viewModel.updateTickerAnimationAndSpeed(a, s, t) },
                        onOpenScreenCustomizer = { showScreenLayoutDialog = true }
                    )
                }
                item(key = "menu_news_templates") {
                    QuickTemplatesCard(
                        templates = viewModel.headlineTemplates,
                        onApplyTemplate = { viewModel.applyHeadlineTemplate(it) }
                    )
                }
            }

            StudioMenuCategory.EFFECTS_LOGO -> {
                item(key = "menu_effects_logo") {
                    VideoEditingToolsBar(
                        project = currentProject,
                        onUpdateLogoConfig = { type, uri, pos, scale, opacity ->
                            viewModel.updateLogoConfig(type, uri, pos, scale, opacity)
                        },
                        onUpdateContrastAndBrightness = { c, b -> viewModel.updateContrastAndBrightness(c, b) },
                        onUpdateFilterAndVfx = { f, v -> viewModel.updateFilterAndVfx(f, v) },
                        onUpdateSpeedAndTrim = { s, ts, te, v -> viewModel.updateSpeedAndTrim(s, ts, te, v) },
                        onUpdateLayoutMode = { viewModel.updateLayoutMode(it) },
                        onToggleSticker = { viewModel.toggleSticker(it) },
                        onPickSecondaryMedia = { viewModel.setSecondaryMediaUri(it) },
                        onUpdateAnimationFilter = { viewModel.updateAnimationFilter(it) },
                        onUpdateVideoCrop = { preset, zoom, px, py, l, t, r, b ->
                            viewModel.updateVideoCrop(preset, zoom, px, py, l, t, r, b)
                        },
                        onResetVideoCrop = { viewModel.resetVideoCrop() }
                    )
                }
            }

            StudioMenuCategory.MEDIA_AUDIO -> {
                item(key = "menu_media_player") {
                    VideoPlayerControlCard(
                        hasGalleryMedia = customMediaUri != null && backgroundSource == BackgroundSource.GALLERY_MEDIA,
                        isVideoPlaying = isVideoPlaying,
                        onAddVideo = {
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        },
                        onTogglePlayPause = {
                            if (customMediaUri == null || backgroundSource != BackgroundSource.GALLERY_MEDIA) {
                                videoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            } else {
                                viewModel.toggleVideoPlayback()
                            }
                        }
                    )
                }
                item(key = "menu_media_source") {
                    BackgroundSourceCard(
                        backgroundSource = backgroundSource,
                        selectedAnimationTheme = selectedAnimationTheme,
                        onSelectVirtualStudio = { viewModel.setBackgroundSource(BackgroundSource.VIRTUAL_STUDIO) },
                        onSelectCamera = {
                            if (hasCameraPermission) {
                                viewModel.setBackgroundSource(BackgroundSource.CAMERA)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        onSelectGalleryMedia = {
                            mediaPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        },
                        onSelectAnimationTheme = { viewModel.setAnimationTheme(it) }
                    )
                }
                item(key = "menu_media_sound") {
                    NewsAudioSoundboard(
                        onTriggerBreakingEffect = { viewModel.triggerBreakingFlash() }
                    )
                }
                item(key = "menu_media_prompter") {
                    TeleprompterControlCard(
                        isTeleprompterActive = isTeleprompterActive,
                        teleprompterSpeed = teleprompterSpeed,
                        teleprompterFontSize = teleprompterFontSize,
                        onToggleActive = { viewModel.setTeleprompterActive(it) },
                        onChangeSpeed = { viewModel.setTeleprompterSpeed(it) },
                        onOpenScriptEditor = { showTeleprompterEditor = true }
                    )
                }
            }

            StudioMenuCategory.RECORD_EXPORT -> {
                item(key = "menu_record_export") {
                    RecordAndExportCard(
                        timelineDurationMs = timelineDurationMs,
                        isRecording = isRecording,
                        recordingSeconds = recordingSeconds,
                        isSavingToGallery = isSavingToGallery,
                        onToggleRecording = { viewModel.toggleRecording(context) },
                        onSaveVideoToGallery = {
                            runWithGalleryPermission { viewModel.saveToGallery(context, asVideo = true) }
                        },
                        onSavePoster = {
                            runWithGalleryPermission { viewModel.saveToGallery(context, asVideo = false) }
                        },
                        onSaveProject = { viewModel.saveCurrentProject() },
                        onInstantFastSave = {
                            runWithGalleryPermission {
                                viewModel.saveToGallery(context, asVideo = true, instantDirectSave = true)
                            }
                        },
                        onSelectDurationMs = { viewModel.setTargetVideoDurationMs(it) }
                    )
                }
            }

            StudioMenuCategory.ALL_TOOLS -> {
                item(key = "all_km_wheel") {
                    KinemasterWheelToolsSection(
                        project = currentProject,
                        isVideoPlaying = isVideoPlaying,
                        isVoiceRecording = isVoiceRecording,
                        isHandwritingActive = isHandwritingActive,
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
                            if (hasAudioPermission) viewModel.toggleVoiceRecording(context)
                            else audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        onOpenToolTab = { viewModel.openKinemasterToolTab(it) },
                        onOpenKeyframe = { showKeyframeDialog = true },
                        onToggleHandwriting = { isHandwritingActive = !isHandwritingActive }
                    )
                }

                if (isKinemasterInspectorOpen) {
                    item(key = "all_km_inspector") {
                        KinemasterClipEditorSheet(
                            project = currentProject,
                            selectedTrack = selectedTrack,
                            initialTab = kinemasterInspectorTab,
                            onClose = { viewModel.setKinemasterInspectorOpen(false) },
                            onTrimLeft = { viewModel.trimTimelineLeft() },
                            onSplitAtPlayhead = { viewModel.splitTimelineAtPlayhead() },
                            onTrimRight = { viewModel.trimTimelineRight() },
                            onUpdateChromaKey = { enabled, color, sim -> viewModel.updateChromaKey(enabled, color, sim) },
                            onUpdateVoiceChanger = { viewModel.updateVoiceChanger(it) },
                            onUpdateTransition = { viewModel.updateTransition(it) },
                            onUpdateAnimation = { inA, outA -> viewModel.updateAnimation(inA, outA) },
                            onUpdateSpeed = { speed ->
                                viewModel.updateSpeedAndTrim(speed, currentProject.videoTrimStart, currentProject.videoTrimEnd, currentProject.videoVolume)
                            },
                            onUpdateContrastAndBrightness = { c, b -> viewModel.updateContrastAndBrightness(c, b) },
                            onUpdatePanZoom = { s, e, l -> viewModel.updatePanZoom(s, e, l) },
                            onUpdateAudioMixer = { v, p, d -> viewModel.updateAudioMixer(v, p, d) },
                            onToggleAudioFade = { fIn, fOut -> viewModel.toggleAudioFade(fIn, fOut) },
                            onUpdateExportSettings = { r, fps -> viewModel.updateExportSettings(r, fps) },
                            onOpenKeyframeDialog = { showKeyframeDialog = true }
                        )
                    }
                }

                item(key = "all_vn_suite") {
                    VnEditorSuiteCard(
                        project = currentProject,
                        isPanelOpen = isVnPanelOpen,
                        activeTab = vnActiveToolTab,
                        onSelectToolTab = { viewModel.openVnToolTab(it) },
                        onClosePanel = { viewModel.closeVnPanel() },
                        onUpdateSpeedCurve = { viewModel.updateVnSpeedCurve(it) },
                        onUpdateFilter = { viewModel.updateFilterAndVfx(it, currentProject.activeVfx) },
                        onUpdateContrastBrightness = { c, b -> viewModel.updateContrastAndBrightness(c, b) },
                        onUpdateAdjustments = { sat, warm, vig, sharp -> viewModel.updateVnAdjustments(sat, warm, vig, sharp) },
                        onUpdateBackgroundAndBorder = { bg, bw, bColor -> viewModel.updateVnBackgroundAndBorder(bg, bw, bColor) },
                        onRotate90 = { viewModel.rotateVnVideo90() },
                        onToggleMirrorHorizontal = { viewModel.toggleVnMirrorHorizontal() },
                        onToggleFlipVertical = { viewModel.toggleVnFlipVertical() },
                        onUpdateAspectPreset = { viewModel.updateAspectPreset(it) },
                        onUpdateMotionFx = { viewModel.updateVnMotionFx(it) },
                        onToggleFreezeFrame = { viewModel.toggleVnFreezeFrame() },
                        onToggleReverse = { viewModel.toggleVnReversePlayback() },
                        onTrimLeft = { viewModel.trimTimelineLeft() },
                        onSplitAtPlayhead = { viewModel.splitTimelineAtPlayhead() },
                        onTrimRight = { viewModel.trimTimelineRight() },
                        onUpdateMaskAndOpacity = { m, o -> viewModel.updateVnMaskAndOpacity(m, o) },
                        onUpdateSubtitleAndBeat = { t, s, b -> viewModel.updateVnSubtitleAndBeat(t, s, b) },
                        onResetVnTools = { viewModel.resetVnTools() },
                        onUpdateAnimationFilter = { viewModel.updateAnimationFilter(it) },
                        onUpdateVideoCrop = { preset, zoom, px, py, l, t, r, b ->
                            viewModel.updateVideoCrop(preset, zoom, px, py, l, t, r, b)
                        },
                        onResetVideoCrop = { viewModel.resetVideoCrop() },
                        onUpdateVoiceChanger = { viewModel.updateVoiceChanger(it) },
                        onUpdateVoicePitch = { viewModel.updateVoicePitch(it) }
                    )
                }

                val hiddenSet = currentProject.hiddenSections.split(",").filter { it.isNotBlank() }.toSet()
                val ordered = currentProject.studioSectionsOrder.split(",").filter { it.isNotBlank() && !hiddenSet.contains(it) }
                ordered.forEach { sectionId ->
                    when (sectionId) {
                        "VIDEO" -> item(key = "section_video") {
                            VideoPlayerControlCard(
                                hasGalleryMedia = customMediaUri != null && backgroundSource == BackgroundSource.GALLERY_MEDIA,
                                isVideoPlaying = isVideoPlaying,
                                onAddVideo = { videoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
                                onTogglePlayPause = {
                                    if (customMediaUri == null || backgroundSource != BackgroundSource.GALLERY_MEDIA) {
                                        videoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                                    } else {
                                        viewModel.toggleVideoPlayback()
                                    }
                                }
                            )
                        }
                        "RECORD" -> item(key = "section_record") {
                            RecordAndExportCard(
                                timelineDurationMs = timelineDurationMs,
                                isRecording = isRecording,
                                recordingSeconds = recordingSeconds,
                                isSavingToGallery = isSavingToGallery,
                                onToggleRecording = { viewModel.toggleRecording(context) },
                                onSaveVideoToGallery = { runWithGalleryPermission { viewModel.saveToGallery(context, asVideo = true) } },
                                onSavePoster = { runWithGalleryPermission { viewModel.saveToGallery(context, asVideo = false) } },
                                onSaveProject = { viewModel.saveCurrentProject() },
                                onInstantFastSave = { runWithGalleryPermission { viewModel.saveToGallery(context, asVideo = true, instantDirectSave = true) } },
                                onSelectDurationMs = { viewModel.setTargetVideoDurationMs(it) }
                            )
                        }
                        "SOURCE" -> item(key = "section_source") {
                            BackgroundSourceCard(
                                backgroundSource = backgroundSource,
                                selectedAnimationTheme = selectedAnimationTheme,
                                onSelectVirtualStudio = { viewModel.setBackgroundSource(BackgroundSource.VIRTUAL_STUDIO) },
                                onSelectCamera = {
                                    if (hasCameraPermission) viewModel.setBackgroundSource(BackgroundSource.CAMERA)
                                    else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                },
                                onSelectGalleryMedia = { mediaPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) },
                                onSelectAnimationTheme = { viewModel.setAnimationTheme(it) }
                            )
                        }
                        "TOOLS" -> item(key = "section_tools") {
                            VideoEditingToolsBar(
                                project = currentProject,
                                onUpdateLogoConfig = { type, uri, pos, scale, opacity -> viewModel.updateLogoConfig(type, uri, pos, scale, opacity) },
                                onUpdateContrastAndBrightness = { c, b -> viewModel.updateContrastAndBrightness(c, b) },
                                onUpdateFilterAndVfx = { f, v -> viewModel.updateFilterAndVfx(f, v) },
                                onUpdateSpeedAndTrim = { s, ts, te, v -> viewModel.updateSpeedAndTrim(s, ts, te, v) },
                                onUpdateLayoutMode = { viewModel.updateLayoutMode(it) },
                                onToggleSticker = { viewModel.toggleSticker(it) },
                                onPickSecondaryMedia = { viewModel.setSecondaryMediaUri(it) },
                                onUpdateAnimationFilter = { viewModel.updateAnimationFilter(it) },
                                onUpdateVideoCrop = { preset, zoom, px, py, l, t, r, b ->
                                    viewModel.updateVideoCrop(preset, zoom, px, py, l, t, r, b)
                                },
                                onResetVideoCrop = { viewModel.resetVideoCrop() }
                            )
                        }
                        "SOUND" -> item(key = "section_sound") {
                            NewsAudioSoundboard(onTriggerBreakingEffect = { viewModel.triggerBreakingFlash() })
                        }
                        "PROMPTER" -> item(key = "section_prompter") {
                            TeleprompterControlCard(
                                isTeleprompterActive = isTeleprompterActive,
                                teleprompterSpeed = teleprompterSpeed,
                                teleprompterFontSize = teleprompterFontSize,
                                onToggleActive = { viewModel.setTeleprompterActive(it) },
                                onChangeSpeed = { viewModel.setTeleprompterSpeed(it) },
                                onOpenScriptEditor = { showTeleprompterEditor = true }
                            )
                        }
                        "HEADLINE" -> item(key = "section_headline") {
                            NewsHeadlineEditorCard(
                                project = currentProject,
                                onUpdateCategory = { viewModel.updateCategory(it) },
                                onUpdateHeadline = { viewModel.updateHeadline(it) },
                                onUpdateSubHeadline = { viewModel.updateSubHeadline(it) },
                                onUpdateHeadlineAnimation = { viewModel.updateHeadlineAnimation(it) },
                                onUpdateTicker = { viewModel.updateTicker(it) },
                                onUpdateTickerAnimSpeedTag = { a, s, t -> viewModel.updateTickerAnimationAndSpeed(a, s, t) },
                                onOpenScreenCustomizer = { showScreenLayoutDialog = true }
                            )
                        }
                    }
                }

                item(key = "all_templates") {
                    QuickTemplatesCard(
                        templates = viewModel.headlineTemplates,
                        onApplyTemplate = { viewModel.applyHeadlineTemplate(it) }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    // ================= DIALOGS & POPUPS =================
    if (showQuickMenuGrid) {
        StudioQuickMenuGridDialog(
            selectedCategory = selectedMenuCategory,
            onSelectCategory = { selectedMenuCategory = it },
            onDismiss = { showQuickMenuGrid = false }
        )
    }

    if (showOverlaySettingsDialog) {
        AlertDialog(
            onDismissRequest = { showOverlaySettingsDialog = false },
            title = { Text("ऑन-स्क्रीन लेयर्स चालू/बंद करें", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LayerSwitchRow("चैनल लोगो (Channel Logo)", currentProject.showLogo) { viewModel.toggleOverlayElement(logo = it) }
                    LayerSwitchRow("LIVE सूचक (Live Badge)", currentProject.showLiveBadge) { viewModel.toggleOverlayElement(live = it) }
                    LayerSwitchRow("रिपोर्टर पट्टी (Reporter Lower Third)", currentProject.showReporterCard) { viewModel.toggleOverlayElement(reporter = it) }
                    LayerSwitchRow("हेडलाइन बॉक्स (Main Headline)", currentProject.showHeadline) { viewModel.toggleOverlayElement(headline = it) }
                    LayerSwitchRow("चलती पट्टी (Scrolling Ticker)", currentProject.showTicker) { viewModel.toggleOverlayElement(ticker = it) }
                    LayerSwitchRow("स्थान / जिला बैज (Location Chip)", currentProject.showLocation) { viewModel.toggleOverlayElement(location = it) }
                    LayerSwitchRow("ब्रेकिंग स्टिकर्स (Stickers Row)", currentProject.showStickers) { viewModel.toggleOverlayElement(stickers = it) }
                    LayerSwitchRow("घड़ी / समय (Time Clock)", currentProject.showClock) { viewModel.toggleOverlayElement(clock = it) }
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

    if (showTeleprompterEditor) {
        var scriptText by remember { mutableStateOf(currentProject.teleprompterText) }
        AlertDialog(
            onDismissRequest = { showTeleprompterEditor = false },
            title = { Text("प्रॉम्प्टर स्क्रिप्ट संपादित करें", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("वाचक इसे स्क्रीन पर देखकर सहजता से बोल सकेंगे:", fontSize = 11.sp, color = Color.Gray)
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
                TextButton(onClick = { showTeleprompterEditor = false }) { Text("रद्द करें") }
            }
        )
    }

    if (savedGalleryUri != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSavedGalleryDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50))
                    Text("सीधे फ़ोन गैलरी में सेव हुआ!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "आपकी एडिट की गई H.K NEWS वीडियो / ब्रॉडकास्ट पोस्टर सीधे आपके फ़ोन की गैलरी (Movies/HkNewsStudio / Photos) में सेव कर दी गई है।",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "हेडलाइन: ${currentProject.headline.take(30)}...",
                        color = NewsGold,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { GalleryMediaSaver.openInGallery(context, savedGalleryUri!!) },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_dialog_open_gallery"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.VideoLibrary, contentDescription = "गैलरी में खोलें", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("गैलरी में देखें (Open in Gallery)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { GalleryMediaSaver.shareMedia(context, savedGalleryUri!!) },
                    modifier = Modifier.height(48.dp).testTag("btn_dialog_share_gallery"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.IosShare, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("शेयर करें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissSavedGalleryDialog() }, modifier = Modifier.height(48.dp)) {
                    Text("ठीक है (Done)", color = Color.White)
                }
            },
            containerColor = Color(0xFF131D31)
        )
    }

    if (showScreenLayoutDialog) {
        ScreenLayoutCustomizerDialog(
            project = currentProject,
            initialTab = 0,
            onDismiss = { showScreenLayoutDialog = false },
            onReorderSection = { sectionId, moveUp -> viewModel.reorderSection(sectionId, moveUp) },
            onToggleSectionVisibility = { viewModel.toggleSectionVisibility(it) },
            onResetSections = { viewModel.resetSectionsOrder() },
            onUpdateElementPosition = { el, pos -> viewModel.updateElementPosition(el, pos) },
            onUpdateLogoScale = { scale ->
                viewModel.updateLogoConfig(
                    type = currentProject.logoType,
                    uri = currentProject.customLogoUri,
                    position = currentProject.logoPosition,
                    scale = scale,
                    opacity = currentProject.logoOpacity
                )
            },
            onUpdateLogoAnimation = { viewModel.updateLogoAnimation(it) },
            onApplyPreset = { viewModel.applyLayoutPreset(it) },
            onToggleOverlayElement = { logo, live, reporter, headline, ticker, clock, location, stickers ->
                viewModel.toggleOverlayElement(logo, live, reporter, headline, ticker, clock, location, stickers)
            },
            onUpdateElementScale = { el, scale -> viewModel.updateElementScale(el, scale) },
            onUpdateHeadlineAnimation = { viewModel.updateHeadlineAnimation(it) },
            onUpdateTickerAnimationAndSpeed = { anim, speed, tagText ->
                viewModel.updateTickerAnimationAndSpeed(anim, speed, tagText)
            },
            onUpdateManualTexts = { liveText, manualClock, tickerTag ->
                viewModel.updateManualTexts(liveText, manualClock, tickerTag)
            },
            onResetAllPositions = { viewModel.resetAllElementPositions() }
        )
    }

    KinemasterLayerMenu(
        isOpen = showKinemasterLayerMenu,
        onDismiss = { showKinemasterLayerMenu = false },
        onSelectMedia = {
            kinemasterPipMediaPicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
            )
        },
        onSelectEffect = {
            if (selectedMenuCategory != StudioMenuCategory.ALL_TOOLS) {
                selectedMenuCategory = StudioMenuCategory.KINEMASTER
            }
            viewModel.openKinemasterToolTab(8)
        },
        onSelectSticker = { showOverlaySettingsDialog = true },
        onSelectText = { showQuickTextDialog = true },
        onSelectHandwriting = { isHandwritingActive = true }
    )

    KinemasterKeyframeDialog(
        isOpen = showKeyframeDialog,
        selectedTarget = selectedTrack,
        currentPlayheadMs = timelinePositionMs,
        totalDurationMs = timelineDurationMs,
        onDismiss = { showKeyframeDialog = false },
        onAddKeyframe = { timeMs, scale, opacity -> viewModel.addKeyframe(timeMs, scale, opacity) }
    )

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
                TextButton(onClick = { showQuickTextDialog = false }) { Text("रद्द करें", color = Color.LightGray) }
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
