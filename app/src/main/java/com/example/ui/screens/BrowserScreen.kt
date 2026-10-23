package com.example.ui.screens

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.data.local.MangaEntity
import com.example.data.local.SourceEntity
import com.example.data.model.MangaSourceConfig
import com.example.ui.components.GlassCard
import com.example.ui.components.MangaZzTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.MangaViewModel
import com.example.ui.viewmodel.SubScreen

data class BrowserTab(
    val id: String,
    val title: String,
    val url: String
)

enum class BrowserViewMode {
    CATALOG, // TachiyomiAZ Native Catalog View
    WEBVIEW  // Direct WebView Browser
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    initialUrl: String = MangaSourceConfig.DEFAULT_SOURCE_URL,
    viewModel: MangaViewModel,
    onBack: () -> Unit
) {
    val colors = LocalMangaZzColors.current
    val context = LocalContext.current
    val allSources by viewModel.allSources.collectAsState()
    val allManga by viewModel.allManga.collectAsState()

    // Determine current source from official sources
    val availableSources = remember(allSources) {
        if (allSources.isNotEmpty()) allSources else MangaSourceConfig.OFFICIAL_SOURCES
    }

    var currentSource by remember(availableSources, initialUrl) {
        mutableStateOf(
            availableSources.firstOrNull { it.url.contains(initialUrl.removePrefix("https://").take(8), ignoreCase = true) }
                ?: availableSources.firstOrNull { it.url == initialUrl }
                ?: availableSources.firstOrNull()
                ?: MangaSourceConfig.OFFICIAL_SOURCES.first()
        )
    }

    var viewMode by remember { mutableStateOf(BrowserViewMode.CATALOG) }
    var showSourcePickerSheet by remember { mutableStateOf(false) }

    // Catalog filtering state (TachiyomiAZ style)
    var catalogCategoryIndex by remember { mutableIntStateOf(0) } // 0: الشائعة, 1: الأحدث, 2: الأعلى تقييماً, 3: المكتملة
    var selectedGenre by remember { mutableStateOf("الكل") }
    var catalogSearchQuery by remember { mutableStateOf("") }

    // WebView state & Error diagnosis
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var currentWebUrl by remember { mutableStateOf(initialUrl) }
    var pageTitle by remember { mutableStateOf(currentSource.name) }
    var isLoading by remember { mutableFloatStateOf(0f) }
    var isPageLoading by remember { mutableStateOf(false) }
    var isDesktopMode by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var errorFailingUrl by remember { mutableStateOf("") }

    // Intercept hardware back button
    BackHandler {
        if (viewMode == BrowserViewMode.WEBVIEW && webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else if (viewMode == BrowserViewMode.WEBVIEW) {
            viewMode = BrowserViewMode.CATALOG
        } else {
            onBack()
        }
    }

    // Filtered manga items for the TachiyomiAZ catalog
    val catalogManga = remember(allManga, catalogCategoryIndex, selectedGenre, catalogSearchQuery, currentSource) {
        var list = when (catalogCategoryIndex) {
            0 -> allManga.sortedByDescending { it.rating } // Popular
            1 -> allManga.sortedByDescending { it.currentChapter } // Latest
            2 -> allManga.sortedByDescending { it.rating } // Top rated
            3 -> allManga.filter { it.status.contains("مكتمل") } // Completed
            else -> allManga
        }

        if (selectedGenre != "الكل") {
            list = list.filter { it.genres.contains(selectedGenre, ignoreCase = true) }
        }

        if (catalogSearchQuery.isNotBlank()) {
            list = list.filter {
                it.title.contains(catalogSearchQuery, ignoreCase = true) ||
                it.titleArabic.contains(catalogSearchQuery, ignoreCase = true) ||
                it.genres.contains(catalogSearchQuery, ignoreCase = true)
            }
        }
        list
    }

    val genres = listOf("الكل", "أكشن", "فانتازيا", "مغامرات", "رعب", "قوى خارقة", "رياضة", "شياطين")

    Scaffold(
        topBar = {
            MangaZzTopBar(
                title = if (viewMode == BrowserViewMode.CATALOG) "كتالوج ${currentSource.name}" else "المتصفح الداخلي",
                subtitle = "عرض مدمج مثل TachiyomiAZ",
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
            // Source Identity Bar with Switcher & Mode Toggle (Image 3 inspired)
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                borderColor = colors.primary.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Source Selector Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.primary.copy(alpha = 0.15f))
                            .border(1.dp, colors.primary, RoundedCornerShape(12.dp))
                            .clickable { showSourcePickerSheet = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentSource.name,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "تبديل المصدر",
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Mode Toggle: Catalog (TachiyomiAZ) vs Live Web
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF060D1A))
                            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                            .padding(2.dp)
                    ) {
                        ModeToggleButton(
                            label = "الكتالوج",
                            icon = Icons.Default.GridView,
                            isSelected = viewMode == BrowserViewMode.CATALOG,
                            onClick = { viewMode = BrowserViewMode.CATALOG }
                        )
                        ModeToggleButton(
                            label = "المتصفح",
                            icon = Icons.Default.Language,
                            isSelected = viewMode == BrowserViewMode.WEBVIEW,
                            onClick = {
                                viewMode = BrowserViewMode.WEBVIEW
                                hasError = false
                                currentWebUrl = currentSource.url
                                webViewInstance?.loadUrl(currentSource.url)
                            }
                        )
                    }
                }
            }

            // Main Content Area: Catalog vs WebView
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (viewMode == BrowserViewMode.CATALOG) {
                    // TACHIYOMI AZ STYLE CATALOG VIEW
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search bar inside source
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = catalogSearchQuery,
                                onValueChange = { catalogSearchQuery = it },
                                placeholder = {
                                    Text(
                                        text = "ابحث في كتالوج ${currentSource.name}...",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "بحث",
                                        tint = colors.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (catalogSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { catalogSearchQuery = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "مسح",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.primary,
                                    unfocusedBorderColor = colors.border,
                                    focusedContainerColor = colors.cardGlass,
                                    unfocusedContainerColor = colors.cardGlass,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            )
                        }

                        // Category Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CatalogTabChip(
                                title = "الشائعة",
                                icon = Icons.Default.Whatshot,
                                isSelected = catalogCategoryIndex == 0,
                                onClick = { catalogCategoryIndex = 0 },
                                modifier = Modifier.weight(1f)
                            )
                            CatalogTabChip(
                                title = "الأحدث",
                                icon = Icons.Default.Update,
                                isSelected = catalogCategoryIndex == 1,
                                onClick = { catalogCategoryIndex = 1 },
                                modifier = Modifier.weight(1f)
                            )
                            CatalogTabChip(
                                title = "الأعلى تقييماً",
                                icon = Icons.Default.Star,
                                isSelected = catalogCategoryIndex == 2,
                                onClick = { catalogCategoryIndex = 2 },
                                modifier = Modifier.weight(1.1f)
                            )
                            CatalogTabChip(
                                title = "المكتملة",
                                icon = Icons.Default.CheckCircle,
                                isSelected = catalogCategoryIndex == 3,
                                onClick = { catalogCategoryIndex = 3 },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Genre Filter Row
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(genres) { genre ->
                                val isSelected = selectedGenre == genre
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) colors.primary else Color(0xFF091427))
                                        .border(
                                            1.dp,
                                            if (isSelected) colors.primary else colors.border,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedGenre = genre }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = genre,
                                        color = if (isSelected) Color.Black else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        // 2-Column Catalog Manga Grid
                        if (catalogManga.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.SearchOff,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "لا توجد نتائج تطابق بحثك في ${currentSource.name}",
                                        color = TextSecondary,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(catalogManga, key = { it.id }) { manga ->
                                    TachiyomiCatalogCard(
                                        manga = manga,
                                        sourceName = currentSource.name,
                                        onClick = {
                                            viewModel.navigateTo(SubScreen.MangaDetail(manga.id))
                                        },
                                        onFavoriteClick = {
                                            viewModel.toggleFavorite(manga.id, manga.isFavorite)
                                        },
                                        onReadClick = {
                                            viewModel.navigateTo(
                                                SubScreen.ReaderFull(
                                                    mangaId = manga.id,
                                                    chapterNumber = manga.currentChapter,
                                                    isOffline = true
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // DIRECT IN-APP WEBVIEW WITH FULL ERROR HANDLING
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Web Navigation Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { webViewInstance?.goBack() },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "رجوع",
                                    tint = if (webViewInstance?.canGoBack() == true) colors.primary else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { webViewInstance?.goForward() },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "تقدم",
                                    tint = if (webViewInstance?.canGoForward() == true) colors.primary else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    hasError = false
                                    webViewInstance?.reload()
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "تحديث",
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Address Bar with SSL Lock Icon
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF091222))
                                    .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (hasError) Icons.Default.Warning else Icons.Default.Lock,
                                        contentDescription = "حالة الأمان",
                                        tint = if (hasError) Color(0xFFFF5252) else Color(0xFF00E676),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = currentWebUrl,
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Open in external browser icon
                            IconButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentWebUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        viewModel.postNotification("تعذر فتح الرابط خارجياً")
                                    }
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInBrowser,
                                    contentDescription = "فتح خارجياً",
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Progress Indicator
                        if (isPageLoading) {
                            LinearProgressIndicator(
                                progress = { isLoading },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.5.dp),
                                color = colors.primary,
                                trackColor = Color.Transparent
                            )
                        }

                        // WebView Container or Custom Error UI
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        val webViewInstanceRef = this
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )

                                        CookieManager.getInstance().apply {
                                            setAcceptCookie(true)
                                            setAcceptThirdPartyCookies(webViewInstanceRef, true)
                                        }

                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        settings.databaseEnabled = true
                                        settings.useWideViewPort = true
                                        settings.loadWithOverviewMode = true
                                        settings.setSupportZoom(true)
                                        settings.builtInZoomControls = true
                                        settings.displayZoomControls = false
                                        settings.cacheMode = WebSettings.LOAD_DEFAULT
                                        settings.userAgentString =
                                            "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

                                        webViewClient = object : WebViewClient() {
                                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                                super.onPageStarted(view, url, favicon)
                                                isPageLoading = true
                                                hasError = false
                                                url?.let { currentWebUrl = it }
                                            }

                                            override fun onPageFinished(view: WebView?, url: String?) {
                                                super.onPageFinished(view, url)
                                                isPageLoading = false
                                                view?.title?.let { pageTitle = it }
                                            }

                                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                                val uri = request?.url ?: return false
                                                val scheme = uri.scheme ?: ""
                                                if (scheme == "http" || scheme == "https") {
                                                    return false // Load in WebView
                                                }
                                                return try {
                                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                                    context.startActivity(intent)
                                                    true
                                                } catch (e: Exception) {
                                                    true
                                                }
                                            }

                                            @Deprecated("Deprecated in Java")
                                            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                                                super.onReceivedError(view, errorCode, description, failingUrl)
                                                hasError = true
                                                errorFailingUrl = failingUrl ?: currentWebUrl
                                                errorMessage = when (errorCode) {
                                                    ERROR_HOST_LOOKUP -> "تعذر حل اسم النطاق DNS (ERR_NAME_NOT_RESOLVED)\nتحقق من اتصال الإنترنت أو صحة عنوان الموقع."
                                                    ERROR_CONNECT -> "فشل الاتصال بخادم الموقع (ERR_CONNECTION_REFUSED)"
                                                    ERROR_TIMEOUT -> "انتهت مهلة انتظار خادم الموقع (ERR_TIMED_OUT)"
                                                    ERROR_FAILED_SSL_HANDSHAKE -> "فشل تأمين الاتصال المشفر (SSL Handshake Failed)"
                                                    else -> description ?: "حدث خطأ في تحميل الصفحة ($errorCode)"
                                                }
                                            }

                                            @TargetApi(Build.VERSION_CODES.M)
                                            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                                super.onReceivedError(view, request, error)
                                                if (request?.isForMainFrame == true) {
                                                    hasError = true
                                                    errorFailingUrl = request.url?.toString() ?: currentWebUrl
                                                    val desc = error?.description?.toString() ?: ""
                                                    errorMessage = if (desc.contains("NAME_NOT_RESOLVED", ignoreCase = true)) {
                                                        "تعذر حل اسم النطاق DNS (ERR_NAME_NOT_RESOLVED)\nتحقق من اتصال الإنترنت أو صحة عنوان الموقع."
                                                    } else {
                                                        "تعذر تحميل الموقع: $desc"
                                                    }
                                                }
                                            }
                                        }

                                        webChromeClient = object : WebChromeClient() {
                                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                                super.onProgressChanged(view, newProgress)
                                                isLoading = newProgress / 100f
                                                if (newProgress >= 100) isPageLoading = false
                                            }
                                        }

                                        loadUrl(currentWebUrl)
                                        webViewInstance = this
                                    }
                                },
                                update = { view ->
                                    if (isDesktopMode) {
                                        view.settings.userAgentString =
                                            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
                                    } else {
                                        view.settings.userAgentString =
                                            "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                                    }
                                }
                            )

                            // Overlay Native Error View when connection fails
                            if (hasError) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xF5040712))
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    GlassCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        borderColor = Color(0xFFFF5252).copy(alpha = 0.6f)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0x33FF5252)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CloudOff,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFF5252),
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(14.dp))

                                            Text(
                                                text = "تعذر الوصول إلى الموقع",
                                                color = TextPrimary,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                text = errorMessage,
                                                color = TextSecondary,
                                                fontSize = 12.sp,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 18.sp
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text(
                                                text = errorFailingUrl,
                                                color = colors.primary,
                                                fontSize = 10.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Spacer(modifier = Modifier.height(18.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        hasError = false
                                                        webViewInstance?.loadUrl(currentSource.url)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = colors.primary,
                                                        contentColor = Color.Black
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("إعادة المحاولة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }

                                                OutlinedButton(
                                                    onClick = { viewMode = BrowserViewMode.CATALOG },
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.weight(1f),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
                                                ) {
                                                    Icon(Icons.Default.GridView, contentDescription = null, modifier = Modifier.size(16.dp), tint = colors.primary)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("الكتالوج", color = TextPrimary, fontSize = 11.sp)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            TextButton(
                                                onClick = { showSourcePickerSheet = true }
                                            ) {
                                                Text("اختيار مصدر آخر من المصادر الثمانية ◄", color = colors.primary, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Browser Toolbar
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
                    label = "ترجمة فورية",
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
                    icon = if (viewMode == BrowserViewMode.CATALOG) Icons.Default.Language else Icons.Default.GridView,
                    label = if (viewMode == BrowserViewMode.CATALOG) "المتصفح" else "الكتالوج",
                    isActive = true,
                    onClick = {
                        viewMode = if (viewMode == BrowserViewMode.CATALOG) {
                            BrowserViewMode.WEBVIEW
                        } else {
                            BrowserViewMode.CATALOG
                        }
                    }
                )

                BrowserBottomAction(
                    icon = Icons.Default.SyncAlt,
                    label = "تبديل المصدر",
                    onClick = {
                        showSourcePickerSheet = true
                    }
                )
            }
        }
    }

    // Source Selection Bottom Sheet - Displays ONLY the 8 Official Sources
    if (showSourcePickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSourcePickerSheet = false },
            containerColor = Color(0xFF070F1E),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(TextSecondary.copy(alpha = 0.5f))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "المصادر المعتمدة (${availableSources.size})",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "اختر من المواقع الثمانية الرسمية لتصفح الكتالوج",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = { showSourcePickerSheet = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                availableSources.forEach { src ->
                    val isSelected = src.id == currentSource.id
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        borderColor = if (isSelected) colors.primary else colors.border,
                        onClick = {
                            currentSource = src
                            currentWebUrl = src.url
                            pageTitle = src.name
                            hasError = false
                            webViewInstance?.loadUrl(src.url)
                            showSourcePickerSheet = false
                            viewModel.postNotification("تم التبديل إلى مصدر: ${src.name} ⚡")
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) colors.primary else colors.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Explore,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.Black else colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = src.name,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = src.url,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "محدد",
                                    tint = colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ModeToggleButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalMangaZzColors.current

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) colors.primary else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color.Black else TextSecondary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = if (isSelected) Color.Black else TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun CatalogTabChip(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMangaZzColors.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) colors.primary.copy(alpha = 0.2f) else Color(0xFF091427))
            .border(
                1.dp,
                if (isSelected) colors.primary else colors.border,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) colors.primary else TextSecondary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                color = if (isSelected) colors.primary else TextSecondary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun TachiyomiCatalogCard(
    manga: MangaEntity,
    sourceName: String,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onReadClick: () -> Unit
) {
    val colors = LocalMangaZzColors.current

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        borderColor = if (manga.isFavorite) colors.primary.copy(alpha = 0.7f) else colors.border,
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Manga Poster with Overlay Badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                Image(
                    painter = painterResource(id = manga.coverDrawable),
                    contentDescription = manga.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient shadow overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0x99050C18),
                                    Color(0xF0050C18)
                                )
                            )
                        )
                )

                // Top Chapter Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xD9060E1D))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "فصل ${manga.currentChapter}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Top Favorite Toggle
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xD9060E1D))
                        .clickable { onFavoriteClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (manga.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "مفضلة",
                        tint = if (manga.isFavorite) Color(0xFFFF5252) else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Bottom Source Tag & Rating
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.primary.copy(alpha = 0.25f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = sourceName,
                            color = colors.primary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${manga.rating}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Manga Info & Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = manga.titleArabic,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = manga.title,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = manga.genres.split(",").firstOrNull()?.trim() ?: "مانجا",
                        color = colors.secondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "قراءة ◄",
                        color = colors.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onReadClick() }
                    )
                }
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
