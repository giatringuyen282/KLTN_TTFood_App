package com.example.a43_kltn_ttfood.ui.screens.food

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a43_kltn_ttfood.data.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Trạng thái của màn hình upload ảnh */
sealed class UploadState {
    data object Idle : UploadState()
    data object Loading : UploadState()
    data class Success(val downloadUrl: String) : UploadState()
    data class Error(val message: String) : UploadState()
}

/**
 * ViewModel quản lý luồng:
 *   1. Người dùng chọn ảnh từ thư viện → [onImagePicked]
 *   2. Nhấn "Upload" → [uploadImage]  gọi [FoodRepository.uploadImageAndSaveUrl]
 *   3. UI quan sát [uploadState] và hiển thị kết quả
 */
class UploadFoodImageViewModel : ViewModel() {

    private val repository = FoodRepository()

    /** Uri ảnh đã chọn từ thư viện thiết bị */
    private val _selectedUri = MutableStateFlow<Uri?>(null)
    val selectedUri: StateFlow<Uri?> = _selectedUri.asStateFlow()

    /** Trạng thái upload: Idle / Loading / Success / Error */
    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    /** Gọi khi người dùng đã chọn ảnh từ thư viện */
    fun onImagePicked(uri: Uri) {
        _selectedUri.value = uri
        _uploadState.value = UploadState.Idle   // reset trạng thái cũ
    }

    /**
     * Thực hiện upload ảnh lên Firebase Storage
     * rồi lưu download URL vào trường [imageUrl] của món ăn trong Firestore.
     *
     * @param foodId  ID số nguyên của món ăn (phải tồn tại trong collection food_items).
     *                Dùng `foodId = 2` để test với "Cơm Tấm Sườn Bì".
     */
    fun uploadImage(foodId: Int) {
        val uri = _selectedUri.value
        if (uri == null) {
            _uploadState.value = UploadState.Error("Chưa chọn ảnh!")
            return
        }

        viewModelScope.launch {
            _uploadState.value = UploadState.Loading

            val result = repository.uploadImageAndSaveUrl(
                foodId = foodId,
                imageUri = uri
            )

            _uploadState.value = result.fold(
                onSuccess = { url -> UploadState.Success(url) },
                onFailure = { e -> UploadState.Error(e.message ?: "Lỗi không xác định") }
            )
        }
    }

    /** Reset về trạng thái ban đầu (dùng sau khi upload xong hoặc khi muốn upload ảnh khác) */
    fun reset() {
        _selectedUri.value = null
        _uploadState.value = UploadState.Idle
    }
}
