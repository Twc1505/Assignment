<?php
// Include the database connection file
include 'db.php';
session_start();

// Check if the user is logged in
if (!isset($_SESSION['user_id'])) {
    header('Location: ../profile/login.php');
    exit();
}

// Use session user_id
$user_id = $_SESSION['user_id'];

// Fetch user details
$user_id = $_SESSION['user_id'];
$user_query = $conn->prepare("SELECT username, role FROM user WHERE userId = ?");
$user_query->bind_param('i', $user_id);
$user_query->execute();
$user_result = $user_query->get_result();
$user = $user_result->fetch_assoc();

// Pagination settings
$limit = 5; // Number of quizzes per page
$page = isset($_GET['page']) && $_GET['page'] > 0 ? (int)$_GET['page'] : 1;
$offset = ($page - 1) * $limit;

// New feature: Filter, Sort, and Order Parameters
$sort = isset($_GET['sort']) ? $_GET['sort'] : 'title'; // Default to title
$order = isset($_GET['order']) && strtolower($_GET['order']) === 'desc' ? 'DESC' : 'ASC';
$time = isset($_GET['time']) ? $_GET['time'] : 'all';

// Sanitize and validate inputs
$allowed_sort_columns = ['title', 'created_at'];
if (!in_array($sort, $allowed_sort_columns)) {
    $sort = 'title';
}

// Time filter logic
$time_filter = '';
if ($time === 'week') {
    $time_filter = "AND quiz.created_at >= DATE_SUB(NOW(), INTERVAL 1 WEEK)";
} elseif ($time === 'month') {
    $time_filter = "AND quiz.created_at >= DATE_SUB(NOW(), INTERVAL 1 MONTH)";
}

// Query to get quiz details (without questions)
$quiz_sql = "
    SELECT quiz.quizId, quiz.title, quiz.description, user.username
    FROM quiz 
    LEFT JOIN user ON user.userId = quiz.userId
    WHERE quiz.userId = ? $time_filter
    ORDER BY $sort $order
    LIMIT ? OFFSET ?";
$quiz_stmt = $conn->prepare($quiz_sql);
$quiz_stmt->bind_param("iii", $user_id, $limit, $offset);
$quiz_stmt->execute();
$quiz_result = $quiz_stmt->get_result();

// Fetch quizzes
$quiz_data = [];
while ($row = $quiz_result->fetch_assoc()) {
    $quiz_data[] = $row;
}
$quiz_stmt->close();

// Get total quiz count for pagination
$count_sql = "SELECT COUNT(*) AS total FROM quiz WHERE userId = ? $time_filter";
$count_stmt = $conn->prepare($count_sql);
$count_stmt->bind_param("i", $user_id);
$count_stmt->execute();
$count_result = $count_stmt->get_result();
$total_quizzes = $count_result->fetch_assoc()['total'];
$count_stmt->close();

// Calculate total pages and handle edge cases
$total_pages = max(ceil($total_quizzes / $limit), 1);
$page = min($page, $total_pages);

// Handle DELETE Request
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Decode JSON input
    $input = json_decode(file_get_contents('php://input'), true);

    if (isset($input['quiz_id'])) {
        $quiz_id = intval($input['quiz_id']);
        $user_id = $_SESSION['user_id'];

        // Start transaction to ensure atomic deletion
        $conn->begin_transaction();

        try {
            // Step 1: Delete from 'attempt' table
            $delete_attempts_sql = "DELETE FROM attempt WHERE quizId = ?";
            $attempt_stmt = $conn->prepare($delete_attempts_sql);
            $attempt_stmt->bind_param("i", $quiz_id);
            $attempt_stmt->execute();
            $attempt_stmt->close();

            // Step 2: Delete from 'question' table
            $delete_questions_sql = "DELETE FROM question WHERE quizId = ?";
            $question_stmt = $conn->prepare($delete_questions_sql);
            $question_stmt->bind_param("i", $quiz_id);
            $question_stmt->execute();
            $question_stmt->close();

            // Step 3: Delete the quiz itself
            $delete_quiz_sql = "DELETE FROM quiz WHERE quizId = ? AND userId = ?";
            $quiz_stmt = $conn->prepare($delete_quiz_sql);
            $quiz_stmt->bind_param("ii", $quiz_id, $user_id);
            $quiz_stmt->execute();

            if ($quiz_stmt->affected_rows > 0) {
                // Commit transaction if everything succeeds
                $conn->commit();
                echo json_encode(['status' => 'success']);
            } else {
                throw new Exception("Failed to delete quiz or unauthorized access.");
            }

            $quiz_stmt->close();
        } catch (Exception $e) {
            // Rollback transaction on failure
            $conn->rollback();
            echo json_encode(['status' => 'error', 'message' => $e->getMessage()]);
        }
    } else {
        echo json_encode(['status' => 'error', 'message' => 'Invalid request. Quiz ID missing.']);
    }

    exit();
}

