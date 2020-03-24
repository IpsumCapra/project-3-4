-- phpMyAdmin SQL Dump
-- version 5.0.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Gegenereerd op: 24 mrt 2020 om 17:15
-- Serverversie: 10.4.11-MariaDB
-- PHP-versie: 7.4.2

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `bank`
--

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `admin`
--

CREATE TABLE `admin` (
  `adminID` int(60) NOT NULL,
  `atmID` int(60) NOT NULL,
  `userID` int(60) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Gegevens worden geëxporteerd voor tabel `admin`
--

INSERT INTO `admin` (`adminID`, `atmID`, `userID`) VALUES
(1, 1, 1);

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `atm`
--

CREATE TABLE `atm` (
  `atmID` int(60) NOT NULL,
  `storedAmount` int(5) NOT NULL,
  `noteCountFive` int(50) NOT NULL,
  `noteCountTen` int(50) NOT NULL,
  `noteCountTwenty` int(50) NOT NULL,
  `noteCountFifty` int(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Gegevens worden geëxporteerd voor tabel `atm`
--

INSERT INTO `atm` (`atmID`, `storedAmount`, `noteCountFive`, `noteCountTen`, `noteCountTwenty`, `noteCountFifty`) VALUES
(1, 1500, 50, 25, 25, 10);

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `user`
--

CREATE TABLE `user` (
  `cardID` varchar(60) NOT NULL,
  `userBalance` int(5) NOT NULL,
  `userPin` varchar(4) NOT NULL,
  `depositTime` date DEFAULT NULL,
  `withdrawalTime` date DEFAULT NULL,
  `blocked` tinyint(1) DEFAULT NULL,
  `pinAttempts` int(11) NOT NULL,
  `userID` int(60) NOT NULL,
  `name` varchar(60) NOT NULL,
  `accountNumber` int(60) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Gegevens worden geëxporteerd voor tabel `user`
--

INSERT INTO `user` (`cardID`, `userBalance`, `userPin`, `depositTime`, `withdrawalTime`, `blocked`, `pinAttempts`, `userID`, `name`, `accountNumber`) VALUES
('1', 1000, '1234', '0000-00-00', '0000-00-00', 0, 0, 1, 'test', 21312321);

--
-- Indexen voor geëxporteerde tabellen
--

--
-- Indexen voor tabel `admin`
--
ALTER TABLE `admin`
  ADD PRIMARY KEY (`adminID`,`atmID`,`userID`);

--
-- Indexen voor tabel `atm`
--
ALTER TABLE `atm`
  ADD PRIMARY KEY (`atmID`);

--
-- Indexen voor tabel `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`userID`),
  ADD UNIQUE KEY `userPin` (`userPin`);

--
-- AUTO_INCREMENT voor geëxporteerde tabellen
--

--
-- AUTO_INCREMENT voor een tabel `admin`
--
ALTER TABLE `admin`
  MODIFY `adminID` int(60) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT voor een tabel `atm`
--
ALTER TABLE `atm`
  MODIFY `atmID` int(60) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
