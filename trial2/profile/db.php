<?php
// db.php - Database Connection File

$servername = "localhost"; // Change to your server name if not localhost
$username = "root";       // Change to your database username
$password = "";          // Change to your database password
$dbname = "rwdd";         // Change to your database name

// Create a new connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check the connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

?>