// Close the database connection
$conn->close();
?>



<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Published Contents</title>
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
    <div class="layout">
        <!-- Sidebar -->
        <aside class="sidebar">
            <h2><?php echo htmlspecialchars($user['username']); ?></h2>
            <ul class="menu">
                <li class="menu-item active"><a href="LibraryPublished.php">Published Contents</a></li>
                <li class="menu-item"><a href="PreviouslyUsed.php">Previously Used</a></li>
            </ul>
        </aside>

        <!-- Content Section -->
        <section class="content">
            <h2>Published Contents</h2>
            <!-- Filters -->
            <div class="filters">
                <div class="filter-group">
                    <label for="time">Filter by Time:</label>
                    <select id="time" onchange="updateURLParam('time', this.value)">
                        <option value="all">All Time</option>
                        <option value="week">Last Week</option>
                        <option value="month">Last Month</option>
                    </select>
                </div>
                <div class="filter-group">
                    <label for="sort">Sort By:</label>
                    <select id="sort" onchange="updateURLParam('sort', this.value)">
                        <option value="title">Name</option>
                        <option value="created_at">Date</option>
                    </select>
                </div>
                <div class="filter-group">
                    <label for="order">Order:</label>
                    <select id="order" onchange="updateURLParam('order', this.value)">
                        <option value="asc">ASC</option>
                        <option value="desc">DESC</option>
                    </select>
                </div>
            </div>

            <!-- Quiz Cards -->
            <div class="quiz-cards">
                <?php if (empty($quiz_data)) { ?>
                    <p>No quizzes are available at the moment.</p>
                <?php } else { ?>
                    <?php foreach ($quiz_data as $quiz) { ?>
                        <div class="quiz-card">
                            <a href="../report/Review.php?quizID=<?= htmlspecialchars($quiz['quizId']) ?>">
                                <div class="quiz-details">
                                    <h3><?= htmlspecialchars($quiz['title']) ?></h3>
                                    <p><?= htmlspecialchars($quiz['description']) ?></p>
                                    <p>Created by: <strong><?= htmlspecialchars($quiz['username']) ?></strong></p>
                                </div>
                            </a>
                            <button class="action-btn delete" data-quiz-id="<?= $quiz['quizId']; ?>">Delete</button>
                        </div>
                    <?php } ?>
                <?php } ?>
            </div>

            <!-- Pagination -->
            <div class="pagination">
                <?php for ($i = 1; $i <= $total_pages; $i++): ?>
                    <a href="?page=<?= $i ?>" class="<?= ($i == $page) ? 'active' : '' ?>"><?= $i ?></a>
                <?php endfor; ?>
            </div>
        </section>
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
    <script>
        function updateURLParam(param, value) {
            const url = new URL(window.location.href);
            url.searchParams.set(param, value);
            window.location.href = url.toString();
        }
    </script>
</body>
</html>



</body>
</html>

