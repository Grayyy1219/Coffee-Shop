-- Coffee Shop sample database and seed data
-- Run with: mysql -u root -p < docs/database.sql

DROP DATABASE IF EXISTS coffee_shop;
CREATE DATABASE coffee_shop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE coffee_shop;

-- Users table for staff login
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'staff'
);

INSERT INTO users (username, password, role) VALUES
    ('owner', 'owner123', 'owner'),
    ('barista', 'coffee!', 'staff'),
    ('cashier', 'payme', 'staff');

-- Menu items available for ordering
CREATE TABLE menu_items (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(8,2) NOT NULL
);

INSERT INTO menu_items (code, name, category, price) VALUES
    ('CF001', 'Espresso', 'Coffee', 3.00),
    ('CF002', 'Latte', 'Coffee', 4.25),
    ('CF003', 'Cappuccino', 'Coffee', 4.50),
    ('CF004', 'Cold Brew', 'Coffee', 4.75),
    ('TE001', 'Chai Tea', 'Tea', 3.75),
    ('TE002', 'Green Tea', 'Tea', 3.25),
    ('PA001', 'Butter Croissant', 'Pastry', 2.95),
    ('PA002', 'Blueberry Muffin', 'Pastry', 3.10),
    ('FD001', 'Avocado Toast', 'Food', 6.50),
    ('FD002', 'Breakfast Sandwich', 'Food', 6.95);

-- Orders header table
CREATE TABLE orders (
    order_id VARCHAR(36) PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    paid BOOLEAN NOT NULL DEFAULT FALSE,
    total DECIMAL(10,2) NOT NULL
);

-- Individual line items per order
CREATE TABLE order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    item_code VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    line_total DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_item FOREIGN KEY (item_code) REFERENCES menu_items(code)
);

-- Sample orders reflecting different statuses
INSERT INTO orders (order_id, customer_name, created_at, status, paid, total) VALUES
    ('ORD-1001', 'Alice', '2024-05-01 08:30:00', 'PENDING', FALSE, 8.25),
    ('ORD-1002', 'Bob', '2024-05-01 08:35:00', 'SERVED', FALSE, 6.00),
    ('ORD-1003', 'Walk-in', '2024-05-01 08:45:00', 'PAID', TRUE, 10.95);

INSERT INTO order_items (order_id, item_code, quantity, line_total) VALUES
    ('ORD-1001', 'CF002', 1, 4.25),
    ('ORD-1001', 'PA001', 1, 2.95),
    ('ORD-1001', 'TE002', 1, 3.25),

    ('ORD-1002', 'CF001', 2, 6.00),

    ('ORD-1003', 'CF004', 1, 4.75),
    ('ORD-1003', 'FD002', 1, 6.95);

-- Simple sales view to support reporting if desired
CREATE OR REPLACE VIEW v_daily_sales AS
SELECT DATE(created_at) AS sale_date,
       SUM(total) AS gross_total,
       SUM(CASE WHEN paid THEN total ELSE 0 END) AS paid_total,
       COUNT(*) AS order_count
FROM orders
GROUP BY DATE(created_at)
ORDER BY sale_date;
