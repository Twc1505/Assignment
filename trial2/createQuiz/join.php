<?php
// Database connection
include "db.php";

session_start();
if (!isset($_SESSION['user_id'])) {
    $_SESSION['redirect_to'] = $_SERVER['REQUEST_URI'] ?? '/index.php';
    header("Location: ../profile/login.php");
    exit();
}
// If the user is not logged in
// Check if the user is logged in
if (!isset($_SESSION['user_id'])) {
    // Start session if not already started
    if (session_status() == PHP_SESSION_NONE) {
        session_start();
    }
    
    // Capture the URL the user was trying to access
    $redirect_to = isset($_SERVER['REQUEST_URI']) ? $_SERVER['REQUEST_URI'] : '/index.php';
    
    // Redirect to login page with the intended location
    header(" ../profile/login.php" );
    exit();
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $quizId = isset($_POST['quizId']) ? (int)$_POST['quizId'] : 0;

    // Check if the quiz ID exists
    $sql = "SELECT * FROM quiz WHERE quizId = ?";
    $stmt = $conn->prepare($sql);
    if (!$stmt) {
        die("Database query error: " . $conn->error);
    }

    $stmt->bind_param("i", $quizId);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        // Redirect to the quiz page
        header("Location: quiz.php?quizId=$quizId");
        exit;
    } else {
        $errorMessage = "Invalid Quiz ID. Please try again.";
    }
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,100..900;1,100..900&family=Open+Sans:ital,wght@0,300..800;1,300..800&display=swap" rel="stylesheet">
    <title>Join Quiz</title>
    <link rel="icon" type="image/x-icon" href="../ideas.png">

<style>
    /* General Reset */
    * {
        margin: 0;
        padding: 0;
        box-sizing: border-box;
        font-family: "Montserrat", sans-serif;
    }

    /* Color Palette */
    :root {
        --primary-light: #AB3CFC;  /* Bright Purple */
        --primary-medium: #7E6DF3; /* Soft Blue */
        --primary-dark: #533B7C;   /* Dark Purple */
        --accent-light: #7C589A;   /* Light Violet */
        --accent-medium: #9F8CCA;  /* Lavender */
        --neutral-dark: #3E394C;   /* Dark Gray */
    }

    /* Body and General */
    body {
        background-color: var(--accent-medium);
        color: var(--neutral-dark);
        line-height: 1.6;
    }
    
    /* Header Styling */
    header {
        background-color: var(--primary-dark);
        color: #fff;
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
        position: sticky;
        top: 0;
        z-index: 1000;
    }

    .header-container {
        max-width: 1200px;
        margin: 0 auto;
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 10px 20px;
    }

    .logo-container {
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .logo-container #logo {
        width: 50px;
        height: 50px;
    }

    .logo-container .site-name {
        font-size: 1.5rem;
        font-weight: bold;
        color: var(--primary-light);
    }

    .nav-links {
        display: flex;
        gap: 20px;
    }

    .nav-links a {
        color: var(--accent-medium);
        text-decoration: none;
        font-size: 1rem;
        font-weight: 600;
        padding: 8px 15px;
        transition: color 0.3s ease, background-color 0.3s ease;
        border-radius: 5px;
    }

    .nav-links a:hover {
        background-color: var(--primary-light);
        color: #fff;
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

    .container {
        max-width: 600px;
        margin: 100px auto;
        background-color: #fff;
        border-radius: 8px;
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        padding: 20px;
        text-align: center;
    }

    h1 {
        font-size: 24px;
        margin-bottom: 20px;
    }

    input[type="text"] {
        width: 80%;
        padding: 10px;
        margin-bottom: 20px;
        border: 1px solid #ccc;
        border-radius: 5px;
        font-size: 16px;
    }

    button {
        padding: 10px 20px;
        background-color: #f9a825;
        color: #fff;
        border: none;
        border-radius: 5px;
        cursor: pointer;
        font-size: 16px;
    }

    button:hover {
        background-color: #e68a00;
    }

    .header {
        background-color: #b0bec5;
        padding: 10px;
        color: white;
        text-align: center;
    }

    .error {
        color: red;
        font-size: 14px;
        margin-top: 10px;
    }

    /* Footer Styling */
    footer {
        background-color: var(--neutral-dark);
        color: #fff;
        padding: 20px;
        text-align: center;
        position: fixed;
        bottom: 0;
        width: 100%;
        box-shadow: 0 -2px 6px rgba(0, 0, 0, 0.1);
    }

    .footer-container {
        display: flex;
        flex-direction: column;
        align-items: center;
    }

    .footer-links a {
        color: var(--accent-medium);
        text-decoration: none;
        margin: 0 10px;
        font-size: 0.9rem;
        transition: color 0.3s ease;
    }

    .footer-links a:hover {
        color: var(--primary-light);
        text-decoration: underline;
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
<body>
    <div class="container">
        <h1>Join Quiz</h1>
        <form method="POST" action="">
            <input type="text" name="quizId" placeholder="Enter a join code" required>
            <br>
            <button type="submit">Join</button>
        </form>
        <?php if (isset($errorMessage)): ?>
            <div class="error"> <?php echo $errorMessage; ?> </div>
        <?php endif; ?>
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
