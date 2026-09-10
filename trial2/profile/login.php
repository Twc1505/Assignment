<?php
include 'db.php';
session_start();

$debug = ""; // Initialize debug messages

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email = $_POST['email'];
    $password = $_POST['password'];

    $sql = "SELECT * FROM user WHERE email = '$email'";
    $result = $conn->query($sql);

    if ($result->num_rows === 1) {
        $user = $result->fetch_assoc();

        if ($password === $user['password']) {
            // Set session and redirect
            $_SESSION['user_id'] = $user['userId'];
            $_SESSION['role'] = $user['role'];
            header("Location: ../createQuiz/join.php");
            exit();
        } else {
            $debug = "Invalid credentials.";
        }
    } else {
        $debug = "Invalid credentials.";
    }
}
?>

<!DOCTYPE html>
<html>
<head>
    <title>Login</title>
    <link rel="stylesheet" href="styling.css">
    <link rel="icon" type="image/x-icon" href="../ideas.png">
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #7e6df3; /* Background color from provided palette */
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
        }

        .login-container {
            background: white;
            padding: 40px;
            border-radius: 12px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
            width: 380px;
            text-align: left;
        }

        h1 {
            margin-bottom: 8px;
            color: #533b7c; /* Dark purple for title */
            font-size: 1.8rem;
            font-weight: bold;
        }

        p {
            color: #7c589a;
            font-size: 1rem;
            margin-bottom: 20px;
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
            color: #533b7c;
            font-weight: bold;
            font-size: 0.9rem;
        }

        input {
            width: calc(100% - 20px);
            padding: 10px;
            margin-bottom: 15px;
            border: 1px solid #ccc;
            border-radius: 5px;
            font-size: 1rem;
            background: #f9f9f9;
        }

        input:focus {
            border-color: #ab3cfc;
            box-shadow: 0 0 5px rgba(171, 60, 252, 0.3);
        }

        .password-container {
            position: relative;
        }

        .show-password {
            position: absolute;
            right: 10px;
            top: 50%;
            transform: translateY(-50%);
            font-size: 0.9rem;
            color: #533b7c;
            cursor: pointer;
            user-select: none;
        }

        .show-password:hover {
            text-decoration: underline;
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

        .sign-up-link {
            font-size: 0.9rem;
            text-align: center;
            margin-top: 20px;
        }

        .sign-up-link a {
            color: #ab3cfc;
            text-decoration: none;
            font-weight: bold;
        }

        .sign-up-link a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="login-container">
        <h1>Sign in</h1>
        <p>Login to access our features</p>

        <?php if (!empty($debug)): ?>
            <div class="debug-messages">
                <?php echo $debug; ?>
            </div>
        <?php endif; ?>

        <form method="POST" action="">
            <label for="email">Email address</label>
            <input type="email" id="email" name="email" placeholder="Email" required>

            <label for="password">Password</label>
            <div class="password-container">
                <input type="password" id="password" name="password" placeholder="Password" required>
                <span class="show-password" onclick="togglePassword()">Show</span>
            </div>

            <button type="submit">Sign in</button>
        </form>

        <div class="sign-up-link">
            Don't have an account? <a href="register.php">Create your account now.</a>
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
