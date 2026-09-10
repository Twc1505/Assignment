-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Generation Time: Dec 15, 2024 at 10:41 PM
-- Server version: 8.3.0
-- PHP Version: 8.2.18

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `rwdd`
--

-- --------------------------------------------------------

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
CREATE TABLE IF NOT EXISTS `admin` (
  `adminId` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `role` varchar(50) NOT NULL,
  PRIMARY KEY (`adminId`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `admin`
--

INSERT INTO `admin` (`adminId`, `username`, `password`, `email`, `role`) VALUES
(1, 'test123', '123', 'test@mail.com', 'administrator'),
(6, 'unago', '123', 'unago@mail.com', 'Operator'),
(10, 'test', '123', 'test123@mail.com', 'administrator'),
(11, 'una', '123', 'una@mail.com', 'Operator');

-- --------------------------------------------------------

--
-- Table structure for table `attempt`
--

DROP TABLE IF EXISTS `attempt`;
CREATE TABLE IF NOT EXISTS `attempt` (
  `attemptId` int NOT NULL AUTO_INCREMENT,
  `attemptTime` timestamp NOT NULL,
  `score` int NOT NULL,
  `comment` varchar(1000) NOT NULL,
  `userId` int NOT NULL,
  `quizId` int NOT NULL,
  `correctQuestions` json NOT NULL,
  PRIMARY KEY (`attemptId`),
  KEY `userId` (`userId`),
  KEY `quizId` (`quizId`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `attempt`
--

INSERT INTO `attempt` (`attemptId`, `attemptTime`, `score`, `comment`, `userId`, `quizId`, `correctQuestions`) VALUES
(28, '2024-12-14 05:57:36', 0, '', 1, 78, '{\"68\": false}'),
(29, '2024-12-15 17:40:18', 0, '', 1, 72, '{\"68\": false}');

-- --------------------------------------------------------

--
-- Table structure for table `question`
--

DROP TABLE IF EXISTS `question`;
CREATE TABLE IF NOT EXISTS `question` (
  `questionId` int NOT NULL AUTO_INCREMENT,
  `questionText` varchar(1000) NOT NULL,
  `questionType` varchar(50) NOT NULL,
  `quizId` int NOT NULL,
  `answerText` varchar(1000) NOT NULL,
  `point` int NOT NULL,
  PRIMARY KEY (`questionId`),
  KEY `question_ibfk_1` (`quizId`)
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `question`
--

INSERT INTO `question` (`questionId`, `questionText`, `questionType`, `quizId`, `answerText`, `point`) VALUES
(61, 'testase', 'checkbox', 72, '[{\"text\":\"no\",\"isCorrect\":true},{\"text\":\"yes\",\"isCorrect\":false},{\"text\":\"yes\",\"isCorrect\":false}]', 213),
(68, 'What is a pineapple?', 'multiple-choice', 78, '[{\"text\":\"Apple\",\"isCorrect\":false},{\"text\":\"Orange\",\"isCorrect\":false},{\"text\":\"Pineapple\",\"isCorrect\":true}]', 10);

-- --------------------------------------------------------

--
-- Table structure for table `quiz`
--

DROP TABLE IF EXISTS `quiz`;
CREATE TABLE IF NOT EXISTS `quiz` (
  `quizId` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `description` varchar(1000) NOT NULL,
  `userId` int NOT NULL,
  `created_at` date NOT NULL,
  PRIMARY KEY (`quizId`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB AUTO_INCREMENT=80 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `quiz`
--

INSERT INTO `quiz` (`quizId`, `title`, `description`, `userId`, `created_at`) VALUES
(72, 'test', '123', 1, '2024-10-09'),
(78, 'This is a pineapple', 'pineapple', 1, '2024-12-14'),
(79, 'This is a apple', 'apple', 6, '2024-12-14');

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `userId` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(50) NOT NULL,
  PRIMARY KEY (`userId`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`userId`, `username`, `email`, `password`, `role`) VALUES
(1, 'eheh', 'eheh@mail.com', '1234', 'student'),
(2, 'steve', 'steve@mail.com', '123', 'teacher'),
(3, 'job', 'job@mail.com', '123', 'student'),
(5, 'testing', 'testing@mail.com', '123', 'student'),
(6, 'ide', 'ide@mail.com', 'ide', 'educator'),
(8, 'eheh', 'moreheh@mail.com', 'eheh', 'student');

--
-- Constraints for dumped tables
--

--
-- Constraints for table `attempt`
--
ALTER TABLE `attempt`
  ADD CONSTRAINT `attempt_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  ADD CONSTRAINT `attempt_ibfk_2` FOREIGN KEY (`quizId`) REFERENCES `quiz` (`quizId`) ON DELETE RESTRICT ON UPDATE RESTRICT;

--
-- Constraints for table `question`
--
ALTER TABLE `question`
  ADD CONSTRAINT `question_ibfk_1` FOREIGN KEY (`quizId`) REFERENCES `quiz` (`quizId`) ON DELETE RESTRICT;

--
-- Constraints for table `quiz`
--
ALTER TABLE `quiz`
  ADD CONSTRAINT `quiz_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`) ON DELETE RESTRICT ON UPDATE RESTRICT;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
