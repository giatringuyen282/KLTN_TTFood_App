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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a43_kltn_ttfood.data.model.*
import com.example.a43_kltn_ttfood.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * 🏠 Màn hình Trang chủ - GrabFood Style
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
    onNavigateToBannerDetail: (Int) -> Unit = {},
    onNavigateToFavorites: () -> Unit = {}
) {
    // Initialize repositories
    val restaurantRepo = remember { com.example.a43_kltn_ttfood.data.repository.RestaurantRepository() }
    val authRepo = remember { com.example.a43_kltn_ttfood.data.repository.AuthRepository() }
    val cartRepo = remember { com.example.a43_kltn_ttfood.data.repository.CartRepository() }
    val reservationRepo = remember { com.example.a43_kltn_ttfood.data.repository.ReservationRepository() }
    
    val categoryRepo = remember { com.example.a43_kltn_ttfood.data.repository.CategoryRepository() }
    val foodRepo = remember { com.example.a43_kltn_ttfood.data.repository.FoodRepository() }
    val dbSeeder = remember { com.example.a43_kltn_ttfood.data.util.DatabaseSeeder }

    // State holders for dynamic data
    var categories by remember { mutableStateOf<List<com.example.a43_kltn_ttfood.data.model.FoodCategory>>(emptyList()) }
    var foods by remember { mutableStateOf<List<com.example.a43_kltn_ttfood.data.model.FoodItem>>(emptyList()) }
    var restaurants by remember { mutableStateOf<List<com.example.a43_kltn_ttfood.data.model.Restaurant>>(emptyList()) }

    // Group restaurants by brand
    val brandCards = remember(restaurants) {
        val grouped = restaurants.groupBy {
            if (it.name.contains(" - ")) it.name.substringBefore(" - ") else it.name
        }
        grouped.map { (brandName, branches) ->
            if (branches.size == 1) {
                // Single branch/restaurant
                branches.first()
            } else {
                // Multi-branch brand
                val mainBranch = branches.first()
                mainBranch.copy(
                    name = brandName,
                    id = "brand_$brandName" // dummy id to trigger branch selection
                )
            }
        }
    }

    var userProfile by remember { mutableStateOf<com.example.a43_kltn_ttfood.data.model.User?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    var address by remember { mutableStateOf("123 Nguyễn Văn Cừ, Q.5, TP.HCM") }
    var cartItemCount by remember { mutableIntStateOf(0) }
    
    var showAddressDialog by remember { mutableStateOf(false) }
    var tempAddress by remember { mutableStateOf("") }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Giao hàng, 1 = Đi Ăn Nhà Hàng

    var showBookingSheet by remember { mutableStateOf(false) }
    var selectedRestaurantForBooking by remember { mutableStateOf<com.example.a43_kltn_ttfood.data.model.Restaurant?>(null) }
    val bookingSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var activeBranchSelectionBrand by remember { mutableStateOf<String?>(null) }
    val branchSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Load data from Firestore
    LaunchedEffect(Unit) {
        try {
            dbSeeder.seedIfNeeded()
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
                        reviewCount = model.reviewCount,
                        distance = sampleMatch?.distance ?: "1.2 km",
                        deliveryTime = sampleMatch?.deliveryTime ?: "15-20 phút",
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

    LaunchedEffect(userProfile) {
        userProfile?.let { user ->
            cartRepo.getCart(user.id).collect { items ->
                cartItemCount = items.sumOf { it.quantity }
            }
        }
    }

    Scaffold(
        containerColor = White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ===== GREEN HEADER =====
            item {
                GrabHeader(
                    address = address,
                    onAddressClick = {
                        tempAddress = address
                        showAddressDialog = true
                    },
                    onFavoriteClick = onNavigateToFavorites,
                    onCartClick = onNavigateToCart,
                    onProfileClick = onNavigateToProfile,
                    cartItemCount = cartItemCount
                )
            }

            // ===== SEARCH BAR =====
            item {
                GrabSearchBar(onClick = onNavigateToSearch)
            }

            // ===== BANNER CAROUSEL =====
            item {
                BannerCarousel(
                    banners = sampleBanners,
                    onBannerClick = onNavigateToBannerDetail
                )
            }

            // ===== GIAO HÀNG / ĐI ĂN NHÀ HÀNG TABS =====
            item {
                DeliveryTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }

            if (selectedTab == 0) {
                // ===== DANH MỤC MÓN ĂN =====
                item {
                    CategorySection(
                        categories = categories,
                        onCategoryClick = { cat ->
                            onNavigateToCategory(cat.id)
                        }
                    )
                }

                // ===== SHORTCUT TILES =====
                item {
                    ShortcutTilesSection()
                }

                // ===== MUA NGAY - Deal Banners =====
                item {
                    SectionHeader(
                        title = "Mua Ngay",
                        onViewAll = {}
                    )
                }
                item {
                    FoodItemsRow(
                        foods = foods,
                        onFoodClick = onNavigateToFood
                    )
                }

                // ===== NHÀ HÀNG NỔI BẬT - Danh sách ngang kiểu GrabFood =====
                item {
                    SectionHeader(
                        title = "Nhà hàng nổi tiếng tuần này",
                        onViewAll = {}
                    )
                }

                // Restaurant list cards (vertical, like GrabFood)
                items(brandCards) { restaurant ->
                    RestaurantListCard(
                        restaurant = restaurant,
                        onClick = {
                            if (restaurant.id.startsWith("brand_")) {
                                activeBranchSelectionBrand = restaurant.name
                            } else {
                                onNavigateToRestaurant(restaurant.id)
                            }
                        }
                    )
                }
            } else {
                // ===== ĐI ĂN NHÀ HÀNG TABS =====
                item {
                    SectionHeader(
                        title = "Khám phá Nhà Hàng có chỗ ngồi",
                        onViewAll = {}
                    )
                }
                
                val dineInRestaurants = brandCards.filter { it.isDineInAvailable }
                
                if (dineInRestaurants.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Chưa có nhà hàng nào hỗ trợ đặt bàn.", color = Gray500)
                        }
                    }
                } else {
                    items(dineInRestaurants) { restaurant ->
                        DineInRestaurantCard(
                            restaurant = restaurant,
                            onBookTable = {
                                selectedRestaurantForBooking = restaurant
                                showBookingSheet = true
                            }
                        )
                    }
                }
            }
        }
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
                        focusedBorderColor = GrabGreen,
                        focusedLabelColor = GrabGreen
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (tempAddress.isNotBlank()) address = tempAddress
                    showAddressDialog = false
                }) {
                    Text("Lưu", color = GrabGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddressDialog = false }) {
                    Text("Hủy", color = Gray500)
                }
            }
        )
    }

    // Reservation Booking Sheet
    if (showBookingSheet && selectedRestaurantForBooking != null) {
        ModalBottomSheet(
            onDismissRequest = { showBookingSheet = false },
            sheetState = bookingSheetState,
            containerColor = White
        ) {
            ReservationBottomSheetContent(
                restaurant = selectedRestaurantForBooking!!,
                onConfirm = { date, time, people ->
                    showBookingSheet = false
                    
                    userProfile?.id?.let { uid ->
                        coroutineScope.launch {
                            val result = reservationRepo.createReservation(
                                userId = uid,
                                restaurantId = selectedRestaurantForBooking!!.id,
                                restaurantName = selectedRestaurantForBooking!!.name,
                                date = date,
                                time = time,
                                numberOfPeople = people
                            )
                            
                            if (result.isSuccess) {
                                android.widget.Toast.makeText(
                                    context, 
                                    "🎉 Đặt bàn thành công!\n$date lúc $time cho $people người.", 
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            } else {
                                android.widget.Toast.makeText(
                                    context, 
                                    "❌ Có lỗi xảy ra khi đặt bàn. Vui lòng thử lại.", 
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } ?: run {
                        android.widget.Toast.makeText(context, "Vui lòng đăng nhập để đặt bàn", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                onCancel = { showBookingSheet = false }
            )
        }
    }

    // Branch Selection Bottom Sheet
    if (activeBranchSelectionBrand != null) {
        val selectedBrand = activeBranchSelectionBrand!!
        val branches = restaurants.filter {
            if (it.name.contains(" - ")) it.name.substringBefore(" - ") == selectedBrand else false
        }
        val mainBranch = branches.firstOrNull()

        ModalBottomSheet(
            onDismissRequest = { activeBranchSelectionBrand = null },
            sheetState = branchSheetState,
            containerColor = White,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                // Header of branch selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedBrand,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Gray900
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$$ · Món Mỹ, Gà Rán, Món Gà, Bánh Mì Kẹp",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray500
                        )
                    }
                    if (mainBranch != null && mainBranch.logo.isNotBlank()) {
                        coil.compose.AsyncImage(
                            model = mainBranch.logo,
                            contentDescription = selectedBrand,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Gray200, RoundedCornerShape(8.dp))
                        )
                    }
                }

                HorizontalDivider(color = Gray200, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // Branch list
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(branches) { branch ->
                        val branchDisplayName = branch.name.substringAfter(" - ")

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    activeBranchSelectionBrand = null
                                    onNavigateToRestaurant(branch.id)
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = branchDisplayName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Gray900
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            // Rating and type
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = WarningYellow,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                val mockRatingCountStr = if (branch.reviewCount >= 1000) "${branch.reviewCount / 1000}K+" else "${branch.reviewCount}"
                                Text(
                                    text = "${branch.rating} ($mockRatingCountStr)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Gray700
                                )
                                Text(
                                    text = "  ·  Món Mỹ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Gray500
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Shipping and time
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🛵 ",
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Miễn phí 9.000đ",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PromoRed
                                )
                                Text(
                                    text = "  ·  ${branch.deliveryTime}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Gray500
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Vouchers
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(1.5.dp, PromoRed.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                        .background(White)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "🏷️",
                                            fontSize = 10.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Giảm 51.000đ",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = PromoRed,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Text(
                                    text = "+4 xem thêm",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Gray500,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        HorizontalDivider(color = Gray200, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

// ==============================
// GREEN HEADER - GrabFood Style
// ==============================
@Composable
private fun GrabHeader(
    address: String,
    onAddressClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    cartItemCount: Int = 0
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GrabGreen,
                        GrabGreenDark
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Address
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onAddressClick)
                    .padding(vertical = 4.dp)
            ) {
                Column {
                    Text(
                        text = "Giao ngay",
                        style = MaterialTheme.typography.labelSmall,
                        color = White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Nhà",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = White
                    )
                }
            }

            // Right: Icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Yêu thích",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = onCartClick) {
                    BadgedBox(
                        badge = {
                            if (cartItemCount > 0) {
                                Badge(
                                    containerColor = Color.Red,
                                    contentColor = White
                                ) {
                                    Text(text = cartItemCount.toString(), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Giỏ hàng",
                            tint = White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                IconButton(onClick = onProfileClick) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Tài khoản",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// ==============================
// SEARCH BAR - GrabFood Style
// ==============================
@Composable
private fun GrabSearchBar(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Gray100)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Gray500,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Bạn đang thèm gì nào?",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500
            )
        }
    }
}

// ==============================
// BANNER CAROUSEL
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
            delay(4000)
            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 10.dp
        ) { page ->
            val banner = banners[page]
            BannerCard(
                banner = banner,
                onClick = { onBannerClick(banner.id) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dot indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(banners.size) { index ->
                val isSelected = pagerState.currentPage == index
                val width by animateDpAsState(
                    targetValue = if (isSelected) 20.dp else 6.dp,
                    animationSpec = tween(300),
                    label = "dotWidth"
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .height(6.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(if (isSelected) GrabGreen else Gray300)
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background image from drawable
            if (banner.imageResId != 0) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = banner.imageResId),
                    contentDescription = banner.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                // Fallback gradient if no image
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

            // Gradient scrim overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.6f),
                                Color.Black.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Text content on top of scrim
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Badge chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = banner.colorStart.copy(alpha = 0.9f),
                    modifier = Modifier
                ) {
                    Text(
                        text = banner.emoji,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Column {
                    Text(
                        text = banner.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = banner.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

// ==============================
// DELIVERY / DINE-IN TABS
// ==============================
@Composable
private fun DeliveryTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Giao hàng tab
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = if (selectedTab == 0) 2.dp else 1.dp,
                    color = if (selectedTab == 0) GrabGreen else Gray300,
                    shape = RoundedCornerShape(24.dp)
                )
                .background(if (selectedTab == 0) GrabGreenLight else White)
                .clickable { onTabSelected(0) }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🛵", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Giao hàng",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (selectedTab == 0) GrabGreen else Gray600
                )
            }
        }

        // Đi Ăn Nhà Hàng tab
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = if (selectedTab == 1) 2.dp else 1.dp,
                    color = if (selectedTab == 1) GrabOrange else Gray300,
                    shape = RoundedCornerShape(24.dp)
                )
                .background(if (selectedTab == 1) Color(0xFFFFF3ED) else White)
                .clickable { onTabSelected(1) }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🏷️", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Đi Ăn Nhà Hàng",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (selectedTab == 1) GrabOrange else Gray600
                )
            }
        }
    }
}

// ==============================
// CATEGORY SECTION - Circular style
// ==============================
@Composable
private fun CategorySection(
    categories: List<FoodCategory>,
    onCategoryClick: (FoodCategory) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(categories) { category ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(68.dp)
                    .clickable { onCategoryClick(category) }
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Gray50),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = category.emoji, fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray700,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

// ==============================
// SHORTCUT TILES - "Gần tôi", "Một Người Ăn", etc.
// ==============================
@Composable
private fun ShortcutTilesSection() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val tiles = listOf(
        Triple("📍", "Gần tôi", "Nhận ngay"),
        Triple("🍜", "Một Người Ăn", "Bao trọn gói"),
        Triple("🏷️", "Vùng deal siêu rẻ", "Săn deal ngay")
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(tiles) { (emoji, title, subtitle) ->
            Card(
                modifier = Modifier
                    .width(150.dp)
                    .height(100.dp)
                    .clickable {
                        android.widget.Toast.makeText(
                            context,
                            "Tính năng '$title' đang được phát triển",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = emoji, fontSize = 28.sp)
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Gray900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = Gray500
                        )
                    }
                }
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
    onViewAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Gray900
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onViewAll)
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Gray500,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ==============================
// FOOD ITEMS ROW - "Mua Ngay" horizontal scroll
// ==============================
@Composable
private fun FoodItemsRow(
    foods: List<FoodItem>,
    onFoodClick: (Int) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(foods) { food ->
            FoodCard(food = food, onClick = { onFoodClick(food.id) })
        }
    }
}

@Composable
private fun FoodCard(food: FoodItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Food image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
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

                // Add button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(GrabGreen)
                        .clickable(onClick = onClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                    color = Gray900
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = food.formattedPrice,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Gray900
                )
            }
        }
    }
}

// ==============================
// RESTAURANT LIST CARD - GrabFood horizontal card style
// ==============================
@Composable
private fun RestaurantListCard(
    restaurant: Restaurant,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Restaurant image (left)
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(RoundedCornerShape(12.dp))
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
            } else if (restaurant.coverImage.isNotBlank()) {
                coil.compose.AsyncImage(
                    model = restaurant.coverImage,
                    contentDescription = restaurant.name,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(text = restaurant.emoji, fontSize = 40.sp)
            }

            // Discount badge
            if (restaurant.badge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(PromoRed)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = restaurant.badge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = White,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Restaurant info (right)
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = restaurant.name,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Gray900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Rating + type
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = WarningYellow,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${restaurant.rating}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Gray700
                )
                Text(
                    text = "  ·  ",
                    color = Gray400
                )
                Text(
                    text = restaurant.distance,
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray500
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Delivery info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🛵 ",
                    fontSize = 12.sp
                )
                Text(
                    text = "Miễn phí",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = PromoRed
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "15.000đ",
                    style = MaterialTheme.typography.labelSmall.copy(
                        textDecoration = TextDecoration.LineThrough
                    ),
                    color = Gray400,
                    fontSize = 11.sp
                )
                Text(
                    text = "  ·  ${restaurant.deliveryTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray500
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Voucher chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, Gray200, RoundedCornerShape(6.dp))
                            .background(White)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🏷️",
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Giảm 15.000đ",
                                style = MaterialTheme.typography.labelSmall,
                                color = Gray700,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Divider
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = Gray200
    )
}

// ==============================
// Shimmer Loading (kept for compatibility)
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
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(4) {
            Card(
                modifier = Modifier.width(180.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
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
// DINE-IN COMPONENTS
// ==============================

@Composable
private fun DineInRestaurantCard(
    restaurant: com.example.a43_kltn_ttfood.data.model.Restaurant,
    onBookTable: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Brush.horizontalGradient(listOf(restaurant.colorStart, restaurant.colorEnd))),
                contentAlignment = Alignment.Center
            ) {
                Text(text = restaurant.emoji, fontSize = 56.sp)
                
                // Rating Badge
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(White.copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Orange500, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = restaurant.rating.toString(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Gray500, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Cách bạn ${restaurant.distance}", style = MaterialTheme.typography.bodySmall, color = Gray500)
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = onBookTable,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GrabOrange)
                ) {
                    Text("Đặt bàn ngay", color = White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ReservationBottomSheetContent(
    restaurant: com.example.a43_kltn_ttfood.data.model.Restaurant,
    onConfirm: (String, String, Int) -> Unit,
    onCancel: () -> Unit
) {
    var selectedDate by remember { mutableStateOf("Hôm nay") }
    var selectedTime by remember { mutableStateOf("18:00") }
    var selectedPeople by remember { mutableIntStateOf(2) }

    val dates = listOf("Hôm nay", "Ngày mai", "Ngày mốt")
    val times = listOf("17:00", "18:00", "19:00", "20:00", "21:00")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Đặt bàn tại ${restaurant.name}",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Số người
        Text(text = "Số lượng người", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = { if (selectedPeople > 1) selectedPeople-- },
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Gray100)
            ) { Text("−", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Gray700) }
            Text("$selectedPeople người", style = MaterialTheme.typography.titleMedium)
            IconButton(
                onClick = { selectedPeople++ },
                modifier = Modifier.size(40.dp).clip(CircleShape).background(GrabOrange.copy(alpha = 0.1f))
            ) { Icon(Icons.Default.Add, "Tăng", tint = GrabOrange) }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Ngày
        Text(text = "Chọn ngày", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(dates) { date ->
                FilterChip(
                    selected = selectedDate == date,
                    onClick = { selectedDate = date },
                    label = { Text(date) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GrabOrange,
                        selectedLabelColor = White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Giờ
        Text(text = "Chọn giờ đến", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(times) { time ->
                FilterChip(
                    selected = selectedTime == time,
                    onClick = { selectedTime = time },
                    label = { Text(time) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GrabOrange,
                        selectedLabelColor = White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Nút xác nhận
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Hủy", color = Gray700)
            }
            Button(
                onClick = { onConfirm(selectedDate, selectedTime, selectedPeople) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GrabOrange)
            ) {
                Text("Xác nhận", color = White, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
