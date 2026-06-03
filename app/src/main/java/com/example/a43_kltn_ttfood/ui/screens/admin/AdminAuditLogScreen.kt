package com.example.a43_kltn_ttfood.ui.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a43_kltn_ttfood.data.model.*
import com.example.a43_kltn_ttfood.data.repository.UserRepository
import com.example.a43_kltn_ttfood.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// =====================================================
// 📜 LỊCH SỬ HOẠT ĐỘNG / AUDIT LOG (ADMIN)
// =====================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAuditLogScreen(
    onNavigateBack: () -> Unit
) {
    val userRepo = remember { UserRepository() }
    var auditLogs by remember { mutableStateOf<List<AuditLog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        userRepo.getAllAuditLogs().collect { result ->
            auditLogs = result
            isLoading = false
        }
    }

    val filteredLogs = remember(auditLogs, selectedFilter) {
        if (selectedFilter == null) auditLogs
        else auditLogs.filter { it.action == selectedFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Lịch sử hoạt động", fontWeight = FontWeight.Bold)
                        Text(
                            "Audit Log — Ai đã làm gì, khi nào",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray500
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter chips
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("Tất cả") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Orange500,
                        selectedLabelColor = White
                    )
                )
                listOf(
                    AuditAction.LOGIN to "Đăng nhập",
                    AuditAction.LOGOUT to "Đăng xuất",
                    AuditAction.BLOCK_USER to "Khóa user",
                    AuditAction.UNBLOCK_USER to "Mở khóa",
                    AuditAction.UPDATE_ORDER to "Cập nhật đơn",
                    AuditAction.CREATE_VOUCHER to "Tạo voucher",
                    AuditAction.UPDATE_RESTAURANT to "Cập nhật NH"
                ).forEach { (action, label) ->
                    FilterChip(
                        selected = selectedFilter == action,
                        onClick = {
                            selectedFilter = if (selectedFilter == action) null else action
                        },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Orange500,
                            selectedLabelColor = White
                        )
                    )
                }
            }

            // Counter
            Text(
                "${filteredLogs.size} bản ghi",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Orange500)
                }
            } else if (filteredLogs.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📜", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Chưa có lịch sử hoạt động", color = Gray500)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredLogs, key = { it.id }) { log ->
                        AuditLogCard(log)
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun AuditLogCard(log: AuditLog) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("vi")) }

    val actionEmoji = when (log.action) {
        AuditAction.LOGIN -> "🔑"
        AuditAction.LOGOUT -> "🚪"
        AuditAction.BLOCK_USER -> "🚫"
        AuditAction.UNBLOCK_USER -> "✅"
        AuditAction.CHANGE_PASSWORD -> "🔒"
        AuditAction.UPDATE_ORDER -> "📦"
        AuditAction.CREATE_VOUCHER -> "🎫"
        AuditAction.UPDATE_RESTAURANT -> "🏪"
        AuditAction.DELETE_FOOD -> "🗑️"
        else -> "📝"
    }

    val actionLabel = when (log.action) {
        AuditAction.LOGIN -> "Đăng nhập"
        AuditAction.LOGOUT -> "Đăng xuất"
        AuditAction.BLOCK_USER -> "Khóa tài khoản"
        AuditAction.UNBLOCK_USER -> "Mở khóa tài khoản"
        AuditAction.CHANGE_PASSWORD -> "Đổi mật khẩu"
        AuditAction.UPDATE_ORDER -> "Cập nhật đơn hàng"
        AuditAction.CREATE_VOUCHER -> "Tạo voucher"
        AuditAction.UPDATE_RESTAURANT -> "Cập nhật nhà hàng"
        AuditAction.DELETE_FOOD -> "Xóa món ăn"
        else -> log.action
    }

    val actionColor = when (log.action) {
        AuditAction.LOGIN -> Color(0xFF667EEA)
        AuditAction.LOGOUT -> Gray500
        AuditAction.BLOCK_USER -> ErrorRed
        AuditAction.UNBLOCK_USER -> SuccessGreen
        AuditAction.UPDATE_ORDER -> InfoBlue
        AuditAction.CREATE_VOUCHER -> Orange500
        AuditAction.UPDATE_RESTAURANT -> Color(0xFF11998E)
        AuditAction.DELETE_FOOD -> ErrorRed
        else -> Gray600
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, Gray100)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Emoji icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(actionColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(actionEmoji, fontSize = 18.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Action label
                Text(
                    actionLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = actionColor
                )

                // Details
                if (log.details.isNotBlank()) {
                    Text(
                        log.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray700,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // User info
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("👤", fontSize = 12.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        log.userName.ifBlank { "Không rõ" },
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray500
                    )

                    if (log.targetId.isNotBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Text("→", style = MaterialTheme.typography.labelSmall, color = Gray400)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${log.targetType} #${log.targetId.take(8)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Gray400
                        )
                    }
                }
            }

            // Timestamp
            Text(
                log.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = Gray400
            )
        }
    }
}