<script>
document.addEventListener('DOMContentLoaded', () => {
    // Dropdown menu logic
    document.querySelectorAll('.menu-button').forEach(button => {
        button.addEventListener('click', (e) => {
            e.stopPropagation();
            const dropdown = button.nextElementSibling;
            document.querySelectorAll('.menu-dropdown').forEach(menu => menu.style.display = 'none');
            dropdown.style.display = 'block';
        });
    });

    // Close dropdowns when clicking outside
    document.addEventListener('click', () => {
        document.querySelectorAll('.menu-dropdown').forEach(menu => menu.style.display = 'none');
    });

    // Delete quiz logic
    document.querySelectorAll('.delete').forEach(button => {
        button.addEventListener('click', () => {
            const quizId = button.getAttribute('data-quiz-id'); // Get the quiz ID
            if (confirm('Are you sure you want to delete this quiz?')) {
                fetch(location.href, {
                    method: 'POST', // Send as POST request
                    headers: {
                        'Content-Type': 'application/json', // JSON input
                    },
                    body: JSON.stringify({ quiz_id: quizId }) // Send quiz ID in the request body
                })
                .then(response => response.json()) // Parse the JSON response
                .then(data => {
                    if (data.status === 'success') {
                        alert('Quiz deleted successfully.');
                        button.closest('.quiz-card').remove(); // Remove the card from DOM
                    } else {
                        alert(`Failed to delete the quiz: ${data.message || 'Unknown error'}`);
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('An error occurred while deleting the quiz.');
                });
            }
        });
    });

});

function updateURLParam(param, value) {
    const url = new URL(window.location.href);
    url.searchParams.set(param, value);
    window.location.href = url.toString(); // Reload with new URL
}

function toggleMenu() {
    const navLinks = document.getElementById("nav-links");
    navLinks.classList.toggle("active");
}
</script>

<style>
    /* General Reset */
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

/* Color Palette */
:root {
    --primary-dark: #533B7C;
    --primary-medium: #7E6DF3;
    --accent-medium: #9F8CCA;
    --neutral-dark: #3E394C;
    --light-bg: #f9f9f9;
}

/* Body and General */
body {
    font-family: 'Montserrat', sans-serif;
    background-color: var(--light-bg);
    color: var(--neutral-dark);
    line-height: 1.6;
}

a {
    color: inherit;
    text-decoration: none;
}

header {
    background-color: var(--primary-dark);
    color: #fff;
    position: sticky;
    top: 0;
    z-index: 1000;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
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

.logo-container img {
    width: 50px;
    height: 50px;
}

.site-name {
    font-size: 1.5rem;
    font-weight: bold;
}

.nav-links {
    display: flex;
    gap: 20px;
}

.nav-links a {
    color: var(--accent-medium);
    padding: 8px 12px;
    border-radius: 5px;
    transition: background-color 0.3s ease;
}

.nav-links a:hover {
    background-color: var(--primary-medium);
    color: #fff;
}

/* Toggle Menu Button */
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
    color: var(--primary-medium);
}

/* Layout */
.layout {
    display: flex;
    flex-wrap: wrap;
    min-height: 100vh;
}

.sidebar {
    width: 250px;
    background-color: var(--primary-dark);
    color: white;
    padding: 20px;
    flex-shrink: 0;
}

.sidebar h2 {
    text-align: center;
    margin-bottom: 20px;
}

.menu {
    list-style: none;
}

.menu-item {
    margin: 10px 0;
}

.menu-item a {
    display: block;
    padding: 10px;
    color: white;
    border-radius: 5px;
    transition: background-color 0.3s ease;
}

.menu-item a:hover {
    background-color: var(--primary-medium);
}

.content {
    flex: 1;
    padding: 20px;
}

.quiz-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 20px;
}

.quiz-card {
    background: white;
    padding: 15px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    position: relative;
    transition: box-shadow 0.3s ease;
}

.quiz-card:hover {
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.quiz-details h3 {
    font-size: 1.1rem;
    margin-bottom: 10px;
}

.quiz-details p {
    color: #666;
    font-size: 0.9rem;
}

.delete {
    position: absolute;
    top: 10px;
    right: 10px;
    background: none;
    color: red;
    border: none;
    font-size: 1rem;
    cursor: pointer;
}

.pagination {
    margin-top: 20px;
    text-align: center;
}

.pagination a {
    padding: 8px 12px;
    margin: 0 5px;
    border: 1px solid var(--primary-medium);
    color: var(--primary-medium);
    border-radius: 5px;
    transition: background-color 0.3s ease;
}

.pagination a:hover, .pagination a.active {
    background-color: var(--primary-medium);
    color: white;
}

/* Responsive Styles */
@media (max-width: 768px) {
    .menu-toggle {
        display: block; /* Show toggle button */
    }

    .nav-links {
        display: none;
        flex-direction: column;
        position: absolute;
        top: 60px;
        left: 0;
        background-color: var(--primary-dark);
        width: 100%;
        padding: 10px 0;
    }

    .nav-links.active {
        display: flex;
    }

    .sidebar {
        width: 100%;
        text-align: center;
    }

    .quiz-cards {
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    }
}

@media (max-width: 480px) {
    .logo-container img {
        width: 40px;
        height: 40px;
    }

    .site-name {
        font-size: 1.2rem;
    }

    .nav-links a {
        font-size: 0.9rem;
    }

    .quiz-card {
        padding: 10px;
    }

    .pagination a {
        padding: 6px 10px;
        font-size: 0.8rem;
    }
}


</style>

