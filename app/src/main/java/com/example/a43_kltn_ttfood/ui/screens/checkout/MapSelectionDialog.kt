package com.example.a43_kltn_ttfood.ui.screens.checkout

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.a43_kltn_ttfood.ui.theme.GrabGreen
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapSelectionDialog(
    initialAddress: String,
    onDismiss: () -> Unit,
    onAddressSelected: (String, LatLng) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Default location: TP.HCM (or try to parse from initialAddress or use District 5 HCMC)
    var currentLatLng by remember { mutableStateOf(LatLng(10.7599, 106.6823)) }
    var selectedAddressText by remember { mutableStateOf(initialAddress) }
    var isLocating by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLatLng, 15f)
    }

    // Update lat/lng based on camera moving (Marker in the middle of map approach)
    val isCameraMoving = cameraPositionState.isMoving
    
    LaunchedEffect(cameraPositionState.position.target) {
        if (!isCameraMoving) {
            val target = cameraPositionState.position.target
            currentLatLng = target
            // Geocode target location to address string
            coroutineScope.launch {
                val address = getAddressFromLatLng(context, target.latitude, target.longitude)
                selectedAddressText = address
            }
        }
    }

    // Fused Location Provider for GPS
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            // Permission granted, get GPS
            getUserGPSLocation(context, fusedLocationClient) { location ->
                val gpsLatLng = LatLng(location.latitude, location.longitude)
                currentLatLng = gpsLatLng
                cameraPositionState.position = CameraPosition.fromLatLngZoom(gpsLatLng, 17f)
            }
        } else {
            Toast.makeText(context, "Ứng dụng cần quyền GPS để tự động định vị!", Toast.LENGTH_SHORT).show()
        }
    }

    // Get current location once when opened if we can
    LaunchedEffect(Unit) {
        getUserGPSLocation(context, fusedLocationClient) { location ->
            val gpsLatLng = LatLng(location.latitude, location.longitude)
            currentLatLng = gpsLatLng
            cameraPositionState.position = CameraPosition.fromLatLngZoom(gpsLatLng, 17f)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Google Maps
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = false), // Handled customly to avoid permission crash
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
                )

                // Pin in center of map
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.offset(y = (-20).dp) // Offset for pin point
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Selected Pin",
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.background(Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng", tint = Color.Black)
                    }
                    
                    Text(
                        text = "Chọn vị trí trên bản đồ",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                    
                    Spacer(modifier = Modifier.size(48.dp)) // Equal spacing spacer
                }

                // Bottom Panel showing address info & actions
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, "Địa chỉ", tint = GrabGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Địa chỉ chọn:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.Gray
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = if (isCameraMoving) "Đang xác định vị trí..." else selectedAddressText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.Black,
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // GPS locate button
                            OutlinedButton(
                                onClick = {
                                    requestPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = "GPS")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Định vị")
                            }

                            Button(
                                onClick = {
                                    if (selectedAddressText.isNotBlank()) {
                                        onAddressSelected(selectedAddressText, currentLatLng)
                                    } else {
                                        Toast.makeText(context, "Không thể lấy địa chỉ ở vị trí này!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                                modifier = Modifier.weight(2.5f)
                            ) {
                                Text("Xác nhận vị trí", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Get physical location from GPS
private fun getUserGPSLocation(
    context: Context,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    onSuccess: (Location) -> Unit
) {
    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                onSuccess(location)
            }
        }
    } catch (e: SecurityException) {
        // Handle permission exception
    }
}

// Geocode latitude/longitude to address string
private suspend fun getAddressFromLatLng(context: Context, latitude: Double, longitude: Double): String {
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale("vi", "VN"))
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val addressFragments = mutableListOf<String>()
                for (i in 0..address.maxAddressLineIndex) {
                    addressFragments.add(address.getAddressLine(i))
                }
                addressFragments.joinToString(separator = ", ")
            } else {
                "Tọa độ: $latitude, $longitude"
            }
        } catch (e: Exception) {
            "Tọa độ: $latitude, $longitude"
        }
    }
}
