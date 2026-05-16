package com.example.a43_kltn_ttfood.ui.screens.checkout

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a43_kltn_ttfood.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToSuccess: () -> Unit = {}
) {
    var selectedTime by remember { mutableStateOf("Giao ngay") }
    var selectedPayment by remember { mutableStateOf("COD") }
    
    var currentAddress by remember { mutableStateOf("123 Nguyễn Văn Cừ, Phường 4, Quận 5, TP.HCM") }
    var showAddressDialog by remember { mutableStateOf(false) }
    var tempAddress by remember { mutableStateOf("") }
    
    var scheduledTime by remember { mutableStateOf("Hôm nay, 19:00") }
    var showTimeDialog by remember { mutableStateOf(false) }
    
    var isProcessing by remember { mutableStateOf(false) }

    if (isProcessing) {
        // Full screen loading
        Box(modifier = Modifier.fillMaxSize().background(White), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Orange500, modifier = Modifier.size(64.dp), strokeWidth = 6.dp)
                Spacer(Modifier.height(24.dp))
                Text("Đang xử lý thanh toán...", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Gray900)
                Spacer(Modifier.height(8.dp))
                Text("Vui lòng không đóng ứng dụng", style = MaterialTheme.typography.bodyMedium, color = Gray500)
            }
        }
        
        LaunchedEffect(Unit) {
            delay(2000)
            onNavigateToSuccess()
        }
        return
    }

    Scaffold(
        containerColor = Gray50,
        topBar = {
            TopAppBar(
                title = { Text("Thanh toán", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        bottomBar = {
            Surface(
                color = White,
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Tổng cộng", style = MaterialTheme.typography.bodyMedium, color = Gray600)
                        Text(
                            text = "215.000đ",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Orange500
                        )
                    }
                    Button(
                        onClick = { isProcessing = true },
                        modifier = Modifier
                            .height(56.dp)
                            .weight(1f)
                            .padding(start = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Orange500)
                    ) {
                        Text("Đặt đơn", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Address Section
            item {
                Text("📍 Địa chỉ giao hàng", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Map Preview Placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Gray200),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🗺️ Bản đồ Preview", color = Gray500)
                            Icon(Icons.Default.LocationOn, "Vị trí", tint = ErrorRed, modifier = Modifier.size(40.dp))
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, "Vị trí", tint = Orange500, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Nhà riêng", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text(currentAddress, style = MaterialTheme.typography.bodySmall, color = Gray600)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, "Người nhận", tint = Orange500, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Nguyễn Văn A - 0901234567", style = MaterialTheme.typography.bodySmall, color = Gray900, fontWeight = FontWeight.SemiBold)
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Gray100)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(onClick = { /* Mở GPS */ }) {
                                Text("📍 Định vị hiện tại", color = InfoBlue)
                            }
                            TextButton(onClick = { 
                                tempAddress = currentAddress
                                showAddressDialog = true 
                            }) {
                                Text("➕ Thêm địa chỉ mới", color = Orange500)
                            }
                        }
                    }
                }
            }

            // Time Section
            item {
                Text("🕐 Thời gian giao hàng", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedTime == "Giao ngay",
                                    onClick = { selectedTime = "Giao ngay" },
                                    colors = RadioButtonDefaults.colors(selectedColor = Orange500)
                                )
                                Column {
                                    Text("Giao ngay (20 - 30 phút)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                    Text("Đơn hàng sẽ được giao ngay lập tức", style = MaterialTheme.typography.labelSmall, color = Gray500)
                                }
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Gray50)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedTime == "Đặt lịch",
                                    onClick = { selectedTime = "Đặt lịch" },
                                    colors = RadioButtonDefaults.colors(selectedColor = Orange500)
                                )
                                Column {
                                    Text("Đặt lịch trước", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                    Text("Chọn ngày và giờ giao hàng", style = MaterialTheme.typography.labelSmall, color = Gray500)
                                }
                            }
                        }
                        
                        AnimatedVisibility(visible = selectedTime == "Đặt lịch") {
                            OutlinedButton(
                                onClick = { showTimeDialog = true },
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Chọn thời gian: $scheduledTime", color = Gray900)
                            }
                        }
                    }
                }
            }

            // Payment Section
            item {
                Text("💳 Phương thức thanh toán", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        PaymentOptionRow("COD", "Tiền mặt khi nhận hàng", "💵", selectedPayment) { selectedPayment = it }
                        PaymentOptionRow("MoMo", "Ví điện tử MoMo", "💜", selectedPayment) { selectedPayment = it }
                        PaymentOptionRow("ZaloPay", "Ví điện tử ZaloPay", "🔵", selectedPayment) { selectedPayment = it }
                        PaymentOptionRow("VNPay", "Quét mã VNPay QR", "🔴", selectedPayment) { selectedPayment = it }
                        PaymentOptionRow("Card", "Thẻ tín dụng / Ghi nợ", "💳", selectedPayment) { selectedPayment = it }
                    }
                }
            }
            
            // Bottom Padding Space
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Address Dialog
    if (showAddressDialog) {
        AlertDialog(
            onDismissRequest = { showAddressDialog = false },
            title = { Text(text = "Thêm địa chỉ mới", style = MaterialTheme.typography.titleMedium) },
            text = {
                OutlinedTextField(
                    value = tempAddress,
                    onValueChange = { tempAddress = it },
                    placeholder = { Text("Nhập địa chỉ giao hàng...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Orange500)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (tempAddress.isNotBlank()) currentAddress = tempAddress
                    showAddressDialog = false
                }) { Text("Lưu", color = Orange500) }
            },
            dismissButton = {
                TextButton(onClick = { showAddressDialog = false }) { Text("Hủy", color = Gray500) }
            }
        )
    }

    // Time Selection Dialog
    if (showTimeDialog) {
        val availableTimes = listOf("Hôm nay, 18:30", "Hôm nay, 19:00", "Hôm nay, 19:30", "Ngày mai, 11:00", "Ngày mai, 12:00")
        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            title = { Text(text = "Chọn thời gian giao hàng", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column {
                    availableTimes.forEach { time ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scheduledTime = time
                                    showTimeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = scheduledTime == time,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = Orange500)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(time, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimeDialog = false }) { Text("Đóng", color = Gray500) }
            }
        )
    }
}

@Composable
fun PaymentOptionRow(id: String, name: String, icon: String, selectedId: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(id) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Gray100),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f))
        RadioButton(
            selected = selectedId == id,
            onClick = { onSelect(id) },
            colors = RadioButtonDefaults.colors(selectedColor = Orange500)
        )
    }
}
