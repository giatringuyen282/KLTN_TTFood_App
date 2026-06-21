package com.example.a43_kltn_ttfood.ui.screens.admin

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a43_kltn_ttfood.data.model.*
import com.example.a43_kltn_ttfood.data.repository.UserRepository
import com.example.a43_kltn_ttfood.data.repository.OrderRepository
import com.example.a43_kltn_ttfood.ui.theme.*
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// =====================================================
// 🛠️ ADMIN DASHBOARD — Trang tổng quan quản trị
// =====================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateToUsers: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToRestaurants: () -> Unit,
    onNavigateToVouchers: () -> Unit,
    onNavigateToAuditLog: () -> Unit,
    onNavigateToUploadImage: () -> Unit = {},   // Upload ảnh món ăn
    onNavigateBack: () -> Unit
) {
    val userRepo = remember { UserRepository() }
    val orderRepo = remember { OrderRepository() }

    var totalUsers by remember { mutableIntStateOf(0) }
    var totalCustomers by remember { mutableIntStateOf(0) }
    var totalShippers by remember { mutableIntStateOf(0) }
    var totalOrders by remember { mutableIntStateOf(0) }

    var totalAdmins by remember { mutableIntStateOf(0) }

    var allOrders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var allUsers by remember { mutableStateOf<List<User>>(emptyList()) }

    var showChartFor by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        totalUsers = userRepo.getUserCount()
        totalCustomers = userRepo.getUserCountByRole(UserRole.CUSTOMER)
        totalShippers = userRepo.getUserCountByRole(UserRole.SHIPPER)
        totalOrders = orderRepo.getOrderCount()
        totalAdmins = userRepo.getUserCountByRole(UserRole.ADMIN)

        launch {
            userRepo.getAllUsers().collect { users ->
                allUsers = users
            }
        }
        launch {
            orderRepo.getAllOrders().collect { orders ->
                allOrders = orders
            }
        }
    }

    if (showChartFor != null) {
        AlertDialog(
            onDismissRequest = { showChartFor = null },
            title = { Text("Biểu đồ: $showChartFor", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            text = {
                if (showChartFor == "Tổng users") {
                    val data = listOf(totalCustomers.toFloat(), totalShippers.toFloat(), totalAdmins.toFloat())
                    val labels = listOf("Khách hàng", "Shipper", "Admin")
                    val colors = listOf(Color(0xFF11998E), Color(0xFFFC5C7D), Color(0xFF667EEA))
                    SimplePieChart(data = data, labels = labels, colors = colors)
                } else {
                    val getDayOfWeekData: (List<Timestamp?>) -> List<Float> = { timestamps ->
                        val counts = FloatArray(7)
                        val cal = Calendar.getInstance()
                        timestamps.forEach { ts ->
                            if (ts != null) {
                                cal.time = ts.toDate()
                                val day = cal.get(Calendar.DAY_OF_WEEK)
                                val index = if (day == Calendar.SUNDAY) 6 else day - 2
                                counts[index] += 1f
                            }
                        }
                        counts.toList()
                    }

                    val data = when(showChartFor) {
                        "Tổng đơn" -> getDayOfWeekData(allOrders.map { it.createdAt })
                        "Khách hàng" -> getDayOfWeekData(allUsers.filter { it.role == UserRole.CUSTOMER }.map { it.createdAt })
                        "Shipper" -> getDayOfWeekData(allUsers.filter { it.role == UserRole.SHIPPER }.map { it.createdAt })
                        else -> listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
                    }
                    val labels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
                    SimpleBarChart(data = data, labels = labels)
                }
            },
            confirmButton = {
                TextButton(onClick = { showChartFor = null }) {
                    Text("Đóng", color = GrabGreen)
                }
            },
            containerColor = White
        )
    }

    Scaffold(
        containerColor = Gray50,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "🛠️ Quản trị viên",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "TTFood Admin Dashboard",
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
                    containerColor = White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Stat cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.People,
                        label = "Tổng users",
                        value = "$totalUsers",
                        colorStart = Color(0xFF667EEA),
                        colorEnd = Color(0xFF764BA2),
                        onClick = { showChartFor = "Tổng users" }
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.ShoppingBag,
                        label = "Tổng đơn",
                        value = "$totalOrders",
                        colorStart = GrabGreen,
                        colorEnd = Red500,
                        onClick = { showChartFor = "Tổng đơn" }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Person,
                        label = "Khách hàng",
                        value = "$totalCustomers",
                        colorStart = Color(0xFF11998E),
                        colorEnd = Color(0xFF38EF7D),
                        onClick = { showChartFor = "Khách hàng" }
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.DeliveryDining,
                        label = "Shipper",
                        value = "$totalShippers",
                        colorStart = Color(0xFFFC5C7D),
                        colorEnd = Color(0xFF6A82FB),
                        onClick = { showChartFor = "Shipper" }
                    )
                }
            }

            // Menu modules
            item {
                Text(
                    "Quản lý",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                AdminMenuItem(
                    icon = Icons.Outlined.People,
                    title = "Quản lý Người dùng",
                    subtitle = "Xem, tìm kiếm, block/unblock tài khoản",
                    color = Color(0xFF667EEA),
                    onClick = onNavigateToUsers
                )
            }

            item {
                AdminMenuItem(
                    icon = Icons.Outlined.Receipt,
                    title = "Quản lý Đơn hàng",
                    subtitle = "Xem, cập nhật trạng thái đơn hàng",
                    color = Orange500,
                    onClick = onNavigateToOrders
                )
            }

            item {
                AdminMenuItem(
                    icon = Icons.Outlined.Store,
                    title = "Quản lý Nhà hàng",
                    subtitle = "Thêm, sửa, xóa nhà hàng đối tác",
                    color = Color(0xFF11998E),
                    onClick = onNavigateToRestaurants
                )
            }

            item {
                AdminMenuItem(
                    icon = Icons.Outlined.LocalOffer,
                    title = "Voucher & Khuyến mãi",
                    subtitle = "Tạo mã giảm giá, theo dõi hiệu quả",
                    color = Color(0xFFFC5C7D),
                    onClick = onNavigateToVouchers
                )
            }

            item {
                AdminMenuItem(
                    icon = Icons.Outlined.History,
                    title = "Lịch sử hoạt động",
                    subtitle = "Audit log — ai đã làm gì, khi nào",
                    color = Color(0xFFFF8F00),
                    onClick = onNavigateToAuditLog
                )
            }

            item {
                AdminMenuItem(
                    icon = Icons.Outlined.Image,
                    title = "Ảnh món ăn",
                    subtitle = "Upload ảnh lên Firebase Storage",
                    color = Color(0xFF6C63FF),
                    onClick = onNavigateToUploadImage
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    colorStart: Color,
    colorEnd: Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(listOf(colorStart, colorEnd)),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { onClick() }
                .padding(16.dp)
        ) {
            Column {
                Icon(
                    icon, null,
                    tint = White.copy(alpha = 0.8f),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun AdminMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }

            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                null,
                tint = Gray400,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { rotationZ = 180f }
            )
        }
    }
}

