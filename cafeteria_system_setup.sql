-- 1. Create the database
DROP DATABASE cafeteria_system;
CREATE DATABASE cafeteria_system;
USE cafeteria_system;
DROP TABLE Students; 
-- 2. Create Students table
CREATE TABLE Students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    loyalty_points DECIMAL(10,2) DEFAULT 0.00
);
CREATE TABLE menu_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    available BOOLEAN DEFAULT TRUE
);
-- 3. Create Items table
CREATE TABLE Items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    available BOOLEAN DEFAULT TRUE
);

-- 4. Create Orders table
CREATE TABLE Orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2),
    FOREIGN KEY (student_id) REFERENCES Students(student_id)
);

-- 5. Create OrderDetails table
CREATE TABLE OrderDetails (
    detail_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT,
    item_id INT,
    quantity INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (item_id) REFERENCES Items(item_id)
);

-- 6. Insert sample students
INSERT INTO Students (name, email, password, loyalty_points) VALUES
('Alice Johnson', 'alice@example.com', 'pass123', 50.00),
('Bob Smith', 'bob@example.com', 'bobpass', 30.00),
('Charlie Brown', 'charlie@example.com', 'charliepass', 20.00);

INSERT INTO menu_items (name, price, available) VALUES
('Burger', 5.50, TRUE),
('Pizza Slice', 3.00, TRUE),
('Coffee', 2.00, TRUE),
('Sandwich', 4.00, TRUE),
('Juice', 2.50, TRUE);
-- 7. Insert sample items
INSERT INTO Items (name, price, available) VALUES
('Burger', 5.50, TRUE),
('Pizza Slice', 3.00, TRUE),
('Coffee', 2.00, TRUE),
('Sandwich', 4.00, TRUE),
('Juice', 2.50, TRUE);
