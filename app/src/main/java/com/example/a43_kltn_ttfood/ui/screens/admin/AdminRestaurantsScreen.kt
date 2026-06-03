package com.example.a43_kltn_ttfood.ui.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a43_kltn_ttfood.data.model.*
import com.example.a43_kltn_ttfood.data.repository.RestaurantRepository
import com.example.a43_kltn_ttfood.ui.theme.*
import kotlinx.coroutines.launch

// =====================================================
// 🏪 QUẢN LÝ NHÀ HÀNG (ADMIN)
// =====================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRestaurantsScreen(
    onNavigateBack: () -> Unit
) {
    val restaurantRepo = remember { RestaurantRepository() }
    val scope = rememberCoroutineScope()
    var restaurants by remember { mutableStateOf<List<RestaurantModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        restaurantRepo.getAllRestaurants().collect { result ->
            restaurants = result
            isLoading = false
        }
    }

    val filteredRestaurants = remember(restaurants, searchQuery) {
        if (searchQuery.isBlank()) restaurants
        else restaurants.filter {
            it.name.lowercase().contains(searchQuery.lowercase().trim()) ||
            it.address.lowercase().contains(searchQuery.lowercase().trim())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Nhà hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
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
            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Tìm nhà hàng theo tên, địa chỉ...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Gray500) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, "Xóa")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange500,
                    cursorColor = Orange500
                )
            )

            // Counter
            Text(
                "${filteredRestaurants.size} nhà hàng",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Orange500)
                }
            } else if (filteredRestaurants.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏪", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Chưa có nhà hàng nào", color = Gray500)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredRestaurants, key = { it.id }) { restaurant ->
                        RestaurantAdminCard(
                            restaurant = restaurant,
                            onToggleOpen = {
                                scope.launch {
                                    restaurantRepo.toggleRestaurantOpen(
                                        restaurantId = restaurant.id,
                                        isOpen = !restaurant.isOpen,
                                        adminId = "admin",
                                        adminName = "Admin"
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
}

@Composable
private fun RestaurantAdminCard(
    restaurant: RestaurantModel,
    onToggleOpen: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (restaurant.isOpen) Gray200 else ErrorRed.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Orange500.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(restaurant.emoji.ifBlank { "🍽️" }, fontSize = 24.sp)
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        restaurant.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (restaurant.isOpen) "Mở" else "Đóng",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (restaurant.isOpen) SuccessGreen else ErrorRed,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(
                                (if (restaurant.isOpen) SuccessGreen else ErrorRed).copy(alpha = 0.1f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }

                Text(
                    restaurant.address.ifBlank { "Chưa có địa chỉ" },
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text("⭐ ${restaurant.rating}", style = MaterialTheme.typography.labelSmall, color = WarningYellow)
                    Spacer(Modifier.width(8.dp))
                    Text("${restaurant.reviewCount} đánh giá", style = MaterialTheme.typography.labelSmall, color = Gray400)
                    Spacer(Modifier.width(8.dp))
                    Text("${restaurant.openTime} - ${restaurant.closeTime}", style = MaterialTheme.typography.labelSmall, color = Gray400)
                }
            }

            // Toggle open/close
            IconButton(onClick = { showConfirmDialog = true }) {
                Icon(
                    if (restaurant.isOpen) Icons.Outlined.ToggleOn else Icons.Outlined.ToggleOff,
                    contentDescription = if (restaurant.isOpen) "Đóng" else "Mở",
                    tint = if (restaurant.isOpen) SuccessGreen else Gray400,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(if (restaurant.isOpen) "Đóng nhà hàng?" else "Mở nhà hàng?")
            },
            text = {
                Text(
                    if (restaurant.isOpen) "Nhà hàng \"${restaurant.name}\" sẽ ngừng nhận đơn."
                    else "Nhà hàng \"${restaurant.name}\" sẽ bắt đầu nhận đơn."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onToggleOpen()
                    showConfirmDialog = false
                }) {
                    Text(
                        if (restaurant.isOpen) "Đóng" else "Mở",
                        color = if (restaurant.isOpen) ErrorRed else SuccessGreen
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}
