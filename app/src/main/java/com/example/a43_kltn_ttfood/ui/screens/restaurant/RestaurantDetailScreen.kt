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
    restaurantId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToFood: (Int) -> Unit = {}
) {
    val cartRepo = remember { CartRepository() }
    val authRepo = remember { AuthRepository() }
    val categoryRepo = remember { com.example.a43_kltn_ttfood.data.repository.CategoryRepository() }
    val restaurantRepo = remember { com.example.a43_kltn_ttfood.data.repository.RestaurantRepository() }
    val foodRepo = remember { com.example.a43_kltn_ttfood.data.repository.FoodRepository() }

    var restaurant by remember { mutableStateOf<Restaurant?>(null) }
    var categories by remember { mutableStateOf<List<com.example.a43_kltn_ttfood.data.model.FoodCategory>>(emptyList()) }
    var restaurantFoods by remember { mutableStateOf<List<FoodItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(restaurantId) {
        isLoading = true
        try {
            val model = restaurantRepo.getRestaurantById(restaurantId)
            if (model != null) {
                val modelName = model.name.orEmpty()
                val sampleMatch = sampleRestaurants.find { it.name.equals(modelName, ignoreCase = true) }
                restaurant = Restaurant(
                    id = model.id,
                    emoji = model.emoji.ifBlank { sampleMatch?.emoji ?: "🍽️" },
                    name = modelName,
                    rating = model.rating.toFloat(),
                    reviewCount = model.reviewCount,
                    distance = sampleMatch?.distance ?: "1.2 km",
                    deliveryTime = sampleMatch?.deliveryTime ?: "15-20 min",
                    badge = if (model.isOpen) null else "Đóng cửa",
                    colorStart = sampleMatch?.colorStart ?: Orange500,
                    colorEnd = sampleMatch?.colorEnd ?: Orange500,
                    logo = model.logo,
                    coverImage = model.coverImage
                )
            }
            categoryRepo.getAllCategories().collect { catList ->
                categories = catList
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(restaurantId) {
        try {
            foodRepo.getAllFoodItems().collect { allFoods ->
                restaurantFoods = allFoods.filter { it.restaurantId == restaurantId }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var isFavorite by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Menu categories setup
    val menuCategories = remember(restaurantFoods, categories) {
        categories.filter { category ->
            restaurantFoods.any { it.categoryId == category.id.toString() }
        }.map { it.name }
    }

    val categorizedMenu = remember(restaurantFoods, categories) {
        categories.filter { category ->
            restaurantFoods.any { it.categoryId == category.id.toString() }
        }.associate { category ->
            category.name to restaurantFoods.filter { it.categoryId == category.id.toString() }
        }
    }

    // Compute indices for sticky tabs
    val categoryIndices = remember(menuCategories, categorizedMenu) {
        val indices = mutableMapOf<Int, Int>()
        var currentIndex = 2 // 0: Header, 1: Sticky Tabs
        menuCategories.forEachIndexed { index, category ->
            indices[index] = currentIndex
            currentIndex += 1 // Category Title
            currentIndex += categorizedMenu[category]?.size ?: 0 // Items
        }
        indices
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

    var selectedFoodForDetail by remember { mutableStateOf<FoodItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val toppingRepo = remember { com.example.a43_kltn_ttfood.data.repository.ToppingGroupRepository() }
    var foodToppingGroups by remember { mutableStateOf<List<com.example.a43_kltn_ttfood.data.model.ToppingGroup>>(emptyList()) }
    var isLoadingToppings by remember { mutableStateOf(false) }

    LaunchedEffect(selectedFoodForDetail) {
        val currentFood = selectedFoodForDetail
        if (currentFood != null && currentFood.toppingGroupIds.isNotEmpty()) {
            isLoadingToppings = true
            foodToppingGroups = toppingRepo.getToppingGroupsByIds(currentFood.toppingGroupIds)
            isLoadingToppings = false
        } else {
            foodToppingGroups = emptyList()
        }
    }

    Scaffold(
        containerColor = Gray50,
        topBar = {
            TopAppBar(
                title = {
                    AnimatedVisibility(
                        visible = topBarAlpha > 0.8f && restaurant != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        val resName = restaurant?.name.orEmpty()
                        val displayTitle = if (resName.contains(" - ")) {
                            resName.substringBefore(" - ") + " (" + resName.substringAfter(" - ") + ")"
                        } else {
                            resName
                        }
                        Text(
                            text = displayTitle,
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
                            val rName = restaurant?.name.orEmpty()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Hãy thử món ăn tuyệt vời tại nhà hàng $rName trên ứng dụng TTFood nhé!")
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
        if (restaurant == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Orange500)
            }
        } else {
            val nonNullRes = restaurant!!
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    RestaurantHeroHeader(
                        restaurant = nonNullRes,
                        scrollOffset = if (firstVisibleItemIndex == 0) scrollOffset else 0
                    )
                }

                if (menuCategories.isNotEmpty()) {
                    stickyHeader {
                        ScrollableTabRow(
                            selectedTabIndex = selectedTab.coerceIn(0, menuCategories.size - 1),
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
                }

                menuCategories.forEach { category ->
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

                    val categoryFoods = categorizedMenu[category] ?: emptyList()
                    items(categoryFoods) { food ->
                        FoodItemHorizontalCard(
                            food = food,
                            onClick = { selectedFoodForDetail = food }
                        )
                        Divider(color = Gray200, modifier = Modifier.padding(horizontal = 20.dp))
                    }
                }
            }
        }
    }

    if (selectedFoodForDetail != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedFoodForDetail = null },
            sheetState = sheetState,
            containerColor = White,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            FoodDetailBottomSheet(
                food = selectedFoodForDetail!!,
                toppingGroups = foodToppingGroups,
                onAddToCart = { quantity, toppings, unitPrice ->
                    val uid = authRepo.currentFirebaseUser?.uid
                    if (uid != null) {
                        coroutineScope.launch {
                            val result = cartRepo.addToCart(
                                userId = uid,
                                food = selectedFoodForDetail!!,
                                quantity = quantity,
                                toppings = toppings,
                                unitPrice = unitPrice
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .graphicsLayer {
                    translationY = scrollOffset * 0.5f
                }
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(restaurant.colorStart, restaurant.colorEnd)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            val localCover = com.example.a43_kltn_ttfood.ui.util.LocalImageMapper.getRestaurantCover(restaurant.name)
            if (restaurant.coverImage.isNotBlank()) {
                coil.compose.AsyncImage(
                    model = restaurant.coverImage,
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    alpha = 0.85f
                )
            } else if (localCover != 0) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = localCover),
                    contentDescription = restaurant.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    alpha = 0.85f
                )
            } else {
                Text(text = restaurant.emoji, fontSize = 100.sp, modifier = Modifier.alpha(0.5f))
            }
        }

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
                        val resName = restaurant.name
                        val hasBranch = resName.contains(" - ")
                        val brandName = if (hasBranch) resName.substringBefore(" - ") else resName
                        val branchName = if (hasBranch) resName.substringAfter(" - ") else ""
                        Text(
                            text = brandName,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (branchName.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "- $branchName",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = Gray500
                            )
                        }
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
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(White)
                            .border(2.dp, Gray200, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val localLogo = com.example.a43_kltn_ttfood.ui.util.LocalImageMapper.getRestaurantLogo(restaurant.name)
                        if (restaurant.logo.isNotBlank()) {
                            coil.compose.AsyncImage(
                                model = restaurant.logo,
                                contentDescription = restaurant.name,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (localLogo != 0) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = localLogo),
                                contentDescription = restaurant.name,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(text = restaurant.emoji, fontSize = 32.sp)
                        }
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
                    text = food.formattedPrice,
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
                val localFoodImg = com.example.a43_kltn_ttfood.ui.util.LocalImageMapper.getFoodImage(food.name)
                if (food.imageUrl.isNotBlank()) {
                    coil.compose.AsyncImage(
                        model = food.imageUrl,
                        contentDescription = food.name,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (localFoodImg != 0) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = localFoodImg),
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
fun FoodDetailBottomSheet(
    food: FoodItem,
    toppingGroups: List<com.example.a43_kltn_ttfood.data.model.ToppingGroup>,
    onAddToCart: (Int, String, Int) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    var selectedSize by remember { mutableStateOf("Vừa") }
    
    // Store selected options for each topping group
    val selectedToppings = remember { mutableStateMapOf<String, String>() }
    
    LaunchedEffect(toppingGroups) {
        selectedToppings.clear()
        toppingGroups.forEach { group ->
            if (group.options.isNotEmpty()) {
                selectedToppings[group.name] = group.options.first().name
            }
        }
    }
    
    // Calculate total unit price including toppings
    val extraPrice = toppingGroups.sumOf { group ->
        val selectedOptionName = selectedToppings[group.name]
        group.options.find { it.name == selectedOptionName }?.price ?: 0
    }
    val unitPrice = food.price + extraPrice
    val totalPrice = quantity * unitPrice
    val formattedPrice = String.format("%,dđ", totalPrice).replace(",", ".")
    
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
            val localFoodImg = com.example.a43_kltn_ttfood.ui.util.LocalImageMapper.getFoodImage(food.name)
            if (food.imageUrl.isNotBlank()) {
                coil.compose.AsyncImage(
                    model = food.imageUrl,
                    contentDescription = food.name,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (localFoodImg != 0) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = localFoodImg),
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
                    text = food.formattedPrice,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = GrabGreen
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = food.description.ifBlank { "Món ăn ngon đậm vị, công thức gia truyền đặc biệt phù hợp với mọi khẩu vị. Nguyên liệu tươi ngon 100%." },
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600
            )
            
            // Dynamic Topping Groups
            if (toppingGroups.isNotEmpty()) {
                toppingGroups.forEach { group ->
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Group Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Gray900
                        )
                        
                        // Badge
                        val isRequired = group.isRequired || group.name == "main"
                        val badgeText = if (isRequired) "Chọn 1" else "Đã áp dụng"
                        val badgeBgColor = if (isRequired) Orange50 else SuccessGreen.copy(alpha = 0.1f)
                        val badgeTextColor = if (isRequired) Orange500 else SuccessGreen
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeBgColor)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = badgeText,
                                color = badgeTextColor,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Options
                    group.options.forEach { option ->
                        val isSelected = selectedToppings[group.name] == option.name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedToppings[group.name] = option.name
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedToppings[group.name] = option.name },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = GrabGreen,
                                    unselectedColor = Gray300
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = option.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray900,
                                modifier = Modifier.weight(1f)
                            )
                            if (option.price > 0) {
                                Text(
                                    text = "+${String.format("%,dđ", option.price).replace(",", ".")}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Gray700
                                )
                            }
                        }
                        HorizontalDivider(color = Gray200, thickness = 0.5.dp)
                    }
                }
            } else {
                // Fallback to legacy Size Selection
                Spacer(modifier = Modifier.height(24.dp))
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
                                    color = if (isSelected) GrabGreen else Gray200,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(if (isSelected) Color(0xFFE8F5E9) else White)
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
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Note
            Text("Ghi chú cho quán", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(12.dp))
            var noteText by remember { mutableStateOf("") }
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                placeholder = { Text("Ví dụ: Không hành, không cay...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GrabGreen,
                    unfocusedBorderColor = Gray300
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Savings bar (if originalPrice > price)
            val savings = (food.originalPrice - food.price) * quantity
            if (savings > 0) {
                Text(
                    text = "Bạn tiết kiệm được ${String.format("%,dđ", savings).replace(",", ".")} sau khi giảm giá.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Gray900,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Add to Cart Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity Selector
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
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clickable { if (quantity > 1) quantity-- }
                    )
                    
                    Text(
                        text = "$quantity", 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    
                    IconButton(
                        onClick = { quantity++ },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(GrabGreen)
                    ) { Icon(Icons.Default.Add, "Tăng", tint = White, modifier = Modifier.size(16.dp)) }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Button(
                    onClick = {
                        val toppingsResult = if (toppingGroups.isNotEmpty()) {
                            selectedToppings.map { "${it.key}: ${it.value}" }.joinToString("\n")
                        } else {
                            "Size: $selectedSize"
                        }
                        onAddToCart(quantity, toppingsResult, unitPrice)
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GrabGreen)
                ) {
                    Text("Thêm vào giỏ hàng - $formattedPrice", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = White))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
