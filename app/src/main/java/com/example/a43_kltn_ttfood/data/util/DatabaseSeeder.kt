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
    private const val SEED_VERSION = "v8_grabfood_with_original_restaurants"

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
            ),
            mapOf(
                "name" to "Starbucks",
                "description" to "Chuỗi cà phê nổi tiếng thế giới với thức uống đặc trưng và không gian sang trọng.",
                "address" to "Nhiều chi nhánh tại TP.HCM",
                "rating" to 4.6,
                "reviewCount" to 2100,
                "deliveryFee" to 20000,
                "openTime" to "07:00",
                "closeTime" to "22:30",
                "isOpen" to true,
                "emoji" to "☕",
                "logo" to "https://www.freepnglogos.com/uploads/starbucks-coffe-logo-hd-image-15.png",
                "coverImage" to "https://www.freepnglogos.com/uploads/starbucks-coffe-logo-hd-image-15.png",
                "ownerId" to ""
            ),
            mapOf(
                "name" to "Phúc Long",
                "description" to "Thương hiệu trà & cà phê Việt Nam với hương vị truyền thống đặc trưng.",
                "address" to "Nhiều chi nhánh tại TP.HCM",
                "rating" to 4.5,
                "reviewCount" to 1850,
                "deliveryFee" to 15000,
                "openTime" to "07:00",
                "closeTime" to "22:00",
                "isOpen" to true,
                "emoji" to "🍹",
                "logo" to "https://uploads-ssl.webflow.com/5fb85f26f126ce08d792d2d9/639d4fb26949fb0d309d5aba_logo-phuc-long-coffee-and-tea.jpg",
                "coverImage" to "https://uploads-ssl.webflow.com/5fb85f26f126ce08d792d2d9/639d4fb26949fb0d309d5aba_logo-phuc-long-coffee-and-tea.jpg",
                "ownerId" to ""
            ),
            mapOf(
                "name" to "Highlands Coffee",
                "description" to "Thương hiệu cà phê Việt Nam tự hào phục vụ dòng sản phẩm cà phê phin đậm đà.",
                "address" to "Nhiều chi nhánh tại TP.HCM",
                "rating" to 4.5,
                "reviewCount" to 2500,
                "deliveryFee" to 15000,
                "openTime" to "07:00",
                "closeTime" to "22:00",
                "isOpen" to true,
                "emoji" to "☕",
                "logo" to "https://images.unsplash.com/photo-1541167760496-1628856ab772?w=500",
                "coverImage" to "https://images.unsplash.com/photo-1541167760496-1628856ab772?w=800",
                "ownerId" to ""
            ),
            mapOf(
                "name" to "Cơm Niêu Thiên Lý",
                "description" to "Cơm niêu truyền thống Việt Nam nấu trong nồi đất, mang hương vị quê hương.",
                "address" to "Nhiều chi nhánh tại TP.HCM",
                "rating" to 4.4,
                "reviewCount" to 980,
                "deliveryFee" to 20000,
                "openTime" to "10:00",
                "closeTime" to "21:30",
                "isOpen" to true,
                "emoji" to "🍚",
                "logo" to "https://cdn-new.topcv.vn/unsafe/https://static.topcv.vn/company_logos/HCPVp2c4i0KBChqJsLE53tsx4rppDAei_1655350229____a446795b9b4619cbf403825d40cc4c32.png",
                "coverImage" to "https://cdn-new.topcv.vn/unsafe/https://static.topcv.vn/company_logos/HCPVp2c4i0KBChqJsLE53tsx4rppDAei_1655350229____a446795b9b4619cbf403825d40cc4c32.png",
                "ownerId" to ""
            ),
            mapOf(
                "name" to "Phở Trang",
                "description" to "Phở bò & phở gà truyền thống, ninh xương 12 tiếng cho nước dùng đậm vị.",
                "address" to "Nhiều chi nhánh tại TP.HCM",
                "rating" to 4.5,
                "reviewCount" to 760,
                "deliveryFee" to 15000,
                "openTime" to "06:00",
                "closeTime" to "22:00",
                "isOpen" to true,
                "emoji" to "🍜",
                "logo" to "https://www.hoteljob.vn/uploads/images/2023/03/01/63ff24d2af7365_74197559.png",
                "coverImage" to "https://www.hoteljob.vn/uploads/images/2023/03/01/63ff24d2af7365_74197559.png",
                "ownerId" to ""
            ),
            mapOf(
                "name" to "Pizza 4P's",
                "description" to "Pizza thủ công phong cách Nhật Bản & Ý, dùng phô mai tươi sản xuất tại chỗ.",
                "address" to "Nhiều chi nhánh tại TP.HCM",
                "rating" to 4.8,
                "reviewCount" to 3200,
                "deliveryFee" to 25000,
                "openTime" to "11:00",
                "closeTime" to "22:00",
                "isOpen" to true,
                "emoji" to "🍕",
                "logo" to "https://uploads-ssl.webflow.com/5fb85f26f126ce08d792d2d9/634965111067819e4534eb71_Before_Cafe-4Ps.jpg",
                "coverImage" to "https://uploads-ssl.webflow.com/5fb85f26f126ce08d792d2d9/634965111067819e4534eb71_Before_Cafe-4Ps.jpg",
                "ownerId" to ""
            ),
            mapOf(
                "name" to "McDonald's",
                "description" to "Chuỗi đồ ăn nhanh toàn cầu nổi tiếng với Big Mac, khoai tây chiên và gà McWings.",
                "address" to "Nhiều chi nhánh tại TP.HCM",
                "rating" to 4.2,
                "reviewCount" to 1560,
                "deliveryFee" to 15000,
                "openTime" to "07:00",
                "closeTime" to "23:00",
                "isOpen" to true,
                "emoji" to "🍔",
                "logo" to "https://cdn.produkto.io/photos/2025/05/04/mcdonalds-logo-1993.webp",
                "coverImage" to "https://cdn.produkto.io/photos/2025/05/04/mcdonalds-logo-1993.webp",
                "ownerId" to ""
            ),
            mapOf(
                "name" to "Pepper Lunch",
                "description" to "Chuỗi nhà hàng Nhật Bản nổi tiếng với cơm thịt bò trên chảo gang nóng hổi.",
                "address" to "Nhiều chi nhánh tại TP.HCM",
                "rating" to 4.4,
                "reviewCount" to 1120,
                "deliveryFee" to 20000,
                "openTime" to "10:00",
                "closeTime" to "22:00",
                "isOpen" to true,
                "emoji" to "🥩",
                "logo" to "https://tse4.mm.bing.net/th/id/OIP.3huEFXk5ThsOEpNYJlstfwHaHa?r=0&rs=1&pid=ImgDetMain&o=7&rm=3",
                "coverImage" to "https://tse4.mm.bing.net/th/id/OIP.3huEFXk5ThsOEpNYJlstfwHaHa?r=0&rs=1&pid=ImgDetMain&o=7&rm=3",
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
            } else if (resName.startsWith("Starbucks")) {
                allFoods += listOf(
                    food(idCounter++, resId, "2", "Ristretto Bianco", 89000, 89000,
                        "https://starbucks.vn/media/tq2iogwk/ristretto-bianco_tcm89-24779_w1024_n.jpg",
                        "☕", "Espresso ristretto kết hợp sữa tươi nguyên kem, thơm nồng", listOf("Signature"), resName),
                    food(idCounter++, resId, "2", "Vanilla Latte", 79000, 85000,
                        "https://starbucks.vn/media/gckm2hqt/asset-vanilla-latte_tcm89-10180_w1024_n.jpg",
                        "☕", "Cà phê latte pha vanilla ngọt dịu thanh mát", listOf("Bán chạy"), resName),
                    food(idCounter++, resId, "2", "Caffè Mocha", 79000, 79000,
                        "https://starbucks.vn/media/ctengd0a/caffee-mocha_tcm89-24781_w1024_n.jpg",
                        "☕", "Espresso pha socola đen đậm vị, whipped cream bên trên", emptyList(), resName),
                    food(idCounter++, resId, "2", "Iced Caffè Americano", 65000, 65000,
                        "https://starbucks.vn/media/nfji4hjd/icedcaffeamericano_tcm89-2097_w1024_n.jpg",
                        "❄️", "Americano đá lạnh sảng khoái, vị cà phê nguyên chất", emptyList(), resName),
                    food(idCounter++, resId, "2", "Espresso Macchiato", 59000, 59000,
                        "https://starbucks.vn/media/jjdfdqqm/espressomacchiato_tcm89-2092_w1024_n.jpg",
                        "☕", "Espresso đậm đặc điểm thêm bọt sữa mịn màng", emptyList(), resName),
                    food(idCounter++, resId, "2", "Cappuccino", 69000, 75000,
                        "https://starbucks.vn/media/rmwlbjxa/cappuccino_tcm89-2066_w1024_n.jpg",
                        "☕", "Cappuccino chuẩn Ý, tỷ lệ bọt sữa hoàn hảo", listOf("Phổ biến"), resName),
                    food(idCounter++, resId, "2", "Caffè Latte", 69000, 69000,
                        "https://starbucks.vn/media/0cnpdydo/caffee-latte_tcm89-2062_w1024_n.jpg",
                        "☕", "Latte nhẹ nhàng, thơm sữa, phù hợp cả ngày", listOf("Bán chạy"), resName),
                    food(idCounter++, resId, "9", "Tuiles d'Amandes", 55000, 55000,
                        "https://starbucks.vn/media/yk1ga3uc/tuiles-d-amandes_tcm89-24805_w1024_n.jpg",
                        "🍪", "Bánh hạnh nhân giòn mỏng nhập khẩu", emptyList(), resName),
                    food(idCounter++, resId, "9", "Mocha Cake", 65000, 65000,
                        "https://starbucks.vn/media/za2nexkc/mocha-cake_tcm89-24818_w1024_n.jpg",
                        "🎂", "Bánh kem mocha đậm vị cà phê và socola", listOf("Mới"), resName),
                    food(idCounter++, resId, "9", "Raisin Oatmeal Cookie", 45000, 45000,
                        "https://starbucks.vn/media/j2klvvxv/raison-oatmeal_tcm89-24814_w1024_n.jpg",
                        "🍪", "Cookie yến mạch nho khô thơm ngon, healthy", emptyList(), resName)
                )
            } else if (resName.startsWith("Phúc Long")) {
                allFoods += listOf(
                    food(idCounter++, resId, "3", "Trà Sữa Phúc Long 100%", 55000, 55000,
                        "https://s3-hcmc02.higiocloud.vn/images/2026/03/ts-phuc-long100-20260312080904.jpg",
                        "🍹", "Trà sữa Phúc Long nguyên chất 100% đặc trưng", listOf("Bán chạy"), resName),
                    food(idCounter++, resId, "3", "Trà Vải Lài", 49000, 49000,
                        "https://s3-hcmc02.higiocloud.vn/images/2026/03/tra-vai-lai100-20260312081006.jpg",
                        "🍵", "Trà vải hương lài thơm mát, dịu ngọt tự nhiên", listOf("Mới"), resName),
                    food(idCounter++, resId, "3", "Lucky Tea", 45000, 50000,
                        "https://s3-hcmc02.higiocloud.vn/images/2026/03/lucky-tea100-20260312045055.jpg",
                        "🍀", "Trà may mắn vị trái cây tươi mát", emptyList(), resName),
                    food(idCounter++, resId, "3", "Trà Ô Long Thạch Dâu", 55000, 55000,
                        "https://s3-hcmc02.higiocloud.vn/images/2026/03/tra-o-long-thach-dau100-20260312080658.jpg",
                        "🍵", "Trà Ô Long thượng hạng kết hợp thạch dâu tươi mát", listOf("Phổ biến"), resName),
                    food(idCounter++, resId, "3", "Trà Đào", 45000, 45000,
                        "https://s3-hcmc02.higiocloud.vn/images/2026/03/tra-dao100-20260312043430.jpg",
                        "🍑", "Trà đào thanh mát, thơm ngọt tự nhiên", listOf("Bán chạy"), resName),
                    food(idCounter++, resId, "3", "Trà Nhãn Lài", 49000, 49000,
                        "https://s3-hcmc02.higiocloud.vn/images/2026/03/tra-nhan-lai100-20260312082056.jpg",
                        "🌸", "Trà nhãn hương lài thơm ngát, nhẹ nhàng", emptyList(), resName),
                    food(idCounter++, resId, "9", "Tiramisu Mini", 39000, 45000,
                        "https://s3-hcmc02.higiocloud.vn/images/2022/60000643-tiramisu-mini_b87000d1-71a7-4652-9c41-6565acf281f7-og.png",
                        "🍮", "Bánh tiramisu mini thơm cà phê, mềm mịn", listOf("Mới"), resName),
                    food(idCounter++, resId, "9", "Bánh Chuối", 25000, 25000,
                        "https://s3-hcmc02.higiocloud.vn/images/2025/03/chuoi-20250305103953.png",
                        "🍌", "Bánh chuối nướng thơm vị dừa truyền thống", emptyList(), resName),
                    food(idCounter++, resId, "3", "Trà Nón Tôm Ướp Lạnh", 45000, 45000,
                        "https://s3-hcmc02.higiocloud.vn/images/2022/60000162-tra-non-tom-u-lanh-tui-tam-giac_53c6ea81-dc90-4e8d-b71b-a7c6403fde78-og.png",
                        "🍵", "Trà nón tôm ướp lạnh cao cấp, hương vị độc đáo", listOf("Signature"), resName),
                    food(idCounter++, resId, "2", "Cappuccino Đá Xay", 55000, 59000,
                        "https://s3-hcmc02.higiocloud.vn/images/2026/03/cappuccino-da-xay100-20260312043002.jpg",
                        "☕", "Cappuccino đá xay mát lạnh, béo ngậy cà phê", listOf("Phổ biến"), resName)
                )
            } else if (resName.startsWith("Highlands Coffee")) {
                allFoods += listOf(
                    food(idCounter++, resId, "2", "Phin Sữa Đá", 39000, 39000,
                        "https://images.unsplash.com/photo-1541167760496-1628856ab772?w=500",
                        "☕", "Cà phê Phin sữa đá Highlands đậm đà chuẩn vị truyền thống", listOf("Bán chạy"), resName),
                    food(idCounter++, resId, "2", "Phin Đen Đá", 35000, 35000,
                        "https://images.unsplash.com/photo-1541167760496-1628856ab772?w=500",
                        "☕", "Cà phê đen đá pha phin đậm vị mộc mạc", emptyList(), resName),
                    food(idCounter++, resId, "3", "Trà Sen Vàng", 49000, 49000,
                        "https://images.unsplash.com/photo-1541167760496-1628856ab772?w=500",
                        "🍵", "Trà Ô Long kết hợp sen vàng bùi ngậy và thạch củ năng giòn ngọt", listOf("Bán chạy", "Signature"), resName),
                    food(idCounter++, resId, "3", "Trà Thạch Đào", 49000, 49000,
                        "https://images.unsplash.com/photo-1541167760496-1628856ab772?w=500",
                        "🍑", "Trà đào thơm ngon ngọt mát kết hợp thạch giòn sần sật", listOf("Phổ biến"), resName),
                    food(idCounter++, resId, "10", "Freeze Trà Xanh", 55000, 55000,
                        "https://images.unsplash.com/photo-1541167760496-1628856ab772?w=500",
                        "🍵", "Freeze trà xanh đá xay mát lạnh kết hợp thạch trà xanh giòn dai", listOf("Bán chạy"), resName),
                    food(idCounter++, resId, "10", "Freeze Phin Cà Phê", 55000, 55000,
                        "https://images.unsplash.com/photo-1541167760496-1628856ab772?w=500",
                        "☕", "Freeze cà phê đá xay thơm béo ngậy kèm thạch cà phê giòn ngọt", emptyList(), resName),
                    food(idCounter++, resId, "9", "Bánh Mì Que Pate", 19000, 19000,
                        "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500",
                        "🥖", "Bánh mì que giòn rụm kẹp pate nóng hổi béo ngậy", emptyList(), resName)
                )
            } else if (resName.startsWith("Cơm Niêu Thiên Lý")) {
                allFoods += listOf(
                    food(idCounter++, resId, "4", "Cơm Niêu Thập Cẩm", 89000, 95000,
                        "https://comnieuthienly.com/_next/image?url=https%3A%2F%2Fhos.comnieuthienly.com%2Fimages%2Fwebp%2F6a100e819b9d1e5cac68f4b0.jpg&w=1920&q=75",
                        "🍚", "Cơm niêu nấu trong nồi đất với thập cẩm thịt cá rau củ", listOf("Bán chạy"), resName),
                    food(idCounter++, resId, "4", "Cơm Niêu Cá Kho Tộ", 79000, 79000,
                        "https://comnieuthienly.com/_next/image?url=https%3A%2F%2Fhos.comnieuthienly.com%2Fimages%2Fwebp%2F6a100e379b9d1f6080fb655d.jpg&w=1920&q=75",
                        "🐟", "Cá kho tộ đậm đà kết hợp cơm niêu thơm dẻo", listOf("Phổ biến"), resName),
                    food(idCounter++, resId, "4", "Cơm Niêu Sườn Non", 85000, 90000,
                        "https://comnieuthienly.com/_next/image?url=https%3A%2F%2Fhos.comnieuthienly.com%2Fimages%2Fwebp%2F6a100e809b9d1e487ccdfd5a.jpg&w=1920&q=75",
                        "🥩", "Sườn non mềm hầm với cơm niêu truyền thống", emptyList(), resName),
                    food(idCounter++, resId, "4", "Cơm Niêu Gà Kho Gừng", 75000, 75000,
                        "https://comnieuthienly.com/_next/image?url=https%3A%2F%2Fhos.comnieuthienly.com%2Fimages%2Fwebp%2F6a100e389b9d1f6080fb655e.jpg&w=1920&q=75",
                        "🐔", "Gà kho gừng thơm nồng, đậm vị quê nhà", emptyList(), resName),
                    food(idCounter++, resId, "4", "Cơm Niêu Mực Rang Muối", 95000, 100000,
                        "https://comnieuthienly.com/_next/image?url=https%3A%2F%2Fhos.comnieuthienly.com%2Fimages%2Fwebp%2F6a100e399b9d1f6080fb6562.jpg&w=1920&q=75",
                        "🦑", "Mực tươi rang muối ớt giòn thơm ăn kèm cơm niêu", listOf("Phổ biến"), resName),
                    food(idCounter++, resId, "4", "Cơm Niêu Đặc Biệt", 110000, 120000,
                        "https://comnieuthienly.com/_next/image?url=https%3A%2F%2Fhos.comnieuthienly.com%2Fimages%2Fwebp%2F6a100e959b9d1f6080fb6579.jpg&w=1920&q=75",
                        "🍱", "Combo đặc biệt nhiều món cao cấp nhất của nhà hàng", listOf("Signature"), resName),
                    food(idCounter++, resId, "4", "Cơm Niêu Tôm Sú", 99000, 105000,
                        "https://comnieuthienly.com/_next/image?url=https%3A%2F%2Fhos.comnieuthienly.com%2Fimages%2Fwebp%2F6a100e969b9d1e6080fb657e.jpg&w=1920&q=75",
                        "🍤", "Tôm sú tươi ngon nướng mỡ hành ăn kèm cơm niêu", listOf("Mới"), resName)
                )
            } else if (resName.startsWith("Phở Trang")) {
                allFoods += listOf(
                    food(idCounter++, resId, "5", "Phở Trộn Đặc Biệt", 69000, 75000,
                        "https://photrang.vn/wp-content/uploads/2025/02/PhoTronDacBiet_2-408x272.png",
                        "🍜", "Phở trộn đặc biệt với đủ loại thịt bò tươi ngon", listOf("Bán chạy"), resName),
                    food(idCounter++, resId, "5", "Phở Tái Nạm Gầu", 65000, 65000,
                        "https://photrang.vn/wp-content/uploads/2025/02/PhoTaiNamGau-408x272.png",
                        "🍜", "Phở bò tái nam gầu nước trong vắt, ngọt xương", listOf("Phổ biến"), resName),
                    food(idCounter++, resId, "5", "Phở KOBE", 85000, 90000,
                        "https://photrang.vn/wp-content/uploads/2025/02/PhoKOBE5-408x272.png",
                        "🥩", "Phở với thịt bò Kobe cao cấp, vị béo ngậy đặc trưng", listOf("Signature"), resName),
                    food(idCounter++, resId, "5", "Phở Đặc Biệt", 75000, 80000,
                        "https://photrang.vn/wp-content/uploads/2025/02/PhoDacBiet-2-408x272.png",
                        "🍜", "Phở đặc biệt đủ loại thịt: tái, chín, gầu, gân, sách", emptyList(), resName),
                    food(idCounter++, resId, "5", "Phở Bắp", 60000, 60000,
                        "https://photrang.vn/wp-content/uploads/2025/02/PhoBap-408x272.png",
                        "🍜", "Phở bắp mềm, gân giòn, nước dùng ngọt thanh", emptyList(), resName),
                    food(idCounter++, resId, "5", "Bún Bò Đặc Biệt", 70000, 75000,
                        "https://photrang.vn/wp-content/uploads/2025/02/BunBoDacBiet-408x272.png",
                        "🍲", "Bún bò Huế đặc biệt sả ớt đậm vị miền Trung", listOf("Phổ biến"), resName),
                    food(idCounter++, resId, "5", "Phở Sườn", 65000, 65000,
                        "https://photrang.vn/wp-content/uploads/2025/02/PhoSuon-408x272.png",
                        "🍜", "Phở sườn non mềm hầm kỹ, nước dùng đậm đà", emptyList(), resName),
                    food(idCounter++, resId, "5", "Phở Tái", 55000, 60000,
                        "https://photrang.vn/wp-content/uploads/2024/07/photai-20200618035833-408x272.png",
                        "🍜", "Phở bò tái truyền thống, thịt mềm ngọt tươi", listOf("Bán chạy"), resName)
                )
            } else if (resName.startsWith("Pizza 4P's")) {
                allFoods += listOf(
                    food(idCounter++, resId, "6", "Pizza Phô Mai Tổng Hợp", 249000, 269000,
                        "https://pizza4ps.com/wp-content/uploads/2023/07/BYO_Assorted-Cheese_S-2-560x560.jpg",
                        "🍕", "Pizza 4 loại phô mai tươi sản xuất tại chỗ, đế mỏng giòn", listOf("Bán chạy"), resName),
                    food(idCounter++, resId, "6", "Pizza Cold Cuts", 229000, 229000,
                        "https://pizza4ps.com/wp-content/uploads/2023/08/BYO_Cold-Cuts_S-2-560x560.jpg",
                        "🍕", "Pizza thịt nguội Ý cao cấp, salami, prosciutto", emptyList(), resName),
                    food(idCounter++, resId, "6", "Pizza Tôm Tỏi", 259000, 279000,
                        "https://pizza4ps.com/wp-content/uploads/2024/04/BYO_Garlic-Shrimp-Pizza-1-560x560.jpg",
                        "🍤", "Pizza tôm sú tỏi phi thơm, bơ lạnh đặc biệt", listOf("Phổ biến"), resName),
                    food(idCounter++, resId, "6", "Pizza Hải Sản Nhật", 269000, 289000,
                        "https://pizza4ps.com/wp-content/uploads/2023/07/20200001_2-560x560.jpg",
                        "🍣", "Pizza phong cách Nhật với cá hồi, bạch tuộc tươi", listOf("Signature"), resName),
                    food(idCounter++, resId, "6", "Pizza Salami Mushroom", 219000, 219000,
                        "https://pizza4ps.com/wp-content/uploads/2023/07/20200003_2-560x560.jpg",
                        "🍕", "Pizza salami nấm hương thơm ngon chuẩn vị Ý", emptyList(), resName),
                    food(idCounter++, resId, "6", "Half & Half Pizza", 279000, 299000,
                        "https://pizza4ps.com/wp-content/uploads/2023/07/30000303_2-560x560.jpg",
                        "🍕", "Pizza chia đôi 2 vị, tự chọn topping yêu thích", listOf("Phổ biến"), resName),
                    food(idCounter++, resId, "6", "Pizza Margherita", 199000, 199000,
                        "https://pizza4ps.com/wp-content/uploads/2023/07/30000304_2-560x560.jpg",
                        "🍅", "Pizza Margherita cổ điển: sốt cà chua, phô mai mozzarella", emptyList(), resName),
                    food(idCounter++, resId, "9", "Yuzu Dessert", 79000, 85000,
                        "https://pizza4ps.com/wp-content/uploads/2023/07/Thiet-ke-chua-co-ten-6.png",
                        "🍋", "Bánh tráng miệng yuzu chua nhẹ, thanh mát", listOf("Mới"), resName),
                    food(idCounter++, resId, "10", "Yuzu Beer", 89000, 89000,
                        "https://pizza4ps.com/wp-content/uploads/2023/07/Yuzu-Beer-1-1-560x560.jpg",
                        "🍺", "Bia yuzu thủ công nhẹ nhàng, hương cam quý độc đáo", listOf("Signature"), resName)
                )
            } else if (resName.startsWith("McDonald's")) {
                allFoods += listOf(
                    food(idCounter++, resId, "7", "Cheese DLX Burger", 79000, 85000,
                        "https://mcdonalds.vn/uploads/2018/food/burgers/cheesedlx_bb.png",
                        "🍔", "Burger gà phi lê giòn, phô mai chảy, sốt đặc biệt", listOf("Bán chạy"), resName),
                    food(idCounter++, resId, "7", "Double Cheeseburger", 69000, 75000,
                        "https://mcdonalds.vn/uploads/2018/food/burgers/doublecheese_bb.png",
                        "🍔", "Burger 2 miếng thịt bò kèm 2 lớp phô mai béo ngậy", listOf("Phổ biến"), resName),
                    food(idCounter++, resId, "1", "20 Miếng Gà Viên Vui Vẻ", 119000, 129000,
                        "https://mcdonalds.vn/uploads/2018/20-mieng-Ga-vien-Vui-ve.jpg",
                        "🍗", "20 viên gà chiên giòn, sốt chấm tùy chọn", listOf("Bán chạy"), resName),
                    food(idCounter++, resId, "1", "3 Miếng Gà McWings", 65000, 65000,
                        "https://mcdonalds.vn/uploads/2018/food/ga-ran/3pcs_chicken_mcwings.png",
                        "🍗", "Cánh gà chiên giòn vàng ươm, thấm vị ướp", emptyList(), resName),
                    food(idCounter++, resId, "10", "Fanta Cam", 29000, 29000,
                        "https://mcdonalds.vn/uploads/2018/food/beverage/hero-pdt-Fanta-201703_0.png",
                        "🥤", "Nước ngọt Fanta cam sảng khoái mát lạnh", emptyList(), resName),
                    food(idCounter++, resId, "10", "Coca Cola", 25000, 25000,
                        "https://mcdonalds.vn/uploads/2018/food/beverage/mcd-food-beverages-soft-drinks-coke.png",
                        "🥤", "Nước ngọt Coca-Cola lạnh, giải khát tức thì", emptyList(), resName),
                    food(idCounter++, resId, "9", "Hot Fudge McSundae", 39000, 39000,
                        "https://mcdonalds.vn/uploads/2018/food/desserts/hotfudge_mcsundae.png",
                        "🍦", "Kem mềm rưới sốt chocolate nóng ngậy ngọt", listOf("Phổ biến"), resName),
                    food(idCounter++, resId, "9", "Bắp Bơ", 25000, 25000,
                        "https://mcdonalds.vn/uploads/2018/corncup-1.png",
                        "🌽", "Bắp nguyên hạt trộn bơ thơm ngậy", emptyList(), resName),
                    food(idCounter++, resId, "1", "Salad McD", 45000, 49000,
                        "https://mcdonalds.vn/uploads/2018/food/mon-an-chung/salad500.png",
                        "🥗", "Salad rau tươi mát, sốt dressing đa dạng", listOf("Healthy"), resName),
                    food(idCounter++, resId, "1", "Khoai Tây Chiên Medium", 39000, 45000,
                        "https://mcdonalds.vn/uploads/2018/food/ga-ran/medium_world_famous_fries.png",
                        "🍟", "Khoai tây chiên vàng giòn nổi tiếng thế giới", listOf("Bán chạy"), resName)
                )
            } else if (resName.startsWith("Pepper Lunch")) {
                allFoods += listOf(
                    food(idCounter++, resId, "8", "The Giant Beef", 189000, 199000,
                        "https://pepper-lunch.vn/uploads/products/the-giant-new-202208230720_thumb.png",
                        "🥩", "Cơm thịt bò khổng lồ trên chảo gang nóng 260°C", listOf("Bán chạy", "Signature"), resName),
                    food(idCounter++, resId, "8", "Cơm Tiêu Đen Thượng Hạng", 169000, 179000,
                        "https://pepper-lunch.vn/uploads/products/com-tieu-den-thuong-hang-202302111723_thumb.png",
                        "🌶️", "Cơm bò thượng hạng sốt tiêu đen đặc trưng Pepper Lunch", listOf("Phổ biến"), resName),
                    food(idCounter++, resId, "8", "Cơm Gà Tiêu Đen", 149000, 159000,
                        "https://pepper-lunch.vn/uploads/products/com-ga-tieu-den-202208222122_thumb.png",
                        "🐔", "Cơm gà fillet nướng chảo gang sốt tiêu đen thơm lừng", emptyList(), resName),
                    food(idCounter++, resId, "8", "Rau Củ Thập Cẩm", 79000, 79000,
                        "https://pepper-lunch.vn/uploads/products/rau-cu-thap-cam-202510151746_thumb.png",
                        "🥦", "Rau củ hỗn hợp nướng chảo gang, ăn kèm sauce", emptyList(), resName),
                    food(idCounter++, resId, "8", "Cơm Gà Cari Phô Mai", 155000, 165000,
                        "https://pepper-lunch.vn/uploads/products/com-ga-cari-voi-pho-mai-202208222118_thumb.png",
                        "🍛", "Gà cari Nhật Bản kết hợp phô mai béo, ăn kèm cơm", listOf("Mới"), resName),
                    food(idCounter++, resId, "8", "Cá Hồi Sốt Sukiyaki", 179000, 189000,
                        "https://pepper-lunch.vn/uploads/products/ca-hoi-sot-sukiyaki-202302111731_thumb.png",
                        "🐟", "Cá hồi Na Uy tươi nướng chảo gang sốt sukiyaki đặc biệt", listOf("Phổ biến"), resName),
                    food(idCounter++, resId, "8", "Bít Tết Gà Teriyaki", 159000, 169000,
                        "https://pepper-lunch.vn/uploads/products/bit-tet-ga-teriyaki-voi-trung-202208222112_thumb.png",
                        "🥚", "Gà teriyaki kết hợp trứng tráng mềm mịn đậm vị", emptyList(), resName),
                    food(idCounter++, resId, "8", "Cơm Trứng Phô Mai Gà", 145000, 155000,
                        "https://pepper-lunch.vn/uploads/products/com-trung-pho-mai-voi-ga-202302111742_thumb.png",
                        "🍳", "Cơm gà phô mai kết hợp trứng tươi trộn đều trên chảo nóng", emptyList(), resName),
                    food(idCounter++, resId, "8", "Cơm Cà Ri Bò Xúc Xích", 169000, 179000,
                        "https://pepper-lunch.vn/uploads/products/com-ca-ri-bo-voi-xuc-xich-202302111745_thumb.png",
                        "🌭", "Cà ri bò đậm vị kết hợp xúc xích phô mai Đức", listOf("Phổ biến"), resName),
                    food(idCounter++, resId, "8", "Mì Ý Gà Aglio Olio", 149000, 159000,
                        "https://pepper-lunch.vn/uploads/products/my-y-ga-aglio-olio-202208230730_thumb.png",
                        "🍝", "Mì Ý sốt tỏi dầu kiểu Nhật với gà phi lê mềm", listOf("Mới"), resName)
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
