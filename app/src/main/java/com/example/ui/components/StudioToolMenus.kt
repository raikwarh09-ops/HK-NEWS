package com.example.ui.components

import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HindiHeadlineTemplate
import com.example.data.model.NewsProject
import com.example.ui.theme.NewsAmber
import com.example.ui.theme.NewsGold
import com.example.ui.theme.NewsRedPrimary
import com.example.viewmodel.BackgroundSource

enum class StudioMenuCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color
) {
    KINEMASTER(
        id = "kinemaster",
        title = "काइनमास्टर",
        subtitle = "मीडिया व्हील, लेयर, क्रोमा की, कीफ़्रेम",
        icon = Icons.Default.MovieCreation,
        accentColor = Color(0xFFE53935)
    ),
    VN_TOOLS(
        id = "vn_tools",
        title = "VN टूल्स",
        subtitle = "कर्व स्पीड, LUT, मिरर, फ़्रीज़, मास्क",
        icon = Icons.Default.Speed,
        accentColor = Color(0xFF00ACC1)
    ),
    NEWS_SCREEN(
        id = "news_screen",
        title = "न्यूज़ व स्क्रीन",
        subtitle = "हेडलाइन, टिकर, ऐनिमेशन, टेम्पलेट्स",
        icon = Icons.Default.Newspaper,
        accentColor = Color(0xFFFFB300)
    ),
    EFFECTS_LOGO(
        id = "effects_logo",
        title = "इफ़ेक्ट व लोगो",
        subtitle = "लोगो, कंट्रास्ट, फ़िल्टर, VFX, स्टिकर",
        icon = Icons.Default.AutoAwesome,
        accentColor = Color(0xFF8E24AA)
    ),
    MEDIA_AUDIO(
        id = "media_audio",
        title = "मीडिया व साउंड",
        subtitle = "वीडियो जोड़ें, कैमरा, साउंडबोर्ड, प्रॉम्प्टर",
        icon = Icons.Default.VideoLibrary,
        accentColor = Color(0xFF1E88E5)
    ),
    RECORD_EXPORT(
        id = "record_export",
        title = "रिकॉर्ड व सेव",
        subtitle = "2 घंटे रिकॉर्डिंग, पोस्टर, डायरेक्ट गैलरी सेव",
        icon = Icons.Default.Save,
        accentColor = Color(0xFF2E7D32)
    ),
    ALL_TOOLS(
        id = "all_tools",
        title = "सभी टूल्स",
        subtitle = "सभी कार्ड एक साथ स्क्रॉल में देखें",
        icon = Icons.Default.Apps,
        accentColor = Color(0xFF546E7A)
    )
}

