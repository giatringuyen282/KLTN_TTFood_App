package com.example.a43_kltn_ttfood.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a43_kltn_ttfood.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.a43_kltn_ttfood.data.repository.AuthRepository

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow

/**
 * 🚀 Splash Screen
 * - Logo TTFood + tagline với hiệu ứng fade-in + scale
 * - Gradient cam → đỏ (brand color) với các họa tiết hình tròn
 * - Tự động chuyển sang Onboarding sau 2 giây
 * - Kiểm tra token: nếu đã đăng nhập → Home
 */
@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    // Animation states
    val logoScale = remember { Animatable(0.3f) }
    val logoAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Logo fade-in + scale animation
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(800, easing = FastOutSlowInEasing)
            )
        }
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(800)
            )
        }

        // Tagline fade-in sau logo
        delay(500)
        taglineAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(600)
        )

        // Chờ 2 giây rồi chuyển trang
        delay(1500)

        // Kiểm tra xem người dùng đã đăng nhập chưa
        val authRepo = AuthRepository()
        if (authRepo.isLoggedIn) {
            onNavigateToHome()
        } else {
            onNavigateToOnboarding()
        }
    }

    // Gradient background cam → đỏ
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GrabGreen, GrabGreenDark)
                )
            )
    ) {
        // Decorative background shapes
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = 150.dp, y = (-100).dp)
                .clip(CircleShape)
                .background(White.copy(alpha = 0.08f))
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-100).dp, y = 100.dp)
                .clip(CircleShape)
                .background(White.copy(alpha = 0.08f))
        )

        // Main Content
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Container
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .shadow(elevation = 16.dp, shape = CircleShape, spotColor = Color.Black.copy(alpha = 0.2f))
                    .clip(CircleShape)
                    .background(White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🍔",
                    fontSize = 64.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                text = "TTFood",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 46.sp,
                    letterSpacing = (-1).sp,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.25f),
                        offset = Offset(0f, 4f),
                        blurRadius = 8f
                    )
                ),
                color = White,
                modifier = Modifier
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tagline
            Text(
                text = "Đặt món ngon, giao tận nơi",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                ),
                color = White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(taglineAlpha.value)
            )
        }

        // Version info & Loading indicator ở dưới cùng
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(taglineAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = White,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "v1.0.0",
                style = MaterialTheme.typography.labelMedium,
                color = White.copy(alpha = 0.6f)
            )
        }
    }
}
