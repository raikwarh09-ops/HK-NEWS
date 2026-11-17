package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ChannelSettingsScreen
import com.example.ui.screens.SavedProjectsScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.screens.TemplatesScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NewsAmber
import com.example.ui.theme.NewsDeepNavy
import com.example.ui.theme.NewsGold
import com.example.ui.theme.NewsRedDark
import com.example.ui.theme.NewsRedPrimary
import com.example.viewmodel.NewsStudioViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                NewsStudioApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsStudioApp(
    viewModel: NewsStudioViewModel = viewModel()
) {
    var selectedScreenIndex by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val userMessage by viewModel.userMessage.collectAsState()

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    // BackHandler to always navigate back to Studio (screen 0) if user presses system back
    if (selectedScreenIndex != 0) {
        BackHandler {
            selectedScreenIndex = 0
        }
    }

    val currentProject by viewModel.currentProject.collectAsState()

    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Channel / App Emblem
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, NewsGold, CircleShape)
                                .background(Color(0xFF0D47A1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.hk_news_logo),
                                contentDescription = "हर खबर H.K NEWS लोगो",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = "HK NEWS",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .background(NewsRedPrimary, RoundedCornerShape(3.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "PRO STUDIO",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp
                                    )
                                }
                            }
                            Text(
                                text = "हर खबर • काइनमास्टर वीडियो एडिटर",
                                color = NewsGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    if (selectedScreenIndex == 0) {
                        Row(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFE53935), Color(0xFFB71C1C))
                                    )
                                )
                                .border(1.dp, NewsGold.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.saveToGallery(context, asVideo = true)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("top_kinemaster_export"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.IosShare,
                                contentDescription = "वीडियो एक्सपोर्ट",
                                tint = NewsGold,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "एक्सपोर्ट (${currentProject.exportResolution})",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF090D16)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                // Tab 0: Studio
                NavigationBarItem(
                    selected = selectedScreenIndex == 0,
                    onClick = { selectedScreenIndex = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "स्टूडियो"
                        )
                    },
                    label = {
                        Text(
                            text = "स्टूडियो",
                            fontSize = 11.sp,
                            fontWeight = if (selectedScreenIndex == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = NewsGold,
                        indicatorColor = NewsRedPrimary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_studio")
                )

                // Tab 1: Templates
                NavigationBarItem(
                    selected = selectedScreenIndex == 1,
                    onClick = { selectedScreenIndex = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "टेम्पलेट्स"
                        )
                    },
                    label = {
                        Text(
                            text = "टेम्पलेट्स",
                            fontSize = 11.sp,
                            fontWeight = if (selectedScreenIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = NewsGold,
                        indicatorColor = NewsRedPrimary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_templates")
                )

                // Tab 2: Saved Projects
                NavigationBarItem(
                    selected = selectedScreenIndex == 2,
                    onClick = { selectedScreenIndex = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "सहेजे गए"
                        )
                    },
                    label = {
                        Text(
                            text = "प्रोजेक्ट्स",
                            fontSize = 11.sp,
                            fontWeight = if (selectedScreenIndex == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = NewsGold,
                        indicatorColor = NewsRedPrimary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_saved_projects")
                )

                // Tab 3: Channel Profile & Settings
                NavigationBarItem(
                    selected = selectedScreenIndex == 3,
                    onClick = { selectedScreenIndex = 3 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "सेटिंग्स"
                        )
                    },
                    label = {
                        Text(
                            text = "सेटिंग्स",
                            fontSize = 11.sp,
                            fontWeight = if (selectedScreenIndex == 3) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = NewsGold,
                        indicatorColor = NewsRedPrimary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(NewsDeepNavy)
        ) {
            when (selectedScreenIndex) {
                0 -> StudioScreen(viewModel = viewModel)
                1 -> TemplatesScreen(
                    viewModel = viewModel,
                    onNavigateToStudio = { selectedScreenIndex = 0 }
                )
                2 -> SavedProjectsScreen(
                    viewModel = viewModel,
                    onOpenProjectInStudio = { selectedScreenIndex = 0 }
                )
                3 -> ChannelSettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { selectedScreenIndex = 0 }
                )
            }
        }
    }
}
