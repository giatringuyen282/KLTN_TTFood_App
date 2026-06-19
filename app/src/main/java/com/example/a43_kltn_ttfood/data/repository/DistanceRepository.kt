package com.example.a43_kltn_ttfood.data.repository

import com.example.a43_kltn_ttfood.data.network.RetrofitInstance
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.math.absoluteValue

class DistanceRepository {
    private val db = FirebaseFirestore.getInstance()
    private val restaurantsCollection = db.collection("restaurants")

    // TODO: Thay bằng Google Maps API Key thực tế từ Google Cloud Console (bật Distance Matrix API)
    private val MAPS_API_KEY = "YOUR_GOOGLE_MAPS_API_KEY_HERE"

    /**
     * Calculate delivery fee using Google Maps API.
     */
    suspend fun calculateDeliveryFee(address: String, restaurantId: String): Int {
        val baseFee = 15000 // Phí cơ bản: 15.000đ
        val pricePerKm = 5000 // 5.000đ mỗi km

        return try {
            // 1. Lấy địa chỉ của nhà hàng từ Firestore (fallback về ĐH Sài Gòn nếu ko tìm thấy)
            val restaurantDoc = try {
                restaurantsCollection.document(restaurantId).get().await()
            } catch (e: Exception) { null }
            
            val restaurantAddress = restaurantDoc?.getString("address") 
                ?: "Trường Đại học Sài Gòn, 273 An Dương Vương, Phường 3, Quận 5, Hồ Chí Minh"

            // 2. Gọi API Google Maps Distance Matrix
            val response = RetrofitInstance.mapsApi.getDistance(
                origins = restaurantAddress,
                destinations = address,
                apiKey = MAPS_API_KEY
            )

            // 3. Xử lý kết quả trả về
            if (response.status == "OK" && response.rows.isNotEmpty()) {
                val element = response.rows[0].elements[0]
                if (element.status == "OK") {
                    val distanceMeters = element.distance?.value ?: 0
                    val distanceKm = distanceMeters / 1000.0
                    
                    // Tính tổng phí
                    return baseFee + (distanceKm * pricePerKm).toInt()
                } else {
                    return calculateMockFee(address)
                }
            } else {
                // Lỗi API (VD: REQUEST_DENIED do sai API Key) -> Fallback
                return calculateMockFee(address)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Lỗi mạng hoặc lỗi kết nối -> Fallback
            return calculateMockFee(address)
        }
    }

    /**
     * Fallback khi API lỗi / chưa có API key.
     * Tính độ dài quãng đường dựa trên chuỗi địa chỉ để thay đổi mức phí (tạo cảm giác chân thực).
     */
    private fun calculateMockFee(address: String): Int {
        val hash = address.hashCode().absoluteValue
        val simulatedDistanceKm = (hash % 6) + 2.0 // Khoảng cách giả định: 2km đến 7km
        return 15000 + (simulatedDistanceKm * 5000).toInt()
    }
}
