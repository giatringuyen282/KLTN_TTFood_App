package com.example.a43_kltn_ttfood.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a43_kltn_ttfood.data.model.*
import com.example.a43_kltn_ttfood.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * 🏠 Màn hình Trang chủ
 * - Header: Avatar + greeting + location + notification
 * - Search bar
 * - Banner carousel auto-scroll
 * - Danh mục món ăn (scroll ngang)
 * - Gợi ý AI (skeleton loading)
 * - Nhà hàng nổi bật
 * - Đặt lại nhanh
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToRestaurant: (String) -> Unit = {},
    onNavigateToFood: (Int) -> Unit = {},
    onNavigateToCategory: (Int) -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToBannerDetail: (Int) -> Unit = {}
) {
    val authRepo = remember { com.example.a43_kltn_ttfood.data.repository.AuthRepository() }
    
    // Initialize repositories
    val categoryRepo = remember { com.example.a43_kltn_ttfood.data.repository.CategoryRepository() }
    val foodRepo = remember { com.example.a43_kltn_ttfood.data.repository.FoodRepository() }
    val restaurantRepo = remember { com.example.a43_kltn_ttfood.data.repository.RestaurantRepository() }
    val dbSeeder = remember { com.example.a43_kltn_ttfood.data.util.DatabaseSeeder }

    // State holders for dynamic data
    var categories by remember { mutableStateOf<List<com.example.a43_kltn_ttfood.data.model.FoodCategory>>(emptyList()) }
    var foods by remember { mutableStateOf<List<com.example.a43_kltn_ttfood.data.model.FoodItem>>(emptyList()) }
    var restaurants by remember { mutableStateOf<List<com.example.a43_kltn_ttfood.data.model.Restaurant>>(emptyList()) }

    var userProfile by remember { mutableStateOf<com.example.a43_kltn_ttfood.data.model.User?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    var address by remember { mutableStateOf("123 Nguyễn Văn Cừ, Q.5, TP.HCM") }
    
    var showNameDialog by remember { mutableStateOf(false) }
    var showAddressDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }
    var tempAddress by remember { mutableStateOf("") }

    var selectedCategory by remember { mutableIntStateOf(0) }
    var isAILoading by remember { mutableStateOf(true) }

    // Load data from Firestore
    LaunchedEffect(Unit) {
        try {
            // Seed if needed
            dbSeeder.seedIfNeeded()
            // Collect categories
            categoryRepo.getAllCategories().collect { categories = it }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    LaunchedEffect(Unit) {
        try {
            foodRepo.getAllFoodItems().collect { foods = it }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    LaunchedEffect(Unit) {
        try {
            restaurantRepo.getAllRestaurants().collect { modelList ->
                restaurants = modelList.map { model ->
                    val modelName = model.name.orEmpty()
                    val modelEmoji = model.emoji.orEmpty()
                    val modelId = model.id.orEmpty()
                    val sampleMatch = sampleRestaurants.find {
                        it.name.equals(modelName, ignoreCase = true)
                    }
                    Restaurant(
                        id = modelId.ifBlank { sampleMatch?.id ?: modelName },
                        emoji = modelEmoji.ifBlank { sampleMatch?.emoji ?: "🍽️" },
                        name = modelName,
                        rating = model.rating.toFloat(),
                        distance = sampleMatch?.distance ?: "1.2 km",
                        deliveryTime = sampleMatch?.deliveryTime ?: "15-20 min",
                        badge = if (model.isOpen) null else "Đóng cửa",
                        colorStart = sampleMatch?.colorStart ?: Orange500,
                        colorEnd = sampleMatch?.colorEnd ?: Orange500,
                        logo = model.logo.orEmpty(),
                        coverImage = model.coverImage.orEmpty()
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(Unit) {
        try {
            userProfile = authRepo.getCurrentUserProfile()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Simulate AI loading
    LaunchedEffect(Unit) {
        delay(1500)
        isAILoading = false
    }

    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Chào buổi sáng"
            in 12..17 -> "Chào buổi chiều"
            else -> "Chào buổi tối"
        }
    }

    Scaffold(
        containerColor = Gray50
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header
            item {
                HomeHeader(
                    greeting = greeting,
                    userName = userProfile?.fullName?.ifBlank { "Người dùng" } ?: "Người dùng",
                    address = address,
                    notificationCount = 3,
                    onNotificationClick = onNavigateToNotifications,
                    onCartClick = onNavigateToCart,
                    onProfileClick = onNavigateToProfile,
                    onNameClick = {
                        tempName = userProfile?.fullName ?: ""
                        showNameDialog = true
                    },
                    onAddressClick = {
                        tempAddress = address
                        showAddressDialog = true
                    }
                )
            }

            // Search Bar
            item {
                SearchBarSection(onClick = onNavigateToSearch)
            }

            // Banner Carousel
            item {
                BannerCarousel(
                    banners = sampleBanners,
                    onBannerClick = onNavigateToBannerDetail
                )
            }

            // Categories
            item {
                CategorySection(
                    categories = categories,
                    selectedIndex = selectedCategory,
                    onCategorySelected = { index ->
                        selectedCategory = index
                        // Navigate using the selected category's id if available
                        categories.getOrNull(index)?.let { onNavigateToCategory(it.id) }
                    }
                )
            }

            // AI Recommendations
            item {
                SectionHeader(
                    title = "🤖 Gợi ý cho bạn",
                    subtitle = "Dựa trên khẩu vị của bạn",
                    onViewAll = {}
                )
            }
            item {
                    if (isAILoading) {
                        ShimmerFoodRow()
                    } else {
                        FoodItemsRow(
                            foods = foods,
                            onFoodClick = onNavigateToFood
                        )
                    }
            }

            // Featured Restaurants
            item {
                SectionHeader(
                    title = "🏪 Nổi bật hôm nay",
                    subtitle = "Nhà hàng được yêu thích nhất",
                    onViewAll = {}
                )
            }
            item {
                RestaurantsRow(
                    restaurants = restaurants,
                    onRestaurantClick = onNavigateToRestaurant
                )
            }

            // Reorder Section
            item {
                ReorderSection(
                    items = sampleReorders,
                    onReorder = {}
                )
            }
        }
    }

    // Name Edit Dialog
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text(text = "Đổi tên hiển thị", style = MaterialTheme.typography.titleMedium) },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Orange500,
                        focusedLabelColor = Orange500
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (tempName.isNotBlank() && userProfile != null) {
                        val currentUser = userProfile!!
                        val updatedUser = currentUser.copy(
                            fullName = tempName,
                            updatedAt = com.google.firebase.Timestamp.now()
                        )
                        coroutineScope.launch {
                            val result = authRepo.updateUserProfile(updatedUser)
                            if (result.isSuccess) {
                                userProfile = updatedUser
                                android.widget.Toast.makeText(context, "Đã cập nhật tên hiển thị!", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                val errMsg = result.exceptionOrNull()?.message ?: "Cập nhật thất bại"
                                android.widget.Toast.makeText(context, "Lỗi: $errMsg", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    showNameDialog = false
                }) {
                    Text("Lưu", color = Orange500)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text("Hủy", color = Gray500)
                }
            }
        )
    }

    // Address Edit Dialog
    if (showAddressDialog) {
        AlertDialog(
            onDismissRequest = { showAddressDialog = false },
            title = { Text(text = "Đổi địa chỉ giao hàng", style = MaterialTheme.typography.titleMedium) },
            text = {
                OutlinedTextField(
                    value = tempAddress,
                    onValueChange = { tempAddress = it },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Orange500,
                        focusedLabelColor = Orange500
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (tempAddress.isNotBlank()) address = tempAddress
                    showAddressDialog = false
                }) {
                    Text("Lưu", color = Orange500)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddressDialog = false }) {
                    Text("Hủy", color = Gray500)
                }
            }
        )
    }
}

// ==============================
// Header Section
// ==============================
@Composable
private fun HomeHeader(
    greeting: String,
    userName: String,
    address: String,
    notificationCount: Int,
    onNotificationClick: () -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNameClick: () -> Unit,
    onAddressClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(White, Gray50)
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable(onClick = onNameClick)
                    .padding(end = 8.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(GradientStart, GradientEnd)
                            )
                        )
                        .clickable(onClick = onProfileClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👤", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "$greeting,",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Cart icon
                BadgedBox(
                    badge = {
                        Badge(containerColor = Orange500) {
                            Text(
                                text = "3", // Mock cart count
                                style = MaterialTheme.typography.labelSmall,
                                color = White
                            )
                        }
                    }
                ) {
                    IconButton(onClick = onCartClick) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Giỏ hàng",
                            tint = Gray700
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Notification bell
                BadgedBox(
                    badge = {
                        if (notificationCount > 0) {
                            Badge(containerColor = ErrorRed) {
                                Text(
                                    text = "$notificationCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = White
                                )
                            }
                        }
                    }
                ) {
                    IconButton(onClick = onNotificationClick) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Thông báo",
                            tint = Gray700
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Location
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onAddressClick)
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .offset(x = (-4).dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Orange500,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = address,
                style = MaterialTheme.typography.bodySmall,
                color = Gray600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                tint = Gray500,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ==============================
// Search Bar
// ==============================
@Composable
private fun SearchBarSection(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Gray400,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Bạn muốn ăn gì hôm nay?",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray400
            )
        }
    }
}

// ==============================
// Banner Carousel
// ==============================
@Composable
private fun BannerCarousel(
    banners: List<Banner>,
    onBannerClick: (Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { banners.size })

    // Auto-scroll
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 12.dp
        ) { page ->
            val banner = banners[page]
            BannerCard(
                banner = banner,
                onClick = { onBannerClick(banner.id) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dot indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(banners.size) { index ->
                val isSelected = pagerState.currentPage == index
                val width by animateDpAsState(
                    targetValue = if (isSelected) 24.dp else 8.dp,
                    animationSpec = tween(300),
                    label = "dotWidth"
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(8.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(if (isSelected) Orange500 else Gray300)
                )
            }
        }
    }
}

@Composable
private fun BannerCard(
    banner: Banner,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(banner.colorStart, banner.colorEnd)
                )
            )
            .clickable(onClick = onClick)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = banner.emoji,
                fontSize = 40.sp
            )
            Column {
                Text(
                    text = banner.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = White
                )
                Text(
                    text = banner.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

// ==============================
// Category Section
// ==============================
@Composable
private fun CategorySection(
    categories: List<FoodCategory>,
    selectedIndex: Int,
    onCategorySelected: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(vertical = 16.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(categories) { index, category ->
            val isSelected = index == selectedIndex
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.1f else 1f,
                animationSpec = tween(200),
                label = "categoryScale"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clickable { onCategorySelected(index) }
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Orange100 else White
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Orange500 else Gray200,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = category.emoji, fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) Orange500 else Gray600,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ==============================
// Section Header
// ==============================
@Composable
private fun SectionHeader(
    title: String,
    subtitle: String? = null,
    onViewAll: () -> Unit
) {
    Column(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = onViewAll) {
                Text(
                    text = "Xem tất cả",
                    style = MaterialTheme.typography.labelMedium,
                    color = Orange500
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Orange500,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Gray500,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// ==============================
// AI Recommendations - Food Row
// ==============================
@Composable
private fun FoodItemsRow(
    foods: List<FoodItem>,
    onFoodClick: (Int) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(foods) { food ->
            FoodItemCard(food = food, onClick = { onFoodClick(food.id) })
        }
    }
}

@Composable
private fun FoodItemCard(food: FoodItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(155.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            // Food image area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(food.bgColor),
                contentAlignment = Alignment.Center
            ) {
                if (food.imageUrl.isNotBlank()) {
                    coil.compose.AsyncImage(
                        model = food.imageUrl,
                        contentDescription = food.name,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        placeholder = null,
                        fallback = null
                    )
                } else {
                    Text(text = food.emoji, fontSize = 48.sp)
                }
            }
            // Info
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = food.restaurant,
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = food.formattedPrice,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Orange500
                    )
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

// ==============================
// Shimmer Loading for AI section
// ==============================
@Composable
private fun ShimmerFoodRow() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(4) {
            Card(
                modifier = Modifier.width(155.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Gray200.copy(alpha = alpha))
                    )
                    Column(modifier = Modifier.padding(12.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Gray200.copy(alpha = alpha))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(10.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Gray200.copy(alpha = alpha))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Gray200.copy(alpha = alpha))
                        )
                    }
                }
            }
        }
    }
}

// ==============================
// Featured Restaurants
// ==============================
@Composable
private fun RestaurantsRow(
    restaurants: List<Restaurant>,
    onRestaurantClick: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(restaurants) { restaurant ->
            RestaurantCard(
                restaurant = restaurant,
                onClick = { onRestaurantClick(restaurant.id) }
            )
        }
    }
}

@Composable
private fun RestaurantCard(restaurant: Restaurant, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(240.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Restaurant image with badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(restaurant.colorStart, restaurant.colorEnd)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (restaurant.logo.isNotBlank()) {
                    coil.compose.AsyncImage(
                        model = restaurant.logo,
                        contentDescription = restaurant.name,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(text = restaurant.emoji, fontSize = 48.sp)
                }

                // Badge
                if (restaurant.badge != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(White.copy(alpha = 0.9f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = restaurant.badge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = restaurant.colorStart
                        )
                    }
                }
            }

            // Info
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = WarningYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${restaurant.rating}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Gray700
                    )
                    Text(
                        text = "  ·  ${restaurant.distance}  ·  ${restaurant.deliveryTime}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray500
                    )
                }
            }
        }
    }
}

// ==============================
// Reorder Section
// ==============================
@Composable
private fun ReorderSection(
    items: List<ReorderItem>,
    onReorder: (Int) -> Unit
) {
    if (items.isEmpty()) return

    Column(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp)
    ) {
        Text(
            text = "⚡ Đặt lại món vừa ăn",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Đặt lại nhanh chỉ 1 chạm",
            style = MaterialTheme.typography.bodySmall,
            color = Gray500,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        items.forEach { item ->
            ReorderItemCard(
                item = item,
                onReorder = { onReorder(item.id) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ReorderItemCard(item: ReorderItem, onReorder: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food emoji
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Orange100),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.emoji, fontSize = 26.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.foodName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${item.restaurant} · ${item.price}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray500
                )
            }

            // Reorder button
            Button(
                onClick = onReorder,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange500
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Đặt lại",
                    style = MaterialTheme.typography.labelMedium,
                    color = White
                )
            }
        }
    }
}
