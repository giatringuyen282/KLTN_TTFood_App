package com.example.a43_kltn_ttfood.data.model

import androidx.compose.ui.graphics.Color

data class Banner(
    val id: Int,
    val emoji: String,
    val title: String,
    val subtitle: String,
    val colorStart: Color,
    val colorEnd: Color
)

data class FoodCategory(
    val id: Int,
    val emoji: String,
    val name: String
)

data class FoodItem(
    val id: Int,
    val emoji: String,
    val name: String,
    val restaurant: String,
    val price: String,
    val rating: Float,
    val bgColor: Color = Color(0xFFFFF3E0)
)

data class Restaurant(
    val id: Int,
    val emoji: String,
    val name: String,
    val rating: Float,
    val distance: String,
    val deliveryTime: String,
    val badge: String? = null,
    val colorStart: Color = Color(0xFFFF6B35),
    val colorEnd: Color = Color(0xFFE53935)
)

data class ReorderItem(
    val id: Int,
    val emoji: String,
    val foodName: String,
    val restaurant: String,
    val price: String
)

// ========================
// Sample Data
// ========================

val sampleBanners = listOf(
    Banner(1, "🔥🍔", "Giảm 50% Burger", "Chỉ hôm nay!", Color(0xFFFF6B35), Color(0xFFE53935)),
    Banner(2, "🎉🍕", "Mua 1 tặng 1 Pizza", "Áp dụng đến hết tuần", Color(0xFF7C4DFF), Color(0xFF536DFE)),
    Banner(3, "🚀💨", "Freeship đơn từ 50K", "Giao nhanh 15 phút", Color(0xFF00BFA5), Color(0xFF1DE9B6)),
    Banner(4, "🌙🍜", "Đêm khuya giảm 30%", "22h - 2h sáng", Color(0xFFE91E63), Color(0xFFFF6090))
)

val sampleCategories = listOf(
    FoodCategory(1, "🍔", "Burger"),
    FoodCategory(2, "🍕", "Pizza"),
    FoodCategory(3, "🍜", "Bún/Phở"),
    FoodCategory(4, "🍚", "Cơm"),
    FoodCategory(5, "🧃", "Đồ uống"),
    FoodCategory(6, "🍰", "Tráng miệng"),
    FoodCategory(7, "🥗", "Healthy"),
    FoodCategory(8, "🍣", "Sushi")
)

val sampleFoodItems = listOf(
    FoodItem(1, "🍜", "Phở bò tái chín", "Phở 24", "55.000₫", 4.8f, Color(0xFFFFF8E1)),
    FoodItem(2, "🍚", "Cơm tấm sườn bì", "Cơm Tấm Bụi", "45.000₫", 4.6f, Color(0xFFFFF3E0)),
    FoodItem(3, "🥖", "Bánh mì thịt nướng", "Bánh Mì Huỳnh Hoa", "35.000₫", 4.9f, Color(0xFFFFECB3)),
    FoodItem(4, "🧋", "Trà sữa trân châu", "Phúc Long", "39.000₫", 4.7f, Color(0xFFE8F5E9)),
    FoodItem(5, "🍗", "Gà rán sốt cay", "KFC Vietnam", "65.000₫", 4.5f, Color(0xFFFFEBEE)),
    FoodItem(6, "🍕", "Pizza hải sản", "Pizza Hut", "129.000₫", 4.4f, Color(0xFFFCE4EC))
)

val sampleRestaurants = listOf(
    Restaurant(1, "🍜🍲", "Phở 24 Nguyễn Văn Cừ", 4.8f, "0.8 km", "15-20 phút", "Phổ biến", Color(0xFFFF6B35), Color(0xFFE53935)),
    Restaurant(2, "🍕🧀", "Pizza Hut Quận 5", 4.5f, "1.2 km", "25-30 phút", "Giảm 20%", Color(0xFF7C4DFF), Color(0xFF536DFE)),
    Restaurant(3, "🍗🔥", "KFC Nguyễn Trãi", 4.3f, "2.0 km", "20-25 phút", "Mới", Color(0xFF00BFA5), Color(0xFF1DE9B6)),
    Restaurant(4, "🍔🍟", "McDonald's Quận 1", 4.6f, "1.5 km", "18-25 phút", null, Color(0xFFE91E63), Color(0xFFFF6090)),
    Restaurant(5, "🧋🍵", "Phúc Long Heritage", 4.7f, "0.5 km", "10-15 phút", "Phổ biến", Color(0xFFFF8F00), Color(0xFFFFB300))
)

val sampleReorders = listOf(
    ReorderItem(1, "🍜", "Phở bò tái chín", "Phở 24", "55.000₫"),
    ReorderItem(2, "🧋", "Trà sữa trân châu đường đen", "Phúc Long", "39.000₫")
)
