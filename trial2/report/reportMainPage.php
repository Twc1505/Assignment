<?php
// Database connection
include 'db.php';
session_start();

// Check if the user is logged in
if (!isset($_SESSION['user_id'])) {
    header('Location: ../profile/login.php');
    exit();
}

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Handle Rename
if (isset($_POST['quizID']) && isset($_POST['newName'])) {
    // Get quizID and newName from POST data
    $quizID = (int)$_POST['quizID'];  // Casting to integer to prevent injection
    $newName = trim($_POST['newName']);  // Trim whitespace around the new name

    // Check if the new name is not empty
    if (!empty($newName)) {
        // Prepare and execute the update query
        $stmt = $conn->prepare("UPDATE quiz SET title = ? WHERE quizID = ?");
        $stmt->bind_param("si", $newName, $quizID);

        if ($stmt->execute()) {
            echo "<script>console.log(\"eheh\")</script>";
        } else {
            echo "<script>console.log(\"eheh\")</script>";
        }

        $stmt->close();
    } 
} 
// Handle Delete
if (isset($_GET['delete'])) {
    // Get the quizID from the URL, casting it to an integer to prevent SQL injection
    $quizID = (int)$_GET['delete'];

    // Start a transaction to ensure data integrity
    $conn->begin_transaction();

    try {
        // First, delete related questions in the 'question' table
        $deleteQuestionsQuery = "DELETE FROM question WHERE quizId = ?";
        if ($stmt = $conn->prepare($deleteQuestionsQuery)) {
            $stmt->bind_param("i", $quizID);
            $stmt->execute();
            $stmt->close();
        } else {
            throw new Exception("Error preparing the delete questions query.");
        }

        // Second, delete related attempts in the 'attempt' table
        $deleteAttemptsQuery = "DELETE FROM attempt WHERE quizId = ?";
        if ($stmt = $conn->prepare($deleteAttemptsQuery)) {
            $stmt->bind_param("i", $quizID);
            $stmt->execute();
            $stmt->close();
        } else {
            throw new Exception("Error preparing the delete attempts query.");
        }

        // Finally, delete the quiz from the 'quiz' table
        $deleteQuizQuery = "DELETE FROM quiz WHERE quizID = ?";
        if ($stmt = $conn->prepare($deleteQuizQuery)) {
            $stmt->bind_param("i", $quizID);
            if ($stmt->execute()) {
                // Commit the transaction if all deletions are successful
                $conn->commit();

                // Redirect back to the current page (or the specified page)
                header("Location: ?page=" . $_GET['page']);
                exit();
            } else {
                throw new Exception("Error deleting the quiz: " . $stmt->error);
            }
        } else {
            throw new Exception("Error preparing the delete quiz query.");
        }
    } catch (Exception $e) {
        // Rollback the transaction if any error occurs
        $conn->rollback();
        echo "Error: " . $e->getMessage();
    }
}


// Pagination setup
$limit = 10;  // Number of quizzes per page
$page = isset($_GET['page']) ? max(1, (int)$_GET['page']) : 1;  // Default to page 1
$offset = ($page - 1) * $limit;  // Calculate offset for SQL query

// Fetch quiz data with pagination
$sql = "
SELECT  
    q.quizID, 
    q.title AS name, 
    COUNT(DISTINCT a.userID) AS participants, 
    ROUND(AVG(a.score), 2) AS accuracy
FROM quiz q
LEFT JOIN attempt a ON q.quizID = a.quizID
GROUP BY q.quizID
LIMIT ? OFFSET ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ii", $limit, $offset);
$stmt->execute();
$result = $stmt->get_result();

$quizzes = [];
while ($row = $result->fetch_assoc()) {
    $quizzes[] = $row;
}
$stmt->close();

// Get total number of quizzes for pagination
$totalQuizzesQuery = "SELECT COUNT(*) AS total FROM quiz";
$totalResult = $conn->query($totalQuizzesQuery);
$totalRow = $totalResult->fetch_assoc();
$totalQuizzes = $totalRow['total'];

// Calculate total pages
$totalPages = ceil($totalQuizzes / $limit);

// Fetch attempts data for additional processing
$sql1 = "SELECT attempt.quizId, attempt.attemptId, attempt.attemptTime, attempt.score, attempt.comment, attempt.correctQuestions, quiz.title, quiz.description
FROM quiz
INNER JOIN attempt ON quiz.quizId = attempt.quizId;";
$sql2 = "SELECT * FROM quiz";
$result1 = $conn->query($sql1);
$result2 = $conn->query($sql2);

$attempts = [];
$quizs = [];
if ($result1->num_rows > 0) {
    while ($row = $result1->fetch_assoc()) {
        $attempts[] = $row;
    }
}

