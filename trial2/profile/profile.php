<?php
include 'db.php';
session_start();

if (!isset($_SESSION['user_id'])) {
    header('Location: login.php');
    exit();
}

// Fetch user details
$user_id = $_SESSION['user_id'];
$user_query = $conn->prepare("SELECT username, role FROM user WHERE userId = ?");
$user_query->bind_param('i', $user_id);
$user_query->execute();
$user_result = $user_query->get_result();
$user = $user_result->fetch_assoc();

// Fetch quizzes created by the user
$quizzes_query = $conn->prepare("SELECT COUNT(*) AS quizzes_created FROM quiz WHERE userId = ?");
$quizzes_query->bind_param('i', $user_id);
$quizzes_query->execute();
$quizzes_result = $quizzes_query->get_result();
$quizzes_data = $quizzes_result->fetch_assoc();
$quizzes_created = $quizzes_data['quizzes_created'] ?? 0;

// Fetch total completed quizzes and total score
$attempt_query = $conn->prepare("SELECT COUNT(*) AS completedQuizzes, SUM(score) AS totalScore FROM attempt WHERE userId = ?");
$attempt_query->bind_param('i', $user_id);
$attempt_query->execute();
$attempt_result = $attempt_query->get_result();
$attempt_data = $attempt_result->fetch_assoc();
$completed_quizzes = $attempt_data['completedQuizzes'] ?? 0;
$total_score = $attempt_data['totalScore'] ?? 0;
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Profile Page</title>
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
        display: flex;
        flex-direction: column;
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
        padding: 20px;
        background-color: #9f8cca;
        overflow-y: auto;
    }

    .header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
    }

    .header h1 {
        margin: 0;
        font-size: 2rem;
    }

    .profile-card {
        background: white;
        padding: 20px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 20px;
        box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
    }

    .profile-info {
        display: flex;
        align-items: center;
        gap: 20px;
    }

    .profile-pic {
        width: 80px;
        height: 80px;
        background-color: #533b7c;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 2rem;
    }

    .statistics {
        display: flex;
        justify-content: space-between;
        background: white;
        padding: 20px;
        border-radius: 8px;
        margin-bottom: 20px;
        box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
    }

    .stat-item {
        text-align: center;
        flex: 1;
    }

    .stat-item:not(:last-child) {
        border-right: 1px solid #ccc;
    }

    .stat-item span {
        display: block;
        font-size: 0.9rem;
        margin-top: 5px;
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
            <h2><?php echo htmlspecialchars($user['username']); ?></h2>
            <ul>
                <li><a href="profile.php">Profile</a></li>
                <li><a href="account_details.php">Account Details</a></li>
                <li><a href="password.php">Password</a></li>
                <li><a href="../admin/logout.php">Logout</a></li>
            </ul>
        </div>

        <!-- Main Content -->
        <div class="content">
            <div class="header">
                <h1>Profile</h1>
            </div>

            <!-- Profile Card -->
            <div class="profile-card">
                <div class="profile-info">
                    <div class="profile-pic"><?php echo strtoupper($user['username'][0]); ?></div>
                    <div>
                        <strong><?php echo htmlspecialchars($user['username']); ?></strong> | <?php echo htmlspecialchars($user['role']); ?><br>
                        @<?php echo htmlspecialchars($user['username']); ?>
                    </div>
                </div>
                <div>
                    <div>Quizzes Created: <strong><?php echo $quizzes_created; ?></strong></div>
                </div>
            </div>

            <!-- Statistics -->
            <div class="statistics">
                <div class="stat-item">
                    <strong><?php echo $total_score; ?></strong>
                    <span>Total Score</span>
                </div>
                <div class="stat-item">
                    <strong><?php echo $completed_quizzes; ?></strong>
                    <span>Total Completed Quizzes</span>
                </div>
            </div>
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