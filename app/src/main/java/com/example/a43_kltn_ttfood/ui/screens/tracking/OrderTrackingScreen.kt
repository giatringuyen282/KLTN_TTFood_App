package com.example.a43_kltn_ttfood.ui.screens.tracking

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a43_kltn_ttfood.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var etaMinutes by remember { mutableIntStateOf(20) }

    // Real-time Simulation
    LaunchedEffect(Unit) {
        while (currentStep < 5) {
            delay(4000) // 4 seconds per step for demo
            currentStep++
            etaMinutes -= 4
        }
    }

    var showReview by remember { mutableStateOf(false) }
    LaunchedEffect(currentStep) {
        if (currentStep == 5) {
            delay(1500)
            showReview = true
        }
    }

    Scaffold(
        containerColor = Gray50,
        topBar = {
            TopAppBar(
                title = { Text("Theo dõi đơn hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Mock Live Map
            MockLiveMap(currentStep = currentStep, modifier = Modifier.weight(0.4f))

            // 2. Info Bottom Sheet (simulated as remaining column)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    // Header ETA
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(if (currentStep == 5) "Đã giao hàng" else "Dự kiến giao hàng", color = Gray500, style = MaterialTheme.typography.bodyMedium)
                                Text(if (currentStep == 5) "Thành công" else "$etaMinutes phút", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = Orange500)
                            }
                            Text("Mã: #TTF892314", color = Gray500, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Divider(modifier = Modifier.padding(vertical = 16.dp), color = Gray100)
                    }

                    // Driver Info
                    if (currentStep >= 3) {
                        item {
                            DriverInfoCard()
                            Divider(modifier = Modifier.padding(vertical = 16.dp), color = Gray100)
                        }
                    }

                    // Timeline
                    item {
                        Text("Trạng thái đơn hàng", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(16.dp))
                        OrderStatusTimeline(currentStep = currentStep)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Review Modal
    if (showReview) {
        ReviewBottomSheet(
            onDismiss = { showReview = false },
            onSubmit = { 
                showReview = false
                onNavigateToHome()
            }
        )
    }
}

@Composable
fun MockLiveMap(currentStep: Int, modifier: Modifier = Modifier) {
    // Animating shipper position
    val progress by animateFloatAsState(
        targetValue = currentStep / 5f,
        animationSpec = tween(1000, easing = LinearEasing),
        label = "Shipper Position"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Gray200)
    ) {
        // Mock Map Grid Pattern
        Text("🗺️ Map API Placeholder", modifier = Modifier.align(Alignment.Center).padding(16.dp), color = Gray500, fontSize = 18.sp)
        
        // Mock Route Line
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.6f)
                .height(4.dp)
                .background(Orange200)
        )
        
        // Restaurant Marker (Left)
        Box(modifier = Modifier.align(Alignment.CenterStart).padding(start = 40.dp)) {
            Text("🏪", fontSize = 32.sp)
        }
        
        // User Marker (Right)
        Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 40.dp)) {
            Text("📍", fontSize = 32.sp)
        }
        
        // Shipper Moving Marker
        if (currentStep >= 3) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 40.dp + (progress * 220).dp, bottom = 20.dp) // Simulated movement
            ) {
                Text("🛵", fontSize = 36.sp)
            }
        }
    }
}

@Composable
fun DriverInfoCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Gray50)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(GradientStart),
            contentAlignment = Alignment.Center
        ) {
            Text("👨", fontSize = 32.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Trần Văn Tài xế", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("59A1-123.45 • Honda AirBlade", style = MaterialTheme.typography.bodySmall, color = Gray600)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.Star, null, tint = WarningYellow, modifier = Modifier.size(16.dp))
                Text(" 4.9 (1.2k+ chuyến)", style = MaterialTheme.typography.labelSmall, color = Gray700)
            }
        }
        // Action Buttons
        Row {
            IconButton(onClick = {}, modifier = Modifier.size(40.dp).clip(CircleShape).background(White)) {
                Icon(Icons.Default.Call, "Gọi điện", tint = SuccessGreen)
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {}, modifier = Modifier.size(40.dp).clip(CircleShape).background(White)) {
                Icon(Icons.Default.Email, "Nhắn tin", tint = InfoBlue)
            }
        }
    }
}

@Composable
fun OrderStatusTimeline(currentStep: Int) {
    val steps = listOf(
        "Đặt hàng thành công",
        "Nhà hàng đã xác nhận",
        "Đang chuẩn bị món",
        "Shipper đang lấy hàng",
        "Đang trên đường giao",
        "Giao hàng thành công"
    )

    Column {
        steps.forEachIndexed { index, title ->
            val isCompleted = currentStep >= index
            val isActive = currentStep == index
            
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                // Line and Dot
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (isCompleted) SuccessGreen else Gray300)
                            .border(4.dp, if (isActive) SuccessGreen.copy(alpha = 0.3f) else Color.Transparent, CircleShape)
                    )
                    if (index < steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .weight(1f)
                                .background(if (currentStep > index) SuccessGreen else Gray200)
                        )
                    }
                }
                
                // Text
                Column(modifier = Modifier.padding(start = 12.dp, bottom = 24.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal),
                        color = if (isCompleted) Gray900 else Gray500
                    )
                    if (isCompleted) {
                        Text("19:0${index}", style = MaterialTheme.typography.labelSmall, color = Gray500) // Mock timestamp
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewBottomSheet(onDismiss: () -> Unit, onSubmit: () -> Unit) {
    var rating by remember { mutableIntStateOf(5) }
    var note by remember { mutableStateOf("") }
    
    val tags = listOf("Giao nhanh 👍", "Món ngon 😋", "Đóng gói đẹp", "Nóng hổi 🍲", "Shipper thân thiện")
    val selectedTags = remember { mutableStateListOf<String>() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Đánh giá đơn hàng ⭐", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Hãy chia sẻ trải nghiệm của bạn nhé!", color = Gray600)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Stars
            Row(horizontalArrangement = Arrangement.Center) {
                for (i in 1..5) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Sao",
                        tint = if (i <= rating) WarningYellow else Gray200,
                        modifier = Modifier.size(48.dp).clickable { rating = i }.padding(4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Tags
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tags) { tag ->
                    val isSelected = selectedTags.contains(tag)
                    FilterChip(
                        selected = isSelected,
                        onClick = { if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag) },
                        label = { Text(tag) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Orange50,
                            selectedLabelColor = Orange500
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Orange500 else Gray300
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Text note
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text("Nhận xét thêm (tùy chọn)...") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Orange500)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange500)
            ) {
                Text("Gửi đánh giá", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