if ($result2->num_rows > 0) {
    while ($row = $result2->fetch_assoc()) {
        $quizs[] = $row;
    }
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="styling.css">
    <link rel="icon" type="image/x-icon" href="../ideas.png">
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,100..900;1,100..900&family=Open+Sans:ital,wght@0,300..800;1,300..800&display=swap" rel="stylesheet">
    <title>Quiz Craze</title>

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
    <div class="main-container">
    <table>
    <tr>
        <th>Quiz Name</th>
        <th>Total Attempts</th>
        <th>Average Accuracy (%)</th>
        <th>Actions</th>
    </tr>

    <?php 
    // Initialize an array to group attempts by quiz
    $quizzes = [];

    // Group attempts by quiz title
    foreach ($attempts as $attempt) {
        $quizTitle = $attempt['quizId'];
        $quizzes[$quizTitle][] = $attempt;
    }

    // Iterate through each quiz
    foreach ($quizzes as $quizTitle => $quizAttempts): 
        // Variables to calculate total participants and average accuracy
        $totalParticipants = count($quizAttempts);
        $totalAccuracy = 0;

        // Calculate total accuracy for the current quiz
        foreach ($quizAttempts as $attempt):
            // Ensure correctQuestions exists and is valid JSON
            $data = json_decode($attempt['correctQuestions'], true);
            if (json_last_error() !== JSON_ERROR_NONE || !is_array($data)) {
                continue; // Skip invalid attempts
            }

            $totalCorrect = 0;
            $totalQuestions = count($data);

            // Count correct answers for this attempt
            foreach ($data as $x => $y) {
                if ($y === true) {
                    $totalCorrect++;
                }
            }

            // Add to total accuracy (percentage of correct answers)
            $totalAccuracy += ($totalCorrect / $totalQuestions);
        endforeach;

        // Calculate average accuracy for the current quiz
        $averageAccuracy = ($totalParticipants > 0) ? ($totalAccuracy / $totalParticipants) * 100 : 0;

        // Get quizId from the first attempt (or any attempt in the group)
        $firstAttempt = $quizAttempts[0];
        $quizId = $firstAttempt['quizId'];
    ?>
        <!-- Row for quiz with click event to redirect to the quiz details page -->
        <tr data-id="<?php echo $quizId; ?>" onclick="window.location.href='Review.php?quizID=<?php echo $quizId; ?>'">
            <td><?php echo htmlspecialchars($attempt['title']); ?></td>
            <td><?php echo $totalParticipants; ?></td> <!-- Display total participants -->
            <td><?php echo number_format($averageAccuracy, 2) . '%'; ?></td> <!-- Display average accuracy -->
            <td>
                <!-- Rename and Delete buttons with dynamic quizId -->
                <button class="action-btn renameBtn" onclick="event.stopPropagation(); showRenameModal(<?php echo $quizId; ?>, '<?php echo addslashes(htmlspecialchars($attempt['title'])); ?>')">Rename</button>
                <button class="action-btn deleteBtn" onclick="event.stopPropagation(); confirmDelete(<?php echo $quizId; ?>, <?php echo $page; ?>)">Delete</button>

                
                <form id="renameForm" method="POST" style="display: none;">
                    <input type="hidden" name="quizID" id="quizID" />
                    <input type="text" name="newName" id="newName" />
                    <input type="submit" value="Submit" />
                </form>
            
            </td>
        </tr>
    <?php endforeach; ?>

    <?php if (empty($quizzes)): ?>
        <tr>
            <td colspan="4">No attempts found</td>
        </tr>
    <?php endif; ?>

</table>
        <!-- Pagination Controls -->
        <div class="pagination">
            <?php 
            if ($page > 1) {
                echo "<a href='?page=" . ($page - 1) . "'>Previous</a>";
            }

            for ($i = 1; $i <= $totalPages; $i++) {
                echo "<a href='?page=$i'" . ($i === $page ? " class='active'" : "") . ">$i</a>";
            }

            if ($page < $totalPages) {
                echo "<a href='?page=" . ($page + 1) . "'>Next</a>";
            }
            ?>
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

let selectedRow = null;

function setSelectedRow(row) {
    selectedRow = row;
}



function showRenameModal(quizID, currentName) {
    // Prompt user for the new name
    const newName = prompt("Enter new quiz name:", currentName);

    if (newName && newName !== currentName) {
        // Set the hidden input fields in the form with the new values
        document.getElementById('quizID').value = quizID;
        document.getElementById('newName').value = newName;

        // Submit the form to the server (this will send a POST request to the same PHP file)
        document.getElementById('renameForm').submit();

        // Optionally, show a message to indicate the process (form submission is handled by PHP)
        alert("Quiz renaming request has been sent!");
    }
}

function viewQuiz() {
    if (selectedRow) {
        const quizID = selectedRow.getAttribute('data-id');
        window.location.href = 'Review.php?quizID=' + quizID; // Redirect to the quiz details page
    } else {
        alert("Please select a quiz to view.");
    }
}

function confirmDelete(quizID, page) {
    if (confirm("Are you sure you want to delete this quiz?")) {
        // Redirect to the PHP page to handle the deletion
        window.location.href = '?delete=' + quizID + '&page=' + page;
    }
}

function navigateToQuiz(quizId) {
    window.location.href = 'quiz_details.php?quizID=' + quizId;
}

function toggleMenu() {
    const navLinks = document.getElementById("nav-links");
    navLinks.classList.toggle("active");
}
</script>

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