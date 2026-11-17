package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

val HEADLINE_ANIMATION_OPTIONS = listOf(
    "NONE" to "स्थिर (None)",
    "SHINE_SWEEP" to "✨ गोल्डन शाइन",
    "PULSE_GLOW" to "🔥 पल्स ग्लो",
    "SLIDE_REVEAL" to "➡️ स्लाइड लाइन",
    "NEON_FLASH" to "⚡ निऑन फ़्लैश",
    "ZOOM_POP" to "🔍 ज़ूम पॉप"
)

val TICKER_ANIMATION_OPTIONS = listOf(
    "NONE" to "साधारण (None)",
    "BREAKING_SWEEP" to "✨ ब्रेकिंग शाइन",
    "PULSE_BORDER" to "🔥 पल्स बॉर्डर",
    "FLASH_TAG" to "🚨 फ़्लैश टैग",
    "WAVE_GLOW" to "🌊 वेव ग्लो"
)

val DEFAULT_SECTIONS_LIST = listOf(
    "VIDEO" to "🎬 वीडियो प्लेयर व प्लेबैक",
    "RECORD" to "🔴 रिकॉर्डिंग व गैलरी सेव बार",
    "HEADLINE" to "📰 हेडलाइन व न्यूज़ पट्टी संपादक",
    "SOURCE" to "🖼️ बैकग्राउंड स्रोत व एनीमेशन थीम्स",
    "TOOLS" to "🎛️ वीडियो एडिटिंग व फ़िल्टर टूल्स",
    "SOUND" to "🔊 साउंड इफ़ेक्ट्स व बीप बोर्ड",
    "PROMPTER" to "📜 टेलीप्रॉम्प्टर कंट्रोलर"
)

@Composable
fun ScreenLayoutCustomizerDialog(
    project: NewsProject,
    initialTab: Int = 0,
    onDismiss: () -> Unit,
    onReorderSection: (sectionId: String, moveUp: Boolean) -> Unit,
    onToggleSectionVisibility: (sectionId: String) -> Unit,
    onResetSections: () -> Unit,
    onUpdateElementPosition: (element: String, position: String) -> Unit,
    onUpdateLogoScale: (scale: Float) -> Unit,
    onUpdateLogoAnimation: (animation: String) -> Unit,
    onApplyPreset: (presetName: String) -> Unit,
    onToggleOverlayElement: (
        logo: Boolean?,
        live: Boolean?,
        reporter: Boolean?,
        headline: Boolean?,
        ticker: Boolean?,
        clock: Boolean?,
        location: Boolean?,
        stickers: Boolean?
    ) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onUpdateElementScale: (element: String, scale: Float) -> Unit = { _, _ -> },
    onUpdateHeadlineAnimation: (anim: String) -> Unit = {},
    onUpdateTickerAnimationAndSpeed: (anim: String?, speed: Float?, tagText: String?) -> Unit = { _, _, _ -> },
    onUpdateManualTexts: (liveText: String?, manualClock: String?, tickerTag: String?) -> Unit = { _, _, _ -> },
    onResetAllPositions: () -> Unit = {}
) {
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 3)) }
    val tabTitles = listOf("🎛️ मैनुअल", "✨ एनीमेशन", "📱 टूल्स क्रम", "⚡ प्रीसेट्स")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DashboardCustomize,
                    contentDescription = null,
                    tint = NewsGold,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "न्यूज़ स्क्रीन मैनुअल कंट्रोलर",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(455.dp)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF0F172A),
                    contentColor = NewsGold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = NewsRedPrimary
                        )
                    }
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 10.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) NewsGold else Color.LightGray,
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                when (selectedTab) {
                    0 -> ManualScreenServicesTab(
                        project = project,
                        onToggleOverlayElement = onToggleOverlayElement,
                        onUpdateElementScale = onUpdateElementScale,
                        onUpdateManualTexts = onUpdateManualTexts,
                        onUpdateElementPosition = onUpdateElementPosition,
                        onResetAllPositions = onResetAllPositions
                    )

                    1 -> OverlayElementsTab(
                        project = project,
                        onUpdateElementPosition = onUpdateElementPosition,
                        onUpdateLogoScale = onUpdateLogoScale,
                        onUpdateLogoAnimation = onUpdateLogoAnimation,
                        onUpdateHeadlineAnimation = onUpdateHeadlineAnimation,
                        onUpdateTickerAnimationAndSpeed = onUpdateTickerAnimationAndSpeed
                    )

                    2 -> ReorderSectionsTab(
                        project = project,
                        onReorderSection = onReorderSection,
                        onToggleSectionVisibility = onToggleSectionVisibility,
                        onResetSections = onResetSections
                    )

                    3 -> PresetsTab(
                        onApplyPreset = onApplyPreset
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NewsRedPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_customizer_done")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("सहेजें व पूर्ण (Done)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF131D31)
    )
}

