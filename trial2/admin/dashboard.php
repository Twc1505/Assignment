<?php
// dashboard.php - Dashboard Page
include 'db.php';
session_start();

if (!isset($_SESSION['user_id'])) {
    header('Location: login.php');
    exit();
}

$user_id = $_SESSION['user_id'];
$role = $_SESSION['role'];
?>
<!DOCTYPE html>
<html>
<head>
    <title>Dashboard</title>
    <link rel="stylesheet" href="styling.css">
    <style>
        /* Widgets and Main Content Styling */
        .dashboard-widgets {
            display: flex;
            flex-wrap: wrap;
            gap: 20px;
            margin-top: 20px;
        }

        .widget {
            background: #ffffff;
            border: 1px solid #e0e0e0;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            flex: 1;
            min-width: 250px;
            padding: 20px;
            text-align: center;
            transition: box-shadow 0.3s;
        }

        .widget:hover {
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
        }

        .widget h2 {
            margin-top: 0;
            font-size: 1.5em;
            color: #333333;
        }

        .widget p {
            margin: 10px 0;
            color: #777777;
        }

        .widget .button {
            display: inline-block;
            margin-top: 10px;
            padding: 10px 20px;
            background-color: #3498db;
            color: #ffffff;
            text-decoration: none;
            border-radius: 5px;
            font-weight: bold;
            transition: background-color 0.3s ease;
        }

        .widget .button:hover {
            background-color: #2980b9;
        }
    </style>
</head>
<body>
    <div id="sidebar" class="collapsed">
        <h2>Cin Cai</h2>
        <button id="toggle-sidebar">☰</button>
        <ul>
            <li><a href="dashboard.php" class="active">Dashboard</a></li>
            <li><a href="admin.php">Admins</a></li>
            <li><a href="user.php">Users</a></li>
            <li><a href="quiz.php">Quizzes</a></li>
            <li><a href="logout.php">Logout</a></li>
        </ul>
    </div>
    <div id="main-content">
        <h1>Welcome to the Dashboard</h1>
        <div class="dashboard-widgets">
            <div class="widget">
                <h2>Admins</h2>
                <p>Manage admins and their roles.</p>
                <a href="admin.php" class="button">Manage Admins</a>
            </div>
            <div class="widget">
                <h2>Users</h2>
                <p>View and manage system users.</p>
                <a href="user.php" class="button">Manage Users</a>
            </div>
            <div class="widget">
                <h2>Quizzes</h2>
                <p>Manage quizzes and their settings.</p>
                <a href="quiz.php" class="button">Manage Quizzes</a>
            </div>
        </div>
    </div>
    <script>
        const toggleSidebar = document.getElementById('toggle-sidebar');
        const sidebar = document.getElementById('sidebar');

        // Apply saved state on page load
        document.addEventListener('DOMContentLoaded', () => {
            const isCollapsed = localStorage.getItem('sidebar-collapsed') === 'true';
            if (isCollapsed) {
                sidebar.classList.add('collapsed');
            }
        });

        // Toggle sidebar state and save to local storage
        toggleSidebar.addEventListener('click', () => {
            sidebar.classList.toggle('collapsed');
            const isCollapsed = sidebar.classList.contains('collapsed');
            localStorage.setItem('sidebar-collapsed', isCollapsed);
        });
    </script>
</body>
</html>
