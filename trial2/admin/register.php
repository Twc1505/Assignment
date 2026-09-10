<?php
include 'db.php';
session_start();

$debug = ""; // Initialize debug messages

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $username = $_POST['username'];
    $email = $_POST['email'];
    $password = $_POST['password'];

    // Check if the email already exists
    $checkEmailSql = "SELECT * FROM admin WHERE email = ?";
    $checkEmailStmt = $conn->prepare($checkEmailSql);
    $checkEmailStmt->bind_param('s', $email);
    $checkEmailStmt->execute();
    $result = $checkEmailStmt->get_result();

    if ($result->num_rows > 0) {
        $debug = "Email already exists. Please use a different email.";
    } else {
        // Insert the user into the database
        $insertSql = "INSERT INTO admin (username, email, password, role) VALUES (?, ?, ?, ?)";
        $stmt = $conn->prepare($insertSql);
        $role = "administrator"; // Default role for new users
        $stmt->bind_param('ssss', $username, $email, $password, $role);

        if ($stmt->execute()) {
            // Automatically log in the user after successful registration
            $_SESSION['user_id'] = $conn->insert_id; // Store the new user ID in session
            $_SESSION['role'] = $role; // Set the user's role
            header("Location: dashboard.php"); // Redirect to the dashboard
            exit();
        } else {
            $debug = "Error: Could not register. Please try again.";
        }
    }
}
?>

<!DOCTYPE html>
<html>
<head>
    <title>Register</title>
    <link rel="stylesheet" href="styling.css">
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f4f4;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
        }

        .register-container {
            background: white;
            padding: 30px 40px;
            border-radius: 8px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
            width: 400px;
            text-align: center;
        }

        h1 {
            margin-bottom: 20px;
            color: #333;
        }

        .debug-messages {
            background-color: #f8d7da; /* Light red background */
            color: #721c24; /* Dark red text */
            border: 1px solid #f5c6cb; /* Border matches background */
            padding: 10px;
            margin-bottom: 20px;
            border-radius: 5px;
            font-size: 0.9rem;
            text-align: left;
        }

        label {
            display: block;
            margin-bottom: 8px;
            text-align: left;
            color: #555;
            font-weight: bold;
            font-size: 0.9rem;
        }

        input {
            width: calc(100% - 20px);
            padding: 12px;
            margin-bottom: 20px;
            border: 1px solid #ccc;
            border-radius: 5px;
            font-size: 1rem;
            background: #f9f9f9;
            box-sizing: border-box;
        }

        input:focus {
            border-color: #3498db;
            box-shadow: 0 0 5px rgba(52, 152, 219, 0.3);
        }

        button {
            width: 100%;
            padding: 12px;
            background-color: #3498db;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 1rem;
            font-weight: bold;
            transition: background-color 0.3s ease;
        }

        button:hover {
            background-color: #1abc9c;
        }
    </style>
</head>
<body>
    <div class="register-container">
        <h1>Register</h1>

        <?php if (!empty($debug)): ?>
            <div class="debug-messages">
                <?php echo $debug; ?>
            </div>
        <?php endif; ?>

        <form method="POST" action="">
            <label>Username:</label>
            <input type="text" name="username" required>

            <label>Email:</label>
            <input type="email" name="email" required>

            <label>Password:</label>
            <input type="password" name="password" required>

            <button type="submit">Register</button>
        </form>
    </div>
</body>
</html>
