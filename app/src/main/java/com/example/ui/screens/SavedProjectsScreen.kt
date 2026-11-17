package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NewsProject
import com.example.ui.theme.NewsAmber
import com.example.ui.theme.NewsDeepNavy
import com.example.ui.theme.NewsGold
import com.example.ui.theme.NewsRedPrimary
import com.example.viewmodel.NewsStudioViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SavedProjectsScreen(
    viewModel: NewsStudioViewModel,
    onOpenProjectInStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.savedProjects.collectAsState()
    var projectToDelete by remember { mutableStateOf<NewsProject?>(null) }

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
                imageVector = Icons.Default.FolderOpen,
                contentDescription = null,
                tint = NewsGold,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = "सहेजे गए न्यूज़ प्रोजेक्ट्स",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "कुल प्रोजेक्ट्स: ${projects.size}",
                    color = NewsAmber,
                    fontSize = 11.sp
                )
            }
        }

        if (projects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "अभी कोई प्रोजेक्ट सहेजा नहीं गया है",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "स्टूडियो में अपनी पहली हेडलाइन और न्यूज़ वीडियो बनाकर 'सहेजें' बटन दबाएं।",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Button(
                        onClick = onOpenProjectInStudio,
                        colors = ButtonDefaults.buttonColors(containerColor = NewsRedPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("स्टूडियो में जाएं", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(projects) { project ->
                    SavedProjectItem(
                        project = project,
                        onOpen = {
                            viewModel.loadProject(project)
                            onOpenProjectInStudio()
                        },
                        onDelete = { projectToDelete = project }
                    )
                }
            }
        }
    }

    if (projectToDelete != null) {
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text("प्रोजेक्ट हटाएं?", fontWeight = FontWeight.Bold) },
            text = { Text("क्या आप इस न्यूज़ प्रोजेक्ट को हटाना चाहते हैं?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        projectToDelete?.let { viewModel.deleteProject(it) }
                        projectToDelete = null
                    }
                ) {
                    Text("हटाएं", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("रद्द करें")
                }
            }
        )
    }
}

@Composable
private fun SavedProjectItem(
    project: NewsProject,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_project_${project.id}"),
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
                    Box(
                        modifier = Modifier
                            .background(NewsRedPrimary, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = project.category,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = project.aspectPreset,
                            color = NewsGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onOpen, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "संपादित करें",
                            tint = NewsGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "हटाएं",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = project.headline,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 2
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${project.channelName} • ${project.reporterName}",
                    color = NewsAmber,
                    fontSize = 11.sp
                )

                val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                    .format(Date(project.updatedAt))
                Text(
                    text = dateStr,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
    }
}
