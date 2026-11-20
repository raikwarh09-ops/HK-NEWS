package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.NewsAmber
import com.example.ui.theme.NewsDeepNavy
import com.example.ui.theme.NewsGold
import com.example.ui.theme.NewsRedPrimary
import com.example.util.PcSoftwareExporter
import kotlinx.coroutines.launch

@Composable
fun PcSoftwareScreen(
    isDesktopLayoutForced: Boolean,
    onToggleDesktopLayout: (Boolean) -> Unit,
    onStatusMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isWorking by remember { mutableStateOf(false) }
    var lastSavedSuccess by remember { mutableStateOf(false) }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch {
                isWorking = true
                val uri = PcSoftwareExporter.savePcSoftwareToDownloads(context)
                isWorking = false
                lastSavedSuccess = uri != null
                onStatusMessage(
                    if (uri != null) {
                        "💻 HK_NEWS_PC_Studio.html फ़ोन के Downloads फ़ोल्डर में सेव हो गया!"
                    } else {
                        "⚠️ PC सॉफ्टवेयर फ़ाइल सेव नहीं हो सकी।"
                    }
                )
            }
        } else {
            onStatusMessage("⚠️ फ़ाइल सेव करने के लिए स्टोरेज अनुमति आवश्यक है।")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NewsDeepNavy),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hero Banner Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NewsGold.copy(alpha = 0.45f), RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0E172B))
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF141E38), Color(0xFF1E1024))
                                )
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(NewsRedPrimary, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Computer,
                                    contentDescription = "HK NEWS PC सॉफ्टवेयर",
                                    tint = NewsGold,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "HK NEWS PC Studio Pro",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "Windows 10/11 • macOS • Linux कंप्यूटर सॉफ्टवेयर",
                                    color = NewsGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Text(
                            text = "बिना किसी भारी सेटअप के सीधे अपने कंप्यूटर या लैपटॉप पर पूरा न्यूज़ स्टूडियो, काइनमास्टर व VN एडिटिंग टूल्स, लाइव वेबकैम और माउस ड्रैग-ड्रॉप चलाएँ।",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )

                        // Save to Downloads Button
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    scope.launch {
                                        isWorking = true
                                        val uri = PcSoftwareExporter.savePcSoftwareToDownloads(context)
                                        isWorking = false
                                        lastSavedSuccess = uri != null
                                        onStatusMessage(
                                            if (uri != null) {
                                                "💻 HK_NEWS_PC_Studio.html फ़ोन के Downloads फ़ोल्डर में सेव हो गया!"
                                            } else {
                                                "⚠️ PC सॉफ्टवेयर फ़ाइल सेव नहीं हो सकी।"
                                            }
                                        )
                                    }
                                } else {
                                    storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                }
                            },
                            enabled = !isWorking,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_download_pc_software"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "PC सॉफ्टवेयर डाउनलोड करें",
                                tint = NewsGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isWorking) "फ़ाइल तैयार हो रही है..." else "💻 PC सॉफ्टवेयर (Downloads) में सेव करें",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Share Directly to PC Button
                        Button(
                            onClick = {
                                scope.launch {
                                    val ok = PcSoftwareExporter.sharePcSoftwareFile(context)
                                    if (!ok) {
                                        onStatusMessage("⚠️ फ़ाइल शेयर करने में समस्या आई।")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_share_pc_software"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.IosShare,
                                contentDescription = "PC पर शेयर करें",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "📤 WhatsApp / Email / Drive से PC पर भेजें",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        if (lastSavedSuccess) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0E2A1E), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "सेव हो गया: Downloads/HK_NEWS_PC_Studio.html — इसे PC में कॉपी कर डबल-क्लिक से खोलें!",
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Desktop Navigation Mode Switch inside App (for Emulator / Tablet / PC Emulator)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF162035))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LaptopMac,
                                contentDescription = null,
                                tint = NewsAmber,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "PC साइड-बार लेआउट (Desktop UI)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "बड़ी स्क्रीन या PC एमुलेटर पर बाईं नेविगेशन पट्टी चालू करें",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Switch(
                            checked = isDesktopLayoutForced,
                            onCheckedChange = onToggleDesktopLayout,
                            modifier = Modifier.testTag("switch_desktop_layout"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NewsGold,
                                checkedTrackColor = NewsRedPrimary
                            )
                        )
                    }
                }
            }

            // Features in PC Software
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "PC सॉफ्टवेयर के मुख्य फीचर्स",
                            color = NewsGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        PcFeatureRow(
                            icon = Icons.Default.Videocam,
                            title = "माउस ड्रैग, हैंडराइटिंग, PIP और लाइव वेबकैम",
                            subtitle = "माउस से सभी ओवरले सेट करें, स्क्रीन पर पेन/तीर/गोला ड्रा करें, टेलीप्रॉम्प्टर चलाएँ व Split/PIP से रिकॉर्ड करें।"
                        )
                        PcFeatureRow(
                            icon = Icons.Default.Tune,
                            title = "ऐप के सभी KineMaster + VN व ChatGPT वॉइस टूल्स",
                            subtitle = "क्रोमा की, कीफ़्रेम, 3D ट्रांज़िशन, स्पीड कर्व, LUT, एनिमेशन फ़िल्टर, वीडियो क्रॉप, 6 ChatGPT AI वॉइस इफ़ेक्ट और 5-साउंड बोर्ड।"
                        )
                        PcFeatureRow(
                            icon = Icons.Default.Movie,
                            title = "2 घंटे तक वीडियो सेव + प्रोजेक्ट व टेम्पलेट्स",
                            subtitle = "हिंदी हेडलाइन टेम्पलेट्स, प्रोजेक्ट सेव/लोड और एडिटेड वीडियो व HD पोस्टर सीधे PC के Downloads में सेव होते हैं।"
                        )
                    }
                }
            }

            // 3-Step Guide
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101829))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "कंप्यूटर (Windows / Mac) में कैसे चलाएँ?",
                            color = NewsAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "1. ऊपर दिए बटन से HK_NEWS_PC_Studio.html को Downloads में सेव करें या WhatsApp/Email से अपने कंप्यूटर पर भेजें।",
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "2. कंप्यूटर में उस फ़ाइल पर डबल-क्लिक करें (Chrome या Edge ब्राउज़र में बिना इंटरनेट के खुलेगी)।",
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "3. Chrome/Edge में ऊपर दाईं ओर 'Install App / Create Shortcut' चुनकर इसे डेस्कटॉप सॉफ्टवेयर की तरह पिन करें। (डेवलपर्स के लिए प्रोजेक्ट में Electron .exe बिल्ड पैकेज भी शामिल है)",
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PcFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF00E5FF),
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
        )
        Column {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Text(
                text = subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}
