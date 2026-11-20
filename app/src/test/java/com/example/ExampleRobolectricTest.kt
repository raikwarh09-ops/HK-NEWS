package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.NewsProject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("HK NEWS", appName)
        assertEquals("Powered by Raikwar ji", context.getString(R.string.powered_by_raikwar_ji))
    }

    @Test
    fun `test news project creation and defaults`() {
        val project = NewsProject(
            title = "ताज़ा समाचार",
            category = "ब्रेकिंग न्यूज़",
            headline = "बड़ा प्रशासनिक फेरबदल",
            subHeadline = "कई अधिकारियों के तबादले",
            tickerText = "पल-पल की ताज़ा अपडेट",
            reporterName = "राजेश शर्मा",
            channelName = "भारत 24"
        )
        assertEquals("बड़ा प्रशासनिक फेरबदल", project.headline)
        assertEquals("भारत 24", project.channelName)
        assertTrue(project.showTicker)
        assertTrue(project.showLiveBadge)
        assertEquals("HK_NEWS", project.logoType)
        assertEquals("NONE", project.activeFilter)
        assertEquals(1.0f, project.videoContrast)
        assertEquals(0.0f, project.videoBrightness)
        assertTrue(project.exportFastMode)
        assertEquals(0f, project.logoOffsetX)
        assertEquals(1.0f, project.panZoomStartScale)
        assertEquals(1.25f, project.panZoomEndScale)
        assertEquals(1.0f, project.audioVolumeBoost)
        assertEquals("720p", project.exportResolution)
        assertEquals(30, project.exportFps)
        assertEquals("NORMAL", project.voiceChangerEffect)
        assertEquals("SHINE_SWEEP", project.headlineAnimation)
        assertEquals("BREAKING_SWEEP", project.tickerAnimation)
        assertEquals(1.0f, project.tickerSpeed)
        assertTrue(project.showLocation)
        assertTrue(project.showStickers)
        assertEquals(1.0f, project.headlineScale)
        assertEquals(1.0f, project.tickerScale)
        assertEquals("LIVE", project.liveBadgeText)
        assertEquals("⚡ ताज़ा अपडेट", project.tickerTagText)
        // VN Video Editor defaults
        assertEquals("NONE", project.vnSpeedCurve)
        assertEquals(0f, project.vnRotationDegrees)
        assertEquals(false, project.vnMirrorHorizontal)
        assertEquals(false, project.vnFlipVertical)
        assertEquals("BLACK", project.vnBackgroundStyle)
        assertEquals(0f, project.vnBorderWidthDp)
        assertEquals(1.0f, project.vnSaturation)
        assertEquals(0.0f, project.vnWarmth)
        assertEquals("NONE", project.vnMotionFx)
        assertEquals("NONE", project.vnMaskType)
        assertEquals(1.0f, project.vnVideoOpacity)
        assertEquals(false, project.vnFreezeFrameActive)
        assertEquals(false, project.vnReversePlayback)
    }

    @Test
    fun `vn video editor tools update viewmodel state cleanly`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.viewmodel.NewsStudioViewModel(app)
        vm.updateVnSpeedCurve("HERO")
        vm.rotateVnVideo90()
        vm.toggleVnMirrorHorizontal()
        vm.updateVnBackgroundAndBorder(bgStyle = "NAVY_STUDIO", borderWidthDp = 6f, borderColorHex = "#00E5FF")
        vm.updateVnAdjustments(saturation = 1.4f, warmth = 0.25f, vignette = 0.5f)
        vm.updateVnMotionFx("SHAKE")
        vm.updateVnMaskAndOpacity(maskType = "RECTANGLE", opacity = 0.85f)
        vm.toggleVnReversePlayback()
        vm.updateVnSubtitleAndBeat(subtitleText = "ब्रेकिंग रिपोर्ट", subtitleStyle = "YELLOW_BOLD", beatMarkersEnabled = true)

        val proj = vm.currentProject.value
        assertEquals("HERO", proj.vnSpeedCurve)
        assertEquals(90f, proj.vnRotationDegrees)
        assertTrue(proj.vnMirrorHorizontal)
        assertEquals("NAVY_STUDIO", proj.vnBackgroundStyle)
        assertEquals(6f, proj.vnBorderWidthDp)
        assertEquals(1.4f, proj.vnSaturation)
        assertEquals(0.25f, proj.vnWarmth)
        assertEquals(0.5f, proj.vnVignette)
        assertEquals("SHAKE", proj.vnMotionFx)
        assertEquals("RECTANGLE", proj.vnMaskType)
        assertEquals(0.85f, proj.vnVideoOpacity)
        assertTrue(proj.vnReversePlayback)
        assertEquals("ब्रेकिंग रिपोर्ट", proj.vnSubtitleText)
        assertEquals("YELLOW_BOLD", proj.vnSubtitleStyle)
        assertTrue(proj.vnBeatMarkersEnabled)

        vm.resetVnTools()
        val resetProj = vm.currentProject.value
        assertEquals("NONE", resetProj.vnSpeedCurve)
        assertEquals(0f, resetProj.vnRotationDegrees)
        assertEquals(false, resetProj.vnMirrorHorizontal)
    }

    @Test
    fun `gallery saver rejects empty or missing video files cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val missingFile = java.io.File(context.cacheDir, "does_not_exist.mp4")
        val missingRes = com.example.util.GalleryMediaSaver.saveLocalFileToVideoGallery(context, missingFile, "Test")
        assertEquals(null, missingRes)

        val emptyFile = java.io.File(context.cacheDir, "empty_video.mp4").apply {
            writeBytes(ByteArray(0))
        }
        val emptyRes = com.example.util.GalleryMediaSaver.saveLocalFileToVideoGallery(context, emptyFile, "Test")
        assertEquals(null, emptyRes)
        emptyFile.delete()
    }

    @Test
    fun `gallery saver saves valid non-empty edited mp4 file directly to media store`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val validMp4 = java.io.File(context.cacheDir, "edited_test_video.mp4").apply {
            // Write minimal ftyp box header bytes so file is non-empty and valid for storage
            writeBytes(byteArrayOf(0x00, 0x00, 0x00, 0x18, 0x66, 0x74, 0x79, 0x70, 0x6D, 0x70, 0x34, 0x32, 0x00, 0x00, 0x00, 0x00))
        }
        val uri = com.example.util.GalleryMediaSaver.saveLocalFileToVideoGallery(context, validMp4, "ताज़ा न्यूज़ एडिटेड वीडियो")
        assertNotNull(uri)
        validMp4.delete()
    }

    @Test
    fun `pc studio html software is bundled in android assets`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val htmlText = context.assets.open("HK_NEWS_PC_Studio.html").bufferedReader().use { it.readText() }
        assertTrue(htmlText.contains("HK NEWS PC Studio Pro"))
        assertTrue(htmlText.contains("studioCanvas"))
        assertTrue(htmlText.contains("MediaRecorder"))
        assertTrue(htmlText.contains("7200"))
    }

    @Test
    fun `supports video duration up to 2 hours cleanly`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.viewmodel.NewsStudioViewModel(app)

        // Exact 2 hours (7,200,000 ms)
        vm.setTargetVideoDurationMs(7_200_000L)
        assertEquals(7_200_000L, vm.timelineDurationMs.value)
        assertEquals(7_200_000L, vm.currentProject.value.videoTotalDurationMs)

        // Clamps above 2 hours safely
        vm.setTargetVideoDurationMs(10_800_000L)
        assertEquals(7_200_000L, vm.timelineDurationMs.value)

        // Adaptive profile keeps 2-hour export safe and compact
        val profile = com.example.util.NewsVideoCompositor.resolveAdaptiveExportProfile(
            trimmedDurationMs = 7_200_000L,
            isFast = true,
            requestedFps = 30,
            videoWidth = 848,
            videoHeight = 480
        )
        assertEquals(2, profile.frameRate)
        assertEquals(2, profile.iFrameIntervalSec)
        assertTrue(profile.minSampleStepUs >= 1_000_000L)
    }

    @Test
    fun `app lock manager enforces installation password 987654321123456789`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.util.AppLockManager.lockNow(context)
        com.example.util.AppLockManager.setRequireEachLaunch(context, false)

        // Fresh install / locked state must be locked initially
        assertEquals(false, com.example.util.AppLockManager.isAppUnlocked(context))

        // Wrong password rejected
        assertEquals(false, com.example.util.AppLockManager.unlockWithPassword(context, "123456"))
        assertEquals(false, com.example.util.AppLockManager.isAppUnlocked(context))

        // Exact password unlocks cleanly
        assertTrue(com.example.util.AppLockManager.unlockWithPassword(context, "987654321123456789"))
        assertTrue(com.example.util.AppLockManager.isAppUnlocked(context))

        // PC Studio software bundles matching password lock gate
        val htmlText = context.assets.open("HK_NEWS_PC_Studio.html").bufferedReader().use { it.readText() }
        assertTrue(htmlText.contains("pcLockOverlay"))
        assertTrue(htmlText.contains("84c390dad5edd0bb361f68747f3a1e5d2cddeb8dde209147b00d929c1a80b7c8"))
    }

    @Test
    fun `studio tools are cleanly organized across menu categories`() {
        val categories = com.example.ui.components.StudioMenuCategory.entries
        assertEquals(7, categories.size)
        assertTrue(categories.any { it.id == "kinemaster" })
        assertTrue(categories.any { it.id == "vn_tools" })
        assertTrue(categories.any { it.id == "news_screen" })
        assertTrue(categories.any { it.id == "effects_logo" })
        assertTrue(categories.any { it.id == "media_audio" })
        assertTrue(categories.any { it.id == "record_export" })
        assertTrue(categories.any { it.id == "all_tools" })

        val context = ApplicationProvider.getApplicationContext<Context>()
        val htmlText = context.assets.open("HK_NEWS_PC_Studio.html").bufferedReader().use { it.readText() }
        assertTrue(htmlText.contains("tab-mediasave"))
        assertTrue(htmlText.contains("animFilter"))
        assertTrue(htmlText.contains("cropPreset"))
        assertTrue(htmlText.contains("voiceEffect"))
        assertTrue(htmlText.contains("voicePitch"))
    }

    @Test
    fun `animation filter, video crop, and voice effect presets update cleanly`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.viewmodel.NewsStudioViewModel(app)

        // Animation Filters
        vm.updateAnimationFilter("NEON_PULSE_WAVE")
        assertEquals("NEON_PULSE_WAVE", vm.currentProject.value.activeAnimationFilter)

        // Video Crop with range clamping
        vm.updateVideoCrop(
            preset = "1:1",
            zoom = 1.6f,
            panX = 1.5f, // clamps to 1.0f
            panY = -0.2f,
            left = 0.1f,
            top = 0.05f,
            right = 0.1f,
            bottom = 0.05f
        )
        val cropped = vm.currentProject.value
        assertEquals("1:1", cropped.videoCropPreset)
        assertEquals(1.6f, cropped.videoCropZoom)
        assertEquals(1.0f, cropped.videoCropPanX)
        assertEquals(-0.2f, cropped.videoCropPanY)
        assertEquals(0.1f, cropped.videoCropLeft)

        vm.resetVideoCrop()
        val uncropped = vm.currentProject.value
        assertEquals("FULL", uncropped.videoCropPreset)
        assertEquals(1.0f, uncropped.videoCropZoom)
        assertEquals(0f, uncropped.videoCropPanX)

        // Voice Effects + Manual Pitch
        vm.updateVoiceChanger("DEEP_ANCHOR")
        assertEquals("DEEP_ANCHOR", vm.currentProject.value.voiceChangerEffect)
        assertEquals(0.76f, vm.currentProject.value.audioPitch)

        vm.updateVoiceChanger("CHIPMUNK")
        assertEquals("CHIPMUNK", vm.currentProject.value.voiceChangerEffect)
        assertEquals(1.55f, vm.currentProject.value.audioPitch)

        vm.updateVoicePitch(1.35f)
        assertEquals(1.35f, vm.currentProject.value.audioPitch)
    }

    @Test
    fun `compositor color matrix and pcm voice dsp processor transform audio and color safely`() {
        val project = NewsProject(
            title = "टेस्ट न्यूज़",
            headline = "ब्रेकिंग ख़बर",
            tickerText = "ताज़ा अपडेट लगातार जारी",
            activeFilter = "BW",
            videoBrightness = 0.1f,
            videoContrast = 1.2f,
            vnSaturation = 1.0f,
            vnWarmth = 0.2f,
            voiceChangerEffect = "ROBOT",
            audioPitch = 0.86f,
            audioVolumeBoost = 1.5f
        )
        val cm = com.example.util.NewsVideoCompositor.buildExportColorMatrix(project)
        assertEquals(20, cm.array.size)

        // PCM Voice DSP verification (Robot ring-mod + Echo + Low-pass)
        val dsp = com.example.util.NewsVideoCompositor.PcmVoiceDspProcessor(
            sampleRate = 44100,
            channels = 1,
            voiceEffect = "ROBOT",
            pitch = 0.86f,
            gain = 1.4f,
            fadeIn = false,
            fadeOut = false,
            totalDurationUs = 2_000_000L
        )
        val samples = ShortArray(1024) { i -> ((kotlin.math.sin(i * 0.1) * 8000.0).toInt()).toShort() }
        dsp.processInPlace(samples, 500_000L)
        assertTrue(samples.any { it != 0.toShort() })

        // Zero-gain DSP outputs silence cleanly
        val mutedDsp = com.example.util.NewsVideoCompositor.PcmVoiceDspProcessor(
            sampleRate = 44100,
            channels = 1,
            voiceEffect = "NORMAL",
            pitch = 1.0f,
            gain = 0.0f,
            fadeIn = false,
            fadeOut = false,
            totalDurationUs = 2_000_000L
        )
        mutedDsp.processInPlace(samples, 500_000L)
        assertTrue(samples.all { it == 0.toShort() })
    }

    @Test
    fun `drag delta accumulation, trim mutual clamping, and split cut work reliably`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.viewmodel.NewsStudioViewModel(app)

        // Cumulative delta drag
        vm.applyElementDragDelta("LOGO", 12.5f, -8.0f)
        vm.applyElementDragDelta("LOGO", 7.5f, 18.0f)
        assertEquals(20.0f, vm.currentProject.value.logoOffsetX, 0.01f)
        assertEquals(10.0f, vm.currentProject.value.logoOffsetY, 0.01f)
        vm.resetAllElementPositions()
        assertEquals(0f, vm.currentProject.value.logoOffsetX, 0.01f)

        // Split cut sets marker and transition
        vm.setTargetVideoDurationMs(20_000L)
        vm.seekTimelineTo(8_000L)
        vm.splitTimelineAtPlayhead()
        val splitRatio = vm.splitPointRatio.value
        assertNotNull(splitRatio)
        assertEquals(0.4f, splitRatio!!, 0.02f)
        assertEquals("3D_NEWS_FLIP", vm.currentProject.value.activeTransition)

        // Trim left & right enforce minimum 3% span without crossing
        vm.seekTimelineTo(16_000L)
        vm.trimTimelineLeft()
        vm.seekTimelineTo(4_000L)
        vm.trimTimelineRight()
        val p = vm.currentProject.value
        assertTrue(p.videoTrimEnd - p.videoTrimStart >= 0.029f)
        assertTrue(p.videoTrimStart >= 0f && p.videoTrimEnd <= 1f)
    }

    @Test
    fun `shared preview and export color matrices handle BW CINEMA WARM and sharpen consistently`() {
        listOf("BW", "CINEMA", "WARM", "VIVID", "CINE_TEAL").forEach { filterId ->
            val previewCm = com.example.ui.components.VideoFilterPalettes.buildAndroidColorMatrix(
                filterId = filterId,
                contrast = 1.15f,
                brightness = 0.05f,
                saturation = 1.1f,
                warmth = 0.15f,
                sharpen = 0.5f
            )
            val exportCm = com.example.util.NewsVideoCompositor.buildExportColorMatrix(
                NewsProject(
                    title = "Test",
                    headline = "टेस्ट ब्रेकिंग न्यूज़",
                    tickerText = "ताज़ा अपडेट लगातार",
                    activeFilter = filterId,
                    videoContrast = 1.15f,
                    videoBrightness = 0.05f,
                    vnSaturation = 1.1f,
                    vnWarmth = 0.15f,
                    vnSharpen = 0.5f
                )
            )
            for (i in 0 until 20) {
                assertEquals("Mismatch at $filterId[$i]", previewCm.array[i], exportCm.array[i], 0.001f)
            }
        }

        // PCM speed resampler keeps duration ratios accurate
        val src = ShortArray(1000) { i -> (i % 100).toShort() }
        val fast = com.example.util.NewsVideoCompositor.resampleInterleavedPcm(src, channels = 1, speedFactor = 2.0f)
        val slow = com.example.util.NewsVideoCompositor.resampleInterleavedPcm(src, channels = 1, speedFactor = 0.5f)
        assertEquals(500, fast.size)
        assertEquals(2000, slow.size)
    }

    @Test
    fun `chatgpt voice effects and full parity pc studio software work cleanly`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.viewmodel.NewsStudioViewModel(app)

        val expectedPitches = mapOf(
            "CHATGPT_COVE" to 0.90f,
            "CHATGPT_BREEZE" to 1.08f,
            "CHATGPT_EMBER" to 0.98f,
            "CHATGPT_JUNIPER" to 1.18f,
            "CHATGPT_SKY" to 1.14f,
            "CHATGPT_SOL" to 0.95f
        )
        expectedPitches.forEach { (effectId, expectedPitch) ->
            vm.updateVoiceChanger(effectId)
            assertEquals(effectId, vm.currentProject.value.voiceChangerEffect)
            assertEquals(expectedPitch, vm.currentProject.value.audioPitch, 0.001f)
            assertEquals(expectedPitch, com.example.audio.NewsAudioEffects.resolveDefaultPitch(effectId), 0.001f)
        }

        // PCM Voice DSP transforms audio cleanly for ChatGPT voice profiles
        listOf("CHATGPT_COVE", "CHATGPT_BREEZE", "CHATGPT_EMBER", "CHATGPT_JUNIPER", "CHATGPT_SKY", "CHATGPT_SOL").forEach { effect ->
            val dsp = com.example.util.NewsVideoCompositor.PcmVoiceDspProcessor(
                sampleRate = 44100,
                channels = 1,
                voiceEffect = effect,
                pitch = com.example.audio.NewsAudioEffects.resolveDefaultPitch(effect),
                gain = 1.2f,
                fadeIn = false,
                fadeOut = false,
                totalDurationUs = 1_500_000L
            )
            val samples = ShortArray(1024) { i -> ((kotlin.math.sin(i * 0.12) * 9000.0).toInt()).toShort() }
            dsp.processInPlace(samples, 250_000L)
            assertTrue("Expected non-silent output for $effect", samples.any { it != 0.toShort() })
        }

        // Bundled PC Studio HTML has all ChatGPT voice effects & parity controls
        val htmlText = app.assets.open("HK_NEWS_PC_Studio.html").bufferedReader().use { it.readText() }
        assertTrue(htmlText.contains("CHATGPT_COVE"))
        assertTrue(htmlText.contains("CHATGPT_BREEZE"))
        assertTrue(htmlText.contains("CHATGPT_EMBER"))
        assertTrue(htmlText.contains("CHATGPT_JUNIPER"))
        assertTrue(htmlText.contains("CHATGPT_SKY"))
        assertTrue(htmlText.contains("CHATGPT_SOL"))
        assertTrue(htmlText.contains("chatGptVoiceChips"))
        assertTrue(htmlText.contains("chromaEnabled"))
        assertTrue(htmlText.contains("showTeleprompter"))
        assertTrue(htmlText.contains("btnSaveProjectLocal"))
        assertTrue(htmlText.contains("btnToggleDraw"))
        assertTrue(htmlText.contains("btnAddKeyframe"))
    }
}

