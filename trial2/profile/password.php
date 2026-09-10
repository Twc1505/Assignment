<?php
include 'db.php';
session_start();

// Redirect if user is not logged in
if (!isset($_SESSION['user_id'])) {
    header('Location: login.php');
    exit();
}

// Fetch user details
$user_id = $_SESSION['user_id'];

// Handle password update
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $current_password = $_POST['current_password'];
    $new_password = $_POST['new_password'];
    $confirm_password = $_POST['confirm_password'];

    // Fetch the current password from the database
    $query = $conn->prepare("SELECT password FROM user WHERE userId = ?");
    $query->bind_param('i', $user_id);
    $query->execute();
    $result = $query->get_result();
    $user = $result->fetch_assoc();

    if ($user && $current_password === $user['password']) {
        if ($new_password === $confirm_password) {
            // Update the password in the database
            $update_query = $conn->prepare("UPDATE user SET password = ? WHERE userId = ?");
            $update_query->bind_param('si', $new_password, $user_id);

            if ($update_query->execute()) {
                $success_message = "Password updated successfully.";
            } else {
                $error_message = "Failed to update password. Please try again.";
            }
        } else {
            $error_message = "New password and confirm password do not match.";
        }
    } else {
        $error_message = "Current password is incorrect.";
    }
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Change Password</title>
    <link rel="icon" type="image/x-icon" href="../ideas.png">
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,100..900;1,100..900&family=Open+Sans:ital,wght@0,300..800;1,300..800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="styling.css">
<style>
    body {
        font-family: Arial, sans-serif;
        margin: 0;
        background-color: #e9f1f4;
    }

    .profile-container {
        display: flex;
        height: 100vh;
    }

    .sidebar {
        width: 250px;
        background-color: #7c589a;
        color: white;
        padding: 20px;
    }

    .sidebar h2 {
        margin-bottom: 20px;
        font-size: 1.5rem;
        text-align: center;
    }

    .sidebar ul {
        list-style: none;
        padding: 0;
    }

    .sidebar ul li {
        margin: 15px 0;
    }

    .sidebar ul li a {
        color: white;
        text-decoration: none;
        font-size: 1.1rem;
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 10px;
        border-radius: 5px;
        transition: background-color 0.3s ease;
    }

    .sidebar ul li a:hover {
        background-color: #533b7c;
    }

    .content {
        flex: 1;
        padding: 40px;
        background-color: #9f8cca;
    }

    .header {
        font-size: 1.8rem;
        color: #333;
        margin-bottom: 30px;
    }

    .form-group {
        margin-bottom: 20px;
    }

    label {
        display: block;
        font-size: 1rem;
        color: #555;
        margin-bottom: 8px;
    }

    input {
        width: calc(100% - 20px);
        padding: 12px;
        font-size: 1rem;
        border: 1px solid #ccc;
        border-radius: 5px;
        background: #f9f9f9;
    }

    input:focus {
        border-color: #7e6df3;
        box-shadow: 0 0 5px rgba(126, 109, 243, 0.3);
    }

    button {
        padding: 10px 20px;
        background-color: #533b7c;
        color: white;
        border: none;
        border-radius: 5px;
        cursor: pointer;
        font-size: 1rem;
        transition: background-color 0.3s ease;
    }

    button:hover {
        background-color: #7c589a;
    }

    .message {
        padding: 10px;
        margin-bottom: 20px;
        border-radius: 5px;
        text-align: center;
    }

    .success {
        background-color: #d4edda;
        color: #155724;
    }

    .error {
        background-color: #f8d7da;
        color: #721c24;
    }
    /* Menu Button for Mobile */
    .menu-toggle {
            display: none;
            font-size: 1.8rem;
            background: none;
            border: none;
            color: #fff;
            cursor: pointer;
            transition: color 0.3s ease;
        }

        .menu-toggle:hover {
            color: var(--primary-light);
        }

    /* Responsive Navigation */
    @media (max-width: 768px) {
        .nav-links {
            display: none; /* Hidden by default */
            flex-direction: column;
            position: absolute;
            top: 60px; /* Below the header */
            left: 0;
            background-color: var(--primary-dark);
            width: 100%;
            padding: 10px 0;
        }

        .nav-links a {
            padding: 10px 20px;
            text-align: center;
        }

        .menu-toggle {
            display: block; /* Show menu button */
        }

        .nav-links.active {
            display: flex; /* Show navigation links when active */
        }

        .logo-container {
            gap: 5px;
        }

        .logo-container .site-name {
            font-size: 1.2rem; /* Smaller font size for mobile */
        }

        .nav-links a {
            font-size: 0.9rem;
            padding: 5px 10px;
        }

        /* Container adjustments */
        .container {
            width: 90%; /* Adjust width for smaller screens */
            margin: 50px auto;
            padding: 15px;
        }

        input[type="text"] {
            width: 100%; /* Full width input box on mobile */
            font-size: 14px;
        }

        h1 {
            font-size: 20px; /* Reduce font size for heading */
        }

        button {
            width: 100%; /* Full-width button on mobile */
            font-size: 14px;
            padding: 8px;
        }

        /* Footer adjustments */
        footer {
            padding: 10px;
            font-size: 0.8rem;
        }

        .footer-links a {
            font-size: 0.8rem;
            margin: 0 5px;
        }
    }

    @media (max-width: 480px) {
        h1 {
            font-size: 18px;
        }

        .nav-links a {
            font-size: 0.8rem;
            padding: 5px;
        }

        button {
            font-size: 12px;
            padding: 6px;
        }

        .footer-links a {
            font-size: 0.7rem;
        }
    }
