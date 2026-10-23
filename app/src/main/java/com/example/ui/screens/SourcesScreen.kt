package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SourceEntity
import com.example.data.model.MangaSourceConfig
import com.example.ui.components.GlassCard
import com.example.ui.components.MangaZzTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.MangaViewModel
import com.example.ui.viewmodel.SubScreen

@Composable
fun SourcesScreen(
    viewModel: MangaViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalMangaZzColors.current
    val allSources by viewModel.allSources.collectAsState()

    var selectedFilterIndex by remember { mutableIntStateOf(0) } // 0: الكل, 1: مدمجة, 2: مخصصة
    var searchQuery by remember { mutableStateOf("") }
    var showExportJsonDialog by remember { mutableStateOf(false) }
    var exportedJson by remember { mutableStateOf("") }

    val displaySources = remember(allSources) {
        if (allSources.isNotEmpty()) allSources else MangaSourceConfig.OFFICIAL_SOURCES
    }

    val filteredSources = remember(displaySources, selectedFilterIndex, searchQuery) {
        displaySources.filter { src ->
            val matchesFilter = when (selectedFilterIndex) {
                1 -> src.isBuiltIn
                2 -> !src.isBuiltIn
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                    src.name.contains(searchQuery, ignoreCase = true) ||
                    src.url.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    val builtInCount = displaySources.count { it.isBuiltIn }
    val customCount = displaySources.count { !it.isBuiltIn }

    Scaffold(
        topBar = {
            MangaZzTopBar(
                title = "المصادر الرسمية (${displaySources.size})",
                subtitle = "المواقع الثمانية المعتمدة لتصفح وقراءة المانجا",
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
            // Informational Notification Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.primary.copy(alpha = 0.12f))
                        .border(1.dp, colors.primary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "تم اعتماد المواقع الثمانية الرسمية وحذف المصادر القديمة",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "تعمل جميع المواقع داخل المتصفح المدمج مع حماية كاملة ضد أخطاء الاتصال.",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Interactive Search Bar
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
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                                cursorBrush = SolidColor(colors.primary),
                                modifier = Modifier.weight(1f),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "ابحث بين المواقع الثمانية المعتمدة...",
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "مسح",
                                    tint = TextSecondary,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { searchQuery = "" }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.cardGlass)
                            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                            .clickable {
                                selectedFilterIndex = (selectedFilterIndex + 1) % 3
                            },
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
            }

            // Filter Tabs (الكل, مدمجة, مخصصة)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterTabButton(
                        title = "الكل (${displaySources.size})",
                        icon = Icons.Default.GridView,
                        isSelected = selectedFilterIndex == 0,
                        onClick = { selectedFilterIndex = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    FilterTabButton(
                        title = "مدمجة ($builtInCount)",
                        icon = Icons.Default.Check,
                        isSelected = selectedFilterIndex == 1,
                        onClick = { selectedFilterIndex = 1 },
                        modifier = Modifier.weight(1f)
                    )
                    FilterTabButton(
                        title = "مخصصة ($customCount)",
                        icon = Icons.Default.Person,
                        isSelected = selectedFilterIndex == 2,
                        onClick = { selectedFilterIndex = 2 },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Action Buttons: تحديث ومزامنة المصادر الثمانية, تصدير
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.resetToOfficialSources()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1.5f),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مزامنة المواقع الثمانية",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            exportedJson = viewModel.exportSourcesJson()
                            showExportJsonDialog = true
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "تصدير", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }

                if (showExportJsonDialog) {
                    AlertDialog(
                        onDismissRequest = { showExportJsonDialog = false },
                        title = {
                            Text("تصدير المصادر المعتمدة (JSON)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        },
                        text = {
                            Column {
                                Text("قائمة المصادر الثمانية المعتمدة:", color = TextSecondary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF040812))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = exportedJson.take(300) + if (exportedJson.length > 300) "..." else "",
                                        color = colors.primary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.postNotification("تم نسخ وتصدير بيانات المواقع بنجاح!")
                                    showExportJsonDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.Black)
                            ) {
                                Text("موافق")
                            }
                        },
                        containerColor = Color(0xFF071224)
                    )
                }
            }

            // Sources List Items
            items(filteredSources, key = { it.id }) { src ->
                SourceItemCard(
                    source = src,
                    onOpenBrowser = {
                        viewModel.navigateTo(SubScreen.Browser(src.url))
                    },
                    onTestConnection = { onResult ->
                        viewModel.testSourceConnection(src) { isOk, message ->
                            onResult(isOk, message)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun FilterTabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMangaZzColors.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected) colors.primary else colors.cardGlass
            )
            .border(
                1.dp,
                if (isSelected) colors.primary else colors.border,
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.Black else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = title,
                color = if (isSelected) Color.Black else TextSecondary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun SourceItemCard(
    source: SourceEntity,
    onOpenBrowser: () -> Unit,
    onTestConnection: (onResult: (Boolean, String) -> Unit) -> Unit
) {
    val colors = LocalMangaZzColors.current
    var isEnabled by remember { mutableStateOf(true) }
    var isTesting by remember { mutableStateOf(false) }
    var testResultText by remember { mutableStateOf<String?>(null) }
    var testIsSuccess by remember { mutableStateOf(true) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        onClick = onOpenBrowser
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Source Initial Avatar
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.primary.copy(alpha = 0.15f))
                        .border(1.dp, colors.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = source.name.take(2).uppercase(),
                        color = colors.primary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name and URL
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = source.name,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isEnabled) Color(0xFF00E676) else TextSecondary)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = source.url,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Enable/Disable Switch
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { isEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = colors.primary,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = Color(0xFF091427)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description / Note
            Text(
                text = source.description,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            // Test Result display if tested
            if (testResultText != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (testIsSuccess) Color(0x1F00E676) else Color(0x1FFF5252))
                        .border(
                            1.dp,
                            if (testIsSuccess) Color(0x4D00E676) else Color(0x4DFF5252),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (testIsSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (testIsSuccess) Color(0xFF00E676) else Color(0xFFFF5252),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = testResultText ?: "",
                        color = if (testIsSuccess) Color(0xFF00E676) else Color(0xFFFF5252),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: "فحص الاتصال", "فتح في المتصفح"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        isTesting = true
                        testResultText = "جاري فحص الاتصال بـ ${source.name}..."
                        onTestConnection { success, message ->
                            isTesting = false
                            testIsSuccess = success
                            testResultText = message
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NetworkCheck,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isTesting) "جاري الفحص..." else "فحص الاتصال",
                        color = TextPrimary,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = onOpenBrowser,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary.copy(alpha = 0.2f),
                        contentColor = colors.primary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.primary.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "فتح المتصفح",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
