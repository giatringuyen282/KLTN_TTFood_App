package com.example.a43_kltn_ttfood.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a43_kltn_ttfood.ui.components.TTFoodButton
import com.example.a43_kltn_ttfood.ui.theme.*
import kotlinx.coroutines.delay

/**
 * 📱 Xác thực OTP
 * - 6 ô nhập OTP tự động focus khi nhập
 * - Đếm ngược 60 giây → nút "Gửi lại OTP"
 * - Tự động xác nhận khi nhập đủ 6 số
 * - Bounce animation khi thành công
 * - Thông báo lỗi khi OTP sai
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    phoneOrEmail: String,
    onVerificationSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var otpValue by remember { mutableStateOf("") }
    var countdown by remember { mutableIntStateOf(60) }
    var canResend by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
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

    // Auto-verify when 6 digits entered
    LaunchedEffect(otpValue) {
        if (otpValue.length == 6) {
            isVerifying = true
            delay(1500) // Simulate API call
            // TODO: Call API verify OTP
            // Simulate success
            isSuccess = true
            delay(800)
            onVerificationSuccess()
        }
    }

    // Bounce animation
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
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Icon
            Text(
                text = if (isSuccess) "✅" else "📱",
                fontSize = 56.sp,
                modifier = Modifier.scale(bounceScale.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Xác thực OTP",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Mã xác thực đã được gửi đến",
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
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // OTP Input boxes
            OtpInputField(
                otpLength = 6,
                otpValue = otpValue,
                onOtpChange = {
                    if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                        otpValue = it
                        isError = false
                    }
                },
                isError = isError,
                isSuccess = isSuccess
            )

            if (isError) {
                Text(
                    text = "Mã OTP không đúng. Vui lòng thử lại.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ErrorRed,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Countdown / Resend
            if (canResend) {
                TextButton(
                    onClick = {
                        canResend = false
                        otpValue = ""
                        isError = false
                        // TODO: Call API resend OTP
                    }
                ) {
                    Text(
                        text = "Gửi lại mã OTP",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Orange500
                    )
                }
            } else {
                Text(
                    text = "Gửi lại mã sau ${countdown}s",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray500
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Verify button
            TTFoodButton(
                text = "Xác nhận",
                onClick = {
                    if (otpValue.length == 6) {
                        isVerifying = true
                        // Verification happens in LaunchedEffect
                    }
                },
                isLoading = isVerifying,
                enabled = otpValue.length == 6 && !isVerifying
            )
        }
    }
}

/**
 * 6 ô OTP tự động focus
 */
@Composable
internal fun OtpInputField(
    otpLength: Int,
    otpValue: String,
    onOtpChange: (String) -> Unit,
    isError: Boolean,
    isSuccess: Boolean
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        // Đợi composable attach xong rồi mới request focus
        kotlinx.coroutines.delay(100)
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {
            // Ignore if focus request fails
        }
    }

    Box {
        // Hidden text field for input — dùng alpha(0f) thay vì size(0.dp) để tránh crash
        BasicTextField(
            value = otpValue,
            onValueChange = onOtpChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .focusRequester(focusRequester)
                .size(1.dp)
                .alpha(0f)
        )

        // Visible OTP boxes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            repeat(otpLength) { index ->
                val char = otpValue.getOrNull(index)?.toString() ?: ""
                val isFocused = otpValue.length == index

                val borderColor = when {
                    isSuccess -> SuccessGreen
                    isError -> ErrorRed
                    isFocused -> Orange500
                    char.isNotEmpty() -> Orange400
                    else -> Gray300
                }

                val bgColor = when {
                    isSuccess -> SuccessGreen.copy(alpha = 0.1f)
                    isError -> ErrorRed.copy(alpha = 0.1f)
                    char.isNotEmpty() -> Orange500.copy(alpha = 0.05f)
                    else -> Gray50
                }

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            color = bgColor,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .border(
                            width = if (isFocused) 2.dp else 1.5.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
