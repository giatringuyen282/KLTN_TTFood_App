package com.example.a43_kltn_ttfood.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp

/**
 * 📁 users collection
 * Lưu thông tin người dùng (customer, admin, shipper)
 */
data class User(
    @DocumentId val id: String = "",
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val role: String = UserRole.CUSTOMER,      // "customer" | "admin" | "shipper"
    val isActive: Boolean = true,
    val fcmToken: String = "",                  // Push notification token
    val dob: String = "",                       // Ngày sinh
    val gender: String = "",                    // Giới tính
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

object UserRole {
    const val CUSTOMER = "customer"
    const val ADMIN = "admin"
    const val SHIPPER = "shipper"
}

/**
 * 📁 addresses collection
 * Địa chỉ giao hàng — tách riêng, mỗi user có nhiều địa chỉ
 */
data class Address(
    @DocumentId val id: String = "",
    val userId: String = "",
    val label: String = "",                     // "Nhà", "Công ty", "Khác"
    val address: String = "",
    val location: GeoPoint? = null,
    val recipientName: String = "",
    val recipientPhone: String = "",
    val isDefault: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

/**
 * 📁 restaurants collection
 * Thông tin nhà hàng đối tác
 */
data class RestaurantModel(
    @DocumentId val id: String = "",
    val ownerId: String = "",                   // FK → users (chủ nhà hàng)
    val name: String = "",
    val description: String = "",
    val address: String = "",
    val location: GeoPoint? = null,             // Tính khoảng cách
    val rating: Double = 0.0,
    val reviewCount: Int = 0,                   // Tránh count lại mỗi query
    val deliveryFee: Int = 0,                   // VNĐ
    val openTime: String = "",                  // "07:00"
    val closeTime: String = "",                 // "22:00"
    val isOpen: Boolean = true,
    val coverImage: String = "",
    val logo: String = "",
    val emoji: String = "🍽️",
    @ServerTimestamp val createdAt: Timestamp? = null
)

/**
 * 📁 categories collection (top-level, dùng chung toàn app)
 */
data class Category(
    @DocumentId val id: String = "",
    val name: String = "",
    val emoji: String = "",
    val sortOrder: Int = 0
)

/**
 * 📁 food_items collection
 */
data class FoodItemModel(
    @DocumentId val id: String = "",
    val restaurantId: String = "",              // FK → restaurants
    val categoryId: String = "",                // FK → categories
    val name: String = "",
    val price: Int = 0,                         // VNĐ
    val originalPrice: Int = 0,                 // Giá gốc (nếu giảm)
    val imageUrl: String = "",
    val emoji: String = "🍔",
    val description: String = "",
    val isAvailable: Boolean = true,
    val calories: Int = 0,
    val allergens: List<String> = emptyList(),
    val badges: List<String> = emptyList(),     // "Bán chạy", "Mới", "Hết hàng"
    val toppingGroupIds: List<String> = emptyList(), // FK → topping_groups
    @ServerTimestamp val createdAt: Timestamp? = null
)

/**
 * 📁 topping_groups collection
 * Nhóm topping dùng chung cho nhiều món
 * Ví dụ: "Size" → [S, M, L], "Đá" → [Ít đá, Bình thường, Nhiều đá]
 */
data class ToppingGroup(
    @DocumentId val id: String = "",
    val restaurantId: String = "",              // FK → restaurants
    val name: String = "",                      // "Size", "Topping", "Đá"
    val isRequired: Boolean = false,            // Bắt buộc chọn?
    val maxSelect: Int = 1,                     // Tối đa chọn mấy option
    val options: List<ToppingOption> = emptyList()
)

data class ToppingOption(
    val name: String = "",                      // "Size M", "Thêm trân châu"
    val price: Int = 0                          // VNĐ (0 = miễn phí)
)

/**
 * 📁 orders collection
 * Đơn hàng — tách rõ subtotal/deliveryFee/discount/total
 */
data class Order(
    @DocumentId val id: String = "",
    val userId: String = "",                    // FK → users
    val restaurantId: String = "",              // FK → restaurants
    val shipperId: String = "",                 // FK → users (shipper)
    val addressId: String = "",                 // FK → addresses
    val voucherId: String = "",                 // FK → vouchers
    val status: String = OrderStatus.PENDING,
    val subtotal: Int = 0,                      // Tổng tiền món
    val deliveryFee: Int = 0,                   // Phí giao hàng
    val discount: Int = 0,                      // Giảm giá từ voucher
    val totalAmount: Int = 0,                   // = subtotal + deliveryFee - discount
    val paymentMethod: String = PaymentMethod.COD,
    val paymentStatus: String = PaymentStatus.PENDING,
    val deliveryAddress: String = "",            // Snapshot địa chỉ lúc đặt
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

object OrderStatus {
    const val PENDING = "pending"               // Chờ xác nhận
    const val CONFIRMED = "confirmed"           // Nhà hàng xác nhận
    const val PREPARING = "preparing"           // Đang chuẩn bị
    const val PICKING_UP = "picking_up"         // Shipper đang lấy hàng
    const val DELIVERING = "delivering"         // Đang giao
    const val DELIVERED = "delivered"            // Giao thành công
    const val CANCELLED = "cancelled"           // Đã hủy
}

object PaymentMethod {
    const val COD = "cod"
    const val MOMO = "momo"
    const val ZALOPAY = "zalopay"
    const val VNPAY = "vnpay"
    const val CARD = "card"
}

object PaymentStatus {
    const val PENDING = "pending"
    const val PAID = "paid"
    const val FAILED = "failed"
    const val REFUNDED = "refunded"
}

/**
 * 📁 orders/{orderId}/order_items subcollection
 * Giữ nguyên subcollection — chỉ đọc kèm orders
 */
data class OrderItem(
    @DocumentId val id: String = "",
    val foodItemId: String = "",                // FK → food_items
    val foodName: String = "",                  // Snapshot tên món
    val quantity: Int = 0,
    val unitPrice: Int = 0,
    val toppings: List<SelectedTopping> = emptyList(),
    val note: String = ""
)

data class SelectedTopping(
    val groupName: String = "",                 // "Size"
    val optionName: String = "",                // "Size L"
    val price: Int = 0
)

/**
 * 📁 vouchers collection
 */
data class Voucher(
    @DocumentId val id: String = "",
    val code: String = "",
    val discountType: String = "percent",       // "percent" | "fixed"
    val discountValue: Int = 0,                 // % hoặc VNĐ
    val minOrder: Int = 0,                      // Đơn tối thiểu
    val maxDiscount: Int = 0,                   // Giảm tối đa (cho %)
    val usageLimit: Int = 0,                    // Giới hạn số lần dùng
    val usedCount: Int = 0,                     // Đã dùng bao nhiêu
    val isActive: Boolean = true,
    val expiresAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

/**
 * 📁 reviews collection
 */
data class Review(
    @DocumentId val id: String = "",
    val userId: String = "",                    // FK → users
    val restaurantId: String = "",              // FK → restaurants
    val orderId: String = "",                   // FK → orders (xác minh đã đặt)
    val rating: Float = 0f,                     // 1.0 – 5.0
    val comment: String = "",
    val imageUrls: List<String> = emptyList(),  // Upload ảnh đánh giá
    val tags: List<String> = emptyList(),       // "Giao nhanh", "Món ngon"
    @ServerTimestamp val createdAt: Timestamp? = null
)

/**
 * 📁 notifications collection
 */
data class Notification(
    @DocumentId val id: String = "",
    val userId: String = "",                    // FK → users
    val title: String = "",
    val body: String = "",
    val type: String = "",                      // "order_update", "promotion", "system"
    val referenceId: String = "",               // orderId hoặc promotionId
    val isRead: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

/**
 * 📁 audit_logs collection
 * Ghi lại hành động quan trọng — debug & compliance
 */
data class AuditLog(
    @DocumentId val id: String = "",
    val userId: String = "",                    // Ai thực hiện
    val userName: String = "",                  // Snapshot tên
    val action: String = "",                    // "login", "block_user", "update_order"...
    val targetType: String = "",                // "user", "order", "restaurant"
    val targetId: String = "",                  // ID đối tượng bị tác động
    val details: String = "",                   // Mô tả chi tiết
    val ipAddress: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

object AuditAction {
    const val LOGIN = "login"
    const val LOGOUT = "logout"
    const val CHANGE_PASSWORD = "change_password"
    const val BLOCK_USER = "block_user"
    const val UNBLOCK_USER = "unblock_user"
    const val UPDATE_ORDER = "update_order"
    const val CREATE_VOUCHER = "create_voucher"
    const val UPDATE_RESTAURANT = "update_restaurant"
    const val DELETE_FOOD = "delete_food"
}
