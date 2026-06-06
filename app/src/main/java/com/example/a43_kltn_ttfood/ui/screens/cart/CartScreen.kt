package com.example.a43_kltn_ttfood.ui.screens.cart

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a43_kltn_ttfood.data.model.CartItem
import com.example.a43_kltn_ttfood.data.repository.CartRepository
import com.example.a43_kltn_ttfood.data.repository.AuthRepository
import com.example.a43_kltn_ttfood.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CartScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToCheckout: () -> Unit = {}
) {
    val cartRepo = remember { CartRepository() }
    val authRepo = remember { AuthRepository() }
    val coroutineScope = rememberCoroutineScope()

    val userId = authRepo.currentFirebaseUser?.uid ?: ""
    val cartItems by cartRepo.getCart(userId).collectAsState(initial = emptyList())

    var voucherCode by remember { mutableStateOf("") }
    var appliedVoucher by remember { mutableStateOf<String?>(null) }
    var voucherError by remember { mutableStateOf(false) }

    var restaurantNote by remember { mutableStateOf("") }
    var shipperNote by remember { mutableStateOf("") }

    // Calculations
    val subtotal = cartItems.sumOf { (it.price * it.quantity).toInt() }
    val deliveryFee = if (subtotal > 200000 || subtotal == 0) 0 else 15000
    val discount = if (appliedVoucher == "TTFOOD50") 50000 else 0
    val total = (subtotal + deliveryFee - discount).coerceAtLeast(0)

    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    fun formatPrice(price: Int) = "${formatter.format(price)}đ"

    Scaffold(
        containerColor = Gray50,
        topBar = {
            TopAppBar(
                title = { Text("Giỏ hàng của bạn", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                },
                actions = {
                    if (cartItems.isNotEmpty()) {
                        TextButton(onClick = { coroutineScope.launch { cartRepo.clearCart(userId) } }) {
                            Text("Xóa tất cả", color = ErrorRed, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
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
                                text = formatPrice(total),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Orange500
                            )
                        }
                        Button(
                            onClick = onNavigateToCheckout,
                            modifier = Modifier
                                .height(56.dp)
                                .weight(1f)
                                .padding(start = 24.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Orange500)
                        ) {
                            Text("Đặt hàng (${cartItems.sumOf { it.quantity }})", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "🛒", fontSize = 80.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Giỏ hàng đang trống", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Hãy khám phá thêm các món ăn ngon nhé!", color = Gray500)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tiếp tục tìm món")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                val groupedItems = cartItems.groupBy { it.restaurantName }

                groupedItems.forEach { (restaurantName, restaurantItems) ->
                    item {
                        Text(
                            text = "🏪 $restaurantName",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(White)
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        )
                        Divider(color = Gray100)
                    }

                    items(items = restaurantItems, key = { cartItem: CartItem -> cartItem.id }) { item: CartItem ->
                        CartItemRow(
                            item = item,
                            modifier = Modifier,
                            onIncrease = {
                                coroutineScope.launch {
                                    cartRepo.updateCartItemQuantity(item.id, item.quantity + 1)
                                }
                            },
                            onDecrease = {
                                coroutineScope.launch {
                                    cartRepo.updateCartItemQuantity(item.id, item.quantity - 1)
                                }
                            },
                            onDelete = {
                                coroutineScope.launch {
                                    cartRepo.deleteCartItem(item.id)
                                }
                            }
                        )
                        Divider(color = Gray100)
                    }
                    
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }

                // Vouchers Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(White)
                            .padding(20.dp)
                    ) {
                        Text("🏷️ Khuyến mãi", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = voucherCode,
                                onValueChange = { 
                                    voucherCode = it
                                    voucherError = false
                                },
                                placeholder = { Text("Nhập mã giảm giá") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Orange500,
                                    unfocusedBorderColor = Gray300
                                ),
                                trailingIcon = {
                                    if (appliedVoucher != null) {
                                        Icon(Icons.Default.CheckCircle, "Đã áp dụng", tint = SuccessGreen)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    if (voucherCode.uppercase() == "TTFOOD50") {
                                        appliedVoucher = "TTFOOD50"
                                        voucherError = false
                                    } else {
                                        appliedVoucher = null
                                        voucherError = true
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Gray900),
                                modifier = Modifier.height(56.dp)
                            ) {
                                Text("Áp dụng")
                            }
                        }
                        if (voucherError) {
                            Text("Mã giảm giá không hợp lệ hoặc đã hết hạn", color = ErrorRed, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
                        } else if (appliedVoucher != null) {
                            Text("Đã áp dụng mã giảm giá thành công!", color = SuccessGreen, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Order Notes Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(White)
                            .padding(20.dp)
                    ) {
                        Text("📝 Ghi chú đơn hàng", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Dành cho nhà hàng", style = MaterialTheme.typography.bodySmall, color = Gray600)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = restaurantNote,
                            onValueChange = { if (it.length <= 200) restaurantNote = it },
                            placeholder = { Text("Ví dụ: Không hành, ít cay...") },
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Orange500)
                        )
                        Text("${restaurantNote.length}/200", style = MaterialTheme.typography.labelSmall, color = Gray500, modifier = Modifier.align(Alignment.End).padding(top = 4.dp))

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Dành cho tài xế", style = MaterialTheme.typography.bodySmall, color = Gray600)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = shipperNote,
                            onValueChange = { if (it.length <= 200) shipperNote = it },
                            placeholder = { Text("Ví dụ: Tới cổng gọi điện...") },
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Orange500)
                        )
                        Text("${shipperNote.length}/200", style = MaterialTheme.typography.labelSmall, color = Gray500, modifier = Modifier.align(Alignment.End).padding(top = 4.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Order Summary
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(White)
                            .padding(20.dp)
                    ) {
                        Text("💰 Tổng cộng", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        SummaryRow("Tạm tính", formatPrice(subtotal))
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        SummaryRow(
                            "Phí giao hàng", 
                            if (deliveryFee == 0) "Miễn phí" else formatPrice(deliveryFee),
                            valueColor = if (deliveryFee == 0) SuccessGreen else Gray900
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (discount > 0) {
                            SummaryRow("Giảm giá voucher", "-${formatPrice(discount)}", valueColor = SuccessGreen)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        
                        Divider(color = Gray200)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tổng cộng", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(formatPrice(total), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Orange500)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, valueColor: Color = Gray900) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Gray600)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = valueColor)
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    modifier: Modifier = Modifier,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    val priceFormatted = "${formatter.format(item.price)}đ"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(White)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(item.foodBgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(text = item.foodEmoji, fontSize = 40.sp)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.foodName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (item.toppings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = item.toppings, style = MaterialTheme.typography.labelSmall, color = Gray500)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = priceFormatted,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Orange500
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Delete, "Xóa", tint = ErrorRed, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Gray50)
                    .border(1.dp, Gray200, RoundedCornerShape(8.dp))
            ) {
                Box(
                    modifier = Modifier.size(28.dp).clickable(onClick = onDecrease),
                    contentAlignment = Alignment.Center
                ) {
                    Text("−", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (item.quantity > 1) Gray900 else Gray400)
                }
                Text(
                    text = "${item.quantity}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Box(
                    modifier = Modifier.size(28.dp).clickable(onClick = onIncrease),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Orange500)
                }
            }
        }
    }
}
