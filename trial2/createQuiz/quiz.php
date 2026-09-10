<?php
// Database connection
include "db.php";

session_start();
if (!isset($_SESSION['user_id'])) {
    $_SESSION['redirect_to'] = $_SERVER['REQUEST_URI'] ?? '/index.php';
    header("Location: ../profile/login.php");
    exit();
}

if (!isset($_GET['quizId']) || !is_numeric($_GET['quizId'])) {
    echo "Invalid access. Please enter a valid quiz ID.";
    exit;
}

$quizId = (int)$_GET['quizId'];
$userId = $_SESSION['user_id'];

// Fetch quiz details
$sql = "SELECT quizId, title, description, userId FROM quiz WHERE quizId = ?";
$stmt = $conn->prepare($sql);
if (!$stmt) {
    die("Database query error: " . $conn->error);
}
$stmt->bind_param("i", $quizId);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo "Quiz not found.";
    exit;
}

$quiz = $result->fetch_assoc();

// Fetch questions for the quiz
$sqlQuestions = "SELECT questionId, questionText, questionType, answerText, time, point FROM question WHERE quizId = ?";
$stmtQuestions = $conn->prepare($sqlQuestions);
if (!$stmtQuestions) {
    die("Database query error: " . $conn->error);
}
$stmtQuestions->bind_param("i", $quizId);
$stmtQuestions->execute();
$questionsResult = $stmtQuestions->get_result();

$questions = [];
while ($row = $questionsResult->fetch_assoc()) {
    $row['answerText'] = json_decode($row['answerText'], true); // Decode JSON if needed
    $questions[] = $row;
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $userAnswers = $_POST['answers'] ?? [];
    $score = 0;
    $correct = 0;
    $incorrect = 0;
    $correctQuestions = [];
    $datetime = date('Y-m-d H:i:s'); // Current datetime

    foreach ($questions as $question) {
        $questionId = $question['questionId'];
        $correctAnswers = array_filter($question['answerText'], function ($answer) {
            return isset($answer['isCorrect']) && $answer['isCorrect'];
        });
        $correctAnswersText = array_column($correctAnswers, 'text');

        // If no correct answers exist for this question
        if (empty($correctAnswersText)) {
            if (isset($userAnswers[$questionId])) {
                // User answered a question that has no correct answers
                $incorrect++;
                $correctQuestions[$questionId] = false; // Mark as incorrect
            }
            continue; // Skip to the next question
        }

        // Validate user answer
        if (isset($userAnswers[$questionId])) {
            $userAnswer = $userAnswers[$questionId];

            if (is_array($userAnswer)) {
                // For multiple-answer questions (checkboxes)
                sort($userAnswer);
                sort($correctAnswersText);
                if ($userAnswer === $correctAnswersText) {
                    $score += $question['point'];
                    $correct++;
                    $correctQuestions[$questionId] = true;
                } else {
                    $incorrect++;
                    $correctQuestions[$questionId] = false;
                }
            } else {
                // For single-answer questions (radio or text)
                if (in_array($userAnswer, $correctAnswersText)) {
                    $score += $question['point'];
                    $correct++;
                    $correctQuestions[$questionId] = true;
                } else {
                    $incorrect++;
                    $correctQuestions[$questionId] = false;
                }
            }
        } else {
            // User did not answer
            $incorrect++;
            $correctQuestions[$questionId] = false; // Mark as incorrect
        }
    }

    // Save the attempt to the database
    $sqlSaveAttempt = "INSERT INTO attempt (userId, quizId, score, correctQuestions, attemptTime) VALUES (?, ?, ?, ?, ?)";
    $stmtSaveAttempt = $conn->prepare($sqlSaveAttempt);
    $correctQuestionsJson = json_encode($correctQuestions);
    $stmtSaveAttempt->bind_param("iiiss", $userId, $quizId, $score, $correctQuestionsJson, $datetime);
    $stmtSaveAttempt->execute();

    // Get the last inserted attemptId
    $attemptId = $stmtSaveAttempt->insert_id;

    // Display hidden form to submit GET request
    echo '<form id="redirectForm" method="GET" action="quizSummary.php">';
    echo '<input type="hidden" name="attemptId" value="' . htmlspecialchars($attemptId) . '">';
    echo '</form>';
    echo '<script>document.getElementById("redirectForm").submit();</script>';
    exit;
}

?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?php echo htmlspecialchars($quiz['title']); ?></title>
    <link rel="icon" type="image/x-icon" href="../ideas.png">

    <style>
        body {
            font-family: 'Arial', sans-serif;
            background-color: #e9f1f4;
            color: #333;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }

        .container {
            max-width: 800px;
            width: 100%;
            margin: auto;
            background-color: #fff;
            border-radius: 8px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            padding: 20px;
        }

        .quiz-header {
            text-align: center;
            margin-bottom: 20px;
        }

        .question {
            margin-bottom: 20px;
        }

        .question h3 {
            font-size: 18px;
        }

        .answer {
            margin: 5px 0;
        }

        .submit-btn {
            display: block;
            width: 100%;
            padding: 10px;
            background-color: #2196F3;
            color: white;
            font-size: 16px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
        }

        .submit-btn:hover {
            background-color: #1976D2;
        }

        .no-questions {
            text-align: center;
            font-size: 16px;
            color: #555;
        }

        h1, h2 {
            margin: 10px 0;
        }

        ul {
            padding-left: 20px;
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
        <div class="quiz-header">
            <h1><?php echo htmlspecialchars($quiz['title']); ?></h1>
            <p>by: <?php echo htmlspecialchars($quiz['author'] ?? 'Unknown'); ?></p>
        </div>

        <?php if (empty($questions)): ?>
            <p class="no-questions">No questions available for this quiz.</p>
        <?php else: ?>
            <form method="POST">
                <?php foreach ($questions as $index => $question): ?>
                    <input type="hidden" name="quizId" value="<?php echo htmlspecialchars($quizId); ?>">

                    <div class="question">
                        <h3><?php echo ($index + 1) . ". " . htmlspecialchars($question['questionText']); ?></h3>

                        <?php if ($question['questionType'] === 'multiple-choice'): ?>
                            <?php foreach ($question['answerText'] as $key => $answer): ?>
                                <div class="answer">
                                    <input type="radio" name="answers[<?php echo $question['questionId']; ?>]" value="<?php echo htmlspecialchars($answer['text']); ?>">
                                    <?php echo htmlspecialchars($answer['text']); ?>
                                </div>
                            <?php endforeach; ?>
                        <?php elseif ($question['questionType'] === 'checkbox'): ?>
                            <?php foreach ($question['answerText'] as $key => $answer): ?>
                                <div class="answer">
                                    <input type="checkbox" name="answers[<?php echo $question['questionId']; ?>][]" value="<?php echo htmlspecialchars($answer['text']); ?>">
                                    <?php echo htmlspecialchars($answer['text']); ?>
                                </div>
                            <?php endforeach; ?>
                        <?php else: ?>
                            <div class="answer">
                                <input type="text" name="answers[<?php echo $question['questionId']; ?>]" placeholder="Your answer">
                            </div>
                        <?php endif; ?>
                    </div>
                <?php endforeach; ?>

                <button type="submit" class="submit-btn">Submit</button>
            </form>
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
