-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jan 30, 2026 at 05:21 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `crowdtrack_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `hazards`
--

CREATE TABLE `hazards` (
  `id` int(11) NOT NULL,
  `lat` double NOT NULL,
  `lng` double NOT NULL,
  `description` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `hazards`
--

INSERT INTO `hazards` (`id`, `lat`, `lng`, `description`) VALUES
(1, 3.1408, 101.6932, 'Danger: Road Construction'),
(2, 3.145, 101.695, 'Flood at Section 7 Entrance'),
(3, 3.15, 101.7, 'Fire reported near Main Hall'),
(4, 3.138, 101.69, 'Car Accident at Junction'),
(5, 3.142, 101.698, 'Heavy Rain and Flash Flood'),
(6, 3.148, 101.692, 'Building Fire - Stay Away');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `hazards`
--
ALTER TABLE `hazards`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `hazards`
--
ALTER TABLE `hazards`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
