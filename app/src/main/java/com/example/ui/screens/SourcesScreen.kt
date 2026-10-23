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
import androidx.compose.material.icons.Icons
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
import com.example.data.local.SourceEntity
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
    var showAddSourceDialog by remember { mutableStateOf(false) }

    val filteredSources = remember(allSources, selectedFilterIndex, searchQuery) {
        allSources.filter { src ->
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

    val builtInCount = allSources.count { it.isBuiltIn }
    val customCount = allSources.count { !it.isBuiltIn }

    Scaffold(
        topBar = {
            MangaZzTopBar(
                title = "المصادر",
                subtitle = "إدارة مواقع مانجا المدمجة والمخصصة داخل التطبيق",
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
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "تفتح جميع المصادر داخل التطبيق بدون الحاجة لترك التطبيق.",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Search Bar
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
                            Text(
                                text = if (searchQuery.isEmpty()) "ابحث عن موقع ..." else searchQuery,
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
                        title = "الكل (${allSources.size})",
                        icon = Icons.Default.GridView,
                        isSelected = selectedFilterIndex == 0,
                        onClick = { selectedFilterIndex = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    FilterTabButton(
                        title = "مدمجة ($builtInCount)",
                        icon = Icons.Default.Settings,
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

            // Action Buttons: إضافة موقع +, استيراد, تصدير
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showAddSourceDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1.5f),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "إضافة موقع",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "استيراد", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }

                    OutlinedButton(
                        onClick = { },
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
            }

            // Sources List
            items(filteredSources, key = { it.id }) { src ->
                SourceItemCard(
                    source = src,
                    onClick = {
                        viewModel.navigateTo(SubScreen.Browser(src.url))
                    },
                    onDelete = {
                        viewModel.deleteSource(src.id)
                    }
                )
            }
        }
    }

    // Add Source Dialog
    if (showAddSourceDialog) {
        var newName by remember { mutableStateOf("") }
        var newUrl by remember { mutableStateOf("https://") }

        AlertDialog(
            onDismissRequest = { showAddSourceDialog = false },
            title = {
                Text(
                    text = "إضافة مصدر مانجا مخصص",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "أدخل اسم الموقع ورابط HTTPS:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("اسم المصدر (مثال: مانجا العرب)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border
                        )
                    )
                    OutlinedTextField(
                        value = newUrl,
                        onValueChange = { newUrl = it },
                        label = { Text("رابط الموقع (URL)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newUrl.isNotBlank()) {
                            viewModel.addCustomSource(newName, newUrl)
                            showAddSourceDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.Black)
                ) {
                    Text("حفظ واختبار")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSourceDialog = false }) {
                    Text("إلغاء", color = TextSecondary)
                }
            },
            containerColor = Color(0xFF0D1424),
            shape = RoundedCornerShape(18.dp)
        )
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
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalMangaZzColors.current

    val (statusLabel, statusColor) = when (source.status) {
        "WORKING" -> Pair("يعمل بشكل جيد", StatusSuccess)
        "SLOW" -> Pair("بطيء أحياناً", StatusWarning)
        else -> Pair("غير متاح حالياً", StatusError)
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag handle 6 dots icon
            Icon(
                imageVector = Icons.Default.DragIndicator,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Delete action
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x22FF5368))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف",
                    tint = Color(0xFFFF5368),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Edit action
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.primary.copy(alpha = 0.15f))
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "تعديل",
                    tint = colors.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Name and URL
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = source.name,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = source.url,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status dot & label
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Type Tag (مدمجة / مخصصة)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x3300D2FF))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (source.isBuiltIn) "مدمجة" else "مخصصة",
                            color = colors.primary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Logo Icon Avatar on right
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF081224))
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = source.name.take(2).uppercase(),
                    color = colors.primary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
