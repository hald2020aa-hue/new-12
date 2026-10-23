package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.MangaEntity
import com.example.data.local.SourceEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.MangaZzTopBar
import com.example.ui.components.NavTab
import com.example.ui.theme.LocalMangaZzColors
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MangaViewModel
import com.example.ui.viewmodel.SubScreen

@Composable
fun HomeScreen(
    viewModel: MangaViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalMangaZzColors.current
    val mangaList by viewModel.allManga.collectAsState()
    val sources by viewModel.allSources.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val filteredManga = remember(searchQuery, mangaList) {
        if (searchQuery.isBlank()) mangaList
        else mangaList.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.titleArabic.contains(searchQuery, ignoreCase = true) ||
            it.genres.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            MangaZzTopBar(
                onNotificationClick = {
                    viewModel.navigateTo(SubScreen.Downloads)
                },
                onProClick = {
                    viewModel.navigateTo(SubScreen.Backup)
                }
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Search Bar & Filter Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF091427))
                            .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "بحث",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                                cursorBrush = SolidColor(colors.primary),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "ابحث عن مانجا، مؤلف، أو أي شيء ...",
                                            color = TextSecondary,
                                            fontSize = 13.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF091427))
                            .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                            .clickable { viewModel.setTab(NavTab.LIBRARY) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "تصفية",
                            tint = colors.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // 4 Category Action Cards in a single row (Matching Image 4)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CategoryMiniCard(
                        title = "المصادر",
                        subtitle = "مواقع مدمجة",
                        icon = Icons.Default.Explore,
                        iconTint = colors.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setTab(NavTab.SOURCES) }
                    )
                    CategoryMiniCard(
                        title = "تصفح حسب النوع",
                        subtitle = "أكشن، رومانسي ...",
                        icon = Icons.Default.GridView,
                        iconTint = colors.secondary,
                        modifier = Modifier.weight(1.05f),
                        onClick = { viewModel.setTab(NavTab.LIBRARY) }
                    )
                    CategoryMiniCard(
                        title = "جديد المانجا",
                        subtitle = "أحدث الإصدارات",
                        icon = Icons.Default.AutoAwesome,
                        iconTint = colors.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setTab(NavTab.LIBRARY) }
                    )
                    CategoryMiniCard(
                        title = "الأكثر رواجاً",
                        subtitle = "مانجا شهيرة الآن",
                        icon = Icons.Default.Whatshot,
                        iconTint = Color(0xFFFF5252),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setTab(NavTab.LIBRARY) }
                    )
                }
            }

            // Featured Hero Card (Solo Leveling)
            item {
                val heroManga = mangaList.firstOrNull { it.id == "solo_leveling" }
                if (heroManga != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    FeaturedHeroBanner(
                        manga = heroManga,
                        onReadClick = {
                            viewModel.navigateTo(
                                SubScreen.ReaderFull(
                                    mangaId = heroManga.id,
                                    chapterNumber = heroManga.currentChapter,
                                    isOffline = true
                                )
                            )
                        },
                        onDetailsClick = {
                            viewModel.navigateTo(SubScreen.MangaDetail(heroManga.id))
                        }
                    )
                }
            }

            // "مانجاك المحفوظة" Section
            item {
                Spacer(modifier = Modifier.height(18.dp))
                SectionHeader(
                    title = "مانجاك المحفوظة",
                    icon = Icons.Default.Bookmark,
                    onViewAllClick = { viewModel.setTab(NavTab.LIBRARY) }
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredManga) { manga ->
                        SavedMangaCard(
                            manga = manga,
                            onClick = {
                                viewModel.navigateTo(SubScreen.MangaDetail(manga.id))
                            },
                            onFavoriteToggle = {
                                viewModel.toggleFavorite(manga.id, manga.isFavorite)
                            }
                        )
                    }
                }
            }

            // "المصادر المدمجة" Section
            item {
                Spacer(modifier = Modifier.height(20.dp))
                SectionHeader(
                    title = "المصادر المدمجة",
                    icon = Icons.Default.Link,
                    onViewAllClick = { viewModel.setTab(NavTab.SOURCES) }
                )
                Spacer(modifier = Modifier.height(10.dp))

                val builtInSources = sources.filter { it.isBuiltIn }.take(4)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    builtInSources.forEach { src ->
                        SourceMiniCard(
                            source = src,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.navigateTo(SubScreen.Browser(src.url))
                            }
                        )
                    }
                }
            }

            // In-app Browser CTA Banner
            item {
                Spacer(modifier = Modifier.height(20.dp))
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onClick = {
                        viewModel.navigateTo(SubScreen.Browser(com.example.data.model.MangaSourceConfig.DEFAULT_SOURCE_URL))
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.primary.copy(alpha = 0.15f))
                                .border(1.dp, colors.primary.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "المتصفح الداخلي",
                                tint = colors.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تصفح المواقع داخل التطبيق",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "يمكنك فتح مواقع المانجا مباشرة داخل Manga V لتجربة قراءة سلسة وآمنة دون الحاجة لترك التطبيق.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "فتح",
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryMiniCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalMangaZzColors.current
    GlassCard(
        modifier = modifier.height(86.dp),
        onClick = onClick,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FeaturedHeroBanner(
    manga: MangaEntity,
    onReadClick: () -> Unit,
    onDetailsClick: () -> Unit
) {
    val colors = LocalMangaZzColors.current

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.hero_solo_leveling),
                contentDescription = manga.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient Overlays
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0x99070A11),
                                Color(0xF2070A11)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x800D1528))
                            .border(1.dp, colors.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "متابعة القراءة",
                            color = colors.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "خيارات",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Middle Content
                Column {
                    Text(
                        text = manga.title,
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "الفصل ${manga.currentChapter} - صعود الملك",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(0.55f)
                    ) {
                        LinearProgressIndicator(
                            progress = { manga.readingProgress / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = colors.primary,
                            trackColor = Color(0x4000D2FF)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${manga.readingProgress}%",
                            color = colors.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onReadClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "متابعة القراءة",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "متابعة القراءة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onDetailsClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "عرض التفاصيل",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onViewAllClick: () -> Unit
) {
    val colors = LocalMangaZzColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.clickable { onViewAllClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "عرض الكل",
                color = TextSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(3.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "عرض الكل",
                tint = TextSecondary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun SavedMangaCard(
    manga: MangaEntity,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val colors = LocalMangaZzColors.current

    Column(
        modifier = Modifier
            .width(115.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(155.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, colors.border, RoundedCornerShape(14.dp))
        ) {
            Image(
                painter = painterResource(id = manga.coverDrawable),
                contentDescription = manga.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Top Favorite Heart Button
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .size(28.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(Color(0x99000000))
                    .clickable { onFavoriteToggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (manga.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "مفضلة",
                    tint = if (manga.isFavorite) Color(0xFFFF2A55) else Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = manga.title,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "فصل ${manga.currentChapter}",
            color = TextSecondary,
            fontSize = 10.sp
        )
    }
}

@Composable
fun SourceMiniCard(
    source: SourceEntity,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalMangaZzColors.current

    GlassCard(
        modifier = modifier.height(72.dp),
        onClick = onClick,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = source.name,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "متوفر داخل التطبيق",
                color = colors.primary,
                fontSize = 8.sp,
                maxLines = 1
            )
        }
    }
}
