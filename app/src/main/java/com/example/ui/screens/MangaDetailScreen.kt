package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.ChapterEntity
import com.example.data.local.MangaEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.LocalMangaZzColors
import com.example.ui.theme.ProGold
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MangaViewModel
import com.example.ui.viewmodel.SubScreen

@Composable
fun MangaDetailScreen(
    mangaId: String,
    viewModel: MangaViewModel,
    onBack: () -> Unit
) {
    val colors = LocalMangaZzColors.current
    val mangaList by viewModel.allManga.collectAsState()
    val manga = mangaList.firstOrNull { it.id == mangaId } ?: mangaList.firstOrNull()

    val chaptersFlow = remember(mangaId) { viewModel.repository.getChapters(mangaId) }
    val chapters by chaptersFlow.collectAsState(initial = emptyList())

    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var chapterSearchQuery by remember { mutableStateOf("") }
    var isDescendingSort by remember { mutableStateOf(true) }

    if (manga == null) return

    val filteredChapters = remember(chapters, chapterSearchQuery, isDescendingSort) {
        var list = chapters.filter {
            chapterSearchQuery.isBlank() ||
            it.title.contains(chapterSearchQuery, ignoreCase = true) ||
            it.chapterNumber.toString().contains(chapterSearchQuery)
        }
        if (!isDescendingSort) list = list.reversed()
        list
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF040710))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Hero Backdrop Image with Gradient
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.hero_solo_leveling),
                        contentDescription = manga.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Dark gradient to smoothly merge into dark canvas
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0x80040710),
                                        Color(0xCC040710),
                                        Color(0xFF040710)
                                    )
                                )
                            )
                    )

                    // Top Bar (Back, Share, Bookmark, More)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0x990A1120))
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

                        val context = androidx.compose.ui.platform.LocalContext.current
                        var showMoreOptions by remember { mutableStateOf(false) }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x990A1120))
                                    .border(1.dp, colors.border, CircleShape)
                                    .clickable {
                                        val sendIntent = android.content.Intent().apply {
                                            action = android.content.Intent.ACTION_SEND
                                            putExtra(android.content.Intent.EXTRA_TEXT, "اقرأ مانجا ${manga.titleArabic} (${manga.title}) على تطبيق Manga V!")
                                            type = "text/plain"
                                        }
                                        val shareIntent = android.content.Intent.createChooser(sendIntent, "مشاركة المانجا")
                                        context.startActivity(shareIntent)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "مشاركة",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x990A1120))
                                    .border(1.dp, colors.border, CircleShape)
                                    .clickable { showMoreOptions = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "خيارات",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )

                                DropdownMenu(
                                    expanded = showMoreOptions,
                                    onDismissRequest = { showMoreOptions = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("فتح في المتصفح المدمج") },
                                        onClick = {
                                            showMoreOptions = false
                                            viewModel.navigateTo(SubScreen.Browser())
                                        },
                                        leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("تنزيل كل الفصول") },
                                        onClick = {
                                            showMoreOptions = false
                                            chapters.forEach { ch ->
                                                viewModel.startDownload(manga, ch.chapterNumber, ch.title, ch.downloadSizeMb)
                                            }
                                        },
                                        leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (manga.isFavorite) "إزالة من المفضلة" else "إضافة إلى المفضلة") },
                                        onClick = {
                                            showMoreOptions = false
                                            viewModel.toggleFavorite(manga.id, manga.isFavorite)
                                        },
                                        leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null) }
                                    )
                                }
                            }
                        }
                    }

                    // Manga Header Info overlay (Cover on left/right + Title)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Image(
                            painter = painterResource(id = manga.coverDrawable),
                            contentDescription = manga.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(105.dp)
                                .height(148.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.5.dp, colors.primary, RoundedCornerShape(14.dp))
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = manga.title,
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = manga.titleArabic,
                                color = TextSecondary,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Rating & Stats
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = ProGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${manga.rating}",
                                    color = ProGold,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "| ${manga.ratingCount}",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Metadata Pills
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TagBadge(text = manga.status, color = colors.primary)
                                TagBadge(text = "${manga.releaseYear}", color = TextSecondary)
                                TagBadge(text = "فصل +${manga.currentChapter}", color = colors.primary)
                                TagBadge(text = manga.ageRating, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            // Genres Chips
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    manga.genres.split(",").forEach { genre ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x2600D2FF))
                                .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = genre.trim(),
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Action Buttons: "ابدأ القراءة", "حفظ", "تنزيل"
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.navigateTo(
                                SubScreen.ReaderFull(
                                    mangaId = manga.id,
                                    chapterNumber = manga.currentChapter,
                                    isOffline = true
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1.8f),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "ابدأ القراءة",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "من الفصل ${manga.currentChapter}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.toggleFavorite(manga.id, manga.isFavorite) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (manga.isFavorite) Icons.Default.Favorite else Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = if (manga.isFavorite) Color(0xFFFF2A55) else colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "حفظ", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.startDownload(
                                manga = manga,
                                chapterNumber = manga.currentChapter,
                                chapterTitle = "الفصل ${manga.currentChapter}",
                                sizeMb = 98.1
                            )
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "تنزيل", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Description / Synopsis
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "الوصف",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = manga.synopsis,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 3
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isDescriptionExpanded) "عرض أقل" else "عرض المزيد",
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { isDescriptionExpanded = !isDescriptionExpanded }
                    )
                }
            }

            // Author and Artist Card
            item {
                Spacer(modifier = Modifier.height(14.dp))
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colors.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "المؤلف", color = TextSecondary, fontSize = 9.sp)
                                Text(text = manga.author, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .height(30.dp)
                                .width(1.dp)
                                .background(colors.border)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colors.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Brush,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "الرسام", color = TextSecondary, fontSize = 9.sp)
                                Text(text = manga.artist, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Chapters Header & Search
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FormatListNumbered,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "الفصول (+${chapters.size} فصل)",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isDescendingSort = !isDescendingSort }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isDescendingSort) "ترتيب الفصول" else "ترتيب الفصول",
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Chapter Search Bar (Image 9)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF091427))
                        .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        androidx.compose.foundation.text.BasicTextField(
                            value = chapterSearchQuery,
                            onValueChange = { chapterSearchQuery = it },
                            textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 12.sp),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.primary),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                if (chapterSearchQuery.isEmpty()) {
                                    Text(
                                        text = "البحث في الفصول (رقم أو عنوان) ...",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                        if (chapterSearchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "مسح",
                                tint = TextSecondary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { chapterSearchQuery = "" }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Chapter list items
            items(filteredChapters, key = { it.id }) { chapter ->
                ChapterItemCard(
                    chapter = chapter,
                    manga = manga,
                    onReadClick = {
                        viewModel.navigateTo(
                            SubScreen.ReaderFull(
                                mangaId = manga.id,
                                chapterNumber = chapter.chapterNumber,
                                isOffline = chapter.isDownloaded
                            )
                        )
                    },
                    onDownloadClick = {
                        viewModel.startDownload(
                            manga = manga,
                            chapterNumber = chapter.chapterNumber,
                            chapterTitle = "الفصل ${chapter.chapterNumber} - ${chapter.title}",
                            sizeMb = chapter.downloadSizeMb
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun TagBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0x330E172A))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = text, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ChapterItemCard(
    chapter: ChapterEntity,
    manga: MangaEntity,
    onReadClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    val colors = LocalMangaZzColors.current

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onReadClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.2f))
                    .clickable { onReadClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "قراءة",
                    tint = colors.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "الفصل ${chapter.chapterNumber}: ${chapter.title}",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (chapter.chapterNumber == 179) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.primary)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(text = "الأحدث", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${chapter.releaseDate} • ${chapter.downloadSizeMb} MB",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }

            // Download or Downloaded Check
            if (chapter.isDownloaded) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(StatusSuccess.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "تم التنزيل",
                        tint = StatusSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                IconButton(onClick = onDownloadClick) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "تنزيل",
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
