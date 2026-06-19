package com.example.a43_kltn_ttfood.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.a43_kltn_ttfood.ui.screens.auth.*
import com.example.a43_kltn_ttfood.ui.screens.category.CategoryScreen
import com.example.a43_kltn_ttfood.ui.screens.food.FoodDetailScreen
import com.example.a43_kltn_ttfood.ui.screens.food.UploadFoodImageScreen
import com.example.a43_kltn_ttfood.ui.screens.home.HomeScreen
import com.example.a43_kltn_ttfood.ui.screens.notifications.NotificationsScreen
import com.example.a43_kltn_ttfood.ui.screens.restaurant.RestaurantDetailScreen
import com.example.a43_kltn_ttfood.ui.screens.search.SearchScreen
import com.example.a43_kltn_ttfood.ui.screens.cart.CartScreen
import com.example.a43_kltn_ttfood.ui.screens.checkout.CheckoutScreen
import com.example.a43_kltn_ttfood.ui.screens.checkout.OrderSuccessScreen
import com.example.a43_kltn_ttfood.ui.screens.tracking.OrderTrackingScreen
import com.example.a43_kltn_ttfood.ui.screens.profile.*
import com.example.a43_kltn_ttfood.ui.screens.admin.*
import com.example.a43_kltn_ttfood.ui.screens.banner.BannerDetailScreen

