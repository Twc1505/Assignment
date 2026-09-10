<?php
// edit_user.php - Edit User Page
include 'db.php';
session_start();

// Redirect to login page if not logged in
if (!isset($_SESSION['user_id'])) {
    header('Location: login.php');
    exit();
}

// Check if `userId` is provided
if (!isset($_GET['userId'])) {
    header('Location: user.php');
    exit();
}

$userId = intval($_GET['userId']);
$message = "";

// Fetch current user details
$sql = "SELECT * FROM user WHERE userId = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $userId);
$stmt->execute();
$result = $stmt->get_result();
$user = $result->fetch_assoc();

if (!$user) {
    echo "<script>alert('User not found!'); window.location.href = 'user.php';</script>";
    exit();
}

// Handle form submission
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $username = $_POST['username'];
    $email = $_POST['email'];
    $role = $_POST['role'];

    // Update user details
    $update_sql = "UPDATE user SET username = ?, email = ?, role = ? WHERE userId = ?";
    $update_stmt = $conn->prepare($update_sql);
    $update_stmt->bind_param("sssi", $username, $email, $role, $userId);

    if ($update_stmt->execute()) {
        $message = "User updated successfully.";
        // Update the fetched user details after the update
        $user['username'] = $username;
        $user['email'] = $email;
        $user['role'] = $role;
    } else {
        $message = "Failed to update user: " . $conn->error;
    }
}
?>

<!DOCTYPE html>
<html>
<head>
    <title>Edit User</title>
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
        <div id="notifications">
            <?php if (!empty($message)) echo "<p>$message</p>"; ?>
        </div>
        <h1>Edit User</h1>
        <form method="POST" action="">
            <label>Username:</label>
            <input type="text" name="username" value="<?= htmlspecialchars($user['username']) ?>" required>

            <label>Email:</label>
            <input type="email" name="email" value="<?= htmlspecialchars($user['email']) ?>" required>

            <label>Role:</label>
            <select name="role">
                <option value="student" <?= $user['role'] === 'student' ? 'selected' : '' ?>>student</option>
                <option value="content creator" <?= $user['role'] === 'content creator' ? 'selected' : '' ?>>content creator</option>
                <option value="educator" <?= $user['role'] === 'educator' ? 'selected' : '' ?>>educator</option>
            </select>

            <button type="submit" style="background-color: #3498db; color: white; padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer;">Update User</button>
        </form>
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
