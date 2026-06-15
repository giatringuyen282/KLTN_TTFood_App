package com.example.a43_kltn_ttfood.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.firebase.firestore.Exclude

data class Banner(
    val id: Int = 0,
    val emoji: String = "",
    val title: String = "",
    val subtitle: String = "",
    val colorStart: Color = Color(0xFFFF6B35),
    val colorEnd: Color = Color(0xFFE53935)
)

data class FoodCategory(
    val id: Int = 0,
    val emoji: String = "",
    val name: String = ""
)

data class FoodItem(
    val id: Int = 0,
    val emoji: String = "",
    val name: String = "",
    val restaurant: String = "",
    val price: Int = 0,
    val originalPrice: Int = 0,
    val rating: Float = 0f,
    val bgColorVal: Long = 0xFFFFF3E0L,
    val imageUrl: String = "",  // URL ảnh thực tế từ Firebase Storage
    val restaurantId: String = "",
    val categoryId: String = "",
    val description: String = "",
    val toppingGroupIds: List<String> = emptyList()
) {
    // Helper property to expose formatted price string for display in UI
    @get:Exclude
    val formattedPrice: String
        get() = String.format(java.util.Locale.US, "%,d₫", price).replace(",", ".")

    // Secondary constructor for compatibility with Color in sample data and existing code
    constructor(
        id: Int,
        emoji: String,
        name: String,
        restaurant: String,
        priceStr: String,
        rating: Float,
        bgColor: Color = Color(0xFFFFF3E0)
    ) : this(
        id = id,
        emoji = emoji,
        name = name,
        restaurant = restaurant,
        price = priceStr.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0,
        originalPrice = priceStr.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0,
        rating = rating,
        bgColorVal = bgColor.toArgb().toLong(),
        imageUrl = "",
        restaurantId = "",
        categoryId = "",
        description = "",
        toppingGroupIds = emptyList()
    )

    // Expose bgColor property so all existing UI layout references continue to work
    @get:Exclude
    val bgColor: Color
        get() = Color(bgColorVal.toInt())
}

data class Restaurant(
    val id: String = "",
    val emoji: String = "",
    val name: String = "",
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val distance: String = "",
    val deliveryTime: String = "",
    val badge: String? = null,
    val colorStart: Color = Color(0xFFFF6B35),
    val colorEnd: Color = Color(0xFFE53935),
    val logo: String = "",
    val coverImage: String = ""
)

data class ReorderItem(
    val id: Int = 0,
    val emoji: String = "",
    val foodName: String = "",
    val restaurant: String = "",
    val price: String = ""
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
    Restaurant(id = "1", emoji = "🍚🍗", name = "Cơm Gà Mâm Tỏi Chị Đẹp", rating = 4.7f, reviewCount = 1200, distance = "1.5 km", deliveryTime = "27 phút trở lên", badge = "Chỉ có trên Grab", colorStart = Color(0xFFFF6B35), colorEnd = Color(0xFFE53935)),
    Restaurant(id = "2", emoji = "🍗🔥", name = "Gà Nướng Lu Hoàng Côn Lôn", rating = 4.6f, reviewCount = 950, distance = "1.8 km", deliveryTime = "24 phút trở lên", badge = "Chỉ có trên Grab", colorStart = Color(0xFF7C4DFF), colorEnd = Color(0xFF536DFE)),
    Restaurant(id = "3", emoji = "🍗🍟", name = "KFC - Đường Lê Văn Sỹ", rating = 4.4f, reviewCount = 8500, distance = "2.0 km", deliveryTime = "26 phút trở lên", badge = null, colorStart = Color(0xFF00BFA5), colorEnd = Color(0xFF1DE9B6)),
    Restaurant(id = "4", emoji = "🍚🥩", name = "Cơm Tấm Nè - Sườn Nướng", rating = 4.5f, reviewCount = 450, distance = "2.5 km", deliveryTime = "36 phút trở lên", badge = null, colorStart = Color(0xFFE91E63), colorEnd = Color(0xFFFF6090))
)

val sampleReorders = listOf(
    ReorderItem(1, "🍜", "Phở bò tái chín", "Phở 24", "55.000₫"),
    ReorderItem(2, "🧋", "Trà sữa trân châu đường đen", "Phúc Long", "39.000₫")
)
