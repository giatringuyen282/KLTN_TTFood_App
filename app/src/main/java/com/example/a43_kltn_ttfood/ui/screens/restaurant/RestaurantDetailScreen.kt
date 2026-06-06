package com.example.a43_kltn_ttfood.ui.screens.restaurant

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a43_kltn_ttfood.data.model.FoodItem
import com.example.a43_kltn_ttfood.data.model.Restaurant
import com.example.a43_kltn_ttfood.data.model.sampleFoodItems
import com.example.a43_kltn_ttfood.data.model.sampleRestaurants
import com.example.a43_kltn_ttfood.data.repository.AuthRepository
import com.example.a43_kltn_ttfood.data.repository.CartRepository
import com.example.a43_kltn_ttfood.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RestaurantDetailScreen(
    restaurantId: Int,
    onNavigateBack: () -> Unit = {},
    onNavigateToFood: (Int) -> Unit = {}
) {
    val cartRepo = remember { CartRepository() }
    val authRepo = remember { AuthRepository() }
    val restaurant = remember {
        sampleRestaurants.find { it.id == restaurantId } ?: sampleRestaurants.first()
    }
    var isFavorite by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Menu categories setup
    val menuCategories = listOf("Món chính", "Khai vị", "Tráng miệng", "Đồ uống")
    val categorizedMenu = remember {
        menuCategories.associateWith { category ->
            sampleFoodItems.shuffled().take(4) // Mock data per category
        }
    }

    // Compute indices for sticky tabs
    val categoryIndices = remember { mutableMapOf<Int, Int>() }
    var currentIndex = 2 // 0: Header, 1: Sticky Tabs
    menuCategories.forEachIndexed { index, category ->
        categoryIndices[index] = currentIndex
        currentIndex += 1 // Category Title
        currentIndex += categorizedMenu[category]!!.size // Items
    }

    // Auto-update selected tab based on scroll position
    val firstVisibleItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    var selectedTab by remember { mutableIntStateOf(0) }
    var isTabClicked by remember { mutableStateOf(false) }

    LaunchedEffect(firstVisibleItemIndex) {
        if (!isTabClicked) {
            val currentCat = categoryIndices.entries.findLast { it.value <= firstVisibleItemIndex }?.key ?: 0
            if (selectedTab != currentCat) {
                selectedTab = currentCat
            }
        }
    }

    // TopAppBar Background Alpha
    val headerHeight = 250f // approx height in px for parallax
    val scrollOffset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }
    val topBarAlpha by remember {
        derivedStateOf {
            if (firstVisibleItemIndex > 0) 1f
            else (scrollOffset / headerHeight).coerceIn(0f, 1f)
        }
    }

    // Bottom Sheet State
    var selectedFoodForDetail by remember { mutableStateOf<FoodItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = Gray50,
        topBar = {
            TopAppBar(
                title = {
                    AnimatedVisibility(
                        visible = topBarAlpha > 0.8f,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = restaurant.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(if (topBarAlpha > 0.5f) Color.Transparent else White.copy(alpha = 0.7f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại", tint = Gray900)
                    }
                },
                actions = {
                    val btnBg = if (topBarAlpha > 0.5f) Color.Transparent else White.copy(alpha = 0.7f)
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Hãy thử món ăn tuyệt vời tại nhà hàng ${restaurant.name} trên ứng dụng TTFood nhé!")
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Chia sẻ nhà hàng")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier.clip(CircleShape).background(btnBg)
                    ) {
                        Icon(Icons.Outlined.Share, "Chia sẻ", tint = Gray900)
                    }
                    IconButton(onClick = { isFavorite = !isFavorite }, modifier = Modifier.clip(CircleShape).background(btnBg)) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            "Yêu thích",
                            tint = if (isFavorite) ErrorRed else Gray900
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White.copy(alpha = topBarAlpha),
                    scrolledContainerColor = White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp) // padding for bottom bar
        ) {
            // 0: Header
            item {
                RestaurantHeroHeader(
                    restaurant = restaurant,
                    scrollOffset = if (firstVisibleItemIndex == 0) scrollOffset else 0
                )
            }

            // 1: Sticky Tabs
            stickyHeader {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = White,
                    contentColor = Orange500,
                    edgePadding = 16.dp,
                    modifier = Modifier.shadow(elevation = 4.dp)
                ) {
                    menuCategories.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                                isTabClicked = true
                                coroutineScope.launch {
                                    listState.animateScrollToItem(categoryIndices[index] ?: 0)
                                    isTabClicked = false
                                }
                            },
                            text = {
                                Text(
                                    title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) Orange500 else Gray600
                                )
                            }
                        )
                    }
                }
            }

            // Menu Items by Category
            menuCategories.forEachIndexed { index, category ->
                // Category Title
                item {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Gray50)
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Foods
                items(categorizedMenu[category]!!) { food ->
                    FoodItemHorizontalCard(
                        food = food,
                        onClick = { selectedFoodForDetail = food }
                    )
                    Divider(color = Gray200, modifier = Modifier.padding(horizontal = 20.dp))
                }
            }
        }
    }

    // Food Detail Bottom Sheet
    if (selectedFoodForDetail != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedFoodForDetail = null },
            sheetState = sheetState,
            containerColor = White,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            FoodDetailBottomSheet(
                food = selectedFoodForDetail!!,
                onAddToCart = { quantity, toppings ->
                    val uid = authRepo.currentFirebaseUser?.uid
                    if (uid != null) {
                        coroutineScope.launch {
                            val result = cartRepo.addToCart(
                                userId = uid,
                                food = selectedFoodForDetail!!,
                                quantity = quantity,
                                toppings = toppings
                            )
                            result.fold(
                                onSuccess = {
                                    android.widget.Toast.makeText(context, "✅ Đã thêm vào giỏ hàng!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                onFailure = { e ->
                                    android.widget.Toast.makeText(context, "❌ Lỗi: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                }
                            )
                            sheetState.hide()
                            selectedFoodForDetail = null
                        }
                    } else {
                        android.widget.Toast.makeText(context, "Vui lòng đăng nhập để đặt hàng", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
fun RestaurantHeroHeader(restaurant: Restaurant, scrollOffset: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // Parallax Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .graphicsLayer {
                    translationY = scrollOffset * 0.5f // Parallax effect
                }
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(restaurant.colorStart, restaurant.colorEnd)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = restaurant.emoji, fontSize = 100.sp, modifier = Modifier.alpha(0.5f))
        }

        // Info Card overlapping
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .align(Alignment.BottomCenter)
                .offset(y = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = restaurant.name,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = WarningYellow, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "${restaurant.rating} (500+ đánh giá)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Gray700
                            )
                        }
                    }
                    // Logo Avatar
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(White)
                            .border(2.dp, Gray200, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = restaurant.emoji, fontSize = 32.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoItem(icon = "🛵", value = restaurant.deliveryTime, label = "Giao hàng")
                    InfoItem(icon = "📍", value = restaurant.distance, label = "Khoảng cách")
                    InfoItem(icon = "💸", value = "15.000đ", label = "Phí giao")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Badges
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SuccessGreen.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Đang mở cửa", color = SuccessGreen, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    if (restaurant.badge != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Orange100)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(restaurant.badge, color = Orange500, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(32.dp))
}

@Composable
fun InfoItem(icon: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(Modifier.width(4.dp))
            Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Gray500)
    }
}

@Composable
fun FoodItemHorizontalCard(food: FoodItem, onClick: () -> Unit) {
    var quantity by remember { mutableIntStateOf(0) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Info on left
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                text = food.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Món ăn ngon đậm vị, công thức gia truyền đặc biệt phù hợp với mọi khẩu vị. Nguyên liệu tươi ngon 100%.",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = food.price,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Orange500
                )
                Spacer(Modifier.width(8.dp))
                // Mock old price
                Text(
                    text = "65.000đ",
                    style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                    color = Gray400
                )
            }
            
            // Badges
            Spacer(Modifier.height(8.dp))
            Row {
                if (food.rating > 4.5f) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(ErrorRed.copy(alpha = 0.1f)).padding(horizontal = 6.dp, vertical = 2.dp)
                    ) { Text("Bán chạy 🔥", color = ErrorRed, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                } else {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(InfoBlue.copy(alpha = 0.1f)).padding(horizontal = 6.dp, vertical = 2.dp)
                    ) { Text("Mới ✨", color = InfoBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }

        // Image & Add Button on right
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(food.bgColor),
                contentAlignment = Alignment.Center
            ) {
                if (food.imageUrl.isNotBlank()) {
                    coil.compose.AsyncImage(
                        model = food.imageUrl,
                        contentDescription = food.name,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(text = food.emoji, fontSize = 48.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            
            // Inline Quantity
            if (quantity == 0) {
                IconButton(
                    onClick = { quantity++ },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Orange500)
                ) {
                    Icon(Icons.Default.Add, "Thêm", tint = White, modifier = Modifier.size(20.dp))
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Orange200, RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "−",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Orange500,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clickable { quantity-- }
                    )
                    Text(
                        text = "$quantity", 
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Icon(
                        Icons.Default.Add, "Tăng", 
                        tint = Orange500, 
                        modifier = Modifier.size(16.dp).clickable { quantity++ }
                    )
                }
            }
        }
    }
}

@Composable
fun FoodDetailBottomSheet(food: FoodItem, onAddToCart: (Int, String) -> Unit) {
    var quantity by remember { mutableIntStateOf(1) }
    var selectedSize by remember { mutableStateOf("Vừa") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(food.bgColor),
            contentAlignment = Alignment.Center
        ) {
            if (food.imageUrl.isNotBlank()) {
                coil.compose.AsyncImage(
                    model = food.imageUrl,
                    contentDescription = food.name,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(text = food.emoji, fontSize = 100.sp)
            }
        }
        
        Column(modifier = Modifier.padding(24.dp)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = food.price,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Orange500
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Món ăn ngon đậm vị, công thức gia truyền đặc biệt phù hợp với mọi khẩu vị. Nguyên liệu tươi ngon 100%, an toàn vệ sinh thực phẩm.",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Size Selection
            Text("Chọn kích cỡ", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("Nhỏ" to "", "Vừa" to "+0đ", "Lớn" to "+10.000đ").forEach { (size, priceDiff) ->
                    val isSelected = selectedSize == size
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Orange500 else Gray200,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(if (isSelected) Orange50 else White)
                            .clickable { selectedSize = size }
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(size, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        if (priceDiff.isNotEmpty()) {
                            Text(priceDiff, style = MaterialTheme.typography.labelSmall, color = Gray500)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Note
            Text("Ghi chú cho quán", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Ví dụ: Không hành, không cay...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange500,
                    unfocusedBorderColor = Gray300
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Add to Cart Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Gray100)
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "−",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gray700,
                        modifier = Modifier.clickable { if (quantity > 1) quantity-- }
                    )
                    
                    Text(
                        text = "$quantity", 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    IconButton(
                        onClick = { quantity++ },
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(Orange500)
                    ) { Icon(Icons.Default.Add, "Tăng", tint = White, modifier = Modifier.size(16.dp)) }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Add Button
                val unitPrice = food.price.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 55000
                val totalPrice = quantity * unitPrice
                val formattedPrice = String.format("%,dđ", totalPrice).replace(",", ".")
                Button(
                    onClick = { onAddToCart(quantity, selectedSize) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange500)
                ) {
                    Text("Thêm - $formattedPrice", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
