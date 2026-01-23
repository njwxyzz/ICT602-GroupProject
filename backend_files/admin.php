<?php
include 'db_connect.php';

// Fungsi Padam Data (Clear Button)
if (isset($_POST['clear'])) {
    $conn->query("TRUNCATE TABLE location_logs");
    echo "<script>alert('Database Berjaya Dikosongkan!'); window.location='admin.php';</script>";
}

// Tarik data dari database (Paling baru di atas)
$sql = "SELECT * FROM location_logs ORDER BY id DESC";
$result = $conn->query($sql);
?>

<!DOCTYPE html>
<html>
<head>
    <title>CrowdTrack Admin</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <meta http-equiv="refresh" content="5">
</head>
<body class="bg-light">

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>🛰️ CrowdTrack Live Feed</h2>
        <form method="post" onsubmit="return confirm('Confirm padam semua data?');">
            <button type="submit" name="clear" class="btn btn-danger">🗑️ Clear Data</button>
        </form>
    </div>

    <div class="card shadow-sm">
        <div class="card-body p-0">
            <table class="table table-striped table-hover mb-0">
                <thead class="table-dark">
                    <tr>
                        <th>ID</th>
                        <th>Username</th>
                        <th>User Agent (Device)</th>
                        <th>Latitude</th>
                        <th>Longitude</th>
                        <th>Time</th>
                        <th>Map</th>
                    </tr>
                </thead>
                <tbody>
                    <?php if ($result->num_rows > 0): ?>
                        <?php while($row = $result->fetch_assoc()): ?>
                        <tr>
                            <td><?php echo $row['id']; ?></td>
                            <td><span class="badge bg-primary"><?php echo $row['username']; ?></span></td>
                            <td><small class="text-muted"><?php echo substr($row['user_agent'], 0, 30); ?>...</small></td>
                            <td><?php echo $row['latitude']; ?></td>
                            <td><?php echo $row['longitude']; ?></td>
                            <td><?php echo $row['created_at']; ?></td>
                            <td>
                                <a href="https://www.google.com/maps/search/?api=1&query=<?php echo $row['latitude']; ?>,<?php echo $row['longitude']; ?>" 
                                   target="_blank" class="btn btn-sm btn-outline-success">View</a>
                            </td>
                        </tr>
                        <?php endwhile; ?>
                    <?php else: ?>
                        <tr><td colspan="7" class="text-center py-3">Tiada data. Sila gerakkan Emulator.</td></tr>
                    <?php endif; ?>
                </tbody>
            </table>
        </div>
    </div>
    <div class="mt-3 text-center text-muted">Total Records: <?php echo $result->num_rows; ?></div>
</div>

</body>
</html>