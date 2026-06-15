package com.example.a43_kltn_ttfood.data.util

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * DatabaseSeeder: Xóa sạch dữ liệu cũ và seed lại toàn bộ dữ liệu thực tế GrabFood
 * 10 Nhà hàng/Chi nhánh: Cơm Gà Chị Đẹp, Gà Nướng Hoàng Côn Lôn, 6 chi nhánh KFC, 2 chi nhánh Cơm Tấm Nè
 */
object DatabaseSeeder {
    private val db = FirebaseFirestore.getInstance()
    private val categoriesCol = db.collection("categories")
    private val foodCol = db.collection("food_items")
    private val restaurantsCol = db.collection("restaurants")
    private val toppingCol = db.collection("topping_groups")

    // ─── Sentinel key để biết đã seed version này chưa ───────────────────────
    private val metaCol = db.collection("_meta")
    private const val SEED_VERSION = "v7_grabfood_full_sync"

    /**
     * Gọi khi app khởi động.
     * - Nếu chưa seed version hiện tại → xóa sạch, seed lại.
     * - Nếu đã seed → bỏ qua.
     */
    suspend fun seedIfNeeded() {
        try {
            val meta = metaCol.document("seed").get().await()
            val seededVersion = meta.getString("version") ?: ""
            if (seededVersion == SEED_VERSION) {
                Log.d("DatabaseSeeder", "Đã seed version $SEED_VERSION, bỏ qua.")
                return
            }
            Log.d("DatabaseSeeder", "Bắt đầu seed lại từ đầu (version $SEED_VERSION)...")
            clearAndSeedAll()
        } catch (e: Exception) {
            Log.e("DatabaseSeeder", "seedIfNeeded lỗi: ${e.message}", e)
        }
    }

    // ─── Xóa toàn bộ rồi seed lại ─────────────────────────────────────────────
    private suspend fun clearAndSeedAll() {
        deleteCollection("categories")
        deleteCollection("food_items")
        deleteCollection("restaurants")
        deleteCollection("vouchers")
        deleteCollection("topping_groups")

        seedCategories()
        val restaurantIds = seedRestaurants()
        val toppingIds = seedToppingGroups(restaurantIds)
        seedFoodItems(restaurantIds, toppingIds)
        seedVouchers()

        // Lưu version đã seed
        metaCol.document("seed").set(mapOf("version" to SEED_VERSION)).await()
        Log.d("DatabaseSeeder", "Seed hoàn tất!")
    }

