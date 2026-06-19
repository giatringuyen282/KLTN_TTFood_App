package com.example.a43_kltn_ttfood.ui.util

import com.example.a43_kltn_ttfood.R

/**
 * Maps food and restaurant names to local drawable resources.
 * Used as a fallback when imageUrl / coverImage / logo from Firestore are empty.
 * This allows the app to display real images even before Firebase Storage URLs are set up.
 */
object LocalImageMapper {

    /**
     * Get a drawable resource ID for a food item by its name.
     * Returns 0 if no match is found (caller should show emoji fallback).
     */
    fun getFoodImage(foodName: String): Int {
        val lower = foodName.lowercase().trim()
        return when {
            // Gà nướng lu
            lower.contains("gà nướng lu thượng") || lower.contains("nguyên con") -> R.drawable.food_ga_nuong
            lower.contains("nửa gà") || lower.contains("1/2 gà") -> R.drawable.food_nua_ga
            lower.contains("đùi gà") -> R.drawable.food_dui_ga
            lower.contains("cánh gà") -> R.drawable.food_canh_ga
            lower.contains("cơm gà") -> R.drawable.food_com_ga
            // Generic gà nướng fallback
            lower.contains("gà nướng") -> R.drawable.food_ga_nuong
            // No match
            else -> 0
        }
    }

    /**
     * Get a drawable resource ID for a restaurant cover image by its name.
     * Returns 0 if no match is found.
     */
    fun getRestaurantCover(restaurantName: String): Int {
        val lower = restaurantName.lowercase().trim()
        return when {
            lower.contains("gà nướng lu") || lower.contains("hoàng côn") -> R.drawable.restaurant_ga_nuong_cover
            else -> 0
        }
    }

    /**
     * Get a drawable resource ID for a restaurant logo by its name.
     * Returns 0 if no match is found.
     */
    fun getRestaurantLogo(restaurantName: String): Int {
        val lower = restaurantName.lowercase().trim()
        return when {
            lower.contains("gà nướng lu") || lower.contains("hoàng côn") -> R.drawable.restaurant_logo_chicken
            else -> 0
        }
    }
}
