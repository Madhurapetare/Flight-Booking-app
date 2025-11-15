-- create DB
CREATE DATABASE flight_app CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE flight_app;

-- users table
CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- flights table
CREATE TABLE flights (
  id INT AUTO_INCREMENT PRIMARY KEY,
  airline VARCHAR(100) NOT NULL,
  flight_number VARCHAR(50) NOT NULL,
  src VARCHAR(100) NOT NULL,
  dest VARCHAR(100) NOT NULL,
  depart_datetime DATETIME NOT NULL,
  arrive_datetime DATETIME NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  seats_available INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- bookings table
CREATE TABLE bookings (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  flight_id INT NOT NULL,
  seats INT NOT NULL,
  total_price DECIMAL(10,2) NOT NULL,
  booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (flight_id) REFERENCES flights(id) ON DELETE CASCADE
);

-- sample flights
INSERT INTO flights (airline, flight_number, src, dest, depart_datetime, arrive_datetime, price, seats_available)
VALUES
('Air India','AI-101','Mumbai','Delhi','2025-12-20 07:00:00','2025-12-20 09:00:00',4500.00,50),
('IndiGo','6E-250','Mumbai','Bangalore','2025-12-21 10:30:00','2025-12-21 12:00:00',3200.00,70),
('Vistara','UK-404','Delhi','Chennai','2025-12-22 14:00:00','2025-12-22 16:30:00',5200.00,40);
