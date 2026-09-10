<?php
// Database connection
include "db.php";

session_start();
if (!isset($_SESSION['user_id'])) {
    $_SESSION['redirect_to'] = $_SERVER['REQUEST_URI'] ?? '/index.php';
    header("Location:  ../profile/login.php");
    exit();
}

if (!isset($_GET['attemptId']) || !is_numeric($_GET['attemptId'])) {
    echo "Invalid access. Please enter a valid attempt ID.";
    exit;
}

$attemptId = (int)$_GET['attemptId'];
$userId = $_SESSION['user_id'];

// Fetch attempt details
$sqlAttempt = "SELECT * FROM attempt WHERE attemptId = ? AND userId = ?";
$stmtAttempt = $conn->prepare($sqlAttempt);
if (!$stmtAttempt) {
    die("Database query error: " . $conn->error);
}
$stmtAttempt->bind_param("ii", $attemptId, $userId);
$stmtAttempt->execute();
$resultAttempt = $stmtAttempt->get_result();

if ($resultAttempt->num_rows === 0) {
    echo "Attempt not found or access denied.";
    exit;
}

$attempt = $resultAttempt->fetch_assoc();
$quizId = $attempt['quizId'];
$score = $attempt['score'];
$correctQuestions = json_decode($attempt['correctQuestions'], true);
$datetime = $attempt['attemptTime'];

// Fetch quiz details
$sqlQuiz = "SELECT title, description FROM quiz WHERE quizId = ?";
$stmtQuiz = $conn->prepare($sqlQuiz);
$stmtQuiz->bind_param("i", $quizId);
$stmtQuiz->execute();
$resultQuiz = $stmtQuiz->get_result();

if ($resultQuiz->num_rows === 0) {
    echo "Quiz not found.";
    exit;
}

$quiz = $resultQuiz->fetch_assoc();

// Fetch questions and their details
$sqlQuestions = "SELECT q.questionId, q.questionText, q.answerText FROM question q WHERE q.quizId = ?";
$stmtQuestions = $conn->prepare($sqlQuestions);
$stmtQuestions->bind_param("i", $quizId);
$stmtQuestions->execute();
$resultQuestions = $stmtQuestions->get_result();

$questions = [];
while ($row = $resultQuestions->fetch_assoc()) {
    $row['answerText'] = json_decode($row['answerText'], true); // Decode JSON answers
    $questions[] = $row;
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quiz Summary</title>
    <link rel="icon" type="image/x-icon" href="../ideas.png">

    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f7fa;
            color: #333;
            margin: 0;
            padding: 0;
        }
        .container {
            max-width: 800px;
            margin: 20px auto;
            background-color: #fff;
            border-radius: 8px;
            padding: 20px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        }
        .header, .summary, .questions {
            margin-bottom: 20px;
        }
        .stat-box {
            display: inline-block;
            width: 150px;
            margin: 10px;
            padding: 10px;
            background-color: #d1e7dd;
            border-radius: 5px;
            text-align: center;
        }
        .question {
            margin-bottom: 20px;
        }
        .correct {
            background-color: #d4edda;
            border: 1px solid #c3e6cb;
            padding: 10px;
            border-radius: 5px;
        }
        .incorrect {
            background-color: #f8d7da;
            border: 1px solid #f5c6cb;
            padding: 10px;
            border-radius: 5px;
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
        <nav class="nav-links">
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
        <div class="header">
            <h2><?php echo htmlspecialchars($quiz['title']); ?></h2>
            <p><?php echo htmlspecialchars($quiz['description']); ?></p>
            <p><strong>Attempted on:</strong> <?php echo htmlspecialchars($datetime); ?></p>
        </div>
        <div class="summary">
            <h3>Summary</h3>
            <div class="stat-box">Score: <strong><?php echo $score; ?></strong></div>
            <div class="stat-box">Correct: <strong><?php echo count(array_filter($correctQuestions)); ?></strong></div>
            <div class="stat-box">Incorrect: <strong><?php echo count($questions) - count(array_filter($correctQuestions)); ?></strong></div>
        </div>
        <div class="questions">
            <h3>Questions</h3>
            <?php foreach ($questions as $question): ?>
                <?php
                $isCorrect = isset($correctQuestions[$question['questionId']]) && $correctQuestions[$question['questionId']];
                $cssClass = $isCorrect ? 'correct' : 'incorrect';
                ?>
                <div class="question <?php echo $cssClass; ?>">
                    <h4><?php echo htmlspecialchars($question['questionText']); ?></h4>
                    <ul>
                        <?php foreach ($question['answerText'] as $answer): ?>
                            <li><?php echo htmlspecialchars($answer['text']); ?></li>
                        <?php endforeach; ?>
                    </ul>
                </div>
            <?php endforeach; ?>
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
