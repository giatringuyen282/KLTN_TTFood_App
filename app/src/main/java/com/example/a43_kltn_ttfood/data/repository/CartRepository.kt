package com.example.a43_kltn_ttfood.data.repository

import com.example.a43_kltn_ttfood.data.model.CartItem
import com.example.a43_kltn_ttfood.data.model.FoodItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CartRepository {
    private val db = FirebaseFirestore.getInstance()
    private val cartCollection = db.collection("cart_items")

    /**
     * Lấy danh sách giỏ hàng của user theo thời gian thực (real-time)
     */
    fun getCart(userId: String): Flow<List<CartItem>> = callbackFlow {
        val listener = cartCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull {
                    it.toObjectSafe(CartItem::class.java)
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Thêm món ăn vào giỏ hàng trên Firestore
     */
    suspend fun addToCart(userId: String, food: FoodItem, quantity: Int, toppings: String): Result<Unit> {
        return try {
            // Kiểm tra xem món ăn này đã có trong giỏ hàng với cùng toppings chưa
            val existingQuery = cartCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("foodId", food.id)
                .whereEqualTo("toppings", toppings)
                .get()
                .await()

            val existingDoc = existingQuery.documents.firstOrNull()

            if (existingDoc != null) {
                // Nếu đã có, cộng dồn số lượng
                val currentQuantity = existingDoc.getLong("quantity")?.toInt() ?: 1
                existingDoc.reference.update("quantity", currentQuantity + quantity).await()
            } else {
                // Nếu chưa có, thêm mới
                val cartItem = CartItem(
                    userId = userId,
                    foodId = food.id,
                    toppings = toppings,
                    quantity = quantity,
                    price = food.price.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0,
                    restaurantName = food.restaurant,
                    foodName = food.name,
                    foodEmoji = food.emoji,
                    foodBgColorVal = food.bgColorVal,
                    foodImageUrl = food.imageUrl
                )
                cartCollection.add(cartItem).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cập nhật số lượng của một mục trong giỏ hàng
     */
    suspend fun updateCartItemQuantity(itemId: String, quantity: Int): Result<Unit> {
        return try {
            if (quantity <= 0) {
                deleteCartItem(itemId)
            } else {
                cartCollection.document(itemId).update("quantity", quantity).await()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Xóa một mục khỏi giỏ hàng
     */
    suspend fun deleteCartItem(itemId: String): Result<Unit> {
        return try {
            cartCollection.document(itemId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Xóa toàn bộ giỏ hàng của user (sau khi đặt hàng thành công)
     */
    suspend fun clearCart(userId: String): Result<Unit> {
        return try {
            val snapshot = cartCollection.whereEqualTo("userId", userId).get().await()
            val batch = db.batch()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
