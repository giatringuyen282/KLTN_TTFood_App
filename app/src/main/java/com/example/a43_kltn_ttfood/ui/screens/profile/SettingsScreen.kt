package com.example.a43_kltn_ttfood.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.a43_kltn_ttfood.ui.theme.*

// ==========================================
// 5. SETTINGS
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit = {}) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var pushNotif by remember { mutableStateOf(true) }
    var promoNotif by remember { mutableStateOf(false) }
    var darkMode by remember { mutableStateOf(false) }
    
    var currentLanguage by remember { mutableStateOf("Tiếng Việt") }
    var showLangDialog by remember { mutableStateOf(false) }

    val isEn = currentLanguage == "English"
    val tSettings = if (isEn) "Settings" else "Cài đặt"
    val tNotifications = if (isEn) "Notifications" else "Thông báo"
    val tPushNotif = if (isEn) "Push Notifications (Orders)" else "Thông báo đẩy (Đơn hàng)"
    val tPromoNotif = if (isEn) "Promotional Notifications" else "Thông báo khuyến mãi"
    val tDisplay = if (isEn) "Display" else "Hiển thị"
    val tDarkMode = if (isEn) "Dark Mode" else "Chế độ Tối (Dark Mode)"
    val tLanguage = if (isEn) "Language" else "Ngôn ngữ"
    val tAccount = if (isEn) "Account & Security" else "Tài khoản & Bảo mật"
    val tAddress = if (isEn) "Manage Addresses" else "Quản lý địa chỉ"
    val tPayment = if (isEn) "Payment Methods" else "Phương thức thanh toán"
    val tPassword = if (isEn) "Change Password" else "Đổi mật khẩu"

    Scaffold(
        containerColor = Gray50,
        topBar = {
            TopAppBar(
                title = { Text(tSettings, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(20.dp)
        ) {
            item {
                Text(tNotifications, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = GrabGreen)
                Spacer(modifier = Modifier.height(8.dp))
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column {
                        SettingSwitchRow(tPushNotif, pushNotif) { pushNotif = it }
                        Divider(color = Gray50)
                        SettingSwitchRow(tPromoNotif, promoNotif) { promoNotif = it }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(tDisplay, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = GrabGreen)
                Spacer(modifier = Modifier.height(8.dp))
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column {
                        SettingSwitchRow(tDarkMode, darkMode) { 
                            darkMode = it
                            if (it) {
                                val toastMsg = if (isEn) "Dark Mode theme is under construction!" else "Giao diện Dark Mode đang được hoàn thiện bộ màu!"
                                android.widget.Toast.makeText(context, toastMsg, android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                        Divider(color = Gray50)
                        SettingClickRow(tLanguage, currentLanguage, onClick = { showLangDialog = true })
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(tAccount, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = GrabGreen)
                Spacer(modifier = Modifier.height(8.dp))
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column {
                        SettingClickRow(tAddress, "")
                        Divider(color = Gray50)
                        SettingClickRow(tPayment, "")
                        Divider(color = Gray50)
                        SettingClickRow(tPassword, "")
                    }
                }
            }
        }
    }

    if (showLangDialog) {
        val languages = listOf("Tiếng Việt", "English", "日本語", "한국어")
        AlertDialog(
            onDismissRequest = { showLangDialog = false },
            title = { Text("Chọn Ngôn ngữ", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    languages.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentLanguage = lang
                                    showLangDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentLanguage == lang,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = GrabGreen)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(lang, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLangDialog = false }) { Text("Đóng", color = Gray500) }
            }
        )
    }
}

@Composable
fun SettingSwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = White, checkedTrackColor = GrabGreen)
        )
    }
}

@Composable
fun SettingClickRow(title: String, value: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value.isNotEmpty()) {
                Text(value, color = Gray500, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Gray400, modifier = Modifier.size(20.dp))
        }
    }
}
