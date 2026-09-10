<?php
// Database connection
include 'db.php';
session_start();

// Check if the user is logged in
if (!isset($_SESSION['user_id'])) {
    header('Location: login.php');
    exit();
}

// Ensure the quiz ID is provided
if (!isset($_GET['quizID'])) {
    echo "Quiz ID not provided.";
    exit();
}

// Sanitize and fetch the quiz ID
$quizID = intval($_GET['quizID']);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Fetch quiz title
$query = "SELECT * FROM quiz WHERE quizID = ?";
$stmt = $conn->prepare($query);
$stmt->bind_param("i", $quizID);
$stmt->execute();
$result = $stmt->get_result();
$quiz = $result->fetch_assoc();
$stmt->close();

// Ensure the quiz exists and has a title
if (!$quiz || !isset($quiz['title'])) {
    echo "Quiz not found or title is missing.";
    exit();
}


if (!$quiz) {
    echo "Quiz not found.";
    exit();
}

// Fetch summary data (TOTAL PARTICIPANTS & QUESTION)
$summaryQuery = "
SELECT 
    ROUND(AVG(a.score), 2) AS accuracy,
    COUNT(DISTINCT a.userID) AS totalParticipants,
    COUNT(DISTINCT q.questionID) AS totalQuestions
FROM attempt a
LEFT JOIN question q ON a.quizID = q.quizID
WHERE a.quizID = ?";
$stmt = $conn->prepare($summaryQuery);
$stmt->bind_param("i", $quizID);
$stmt->execute();
$summaryResult = $stmt->get_result()->fetch_assoc();
$stmt->close();

// Get the sortBy parameter from the URL
$sortBy = isset($_GET['sortBy']) ? $_GET['sortBy'] : 'name'; // Default to 'name'

// Determine the SQL column to sort by
switch ($sortBy) {
    case 'accuracy':
        $orderBy = 'accuracy DESC';
        break;
    case 'points':
        $orderBy = 'points DESC';
        break;
    case 'score':
        $orderBy = 'totalScore DESC';
        break;
    case 'name':
    default:
        $orderBy = 'u.username ASC'; // Default to sorting by name
        break;
}

// Fetch PARTICIPANT DETAILS with attemptId
$participantQuery = "
SELECT 
    u.username AS name,  
    ROUND((SUM(CASE WHEN a.score > 0 THEN 1 ELSE 0 END) / COUNT(q.questionID)) * 100, 2) AS accuracy, 
    CONCAT(JSON_LENGTH(JSON_EXTRACT(a.correctQuestions, '$')), '/', COUNT(q.questionID)) AS points,
    SUM(a.score) AS totalScore, 
    a.userID,
    a.correctQuestions,
    a.attemptId 
FROM attempt a
LEFT JOIN question q ON a.quizID = q.quizID
LEFT JOIN user u ON a.userID = u.userID  
WHERE a.quizID = ?
GROUP BY a.userID, a.attemptId
ORDER BY $orderBy";
$stmt = $conn->prepare($participantQuery);
$stmt->bind_param("i", $quizID);
$stmt->execute();
$participants = $stmt->get_result()->fetch_all(MYSQLI_ASSOC);
$stmt->close();

foreach ($participants as &$participant) {
    // Check if correctQuestions exists and is not null
    if (isset($participant['correctQuestions']) && !empty($participant['correctQuestions'])) {
        // Decode JSON
        $correctAnswers = json_decode($participant['correctQuestions'], true);

        if (json_last_error() === JSON_ERROR_NONE && is_array($correctAnswers)) {
            // Count true and false values
            $participant['trueCount'] = count(array_filter($correctAnswers, fn($value) => $value === true));
            $participant['falseCount'] = count(array_filter($correctAnswers, fn($value) => $value === false));
        } else {
            // Handle invalid JSON
            $participant['trueCount'] = 0;
            $participant['falseCount'] = 0;
        }
    } else {
        // Default values if correctQuestions is missing or empty
        $participant['trueCount'] = 0;
        $participant['falseCount'] = 0;
    }
}
unset($participant);




// Calculate average accuracy for all attempts
$sqlAttempts = "SELECT correctQuestions FROM attempt WHERE quizId = ?";
$stmt = $conn->prepare($sqlAttempts);
$stmt->bind_param("i", $quizID);
$stmt->execute();
$result1 = $stmt->get_result();
$totalParticipants = $result1->num_rows;
$totalAccuracy = 0;

while ($row = $result1->fetch_assoc()) {
    $data = json_decode($row['correctQuestions'], true);
    if (json_last_error() !== JSON_ERROR_NONE || !is_array($data)) {
        continue;
    }
    $totalCorrect = array_sum($data);
    $totalQuestions = count($data);
    $totalAccuracy += ($totalCorrect / $totalQuestions);
}

