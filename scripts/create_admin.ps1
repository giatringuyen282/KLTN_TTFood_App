# ============================================================
# TTFood – Tạo tài khoản ADMIN trong Firestore (PowerShell)
# ============================================================
# Cách dùng:
#   1. Điền API_KEY và UID bên dưới
#   2. Chạy trong PowerShell: .\create_admin.ps1
# ============================================================

# ── BƯỚC 1: Lấy thông tin này từ Firebase Console ───────────
# Firebase Console → Project Settings → General → Web API Key
$API_KEY = "PASTE_YOUR_WEB_API_KEY_HERE"

# Email và mật khẩu tài khoản tring22@uef.edu.vn
$EMAIL    = "tring22@uef.edu.vn"
$PASSWORD = "PASTE_YOUR_PASSWORD_HERE"

# Firebase project ID (lấy từ URL console: project/tt-food-51570)
$PROJECT_ID = "tt-food-51570"

# Thông tin admin
$FULL_NAME = "Nguyen Gia In"
$PHONE     = "0900000000"
# ─────────────────────────────────────────────────────────────

Write-Host "🔐 Đang đăng nhập Firebase..." -ForegroundColor Cyan

# BƯỚC 2: Đăng nhập lấy idToken + localId (UID)
$loginBody = @{
    email             = $EMAIL
    password          = $PASSWORD
    returnSecureToken = $true
} | ConvertTo-Json

try {
    $loginResp = Invoke-RestMethod `
        -Uri "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$API_KEY" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody

    $idToken = $loginResp.idToken
    $uid     = $loginResp.localId
    Write-Host "✅ Đăng nhập thành công! UID: $uid" -ForegroundColor Green

} catch {
    Write-Host "❌ Đăng nhập thất bại: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   Kiểm tra lại API_KEY và mật khẩu." -ForegroundColor Yellow
    exit 1
}

# BƯỚC 3: Ghi document vào Firestore collection `users`
Write-Host "📝 Đang ghi document admin vào Firestore..." -ForegroundColor Cyan

$firestoreBody = @{
    fields = @{
        id        = @{ stringValue = $uid }
        fullName  = @{ stringValue = $FULL_NAME }
        email     = @{ stringValue = $EMAIL }
        phone     = @{ stringValue = $PHONE }
        role      = @{ stringValue = "admin" }
        isActive  = @{ booleanValue = $true }
        avatarUrl = @{ stringValue = "" }
        fcmToken  = @{ stringValue = "" }
        dob       = @{ stringValue = "" }
        gender    = @{ stringValue = "" }
    }
} | ConvertTo-Json -Depth 5

$firestoreUrl = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/users/$uid"

try {
    $resp = Invoke-RestMethod `
        -Uri $firestoreUrl `
        -Method PATCH `
        -ContentType "application/json" `
        -Headers @{ Authorization = "Bearer $idToken" } `
        -Body $firestoreBody

    Write-Host ""
    Write-Host "✅ THÀNH CÔNG! Tài khoản admin đã được tạo." -ForegroundColor Green
    Write-Host "──────────────────────────────────────────────" -ForegroundColor DarkGray
    Write-Host "  Email  : $EMAIL"
    Write-Host "  UID    : $uid"
    Write-Host "  Role   : admin"
    Write-Host "──────────────────────────────────────────────" -ForegroundColor DarkGray
    Write-Host "👉 Đăng xuất và đăng nhập lại trong app là xong!" -ForegroundColor Yellow

} catch {
    Write-Host "❌ Ghi Firestore thất bại: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
