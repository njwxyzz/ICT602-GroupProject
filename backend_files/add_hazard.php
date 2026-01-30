<?php
header('Content-Type: application/json');

// 1. Connect to Database
$conn = mysqli_connect("localhost", "root", "", "crowdtrack_db");

// 2. Get POST data
$lat = $_POST['latitude'];
$lng = $_POST['longitude'];
$desc = $_POST['description'];

// 3. Insert into Database
$sql = "INSERT INTO hazards (lat, lng, description) VALUES ('$lat', '$lng', '$desc')";

if (mysqli_query($conn, $sql)) {
    echo json_encode(["status" => "success", "message" => "Hazard Reported!"]);
} else {
    echo json_encode(["status" => "error", "message" => "Database Error"]);
}
?>