@Composable
fun TTFoodNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        // Splash Screen
        composable(
            route = Screen.Splash.route,
            enterTransition = { fadeIn(animationSpec = tween(500)) },
            exitTransition = { fadeOut(animationSpec = tween(500)) }
        ) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Onboarding Screen
        composable(route = Screen.Onboarding.route) {
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Login Screen
        composable(route = Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ResetPassword.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Register Screen
        composable(route = Screen.Register.route) {
            RegisterScreen(
                onNavigateToOtp = { phoneOrEmail ->
                    navController.navigate(Screen.OtpVerification.createRoute(phoneOrEmail))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // OTP Verification
        composable(
            route = Screen.OtpVerification.route,
            arguments = listOf(
                navArgument("phoneOrEmail") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val phoneOrEmail = backStackEntry.arguments?.getString("phoneOrEmail") ?: ""
            OtpVerificationScreen(
                phoneOrEmail = phoneOrEmail,
                onVerificationSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Reset Password
        composable(route = Screen.ResetPassword.route) {
            ResetPasswordScreen(
                onResetSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ResetPassword.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Home Screen
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToRestaurant = { restaurantId -> 
                    navController.navigate(Screen.RestaurantDetail.createRoute(restaurantId)) 
                },
                onNavigateToFood = { foodId -> 
                    navController.navigate(Screen.FoodDetail.createRoute(foodId)) 
                },
                onNavigateToCategory = { categoryId -> 
                    navController.navigate(Screen.Category.createRoute(categoryId)) 
                },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToProfile = { navController.navigate(Screen.ProfileDashboard.route) },
                onNavigateToBannerDetail = { bannerId ->
                    navController.navigate(Screen.BannerDetail.createRoute(bannerId))
                }
            )
        }

        // Search Screen
        composable(route = Screen.Search.route) {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFood = { foodId -> 
                    navController.navigate(Screen.FoodDetail.createRoute(foodId)) 
                },
                onNavigateToRestaurant = { restaurantId -> 
                    navController.navigate(Screen.RestaurantDetail.createRoute(restaurantId)) 
                }
            )
        }

        // Notifications Screen
        composable(route = Screen.Notifications.route) {
            NotificationsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Restaurant Detail
        composable(
            route = Screen.RestaurantDetail.route,
            arguments = listOf(navArgument("restaurantId") { type = NavType.StringType })
        ) { backStackEntry ->
            val restaurantId = backStackEntry.arguments?.getString("restaurantId").orEmpty()
            RestaurantDetailScreen(
                restaurantId = restaurantId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFood = { foodId -> 
                    navController.navigate(Screen.FoodDetail.createRoute(foodId)) 
                }
            )
        }

        // Food Detail
        composable(
            route = Screen.FoodDetail.route,
            arguments = listOf(navArgument("foodId") { type = NavType.IntType })
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getInt("foodId") ?: 0
            FoodDetailScreen(
                foodId = foodId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Category Screen
        composable(
            route = Screen.Category.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.IntType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 0
            CategoryScreen(
                categoryId = categoryId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFood = { foodId -> 
                    navController.navigate(Screen.FoodDetail.createRoute(foodId)) 
                }
            )
        }

        // Banner Detail Screen
        composable(
            route = Screen.BannerDetail.route,
            arguments = listOf(navArgument("bannerId") { type = NavType.IntType })
        ) { backStackEntry ->
            val bannerId = backStackEntry.arguments?.getInt("bannerId") ?: 0
            BannerDetailScreen(
                bannerId = bannerId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFood = { foodId -> 
                    navController.navigate(Screen.FoodDetail.createRoute(foodId)) 
                }
            )
        }

        // Cart Screen
        composable(route = Screen.Cart.route) {
            CartScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCheckout = { navController.navigate(Screen.Checkout.route) }
            )
        }

        // Checkout Screen
        composable(route = Screen.Checkout.route) {
            CheckoutScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSuccess = { orderId ->
                    navController.navigate(Screen.OrderSuccess.createRoute(orderId)) {
                        popUpTo(Screen.Cart.route) { inclusive = true }
                    }
                }
            )
        }

        // Order Success Screen
        composable(route = Screen.OrderSuccess.route) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderSuccessScreen(
                orderId = orderId,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToTracking = { trackingOrderId ->
                    navController.navigate(Screen.OrderTracking.createRoute(trackingOrderId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        // Order Tracking Screen
        composable(route = Screen.OrderTracking.route) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderTrackingScreen(
                orderId = orderId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // Profile Dashboard Screen
        composable(route = Screen.ProfileDashboard.route) {
            ProfileDashboardScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToOrderHistory = { navController.navigate(Screen.OrderHistory.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToAdmin = { navController.navigate(Screen.AdminDashboard.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Edit Profile Screen
        composable(route = Screen.EditProfile.route) {
            EditProfileScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Order History Screen
        composable(route = Screen.OrderHistory.route) {
            OrderHistoryScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Favorites Screen
        composable(route = Screen.Favorites.route) {
            FavoritesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFoodDetail = { foodId ->
                    navController.navigate(Screen.FoodDetail.createRoute(foodId))
                }
            )
        }

        // Settings Screen
        composable(route = Screen.Settings.route) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        // ==================== ADMIN ====================

        // Admin Dashboard
        composable(route = Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onNavigateToUsers = { navController.navigate(Screen.AdminUsers.route) },
                onNavigateToOrders = { navController.navigate(Screen.AdminOrders.route) },
                onNavigateToRestaurants = { navController.navigate(Screen.AdminRestaurants.route) },
                onNavigateToVouchers = { navController.navigate(Screen.AdminVouchers.route) },
                onNavigateToAuditLog = { navController.navigate(Screen.AdminAuditLog.route) },
                onNavigateToUploadImage = { navController.navigate(Screen.UploadFoodImage.createRoute(2)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Admin - User Management
        composable(route = Screen.AdminUsers.route) {
            UserManagementScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUserDetail = { userId ->
                    navController.navigate(Screen.AdminUserDetail.createRoute(userId))
                }
            )
        }

        // Admin - User Detail
        composable(
            route = Screen.AdminUserDetail.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            UserDetailScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Admin - Orders Management
        composable(route = Screen.AdminOrders.route) {
            AdminOrdersScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToOrderDetail = { orderId ->
                    navController.navigate(Screen.AdminOrderDetail.createRoute(orderId))
                }
            )
        }

        // Admin - Order Detail
        composable(
            route = Screen.AdminOrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            AdminOrderDetailScreen(
                orderId = orderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Admin - Restaurants Management
        composable(route = Screen.AdminRestaurants.route) {
            AdminRestaurantsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Admin - Vouchers Management
        composable(route = Screen.AdminVouchers.route) {
            AdminVouchersScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Admin - Audit Log
        composable(route = Screen.AdminAuditLog.route) {
            AdminAuditLogScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // UploadFoodImage – màn hình upload ảnh món ăn lên Firebase Storage
        composable(
            route = Screen.UploadFoodImage.route,
            arguments = listOf(navArgument("foodId") { type = NavType.IntType; defaultValue = 2 })
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getInt("foodId") ?: 2
            UploadFoodImageScreen(
                foodId = foodId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