</style>
</head>
<body>

<header>
    <div class="header-container">
        <div class="logo-container">
            <img src="../rwdd_logo.png" alt="logo" id="logo">
            <span class="site-name">Quiz Craze</span>
        </div>
        <!-- Menu Toggle Button -->
        <button class="menu-toggle" onclick="toggleMenu()">☰</button>
        <!-- Navigation Links -->
        <nav class="nav-links" id="nav-links">
            <a href="../createQuiz/join.php">Join Quiz</a>
            <a href="../createQuiz/create.php">Create Quiz</a>
            <a href="../explore/explore.php">Explore</a>
            <a href="../library/LibraryPublished.php">Library</a>
            <a href="../report/reportMainPage.php">Report</a>
            <a href="../profile/profile.php">Profile</a>
        </nav>
    </div>
</header>
    <div class="profile-container">
        <!-- Sidebar -->
        <div class="sidebar">
            <h2>Settings</h2>
            <ul>
                <li><a href="profile.php">Profile</a></li>
                <li><a href="account_details.php">Account Details</a></li>
                <li><a href="password.php">Password</a></li>
                <li><a href="../profile/logout.php">Logout</a></li>
            </ul>
        </div>

        <!-- Main Content -->
        <div class="content">
            <div class="header">Change Password</div>

            <!-- Success/Error Message -->
            <?php if (!empty($success_message)): ?>
                <div class="message success"><?php echo $success_message; ?></div>
            <?php endif; ?>
            <?php if (!empty($error_message)): ?>
                <div class="message error"><?php echo $error_message; ?></div>
            <?php endif; ?>

            <!-- Password Form -->
            <form method="POST" action="">
                <div class="form-group">
                    <label for="current_password">Current Password</label>
                    <input type="password" id="current_password" name="current_password" required>
                </div>
                <div class="form-group">
                    <label for="new_password">New Password</label>
                    <input type="password" id="new_password" name="new_password" required>
                </div>
                <div class="form-group">
                    <label for="confirm_password">Confirm Password</label>
                    <input type="password" id="confirm_password" name="confirm_password" required>
                </div>
                <button type="submit">Save Changes</button>
            </form>
        </div>
    </div>

<footer>
<div class="footer-container">
    <p>&copy; 2024 Quiz Craze. All rights reserved.</p>
    <div class="footer-links">
        <a href="../terms/terms.php">Terms of Service</a> | 
        <a href="../privacy/privacy.php">Privacy Policy</a> | 
        <a href="../contact/contact.php">Contact Us</a>
    </div>
</div>
</footer>
</body>
</html>

<script>
function toggleMenu() {
    const navLinks = document.getElementById("nav-links");
    navLinks.classList.toggle("active");
}
</script>