    private suspend fun deleteCollection(collectionName: String) {
        try {
            val docs = db.collection(collectionName).get().await()
            for (doc in docs.documents) {
                doc.reference.delete().await()
            }
            Log.d("DatabaseSeeder", "Đã xóa collection '$collectionName' (${docs.size()} docs)")
        } catch (e: Exception) {
            Log.w("DatabaseSeeder", "Không thể xóa '$collectionName': ${e.message}")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CATEGORIES
    // ─────────────────────────────────────────────────────────────────────────
    private suspend fun seedCategories() {
        val categories = listOf(
            mapOf("id" to 1, "emoji" to "🍗", "name" to "Gà rán", "sortOrder" to 1),
            mapOf("id" to 2, "emoji" to "☕", "name" to "Cà phê", "sortOrder" to 2),
            mapOf("id" to 3, "emoji" to "🧋", "name" to "Trà & Nước", "sortOrder" to 3),
            mapOf("id" to 4, "emoji" to "🍚", "name" to "Cơm", "sortOrder" to 4),
            mapOf("id" to 5, "emoji" to "🍜", "name" to "Phở & Bún", "sortOrder" to 5),
            mapOf("id" to 6, "emoji" to "🍕", "name" to "Pizza", "sortOrder" to 6),
            mapOf("id" to 7, "emoji" to "🍔", "name" to "Burger", "sortOrder" to 7),
            mapOf("id" to 8, "emoji" to "🥩", "name" to "Bò & Teppanyaki", "sortOrder" to 8),
            mapOf("id" to 9, "emoji" to "🍰", "name" to "Bánh & Tráng miệng", "sortOrder" to 9),
            mapOf("id" to 10, "emoji" to "🥤", "name" to "Đồ uống", "sortOrder" to 10)
        )
        categories.forEach { categoriesCol.add(it).await() }
        Log.d("DatabaseSeeder", "Seeded ${categories.size} categories")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RESTAURANTS — trả về Map<tên ngắn/chi nhánh → documentId>
    // ─────────────────────────────────────────────────────────────────────────
    private suspend fun seedRestaurants(): Map<String, String> {
        val restaurants: List<Map<String, Any>> = listOf(
            mapOf(
                "name" to "Cơm Gà Mâm Tỏi Chị Đẹp",
                "description" to "Chỉ có trên Grab. Cơm gà mâm tỏi thơm lừng tỏi phi, thịt dai thơm chuẩn vị chị Đẹp.",
                "address" to "123 Lê Văn Sỹ, Phường 14, Quận 3, TP.HCM",
                "rating" to 4.7,
                "reviewCount" to 368,
                "deliveryFee" to 0,
                "openTime" to "07:00",
                "closeTime" to "22:00",
                "isOpen" to true,
                "emoji" to "🍚",
                "logo" to "https://images.unsplash.com/photo-1562967914-608f82629710?w=500",
                "coverImage" to "https://images.unsplash.com/photo-1562967914-608f82629710?w=800",
                "ownerId" to ""
            ),
            mapOf(
                "name" to "Gà Nướng Lu Hoàng Côn Lôn",
                "description" to "Chỉ có trên Grab. Gà nướng lu chuẩn vị, da giòn sần sật tẩm vị đậm đà.",
                "address" to "45 Phạm Ngọc Thạch, Phường 6, Quận 3, TP.HCM",
                "rating" to 4.6,
                "reviewCount" to 677,
                "deliveryFee" to 0,
                "openTime" to "08:00",
                "closeTime" to "22:00",
                "isOpen" to true,
                "emoji" to "🍗",
                "logo" to "https://images.unsplash.com/photo-1598515214211-89d3e73ae83b?w=500",
                "coverImage" to "https://images.unsplash.com/photo-1598515214211-89d3e73ae83b?w=800",
                "ownerId" to ""
            ),
            mapOf(
                "name" to "KFC - Đường Lê Văn Sỹ",
                "description" to "Gà rán giòn rụm nức tiếng thế giới - Chi nhánh Đường Lê Văn Sỹ.",
                "address" to "320 Đường Lê Văn Sỹ, Phường 14, Quận 3, TP.HCM",
                "rating" to 4.4,
                "reviewCount" to 4100,
                "deliveryFee" to 0,
                "openTime" to "09:00",
                "closeTime" to "22:00",
                "isOpen" to true,
                "emoji" to "🍗",
                "logo" to "https://th.bing.com/th/id/R.cf5766479f3666c5546c71bb265bbc01?rik=36Q39hnEIzQXKw&pid=ImgRaw&r=0",
                "coverImage" to "https://images.unsplash.com/photo-1513639776629-7b61b0ac237b?w=800",
                "ownerId" to ""
            ),
            mapOf(
                "name" to "KFC - Đường Phạm Ngọc Thạch",
                "description" to "Gà rán giòn rụm nức tiếng thế giới - Chi nhánh Đường Phạm Ngọc Thạch.",
                "address" to "2 Đường Phạm Ngọc Thạch, Phường Bến Nghé, Quận 1, TP.HCM",
                "rating" to 4.3,
                "reviewCount" to 3200,
                "deliveryFee" to 0,
                "openTime" to "09:00",
                "closeTime" to "22:00",
                "isOpen" to true,
                "emoji" to "🍗",
                "logo" to "https://th.bing.com/th/id/R.cf5766479f3666c5546c71bb265bbc01?rik=36Q39hnEIzQXKw&pid=ImgRaw&r=0",
                "coverImage" to "https://images.unsplash.com/photo-1513639776629-7b61b0ac237b?w=800",
                "ownerId" to ""
            ),
            mapOf(
                "name" to "KFC - TTTM Vincom Plaza Ba Tháng Hai",
                "description" to "Gà rán giòn rụm nức tiếng thế giới - Chi nhánh TTTM Vincom Plaza Ba Tháng Hai.",
                "address" to "Vincom Plaza 3/2, Phường 11, Quận 10, TP.HCM",
                "rating" to 4.1,
                "reviewCount" to 223,
                "deliveryFee" to 0,
                "openTime" to "09:00",
                "closeTime" to "22:00",
                "isOpen" to true,
                "emoji" to "🍗",
                "logo" to "https://th.bing.com/th/id/R.cf5766479f3666c5546c71bb265bbc01?rik=36Q39hnEIzQXKw&pid=ImgRaw&r=0",
                "coverImage" to "https://images.unsplash.com/photo-1513639776629-7b61b0ac237b?w=800",
                "ownerId" to ""
            ),
            mapOf(
                "name" to "KFC - Đường Thích Quảng Đức",
                "description" to "Gà rán giòn rụm nức tiếng thế giới - Chi nhánh Đường Thích Quảng Đức.",
                "address" to "89 Đường Thích Quảng Đức, Phường 5, Quận Phú Nhuận, TP.HCM",
                "rating" to 4.4,
                "reviewCount" to 2100,
                "deliveryFee" to 0,
                "openTime" to "09:00",
                "closeTime" to "22:00",
                "isOpen" to true,
                "emoji" to "🍗",
                "logo" to "https://th.bing.com/th/id/R.cf5766479f3666c5546c71bb265bbc01?rik=36Q39hnEIzQXKw&pid=ImgRaw&r=0",
                "coverImage" to "https://images.unsplash.com/photo-1513639776629-7b61b0ac237b?w=800",
                "ownerId" to ""
            ),
            mapOf(
                "name" to "KFC - Đường Nguyễn Văn Giai",
                "description" to "Gà rán giòn rụm nức tiếng thế giới - Chi nhánh Đường Nguyễn Văn Giai.",
                "address" to "12 Đường Nguyễn Văn Giai, Phường Đa Kao, Quận 1, TP.HCM",
                "rating" to 4.4,
                "reviewCount" to 3000,
                "deliveryFee" to 0,
                "openTime" to "09:00",
                "closeTime" to "22:00",
                "isOpen" to true,
                "emoji" to "🍗",
                "logo" to "https://th.bing.com/th/id/R.cf5766479f3666c5546c71bb265bbc01?rik=36Q39hnEIzQXKw&pid=ImgRaw&r=0",
                "coverImage" to "https://images.unsplash.com/photo-1513639776629-7b61b0ac237b?w=800",
                "ownerId" to ""
            ),
            mapOf(
                "name" to "KFC - TTTM Maximark Cộng Hòa",
                "description" to "Gà rán giòn rụm nức tiếng thế giới - Chi nhánh TTTM Maximark Cộng Hòa.",
                "address" to "15-17 Cộng Hòa, Phường 4, Quận Tân Bình, TP.HCM",
                "rating" to 4.1,
                "reviewCount" to 2050,
                "deliveryFee" to 0,
                "openTime" to "09:00",
                "closeTime" to "22:00",
                "isOpen" to true,
                "emoji" to "🍗",
                "logo" to "https://th.bing.com/th/id/R.cf5766479f3666c5546c71bb265bbc01?rik=36Q39hnEIzQXKw&pid=ImgRaw&r=0",
                "coverImage" to "https://images.unsplash.com/photo-1513639776629-7b61b0ac237b?w=800",
                "ownerId" to ""
            ),
            mapOf(
                "name" to "Cơm Tấm Nè - Sườn Nướng",
                "description" to "Cơm Tấm Nè nổi danh sườn nướng mật ong vàng óng, bì chả cực ngon.",
                "address" to "12 Đặng Văn Bi, Phường Bình Thọ, TP. Thủ Đức, TP.HCM",
                "rating" to 4.5,
                "reviewCount" to 1200,
                "deliveryFee" to 4000,
                "openTime" to "06:00",
                "closeTime" to "21:30",
                "isOpen" to true,
                "emoji" to "🍚",
                "logo" to "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500",
                "coverImage" to "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=800",
                "ownerId" to ""
            ),
            mapOf(
                "name" to "Cơm Tấm Nè - Lê Hồng Phong",
                "description" to "Cơm Tấm Nè nổi danh sườn nướng mật ong vàng óng - Chi nhánh Lê Hồng Phong.",
                "address" to "245 Lê Hồng Phong, Phường 4, Quận 5, TP.HCM",
                "rating" to 4.5,
                "reviewCount" to 850,
                "deliveryFee" to 4000,
                "openTime" to "06:00",
                "closeTime" to "21:30",
                "isOpen" to true,
                "emoji" to "🍚",
                "logo" to "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500",
                "coverImage" to "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=800",
                "ownerId" to ""
            )
        )

        val idMap = mutableMapOf<String, String>()
        restaurants.forEach { restaurant ->
            val mutableRes = restaurant.toMutableMap()
            mutableRes["createdAt"] = com.google.firebase.Timestamp.now()
            val docRef = restaurantsCol.add(mutableRes).await()
            idMap[restaurant["name"] as String] = docRef.id
        }
        Log.d("DatabaseSeeder", "Seeded ${restaurants.size} restaurants")
        return idMap
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TOPPING GROUPS
    // ─────────────────────────────────────────────────────────────────────────
    private suspend fun seedToppingGroups(restaurantIds: Map<String, String>): Map<String, List<String>> {
        val result = mutableMapOf<String, List<String>>()
        for ((resName, resId) in restaurantIds) {
            if (resName.startsWith("KFC")) {
                val group1 = mapOf(
                    "restaurantId" to resId,
                    "name" to "main",
                    "isRequired" to true,
                    "maxSelect" to 1,
                    "options" to listOf(
                        mapOf("name" to "2 Fried Chicken - Gà Giòn Cay", "price" to 0),
                        mapOf("name" to "2 Fried Chicken - Gà Giòn Không Cay", "price" to 0),
                        mapOf("name" to "2 Fried Chicken - Gà Truyền Thống (mềm)", "price" to 0)
                    )
                )
                val group2 = mapOf(
                    "restaurantId" to resId,
                    "name" to "side",
                    "isRequired" to false,
                    "maxSelect" to 1,
                    "options" to listOf(
                        mapOf("name" to "Khoai Tây Chiên (Vừa)", "price" to 0)
                    )
                )
                val group3 = mapOf(
                    "restaurantId" to resId,
                    "name" to "Drink",
                    "isRequired" to false,
                    "maxSelect" to 1,
                    "options" to listOf(
                        mapOf("name" to "Pepsi (J) CBO", "price" to 0)
                    )
                )
                val id1 = toppingCol.add(group1).await().id
                val id2 = toppingCol.add(group2).await().id
                val id3 = toppingCol.add(group3).await().id
                result[resName] = listOf(id1, id2, id3)
            }
        }
        Log.d("DatabaseSeeder", "Seeded topping groups for KFC branches")
        return result
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FOOD ITEMS
    // ─────────────────────────────────────────────────────────────────────────
    private suspend fun seedFoodItems(restaurantIds: Map<String, String>, toppingIdsMap: Map<String, List<String>>) {
        val allFoods = mutableListOf<Map<String, Any>>()
        var idCounter = 1

        for ((resName, resId) in restaurantIds) {
            if (resName.startsWith("KFC")) {
                val tIds = toppingIdsMap[resName] ?: emptyList()
                allFoods += listOf(
                    food(idCounter++, resId, "1", "Combo Tiêu Tung Chill 85k", 85000, 97000,
                        "https://static.kfcvietnam.com.vn/images/items/lg/Combo-Tieu-Tung-Chill.jpg?v=gdOn84",
                        "🍗", "1 Miếng Gà Rán + 1 Miếng Gà Lắc Tiêu Chanh + 1 ly Pepsi Không Đường (Đại)", emptyList(), resName),
                    food(idCounter++, resId, "9", "4 Bánh Trứng", 72000, 72000,
                        "https://static.kfcvietnam.com.vn/images/items/lg/4-eggtart.jpg?v=gdOn84",
                        "🥧", "Bánh trứng nướng giòn thơm béo ngậy kiểu Bồ Đào Nha.", listOf("Bán chạy"), resName, 4.8f),
                    food(idCounter++, resId, "1", "6 Miếng Gà Rán", 210000, 210000,
                        "https://static.kfcvietnam.com.vn/images/items/lg/6-Pieces.jpg?v=gdOn84",
                        "🍗", "6 Miếng Gà Rán giòn rụm thơm ngon chuẩn vị KFC.", emptyList(), resName),
                    food(idCounter++, resId, "1", "Combo 2 Miếng Gà", 91000, 91000,
                        "https://static.kfcvietnam.com.vn/images/items/lg/Combo-2pcs.jpg?v=gdOn84",
                        "🍗", "2 Miếng Gà + 1 Khoai Tây Chiên (Vừa) + 1 Pepsi (Vừa)", emptyList(), resName),
                    food(idCounter++, resId, "1", "3 Miếng Gà Rán", 105000, 105000,
                        "https://static.kfcvietnam.com.vn/images/items/lg/3-Pieces.jpg?v=gdOn84",
                        "🍗", "3 Miếng Gà Rán giòn rụm chuẩn vị KFC.", emptyList(), resName),
                    food(idCounter++, resId, "1", "COMBO ĐỘC QUYỀN GRAB 85K", 85000, 114000,
                        "https://static.kfcvietnam.com.vn/images/items/lg/Combo-Doc-Quyen.jpg?v=gdOn84",
                        "🍗", "2 Miếng Gà + 1 Khoai Tây Chiên (Vừa) + 1 Pepsi (Đại)", emptyList(), resName, 4.6f, tIds),
                    food(idCounter++, resId, "4", "1 Cơm Phi-lê Gà Quay", 54000, 54000,
                        "https://static.kfcvietnam.com.vn/images/items/lg/Rice-Flav.jpg?v=gdOn84",
                        "🍚", "1 Cơm Phi-lê Gà Quay xào sốt đậm vị thơm ngon.", emptyList(), resName),
                    food(idCounter++, resId, "1", "COMBO CUỐI TUẦN 119K", 119000, 156000,
                        "https://static.kfcvietnam.com.vn/images/items/lg/Combo-Weekend.jpg?v=gdOn84",
                        "🍗", "2 Miếng Gà + 1 Burger Tôm + 1 Khoai Tây Chiên (Vừa) + 1 Pepsi (Vừa)", emptyList(), resName)
                )
            } else if (resName.startsWith("Cơm Gà")) {
                allFoods += listOf(
                    food(idCounter++, resId, "4", "Cơm Đùi Gà Mâm Tỏi", 59000, 69000,
                        "https://images.unsplash.com/photo-1562967914-608f82629710?w=500",
                        "🍚", "Đùi gà chiên tỏi thơm phức ăn kèm cơm dẻo chuẩn vị chị Đẹp.", listOf("Bán chạy"), resName),
                    food(idCounter++, resId, "4", "Cơm Cánh Gà Mâm Tỏi", 49000, 49000,
                        "https://images.unsplash.com/photo-1562967914-608f82629710?w=500",
                        "🍚", "Cánh gà chiên giòn cháy tỏi mặn ngọt đậm đà.", emptyList(), resName),
                    food(idCounter++, resId, "4", "Gà Tỏi Chặt Mâm", 189000, 219000,
                        "https://images.unsplash.com/photo-1562967914-608f82629710?w=500",
                        "🍗", "Gà ta chặt mâm tẩm tỏi phi thơm phức cho cả gia đình.", emptyList(), resName)
                )
            } else if (resName.startsWith("Gà Nướng")) {
                allFoods += listOf(
                    food(idCounter++, resId, "1", "Gà Nướng Lu Thượng Hạng", 199000, 239000,
                        "https://images.unsplash.com/photo-1598515214211-89d3e73ae83b?w=500",
                        "🍗", "Gà nướng lu nguyên con da vàng óng nước sốt Hoàng Côn Lôn.", listOf("Bán chạy"), resName),
                    food(idCounter++, resId, "1", "Nửa Gà Nướng Lu", 109000, 119000,
                        "https://images.unsplash.com/photo-1598515214211-89d3e73ae83b?w=500",
                        "🍗", "Nửa con gà nướng lu nóng hổi chuẩn vị lu đất.", emptyList(), resName),
                    food(idCounter++, resId, "1", "Đùi Gà Nướng Lu", 45000, 45000,
                        "https://images.unsplash.com/photo-1598515214211-89d3e73ae83b?w=500",
                        "🍗", "Đùi gà nướng lu thơm ngon ngập sốt.", emptyList(), resName)
                )
            } else if (resName.startsWith("Cơm Tấm")) {
                allFoods += listOf(
                    food(idCounter++, resId, "4", "Cơm Tấm Sườn Nướng", 45000, 45000,
                        "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500",
                        "🍚", "Cơm tấm nóng hổi ăn kèm sườn cốt lết nướng mật ong thơm phức.", listOf("Bán chạy"), resName),
                    food(idCounter++, resId, "4", "Cơm Tấm Sườn Bì Chả", 55000, 65000,
                        "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500",
                        "🍚", "Đĩa cơm tấm đầy đủ sườn cốt lết dày, bì chả tự làm và trứng ốp la.", listOf("Bán chạy"), resName),
                    food(idCounter++, resId, "4", "Cơm Tấm Ba Chỉ Nướng", 49000, 49000,
                        "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500",
                        "🍚", "Cơm tấm thịt ba chỉ giòn rụm nướng cạnh lò than cực thơm ngon.", emptyList(), resName)
                )
            }
        }

        allFoods.forEach { foodCol.add(it).await() }
        Log.d("DatabaseSeeder", "Seeded ${allFoods.size} food items")
    }

    private fun food(
        id: Int,
        restaurantId: String,
        categoryId: String,
        name: String,
        price: Int,
        originalPrice: Int,
        imageUrl: String,
        emoji: String,
        description: String,
        badges: List<String>,
        restaurantName: String = "",
        rating: Float = 4.5f,
        toppingGroupIds: List<String> = emptyList()
    ): Map<String, Any> = mapOf(
        "id" to id,
        "restaurantId" to restaurantId,
        "categoryId" to categoryId,
        "name" to name,
        "price" to price,
        "originalPrice" to originalPrice,
        "imageUrl" to imageUrl,
        "emoji" to emoji,
        "description" to description,
        "restaurant" to restaurantName,
        "rating" to rating,
        "bgColorVal" to 0xFFFFF3E0L,
        "isAvailable" to true,
        "calories" to 0,
        "allergens" to emptyList<String>(),
        "badges" to badges,
        "toppingGroupIds" to toppingGroupIds
    )

    private suspend fun seedVouchers() {
        val vouchersCol = db.collection("vouchers")
        val vouchers = listOf(
            mapOf(
                "code" to "TTFOOD50",
                "discountType" to "fixed",
                "discountValue" to 50000,
                "minOrder" to 100000,
                "maxDiscount" to 50000,
                "usageLimit" to 1000,
                "usedCount" to 0,
                "isActive" to true,
                "expiresAt" to com.google.firebase.Timestamp(java.util.Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)),
                "createdAt" to com.google.firebase.Timestamp.now()
            ),
            mapOf(
                "code" to "KFC51",
                "discountType" to "fixed",
                "discountValue" to 51000,
                "minOrder" to 99000,
                "maxDiscount" to 51000,
                "usageLimit" to 2000,
                "usedCount" to 0,
                "isActive" to true,
                "expiresAt" to com.google.firebase.Timestamp(java.util.Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)),
                "createdAt" to com.google.firebase.Timestamp.now()
            )
        )
        for (v in vouchers) {
            vouchersCol.add(v).await()
        }
        Log.d("DatabaseSeeder", "Seeded ${vouchers.size} vouchers")
    }
}
