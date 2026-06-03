package com.example.a43_kltn_ttfood.data.repository

import com.example.a43_kltn_ttfood.data.model.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * 🔐 Repository quản lý Firebase Authentication
 * - Đăng nhập bằng Email/Password
 * - Đăng ký tài khoản mới (tạo cả document Firestore)
 * - Đăng xuất
 * - Lấy thông tin user hiện tại
 * - Lắng nghe trạng thái auth thay đổi
 */
class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val auditCollection = db.collection("audit_logs")

    /** User hiện tại (null = chưa đăng nhập) */
    val currentFirebaseUser: FirebaseUser?
        get() = auth.currentUser

    /** Kiểm tra đã đăng nhập chưa */
    val isLoggedIn: Boolean
        get() = auth.currentUser != null

    /**
     * Đăng nhập bằng Email + Password
     * Trả về Result<User> — User là Firestore document
     */
    suspend fun loginWithEmail(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Không thể lấy thông tin đăng nhập"))

            // Kiểm tra email đã xác minh chưa
            if (!firebaseUser.isEmailVerified) {
                try { firebaseUser.sendEmailVerification().await() } catch (_: Exception) {}
                return Result.failure(Exception("EMAIL_NOT_VERIFIED"))
            }

            val uid = firebaseUser.uid

            // Lấy user profile từ Firestore
            val userDoc = usersCollection.document(uid).get().await()
            val user = userDoc.toObject(User::class.java)
                ?: return Result.failure(Exception("Không tìm thấy thông tin người dùng"))

            // Kiểm tra tài khoản có bị khóa không
            if (!user.isActive) {
                auth.signOut()
                return Result.failure(Exception("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ hỗ trợ."))
            }

            // Ghi audit log: đăng nhập thành công
            try {
                val log = AuditLog(
                    userId = uid,
                    userName = user.fullName,
                    action = AuditAction.LOGIN,
                    targetType = "user",
                    targetId = uid,
                    details = "Đăng nhập thành công"
                )
                auditCollection.add(log).await()
            } catch (_: Exception) { }

            Result.success(user)
        } catch (e: Exception) {
            val message = when {
                e.message == "EMAIL_NOT_VERIFIED" -> "EMAIL_NOT_VERIFIED"
                e.message?.contains("no user record") == true ||
                e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true ->
                    "Email hoặc mật khẩu không đúng"
                e.message?.contains("badly formatted") == true ->
                    "Định dạng email không hợp lệ"
                e.message?.contains("network") == true ->
                    "Lỗi kết nối mạng. Vui lòng thử lại"
                else -> e.message ?: "Đăng nhập thất bại"
            }
            Result.failure(Exception(message))
        }
    }

    /**
     * Đăng ký tài khoản mới
     * 1. Tạo Firebase Auth account
     * 2. Gửi email xác minh
     * 3. Tạo Firestore user document
     */
    suspend fun register(
        fullName: String,
        phone: String,
        email: String,
        password: String
    ): Result<User> {
        return try {
            // 1. Tạo Auth account
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Không thể tạo tài khoản"))
            val uid = firebaseUser.uid

            // 2. Gửi email xác minh
            firebaseUser.sendEmailVerification().await()

            // 3. Tạo Firestore document
            val user = User(
                id = uid,
                fullName = fullName,
                phone = phone,
                email = email,
                role = UserRole.CUSTOMER,
                isActive = true
            )

            usersCollection.document(uid).set(user).await()

            Result.success(user)
        } catch (e: Exception) {
            val message = when {
                e.message?.contains("email address is already in use") == true ->
                    "Email này đã được sử dụng"
                e.message?.contains("weak password") == true ->
                    "Mật khẩu quá yếu (tối thiểu 6 ký tự)"
                e.message?.contains("badly formatted") == true ->
                    "Định dạng email không hợp lệ"
                else -> e.message ?: "Đăng ký thất bại"
            }
            Result.failure(Exception(message))
        }
    }

    /**
     * Đăng xuất
     */
    suspend fun logout() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                val userDoc = usersCollection.document(uid).get().await()
                val userName = userDoc.getString("fullName") ?: ""
                val log = AuditLog(
                    userId = uid,
                    userName = userName,
                    action = AuditAction.LOGOUT,
                    targetType = "user",
                    targetId = uid,
                    details = "Đăng xuất"
                )
                auditCollection.add(log).await()
            } catch (_: Exception) { }
        }
        auth.signOut()
    }

    /**
     * Lấy Firestore User profile của user hiện tại
     */
    suspend fun getCurrentUserProfile(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            usersCollection.document(uid).get().await()
                .toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Lắng nghe trạng thái auth thay đổi (realtime)
     */
    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Gửi lại email xác minh cho user hiện tại
     */
    suspend fun sendVerificationEmail(): Result<Unit> {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Không thể gửi email xác minh"))
        }
    }

    /**
     * Kiểm tra xem user hiện tại đã click link xác minh email chưa
     */
    suspend fun checkIfEmailVerified(): Result<Boolean> {
        return try {
            val user = auth.currentUser
            if (user != null) {
                user.reload().await()
                Result.success(user.isEmailVerified)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cập nhật thông tin profile của user trong Firestore
     */
    suspend fun updateUserProfile(user: User): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Chưa đăng nhập"))
        if (uid != user.id) {
            return Result.failure(Exception("ID người dùng không khớp"))
        }
        return try {
            usersCollection.document(uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gửi email đặt lại mật khẩu
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Gửi email thất bại"))
        }
    }
}
