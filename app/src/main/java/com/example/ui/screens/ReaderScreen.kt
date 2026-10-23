package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ReadingDirection
import com.example.data.model.SpeechBubbleTranslation
import com.example.ui.components.GlassCard
import com.example.ui.theme.LocalMangaZzColors
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MangaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    mangaId: String = "solo_leveling",
    chapterNumber: Int = 179,
    isOffline: Boolean = false,
    viewModel: MangaViewModel,
    onBack: () -> Unit
) {
    val colors = LocalMangaZzColors.current

    var showControls by remember { mutableStateOf(true) }
    var showTranslationSheet by remember { mutableStateOf(false) }
    var showBrightnessDialog by remember { mutableStateOf(false) }

    val readingDirection by viewModel.readingDirection.collectAsState()
    val ocrEnabled by viewModel.ocrEnabled.collectAsState()
    val arabicTranslationEnabled by viewModel.arabicTranslationEnabled.collectAsState()
    val showOriginalText by viewModel.showOriginalText.collectAsState()
    val dualDisplayEnabled by viewModel.dualDisplayEnabled.collectAsState()
    val replaceBubbleMode by viewModel.replaceBubbleMode.collectAsState()
    val fontSizePercent by viewModel.fontSizePercent.collectAsState()
    val backgroundOpacity by viewModel.backgroundOpacity.collectAsState()

    var currentPage by remember { mutableIntStateOf(12) }
    val totalPages = 28

    // Speech bubbles with translated Arabic text and Japanese original
    val speechBubbles = remember {
        listOf(
            SpeechBubbleTranslation(
                id = "bubble_1",
                originalText = "俺は今までよりもずっと強くなる。",
                arabicTranslation = "سأصبح أقوى من أي وقت مضى.",
                xPercent = 0.08f,
                yPercent = 0.12f,
                widthPercent = 0.42f,
                heightPercent = 0.16f
            ),
            SpeechBubbleTranslation(
                id = "bubble_2",
                originalText = "それだけの力だと思っているのか？",
                arabicTranslation = "هل تعتقد أن هذا كل ما لديك؟",
                xPercent = 0.60f,
                yPercent = 0.38f,
                widthPercent = 0.36f,
                heightPercent = 0.15f
            ),
            SpeechBubbleTranslation(
                id = "bubble_3",
                originalText = "いいや... これからだ。",
                arabicTranslation = "لا... هذه مجرد البداية فقط.",
                xPercent = 0.08f,
                yPercent = 0.60f,
                widthPercent = 0.40f,
                heightPercent = 0.16f
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF04060A))
    ) {
        // Main Manga Canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { showControls = !showControls }
        ) {
            if (readingDirection == ReadingDirection.VERTICAL) {
                // Webtoon continuous scroll
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = if (showControls) 70.dp else 0.dp, bottom = if (showControls) 100.dp else 0.dp)
                ) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Image(
                                painter = painterResource(id = R.drawable.reader_page_sample),
                                contentDescription = "صفحة مانجا",
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Overlay speech bubbles if Arabic Translation is ON
                            if (arabicTranslationEnabled) {
                                speechBubbles.forEach { bubble ->
                                    SpeechBubbleOverlay(
                                        bubble = bubble,
                                        showOriginal = showOriginalText,
                                        dualDisplay = dualDisplayEnabled,
                                        replaceMode = replaceBubbleMode,
                                        fontSizeMultiplier = fontSizePercent / 100f,
                                        bgOpacity = backgroundOpacity
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Horizontal pager for traditional manga
                val pagerState = rememberPagerState(initialPage = 11, pageCount = { totalPages })
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = if (page % 2 == 0) R.drawable.reader_page_1 else R.drawable.reader_page_2),
                            contentDescription = "صفحة $page",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // Top Reader Header Controls
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xF2070A11),
                                Color(0xCC070A11),
                                Color.Transparent
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0x990E182A))
                                .border(1.dp, colors.border, CircleShape)
                                .clickable { onBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "الفصل $chapterNumber",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isOffline) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "دون إنترنت (محلي)",
                                        color = colors.primary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "Solo Leveling",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showBrightnessDialog = !showBrightnessDialog }) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "سطوع",
                                tint = TextPrimary
                            )
                        }
                        IconButton(onClick = {
                            viewModel.toggleFavorite("solo_leveling", false)
                        }) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = "حفظ",
                                tint = TextPrimary
                            )
                        }
                        IconButton(onClick = { showTranslationSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "إعدادات الترجمة",
                                tint = colors.primary
                            )
                        }
                    }
                }
            }
        }

        // Floating Quick Tools (Left / Right)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 100.dp)
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xCC0E182A))
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Brightness6,
                    label = "السطوع",
                    onClick = { showBrightnessDialog = true }
                )
                QuickActionButton(
                    icon = Icons.Default.Translate,
                    label = "ترجمة",
                    isActive = arabicTranslationEnabled,
                    onClick = { viewModel.toggleArabicTranslation() }
                )
                QuickActionButton(
                    icon = Icons.Default.FilterCenterFocus,
                    label = "وضع التركيز",
                    onClick = { showControls = false }
                )
            }
        }

        // Floating Bottom-Right Translation Action (Image 8)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 100.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(colors.primary)
                    .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    .clickable { showTranslationSheet = true },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "ترجمة",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "ترجمة",
                        color = Color.Black,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Bottom Navigation Bar
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xCC070A11),
                                Color(0xF2070A11)
                            )
                        )
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.clickable {
                                    if (currentPage > 1) currentPage--
                                },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "الفصل السابق",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Text(
                                text = "$currentPage / $totalPages",
                                color = colors.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier.clickable {
                                    if (currentPage < totalPages) currentPage++
                                },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "الفصل التالي",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Slider(
                            value = currentPage.toFloat(),
                            onValueChange = { currentPage = it.toInt() },
                            valueRange = 1f..totalPages.toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = colors.primary,
                                activeTrackColor = colors.primary,
                                inactiveTrackColor = colors.primary.copy(alpha = 0.25f)
                            )
                        )
                    }
                }
            }
        }

        // Translation & OCR Bottom Sheet (Image 5)
        if (showTranslationSheet) {
            ModalBottomSheet(
                onDismissRequest = { showTranslationSheet = false },
                containerColor = Color(0xF2080D18),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "إعدادات التعرف والترجمة (OCR)",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 4 Action Feature Cards (Image 5)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FeatureToggleCard(
                            title = "OCR",
                            subtitle = "التعرف على النص",
                            icon = Icons.Default.AutoAwesome,
                            isActive = ocrEnabled,
                            onToggle = { viewModel.toggleOcr() },
                            modifier = Modifier.weight(1f)
                        )
                        FeatureToggleCard(
                            title = "ترجمة للعربية",
                            subtitle = "ترجمة تلقائية فورية",
                            icon = Icons.Default.Translate,
                            isActive = arabicTranslationEnabled,
                            onToggle = { viewModel.toggleArabicTranslation() },
                            modifier = Modifier.weight(1.2f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FeatureToggleCard(
                            title = "إظهار النص الأصلي",
                            subtitle = "اليابانية (داعم)",
                            icon = Icons.Default.GTranslate,
                            isActive = showOriginalText,
                            onToggle = { viewModel.toggleShowOriginalText() },
                            modifier = Modifier.weight(1f)
                        )
                        FeatureToggleCard(
                            title = "عرض مزدوج",
                            subtitle = "عربي + أصلي",
                            icon = Icons.Default.ViewAgenda,
                            isActive = dualDisplayEnabled,
                            onToggle = { viewModel.toggleDualDisplay() },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mode Selection: استبدال الفقاعة vs إظهار فوق الفقاعة
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.setReplaceBubbleMode(true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (replaceBubbleMode) colors.primary else Color(0x3300D2FF),
                                contentColor = if (replaceBubbleMode) Color.Black else TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "استبدال الفقاعة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.setReplaceBubbleMode(false) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!replaceBubbleMode) colors.primary else Color(0x3300D2FF),
                                contentColor = if (!replaceBubbleMode) Color.Black else TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "إظهار فوق الفقاعة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Font Size Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "حجم الخط", color = TextPrimary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "AA", color = TextSecondary, fontSize = 10.sp)
                        Slider(
                            value = fontSizePercent.toFloat(),
                            onValueChange = { viewModel.setFontSizePercent(it.toInt()) },
                            valueRange = 70f..150f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = colors.primary, activeTrackColor = colors.primary)
                        )
                        Text(text = "A", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "$fontSizePercent%", color = colors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Background Opacity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "شفافية الخلفية", color = TextPrimary, fontSize = 12.sp)
                        Slider(
                            value = backgroundOpacity,
                            onValueChange = { viewModel.setBackgroundOpacity(it) },
                            valueRange = 0.2f..1.0f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = colors.primary, activeTrackColor = colors.primary)
                        )
                        Text(text = "${(backgroundOpacity * 100).toInt()}%", color = colors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    val colors = LocalMangaZzColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) colors.primary else TextPrimary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            color = if (isActive) colors.primary else TextSecondary,
            fontSize = 9.sp
        )
    }
}

@Composable
fun FeatureToggleCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMangaZzColors.current

    GlassCard(
        modifier = modifier.clickable { onToggle() },
        shape = RoundedCornerShape(16.dp),
        borderColor = if (isActive) colors.primary else colors.border
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isActive) colors.primary else TextSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Switch(
                checked = isActive,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = colors.primary,
                    uncheckedTrackColor = Color(0x33FFFFFF)
                ),
                modifier = Modifier.scale(0.75f)
            )
        }
    }
}

@Composable
fun SpeechBubbleOverlay(
    bubble: SpeechBubbleTranslation,
    showOriginal: Boolean,
    dualDisplay: Boolean,
    replaceMode: Boolean,
    fontSizeMultiplier: Float,
    bgOpacity: Float
) {
    val colors = LocalMangaZzColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = (bubble.yPercent * 800).dp, start = (bubble.xPercent * 340).dp)
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 140.dp, max = 220.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = bgOpacity))
                .border(2.dp, colors.primary, RoundedCornerShape(18.dp))
                .padding(10.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = bubble.arabicTranslation,
                        color = Color.Black,
                        fontSize = (13 * fontSizeMultiplier).sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = (18 * fontSizeMultiplier).sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.primary)
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل الترجمة",
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                if (dualDisplay || showOriginal) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xEE0D1525))
                            .padding(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "النص الأصلي:",
                                    color = TextSecondary,
                                    fontSize = 8.sp
                                )
                                Text(
                                    text = bubble.originalText,
                                    color = colors.primary,
                                    fontSize = (9 * fontSizeMultiplier).sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "نسخ",
                                tint = TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.scale(scale: Float): Modifier = this.graphicsLayer(scaleX = scale, scaleY = scale)
