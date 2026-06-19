package com.example.a43_kltn_ttfood.ui.screens.checkout

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a43_kltn_ttfood.ui.theme.*

@Composable
fun OrderSuccessScreen(
    orderId: String,
    onNavigateToHome: () -> Unit = {},
    onNavigateToTracking: (String) -> Unit = {}
) {
    // Animation states
    var visible by remember { mutableStateOf(false) }
    
    val scale = animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "Checkmark Scale"
    )

    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        visible = true
        // Simulate automatic Email/SMS
        android.widget.Toast.makeText(context, "Đã gửi tự động SMS & Email xác nhận đơn hàng!", android.widget.Toast.LENGTH_LONG).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Confetti / Success Icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale.value)
                .clip(CircleShape)
                .background(SuccessGreen.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Thành công",
                    tint = White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "🎉 Đặt hàng thành công!",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = Gray900,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Cảm ơn bạn đã đặt hàng tại TTFood.\nMột email xác nhận đã được gửi đến bạn.",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray500,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Order details box
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Gray50),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Mã đơn hàng", color = Gray600)
                    Text("#${orderId.takeLast(6).uppercase()}", fontWeight = FontWeight.Bold, color = Gray900)
                }
                Divider(color = Gray200)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Thời gian giao (Dự kiến)", color = Gray600)
                    Text("19:25 - 19:35", fontWeight = FontWeight.Bold, color = GrabGreen)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Buttons
        Button(
            onClick = { onNavigateToTracking(orderId) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GrabGreen)
        ) {
            Text("Theo dõi đơn hàng", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onNavigateToHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Gray900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Gray300)
        ) {
            Text("Về trang chủ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
