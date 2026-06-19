package com.example.a43_kltn_ttfood.data.repository

import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Repository for calculating delivery fee based on distance.
 * This is a simplified implementation using a placeholder logic.
 * In a real app, you would integrate with a maps API to compute distance
 * between the user address and the restaurant location.
 */
class DistanceRepository {
    private val db = FirebaseFirestore.getInstance()
    private val restaurantsCollection = db.collection("restaurants")

    /**
     * Calculate delivery fee.
     * @param address The delivery address string (for now unused).
     * @param restaurantId The restaurant ID to fetch its location.
     * @return delivery fee in VND.
     */
    suspend fun calculateDeliveryFee(address: String, restaurantId: String): Int {
        // Placeholder: fetch restaurant to simulate distance lookup.
        return try {
            // In a real implementation, you'd get GeoPoint for restaurant and compute distance.
            // Here we just return a fixed fee of 15000 VND.
            15000
        } catch (e: Exception) {
            // In case of any error, fallback to default fee.
            15000
        }
    }
}
