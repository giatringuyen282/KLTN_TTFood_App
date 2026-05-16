package com.example.a43_kltn_ttfood.ui.screens.food

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a43_kltn_ttfood.data.model.sampleFoodItems
import com.example.a43_kltn_ttfood.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailScreen(
    foodId: Int,
    onNavigateBack: () -> Unit = {}
) {
    val food = remember {
        sampleFoodItems.find { it.id == foodId } ?: sampleFoodItems.first()
    }
    var isFavorite by remember { mutableStateOf(false) }
    var quantity by remember { mutableIntStateOf(1) }

    Scaffold(
        containerColor = Gray50,
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại", tint = Gray700)
                    }
                },
                actions = {
                    IconButton(onClick = { isFavorite = !isFavorite }) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            "Yêu thích",
                            tint = if (isFavorite) ErrorRed else Gray500
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = food.bgColor)
            )
        },
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 16.dp, color = White) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(Gray100)
                        ) { Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gray700) }
                        Text("$quantity", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        IconButton(
                            onClick = { quantity++ },
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(Orange100)
                        ) { Icon(Icons.Default.Add, "Tăng", tint = Orange500, modifier = Modifier.size(18.dp)) }
                    }
                    Button(
                        onClick = { },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)
                    ) {
                        Text("Thêm vào giỏ · ${food.price}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = White)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(250.dp).background(food.bgColor),
                contentAlignment = Alignment.Center
            ) { Text(food.emoji, fontSize = 120.sp) }

            Card(
                modifier = Modifier.fillMaxWidth().offset(y = (-20).dp).padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text(food.name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f, false))
                        Text(food.price, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Orange500)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(food.restaurant, style = MaterialTheme.typography.bodyMedium, color = Gray500)
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("⭐", fontSize = 24.sp); Text("${food.rating}", fontWeight = FontWeight.Bold); Text("Đánh giá", style = MaterialTheme.typography.labelSmall, color = Gray500) }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("🚚", fontSize = 24.sp); Text("Miễn phí", fontWeight = FontWeight.Bold); Text("Giao hàng", style = MaterialTheme.typography.labelSmall, color = Gray500) }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("⏱️", fontSize = 24.sp); Text("15-20p", fontWeight = FontWeight.Bold); Text("Thời gian", style = MaterialTheme.typography.labelSmall, color = Gray500) }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("📝 Mô tả", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Món ăn được chế biến từ nguyên liệu tươi ngon, đảm bảo vệ sinh an toàn thực phẩm. Hương vị đậm đà, phù hợp khẩu vị người Việt.",
                        style = MaterialTheme.typography.bodyMedium, color = Gray600, lineHeight = 22.sp
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
