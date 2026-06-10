package com.example.a43_kltn_ttfood.navigation

/**
 * Định nghĩa tất cả routes trong app TTFood
 */
sealed class Screen(val route: String) {
    // Auth flow
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object OtpVerification : Screen("otp_verification/{phoneOrEmail}") {
        fun createRoute(phoneOrEmail: String) = "otp_verification/$phoneOrEmail"
    }
    data object ResetPassword : Screen("reset_password")

    // Main app
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Notifications : Screen("notifications")
    data object RestaurantDetail : Screen("restaurant_detail/{restaurantId}") {
        fun createRoute(restaurantId: String) = "restaurant_detail/$restaurantId"
    }
    data object FoodDetail : Screen("food_detail/{foodId}") {
        fun createRoute(foodId: Int) = "food_detail/$foodId"
    }
    data object Category : Screen("category/{categoryId}") {
        fun createRoute(categoryId: Int) = "category/$categoryId"
    }
    data object BannerDetail : Screen("banner_detail/{bannerId}") {
        fun createRoute(bannerId: Int) = "banner_detail/$bannerId"
    }
    data object Cart : Screen("cart")
    data object Checkout : Screen("checkout")
    data object OrderSuccess : Screen("order_success")
    data object OrderTracking : Screen("order_tracking")
    data object ProfileDashboard : Screen("profile_dashboard")
    data object EditProfile : Screen("edit_profile")
    data object OrderHistory : Screen("order_history")
    data object Favorites : Screen("favorites")
    data object Settings : Screen("settings")

    // Admin
    data object AdminDashboard : Screen("admin_dashboard")
    data object AdminUsers : Screen("admin_users")
    data object AdminUserDetail : Screen("admin_user_detail/{userId}") {
        fun createRoute(userId: String) = "admin_user_detail/$userId"
    }
    data object AdminOrders : Screen("admin_orders")
    data object AdminOrderDetail : Screen("admin_order_detail/{orderId}") {
        fun createRoute(orderId: String) = "admin_order_detail/$orderId"
    }
    data object AdminRestaurants : Screen("admin_restaurants")
    data object AdminVouchers : Screen("admin_vouchers")
    data object AdminAuditLog : Screen("admin_audit_log")

    // Upload tool (dùng nhập ảnh vào Firebase Storage)
    data object UploadFoodImage : Screen("upload_food_image/{foodId}") {
        fun createRoute(foodId: Int = 2) = "upload_food_image/$foodId"
    }
}