@Composable
private fun ManualScreenServicesTab(
    project: NewsProject,
    onToggleOverlayElement: (
        logo: Boolean?,
        live: Boolean?,
        reporter: Boolean?,
        headline: Boolean?,
        ticker: Boolean?,
        clock: Boolean?,
        location: Boolean?,
        stickers: Boolean?
    ) -> Unit,
    onUpdateElementScale: (element: String, scale: Float) -> Unit,
    onUpdateManualTexts: (liveText: String?, manualClock: String?, tickerTag: String?) -> Unit,
    onUpdateElementPosition: (element: String, position: String) -> Unit,
    onResetAllPositions: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("manual_screen_services_list"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "सभी स्क्रीन सेवाएं चालू/बंद और सेट करें:",
                    color = NewsAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = onResetAllPositions,
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = NewsGold, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("सब रीसेट", color = NewsGold, fontSize = 10.sp)
                }
            }
        }

        // All Overlay Visibility Toggles
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ManualToggleRow("📺 चैनल लोगो (Channel Logo)", project.showLogo) {
                    onToggleOverlayElement(it, null, null, null, null, null, null, null)
                }
                ManualToggleRow("🔴 LIVE बैज (Live Badge)", project.showLiveBadge) {
                    onToggleOverlayElement(null, it, null, null, null, null, null, null)
                }
                ManualToggleRow("📍 स्थान बैज (Location Chip)", project.showLocation) {
                    onToggleOverlayElement(null, null, null, null, null, null, it, null)
                }
                ManualToggleRow("🕒 घड़ी / समय (Time Clock)", project.showClock) {
                    onToggleOverlayElement(null, null, null, null, null, it, null, null)
                }
                ManualToggleRow("🏷️ ब्रेकिंग स्टिकर्स (Stickers)", project.showStickers) {
                    onToggleOverlayElement(null, null, null, null, null, null, null, it)
                }
                ManualToggleRow("🎙️ रिपोर्टर पट्टी (Reporter Card)", project.showReporterCard) {
                    onToggleOverlayElement(null, null, it, null, null, null, null, null)
                }
                ManualToggleRow("📰 मुख्य हेडलाइन (Headline Box)", project.showHeadline) {
                    onToggleOverlayElement(null, null, null, it, null, null, null, null)
                }
                ManualToggleRow("⚡ ब्रेकिंग पट्टी (Scrolling Ticker)", project.showTicker) {
                    onToggleOverlayElement(null, null, null, null, it, null, null, null)
                }
            }
        }

        // Manual Custom Texts (Live Text, Ticker Tag, Manual Clock)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "मैनुअल बैज व पट्टी टेक्स्ट सेटिंग्स:",
                    color = NewsGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = project.liveBadgeText,
                    onValueChange = { onUpdateManualTexts(it, null, null) },
                    label = { Text("LIVE बैज टेक्स्ट (जैसे LIVE / LIVE TV)", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_manual_live_text"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NewsGold,
                        unfocusedBorderColor = Color(0x55FFFFFF),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = NewsGold,
                        unfocusedLabelColor = Color.LightGray
                    )
                )
                OutlinedTextField(
                    value = project.tickerTagText,
                    onValueChange = { onUpdateManualTexts(null, null, it) },
                    label = { Text("ब्रेकिंग पट्टी टैग (जैसे ⚡ ताज़ा अपडेट)", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_manual_ticker_tag"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NewsGold,
                        unfocusedBorderColor = Color(0x55FFFFFF),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = NewsGold,
                        unfocusedLabelColor = Color.LightGray
                    )
                )
                OutlinedTextField(
                    value = project.manualClockText,
                    onValueChange = { onUpdateManualTexts(null, it, null) },
                    label = { Text("मैनुअल समय/दिनांक (खाली छोड़ने पर ऑटो समय)", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_manual_clock_text"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NewsAmber,
                        unfocusedBorderColor = Color(0x55FFFFFF),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = NewsAmber,
                        unfocusedLabelColor = Color.LightGray
                    )
                )
            }
        }

        // Per-Element Manual Scale / Size Sliders
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "हर सेवा का आकार मैनुअल सेट करें (Size 70%-160%):",
                    color = NewsGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                ScaleSliderRow("हेडलाइन बॉक्स आकार", project.headlineScale) {
                    onUpdateElementScale("HEADLINE", it)
                }
                ScaleSliderRow("ब्रेकिंग पट्टी आकार", project.tickerScale) {
                    onUpdateElementScale("TICKER", it)
                }
                ScaleSliderRow("रिपोर्टर कार्ड आकार", project.reporterScale) {
                    onUpdateElementScale("REPORTER", it)
                }
                ScaleSliderRow("LIVE बैज आकार", project.liveBadgeScale) {
                    onUpdateElementScale("LIVE_BADGE", it)
                }
                ScaleSliderRow("घड़ी / समय आकार", project.clockScale) {
                    onUpdateElementScale("CLOCK", it)
                }
            }
        }

        // Quick Position Placement
        item {
            ElementPositionControl(
                title = "LIVE बैज स्थान (LIVE Position):",
                currentPosition = project.liveBadgePosition,
                options = listOf("TOP_LEFT" to "बाएं ऊपर", "TOP_RIGHT" to "दाएं ऊपर"),
                onSelect = { onUpdateElementPosition("LIVE_BADGE", it) }
            )
        }
        item {
            ElementPositionControl(
                title = "घड़ी स्थान (Clock Position):",
                currentPosition = project.clockPosition,
                options = listOf("TOP_RIGHT" to "दाएं ऊपर", "TOP_LEFT" to "बाएं ऊपर"),
                onSelect = { onUpdateElementPosition("CLOCK", it) }
            )
        }
    }
}

