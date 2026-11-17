package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HindiHeadlineTemplate
import com.example.data.model.HindiTeleprompterPreset
import com.example.ui.theme.NewsAmber
import com.example.ui.theme.NewsDeepNavy
import com.example.ui.theme.NewsGold
import com.example.ui.theme.NewsRedDark
import com.example.ui.theme.NewsRedPrimary
import com.example.viewmodel.NewsStudioViewModel

@Composable
fun TemplatesScreen(
    viewModel: NewsStudioViewModel,
    onNavigateToStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("हेडलाइन टेम्पलेट्स", "प्रॉम्प्टर स्क्रिप्ट्स")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NewsDeepNavy)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = null,
                tint = NewsGold,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = "हिंदी न्यूज़ टेम्पलेट लाइब्रेरी",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "एक क्लिक में पेशेवर ब्रॉडकास्ट हेडलाइन लोड करें",
                    color = NewsAmber,
                    fontSize = 11.sp
                )
            }
        }

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color(0xFF131D31),
            contentColor = NewsGold,
            edgePadding = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (selectedTabIndex == index) NewsGold else Color.Gray
                        )
                    }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> {
                // Headline Templates List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(viewModel.headlineTemplates) { template ->
                        TemplateCard(
                            template = template,
                            onApply = {
                                viewModel.applyHeadlineTemplate(template)
                                onNavigateToStudio()
                            }
                        )
                    }
                }
            }

            1 -> {
                // Teleprompter Scripts List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(viewModel.teleprompterPresets) { preset ->
                        TeleprompterPresetCard(
                            preset = preset,
                            onApply = {
                                viewModel.applyTeleprompterPreset(preset)
                                onNavigateToStudio()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: HindiHeadlineTemplate,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("template_card_${template.category}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF162035)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(NewsRedPrimary, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = template.category,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }

                ElevatedButton(
                    onClick = onApply,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("लागू करें", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(12.dp))
                }
            }

            Text(
                text = template.headline,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                lineHeight = 17.sp
            )

            Text(
                text = template.subHeadline,
                color = NewsAmber,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )

            // Ticker preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                    .padding(6.dp)
            ) {
                Text(
                    text = "चलती पट्टी: ${template.defaultTicker}",
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun TeleprompterPresetCard(
    preset: HindiTeleprompterPreset,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("teleprompter_card_${preset.category}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF162035)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = NewsGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = preset.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                ElevatedButton(
                    onClick = onApply,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("प्रॉम्प्टर में लोड करें", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = preset.script,
                    color = Color(0xFFFFFF8D),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 4
                )
            }
        }
    }
}
