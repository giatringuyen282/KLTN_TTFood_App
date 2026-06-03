package com.example.a43_kltn_ttfood.ui.screens.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a43_kltn_ttfood.data.model.FoodItem
import com.example.a43_kltn_ttfood.data.model.sampleFoodItems
import com.example.a43_kltn_ttfood.data.model.sampleRestaurants
import com.example.a43_kltn_ttfood.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==========================================
// 1. PROFILE DASHBOARD
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDashboardScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToOrderHistory: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val authRepo = remember { com.example.a43_kltn_ttfood.data.repository.AuthRepository() }

    // Lấy user profile từ Firestore
    var userProfile by remember { mutableStateOf<com.example.a43_kltn_ttfood.data.model.User?>(null) }
    LaunchedEffect(Unit) {
        userProfile = authRepo.getCurrentUserProfile()
    }

    val displayName = userProfile?.fullName?.ifBlank { "Người dùng" } ?: "Người dùng"
    val displayContact = buildString {
        userProfile?.phone?.let { if (it.isNotBlank()) append(it) }
        userProfile?.email?.let { if (it.isNotBlank()) { if (isNotEmpty()) append(" • "); append(it) } }
        if (isEmpty()) append("Chưa cập nhật thông tin")
    }
    val isAdmin = userProfile?.role == com.example.a43_kltn_ttfood.data.model.UserRole.ADMIN

    Scaffold(
        containerColor = Gray50,
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ của tôi", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(20.dp)
        ) {
            // Header: Avatar & Info
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(White)
                        .clickable(onClick = onNavigateToEditProfile)
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(64.dp).clip(CircleShape).background(GradientStart),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            displayName.firstOrNull()?.uppercase() ?: "?",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(displayName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        Text(displayContact, style = MaterialTheme.typography.bodySmall, color = Gray500)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Sửa", tint = Gray400)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Menu Items
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        ProfileMenuItem("Lịch sử đơn hàng", "📦", onNavigateToOrderHistory)
                        Divider(color = Gray100, modifier = Modifier.padding(horizontal = 20.dp))
                        ProfileMenuItem("Món ăn yêu thích", "❤️", onNavigateToFavorites)
                        Divider(color = Gray100, modifier = Modifier.padding(horizontal = 20.dp))
                        ProfileMenuItem("Cài đặt & Quyền riêng tư", "⚙️", onNavigateToSettings)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Admin panel — chỉ hiển thị khi role = admin
            if (isAdmin) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ProfileMenuItem("🛠️ Quản trị viên (Admin)", "⚙️", onNavigateToAdmin)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Logout
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showLogoutDialog = true }.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🚪", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Đăng xuất", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ErrorRed)
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Xác nhận đăng xuất", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản không?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        scope.launch {
                            authRepo.logout()
                            onLogout()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Đăng xuất")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Hủy", color = Gray600) }
            }
        )
    }
}

@Composable
fun ProfileMenuItem(title: String, icon: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Gray400)
    }
}


