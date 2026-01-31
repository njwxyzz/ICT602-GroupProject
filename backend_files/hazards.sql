-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jan 31, 2026 at 10:16 PM
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
(6, 3.148, 101.692, 'Building Fire - Stay Away'),
(8, 6.4485, 100.2776, 'Flash Flood: UiTM Main Gate Entrance'),
(9, 6.4297, 100.2698, 'Accident: Near Arau KTM Station'),
(10, 6.44, 100.198, 'Fire: Bush fire along Kangar Highway'),
(11, 6.452, 100.285, 'Construction: Road closure near Matrikulasi'),
(12, 6.435, 100.25, 'Obstruction: Car breakdown blocking left lane'),
(13, 6.46, 100.21, 'Heavy Rain: Visibility low at Pauh Putra'),
(14, 3.0697, 101.5037, 'Flash Flood: UiTM Shah Alam Seksyen 7'),
(15, 3.1579, 101.7116, 'Traffic Jam: Massive Gridlock near KLCC'),
(16, 5.3546, 100.4079, 'Accident: Car breakdown on Penang Bridge (Mid Span)'),
(17, 2.223, 102.214, 'Fire: Shoplot fire near Dataran Pahlawan Melaka'),
(18, 6.1254, 102.2381, 'Heavy Rain: Thunderstorm warning in Kota Bharu'),
(19, 1.4927, 110.36, 'Construction: Roadworks near UiTM Sarawak (Samarahan)'),
(20, 5.9788, 116.0753, 'Landslide: Hill slope warning near Kota Kinabalu');

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
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
