package com.example.a43_kltn_ttfood.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a43_kltn_ttfood.ui.components.TTFoodButton
import com.example.a43_kltn_ttfood.ui.components.TTFoodTextField
import com.example.a43_kltn_ttfood.ui.theme.*
import kotlinx.coroutines.delay

/**
 * 🔄 Đặt lại mật khẩu - 3 bước
 * - Bước 1: Nhập email/SĐT → gửi OTP
 * - Bước 2: Nhập OTP xác thực
 * - Bước 3: Nhập mật khẩu mới + xác nhận
 * - Progress bar 3 bước
 * - Thông báo thành công với icon ✅ + animation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    onResetSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }
    var isLoading by remember { mutableStateOf(false) }
    var isComplete by remember { mutableStateOf(false) }

    // Step 1: Email/Phone
    var emailOrPhone by remember { mutableStateOf("") }
    var emailOrPhoneError by remember { mutableStateOf<String?>(null) }

    // Step 2: OTP
    var otpValue by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf(false) }

    // Step 3: New password
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var newPasswordError by remember { mutableStateOf<String?>(null) }
    var confirmNewPasswordError by remember { mutableStateOf<String?>(null) }

    // Success animation
    val successScale = remember { Animatable(0f) }
    LaunchedEffect(isComplete) {
        if (isComplete) {
            successScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            delay(2000)
            onResetSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    if (!isComplete) {
                        IconButton(onClick = {
                            if (currentStep > 1) currentStep-- else onNavigateBack()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay lại"
                            )
                        }
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
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isComplete) {
                // Progress bar 3 bước
                StepProgressBar(
                    totalSteps = 3,
                    currentStep = currentStep,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Đặt lại mật khẩu",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Step description
                val stepDesc = when (currentStep) {
                    1 -> "Nhập email hoặc số điện thoại để nhận mã OTP"
                    2 -> "Nhập mã OTP đã gửi đến thiết bị của bạn"
                    3 -> "Tạo mật khẩu mới cho tài khoản"
                    else -> ""
                }
                Text(
                    text = stepDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray500,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Step content
                when (currentStep) {
                    1 -> StepOneContent(
                        emailOrPhone = emailOrPhone,
                        onValueChange = {
                            emailOrPhone = it
                            emailOrPhoneError = null
                        },
                        error = emailOrPhoneError
                    )

                    2 -> StepTwoContent(
                        otpValue = otpValue,
                        onOtpChange = {
                            if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                otpValue = it
                                otpError = false
                            }
                        },
                        isError = otpError,
                        emailOrPhone = emailOrPhone
                    )

                    3 -> StepThreeContent(
                        newPassword = newPassword,
                        onNewPasswordChange = {
                            newPassword = it
                            newPasswordError = null
                        },
                        confirmNewPassword = confirmNewPassword,
                        onConfirmChange = {
                            confirmNewPassword = it
                            confirmNewPasswordError = null
                        },
                        newPasswordError = newPasswordError,
                        confirmNewPasswordError = confirmNewPasswordError
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Action button
                TTFoodButton(
                    text = when (currentStep) {
                        1 -> "Gửi mã OTP"
                        2 -> "Xác nhận"
                        3 -> "Đặt lại mật khẩu"
                        else -> ""
                    },
                    onClick = {
                        when (currentStep) {
                            1 -> {
                                if (emailOrPhone.isBlank()) {
                                    emailOrPhoneError = "Vui lòng nhập email hoặc SĐT"
                                } else {
                                    isLoading = true
                                    // TODO: Call API send OTP
                                    currentStep = 2
                                    isLoading = false
                                }
                            }

                            2 -> {
                                if (otpValue != "123456") {
                                    otpError = true
                                } else {
                                    currentStep = 3
                                    otpError = false
                                }
                            }

                            3 -> {
                                var valid = true
                                if (newPassword.length < 6) {
                                    newPasswordError = "Mật khẩu phải có ít nhất 6 ký tự"
                                    valid = false
                                }
                                if (confirmNewPassword != newPassword) {
                                    confirmNewPasswordError = "Mật khẩu không khớp"
                                    valid = false
                                }
                                if (valid) {
                                    isLoading = true
                                    // TODO: Call API reset password
                                    isComplete = true
                                    isLoading = false
                                }
                            }
                        }
                    },
                    isLoading = isLoading
                )
            } else {
                // Success screen
                Spacer(modifier = Modifier.height(80.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(successScale.value)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    SuccessGreen.copy(alpha = 0.2f),
                                    SuccessGreen.copy(alpha = 0.05f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✅",
                        fontSize = 56.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Đặt lại thành công!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = SuccessGreen
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Mật khẩu đã được cập nhật.\nBạn sẽ được chuyển về đăng nhập...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray500,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun StepOneContent(
    emailOrPhone: String,
    onValueChange: (String) -> Unit,
    error: String?
) {
    TTFoodTextField(
        value = emailOrPhone,
        onValueChange = onValueChange,
        label = "Email hoặc Số điện thoại",
        leadingIcon = if (emailOrPhone.contains("@")) Icons.Outlined.Email
        else Icons.Outlined.Phone,
        isError = error != null,
        errorMessage = error,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done
        )
    )
}

@Composable
private fun StepTwoContent(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    isError: Boolean,
    emailOrPhone: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Mã đã gửi đến: $emailOrPhone",
            style = MaterialTheme.typography.bodySmall,
            color = Gray500
        )

        Spacer(modifier = Modifier.height(24.dp))

        OtpInputField(
            otpLength = 6,
            otpValue = otpValue,
            onOtpChange = onOtpChange,
            isError = isError,
            isSuccess = false
        )

        if (isError) {
            Text(
                text = "Mã OTP không đúng",
                style = MaterialTheme.typography.bodySmall,
                color = ErrorRed,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun StepThreeContent(
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    confirmNewPassword: String,
    onConfirmChange: (String) -> Unit,
    newPasswordError: String?,
    confirmNewPasswordError: String?
) {
    val focusManager = LocalFocusManager.current

    TTFoodTextField(
        value = newPassword,
        onValueChange = onNewPasswordChange,
        label = "Mật khẩu mới",
        leadingIcon = Icons.Outlined.Lock,
        isPassword = true,
        isError = newPasswordError != null,
        errorMessage = newPasswordError,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        )
    )

    com.example.a43_kltn_ttfood.ui.components.PasswordStrengthIndicator(
        password = newPassword,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    TTFoodTextField(
        value = confirmNewPassword,
        onValueChange = onConfirmChange,
        label = "Xác nhận mật khẩu mới",
        leadingIcon = Icons.Outlined.Lock,
        isPassword = true,
        isError = confirmNewPasswordError != null,
        errorMessage = confirmNewPasswordError,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        )
    )
}

/**
 * Progress bar 3 bước ở đầu màn hình
 */
@Composable
private fun StepProgressBar(
    totalSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val step = index + 1
            val isActive = step <= currentStep

            val animatedWidth by animateFloatAsState(
                targetValue = if (isActive) 1f else 0f,
                animationSpec = tween(400),
                label = "stepProgress"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Gray200)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedWidth)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(GradientStart, GradientEnd)
                            )
                        )
                )
            }
        }
    }

    // Step labels
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        listOf("Nhập SĐT/Email", "Xác thực OTP", "Mật khẩu mới").forEachIndexed { index, label ->
            val step = index + 1
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (step <= currentStep) Orange500 else Gray400,
                fontWeight = if (step == currentStep) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

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
        delay(100)
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Box {
        // Hidden text field for input
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
