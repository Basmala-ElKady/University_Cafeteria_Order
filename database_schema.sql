-- University Cafeteria Order System Database Schema
-- Version: 1.0.0
-- Created: January 9, 2025
-- Author: Basmala ElKady

-- Create database
CREATE DATABASE IF NOT EXISTS cafeteria_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use database
USE cafeteria_system;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    type ENUM('student', 'staff') DEFAULT 'student',
    points INT DEFAULT 50,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_type (type)
);

-- Create menu_items table
CREATE TABLE IF NOT EXISTS menu_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(50),
    image VARCHAR(255),
    available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_available (available)
);

-- Create orders table
CREATE TABLE IF NOT EXISTS orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    items JSON NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    tax DECIMAL(10,2) NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    status ENUM('pending', 'processing', 'preparing', 'ready', 'completed', 'cancelled') DEFAULT 'pending',
    points_earned INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- Create loyalty_transactions table
CREATE TABLE IF NOT EXISTS loyalty_transactions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    transaction_type ENUM('earned', 'redeemed', 'bonus', 'penalty') NOT NULL,
    points INT NOT NULL,
    description TEXT,
    order_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_created_at (created_at)
);

-- Create system_settings table
CREATE TABLE IF NOT EXISTS system_settings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_setting_key (setting_key)
);

-- Create audit_logs table
CREATE TABLE IF NOT EXISTS audit_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NULL,
    action VARCHAR(100) NOT NULL,
    table_name VARCHAR(50),
    record_id INT,
    old_values JSON,
    new_values JSON,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_table_name (table_name),
    INDEX idx_created_at (created_at)
);

-- Insert default system settings
INSERT INTO system_settings (setting_key, setting_value, description) VALUES
('points_per_dollar', '1', 'Points earned per dollar spent'),
('points_value', '0.20', 'Cash value of one point'),
('min_redemption_points', '10', 'Minimum points required for redemption'),
('tax_rate', '0.10', 'Tax rate as decimal (10%)'),
('default_student_points', '50', 'Default points for new students'),
('system_name', 'University Cafeteria Order System', 'System name'),
('system_version', '1.0.0', 'System version'),
('maintenance_mode', 'false', 'Maintenance mode flag');

-- Insert sample users
INSERT INTO users (name, email, password, type, points) VALUES
('John Doe', 'student@example.com', 'password123', 'student', 50),
('Jane Smith', 'staff@cafeteria.com', 'password123', 'staff', 0),
('Test User', 'test@test.com', 'test', 'student', 50),
('Admin User', 'admin@admin.com', 'admin', 'staff', 0);

-- Insert sample menu items
INSERT INTO menu_items (name, description, price, category, image) VALUES
('Chicken Burger', 'Delicious grilled chicken burger with fresh vegetables', 15.99, 'Main Course', 'https://via.placeholder.com/300x200/FF6B6B/FFFFFF?text=Chicken+Burger'),
('Caesar Salad', 'Fresh romaine lettuce with caesar dressing', 8.99, 'Salad', 'https://via.placeholder.com/300x200/4ECDC4/FFFFFF?text=Caesar+Salad'),
('French Fries', 'Crispy golden french fries', 5.99, 'Side', 'https://via.placeholder.com/300x200/FFD93D/FFFFFF?text=French+Fries'),
('Pizza Margherita', 'Classic pizza with tomato sauce, mozzarella, and basil', 12.99, 'Main Course', 'https://via.placeholder.com/300x200/FF6B6B/FFFFFF?text=Pizza+Margherita'),
('Chicken Wings', 'Spicy buffalo chicken wings with ranch dip', 9.99, 'Appetizer', 'https://via.placeholder.com/300x200/FF9800/FFFFFF?text=Chicken+Wings'),
('Vegetable Soup', 'Hearty vegetable soup with fresh ingredients', 6.99, 'Soup', 'https://via.placeholder.com/300x200/4CAF50/FFFFFF?text=Vegetable+Soup'),
('Chocolate Cake', 'Rich chocolate cake with chocolate frosting', 4.99, 'Dessert', 'https://via.placeholder.com/300x200/8D6E63/FFFFFF?text=Chocolate+Cake'),
('Coffee', 'Freshly brewed coffee', 2.99, 'Beverage', 'https://via.placeholder.com/300x200/795548/FFFFFF?text=Coffee'),
('Orange Juice', 'Fresh squeezed orange juice', 3.99, 'Beverage', 'https://via.placeholder.com/300x200/FF9800/FFFFFF?text=Orange+Juice'),
('Grilled Chicken', 'Tender grilled chicken breast with herbs', 13.99, 'Main Course', 'https://via.placeholder.com/300x200/FF6B6B/FFFFFF?text=Grilled+Chicken');

-- Insert sample orders
INSERT INTO orders (user_id, items, subtotal, tax, total, status, points_earned) VALUES
(1, '[{"id": 1, "name": "Chicken Burger", "price": 15.99, "quantity": 1}]', 15.99, 1.60, 17.59, 'completed', 1),
(1, '[{"id": 2, "name": "Caesar Salad", "price": 8.99, "quantity": 1}]', 8.99, 0.90, 9.89, 'pending', 0),
(3, '[{"id": 4, "name": "Pizza Margherita", "price": 12.99, "quantity": 1}]', 12.99, 1.30, 14.29, 'processing', 1);

