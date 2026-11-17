package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.NewsAmber
import com.example.ui.theme.NewsGold
import com.example.ui.theme.NewsRedPrimary

/**
 * Authentic KineMaster 5-Layer Flyout Menu:
 * 1. 📁 Media (मीडिया / PIP - Picture in picture video/image)
 * 2. ✨ Effect (इफ़ेक्ट FX - Blur, Glitch, Scanlines, VHS)
 * 3. 🎭 Sticker (स्टिकर - TV Live, Breaking badges, Arrows)
 * 4. 📝 Text (टेक्स्ट - News headline, sub-headline, ticker)
 * 5. ✍️ Handwriting (हैंडराइटिंग - Finger drawing on screen)
 */
@Composable
fun KinemasterLayerMenu(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSelectMedia: () -> Unit,
    onSelectEffect: () -> Unit,
    onSelectSticker: () -> Unit,
    onSelectText: () -> Unit,
    onSelectHandwriting: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .shadow(16.dp, RoundedCornerShape(20.dp))
                .testTag("kinemaster_layer_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF20F172A)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, NewsGold.copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(NewsRedPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Layers,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "काइनमास्टर लेयर मेनू (Layer)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "वीडियो पर नई परत या ग्राफ़िक्स जोड़ें",
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "बंद करें",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // The 5 Iconic KineMaster Layer Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Media (PIP - Picture-in-Picture)
                    LayerOptionItem(
                        icon = Icons.Default.Movie,
                        iconBg = Brush.radialGradient(listOf(Color(0xFFE53935), Color(0xFFB71C1C))),
                        titleHindi = "मीडिया (Media / PIP)",
                        subtitle = "गैलरी से दूसरी वीडियो या फ़ोटो लेयर जोड़ें (PIP बॉक्स)",
                        onClick = {
                            onDismiss()
                            onSelectMedia()
                        }
                    )

                    // 2. Effect (FX)
                    LayerOptionItem(
                        icon = Icons.Default.AutoAwesome,
                        iconBg = Brush.radialGradient(listOf(Color(0xFF8E24AA), Color(0xFF4A148C))),
                        titleHindi = "इफ़ेक्ट (Effect / FX)",
                        subtitle = "ग्लिच, ब्लर, स्कैनलाइन, मोशन वियू, विंटेज टीवी",
                        onClick = {
                            onDismiss()
                            onSelectEffect()
                        }
                    )

                    // 3. Sticker (Overlays)
                    LayerOptionItem(
                        icon = Icons.Default.EmojiEmotions,
                        iconBg = Brush.radialGradient(listOf(Color(0xFF00ACC1), Color(0xFF006064))),
                        titleHindi = "स्टिकर (Stickers / ओवरले)",
                        subtitle = "लाइव स्टैम्प, ब्रेकिंग न्यूज़ टैग, माइक, तीर, अलर्ट",
                        onClick = {
                            onDismiss()
                            onSelectSticker()
                        }
                    )

                    // 4. Text (Headline & Ticker)
                    LayerOptionItem(
                        icon = Icons.Default.TextFields,
                        iconBg = Brush.radialGradient(listOf(Color(0xFFFFB300), Color(0xFFFF6F00))),
                        titleHindi = "टेक्स्ट (Text / हेडलाइन)",
                        subtitle = "न्यूज़ हेडलाइन, टिकर, रिपोर्टर का नाम, फॉन्ट स्टाइल",
                        onClick = {
                            onDismiss()
                            onSelectText()
                        }
                    )

                    // 5. Handwriting (Draw on screen)
                    LayerOptionItem(
                        icon = Icons.Default.Create,
                        iconBg = Brush.radialGradient(listOf(Color(0xFFD81B60), Color(0xFF880E4F))),
                        titleHindi = "हैंडराइटिंग (Handwriting / पेन)",
                        subtitle = "स्क्रीन पर उंगली से तीर बनाएं, गोला लगाएं या ड्रॉ करें",
                        onClick = {
                            onDismiss()
                            onSelectHandwriting()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LayerOptionItem(
    icon: ImageVector,
    iconBg: Brush,
    titleHindi: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E293B))
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(iconBg)
                .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = titleHindi,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titleHindi,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = subtitle,
                color = Color.LightGray,
                fontSize = 10.sp
            )
        }

        Text(
            text = "जोड़ें ➕",
            color = NewsGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
