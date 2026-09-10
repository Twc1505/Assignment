<?php
// quiz.php - Quiz Management
include 'db.php';
session_start();

// Redirect to login page if not logged in
if (!isset($_SESSION['user_id'])) {
    header('Location: login.php');
    exit();
}

// Assume session stores logged-in admin details
$loggedInAdminRole = $_SESSION['role'] ?? 'Guest'; // Default role as Guest

// Handle form submission for create, edit, and delete actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $action = $_POST['action'] ?? '';

    if ($action === 'create' && $loggedInAdminRole === 'Administrator') {
        // Add a new quiz
        $title = $_POST['title'];
        $description = $_POST['description'];
        $userId = intval($_POST['userId']);
        $code = $_POST['code'];

        $insert_sql = "INSERT INTO quiz (title, description, userId) VALUES (?, ?, ?, ?)";
        $stmt = $conn->prepare($insert_sql);
        $stmt->bind_param("ssis", $title, $description, $userId, $code);
        if ($stmt->execute()) {
            echo "<script>alert('Quiz added successfully!');</script>";
        } else {
            echo "<script>alert('Failed to add quiz.');</script>";
        }
        $stmt->close();
    } elseif ($action === 'delete' && $loggedInAdminRole !== 'Guest') {
        // Delete a quiz
        $quizId = intval($_POST['quizId']);

        // Start transaction to ensure atomic deletion
        $conn->begin_transaction();
        try {
            // Step 1: Delete related attempts
            $delete_attempts = "DELETE FROM attempt WHERE quizId = ?";
            $stmt_attempts = $conn->prepare($delete_attempts);
            $stmt_attempts->bind_param("i", $quizId);
            $stmt_attempts->execute();
            $stmt_attempts->close();

            // Step 2: Delete related questions
            $delete_questions = "DELETE FROM question WHERE quizId = ?";
            $stmt_questions = $conn->prepare($delete_questions);
            $stmt_questions->bind_param("i", $quizId);
            $stmt_questions->execute();
            $stmt_questions->close();

            // Step 3: Delete the quiz itself
            $delete_quiz = "DELETE FROM quiz WHERE quizId = ?";
            $stmt_quiz = $conn->prepare($delete_quiz);
            $stmt_quiz->bind_param("i", $quizId);
            $stmt_quiz->execute();
            $stmt_quiz->close();

            // Commit transaction
            $conn->commit();
            echo "<script>alert('Quiz deleted successfully!'); window.location.href = 'quiz.php';</script>";
        } catch (Exception $e) {
            $conn->rollback();
            echo "<script>alert('Failed to delete quiz: " . $e->getMessage() . "');</script>";
        }
    }
}

// Fetch all quizzes
$sql = "SELECT * FROM quiz";
$result = $conn->query($sql);
?>

<!DOCTYPE html>
<html>
<head>
    <title>Quiz Management</title>
    <link rel="stylesheet" href="styling.css">
</head>
<body>
    <div id="sidebar" class="collapsed">
        <h2>Cin Cai</h2>
        <button id="toggle-sidebar">☰</button>
        <ul>
            <li><a href="dashboard.php">Dashboard</a></li>
            <li><a href="admin.php">Admins</a></li>
            <li><a href="user.php">Users</a></li>
            <li><a href="quiz.php">Quizzes</a></li>
            <li><a href="logout.php">Logout</a></li>
        </ul>
    </div>
    <div id="main-content">
        <h1>Quiz Management</h1>

        <?php if ($loggedInAdminRole === 'Administrator'): ?>
        <form method="POST" action="">
            <h2>Add Quiz</h2>
            <label>Title:</label><br>
            <input type="text" name="title" required><br>

            <label>Description:</label><br>
            <textarea name="description" required></textarea><br>

            <label>Author (User ID):</label><br>
            <input type="number" name="userId" required><br>

            <label>Code:</label><br>
            <input type="text" name="code" required><br><br>

            <input type="hidden" name="action" value="create">
            <button type="submit">Add Quiz</button>
        </form>
        <?php endif; ?>

        <h2>Quiz List</h2>
        <table>
            <tr>
                <th>ID</th>
                <th>Title</th>
                <th>Description</th>
                <th>Author</th>
                <th>Code</th>
                <th>Actions</th>
            </tr>
            <?php
            if ($result->num_rows > 0) {
                while ($row = $result->fetch_assoc()) {
                    echo "<tr>";
                    echo "<td>" . $row['quizId'] . "</td>";
                    echo "<td>" . htmlspecialchars($row['title']) . "</td>";
                    echo "<td>" . htmlspecialchars($row['description']) . "</td>";
                    echo "<td>" . $row['userId'] . "</td>";
                    echo "<td>" . htmlspecialchars($row['quizId']) . "</td>";
                    echo "<td>";
                    if ($loggedInAdminRole !== 'Guest') {
                        echo "<form method='POST' action='' style='display:inline; margin:0; padding:0;'>
                                <input type='hidden' name='quizId' value='" . $row['quizId'] . "'>";
                        if ($loggedInAdminRole === 'Administrator') {
                            echo "<input type='hidden' name='action' value='edit'>
                                  <button type='submit'>Edit</button>";
                        }
                        echo "<input type='hidden' name='action' value='delete'>
                              <button type='submit' onclick=\"return confirm('Are you sure you want to delete this quiz?');\">Delete</button>
                              </form>";
                    }
                    echo "</td>";
                    echo "</tr>";
                }
            } else {
                echo "<tr><td colspan='6'>No quizzes found</td></tr>";
            }
            ?>
        </table>
    </div>
    <script>
        const toggleSidebar = document.getElementById('toggle-sidebar');
        const sidebar = document.getElementById('sidebar');

        toggleSidebar.addEventListener('click', () => {
            sidebar.classList.toggle('collapsed');
        });
    </script>
</body>
</html>
