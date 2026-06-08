package com.example.a43_kltn_ttfood.ui.screens.food

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

// ─────────────────────────────────────────────
// Màu sắc nội bộ (dùng lại theme của dự án)
// ─────────────────────────────────────────────
private val Orange500 = Color(0xFFFF6B35)
private val Orange100 = Color(0xFFFFE0D0)
private val GreenSuccess = Color(0xFF2ECC71)
private val RedError = Color(0xFFE74C3C)
private val Gray600 = Color(0xFF555555)
private val BgDark = Color(0xFFF7F7F7)

/**
 * Màn hình demo: CHỌN ẢNH → UPLOAD LÊN FIREBASE STORAGE → LƯU URL VÀO FIRESTORE
 *
 * Cách dùng để test với ảnh cơm tấm:
 *   - foodId = 2 (tương ứng "Cơm Tấm Sườn Bì" trong sampleFoodItems)
 *   - Nếu món chưa tồn tại trong Firestore, gọi FoodRepository.addFoodItem() trước.
 *
 * @param foodId     ID món ăn cần gắn ảnh (mặc định = 2 = cơm tấm)
 * @param onBack     callback khi nhấn nút Quay lại
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadFoodImageScreen(
    foodId: Int = 2,
    onBack: () -> Unit = {}
) {
    val vm: UploadFoodImageViewModel = viewModel()
    val selectedUri by vm.selectedUri.collectAsState()
    val uploadState by vm.uploadState.collectAsState()
    val clipboard = LocalClipboardManager.current

    // Launcher để mở thư viện ảnh
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { vm.onImagePicked(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload ảnh món ăn", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Orange500
                )
            )
        },
        containerColor = BgDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ─── Tiêu đề hướng dẫn ───────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("🍽️ Gắn ảnh cho món ăn", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "foodId = $foodId  •  food_images/$foodId.jpg",
                        fontSize = 12.sp,
                        color = Gray600,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        "Bước 1: Chọn ảnh  →  Bước 2: Upload  →  Bước 3: Copy URL",
                        fontSize = 12.sp,
                        color = Gray600
                    )
                }
            }

            // ─── Khu vực xem trước ảnh ───────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFFF3E0))
                    .border(
                        width = 2.dp,
                        color = if (selectedUri != null) Orange500 else Color(0xFFDDDDDD),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedUri != null) {
                    AsyncImage(
                        model = selectedUri,
                        contentDescription = "Ảnh xem trước",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp))
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = Orange500,
                            modifier = Modifier.size(56.dp)
                        )
                        Text("Nhấn để chọn ảnh", color = Gray600, fontWeight = FontWeight.Medium)
                        Text("PNG, JPG, WEBP", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            // ─── Nút chọn ảnh (nếu đã có ảnh, hiện nút đổi ảnh) ────
            OutlinedButton(
                onClick = { imagePicker.launch("image/*") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Orange500),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(listOf(Orange500, Orange500))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (selectedUri != null) "Đổi ảnh khác" else "Chọn ảnh từ thư viện")
            }

            // ─── Nút UPLOAD ──────────────────────────────────────────
            Button(
                onClick = { vm.uploadImage(foodId) },
                enabled = selectedUri != null && uploadState !is UploadState.Loading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uploadState is UploadState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Đang upload…", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Upload lên Firebase Storage", fontWeight = FontWeight.Bold)
                }
            }

            // ─── Kết quả: Thành công ─────────────────────────────────
            AnimatedVisibility(
                visible = uploadState is UploadState.Success,
                enter = fadeIn(tween(400)) + scaleIn(tween(400))
            ) {
                val url = (uploadState as? UploadState.Success)?.downloadUrl ?: ""
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FFF4)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(GreenSuccess, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Upload thành công! 🎉", fontWeight = FontWeight.Bold, color = GreenSuccess)
                                Text("URL đã được lưu vào Firestore", fontSize = 12.sp, color = Gray600)
                            }
                        }

                        HorizontalDivider(color = Color(0xFFDDEEDD))

                        // URL box
                        Text("Download URL:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFEAF5EA), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                url,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF2D6A2D),
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Copy button
                        TextButton(
                            onClick = { clipboard.setText(AnnotatedString(url)) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("📋 Copy URL", color = Orange500, fontWeight = FontWeight.Bold)
                        }

                        HorizontalDivider(color = Color(0xFFDDEEDD))

                        Text(
                            "✅ Bây giờ bạn có thể dùng URL này trong sampleFoodItems\nhoặc truy vấn lại từ Firestore để hiển thị ảnh trong app.",
                            fontSize = 12.sp,
                            color = Gray600,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = { vm.reset() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Upload ảnh khác")
                        }
                    }
                }
            }

            // ─── Kết quả: Lỗi ────────────────────────────────────────
            AnimatedVisibility(visible = uploadState is UploadState.Error) {
                val msg = (uploadState as? UploadState.Error)?.message ?: ""
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("❌ Upload thất bại", fontWeight = FontWeight.Bold, color = RedError)
                        Text(msg, fontSize = 13.sp, color = Gray600)
                        TextButton(onClick = { vm.reset() }) {
                            Text("Thử lại", color = Orange500)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
