package com.example.a43_kltn_ttfood.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a43_kltn_ttfood.data.model.Order
import com.example.a43_kltn_ttfood.data.model.OrderStatus
import com.example.a43_kltn_ttfood.data.repository.OrderRepository
import com.example.a43_kltn_ttfood.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTracking: (String) -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    val orderRepo = remember { OrderRepository() }

    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            orderRepo.getOrdersByUser(userId).collect { fetchedOrders ->
                orders = fetchedOrders
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = Gray50,
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử đơn hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GrabGreen)
            }
        } else if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(64.dp), tint = Gray300)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Bạn chưa có đơn hàng nào", style = MaterialTheme.typography.titleMedium, color = Gray600)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    OrderItemCard(order = order, onClick = { onNavigateToTracking(order.id) })
                }
            }
        }
    }
}

@Composable
fun OrderItemCard(order: Order, onClick: () -> Unit = {}) {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateString = order.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "Đang xử lý..."

    val statusColor = when (order.status) {
        OrderStatus.PENDING -> WarningYellow
        OrderStatus.CONFIRMED, OrderStatus.PREPARING, OrderStatus.PICKING_UP, OrderStatus.DELIVERING -> InfoBlue
        OrderStatus.DELIVERED -> SuccessGreen
        OrderStatus.CANCELLED -> ErrorRed
        else -> Gray500
    }

    val statusText = when (order.status) {
        OrderStatus.PENDING -> "Chờ xác nhận"
        OrderStatus.CONFIRMED -> "Đã xác nhận"
        OrderStatus.PREPARING -> "Đang chuẩn bị"
        OrderStatus.PICKING_UP -> "Đang lấy hàng"
        OrderStatus.DELIVERING -> "Đang giao hàng"
        OrderStatus.DELIVERED -> "Giao thành công"
        OrderStatus.CANCELLED -> "Đã hủy"
        else -> "Không rõ"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đơn #${order.id.takeLast(6).uppercase()}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Gray900
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(statusText, style = MaterialTheme.typography.labelMedium, color = statusColor, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(dateString, style = MaterialTheme.typography.bodySmall, color = Gray500)
            
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Gray100)

            // Items
            order.items.take(2).forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${item.quantity}x ${item.foodName}", style = MaterialTheme.typography.bodyMedium, color = Gray800, modifier = Modifier.weight(1f))
                    Text("${formatter.format(item.price * item.quantity)}đ", style = MaterialTheme.typography.bodyMedium, color = Gray600)
                }
            }
            if (order.items.size > 2) {
                Text("... và ${order.items.size - 2} món khác", style = MaterialTheme.typography.bodySmall, color = Gray500)
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Gray100)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Tổng tiền", style = MaterialTheme.typography.bodyMedium, color = Gray600)
                Text("${formatter.format(order.totalAmount)}đ", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = GrabGreen)
            }
        }
    }
}
