-- MySQL dump 10.13  Distrib 5.1.53, for apple-darwin10.5.0 (i386)
--
-- Host: localhost    Database: rdftests
-- ------------------------------------------------------
-- Server version	5.1.52

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `Dept`
--

DROP TABLE IF EXISTS `Dept`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Dept` (
  `deptno` int(11) DEFAULT NULL,
  `dname` varchar(30) DEFAULT NULL,
  `loc` varchar(100) DEFAULT NULL,
  UNIQUE KEY `deptno` (`deptno`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Dept`
--

LOCK TABLES `Dept` WRITE;
/*!40000 ALTER TABLE `Dept` DISABLE KEYS */;
INSERT INTO `Dept` VALUES (10,'APPSERVER','NEW YORK');
/*!40000 ALTER TABLE `Dept` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Referenced`
--

DROP TABLE IF EXISTS `Referenced`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Referenced` (
  `id` int(11) NOT NULL,
  `col1` int(11) NOT NULL,
  `col2` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `col1` (`col1`,`col2`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Referenced`
--

LOCK TABLES `Referenced` WRITE;
/*!40000 ALTER TABLE `Referenced` DISABLE KEYS */;
INSERT INTO `Referenced` VALUES (1,2,3),(4,5,6);
/*!40000 ALTER TABLE `Referenced` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Referencing`
--

DROP TABLE IF EXISTS `Referencing`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Referencing` (
  `id` int(11) NOT NULL,
  `refcol1` int(11) NOT NULL,
  `refcol2` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `refcol1` (`refcol1`,`refcol2`),
  CONSTRAINT `Referencing_ibfk_1` FOREIGN KEY (`refcol1`, `refcol2`) REFERENCES `Referenced` (`col1`, `col2`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Referencing`
--

LOCK TABLES `Referencing` WRITE;
/*!40000 ALTER TABLE `Referencing` DISABLE KEYS */;
INSERT INTO `Referencing` VALUES (10,2,3),(11,5,6);
/*!40000 ALTER TABLE `Referencing` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-12-09 18:51:08
