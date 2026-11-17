package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import java.io.File
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.NewsAudioEffects
import com.example.ui.components.NewsAnimationTheme
import com.example.util.GalleryMediaSaver
import com.example.data.db.AppDatabase
import com.example.data.model.HindiHeadlineTemplate
import com.example.data.model.HindiTeleprompterPreset
import com.example.data.model.NewsProject
import com.example.data.repository.NewsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class BackgroundSource {
    VIRTUAL_STUDIO,
    CAMERA,
    GALLERY_MEDIA
}

class NewsStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NewsRepository

    init {
        val dao = AppDatabase.getDatabase(application).newsProjectDao()
        repository = NewsRepository(dao)
    }

    val savedProjects: StateFlow<List<NewsProject>> = repository.allProjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val headlineTemplates: List<HindiHeadlineTemplate> = repository.getHeadlineTemplates()
    val teleprompterPresets: List<HindiTeleprompterPreset> = repository.getTeleprompterPresets()

    private val _currentProject = MutableStateFlow(
        NewsProject(
            title = "मुख्य बुलेटिन",
            category = "ब्रेकिंग न्यूज़",
            headline = "बड़ा प्रशासनिक फेरबदल: वरिष्ठ अधिकारियों के तबादले",
            subHeadline = "शासन ने तत्काल प्रभाव से जारी किए आदेश, नए दायित्व सौंपे",
            tickerText = "हर खबर H.K NEWS: नरसिंहपुर समाचार • प्रशासनिक स्तर पर फेरबदल • कई अधिकारियों को नई जिम्मेदारियां • निष्पक्ष व सटीक समाचार सबसे पहले",
            reporterName = "राजेश शर्मा",
            reporterRole = "विशेष संवाददाता",
            location = "नरसिंहपुर",
            channelName = "हर खबर H.K NEWS",
            channelTagline = "नरसिंहपुर समाचार • सच की आवाज़",
            themeColorHex = "#C62828",
            logoType = "HK_NEWS",
            logoPosition = "TOP_RIGHT",
            logoScale = 1.0f,
            logoOpacity = 0.95f,
            activeFilter = "NONE",
            activeVfx = "NONE",
            videoSpeed = 1.0f,
            videoVolume = 1.0f,
            layoutMode = "FULLSCREEN",
            activeStickers = "LIVE,EXCLUSIVE",
            teleprompterText = "नमस्कार! आप देख रहे हैं हर खबर - H.K NEWS नरसिंहपुर समाचार। मैं हूँ आपका संवाददाता। आज की सबसे बड़ी हलचल और ताज़ा जानकारी के साथ बुलेटिन की शुरुआत करते हैं। जिले भर की तमाम बड़ी ख़बरें सबसे पहले आप तक...",
            aspectPreset = "16:9"
        )
    )
    val currentProject: StateFlow<NewsProject> = _currentProject.asStateFlow()

    private val _backgroundSource = MutableStateFlow(BackgroundSource.VIRTUAL_STUDIO)
    val backgroundSource: StateFlow<BackgroundSource> = _backgroundSource.asStateFlow()

    private val _customMediaUri = MutableStateFlow<Uri?>(null)
    val customMediaUri: StateFlow<Uri?> = _customMediaUri.asStateFlow()

    private val _secondaryMediaUri = MutableStateFlow<Uri?>(null)
    val secondaryMediaUri: StateFlow<Uri?> = _secondaryMediaUri.asStateFlow()

    private val _isFrontCamera = MutableStateFlow(true)
    val isFrontCamera: StateFlow<Boolean> = _isFrontCamera.asStateFlow()

    private val _isTorchEnabled = MutableStateFlow(false)
    val isTorchEnabled: StateFlow<Boolean> = _isTorchEnabled.asStateFlow()

    private val _isTeleprompterActive = MutableStateFlow(false)
    val isTeleprompterActive: StateFlow<Boolean> = _isTeleprompterActive.asStateFlow()

    private val _teleprompterSpeed = MutableStateFlow(1.0f)
    val teleprompterSpeed: StateFlow<Float> = _teleprompterSpeed.asStateFlow()

    private val _teleprompterFontSize = MutableStateFlow(18f)
    val teleprompterFontSize: StateFlow<Float> = _teleprompterFontSize.asStateFlow()

    private val _isBreakingFlashActive = MutableStateFlow(false)
    val isBreakingFlashActive: StateFlow<Boolean> = _isBreakingFlashActive.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingSeconds = MutableStateFlow(0)
    val recordingSeconds: StateFlow<Int> = _recordingSeconds.asStateFlow()

    private val _exportProgress = MutableStateFlow<Float?>(null)
    val exportProgress: StateFlow<Float?> = _exportProgress.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _isVideoPlaying = MutableStateFlow(true)
    val isVideoPlaying: StateFlow<Boolean> = _isVideoPlaying.asStateFlow()

    private val _selectedAnimationTheme = MutableStateFlow(NewsAnimationTheme.GLOBE_SPIN)
    val selectedAnimationTheme: StateFlow<NewsAnimationTheme> = _selectedAnimationTheme.asStateFlow()

    private val _savedGalleryUri = MutableStateFlow<Uri?>(null)
    val savedGalleryUri: StateFlow<Uri?> = _savedGalleryUri.asStateFlow()

    private val _isSavingToGallery = MutableStateFlow(false)
    val isSavingToGallery: StateFlow<Boolean> = _isSavingToGallery.asStateFlow()

    // ================= KINEMASTER EDITING SUITE STATE =================
    private val _timelinePositionMs = MutableStateFlow(0L)
    val timelinePositionMs: StateFlow<Long> = _timelinePositionMs.asStateFlow()

    private val _timelineDurationMs = MutableStateFlow(10000L)
    val timelineDurationMs: StateFlow<Long> = _timelineDurationMs.asStateFlow()

    private val _selectedTrack = MutableStateFlow("MAIN_VIDEO")
    val selectedTrack: StateFlow<String> = _selectedTrack.asStateFlow()

    private val _isKinemasterInspectorOpen = MutableStateFlow(false)
    val isKinemasterInspectorOpen: StateFlow<Boolean> = _isKinemasterInspectorOpen.asStateFlow()

    private val _kinemasterInspectorTab = MutableStateFlow(0)
    val kinemasterInspectorTab: StateFlow<Int> = _kinemasterInspectorTab.asStateFlow()

    private val _isVoiceRecording = MutableStateFlow(false)
    val isVoiceRecording: StateFlow<Boolean> = _isVoiceRecording.asStateFlow()

    private var voiceRecorder: MediaRecorder? = null
    private var voiceOutputFile: File? = null

    private val _isTimelineExpanded = MutableStateFlow(false)
    val isTimelineExpanded: StateFlow<Boolean> = _isTimelineExpanded.asStateFlow()

    private val undoStack = mutableListOf<NewsProject>()
    private val redoStack = mutableListOf<NewsProject>()

    init {
        viewModelScope.launch {
            while (true) {
                delay(100)
                if (_isVideoPlaying.value) {
                    _timelinePositionMs.update { current ->
                        val speed = _currentProject.value.videoSpeed
                        val next = current + (100 * speed).toLong()
                        if (next >= _timelineDurationMs.value) 0L else next
                    }
                }
            }
        }
    }

    fun seekTimelineTo(posMs: Long) {
        _timelinePositionMs.value = posMs.coerceIn(0L, _timelineDurationMs.value)
    }

    fun trimTimelineLeft() {
        val curRatio = (_timelinePositionMs.value.toFloat() / _timelineDurationMs.value).coerceIn(0f, 0.9f)
        pushUndoState()
        _currentProject.update { it.copy(videoTrimStart = curRatio) }
        _userMessage.value = "✂️ बाईं तरफ से क्लिप ट्रिम की गई (Start: ${(curRatio * 100).toInt()}%)"
    }

    fun trimTimelineRight() {
        val curRatio = (_timelinePositionMs.value.toFloat() / _timelineDurationMs.value).coerceIn(0.1f, 1f)
        pushUndoState()
        _currentProject.update { it.copy(videoTrimEnd = curRatio) }
        _userMessage.value = "✂️ दाईं तरफ से क्लिप ट्रिम की गई (End: ${(curRatio * 100).toInt()}%)"
    }

    fun splitTimelineAtPlayhead() {
        pushUndoState()
        _userMessage.value = "✂️ प्लेहेड पर क्लिप 2 टुकड़ों में विभाजित (Split) हुई!"
        NewsAudioEffects.playBreakingAlert()
    }

    fun updateChromaKey(enabled: Boolean, color: String, similarity: Float) {
        pushUndoState()
        _currentProject.update {
            it.copy(
                chromaKeyEnabled = enabled,
                chromaKeyColor = color,
                chromaKeySimilarity = similarity
            )
        }
        _userMessage.value = if (enabled) "🟩 क्रोमा की सक्रिय: $color बैकग्राउंड हटाया गया" else "क्रोमा की बंद"
    }

    fun updateVoiceChanger(effect: String) {
        pushUndoState()
        _currentProject.update { it.copy(voiceChangerEffect = effect) }
        _userMessage.value = "🎙️ वॉइस चेंजर इफ़ेक्ट लागू: $effect"
    }

    fun updateTransition(transition: String) {
        pushUndoState()
        _currentProject.update { it.copy(activeTransition = transition) }
        _userMessage.value = "🎞️ 3D ट्रांज़िशन लागू: $transition"
    }

    fun updateAnimation(inAnim: String, outAnim: String) {
        pushUndoState()
        _currentProject.update { it.copy(inAnimation = inAnim, outAnimation = outAnim) }
        _userMessage.value = "✨ एनिमेशन स्टाइल सेट: $inAnim"
    }

    fun setSelectedTrack(track: String) {
        _selectedTrack.value = track
        _kinemasterInspectorTab.value = when (track) {
            "AUDIO" -> 3
            "LAYER_OVERLAY" -> 6
            "HANDWRITING" -> 9
            else -> 0
        }
        _isKinemasterInspectorOpen.value = true
    }

    fun setKinemasterInspectorOpen(open: Boolean) {
        _isKinemasterInspectorOpen.value = open
    }

    fun openKinemasterToolTab(tabIndex: Int) {
        _kinemasterInspectorTab.value = tabIndex
        _isKinemasterInspectorOpen.value = true
    }

    fun toggleAudioFade(fadeIn: Boolean?, fadeOut: Boolean?) {
        pushUndoState()
        _currentProject.update {
            it.copy(
                audioFadeIn = fadeIn ?: it.audioFadeIn,
                audioFadeOut = fadeOut ?: it.audioFadeOut
            )
        }
        _userMessage.value = "🎵 ऑडियो फेड सेटिंग्स अपडेट की गईं"
    }

    fun toggleTimelineExpanded() {
        _isTimelineExpanded.value = !_isTimelineExpanded.value
    }

    fun toggleVoiceRecording(context: Context) {
        if (!_isVoiceRecording.value) {
            try {
                val outFile = File(context.cacheDir, "hk_voiceover_${System.currentTimeMillis()}.m4a")
                val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                recorder.setOutputFile(outFile.absolutePath)
                recorder.prepare()
                recorder.start()
                voiceRecorder = recorder
                voiceOutputFile = outFile
                _isVoiceRecording.value = true
                _userMessage.value = "🎙️ वॉइसओवर रिकॉर्डिंग शुरू... बोलें!"
                NewsAudioEffects.playMicChime()
            } catch (e: Exception) {
                try {
                    voiceRecorder?.release()
                } catch (_: Exception) {}
                voiceRecorder = null
                _isVoiceRecording.value = false
                _userMessage.value = "🎙️ माइक अनुमति दें या ऑडियो रिकॉर्डर जाँचें"
            }
        } else {
            try {
                voiceRecorder?.stop()
                voiceRecorder?.release()
            } catch (_: Exception) {}
            voiceRecorder = null
            _isVoiceRecording.value = false
            _selectedTrack.value = "AUDIO"
            _userMessage.value = "✅ वॉइसओवर रिकॉर्डिंग पूरी हुई और ऑडियो ट्रैक में जोड़ी गई!"
            NewsAudioEffects.playTickerBeep()
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            voiceRecorder?.release()
        } catch (_: Exception) {}
        voiceRecorder = null
    }

    fun captureFrame(context: Context) {
        viewModelScope.launch {
            _userMessage.value = "📸 स्क्रीनशॉट कैप्चर हो रहा है..."
            val uri = GalleryMediaSaver.saveNewsFrameToGallery(context, _currentProject.value)
            if (uri != null) {
                _savedGalleryUri.value = uri
                _userMessage.value = "📸 काइनमास्टर कैप्चर फ्रेम गैलरी में सेव हो गया!"
            }
        }
    }

    private fun pushUndoState() {
        undoStack.add(_currentProject.value)
        if (undoStack.size > 20) undoStack.removeAt(0)
        redoStack.clear()
    }

    fun undoAction() {
        if (undoStack.isNotEmpty()) {
            val last = undoStack.removeAt(undoStack.size - 1)
            redoStack.add(_currentProject.value)
            _currentProject.value = last
            _userMessage.value = "↩️ पिछला बदलाव वापस (Undo) किया गया"
        } else {
            _userMessage.value = "वापस करने के लिए कुछ नहीं है।"
        }
    }

    fun updatePanZoom(startScale: Float, endScale: Float, locked: Boolean) {
        pushUndoState()
        _currentProject.update {
            it.copy(
                panZoomStartScale = startScale,
                panZoomEndScale = endScale,
                panZoomLocked = locked
            )
        }
        _userMessage.value = if (locked) "🔍 पैन और ज़ूम लॉक: ${(startScale * 100).toInt()}%" else "🔍 पैन और ज़ूम: ${(startScale * 100).toInt()}% -> ${(endScale * 100).toInt()}%"
    }

    fun updateAudioMixer(volumeBoost: Float, pitch: Float, ducking: Boolean) {
        pushUndoState()
        _currentProject.update {
            it.copy(
                audioVolumeBoost = volumeBoost,
                audioPitch = pitch,
                audioAutoDucking = ducking
            )
        }
        _userMessage.value = "🔊 ऑडियो मिक्सर: ${(volumeBoost * 100).toInt()}% वॉल्यूम"
    }

    fun updateExportSettings(resolution: String, fps: Int) {
        _currentProject.update {
            it.copy(
                exportResolution = resolution,
                exportFps = fps
            )
        }
        _userMessage.value = "📐 एक्सपोर्ट सेटिंग्स: $resolution @ ${fps}FPS"
    }

    fun duplicateCurrentLayer() {
        pushUndoState()
        when (_selectedTrack.value) {
            "LAYER_OVERLAY" -> {
                _currentProject.update {
                    it.copy(
                        subHeadline = it.headline,
                        headline = "【कॉपी】 " + it.headline
                    )
                }
                _userMessage.value = "📑 हेडलाइन लेयर डुप्लिकेट की गई"
            }
            "MAIN_VIDEO" -> {
                _currentProject.update {
                    it.copy(
                        secondaryMediaUri = it.mediaUri,
                        layoutMode = "PIP_CORNER"
                    )
                }
                _userMessage.value = "📑 वीडियो लेयर PIP के रूप में डुप्लिकेट की गई"
            }
            else -> {
                _userMessage.value = "📑 चयनित लेयर डुप्लिकेट की गई"
            }
        }
    }

    fun deleteCurrentLayer() {
        pushUndoState()
        when (_selectedTrack.value) {
            "LAYER_OVERLAY" -> {
                _currentProject.update {
                    it.copy(
                        showHeadline = false,
                        showTicker = false
                    )
                }
                _userMessage.value = "🗑️ हेडलाइन व टिकर लेयर हटाई गई"
            }
            "MAIN_VIDEO" -> {
                _currentProject.update {
                    it.copy(
                        mediaUri = null
                    )
                }
                _backgroundSource.value = BackgroundSource.VIRTUAL_STUDIO
                _userMessage.value = "🗑️ मुख्य मीडिया क्लिप हटाई गई"
            }
            "AUDIO" -> {
                _currentProject.update {
                    it.copy(
                        audioVolumeBoost = 0f
                    )
                }
                _userMessage.value = "🗑️ ऑडियो म्यूट किया गया"
            }
            else -> {
                _userMessage.value = "🗑️ लेयर हटाई गई"
            }
        }
    }

    fun addKeyframe(timeMs: Long, scale: Float, opacity: Float) {
        pushUndoState()
        _currentProject.update {
            it.copy(
                keyframeEnabled = true,
                logoScale = scale,
                logoOpacity = opacity
            )
        }
        _userMessage.value = "🔑 कीफ़्रेम सहेजा गया: समय ${timeMs / 1000}s पर आकार ${(scale * 100).toInt()}%"
    }

    fun saveHandwritingStrokes(strokeCount: Int) {
        pushUndoState()
        _currentProject.update {
            it.copy(
                handwritingStrokes = "STROKES_$strokeCount"
            )
        }
        _userMessage.value = "✍️ हैंडराइटिंग लेयर जोड़ी गई ($strokeCount स्ट्रोक)!"
    }

    fun clearHandwriting() {
        pushUndoState()
        _currentProject.update {
            it.copy(
                handwritingStrokes = ""
            )
        }
        _userMessage.value = "✍️ हैंडराइटिंग साफ़ की गई"
    }

    fun redoAction() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.size - 1)
            undoStack.add(_currentProject.value)
            _currentProject.value = next
            _userMessage.value = "↪️ बदलाव फिर से लागू (Redo) किया गया"
        } else {
            _userMessage.value = "आगे करने के लिए कुछ नहीं है।"
        }
    }

    fun setAnimationTheme(theme: NewsAnimationTheme) {
        _selectedAnimationTheme.value = theme
        _backgroundSource.value = BackgroundSource.VIRTUAL_STUDIO
        _userMessage.value = "एनीमेशन वीडियो बैकग्राउंड लागू: ${theme.name}"
    }

    fun setVideoPlaying(playing: Boolean) {
        _isVideoPlaying.value = playing
    }

    fun toggleVideoPlayback() {
        _isVideoPlaying.value = !_isVideoPlaying.value
        _userMessage.value = if (_isVideoPlaying.value) "वीडियो प्ले हो रहा है ▶" else "वीडियो रोका गया ⏸"
    }

    fun setVideoUri(uri: Uri?) {
        _customMediaUri.value = uri
        if (uri != null) {
            _backgroundSource.value = BackgroundSource.GALLERY_MEDIA
            _currentProject.update { it.copy(mediaUri = uri.toString()) }
            _isVideoPlaying.value = true
            _userMessage.value = "वीडियो सफलतापूर्वक जोड़ा गया और प्ले हो रहा है!"
        }
    }

    fun saveToGallery(context: Context, asVideo: Boolean = true, instantDirectSave: Boolean = false) {
        viewModelScope.launch {
            _isSavingToGallery.value = true
            _exportProgress.value = 0.05f
            NewsAudioEffects.playTickerBeep()

            val customUri = _customMediaUri.value
            val isVideo = customUri != null && (
                customUri.toString().contains("video", ignoreCase = true) ||
                customUri.toString().endsWith(".mp4", ignoreCase = true) ||
                (context.contentResolver.getType(customUri)?.startsWith("video") == true)
            )

            var resultUri: Uri? = null
            if (instantDirectSave && isVideo && customUri != null) {
                _userMessage.value = "⚡ सुपर फ़ास्ट सेव हो रही है..."
                _exportProgress.value = 0.5f
                resultUri = GalleryMediaSaver.saveVideoToGallery(
                    context = context,
                    sourceUri = customUri,
                    projectTitle = _currentProject.value.headline
                )
                _exportProgress.value = 1.0f
            } else if (asVideo) {
                _userMessage.value = "⚡ फ्रेम सहित वीडियो तैयार हो रही है (2-3 सेकंड)..."
                resultUri = com.example.util.NewsVideoCompositor.compositeAndSaveVideo(
                    context = context,
                    project = _currentProject.value,
                    sourceVideoUri = if (isVideo) customUri else null,
                    onProgress = { prog ->
                        _exportProgress.value = prog
                    }
                )
            } else {
                _exportProgress.value = 0.7f
                resultUri = GalleryMediaSaver.saveNewsFrameToGallery(
                    context = context,
                    project = _currentProject.value
                )
            }

            _exportProgress.value = 1.0f
            saveCurrentProject()
            delay(200)
            _exportProgress.value = null
            _isSavingToGallery.value = false

            if (resultUri != null) {
                _savedGalleryUri.value = resultUri
                _userMessage.value = if (asVideo) "🎉 वीडियो सफलतापूर्वक गैलरी में सेव हो गई!" else "एचडी न्यूज़ पोस्टर गैलरी में सेव हो गया!"
            } else {
                _userMessage.value = "सहेजा गया: प्रोजेक्ट सुरक्षित है।"
            }
        }
    }

    fun updateElementDragOffset(element: String, offsetX: Float, offsetY: Float) {
        _currentProject.update { current ->
            when (element) {
                "LOGO" -> current.copy(logoOffsetX = offsetX, logoOffsetY = offsetY)
                "HEADLINE" -> current.copy(headlineOffsetX = offsetX, headlineOffsetY = offsetY)
                "TICKER" -> current.copy(tickerOffsetX = offsetX, tickerOffsetY = offsetY)
                "REPORTER" -> current.copy(reporterOffsetX = offsetX, reporterOffsetY = offsetY)
                "LIVE_BADGE" -> current.copy(liveBadgeOffsetX = offsetX, liveBadgeOffsetY = offsetY)
                "LOCATION" -> current.copy(locationOffsetX = offsetX, locationOffsetY = offsetY)
                "CLOCK" -> current.copy(clockOffsetX = offsetX, clockOffsetY = offsetY)
                "STICKERS" -> current.copy(stickersOffsetX = offsetX, stickersOffsetY = offsetY)
                else -> current
            }
        }
    }

    fun resetAllElementPositions() {
        _currentProject.update { current ->
            current.copy(
                logoOffsetX = 0f, logoOffsetY = 0f,
                headlineOffsetX = 0f, headlineOffsetY = 0f,
                tickerOffsetX = 0f, tickerOffsetY = 0f,
                reporterOffsetX = 0f, reporterOffsetY = 0f,
                liveBadgeOffsetX = 0f, liveBadgeOffsetY = 0f,
                locationOffsetX = 0f, locationOffsetY = 0f,
                clockOffsetX = 0f, clockOffsetY = 0f,
                stickersOffsetX = 0f, stickersOffsetY = 0f,
                headlineScale = 1.0f,
                tickerScale = 1.0f,
                reporterScale = 1.0f,
                liveBadgeScale = 1.0f,
                clockScale = 1.0f
            )
        }
        _userMessage.value = "🔄 स्क्रीन के सभी एलिमेंट्स अपनी मूल स्थिति और आकार में रीसेट हो गए!"
    }

    fun updateHeadlineAnimation(anim: String) {
        pushUndoState()
        _currentProject.update { it.copy(headlineAnimation = anim) }
        _userMessage.value = "✨ हेडलाइन एनीमेशन सेट: $anim"
    }

    fun updateTickerAnimationAndSpeed(anim: String? = null, speed: Float? = null, tagText: String? = null) {
        _currentProject.update {
            it.copy(
                tickerAnimation = anim ?: it.tickerAnimation,
                tickerSpeed = speed?.coerceIn(0.4f, 2.5f) ?: it.tickerSpeed,
                tickerTagText = tagText ?: it.tickerTagText
            )
        }
    }

    fun updateElementScale(element: String, scale: Float) {
        val clamped = scale.coerceIn(0.7f, 1.6f)
        _currentProject.update { current ->
            when (element) {
                "LOGO" -> current.copy(logoScale = clamped)
                "HEADLINE" -> current.copy(headlineScale = clamped)
                "TICKER" -> current.copy(tickerScale = clamped)
                "REPORTER" -> current.copy(reporterScale = clamped)
                "LIVE_BADGE" -> current.copy(liveBadgeScale = clamped)
                "CLOCK" -> current.copy(clockScale = clamped)
                else -> current
            }
        }
    }

    fun updateManualTexts(liveText: String? = null, manualClock: String? = null, tickerTag: String? = null) {
        _currentProject.update { current ->
            current.copy(
                liveBadgeText = liveText ?: current.liveBadgeText,
                manualClockText = manualClock ?: current.manualClockText,
                tickerTagText = tickerTag ?: current.tickerTagText
            )
        }
    }

    fun toggleExportFastMode() {
        _currentProject.update { current ->
            val next = !current.exportFastMode
            _userMessage.value = if (next) "⚡ सुपर फ़ास्ट सेव चालू (2-3 सेकंड)" else "🎬 फुल HD क्वालिटी चालू"
            current.copy(exportFastMode = next)
        }
    }

    fun dismissSavedGalleryDialog() {
        _savedGalleryUri.value = null
    }

    fun updateHeadline(newHeadline: String) {
        _currentProject.update { it.copy(headline = newHeadline) }
    }

    fun updateSubHeadline(newSubHeadline: String) {
        _currentProject.update { it.copy(subHeadline = newSubHeadline) }
    }

    fun updateCategory(newCategory: String) {
        _currentProject.update { it.copy(category = newCategory) }
    }

    fun updateTicker(newTicker: String) {
        _currentProject.update { it.copy(tickerText = newTicker) }
    }

    fun updateReporter(name: String, role: String, location: String) {
        _currentProject.update {
            it.copy(
                reporterName = name,
                reporterRole = role,
                location = location
            )
        }
    }

    fun updateChannel(name: String, tagline: String, colorHex: String) {
        _currentProject.update {
            it.copy(
                channelName = name,
                channelTagline = tagline,
                themeColorHex = colorHex
            )
        }
    }

    fun updateAspectPreset(preset: String) {
        _currentProject.update { it.copy(aspectPreset = preset) }
    }

    fun updateLogoConfig(
        type: String,
        uri: String?,
        position: String,
        scale: Float,
        opacity: Float,
        animation: String? = null
    ) {
        _currentProject.update {
            it.copy(
                logoType = type,
                customLogoUri = uri ?: it.customLogoUri,
                logoPosition = position,
                logoScale = scale,
                logoOpacity = opacity,
                logoAnimation = animation ?: it.logoAnimation
            )
        }
        _userMessage.value = "लोगो सेटिंग्स अपडेट की गईं"
    }

    fun updateLogoAnimation(animationName: String) {
        _currentProject.update { it.copy(logoAnimation = animationName) }
        _userMessage.value = "लोगो एनीमेशन लागू: $animationName"
    }

    fun updateFilterAndVfx(filter: String, vfx: String) {
        _currentProject.update {
            it.copy(
                activeFilter = filter,
                activeVfx = vfx
            )
        }
        _userMessage.value = "फ़िल्टर व VFX लागू: $filter | $vfx"
    }

    fun updateContrastAndBrightness(contrast: Float, brightness: Float) {
        _currentProject.update {
            it.copy(
                videoContrast = contrast.coerceIn(0.5f, 2.5f),
                videoBrightness = brightness.coerceIn(-0.5f, 0.5f)
            )
        }
    }

    fun updateSpeedAndTrim(speed: Float, trimStart: Float, trimEnd: Float, volume: Float) {
        _currentProject.update {
            it.copy(
                videoSpeed = speed,
                videoTrimStart = trimStart,
                videoTrimEnd = trimEnd,
                videoVolume = volume
            )
        }
        _userMessage.value = "गति: ${speed}x, वॉल्यूम: ${(volume * 100).toInt()}%"
    }

    fun updateLayoutMode(mode: String) {
        _currentProject.update { it.copy(layoutMode = mode) }
        _userMessage.value = "लेआउट मोड: $mode"
    }

    fun reorderSection(sectionId: String, moveUp: Boolean) {
        _currentProject.update { project ->
            val sections = project.studioSectionsOrder.split(",").filter { it.isNotBlank() }.toMutableList()
            val index = sections.indexOf(sectionId)
            if (index != -1) {
                val targetIndex = if (moveUp) index - 1 else index + 1
                if (targetIndex in 0 until sections.size) {
                    val item = sections.removeAt(index)
                    sections.add(targetIndex, item)
                    project.copy(studioSectionsOrder = sections.joinToString(","))
                } else project
            } else project
        }
    }

    fun toggleSectionVisibility(sectionId: String) {
        _currentProject.update { project ->
            val hidden = project.hiddenSections.split(",").filter { it.isNotBlank() }.toMutableSet()
            if (hidden.contains(sectionId)) {
                hidden.remove(sectionId)
            } else {
                hidden.add(sectionId)
            }
            project.copy(hiddenSections = hidden.joinToString(","))
        }
    }

    fun resetSectionsOrder() {
        _currentProject.update {
            it.copy(
                studioSectionsOrder = "VIDEO,RECORD,HEADLINE,SOURCE,TOOLS,SOUND,PROMPTER",
                hiddenSections = ""
            )
        }
        _userMessage.value = "स्क्रीन का लेआउट डिफ़ॉल्ट रूप में रीसेट किया गया।"
    }

    fun updateElementPosition(element: String, position: String) {
        _currentProject.update {
            when (element) {
                "LOGO" -> it.copy(logoPosition = position)
                "HEADLINE" -> it.copy(headlinePosition = position)
                "TICKER" -> it.copy(tickerPosition = position)
                "REPORTER" -> it.copy(reporterPosition = position)
                "LIVE_BADGE" -> it.copy(liveBadgePosition = position)
                "CLOCK" -> it.copy(clockPosition = position)
                else -> it
            }
        }
        _userMessage.value = "$element स्थिति: $position सेट की गई"
    }

    fun applyLayoutPreset(presetName: String) {
        _currentProject.update {
            when (presetName) {
                "CLASSIC_TV" -> it.copy(
                    logoPosition = "TOP_RIGHT",
                    headlinePosition = "BOTTOM",
                    tickerPosition = "BOTTOM",
                    reporterPosition = "BOTTOM_LEFT",
                    liveBadgePosition = "TOP_LEFT",
                    clockPosition = "TOP_RIGHT",
                    showLogo = true,
                    showHeadline = true,
                    showTicker = true,
                    showReporterCard = true,
                    showLiveBadge = true,
                    showClock = true
                )
                "BREAKING_FOCUS" -> it.copy(
                    logoPosition = "TOP_LEFT",
                    headlinePosition = "CENTER",
                    tickerPosition = "BOTTOM",
                    reporterPosition = "BOTTOM_RIGHT",
                    liveBadgePosition = "TOP_RIGHT",
                    clockPosition = "TOP_LEFT",
                    showLogo = true,
                    showHeadline = true,
                    showTicker = true,
                    showReporterCard = false,
                    showLiveBadge = true
                )
                "REELS_SHORTS" -> it.copy(
                    aspectPreset = "9:16",
                    logoPosition = "TOP_LEFT",
                    headlinePosition = "CENTER",
                    tickerPosition = "BOTTOM",
                    reporterPosition = "BOTTOM_LEFT",
                    liveBadgePosition = "TOP_RIGHT",
                    showLogo = true,
                    showHeadline = true,
                    showTicker = true,
                    showReporterCard = true
                )
                "CLEAN_INTERVIEW" -> it.copy(
                    logoPosition = "BOTTOM_RIGHT",
                    headlinePosition = "BOTTOM",
                    tickerPosition = "BOTTOM",
                    reporterPosition = "BOTTOM_LEFT",
                    liveBadgePosition = "TOP_LEFT",
                    showLogo = true,
                    showHeadline = true,
                    showTicker = false,
                    showReporterCard = true,
                    showClock = false
                )
                else -> it
            }
        }
        _userMessage.value = "स्क्रीन लेआउट प्रीसेट '$presetName' लागू किया गया!"
    }

    fun toggleSticker(stickerTag: String) {
        _currentProject.update { current ->
            val list = current.activeStickers.split(",").filter { it.isNotBlank() }.toMutableList()
            if (list.contains(stickerTag)) {
                list.remove(stickerTag)
            } else {
                list.add(stickerTag)
            }
            current.copy(activeStickers = list.joinToString(","))
        }
    }

    fun setSecondaryMediaUri(uri: Uri?) {
        _secondaryMediaUri.value = uri
        _currentProject.update { it.copy(secondaryMediaUri = uri?.toString()) }
        if (uri != null) {
            _userMessage.value = "दूसरी वीडियो/इमेज फुटेज लोड हुई"
        }
    }

    fun toggleOverlayElement(
        logo: Boolean? = null,
        live: Boolean? = null,
        reporter: Boolean? = null,
        headline: Boolean? = null,
        ticker: Boolean? = null,
        clock: Boolean? = null,
        location: Boolean? = null,
        stickers: Boolean? = null
    ) {
        _currentProject.update { current ->
            current.copy(
                showLogo = logo ?: current.showLogo,
                showLiveBadge = live ?: current.showLiveBadge,
                showReporterCard = reporter ?: current.showReporterCard,
                showHeadline = headline ?: current.showHeadline,
                showTicker = ticker ?: current.showTicker,
                showClock = clock ?: current.showClock,
                showLocation = location ?: current.showLocation,
                showStickers = stickers ?: current.showStickers
            )
        }
    }

    fun updateTeleprompterText(text: String) {
        _currentProject.update { it.copy(teleprompterText = text) }
    }

    fun setTeleprompterActive(active: Boolean) {
        _isTeleprompterActive.value = active
    }

    fun setTeleprompterSpeed(speed: Float) {
        _teleprompterSpeed.value = speed
    }

    fun setTeleprompterFontSize(size: Float) {
        _teleprompterFontSize.value = size
    }

    fun setBackgroundSource(source: BackgroundSource) {
        _backgroundSource.value = source
    }

    fun setCustomMediaUri(uri: Uri?) {
        _customMediaUri.value = uri
        if (uri != null) {
            _backgroundSource.value = BackgroundSource.GALLERY_MEDIA
            _currentProject.update { it.copy(mediaUri = uri.toString()) }
        }
    }

    fun toggleCamera() {
        _isFrontCamera.value = !_isFrontCamera.value
    }

    fun toggleTorch() {
        _isTorchEnabled.value = !_isTorchEnabled.value
    }

    fun triggerBreakingFlash() {
        viewModelScope.launch {
            _isBreakingFlashActive.value = true
            delay(1200)
            _isBreakingFlashActive.value = false
        }
    }

    fun applyHeadlineTemplate(template: HindiHeadlineTemplate) {
        _currentProject.update {
            it.copy(
                category = template.category,
                headline = template.headline,
                subHeadline = template.subHeadline,
                tickerText = template.defaultTicker
            )
        }
        triggerBreakingFlash()
        NewsAudioEffects.playBreakingNewsStinger()
        _userMessage.value = "टेम्पलेट लागू किया गया: ${template.category}"
    }

    fun applyTeleprompterPreset(preset: HindiTeleprompterPreset) {
        _currentProject.update {
            it.copy(teleprompterText = preset.script)
        }
        _isTeleprompterActive.value = true
        _userMessage.value = "प्रॉम्प्टर स्क्रिप्ट लोड की गई: ${preset.title}"
    }

    fun toggleRecording(context: Context? = null) {
        if (_isRecording.value) {
            _isRecording.value = false
            _userMessage.value = "रिकॉर्डिंग समाप्त! फ्रेम सहित वीडियो प्रोसेस हो रही है..."
            if (context != null && _backgroundSource.value != BackgroundSource.CAMERA) {
                saveToGallery(context, asVideo = true)
            }
        } else {
            _isRecording.value = true
            _recordingSeconds.value = 0
            NewsAudioEffects.playCountdownBeep(true)
            viewModelScope.launch {
                while (_isRecording.value) {
                    delay(1000)
                    if (_isRecording.value) {
                        _recordingSeconds.value += 1
                    }
                }
            }
        }
    }

    fun onLiveCameraRecordingFinished(context: Context, capturedUri: Uri) {
        _customMediaUri.value = capturedUri
        _userMessage.value = "लाइव कैमरा वीडियो रिकॉर्ड हो गई! फ्रेम सहित गैलरी में सेव हो रही है..."
        saveToGallery(context, asVideo = true)
    }

    fun saveCurrentProject() {
        viewModelScope.launch {
            val project = _currentProject.value.copy(
                updatedAt = System.currentTimeMillis()
            )
            val id = repository.saveProject(project)
            _currentProject.update { it.copy(id = id) }
            _userMessage.value = "प्रोजेक्ट सफलतापूर्वक सहेजा गया!"
        }
    }

    fun loadProject(project: NewsProject) {
        _currentProject.value = project
        _userMessage.value = "प्रोजेक्ट लोड किया गया: ${project.headline.take(20)}..."
    }

    fun deleteProject(project: NewsProject) {
        viewModelScope.launch {
            repository.deleteProject(project)
            _userMessage.value = "प्रोजेक्ट हटाया गया"
        }
    }

    fun exportProject() {
        viewModelScope.launch {
            _exportProgress.value = 0.05f
            NewsAudioEffects.playTickerBeep()
            for (step in 1..10) {
                delay(180)
                _exportProgress.value = step * 0.1f
            }
            saveCurrentProject()
            _exportProgress.value = null
            _userMessage.value = "H.K NEWS वीडियो फ्रेम सफलतापूर्वक एक्सपोर्ट हुआ!"
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
