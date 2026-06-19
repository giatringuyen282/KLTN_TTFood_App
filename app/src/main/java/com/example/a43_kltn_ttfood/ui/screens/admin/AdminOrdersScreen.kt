package com.example.a43_kltn_ttfood.ui.screens.admin

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a43_kltn_ttfood.data.model.*
import com.example.a43_kltn_ttfood.data.repository.OrderRepository
import com.example.a43_kltn_ttfood.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// =====================================================
// 📦 QUẢN LÝ ĐƠN HÀNG (ADMIN)
// =====================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOrderDetail: (String) -> Unit
) {
    val orderRepo = remember { OrderRepository() }
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        orderRepo.getAllOrders().collect { result ->
            orders = result
            isLoading = false
        }
    }

    // Lọc đơn hàng cục bộ
    val filteredOrders = remember(orders, selectedStatusFilter, searchQuery) {
        orders.filter { order ->
            val matchesStatus = selectedStatusFilter == null || order.status == selectedStatusFilter
            val matchesQuery = searchQuery.isBlank() || 
                    order.id.lowercase().contains(searchQuery.lowercase().trim()) ||
                    order.userId.lowercase().contains(searchQuery.lowercase().trim()) ||
                    order.deliveryAddress.lowercase().contains(searchQuery.lowercase().trim())
            matchesStatus && matchesQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Đơn hàng", fontWeight = FontWeight.Bold) },
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
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Tìm theo Mã đơn, User ID...") },
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
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GrabGreen,
                    cursorColor = GrabGreen
                )
            )

            // Status Filter Chips
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatusFilter == null,
                    onClick = { selectedStatusFilter = null },
                    label = { Text("Tất cả") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GrabGreen,
                        selectedLabelColor = White
                    )
                )
                listOf(
                    OrderStatus.PENDING to "Chờ xác nhận",
                    OrderStatus.CONFIRMED to "Đã xác nhận",
                    OrderStatus.PREPARING to "Đang chuẩn bị",
                    OrderStatus.PICKING_UP to "Đang lấy hàng",
                    OrderStatus.DELIVERING to "Đang giao",
                    OrderStatus.DELIVERED to "Đã giao",
                    OrderStatus.CANCELLED to "Đã hủy"
                ).forEach { (status, label) ->
                    FilterChip(
                        selected = selectedStatusFilter == status,
                        onClick = {
                            selectedStatusFilter = if (selectedStatusFilter == status) null else status
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
                "${filteredOrders.size} đơn hàng",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GrabGreen)
                }
            } else if (filteredOrders.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📦", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Không có đơn hàng nào", color = Gray500)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredOrders, key = { it.id }) { order ->
                        AdminOrderCard(
                            order = order,
                            onClick = { onNavigateToOrderDetail(order.id) }
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun AdminOrderCard(
    order: Order,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi")) }
    val statusColor = when (order.status) {
        OrderStatus.DELIVERED -> SuccessGreen
        OrderStatus.CANCELLED -> ErrorRed
        OrderStatus.PENDING -> WarningYellow
        else -> InfoBlue
    }

    val statusText = when (order.status) {
        OrderStatus.PENDING -> "Chờ xác nhận"
        OrderStatus.CONFIRMED -> "Đã xác nhận"
        OrderStatus.PREPARING -> "Đang chuẩn bị"
        OrderStatus.PICKING_UP -> "Đang lấy hàng"
        OrderStatus.DELIVERING -> "Đang giao"
        OrderStatus.DELIVERED -> "Đã giao"
        OrderStatus.CANCELLED -> "Đã hủy"
        else -> order.status
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Gray200)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Đơn hàng #${order.id.take(8).uppercase()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
            Divider(color = Gray100)
            Spacer(Modifier.height(8.dp))

            Text(
                "Khách hàng: ID #${order.userId.take(8)}",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray700
            )

            Text(
                "Địa chỉ: ${order.deliveryAddress}",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    order.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray400
                )

                Text(
                    "Tổng tiền: ${String.format("%,d", order.totalAmount)}₫",
                    style = MaterialTheme.typography.titleMedium,
                    color = GrabGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
