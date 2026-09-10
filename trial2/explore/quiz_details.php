<?php
// Include the database connection file
include 'db.php';

// Assume $quizID is provided via GET request
$quizID = intval($_GET['quizID']);

// Fetch quiz details including the creator's username and the quiz creation date
$query = "
SELECT q.title, q.description, u.username AS createdBy, q.created_at, q.quizId
FROM quiz q
JOIN user u ON q.userId = u.userId
WHERE q.quizId = $quizID";

$result = mysqli_query($conn, $query);
if (!$result) {
    echo "Error executing quiz query: " . mysqli_error($conn);
    exit();
}
$quiz = mysqli_fetch_assoc($result);

if (!$quiz) {
    echo "Quiz not found.";
    exit();
}

// Fetch questions related to this quiz
$questionsQuery = "
SELECT questionId, questionText, questionType, answerText, point 
FROM question 
WHERE quizId = $quizID";
$questionsResult = mysqli_query($conn, $questionsQuery);
if (!$questionsResult) {
    echo "Error executing questions query: " . mysqli_error($conn);
    exit();
}
$questions = mysqli_fetch_all($questionsResult, MYSQLI_ASSOC);

foreach ($questions as &$question) {
    // Decode the 'answerText' JSON field
    $decodedAnswers = json_decode($question['answerText'], true);

    if (json_last_error() !== JSON_ERROR_NONE) {
        die("JSON decoding failed for questionId = {$question['questionId']}. Error: " . json_last_error_msg());
    }

    $question['answerText'] = $decodedAnswers; // Replace raw JSON with decoded array
}
unset($question); // Unset reference to prevent unintended behavior
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?php echo htmlspecialchars($quiz['title']); ?> - Quiz Details</title>
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

        .container {
            max-width: 900px;
            margin: 50px auto;
            padding: 20px;
            background: #ffffff;
            border-radius: 8px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }

        .quiz-header {
            margin-bottom: 30px;
            position: relative;
        }

        .quiz-header h1 {
            font-size: 24px;
            margin: 0;
            color: #007bff;
        }

        .quiz-header p {
            margin: 5px 0;
            font-size: 14px;
            color: #555;
        }

        .quiz-header .join-button {
            position: absolute;
            top: 0;
            right: 0;
        }

        .join-button a {
            display: inline-block;
            padding: 10px 20px;
            background-color: #007bff;
            color: #fff;
            text-decoration: none;
            font-size: 16px;
            border-radius: 5px;
            transition: background-color 0.3s;
        }

        .join-button a:hover {
            background-color: #0056b3;
        }

        .question {
            margin-bottom: 20px;
            padding: 15px;
            border: 1px solid #ddd;
            border-radius: 5px;
            background-color: #f9f9f9;
        }

        .question h3 {
            margin: 0 0 10px;
            font-size: 18px;
            color: #333;
        }

        .answers {
            margin-top: 10px;
        }

        .answers ul {
            list-style: none;
            padding: 0;
        }

        .answers ul li {
            margin-bottom: 5px;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 5px;
            background: #ffffff;
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
    <div class="container">
        <div class="quiz-header">
            <h1><?php echo htmlspecialchars($quiz['title']); ?></h1>
            <p>Created by: <?php echo htmlspecialchars($quiz['createdBy']); ?></p>
            <p>Created on: <?php echo htmlspecialchars($quiz['created_at']); ?></p>
            <p>Description: <?php echo htmlspecialchars($quiz['description']); ?></p>
            <div class="join-button">
                <a href="../createQuiz/quiz.php?quizId=<?php echo htmlspecialchars($quiz['quizId']); ?>">Join Quiz</a>
            </div>
        </div>

        <div class="questions">
            <h2>Questions</h2>
            <?php if (!empty($questions)): ?>
                <?php foreach ($questions as $index => $question): ?>
                    <div class="question">
                        <h3><?php echo ($index + 1) . ". " . htmlspecialchars($question['questionText']); ?></h3>
                        <div class="answers">
                            <ul>
                                <?php foreach ($question['answerText'] as $answer): ?>
                                    <li><?php echo htmlspecialchars($answer['text']); ?></li>
                                <?php endforeach; ?>
                            </ul>
                        </div>
                    </div>
                <?php endforeach; ?>
            <?php else: ?>
                <p>No questions found for this quiz.</p>
            <?php endif; ?>
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