@Composable
private fun ManualToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NewsRedPrimary
            )
        )
    }
}

@Composable
private fun ScaleSliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.LightGray, fontSize = 10.sp)
            Text("${(value * 100).toInt()}%", color = NewsGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value.coerceIn(0.7f, 1.6f),
            onValueChange = onValueChange,
            valueRange = 0.7f..1.6f,
            colors = SliderDefaults.colors(
                thumbColor = NewsGold,
                activeTrackColor = NewsRedPrimary
            )
        )
    }
}

@Composable
private fun ReorderSectionsTab(
    project: NewsProject,
    onReorderSection: (sectionId: String, moveUp: Boolean) -> Unit,
    onToggleSectionVisibility: (sectionId: String) -> Unit,
    onResetSections: () -> Unit
) {
    val currentOrder = remember(project.studioSectionsOrder) {
        val parsed = project.studioSectionsOrder.split(",").filter { it.isNotBlank() }
        val map = DEFAULT_SECTIONS_LIST.toMap()
        parsed.map { id -> id to (map[id] ?: id) }
    }
    val hiddenSet = remember(project.hiddenSections) {
        project.hiddenSections.split(",").filter { it.isNotBlank() }.toSet()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "स्क्रीन पर फ़ंक्शन ऊपर-नीचे जमाएं:",
                color = NewsAmber,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(
                onClick = onResetSections,
                modifier = Modifier.height(28.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = NewsGold, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("रीसेट", color = NewsGold, fontSize = 10.sp)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(currentOrder) { index, (id, label) ->
                val isHidden = hiddenSet.contains(id)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isHidden) Color(0xFF1E293B).copy(alpha = 0.5f) else Color(0xFF1E293B)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp,
                        if (isHidden) Color(0x22FFFFFF) else Color(0x55FFFFFF)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${index + 1}.",
                                color = NewsGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = label,
                                color = if (isHidden) Color.Gray else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // Move Up
                            IconButton(
                                onClick = { onReorderSection(id, true) },
                                enabled = index > 0,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.ArrowUpward,
                                    contentDescription = "ऊपर",
                                    tint = if (index > 0) NewsGold else Color.DarkGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Move Down
                            IconButton(
                                onClick = { onReorderSection(id, false) },
                                enabled = index < currentOrder.size - 1,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.ArrowDownward,
                                    contentDescription = "नीचे",
                                    tint = if (index < currentOrder.size - 1) NewsGold else Color.DarkGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Toggle Visibility
                            IconButton(
                                onClick = { onToggleSectionVisibility(id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "दिखाएं / छुपाएं",
                                    tint = if (isHidden) Color.Red else Color(0xFF4CAF50),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverlayElementsTab(
    project: NewsProject,
    onUpdateElementPosition: (element: String, position: String) -> Unit,
    onUpdateLogoScale: (scale: Float) -> Unit,
    onUpdateLogoAnimation: (animation: String) -> Unit,
    onUpdateHeadlineAnimation: (anim: String) -> Unit,
    onUpdateTickerAnimationAndSpeed: (anim: String?, speed: Float?, tagText: String?) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Headline Animation Selection
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "📰 हेडलाइन ऐनिमेशन (Headline Animation):",
                    color = NewsGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(HEADLINE_ANIMATION_OPTIONS.size) { i ->
                        val (key, label) = HEADLINE_ANIMATION_OPTIONS[i]
                        val isSelected = project.headlineAnimation == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) NewsRedPrimary else Color(0xFF1E293B))
                                .clickable { onUpdateHeadlineAnimation(key) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else Color.LightGray,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Breaking News Ticker Animation + Speed
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "⚡ ब्रेकिंग न्यूज़ पट्टी ऐनिमेशन (Ticker Animation):",
                    color = NewsAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(TICKER_ANIMATION_OPTIONS.size) { i ->
                        val (key, label) = TICKER_ANIMATION_OPTIONS[i]
                        val isSelected = project.tickerAnimation == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) NewsRedPrimary else Color(0xFF1E293B))
                                .clickable { onUpdateTickerAnimationAndSpeed(key, null, null) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else Color.LightGray,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("पट्टी स्क्रॉल गति (Speed):", color = Color.LightGray, fontSize = 10.sp)
                    Text("${"%.1f".format(project.tickerSpeed)}x", color = NewsGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = project.tickerSpeed.coerceIn(0.4f, 2.5f),
                    onValueChange = { onUpdateTickerAnimationAndSpeed(null, it, null) },
                    valueRange = 0.4f..2.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = NewsGold,
                        activeTrackColor = NewsRedPrimary
                    )
                )
            }
        }

        // Logo Position
        item {
            ElementPositionControl(
                title = "1. चैनल लोगो की स्थिति (Logo Position):",
                currentPosition = project.logoPosition,
                options = listOf(
                    "TOP_RIGHT" to "दाएं ऊपर",
                    "TOP_LEFT" to "बाएं ऊपर",
                    "BOTTOM_RIGHT" to "दाएं नीचे",
                    "BOTTOM_LEFT" to "बाएं नीचे"
                ),
                onSelect = { onUpdateElementPosition("LOGO", it) }
            )
        }

        // Logo Animation Filter Selection
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "लोगो एनीमेशन फ़िल्टर (Unlimited Animations):",
                        color = NewsAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        LOGO_ANIMATION_FILTERS.firstOrNull { it.first == project.logoAnimation }?.second ?: project.logoAnimation,
                        color = NewsGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(LOGO_ANIMATION_FILTERS.size) { i ->
                        val (filterKey, filterLabel) = LOGO_ANIMATION_FILTERS[i]
                        val isSelected = project.logoAnimation == filterKey
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) NewsRedPrimary else Color(0xFF1E293B))
                                .clickable { onUpdateLogoAnimation(filterKey) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = filterLabel,
                                color = if (isSelected) Color.White else Color.LightGray,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Logo Scale Slider
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("लोगो का आकार (Logo Size):", color = Color.White, fontSize = 11.sp)
                    Text("${(project.logoScale * 100).toInt()}%", color = NewsGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = project.logoScale,
                    onValueChange = onUpdateLogoScale,
                    valueRange = 0.6f..1.8f,
                    colors = SliderDefaults.colors(
                        thumbColor = NewsGold,
                        activeTrackColor = NewsRedPrimary
                    )
                )
            }
        }

        // Headline Position
        item {
            ElementPositionControl(
                title = "2. मुख्य हेडलाइन पट्टी (Headline Placement):",
                currentPosition = project.headlinePosition,
                options = listOf(
                    "BOTTOM" to "निचली पट्टी (Classic)",
                    "CENTER" to "मध्य में (Breaking Pop)",
                    "TOP" to "शीर्ष पर (Top Banner)"
                ),
                onSelect = { onUpdateElementPosition("HEADLINE", it) }
            )
        }

        // Ticker Tape Position
        item {
            ElementPositionControl(
                title = "3. चलती पट्टी (Ticker Tape Placement):",
                currentPosition = project.tickerPosition,
                options = listOf(
                    "BOTTOM" to "सबसे नीचे",
                    "TOP" to "सबसे ऊपर"
                ),
                onSelect = { onUpdateElementPosition("TICKER", it) }
            )
        }

        // Reporter Lower Third Position
        item {
            ElementPositionControl(
                title = "4. रिपोर्टर पट्टी (Reporter Lower Third):",
                currentPosition = project.reporterPosition,
                options = listOf(
                    "BOTTOM_LEFT" to "बाएं नीचे",
                    "BOTTOM_RIGHT" to "दाएं नीचे"
                ),
                onSelect = { onUpdateElementPosition("REPORTER", it) }
            )
        }

        // LIVE Badge Position
        item {
            ElementPositionControl(
                title = "5. LIVE बैज (Live Indicator):",
                currentPosition = project.liveBadgePosition,
                options = listOf(
                    "TOP_LEFT" to "बाएं शीर्ष",
                    "TOP_RIGHT" to "दाएं शीर्ष"
                ),
                onSelect = { onUpdateElementPosition("LIVE_BADGE", it) }
            )
        }

        // Clock & Location Position
        item {
            ElementPositionControl(
                title = "6. घड़ी व स्थान बैज (Clock / Location):",
                currentPosition = project.clockPosition,
                options = listOf(
                    "TOP_RIGHT" to "दाएं शीर्ष",
                    "TOP_LEFT" to "बाएं शीर्ष"
                ),
                onSelect = { onUpdateElementPosition("CLOCK", it) }
            )
        }
    }
}

@Composable
private fun ElementPositionControl(
    title: String,
    currentPosition: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = title, color = NewsAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { (posKey, label) ->
                val selected = currentPosition == posKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selected) NewsRedPrimary else Color(0xFF1E293B))
                        .clickable { onSelect(posKey) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
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
    }
}

@Composable
private fun PresetsTab(
    onApplyPreset: (presetName: String) -> Unit
) {
    val presets = listOf(
        Triple("CLASSIC_TV", "📺 क्लासिक टीवी न्यूज़ (Classic TV News)", "लोगो दाएं ऊपर, हेडलाइन नीचे, टिकर सबसे नीचे, लाइव बाएं ऊपर।"),
        Triple("BREAKING_FOCUS", "⚡ ब्रेकिंग न्यूज़ फोकस (Breaking News)", "हेडलाइन स्क्रीन के मध्य में बड़ी पट्टी के रूप में, लाइव दाएं ऊपर।"),
        Triple("REELS_SHORTS", "📱 मोबाइल रील्स व शॉर्ट्स (9:16 Format)", "वर्टिकल स्क्रीन हेतु अनुकूलित। लोगो ऊपर, हेडलाइन केंद्र में।"),
        Triple("CLEAN_INTERVIEW", "🎙️ क्लीन इंटरव्यू व टॉक शो", "बिना टिकर के स्वच्छ प्रसारण, केवल रिपोर्टर और चैनल का लोगो।")
    )

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(presets) { _, (id, title, desc) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onApplyPreset(id) },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFD54F))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            color = NewsGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(NewsRedPrimary, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("लागू करें", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = desc,
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )
                }
            }
        }
    }
}
