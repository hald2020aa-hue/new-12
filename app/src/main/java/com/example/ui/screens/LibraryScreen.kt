package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MangaEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.MangaZzTopBar
import com.example.ui.theme.LocalMangaZzColors
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MangaViewModel
import com.example.ui.viewmodel.SubScreen

@Composable
fun LibraryScreen(
    viewModel: MangaViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalMangaZzColors.current
    val allManga by viewModel.allManga.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val readLater by viewModel.readLater.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: الكل, 1: المفضلة, 2: أقرأ لاحقاً, 3: المحملة
    var selectedFolder by remember { mutableStateOf("جميع المجلدات") }
    var searchQuery by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(true) }

    val folders = listOf("جميع المجلدات", "المتابعة", "المكتملة", "المفضلة")

    val displayedManga = remember(allManga, selectedTab, selectedFolder, searchQuery) {
        var list = when (selectedTab) {
            1 -> favorites
            2 -> readLater
            3 -> allManga.filter { it.readingProgress > 0 || it.id in listOf("solo_leveling", "jujutsu_kaisen", "one_piece") }
            else -> allManga
        }

        if (selectedFolder != "جميع المجلدات") {
            list = list.filter { it.folder == selectedFolder }
        }

        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.titleArabic.contains(searchQuery, ignoreCase = true)
            }
        }
        list
    }

    Scaffold(
        topBar = {
            MangaZzTopBar(
                title = "المكتبة",
                subtitle = "كل أعمالك في مكان واحد",
                onProClick = { viewModel.navigateTo(SubScreen.Backup) }
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.cardGlass)
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "ابحث في مكتبتك ..." else searchQuery,
                            color = if (searchQuery.isEmpty()) TextSecondary else TextPrimary,
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.cardGlass)
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "فلاتر",
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Tabs Row (الكل, المفضلة, أقرأ لاحقاً, المحملة)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LibraryTabChip(
                    title = "الكل",
                    count = allManga.size,
                    icon = Icons.Default.GridView,
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                LibraryTabChip(
                    title = "المفضلة",
                    count = favorites.size,
                    icon = Icons.Default.Favorite,
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
                LibraryTabChip(
                    title = "أقرأ لاحقاً",
                    count = readLater.size,
                    icon = Icons.Default.Schedule,
                    isSelected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.weight(1.1f)
                )
                LibraryTabChip(
                    title = "المحملة",
                    count = 3,
                    icon = Icons.Default.Download,
                    isSelected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    modifier = Modifier.weight(1f)
                )
            }

            // Sort & Filter row: الأحدث إضافة, كل الأنواع, كل الحالات, Grid/List toggle (Image 6)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LibraryFilterPill(title = "الأحدث إضافة", icon = Icons.Default.SwapVert)
                    LibraryFilterPill(title = "كل الأنواع", icon = Icons.Default.MenuBook)
                    LibraryFilterPill(title = "كل الحالات", icon = Icons.Default.LocalOffer)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF091427))
                        .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isGridView) colors.primary else Color.Transparent)
                            .clickable { isGridView = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "عرض شبكي",
                            tint = if (isGridView) Color.Black else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!isGridView) colors.primary else Color.Transparent)
                            .clickable { isGridView = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatListBulleted,
                            contentDescription = "عرض قائمة",
                            tint = if (!isGridView) Color.Black else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Folders Horizontal List
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(folders) { folderName ->
                    val isSelected = selectedFolder == folderName
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) colors.primary.copy(alpha = 0.2f) else colors.cardGlass)
                            .border(1.dp, if (isSelected) colors.primary else colors.border, RoundedCornerShape(12.dp))
                            .clickable { selectedFolder = folderName }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = if (isSelected) colors.primary else TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = folderName,
                                color = if (isSelected) colors.primary else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.cardGlass)
                            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                            .clickable { }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "إضافة مجلد",
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Manga Cards Grid (3 Columns)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayedManga, key = { it.id }) { manga ->
                    LibraryMangaItem(
                        manga = manga,
                        onClick = {
                            viewModel.navigateTo(SubScreen.MangaDetail(manga.id))
                        },
                        onFavoriteClick = {
                            viewModel.toggleFavorite(manga.id, manga.isFavorite)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LibraryTabChip(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMangaZzColors.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) colors.primary else colors.cardGlass)
            .border(1.dp, if (isSelected) colors.primary else colors.border, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.Black else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                color = if (isSelected) Color.Black else TextPrimary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            Text(
                text = "$count أعمال",
                color = if (isSelected) Color(0xCC000000) else TextSecondary,
                fontSize = 8.sp
            )
        }
    }
}

@Composable
fun LibraryMangaItem(
    manga: MangaEntity,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val colors = LocalMangaZzColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, colors.border, RoundedCornerShape(14.dp))
        ) {
            Image(
                painter = painterResource(id = manga.coverDrawable),
                contentDescription = manga.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Top Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (manga.id == "solo_leveling") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.primary)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "جديد",
                            color = Color.Black,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Heart favorite icon
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0x99000000))
                        .clickable { onFavoriteClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (manga.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "مفضلة",
                        tint = if (manga.isFavorite) Color(0xFFFF2A55) else Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = manga.title,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "الفصل ${manga.currentChapter} من ${if (manga.status == "مكتمل") manga.totalChapters else "؟"}",
            color = TextSecondary,
            fontSize = 9.sp
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { manga.readingProgress / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = colors.primary,
            trackColor = Color(0x3300D2FF)
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Status Tag
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (manga.status == "مكتمل") StatusSuccess.copy(alpha = 0.2f) else colors.primary.copy(alpha = 0.2f))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = manga.status,
                    color = if (manga.status == "مكتمل") StatusSuccess else colors.primary,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "${manga.readingProgress}%",
                color = colors.primary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LibraryFilterPill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val colors = LocalMangaZzColors.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF091427))
            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
            .clickable { }
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(2.dp))
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(14.dp)
        )
    }
}

