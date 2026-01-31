<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Login - CrowdTrack</title>
    
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;600&display=swap" rel="stylesheet">

    <style>
        /* 1. BACKGROUND PETA */
        body {
            /* Gambar Peta Gelap dari Unsplash */
            background-image: url('https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=2072&auto=format&fit=crop');
            background-size: cover;
            background-position: center;
            height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: 'Poppins', sans-serif;
            overflow: hidden;
        }

        /* Overlay Gelap supaya tulisan nampak */
        body::before {
            content: "";
            position: absolute;
            top: 0; left: 0; right: 0; bottom: 0;
            background: rgba(0, 0, 0, 0.6); /* Gelapkan background 60% */
            z-index: -1;
        }

        /* 2. EFFECT GLASSMORPHISM (KACA) */
        .glass-card {
            background: rgba(255, 255, 255, 0.1); /* Putih lutsinar 10% */
            backdrop-filter: blur(15px);           /* Effect Blur belakang kad */
            -webkit-backdrop-filter: blur(15px);
            border: 1px solid rgba(255, 255, 255, 0.2); /* Border nipis */
            box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);
            border-radius: 20px;
            width: 100%;
            max-width: 400px;
            padding: 40px;
            color: white;
        }

        /* 3. INPUT FIELD CUSTOM */
        .form-control {
            background: rgba(255, 255, 255, 0.1);
            border: 1px solid rgba(255, 255, 255, 0.2);
            color: white;
            border-radius: 10px;
            padding: 12px 15px;
        }

        .form-control:focus {
            background: rgba(255, 255, 255, 0.2);
            border-color: #4e73df;
            color: white;
            box-shadow: none;
        }
        
        /* Placeholder warna pudar */
        ::placeholder { color: rgba(255, 255, 255, 0.6) !important; }

        .input-group-text {
            background: rgba(255, 255, 255, 0.1);
            border: 1px solid rgba(255, 255, 255, 0.2);
            color: white;
            border-right: none;
        }

        /* 4. BUTTON GLOW */
        .btn-glow {
            background: linear-gradient(45deg, #4e73df, #224abe);
            border: none;
            color: white;
            padding: 12px;
            border-radius: 10px;
            font-weight: 600;
            letter-spacing: 1px;
            transition: all 0.3s ease;
        }
        .btn-glow:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(78, 115, 223, 0.4);
            background: linear-gradient(45deg, #224abe, #4e73df);
            color: white;
        }

        .brand-icon {
            font-size: 3rem;
            background: -webkit-linear-gradient(#4e73df, #00d2ff);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            margin-bottom: 10px;
        }
    </style>
</head>
<body>

<?php
session_start();
$conn = new mysqli("localhost", "root", "", "crowdtrack_db");

if (isset($_POST['login'])) {
    $username = $_POST['username'];
    $password = $_POST['password'];

    // Secure sikit guna prepared statement (Optional tapi recommended)
    $result = $conn->query("SELECT * FROM admins WHERE username='$username'");
    if ($result->num_rows > 0) {
        $row = $result->fetch_assoc();
        if (password_verify($password, $row['password']) || $password == $row['password']) {
            $_SESSION['admin_logged_in'] = true;
            $_SESSION['admin_name'] = $row['username'];
            header("Location: admin.php"); 
            exit();
        } else {
            $error = "Incorrect Password";
        }
    } else {
        $error = "Admin account not found";
    }
}
?>

<div class="glass-card">
    <div class="text-center mb-4">
        <i class="fas fa-satellite-dish brand-icon"></i>
        <h3 class="fw-bold">Admin Portal</h3>
        <p class="text-white-50 small">Secure Access CrowdTrack</p>
    </div>
    
    <?php if(isset($error)) echo "<div class='alert alert-danger bg-danger bg-opacity-75 text-white border-0'>$error</div>"; ?>

    <form method="POST">
        <div class="mb-3">
            <label class="small text-white-50 mb-1">Username</label>
            <div class="input-group">
                <span class="input-group-text"><i class="fas fa-user"></i></span>
                <input type="text" name="username" class="form-control" placeholder="Enter ID" required>
            </div>
        </div>
        
        <div class="mb-4">
            <label class="small text-white-50 mb-1">Password</label>
            <div class="input-group">
                <span class="input-group-text"><i class="fas fa-lock"></i></span>
                <input type="password" name="password" class="form-control" placeholder="Enter Password" required>
            </div>
        </div>

        <button type="submit" name="login" class="btn btn-glow w-100 mb-3">
            LOGIN SYSTEM
        </button>
    </form>

    <div class="text-center">
        <a href="../index.php" class="text-white-50 text-decoration-none small hover-white">
            <i class="fas fa-arrow-left me-1"></i> Back to Main Site
        </a>
    </div>
</div>

</body>
</html>