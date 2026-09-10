<?php
// Include the database connection file
include 'db.php';
session_start();

// Check if the user is logged in
if (!isset($_SESSION['user_id'])) {
    header('Location: ../profile/login.php');
    exit();
}

// Retrieve the quiz ID from the URL (ensure correct parameter name)
$quiz_id = isset($_GET['quizID']) ? intval($_GET['quizID']) : 0;

// Validate quiz ID
if ($quiz_id <= 0) {
    die("<h2>Invalid quiz ID. Please provide a valid quiz ID in the URL.</h2>");
}

// Define pagination settings
$limit = 10; // Entries per page
$page = isset($_GET['page']) && $_GET['page'] > 0 ? intval($_GET['page']) : 1;
$offset = ($page - 1) * $limit;

// Query to get the total number of leaderboard entries
$total_sql = "SELECT COUNT(*) AS total FROM attempt WHERE quizId = ?";
$total_stmt = $conn->prepare($total_sql);
$total_stmt->bind_param("i", $quiz_id);
$total_stmt->execute();
$total_result = $total_stmt->get_result();
$total_entries = $total_result->fetch_assoc()['total'];
$total_pages = ceil($total_entries / $limit);
$total_stmt->close();

// Query to fetch leaderboard data
$leaderboard_sql = "
    SELECT u.username, a.score
    FROM attempt a
    JOIN user u ON a.userId = u.userId
    WHERE a.quizId = ?
    ORDER BY a.score DESC
    LIMIT ? OFFSET ?";
$leaderboard_stmt = $conn->prepare($leaderboard_sql);
$leaderboard_stmt->bind_param("iii", $quiz_id, $limit, $offset);
$leaderboard_stmt->execute();
$leaderboard_result = $leaderboard_stmt->get_result();
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Leaderboard</title>
    <link rel="icon" type="image/x-icon" href="../ideas.png">
    <link rel="stylesheet" href="styling.css">
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">
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

<main>
    <div class="leaderboard-container">
        <h1>Leaderboard</h1>
        <table>
            <thead>
                <tr>
                    <th>Rank</th>
                    <th>Username</th>
                    <th>Score</th>
                </tr>
            </thead>
            <tbody>
                <?php 
                $rank = ($page - 1) * $limit + 1; // Adjust rank based on page
                while ($row = $leaderboard_result->fetch_assoc()):
                ?>
                    <tr>
                        <td><?= $rank++; ?></td>
                        <td><?= htmlspecialchars($row['username']); ?></td>
                        <td><?= htmlspecialchars($row['score']); ?></td>
                    </tr>
                <?php endwhile; ?>
            </tbody>
        </table>
        <!-- Pagination -->
        <div class="pagination">
            <?php for ($i = 1; $i <= $total_pages; $i++): ?>
                <?php if ($i == $page): ?>
                    <span><?= $i ?></span>
                <?php else: ?>
                    <a href="?quizID=<?= $quiz_id ?>&page=<?= $i ?>"><?= $i ?></a>
                <?php endif; ?>
            <?php endfor; ?>
        </div>
    </div>
</main>

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

<style>
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

<script>
function toggleMenu() {
    const navLinks = document.getElementById("nav-links");
    navLinks.classList.toggle("active");
}
</script>

<?php
// Close the statement and database connection
$leaderboard_stmt->close();
$conn->close();
?>
