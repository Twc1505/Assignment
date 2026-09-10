<?php
// addAdmin.php - Add Admin Page
include 'db.php';
session_start();

// Redirect to login page if not logged in
if (!isset($_SESSION['user_id'])) {
    header('Location: login.php');
    exit();
}
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $username = $_POST['username'];
    $email = $_POST['email'];
    $password = password_hash($_POST['password'], PASSWORD_BCRYPT);
    $role = $_POST['role'];

    $sql = "INSERT INTO admin (username, email, password, role) VALUES ('$username', '$email', '$password', '$role')";

    if ($conn->query($sql) === TRUE) {
        $message = "New admin created successfully.";
    } else {
        $message = "Error: " . $sql . "<br>" . $conn->error;
    }
}
?>

<!DOCTYPE html>
<html>
<head>
    <title>Add Admin</title>
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
            <?php if (isset($message)) echo "<p>$message</p>"; ?>
        </div>
        <h1>Add New Admin</h1>
        <form method="POST" action="">
            <label>Username:</label>
            <input type="text" name="username" required>

            <label>Email:</label>
            <input type="email" name="email" required>

            <label>Password:</label>
            <input type="password" name="password" required>

            <label>Role:</label>
            <select name="role">
                <option value="Administrator">Administrator</option>
                <option value="Operator">Operator</option>
                <option value="Manager">Manager</option>
            </select>

            <button type="submit">Add Admin</button>
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