$averageAccuracy = $totalParticipants > 0 ? ($totalAccuracy / $totalParticipants) * 100 : 0;
$stmt->close();


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
            echo "<script>alert('Quiz name updated successfully!');</script>";
            // You can redirect after the rename
            header("Location: ?quizID=" . $quizID);
            exit();
        } else {
            echo "<script>alert('Error renaming quiz!');</script>";
        }

        $stmt->close();
    } 
} 
// Handle Delete Attempt
if (isset($_GET['attemptId'])) {
    $attemptId = intval($_GET['attemptId']);
    $quizID = isset($_GET['quizID']) ? intval($_GET['quizID']) : null;

    // Validate quizID
    if (!$quizID) {
        echo "Quiz ID not provided.";
        exit();
    }

    // Delete the attempt from the database
    $stmt = $conn->prepare("DELETE FROM attempt WHERE attemptId = ?");
    $stmt->bind_param("i", $attemptId);

    if ($stmt->execute()) {
        // Redirect to the original page after deletion
        $returnUrl = isset($_GET['returnUrl']) ? filter_var($_GET['returnUrl'], FILTER_SANITIZE_URL) : "Review.php?quizID=$quizID";
        header("Location: " . $returnUrl);
        exit();
    } else {
        echo "Error deleting attempt: " . $stmt->error;
    }

    $stmt->close();
}




// Handle Delete Quiz
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['delete_quiz'])) {
    // Ensure the quiz ID is set
    $quizId = intval($_POST['quizId']);

    // Check if the logged-in user is the creator of the quiz
    $stmt = $conn->prepare("SELECT userId FROM quiz WHERE quizId = ?");
    $stmt->bind_param("i", $quizId);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $quiz = $result->fetch_assoc();
            // Delete related attempts and quiz
            $conn->begin_transaction();

            try {
                $conn->prepare("DELETE FROM attempt WHERE quizId = ?")->execute([$quizId]);
                $conn->prepare("DELETE FROM question WHERE quizId = ?")->execute([$quizId]);
                $conn->prepare("DELETE FROM quiz WHERE quizId = ?")->execute([$quizId]);

                $conn->commit();
                echo "<script>alert('Quiz deleted successfully.');</script>";
                header("Location: reportMainPage.php"); // Redirect to the quiz list page
                exit();
            } catch (Exception $e) {
                $conn->rollback();
                echo "<script>alert('Error deleting quiz: {$e->getMessage()}');</script>";
            }
        
    }
}


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

// Close connection
$conn->close();
?>


<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="styling.css">
    <link rel="icon" type="image/x-icon" href="../ideas.png">
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,100..900;1,100..900&family=Open+Sans:ital,wght@0,300..800;1,300..800&display=swap" rel="stylesheet">
    <title><?php echo htmlspecialchars($quiz['title']); ?> - Report</title>
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
    <!-- Main Content -->
    <div class="main-container">
        <div class="report-summary">
            <h2><?php echo htmlspecialchars($quiz['title']); ?> (Quiz Code: <?php echo $quizID; ?>)</h2>
            <p>Created: <?php echo date('Y-m-d'); ?></p>

            <div class="summary-stats" style="display: flex; gap: 20px;">
                <div class="stat-box"><p>Average Accuracy</p><h3><?php echo number_format($averageAccuracy, 2); ?>%</h3></div>
                <div class="stat-box"><p>Completion Rate</p><h3>100%</h3></div>
                <div class="stat-box"><p>Total Participants</p><h3><?php echo $summaryResult['totalParticipants']; ?></h3></div>
                <div class="stat-box"><p>Questions</p><h3><?php echo $summaryResult['totalQuestions']; ?></h3></div>
            </div>

            <div class="button-container" style="margin-top: 20px; display: flex; gap: 10px;">
                <button class="action-btn rename-btn" onclick="renameQuiz()">Rename Quiz</button>
                <button class="action-btn" onclick="location.href='Leaderboard.php?quizID=<?php echo $quizID; ?>'">Leaderboard</button>
                <form method="POST" style="display:inline;">
                    <input type="hidden" name="quizId" value="<?php echo $quizID; ?>">
                    <button class="action-btn delete-btn" type="submit" name="delete_quiz">Delete Quiz</button>
                </form>
            </div>
        </div>

        <!-- Dropdown Sorting -->
        <div class="sort-by-container" style="margin-top: 20px; display: flex; justify-content: flex-end; gap: 10px;">
            <label for="sortBy" style="font-weight: bold;">Sort By:</label>
            <select id="sortBy" onchange="applySort()">
                <option value="name">Name</option>
                <option value="accuracy">Accuracy</option>
                <option value="points">Points</option>
                <option value="score">Score</option>
            </select>
        </div>

        <!-- Participant Table -->
        <table class="participant-table" style="width: 100%; border-collapse: collapse; margin-top: 20px;">
            <thead>
                <tr>
                    <th>Name</th>
                    <th>Accuracy</th>
                    <th>Correct Questions</th>
                    <th>Wrong Questions</th>
                    <th>Score</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <?php foreach ($participants as $participant): ?>
                    <tr>
                        <td><?php echo htmlspecialchars($participant['name'] ?? 'Unknown'); ?></td>
                        <td><?php echo $participant['accuracy']; ?>%</td>
                        <td><?php echo $participant['trueCount']; ?></td>
                        <td><?php echo $participant['falseCount']; ?></td>
                        <td><?php echo $participant['totalScore']; ?></td>
                        <td>
                            <button class="btn review" onclick="location.href='ReviewComment.php?quizID=<?php echo $quizID; ?>&userID=<?php echo $participant['userID']; ?>'">Review</button>
                            <button class="btn delete" onclick="deleteAttempt('<?php echo $participant['attemptId']; ?>')">Delete</button>
                        </td>
                    </tr>
                <?php endforeach; ?>
            </tbody>
        </table>
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
function renameQuiz() {
    const newName = prompt("Enter new quiz name:", "<?php echo htmlspecialchars($quiz['title']); ?>");
    if (newName) {
        const form = document.createElement('form');
        form.method = 'POST';
        form.innerHTML = `
            <input type="hidden" name="quizID" value="<?php echo $quizID; ?>">
            <input type="hidden" name="newName" value="${newName}">
        `;
        document.body.appendChild(form);
        form.submit();
    }
}

