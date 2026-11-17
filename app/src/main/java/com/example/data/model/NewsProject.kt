package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news_projects")
data class NewsProject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String = "ब्रेकिंग न्यूज़",
    val headline: String,
    val subHeadline: String = "",
    val tickerText: String,
    val reporterName: String = "राजेश शर्मा",
    val reporterRole: String = "विशेष संवाददाता",
    val location: String = "नरसिंहपुर",
    val channelName: String = "हर खबर H.K NEWS",
    val channelTagline: String = "नरसिंहपुर समाचार • सच की आवाज़",
    val themeColorHex: String = "#C62828",
    val mediaUri: String? = null,
    val secondaryMediaUri: String? = null,
    val aspectPreset: String = "16:9", // "16:9", "9:16", "1:1", "4:5"
    val showLogo: Boolean = true,
    val logoType: String = "HK_NEWS", // "HK_NEWS", "CUSTOM_IMAGE", "TEXT_BADGE"
    val customLogoUri: String? = null,
    val logoPosition: String = "TOP_RIGHT", // "TOP_RIGHT", "TOP_LEFT", "BOTTOM_RIGHT", "BOTTOM_LEFT"
    val logoScale: Float = 1.0f,
    val logoOpacity: Float = 0.95f,
    val logoAnimation: String = "SHINE_SWEEP", // "NONE", "SHINE_SWEEP", "SPIN_3D", "PULSE_GLOW", "NEON_BORDER", "FLOAT_BOB", "HOLOGRAM_GLITCH", "BOUNCE_POP", "GOLDEN_FLARE", "RADIAL_RIPPLE", "TV_SCANLINE", "SPARKLE_BURST", "CHROME_BEVEL"
    val activeFilter: String = "NONE",
    val activeVfx: String = "NONE",
    val videoContrast: Float = 1.0f, // 0.5f to 2.0f (1.0f is default/normal)
    val videoBrightness: Float = 0.0f, // -0.5f to +0.5f (0.0f is normal)
    val videoSpeed: Float = 1.0f,
    val videoTrimStart: Float = 0.0f,
    val videoTrimEnd: Float = 1.0f,
    val videoVolume: Float = 1.0f,
    val layoutMode: String = "FULLSCREEN", // "FULLSCREEN", "SPLIT_SCREEN", "PIP_CORNER"
    val activeStickers: String = "LIVE", // comma-separated tags
    val helplineNumber: String = "ख़बर हेतु संपर्क: 94XXXXXXXX",
    val showReporterCard: Boolean = true,
    val showTicker: Boolean = true,
    val showHeadline: Boolean = true,
    val showClock: Boolean = true,
    val showLiveBadge: Boolean = true,
    val headlinePosition: String = "BOTTOM", // "BOTTOM", "CENTER", "TOP"
    val tickerPosition: String = "BOTTOM", // "BOTTOM", "TOP"
    val reporterPosition: String = "BOTTOM_LEFT", // "BOTTOM_LEFT", "BOTTOM_RIGHT"
    val liveBadgePosition: String = "TOP_LEFT", // "TOP_LEFT", "TOP_RIGHT"
    val clockPosition: String = "TOP_RIGHT", // "TOP_RIGHT", "TOP_LEFT"
    val studioSectionsOrder: String = "VIDEO,RECORD,HEADLINE,SOURCE,TOOLS,SOUND,PROMPTER",
    val hiddenSections: String = "",
    val teleprompterText: String = "",
    val logoOffsetX: Float = 0f,
    val logoOffsetY: Float = 0f,
    val headlineOffsetX: Float = 0f,
    val headlineOffsetY: Float = 0f,
    val tickerOffsetX: Float = 0f,
    val tickerOffsetY: Float = 0f,
    val reporterOffsetX: Float = 0f,
    val reporterOffsetY: Float = 0f,
    val liveBadgeOffsetX: Float = 0f,
    val liveBadgeOffsetY: Float = 0f,
    val locationOffsetX: Float = 0f,
    val locationOffsetY: Float = 0f,
    val clockOffsetX: Float = 0f,
    val clockOffsetY: Float = 0f,
    val exportFastMode: Boolean = true,
    // KineMaster Editing Suite Features
    val chromaKeyEnabled: Boolean = false,
    val chromaKeyColor: String = "#00FF00", // Green / Blue Screen
    val chromaKeySimilarity: Float = 0.45f,
    val voiceChangerEffect: String = "NORMAL", // "NORMAL", "DEEP_ANCHOR", "RADIO_MIC", "ECHO_STUDIO", "CHIPMUNK", "ROBOT"
    val activeTransition: String = "NONE", // "NONE", "3D_NEWS_FLIP", "CUBE_ROTATE", "WIPE_RIGHT", "ZOOM_BURST", "CROSSFADE", "GLITCH_TV"
    val inAnimation: String = "FADE_IN", // "NONE", "FADE_IN", "POP_UP", "SLIDE_RIGHT", "SLIDE_UP", "SPIN_CLOCKWISE"
    val outAnimation: String = "FADE_OUT", // "NONE", "FADE_OUT", "SLIDE_LEFT", "SLIDE_DOWN"
    val panZoomScale: Float = 1.0f,
    val panZoomStartScale: Float = 1.0f,
    val panZoomEndScale: Float = 1.25f,
    val panZoomLocked: Boolean = false,
    val panZoomStartX: Float = 0f,
    val panZoomStartY: Float = 0f,
    val panZoomEndX: Float = 0f,
    val panZoomEndY: Float = 0f,
    val audioVolumeBoost: Float = 1.0f, // 0.0f to 2.0f (up to 200% volume boost)
    val audioPitch: Float = 1.0f, // 0.5f to 1.5f pitch shift
    val audioAutoDucking: Boolean = true,
    val audioFadeIn: Boolean = true,
    val audioFadeOut: Boolean = true,
    val handwritingStrokes: String = "",
    val keyframeEnabled: Boolean = false,
    val keyframeData: String = "",
    val exportResolution: String = "720p", // "480p", "720p", "1080p"
    val exportFps: Int = 30, // 24, 30, 60
    // Headline & Breaking News Strip Animations + Full Manual Screen Controls
    val headlineAnimation: String = "SHINE_SWEEP", // "NONE", "SHINE_SWEEP", "PULSE_GLOW", "SLIDE_REVEAL", "NEON_FLASH", "ZOOM_POP"
    val tickerAnimation: String = "BREAKING_SWEEP", // "NONE", "BREAKING_SWEEP", "PULSE_BORDER", "FLASH_TAG", "WAVE_GLOW"
    val tickerSpeed: Float = 1.0f, // 0.4f to 2.5f
    val tickerTagText: String = "⚡ ताज़ा अपडेट",
    val showLocation: Boolean = true,
    val showStickers: Boolean = true,
    val liveBadgeText: String = "LIVE",
    val manualClockText: String = "",
    val headlineScale: Float = 1.0f,
    val tickerScale: Float = 1.0f,
    val reporterScale: Float = 1.0f,
    val liveBadgeScale: Float = 1.0f,
    val clockScale: Float = 1.0f,
    val stickersOffsetX: Float = 0f,
    val stickersOffsetY: Float = 0f,
    val timelineCurrentTimeMs: Long = 0L,
    val videoTotalDurationMs: Long = 10000L,
    val updatedAt: Long = System.currentTimeMillis()
)

data class HindiHeadlineTemplate(
    val category: String,
    val headline: String,
    val subHeadline: String,
    val defaultTicker: String
)

data class HindiTeleprompterPreset(
    val title: String,
    val category: String,
    val script: String
)

data class VideoFilterInfo(
    val id: String,
    val name: String,
    val description: String,
    val previewTint: Long
)

data class VideoVfxInfo(
    val id: String,
    val name: String,
    val iconName: String
)
