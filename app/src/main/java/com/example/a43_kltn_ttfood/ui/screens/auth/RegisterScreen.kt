package com.example.a43_kltn_ttfood.ui.screens.auth

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a43_kltn_ttfood.ui.components.PasswordStrengthIndicator
import com.example.a43_kltn_ttfood.ui.components.TTFoodButton
import com.example.a43_kltn_ttfood.ui.components.TTFoodTextField
import com.example.a43_kltn_ttfood.ui.theme.*

/**
 * ✍️ Màn hình Đăng ký
 * - Họ tên, SĐT, Email, Mật khẩu + Xác nhận
 * - Password strength indicator
 * - Checkbox đồng ý Điều khoản & Chính sách
 * - Gửi OTP xác thực
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToOtp: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    // Form states
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreeTerms by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Error states
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        var valid = true

        if (fullName.isBlank()) {
            fullNameError = "Vui lòng nhập họ và tên"
            valid = false
        } else fullNameError = null

        if (phone.isBlank() || phone.length < 10) {
            phoneError = "Số điện thoại không hợp lệ"
            valid = false
        } else phoneError = null

        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (email.isBlank() || !emailRegex.matches(email)) {
            emailError = "Email không hợp lệ"
            valid = false
        } else emailError = null

        if (password.length < 6) {
            passwordError = "Mật khẩu phải có ít nhất 6 ký tự"
            valid = false
        } else passwordError = null

        if (confirmPassword != password) {
            confirmPasswordError = "Mật khẩu xác nhận không khớp"
            valid = false
        } else confirmPasswordError = null

        return valid
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
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Tạo tài khoản",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Đăng ký để bắt đầu đặt món ngon",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            // Full Name
            TTFoodTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    fullNameError = null
                },
                label = "Họ và tên",
                leadingIcon = Icons.Outlined.Person,
                isError = fullNameError != null,
                errorMessage = fullNameError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Phone
            TTFoodTextField(
                value = phone,
                onValueChange = {
                    phone = it.filter { c -> c.isDigit() }
                    phoneError = null
                },
                label = "Số điện thoại",
                leadingIcon = Icons.Outlined.Phone,
                isError = phoneError != null,
                errorMessage = phoneError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Email
            TTFoodTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = "Email",
                leadingIcon = Icons.Outlined.Email,
                isError = emailError != null,
                errorMessage = emailError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Password
            TTFoodTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                label = "Mật khẩu",
                leadingIcon = Icons.Outlined.Lock,
                isPassword = true,
                isError = passwordError != null,
                errorMessage = passwordError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            // Password strength
            PasswordStrengthIndicator(
                password = password,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Confirm Password
            TTFoodTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError = null
                },
                label = "Xác nhận mật khẩu",
                leadingIcon = Icons.Outlined.Lock,
                isPassword = true,
                isError = confirmPasswordError != null,
                errorMessage = confirmPasswordError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Terms checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { agreeTerms = !agreeTerms },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = agreeTerms,
                    onCheckedChange = { agreeTerms = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Orange500,
                        uncheckedColor = Gray400
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Tôi đồng ý với ",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
                Text(
                    text = "Điều khoản sử dụng",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Orange500,
                    modifier = Modifier.clickable { /* TODO: Open terms */ }
                )
                Text(
                    text = " & ",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
                Text(
                    text = "Chính sách",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Orange500,
                    modifier = Modifier.clickable { /* TODO: Open policy */ }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Register button
            TTFoodButton(
                text = "Đăng ký",
                onClick = {
                    if (validate() && agreeTerms) {
                        isLoading = true
                        // TODO: Call API register → send OTP
                        onNavigateToOtp(phone)
                    }
                },
                isLoading = isLoading,
                enabled = agreeTerms && fullName.isNotBlank() && phone.isNotBlank()
                        && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Login link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đã có tài khoản? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
                TextButton(
                    onClick = onNavigateBack,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Đăng nhập",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Orange500
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
