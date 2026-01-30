<?php
include 'db_connect.php';

// Benarkan akses dari semua source (penting untuk mobile app)
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");

// 1. Tangkap data (Sama ada dari POST biasa atau JSON body)
$username = $_POST['username'] ?? '';
$latitude = $_POST['latitude'] ?? '';
$longitude = $_POST['longitude'] ?? '';

// Kalau kosong, cuba baca JSON body (kadang-kadang Android hantar cara ni)
if (empty($username)) {
    $json = file_get_contents("php://input");
    $data = json_decode($json, true);
    if ($data) {
        $username = $data['username'] ?? '';
        $latitude = $data['latitude'] ?? '';
        $longitude = $data['longitude'] ?? '';
    }
}

// 2. Validasi: Pastikan data tak kosong
if (!empty($username) && !empty($latitude) && !empty($longitude)) {
    
    // 3. Tangkap User-Agent (Nama Device)
    $user_agent = $_SERVER['HTTP_USER_AGENT'] ?? 'Unknown Android Device';

    // 4. Masukkan ke Database
    $stmt = $conn->prepare("INSERT INTO location_logs (username, user_agent, latitude, longitude) VALUES (?, ?, ?, ?)");
    $stmt->bind_param("ssdd", $username, $user_agent, $latitude, $longitude);

    if ($stmt->execute()) {
        echo json_encode(["status" => "success", "message" => "Data berjaya disimpan!"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Database Error: " . $stmt->error]);
    }
    $stmt->close();

} else {
    // Kalau data tak cukup
    echo json_encode(["status" => "error", "message" => "Data tak lengkap! Username/Lat/Long missing."]);
}

$conn->close();
?>