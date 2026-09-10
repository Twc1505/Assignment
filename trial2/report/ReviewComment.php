<?php
// ReviewComment.php
include 'db.php';
session_start();

// Check if the user is logged in
if (!isset($_SESSION['user_id'])) {
    header('Location: login.php');
    exit();
}

// Validate and sanitize input
if (!isset($_GET['quizID']) || !isset($_GET['userID'])) {
    header('Location: error.php?error=missing_parameters');
    exit();
}

$quizID = intval($_GET['quizID']);
$userID = intval($_GET['userID']);

// Fetch quiz and user data
$query = "SELECT q.title, u.username FROM quiz q, user u WHERE q.quizID = ? AND u.userID = ?";
$stmt = $conn->prepare($query);
$stmt->bind_param("ii", $quizID, $userID);
$stmt->execute();
$result = $stmt->get_result();
$data = $result->fetch_assoc();
$stmt->close();

if (!$data) {
    header('Location: error.php?error=quiz_or_user_not_found');
    exit();
}

// Fetch existing comment
$commentQuery = "SELECT comment FROM attempt WHERE quizID = ? AND userID = ?";
$stmt = $conn->prepare($commentQuery);
$stmt->bind_param("ii", $quizID, $userID);
$stmt->execute();
$result = $stmt->get_result();
$commentData = $result->fetch_assoc();
$stmt->close();

$currentComment = $commentData['comment'] ?? "";

// Handle form submission
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $newComment = htmlspecialchars(trim($_POST['comment']));

    // Update the comment in the database
    $updateQuery = "UPDATE attempt SET comment = ? WHERE quizID = ? AND userID = ?";
    $stmt = $conn->prepare($updateQuery);
    $stmt->bind_param("sii", $newComment, $quizID, $userID);
    if ($stmt->execute()) {
        $successMessage = "Comment updated successfully.";
    } else {
        $errorMessage = "Failed to update comment.";
    }
    $stmt->close();
}

$conn->close();
?>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Review Comment</title>
<link rel="icon" type="image/x-icon" href="../ideas.png">
<style>
    body {
        font-family: 'Montserrat', sans-serif;
        background-color: #f4f4f9;
        color: #333;
        margin: 0;
        padding: 0;
    }
    .container {
        max-width: 800px;
        margin: 50px auto;
        background: #fff;
        padding: 20px;
        border-radius: 8px;
        box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
    }
    h1 {
        font-size: 24px;
        margin-bottom: 20px;
    }
    form {
        display: flex;
        flex-direction: column;
        gap: 15px;
    }
    textarea {
        width: 100%;
        height: 150px;
        padding: 10px;
        border: 1px solid #ddd;
        border-radius: 4px;
        resize: none;
        font-size: 16px;
    }
    button {
        padding: 10px 20px;
        background-color: #4caf50;
        color: #fff;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-size: 16px;
    }
    button:hover {
        background-color: #45a049;
    }
    .message {
        margin-top: 20px;
        padding: 10px;
        border-radius: 4px;
        font-size: 14px;
    }
    .message.success {
        background-color: #d4edda;
        color: #155724;
    }
    .message.error {
        background-color: #f8d7da;
        color: #721c24;
    }
</style>
</head>
<body>
    <div class="container">
        <h1>Review Comment for <?php echo htmlspecialchars($data['username']); ?> - Quiz: <?php echo htmlspecialchars($data['title']); ?></h1>

        <?php if (isset($successMessage)): ?>
            <div class="message success"> <?php echo $successMessage; ?> </div>
        <?php endif; ?>

        <?php if (isset($errorMessage)): ?>
            <div class="message error"> <?php echo $errorMessage; ?> </div>
        <?php endif; ?>

        <form method="POST">
            <label for="comment">Comment:</label>
            <textarea name="comment" id="comment" placeholder="Write your feedback here..."><?php echo htmlspecialchars($currentComment); ?></textarea>
            <button type="submit">Update Comment</button>
        </form>
    </div>
</body>
</html>