package com.example.a43_kltn_ttfood.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
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
import com.example.a43_kltn_ttfood.data.model.FoodItemModel
import com.example.a43_kltn_ttfood.data.repository.FavoriteRepository
import com.example.a43_kltn_ttfood.data.repository.FoodRepository
import com.example.a43_kltn_ttfood.ui.theme.*
import com.example.a43_kltn_ttfood.ui.util.LocalImageMapper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToFoodDetail: (Int) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    val favoriteRepo = remember { FavoriteRepository() }
    val foodRepo = remember { FoodRepository() }
    val coroutineScope = rememberCoroutineScope()

    var favoriteFoodIds by remember { mutableStateOf<List<Int>>(emptyList()) }
    var favoriteFoods by remember { mutableStateOf<List<com.example.a43_kltn_ttfood.data.model.FoodItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            favoriteRepo.getFavoriteFoodIds(userId).collect { ids ->
                favoriteFoodIds = ids
                // Fetch full food details for these IDs
                if (ids.isNotEmpty()) {
                    // For simplicity, we fetch all and filter. In a real app with many foods, you'd use whereIn (limit 10) or fetch sequentially
                    foodRepo.getAllFoodItems().collect { allFoods ->
                        favoriteFoods = allFoods.filter { food ->
                            ids.contains(food.id)
                        }
                        isLoading = false
                    }
                } else {
                    favoriteFoods = emptyList()
                    isLoading = false
                }
            }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = Gray50,
        topBar = {
            TopAppBar(
                title = { Text("Món ăn yêu thích", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GrabGreen)
            }
        } else if (favoriteFoods.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(64.dp), tint = Gray300)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Bạn chưa yêu thích món nào", style = MaterialTheme.typography.titleMedium, color = Gray600)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(favoriteFoods, key = { it.id }) { food ->
                    FavoriteFoodItemCard(
                        food = food,
                        onClick = { onNavigateToFoodDetail(food.id) },
                        onRemoveFavorite = {
                            coroutineScope.launch {
                                favoriteRepo.toggleFavorite(userId, food.id, false)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteFoodItemCard(
    food: com.example.a43_kltn_ttfood.data.model.FoodItem,
    onClick: () -> Unit,
    onRemoveFavorite: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fake Image / Emoji
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(food.bgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(food.emoji, fontSize = 40.sp)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = WarningYellow, modifier = Modifier.size(16.dp))
                    Text(" 4.8", style = MaterialTheme.typography.labelMedium, color = Gray700)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = food.formattedPrice,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = GrabGreen
                )
            }
            
            IconButton(onClick = onRemoveFavorite) {
                Icon(Icons.Default.Favorite, contentDescription = "Bỏ yêu thích", tint = ErrorRed)
            }
        }
    }
}
