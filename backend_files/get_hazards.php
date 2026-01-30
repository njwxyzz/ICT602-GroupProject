<?php
header('Content-Type: application/json');

// 1. Update DB Name here
$db = "crowdtrack_db"; 
$conn = mysqli_connect("localhost", "root", "", $db);

if (!$conn) {
    die(json_encode(["error" => "Connection failed"]));
}

// 2. Fetch Hazards
$sql = "SELECT lat, lng, description FROM hazards"; 
$result = mysqli_query($conn, $sql);

$hazards = array();
while($row = mysqli_fetch_assoc($result)) {
    $hazards[] = array(
        'lat' => (float)$row['lat'],
        'lng' => (float)$row['lng'],
        'description' => $row['description']
    );
}

echo json_encode($hazards);
mysqli_close($conn);
?>
