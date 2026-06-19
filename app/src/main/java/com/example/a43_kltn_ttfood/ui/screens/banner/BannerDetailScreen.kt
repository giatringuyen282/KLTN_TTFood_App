package com.example.a43_kltn_ttfood.ui.screens.banner

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.a43_kltn_ttfood.data.model.Banner
import com.example.a43_kltn_ttfood.data.model.FoodItem
import com.example.a43_kltn_ttfood.data.model.Voucher
import com.example.a43_kltn_ttfood.data.model.sampleBanners
import com.example.a43_kltn_ttfood.data.repository.FoodRepository
import com.example.a43_kltn_ttfood.data.repository.VoucherRepository
import com.example.a43_kltn_ttfood.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BannerDetailScreen(
    bannerId: Int,
    onNavigateBack: () -> Unit = {},
    onNavigateToFood: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val banner = remember(bannerId) {
        sampleBanners.find { it.id == bannerId } ?: sampleBanners.first()
    }

    val foodRepo = remember { FoodRepository() }
    val voucherRepo = remember { VoucherRepository() }

    var foods by remember { mutableStateOf<List<FoodItem>>(emptyList()) }
    var vouchers by remember { mutableStateOf<List<Voucher>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            foodRepo.getAllFoodItems().collect { foods = it }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(Unit) {
        try {
            voucherRepo.getAllVouchers().collect { vouchers = it.filter { v -> v.isActive } }
            isLoading = false
        } catch (e: Exception) {
            e.printStackTrace()
            isLoading = false
        }
    }

    // Filter foods based on banner
    val filteredFoods = remember(foods, bannerId) {
        when (bannerId) {
            1 -> foods.filter { it.price <= 70000 && (it.name.lowercase().contains("gà") || it.name.lowercase().contains("burger") || it.restaurant.lowercase().contains("kfc")) } // Burger & Gà Rán khuyến mãi
            2 -> foods.filter { it.name.lowercase().contains("pizza") || it.restaurant.lowercase().contains("pizza") } // Pizza khuyến mãi
            3 -> foods.filter { it.price >= 50000 } // Freeship đơn từ 50K
            4 -> foods.filter { it.price <= 60000 && (it.name.lowercase().contains("phở") || it.name.lowercase().contains("bún") || it.name.lowercase().contains("trà")) } // Đêm khuya giảm 30%
            else -> foods
        }
    }

    Scaffold(
        containerColor = Gray50,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = banner.title.replace("🔥", "").replace("🎉", "").replace("🚀", "").replace("🌙", "").trim(),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Banner Header Info Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .height(160.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Background image
                    if (banner.imageResId != 0) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = banner.imageResId),
                            contentDescription = banner.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(banner.colorStart, banner.colorEnd)
                                    )
                                )
                        )
                    }
                    // Gradient scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.55f),
                                        Color.Black.copy(alpha = 0.1f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    // Text content
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = banner.emoji, fontSize = 48.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = banner.title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = banner.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // 2. Vouchers Horizontal Row
            if (vouchers.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "🎟️ Voucher Khuyến Mãi",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(vouchers) { voucher ->
                                VoucherDetailCard(
                                    voucher = voucher,
                                    onCopy = { code ->
                                        clipboardManager.setText(AnnotatedString(code))
                                        Toast.makeText(context, "Đã sao chép mã: $code", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 3. Section Title for Foods
            item {
                Text(
                    text = "🍔 Danh Sách Món Ăn Áp Dụng",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp)
                )
            }

            // 4. Food Items Grid/List
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Orange500)
                    }
                }
            } else if (filteredFoods.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Không tìm thấy món ăn nào phù hợp",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray500
                        )
                    }
                }
            } else {
                items(filteredFoods) { food ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clickable { onNavigateToFood(food.id) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(food.bgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                if (food.imageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = food.imageUrl,
                                        contentDescription = food.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(text = food.emoji, fontSize = 36.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = food.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = food.restaurant,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gray500
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = food.formattedPrice,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Orange500
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = WarningYellow,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "${food.rating}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Gray600
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                // Thêm khoảng đệm ở dưới cùng
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun VoucherDetailCard(
    voucher: Voucher,
    onCopy: (String) -> Unit
) {
    val formatter = java.text.DecimalFormat("#,###")
    val description = remember(voucher) {
        if (voucher.discountType == "fixed") {
            "Giảm ${formatter.format(voucher.discountValue)}đ cho đơn hàng từ ${formatter.format(voucher.minOrder)}đ"
        } else {
            "Giảm ${voucher.discountValue}% (tối đa ${formatter.format(voucher.maxDiscount)}đ) đơn từ ${formatter.format(voucher.minOrder)}đ"
        }
    }

    Card(
        modifier = Modifier
            .width(260.dp)
            .height(100.dp)
            .border(
                width = 1.dp,
                color = Orange100,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Orange50.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = voucher.code,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Orange500
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { onCopy(voucher.code) },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Orange500)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
