<?php
// Sambung ke database
$conn = new mysqli("localhost", "root", "", "crowdtrack_db");

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$username = $_POST['username'];
$password = $_POST['password']; // Password yang user taip (contoh: "123456")

// 1. Cari user
$sql = "SELECT * FROM users WHERE username = '$username'";
$result = $conn->query($sql);

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    $db_password = $row['password']; // Password dalam database

    // 2. CHECK HYBRID (Hash ATAU Plain Text)
    // Cara 1: Check kalau dia Hash yang betul
    if (password_verify($password, $db_password)) {
        echo json_encode(array("status" => "success", "username" => $row['username']));
    } 
    // Cara 2: Check kalau dia Plain Text (untuk user baru macam adam/anas)
    else if ($password == $db_password) {
        echo json_encode(array("status" => "success", "username" => $row['username']));
    } 
    else {
        // Dua-dua salah
        echo json_encode(array("status" => "error", "message" => "Wrong password"));
    }
} else {
    echo json_encode(array("status" => "error", "message" => "User not found"));
}

$conn->close();
?>