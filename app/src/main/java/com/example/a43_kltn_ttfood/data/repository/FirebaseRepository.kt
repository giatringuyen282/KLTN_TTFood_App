package com.example.a43_kltn_ttfood.data.repository

import android.net.Uri
import com.example.a43_kltn_ttfood.data.model.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository quản lý Users trên Firestore
 * Dùng cho Admin: xem danh sách, tìm kiếm, block/unblock
 */
class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val auditCollection = db.collection("audit_logs")

    /**
     * Lấy danh sách tất cả users (real-time)
     */
    fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val listener = usersCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull {
                    it.toObject(User::class.java)
                } ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Tìm kiếm user theo tên, SĐT hoặc email
     */
    fun searchUsers(query: String): Flow<List<User>> = callbackFlow {
        val queryLower = query.lowercase().trim()

        val listener = usersCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull {
                    it.toObject(User::class.java)
                }?.filter { user ->
                    user.fullName.lowercase().contains(queryLower) ||
                    user.phone.contains(queryLower) ||
                    user.email.lowercase().contains(queryLower)
                } ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Lấy user theo ID
     */
    suspend fun getUserById(userId: String): User? {
        return try {
            usersCollection.document(userId).get().await()
                .toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Lấy users theo role
     */
    fun getUsersByRole(role: String): Flow<List<User>> = callbackFlow {
        val listener = usersCollection
            .whereEqualTo("role", role)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull {
                    it.toObject(User::class.java)
                } ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Block / Unblock user
     */
    suspend fun toggleUserActive(
        userId: String,
        isActive: Boolean,
        adminId: String,
        adminName: String
    ): Result<Unit> {
        return try {
            usersCollection.document(userId).update("isActive", isActive).await()

            // Ghi audit log
            val log = AuditLog(
                userId = adminId,
                userName = adminName,
                action = if (isActive) AuditAction.UNBLOCK_USER else AuditAction.BLOCK_USER,
                targetType = "user",
                targetId = userId,
                details = if (isActive) "Mở khóa tài khoản" else "Khóa tài khoản"
            )
            auditCollection.add(log).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cập nhật role user
     */
    suspend fun updateUserRole(userId: String, newRole: String): Result<Unit> {
        return try {
            usersCollection.document(userId).update("role", newRole).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Đếm tổng số users
     */
    suspend fun getUserCount(): Int {
        return try {
            usersCollection.get().await().size()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Đếm users theo role
     */
    suspend fun getUserCountByRole(role: String): Int {
        return try {
            usersCollection.whereEqualTo("role", role).get().await().size()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Lấy audit logs cho một user cụ thể
     */
    fun getAuditLogsForUser(targetId: String): Flow<List<AuditLog>> = callbackFlow {
        val listener = auditCollection
            .whereEqualTo("targetId", targetId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val logs = snapshot?.documents?.mapNotNull {
                    it.toObject(AuditLog::class.java)
                } ?: emptyList()
                trySend(logs)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Lấy tất cả audit logs (Admin)
     */
    fun getAllAuditLogs(): Flow<List<AuditLog>> = callbackFlow {
        val listener = auditCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val logs = snapshot?.documents?.mapNotNull {
                    it.toObject(AuditLog::class.java)
                } ?: emptyList()
                trySend(logs)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Ghi một audit log mới
     */
    suspend fun logAction(
        userId: String,
        userName: String,
        action: String,
        targetType: String = "",
        targetId: String = "",
        details: String = ""
    ) {
        try {
            val log = AuditLog(
                userId = userId,
                userName = userName,
                action = action,
                targetType = targetType,
                targetId = targetId,
                details = details
            )
            auditCollection.add(log).await()
        } catch (_: Exception) { }
    }
}

/**
 * Repository quản lý Orders trên Firestore
 */
class OrderRepository {
    private val db = FirebaseFirestore.getInstance()
    private val ordersCollection = db.collection("orders")

    /**
     * Lấy đơn hàng của một user cụ thể
     */
    fun getOrdersByUser(userId: String): Flow<List<Order>> = callbackFlow {
        val listener = ordersCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull {
                    it.toObject(Order::class.java)
                } ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Lấy tất cả đơn hàng (Admin)
     */
    fun getAllOrders(): Flow<List<Order>> = callbackFlow {
        val listener = ordersCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull {
                    it.toObject(Order::class.java)
                } ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    private val auditCollection = db.collection("audit_logs")

    /**
     * Lấy chi tiết một đơn hàng theo ID
     */
    suspend fun getOrderById(orderId: String): Order? {
        return try {
            ordersCollection.document(orderId).get().await()
                .toObject(Order::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Lấy các món ăn trong đơn hàng (real-time)
     */
    fun getOrderItems(orderId: String): Flow<List<OrderItem>> = callbackFlow {
        val listener = ordersCollection.document(orderId).collection("order_items")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull {
                    it.toObject(OrderItem::class.java)
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Cập nhật trạng thái đơn hàng (Admin/Shipper)
     */
    suspend fun updateOrderStatus(
        orderId: String,
        status: String,
        adminId: String,
        adminName: String
    ): Result<Unit> {
        return try {
            ordersCollection.document(orderId).update("status", status).await()

            // Ghi audit log
            val log = AuditLog(
                userId = adminId,
                userName = adminName,
                action = AuditAction.UPDATE_ORDER,
                targetType = "order",
                targetId = orderId,
                details = "Cập nhật trạng thái đơn sang: $status"
            )
            auditCollection.add(log).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Đếm tổng đơn hàng
     */
    suspend fun getOrderCount(): Int {
        return try {
            ordersCollection.get().await().size()
        } catch (e: Exception) {
            0
        }
    }
}

/**
 * Repository quản lý Restaurants trên Firestore
 */
class RestaurantRepository {
    private val db = FirebaseFirestore.getInstance()
    private val restaurantsCollection = db.collection("restaurants")
    private val auditCollection = db.collection("audit_logs")

    /**
     * Lấy tất cả nhà hàng (real-time)
     */
    fun getAllRestaurants(): Flow<List<RestaurantModel>> = callbackFlow {
        val listener = restaurantsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val restaurants = snapshot?.documents?.mapNotNull {
                    it.toObject(RestaurantModel::class.java)
                } ?: emptyList()
                trySend(restaurants)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Lấy nhà hàng theo ID
     */
    suspend fun getRestaurantById(restaurantId: String): RestaurantModel? {
        return try {
            restaurantsCollection.document(restaurantId).get().await()
                .toObject(RestaurantModel::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Toggle trạng thái mở/đóng nhà hàng
     */
    suspend fun toggleRestaurantOpen(
        restaurantId: String,
        isOpen: Boolean,
        adminId: String,
        adminName: String
    ): Result<Unit> {
        return try {
            restaurantsCollection.document(restaurantId).update("isOpen", isOpen).await()

            val log = AuditLog(
                userId = adminId,
                userName = adminName,
                action = AuditAction.UPDATE_RESTAURANT,
                targetType = "restaurant",
                targetId = restaurantId,
                details = if (isOpen) "Mở nhà hàng" else "Đóng nhà hàng"
            )
            auditCollection.add(log).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Đếm tổng nhà hàng
     */
    suspend fun getRestaurantCount(): Int {
        return try {
            restaurantsCollection.get().await().size()
        } catch (e: Exception) {
            0
        }
    }
}

/**
 * Repository quản lý Vouchers trên Firestore
 */
class VoucherRepository {
    private val db = FirebaseFirestore.getInstance()
    private val vouchersCollection = db.collection("vouchers")
    private val auditCollection = db.collection("audit_logs")

    /**
     * Lấy tất cả vouchers (real-time)
     */
    fun getAllVouchers(): Flow<List<Voucher>> = callbackFlow {
        val listener = vouchersCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val vouchers = snapshot?.documents?.mapNotNull {
                    it.toObject(Voucher::class.java)
                } ?: emptyList()
                trySend(vouchers)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Tạo voucher mới
     */
    suspend fun createVoucher(
        voucher: Voucher,
        adminId: String,
        adminName: String
    ): Result<String> {
        return try {
            val docRef = vouchersCollection.add(voucher).await()

            val log = AuditLog(
                userId = adminId,
                userName = adminName,
                action = AuditAction.CREATE_VOUCHER,
                targetType = "voucher",
                targetId = docRef.id,
                details = "Tạo voucher: ${voucher.code}"
            )
            auditCollection.add(log).await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Toggle trạng thái voucher
     */
    suspend fun toggleVoucherActive(voucherId: String, isActive: Boolean): Result<Unit> {
        return try {
            vouchersCollection.document(voucherId).update("isActive", isActive).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Đếm tổng vouchers
     */
    suspend fun getVoucherCount(): Int {
        return try {
            vouchersCollection.get().await().size()
        } catch (e: Exception) {
            0
        }
    }
}

// ==============================
// Category Repository
// ==============================
class CategoryRepository {
    private val db = FirebaseFirestore.getInstance()
    private val categoriesCollection = db.collection("categories")

    /** Retrieve all categories (real-time) */
    fun getAllCategories(): Flow<List<FoodCategory>> = callbackFlow {
        val listener = categoriesCollection
            .orderBy("id", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val categories = snapshot?.documents?.mapNotNull {
                    it.toObject(FoodCategory::class.java)
                } ?: emptyList()
                trySend(categories)
            }
        awaitClose { listener.remove() }
    }

    /** Get a single category by its id */
    suspend fun getCategoryById(id: Int): FoodCategory? {
        return try {
            categoriesCollection.whereEqualTo("id", id).limit(1).get().await()
                .documents.firstOrNull()?.toObject(FoodCategory::class.java)
        } catch (e: Exception) {
            null
        }
    }
}

// ==============================
// Food Repository
// ==============================
class FoodRepository {
    private val db = FirebaseFirestore.getInstance()
    private val foodCollection = db.collection("food_items")

    /** Retrieve all food items (real-time) */
    fun getAllFoodItems(): Flow<List<FoodItem>> = callbackFlow {
        val listener = foodCollection
            .orderBy("id", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val foods = snapshot?.documents?.mapNotNull {
                    it.toObject(FoodItem::class.java)
                } ?: emptyList()
                trySend(foods)
            }
        awaitClose { listener.remove() }
    }

    /** Get a single food item by id */
    suspend fun getFoodById(id: Int): FoodItem? {
        return try {
            foodCollection.whereEqualTo("id", id).limit(1).get().await()
                .documents.firstOrNull()?.toObject(FoodItem::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /** Search foods by name or restaurant */
    fun searchFood(query: String): Flow<List<FoodItem>> = callbackFlow {
        val lower = query.lowercase().trim()
        val listener = foodCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val foods = snapshot?.documents?.mapNotNull { it.toObject(FoodItem::class.java) }
                    ?.filter { food ->
                        food.name.lowercase().contains(lower) ||
                                food.restaurant.lowercase().contains(lower)
                    } ?: emptyList()
                trySend(foods)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Upload ảnh món ăn lên Firebase Storage
     * Đường dẫn: food_images/{foodId}.jpg
     * Trả về URL tải xuống công khai
     */
    suspend fun uploadFoodImage(foodId: Int, imageUri: Uri): Result<String> {
        return try {
            val storageRef = FirebaseStorage.getInstance()
                .reference
                .child("food_images/$foodId.jpg")
            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cập nhật trường imageUrl trong tài liệu Firestore của món ăn
     */
    suspend fun updateFoodImageUrl(foodId: Int, imageUrl: String): Result<Unit> {
        return try {
            val snap = foodCollection.whereEqualTo("id", foodId).limit(1).get().await()
            val docId = snap.documents.firstOrNull()?.id
                ?: return Result.failure(Exception("Không tìm thấy món ăn"))
            foodCollection.document(docId).update("imageUrl", imageUrl).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
