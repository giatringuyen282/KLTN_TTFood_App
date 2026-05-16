package com.example.a43_kltn_ttfood.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a43_kltn_ttfood.ui.theme.*

data class NotificationItem(
    val id: Int,
    val emoji: String,
    val title: String,
    val message: String,
    val time: String,
    val isRead: Boolean = false
)

/**
 * 🔔 Màn hình Thông báo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit = {}
) {
    val notifications = remember {
        listOf(
            NotificationItem(
                1, "🎉", "Khuyến mãi đặc biệt!",
                "Giảm 50% cho đơn hàng đầu tiên trong ngày. Áp dụng đến hết hôm nay!",
                "5 phút trước"
            ),
            NotificationItem(
                2, "🚚", "Đơn hàng đang giao",
                "Tài xế đang trên đường giao đơn hàng #1234 đến bạn.",
                "15 phút trước"
            ),
            NotificationItem(
                3, "⭐", "Đánh giá đơn hàng",
                "Hãy đánh giá đơn hàng Phở bò tái chín từ Phở 24 nhé!",
                "1 giờ trước",
                isRead = true
            ),
            NotificationItem(
                4, "🎁", "Quà tặng sinh nhật",
                "Chúc mừng sinh nhật! Bạn nhận được voucher giảm 100.000₫.",
                "2 giờ trước",
                isRead = true
            ),
            NotificationItem(
                5, "📢", "Nhà hàng mới",
                "Pizza Hut vừa mở cửa hàng mới gần bạn. Khám phá ngay!",
                "3 giờ trước",
                isRead = true
            )
        )
    }

    Scaffold(
        containerColor = Gray50,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Thông báo",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Gray700
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { /* TODO: Mark all as read */ }) {
                        Text(
                            text = "Đọc tất cả",
                            style = MaterialTheme.typography.labelMedium,
                            color = Orange500
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notifications) { notification ->
                NotificationCard(notification = notification)
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: NotificationItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) White else Orange50
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.isRead) 1.dp else 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Emoji icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (notification.isRead) Gray100 else Orange100),
                contentAlignment = Alignment.Center
            ) {
                Text(text = notification.emoji, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Orange500)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = notification.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray400
                )
            }
        }
    }
}
