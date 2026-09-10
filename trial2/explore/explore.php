<?php
// Assume connection to database is established
include "db.php";

// Retrieve parameters from GET request
$sort = isset($_GET['sort']) ? $_GET['sort'] : 'id'; // Default sort column
$order = isset($_GET['order']) && strtolower($_GET['order']) === 'desc' ? 'DESC' : 'ASC';
$page = isset($_GET['page']) ? (int)$_GET['page'] : 1; // Current page number
$limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 10; // Records per page
$time = isset($_GET['time']) ? $_GET['time'] : 'all'; // Default time filter

// Validate and sanitize inputs
$allowed_sort_columns = ['userId', 'title', 'created_at']; // Adjust based on your table columns
if (!in_array($sort, $allowed_sort_columns)) {
    $sort = 'quizId';
}
$limit = $limit > 0 ? $limit : 10;
$offset = ($page - 1) * $limit;

// Time filter logic
$time_filter = '';
if ($time === 'week') {
    $time_filter = "AND quiz.created_at >= DATE_SUB(NOW(), INTERVAL 1 WEEK)";
} elseif ($time === 'month') {
    $time_filter = "AND quiz.created_at >= DATE_SUB(NOW(), INTERVAL 1 MONTH)";
}

// Build the SQL query with sorting, pagination, and time filtering
$sql = "SELECT quiz.*, user.role AS role, user.username AS author 
        FROM quiz 
        INNER JOIN user ON user.userId = quiz.userId 
        WHERE 1=1 $time_filter 
        ORDER BY $sort $order 
        LIMIT $limit OFFSET $offset";
$result = $conn->query($sql);

// Fetch results
$data = [];
if ($result && $result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $data[] = $row;
    }
}

// Calculate total pages with the same time filter
$total_sql = "SELECT COUNT(*) as total 
              FROM quiz 
              INNER JOIN user ON user.userId = quiz.userId 
              WHERE 1=1 $time_filter";
$total_result = $conn->query($total_sql);
$total_rows = $total_result->fetch_assoc()['total'];
$total_pages = ceil($total_rows / $limit);
?>


<!DOCTYPE html>
<html>
<head>
    <title>Explore</title>
    <link rel="stylesheet" href="styling.css">
    <link rel="icon" type="image/x-icon" href="../ideas.png">
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,100..900;1,100..900&family=Open+Sans:ital,wght@0,300..800;1,300..800&display=swap" rel="stylesheet">
    <style>
        /* Mobile Menu Toggle Styling */
/* Mobile Menu Toggle Styling */
.menu-toggle {
    display: none;
    font-size: 1.8rem;
    background: none;
    border: none;
    color: var(--primary-light);
    cursor: pointer;
    transition: color 0.3s ease;
}

.menu-toggle:hover {
    color: var(--primary-light);
}

/* Navigation Links - Mobile */
@media (max-width: 768px) {
    .nav-links {
        display: none; /* Hidden initially */
        flex-direction: column;
        position: absolute;
        top: 60px;
        left: 0;
        width: 100%;
        background-color: var(--primary-dark);
        z-index: 1000;
        padding: 0;
    }

    .nav-links.active {
        display: flex; /* Show menu when active */
    }

    .nav-links a {
        padding: 15px;
        text-align: center;
        color: #fff;
        border-bottom: 1px solid var(--accent-light);
        transition: background-color 0.3s ease;
    }

    .nav-links a:hover {
        background-color: var(--primary-medium);
    }

    .menu-toggle {
        display: block; /* Show the menu toggle button */
    }
}


        
        .container {
            max-width: 1200px;
            margin: 20px auto;
            padding: 20px;
            background: #ffffff;
            border-radius: 10px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }

        h1 {
            text-align: left;
            font-size: 24px;
            margin-bottom: 20px;
        }

        .filters {
            display: flex;
            justify-content: flex-start;
            gap: 10px;
            margin-bottom: 20px;
        }

        .filters select {
            padding: 10px;
            border-radius: 5px;
            border: 1px solid #ccc;
        }

        .grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
            gap: 20px;
        }

        .card {
            background: #f7f7f7;
            padding: 15px;
            border-radius: 8px;
            text-align: left;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }

        .card h2 {
            font-size: 18px;
            margin-bottom: 10px;
        }

        .card p {
            font-size: 14px;
            margin: 5px 0;
            color: #555;
        }

        .pagination {
            margin-top: 20px;
            text-align: center;
        }

        .pagination a {
            display: inline-block;
            padding: 10px 15px;
            margin: 0 5px;
            text-decoration: none;
            color: #007bff;
            border: 1px solid #ddd;
            border-radius: 5px;
        }

        .pagination a.active {
            background-color: #007bff;
            color: #fff;
            border-color: #007bff;
        }

        .pagination a:hover {
            background-color: #0056b3;
            color: white;
        }

