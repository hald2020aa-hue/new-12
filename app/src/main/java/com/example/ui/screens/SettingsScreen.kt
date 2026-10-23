package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppColorTheme
import com.example.data.model.ReadingDirection
import com.example.ui.components.GlassCard
import com.example.ui.components.MangaZzTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.MangaViewModel
import com.example.ui.viewmodel.SubScreen

@Composable
fun SettingsScreen(
    viewModel: MangaViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalMangaZzColors.current
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val isAdBlockerEnabled by viewModel.isAdBlockerEnabled.collectAsState()
    val readingDirection by viewModel.readingDirection.collectAsState()

    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MangaZzTopBar(
                title = "الإعدادات والمظهر",
                subtitle = "خصص تجربتك في Manga V بالشكل الذي يناسبك",
                onProClick = { viewModel.navigateTo(SubScreen.Backup) }
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
            // Language Selector Card
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "اللغة", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "اختر لغة واجهة التطبيق", color = TextSecondary, fontSize = 10.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF091222))
                                .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                                .clickable { showLanguageDialog = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "العربية", color = colors.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Theme Mode (فاتح, داكن, تلقائي)
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DarkMode,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "المظهر", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "اختر النمط العام للتطبيق", color = TextSecondary, fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeModePill(title = "فاتح", icon = Icons.Default.WbSunny, isSelected = false, modifier = Modifier.weight(1f))
                            ThemeModePill(title = "داكن", icon = Icons.Default.NightlightRound, isSelected = true, modifier = Modifier.weight(1f))
                            ThemeModePill(title = "تلقائي", icon = Icons.Default.BrightnessAuto, isSelected = false, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // 5 Theme Color Palettes (ألوان الثيم)
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "ألوان الثيم", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = "اختر لون المظهر الذي تفضله", color = TextSecondary, fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ThemeColorAvatarCard(
                            theme = AppColorTheme.BLUE,
                            avatarDrawable = R.drawable.theme_avatar_blue,
                            isSelected = selectedTheme == AppColorTheme.BLUE,
                            accentColor = ThemeBluePrimary,
                            onSelect = { viewModel.setTheme(AppColorTheme.BLUE) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeColorAvatarCard(
                            theme = AppColorTheme.RED,
                            avatarDrawable = R.drawable.theme_avatar_red,
                            isSelected = selectedTheme == AppColorTheme.RED,
                            accentColor = ThemeRedPrimary,
                            onSelect = { viewModel.setTheme(AppColorTheme.RED) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeColorAvatarCard(
                            theme = AppColorTheme.GREEN,
                            avatarDrawable = R.drawable.theme_avatar_green,
                            isSelected = selectedTheme == AppColorTheme.GREEN,
                            accentColor = ThemeGreenPrimary,
                            onSelect = { viewModel.setTheme(AppColorTheme.GREEN) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeColorAvatarCard(
                            theme = AppColorTheme.PURPLE,
                            avatarDrawable = R.drawable.theme_avatar_purple,
                            isSelected = selectedTheme == AppColorTheme.PURPLE,
                            accentColor = ThemePurplePrimary,
                            onSelect = { viewModel.setTheme(AppColorTheme.PURPLE) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeColorAvatarCard(
                            theme = AppColorTheme.GOLD,
                            avatarDrawable = R.drawable.theme_avatar_gold,
                            isSelected = selectedTheme == AppColorTheme.GOLD,
                            accentColor = ThemeGoldPrimary,
                            onSelect = { viewModel.setTheme(AppColorTheme.GOLD) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Reading Preferences (تفضيلات القراءة)
            item {
                Spacer(modifier = Modifier.height(14.dp))
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "تفضيلات القراءة", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "خصص تجربة القراءة كما تحب", color = TextSecondary, fontSize = 10.sp)
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PrefMiniChip(label = "حجم الخط", value = "متوسط", modifier = Modifier.weight(1f))
                            PrefMiniChip(
                                label = "نمط القراءة",
                                value = if (readingDirection == ReadingDirection.VERTICAL) "عمودي" else "أفقي",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    viewModel.setReadingDirection(
                                        if (readingDirection == ReadingDirection.VERTICAL) ReadingDirection.HORIZONTAL else ReadingDirection.VERTICAL
                                    )
                                }
                            )
                            PrefMiniChip(label = "سطوع الشاشة", value = "تلقائي", modifier = Modifier.weight(1f))
                            PrefMiniChip(label = "تأثير الانتقال", value = "انزلاق", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Ad Blocker Switch Card
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "حظر الإعلانات", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "تفعيل ميزة حظر الإعلانات لتحصل على تجربة أنظف", color = TextSecondary, fontSize = 10.sp)
                            }
                        }

                        Switch(
                            checked = isAdBlockerEnabled,
                            onCheckedChange = { viewModel.toggleAdBlocker() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.primary
                            )
                        )
                    }
                }
            }

            // Backup & Restore Link Card
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    onClick = { viewModel.navigateTo(SubScreen.Backup) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "النسخ الاحتياطي والاستعادة", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "احفظ بياناتك واستعدها في أي وقت", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // About App Card
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "حول التطبيق", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "معلومات الإصدار، الحقوق والمزيد", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                        Text(text = "الإصدار 2.4.0", color = colors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeModePill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalMangaZzColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) colors.primary.copy(alpha = 0.2f) else colors.cardGlass)
            .border(1.dp, if (isSelected) colors.primary else colors.border, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) colors.primary else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                color = if (isSelected) colors.primary else TextSecondary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun ThemeColorAvatarCard(
    theme: AppColorTheme,
    avatarDrawable: Int,
    isSelected: Boolean,
    accentColor: Color,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMangaZzColors.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable { onSelect() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) accentColor else colors.border,
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            Image(
                painter = painterResource(id = avatarDrawable),
                contentDescription = theme.titleArabic,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Selected checkmark indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.TopEnd)
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = when (theme) {
                AppColorTheme.BLUE -> "الأزرق"
                AppColorTheme.RED -> "الأحمر"
                AppColorTheme.GREEN -> "الأخضر"
                AppColorTheme.PURPLE -> "البنفسجي"
                AppColorTheme.GOLD -> "ذهبي داكن"
            },
            color = if (isSelected) accentColor else TextSecondary,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun PrefMiniChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalMangaZzColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF091222))
            .border(1.dp, colors.border, RoundedCornerShape(10.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, color = TextSecondary, fontSize = 8.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, color = colors.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
