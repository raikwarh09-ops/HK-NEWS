package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwitchCamera
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.model.NewsProject
import com.example.ui.theme.NewsAmber
import com.example.ui.theme.NewsGold
import com.example.ui.theme.NewsRedPrimary
import kotlin.math.roundToInt

/**
 * Authentic KineMaster Clip Properties / Tool Inspector Sheet:
 * Trim/Split, Chroma Key, Speed, Voice Changer, Animations, Pan/Zoom, and 3D Transitions
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KinemasterClipEditorSheet(
    project: NewsProject,
    selectedTrack: String,
    initialTab: Int = 0,
    onClose: () -> Unit,
    onTrimLeft: () -> Unit = {},
    onSplitAtPlayhead: () -> Unit = {},
    onTrimRight: () -> Unit = {},
    onUpdateChromaKey: (enabled: Boolean, color: String, similarity: Float) -> Unit,
    onUpdateVoiceChanger: (effect: String) -> Unit,
    onUpdateTransition: (transition: String) -> Unit,
    onUpdateAnimation: (inAnim: String, outAnim: String) -> Unit,
    onUpdateSpeed: (speed: Float) -> Unit,
    onUpdateContrastAndBrightness: (contrast: Float, brightness: Float) -> Unit,
    onUpdatePanZoom: (startScale: Float, endScale: Float, locked: Boolean) -> Unit = { _, _, _ -> },
    onUpdateAudioMixer: (volumeBoost: Float, pitch: Float, ducking: Boolean) -> Unit = { _, _, _ -> },
    onToggleAudioFade: (fadeIn: Boolean?, fadeOut: Boolean?) -> Unit = { _, _ -> },
    onUpdateExportSettings: (resolution: String, fps: Int) -> Unit = { _, _ -> },
    onOpenKeyframeDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var activeTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    val tabs = listOf(
        "✂️ ट्रिम/कट",
        "🟩 क्रोमा की",
        "🔍 पैन व ज़ूम",
        "🔊 ऑडियो मिक्सर",
        "🎙️ आवाज़ बदलें",
        "⚡ स्पीड",
        "✨ एनिमेशन",
        "🎞️ 3D ट्रांज़िशन",
        "🎨 कलर ग्रेडिंग",
        "🔑 कीफ़्रेम",
        "📐 रिज़ॉल्यूशन"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("kinemaster_clip_inspector"),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, NewsGold.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            // Header with Close
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
                            .size(24.dp)
                            .background(NewsRedPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("K", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                    Text(
                        text = "काइनमास्टर एडिटिंग टूल्स (KineMaster Studio)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "बंद करें", tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Scrollable Tabs
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                containerColor = Color(0xFF1E293B),
                contentColor = NewsGold,
                edgePadding = 4.dp,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
            ) {
                tabs.forEachIndexed { index, label ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (activeTab) {
                    // TAB 0: TRIM / SPLIT (काटें व छांटें)
                    0 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "वीडियो क्लिप कट और ट्रिमिंग (Trim & Split)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "काइनमास्टर की तरह टाइमलाइन पर जहां प्लेहेड है, वहां से वीडियो को तुरंत काटें:",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onTrimLeft,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                    modifier = Modifier.weight(1f).border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                                ) {
                                    Text("✂️ बाएं ट्रिम", fontSize = 11.sp, color = NewsGold)
                                }
                                Button(
                                    onClick = onSplitAtPlayhead,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                    modifier = Modifier.weight(1f).border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                                ) {
                                    Text("✂️ स्प्लिट", fontSize = 11.sp, color = Color.Cyan)
                                }
                                Button(
                                    onClick = onTrimRight,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                    modifier = Modifier.weight(1f).border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                                ) {
                                    Text("✂️ दाएं ट्रिम", fontSize = 11.sp, color = NewsGold)
                                }
                            }

                            Text(
                                text = "💡 टिप: नीचे टाइमलाइन में लाल प्लेहेड को उंगली से आगे-पीछे करके ऊपर कैंची पर क्लिक करें!",
                                color = NewsAmber,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // TAB 1: CHROMA KEY (ग्रीन स्क्रीन रिमूवर)
                    1 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "क्रोमा की / ग्रीन स्क्रीन (Chroma Key)",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "ग्रीन बैकग्राउंड हटाकर टीवी स्टूडियो बैकड्रॉप लगाएं",
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }

                                Switch(
                                    checked = project.chromaKeyEnabled,
                                    onCheckedChange = { isEnabled ->
                                        onUpdateChromaKey(isEnabled, project.chromaKeyColor, project.chromaKeySimilarity)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.Green,
                                        checkedTrackColor = Color(0xFF1B5E20)
                                    )
                                )
                            }

                            if (project.chromaKeyEnabled) {
                                Text("की कलर (Key Color चुनिए):", color = Color.LightGray, fontSize = 11.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(
                                        "#00FF00" to "🟢 हरा (Green)",
                                        "#0000FF" to "🔵 नीला (Blue)",
                                        "#FFFFFF" to "⚪ सफ़ेद (White)"
                                    ).forEach { (hex, label) ->
                                        val selected = project.chromaKeyColor == hex
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (selected) NewsGold else Color(0xFF1E293B))
                                                .clickable { onUpdateChromaKey(true, hex, project.chromaKeySimilarity) }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
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

                                // Similarity Slider
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("थ्रेशोल्ड संवेदनशीलता (Similarity)", color = Color.Gray, fontSize = 11.sp)
                                        Text("${(project.chromaKeySimilarity * 100).toInt()}%", color = NewsAmber, fontSize = 11.sp)
                                    }
                                    Slider(
                                        value = project.chromaKeySimilarity,
                                        onValueChange = { onUpdateChromaKey(true, project.chromaKeyColor, it) },
                                        valueRange = 0.1f..0.9f,
                                        colors = SliderDefaults.colors(thumbColor = Color.Green, activeTrackColor = Color.Green)
                                    )
                                }
                            }
                        }
                    }

                    // TAB 2: PAN & ZOOM (पैन और ज़ूम / Ken Burns Crop)
                    2 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("काइनमास्टर पैन और ज़ूम (Pan & Zoom)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("शुरुआती और अंतिम फ्रेम का ज़ूम सेट करें (Ken Burns)", color = Color.Gray, fontSize = 11.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("बराबर (=)", color = NewsGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Switch(
                                        checked = project.panZoomLocked,
                                        onCheckedChange = { locked ->
                                            onUpdatePanZoom(project.panZoomStartScale, if (locked) project.panZoomStartScale else project.panZoomEndScale, locked)
                                        },
                                        colors = SwitchDefaults.colors(checkedThumbColor = NewsGold, checkedTrackColor = NewsRedPrimary)
                                    )
                                }
                            }

                            // Start Position Zoom Slider
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("🟢 शुरुआती फ़्रेम ज़ूम (Start Position):", color = Color.LightGray, fontSize = 11.sp)
                                    Text("${(project.panZoomStartScale * 100).toInt()}%", color = Color.Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = project.panZoomStartScale,
                                    onValueChange = {
                                        onUpdatePanZoom(it, if (project.panZoomLocked) it else project.panZoomEndScale, project.panZoomLocked)
                                    },
                                    valueRange = 1.0f..2.5f,
                                    colors = SliderDefaults.colors(thumbColor = Color.Green, activeTrackColor = Color.Green)
                                )
                            }

                            // End Position Zoom Slider
                            if (!project.panZoomLocked) {
                                Column {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("🔴 अंतिम फ़्रेम ज़ूम (End Position):", color = Color.LightGray, fontSize = 11.sp)
                                        Text("${(project.panZoomEndScale * 100).toInt()}%", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Slider(
                                        value = project.panZoomEndScale,
                                        onValueChange = { onUpdatePanZoom(project.panZoomStartScale, it, false) },
                                        valueRange = 1.0f..2.5f,
                                        colors = SliderDefaults.colors(thumbColor = Color.Red, activeTrackColor = Color.Red)
                                    )
                                }
                            }
                        }
                    }

                    // TAB 3: AUDIO MIXER (ऑडियो मिक्सर 200% बूस्ट और डकिंग)
                    3 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("काइनमास्टर ऑडियो मिक्सर (Volume 200% & Ducking)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                            // Master Volume 200% Booster
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("🔊 वॉल्यूम बूस्टर (0% - 200%):", color = Color.LightGray, fontSize = 11.sp)
                                    Text("${(project.audioVolumeBoost * 100).toInt()}%", color = NewsGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                }
                                Slider(
                                    value = project.audioVolumeBoost,
                                    onValueChange = { onUpdateAudioMixer(it, project.audioPitch, project.audioAutoDucking) },
                                    valueRange = 0.0f..2.0f,
                                    colors = SliderDefaults.colors(thumbColor = NewsGold, activeTrackColor = NewsRedPrimary)
                                )
                            }

                            // Auto Ducking
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("ऑटो-डकिंग (Auto-Ducking)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("एंकर के बोलते समय बैकग्राउंड म्यूजिक अपने-आप धीमा होगा", color = Color.Gray, fontSize = 10.sp)
                                }
                                Switch(
                                    checked = project.audioAutoDucking,
                                    onCheckedChange = { onUpdateAudioMixer(project.audioVolumeBoost, project.audioPitch, it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = NewsGold, checkedTrackColor = NewsRedPrimary)
                                )
                            }

                            // Audio Fade in / out
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (project.audioFadeIn) Color(0xFF1B5E20) else Color(0xFF1E293B))
                                        .clickable { onToggleAudioFade(!project.audioFadeIn, null) }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (project.audioFadeIn) "🎵 फेड-इन: चालू" else "🎵 फेड-इन: बंद",
                                        color = Color.White,
                                        fontSize = 10.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (project.audioFadeOut) Color(0xFF1B5E20) else Color(0xFF1E293B))
                                        .clickable { onToggleAudioFade(null, !project.audioFadeOut) }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (project.audioFadeOut) "🎵 फेड-आउट: चालू" else "🎵 फेड-आउट: बंद",
                                        color = Color.White,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    // TAB 4: VOICE CHANGER (आवाज़ बदलें)
                    4 -> {
                        val ctx = androidx.compose.ui.platform.LocalContext.current
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "🤖 ChatGPT AI वॉइस इफ़ेक्ट्स (ChatGPT Voice Modes)",
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )

                            val chatGptVoices = listOf(
                                "CHATGPT_COVE" to "💬 ChatGPT Cove (गहरी शांत)",
                                "CHATGPT_BREEZE" to "🌬️ ChatGPT Breeze (फ्रेंडली)",
                                "CHATGPT_EMBER" to "🔥 ChatGPT Ember (दमदार एंकर)",
                                "CHATGPT_JUNIPER" to "🎙️ ChatGPT Juniper (क्लियर AI)",
                                "CHATGPT_SKY" to "✨ ChatGPT Sky (स्मूद असिस्टेंट)",
                                "CHATGPT_SOL" to "☀️ ChatGPT Sol (न्यूज़ प्रो)"
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                chatGptVoices.forEach { (id, label) ->
                                    val selected = project.voiceChangerEffect == id
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (selected) Color(0xFF00E5FF) else Color(0xFF12233B))
                                            .border(0.8.dp, if (selected) Color.White else Color(0x6600E5FF), RoundedCornerShape(6.dp))
                                            .clickable {
                                                onUpdateVoiceChanger(id)
                                                com.example.audio.NewsAudioEffects.playVoiceEffectPreview(
                                                    id,
                                                    com.example.audio.NewsAudioEffects.resolveDefaultPitch(id)
                                                )
                                            }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
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

                            Text(
                                text = "काइनमास्टर स्टूडियो वॉइस इफ़ेक्ट्स व पिच:",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )

                            val voices = listOf(
                                "NORMAL" to "सामान्य (Original)",
                                "DEEP_ANCHOR" to "🎙️ गहरी एंकर आवाज़",
                                "FEMALE_ANCHOR" to "👩 महिला एंकर",
                                "RADIO_MIC" to "📻 वायरलेस माइक",
                                "ECHO_STUDIO" to "🏛️ लाइव स्टूडियो इको",
                                "CHIPMUNK" to "🐿️ तेज़ हाई पिच",
                                "ROBOT" to "🤖 AI रोबोट",
                                "MEGAPHONE" to "📢 लाउडस्पीकर",
                                "SLOW_CINEMA" to "🎬 भारी सिनेमा आवाज़"
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                voices.forEach { (id, label) ->
                                    val selected = project.voiceChangerEffect == id
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (selected) NewsGold else Color(0xFF1E293B))
                                            .border(0.5.dp, if (selected) NewsAmber else Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                                            .clickable {
                                                onUpdateVoiceChanger(id)
                                                com.example.audio.NewsAudioEffects.playVoiceEffectPreview(
                                                    id,
                                                    com.example.audio.NewsAudioEffects.resolveDefaultPitch(id)
                                                )
                                            }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (selected) Color.Black else Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("पिच (Pitch): ${(project.audioPitch * 100).roundToInt()}%", color = NewsAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "🗣️ हेडलाइन बोलें",
                                        color = NewsGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable {
                                            com.example.audio.NewsAudioEffects.speakTextWithVoiceEffect(
                                                ctx,
                                                project.headline,
                                                project.voiceChangerEffect,
                                                project.audioPitch
                                            )
                                        }
                                    )
                                    Text(
                                        text = "🔊 टेस्ट सुनें",
                                        color = Color.Cyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable {
                                            com.example.audio.NewsAudioEffects.playVoiceEffectPreview(project.voiceChangerEffect, project.audioPitch)
                                        }
                                    )
                                }
                            }
                            Slider(
                                value = project.audioPitch,
                                onValueChange = { onUpdateAudioMixer(project.audioVolumeBoost, it, project.audioAutoDucking) },
                                valueRange = 0.5f..1.8f,
                                colors = SliderDefaults.colors(thumbColor = NewsGold, activeTrackColor = NewsRedPrimary),
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }

                    // TAB 5: SPEED CONTROL (गति नियंत्रण)
                    5 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("वीडियो प्लेबैक स्पीड (0.25x - 4x)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${project.videoSpeed}x", color = NewsGold, fontWeight = FontWeight.Black, fontSize = 14.sp)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(0.25f to "0.25x स्लो", 0.5f to "0.5x", 1.0f to "1.0x सामान्य", 1.5f to "1.5x", 2.0f to "2.0x तेज़", 4.0f to "4.0x").forEach { (spd, lbl) ->
                                    val selected = project.videoSpeed == spd
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (selected) NewsRedPrimary else Color(0xFF1E293B))
                                            .clickable { onUpdateSpeed(spd) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = lbl,
                                            color = if (selected) Color.White else Color.Gray,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Slider(
                                value = project.videoSpeed,
                                onValueChange = onUpdateSpeed,
                                valueRange = 0.25f..3.0f,
                                colors = SliderDefaults.colors(thumbColor = NewsGold, activeTrackColor = NewsRedPrimary)
                            )
                        }
                    }

                    // TAB 6: ANIMATIONS (इन / आउट एनिमेशन)
                    6 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("इन-एनिमेशन (In-Animation प्रवेश स्टाइल):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            val inAnims = listOf(
                                "NONE" to "कोई नहीं",
                                "FADE_IN" to "🌟 फ़ेड इन (Fade)",
                                "POP_UP" to "💥 पॉप अप (Pop)",
                                "SLIDE_RIGHT" to "➡️ स्लाइड राइट",
                                "SLIDE_UP" to "⬆️ स्लाइड अप",
                                "SPIN_CLOCKWISE" to "🔄 3D स्पिन चक्र"
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                inAnims.forEach { (id, label) ->
                                    val selected = project.inAnimation == id
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (selected) NewsGold else Color(0xFF1E293B))
                                            .clickable { onUpdateAnimation(id, project.outAnimation) }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = label, color = if (selected) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // TAB 7: 3D TRANSITIONS (3D ट्रांज़िशन)
                    7 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("काइनमास्टर 3D न्यूज़ ट्रांज़िशन (3D Transitions):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            val transitions = listOf(
                                "NONE" to "बिना ट्रांज़िशन (Cut)",
                                "3D_NEWS_FLIP" to "🌐 3D न्यूज़ ग्लोब फ्लिप",
                                "CUBE_ROTATE" to "🧊 3D क्यूब रोटेशन",
                                "WIPE_RIGHT" to "⚡ ब्रेकिंग वाइप स्वाइप",
                                "ZOOM_BURST" to "🚀 ज़ूम ब्लास्ट पंच",
                                "CROSSFADE" to "✨ स्मूथ क्रॉसफ़ेड",
                                "GLITCH_TV" to "📺 टीवी ब्रॉडकास्ट ग्लिच"
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                transitions.forEach { (id, label) ->
                                    val selected = project.activeTransition == id
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (selected) NewsRedPrimary else Color(0xFF1E293B))
                                            .border(0.5.dp, if (selected) NewsGold else Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                                            .clickable { onUpdateTransition(id) }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = label, color = if (selected) Color.White else Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // TAB 8: COLOR GRADING
                    8 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("कंट्रास्ट: ${(project.videoContrast * 100).roundToInt()}%", color = Color.White, fontSize = 11.sp)
                                Text("ब्राइटनेस: ${(project.videoBrightness * 100).roundToInt()}%", color = NewsAmber, fontSize = 11.sp)
                            }

                            Slider(
                                value = project.videoContrast,
                                onValueChange = { onUpdateContrastAndBrightness(it, project.videoBrightness) },
                                valueRange = 0.6f..2.2f,
                                colors = SliderDefaults.colors(thumbColor = NewsGold, activeTrackColor = NewsGold)
                            )

                            Slider(
                                value = project.videoBrightness,
                                onValueChange = { onUpdateContrastAndBrightness(project.videoContrast, it) },
                                valueRange = -0.4f..0.4f,
                                colors = SliderDefaults.colors(thumbColor = Color.Cyan, activeTrackColor = Color.Cyan)
                            )
                        }
                    }

                    // TAB 9: KEYFRAME ANIMATION (कीफ़्रेम)
                    9 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("काइनमास्टर कीफ़्रेम (Keyframe Tool 🔑)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("किसी भी ग्राफ़िक्स, लोगो या टेक्स्ट को टाइमलाइन पर एनिमेट करें:", color = Color.LightGray, fontSize = 11.sp)

                            Button(
                                onClick = {
                                    onClose()
                                    onOpenKeyframeDialog()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NewsGold),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                Text("🔑 कीफ़्रेम कंट्रोलर खोलें (+ Add Keyframe)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    // TAB 10: EXPORT RESOLUTION & FPS
                    10 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("काइनमास्टर एक्सपोर्ट सेटिंग्स (Export & Resolution)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                            // Resolution
                            Text("रिज़ॉल्यूशन चुनें:", color = Color.LightGray, fontSize = 11.sp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("480p" to "480p SD", "720p" to "720p HD", "1080p" to "1080p FHD").forEach { (res, lbl) ->
                                    val sel = project.exportResolution == res
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (sel) NewsRedPrimary else Color(0xFF1E293B))
                                            .border(1.dp, if (sel) NewsGold else Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                                            .clickable { onUpdateExportSettings(res, project.exportFps) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(lbl, color = if (sel) Color.White else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Frame Rate (FPS)
                            Text("फ़्रेम रेट (Frame Rate):", color = Color.LightGray, fontSize = 11.sp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(24 to "24 सिनेमा", 30 to "30 FPS सामान्य", 60 to "60 FPS स्मूथ").forEach { (fps, lbl) ->
                                    val sel = project.exportFps == fps
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (sel) NewsGold else Color(0xFF1E293B))
                                            .clickable { onUpdateExportSettings(project.exportResolution, fps) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(lbl, color = if (sel) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
