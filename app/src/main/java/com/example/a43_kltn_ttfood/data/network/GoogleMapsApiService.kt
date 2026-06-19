package com.example.a43_kltn_ttfood.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// ==========================================
// MÔ HÌNH DỮ LIỆU TỪ GOOGLE MAPS API
// ==========================================

data class DistanceMatrixResponse(
    val destination_addresses: List<String>,
    val origin_addresses: List<String>,
    val rows: List<DistanceMatrixRow>,
    val status: String,
    val error_message: String? = null
)

data class DistanceMatrixRow(
    val elements: List<DistanceMatrixElement>
)

data class DistanceMatrixElement(
    val distance: DistanceValue?,
    val duration: DurationValue?,
    val status: String
)

data class DistanceValue(
    val text: String,
    val value: Int // Khoảng cách tính bằng mét (meters)
)

data class DurationValue(
    val text: String,
    val value: Int // Thời gian tính bằng giây (seconds)
)

// ==========================================
// INTERFACE RETROFIT
// ==========================================

interface GoogleMapsApiService {
    @GET("maps/api/distancematrix/json")
    suspend fun getDistance(
        @Query("origins") origins: String,
        @Query("destinations") destinations: String,
        @Query("key") apiKey: String,
        @Query("language") language: String = "vi"
    ): DistanceMatrixResponse
}

object RetrofitInstance {
    private const val BASE_URL = "https://maps.googleapis.com/"

    val mapsApi: GoogleMapsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoogleMapsApiService::class.java)
    }
}
