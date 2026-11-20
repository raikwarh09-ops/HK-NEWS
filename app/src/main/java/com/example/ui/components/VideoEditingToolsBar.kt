package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.example.data.model.NewsProject
import com.example.ui.theme.NewsAmber
import com.example.ui.theme.NewsGold
import com.example.ui.theme.NewsRedPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VideoEditingToolsBar(
    project: NewsProject,
    onUpdateLogoConfig: (type: String, uri: String?, position: String, scale: Float, opacity: Float) -> Unit,
    onUpdateContrastAndBrightness: (contrast: Float, brightness: Float) -> Unit,
    onUpdateFilterAndVfx: (filter: String, vfx: String) -> Unit,
    onUpdateSpeedAndTrim: (speed: Float, trimStart: Float, trimEnd: Float, volume: Float) -> Unit,
    onUpdateLayoutMode: (mode: String) -> Unit,
    onToggleSticker: (stickerTag: String) -> Unit,
    onPickSecondaryMedia: (Uri?) -> Unit,
    onUpdateAnimationFilter: (String) -> Unit = {},
    onUpdateVideoCrop: (preset: String?, zoom: Float?, panX: Float?, panY: Float?, left: Float?, top: Float?, right: Float?, bottom: Float?) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onResetVideoCrop: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedToolTab by remember { mutableIntStateOf(0) }
    val toolTabs = listOf(
        "लोगो (Logo)",
        "कंट्रास्ट (Contrast)",
        "फ़िल्टर्स (Filters)",
        "इफ़ेक्ट्स (VFX)",
        "✨ एनिमेशन फ़िल्टर",
        "✂️ वीडियो क्रॉप",
        "स्पीड व कट (Trim)",
        "स्टिकर्स (Stickers)",
        "लेआउट (PIP)"
    )

    // Gallery picker for custom logo
    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onUpdateLogoConfig(
                "CUSTOM_IMAGE",
                uri.toString(),
                project.logoPosition,
                project.logoScale,
                project.logoOpacity
            )
        }
    }

    // Gallery picker for secondary PIP/Split video
    val secondaryMediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        onPickSecondaryMedia(uri)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_editing_tools_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = NewsGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "अनलिमिटेड वीडियो एडिटिंग टूल्स",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(NewsRedPrimary, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "PRO EDIT",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp
                    )
                }
            }

            // Sub-Tool Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedToolTab,
                containerColor = Color(0xFF0F172A),
                contentColor = NewsGold,
                edgePadding = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
            ) {
                toolTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedToolTab == index,
                        onClick = { selectedToolTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (selectedToolTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedToolTab == index) NewsGold else Color.Gray
                            )
                        }
                    )
                }
            }

            when (selectedToolTab) {
                // TAB 0: LOGO CONTROLS
                0 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "चैनल लोगो विकल्प (H.K NEWS / कस्टम लोगो)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        // Logo Type selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = project.logoType == "HK_NEWS",
                                onClick = {
                                    onUpdateLogoConfig(
                                        "HK_NEWS",
                                        project.customLogoUri,
                                        project.logoPosition,
                                        project.logoScale,
                                        project.logoOpacity
                                    )
                                },
                                label = { Text("H.K NEWS (नरसिंहपुर)", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NewsRedPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            FilterChip(
                                selected = project.logoType == "CUSTOM_IMAGE",
                                onClick = {
                                    logoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                label = { Text("गैलरी से लोगो", fontSize = 10.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(12.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NewsRedPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Logo Position
                        Text(
                            text = "लोगो स्थिति (Position):",
                            color = NewsAmber,
                            fontSize = 11.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "TOP_RIGHT" to "दाएं ऊपर",
                                "TOP_LEFT" to "बाएं ऊपर",
                                "BOTTOM_RIGHT" to "दाएं नीचे",
                                "BOTTOM_LEFT" to "बाएं नीचे"
                            ).forEach { (pos, label) ->
                                val selected = project.logoPosition == pos
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (selected) NewsRedPrimary else Color(0xFF0F172A))
                                        .clickable {
                                            onUpdateLogoConfig(
                                                project.logoType,
                                                project.customLogoUri,
                                                pos,
                                                project.logoScale,
                                                project.logoOpacity
                                            )
                                        }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (selected) Color.White else Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Logo Size & Opacity Sliders
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "लोगो साइज़: ${(project.logoScale * 100).toInt()}%",
                                color = Color.White,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "पारदर्शिता (Opacity): ${(project.logoOpacity * 100).toInt()}%",
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Slider(
                                value = project.logoScale,
                                onValueChange = {
                                    onUpdateLogoConfig(
                                        project.logoType,
                                        project.customLogoUri,
                                        project.logoPosition,
                                        it,
                                        project.logoOpacity
                                    )
                                },
                                valueRange = 0.6f..1.6f,
                                colors = SliderDefaults.colors(
                                    thumbColor = NewsGold,
                                    activeTrackColor = NewsRedPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            Slider(
                                value = project.logoOpacity,
                                onValueChange = {
                                    onUpdateLogoConfig(
                                        project.logoType,
                                        project.customLogoUri,
                                        project.logoPosition,
                                        project.logoScale,
                                        it
                                    )
                                },
                                valueRange = 0.3f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = NewsGold,
                                    activeTrackColor = NewsRedPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // TAB 1: VIDEO CONTRAST & BRIGHTNESS
                1 -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "वीडियो कंट्रास्ट नियंत्रण (Video Contrast)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${(project.videoContrast * 100).roundToInt()}%",
                                color = NewsGold,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }

                        // Contrast Slider
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("कंट्रास्ट (Contrast)", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                Text(
                                    text = when {
                                        project.videoContrast > 1.4f -> "🔥 हाई टीवी कंट्रास्ट"
                                        project.videoContrast > 1.05f -> "✨ ब्राइट पंच"
                                        project.videoContrast < 0.95f -> "सॉफ्ट"
                                        else -> "सामान्य (Default)"
                                    },
                                    color = NewsAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Slider(
                                value = project.videoContrast,
                                onValueChange = { newContrast ->
                                    onUpdateContrastAndBrightness(newContrast, project.videoBrightness)
                                },
                                valueRange = 0.6f..2.2f,
                                colors = SliderDefaults.colors(
                                    thumbColor = NewsGold,
                                    activeTrackColor = NewsGold,
                                    inactiveTrackColor = Color.DarkGray
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("slider_contrast")
                            )
                        }

                        // Brightness Slider
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("ब्राइटनेस / लाइटिंग (Brightness)", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                Text(
                                    text = "${(project.videoBrightness * 100).roundToInt()}%",
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
                            Slider(
                                value = project.videoBrightness,
                                onValueChange = { newBright ->
                                    onUpdateContrastAndBrightness(project.videoContrast, newBright)
                                },
                                valueRange = -0.4f..0.4f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.Cyan,
                                    activeTrackColor = Color.Cyan,
                                    inactiveTrackColor = Color.DarkGray
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("slider_brightness")
                            )
                        }

                        // Quick Contrast Presets
                        Text("क्विक कंट्रास्ट प्रीसेट्स:", color = Color.Gray, fontSize = 11.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { onUpdateContrastAndBrightness(1.0f, 0.0f) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (project.videoContrast == 1.0f && project.videoBrightness == 0.0f) NewsGold else Color(0xFF334155)
                                ),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Text(
                                    text = "सामान्य",
                                    fontSize = 10.sp,
                                    color = if (project.videoContrast == 1.0f && project.videoBrightness == 0.0f) Color.Black else Color.White
                                )
                            }
                            Button(
                                onClick = { onUpdateContrastAndBrightness(1.35f, 0.05f) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (project.videoContrast in 1.3f..1.4f) NewsGold else Color(0xFF334155)
                                ),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Text(
                                    text = "🎬 TV पंच",
                                    fontSize = 10.sp,
                                    color = if (project.videoContrast in 1.3f..1.4f) Color.Black else Color.White
                                )
                            }
                            Button(
                                onClick = { onUpdateContrastAndBrightness(1.7f, 0.1f) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (project.videoContrast > 1.5f) NewsGold else Color(0xFF334155)
                                ),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Text(
                                    text = "🔥 विविड HDR",
                                    fontSize = 10.sp,
                                    color = if (project.videoContrast > 1.5f) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }

                // TAB 2: COLOR FILTERS
                2 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "ब्रॉडकास्ट कलर ग्रेडिंग एवं फिल्टर्स (10+ Filters)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        val filterList = listOf(
                            "NONE" to "सामान्य (Original)",
                            "BREAKING_ALERT" to "🚨 ब्रेकिंग रेड अलर्ट",
                            "VIVID" to "✨ वाइब्रेंट ब्रॉडकास्ट",
                            "NOIR" to "🖤 ब्लैक & व्हाइट (स्टिंग)",
                            "NIGHT_VISION" to "🟢 नाइट विज़न कैमरा",
                            "VINTAGE" to "🎞️ रेट्रो आर्काइव",
                            "COOL_STUDIO" to "❄️ कूल न्यूज़ रूम",
                            "GOLDEN_HOUR" to "🌅 गोल्डन आवर",
                            "CYBER" to "🌐 साइबर टेक्नोलॉजी",
                            "SAFFRON" to "🟠 केसरिया भारत"
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            filterList.forEach { (id, name) ->
                                val selected = project.activeFilter == id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selected) NewsRedPrimary else Color(0xFF0F172A))
                                        .border(
                                            0.5.dp,
                                            if (selected) NewsGold else Color(0x33FFFFFF),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            onUpdateFilterAndVfx(id, project.activeVfx)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = name,
                                        color = if (selected) Color.White else Color.LightGray,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // TAB 3: VFX OVERLAYS
                3 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "लाइव मोशन VFX इफ़ेक्ट्स",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        val vfxList = listOf(
                            "NONE" to "बंद (None)",
                            "SCANLINES" to "📺 CRT टीवी स्कैनलाइन्स",
                            "FLASH_STROBE" to "⚡ ब्रेकिंग स्ट्रोब फ़्लैश",
                            "VIGNETTE" to "🌑 ड्रामेटिक विग्नेट",
                            "EMERGENCY_SIREN" to "🚨 इमरजेंसी सायरन लाइट",
                            "GLITCH" to "👾 डिजिटल टीवी ग्लिच",
                            "RAIN_STORM" to "🌧️ मौसम बारिश अलर्ट"
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            vfxList.forEach { (id, name) ->
                                val selected = project.activeVfx == id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selected) NewsGold else Color(0xFF0F172A))
                                        .border(
                                            0.5.dp,
                                            if (selected) NewsAmber else Color(0x33FFFFFF),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            onUpdateFilterAndVfx(project.activeFilter, id)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = name,
                                        color = if (selected) Color.Black else Color.LightGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // TAB 4: ANIMATION FILTERS
                4 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "एनिमेशन वीडियो फ़िल्टर (Animated Filters)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        val animFilters = listOf(
                            "NONE" to "बंद (None)",
                            "NEON_PULSE_WAVE" to "⚡ नियॉन पल्स वेव",
                            "GOLDEN_SPARKLE" to "✨ गोल्डन स्पार्कल",
                            "LIGHT_LEAK_SWEEP" to "🌅 लाइट लीक स्वीप",
                            "VHS_RETRO_WAVE" to "📼 VHS रेट्रो वेव",
                            "PRISM_AURORA" to "🌈 प्रिज्म ऑरोरा ग्लो",
                            "CYBER_MATRIX" to "💚 साइबर मैट्रिक्स",
                            "FIRE_EMBERS" to "🔥 फ़ायर एम्बर्स",
                            "SNOW_FALL" to "❄️ स्नो फॉल"
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            animFilters.forEach { (id, name) ->
                                val selected = project.activeAnimationFilter == id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selected) NewsGold else Color(0xFF0F172A))
                                        .border(0.5.dp, if (selected) NewsAmber else Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                                        .clickable { onUpdateAnimationFilter(id) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = name,
                                        color = if (selected) Color.Black else Color.LightGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // TAB 5: VIDEO CROP TOOLS
                5 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "वीडियो क्रॉप टूल्स (Crop Ratio, Zoom & Edges)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            OutlinedButton(
                                onClick = onResetVideoCrop,
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("🔄 रीसेट", fontSize = 10.sp, color = NewsGold)
                            }
                        }
                        val presets = listOf(
                            "FULL" to "Full",
                            "16:9" to "16:9",
                            "9:16" to "9:16",
                            "1:1" to "1:1",
                            "4:5" to "4:5",
                            "4:3" to "4:3",
                            "21:9" to "21:9"
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            presets.forEach { (id, label) ->
                                val sel = project.videoCropPreset == id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (sel) NewsRedPrimary else Color(0xFF0F172A))
                                        .clickable { onUpdateVideoCrop(id, null, null, null, null, null, null, null) }
                                        .padding(horizontal = 9.dp, vertical = 6.dp)
                                ) {
                                    Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text("क्रॉप ज़ूम: ${(project.videoCropZoom * 100).roundToInt()}%", color = NewsAmber, fontSize = 11.sp)
                        Slider(
                            value = project.videoCropZoom,
                            onValueChange = { onUpdateVideoCrop(null, it, null, null, null, null, null, null) },
                            valueRange = 1.0f..3.0f,
                            colors = SliderDefaults.colors(thumbColor = NewsGold, activeTrackColor = NewsRedPrimary)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("बायां/दायां पैन: ${(project.videoCropPanX * 100).roundToInt()}%", color = Color.LightGray, fontSize = 10.sp)
                                Slider(
                                    value = project.videoCropPanX,
                                    onValueChange = { onUpdateVideoCrop(null, null, it, null, null, null, null, null) },
                                    valueRange = -1.0f..1.0f
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("ऊपर/नीचे पैन: ${(project.videoCropPanY * 100).roundToInt()}%", color = Color.LightGray, fontSize = 10.sp)
                                Slider(
                                    value = project.videoCropPanY,
                                    onValueChange = { onUpdateVideoCrop(null, null, null, it, null, null, null, null) },
                                    valueRange = -1.0f..1.0f
                                )
                            }
                        }
                    }
                }

                // TAB 6: SPEED & TRIM
                6 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "वीडियो गति नियंत्रण (Speed Control)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(0.5f to "0.5x स्लो", 1.0f to "1.0x सामान्य", 1.5f to "1.5x तेज़", 2.0f to "2.0x 2X").forEach { (speed, label) ->
                                val selected = project.videoSpeed == speed
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selected) NewsRedPrimary else Color(0xFF0F172A))
                                        .clickable {
                                            onUpdateSpeedAndTrim(
                                                speed,
                                                project.videoTrimStart,
                                                project.videoTrimEnd,
                                                project.videoVolume
                                            )
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (selected) Color.White else Color.Gray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Text(
                            text = "वीडियो वॉल्यूम: ${(project.videoVolume * 100).toInt()}%",
                            color = NewsAmber,
                            fontSize = 11.sp
                        )
                        Slider(
                            value = project.videoVolume,
                            onValueChange = {
                                onUpdateSpeedAndTrim(
                                    project.videoSpeed,
                                    project.videoTrimStart,
                                    project.videoTrimEnd,
                                    it
                                )
                            },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = NewsGold,
                                activeTrackColor = NewsRedPrimary
                            )
                        )
                    }
                }

                // TAB 7: STICKERS & BADGES
                7 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "ऑन-स्क्रीन ब्रॉडकास्ट बैज व स्टिकर्स",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        val stickers = listOf(
                            "LIVE" to "🔴 LIVE प्रसारण",
                            "EXCLUSIVE" to "⭐ विशेष ख़बर",
                            "BIG_BREAKING" to "🚨 बड़ा खुलासा",
                            "GROUND_ZERO" to "🎤 ग्राउंड ज़ीरो",
                            "VIRAL" to "⚡ वायरल वीडियो",
                            "WEATHER_ALERT" to "🌧️ मौसम चेतावनी",
                            "ELECTION_SPECIAL" to "🏛️ चुनाव विशेष"
                        )

                        val activeList = project.activeStickers.split(",")

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            stickers.forEach { (tag, label) ->
                                val active = activeList.contains(tag)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) NewsRedPrimary else Color(0xFF0F172A))
                                        .border(
                                            0.5.dp,
                                            if (active) NewsGold else Color(0x33FFFFFF),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { onToggleSticker(tag) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = if (active) Color.White else Color.Gray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // TAB 8: LAYOUT (SPLIT SCREEN / PIP)
                8 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "स्क्रीन लेआउट मोड (Anchor + Ground Reporter)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "FULLSCREEN" to "पूर्ण स्क्रीन (Full)",
                                "SPLIT_SCREEN" to "स्प्लिट 50:50 (Split)",
                                "PIP_CORNER" to "PIP विंडो (Float)"
                            ).forEach { (mode, label) ->
                                val selected = project.layoutMode == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selected) NewsRedPrimary else Color(0xFF0F172A))
                                        .clickable { onUpdateLayoutMode(mode) }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (selected) Color.White else Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (project.layoutMode != "FULLSCREEN") {
                            OutlinedButton(
                                onClick = {
                                    secondaryMediaPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.PictureInPicture, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ग्राउंड रिपोर्टर / दूसरी फुटेज जोड़ें", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
