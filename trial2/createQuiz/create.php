<?php
// Include database connection file
include "db.php";
session_start();

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
    header("Location: ../profile/login.php");
    exit();
}


if ($_SERVER["REQUEST_METHOD"] === "POST") {
    $conn->begin_transaction();

    try {
        // Create a new quiz
        $title = trim($_POST['title'] ?? '');
        $description = trim($_POST['description'] ?? '');
        $userId = $_SESSION['user_id'];

        if (empty($title) || empty($description)) {
            throw new Exception("Title and description are required.");
        }

        $sql1 = "INSERT INTO quiz (title, description, userId, created_at) VALUES (?, ?, ?, NOW())";
        $stmt1 = $conn->prepare($sql1);
        $stmt1->bind_param("ssi", $title, $description, $userId);
        $stmt1->execute();

        // Retrieve the generated quizId
        $quizId = $conn->insert_id;

        // Insert each question related to the quiz
        if (!empty($_POST['questionText']) && is_array($_POST['questionText'])) {
            foreach ($_POST['questionText'] as $index => $questionText) {
                $questionText = trim($_POST['questionText'][$index] ?? '');
                $questionType = trim($_POST['questionType'][$index] ?? '');
                $time = (int) ($_POST['time'][$index] ?? 10);
                $point = (int) ($_POST['points'][$index] ?? 1);

                if (empty($questionText)) {
                    throw new Exception("Each question must have a question text.");
                }

                $answerData = [];

                if ($questionType === 'multiple-choice' || $questionType === 'checkbox') {
                    $answers = array_filter($_POST['answers'][$index] ?? [], function($answer) {
                        return trim($answer) !== '';
                    });
                    $correctAnswers = isset($_POST['correctAnswer'][$index]) ? array_map('intval', (array)$_POST['correctAnswer'][$index]) : [];
                    foreach ($answers as $key => $answer) {
                        $isCorrect = in_array($key, $correctAnswers, true) ? true : false;
                        $answerData[] = ["text" => htmlspecialchars($answer), "isCorrect" => $isCorrect];
                    }
                } else {
                    $answers = array_filter($_POST['answers'][$index] ?? [], function($answer) {
                        return trim($answer) !== '';
                    });
                    foreach ($answers as $answer) {
                        $answerData[] = ["text" => htmlspecialchars($answer)];
                    }
                }

                $answerText = json_encode($answerData);

                if (empty($answerData)) {
                    throw new Exception("Each question must have at least one valid answer.");
                }

                $sql2 = "INSERT INTO question (quizId, questionText, questionType, answerText, time, point) 
                         VALUES (?, ?, ?, ?, ?, ?)";
                $stmt2 = $conn->prepare($sql2);
                $stmt2->bind_param("isssii", $quizId, $questionText, $questionType, $answerText, $time, $point);
                $stmt2->execute();
            }
        } else {
            throw new Exception("No questions provided.");
        }

        // Commit the transaction
        $conn->commit();
    } catch (Exception $e) {
        // Rollback the transaction if there's an error
        $conn->rollback();
        echo "Error: " . $e->getMessage();
    }
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create Quiz</title>
    <link rel="icon" type="image/x-icon" href="../ideas.png">
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">
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
        display: flex;
        flex-direction: column;
        min-height: 100vh; /* Ensures full viewport height */
        background-color: var(--accent-medium);
        color: var(--neutral-dark);
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
    main {
    flex: 1; /* Allow content to grow */
    padding: 20px;
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
        max-width: 900px;
        margin: 50px auto;
        background-color: #fff;
        padding: 20px;
        border-radius: 10px;
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    }

    h1 {
        text-align: center;
        margin-bottom: 20px;
    }

    .question-block {
        margin-bottom: 30px;
        padding: 20px;
        border: 1px solid #ccc;
        border-radius: 5px;
    }

    .question-label {
        font-weight: bold;
        margin-bottom: 10px;
        display: block;
    }

    label {
        display: block;
        font-weight: bold;
        margin-bottom: 5px;
    }

    input[type="text"], textarea, select {
        width: 100%;
        padding: 10px;
        margin-bottom: 15px;
        border: 1px solid #ccc;
        border-radius: 5px;
        font-size: 16px;
    }

    input[type="number"], textarea, select {
        width: 100%;
        padding: 10px;
        margin-bottom: 15px;
        border: 1px solid #ccc;
        border-radius: 5px;
        font-size: 16px;
    }

    .answer-option {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-bottom: 10px;
    }

    button {
        background-color: #4caf50;
        color: white;
        border: none;
        padding: 10px 20px;
        border-radius: 5px;
        cursor: pointer;
        font-size: 16px;
    }

    button:hover {
        background-color: #45a049;
    }

    .delete-btn {
        background-color: #f44336;
        color: white;
    }

    .delete-btn:hover {
        background-color: #e53935;
    }

    .add-question-btn {
        display: flex;
        justify-content: center;
        margin-top: 20px;
    }

    /* Footer Styling */
    footer {
        background-color: var(--neutral-dark);
        color: #fff;
        padding: 20px;
        text-align: center;
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
        <h1>Create a New Quiz</h1>
        <form id="quizForm" method="POST">
            <label>Title:</label>
            <input type="text" name="title" placeholder="Enter your quiz title" required>
            <input type="text" name="description" placeholder="Enter your quiz description" required>
            <div id="questionContainer">
                <div class="question-block" data-index="0">
                    <label class="question-label">Question 1</label>
                    <label for="questionType">Select Question Type:</label>
                    <select name="questionType[0]" onchange="changeQuestionType(this)">
                        <option value="multiple-choice">Multiple Choice</option>
                        <option value="fill-in-the-blank">Fill in the Blank</option>
                        <option value="checkbox">Checkbox</option>
                    </select>
                    <label>Question:</label>
                    <input type="text" name="questionText[0]" placeholder="Enter your question" required>
                    <label>Points:</label>
                    <input type="number" name="points[0]" min="0" placeholder="Points for this question">
                    <div class="multiple-choice-form question-type">
                        <label>Answer Choices:</label>
                        <div class="answer-container">
                            <div class="answer-option">
                                <input type="text" name="answers[0][]" placeholder="Option 1">
                                <input type="radio" name="correctAnswer[0]" value="0"> Correct
                                <button type="button" class="delete-btn" onclick="removeOption(this)">Delete</button>
                            </div>
                        </div>
                        <button type="button" onclick="addOption(this, 'radio')">Add Option</button>
                    </div>
                    <div class="fill-in-the-blank-form question-type" style="display: none;">
                        <label>Correct Answers:</label>
                        <div class="answer-container">
                            <div class="answer-option">
                                <input type="text" name="answers[0][]" placeholder="Correct Answer">
                                <button type="button" class="delete-btn" onclick="removeOption(this)">Delete</button>
                            </div>
                        </div>
                    </div>
                    <div class="checkbox-form question-type" style="display: none;">
                        <label>Answer Choices:</label>
                        <div class="answer-container">
                            <div class="answer-option">
                                <input type="text" name="answers[0][]" placeholder="Option 1">
                                <input type="checkbox" name="correctAnswer[0][]" value="0"> Correct
                                <button type="button" class="delete-btn" onclick="removeOption(this)">Delete</button>
                            </div>
                        </div>
                        <button type="button" onclick="addOption(this, 'checkbox')">Add Option</button>
                    </div>
                    <button type="button" class="delete-btn" onclick="deleteQuestion(this)" id="deleteButton">Delete Question</button>
                </div>
            </div>
            <div class="add-question-btn">
                <button type="button" onclick="addQuestion()">Add More Questions</button>
            </div>
            <div style="text-align: center;">
                <button type="submit">Save Quiz</button>
            </div>
        </form>
    </div>
    <script>
        let questionIndex = 1;
        function changeQuestionType(selectElement) {
            const questionBlock = selectElement.closest('.question-block');
            const questionTypes = questionBlock.querySelectorAll('.question-type');
            questionTypes.forEach(type => type.style.display = 'none');
            const selectedType = selectElement.value;
            questionBlock.querySelector(`.${selectedType}-form`).style.display = 'block';
        }

        function addOption(button, inputType) {
            const answerContainer = button.previousElementSibling;
            const newOption = document.createElement('div');
            newOption.classList.add('answer-option');
            const optionIndex = answerContainer.children.length;
            const questionIndex = answerContainer.closest('.question-block').dataset.index;

            newOption.innerHTML = `
                <input type="text" name="answers[${questionIndex}][]" placeholder="Option ${optionIndex + 1}">
                <input type="${inputType}" name="correctAnswer[${questionIndex}]${inputType === 'checkbox' ? '[]' : ''}" value="${optionIndex}"> Correct
                <button type="button" class="delete-btn" onclick="removeOption(this)">Delete</button>
            `;
            answerContainer.appendChild(newOption);
        }

        function addQuestion() {
            const questionContainer = document.getElementById('questionContainer');
            const newQuestion = document.querySelector('.question-block').cloneNode(true);
            newQuestion.dataset.index = questionIndex;
            newQuestion.querySelector('.question-label').innerText = `Question ${questionIndex + 1}`;
            newQuestion.querySelectorAll('input, select, textarea').forEach(input => {
                const name = input.name;
                if (name) {
                    input.name = name.replace(/\d+/, questionIndex);
                    if (input.type !== 'radio' && input.type !== 'checkbox') {
                        input.value = '';
                    }
                }
            });
            questionContainer.appendChild(newQuestion);
            questionIndex++;
        }

        function deleteQuestion(button) {
            const questionBlock = button.closest('.question-block');
            if (document.querySelectorAll('.question-block').length > 1) {
                questionBlock.remove();
            } else {
                alert('At least one question is required.');
            }
        }

        function removeOption(button) {
            const option = button.closest('.answer-option');
            const container = option.parentElement;
            if (container.children.length > 1) {
                option.remove();
            }
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
