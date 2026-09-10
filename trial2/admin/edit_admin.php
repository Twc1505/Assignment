<?php
// edit_admin.php - Edit Admin Page
include 'db.php';
session_start();

// Redirect to login page if not logged in
if (!isset($_SESSION['user_id'])) {
    header('Location: login.php');
    exit();
}

// Check if `adminId` is provided
if (!isset($_GET['adminId'])) {
    header('Location: admin.php');
    exit();
}

$adminId = intval($_GET['adminId']);
$message = "";

// Fetch current admin details
$sql = "SELECT * FROM admin WHERE adminId = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $adminId);
$stmt->execute();
$result = $stmt->get_result();
$admin = $result->fetch_assoc();

if (!$admin) {
    echo "<script>alert('Admin not found!'); window.location.href = 'admin.php';</script>";
    exit();
}

// Handle form submission
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $username = $_POST['username'];
    $email = $_POST['email'];
    $role = $_POST['role'];

    // Update admin details
    $update_sql = "UPDATE admin SET username = ?, email = ?, role = ? WHERE adminId = ?";
    $update_stmt = $conn->prepare($update_sql);
    $update_stmt->bind_param("sssi", $username, $email, $role, $adminId);

    if ($update_stmt->execute()) {
        $message = "Admin updated successfully.";
    } else {
        $message = "Failed to update admin: " . $conn->error;
    }
}
?>

<!DOCTYPE html>
<html>
<head>
    <title>Edit Admin</title>
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
        <h1>Edit Admin</h1>
        <form method="POST" action="">
            <label>Username:</label>
            <input type="text" name="username" value="<?= htmlspecialchars($admin['username']) ?>" required>

            <label>Email:</label>
            <input type="email" name="email" value="<?= htmlspecialchars($admin['email']) ?>" required>

            <label>Role:</label>
            <select name="role">
                <option value="Administrator" <?= $admin['role'] === 'administrator' ? 'selected' : '' ?>>Administrator</option>
                <option value="Operator" <?= $admin['role'] === 'operator' ? 'selected' : '' ?>>Operator</option>
                <option value="Manager" <?= $admin['role'] === 'manager' ? 'selected' : '' ?>>Manager</option>
            </select>

            <button type="submit">Update Admin</button>
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
