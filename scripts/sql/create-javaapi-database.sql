CREATE DATABASE IF NOT EXISTS `JavaApi`;

CREATE USER IF NOT EXISTS 'javaapi'@'localhost' IDENTIFIED BY 'javaapi_dev';
GRANT ALL PRIVILEGES ON `JavaApi`.* TO 'javaapi'@'localhost';
FLUSH PRIVILEGES;
