package com.example.a43_kltn_ttfood.ui.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a43_kltn_ttfood.data.model.*
import com.example.a43_kltn_ttfood.data.repository.VoucherRepository
import com.example.a43_kltn_ttfood.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// =====================================================
// 🎫 QUẢN LÝ VOUCHER (ADMIN)
// =====================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminVouchersScreen(
    onNavigateBack: () -> Unit
) {
    val voucherRepo = remember { VoucherRepository() }
    val scope = rememberCoroutineScope()
    var vouchers by remember { mutableStateOf<List<Voucher>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        voucherRepo.getAllVouchers().collect { result ->
            vouchers = result
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voucher & Khuyến mãi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, "Tạo mới", tint = GrabGreen)
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
            // Counter
            Text(
                "${vouchers.size} voucher",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GrabGreen)
                }
            } else if (vouchers.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎫", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Chưa có voucher nào", color = Gray500)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Tạo voucher đầu tiên")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(vouchers, key = { it.id }) { voucher ->
                        VoucherAdminCard(
                            voucher = voucher,
                            onToggleActive = {
                                scope.launch {
                                    voucherRepo.toggleVoucherActive(
                                        voucherId = voucher.id,
                                        isActive = !voucher.isActive
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

    // Create Voucher Dialog
    if (showCreateDialog) {
        CreateVoucherDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { voucher ->
                scope.launch {
                    voucherRepo.createVoucher(
                        voucher = voucher,
                        adminId = "admin",
                        adminName = "Admin"
                    )
                    showCreateDialog = false
                }
            }
        )
    }
}

@Composable
private fun VoucherAdminCard(
    voucher: Voucher,
    onToggleActive: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("vi")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (voucher.isActive)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            1.dp,
            if (voucher.isActive) GrabGreen.copy(alpha = 0.3f) else Gray200
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎫", fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        voucher.code,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (voucher.isActive) GrabGreen else Gray400
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (voucher.isActive) "Đang hoạt động" else "Đã tắt",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (voucher.isActive) SuccessGreen else Gray400,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(
                                (if (voucher.isActive) SuccessGreen else Gray400).copy(alpha = 0.1f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Switch(
                        checked = voucher.isActive,
                        onCheckedChange = { onToggleActive() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = White,
                            checkedTrackColor = SuccessGreen
                        )
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Divider(color = Gray100)
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Giảm giá", style = MaterialTheme.typography.bodySmall, color = Gray500)
                    Text(
                        if (voucher.discountType == "percent") "${voucher.discountValue}%"
                        else "${String.format("%,d", voucher.discountValue)}₫",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = GrabGreen
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Đơn tối thiểu", style = MaterialTheme.typography.bodySmall, color = Gray500)
                    Text(
                        "${String.format("%,d", voucher.minOrder)}₫",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Đã dùng", style = MaterialTheme.typography.bodySmall, color = Gray500)
                    Text(
                        "${voucher.usedCount}/${voucher.usageLimit}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (voucher.expiresAt != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Hết hạn: ${dateFormat.format(voucher.expiresAt.toDate())}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray400
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateVoucherDialog(
    onDismiss: () -> Unit,
    onCreate: (Voucher) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var discountType by remember { mutableStateOf("percent") }
    var discountValue by remember { mutableStateOf("") }
    var minOrder by remember { mutableStateOf("") }
    var maxDiscount by remember { mutableStateOf("") }
    var usageLimit by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tạo Voucher mới", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text("Mã voucher") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Discount type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = discountType == "percent",
                        onClick = { discountType = "percent" },
                        label = { Text("Phần trăm (%)") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GrabGreen,
                            selectedLabelColor = White
                        )
                    )
                    FilterChip(
                        selected = discountType == "fixed",
                        onClick = { discountType = "fixed" },
                        label = { Text("Số tiền (₫)") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GrabGreen,
                            selectedLabelColor = White
                        )
                    )
                }

                OutlinedTextField(
                    value = discountValue,
                    onValueChange = { discountValue = it.filter { c -> c.isDigit() } },
                    label = { Text(if (discountType == "percent") "Giảm (%)" else "Giảm (₫)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = minOrder,
                    onValueChange = { minOrder = it.filter { c -> c.isDigit() } },
                    label = { Text("Đơn tối thiểu (₫)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp)
                )

                if (discountType == "percent") {
                    OutlinedTextField(
                        value = maxDiscount,
                        onValueChange = { maxDiscount = it.filter { c -> c.isDigit() } },
                        label = { Text("Giảm tối đa (₫)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = usageLimit,
                    onValueChange = { usageLimit = it.filter { c -> c.isDigit() } },
                    label = { Text("Giới hạn sử dụng") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.isNotBlank() && discountValue.isNotBlank()) {
                        val voucher = Voucher(
                            code = code,
                            discountType = discountType,
                            discountValue = discountValue.toIntOrNull() ?: 0,
                            minOrder = minOrder.toIntOrNull() ?: 0,
                            maxDiscount = maxDiscount.toIntOrNull() ?: 0,
                            usageLimit = usageLimit.toIntOrNull() ?: 100,
                            isActive = true
                        )
                        onCreate(voucher)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                shape = RoundedCornerShape(10.dp),
                enabled = code.isNotBlank() && discountValue.isNotBlank()
            ) {
                Text("Tạo voucher")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