function deleteAttempt(attemptId) {
    if (confirm("Are you sure you want to delete this attempt?")) {
        window.location.href = `?attemptId=${attemptId}&quizID=<?php echo $quizID; ?>`;
    }
}

function applySort() {
    const sortBy = document.getElementById('sortBy').value;
    const url = new URL(window.location.href);
    url.searchParams.set('sortBy', sortBy);
    window.location.href = url.toString();
}

function toggleMenu() {
    const navLinks = document.getElementById("nav-links");
    navLinks.classList.toggle("active");
}
</script>

<style>
/* Main Content */
.main-container {
    max-width: 1200px;
    margin: 20px auto;
    padding: 20px;
    background-color: #7e6df3;
    border-radius: 10px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
    color: #fff;
}

.report-summary {
    text-align: left;
    margin-bottom: 20px;
}

.summary-stats {
    display: flex;
    justify-content: space-between;
    gap: 10px;
    margin-top: 20px;
}

.stat-box {
    background-color: #533b7c;
    padding: 20px;
    border-radius: 5px;
    text-align: center;
    flex: 1;
    color: #fff;
}

.stat-box h3 {
    font-size: 1.8rem;
    margin: 0;
}

/* Buttons */
.button-container {
    display: flex;
    gap: 10px;
    margin-top: 20px;
}

.action-btn {
    background-color: #533b7c;
    color: white;
    padding: 10px 15px;
    font-size: 1rem;
    font-weight: bold;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    transition: all 0.3s ease;
}

.action-btn:hover {
    background-color: #ab3cfc;
    transform: scale(1.05);
}

.rename-btn {
    background-color: #4CAF50;
}

.delete-btn {
    background-color: #e74c3c;
}

/* Sort By Dropdown */
.sort-by-container {
    text-align: right;
    margin-top: 10px;
}

.sort-by-container select {
    padding: 8px;
    border-radius: 5px;
    border: 1px solid #ddd;
}

/* Table */
.participant-table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 20px;
    color: #333;
}

.participant-table th, .participant-table td {
    padding: 12px;
    text-align: center;
    border: 1px solid #533b7c;
    background-color: #3e394c;
    color: #fff;
}

.participant-table th {
    background-color: #533b7c;
    color: #fff;
}

.progress-bar {
    display: flex;
    width: 100%;
    height: 10px;
    background-color: #ddd;
    border-radius: 5px;
    overflow: hidden;
}

.correct {
    background-color: #4CAF50;
}

.incorrect {
    background-color: #e74c3c;
}

.btn {
    padding: 5px 10px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.9rem;
}

.review {
    background-color: #7e6df3;
    color: white;
}

.delete {
    background-color: #e74c3c;
    color: white;
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