// ==========================================
// 2. EDIT PROFILE
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onNavigateBack: () -> Unit = {}) {
    val authRepo = remember { com.example.a43_kltn_ttfood.data.repository.AuthRepository() }
    var userProfile by remember { mutableStateOf<com.example.a43_kltn_ttfood.data.model.User?>(null) }
    
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Nam") }
    
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        isLoading = true
        userProfile = authRepo.getCurrentUserProfile()
        userProfile?.let {
            name = it.fullName
            email = it.email
            phone = it.phone
            dob = it.dob
            gender = it.gender.ifBlank { "Nam" }
        }
        isLoading = false
    }

    Scaffold(
        containerColor = White,
        topBar = {
            TopAppBar(
                title = { Text("Chỉnh sửa hồ sơ", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(20.dp).navigationBarsPadding()) {
                Button(
                    onClick = {
                        val currentUser = userProfile
                        if (currentUser == null) {
                            android.widget.Toast.makeText(context, "Lỗi: Không tìm thấy thông tin người dùng", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (name.isBlank()) {
                            android.widget.Toast.makeText(context, "Họ và tên không được để trống", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSaving = true
                        coroutineScope.launch {
                            val updatedUser = currentUser.copy(
                                fullName = name,
                                phone = phone,
                                dob = dob,
                                gender = gender,
                                updatedAt = com.google.firebase.Timestamp.now()
                            )
                            val result = authRepo.updateUserProfile(updatedUser)
                            isSaving = false
                            if (result.isSuccess) {
                                android.widget.Toast.makeText(context, "Đã lưu thay đổi!", android.widget.Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            } else {
                                val errMsg = result.exceptionOrNull()?.message ?: "Cập nhật thất bại"
                                android.widget.Toast.makeText(context, "Lỗi: $errMsg", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                    enabled = !isSaving && !isLoading
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Lưu thay đổi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Orange500)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                // Avatar
                Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.clickable { /* Mở Gallery */ }) {
                    Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(GradientStart), contentAlignment = Alignment.Center) {
                        Text("👤", fontSize = 50.sp)
                    }
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Orange500).border(2.dp, White, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Edit, "Đổi ảnh", tint = White, modifier = Modifier.size(16.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Họ và tên") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Orange500, focusedLabelColor = Orange500),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSaving
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("Số điện thoại") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Orange500, focusedLabelColor = Orange500),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSaving
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email, onValueChange = {},
                    label = { Text("Email (Không thể thay đổi)") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gray300,
                        unfocusedBorderColor = Gray200,
                        focusedLabelColor = Gray500
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = false
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = dob, onValueChange = { dob = it },
                        label = { Text("Ngày sinh") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Orange500, focusedLabelColor = Orange500),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSaving
                    )
                    var expandedGender by remember { mutableStateOf(false) }
                    val genderOptions = listOf("Nam", "Nữ", "Khác")
                    
                    ExposedDropdownMenuBox(
                        expanded = expandedGender && !isSaving,
                        onExpandedChange = { if (!isSaving) expandedGender = !expandedGender },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Giới tính") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGender) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Orange500, focusedLabelColor = Orange500),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isSaving
                        )
                        ExposedDropdownMenu(
                            expanded = expandedGender && !isSaving,
                            onDismissRequest = { expandedGender = false },
                            modifier = Modifier.background(White)
                        ) {
                            genderOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        gender = option
                                        expandedGender = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 3. ORDER HISTORY
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(onNavigateBack: () -> Unit = {}) {
    var selectedTab by remember { mutableIntStateOf(1) } // 0: Đang xử lý, 1: Hoàn thành, 2: Đã hủy
    val tabs = listOf("Đang xử lý", "Hoàn thành", "Đã hủy")

    Scaffold(
        containerColor = Gray50,
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử đơn hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = White,
                contentColor = Orange500
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal, color = if (selectedTab == index) Orange500 else Gray600) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mocking 5 orders for selected tab
                items(5) {
                    OrderHistoryCard(tabIndex = selectedTab)
                }
            }
        }
    }
}

@Composable
fun OrderHistoryCard(tabIndex: Int) {
    val statusText = when (tabIndex) {
        0 -> "Đang chuẩn bị"
        1 -> "Giao thành công"
        else -> "Đã hủy"
    }
    val statusColor = when (tabIndex) {
        0 -> InfoBlue
        1 -> SuccessGreen
        else -> ErrorRed
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("15 Tháng 5, 2026 • 18:30", style = MaterialTheme.typography.labelMedium, color = Gray500)
                Text(statusText, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = statusColor)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(Orange50), contentAlignment = Alignment.Center) {
                    Text("🍕", fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Pizza Hut - Quận 5", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("1x Pizza Hải Sản, 2x Coca Cola", style = MaterialTheme.typography.bodySmall, color = Gray600, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Tổng: 185.000đ", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Orange500)
                if (tabIndex == 1) {
                    Button(
                        onClick = {},
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Đặt lại", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}


// ==========================================
// 4. FAVORITES
// ==========================================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FavoritesScreen(onNavigateBack: () -> Unit = {}) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var favItems = remember { mutableStateListOf(*sampleFoodItems.toTypedArray()) }

    Scaffold(
        containerColor = Gray50,
        topBar = {
            TopAppBar(
                title = { Text("Món ăn yêu thích", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { paddingValues ->
        if (favItems.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("💔", fontSize = 80.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Chưa có mục yêu thích nào", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Text("Hãy thả tim cho các món ăn bạn thích nhé", color = Gray500)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(favItems, key = { it.id }) { item ->
                    FavoriteItemCard(
                        food = item,
                        modifier = Modifier,
                        onRemove = { favItems.remove(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteItemCard(food: FoodItem, modifier: Modifier = Modifier, onRemove: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(food.bgColor), contentAlignment = Alignment.Center) {
                Text(food.emoji, fontSize = 32.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(food.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(food.restaurant, style = MaterialTheme.typography.bodySmall, color = Gray600)
                Spacer(modifier = Modifier.height(4.dp))
                Text(food.price, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Orange500)
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(36.dp).clip(CircleShape).background(ErrorRed.copy(alpha = 0.1f))) {
                Icon(Icons.Default.Favorite, "Bỏ thích", tint = ErrorRed, modifier = Modifier.size(20.dp))
            }
        }
    }
}


// ==========================================
// 5. SETTINGS
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit = {}) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var pushNotif by remember { mutableStateOf(true) }
    var promoNotif by remember { mutableStateOf(false) }
    var darkMode by remember { mutableStateOf(false) }
    
    var currentLanguage by remember { mutableStateOf("Tiếng Việt") }
    var showLangDialog by remember { mutableStateOf(false) }

    val isEn = currentLanguage == "English"
    val tSettings = if (isEn) "Settings" else "Cài đặt"
    val tNotifications = if (isEn) "Notifications" else "Thông báo"
    val tPushNotif = if (isEn) "Push Notifications (Orders)" else "Thông báo đẩy (Đơn hàng)"
    val tPromoNotif = if (isEn) "Promotional Notifications" else "Thông báo khuyến mãi"
    val tDisplay = if (isEn) "Display" else "Hiển thị"
    val tDarkMode = if (isEn) "Dark Mode" else "Chế độ Tối (Dark Mode)"
    val tLanguage = if (isEn) "Language" else "Ngôn ngữ"
    val tAccount = if (isEn) "Account & Security" else "Tài khoản & Bảo mật"
    val tAddress = if (isEn) "Manage Addresses" else "Quản lý địa chỉ"
    val tPayment = if (isEn) "Payment Methods" else "Phương thức thanh toán"
    val tPassword = if (isEn) "Change Password" else "Đổi mật khẩu"

    Scaffold(
        containerColor = Gray50,
        topBar = {
            TopAppBar(
                title = { Text(tSettings, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(20.dp)
        ) {
            item {
                Text(tNotifications, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Orange500)
                Spacer(modifier = Modifier.height(8.dp))
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column {
                        SettingSwitchRow(tPushNotif, pushNotif) { pushNotif = it }
                        Divider(color = Gray50)
                        SettingSwitchRow(tPromoNotif, promoNotif) { promoNotif = it }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(tDisplay, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Orange500)
                Spacer(modifier = Modifier.height(8.dp))
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column {
                        SettingSwitchRow(tDarkMode, darkMode) { 
                            darkMode = it
                            if (it) {
                                val toastMsg = if (isEn) "Dark Mode theme is under construction!" else "Giao diện Dark Mode đang được hoàn thiện bộ màu!"
                                android.widget.Toast.makeText(context, toastMsg, android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                        Divider(color = Gray50)
                        SettingClickRow(tLanguage, currentLanguage, onClick = { showLangDialog = true })
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(tAccount, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Orange500)
                Spacer(modifier = Modifier.height(8.dp))
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column {
                        SettingClickRow(tAddress, "")
                        Divider(color = Gray50)
                        SettingClickRow(tPayment, "")
                        Divider(color = Gray50)
                        SettingClickRow(tPassword, "")
                    }
                }
            }
        }
    }

    if (showLangDialog) {
        val languages = listOf("Tiếng Việt", "English", "日本語", "한국어")
        AlertDialog(
            onDismissRequest = { showLangDialog = false },
            title = { Text("Chọn Ngôn ngữ", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    languages.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentLanguage = lang
                                    showLangDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentLanguage == lang,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = Orange500)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(lang, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLangDialog = false }) { Text("Đóng", color = Gray500) }
            }
        )
    }
}

@Composable
fun SettingSwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = White, checkedTrackColor = Orange500)
        )
    }
}

@Composable
fun SettingClickRow(title: String, value: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value.isNotEmpty()) {
                Text(value, color = Gray500, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Gray400, modifier = Modifier.size(20.dp))
        }
    }
}
