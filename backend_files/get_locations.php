<?php
// get_locations.php
header('Content-Type: application/json'); // Penting! Bagitahu Android ini fail JSON
include 'db_connect.php';

// Ambil semua data lokasi, susun dari yang paling baru
$sql = "SELECT * FROM location_logs ORDER BY created_at DESC";
$result = $conn->query($sql);

$locations = array();

if ($result->num_rows > 0) {
    // Loop setiap baris data dan masukkan dalam array
    while($row = $result->fetch_assoc()) {
        $locations[] = $row;
    }
}

// Tukar array jadi JSON string
echo json_encode($locations);

$conn->close();
?>