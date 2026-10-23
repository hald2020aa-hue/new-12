package com.example.ui.components

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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalMangaZzColors
import com.example.ui.theme.ProGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MangaZzTopBar(
    title: String? = null,
    subtitle: String? = null,
    showBackButton: Boolean = false,
    headerSubtitle: String = "عالم المانجا بين يديك",
    onBackClick: (() -> Unit)? = null,
    onProClick: (() -> Unit)? = null,
    onNotificationClick: (() -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null
) {
    val colors = LocalMangaZzColors.current

    var showNotificationModal by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Top Global Header Bar (Manga ZZ logo, bell, PRO badge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Notification Bell (left in RTL or back button)
            if (showBackButton) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0C162A))
                        .border(1.dp, colors.border, CircleShape)
                        .clickable { onBackClick?.invoke() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0C162A))
                        .border(1.dp, colors.border, CircleShape)
                        .clickable {
                            if (onNotificationClick != null) {
                                onNotificationClick()
                            } else {
                                showNotificationModal = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "الإشعارات",
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    // Notification dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 8.dp)
                            .clip(CircleShape)
                            .background(colors.primary)
                    )
                }
            }

            // Manga ZZ Logo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Manga ZZ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = colors.primary
                )
                Text(
                    text = headerSubtitle,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Normal
                )
            }

            // PRO Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0x33FFD75E),
                                Color(0x11FFD75E)
                            )
                        )
                    )
                    .border(1.dp, ProGold.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                    .clickable { onProClick?.invoke() }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "PRO",
                    tint = ProGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "PRO",
                    color = ProGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Sub-Screen Header (When title is provided, e.g. "المصادر", "المكتبة", "التنزيلات", "النسخ الاحتياطي")
        if (title != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                if (actions != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        actions()
                    }
                }
            }
        }
    }

    if (showNotificationModal) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showNotificationModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إشعارات المانجا",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    NotificationItemRow(
                        title = "فصل جديد: Solo Leveling",
                        body = "صدر الفصل 180 مترجماً بالعربية بالكامل!",
                        time = "منذ 15 دقيقة",
                        isNew = true
                    )
                    NotificationItemRow(
                        title = "تحديث المصادر ⚡",
                        body = "تم تحديث كتالوج MangaDex و Comick بنجاح",
                        time = "منذ ساعتين",
                        isNew = false
                    )
                    NotificationItemRow(
                        title = "عضوية PRO الذهبية",
                        body = "تصفح بدون إعلانات وقراءة سريعة دون حدود مفعلة",
                        time = "أمس",
                        isNew = false
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = { showNotificationModal = false },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("تم", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF071224),
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
fun NotificationItemRow(
    title: String,
    body: String,
    time: String,
    isNew: Boolean
) {
    val colors = LocalMangaZzColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0A1830))
            .border(1.dp, if (isNew) colors.primary.copy(alpha = 0.5f) else colors.border, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = if (isNew) colors.primary else TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
                text = time,
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = body,
            color = TextSecondary,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}
