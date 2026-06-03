package com.example.a43_kltn_ttfood.ui.screens.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a43_kltn_ttfood.data.repository.AuthRepository
import com.example.a43_kltn_ttfood.ui.components.TTFoodButton
import com.example.a43_kltn_ttfood.ui.components.TTFoodTextField
import com.example.a43_kltn_ttfood.ui.components.SocialLoginButton
import com.example.a43_kltn_ttfood.ui.theme.*
import kotlinx.coroutines.launch

/**
 * 🔐 Màn hình Đăng nhập
 * - Logo + "Chào mừng trở lại!"
 * - Input: SĐT / Email + Mật khẩu (show/hide 👁️)
 * - Checkbox "Ghi nhớ đăng nhập"
 * - "Quên mật khẩu?" → OTP
 * - Social login (Google / Facebook / Apple)
 * - "Chưa có tài khoản? Đăng ký ngay"
 * - Shake animation khi sai mật khẩu
 */
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val authRepo = remember { AuthRepository() }

    // Form states
    var phoneOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberLogin by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Error states
    var phoneOrEmailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var loginErrorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo
            Text(
                text = "🍔",
                fontSize = 56.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "TTFood",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                ),
                color = Orange500
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Chào mừng trở lại!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Đăng nhập để tiếp tục đặt món",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Phone or Email input
            TTFoodTextField(
                value = phoneOrEmail,
                onValueChange = {
                    phoneOrEmail = it
                    phoneOrEmailError = false
                    loginErrorMessage = null
                },
                label = "Số điện thoại hoặc Email",
                leadingIcon = if (phoneOrEmail.contains("@")) Icons.Outlined.Email
                else Icons.Default.Phone,
                isError = phoneOrEmailError,
                errorMessage = if (phoneOrEmailError) "Vui lòng nhập SĐT hoặc email hợp lệ" else null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password input
            TTFoodTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = false
                    loginErrorMessage = null
                },
                label = "Mật khẩu",
                leadingIcon = Icons.Outlined.Lock,
                isPassword = true,
                isError = passwordError || loginErrorMessage != null,
                errorMessage = loginErrorMessage ?: if (passwordError) "Mật khẩu không đúng" else null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Remember me + Forgot password
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { rememberLogin = !rememberLogin }
                ) {
                    Checkbox(
                        checked = rememberLogin,
                        onCheckedChange = { rememberLogin = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Orange500,
                            uncheckedColor = Gray400
                        )
                    )
                    Text(
                        text = "Ghi nhớ đăng nhập",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                }

                // Forgot password
                TextButton(onClick = onNavigateToForgotPassword) {
                    Text(
                        text = "Quên mật khẩu?",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Orange500
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login button — Firebase Auth
            TTFoodButton(
                text = "Đăng nhập",
                onClick = {
                    // Validate
                    phoneOrEmailError = phoneOrEmail.isBlank()
                    passwordError = password.isBlank()

                    if (!phoneOrEmailError && !passwordError) {
                        isLoading = true
                        loginErrorMessage = null

                        scope.launch {
                            val result = authRepo.loginWithEmail(
                                email = phoneOrEmail.trim(),
                                password = password
                            )
                            isLoading = false

                            result.fold(
                                onSuccess = {
                                    onLoginSuccess()
                                },
                                onFailure = { error ->
                                    loginErrorMessage = if (error.message == "EMAIL_NOT_VERIFIED") {
                                        "Tài khoản chưa được xác minh. Vui lòng kiểm tra hộp thư để nhấp vào liên kết xác minh."
                                    } else {
                                        error.message
                                    }
                                }
                            )
                        }
                    }
                },
                isLoading = isLoading,
                enabled = phoneOrEmail.isNotBlank() && password.isNotBlank() && !isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Divider "Hoặc"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Gray300
                )
                Text(
                    text = "  Hoặc đăng nhập bằng  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Gray300
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Social login buttons
            SocialLoginButton(
                text = "Tiếp tục với Google",
                icon = Icons.Default.Email,
                backgroundColor = GoogleRed,
                onClick = { /* TODO: Google login */ }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SocialLoginButton(
                text = "Tiếp tục với Facebook",
                icon = Icons.Default.Phone,
                backgroundColor = FacebookBlue,
                onClick = { /* TODO: Facebook login */ }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Register link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chưa có tài khoản? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
                TextButton(
                    onClick = onNavigateToRegister,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Đăng ký ngay",
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