/* Responsive Navigation */
@media (max-width: 768px) {
    .menu-toggle {
        display: block; /* Show toggle button on smaller screens */
    }

    .nav-links {
        display: none; /* Hide nav links by default */
        flex-direction: column;
        position: absolute;
        top: 60px; /* Adjust based on header height */
        left: 0;
        background-color: var(--primary-dark);
        width: 100%;
        padding: 10px 0;
        z-index: 1000;
    }

    .nav-links.active {
        display: flex; /* Show when toggled */
    }

    .nav-links a {
        padding: 10px;
        text-align: center;
        color: white;
        border-bottom: 1px solid var(--neutral-dark);
    }

    .logo-container .site-name {
        font-size: 1.2rem; /* Adjust font size */
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

    <div class="container">
        <h1>Explore</h1>
        <div class="filters">
            <div class="filter-group">
                <label for="time">Filter by Time:</label>
                <select id="time" name="time" onchange="updateURLParam('time', this.value)">
                    <option value="all" <?php echo (!isset($_GET['time']) || $_GET['time'] === 'all') ? 'selected' : ''; ?>>All Time</option>
                    <option value="week" <?php echo isset($_GET['time']) && $_GET['time'] === 'week' ? 'selected' : ''; ?>>Last Week</option>
                    <option value="month" <?php echo isset($_GET['time']) && $_GET['time'] === 'month' ? 'selected' : ''; ?>>Last Month</option>
                </select>
            </div>
            <div class="filter-group">
                <label for="sort">Sort by:</label>
                <select id="sort" name="sort" onchange="updateURLParam('sort', this.value)">
                    <option value="title" <?php echo $sort === 'title' ? 'selected' : ''; ?>>Name</option>
                    <option value="created_at" <?php echo $sort === 'created_at' ? 'selected' : ''; ?>>Date</option>
                </select>
            </div>
            <div class="filter-group">
                <label for="order">Order:</label>
                <select id="order" name="order" onchange="updateURLParam('order', this.value)">
                    <option value="asc" <?php echo $order === 'ASC' ? 'selected' : ''; ?>>ASC</option>
                    <option value="desc" <?php echo $order === 'DESC' ? 'selected' : ''; ?>>DESC</option>
                </select>
            </div>
        </div>

        <div class="grid">
            <?php foreach ($data as $row): ?>
                <div class="card">
                    <a href="quiz_details.php?quizID=<?php echo htmlspecialchars($row['quizId']); ?>" style="text-decoration: none; color: inherit; display: block;">
                        <h2><?php echo htmlspecialchars($row['title']); ?></h2>
                        <h2><?php echo htmlspecialchars($row['description']); ?></h2>
                        <p>Published: <?php echo htmlspecialchars($row['created_at']); ?></p>
                        <p>Author: <?php echo htmlspecialchars($row['author']); ?></p>
                        <p style="color: RGB(145, 83, 207)"><?php echo htmlspecialchars($row['role']); ?></p>
                    </a>
                </div>
            <?php endforeach; ?>
        </div>  

        <div class="pagination">
            <a href="?page=1&sort=<?php echo $sort; ?>&order=<?php echo $order; ?>&limit=<?php echo $limit; ?>">First</a>
            <a href="?page=<?php echo max(1, $page - 1); ?>&sort=<?php echo $sort; ?>&order=<?php echo $order; ?>&limit=<?php echo $limit; ?>">Previous</a>
            <a href="?page=<?php echo min($total_pages, $page + 1); ?>&sort=<?php echo $sort; ?>&order=<?php echo $order; ?>&limit=<?php echo $limit; ?>">Next</a>
            <a href="?page=<?php echo $total_pages; ?>&sort=<?php echo $sort; ?>&order=<?php echo $order; ?>&limit=<?php echo $limit; ?>">Last</a>
        </div>
    </div>


<script>
    function updateURLParam(key, value) {
        const url = new URL(window.location.href);
        url.searchParams.set(key, value);
        url.searchParams.set('page', 1); // Reset page to 1 when filter changes
        window.location.href = url.toString();
    }
    function toggleMenu() {
        const navLinks = document.getElementById("nav-links");
        navLinks.classList.toggle("active");
    }
</script>

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

