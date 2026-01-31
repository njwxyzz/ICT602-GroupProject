<?php
session_start();

// 1. SECURITY CHECK (WAJIB ADA KAT ATAS SEKALI)
if (!isset($_SESSION['admin_logged_in'])) {
    header("Location: admin_login.php");
    exit();
}

// 2. CONFIG DATABASE
$conn = new mysqli("localhost", "root", "", "crowdtrack_db");

// 3. LOGIC ACTIONS
// --- Logout ---
if (isset($_GET['logout'])) {
    session_destroy();
    header("Location: admin_login.php");
    exit();
}
// --- Add New Admin ---
if (isset($_POST['add_admin'])) {
    $new_user = $_POST['new_username'];
    $new_pass = password_hash($_POST['new_password'], PASSWORD_DEFAULT); // Simpan secure hash
    
    $conn->query("INSERT INTO admins (username, password) VALUES ('$new_user', '$new_pass')");
    echo "<script>alert('New Admin Added Successfully!'); window.location='admin.php';</script>";
}
// --- Delete Actions ---
if (isset($_GET['del_user'])) {
    $conn->query("DELETE FROM users WHERE id={$_GET['del_user']}");
    echo "<script>window.location='admin.php?tab=users';</script>";
}
if (isset($_GET['del_hazard'])) {
    $conn->query("DELETE FROM hazards WHERE id={$_GET['del_hazard']}");
    echo "<script>window.location='admin.php?tab=hazards';</script>";
}
if (isset($_GET['clear_feed'])) {
    $conn->query("TRUNCATE TABLE location_logs"); 
    echo "<script>window.location='admin.php';</script>";
}

// 4. STATISTIK
$total_users = 0; $total_hazards = 0; $live_pings = 0;
if($r = $conn->query("SELECT id FROM users")) $total_users = $r->num_rows;
if($r = $conn->query("SELECT id FROM hazards")) $total_hazards = $r->num_rows;
if($r = $conn->query("SELECT id FROM location_logs")) $live_pings = $r->num_rows;

