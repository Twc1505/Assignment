<?php
// admin.php - Manage Admins
include 'db.php';

session_start();

// Redirect to login page if not logged in
if (!isset($_SESSION['user_id'])) {
    header('Location: login.php');
    exit();
}

// Handle delete request
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['delete_id'])) {
    $delete_id = intval($_POST['delete_id']);
    $delete_sql = "DELETE FROM admin WHERE adminId = ?";
    $stmt = $conn->prepare($delete_sql);
    $stmt->bind_param("i", $delete_id);

    if ($stmt->execute()) {
        echo "<script>alert('Admin deleted successfully!'); window.location.href = 'admin.php';</script>";
    } else {
        echo "<script>alert('Error deleting admin.');</script>";
    }

    $stmt->close();
}

// Fetch all admins
$sql = "SELECT * FROM admin";
$result = $conn->query($sql);
$admins = [];
if ($result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $admins[] = $row;
    }
}
?>

<!DOCTYPE html>
<html>
<head>
    <title>Manage Admins</title>
    <link rel="stylesheet" href="styling.css">
    <style>
        .button {
            display: inline-block;
            padding: 8px 12px;
            background-color: #3498db;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            border: none;
            cursor: pointer;
            transition: background-color 0.3s;
        }

        .button:hover {
            background-color: #2980b9;
        }

        .button.delete {
            background-color: #e74c3c;
        }

        .button.delete:hover {
            background-color: #c0392b;
        }
    </style>
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
        <h1>Manage Admins</h1>

        <button onclick="location.href='addAdmin.php'" class="button" style="margin-bottom: 20px;">Add Admin</button>

        <h2>Admin List</h2>
        <table>
            <tr>
                <th>ID</th>
                <th>Username</th>
                <th>Email</th>
                <th>Role</th>
                <th>Actions</th>
            </tr>
            <?php if (count($admins) > 0): ?>
                <?php foreach ($admins as $admin): ?>
                    <tr>
                        <td><?php echo $admin['adminId']; ?></td>
                        <td><?php echo htmlspecialchars($admin['username']); ?></td>
                        <td><?php echo htmlspecialchars($admin['email']); ?></td>
                        <td><?php echo htmlspecialchars($admin['role']); ?></td>
                        <td>
                            <a href="edit_admin.php?adminId=<?= $admin['adminId'] ?>" class="button">Edit</a>
                            <form method="POST" action="" style="display: inline; margin: 0; padding: 0; border: none;">
                                <input type="hidden" name="delete_id" value="<?= $admin['adminId'] ?>">
                                <button type="submit" class="button delete" onclick="return confirm('Are you sure you want to delete this admin?');">Delete</button>
                            </form>
                        </td>
                    </tr>
                <?php endforeach; ?>
            <?php else: ?>
                <tr>
                    <td colspan="5">No admins found</td>
                </tr>
            <?php endif; ?>
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
