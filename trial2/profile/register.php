<?php
include 'db.php';
session_start();

$debug = ""; // Initialize debug messages

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $username = $_POST['username'];
    $email = $_POST['email'];
    $password = $_POST['password'];
    $role = $_POST['role']; // Capture the selected role

    // Check if the email already exists
    $checkEmailSql = "SELECT * FROM user WHERE email = ?";
    $checkEmailStmt = $conn->prepare($checkEmailSql);
    $checkEmailStmt->bind_param('s', $email);
    $checkEmailStmt->execute();
    $result = $checkEmailStmt->get_result();

    if ($result->num_rows > 0) {
        $debug = "Email already exists. Please use a different email.";
    } else {
        // Insert the user into the database
        $insertSql = "INSERT INTO user (username, email, password, role) VALUES (?, ?, ?, ?)";
        $stmt = $conn->prepare($insertSql);
        $stmt->bind_param('ssss', $username, $email, $password, $role);

        if ($stmt->execute()) {
            // Automatically log in the user after successful registration
            $_SESSION['user_id'] = $conn->insert_id;
            $_SESSION['role'] = $role; 
            header("Location: ../createQuiz/join.php"); 
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
    <title>Create Account</title>
    <link rel="stylesheet" href="styling.css">
    <link rel="icon" type="image/x-icon" href="../ideas.png">

    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #7e6df3; 
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
        }

        .register-container {
            background: #fff;
            padding: 40px;
            border-radius: 12px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
            width: 600px;
            display: flex;
            flex-direction: row;
            align-items: stretch;
            justify-content: space-between;
        }

        .form-container, .info-container {
            flex: 1;
        }

        .form-container {
            padding: 20px;
        }

        .info-container {
            padding: 20px;
            background-color: #9f8cca; 
            border-left: 1px solid #7c589a;
            color: #fff;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
        }

        .info-container h1 {
            color: #fff;
            font-size: 1.5rem;
            margin-bottom: 20px;
        }

        .info-container p {
            font-size: 0.9rem;
            margin-bottom: 10px;
            text-align: center;
        }

        .info-container ul {
            list-style: none;
            padding: 0;
            margin: 0;
        }

        .info-container ul li {
            margin-bottom: 10px;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .info-container ul li span {
            font-size: 1.2rem;
        }

        h1 {
            margin-bottom: 20px;
            color: #533b7c;
            font-size: 1.8rem;
        }

        .debug-messages {
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
            padding: 10px;
            margin-bottom: 20px;
            border-radius: 5px;
            font-size: 0.9rem;
        }

        label {
            display: block;
            margin-bottom: 8px;
            text-align: left;
            color: #533b7c; 
            font-weight: bold;
            font-size: 0.9rem;
        }

        input, select {
            width: calc(100% - 20px);
            padding: 12px;
            margin-bottom: 20px;
            border: 1px solid #ccc;
            border-radius: 5px;
            font-size: 1rem;
            background: #f9f9f9;
            box-sizing: border-box;
        }

        input:focus, select:focus {
            border-color: #ab3cfc;
            box-shadow: 0 0 5px rgba(171, 60, 252, 0.3);
        }

        button {
            width: 100%;
            padding: 12px;
            background-color: #533b7c;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 1rem;
            font-weight: bold;
            transition: background-color 0.3s ease;
        }

        button:hover {
            background-color: #3e394c; 
        }

        .sign-in-link {
            text-align: center;
            margin-top: 10px;
            font-size: 0.9rem;
        }

        .sign-in-link a {
            color: #ab3cfc;
            text-decoration: none;
            font-weight: bold;
        }

        .sign-in-link a:hover {
            text-decoration: underline;
        }

        .password-container {
            position: relative;
        }

        .password-container input {
            padding-right: 60px;
        }

        .show-password {
            position: absolute;
            right: 10px;
            top: 50%;
            transform: translateY(-50%);
            font-size: 0.9rem;
            color: #533b7c;
            cursor: pointer;
        }

        .show-password:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="register-container">
        <div class="form-container">
            <h1>Create Account</h1>

            <?php if (!empty($debug)): ?>
                <div class="debug-messages">
                    <?php echo $debug; ?>
                </div>
            <?php endif; ?>

            <form method="POST" action="">
                <label>Username:</label>
                <input type="text" name="username" required>

                <label>Email Address:</label>
                <input type="email" name="email" required>

                <label>Create Password:</label>
                    <div class="password-container">
                    <input type="password" id="password" name="password" placeholder="Password" required>
                    <span class="show-password" onclick="togglePassword()">Show</span>
                </div>

                <label>Role:</label>
                <select name="role" required>
                    <option value="student">Student</option>
                    <option value="educator">Educator</option>
                    <option value="content manager">Content Manager</option>
                </select>

                <button type="submit">Create Account</button>
            </form>

            <div class="sign-in-link">
                Already have an account? <a href="login.php">Sign in</a>
            </div>
        </div>
        <div class="info-container">
            <h1>Start creating quizzes</h1>
            <p style="text-align:left">Quiz Craze provides you with the option of creating your own form and allows you to give feedback to those who participate in your quiz. Below are some of the features we offer:</p>
            <br>
            <ul>
                <li><span>🎯</span> Track your quiz performance</li>
                <li><span>🎓</span> Increase your knowledge</li>
                <li><span>💬</span> Provide feedback</li>
            </ul>
        </div>
    </div>

    <script>
        function togglePassword() {
            const passwordField = document.getElementById('password');
            const toggleText = document.querySelector('.show-password');
            if (passwordField.type === 'password') {
                passwordField.type = 'text';
                toggleText.textContent = 'Hide';
            } else {
                passwordField.type = 'password';
                toggleText.textContent = 'Show';
            }
        }
    </script>
</body>
</html>
