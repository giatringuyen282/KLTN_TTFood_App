package com.example.a43_kltn_ttfood.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a43_kltn_ttfood.data.repository.AuthRepository
import com.example.a43_kltn_ttfood.ui.components.TTFoodButton
import com.example.a43_kltn_ttfood.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 📧 Màn hình Xác minh Email (Thay thế OtpVerificationScreen)
 * - Hiển thị hướng dẫn xác minh email qua link của Firebase
 * - Nút "Xác nhận đã kích hoạt" kiểm tra trạng thái real-time
 * - Nút "Gửi lại email" với đếm ngược 60 giây
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    phoneOrEmail: String,
    onVerificationSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepo = remember { AuthRepository() }

    var countdown by remember { mutableIntStateOf(60) }
    var canResend by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    // Countdown timer
    LaunchedEffect(canResend) {
        if (!canResend) {
            countdown = 60
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            canResend = true
        }
    }

    // Success bounce animation
    val bounceScale = remember { Animatable(1f) }
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            bounceScale.animateTo(
                targetValue = 1.2f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            bounceScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            )
            delay(1000)
            onVerificationSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Beautiful status icon container
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(bounceScale.value)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = if (isSuccess) {
                                listOf(SuccessGreen.copy(alpha = 0.2f), SuccessGreen.copy(alpha = 0.02f))
                            } else {
                                listOf(Orange500.copy(alpha = 0.15f), Orange500.copy(alpha = 0.01f))
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isSuccess) "✅" else "✉️",
                    fontSize = 54.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Xác minh tài khoản",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Liên kết xác minh đã được gửi đến email:",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500,
                textAlign = TextAlign.Center
            )

            Text(
                text = phoneOrEmail,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Orange500,
                modifier = Modifier.padding(vertical = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Step-by-step instructions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Gray50),
                border = BorderStroke(1.dp, Gray200)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Các bước thực hiện:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    InstructionStep(number = "1", text = "Mở hộp thư Email của bạn.")
                    InstructionStep(number = "2", text = "Tìm email từ TTFood (hoặc Firebase).")
                    InstructionStep(number = "3", text = "Bấm vào đường dẫn xác minh trong email.")
                    InstructionStep(number = "4", text = "Quay lại đây và bấm nút \"Xác nhận đã kích hoạt\".")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error display
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = ErrorRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }

            // Confirm Button
            TTFoodButton(
                text = "Xác nhận đã kích hoạt",
                onClick = {
                    isChecking = true
                    errorMessage = null
                    scope.launch {
                        val result = authRepo.checkIfEmailVerified()
                        isChecking = false
                        result.fold(
                            onSuccess = { isVerified ->
                                if (isVerified) {
                                    isSuccess = true
                                } else {
                                    errorMessage = "Email của bạn vẫn chưa được xác minh. Vui lòng bấm vào liên kết trong email và thử lại."
                                }
                            },
                            onFailure = { error ->
                                errorMessage = error.message ?: "Có lỗi xảy ra khi kiểm tra xác minh."
                            }
                        )
                    }
                },
                isLoading = isChecking,
                enabled = !isChecking && !isSuccess
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Resend link button
            if (canResend) {
                TextButton(
                    onClick = {
                        canResend = false
                        errorMessage = null
                        scope.launch {
                            val result = authRepo.sendVerificationEmail()
                            result.fold(
                                onSuccess = {
                                    Toast.makeText(context, "Đã gửi lại email xác minh!", Toast.LENGTH_SHORT).show()
                                },
                                onFailure = { error ->
                                    errorMessage = error.message ?: "Không thể gửi lại email xác minh."
                                    canResend = true
                                }
                            )
                        }
                    }
                ) {
                    Text(
                        text = "Gửi lại email xác minh",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Orange500
                    )
                }
            } else {
                Text(
                    text = "Gửi lại link sau ${countdown}s",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray500
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun InstructionStep(number: String, text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Orange500.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Orange500
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray700
        )
    }
}
