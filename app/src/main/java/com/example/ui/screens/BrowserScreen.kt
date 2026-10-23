package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.components.GlassCard
import com.example.ui.components.MangaZzTopBar
import com.example.ui.theme.LocalMangaZzColors
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MangaViewModel
import com.example.ui.viewmodel.SubScreen

data class BrowserTab(
    val id: String,
    val title: String,
    val url: String
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    initialUrl: String = "https://mangadex.org",
    viewModel: MangaViewModel,
    onBack: () -> Unit
) {
    val colors = LocalMangaZzColors.current

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var currentUrl by remember { mutableStateOf(initialUrl) }
    var pageTitle by remember { mutableStateOf("MangaDex") }
    var isLoading by remember { mutableStateOf(true) }
    var loadingProgress by remember { mutableFloatStateOf(0f) }
    var isDesktopMode by remember { mutableStateOf(false) }

    val tabs = remember {
        mutableStateListOf(
            BrowserTab(id = "tab_1", title = "MangaDex", url = initialUrl)
        )
    }
    var activeTabIndex by remember { mutableIntStateOf(0) }

    // Intercept hardware back button
    BackHandler {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            MangaZzTopBar(
                title = "المتصفح الداخلي",
                subtitle = "داخل التطبيق",
                showBackButton = true,
                onBackClick = onBack,
                onProClick = { viewModel.navigateTo(SubScreen.Backup) }
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs Row (Tab chips + New Tab + count)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.primary.copy(alpha = 0.2f))
                        .border(1.dp, colors.primary, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pageTitle.take(14),
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق التبويب",
                        tint = TextSecondary,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable {
                                if (tabs.size > 1) {
                                    tabs.removeAt(activeTabIndex)
                                    activeTabIndex = 0
                                }
                            }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // + New Tab
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.cardGlass)
                        .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                        .clickable {
                            tabs.add(BrowserTab("tab_${System.currentTimeMillis()}", "MangaPlus", "https://mangaplus.shueisha.co.jp"))
                            activeTabIndex = tabs.lastIndex
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "تبويب جديد",
                        tint = colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Tab count badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.cardGlass)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${tabs.size} تبويب",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Navigation Bar (Back, Forward, Refresh, Address Bar, Bookmark)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { webViewInstance?.goBack() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع للخلف",
                        tint = if (webViewInstance?.canGoBack() == true) colors.primary else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { webViewInstance?.goForward() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "تقدم للأمام",
                        tint = if (webViewInstance?.canGoForward() == true) colors.primary else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { webViewInstance?.reload() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "إعادة التحميل",
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Address Bar with SSL Lock Icon
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF091222))
                        .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "آمن",
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentUrl,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        viewModel.toggleFavorite("solo_leveling", false)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.StarBorder,
                        contentDescription = "حفظ للمفضلة",
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Loading Progress Bar
            if (isLoading) {
                LinearProgressIndicator(
                    progress = { loadingProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = colors.primary,
                    trackColor = Color.Transparent
                )
            }

            // Embedded Android WebView
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        WebView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.databaseEnabled = true
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true
                            settings.setSupportZoom(true)
                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    isLoading = true
                                    url?.let { currentUrl = it }
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                    view?.title?.let { pageTitle = it }
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    super.onProgressChanged(view, newProgress)
                                    loadingProgress = newProgress / 100f
                                    if (newProgress >= 100) isLoading = false
                                }
                            }

                            loadUrl(initialUrl)
                            webViewInstance = this
                        }
                    },
                    update = { view ->
                        if (isDesktopMode) {
                            view.settings.userAgentString =
                                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                        } else {
                            view.settings.userAgentString = null
                        }
                    }
                )
            }

            // Floating Source Bar (Image 3)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Source Selector Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF091427))
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                        .clickable { viewModel.setTab(com.example.ui.components.NavTab.SOURCES) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "المصدر: ",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = pageTitle.take(12),
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Quick Floating Actions (List, Bookmark, Fullscreen)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF091427))
                            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                            .clickable { viewModel.setTab(com.example.ui.components.NavTab.LIBRARY) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatListBulleted,
                            contentDescription = "قائمة",
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF091427))
                            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleFavorite("solo_leveling", false) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = "حفظ",
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF091427))
                            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "ملء الشاشة",
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Bottom Browser Toolbar (القارئ, ترجمة, حفظ للمكتبة, وضع سطح المكتب, تبديل المصدر)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xF2080D18))
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BrowserBottomAction(
                    icon = Icons.Default.MenuBook,
                    label = "القارئ",
                    isPrimary = true,
                    onClick = {
                        viewModel.navigateTo(
                            SubScreen.ReaderFull(
                                mangaId = "solo_leveling",
                                chapterNumber = 179,
                                isOffline = true
                            )
                        )
                    }
                )

                BrowserBottomAction(
                    icon = Icons.Default.Translate,
                    label = "ترجمة",
                    onClick = {
                        viewModel.navigateTo(
                            SubScreen.ReaderFull(
                                mangaId = "solo_leveling",
                                chapterNumber = 179,
                                isOffline = true
                            )
                        )
                    }
                )

                BrowserBottomAction(
                    icon = Icons.Default.BookmarkBorder,
                    label = "حفظ للمكتبة",
                    onClick = {
                        viewModel.toggleFavorite("solo_leveling", false)
                    }
                )

                BrowserBottomAction(
                    icon = Icons.Default.Laptop,
                    label = "وضع سطح المكتب",
                    isActive = isDesktopMode,
                    onClick = {
                        isDesktopMode = !isDesktopMode
                        webViewInstance?.reload()
                    }
                )

                BrowserBottomAction(
                    icon = Icons.Default.SyncAlt,
                    label = "تبديل المصدر",
                    onClick = {
                        viewModel.setTab(com.example.ui.components.NavTab.SOURCES)
                    }
                )
            }
        }
    }
}

@Composable
fun BrowserBottomAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isPrimary: Boolean = false,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    val colors = LocalMangaZzColors.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isPrimary || isActive) colors.primary else TextPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isPrimary || isActive) colors.primary else TextSecondary,
            fontSize = 9.sp,
            fontWeight = if (isPrimary || isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}
