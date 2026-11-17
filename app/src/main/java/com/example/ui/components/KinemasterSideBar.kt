package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NewsAmber
import com.example.ui.theme.NewsGold

import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key

/**
 * Authentic KineMaster Left Vertical Action Toolbar
 * Featuring Undo, Redo, Screen Capture, Keyframe Animation (Key), Duplicate, Delete, Timeline Expand, Settings
 */
@Composable
fun KinemasterSideBar(
    isTimelineExpanded: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onCaptureFrame: () -> Unit,
    onOpenKeyframe: () -> Unit = {},
    onDuplicateLayer: () -> Unit = {},
    onDeleteLayer: () -> Unit = {},
    onToggleExpandTimeline: () -> Unit,
    onJumpToBeginning: () -> Unit,
    onJumpToEnd: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xE60F172A))
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // 1. Undo
        IconButton(onClick = onUndo, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Undo, contentDescription = "अनडू (Undo)", tint = Color.White, modifier = Modifier.size(17.dp))
        }

        // 2. Redo
        IconButton(onClick = onRedo, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Redo, contentDescription = "रीडू (Redo)", tint = Color.White, modifier = Modifier.size(17.dp))
        }

        // 3. Capture Frame
        IconButton(onClick = onCaptureFrame, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Camera, contentDescription = "कैप्चर फ्रेम", tint = NewsGold, modifier = Modifier.size(17.dp))
        }

        // 4. KineMaster Keyframe Animation (🔑 चाबी)
        IconButton(onClick = onOpenKeyframe, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Key, contentDescription = "कीफ़्रेम (Keyframe)", tint = NewsAmber, modifier = Modifier.size(17.dp))
        }

        // 5. Duplicate Layer
        IconButton(onClick = onDuplicateLayer, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.ContentCopy, contentDescription = "डुप्लिकेट लेयर", tint = Color.LightGray, modifier = Modifier.size(15.dp))
        }

        // 6. Delete Layer
        IconButton(onClick = onDeleteLayer, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "हटाएं (Delete)", tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp))
        }

        // 7. Jump to Start
        IconButton(onClick = onJumpToBeginning, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.FirstPage, contentDescription = "शुरुआत में जाएं", tint = Color.Cyan, modifier = Modifier.size(17.dp))
        }

        // 8. Jump to End
        IconButton(onClick = onJumpToEnd, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.LastPage, contentDescription = "अंत में जाएं", tint = Color.Cyan, modifier = Modifier.size(17.dp))
        }

        // 9. Expand / Collapse Timeline
        IconButton(onClick = onToggleExpandTimeline, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = if (isTimelineExpanded) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                contentDescription = "टाइमलाइन विस्तार",
                tint = NewsGold,
                modifier = Modifier.size(17.dp)
            )
        }

        // 10. Settings
        IconButton(onClick = onOpenSettings, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Settings, contentDescription = "प्रोजेक्ट सेटिंग्स", tint = Color.LightGray, modifier = Modifier.size(16.dp))
        }
    }
}
