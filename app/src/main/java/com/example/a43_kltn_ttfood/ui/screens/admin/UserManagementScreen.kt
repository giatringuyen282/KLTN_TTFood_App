package com.example.a43_kltn_ttfood.ui.screens.admin

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.a43_kltn_ttfood.data.model.*
import com.example.a43_kltn_ttfood.data.repository.UserRepository
import com.example.a43_kltn_ttfood.data.repository.OrderRepository
import com.example.a43_kltn_ttfood.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// =====================================================
// 👥 QUẢN LÝ NGƯỜI DÙNG
// =====================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUserDetail: (String) -> Unit
) {
    val userRepo = remember { UserRepository() }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf<String?>(null) }
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load users real-time
    LaunchedEffect(searchQuery, selectedRoleFilter) {
        val flow = when {
            searchQuery.isNotBlank() -> userRepo.searchUsers(searchQuery)
            selectedRoleFilter != null -> userRepo.getUsersByRole(selectedRoleFilter!!)
            else -> userRepo.getAllUsers()
        }
        flow.collect { result ->
            users = result
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Quản lý Người dùng", fontWeight = FontWeight.Bold)
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
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Tìm theo tên, SĐT, email...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Gray500) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, "Xóa")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GrabGreen,
                    cursorColor = GrabGreen
                )
            )

            // Role filter chips
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedRoleFilter == null,
                    onClick = { selectedRoleFilter = null },
                    label = { Text("Tất cả") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GrabGreen,
                        selectedLabelColor = White
                    )
                )
                listOf(
                    UserRole.CUSTOMER to "Khách hàng",
                    UserRole.ADMIN to "Admin",
                    UserRole.SHIPPER to "Shipper"
                ).forEach { (role, label) ->
                    FilterChip(
                        selected = selectedRoleFilter == role,
                        onClick = {
                            selectedRoleFilter = if (selectedRoleFilter == role) null else role
                        },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GrabGreen,
                            selectedLabelColor = White
                        )
                    )
                }
            }

            // Counter
            Text(
                "${users.size} người dùng",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            // User list
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GrabGreen)
                }
            } else if (users.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👤", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Không tìm thấy người dùng", color = Gray500)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users, key = { it.id }) { user ->
                        UserCard(
                            user = user,
                            onClick = { onNavigateToUserDetail(user.id) },
                            onToggleActive = {
                                scope.launch {
                                    userRepo.toggleUserActive(
                                        userId = user.id,
                                        isActive = !user.isActive,
                                        adminId = "admin",  // TODO: real admin ID
                                        adminName = "Admin" // TODO: real admin name
                                    )
                                }
                            }
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun UserCard(
    user: User,
    onClick: () -> Unit,
    onToggleActive: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    val roleEmoji = when (user.role) {
        UserRole.ADMIN -> "🛠️"
        UserRole.SHIPPER -> "🛵"
        else -> "🧑"
    }
    val roleLabel = when (user.role) {
        UserRole.ADMIN -> "Admin"
        UserRole.SHIPPER -> "Shipper"
        else -> "Khách hàng"
    }
    val roleColor = when (user.role) {
        UserRole.ADMIN -> Color(0xFF667EEA)
        UserRole.SHIPPER -> Color(0xFFFC5C7D)
        else -> Color(0xFF11998E)
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (user.isActive)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        border = BorderStroke(
            1.dp,
            if (user.isActive) Gray200 else ErrorRed.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(roleColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(roleEmoji, fontSize = 22.sp)
            }

            Spacer(Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        user.fullName.ifBlank { "Chưa đặt tên" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!user.isActive) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "BLOCKED",
                            style = MaterialTheme.typography.labelSmall,
                            color = White,
                            modifier = Modifier
                                .background(ErrorRed, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    user.email.ifBlank { user.phone },
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500,
                    maxLines = 1
                )
                // Role badge
                Text(
                    roleLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = roleColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .background(roleColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            // Block/Unblock button
            IconButton(onClick = { showConfirmDialog = true }) {
                Icon(
                    if (user.isActive) Icons.Outlined.Block else Icons.Outlined.CheckCircle,
                    contentDescription = if (user.isActive) "Khóa" else "Mở khóa",
                    tint = if (user.isActive) ErrorRed else SuccessGreen
                )
            }
        }
    }

    // Confirm dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(if (user.isActive) "Khóa tài khoản?" else "Mở khóa tài khoản?")
            },
            text = {
                Text(
                    if (user.isActive)
                        "Người dùng \"${user.fullName}\" sẽ không thể đăng nhập."
                    else
                        "Người dùng \"${user.fullName}\" sẽ có thể đăng nhập lại."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onToggleActive()
                    showConfirmDialog = false
                }) {
                    Text(
                        if (user.isActive) "Khóa" else "Mở khóa",
                        color = if (user.isActive) ErrorRed else SuccessGreen
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

// =====================================================
// 📋 CHI TIẾT NGƯỜI DÙNG + LỊCH SỬ ĐƠN + AUDIT LOG
// =====================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    userId: String,
    onNavigateBack: () -> Unit
) {
    val userRepo = remember { UserRepository() }
    val orderRepo = remember { OrderRepository() }
    val scope = rememberCoroutineScope()

    var user by remember { mutableStateOf<User?>(null) }
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var auditLogs by remember { mutableStateOf<List<AuditLog>>(emptyList()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Thông tin", "Đơn hàng", "Audit Log")

    LaunchedEffect(userId) {
        user = userRepo.getUserById(userId)
        orderRepo.getOrdersByUser(userId).collect { orders = it }
    }
    LaunchedEffect(userId) {
        userRepo.getAuditLogsForUser(userId).collect { auditLogs = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết người dùng", fontWeight = FontWeight.Bold) },
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
        if (user == null) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GrabGreen)
            }
            return@Scaffold
        }

        val u = user!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // User header card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                listOf(GrabGreen.copy(alpha = 0.1f), Red500.copy(alpha = 0.05f))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(GrabGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                u.fullName.firstOrNull()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = GrabGreen
                            )
                        }

                        Spacer(Modifier.height(12.dp))
                        Text(u.fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (u.email.isNotBlank()) {
                                Text("📧 ${u.email}", style = MaterialTheme.typography.bodySmall, color = Gray600)
                            }
                            if (u.phone.isNotBlank()) {
                                Text("📱 ${u.phone}", style = MaterialTheme.typography.bodySmall, color = Gray600)
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        // Status badge
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val roleColor = when (u.role) {
                                UserRole.ADMIN -> Color(0xFF667EEA)
                                UserRole.SHIPPER -> Color(0xFFFC5C7D)
                                else -> Color(0xFF11998E)
                            }
                            Text(
                                u.role.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = roleColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(roleColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                            Text(
                                if (u.isActive) "ACTIVE" else "BLOCKED",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (u.isActive) SuccessGreen else ErrorRed,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(
                                        (if (u.isActive) SuccessGreen else ErrorRed).copy(alpha = 0.1f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = GrabGreen,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = GrabGreen
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Tab content
            when (selectedTab) {
                0 -> UserInfoTab(u)
                1 -> UserOrdersTab(orders)
                2 -> UserAuditTab(auditLogs)
            }
        }
    }
}

@Composable
private fun UserInfoTab(user: User) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi")) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { InfoRow("Họ tên", user.fullName) }
        item { InfoRow("Email", user.email) }
        item { InfoRow("Số điện thoại", user.phone) }
        item { InfoRow("Vai trò", user.role) }
        item { InfoRow("Trạng thái", if (user.isActive) "Hoạt động" else "Đã khóa") }
        item {
            InfoRow("Ngày tạo",
                user.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "N/A")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Gray600)
        Text(
            value.ifBlank { "—" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun UserOrdersTab(orders: List<Order>) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi")) }

    if (orders.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Chưa có đơn hàng", color = Gray500)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(orders) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "#${order.id.take(8)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                order.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray500
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${String.format("%,d", order.totalAmount)}₫",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = GrabGreen
                            )
                            val statusColor = when (order.status) {
                                OrderStatus.DELIVERED -> SuccessGreen
                                OrderStatus.CANCELLED -> ErrorRed
                                else -> WarningYellow
                            }
                            Text(
                                order.status,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                modifier = Modifier
                                    .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserAuditTab(auditLogs: List<AuditLog>) {
    val dateFormat = remember { SimpleDateFormat("dd/MM HH:mm", Locale("vi")) }

    if (auditLogs.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Chưa có lịch sử hoạt động", color = Gray500)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(auditLogs) { log ->
                val actionEmoji = when (log.action) {
                    AuditAction.LOGIN -> "🔑"
                    AuditAction.BLOCK_USER -> "🚫"
                    AuditAction.UNBLOCK_USER -> "✅"
                    AuditAction.CHANGE_PASSWORD -> "🔒"
                    else -> "📝"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(actionEmoji, fontSize = 20.sp)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            log.details.ifBlank { log.action },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "bởi ${log.userName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Gray500
                        )
                    }
                    Text(
                        log.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray400
                    )
                }
            }
        }
    }
}
