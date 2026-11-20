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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HkNewsBrandedBadge
import com.example.ui.theme.NewsAmber
import com.example.ui.theme.NewsDeepNavy
import com.example.ui.theme.NewsGold
import com.example.ui.theme.NewsRedPrimary
import com.example.util.AppLockManager
import com.example.viewmodel.NewsStudioViewModel

data class NewsColorPreset(
    val name: String,
    val hex: String
)

@Composable
fun ChannelSettingsScreen(
    viewModel: NewsStudioViewModel,
    onNavigateBack: () -> Unit,
    onLockApp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var requireEachLaunch by remember { mutableStateOf(AppLockManager.isRequireEachLaunch(context)) }
    val currentProject by viewModel.currentProject.collectAsState()

    var channelName by remember { mutableStateOf(currentProject.channelName) }
    var channelTagline by remember { mutableStateOf(currentProject.channelTagline) }
    var reporterName by remember { mutableStateOf(currentProject.reporterName) }
    var reporterRole by remember { mutableStateOf(currentProject.reporterRole) }
    var location by remember { mutableStateOf(currentProject.location) }
    var selectedColorHex by remember { mutableStateOf(currentProject.themeColorHex) }

    val colorPresets = listOf(
        NewsColorPreset("राष्ट्रीय लाल", "#C62828"),
        NewsColorPreset("गहरा ब्लू", "#1565C0"),
        NewsColorPreset("केसरिया समाचार", "#E65100"),
        NewsColorPreset("डार्क एमराल्ड", "#1B5E20"),
        NewsColorPreset("रॉयल पर्पल", "#4A148C"),
        NewsColorPreset("डार्क ग्रेफाइट", "#263238")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NewsDeepNavy)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = null,
                    tint = NewsGold,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "चैनल एवं रिपोर्टर प्रोफाइल",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "अपना निजी समाचार चैनल ब्रांड और लोगो कस्टमाइज़ करें",
                        color = NewsAmber,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Live Brand Preview Card
        item {
            val previewColor = remember(selectedColorHex) {
                try {
                    Color(android.graphics.Color.parseColor(selectedColorHex))
                } catch (_: Exception) {
                    NewsRedPrimary
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "लाइव ब्रांडिंग प्रिव्यू (Preview)",
                        color = NewsAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Channel Logo Box
                        HkNewsBrandedBadge(
                            channelName = channelName.ifBlank { "हर खबर" },
                            location = location.ifBlank { "नरसिंहपुर" }
                        )

                        // Location Chip
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF1E293B), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = location.ifBlank { "स्थान" },
                                color = NewsGold,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Reporter Preview Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(previewColor, Color(0xFF1E293B))
                                ),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = reporterName.ifBlank { "संवाददाता का नाम" },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${reporterRole.ifBlank { "पद / पदनाम" }} • ${channelTagline.ifBlank { "स्लोगन" }}",
                                color = NewsGold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Color Theme Presets
        item {
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = null,
                            tint = NewsGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "ब्रॉडकास्ट थीम रंग (Theme Color)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        colorPresets.forEach { preset ->
                            val isSelected = selectedColorHex.equals(preset.hex, ignoreCase = true)
                            val color = Color(android.graphics.Color.parseColor(preset.hex))

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorHex = preset.hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Channel Info Fields
        item {
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
                        text = "चैनल जानकारी",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = channelName,
                        onValueChange = { channelName = it },
                        label = { Text("चैनल का नाम (जैसे: भारत 24, जनता की आवाज़)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_channel_name"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NewsGold,
                            unfocusedBorderColor = Color(0x66FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = NewsGold
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = channelTagline,
                        onValueChange = { channelTagline = it },
                        label = { Text("चैनल स्लोगन (जैसे: सच की आवाज़ सबसे पहले)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_channel_tagline"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NewsAmber,
                            unfocusedBorderColor = Color(0x66FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = NewsAmber
                        ),
                        singleLine = true
                    )
                }
            }
        }

        // Reporter Info Fields
        item {
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
                        text = "संवाददाता / एंकर जानकारी",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = reporterName,
                        onValueChange = { reporterName = it },
                        label = { Text("संवाददाता का नाम") },
                        modifier = Modifier.fillMaxWidth().testTag("input_reporter_name"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NewsGold,
                            unfocusedBorderColor = Color(0x66FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = NewsGold
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = reporterRole,
                        onValueChange = { reporterRole = it },
                        label = { Text("पद / पदनाम (जैसे: विशेष संवाददाता)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_reporter_role"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NewsAmber,
                            unfocusedBorderColor = Color(0x66FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = NewsAmber
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("शहर / ज़िला (जैसे: नई दिल्ली, लखनऊ)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_location"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NewsGold,
                            unfocusedBorderColor = Color(0x66FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = NewsGold
                        ),
                        singleLine = true
                    )
                }
            }
        }

        // Security Lock Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF162035))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = NewsGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "सुरक्षा एवं पासवर्ड लॉक (App Lock)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "हर बार खुलने पर पासवर्ड मांगें",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "चालू रखने पर हर बार ऐप खोलते समय पासवर्ड डालना होगा",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = requireEachLaunch,
                            onCheckedChange = { enabled ->
                                requireEachLaunch = enabled
                                AppLockManager.setRequireEachLaunch(context, enabled)
                            },
                            modifier = Modifier.testTag("switch_require_password_each_launch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NewsGold,
                                checkedTrackColor = NewsRedPrimary
                            )
                        )
                    }

                    OutlinedButton(
                        onClick = onLockApp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_lock_app_now"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "अभी लॉक करें",
                            tint = NewsGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🔒 अभी ऐप लॉक करें",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    viewModel.updateChannel(channelName, channelTagline, selectedColorHex)
                    viewModel.updateReporter(reporterName, reporterRole, location)
                    viewModel.saveCurrentProject()
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_save_channel_settings"),
                colors = ButtonDefaults.buttonColors(containerColor = NewsRedPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "सेटिंग्स सहेजें और स्टूडियो में लागू करें",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