@Composable
fun SimpleBarChart(data: List<Float>, labels: List<String>) {
    val maxData = data.maxOrNull() ?: 1f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEachIndexed { index, value ->
            val heightPercent = if (maxData > 0) value / maxData else 0f
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                Text(
                    text = value.toInt().toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray500
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .fillMaxHeight(heightPercent.coerceAtLeast(0.01f))
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(GrabGreen)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = labels.getOrElse(index) { "" },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SimplePieChart(data: List<Float>, labels: List<String>, colors: List<Color>) {
    val total = data.sum()
    if (total == 0f) {
        Text("Chưa có dữ liệu", color = Gray500, modifier = Modifier.padding(16.dp))
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Pie Chart
        Canvas(modifier = Modifier.size(120.dp)) {
            var startAngle = -90f
            data.forEachIndexed { index, value ->
                if (value > 0) {
                    val sweepAngle = (value / total) * 360f
                    drawArc(
                        color = colors.getOrElse(index) { Color.Gray },
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true
                    )
                    startAngle += sweepAngle
                }
            }
        }
        
        Spacer(modifier = Modifier.width(24.dp))
        
        // Legend
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            data.forEachIndexed { index, value ->
                if (value > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(colors.getOrElse(index) { Color.Gray })
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = labels.getOrNull(index) ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray600
                            )
                            Text(
                                text = value.toInt().toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
