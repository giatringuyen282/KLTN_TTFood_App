package com.example.a43_kltn_ttfood.ui.screens.food

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.a43_kltn_ttfood.data.model.FoodItem
import com.example.a43_kltn_ttfood.data.model.UserRole
import com.example.a43_kltn_ttfood.data.model.sampleFoodItems
import com.example.a43_kltn_ttfood.data.repository.AuthRepository
import com.example.a43_kltn_ttfood.data.repository.FoodRepository
import com.example.a43_kltn_ttfood.data.repository.CartRepository
import com.example.a43_kltn_ttfood.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailScreen(
    foodId: Int,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val foodRepo = remember { FoodRepository() }
    val authRepo = remember { AuthRepository() }
    val cartRepo = remember { CartRepository() }
    val scope = rememberCoroutineScope()

    // Trạng thái dữ liệu
    var food by remember { mutableStateOf<FoodItem?>(null) }
    var isAdmin by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }
    var quantity by remember { mutableIntStateOf(1) }
    var isUploading by remember { mutableStateOf(false) }
    var currentImageUrl by remember { mutableStateOf("") }

    // Tải thông tin món ăn và vai trò người dùng
    LaunchedEffect(foodId) {
        // Lấy thông tin món ăn từ Firestore, fallback về sample nếu chưa sync
        food = foodRepo.getFoodById(foodId)
            ?: sampleFoodItems.find { it.id == foodId }
            ?: sampleFoodItems.first()
        currentImageUrl = food?.imageUrl ?: ""

        // Kiểm tra xem tài khoản hiện tại có phải Admin không
        val userProfile = authRepo.getCurrentUserProfile()
        isAdmin = userProfile?.role == UserRole.ADMIN
    }

    // Launcher để chọn ảnh từ thư viện thiết bị
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && food != null) {
            scope.launch {
                isUploading = true
                val result = foodRepo.uploadFoodImage(food!!.id, uri)
                result.fold(
                    onSuccess = { downloadUrl ->
                        val updateResult = foodRepo.updateFoodImageUrl(food!!.id, downloadUrl)
                        updateResult.fold(
                            onSuccess = {
                                currentImageUrl = downloadUrl
                                Toast.makeText(context, "✅ Đã cập nhật ảnh món ăn!", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { e ->
                                Toast.makeText(context, "❌ Lỗi cập nhật Firestore: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    onFailure = { e ->
                        Toast.makeText(context, "❌ Lỗi upload ảnh: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                )
                isUploading = false
            }
        }
    }

    // Hiển thị loading khi chưa có dữ liệu
    if (food == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Orange500)
        }
        return
    }

    val foodData = food!!

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = foodData.bgColor)
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
                        onClick = {
                            val uid = authRepo.currentFirebaseUser?.uid
                            if (uid != null) {
                                scope.launch {
                                    val result = cartRepo.addToCart(
                                        userId = uid,
                                        food = foodData,
                                        quantity = quantity,
                                        toppings = ""
                                    )
                                    result.fold(
                                        onSuccess = {
                                            Toast.makeText(context, "✅ Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show()
                                        },
                                        onFailure = { e ->
                                            Toast.makeText(context, "❌ Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            } else {
                                Toast.makeText(context, "Vui lòng đăng nhập để đặt hàng", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)
                    ) {
                        Text(
                            "Thêm vào giỏ · ${foodData.price}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = White
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // ────────────────────────────────────────────────────────
            // Phần ảnh món ăn – Admin có thể nhấn để đổi ảnh
            // ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(foodData.bgColor),
                contentAlignment = Alignment.Center
            ) {
                // Hiển thị ảnh thực nếu có URL, nếu không dùng emoji
                if (currentImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = currentImageUrl,
                        contentDescription = foodData.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(foodData.emoji, fontSize = 120.sp)
                }

                // Hiển thị trạng thái đang upload
                if (isUploading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = White)
                            Spacer(Modifier.height(8.dp))
                            Text("Đang tải ảnh lên...", color = White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Nút "Đổi ảnh" chỉ hiện với Admin và khi không đang upload
                if (isAdmin && !isUploading) {
                    FloatingActionButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .size(48.dp),
                        containerColor = White.copy(alpha = 0.9f),
                        contentColor = Orange500,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, "Đổi ảnh", modifier = Modifier.size(24.dp))
                    }
                }
            }

            // ────────────────────────────────────────────────────────
            // Card thông tin
            // ────────────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-20).dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text(
                            foodData.name,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f, false)
                        )
                        Text(
                            foodData.price,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Orange500
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(foodData.restaurant, style = MaterialTheme.typography.bodyMedium, color = Gray500)
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⭐", fontSize = 24.sp)
                            Text("${foodData.rating}", fontWeight = FontWeight.Bold)
                            Text("Đánh giá", style = MaterialTheme.typography.labelSmall, color = Gray500)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🚚", fontSize = 24.sp)
                            Text("Miễn phí", fontWeight = FontWeight.Bold)
                            Text("Giao hàng", style = MaterialTheme.typography.labelSmall, color = Gray500)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⏱️", fontSize = 24.sp)
                            Text("15-20p", fontWeight = FontWeight.Bold)
                            Text("Thời gian", style = MaterialTheme.typography.labelSmall, color = Gray500)
                        }
                    }
                }
            }

            // ────────────────────────────────────────────────────────
            // Card mô tả
            // ────────────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
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

            // Badge Admin giải thích tính năng
            if (isAdmin) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Orange500.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, null, tint = Orange500, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Chế độ Admin: Nhấn 📷 ở ảnh bên trên để upload ảnh thực tế lên Firebase Storage.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Orange500
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
