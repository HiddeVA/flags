-- phpMyAdmin SQL Dump
-- version 4.7.4
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Generation Time: Jan 22, 2018 at 03:24 PM
-- Server version: 5.7.19
-- PHP Version: 5.6.31

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `flags`
--

-- --------------------------------------------------------

--
-- Table structure for table `country`
--

DROP TABLE IF EXISTS `country`;
CREATE TABLE IF NOT EXISTS `country` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=45 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `country`
--

INSERT INTO `country` (`id`, `name`) VALUES
(1, 'Netherlands'),
(2, 'Belgium'),
(3, 'Palau'),
(4, 'Myanmar'),
(5, 'Panama'),
(6, 'China'),
(13, 'Japan'),
(11, 'Vietnam'),
(37, 'Czech Republic'),
(24, 'Costa Rica'),
(20, 'Indonesia'),
(25, 'Thailand'),
(26, 'Armenia'),
(27, 'Austria'),
(29, 'Bangladesh'),
(39, 'Tanzania'),
(34, 'Poland'),
(35, 'Germany'),
(38, 'Saint Lucia'),
(40, 'Trinidad and Tobago'),
(44, 'Syria');

-- --------------------------------------------------------

--
-- Table structure for table `flag`
--

DROP TABLE IF EXISTS `flag`;
CREATE TABLE IF NOT EXISTS `flag` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `country_id` int(11) NOT NULL,
  `background` varchar(15) DEFAULT NULL,
  `flagstyle` set('simple','horizontal','vertical','block','diagonal','triangle') NOT NULL DEFAULT 'simple',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=37 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `flag`
-- If background is empty or NULL, it means the background is white
--

INSERT INTO `flag` (`id`, `country_id`, `background`, `flagstyle`) VALUES
(1, 1, '', 'horizontal'),
(2, 2, '', 'vertical'),
(3, 3, '0x40e0d0ff', 'simple'),
(4, 4, '', 'horizontal'),
(5, 5, '', 'block'),
(6, 6, '0xff0000', 'simple'),
(7, 11, '0xff0000ff', 'simple'),
(9, 13, '0xffffffff', 'simple'),
(31, 37, NULL, 'triangle'),
(11, 16, NULL, 'horizontal'),
(15, 20, NULL, 'horizontal'),
(19, 25, NULL, 'horizontal'),
(18, 24, NULL, 'horizontal'),
(20, 26, NULL, 'horizontal'),
(21, 27, NULL, 'horizontal'),
(23, 29, '0x336633ff', 'simple'),
(36, 44, NULL, 'horizontal'),
(33, 40, 'RED', 'diagonal'),
(28, 34, NULL, 'horizontal'),
(29, 35, NULL, 'horizontal'),
(32, 38, 'TURQUOISE', 'triangle');

-- --------------------------------------------------------

--
-- Table structure for table `flagcolours`
--

