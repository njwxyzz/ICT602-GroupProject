<?php
include 'db_connect.php';

$username = $_POST['username'];
$password = $_POST['password'];

$sql = "SELECT * FROM users WHERE username = '$username'";
$result = $conn->query($sql);

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    // Verify the hashed password
    if (password_verify($password, $row['password'])) {
        echo json_encode(["status" => "success", "message" => "Login successful", "username" => $username]);
    } else {
        echo json_encode(["status" => "error", "message" => "Invalid password"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "User not found"]);
}

$conn->close();
?>