/**
 * ============================================================
 * TTFood – Tạo tài khoản ADMIN trong Firestore
 * ============================================================
 * Cách dùng:
 *   1. Đặt file serviceAccountKey.json vào cùng thư mục này
 *   2. Chạy: node create_admin.js
 * ============================================================
 */

const admin = require("firebase-admin");
const serviceAccount = require("./serviceAccountKey.json");

// ── Cấu hình ─────────────────────────────────────────────────
const ADMIN_EMAIL = "tring22@uef.edu.vn";   // email tài khoản cần nâng lên admin
const ADMIN_NAME  = "Nguyễn Gia In";        // tên hiển thị
const ADMIN_PHONE = "0900000000";           // số điện thoại (tuỳ chỉnh)
// ─────────────────────────────────────────────────────────────

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const auth = admin.auth();
const db   = admin.firestore();

async function main() {
  console.log("🔍 Đang tìm tài khoản:", ADMIN_EMAIL);

  // 1. Lấy UID từ Firebase Authentication theo email
  let userRecord;
  try {
    userRecord = await auth.getUserByEmail(ADMIN_EMAIL);
  } catch (err) {
    console.error("❌ Không tìm thấy tài khoản trong Firebase Auth:", err.message);
    process.exit(1);
  }

  const uid = userRecord.uid;
  console.log("✅ Tìm thấy UID:", uid);

  // 2. Tạo hoặc ghi đè document trong collection `users`
  const userDoc = {
    id:        uid,
    fullName:  ADMIN_NAME,
    email:     ADMIN_EMAIL,
    phone:     ADMIN_PHONE,
    role:      "admin",          // ← nâng lên admin
    isActive:  true,
    avatarUrl: "",
    fcmToken:  "",
    dob:       "",
    gender:    "",
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  };

  try {
    await db.collection("users").doc(uid).set(userDoc, { merge: true });
    console.log("✅ Đã tạo/cập nhật document admin trong Firestore!");
    console.log("──────────────────────────────────────────────");
    console.log("  Email :", ADMIN_EMAIL);
    console.log("  UID   :", uid);
    console.log("  Role  : admin");
    console.log("──────────────────────────────────────────────");
    console.log("👉 Bây giờ đăng xuất và đăng nhập lại trong app là xong!");
  } catch (err) {
    console.error("❌ Lỗi khi ghi Firestore:", err.message);
    process.exit(1);
  }

  process.exit(0);
}

main();
