package com.example.a43_kltn_ttfood.data.util

import com.example.a43_kltn_ttfood.data.model.sampleCategories
import com.example.a43_kltn_ttfood.data.model.sampleFoodItems
import com.example.a43_kltn_ttfood.data.model.sampleRestaurants
import com.example.a43_kltn_ttfood.data.model.FoodCategory
import com.example.a43_kltn_ttfood.data.model.FoodItem
import com.example.a43_kltn_ttfood.data.model.Restaurant
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object DatabaseSeeder {
    private val db = FirebaseFirestore.getInstance()
    private val categoriesCol = db.collection("categories")
    private val foodCol = db.collection("food_items")
    private val restaurantsCol = db.collection("restaurants")

    /**
     * Checks if the collections are empty and, if so, seeds them with sample data.
     * This function should be called once (e.g., on app launch).
     */
    suspend fun seedIfNeeded() {
        try {
            // Seed categories
            val catCount = try { categoriesCol.get().await().size() } catch (e: Exception) { 0 }
            if (catCount == 0) {
                sampleCategories.forEach { categoriesCol.add(it).await() }
            }

            // Seed restaurants
            val restCount = try { restaurantsCol.get().await().size() } catch (e: Exception) { 0 }
            if (restCount == 0) {
                sampleRestaurants.forEach { restaurantsCol.add(it).await() }
            }

            // Seed food items
            val foodCount = try { foodCol.get().await().size() } catch (e: Exception) { 0 }
            if (foodCount == 0) {
                sampleFoodItems.forEach { foodCol.add(it).await() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