$tab = isset($_GET['tab']) ? $_GET['tab'] : 'dashboard';
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CrowdTrack Admin Pro</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600&display=swap" rel="stylesheet">
    
    <style>
        body { background-color: #f0f2f5; font-family: 'Inter', sans-serif; }
        
        /* SIDEBAR STYLING */
        .sidebar { 
            min-height: 100vh; 
            background: #1e293b; 
            color: #e2e8f0; 
            box-shadow: 4px 0 10px rgba(0,0,0,0.1);
        }
        .sidebar h3 { font-weight: 700; letter-spacing: 1px; color: #fff; }
        .nav-link { 
            color: #94a3b8; 
            margin-bottom: 8px; 
            padding: 12px 20px; 
            border-radius: 8px; 
            transition: all 0.3s; 
            font-weight: 500;
        }
        .nav-link:hover { background-color: #334155; color: #fff; padding-left: 25px; }
        .nav-link.active { background-color: #3b82f6; color: white; box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4); }
        .admin-badge { background: #334155; padding: 10px; border-radius: 10px; margin-bottom: 20px; }

        /* CARD STATS */
        .card-stat { 
            border: none; 
            border-radius: 16px; 
            overflow: hidden; 
            transition: transform 0.3s;
            position: relative;
        }
        .card-stat:hover { transform: translateY(-5px); }
        .bg-gradient-primary { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
        .bg-gradient-danger { background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 99%, #fecfef 100%); background-image: linear-gradient(135deg, #ff6b6b 0%, #ee5253 100%); }
        .bg-gradient-success { background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%); background-image: linear-gradient(135deg, #20bf6b 0%, #0fb9b1 100%); }
        
        /* TABLES */
        .card-table { border: none; border-radius: 12px; box-shadow: 0 5px 20px rgba(0,0,0,0.05); }
        .table thead th { 
            background-color: #f8fafc; 
            color: #475569; 
            font-weight: 600; 
            text-transform: uppercase; 
            font-size: 0.85rem; 
            padding: 15px;
            border-bottom: 2px solid #e2e8f0;
        }
        .table tbody td { vertical-align: middle; padding: 15px; color: #334155; }
        .btn-action { margin-right: 5px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
        
        /* MODAL */
        .modal-content { border-radius: 15px; border: none; }
        .modal-header { background: #f8fafc; border-bottom: 1px solid #e2e8f0; }
    </style>
</head>
<body>

<div class="container-fluid">
    <div class="row">
        <div class="col-md-2 sidebar p-4 d-flex flex-column">
            <h3 class="text-center mb-4"><i class="fas fa-satellite-dish me-2"></i>AdminPro</h3>
            
            <div class="admin-badge text-center">
                <div class="small text-muted">Welcome back,</div>
                <div class="fw-bold text-white"><?php echo $_SESSION['admin_name']; ?></div>
            </div>

            <ul class="nav flex-column mb-auto">
                <li class="nav-item">
                    <a class="nav-link <?php echo ($tab == 'dashboard') ? 'active' : ''; ?>" href="?tab=dashboard">
                        <i class="fas fa-chart-line me-3"></i> Dashboard
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link <?php echo ($tab == 'hazards') ? 'active' : ''; ?>" href="?tab=hazards">
                        <i class="fas fa-exclamation-triangle me-3"></i> Hazards
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link <?php echo ($tab == 'users') ? 'active' : ''; ?>" href="?tab=users">
                        <i class="fas fa-users me-3"></i> Users
                    </a>
                </li>
            </ul>
            
            <hr class="text-secondary opacity-25">
            <a href="?logout=1" class="btn btn-danger w-100 py-2 rounded-3 shadow-sm">
                <i class="fas fa-sign-out-alt me-2"></i> Logout
            </a>
        </div>

        <div class="col-md-10 p-5" style="background-color: #f8f9fa;">
            
            <div class="d-flex justify-content-between align-items-center mb-5">
                <div>
                    <h2 class="fw-bold text-dark mb-1">Overview Dashboard</h2>
                    <p class="text-muted mb-0">Monitor your system performance and data.</p>
                </div>
                <button type="button" class="btn btn-dark shadow-sm px-4 py-2 rounded-3" data-bs-toggle="modal" data-bs-target="#addAdminModal">
                    <i class="fas fa-user-plus me-2"></i> Add Admin
                </button>
            </div>

            <div class="row mb-5">
                <div class="col-md-4">
                    <div class="card card-stat bg-gradient-primary p-4 text-white">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h1 class="fw-bold mb-0"><?php echo $total_users; ?></h1>
                                <span class="opacity-75">Registered Users</span>
                            </div>
                            <i class="fas fa-user fa-3x opacity-25"></i>
                        </div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="card card-stat bg-gradient-danger p-4 text-white">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h1 class="fw-bold mb-0"><?php echo $total_hazards; ?></h1>
                                <span class="opacity-75">Active Hazards</span>
                            </div>
                            <i class="fas fa-fire fa-3x opacity-25"></i>
                        </div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="card card-stat bg-gradient-success p-4 text-white">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h1 class="fw-bold mb-0"><?php echo $live_pings; ?></h1>
                                <span class="opacity-75">Live Location Pings</span>
                            </div>
                            <i class="fas fa-map-marker-alt fa-3x opacity-25"></i>
                        </div>
                    </div>
                </div>
            </div>

            <?php if ($tab == 'dashboard'): ?>
                <div class="card card-table">
                    <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
                        <h5 class="m-0 fw-bold text-primary"><i class="fas fa-satellite me-2"></i>Live Location Feed</h5>
                        <a href="?clear_feed=1" class="btn btn-outline-danger btn-sm rounded-pill px-3" onclick="return confirm('Are you sure you want to clear all tracking history?')">
                            <i class="fas fa-trash me-1"></i> Clear History
                        </a>
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover mb-0">
                                <thead>
                                    <tr>
                                        <th>Username</th>
                                        <th>Coordinates (Lat, Lng)</th>
                                        <th>Time Recorded</th>
                                        <th class="text-end">Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <?php
                                    $res = $conn->query("SELECT * FROM location_logs ORDER BY id DESC LIMIT 50");
                                    if($res && $res->num_rows > 0) {
                                        while($row = $res->fetch_assoc()) {
                                            $url = "http://maps.google.com/?q={$row['latitude']},{$row['longitude']}";
                                            // Handle Time Column
                                            $time_display = isset($row['created_at']) ? $row['created_at'] : '<span class="text-muted fst-italic">Just Now</span>';

                                            echo "<tr>
                                                <td>
                                                    <div class='d-flex align-items-center'>
                                                        <div class='rounded-circle bg-primary text-white d-flex align-items-center justify-content-center me-2' style='width:35px; height:35px;'>
                                                            ".strtoupper(substr($row['username'], 0, 1))."
                                                        </div>
                                                        <span class='fw-bold'>{$row['username']}</span>
                                                    </div>
                                                </td>
                                                <td class='font-monospace'>{$row['latitude']}, {$row['longitude']}</td>
                                                <td>{$time_display}</td>
                                                <td class='text-end'>
                                                    <a href='$url' target='_blank' class='btn btn-sm btn-primary btn-action rounded-3 px-3'>
                                                        <i class='fas fa-map-marked-alt me-1'></i> View on Map
                                                    </a>
                                                </td>
                                            </tr>";
                                        }
                                    } else {
                                        echo "<tr><td colspan='4' class='text-center py-5 text-muted'>No live tracking data available yet.</td></tr>";
                                    }
                                    ?>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            <?php endif; ?>

            <?php if ($tab == 'hazards'): ?>
                <div class="card card-table">
                    <div class="card-header bg-white py-3">
                        <h5 class="m-0 fw-bold text-danger"><i class="fas fa-exclamation-circle me-2"></i>Hazard Management</h5>
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover mb-0">
                                <thead>
                                    <tr>
                                        <th>Hazard Description</th>
                                        <th>Location Coordinates</th>
                                        <th class="text-end">Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <?php
                                    $res = $conn->query("SELECT * FROM hazards ORDER BY id DESC");
                                    while($row = $res->fetch_assoc()) {
                                        $map = "http://maps.google.com/?q={$row['lat']},{$row['lng']}";
                                        
                                        // Badge Logic
                                        $desc = strtolower($row['description']);
                                        $icon = "exclamation-triangle";
                                        $badge_class = "bg-secondary";
                                        
                                        if(strpos($desc, 'fire') !== false) { $icon = "fire"; $badge_class = "bg-danger"; }
                                        elseif(strpos($desc, 'flood') !== false) { $icon = "water"; $badge_class = "bg-primary"; }
                                        elseif(strpos($desc, 'accident') !== false) { $icon = "car-crash"; $badge_class = "bg-warning text-dark"; }

                                        echo "<tr>
                                            <td>
                                                <span class='badge $badge_class me-2 p-2'><i class='fas fa-$icon'></i></span>
                                                <span class='fw-semibold'>{$row['description']}</span>
                                            </td>
                                            <td class='font-monospace'>{$row['lat']}, {$row['lng']}</td>
                                            <td class='text-end'>
                                                <a href='$map' target='_blank' class='btn btn-sm btn-primary btn-action rounded-3'>
                                                    <i class='fas fa-map-marker-alt me-1'></i> View Map
                                                </a>
                                                <a href='?del_hazard={$row['id']}' class='btn btn-sm btn-danger btn-action rounded-3' onclick='return confirm(\"Are you sure you want to delete this hazard report?\")'>
                                                    <i class='fas fa-trash me-1'></i> Delete
                                                </a>
                                            </td>
                                        </tr>";
                                    }
                                    ?>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            <?php endif; ?>

            <?php if ($tab == 'users'): ?>
                <div class="card card-table">
                    <div class="card-header bg-white py-3">
                        <h5 class="m-0 fw-bold text-dark"><i class="fas fa-users-cog me-2"></i>User Management</h5>
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover mb-0">
                                <thead>
                                    <tr>
                                        <th>Username</th>
                                        <th>Email Address</th>
                                        <th>Registration Date</th>
                                        <th class="text-end">Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <?php
                                    $res = $conn->query("SELECT * FROM users ORDER BY id DESC");
                                    while($row = $res->fetch_assoc()) {
                                        $joined = isset($row['created_at']) ? $row['created_at'] : '-';
                                        echo "<tr>
                                            <td>
                                                <div class='d-flex align-items-center'>
                                                    <div class='bg-light text-dark rounded px-2 py-1 me-2 fw-bold border'>ID: {$row['id']}</div>
                                                    <span class='fw-bold text-primary'>{$row['username']}</span>
                                                </div>
                                            </td>
                                            <td>{$row['email']}</td>
                                            <td>{$joined}</td>
                                            <td class='text-end'>
                                                <a href='?del_user={$row['id']}' class='btn btn-sm btn-outline-danger rounded-3' onclick='return confirm(\"Warning: This will permanently remove user {$row['username']}. Continue?\")'>
                                                    <i class='fas fa-user-times me-1'></i> Remove User
                                                </a>
                                            </td>
                                        </tr>";
                                    }
                                    ?>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            <?php endif; ?>

        </div>
    </div>
</div>

<div class="modal fade" id="addAdminModal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content shadow-lg">
            <div class="modal-header">
                <h5 class="modal-title fw-bold"><i class="fas fa-shield-alt me-2 text-primary"></i>Register New Admin</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form method="POST">
                <div class="modal-body p-4">
                    <div class="mb-3">
                        <label class="form-label fw-bold">New Username</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light"><i class="fas fa-user"></i></span>
                            <input type="text" name="new_username" class="form-control" placeholder="Enter username" required>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold">New Password</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light"><i class="fas fa-lock"></i></span>
                            <input type="password" name="new_password" class="form-control" placeholder="Enter secure password" required>
                        </div>
                    </div>
                </div>
                <div class="modal-footer bg-light">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" name="add_admin" class="btn btn-primary px-4">Create Admin</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>