-- Insert sample loyalty transactions
INSERT INTO loyalty_transactions (user_id, transaction_type, points, description, order_id) VALUES
(1, 'earned', 1, 'Points earned from order #1', 1),
(1, 'bonus', 50, 'Welcome bonus for new student', NULL),
(3, 'earned', 1, 'Points earned from order #3', 3);

-- Create views for common queries
CREATE VIEW user_order_summary AS
SELECT 
    u.id as user_id,
    u.name,
    u.email,
    u.points as current_points,
    COUNT(o.id) as total_orders,
    COALESCE(SUM(o.total), 0) as total_spent,
    COALESCE(SUM(o.points_earned), 0) as total_points_earned
FROM users u
LEFT JOIN orders o ON u.id = o.user_id
GROUP BY u.id, u.name, u.email, u.points;

CREATE VIEW daily_sales_summary AS
SELECT 
    DATE(created_at) as sale_date,
    COUNT(*) as total_orders,
    SUM(total) as total_revenue,
    AVG(total) as average_order_value,
    SUM(points_earned) as total_points_earned
FROM orders
WHERE status = 'completed'
GROUP BY DATE(created_at)
ORDER BY sale_date DESC;

CREATE VIEW popular_menu_items AS
SELECT 
    mi.id,
    mi.name,
    mi.category,
    mi.price,
    COUNT(*) as order_count,
    SUM(JSON_EXTRACT(o.items, '$[*].quantity')) as total_quantity
FROM menu_items mi
JOIN orders o ON JSON_CONTAINS(o.items, JSON_OBJECT('id', mi.id))
WHERE o.status = 'completed'
GROUP BY mi.id, mi.name, mi.category, mi.price
ORDER BY order_count DESC;

-- Create stored procedures
DELIMITER //

CREATE PROCEDURE GetUserPoints(IN user_email VARCHAR(100))
BEGIN
    SELECT points FROM users WHERE email = user_email;
END //

CREATE PROCEDURE UpdateUserPoints(IN user_email VARCHAR(100), IN points_change INT)
BEGIN
    UPDATE users 
    SET points = points + points_change 
    WHERE email = user_email;
END //

CREATE PROCEDURE GetOrderHistory(IN user_email VARCHAR(100))
BEGIN
    SELECT 
        o.id,
        o.items,
        o.total,
        o.status,
        o.points_earned,
        o.created_at
    FROM orders o
    JOIN users u ON o.user_id = u.id
    WHERE u.email = user_email
    ORDER BY o.created_at DESC;
END //

DELIMITER ;

-- Create triggers for audit logging
DELIMITER //

CREATE TRIGGER users_audit_insert
AFTER INSERT ON users
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (user_id, action, table_name, record_id, new_values)
    VALUES (NEW.id, 'INSERT', 'users', NEW.id, JSON_OBJECT(
        'name', NEW.name,
        'email', NEW.email,
        'type', NEW.type,
        'points', NEW.points
    ));
END //

CREATE TRIGGER users_audit_update
AFTER UPDATE ON users
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (user_id, action, table_name, record_id, old_values, new_values)
    VALUES (NEW.id, 'UPDATE', 'users', NEW.id, 
        JSON_OBJECT(
            'name', OLD.name,
            'email', OLD.email,
            'type', OLD.type,
            'points', OLD.points
        ),
        JSON_OBJECT(
            'name', NEW.name,
            'email', NEW.email,
            'type', NEW.type,
            'points', NEW.points
        )
    );
END //

CREATE TRIGGER orders_audit_insert
AFTER INSERT ON orders
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (user_id, action, table_name, record_id, new_values)
    VALUES (NEW.user_id, 'INSERT', 'orders', NEW.id, JSON_OBJECT(
        'items', NEW.items,
        'total', NEW.total,
        'status', NEW.status,
        'points_earned', NEW.points_earned
    ));
END //

CREATE TRIGGER orders_audit_update
AFTER UPDATE ON orders
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (user_id, action, table_name, record_id, old_values, new_values)
    VALUES (NEW.user_id, 'UPDATE', 'orders', NEW.id,
        JSON_OBJECT(
            'status', OLD.status,
            'total', OLD.total
        ),
        JSON_OBJECT(
            'status', NEW.status,
            'total', NEW.total
        )
    );
END //

DELIMITER ;

-- Create indexes for performance
CREATE INDEX idx_orders_user_status ON orders(user_id, status);
CREATE INDEX idx_orders_created_status ON orders(created_at, status);
CREATE INDEX idx_loyalty_user_type ON loyalty_transactions(user_id, transaction_type);
CREATE INDEX idx_audit_user_action ON audit_logs(user_id, action);
CREATE INDEX idx_audit_created_at ON audit_logs(created_at);

-- Grant permissions (adjust as needed for your environment)
-- GRANT ALL PRIVILEGES ON cafeteria_system.* TO 'cafeteria_user'@'%';
-- FLUSH PRIVILEGES;

-- End of schema