@Composable
fun StudioMainMenuBar(
    selectedCategory: StudioMenuCategory,
    onSelectCategory: (StudioMenuCategory) -> Unit,
    onOpenQuickMenuGrid: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("studio_main_menu_bar"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E172A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, NewsGold.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 8.dp),
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
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = NewsGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "स्टूडियो मुख्य टूल मेनू (All Tools Menu)",
                        color = NewsGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E293B))
                        .border(0.8.dp, NewsAmber.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .clickable { onOpenQuickMenuGrid() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("btn_open_all_menus_grid"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "मेनू ग्रिड खोलें",
                        tint = NewsAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "मेनू ग्रिड",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(StudioMenuCategory.entries) { category ->
                    val isSelected = selectedCategory == category
                    Row(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) {
                                    Brush.horizontalGradient(
                                        listOf(category.accentColor, category.accentColor.copy(alpha = 0.72f))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF17233A), Color(0xFF17233A))
                                    )
                                }
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 0.8.dp,
                                color = if (isSelected) NewsGold else Color(0x33FFFFFF),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectCategory(category) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("menu_tab_${category.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.title,
                            tint = if (isSelected) Color.White else category.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = category.title,
                            color = if (isSelected) Color.White else Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudioQuickMenuGridDialog(
    selectedCategory: StudioMenuCategory,
    onSelectCategory: (StudioMenuCategory) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.GridView, contentDescription = null, tint = NewsGold)
                Text(
                    text = "सभी एडिटिंग टूल्स मेनू",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "जिस टूल समूह पर काम करना हो उसे चुनें:",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                StudioMenuCategory.entries.forEach { category ->
                    val isSelected = selectedCategory == category
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF131D31))
                            .border(
                                width = if (isSelected) 1.5.dp else 0.7.dp,
                                color = if (isSelected) NewsGold else Color(0x33FFFFFF),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                onSelectCategory(category)
                                onDismiss()
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(category.accentColor.copy(alpha = 0.22f))
                                .border(1.dp, category.accentColor, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                tint = category.accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = category.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = category.subtitle,
                                color = Color(0xFFCBD5E1),
                                fontSize = 10.sp
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "चयनित",
                                tint = NewsGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Text("बंद करें", color = NewsGold, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun KinemasterWheelToolsSection(
    project: NewsProject,
    isVideoPlaying: Boolean,
    isVoiceRecording: Boolean,
    isHandwritingActive: Boolean,
    onTogglePlayPause: () -> Unit,
    onOpenMediaPicker: () -> Unit,
    onOpenLayerMenu: () -> Unit,
    onOpenAudioSoundboard: () -> Unit,
    onToggleVoiceRecording: () -> Unit,
    onOpenToolTab: (Int) -> Unit,
    onOpenKeyframe: () -> Unit,
    onToggleHandwriting: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("kinemaster_wheel_card"),
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
                KinemasterMediaWheel(
                    isPlaying = isVideoPlaying,
                    isVoiceRecording = isVoiceRecording,
                    onTogglePlayPause = onTogglePlayPause,
                    onOpenMediaPicker = onOpenMediaPicker,
                    onOpenLayerMenu = onOpenLayerMenu,
                    onOpenAudioSoundboard = onOpenAudioSoundboard,
                    onToggleVoiceRecording = onToggleVoiceRecording
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "काइनमास्टर मेनू (Pro Tools)",
                            color = NewsGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${project.exportResolution}•${project.exportFps}fps",
                            color = Color.Cyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1E293B))
                                .clickable { onOpenToolTab(10) }
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    val kmTools = listOf(
                        Triple("✂️ ट्रिम/कट", 0, project.videoTrimStart > 0f || project.videoTrimEnd < 1f),
                        Triple("🟩 क्रोमा की", 1, project.chromaKeyEnabled),
                        Triple("🔍 पैन व ज़ूम", 2, project.panZoomStartScale != 1f || project.panZoomEndScale != 1f),
                        Triple("🔊 ऑडियो ${(project.audioVolumeBoost * 100).toInt()}%", 3, project.audioVolumeBoost > 1f),
                        Triple("🎙️ आवाज़ बदलें", 4, project.voiceChangerEffect != "NORMAL"),
                        Triple("⚡ स्पीड ${project.videoSpeed}x", 5, project.videoSpeed != 1f),
                        Triple("✨ एनिमेशन", 6, project.inAnimation != "NONE"),
                        Triple("🎞️ 3D ट्रांज़िशन", 7, project.activeTransition != "NONE"),
                        Triple("🎨 कलर ग्रेड", 8, project.videoContrast != 1f || project.videoBrightness != 0f),
                        Triple("🔑 कीफ़्रेम", 9, project.keyframeEnabled)
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
                                            if (tabIdx == 9) onOpenKeyframe() else onOpenToolTab(tabIdx)
                                        }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
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
                                .clickable { onToggleHandwriting() }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
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
                                .clickable { onOpenLayerMenu() }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
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

@Composable
fun VideoPlayerControlCard(
    hasGalleryMedia: Boolean,
    isVideoPlaying: Boolean,
    onAddVideo: () -> Unit,
    onTogglePlayPause: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("video_add_play_card"),
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
                        text = "वीडियो जोड़ें और प्ले करें (Video Player)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                if (hasGalleryMedia) {
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
                Button(
                    onClick = onAddVideo,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp)
                        .testTag("btn_add_video"),
                    colors = ButtonDefaults.buttonColors(containerColor = NewsRedPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("वीडियो जोड़ें (Add Video)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_play_pause_video"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isVideoPlaying && hasGalleryMedia) Color(0xFF2E7D32) else Color(0xFF1565C0)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = if (isVideoPlaying && hasGalleryMedia) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isVideoPlaying && hasGalleryMedia) "रोकें (Pause)" else "चलाएं (Play)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecordAndExportCard(
    timelineDurationMs: Long,
    isRecording: Boolean,
    recordingSeconds: Int,
    isSavingToGallery: Boolean,
    onToggleRecording: () -> Unit,
    onSaveVideoToGallery: () -> Unit,
    onSavePoster: () -> Unit,
    onSaveProject: () -> Unit,
    onInstantFastSave: () -> Unit,
    onSelectDurationMs: (Long) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onToggleRecording,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
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
                    val recLabel = if (isRecording) {
                        if (recordingSeconds >= 3600) {
                            "रोकें (%02d:%02d:%02d)".format(
                                recordingSeconds / 3600,
                                (recordingSeconds % 3600) / 60,
                                recordingSeconds % 60
                            )
                        } else {
                            "रोकें (%02d:%02d)".format(
                                recordingSeconds / 60,
                                recordingSeconds % 60
                            )
                        }
                    } else {
                        "लाइव रिकॉर्डिंग शुरू"
                    }
                    Text(
                        text = recLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Button(
                onClick = onSaveVideoToGallery,
                modifier = Modifier
                    .weight(1.3f)
                    .height(48.dp)
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
                        contentDescription = "एडिटेड वीडियो गैलरी में सेव करें",
                        tint = NewsGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isSavingToGallery) "⚡ सेव हो रही..." else "🎬 गैलरी में सेव",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Button(
                onClick = onSavePoster,
                modifier = Modifier
                    .weight(0.7f)
                    .height(48.dp)
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

            IconButton(
                onClick = onSaveProject,
                modifier = Modifier
                    .height(48.dp)
                    .width(48.dp)
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

        Button(
            onClick = onInstantFastSave,
            enabled = !isSavingToGallery,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
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
                    contentDescription = "एडिटेड वीडियो सीधे फ़ोन गैलरी में सेव करें",
                    tint = NewsGold,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (isSavingToGallery) {
                        "⚡ एडिटेड वीडियो गैलरी में सेव हो रही है..."
                    } else {
                        "⚡ एडिटेड वीडियो सीधे फ़ोन गैलरी में सेव करें"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        val durationPresets = listOf(
            "10s" to 10_000L,
            "1m" to 60_000L,
            "5m" to 300_000L,
            "15m" to 900_000L,
            "30m" to 1_800_000L,
            "1 Hr" to 3_600_000L,
            "2 Hr" to 7_200_000L
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF131D31), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val totalMin = (timelineDurationMs / 60_000L)
            val durBadge = when {
                timelineDurationMs >= 3_600_000L -> "${timelineDurationMs / 3_600_000L} घंटे ${(totalMin % 60L)} मिनट"
                totalMin >= 1L -> "$totalMin मिनट"
                else -> "${timelineDurationMs / 1000L} सेकंड"
            }
            Text(
                text = "⏱️ वीडियो की लंबाई चुनें (अधिकतम 2 घंटे): अभी $durBadge",
                color = NewsGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                durationPresets.forEach { (label, ms) ->
                    val selected = kotlin.math.abs(timelineDurationMs - ms) < 1500L
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectDurationMs(ms) },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NewsRedPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.testTag("chip_duration_$label")
                    )
                }
            }
        }
    }
}

@Composable
fun ExportProgressAndSavedBanner(
    exportProgress: Float?,
    savedGalleryUri: Uri?,
    onCancelExport: () -> Unit,
    onOpenInGallery: (Uri) -> Unit,
    onShareMedia: (Uri) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (exportProgress != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF131D31))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LinearProgressIndicator(
                    progress = { exportProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = NewsGold,
                    trackColor = Color(0xFF1E293B)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "एडिटेड वीडियो तैयार हो रहा है: ${(exportProgress * 100).toInt()}%",
                        color = NewsAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = onCancelExport,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("btn_cancel_export"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("रोकें", color = Color(0xFFFF8A80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (savedGalleryUri != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_saved_gallery_actions"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E2A1E))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "फ़ोन गैलरी (Movies/HkNewsStudio) में सेव हो गया",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onOpenInGallery(savedGalleryUri) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_open_saved_gallery_inline"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = "गैलरी में खोलें",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("गैलरी में खोलें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onShareMedia(savedGalleryUri) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_share_saved_gallery_inline"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.IosShare,
                                contentDescription = "शेयर करें",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("शेयर करें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BackgroundSourceCard(
    backgroundSource: BackgroundSource,
    selectedAnimationTheme: NewsAnimationTheme,
    onSelectVirtualStudio: () -> Unit,
    onSelectCamera: () -> Unit,
    onSelectGalleryMedia: () -> Unit,
    onSelectAnimationTheme: (NewsAnimationTheme) -> Unit
) {
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
                    onClick = onSelectVirtualStudio,
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
                    onClick = onSelectCamera,
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
                    onClick = onSelectGalleryMedia,
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
                                .clickable { onSelectAnimationTheme(theme) }
                                .padding(vertical = 6.dp),
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

@Composable
fun TeleprompterControlCard(
    isTeleprompterActive: Boolean,
    teleprompterSpeed: Float,
    teleprompterFontSize: Float,
    onToggleActive: (Boolean) -> Unit,
    onChangeSpeed: (Float) -> Unit,
    onOpenScriptEditor: () -> Unit
) {
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
                    onCheckedChange = onToggleActive,
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
                        onValueChange = onChangeSpeed,
                        valueRange = 0.5f..2.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = NewsGold,
                            activeTrackColor = NewsRedPrimary
                        )
                    )

                    OutlinedButton(
                        onClick = onOpenScriptEditor,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewsHeadlineEditorCard(
    project: NewsProject,
    onUpdateCategory: (String) -> Unit,
    onUpdateHeadline: (String) -> Unit,
    onUpdateSubHeadline: (String) -> Unit,
    onUpdateHeadlineAnimation: (String) -> Unit,
    onUpdateTicker: (String) -> Unit,
    onUpdateTickerAnimSpeedTag: (anim: String?, speed: Float?, tagText: String?) -> Unit,
    onOpenScreenCustomizer: () -> Unit
) {
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
                Text(
                    text = "हेडलाइन एवं ब्रेकिंग पट्टी + ऐनिमेशन",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Button(
                    onClick = onOpenScreenCustomizer,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.DashboardCustomize, contentDescription = null, tint = NewsGold, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("मैनुअल स्क्रीन", color = NewsGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("ब्रेकिंग न्यूज़", "विशेष ख़बर", "मुख्य समाचार", "सुपरफास्ट 100", "ग्राउंड रिपोर्ट").forEach { cat ->
                    val selected = project.category == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (selected) NewsRedPrimary else Color(0xFF1E293B))
                            .clickable { onUpdateCategory(cat) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
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

            OutlinedTextField(
                value = project.headline,
                onValueChange = onUpdateHeadline,
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
                    val selected = project.headlineAnimation == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selected) NewsRedPrimary else Color(0xFF1E293B))
                            .border(0.6.dp, if (selected) NewsGold else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable { onUpdateHeadlineAnimation(key) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
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

            OutlinedTextField(
                value = project.subHeadline,
                onValueChange = onUpdateSubHeadline,
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

            OutlinedTextField(
                value = project.tickerTagText,
                onValueChange = { onUpdateTickerAnimSpeedTag(null, null, it) },
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

            OutlinedTextField(
                value = project.tickerText,
                onValueChange = onUpdateTicker,
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
                    val selected = project.tickerAnimation == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selected) NewsRedPrimary else Color(0xFF1E293B))
                            .border(0.6.dp, if (selected) NewsGold else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable { onUpdateTickerAnimSpeedTag(key, null, null) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("पट्टी चलने की गति (Speed):", color = Color.LightGray, fontSize = 10.sp)
                Text("${"%.1f".format(project.tickerSpeed)}x", color = NewsGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = project.tickerSpeed.coerceIn(0.4f, 2.5f),
                onValueChange = { onUpdateTickerAnimSpeedTag(null, it, null) },
                valueRange = 0.4f..2.5f,
                colors = SliderDefaults.colors(
                    thumbColor = NewsGold,
                    activeTrackColor = NewsRedPrimary
                )
            )
        }
    }
}

@Composable
fun QuickTemplatesCard(
    templates: List<HindiHeadlineTemplate>,
    onApplyTemplate: (HindiHeadlineTemplate) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "त्वरित ताज़ा ख़बरें (Quick Hindi Templates)",
            color = NewsGold,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )

        templates.take(4).forEach { template ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onApplyTemplate(template) },
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