DROP TABLE IF EXISTS `flagcolours`;
CREATE TABLE IF NOT EXISTS `flagcolours` (
  `flag_id` int(11) NOT NULL,
  `colour_code` varchar(15) NOT NULL,
  `colour_order` int(11) NOT NULL,
  `row_width` float DEFAULT NULL,
  PRIMARY KEY (`flag_id`,`colour_order`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `flagcolours`
-- Some flags need an extra parameter, for example the orientation of diagonal lines on a flag.
-- If colour_order is 0, it indicates that this is not a colour, but an extra parameter
-- The value of this parameter is stored in colour_code
--

INSERT INTO `flagcolours` (`flag_id`, `colour_code`, `colour_order`, `row_width`) VALUES
(1, 'RED', 1, NULL),
(1, 'BLUE', 3, NULL),
(1, 'WHITE', 2, NULL),
(2, 'BLACK', 1, NULL),
(2, 'YELLOW', 2, NULL),
(2, 'RED', 3, NULL),
(4, 'YELLOW', 1, NULL),
(4, 'GREEN', 2, NULL),
(4, 'RED', 3, NULL),
(35, '0x000000ff', 3, NULL),
(35, '0xffffffff', 2, NULL),
(35, '0xb31a1aff', 1, NULL),
(15, '0xff0000ff', 1, NULL),
(15, '0xffffffff', 2, NULL),
(18, '0x0000ffff', 1, NULL),
(18, '0xffffffff', 2, NULL),
(18, '0xff0000ff', 3, NULL),
(18, '0xff0000ff', 4, NULL),
(18, '0xffffffff', 5, NULL),
(18, '0x0000ffff', 6, NULL),
(19, '0xff0000ff', 1, NULL),
(19, '0xffffffff', 2, NULL),
(19, '0x0000ffff', 3, NULL),
(19, '0x0000ffff', 4, NULL),
(19, '0xffffffff', 5, NULL),
(19, '0xff0000ff', 6, NULL),
(20, '0xde1818ff', 1, NULL),
(20, '0x0000ffff', 2, NULL),
(20, '0xe4d400ff', 3, NULL),
(21, '0xde1818ff', 1, NULL),
(21, '0xffffffff', 2, NULL),
(21, '0xde1818ff', 3, NULL),
(28, '0xffffffff', 1, NULL),
(28, '0xcc2a2aff', 2, NULL),
(29, '0x000000ff', 1, NULL),
(29, '0xff0000ff', 2, NULL),
(29, '0xffd400ff', 3, NULL),
(33, 'WHITE', 1, 8),
(33, 'BLACK', 2, 25),
(33, '-1.45', 0, NULL),
(33, 'WHITE', 3, 8),
(36, '0x990000ff', 1, NULL),
(36, '0xffffffff', 2, NULL),
(36, '0x000000ff', 3, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `flagsymbol`
--

DROP TABLE IF EXISTS `flagsymbol`;
CREATE TABLE IF NOT EXISTS `flagsymbol` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `flag_id` int(11) NOT NULL,
  `symboltype` set('Circle','Star','Crescent','Block','TriangleUp','TriangleSide') NOT NULL DEFAULT 'Star',
  `colour` varchar(15) NOT NULL,
  `size` float DEFAULT '20',
  `xPosition` float DEFAULT '0',
  `yPosition` float DEFAULT '0',
  `orientation` float DEFAULT '0',
  `view_order` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=55 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `flagsymbol`
-- All types of flag-objects are put in this table
-- The orientation column is used to hold extra parameters
-- For stars and crescents, it indicates how they are rotated
-- For blocks and triangles it is the ratio between the width and height
--

INSERT INTO `flagsymbol` (`id`, `flag_id`, `symboltype`, `colour`, `size`, `xPosition`, `yPosition`, `orientation`, `view_order`) VALUES
(2, 4, 'Star', 'WHITE', 80, 0, 0, 0, 0),
(3, 5, 'Star', 'BLUE', 30, 50, 30, 0, 0),
(4, 5, 'Star', 'RED', 30, 150, 90, 0, 0),
(5, 6, 'Star', 'YELLOW', 40, 30, 30, 0, 0),
(6, 6, 'Star', 'YELLOW', 12, 68, 12, 5.4978, 0),
(7, 6, 'Star', 'YELLOW', 12, 77, 25, 0.7854, 0),
(8, 6, 'Star', 'YELLOW', 12, 77, 41, 0, 0),
(9, 6, 'Star', 'YELLOW', 12, 68, 54, 5.4978, 0),
(13, 3, 'Circle', '0xffff00ff', 60, 100, 60, 0, 0),
(14, 9, 'Circle', '0xff0000ff', 65, 100, 60, 0, 0),
(20, 7, 'Star', '0xffff00ff', 70, 100, 60, 0, 0),
(45, 31, 'TriangleSide', 'BLUE', 120, 0, 0, 0.8, 1),
(44, 31, 'Block', 'RED', 200, 0, 60, 0.3, 0),
(23, 23, 'Circle', '0xcc3333ff', 70, 84, 56, 0, 0),
(46, 32, 'TriangleUp', 'WHITE', 60, 70, 15, 1.5, 0),
(47, 32, 'TriangleUp', '', 46, 77, 36, 1.5, 1),
(48, 32, 'TriangleUp', 'YELLOW', 60, 70, 60, 0.75, 2),
(49, 5, 'Block', 'RED', 100, 100, 0, 0.6, 0),
(50, 5, 'Block', 'RED', 100, 100, 0, 0.6, 0),
(51, 5, 'Block', 'BLUE', 100, 0, 60, 0.6, 0),
(53, 36, 'Star', '0x003300ff', 30, 66, 60, 0, 1),
(54, 36, 'Star', '0x003300ff', 30, 132, 60, 0, 2);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
