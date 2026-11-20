package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallpaper
import com.example.audio.NewsAudioEffects
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NewsProject
import com.example.ui.theme.NewsAmber
import com.example.ui.theme.NewsGold
import com.example.ui.theme.NewsRedPrimary
import kotlin.math.roundToInt

private val VnCyan = Color(0xFF00E5FF)
private val VnTealDark = Color(0xFF004D40)
private val VnSurface = Color(0xFF0B111E)
private val VnCard = Color(0xFF131C2E)

data class VnToolPill(
    val index: Int,
    val title: String,
    val icon: ImageVector,
    val isActive: Boolean,
    val tag: String
)

/**
 * Authentic VN Video Editor Horizontal Action Strip + Expandable Tool Inspector Sheet
 */
@Composable
fun VnEditorSuiteCard(
    project: NewsProject,
    isPanelOpen: Boolean,
    activeTab: Int,
    onSelectToolTab: (Int) -> Unit,
    onClosePanel: () -> Unit,
    onUpdateSpeedCurve: (String) -> Unit,
    onUpdateFilter: (String) -> Unit,
    onUpdateContrastBrightness: (Float, Float) -> Unit,
    onUpdateAdjustments: (saturation: Float?, warmth: Float?, vignette: Float?, sharpen: Float?) -> Unit,
    onUpdateBackgroundAndBorder: (bgStyle: String?, borderWidthDp: Float?, borderColorHex: String?) -> Unit,
    onRotate90: () -> Unit,
    onToggleMirrorHorizontal: () -> Unit,
    onToggleFlipVertical: () -> Unit,
    onUpdateAspectPreset: (String) -> Unit,
    onUpdateMotionFx: (String) -> Unit,
    onToggleFreezeFrame: () -> Unit,
    onToggleReverse: () -> Unit,
    onTrimLeft: () -> Unit,
    onSplitAtPlayhead: () -> Unit,
    onTrimRight: () -> Unit,
    onUpdateMaskAndOpacity: (maskType: String?, opacity: Float?) -> Unit,
    onUpdateSubtitleAndBeat: (text: String?, style: String?, beatMarkers: Boolean?) -> Unit,
    onResetVnTools: () -> Unit,
    onUpdateAnimationFilter: (String) -> Unit = {},
    onUpdateVideoCrop: (preset: String?, zoom: Float?, panX: Float?, panY: Float?, left: Float?, top: Float?, right: Float?, bottom: Float?) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onResetVideoCrop: () -> Unit = {},
    onUpdateVoiceChanger: (String) -> Unit = {},
    onUpdateVoicePitch: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cropActive = project.videoCropPreset != "FULL" || project.videoCropZoom > 1.02f ||
        project.videoCropLeft > 0.01f || project.videoCropTop > 0.01f ||
        project.videoCropRight > 0.01f || project.videoCropBottom > 0.01f
    val toolPills = listOf(
        VnToolPill(0, "कर्व स्पीड", Icons.Default.Speed, project.vnSpeedCurve != "NONE", "vn_tool_curve"),
        VnToolPill(1, "फ़िल्टर LUT", Icons.Default.FilterVintage, project.activeFilter != "NONE", "vn_tool_filter"),
        VnToolPill(10, "वीडियो क्रॉप", Icons.Default.Crop, cropActive, "vn_tool_crop"),
        VnToolPill(11, "एनिमेशन फ़िल्टर", Icons.Default.AutoAwesome, project.activeAnimationFilter != "NONE", "vn_tool_anim_filter"),
        VnToolPill(12, "वॉइस इफ़ेक्ट", Icons.Default.Mic, project.voiceChangerEffect != "NORMAL" || kotlin.math.abs(project.audioPitch - 1f) > 0.03f, "vn_tool_voice"),
        VnToolPill(2, "एडजस्ट", Icons.Default.Tune, project.vnSaturation != 1f || project.vnWarmth != 0f || project.vnVignette != 0f || project.vnSharpen > 0.02f, "vn_tool_adjust"),
        VnToolPill(3, "BG व बॉर्डर", Icons.Default.Wallpaper, project.vnBackgroundStyle != "BLACK" || project.vnBorderWidthDp > 0f, "vn_tool_bg"),
        VnToolPill(4, "रोटेट/मिरर", Icons.Default.Rotate90DegreesCw, project.vnRotationDegrees != 0f || project.vnMirrorHorizontal || project.vnFlipVertical, "vn_tool_transform"),
        VnToolPill(5, "VN मोशन FX", Icons.Default.AutoAwesome, project.vnMotionFx != "NONE", "vn_tool_fx"),
        VnToolPill(6, "फ़्रीज़/रिवर्स", Icons.Default.AcUnit, project.vnFreezeFrameActive || project.vnReversePlayback, "vn_tool_freeze"),
        VnToolPill(7, "मास्क/ओपैसिटी", Icons.Default.Opacity, project.vnMaskType != "NONE" || project.vnVideoOpacity < 0.99f, "vn_tool_mask"),
        VnToolPill(8, "सबटाइटल/बीट", Icons.Default.Subtitles, project.vnSubtitleText.isNotBlank() || project.vnBeatMarkersEnabled, "vn_tool_subtitle"),
        VnToolPill(9, "ट्रिम/स्प्लिट", Icons.Default.ContentCut, project.videoTrimStart > 0f || project.videoTrimEnd < 1f, "vn_tool_trim")
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vn_editor_suite_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = VnSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, VnCyan.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // VN Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFF00ACC1), Color(0xFF00796B))),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "VN",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                    Column {
                        Text(
                            text = "VN वीडियो एडिटर टूल्स (VN Pro Studio)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "स्पीड कर्व • LUT • BG बॉर्डर • मिरर • फ़्रीज़ • मास्क • सबटाइटल",
                            color = VnCyan.copy(alpha = 0.85f),
                            fontSize = 9.sp
                        )
                    }
                }

                IconButton(
                    onClick = onResetVnTools,
                    modifier = Modifier.size(40.dp).testTag("vn_reset_all_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "VN टूल्स रीसेट करें",
                        tint = NewsGold,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Signature VN Scrollable Bottom-Style Tools Bar
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                itemsIndexed(toolPills) { _, item ->
                    val isSelected = isPanelOpen && activeTab == item.index
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isSelected -> Color(0xFF006064)
                                    item.isActive -> Color(0xFF132D3B)
                                    else -> VnCard
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = when {
                                    isSelected -> VnCyan
                                    item.isActive -> VnCyan.copy(alpha = 0.55f)
                                    else -> Color(0x28FFFFFF)
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectToolTab(item.index) }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag(item.tag),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = if (isSelected || item.isActive) VnCyan else Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = item.title,
                            color = if (isSelected || item.isActive) Color.White else Color.LightGray,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected || item.isActive) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // Expandable VN Tool Inspector Panel
            AnimatedVisibility(visible = isPanelOpen) {
                VnInspectorContent(
                    project = project,
                    activeTab = activeTab,
                    onClose = onClosePanel,
                    onUpdateSpeedCurve = onUpdateSpeedCurve,
                    onUpdateFilter = onUpdateFilter,
                    onUpdateContrastBrightness = onUpdateContrastBrightness,
                    onUpdateAdjustments = onUpdateAdjustments,
                    onUpdateBackgroundAndBorder = onUpdateBackgroundAndBorder,
                    onRotate90 = onRotate90,
                    onToggleMirrorHorizontal = onToggleMirrorHorizontal,
                    onToggleFlipVertical = onToggleFlipVertical,
                    onUpdateAspectPreset = onUpdateAspectPreset,
                    onUpdateMotionFx = onUpdateMotionFx,
                    onToggleFreezeFrame = onToggleFreezeFrame,
                    onToggleReverse = onToggleReverse,
                    onTrimLeft = onTrimLeft,
                    onSplitAtPlayhead = onSplitAtPlayhead,
                    onTrimRight = onTrimRight,
                    onUpdateMaskAndOpacity = onUpdateMaskAndOpacity,
                    onUpdateSubtitleAndBeat = onUpdateSubtitleAndBeat,
                    onUpdateAnimationFilter = onUpdateAnimationFilter,
                    onUpdateVideoCrop = onUpdateVideoCrop,
                    onResetVideoCrop = onResetVideoCrop,
                    onUpdateVoiceChanger = onUpdateVoiceChanger,
                    onUpdateVoicePitch = onUpdateVoicePitch
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VnInspectorContent(
    project: NewsProject,
    activeTab: Int,
    onClose: () -> Unit,
    onUpdateSpeedCurve: (String) -> Unit,
    onUpdateFilter: (String) -> Unit,
    onUpdateContrastBrightness: (Float, Float) -> Unit,
    onUpdateAdjustments: (Float?, Float?, Float?, Float?) -> Unit,
    onUpdateBackgroundAndBorder: (String?, Float?, String?) -> Unit,
    onRotate90: () -> Unit,
    onToggleMirrorHorizontal: () -> Unit,
    onToggleFlipVertical: () -> Unit,
    onUpdateAspectPreset: (String) -> Unit,
    onUpdateMotionFx: (String) -> Unit,
    onToggleFreezeFrame: () -> Unit,
    onToggleReverse: () -> Unit,
    onTrimLeft: () -> Unit,
    onSplitAtPlayhead: () -> Unit,
    onTrimRight: () -> Unit,
    onUpdateMaskAndOpacity: (String?, Float?) -> Unit,
    onUpdateSubtitleAndBeat: (String?, String?, Boolean?) -> Unit,
    onUpdateAnimationFilter: (String) -> Unit,
    onUpdateVideoCrop: (String?, Float?, Float?, Float?, Float?, Float?, Float?, Float?) -> Unit,
    onResetVideoCrop: () -> Unit,
    onUpdateVoiceChanger: (String) -> Unit,
    onUpdateVoicePitch: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(VnCard)
            .border(0.75.dp, VnCyan.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabTitle = when (activeTab) {
                0 -> "⚡ VN कर्व स्पीड (Curve Speed Presets)"
                1 -> "🎨 VN सिनेमा फ़िल्टर व LUT"
                2 -> "🎛️ VN कलर एडजस्ट (Adjustments)"
                3 -> "🖼️ VN बैकग्राउंड व कैनवास बॉर्डर (BG & Border)"
                4 -> "🔄 रोटेट, मिरर, फ्लिप व फ़्रेम अनुपात"
                5 -> "🎞️ VN मोशन इफ़ेक्ट्स (Motion FX)"
                6 -> "🧊 फ़्रीज़ फ़्रेम व रिवर्स (Freeze & Reverse)"
                7 -> "🎭 मास्क व ओपैसिटी (Mask & Opacity)"
                8 -> "💬 VN ऑटो-सबटाइटल व बीट मार्कर"
                9 -> "✂️ स्प्लिट और ट्रिम कंट्रोल"
                10 -> "✂️ वीडियो क्रॉप टूल्स (Crop & Framing)"
                11 -> "✨ एनिमेशन फ़िल्टर (Animated Filters)"
                12 -> "🎙️ वॉइस इफ़ेक्ट व पिच कंट्रोल (Voice FX)"
                else -> "⚡ VN टूल्स"
            }
            Text(
                text = tabTitle,
                color = VnCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "बंद करें", tint = Color.LightGray, modifier = Modifier.size(16.dp))
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (activeTab) {
                // 0: VN Speed Curve
                0 -> {
                    val curves = listOf(
                        "NONE" to "सामान्य (Normal)",
                        "MONTAGE" to "🎬 मोंटाज (Montage)",
                        "HERO" to "🦸 हीरो रैंप (Hero)",
                        "BULLET" to "⚡ बुलेट टाइम (Bullet)",
                        "JUMP_CUT" to "✂️ जंप कट (Jump)",
                        "FLASH_IN" to "🚀 फ़्लैश इन (Flash In)",
                        "FLASH_OUT" to "🌠 फ़्लैश आउट"
                    )
                    // Curve visualizer
                    VnSpeedCurveGraph(curveId = project.vnSpeedCurve)

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        curves.forEach { (id, label) ->
                            val selected = project.vnSpeedCurve == id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selected) VnCyan else VnSurface)
                                    .border(0.7.dp, if (selected) Color.White else Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                                    .clickable { onUpdateSpeedCurve(id) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (selected) Color.Black else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 1: VN Cinema Filters & LUTs
                1 -> {
                    val vnFilters = listOf(
                        "NONE" to "ओरिजिनल",
                        "CINE_TEAL" to "🎥 टील & ऑरेंज LUT",
                        "MOODY_NEWS" to "🌑 मूडी न्यूज़ डॉक",
                        "WARM_VLOG" to "☀️ वार्म व्लॉग",
                        "FRESH_DAY" to "🌿 फ्रेश डेलाइट",
                        "VIVID" to "✨ वाइब्रेंट प्रो",
                        "BREAKING_ALERT" to "🚨 ब्रेकिंग रेड",
                        "COOL_STUDIO" to "❄️ कूल स्टूडियो",
                        "GOLDEN_HOUR" to "🌅 गोल्डन आवर",
                        "NOIR" to "🖤 ब्लैक & व्हाइट"
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        vnFilters.forEach { (id, label) ->
                            val selected = project.activeFilter == id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selected) VnCyan else VnSurface)
                                    .border(0.7.dp, if (selected) Color.White else Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                                    .clickable { onUpdateFilter(id) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (selected) Color.Black else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 2: VN Color Adjust
                2 -> {
                    VnSliderRow(
                        label = "सैचुरेशन (Saturation): ${(project.vnSaturation * 100).roundToInt()}%",
                        value = project.vnSaturation,
                        range = 0f..2f,
                        onValueChange = { onUpdateAdjustments(it, null, null, null) }
                    )
                    VnSliderRow(
                        label = "वॉर्म / तापमान (Warmth): ${(project.vnWarmth * 100).roundToInt()}",
                        value = project.vnWarmth,
                        range = -0.5f..0.5f,
                        onValueChange = { onUpdateAdjustments(null, it, null, null) }
                    )
                    VnSliderRow(
                        label = "कंट्रास्ट (Contrast): ${(project.videoContrast * 100).roundToInt()}%",
                        value = project.videoContrast,
                        range = 0.6f..2.2f,
                        onValueChange = { onUpdateContrastBrightness(it, project.videoBrightness) }
                    )
                    VnSliderRow(
                        label = "ब्राइटनेस (Exposure): ${(project.videoBrightness * 100).roundToInt()}%",
                        value = project.videoBrightness,
                        range = -0.4f..0.4f,
                        onValueChange = { onUpdateContrastBrightness(project.videoContrast, it) }
                    )
                    VnSliderRow(
                        label = "विग्नेट (Vignette): ${(project.vnVignette * 100).roundToInt()}%",
                        value = project.vnVignette,
                        range = 0f..1f,
                        onValueChange = { onUpdateAdjustments(null, null, it, null) }
                    )
                    VnSliderRow(
                        label = "शार्पनेस (Sharpen): ${(project.vnSharpen * 100).roundToInt()}%",
                        value = project.vnSharpen,
                        range = 0f..1f,
                        onValueChange = { onUpdateAdjustments(null, null, null, it) }
                    )
                }

                // 3: VN Background & Border
                3 -> {
                    Text("कैनवास बैकग्राउंड (BG Fill):", color = Color.LightGray, fontSize = 11.sp)
                    val bgs = listOf(
                        "BLACK" to "⬛ काला (Black)",
                        "NAVY_STUDIO" to "🟦 न्यूज़ नेवी",
                        "CRIMSON_NEWS" to "🟥 क्रिमसन रेड",
                        "GOLD_ROYAL" to "🟨 गोल्ड रॉयल",
                        "GRADIENT_DARK" to "🌌 डार्क ग्रेडिएंट"
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        bgs.forEach { (id, label) ->
                            val selected = project.vnBackgroundStyle == id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selected) VnCyan else VnSurface)
                                    .clickable { onUpdateBackgroundAndBorder(id, null, null) }
                                    .padding(horizontal = 8.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (selected) Color.Black else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    VnSliderRow(
                        label = "बॉर्डर मोटाई (Border): ${project.vnBorderWidthDp.roundToInt()} dp",
                        value = project.vnBorderWidthDp,
                        range = 0f..16f,
                        onValueChange = { onUpdateBackgroundAndBorder(null, it, null) }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("बॉर्डर रंग:", color = Color.LightGray, fontSize = 11.sp)
                        listOf("#FFD54F", "#FFFFFF", "#FF1744", "#00E5FF", "#00E676").forEach { hex ->
                            val c = Color(android.graphics.Color.parseColor(hex))
                            val sel = project.vnBorderColorHex == hex
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(if (sel) 2.5.dp else 1.dp, if (sel) Color.White else Color.Gray, CircleShape)
                                    .clickable { onUpdateBackgroundAndBorder(null, null, hex) }
                            )
                        }
                    }
                }

                // 4: VN Rotate, Mirror, Flip, Crop Aspect
                4 -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = onRotate90,
                            colors = ButtonDefaults.buttonColors(containerColor = if (project.vnRotationDegrees != 0f) Color(0xFF006064) else VnSurface),
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🔄 रोटेट (${project.vnRotationDegrees.toInt()}°)", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onToggleMirrorHorizontal,
                            colors = ButtonDefaults.buttonColors(containerColor = if (project.vnMirrorHorizontal) Color(0xFF006064) else VnSurface),
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🪞 मिरर ↔", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onToggleFlipVertical,
                            colors = ButtonDefaults.buttonColors(containerColor = if (project.vnFlipVertical) Color(0xFF006064) else VnSurface),
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("↕️ फ्लिप ↕", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text("कैनवास अनुपात (VN Frame Ratio):", color = Color.LightGray, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("16:9" to "16:9 TV", "9:16" to "9:16 Reels", "1:1" to "1:1 Square", "4:5" to "4:5 Post").forEach { (ratio, label) ->
                            val sel = project.aspectPreset == ratio
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (sel) VnCyan else VnSurface)
                                    .clickable { onUpdateAspectPreset(ratio) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, color = if (sel) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 5: VN Motion FX
                5 -> {
                    val fxList = listOf(
                        "NONE" to "बंद (None)",
                        "SHAKE" to "📳 कैमरा शेक (Shake)",
                        "JITTER" to "⚡ बीट जिटर (Jitter)",
                        "ZOOM_PULSE" to "🔍 ज़ूम पल्स (Pulse)",
                        "EXPANSION" to "💥 एक्सपैंड पॉप",
                        "FLASH_CUT" to "⚡ फ़्लैश कट (Flash)"
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        fxList.forEach { (id, label) ->
                            val selected = project.vnMotionFx == id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selected) VnCyan else VnSurface)
                                    .clickable { onUpdateMotionFx(id) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(label, color = if (selected) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 6: VN Freeze Frame & Reverse
                6 -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onToggleFreezeFrame,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (project.vnFreezeFrameActive) Color(0xFF00838F) else VnSurface
                            ),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("vn_btn_freeze"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (project.vnFreezeFrameActive) "🧊 फ़्रीज़ चालू (Unfreeze)" else "🧊 फ़्रीज़ फ़्रेम (Freeze)",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = onToggleReverse,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (project.vnReversePlayback) Color(0xFFC62828) else VnSurface
                            ),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("vn_btn_reverse"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (project.vnReversePlayback) "⏪ रिवर्स चालू (Reverse)" else "⏪ रिवर्स वीडियो",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "💡 फ़्रीज़ फ़्रेम वर्तमान सीन को स्थिर रखता है और रिवर्स मोड एक्सपोर्ट में वीडियो को पीछे से चलाता है।",
                        color = NewsAmber,
                        fontSize = 10.sp
                    )
                }

                // 7: VN Mask & Opacity
                7 -> {
                    VnSliderRow(
                        label = "वीडियो पारदर्शिता (Opacity): ${(project.vnVideoOpacity * 100).roundToInt()}%",
                        value = project.vnVideoOpacity,
                        range = 0.2f..1.0f,
                        onValueChange = { onUpdateMaskAndOpacity(null, it) }
                    )

                    Text("VN मास्क आकार (Mask Shape):", color = Color.LightGray, fontSize = 11.sp)
                    val masks = listOf(
                        "NONE" to "कोई नहीं",
                        "LINEAR" to "▤ लीनियर फ़ेड",
                        "MIRROR" to "◫ सिनेमा स्लिट",
                        "RADIAL" to "⭕ स्पॉटलाइट",
                        "RECTANGLE" to "▬ मूवी बार्स (Letterbox)"
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        masks.forEach { (id, label) ->
                            val selected = project.vnMaskType == id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selected) VnCyan else VnSurface)
                                    .clickable { onUpdateMaskAndOpacity(id, null) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(label, color = if (selected) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 8: VN Subtitles & Beat Markers
                8 -> {
                    OutlinedTextField(
                        value = project.vnSubtitleText,
                        onValueChange = { onUpdateSubtitleAndBeat(it, null, null) },
                        label = { Text("VN सबटाइटल / कैप्शन लिखें", color = Color.LightGray, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = VnCyan,
                            unfocusedBorderColor = Color(0x44FFFFFF)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    val styles = listOf(
                        "CINE" to "🎬 सिनेमा ब्लैक",
                        "NEWS_BAR" to "🔴 न्यूज़ रेड",
                        "YELLOW_BOLD" to "🟡 येलो बोल्ड",
                        "MINIMAL" to "✨ मिनिमल"
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        styles.forEach { (id, label) ->
                            val selected = project.vnSubtitleStyle == id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selected) VnCyan else VnSurface)
                                    .clickable { onUpdateSubtitleAndBeat(null, id, null) }
                                    .padding(horizontal = 8.dp, vertical = 7.dp)
                            ) {
                                Text(label, color = if (selected) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = VnCyan, modifier = Modifier.size(16.dp))
                            Text("🎵 टाइमलाइन बीट मार्कर (Beat Flags)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Switch(
                            checked = project.vnBeatMarkersEnabled,
                            onCheckedChange = { onUpdateSubtitleAndBeat(null, null, it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = VnCyan, checkedTrackColor = VnTealDark)
                        )
                    }
                }

                // 10: Video Crop Tools
                10 -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("क्रॉप अनुपात (Crop Frame Presets):", color = Color.LightGray, fontSize = 11.sp)
                        OutlinedButton(
                            onClick = onResetVideoCrop,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp).testTag("vn_reset_crop_btn")
                        ) {
                            Text("🔄 रीसेट क्रॉप", color = NewsGold, fontSize = 10.sp)
                        }
                    }

                    val cropPresets = listOf(
                        "FULL" to "पूरा (Full)",
                        "16:9" to "16:9 TV",
                        "9:16" to "9:16 Reels",
                        "1:1" to "1:1 Square",
                        "4:5" to "4:5 Post",
                        "4:3" to "4:3 Classic",
                        "21:9" to "21:9 Cinema"
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cropPresets.forEach { (id, label) ->
                            val sel = project.videoCropPreset == id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (sel) VnCyan else VnSurface)
                                    .border(0.7.dp, if (sel) Color.White else Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                                    .clickable { onUpdateVideoCrop(id, null, null, null, null, null, null, null) }
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Text(label, color = if (sel) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    VnSliderRow(
                        label = "क्रॉप ज़ूम (Crop Zoom): ${(project.videoCropZoom * 100).roundToInt()}%",
                        value = project.videoCropZoom,
                        range = 1.0f..3.0f,
                        onValueChange = { onUpdateVideoCrop(null, it, null, null, null, null, null, null) }
                    )
                    VnSliderRow(
                        label = "बाएँ-दाएँ खिसकाएं (Pan X): ${(project.videoCropPanX * 100).roundToInt()}%",
                        value = project.videoCropPanX,
                        range = -1.0f..1.0f,
                        onValueChange = { onUpdateVideoCrop(null, null, it, null, null, null, null, null) }
                    )
                    VnSliderRow(
                        label = "ऊपर-नीचे खिसकाएं (Pan Y): ${(project.videoCropPanY * 100).roundToInt()}%",
                        value = project.videoCropPanY,
                        range = -1.0f..1.0f,
                        onValueChange = { onUpdateVideoCrop(null, null, null, it, null, null, null, null) }
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            VnSliderRow(
                                label = "बायां कट: ${(project.videoCropLeft * 100).roundToInt()}%",
                                value = project.videoCropLeft,
                                range = 0f..0.4f,
                                onValueChange = { onUpdateVideoCrop(null, null, null, null, it, null, null, null) }
                            )
                            VnSliderRow(
                                label = "दायां कट: ${(project.videoCropRight * 100).roundToInt()}%",
                                value = project.videoCropRight,
                                range = 0f..0.4f,
                                onValueChange = { onUpdateVideoCrop(null, null, null, null, null, null, it, null) }
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            VnSliderRow(
                                label = "ऊपरी कट: ${(project.videoCropTop * 100).roundToInt()}%",
                                value = project.videoCropTop,
                                range = 0f..0.4f,
                                onValueChange = { onUpdateVideoCrop(null, null, null, null, null, it, null, null) }
                            )
                            VnSliderRow(
                                label = "निचला कट: ${(project.videoCropBottom * 100).roundToInt()}%",
                                value = project.videoCropBottom,
                                range = 0f..0.4f,
                                onValueChange = { onUpdateVideoCrop(null, null, null, null, null, null, null, it) }
                            )
                        }
                    }
                }

                // 11: Animation Filters
                11 -> {
                    val animFilters = listOf(
                        "NONE" to "बंद (None)",
                        "NEON_PULSE_WAVE" to "⚡ नियॉन पल्स वेव",
                        "GOLDEN_SPARKLE" to "✨ गोल्डन स्पार्कल",
                        "LIGHT_LEAK_SWEEP" to "🌅 लाइट लीक स्वीप",
                        "VHS_RETRO_WAVE" to "📼 VHS रेट्रो वेव",
                        "PRISM_AURORA" to "🌈 प्रिज्म ऑरोरा ग्लो",
                        "CYBER_MATRIX" to "💚 साइबर मैट्रिक्स",
                        "FIRE_EMBERS" to "🔥 फ़ायर स्पार्क एम्बर्स",
                        "SNOW_FALL" to "❄️ विंटर स्नो फॉल"
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        animFilters.forEach { (id, label) ->
                            val sel = project.activeAnimationFilter == id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (sel) NewsGold else VnSurface)
                                    .border(0.7.dp, if (sel) Color.White else Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                                    .clickable { onUpdateAnimationFilter(id) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(label, color = if (sel) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 12: Voice Effects & Pitch Control
                12 -> {
                    val ctx = androidx.compose.ui.platform.LocalContext.current
                    Text(
                        text = "🤖 ChatGPT AI वॉइस इफ़ेक्ट्स (ChatGPT Voice Modes):",
                        color = VnCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val chatGptVoices = listOf(
                        "CHATGPT_COVE" to "💬 ChatGPT Cove (गहरी शांत)",
                        "CHATGPT_BREEZE" to "🌬️ ChatGPT Breeze (फ्रेंडली)",
                        "CHATGPT_EMBER" to "🔥 ChatGPT Ember (एंकर)",
                        "CHATGPT_JUNIPER" to "🎙️ ChatGPT Juniper (क्लियर)",
                        "CHATGPT_SKY" to "✨ ChatGPT Sky (स्मूद AI)",
                        "CHATGPT_SOL" to "☀️ ChatGPT Sol (न्यूज़ प्रो)"
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        chatGptVoices.forEach { (id, label) ->
                            val sel = project.voiceChangerEffect == id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (sel) VnCyan else Color(0xFF102238))
                                    .border(0.8.dp, if (sel) Color.White else VnCyan.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                                    .clickable {
                                        onUpdateVoiceChanger(id)
                                        NewsAudioEffects.playVoiceEffectPreview(id, NewsAudioEffects.resolveDefaultPitch(id))
                                    }
                                    .padding(horizontal = 9.dp, vertical = 7.dp)
                            ) {
                                Text(label, color = if (sel) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    val voicePresets = listOf(
                        "NORMAL" to "सामान्य (Normal)",
                        "DEEP_ANCHOR" to "🎙️ गहरी एंकर आवाज़ (Deep Male)",
                        "FEMALE_ANCHOR" to "👩 महिला न्यूज़ एंकर",
                        "RADIO_MIC" to "📻 वायरलेस रिपोर्टर माइक",
                        "ECHO_STUDIO" to "🏛️ लाइव स्टूडियो इको",
                        "CHIPMUNK" to "🐿️ चिपमंक (तेज़ हाई पिच)",
                        "ROBOT" to "🤖 AI रोबोट न्यूज़कास्टर",
                        "MEGAPHONE" to "📢 लाउडस्पीकर / रैली माइक",
                        "SLOW_CINEMA" to "🎬 धीमी भारी आवाज़"
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        voicePresets.forEach { (id, label) ->
                            val sel = project.voiceChangerEffect == id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (sel) NewsGold else VnSurface)
                                    .border(0.7.dp, if (sel) Color.White else Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                                    .clickable {
                                        onUpdateVoiceChanger(id)
                                        NewsAudioEffects.playVoiceEffectPreview(id, NewsAudioEffects.resolveDefaultPitch(id))
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(label, color = if (sel) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    VnSliderRow(
                        label = "मैनुअल वॉइस पिच (Voice Pitch): ${(project.audioPitch * 100).roundToInt()}%",
                        value = project.audioPitch,
                        range = 0.5f..1.8f,
                        onValueChange = onUpdateVoicePitch
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { NewsAudioEffects.playVoiceEffectPreview(project.voiceChangerEffect, project.audioPitch) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006064)),
                            modifier = Modifier.weight(1f).height(42.dp).testTag("vn_test_voice_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🔊 साउंड टेस्ट", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                NewsAudioEffects.speakTextWithVoiceEffect(
                                    ctx,
                                    project.headline,
                                    project.voiceChangerEffect,
                                    project.audioPitch
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                            modifier = Modifier.weight(1.2f).height(42.dp).testTag("vn_speak_chatgpt_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🗣️ ChatGPT वॉइस सुनें", color = NewsGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 9: VN Trim / Split
                else -> {
                    Text(
                        text = "सक्रिय ट्रिम हिस्सा: ${(project.videoTrimStart * 100).roundToInt()}% – ${(project.videoTrimEnd * 100).roundToInt()}%",
                        color = NewsAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onTrimLeft,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("✂️ बायां ट्रिम", color = NewsGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onSplitAtPlayhead,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("✂️ स्प्लिट कट", color = VnCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onTrimRight,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("✂️ दायां ट्रिम", color = NewsGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VnSliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = VnCyan,
                activeTrackColor = VnCyan,
                inactiveTrackColor = Color(0xFF28354E)
            ),
            modifier = Modifier.height(26.dp)
        )
    }
}

@Composable
private fun VnSpeedCurveGraph(curveId: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(VnSurface)
            .border(0.6.dp, VnCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(42.dp)) {
            val w = size.width
            val h = size.height
            // Midline 1.0x reference
            drawLine(
                color = Color(0x33FFFFFF),
                start = Offset(0f, h * 0.55f),
                end = Offset(w, h * 0.55f),
                strokeWidth = 1f
            )

            val pts: List<Pair<Float, Float>> = when (curveId) {
                "MONTAGE" -> listOf(0f to 0.7f, 0.25f to 0.2f, 0.55f to 0.8f, 0.8f to 0.25f, 1f to 0.65f)
                "HERO" -> listOf(0f to 0.3f, 0.35f to 0.2f, 0.55f to 0.85f, 0.8f to 0.2f, 1f to 0.3f)
                "BULLET" -> listOf(0f to 0.25f, 0.4f to 0.9f, 0.65f to 0.9f, 1f to 0.2f)
                "JUMP_CUT" -> listOf(0f to 0.6f, 0.3f to 0.15f, 0.5f to 0.65f, 0.75f to 0.15f, 1f to 0.6f)
                "FLASH_IN" -> listOf(0f to 0.15f, 0.35f to 0.5f, 1f to 0.65f)
                "FLASH_OUT" -> listOf(0f to 0.65f, 0.65f to 0.5f, 1f to 0.15f)
                else -> listOf(0f to 0.55f, 0.5f to 0.55f, 1f to 0.55f)
            }

            val path = Path().apply {
                pts.forEachIndexed { idx, (px, py) ->
                    val x = px * w
                    val y = py * h
                    if (idx == 0) moveTo(x, y) else lineTo(x, y)
                }
            }
            drawPath(
                path = path,
                color = VnCyan,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            pts.forEach { (px, py) ->
                drawCircle(
                    color = NewsGold,
                    radius = 3.5.dp.toPx(),
                    center = Offset(px * w, py * h)
                )
            }
        }
    }
}
