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
    }
}
