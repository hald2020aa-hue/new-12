package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.MangaZzBottomBar
import com.example.ui.components.NavTab
import com.example.ui.screens.*
import com.example.ui.theme.BgDark
import com.example.ui.theme.MangaZzTheme
import com.example.ui.viewmodel.MangaViewModel
import com.example.ui.viewmodel.SubScreen
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MangaViewModel = viewModel()
            val selectedTheme by viewModel.selectedTheme.collectAsState()
            val currentTab by viewModel.currentTab.collectAsState()
            val subScreen by viewModel.currentSubScreen.collectAsState()

            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(Unit) {
                viewModel.notificationMessage.collectLatest { msg ->
                    snackbarHostState.showSnackbar(msg)
                }
            }

            // Handle system back button for sub-screens
            BackHandler(enabled = subScreen !is SubScreen.Main) {
                viewModel.navigateBack()
            }

            MangaZzTheme(selectedTheme = selectedTheme) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgDark)
                ) {
                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        bottomBar = {
                            // Only show bottom navigation on main tabs, not during full reader or browser
                            val showBottomNav = subScreen is SubScreen.Main
                            AnimatedVisibility(
                                visible = showBottomNav,
                                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                            ) {
                                MangaZzBottomBar(
                                    currentTab = currentTab,
                                    onTabSelected = { tab ->
                                        viewModel.setTab(tab)
                                    }
                                )
                            }
                        },
                        containerColor = Color.Transparent,
                        contentWindowInsets = WindowInsets(0, 0, 0, 0)
                    ) { _ ->
                        AnimatedContent(
                            targetState = subScreen,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                            },
                            label = "subScreenTransition"
                        ) { targetScreen ->
                            when (targetScreen) {
                                is SubScreen.Main -> {
                                    when (currentTab) {
                                        NavTab.HOME -> HomeScreen(viewModel = viewModel)
                                        NavTab.SOURCES -> SourcesScreen(viewModel = viewModel)
                                        NavTab.LIBRARY -> LibraryScreen(viewModel = viewModel)
                                        NavTab.READER -> ReaderScreen(
                                            mangaId = "solo_leveling",
                                            chapterNumber = 179,
                                            isOffline = true,
                                            viewModel = viewModel,
                                            onBack = { viewModel.setTab(NavTab.HOME) }
                                        )
                                        NavTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                                    }
                                }
                                is SubScreen.MangaDetail -> {
                                    MangaDetailScreen(
                                        mangaId = targetScreen.mangaId,
                                        viewModel = viewModel,
                                        onBack = { viewModel.navigateBack() }
                                    )
                                }
                                is SubScreen.Downloads -> {
                                    DownloadsScreen(viewModel = viewModel)
                                }
                                is SubScreen.Browser -> {
                                    BrowserScreen(
                                        initialUrl = targetScreen.url,
                                        viewModel = viewModel,
                                        onBack = { viewModel.navigateBack() }
                                    )
                                }
                                is SubScreen.Backup -> {
                                    BackupScreen(
                                        viewModel = viewModel,
                                        onBack = { viewModel.navigateBack() }
                                    )
                                }
                                is SubScreen.ReaderFull -> {
                                    ReaderScreen(
                                        mangaId = targetScreen.mangaId,
                                        chapterNumber = targetScreen.chapterNumber,
                                        isOffline = targetScreen.isOffline,
                                        viewModel = viewModel,
                                        onBack = { viewModel.navigateBack() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
