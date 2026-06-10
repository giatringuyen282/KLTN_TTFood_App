package com.example.a43_kltn_ttfood.ui.screens.search

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a43_kltn_ttfood.data.model.*
import com.example.a43_kltn_ttfood.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 🔍 Màn hình Tìm kiếm
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToFood: (Int) -> Unit = {},
    onNavigateToRestaurant: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    var showFilterSheet by remember { mutableStateOf(false) }
    
    val searchHistory = remember { mutableStateListOf("Phở bò", "Cơm tấm", "Trà sữa", "Bánh mì", "Pizza") }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tất cả", "Nhà hàng", "Món ăn")

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(searchQuery) {
        delay(300)
        debouncedQuery = searchQuery
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            containerColor = White
        ) {
            FilterSheetContent(
                onApply = {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion { showFilterSheet = false }
                },
                onReset = {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion { showFilterSheet = false }
                }
            )
        }
    }

    Scaffold(
        containerColor = Gray50,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Gray100)
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Gray400,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Tìm món ăn, nhà hàng...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Gray400
                                        )
                                    }
                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onBackground
                                        ),
                                        cursorBrush = SolidColor(Orange500),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(focusRequester)
                                    )
                                }
                                AnimatedVisibility(
                                    visible = searchQuery.isNotEmpty(),
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Xóa",
                                            tint = Gray500,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        // Nút lọc
                        IconButton(
                            onClick = { showFilterSheet = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Orange50)
                        ) {
                            Text(text = "🎛️", fontSize = 18.sp)
                        }
                    }
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (debouncedQuery.isEmpty()) {
                // Search History
                if (searchHistory.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🕐 Lịch sử tìm kiếm",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        TextButton(onClick = { searchHistory.clear() }) {
                            Text("Xóa tất cả", style = MaterialTheme.typography.labelSmall, color = Orange500)
                        }
                    }
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(searchHistory, key = { it }) { item ->
                            HistoryItem(
                                text = item,
                                onClick = { searchQuery = item },
                                onDelete = { searchHistory.remove(item) }
                            )
                        }
                    }
                }
            } else {
                // Search Results
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = White,
                    contentColor = Orange500
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                Text(
                                    title, 
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) Orange500 else Gray500
                                ) 
                            }
                        )
                    }
                }
                
                val matchedFoods = sampleFoodItems.filter { it.name.contains(debouncedQuery, ignoreCase = true) }
                val matchedRestaurants = sampleRestaurants.filter { it.name.contains(debouncedQuery, ignoreCase = true) }
                
                if (matchedFoods.isEmpty() && matchedRestaurants.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "🤷‍♂️", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Không tìm thấy kết quả nào cho \"$debouncedQuery\"",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Thử lại với các từ khóa phổ biến: Phở, Cơm tấm, Trà sữa...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray500,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (selectedTab == 0 || selectedTab == 1) {
                            if (matchedRestaurants.isNotEmpty()) {
                                item {
                                    Text("Nhà hàng", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 8.dp))
                                }
                                items(matchedRestaurants) { res ->
                                    RestaurantSearchResultItem(restaurant = res, query = debouncedQuery) {
                                        onNavigateToRestaurant(res.id)
                                    }
                                }
                            }
                        }
                        
                        if (selectedTab == 0 || selectedTab == 2) {
                            if (matchedFoods.isNotEmpty()) {
                                item {
                                    Text("Món ăn", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(vertical = 8.dp))
                                }
                                items(matchedFoods) { food ->
                                    FoodSearchResultItem(food = food, query = debouncedQuery) {
                                        onNavigateToFood(food.id)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(text: String, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Gray50)
            .clickable(onClick = onClick)
            .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🕒", fontSize = 16.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Close, contentDescription = "Xóa", tint = Gray400, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun HighlightedText(text: String, query: String, style: androidx.compose.ui.text.TextStyle, modifier: Modifier = Modifier) {
    if (query.isEmpty() || !text.contains(query, ignoreCase = true)) {
        Text(text = text, style = style, modifier = modifier, maxLines = 1, overflow = TextOverflow.Ellipsis)
        return
    }
    
    val annotatedString = buildAnnotatedString {
        val startIndex = text.indexOf(query, ignoreCase = true)
        if (startIndex >= 0) {
            append(text.substring(0, startIndex))
            withStyle(style = SpanStyle(color = Orange500, fontWeight = FontWeight.Bold)) {
                append(text.substring(startIndex, startIndex + query.length))
            }
            append(text.substring(startIndex + query.length))
        } else {
            append(text)
        }
    }
    Text(text = annotatedString, style = style, modifier = modifier, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

@Composable
fun RestaurantSearchResultItem(restaurant: Restaurant, query: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(Orange100),
                contentAlignment = Alignment.Center
            ) { Text(restaurant.emoji, fontSize = 28.sp) }
            Spacer(Modifier.width(12.dp))
            Column {
                HighlightedText(
                    text = restaurant.name,
                    query = query,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "⭐ ${restaurant.rating} · ${restaurant.distance} · ${restaurant.deliveryTime}",
                    style = MaterialTheme.typography.bodySmall, color = Gray600
                )
            }
        }
    }
}

@Composable
fun FoodSearchResultItem(food: FoodItem, query: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(food.bgColor),
                contentAlignment = Alignment.Center
            ) { Text(food.emoji, fontSize = 28.sp) }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                HighlightedText(
                    text = food.name,
                    query = query,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(4.dp))
                Text(text = food.restaurant, style = MaterialTheme.typography.bodySmall, color = Gray600)
            }
            Text(food.formattedPrice, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Orange500)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheetContent(onApply: () -> Unit, onReset: () -> Unit) {
    var selectedSort by remember { mutableStateOf("Phổ biến") }
    var freeShipping by remember { mutableStateOf(false) }
    var openNow by remember { mutableStateOf(false) }
    var isNew by remember { mutableStateOf(false) }
    
    var selectedCategory by remember { mutableStateOf("Tất cả") }
    var selectedRating by remember { mutableStateOf("Tất cả") }
    var selectedPrice by remember { mutableStateOf("Tất cả") }
    var selectedDistance by remember { mutableStateOf("Tất cả") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("🎛️ Bộ lọc nâng cao", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Sắp xếp theo", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Phổ biến", "Gần nhất", "Giá thấp").forEach { sortOpt ->
                FilterChip(
                    selected = selectedSort == sortOpt,
                    onClick = { selectedSort = sortOpt },
                    label = { Text(sortOpt) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Orange100,
                        selectedLabelColor = Orange500
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Lọc nhanh", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = openNow, onCheckedChange = { openNow = it }, colors = CheckboxDefaults.colors(checkedColor = Orange500))
                Text("Đang mở cửa", style = MaterialTheme.typography.bodyMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = freeShipping, onCheckedChange = { freeShipping = it }, colors = CheckboxDefaults.colors(checkedColor = Orange500))
                Text("Giao hàng miễn phí", style = MaterialTheme.typography.bodyMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = isNew, onCheckedChange = { isNew = it }, colors = CheckboxDefaults.colors(checkedColor = Orange500))
                Text("Mới nhất", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Danh mục", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(12.dp))
        // To avoid importing all categories, just use some mock text for simplicity
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Tất cả", "Cơm", "Phở", "Trà sữa", "Pizza").forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Orange100,
                        selectedLabelColor = Orange500
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Khoảng cách", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Tất cả", "< 1km", "1-3km", "> 3km").forEach { dist ->
                FilterChip(
                    selected = selectedDistance == dist,
                    onClick = { selectedDistance = dist },
                    label = { Text(dist) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Orange100,
                        selectedLabelColor = Orange500
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Đánh giá", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Tất cả", "⭐ 4.5+", "⭐ 4.0+", "⭐ 3.0+").forEach { rating ->
                FilterChip(
                    selected = selectedRating == rating,
                    onClick = { selectedRating = rating },
                    label = { Text(rating) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Orange100,
                        selectedLabelColor = Orange500
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Khoảng giá", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Tất cả", "< 50K", "50K - 100K", "> 100K").forEach { price ->
                FilterChip(
                    selected = selectedPrice == price,
                    onClick = { selectedPrice = price },
                    label = { Text(price) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Orange100,
                        selectedLabelColor = Orange500
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = {
                    selectedSort = "Phổ biến"
                    freeShipping = false
                    openNow = false
                    isNew = false
                    selectedCategory = "Tất cả"
                    selectedRating = "Tất cả"
                    selectedPrice = "Tất cả"
                    selectedDistance = "Tất cả"
                    onReset()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Đặt lại", color = Gray600)
            }
            Button(
                onClick = onApply,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange500)
            ) {
                Text("Áp dụng", color = White)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
