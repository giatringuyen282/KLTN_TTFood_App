package com.example.a43_kltn_ttfood.ui.screens.admin

import androidx.compose.animation.*
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
import com.example.a43_kltn_ttfood.data.repository.OrderRepository
import com.example.a43_kltn_ttfood.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// =====================================================
// 📝 CHI TIẾT ĐƠN HÀNG (ADMIN)
// =====================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderDetailScreen(
    orderId: String,
    onNavigateBack: () -> Unit
) {
    val orderRepo = remember { OrderRepository() }
    val scope = rememberCoroutineScope()

    var order by remember { mutableStateOf<Order?>(null) }
    var orderItems by remember { mutableStateOf<List<OrderItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isUpdatingStatus by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("vi")) }

    LaunchedEffect(orderId) {
        order = orderRepo.getOrderById(orderId)
        orderRepo.getOrderItems(orderId).collect {
            orderItems = it
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết đơn hàng", fontWeight = FontWeight.Bold) },
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
        if (isLoading || order == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GrabGreen)
            }
            return@Scaffold
        }

        val ord = order!!

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // 1. Trạng thái & Action cập nhật
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Gray200),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Trạng thái hiện tại",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray500
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val statusColor = when (ord.status) {
                                OrderStatus.DELIVERED -> SuccessGreen
                                OrderStatus.CANCELLED -> ErrorRed
                                OrderStatus.PENDING -> WarningYellow
                                else -> InfoBlue
                            }
                            val statusText = when (ord.status) {
                                OrderStatus.PENDING -> "Chờ xác nhận"
                                OrderStatus.CONFIRMED -> "Đã xác nhận"
                                OrderStatus.PREPARING -> "Đang chuẩn bị"
                                OrderStatus.PICKING_UP -> "Đang lấy hàng"
                                OrderStatus.DELIVERING -> "Đang giao"
                                OrderStatus.DELIVERED -> "Đã giao"
                                OrderStatus.CANCELLED -> "Đã hủy"
                                else -> ord.status
                            }

                            Text(
                                statusText.uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )

                            // Dropdown đổi trạng thái
                            var showStatusMenu by remember { mutableStateOf(false) }
                            Box {
                                Button(
                                    onClick = { showStatusMenu = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    enabled = !isUpdatingStatus
                                ) {
                                    if (isUpdatingStatus) {
                                        CircularProgressIndicator(color = White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    } else {
                                        Text("Cập nhật", fontSize = 14.sp)
                                        Spacer(Modifier.width(4.dp))
                                        Icon(Icons.Default.ArrowDropDown, null)
                                    }
                                }

                                DropdownMenu(
                                    expanded = showStatusMenu,
                                    onDismissRequest = { showStatusMenu = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                ) {
                                    listOf(
                                        OrderStatus.PENDING to "Chờ xác nhận",
                                        OrderStatus.CONFIRMED to "Xác nhận đơn",
                                        OrderStatus.PREPARING to "Đang chuẩn bị",
                                        OrderStatus.PICKING_UP to "Đang lấy hàng",
                                        OrderStatus.DELIVERING to "Bắt đầu giao",
                                        OrderStatus.DELIVERED to "Hoàn thành (Đã giao)",
                                        OrderStatus.CANCELLED to "Hủy đơn"
                                    ).forEach { (status, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                showStatusMenu = false
                                                isUpdatingStatus = true
                                                scope.launch {
                                                    orderRepo.updateOrderStatus(
                                                        orderId = ord.id,
                                                        status = status,
                                                        adminId = "admin",
                                                        adminName = "Admin"
                                                    )
                                                    order = orderRepo.getOrderById(ord.id)
                                                    isUpdatingStatus = false
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Thông tin chung
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Gray200),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Thông tin chung", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Divider(color = Gray100)

                        DetailRow("Mã đơn hàng", ord.id.uppercase())
                        DetailRow("Thời gian đặt", ord.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "N/A")
                        DetailRow("ID Khách hàng", ord.userId)
                        DetailRow("Phương thức thanh toán", ord.paymentMethod.uppercase())
                        DetailRow("Trạng thái thanh toán", ord.paymentStatus.uppercase())
                        DetailRow("Ghi chú", ord.note.ifBlank { "Không có" })
                    }
                }
            }

            // 3. Địa chỉ giao hàng
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Gray200),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocationOn, null, tint = GrabGreen)
                            Spacer(Modifier.width(8.dp))
                            Text("Địa chỉ giao hàng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(ord.deliveryAddress, style = MaterialTheme.typography.bodyMedium, color = Gray700)
                    }
                }
            }

            // 4. Danh sách món ăn
            item {
                Text("Món ăn đã đặt", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (orderItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Không có món ăn trong đơn này", color = Gray500)
                    }
                }
            } else {
                items(orderItems) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        item.foodName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (item.note.isNotBlank()) {
                                        Text(
                                            "Lưu ý: ${item.note}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Gray500
                                        )
                                    }
                                }
                                Text(
                                    "x${item.quantity}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GrabGreen
                                )
                            }

                            // Toppings list
                            if (item.toppings.isNotEmpty()) {
                                Spacer(Modifier.height(6.dp))
                                item.toppings.forEach { topping ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "- ${topping.groupName}: ${topping.optionName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Gray600
                                        )
                                        if (topping.price > 0) {
                                            Text(
                                                "+${String.format("%,d", topping.price)}₫",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Gray600
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    "Đơn giá: ${String.format("%,d", item.unitPrice)}₫",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gray500
                                )
                            }
                        }
                    }
                }
            }

            // 5. Chi tiết thanh toán
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Gray200),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Chi tiết thanh toán", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Divider(color = Gray100)

                        DetailRow("Tạm tính", "${String.format("%,d", ord.subtotal)}₫")
                        DetailRow("Phí vận chuyển", "${String.format("%,d", ord.deliveryFee)}₫")
                        DetailRow("Giảm giá voucher", "-${String.format("%,d", ord.discount)}₫")

                        Divider(color = Gray100)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Tổng tiền", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "${String.format("%,d", ord.totalAmount)}₫",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = GrabGreen
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Gray600)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
